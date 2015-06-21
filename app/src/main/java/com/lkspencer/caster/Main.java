package com.lkspencer.caster;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.common.api.ResultCallback;
import com.lkspencer.caster.adapters.DIDLAdapter;
import com.lkspencer.caster.adapters.DeviceAdapter;
import com.lkspencer.caster.upnp.BrowseRegistryListener;
import com.lkspencer.caster.upnp.DeviceDisplay;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.android.FixedAndroidLogHandler;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Timer;


public class Main extends AppCompatActivity implements NavigationDrawerFragment.INavigationDrawerCallbacks {
  public static final String TAG = "Main";
  public int position = 0;
  public ImageButton pause;
  public LinearLayout playback;
  public SeekBar seekBar;
  public MediaPlayer mediaPlayer;

  private NavigationDrawerFragment mNavigationDrawerFragment;
  private CharSequence mTitle;
  private DeviceAdapter deviceAdapter;
  private DIDLAdapter didlAdapter;
  private String currentId = "0";
  private Stack<String> ids = new Stack<>();
  private Service service;

  private BrowseRegistryListener registryListener;
  private AndroidUpnpService upnpService;
  private ServiceConnection serviceConnection = new ServiceConnection() {
    public void onServiceConnected(ComponentName className, IBinder service) {
      upnpService = (AndroidUpnpService) service;

      // Clear the list
      deviceAdapter.clear();

      // Get ready for future device advertisements
      registryListener = new BrowseRegistryListener(Main.this, deviceAdapter);
      upnpService.getRegistry().addListener(registryListener);

      // Now add all devices to the list we already know about
      for (Device device : upnpService.getRegistry().getDevices()) {
        registryListener.deviceAdded(device);
      }

      // Search asynchronously for all devices, they will respond soon
      upnpService.getControlPoint().search();
    }

    public void onServiceDisconnected(ComponentName className) { upnpService = null; }
  };

