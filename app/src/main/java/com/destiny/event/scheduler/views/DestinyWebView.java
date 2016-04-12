package com.destiny.event.scheduler.views;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.destiny.event.scheduler.utils.CookiesUtils;

public class DestinyWebView extends WebView {
    protected static final String TAG = DestinyWebView.class.getSimpleName();
    public static final String REDIRECT_FINISH = "https://www.bungie.net/";
    private DestinyListener mListener;

    private String serverToken = "";
    private String xRefToken = "";

    public DestinyWebView(Context context) {
        super(context);
        init();
    }

    public DestinyWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DestinyWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }



    @TargetApi(21)
    public DestinyWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @TargetApi(11)
    public DestinyWebView(Context context, AttributeSet attrs, int defStyleAttrs, boolean privateBrowsing) {
        super(context, attrs, defStyleAttrs, privateBrowsing);
        init();
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void init() {
        requestFocus();
        getSettings().setJavaScriptEnabled(true);
        setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String loadingUrl) {
                Log.e(TAG, loadingUrl);
                mListener.onPageChanged(loadingUrl);
                if (REDIRECT_FINISH.equals(loadingUrl)) {
                    serverToken = CookiesUtils.getCookies(loadingUrl);
                    Log.e("SERVER_TOKEN", serverToken);
                    if (mListener != null) {
                        if (serverToken != null) {
                            xRefToken = CookiesUtils.getCrossReferenceToken(serverToken);
                            mListener.onUserLoggedIn(serverToken, xRefToken);
                            Log.e("CROSS_REF", xRefToken);
                        } else {
                            Log.e("STATUS_LOGIN", "Login falhou!");
                            mListener.onLoginFailed();
                        }
                    }
                    clearUserCookies();
                } else {
                }

                return false;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                if (mListener != null) {
                    mListener.onLoginFailed();
                }
            }
        });
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP) {
            if (!hasFocus()) {
                requestFocus();
            }
        }
        return super.onTouchEvent(event);
    }

    public void setListener(DestinyListener listener) {
        mListener = listener;
    }

    public void loadLoginUrl(String url) {
        super.loadUrl(url);
    }

    public void clearUserCookies() {
        CookieManager.getInstance().removeAllCookie();
    }

    @TargetApi(21)
    public void clearUserCookies(ValueCallback<Boolean> callback) {
        CookieManager.getInstance().removeAllCookies(callback);
    }

    public interface DestinyListener {

        void onUserLoggedIn(String cookies, String crossRefToken);

        void onLoginFailed();

        void onPageChanged(String url);

    }

}
