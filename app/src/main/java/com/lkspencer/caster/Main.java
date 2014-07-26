package com.lkspencer.caster;

import android.app.Activity;
import android.os.Bundle;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;


public class Main extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

  private NavigationDrawerFragment mNavigationDrawerFragment;
  private CharSequence mTitle;
  private final MediaRouter.Callback mediaRouterCallback = new MediaRouter.Callback() {

    @Override public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo route) {
      CastDevice device = CastDevice.getFromBundle(route.getExtras());
      setSelectedDevice(device);
    }

    @Override public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo route) {
      setSelectedDevice(null);
    }

  };
  private MediaRouter mediaRouter;
  private MediaRouteSelector mediaRouteSelector;
  private CastDevice selectedDevice;
  private GoogleApiClient apiClient;
  private boolean applicationStarted;
  public static final String TAG = "Main";
  private final Cast.Listener castClientListener = new Cast.Listener() {
    @Override public void onApplicationDisconnected(int statusCode) { }

    @Override public void onVolumeChanged() { }
  };
  private final GoogleApiClient.ConnectionCallbacks connectionCallback = new GoogleApiClient.ConnectionCallbacks() {
    @Override public void onConnected(Bundle bundle) {
      Toast.makeText(Main.this, "Connected", Toast.LENGTH_SHORT).show();
      try {
        Cast.CastApi.launchApplication(apiClient, CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID, false).setResultCallback(connectionResultCallback);
      } catch (Exception e) {
        Log.e(TAG, "Failed to launch application", e);
      }
    }

    @Override public void onConnectionSuspended(int i) { }
  };
  private final GoogleApiClient.OnConnectionFailedListener connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
    @Override public void onConnectionFailed(ConnectionResult connectionResult) {
      Toast.makeText(Main.this, "Failed to connect " + connectionResult.toString(), Toast.LENGTH_SHORT).show();
      setSelectedDevice(null);
    }
  };
  private final ResultCallback connectionResultCallback = new ResultCallback() {
    @Override public void onResult(Result result) {
      Toast.makeText(Main.this, "result: " + result.getStatus().getStatusMessage(), Toast.LENGTH_SHORT).show();

      Status status = result.getStatus();
      if (status.isSuccess()) {
        applicationStarted = true;
      }
    }

  };
  private static final String NAMESPACE = "urn:x-cast:com.lkspencer.caster";
  private static final int REQUEST_GMS_ERROR = 0;



  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mNavigationDrawerFragment = (NavigationDrawerFragment)getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
    mTitle = getTitle();

    // Set up the drawer.
    mNavigationDrawerFragment.setUp(
      R.id.navigation_drawer,
      (DrawerLayout) findViewById(R.id.drawer_layout));

    mediaRouter = MediaRouter.getInstance(getApplicationContext());
    mediaRouteSelector = new MediaRouteSelector.Builder().addControlCategory(CastMediaControlIntent.categoryForCast(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)).build();
  }

  @Override public void onNavigationDrawerItemSelected(int position) {
    // update the main content by replacing fragments
    FragmentManager fragmentManager = getSupportFragmentManager();
    fragmentManager.beginTransaction()
            .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
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
    int id = item.getItemId();
    String url = null;
    if (id == R.id.action_video1) {
      url = "http://10.0.0.2/MyWeb/video/ComeFollowMe/2014/07%20-%20July/Young%20Women/02%20-%20Why%20are%20covenants%20important%20in%20my%20life/2013-03-004-using-pictures-360p-eng.mp4";
    } else if (id == R.id.action_video2) {
      url = "http://10.0.0.2/MyWeb/video/ComeFollowMe/2014/07%20-%20July/Aaronic%20Priesthood/01%20-%20How%20can%20I%20help%20others%20have%20a%20meaningful%20experience%20with%20the%20sacrament%20(Duty%20to%20God)/2014-06-001-always-remember-him-720p-eng.mp4";
    } else if (id == R.id.action_audio) {
      url = "http://www.ghostwhisperer.us/Music/Queen/We%20Will%20Rock%20You.mp3";
    }
    if (url != null) {
      RemoteMediaPlayer rmp = new RemoteMediaPlayer();
      MediaMetadata mMediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
      mMediaMetadata.putString(MediaMetadata.KEY_TITLE, "Demo Video");
      MediaInfo data = new MediaInfo.Builder(url)
              .setContentType("audio/mpeg")
              .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
              .setMetadata(mMediaMetadata)
              .build();

      if (apiClient != null) {
        rmp.load(apiClient, data, true);
      }
      return true;
    }
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



  public void onSectionAttached(int number) {
    switch (number) {
      case 1:
        mTitle = getString(R.string.title_section1);
        break;
      case 2:
        mTitle = getString(R.string.title_section2);
        break;
      case 3:
        mTitle = getString(R.string.title_section3);
        break;
    }
  }

  public void restoreActionBar() {
    ActionBar actionBar = getSupportActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    actionBar.setDisplayShowTitleEnabled(true);
    actionBar.setTitle(mTitle);
  }

  private void sendMessage(String message) {
    RemoteMediaPlayer rmp = new RemoteMediaPlayer();

    if (apiClient != null) {
      try {
        Cast.CastApi.sendMessage(apiClient, NAMESPACE, message).setResultCallback(new ResultCallback<Status>() {
          @Override public void onResult(Status result) {
            if (!result.isSuccess()) {
              Log.e(TAG, "Sending message failed");
            }
          }
        });
      } catch (Exception e) {
        Log.e(TAG, "Exception while sending message", e);
      }
    }
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



  /**
   * A placeholder fragment containing a simple view.
   */
  public static class PlaceholderFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber) {
      PlaceholderFragment fragment = new PlaceholderFragment();
      Bundle args = new Bundle();
      args.putInt(ARG_SECTION_NUMBER, sectionNumber);
      fragment.setArguments(args);
      return fragment;
    }

    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      View rootView = inflater.inflate(R.layout.fragment_main, container, false);
      return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
      super.onAttach(activity);
      ((Main) activity).onSectionAttached(
              getArguments().getInt(ARG_SECTION_NUMBER));
    }
  }

}
