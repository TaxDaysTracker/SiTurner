Offshore Days - Android project
===============================

This folder is the source for OffshoreDays.apk. You only need it to make changes
and build a new version.

KEEP app/offshore-days.keystore AND app/keystore.properties SAFE AND PRIVATE.
They sign the app. A new version only installs over the old one (keeping your
trips) if it is signed with this same key. Lose the key and you would have to
uninstall the old app, which deletes its trips (restore them from a backup file).

The app itself is app/src/main/assets/index.html, shown inside a native window
(app/src/main/java/uk/offshoredays/app/MainActivity.java).

To build a new version:
1. Open this folder in Android Studio (File > Open).
2. In app/build.gradle, raise versionCode by 1.
3. Build > Build Bundle(s) / APK(s) > Build APK(s), using the release variant.
