package io.github.jianjianghui.bridge;

import java.util.function.Function;

public interface BridgeRegister {
    void register(String methodName, Function<String,String> function);

}
