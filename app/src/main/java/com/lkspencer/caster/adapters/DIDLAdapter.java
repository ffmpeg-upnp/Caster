package com.lkspencer.caster.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.fourthline.cling.support.model.DIDLObject;

import java.util.ArrayList;

public class DIDLAdapter extends ArrayAdapter<DIDLObject> {
  public DIDLAdapter(Context context, int layout, int textView, ArrayList<DIDLObject> arrayListDIDLObjects) {
    super(context, layout, textView, arrayListDIDLObjects);
    this.arrayListDIDLObjects = arrayListDIDLObjects;
    this.layout = layout;
  }

  private ArrayList<DIDLObject> arrayListDIDLObjects;
  private int layout;

  @Override public int getCount() { return arrayListDIDLObjects.size(); }

  @Override public View getView(int position, View contentView, ViewGroup viewGroup) {
    View view;

    if (contentView == null) {
      LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      view = layoutInflater.inflate(layout, null);
    } else {
      view = contentView;
    }
    if (view != null) {
      ((TextView)view).setText(arrayListDIDLObjects.get(position).getTitle());
    }

    return view;
  }

}
