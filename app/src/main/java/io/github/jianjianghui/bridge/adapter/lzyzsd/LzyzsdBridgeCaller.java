package io.github.jianjianghui.bridge.adapter.lzyzsd;

import com.github.lzyzsd.jsbridge.BridgeWebView;

import java.util.function.Consumer;

import io.github.jianjianghui.bridge.BridgeCaller;


public class LzyzsdBridgeCaller implements BridgeCaller {
    final BridgeWebView webView;

    public LzyzsdBridgeCaller(BridgeWebView webView) {
        this.webView = webView;
    }


    @Override
    public void call(String methodName, String params, Consumer<String> callback) {
        webView.callHandler(methodName, params, callback::accept);
    }
}
