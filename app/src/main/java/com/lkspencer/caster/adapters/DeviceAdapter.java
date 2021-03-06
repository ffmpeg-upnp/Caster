package com.lkspencer.caster.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lkspencer.caster.upnp.DeviceDisplay;

import java.util.ArrayList;

public class DeviceAdapter extends ArrayAdapter<DeviceDisplay> {

  public DeviceAdapter(Context context, int layout, int textView, ArrayList<DeviceDisplay> arrayListDevice) {
    super(context, layout, textView, arrayListDevice);
    this.arrayListDevice = arrayListDevice;
    this.layout = layout;
  }



  private ArrayList<DeviceDisplay> arrayListDevice;
  private int layout;



  @Override public int getCount() { return arrayListDevice.size(); }

  @Override public View getView(int position, View contentView, ViewGroup viewGroup) {
    View view;

    if (contentView == null) {
      LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      view = layoutInflater.inflate(layout, null);
    } else {
      view = contentView;
    }
    if (view != null) {
      ((TextView)view).setText(arrayListDevice.get(position).getName());
    }

    return view;
  }

}
