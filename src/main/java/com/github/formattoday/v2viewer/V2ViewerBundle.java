package com.github.formattoday.v2viewer;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

/**
 * 插件国际化资源类
 */
public final class V2ViewerBundle extends DynamicBundle {
    // 资源文件路径
    @NonNls
    private static final String BUNDLE = "messages.V2ViewerBundle";

    // 单例实例
    private static final V2ViewerBundle INSTANCE = new V2ViewerBundle();

    private V2ViewerBundle() {
        super(BUNDLE);
    }

    /**
     * 获取国际化消息
     */
    public static String message(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return INSTANCE.getMessage(key, params);
    }
} 