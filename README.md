# Telegram Web Desktop (unofficial Android wrapper)

This is an unofficial Android WebView wrapper for https://web.telegram.org/.

## Build
GitHub Actions builds a debug APK. Open the Actions tab and run **Build Android APK**, then download the artifact.

## Security notes
- Uses Android device credential/biometric prompt for app lock (when enabled).
- WebView file/content access is disabled and cleartext traffic is disabled.
- Incognito uses a separate Android process and clears WebView cookies/cache on exit. Android's CookieManager cookies are shared at app UID level in some configurations; do not treat this as a hardened privacy boundary or use the normal session at the same time.
- This is not official Telegram software. Never enter Telegram credentials into a page other than the official https://web.telegram.org/ domain.
- The wrapper does not save Telegram passwords; Telegram Web itself handles its session cookies.
- Debug APK is unsigned for release distribution and is for personal testing only.
