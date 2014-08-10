package com.lkspencer.caster;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Kirk on 8/10/2014.
 * asdf
 */
public class CurriculumAdapter extends ArrayAdapter<Curriculum> {

  public CurriculumAdapter(Context context, int layout, int textView, ArrayList<Curriculum> arrayListCurriculum) {
    super(context, layout, textView, arrayListCurriculum);
    this.arrayListCurriculum = arrayListCurriculum;
    this.layout = layout;
    this.textView = textView;
    this.context = context;
  }



  private ArrayList<Curriculum> arrayListCurriculum;
  private int layout;
  private int textView;
  private Context context;



    @Override public int getCount() {
      return arrayListCurriculum.size();
    }

    @Override public View getView(int position, View contentView, ViewGroup viewGroup) {
      View view = null;

      if (contentView == null) {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = layoutInflater.inflate(layout, null);
      } else {
        view = contentView;
      }
      if (view != null) {
        ((TextView)view).setText(arrayListCurriculum.get(position).Name);
      }

      return view;
    }
}
