package com.lkspencer.caster.upnp;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteDeviceIdentity;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;

import java.net.URL;

public class BrowserRegistryListener implements RegistryListener {

  public BrowserRegistryListener(Activity a) {
    this.a = a;
  }

  private Activity a;

  /* Discovery performance optimization for very slow Android devices! */
  @Override public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
    deviceAdded(device);
  }

  @Override public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
    a.runOnUiThread(new Runnable() {
      public void run() {
        Toast.makeText(a, "Discovery failed of '" + device.getDisplayString() + "': " + (ex != null ? ex.toString() : "Couldn't retrieve device/service descriptors"), Toast.LENGTH_LONG).show();
      }
    });
    deviceRemoved(device);
  }
    /* End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz) */

  @Override public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
    deviceAdded(device);
  }

  @Override public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {

  }

  @Override public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
    deviceRemoved(device);
  }

  @Override public void localDeviceAdded(Registry registry, LocalDevice device) {
    deviceAdded(device);
  }

  @Override public void localDeviceRemoved(Registry registry, LocalDevice device) {
    deviceRemoved(device);
  }

  @Override public void beforeShutdown(Registry registry) {

  }

  @Override public void afterShutdown() {

  }

  public void deviceAdded(final Device device) {
    a.runOnUiThread(new Runnable() {
      public void run() {
        RemoteDevice rd = ((RemoteDevice) device);
        if (rd != null) {
          RemoteDeviceIdentity rdi = rd.getIdentity();
          if (rdi != null) {
            URL url = rdi.getDescriptorURL();
            if (url != null) {
              String address = url.getHost();
              if (address != null) {
                Toast.makeText(a, rd.getDetails().getFriendlyName() + " - port:" + url.getPort(), Toast.LENGTH_LONG).show();
              }
            }
          }
        }
          /*
          int position = listAdapter.getPosition(d);
          if (position >= 0) {
            // Device already in the list, re-set new value at same position
            listAdapter.remove(d);
            listAdapter.insert(d, position);
          } else {
            listAdapter.add(d);
          }
          */
      }
    });
  }

  public void deviceRemoved(final Device device) {
    a.runOnUiThread(new Runnable() {
      public void run() {
        Toast.makeText(a, device.getDetails().getFriendlyName(), Toast.LENGTH_LONG).show();
        //listAdapter.remove(new DeviceDisplay(device));
      }
    });
  }
}
