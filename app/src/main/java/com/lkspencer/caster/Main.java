package com.lkspencer.caster;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.lkspencer.caster.adapters.ClassAdapter;
import com.lkspencer.caster.adapters.TopicAdapter;
import com.lkspencer.caster.adapters.VideoAdapter;
import com.lkspencer.caster.datamodels.ClassDataModel;
import com.lkspencer.caster.datamodels.TopicDataModel;
import com.lkspencer.caster.datamodels.VideoDataModel;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class Main extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks, IVideoRepositoryCallback {

  private NavigationDrawerFragment mNavigationDrawerFragment;
  private CharSequence mTitle;
  private MediaRouter mediaRouter;
  private MediaRouteSelector mediaRouteSelector;
  private CastDevice selectedDevice;
  private GoogleApiClient apiClient;
  private boolean applicationStarted;
  public static final String TAG = "Main";
  private int classId;
  private int topicId;
  private int year;
  private int month;
  private int main_position = 0;
  private static final double VOLUME_INCREMENT = 0.05;
  private boolean monthSelected = false;
  private boolean yearSelected = false;
  //private boolean playing = false;
  private boolean paused = true;
  private String sessionId;
  private RemoteMediaPlayer mRemoteMediaPlayer;
  private ImageButton pause;
  private LinearLayout playback;
  private SeekBar seekBar;
  private CasterChannel casterChannel;
  private Timer progress;
  private final MediaRouter.Callback mediaRouterCallback = new MediaRouter.Callback() {

    @Override public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo route) {
      CastDevice device = CastDevice.getFromBundle(route.getExtras());
      setSelectedDevice(device);
    }

    @Override public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo route) {
      teardown();
      setSelectedDevice(null);
    }

  };
  private final Cast.Listener castClientListener = new Cast.Listener() {
    @Override public void onApplicationDisconnected(int statusCode) { }

    @Override public void onVolumeChanged() { }
  };
  private final GoogleApiClient.ConnectionCallbacks connectionCallback = new GoogleApiClient.ConnectionCallbacks() {
    @Override public void onConnected(Bundle bundle) {
      try {
        Cast.CastApi.launchApplication(apiClient, CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID, false).setResultCallback(connectionResultCallback);
      } catch (Exception e) {
        Log.e(TAG, "Failed to launch application", e);
      }
    }

    @Override public void onConnectionSuspended(int i) { }

  };
  private final GoogleApiClient.OnConnectionFailedListener connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
    {}
    @Override public void onConnectionFailed(ConnectionResult connectionResult) {
      Toast.makeText(Main.this, "Failed to connect " + connectionResult.toString(), Toast.LENGTH_SHORT).show();
      setSelectedDevice(null);
    }
  };
  private final ResultCallback<Cast.ApplicationConnectionResult> connectionResultCallback = new ResultCallback<Cast.ApplicationConnectionResult>() {
    {} @Override public void onResult(Cast.ApplicationConnectionResult result) {
      Status status = result.getStatus();
      if (status.isSuccess()) {
        applicationStarted = true;
        sessionId = result.getSessionId();

        mRemoteMediaPlayer = new RemoteMediaPlayer();
        mRemoteMediaPlayer.setOnStatusUpdatedListener(new RemoteMediaPlayer.OnStatusUpdatedListener() {
          {} @Override public void onStatusUpdated() {
            /*
            MediaStatus mediaStatus = mRemoteMediaPlayer.getMediaStatus();
            if (mediaStatus != null) {
              boolean isPlaying = mediaStatus.getPlayerState() == MediaStatus.PLAYER_STATE_PLAYING;
              if (!isPlaying && progress != null) {
              }
            }
            //*/
          }
        });

        mRemoteMediaPlayer.setOnMetadataUpdatedListener(new RemoteMediaPlayer.OnMetadataUpdatedListener() {
          {} @Override public void onMetadataUpdated() {
            /*
            MediaInfo mediaInfo = mRemoteMediaPlayer.getMediaInfo();
            if (mediaInfo != null) {
              MediaMetadata metadata = mediaInfo.getMetadata();
            }
            */
          }
        });

        try {
          Cast.CastApi.setMessageReceivedCallbacks(apiClient, mRemoteMediaPlayer.getNamespace(), mRemoteMediaPlayer);
        } catch (IOException e) {
          Log.e(TAG, "Exception while creating media channel", e);
        }
        mRemoteMediaPlayer
          .requestStatus(apiClient)
          .setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
            {} @Override public void onResult(RemoteMediaPlayer.MediaChannelResult result) {
                if (result.getStatus().isSuccess()) {
                  Log.e(TAG, "The status has been requested!");
                } else {
                  Log.e(TAG, "Failed to request status.");
                }
              }
            });
      } else {
        teardown();
      }
    }
  };
  private TimerTask progressUpdater;



  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
    mTitle = getTitle();

    // Set up the drawer.
    mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

    mediaRouter = MediaRouter.getInstance(getApplicationContext());
    mediaRouteSelector = new MediaRouteSelector.Builder().addControlCategory(CastMediaControlIntent.categoryForCast(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)).build();
    GregorianCalendar now = new GregorianCalendar();
    month = now.get(Calendar.MONTH) + 1;
    year = now.get(Calendar.YEAR);
  }

  @Override public void onNavigationDrawerItemSelected(int position) {
    this.classId = 0;
    this.topicId = 0;
    main_position = 0;
    // update the main content by replacing fragments
    FragmentManager fragmentManager = getSupportFragmentManager();
    fragmentManager
      .beginTransaction()
      .replace(R.id.container, PlaceholderFragment.newInstance(main_position))
      .commit();
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    if (!mNavigationDrawerFragment.isDrawerOpen()) {
      // Only show items in the action bar relevant to this screen
      // if the drawer is not showing. Otherwise, let the drawer
      // decide what to show in the action bar.
      getMenuInflater().inflate(R.menu.main, menu);
      MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
      MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
      mediaRouteActionProvider.setRouteSelector(mediaRouteSelector);
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
    mediaRouter.addCallback(mediaRouteSelector, mediaRouterCallback, MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
  }

  @Override protected void onStop() {
    //setSelectedDevice(null);
    mediaRouter.removeCallback(mediaRouterCallback);
    super.onStop();
  }

  @Override public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
    int action = event.getAction();
    int keyCode = event.getKeyCode();
    switch (keyCode) {
      case KeyEvent.KEYCODE_VOLUME_UP:
        if (action == KeyEvent.ACTION_DOWN) {
          if (mRemoteMediaPlayer != null) {
            double currentVolume = Cast.CastApi.getVolume(apiClient);
            if (currentVolume < 1.0) {
              try {
                Cast.CastApi.setVolume(apiClient, Math.min(currentVolume + VOLUME_INCREMENT, 1.0));
              } catch (Exception e) {
                Log.e(TAG, "unable to set volume", e);
              }
            }
          } else {
            Log.e(TAG, "dispatchKeyEvent - volume up");
          }
        }
        return true;
      case KeyEvent.KEYCODE_VOLUME_DOWN:
        if (action == KeyEvent.ACTION_DOWN) {
          if (mRemoteMediaPlayer != null) {
            double currentVolume = Cast.CastApi.getVolume(apiClient);
            if (currentVolume > 0.0) {
              try {
                Cast.CastApi.setVolume(apiClient, Math.max(currentVolume - VOLUME_INCREMENT, 0.0));
              } catch (Exception e) {
                Log.e(TAG, "unable to set volume", e);
              }
            }
          } else {
            Log.e(TAG, "dispatchKeyEvent - volume down");
          }
        }
        return true;
      default:
        return super.dispatchKeyEvent(event);
    }
  }



  private void setupTimerTask() {
    progressUpdater = new TimerTask() {
      {} @Override public void run() {
        if (seekBar == null || mRemoteMediaPlayer == null) return;

        seekBar.setProgress((int)mRemoteMediaPlayer.getApproximateStreamPosition());
      }
    };
  }

  public void onSectionAttached(int position) {
    VideoRepository vr = new VideoRepository(this, year, month);
    Integer[] params;
    switch (position) {
      case 0:
        params = new Integer[4];
        params[0] = VideoRepository.Actions.GET_CLASSES;
        params[1] = mNavigationDrawerFragment.getCurrentCurriculumId();
        vr.execute(params);
        mTitle = "Classes";
        break;
      case 1:
        params = new Integer[4];
        params[0] = VideoRepository.Actions.GET_TOPICS;
        params[1] = mNavigationDrawerFragment.getCurrentCurriculumId();
        params[2] = classId;
        vr.execute(params);
        mTitle = "Topics";
        break;
      case 2:
        params = new Integer[4];
        params[0] = VideoRepository.Actions.GET_VIDEOS;
        params[1] = mNavigationDrawerFragment.getCurrentCurriculumId();
        params[2] = classId;
        params[3] = topicId;
        vr.execute(params);
        mTitle = "Videos";
        break;
    }
    ActionBar actionBar = getSupportActionBar();
    actionBar.setTitle(mTitle);
  }

  private void SetSpinnerSelectedValue(Spinner spinner, String value) {
    if (value == null || "".equalsIgnoreCase(value)) return;

    SpinnerAdapter adapter = spinner.getAdapter();
    int count = adapter.getCount();
    for (int i = 0; i < count; i++) {
      if (value.equalsIgnoreCase((String)adapter.getItem(i))) {
        spinner.setSelection(i);
        break;
      }
    }
  }

  public String getMonth(int month) {
    return new DateFormatSymbols().getMonths()[month];
  }

  public int getMonthInt(String value) {
    Date date = null;
    try {
      date = new SimpleDateFormat("MMMM", Locale.ENGLISH).parse(value);
    } catch (ParseException e) {
      e.printStackTrace();
    }
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    return calendar.get(Calendar.MONTH) + 1;
  }

  public void onFragmentInflated(View v) {
    final Spinner year_filter = (Spinner)v.findViewById(R.id.year_filter);
    SetSpinnerSelectedValue(year_filter, String.valueOf(year));
    year_filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        year = Integer.parseInt((String) year_filter.getSelectedItem());
        if (yearSelected) {
          onSectionAttached(main_position);
        } else {
          yearSelected = true;
        }
      }

      @Override public void onNothingSelected(AdapterView<?> parent) { }
    });
    final Spinner month_filter = (Spinner)v.findViewById(R.id.month_filter);
    SetSpinnerSelectedValue(month_filter, getMonth(month - 1));
    month_filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        month = getMonthInt((String)parent.getItemAtPosition(position));
        if (monthSelected) {
          onSectionAttached(main_position);
        } else {
          monthSelected = true;
        }
      }

      @Override public void onNothingSelected(AdapterView<?> parent) { }
    });
    pause = (ImageButton)v.findViewById(R.id.pause);
    pause.setOnClickListener(new View.OnClickListener() {
      {} @Override public void onClick(View v) {
        if (mRemoteMediaPlayer != null /*&& mRemoteMediaPlayer.getStreamDuration() > 0 && mRemoteMediaPlayer.getMediaStatus() != null*/) {
          if (paused) {
            pause.setImageResource(android.R.drawable.ic_media_pause);
            mRemoteMediaPlayer.play(apiClient).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
              {} @Override public void onResult(RemoteMediaPlayer.MediaChannelResult result) {
                Status status = result.getStatus();
                if (status.isSuccess()) {
                  paused = false;
                  setupTimerTask();
                  progress = new Timer("progress");
                  progress.schedule(progressUpdater, 0, 500);
                } else {
                  Log.w(TAG, "Unable to toggle pause: " + status.getStatusCode());
                }
              }
            });
          } else {
            pause.setImageResource(android.R.drawable.ic_media_play);
            mRemoteMediaPlayer.pause(apiClient).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
              {} @Override public void onResult(RemoteMediaPlayer.MediaChannelResult result) {
                Status status = result.getStatus();
                if (!status.isSuccess()) {
                  progress.cancel();
                  progressUpdater.cancel();
                  Log.w(TAG, "Unable to toggle pause: " + status.getStatusCode());
                } else {
                  paused = true;
                }
              }
            });
          }
        }
      }
    });
    ImageButton stop = (ImageButton)v.findViewById(R.id.stop);
    stop.setOnClickListener(new View.OnClickListener() {
      {} @Override public void onClick(View v) {
        if (mRemoteMediaPlayer != null) {
          mRemoteMediaPlayer.stop(apiClient).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
            {} @Override public void onResult(RemoteMediaPlayer.MediaChannelResult result) {
              Status status = result.getStatus();
              if (status.isSuccess()) {
                progress.cancel();
                progressUpdater.cancel();
                playback.setVisibility(View.GONE);
                pause.setImageResource(android.R.drawable.ic_media_play);
                paused = true;
                //playing = false;
              } else {
                Log.w(TAG, "Unable to stop playback: " + status.getStatusCode());
              }
            }
          });
        } else {
          playback.setVisibility(View.GONE);
          pause.setImageResource(android.R.drawable.ic_media_play);
          paused = true;
          //playing = false;
        }
      }
    });
    playback = (LinearLayout)v.findViewById(R.id.playback);
    seekBar = (SeekBar)v.findViewById(R.id.seekBar);
    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      private int progress;

      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        this.progress = progress;
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) { }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {
        if (mRemoteMediaPlayer != null) {
          mRemoteMediaPlayer.seek(apiClient, progress);
        }
      }
    });
  }

  public void restoreActionBar() {
    ActionBar actionBar = getSupportActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    actionBar.setDisplayShowTitleEnabled(true);
    actionBar.setTitle(mTitle);
  }

  private void setSelectedDevice(CastDevice device) {
    Log.d(TAG, "setSelectedDevice: " + device);

    selectedDevice = device;

    if (selectedDevice != null) {
      try {
        stopApplication();
        disconnectApiClient();
        connectApiClient();
      } catch (IllegalStateException e) {
        Log.w(TAG, "Exception while connecting API client", e);
        disconnectApiClient();
      }
    } else {
      if (apiClient != null) {
        disconnectApiClient();
      }

      mediaRouter.selectRoute(mediaRouter.getDefaultRoute());
    }
  }

  private void connectApiClient() {
    Cast.CastOptions apiOptions = Cast.CastOptions.builder(selectedDevice, castClientListener).build();
    apiClient = new GoogleApiClient.Builder(this)
            .addApi(Cast.API, apiOptions)
            .addConnectionCallbacks(connectionCallback)
            .addOnConnectionFailedListener(connectionFailedListener)
            .build();
    apiClient.connect();
  }

  private void disconnectApiClient() {
    if (apiClient != null) {
      apiClient.disconnect();
      apiClient = null;
    }
  }

  private void stopApplication() {
    if (apiClient == null) return;

    if (applicationStarted) {
      Cast.CastApi.stopApplication(apiClient);
      applicationStarted = false;
    }
  }

  public void ProcessCurriculums(VideoRepository repository) { }

  public void ProcessClasses(VideoRepository repository) {
    LinearLayout filters = (LinearLayout)findViewById(R.id.filters);
    if (filters == null) {
      Toast.makeText(this, "An error occurred processing the filters.", Toast.LENGTH_LONG).show();
      return;
    }
    filters.setVisibility(View.VISIBLE);
    ListView classes = (ListView) findViewById(R.id.classes);
    if (classes == null) {
      Toast.makeText(this, "An error occurred processing the classes.", Toast.LENGTH_LONG).show();
      return;
    }
    ClassAdapter ca = new ClassAdapter(
      this,
      android.R.layout.simple_list_item_1,
      android.R.id.text1,
      repository.classDataModels);
    classes.setAdapter(ca);
    classes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      {} @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ClassDataModel c = (ClassDataModel) parent.getAdapter().getItem(position);
        Main.this.classId = c.ClassId;
        main_position = 1;
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager
          .beginTransaction()
          .replace(R.id.container, PlaceholderFragment.newInstance(main_position))
          .commit();
      }
    });
  }

  public void ProcessTopics(VideoRepository repository) {
    LinearLayout filters = (LinearLayout)findViewById(R.id.filters);
    if (filters == null) {
      Toast.makeText(this, "An error occurred processing the filters.", Toast.LENGTH_LONG).show();
      return;
    }
    filters.setVisibility(View.VISIBLE);
    ListView classes = (ListView) findViewById(R.id.classes);
    if (classes == null) {
      Toast.makeText(this, "An error occurred processing the classes.", Toast.LENGTH_LONG).show();
      return;
    }
    TopicAdapter ta = new TopicAdapter(
            this,
            android.R.layout.simple_list_item_1,
            android.R.id.text1,
            repository.topicDataModels);
    classes.setAdapter(ta);
    classes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      {} @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TopicDataModel t = (TopicDataModel) parent.getAdapter().getItem(position);
        Main.this.topicId = t.TopicId;
        main_position = 2;
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager
          .beginTransaction()
          .replace(R.id.container, PlaceholderFragment.newInstance(main_position))
          .commit();
      }
    });
  }

  public void ProcessVideos(VideoRepository repository) {
    LinearLayout filters = (LinearLayout)findViewById(R.id.filters);
    if (filters == null) {
      Toast.makeText(this, "An error occurred processing the filters.", Toast.LENGTH_LONG).show();
      return;
    }
    filters.setVisibility(View.GONE);
    ListView classes = (ListView) findViewById(R.id.classes);
    if (classes == null) {
      Toast.makeText(this, "An error occurred processing the classes.", Toast.LENGTH_LONG).show();
      return;
    }
    VideoAdapter va = new VideoAdapter(
            this,
            android.R.layout.simple_list_item_1,
            android.R.id.text1,
            repository.videoDataModels);
    classes.setAdapter(va);
    classes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      {} @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        VideoDataModel v = (VideoDataModel) parent.getAdapter().getItem(position);
        String url = v.Link;
        String title = v.Name;
        String contentType = "video/mpeg";
        MediaMetadata mMediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        mMediaMetadata.putString(MediaMetadata.KEY_TITLE, title);
        /*
        url = "http://www.ghostwhisperer.us/Music/Queen/We%20Will%20Rock%20You.mp3";
        contentType = "audio/mpeg";
        mMediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);
        mMediaMetadata.putString(MediaMetadata.KEY_TITLE, "We Will Rock You");
        //*/
        final MediaInfo data = new MediaInfo.Builder(url)
                .setContentType(contentType)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mMediaMetadata)
                .build();
        if (apiClient != null && mRemoteMediaPlayer != null) {
          try {
            mRemoteMediaPlayer.load(apiClient, data, true).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
              {} @Override public void onResult(RemoteMediaPlayer.MediaChannelResult result) {
                if (result.getStatus().isSuccess()) {
                  if (seekBar != null) {
                    seekBar.setMax((int)mRemoteMediaPlayer.getMediaInfo().getStreamDuration());
                  }
                  pause.setImageResource(android.R.drawable.ic_media_pause);
                  playback.setVisibility(View.VISIBLE);
                  //playing = true;
                  paused = false;
                  Log.d(TAG, "Media loaded successfully");

                  progress = new Timer("progress");
                  setupTimerTask();
                  progress.schedule(progressUpdater, 0, 500);
                }
              }
            });
          } catch (IllegalStateException e) {
            Log.e(TAG, "Problem occurred with media during loading", e);
          } catch (Exception e) {
            Log.e(TAG, "Problem opening media during loading", e);
          }
        }
      }
    });
  }

  private void teardown() {
    Log.d(TAG, "teardown");
    if (apiClient != null) {
      if (applicationStarted) {
        if (apiClient.isConnected()) {
          Cast.CastApi.stopApplication(apiClient, sessionId);
          try {
            if (casterChannel != null) {
              Cast.CastApi.removeMessageReceivedCallbacks( apiClient, casterChannel.getNamespace());
              casterChannel = null;
            }
          } catch (IOException e) {
            Log.e(TAG, "Exception while removing channel", e);
          }
          apiClient.disconnect();
        }
        applicationStarted = false;
      }
      apiClient = null;
    }
    selectedDevice = null;
    mRemoteMediaPlayer = null;
  }


  /**
   * A placeholder fragment containing a simple view.
   */
  public static class PlaceholderFragment extends Fragment {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_POSITION_NUMBER = "position_number";
    private Activity activity;



    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      /*
      if (savedInstanceState != null) {
        switch (savedInstanceState.getInt(ARG_ID_NUMBER)) {
          case 1:
            return inflater.inflate(R.layout.fragment_classes, container, false);
          case 2:
            return inflater.inflate(R.layout.fragment_classes, container, false);
        }
      }
      */
      View v = inflater.inflate(R.layout.fragment_classes, container, false);
      ((Main)this.activity).onFragmentInflated(v);
      return v;
    }

    @Override public void onAttach(Activity activity) {
      super.onAttach(activity);
      this.activity = activity;
      ((Main)activity).yearSelected = false;
      ((Main)activity).monthSelected = false;
      ((Main)activity).onSectionAttached(getArguments().getInt(ARG_POSITION_NUMBER));
    }



    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(int positionNumber) {
      PlaceholderFragment fragment = new PlaceholderFragment();
      Bundle args = new Bundle();
      args.putInt(ARG_POSITION_NUMBER, positionNumber);
      fragment.setArguments(args);
      return fragment;
    }

    public PlaceholderFragment() { }

  }

  public class CasterChannel implements Cast.MessageReceivedCallback {
    public String getNamespace() {
      return "urn:x-cast:com.lkspencer.caster";
    }

    @Override public void onMessageReceived(CastDevice castDevice, String namespace, String message) {
      Log.d(TAG, "onMessageReceived: " + message);
    }
  }
}
