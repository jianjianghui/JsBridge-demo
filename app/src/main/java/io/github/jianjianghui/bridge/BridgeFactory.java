package io.github.jianjianghui.bridge;


import android.app.Application;
import android.content.Context;
import android.webkit.WebView;

import io.github.jianjianghui.bridge.adapter.lzyzsd.LzyzsdBridgeCaller;
import io.github.jianjianghui.bridge.adapter.lzyzsd.LzyzsdBridgeRegister;
import kotlin.Pair;

public class BridgeFactory {
    public static Pair<BridgeRegister, BridgeCaller> get( WebView webView) {
        if (webView instanceof com.github.lzyzsd.jsbridge.BridgeWebView) {
            return new Pair<>(new LzyzsdBridgeRegister((com.github.lzyzsd.jsbridge.BridgeWebView) webView), new LzyzsdBridgeCaller((com.github.lzyzsd.jsbridge.BridgeWebView) webView));
        }

        return null;

    }
}
