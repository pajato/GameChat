package com.pajato.android.gamechat.help;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebViewFragment;

/**
 * Fragment for help content.
 */
public class HelpContentFragment extends WebViewFragment {

    private static final String KEY_FILE = "file";

    static HelpContentFragment newInstance(String file) {
        HelpContentFragment fragment = new HelpContentFragment();
        Bundle args = new Bundle();
        args.putString(KEY_FILE, file);
        fragment.setArguments(args);
        return(fragment);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result= super.onCreateView(inflater, container, savedInstanceState);
        getWebView().getSettings().setJavaScriptEnabled(true);
        getWebView().getSettings().setSupportZoom(true);
        getWebView().getSettings().setBuiltInZoomControls(true);
        getWebView().loadUrl(getPage());
        return(result);
    }

    private String getPage() {
        return(getArguments().getString(KEY_FILE));
    }
}
