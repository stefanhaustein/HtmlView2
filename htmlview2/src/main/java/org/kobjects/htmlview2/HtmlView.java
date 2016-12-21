package org.kobjects.htmlview2;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import org.kobjects.dom.Element;
import org.kobjects.dom.Window;
import org.kobjects.css.CssProperty;
import org.kobjects.css.CssStyleDeclaration;
import org.kobjects.css.CssStyleSheet;
import org.kobjects.css.CssUnit;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;


/** 
 * View corresponding to the HTML root element. Also holds state information such as the location and
 * css pixel scale. Can't refer to the HTML body element directly because margins, borders and
 * paddings are managed by the parent HtmlViewGroup.
 */
public class HtmlView extends HtmlViewGroup implements Window {
  static final String TAG = "HtmlView";
  static final int PAINT_MASK = ~(Paint.STRIKE_THRU_TEXT_FLAG | Paint.UNDERLINE_TEXT_FLAG);

  static final String DATA_URL_SCHEME = "data";
  static final String BASE64_MARKER = "base64,";
  static final String ASSET_BASE_URL = "file:///android_asset/";

  private CssStyleSheet styleSheet;
  float scale;
  URI baseUri;
  private Hv2DomDocument document;
  private WebSettings settings = new WebSettings();

  public HtmlView(Context context) {
    super(context, null, null);
    scale = context.getResources().getDisplayMetrics().density;
    htmlView = this;
    node = document;
    try {
      baseUri = new URI(ASSET_BASE_URL);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    clearAll();
  }

  int getTextSize(CssStyleDeclaration style) {
    return Math.round(scale * style.get(CssProperty.FONT_SIZE, CssUnit.PX));
  }

  void setPaint(CssStyleDeclaration style, Paint paint) {
    paint.setTextSize(getTextSize(style));
    paint.setTypeface(CssConversion.getTypeface(style));
    paint.setFlags((paint.getFlags() & PAINT_MASK) | CssConversion.getPaintFlags(style));
  }

  public void clearAll() {
    node = document = new Hv2DomDocument(this);
    styleSheet = null;
    removeAllViews();
  }

  public URI createUri(String uri) throws URISyntaxException {
    return baseUri.resolve(uri);
  }

  public Hv2DomDocument getDocument() {
    return document;
  }

  public CssStyleSheet getStyleSheet() {
    if (styleSheet == null) {
      styleSheet = CssStyleSheet.createDefault(settings.getDefaultFontSize());
    }
    return styleSheet;
  }

  public URI getBaseUri() {
    return baseUri;
  }

  /**
   * This method expects that the URI is absolute.
   */
  public void loadAsync(final URI uri, final byte[] post, final Object onload) {
    final Context context = getContext();
    new AsyncTask<Void, Integer, Exception>() {
      String encoding;
      byte[] rawData;
      Bitmap image;
      @Override
      protected Exception doInBackground(Void... params) {
        try {
          if (uri.getScheme().equals(DATA_URL_SCHEME)) {
            String s = uri.toString();
            int cut = s.indexOf(BASE64_MARKER);
            if (cut != -1) {
              s = s.substring(cut + BASE64_MARKER.length());
              rawData = Base64.decode(s, Base64.DEFAULT);  // When to use URL-safe?
            }
          } else {
            int contentLength = -1;
            InputStream is;
            String uriStr = uri.toString();
            if (uriStr.startsWith(ASSET_BASE_URL)) {
              String assetName = uriStr.substring(ASSET_BASE_URL.length());
              is = context.getAssets().open(assetName);
              encoding = null;
            } else {
              // publishProgress(RequestHandler.ProgressType.CONNECTING.ordinal(), 0);
              URLConnection con = uri.toURL().openConnection();
              con.setRequestProperty("UserAgent", "AndroidHtmlView/1.0 (Mobile)");
              if (post != null) {
                con.setDoOutput(true);
                con.getOutputStream().write(post);
              }
              is = con.getInputStream();
              encoding = "ISO-8859-1";  // As per HTTP spec.
              String contentType = con.getContentType();
              if (contentType != null) {
                int cut = contentType.indexOf("charset=");
                if (cut != -1) {
                  encoding = contentType.substring(cut + 8).trim();
                }
              }
              contentLength = con.getContentLength();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[8096];
            while (true) {
              if (contentLength <= 0) {
                //publishProgress(RequestHandler.ProgressType.LOADING_BYTES.ordinal(),
                //   baos.size());
              } else {
                //  publishProgress(RequestHandler.ProgressType.LOADING_PERCENT.ordinal(),
                //    baos.size() * 100 / contentLength);
              }
              int count = is.read(buf);
              if (count <= 0) {
                break;
              }
              baos.write(buf, 0, count);
            }
            is.close();
            //publishProgress(RequestHandler.ProgressType.DONE.ordinal(), 0);
            rawData = baos.toByteArray();
          }
          if (onload instanceof ImageTarget) {
            image = BitmapFactory.decodeByteArray(rawData, 0, rawData.length);
            rawData = null;
          }
          return null;
        } catch (Exception e) {
          encoding = null;
          return e;
        }
      }

      @Override
      protected void onPostExecute(Exception e) {
        if (e != null) {
          Log.e(TAG, "Error loading resource", e);
        } else if (onload instanceof ImageTarget) {
          ((ImageTarget) onload).setImage(image);
        }
      }
/*          switch(onload) {
            case SHOW_HTML:
              loadData(rawData, encoding, uri);
              break;
            case ADD_IMAGE:
              addImage(uri, image);
              break;
            case ADD_STYLE_SHEET:
              if (encoding == null) {
                encoding = HtmlUtils.UTF8;
              }
              try {
                addStyleSheet(uri, new String(rawData, encoding));
              } catch (UnsupportedEncodingException uee) {
                Log.e("HtmlView", "Unsupported Encoding: " + encoding, uee);
              }
              break;
          }
        }
      }

      @Override
      protected void onProgressUpdate(Integer... progress) {
        if (onload == Onload.SHOW_HTML) {
          requestHandler.progress(HtmlView.this,
              RequestHandler.ProgressType.values()[progress[0]], progress[1]);
        }
      }*/
    }.execute(null, null);
  }

  public void openLink(Element element, URI uri) {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.setData(Uri.parse(uri.toString()));
    getContext().startActivity(intent);
  }

  public void requestStyleSheet(HtmlViewGroup rootElement, URI uri) {
    System.out.println("NYI: requestStyleSheet; uri: " + uri);
  }

  public void requestImage(ImageTarget target, URI uri) {
    loadAsync(uri, null, target);
  }

}
