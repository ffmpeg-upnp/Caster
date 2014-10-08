package com.lkspencer.caster;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PlaceholderFragment extends Fragment {

  private static final String ARG_POSITION_NUMBER = "position_number";
  public Main main;



  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_classes, container, false);
    main.onFragmentInflated(v);
    return v;
  }

  @Override public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.main = (Main)activity;
    main.yearSelected = false;
    main.monthSelected = false;
  }



  public static PlaceholderFragment newInstance(int positionNumber, Main main) {
    PlaceholderFragment fragment = new PlaceholderFragment();
    fragment.main = main;
    Bundle args = new Bundle();
    args.putInt(ARG_POSITION_NUMBER, positionNumber);
    fragment.setArguments(args);
    return fragment;
  }

}
