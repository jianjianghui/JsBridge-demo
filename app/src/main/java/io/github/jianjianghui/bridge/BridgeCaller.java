package io.github.jianjianghui.bridge;

import java.util.function.Consumer;

public interface BridgeCaller {
    void call(String methodName, String params, Consumer<String> callback);
}
