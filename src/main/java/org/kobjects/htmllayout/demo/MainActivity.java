package org.kobjects.htmllayout.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ScrollView;

import org.kobjects.htmllayout.DefaultRequestHandler;
import org.kobjects.htmllayout.HtmlContext;
import org.kobjects.htmllayout.HtmlLayout;
import org.kobjects.htmllayout.parser.HtmlProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    try {
      Reader reader = new BufferedReader(
          new InputStreamReader(getAssets().open("index.html")));

      HtmlContext htmlContext = new HtmlContext(this, new DefaultRequestHandler(this));
      HtmlProcessor parser = new HtmlProcessor(htmlContext);
      HtmlLayout html = parser.parse(reader);

      ScrollView scrollView = new ScrollView(this);
      scrollView.addView(html);
      setContentView(scrollView);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }


  }
}
