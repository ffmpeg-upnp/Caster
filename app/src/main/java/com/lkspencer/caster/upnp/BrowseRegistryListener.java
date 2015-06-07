package com.lkspencer.caster.upnp;

import android.app.Activity;

import com.lkspencer.caster.adapters.DeviceAdapter;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;

public class BrowseRegistryListener implements RegistryListener {

  public BrowseRegistryListener(/*AndroidUpnpService upnpService,*/ Activity a, /*ArrayList<DeviceDisplay> devices,*/ DeviceAdapter da) {
    this.a = a;
    //this.devices = devices;
    //this.upnpService = upnpService;
    this.da = da;
  }



  private Activity a;
  //private ArrayList<DeviceDisplay> devices;
  //private AndroidUpnpService upnpService;
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
            String name = getDeviceName(device);
            if (name != null && name.contains("LDS-Media")) {
              da.add(new DeviceDisplay(device, "0", getDeviceName(device), service));
            }
            /*
            try {
              Browse b = new Browse(service, "0", BrowseFlag.DIRECT_CHILDREN) {
                @Override public void received(ActionInvocation actionInvocation, DIDLContent didl) {
                  for (Container container : didl.getContainers()) {
                    String title = container.getTitle();
                    if (title != null) {
                      da.add(new DeviceDisplay(device, container.getId(), getDeviceName(device) + " - " + title));
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
            */
          }
        }
      }
    });
  }

  public void deviceRemoved(final Device device) {
    a.runOnUiThread(new Runnable() {
      {} public void run() {
        //da.add(new DeviceDisplay(device, "", ""));
        //Toast.makeText(a, device.getDetails().getFriendlyName(), Toast.LENGTH_LONG).show();
        da.remove(new DeviceDisplay(device));
      }
    });
  }

  public String getDeviceName(Device device) {
    return device.getDetails() != null && device.getDetails().getFriendlyName() != null
        ? device.getDetails().getFriendlyName()
        : device.getDisplayString();
  }
}
