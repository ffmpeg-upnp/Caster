package com.lkspencer.caster;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
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

import com.google.android.gms.cast.CastMediaControlIntent;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;


public class Main extends ActionBarActivity implements NavigationDrawerFragment.INavigationDrawerCallbacks {

  public static final String TAG = "Main";
  public int classId;
  public int topicId;
  public int main_position = 0;
  public ImageButton pause;
  public LinearLayout playback;
  public SeekBar seekBar;
  public MediaPlayer mediaPlayer;

  private NavigationDrawerFragment mNavigationDrawerFragment;
  private CharSequence mTitle;
  private int year;
  private int month;
  private boolean monthSelected = false;
  private boolean yearSelected = false;
  private VideoRepositoryCallback vrc;



  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

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
    //TODO: verify that they are connected to the STVS or STVS-N wifi network

    // Set up the drawer.
    mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
    mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

    mTitle = getTitle();

    mediaPlayer.mediaRouter = MediaRouter.getInstance(getApplicationContext());
    mediaPlayer.mediaRouteSelector = new MediaRouteSelector
            .Builder()
            .addControlCategory(CastMediaControlIntent.categoryForCast(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID))
            .build();
    GregorianCalendar now = new GregorianCalendar();
    month = now.get(Calendar.MONTH) + 1;
    year = now.get(Calendar.YEAR);
    vrc = new VideoRepositoryCallback(this, null);
  }

  @Override public void onNavigationDrawerItemSelected(int position) {
    this.classId = 0;
    this.topicId = 0;
    main_position = 0;
    // update the main content by replacing fragments
    ListView classes = (ListView) findViewById(R.id.classes);
    if (classes == null) {
      FragmentManager fragmentManager = getSupportFragmentManager();
      fragmentManager
              .beginTransaction()
              .replace(R.id.container, PlaceholderFragment.newInstance(main_position))
              .commit();
    } else {
      onSectionAttached(main_position);
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
      default:
        return super.dispatchKeyEvent(event);
    }
  }



  public void onSectionAttached(int position) {
    VideoRepository vr = new VideoRepository(vrc, year, month);
    Integer[] params;
    switch (position) {
      case 0:
        params = new Integer[4];
        params[0] = VideoRepository.Actions.GET_CLASSES;
        if (mNavigationDrawerFragment != null) {
          params[1] = mNavigationDrawerFragment.getCurrentCurriculumId();
        }
        vr.execute(params);
        mTitle = "Classes";
        break;
      case 1:
        params = new Integer[4];
        params[0] = VideoRepository.Actions.GET_TOPICS;
        if (mNavigationDrawerFragment != null) {
          params[1] = mNavigationDrawerFragment.getCurrentCurriculumId();
        }
        params[2] = classId;
        vr.execute(params);
        mTitle = "Topics";
        break;
      case 2:
        params = new Integer[4];
        params[0] = VideoRepository.Actions.GET_VIDEOS;
        if (mNavigationDrawerFragment != null) {
          params[1] = mNavigationDrawerFragment.getCurrentCurriculumId();
        }
        params[2] = classId;
        params[3] = topicId;
        vr.execute(params);
        mTitle = "Videos";
        break;
    }
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setTitle(mTitle);
    }
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
        if (mediaPlayer.paused) {
          pause.setImageResource(android.R.drawable.ic_media_pause);
        } else {
          pause.setImageResource(android.R.drawable.ic_media_play);
        }
        mediaPlayer.pausePlayback(seekBar);
      }
    });
    ImageButton stop = (ImageButton)v.findViewById(R.id.stop);
    stop.setOnClickListener(new View.OnClickListener() {
      {} @Override public void onClick(View v) {
        mediaPlayer.stopPlayback(Main.this);
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
        mediaPlayer.seekPlayback(progress);
      }
    });
  }

  public void restoreActionBar() {
    ActionBar actionBar = getSupportActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    actionBar.setDisplayShowTitleEnabled(true);
    actionBar.setTitle(mTitle);
  }

  public void hidePlayback() {
    if (playback != null) {
      playback.setVisibility(View.GONE);
    }
    if (pause != null) {
      pause.setImageResource(android.R.drawable.ic_media_play);
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
      /*
      if (getArguments().containsKey(ARG_POSITION_NUMBER)) {
        int position_number = getArguments().getInt(ARG_POSITION_NUMBER);
        ((Main)activity).onSectionAttached(position_number);
      }
      */
    }



    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    /*
    public static PlaceholderFragment newInstance() {
      PlaceholderFragment fragment = new PlaceholderFragment();
      Bundle args = new Bundle();
      fragment.setArguments(args);
      return fragment;
    }
    */

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
