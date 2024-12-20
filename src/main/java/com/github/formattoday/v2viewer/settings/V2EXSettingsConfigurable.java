package com.github.formattoday.v2viewer.settings;

import com.github.formattoday.v2viewer.V2ViewerBundle;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.options.Configurable;
import com.intellij.ui.ColorPanel;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.JBIntSpinner;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * V2EX 设置界面类
 * 负责创建和管理设置界面
 */
public class V2EXSettingsConfigurable implements Configurable {
    // UI 组件
    private MaskedTokenField tokenField;      // Token 输入框
    private JBCheckBox useProxyCheckBox;      // 代理开关
    private JBTextField proxyHostField;       // 代理主机输入框
    private JBTextField proxyPortField;       // 代理端口输入框
    private JBRadioButton httpProxyRadio;     // HTTP 代理选项
    private JBRadioButton socksProxyRadio;    // SOCKS 代��选项
    private JComboBox<String> fontFamilyCombo;// 字体选择
    private JBIntSpinner fontSizeSpinner;     // 字号选择
    private ColorPanel fontColorPanel;        // 字体颜色选择
    private final V2EXSettings settings;      // 设置实例

    /**
     * 构造函数
     */
    public V2EXSettingsConfigurable() {
        settings = V2EXSettings.getInstance();
    }

