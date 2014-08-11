package com.lkspencer.caster.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lkspencer.caster.datamodels.TopicDataModel;

import java.util.ArrayList;

/**
 * Created by Kirk on 8/10/2014.
 * ArrayAdapter
 */
public class TopicAdapter extends ArrayAdapter<TopicDataModel> {

  public TopicAdapter(Context context, int layout, int textView, ArrayList<TopicDataModel> arrayListTopicDataModel) {
    super(context, layout, textView, arrayListTopicDataModel);
    this.arrayListTopicDataModel = arrayListTopicDataModel;
    this.layout = layout;
    //this.textView = textView;
    //this.context = context;
  }



  private ArrayList<TopicDataModel> arrayListTopicDataModel;
  private int layout;
  //private int textView;
  //private Context context;



    @Override public int getCount() {
      return arrayListTopicDataModel.size();
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
        ((TextView)view).setText(arrayListTopicDataModel.get(position).Name);
      }

      return view;
    }

}
