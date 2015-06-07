package com.lkspencer.caster;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.common.api.ResultCallback;
import com.lkspencer.caster.adapters.ClassAdapter;
import com.lkspencer.caster.adapters.CurriculumAdapter;
import com.lkspencer.caster.adapters.TopicAdapter;
import com.lkspencer.caster.adapters.VideoAdapter;
import com.lkspencer.caster.datamodels.ClassDataModel;
import com.lkspencer.caster.datamodels.TopicDataModel;
import com.lkspencer.caster.datamodels.VideoDataModel;

import java.util.Timer;

/**
 * Created by Kirk on 10/7/2014.
 * Used to process the data received from the MySQL database
 */
public class VideoRepositoryCallback implements IVideoRepositoryCallback {

  //Constructor
  public VideoRepositoryCallback(Main m, NavigationDrawerFragment ndf) {
    this.m = m;
    this.ndf = ndf;
  }



  //Variables
  Main m;
  NavigationDrawerFragment ndf;



  //Event Handlers
  public void ProcessCurriculums(VideoRepository repository) {
    CurriculumAdapter ca = new CurriculumAdapter(
            ndf.getActionBar().getThemedContext(),
            android.R.layout.simple_list_item_1,
            android.R.id.text1,
            repository.curriculumsDataModels);
    ndf.mDrawerListView.setAdapter(ca);
    ndf.mDrawerListView.setItemChecked(ndf.mCurrentSelectedPosition, true);
  }

  public void ProcessClasses(VideoRepository repository) {
    LinearLayout filters = (LinearLayout)m.findViewById(R.id.filters);
    if (filters == null) {
      Toast.makeText(m, "An error occurred processing the filters.", Toast.LENGTH_LONG).show();
      return;
    }
    filters.setVisibility(View.VISIBLE);
    ListView classes = (ListView)m.findViewById(R.id.classes);
    if (classes == null) {
      Toast.makeText(m, "An error occurred processing the classes.", Toast.LENGTH_LONG).show();
      return;
    }
    ClassAdapter ca = new ClassAdapter(
            m,
            android.R.layout.simple_list_item_1,
            android.R.id.text1,
            repository.classDataModels);
    classes.setAdapter(ca);
    classes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      {} @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ClassDataModel c = (ClassDataModel) parent.getAdapter().getItem(position);
        m.classId = c.ClassId;
        m.main_position = 1;
        //m.onSectionAttached(m.main_position);
        /*
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager
          .beginTransaction()
          .replace(R.id.container, PlaceholderFragment.newInstance(main_position))
          .commit();
        */
      }
    });
  }

  public void ProcessTopics(VideoRepository repository) {
    LinearLayout filters = (LinearLayout)m.findViewById(R.id.filters);
    if (filters == null) {
      Toast.makeText(m, "An error occurred processing the filters.", Toast.LENGTH_LONG).show();
      return;
    }
    filters.setVisibility(View.VISIBLE);
    ListView classes = (ListView)m.findViewById(R.id.classes);
    if (classes == null) {
      Toast.makeText(m, "An error occurred processing the classes.", Toast.LENGTH_LONG).show();
      return;
    }
    TopicAdapter ta = new TopicAdapter(
            m,
            android.R.layout.simple_list_item_1,
            android.R.id.text1,
            repository.topicDataModels);
    classes.setAdapter(ta);
    classes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      {} @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TopicDataModel t = (TopicDataModel) parent.getAdapter().getItem(position);
        m.topicId = t.TopicId;
        m.main_position = 2;
        //m.onSectionAttached(m.main_position);
        /*
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager
          .beginTransaction()
          .replace(R.id.container, PlaceholderFragment.newInstance(main_position))
          .commit();
        */
      }
    });
  }

  public void ProcessVideos(VideoRepository repository) {
    LinearLayout filters = (LinearLayout)m.findViewById(R.id.filters);
    if (filters == null) {
      Toast.makeText(m, "An error occurred processing the filters.", Toast.LENGTH_LONG).show();
      return;
    }
    filters.setVisibility(View.GONE);
    ListView classes = (ListView)m.findViewById(R.id.classes);
    if (classes == null) {
      Toast.makeText(m, "An error occurred processing the classes.", Toast.LENGTH_LONG).show();
      return;
    }
    VideoAdapter va = new VideoAdapter(
            m,
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
        if (m.mediaPlayer.apiClient != null && m.mediaPlayer.mRemoteMediaPlayer != null) {
          try {
            m.mediaPlayer.mRemoteMediaPlayer.load(m.mediaPlayer.apiClient, data, true).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
              {} @Override public void onResult(RemoteMediaPlayer.MediaChannelResult result) {
                if (result.getStatus().isSuccess()) {
                  if (m.seekBar != null) {
                    m.seekBar.setMax((int)m.mediaPlayer.mRemoteMediaPlayer.getMediaInfo().getStreamDuration());
                  }
                  m.pause.setImageResource(android.R.drawable.ic_media_pause);
                  m.playback.setVisibility(View.VISIBLE);
                  //playing = true;
                  m.mediaPlayer.paused = false;
                  Log.d(Main.TAG, "Media loaded successfully");

                  m.mediaPlayer.progress = new Timer("progress");
                  m.mediaPlayer.setupTimerTask(m.seekBar);
                  m.mediaPlayer.progress.schedule(m.mediaPlayer.progressUpdater, 0, 500);
                }
              }
            });
          } catch (IllegalStateException e) {
            Log.e(Main.TAG, "Problem occurred with media during loading", e);
          } catch (Exception e) {
            Log.e(Main.TAG, "Problem opening media during loading", e);
          }
        }
      }
    });
  }

}
