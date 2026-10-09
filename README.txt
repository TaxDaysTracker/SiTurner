Offshore Days - install on Android
==================================

The app needs to be hosted on a web address (https) once. After that it installs
on your phone and works offline. Your trips stay on the phone.

Option A: GitHub Pages (free, permanent)
1. Sign in at github.com and create a new public repository, e.g. "offshore-days".
2. Choose "uploading an existing file" and drag in everything from this folder
   (index.html, manifest.webmanifest, sw.js and the icons folder). Commit.
3. Open Settings > Pages. Under "Build and deployment", pick "Deploy from a branch",
   branch "main", folder "/ (root)", and Save.
4. After a minute your app is at https://YOUR-USERNAME.github.io/offshore-days/

Option B: Netlify Drop
1. Go to app.netlify.com/drop and drag this whole folder onto the page.
2. Sign in when asked so the site stays up, and note the address it gives you.

Install on the phone
1. Open the address in Chrome on your Android phone.
2. Tap the three-dot menu, then "Install app" (or "Add to Home screen").
3. Open Offshore Days from your home screen. It runs full screen and offline.

Keeping your data safe
Trips are stored inside Chrome on this phone. Clearing Chrome's data for the site
or uninstalling the app removes them, so use the app's menu > "Save backup" from
time to time. "Restore backup" loads a backup file on a new phone.

Updating the app
If you change any file, also change VERSION at the top of sw.js so phones pick up
the new version.
