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
  /** WebBrowser client. */
  private class WebBrowserClient extends WebViewClient {
    private final EditText urlBox;
    
    WebBrowserClient(EditText urlBox) {
      this.urlBox = urlBox;
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
    final WebView webBrowser = (WebView) findViewById(R.id.web_browser);
    // Allow JavaScript execution in the web view.
    webBrowser.getSettings().setJavaScriptEnabled(true);

    // Add a listener for "Go" button.
    Button goButton = (Button)findViewById(R.id.go_button);
    goButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        webBrowser.setWebViewClient(new WebBrowserClient(urlBox));
        webBrowser.loadUrl(urlBox.getText().toString());
      }
    });
    goButton.callOnClick();
 
    // Add a listener for "Print" button.
    Button printButton = (Button) findViewById(R.id.print_button);
    printButton.setOnClickListener(new OnClickListener() {
      public void onClick(View view) {
        webBrowser.loadUrl("javascript:window.PrintInterface.printHtml(" +
            "new XMLSerializer().serializeToString(document));");
      }
    });
    
    // Create "Cloud Print" intent and interface for JavaScript->Java bridge. 
    Intent printIntent = new Intent(this, CloudPrintDialog.class);
    webBrowser.addJavascriptInterface(
        new PrintInterface(webBrowser, printIntent), "PrintInterface");
    }


  
  /** Interface to be used as JavaScrtip->Java bridge to pass content of HTML page. */
  private class PrintInterface {
	  private final WebView webBrowser;
	  private final Intent printIntent;
	  
	  PrintInterface(WebView webBrowser, Intent printIntent) {
		  this.webBrowser = webBrowser;
		  this.printIntent = printIntent;
	  }

    /**
     * Prints given HTML content.
     *
     * @param htmlContent to print
     * @throws Exception if failed to print the given content
     */
	  @SuppressWarnings("unused")
    public void printHtml(String htmlContent) throws Exception {
	    // Set data to be printed.
      printIntent.setDataAndType(dumpHtmlToFile(htmlContent), "text/html");
      // Set title to be used.
      printIntent.putExtra("title", webBrowser.getTitle());
      // Start intent.
      startActivity(printIntent);
    }
    
    /**
     * Dumps HTML string into a file and returns URI of the file.
     *
     * @param htmlContent to dump
     * @return URI of the file, containing given HTML string
     * @throws Exception in case if failed to create file or save it to disk
     */
    private Uri dumpHtmlToFile(String htmlContent) throws Exception {
        String tmpFileName = "webpage_tmp.html";
        // Create a file to save give string in.
        FileOutputStream fos = openFileOutput(tmpFileName, MODE_PRIVATE);
        // Write string into the file and flush the output stream.
        fos.write(htmlContent.getBytes());
        fos.flush();
        
        // Get URI of the created file.
        return Uri.fromFile(getFileStreamPath(tmpFileName));   
    }
  }  
}
