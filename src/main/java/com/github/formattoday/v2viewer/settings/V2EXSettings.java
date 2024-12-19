package com.github.formattoday.v2viewer.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.ui.JBColor;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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

    // 监听器列表
    private final List<SettingsChangeListener> listeners = new ArrayList<>();
    // 字体设置
    public String fontFamily = "仿宋";  // 默认字体
    public int fontSize = 14;                      // 默认字号
    public Color fontColor = JBColor.BLACK;          // 默认颜色

    /**
     * 添加设置变更监听器
     */
    public void addChangeListener(SettingsChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * 移除设置变更监听器
     */
    public void removeChangeListener(SettingsChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * 通知所有监听器设置已变更
     */
    public void notifySettingsChanged() {
        for (SettingsChangeListener listener : listeners) {
            listener.onSettingsChanged();
        }
    }

    /**
     * 设置变更监听器接口
     */
    public interface SettingsChangeListener {
        void onSettingsChanged();
    }

    /**
     * 获取设置实例
     */
    public static V2EXSettings getInstance() {
        return ApplicationManager.getApplication().getService(V2EXSettings.class);
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