  @Override protected void onCreate(Bundle savedInstanceState) {
    initializeListView();
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    FragmentManager fragmentManager = getSupportFragmentManager();
    fragmentManager
        .beginTransaction()
        .replace(R.id.container, PlaceholderFragment.newInstance("fragment_main", this))
        .commit();

    // setup the UPnP service to find available media devices
    org.seamless.util.logging.LoggingUtil.resetRootHandler(new FixedAndroidLogHandler());
    getApplicationContext().bindService(
        new Intent(this, AndroidUpnpServiceImpl.class),
        serviceConnection,
        Context.BIND_AUTO_CREATE
    );

    mediaPlayer = new MediaPlayer(this);
    WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
    if (!wifi.isWifiEnabled()){
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setMessage(R.string.no_wifi);
      builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
        {} @Override public void onClick(DialogInterface dialog, int which) {
          dialog.dismiss();
          mediaPlayer.teardown();
          Main.this.finish();
        }
      });
      AlertDialog dialog = builder.create();
      dialog.show();
    }

    mTitle = getTitle();
    mediaPlayer.mediaRouter = MediaRouter.getInstance(getApplicationContext());
    mediaPlayer.mediaRouteSelector = new MediaRouteSelector
            .Builder()
            .addControlCategory(CastMediaControlIntent.categoryForCast(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID))
            .build();
    initializeMenu();
  }

  @Override public void onNavigationDrawerItemSelected(int position) {
    this.position = position;
    ListView media_items = (ListView)findViewById(R.id.media_items);
    if (media_items == null) {
      FragmentManager fragmentManager = getSupportFragmentManager();
      fragmentManager
          .beginTransaction()
          .replace(R.id.container, PlaceholderFragment.newInstance("fragment_mediaitems", this))
          .commit();
    }
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    if (!mNavigationDrawerFragment.isDrawerOpen()) {
      // Only show items in the action bar relevant to this screen
      // if the drawer is not showing. Otherwise, let the drawer
      // decide what to show in the action bar.
      getMenuInflater().inflate(R.menu.main, menu);
      MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
      MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
      mediaRouteActionProvider.setRouteSelector(mediaPlayer.mediaRouteSelector);
      restoreActionBar();
      return true;
    }
    return super.onCreateOptionsMenu(menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    return super.onOptionsItemSelected(item);
  }

  @Override protected void onStart() {
    super.onStart();
    mediaPlayer.start();
  }

  @Override protected void onStop() {
    //setSelectedDevice(null);
    mediaPlayer.stop();
    super.onStop();
  }

  @Override public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
    int action = event.getAction();
    int keyCode = event.getKeyCode();
    switch (keyCode) {
      case KeyEvent.KEYCODE_VOLUME_UP:
        if (action == KeyEvent.ACTION_DOWN) {
          mediaPlayer.increaseVolume();
        }
        return true;
      case KeyEvent.KEYCODE_VOLUME_DOWN:
        if (action == KeyEvent.ACTION_DOWN) {
          mediaPlayer.decreaseVolume();
        }
        return true;
      case KeyEvent.KEYCODE_BACK:
        if (action == KeyEvent.ACTION_DOWN) {
          ListView classes = (ListView)findViewById(R.id.media_items);
          if (classes != null && classes.getChildCount() > 0) {
            TextView tv = (TextView)classes.getChildAt(0);
            if ("Back...".equalsIgnoreCase(tv.getText().toString())) {
              classes.performItemClick(tv, 0, didlAdapter.getItemId(0));
              return true;
            }
          }
        }
      default:
        return super.dispatchKeyEvent(event);
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (upnpService != null) {
      upnpService.getRegistry().removeListener(registryListener);
    }
    // This will stop the UPnP service if nobody else is bound to it
    getApplicationContext().unbindService(serviceConnection);
  }



  public void onFragmentInflated(View v) {
    pause = (ImageButton) v.findViewById(R.id.pause);
    if (pause != null) {
      pause.setOnClickListener(new View.OnClickListener() {
        {
        }

        @Override
        public void onClick(View v) {
          if (mediaPlayer.paused) {
            pause.setImageResource(android.R.drawable.ic_media_pause);
          } else {
            pause.setImageResource(android.R.drawable.ic_media_play);
          }
          mediaPlayer.pausePlayback(seekBar);
        }
      });
    }
    ImageButton stop = (ImageButton) v.findViewById(R.id.stop);
    if (stop != null) {
      stop.setOnClickListener(new View.OnClickListener() {
        {
        }

        @Override
        public void onClick(View v) {
          mediaPlayer.stopPlayback(Main.this);
        }
      });
    }
    playback = (LinearLayout) v.findViewById(R.id.playback);
    seekBar = (SeekBar) v.findViewById(R.id.seekBar);
    if (seekBar != null) {
      seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
        private int progress;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
          this.progress = progress;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
          mediaPlayer.seekPlayback(progress);
        }
      });
    }
    ListView media_items = (ListView) v.findViewById(R.id.media_items);
    if (media_items != null) {
      media_items.setAdapter(didlAdapter);
      media_items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        {} @Override public void onItemClick(AdapterView<?> parent, View view, final int itemClickedPosition, long id) {
          DIDLObject didl = (DIDLObject) parent.getAdapter().getItem(itemClickedPosition);
          if (itemClickedPosition > 0 && didl instanceof Item) {
            Item video = (Item) didl;
            List<Res> resources = video.getResources();
            boolean started = false;
            for (Res r : resources) {
              String url = r.getValue();
              String title = video.getTitle();
              String contentType = "video/mpeg";
              MediaMetadata mMediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
              mMediaMetadata.putString(MediaMetadata.KEY_TITLE, title);
              final MediaInfo data = new MediaInfo.Builder(url)
                  .setContentType(contentType)
                  .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                  .setMetadata(mMediaMetadata)
                  .build();
              if (!started && mediaPlayer.apiClient != null && mediaPlayer.mRemoteMediaPlayer != null) {
                try {
                  started = true;
                  mediaPlayer.mRemoteMediaPlayer.load(mediaPlayer.apiClient, data, true).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                    {} @Override public void onResult(RemoteMediaPlayer.MediaChannelResult result) {
                      if (result.getStatus().isSuccess()) {
                        if (seekBar != null) {
                          seekBar.setMax((int) mediaPlayer.mRemoteMediaPlayer.getMediaInfo().getStreamDuration());
                        }
                        pause.setImageResource(android.R.drawable.ic_media_pause);
                        playback.setVisibility(View.VISIBLE);
                        //playing = true;
                        mediaPlayer.paused = false;
                        Log.d(Main.TAG, "Media loaded successfully");

                        mediaPlayer.progress = new Timer("progress");
                        mediaPlayer.setupTimerTask(seekBar);
                        mediaPlayer.progress.schedule(mediaPlayer.progressUpdater, 0, 500);
                      }
                    }
                  });

                } catch (IllegalStateException e) {
                  Log.e(Main.TAG, "Problem occurred with media during loading", e);
                } catch (Exception e) {
                  Log.e(Main.TAG, "Problem opening media during loading", e);
                }
              } else {
                // not connected to chromecast. attempt to play file on android device.
                if (!started) {
                  started = true;
                  Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                  intent.setDataAndType(Uri.parse(url), "video/mpeg");
                  startActivity(intent);
                }
              }

            }
            return;
          }
          if (itemClickedPosition == 0 && ids.size() > 0) {
            currentId = ids.pop();
          } else {
            ids.push(currentId);
            currentId = didl.getId();
          }
          didlAdapter.clear();
          //deviceAdapter.clear();
          Browse b = new Browse(service, currentId, BrowseFlag.DIRECT_CHILDREN) {
            @Override
            public void received(ActionInvocation actionInvocation, DIDLContent didl) {
              if (ids.size() > 0) {
                Item i = new Item();
                i.setId("0");
                i.setTitle("Back...");
                didlAdapter.add(i);
              }
              for (Container container : didl.getContainers()) {
                didlAdapter.add(container);
              }
              for (Item item : didl.getItems()) {
                didlAdapter.add(item);
              }
            }

            @Override
            public void updateStatus(Status status) {
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
            }
          };
          b.setControlPoint(upnpService.getControlPoint());
          b.run();
        }
      });
      DeviceDisplay dd = deviceAdapter.getItem(this.position);
      service = dd.getService();
      Browse b = new Browse(service, "0", BrowseFlag.DIRECT_CHILDREN) {
        @Override public void received(ActionInvocation actionInvocation, DIDLContent didl) {
          didlAdapter.clear();
          for (Container container : didl.getContainers()) {
            String title = container.getTitle();
            if (title != null) {
              didlAdapter.add(container);
            }
          }
        }
        @Override public void updateStatus(Status status) { }
        @Override public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) { }
      };
      b.setControlPoint(upnpService.getControlPoint());
      b.run();
    }
  }

  public void restoreActionBar() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
      actionBar.setDisplayShowTitleEnabled(true);
      actionBar.setTitle(mTitle);
    }
  }

  public void hidePlayback() {
    if (playback != null) {
      playback.setVisibility(View.GONE);
    }
    if (pause != null) {
      pause.setImageResource(android.R.drawable.ic_media_play);
    }
  }

  public void initializeListView() {
    didlAdapter = new DIDLAdapter(this, android.R.layout.simple_list_item_1, android.R.id.text1, new ArrayList<DIDLObject>());
  }

  public void initializeMenu() {
    deviceAdapter = new DeviceAdapter(this, android.R.layout.simple_list_item_1, android.R.id.text1, new ArrayList<DeviceDisplay>());
    // Set up the drawer.
    mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
    mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), deviceAdapter);
  }

}
