package com.lkspencer.caster.upnp;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;

public class DeviceDisplay {

  Device device;
  String id;
  String name;
  Service service;



  public DeviceDisplay(Device device) {
    this.device = device;
    this.id = "";
    this.name = "";
  }
  public DeviceDisplay(Device device, String id, String name, Service service) {
    this.device = device;
    this.id = id;
    this.name = name;
    this.service = service;
  }



  public Device getDevice() { return device; }
  public String getId() { return id; }
  public String getName() { return name; }
  public Service getService() { return service; }



  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DeviceDisplay that = (DeviceDisplay) o;
    return device.equals(that.device);
  }

  @Override public int hashCode() { return device.hashCode(); }

  @Override public String toString() {
    String name =
        getDevice().getDetails() != null && getDevice().getDetails().getFriendlyName() != null
            ? getDevice().getDetails().getFriendlyName()
            : getDevice().getDisplayString();
    // Display a little star while the device is being loaded (see performance optimization earlier)
    return device.isFullyHydrated() ? name : name + " *";
  }

}
