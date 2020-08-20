package com.luckyspinner.app.fragemnts;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.facebook.applinks.AppLinkData;
import com.luckyspinner.app.R;
import com.onesignal.OneSignal;

public class WebViewFragment extends Fragment {
    private static final String TAG = "WEB_VIEW_FRAGMENT";
    private boolean nobot;
    private String fullUrl;
    private OnModerationDetect onModeration;
    private SharedPreferences sharedPreferences;
    private String currentUrl;
    private WebView webView;
    private Context context;
    private AppLinkData appLinkData;
    private Uri uri;

    public WebViewFragment() {
    }

    public WebViewFragment(OnModerationDetect onModerationDetect, AppLinkData appLinkData) {
        this.appLinkData = appLinkData;
        this.onModeration = onModerationDetect;
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
        sharedPreferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
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
        webView.setWebViewClient( new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!url.contains(getDeepParams())) {
                    url = url + getDeepParams();
                }
                view.loadUrl(url);
                return false;
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                Log.d(TAG, "onReceivedHttpError: " +  errorResponse.getStatusCode());
                if (errorResponse.getStatusCode() > 500) {
                   onModeration.onModeration();
                } else {
                   super.onReceivedHttpError(view, request, errorResponse);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (url.equals(context.getResources().getString(R.string.redirected_url))) {
                    editor.putBoolean("nobot", false);
                    OneSignal.sendTag("nobot", "1");
                    editor.apply();
                    onModeration.onModeration();
                } else if (url.equals(context.getResources().getString(R.string.redirected_nobot_url))) {
                    editor.putBoolean("nobot", true);
                    editor.apply();
                    view.stopLoading();
                    view.loadUrl(fullUrl + getDeepParams());
                } else {
                    Log.d(TAG, "onPageFinishedUrl: " + url);
                    editor.putString("url", url);
                    editor.apply();
                    view.setVisibility(View.VISIBLE);
                }
            }
        });
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            initWebView();
        }
        return view;
    }

    private void initWebView() {
        sharedPreferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        nobot = sharedPreferences.getBoolean("nobot", false);
        currentUrl = sharedPreferences.getString("url", null);
        String deepParams =  getDeepParams();
        if (nobot) {
            if(currentUrl != null) {
                if(!currentUrl.contains(deepParams)) {
                    currentUrl = currentUrl + deepParams;
                }
                webView.loadUrl(currentUrl);
            } else {
                webView.loadUrl(fullUrl + deepParams);
            }
        }
        if (!nobot) {
            webView.loadUrl(context.getResources().getString(R.string.url_1));
        }
    }

    public interface OnModerationDetect {
        void onModeration();
    }

    private String getDeepParams() {
    String deepParams = "";
        if (appLinkData != null) {
            uri = appLinkData.getTargetUri();
        }
        String query;
        if (uri != null && (query = uri.getQuery()) != null && nobot) {
            deepParams = "&" + query;
            Log.d(TAG, "query: " + query);
        }
        Log.d(TAG, "getDeepParams: "+ deepParams);
        return  deepParams;
    }
}
