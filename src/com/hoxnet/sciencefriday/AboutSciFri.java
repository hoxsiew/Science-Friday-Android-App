package com.hoxnet.sciencefriday;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class AboutSciFri extends Activity{
  @Override
  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    setContentView(R.layout.about);
    TextView mTitleTxt=(TextView)findViewById(R.id.TextView01);
    mTitleTxt.setGravity(android.view.Gravity.CENTER);
    mTitleTxt.setText("Science Friday");
    TextView mAboutTxt=(TextView)findViewById(R.id.TextView02);
    mAboutTxt.setText(R.string.about);
    android.text.util.Linkify.addLinks(mAboutTxt,android.text.util.Linkify.WEB_URLS);
  }
}
