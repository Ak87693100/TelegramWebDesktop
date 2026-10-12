package com.example.telegramwebdesktop;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;

public class IncognitoActivity extends Activity {
    private WebView web;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        Button close = new Button(this); close.setText("Exit Incognito and erase data");
        close.setOnClickListener(v -> { clearAndClose(); });
        root.addView(close);
        web = new WebView(this);
        WebSettings s = web.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true);
        s.setCacheMode(WebSettings.LOAD_NO_CACHE); s.setSaveFormData(false); s.setAllowFileAccess(false); s.setAllowContentAccess(false);
        web.setWebViewClient(new WebViewClient()); web.setWebChromeClient(new WebChromeClient());
        root.addView(web, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        web.loadUrl("https://web.telegram.org/");
    }
    private void clearAndClose() {
        if (web != null) {
            web.stopLoading(); web.loadUrl("about:blank"); web.clearHistory(); web.clearCache(true); web.clearFormData();
            web.clearSslPreferences(); web.clearView();
        }
        CookieManager.getInstance().removeAllCookies(value -> {
            CookieManager.getInstance().flush();
            if (web != null) { web.destroy(); web = null; }
            finish();
        });
    }
    @Override public void onBackPressed() { clearAndClose(); }
    @Override protected void onDestroy() { if (web != null) { web.destroy(); web = null; } super.onDestroy(); }
}
