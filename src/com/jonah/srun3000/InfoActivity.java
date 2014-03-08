package com.jonah.srun3000;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * Created by Jonah on 13-9-9.
 */
public class InfoActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Uri uri = (Uri)getIntent().getParcelableExtra("uri");
        WebView webview = new WebView(this);
        setContentView(webview);
        webview.loadUrl(uri.toString());
    }


}
