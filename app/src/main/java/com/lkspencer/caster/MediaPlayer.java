package com.lkspencer.caster;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Kirk on 10/7/2014.
 *
 */
public class MediaPlayer {

  //Constructor
  public MediaPlayer(Activity a) {
    this.a = a;
  }



  //Variables
  public MediaRouter mediaRouter;
  public GoogleApiClient apiClient;
  public static final String TAG = "MediaPlayer";
  public RemoteMediaPlayer mRemoteMediaPlayer;
  public TimerTask progressUpdater;
  public MediaRouteSelector mediaRouteSelector;
  public boolean paused = true;
  public Timer progress;

  private String sessionId;
  private boolean applicationStarted;
  private CastDevice selectedDevice;
  private CasterChannel casterChannel;
  private Activity a;
  private static final double VOLUME_INCREMENT = 0.05;



  public final MediaRouter.Callback mediaRouterCallback = new MediaRouter.Callback() {

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
    {} @Override public void onConnectionFailed(ConnectionResult connectionResult) {
      Toast.makeText(a, "Failed to connect " + connectionResult.toString(), Toast.LENGTH_SHORT).show();
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



  public void teardown() {
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

  public void setupTimerTask(final SeekBar seekBar) {
    progressUpdater = new TimerTask() {
      {} @Override public void run() {
        if (seekBar == null || mRemoteMediaPlayer == null) return;

        seekBar.setProgress((int)mRemoteMediaPlayer.getApproximateStreamPosition());
      }
    };
  }

  public void start() {
    mediaRouter.addCallback(mediaRouteSelector, mediaRouterCallback, MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
  }

  public void stop() {
    mediaRouter.removeCallback(mediaRouterCallback);
  }

  public void increaseVolume() {
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

  public void decreaseVolume() {
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

  public void pausePlayback(final SeekBar seekBar) {
    if (mRemoteMediaPlayer != null /*&& mRemoteMediaPlayer.getStreamDuration() > 0 && mRemoteMediaPlayer.getMediaStatus() != null*/) {
      if (paused) {
        mRemoteMediaPlayer.play(apiClient).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
          {} @Override public void onResult(RemoteMediaPlayer.MediaChannelResult result) {
            Status status = result.getStatus();
            if (status.isSuccess()) {
              paused = false;
              setupTimerTask(seekBar);
              progress = new Timer("progress");
              progress.schedule(progressUpdater, 0, 500);
            } else {
              Log.w(TAG, "Unable to toggle pause: " + status.getStatusCode());
            }
          }
        });
      } else {
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

  public void stopPlayback(final Main m) {
    if (mRemoteMediaPlayer != null) {
      mRemoteMediaPlayer.stop(apiClient).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
        {} @Override public void onResult(RemoteMediaPlayer.MediaChannelResult result) {
          Status status = result.getStatus();
          if (status.isSuccess()) {
            progress.cancel();
            progressUpdater.cancel();
            m.hidePlayback();
            paused = true;
            //playing = false;
          } else {
            Log.w(TAG, "Unable to stop playback: " + status.getStatusCode());
          }
        }
      });
    } else {
      m.hidePlayback();
      paused = true;
      //playing = false;
    }
  }

  public void seekPlayback(int progress) {
    if (mRemoteMediaPlayer != null) {
      mRemoteMediaPlayer.seek(apiClient, progress);
    }
  }

  private void setSelectedDevice(CastDevice device) {
    Log.d(TAG, "setSelectedDevice: " + device);

    selectedDevice = device;

    if (selectedDevice != null) {
      try {
        teardown();
        connectApiClient();
      } catch (IllegalStateException e) {
        Log.w(TAG, "Exception while connecting API client", e);
        teardown();
      }
    } else {
      teardown();

      mediaRouter.selectRoute(mediaRouter.getDefaultRoute());
    }
  }

  private void connectApiClient() {
    Cast.CastOptions apiOptions = Cast.CastOptions.builder(selectedDevice, castClientListener).build();
    apiClient = new GoogleApiClient.Builder(a)
            .addApi(Cast.API, apiOptions)
            .addConnectionCallbacks(connectionCallback)
            .addOnConnectionFailedListener(connectionFailedListener)
            .build();
    apiClient.connect();
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
