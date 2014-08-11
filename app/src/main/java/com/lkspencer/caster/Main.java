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
import android.widget.AdapterView;
import android.widget.ListView;
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
import com.lkspencer.caster.adapters.ClassAdapter;
import com.lkspencer.caster.adapters.TopicAdapter;
import com.lkspencer.caster.datamodels.ClassDataModel;
import com.lkspencer.caster.datamodels.TopicDataModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;


public class Main extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks, IVideoRepositoryCallback {

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
  private int classId;
  private int topicId;
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



  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mNavigationDrawerFragment = (NavigationDrawerFragment)getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
    mTitle = getTitle();

    // Set up the drawer.
    mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

    mediaRouter = MediaRouter.getInstance(getApplicationContext());
    mediaRouteSelector = new MediaRouteSelector.Builder().addControlCategory(CastMediaControlIntent.categoryForCast(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)).build();
  }

  @Override public void onNavigationDrawerItemSelected(int position) {
    // update the main content by replacing fragments
    FragmentManager fragmentManager = getSupportFragmentManager();
    fragmentManager
      .beginTransaction()
      .replace(R.id.container, PlaceholderFragment.newInstance(0))
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
    String title = null;
    String contentType = null;
    if (id == R.id.action_video1) {
      url = "http://192.168.1.2:32400/video/:/transcode/universal/start?path=http%3A%2F%2F127.0.0.1%3A32400%2Flibrary%2Fmetadata%2F706&mediaIndex=0&partIndex=0&protocol=http&offset=0&fastSeek=1&directPlay=0&directStream=1&videoQuality=60&videoResolution=1920x1080&maxVideoBitrate=8000&subtitleSize=100&audioBoost=100&session=dik4af9z5il1h5mi&X-Plex-Client-Identifier=9diqvrletp4x6r&X-Plex-Product=Plex+Web&X-Plex-Device=Windows&X-Plex-Platform=Chrome&X-Plex-Platform-Version=36.0&X-Plex-Version=2.1.12&X-Plex-Device-Name=Plex+Web+(Chrome)&X-Plex-Token=1bLEdyGBB6F4csSYpC5Q&X-Plex-Username=lkspencer";
      title = "Using Pictures";
      contentType = "video/mpeg";
    } else if (id == R.id.action_video2) {
      url = "\\\\KIRK-PC\\My Videos\\2014-06-001-always-remember-him-1080p-eng.mp4";
      title = "Always Remember Him";
      contentType = "video/mpeg";
    } else if (id == R.id.action_audio) {
      url = "http://www.ghostwhisperer.us/Music/Queen/We%20Will%20Rock%20You.mp3";
      title = "We Will Rock You";
      contentType = "audio/mpeg";
    }
    if (url != null && title != null && contentType != null) {
      RemoteMediaPlayer rmp = new RemoteMediaPlayer();
      MediaMetadata mMediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
      mMediaMetadata.putString(MediaMetadata.KEY_TITLE, title);
      MediaInfo data = new MediaInfo.Builder(url)
              .setContentType(contentType)
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



  public void onSectionAttached(int position) {
    GregorianCalendar now = new GregorianCalendar();
    VideoRepository vr = new VideoRepository(this, now.get(GregorianCalendar.YEAR), now.get(GregorianCalendar.MONTH));
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
        //params[2] = classId;
        //params[3] = topicId;
        vr.execute(params);
        mTitle = "Videos";
        break;
    }
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

  public void ProcessResultSet(VideoRepository repository, ResultSet resultSet) {
    ListView classes = (ListView) findViewById(R.id.classes);
    if (classId == 0) {
      try {
        if (resultSet == null || resultSet.isClosed() || !resultSet.first()) return;
        ArrayList<ClassDataModel> classDataModels = new ArrayList<ClassDataModel>();

        do {
          ClassDataModel classDataModel = new ClassDataModel();
          classDataModel.ClassId = resultSet.getInt(1);
          classDataModel.Name = resultSet.getString(2);
          classDataModels.add(classDataModel);
        } while (resultSet.next());
        ClassAdapter ca = new ClassAdapter(
                this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                classDataModels);
        classes.setAdapter(ca);
        classes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ClassDataModel c = (ClassDataModel) parent.getAdapter().getItem(position);
            Main.this.classId = c.ClassId;
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.container, PlaceholderFragment.newInstance(1))
                    .commit();
          }
        });
      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        repository.Close();
      }
    } else if (topicId == 0) {
      try {
        if (resultSet == null || resultSet.isClosed() || !resultSet.first()) return;
        ArrayList<TopicDataModel> topicDataModels = new ArrayList<TopicDataModel>();

        do {
          TopicDataModel topicDataModel = new TopicDataModel();
          topicDataModel.TopicId = resultSet.getInt(1);
          topicDataModel.Name = resultSet.getString(2);
          topicDataModels.add(topicDataModel);
        } while (resultSet.next());
        TopicAdapter ta = new TopicAdapter(
                this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                topicDataModels);
        classes.setAdapter(ta);
        classes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            TopicDataModel t = (TopicDataModel) parent.getAdapter().getItem(position);
            Main.this.topicId = t.TopicId;
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.container, PlaceholderFragment.newInstance(1))
                    .commit();
          }
        });
      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        repository.Close();
      }
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
    private static final String ARG_POSITION_NUMBER = "position_number";
    //private static final String ARG_ID_NUMBER = "id_number";



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
      return inflater.inflate(R.layout.fragment_classes, container, false);
    }

    @Override public void onAttach(Activity activity) {
      super.onAttach(activity);
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

}
