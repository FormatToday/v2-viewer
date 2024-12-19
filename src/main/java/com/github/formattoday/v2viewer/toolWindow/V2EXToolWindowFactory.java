package com.github.formattoday.v2viewer.toolWindow;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

/**
 * V2EX 工具窗口工厂类
 * 负责创建和初始化工具窗口
 */
public class V2EXToolWindowFactory implements ToolWindowFactory, DumbAware {

    /**
     * 创建工具窗口内容
     *
     * @param project    当前项目
     * @param toolWindow 工具窗口实例
     */
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // 创建主面板
        V2EXNewsPanel v2exPanel = new V2EXNewsPanel(project);

        // 创建内容并添加到工具窗口
        Content content = ContentFactory.getInstance().createContent(
                v2exPanel.getContent(), // 面板内容
                "",                     // 标题（空）
                false                   // 不允许关闭
        );

        // 将内容添加到工具窗口
        toolWindow.getContentManager().addContent(content);
    }
} 