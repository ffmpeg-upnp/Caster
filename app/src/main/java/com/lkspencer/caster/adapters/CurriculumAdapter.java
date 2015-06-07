package com.lkspencer.caster.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lkspencer.caster.datamodels.CurriculumDataModel;

import java.util.ArrayList;

/**
 * Created by Kirk on 8/10/2014.
 * ArrayAdapter
 */
public class CurriculumAdapter extends ArrayAdapter<CurriculumDataModel> {

  public CurriculumAdapter(Context context, int layout, int textView, ArrayList<CurriculumDataModel> arrayListCurriculum) {
    super(context, layout, textView, arrayListCurriculum);
    this.arrayListCurriculum = arrayListCurriculum;
    this.layout = layout;
    //this.textView = textView;
    //this.context = context;
  }



  private ArrayList<CurriculumDataModel> arrayListCurriculum;
  private int layout;
  //private int textView;
  //private Context context;



    @Override public int getCount() { return arrayListCurriculum.size(); }

    @Override public View getView(int position, View contentView, ViewGroup viewGroup) {
      View view;

      if (contentView == null) {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = layoutInflater.inflate(layout, null);
      } else {
        view = contentView;
      }
      if (view != null) {
        TextView curriculum = ((TextView)view);
        curriculum.setText(arrayListCurriculum.get(position).Name);
        curriculum.setPadding(64, 0, 64, 0);
      }

      return view;
    }

}
