// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.android.apps.cloudprint;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import java.io.FileOutputStream;

/** Cloud Print Demo application. */
public class CloudPrintDemo extends Activity {
  
  /** Holds HTML content passed from WebView. */
  private class HtmlContentHolder {
    String htmlContent;
    
    /** Sets HTML content to hold. */
    public void setHtmlContent(String htmlContent) {
      this.htmlContent = htmlContent;
    }
    
    /** Returns HTML content as string.*/
    public String getHtmlContentAsString() {
      return htmlContent;
    }
    
    /** Returns URI of temporary file which contains HTML content.*/
    public Uri getHtmlContentAsFile() {
      String tmpFileName = "webpage_tmp.html";
      try {
        // Create a file to save give string in.
        FileOutputStream fos = openFileOutput(tmpFileName, MODE_PRIVATE);
        // Write string into the file and flush the output stream.
        fos.write(htmlContent.getBytes());
        fos.flush();
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
      
      // Get URI of the created file.
      return Uri.fromFile(getFileStreamPath(tmpFileName));
    }
  }
  
  /** WebBrowser client. */
  private class WebBrowserClient extends WebViewClient {
    private final EditText urlBox;
    private final WebView view;
    private final HtmlContentHolder htmlContentHolder = new HtmlContentHolder();
    
    WebBrowserClient(EditText urlBox, WebView view) {
      this.urlBox = urlBox;
      this.view = view;
      
      // Allow JavaScript execution in the web view.
      view.getSettings().setJavaScriptEnabled(true);
      view.addJavascriptInterface(htmlContentHolder, "HtmlContentHolder");
    }
    
    /** Returns URI to the file that contains HTML content of the opened web page. */
    public Uri getHtmlContentAsFile() {
      return htmlContentHolder.getHtmlContentAsFile();
    }
    
    @Override
    public void onPageFinished(WebView view, String url) {
      view.loadUrl("javascript:window.HtmlContentHolder.setHtmlContent(" +
          "new XMLSerializer().serializeToString(document));");      
    }
    
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      urlBox.setText(url);
      view.loadUrl(url);
      return false;
    }
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
        
    // References to URL box and web view.
    final EditText urlBox = (EditText)findViewById(R.id.url_box);
    final WebView webView = (WebView) findViewById(R.id.web_browser);
  
    // Create and set web browser client.
    final WebBrowserClient webBrowserClient = new WebBrowserClient(urlBox, webView);
    webView.setWebViewClient(webBrowserClient);

    // Add a listener for "Go" button.
    Button goButton = (Button)findViewById(R.id.go_button);
    goButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        webView.loadUrl(urlBox.getText().toString());
      }
    });
    goButton.callOnClick();
       
    // Create "Cloud Print" intent. 
    final Intent printIntent = new Intent(this, CloudPrintDialog.class);

    // Add a listener for "Print" button.
    Button printButton = (Button) findViewById(R.id.print_button);
    printButton.setOnClickListener(new OnClickListener() {
      public void onClick(View view) {
        Uri htmlContentFile = webBrowserClient.getHtmlContentAsFile();

        printIntent.setDataAndType(htmlContentFile, "text/html");
        printIntent.putExtra("title", webView.getTitle());;

        startActivity(printIntent);
      }
    });
  }
}
