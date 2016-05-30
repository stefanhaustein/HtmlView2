package org.kobjects.htmllayout.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ScrollView;

import org.kobjects.htmllayout.DefaultRequestHandler;
import org.kobjects.htmllayout.PageContext;
import org.kobjects.htmllayout.HtmlLayout;
import org.kobjects.htmllayout.parser.HtmlProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

  HtmlProcessor htmlProcessor = new HtmlProcessor();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    try {
      Reader reader = new BufferedReader(
          new InputStreamReader(getAssets().open("index.html")));

      PageContext pageContext = new PageContext(this, new DefaultRequestHandler(this),
          new URI("file:///android_asset/"));
      HtmlLayout html = htmlProcessor.parse(reader, pageContext);

      ScrollView scrollView = new ScrollView(this);
      scrollView.addView(html);
      setContentView(scrollView);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
