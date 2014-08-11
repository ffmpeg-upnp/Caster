package com.lkspencer.caster.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lkspencer.caster.datamodels.ClassDataModel;

import java.util.ArrayList;

/**
 * Created by Kirk on 8/10/2014.
 * ArrayAdapter
 */
public class ClassAdapter extends ArrayAdapter<ClassDataModel> {

  public ClassAdapter(Context context, int layout, int textView, ArrayList<ClassDataModel> arrayListClassDataModel) {
    super(context, layout, textView, arrayListClassDataModel);
    this.arrayListClassDataModel = arrayListClassDataModel;
    this.layout = layout;
    //this.textView = textView;
    //this.context = context;
  }



  private ArrayList<ClassDataModel> arrayListClassDataModel;
  private int layout;
  //private int textView;
  //private Context context;



    @Override public int getCount() {
      return arrayListClassDataModel.size();
    }

    @Override public View getView(int position, View contentView, ViewGroup viewGroup) {
      View view;

      if (contentView == null) {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = layoutInflater.inflate(layout, null);
      } else {
        view = contentView;
      }
      if (view != null) {
        ((TextView)view).setText(arrayListClassDataModel.get(position).Name);
      }

      return view;
    }

}
