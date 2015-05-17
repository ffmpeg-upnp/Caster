package com.lkspencer.caster.upnp;

import android.app.Activity;
import android.util.Log;

import com.lkspencer.caster.adapters.DeviceAdapter;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.container.Container;

import java.util.ArrayList;

public class BrowseRegistryListener implements RegistryListener {

  public BrowseRegistryListener(AndroidUpnpService upnpService, Activity a, ArrayList<DeviceDisplay> devices, DeviceAdapter da) {
    this.a = a;
    this.devices = devices;
    this.upnpService = upnpService;
    this.da = da;
  }



  private Activity a;
  private ArrayList<DeviceDisplay> devices;
  private AndroidUpnpService upnpService;
  private DeviceAdapter da;



  /* Discovery performance optimization for very slow Android devices! */
  @Override public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) { deviceAdded(device); }

  @Override public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) { deviceRemoved(device); }
  /* End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz) */

  @Override public void remoteDeviceAdded(Registry registry, RemoteDevice device) { deviceAdded(device); }

  @Override public void remoteDeviceUpdated(Registry registry, RemoteDevice device) { }

  @Override public void remoteDeviceRemoved(Registry registry, RemoteDevice device) { deviceRemoved(device); }

  @Override public void localDeviceAdded(Registry registry, LocalDevice device) { deviceAdded(device); }

  @Override public void localDeviceRemoved(Registry registry, LocalDevice device) { deviceRemoved(device); }

  @Override public void beforeShutdown(Registry registry) { }

  @Override public void afterShutdown() { }

  public void deviceAdded(final Device device) {
    a.runOnUiThread(new Runnable() {
      {} public void run() {
        Service[] services = device.getServices();
        for (final Service service : services) {
          if ("ContentDirectory".equalsIgnoreCase(service.getServiceId().getId()) && service.hasActions()) {
            try {
              Browse b = new Browse(service, "0", BrowseFlag.DIRECT_CHILDREN) {
                @Override public void received(ActionInvocation actionInvocation, DIDLContent didl) {
                  for (Container container : didl.getContainers()) {
                    String title = container.getTitle();
                    if (title != null && title.contains("Video")) {
                      DeviceDisplay d = new DeviceDisplay(
                        device,
                        container.getId(),
                        device.getDetails() != null && device.getDetails().getFriendlyName() != null
                          ? device.getDetails().getFriendlyName()
                          : device.getDisplayString());
                      int position = devices.indexOf(d);
                      if (position >= 0) {
                        devices.remove(d);
                        devices.add(position, d);
                      } else {
                        devices.add(d);
                      }
                      position = da.getPosition(d);
                      if (position >= 0) {
                        da.remove(d);
                        da.add(d);
                      } else {
                        da.add(d);
                      }
                    }
                  }
                }
                @Override public void updateStatus(Status status) { }
                @Override public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) { }
              };
              b.setControlPoint(upnpService.getControlPoint());
              b.run();
            } catch (Exception ex) {
              Log.e("asdf", ex.getMessage());
            }
          }
        }
        /*
        long firstResult = 1;
        long maxResults = 10;
        Browse b = new Browse(null, "0", BrowseFlag.DIRECT_CHILDREN, "*", firstResult, maxResults, new SortCriterion(true, "dc:title"), new SortCriterion(false, "dc:creator")) {
          @Override public void received(ActionInvocation actionInvocation, DIDLContent didl) {

          }

          @Override public void updateStatus(Status status) {

          }

          @Override public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {

          }
        };
        //*/

        /*
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
        //*/
      }
    });
  }

  public void deviceRemoved(final Device device) {
    a.runOnUiThread(new Runnable() {
      {} public void run() {
        //Toast.makeText(a, device.getDetails().getFriendlyName(), Toast.LENGTH_LONG).show();
        devices.remove(new DeviceDisplay(device));
      }
    });
  }
}
