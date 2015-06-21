package com.lkspencer.caster;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PlaceholderFragment extends Fragment {

  private static final String FRAGMENT_NAME = "fragment_name";
  public Main main;



  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    if (savedInstanceState == null) savedInstanceState = this.getArguments();
    if (savedInstanceState == null || !savedInstanceState.containsKey(FRAGMENT_NAME))
      return getDefaultView(inflater, container);
    String name = savedInstanceState.getString(FRAGMENT_NAME);
    if (name == null) getDefaultView(inflater, container);
    assert name != null;
    switch (name) {
      case "fragment_main":
        return getDefaultView(inflater, container);
      case "fragment_mediaitems":
        View v = inflater.inflate(R.layout.fragment_mediaitems, container, false);
        main.onFragmentInflated(v);
        return v;
    }
    return getDefaultView(inflater, container);
  }

  @Override public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.main = (Main)activity;
  }



  private View getDefaultView(LayoutInflater inflater, ViewGroup container) {
    View v = inflater.inflate(R.layout.fragment_main, container, false);
    main.onFragmentInflated(v);
    return v;
  }

  public static PlaceholderFragment newInstance(String fragmentName, Main main) {
    PlaceholderFragment fragment = new PlaceholderFragment();
    fragment.main = main;
    Bundle args = new Bundle();
    args.putString(FRAGMENT_NAME, fragmentName);
    fragment.setArguments(args);
    return fragment;
  }

}
