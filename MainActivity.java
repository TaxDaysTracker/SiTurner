package uk.offshoredays.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Offshore Days: hosts the tracker page (assets/index.html) in a WebView.
 * Trips are kept in the WebView's local storage inside this app's private data.
 * The page calls AndroidBridge.saveFile(...) for backups and CSV exports, which opens
 * the system "Save to" screen; the file input on the page opens the system file picker.
 */
public class MainActivity extends Activity {

    private static final int REQUEST_SAVE = 1;
    private static final int REQUEST_OPEN = 2;

    private WebView webView;
    private String pendingSaveContent;
    private ValueCallback<Uri[]> pendingFileCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setSupportZoom(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Keep the app on its own page; open anything else outside the app.
                if (url.startsWith("file:///android_asset/")) return false;
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (Exception ignored) {
                }
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            // Having a WebChromeClient also makes the page's confirm() dialogs work.
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback,
                                             FileChooserParams params) {
                if (pendingFileCallback != null) pendingFileCallback.onReceiveValue(null);
                pendingFileCallback = callback;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                try {
                    startActivityForResult(Intent.createChooser(intent, "Choose a backup file"), REQUEST_OPEN);
                } catch (Exception e) {
                    pendingFileCallback = null;
                    return false;
                }
                return true;
            }
        });

        webView.addJavascriptInterface(new Bridge(), "AndroidBridge");

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl("file:///android_asset/index.html");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    private class Bridge {
        @JavascriptInterface
        public void saveFile(final String name, final String mimeType, final String content) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pendingSaveContent = content;
                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType(mimeType == null || mimeType.isEmpty() ? "application/octet-stream" : mimeType);
                    intent.putExtra(Intent.EXTRA_TITLE, name);
                    try {
                        startActivityForResult(intent, REQUEST_SAVE);
                    } catch (Exception e) {
                        pendingSaveContent = null;
                        reportSave(false);
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OPEN) {
            if (pendingFileCallback != null) {
                pendingFileCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                pendingFileCallback = null;
            }
        } else if (requestCode == REQUEST_SAVE) {
            boolean ok = false;
            Uri uri = data != null ? data.getData() : null;
            if (resultCode == RESULT_OK && uri != null && pendingSaveContent != null) {
                try (OutputStream out = getContentResolver().openOutputStream(uri, "wt")) {
                    if (out != null) {
                        out.write(pendingSaveContent.getBytes(StandardCharsets.UTF_8));
                        ok = true;
                    }
                } catch (Exception ignored) {
                }
            }
            pendingSaveContent = null;
            reportSave(ok);
        }
    }

    private void reportSave(boolean ok) {
        webView.evaluateJavascript("window.__saveDone && window.__saveDone(" + ok + ");", null);
    }

    @Override
    public void onBackPressed() {
        // Back closes an open sheet first; otherwise it leaves the app.
        webView.evaluateJavascript(
                "(function(){var d=document.querySelector('dialog[open]');if(d){d.close();return 'closed';}return 'none';})()",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        if (!"\"closed\"".equals(value)) MainActivity.super.onBackPressed();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
