package com.alc4phase1challenge.ronaldlumor;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class AboutActivity extends AppCompatActivity implements NetworkChangeReceiver.ConnectionChangeCallback{
    private static final String URL = "https://andela.com/alc/";
    private boolean isLoaded = true;

    private AlertDialog.Builder mConnectDialog;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ((ProgressBar) findViewById(R.id.progressBar)).setProgressTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_blue_dark)));
            ((ProgressBar) findViewById(R.id.progressBar)).setProgressBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.white)));
        }

        NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        networkChangeReceiver.setConnectionChangeCallback(AboutActivity.this);

        ((WebView) findViewById(R.id.webView)).getSettings().setJavaScriptEnabled(true);
        ((WebView) findViewById(R.id.webView)).getSettings().setBuiltInZoomControls(true);
        ((WebView) findViewById(R.id.webView)).setWebViewClient(new WebViewClient(){
            @Override public void onReceivedError(WebView view,
                                                  WebResourceRequest request,
                                                  WebResourceError error) {
                isLoaded = false;
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                onConnectError();
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                super.onPageFinished(view, url);

                if(mConnectDialog != null) {
                    AlertDialog dialog = mConnectDialog.create();
                    dialog.dismiss();
                }
            }
        });
        ((WebView) findViewById(R.id.webView)).loadUrl(URL);
    }

    private void reloadUrl(){
        ((WebView) findViewById(R.id.webView)).reload();
    }

    @Override
    public void onConnectionChange(boolean isConnected) {
        if(isConnected) {
            if(!isLoaded) reloadUrl();
        }
    }

    private void onConnectError(){
        mConnectDialog = new AlertDialog.Builder(AboutActivity.this);
        mConnectDialog.setTitle("NO CONNECTION");
        mConnectDialog.setMessage("Please, turn on your mobile data connection!");
        mConnectDialog.setPositiveButton(getString(R.string.ok_title), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                reloadUrl();
            }
        });
        mConnectDialog.setCancelable(true);
        mConnectDialog.show();
    }

    @Override
    public void onBackPressed() {
        ((WebView) findViewById(R.id.webView)).destroy();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        ((WebView) findViewById(R.id.webView)).destroy();
        super.onDestroy();
    }
}