package org.kobjects.htmlview2.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ScrollView;

import org.kobjects.htmlview2.DefaultRequestHandler;
import org.kobjects.htmlview2.HtmlView;
import org.kobjects.htmlview2.HtmlViewGroup;
import org.kobjects.htmlview2.parser.HtmlProcessor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;

public class MainActivity extends AppCompatActivity {

  HtmlProcessor htmlProcessor = new HtmlProcessor();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    try {
      Reader reader = new BufferedReader(
          new InputStreamReader(getAssets().open("index.html")));

      HtmlView htmlView = new HtmlView(this, new DefaultRequestHandler(this),
          new URI("file:///android_asset/"));
      htmlProcessor.parse(reader, htmlView);

      ScrollView scrollView = new ScrollView(this);
      scrollView.addView(htmlView);
      setContentView(scrollView);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
