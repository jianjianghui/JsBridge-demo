package io.github.jianjianghui.bridge.adapter.lzyzsd;

import com.github.lzyzsd.jsbridge.BridgeWebView;

import java.util.function.Function;

import io.github.jianjianghui.bridge.BridgeRegister;


public class LzyzsdBridgeRegister implements BridgeRegister {
    final BridgeWebView webView;

    public LzyzsdBridgeRegister( BridgeWebView webView) {
        this.webView = webView;
    }


    @Override
    public void register(String methodName, Function<String,String> function) {
        webView.registerHandler(methodName, (data, callback) -> {
            String result = function.apply(data);
            callback.onCallBack(result);
        });
    }
    
}
