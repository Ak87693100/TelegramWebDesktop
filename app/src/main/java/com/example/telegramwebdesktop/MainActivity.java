package com.example.telegramwebdesktop;

import android.app.AlertDialog;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.biometric.BiometricPrompt;
import androidx.biometric.BiometricManager;
import androidx.core.content.ContextCompat;

public class MainActivity extends FragmentActivity {
    private WebView web;
    private SharedPreferences prefs;
    private boolean authenticated = false;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        if (prefs.getBoolean("lock_enabled", false)) { authenticate(); } else { showHome(); }
    }

    private void authenticate() {
        int can = BiometricManager.from(this).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
        if (can != BiometricManager.BIOMETRIC_SUCCESS) {
            new AlertDialog.Builder(this).setTitle("Screen lock unavailable")
                .setMessage("Set a device PIN/password and enroll biometrics in Android Settings first.")
                .setPositiveButton("Continue", (d,w) -> showHome()).setCancelable(false).show();
            return;
        }
        BiometricPrompt prompt = new BiometricPrompt(this, ContextCompat.getMainExecutor(this),
            new BiometricPrompt.AuthenticationCallback() {
                @Override public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    authenticated = true; showHome();
                }
                @Override public void onAuthenticationError(int code, CharSequence msg) {
                    new AlertDialog.Builder(MainActivity.this).setMessage("Authentication cancelled. Try again to unlock.")
                        .setPositiveButton("Retry", (d,w) -> authenticate()).setNegativeButton("Exit", (d,w) -> finish()).show();
                }
            });
        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Telegram Web")
            .setSubtitle("Use fingerprint, face, or your device screen lock")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build();
        prompt.authenticate(info);
    }

    private void showHome() {
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        LinearLayout bar = new LinearLayout(this);
        Button lock = new Button(this); lock.setText(prefs.getBoolean("lock_enabled", false) ? "Disable lock" : "Enable lock");
        lock.setOnClickListener(v -> {
            if (prefs.getBoolean("lock_enabled", false)) {
                new AlertDialog.Builder(this).setTitle("Disable app lock?")
                    .setMessage("Confirm with device authentication first.")
                    .setPositiveButton("Authenticate", (d,w) -> authenticateToDisable()).setNegativeButton("Cancel", null).show();
            } else {
                prefs.edit().putBoolean("lock_enabled", true).apply();
                lock.setText("Disable lock");
                new AlertDialog.Builder(this).setMessage("App lock enabled. It uses your Android device's biometric or screen-lock credential.")
                    .setPositiveButton("OK", null).show();
            }
        });
        Button incognito = new Button(this); incognito.setText("Incognito");
        incognito.setOnClickListener(v -> startActivity(new Intent(this, IncognitoActivity.class)));
        Button layout = new Button(this); layout.setText(prefs.getBoolean("desktop", true) ? "Desktop: ON" : "Desktop: OFF");
        layout.setOnClickListener(v -> {
            prefs.edit().putBoolean("desktop", !prefs.getBoolean("desktop", true)).apply();
            layout.setText(prefs.getBoolean("desktop", true) ? "Desktop: ON" : "Desktop: OFF");
            if (web != null) { configureUserAgent(); web.reload(); }
        });
        bar.addView(lock, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        bar.addView(incognito, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        bar.addView(layout, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        web = new WebView(this);
        WebSettings s = web.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true);
        s.setAllowFileAccess(false); s.setAllowContentAccess(false); s.setSupportMultipleWindows(false);
        web.setWebViewClient(new WebViewClient()); web.setWebChromeClient(new WebChromeClient());
        configureUserAgent();
        root.addView(bar); root.addView(web, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root); web.loadUrl("https://web.telegram.org/");
    }

    private void authenticateToDisable() {
        BiometricPrompt prompt = new BiometricPrompt(this, ContextCompat.getMainExecutor(this), new BiometricPrompt.AuthenticationCallback() {
            @Override public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                prefs.edit().putBoolean("lock_enabled", false).apply(); recreate();
            }
        });
        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder().setTitle("Confirm to disable app lock")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL).build();
        prompt.authenticate(info);
    }

    private void configureUserAgent() {
        if (web == null) return;
        String ua = WebSettings.getDefaultUserAgent(this);
        if (prefs.getBoolean("desktop", true)) ua = ua.replace("Mobile", "").replace("Android", "X11; Linux x86_64");
        web.getSettings().setUserAgentString(ua);
    }
    @Override protected void onResume() {
        super.onResume();
        if (prefs != null && prefs.getBoolean("lock_enabled", false) && authenticated) { authenticated = false; }
    }
    @Override public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
}
