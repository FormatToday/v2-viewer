package com.github.formattoday.v2viewer.settings;

import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * V2EX 设置状态类
 * 负责保存和加载插件设置
 */
@State(
    name = "com.github.formattoday.v2viewer.settings.V2EXSettings",
    storages = @Storage("v2exViewer.xml")
)
public class V2EXSettings implements PersistentStateComponent<V2EXSettings> {
    // API Token 设置
    public String apiToken = "";
    
    // 代理设置
    public boolean useProxy = false;        // 是否使用代理
    public String proxyHost = "127.0.0.1";  // 代理主机地址
    public int proxyPort = 10808;           // 代理端口
    public String proxyType = "SOCKS";      // 代理类型（SOCKS/HTTP）
    
    // 显示设置
    public String fontFamily = "JetBrains Mono";  // 字体
    public int fontSize = 14;                     // 字号
    public boolean useBoldTitle = true;          // 标题是否使用粗体

    /**
     * 获取设置实例
     */
    public static V2EXSettings getInstance() {
        return ServiceManager.getService(V2EXSettings.class);
    }

    /**
     * 获取当前状态
     */
    @Override
    public @Nullable V2EXSettings getState() {
        return this;
    }

    /**
     * 加载状态
     */
    @Override
    public void loadState(@NotNull V2EXSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }
} 