    /**
     * 获取设置页面显示名称
     */
    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "V2EX Viewer";
    }

    /**
     * 创建设置界面组件
     */
    @Override
    public @Nullable JComponent createComponent() {
        // 创建主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = JBUI.insets(2);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;

        // 创建 API Token 设置面板
        mainPanel.add(createTokenPanel(), c);

        // 创建字体设置面板
        c.gridy = 1;
        mainPanel.add(createFontPanel(), c);

        // 创建代理设置面板
        c.gridy = 2;
        mainPanel.add(createProxyPanel(), c);

        return mainPanel;
    }

    /**
     * 创建 Token 设置面板
     */
    private JPanel createTokenPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(IdeBorderFactory.createTitledBorder("API Token 设置"));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = JBUI.insets(2);

        // 添加 Token 输入框
        c.gridx = 0;
        c.gridy = 0;
        panel.add(new JLabel(V2ViewerBundle.message("settings.token") + ":"), c);

        tokenField = new MaskedTokenField(settings.apiToken);
        c.gridx = 1;
        c.weightx = 1.0;
        panel.add(tokenField, c);

        // 添加获取 Token 链接
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        ActionLink tokenLink = new ActionLink(V2ViewerBundle.message("settings.token.get"), (ActionListener) e ->
                BrowserUtil.browse("https://v2ex.com/settings/tokens")
        );
        panel.add(tokenLink, c);

        return panel;
    }

    /**
     * 创建字体设置面板
     */
    private JPanel createFontPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(IdeBorderFactory.createTitledBorder("字体设置"));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = JBUI.insets(2);

        // 添加字体选择
        c.gridx = 0;
        c.gridy = 0;
        panel.add(new JLabel("字体:"), c);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = ge.getAvailableFontFamilyNames();
        fontFamilyCombo = new JComboBox<>(fontNames);
        fontFamilyCombo.setSelectedItem(settings.fontFamily);
        c.gridx = 1;
        c.weightx = 1.0;
        panel.add(fontFamilyCombo, c);

        // 添加字号选择
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        panel.add(new JLabel("字号:"), c);

        fontSizeSpinner = new JBIntSpinner(settings.fontSize, 8, 72, 1);
        c.gridx = 1;
        panel.add(fontSizeSpinner, c);

        // 添加颜色选择
        c.gridx = 0;
        c.gridy = 2;
        panel.add(new JLabel("颜色:"), c);

        fontColorPanel = new ColorPanel();
        fontColorPanel.setSelectedColor(settings.fontColor);
        c.gridx = 1;
        panel.add(fontColorPanel, c);

        return panel;
    }

    /**
     * 创建代理设置面板
     */
    private JPanel createProxyPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(IdeBorderFactory.createTitledBorder(V2ViewerBundle.message("settings.proxy")));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = JBUI.insets(2);

        // 添加代理开关
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        useProxyCheckBox = new JBCheckBox(V2ViewerBundle.message("settings.proxy.use"), settings.useProxy);
        panel.add(useProxyCheckBox, c);

        // 添加代理类型选择
        c.gridy = 1;
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ButtonGroup group = new ButtonGroup();
        httpProxyRadio = new JBRadioButton("HTTP", "HTTP".equals(settings.proxyType));
        socksProxyRadio = new JBRadioButton("SOCKS", "SOCKS".equals(settings.proxyType));
        group.add(httpProxyRadio);
        group.add(socksProxyRadio);
        typePanel.add(httpProxyRadio);
        typePanel.add(socksProxyRadio);
        panel.add(typePanel, c);

        // 添加代理主机设置
        c.gridy = 2;
        c.gridwidth = 1;
        panel.add(new JLabel(V2ViewerBundle.message("settings.proxy.host") + ":"), c);

        proxyHostField = new JBTextField(settings.proxyHost);
        c.gridx = 1;
        c.weightx = 1.0;
        panel.add(proxyHostField, c);

        // 添加代理端口设置
        c.gridy = 3;
        c.gridx = 0;
        c.weightx = 0;
        panel.add(new JLabel(V2ViewerBundle.message("settings.proxy.port") + ":"), c);

        proxyPortField = new JBTextField(String.valueOf(settings.proxyPort));
        c.gridx = 1;
        c.weightx = 1.0;
        panel.add(proxyPortField, c);

        // 设置代理字段状态
        updateProxyFieldsState();
        useProxyCheckBox.addActionListener(e -> updateProxyFieldsState());

        return panel;
    }

    /**
     * 更新代理相关字段的启用状态
     */
    private void updateProxyFieldsState() {
        boolean enabled = useProxyCheckBox.isSelected();
        httpProxyRadio.setEnabled(enabled);
        socksProxyRadio.setEnabled(enabled);
        proxyHostField.setEnabled(enabled);
        proxyPortField.setEnabled(enabled);
    }

    /**
     * 检查设置是否被修改
     */
    @Override
    public boolean isModified() {
        return !settings.apiToken.equals(tokenField.getText()) ||
                settings.useProxy != useProxyCheckBox.isSelected() ||
                !settings.proxyHost.equals(proxyHostField.getText()) ||
                settings.proxyPort != getProxyPort() ||
                !settings.proxyType.equals(getProxyType()) ||
                !settings.fontFamily.equals(fontFamilyCombo.getSelectedItem()) ||
                settings.fontSize != fontSizeSpinner.getNumber() ||
                !settings.fontColor.equals(fontColorPanel.getSelectedColor());
    }

    /**
     * 获取当前选择的代理类型
     */
    private String getProxyType() {
        return httpProxyRadio.isSelected() ? "HTTP" : "SOCKS";
    }

    /**
     * 获取当前设置的代理端口
     */
    private int getProxyPort() {
        try {
            return Integer.parseInt(proxyPortField.getText());
        } catch (NumberFormatException e) {
            return 10808;
        }
    }

    /**
     * 应用设置更改
     */
    @Override
    public void apply() {
        settings.apiToken = tokenField.getText();
        settings.useProxy = useProxyCheckBox.isSelected();
        settings.proxyHost = proxyHostField.getText();
        settings.proxyPort = getProxyPort();
        settings.proxyType = getProxyType();
        settings.fontFamily = (String) fontFamilyCombo.getSelectedItem();
        settings.fontSize = fontSizeSpinner.getNumber();
        settings.fontColor = fontColorPanel.getSelectedColor();
        settings.notifySettingsChanged();
    }
} 