package com.github.formattoday.v2viewer.toolWindow;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.ActionLink;
import com.intellij.util.ui.JBUI;
import com.github.formattoday.v2viewer.V2ViewerBundle;
import com.github.formattoday.v2viewer.settings.V2EXSettings;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * V2EX 新闻面板
 * 负责显示主题列表和内容
 */
public class V2EXNewsPanel {
    // UI 组件
    private final JPanel mainPanel;           // 主面板
    private final JPanel contentPanel;        // 内容面板
    private final JTextPane contentArea;      // 内容区域
    private final Project project;            // 项目实例
    
    // HTTP 客户端
    private final OkHttpClient.Builder clientBuilder;
    
    // 数据状态
    private final List<TopicInfo> currentTopics = new ArrayList<>();  // 当前主题列表
    private boolean isShowingList = true;     // 是否显示列表视图
    private static final int REPLIES_PER_PAGE = 20;  // 每页回复数
    private int currentPage = 1;              // 当前页码
    private int totalReplies = 0;             // 总回复数
    private int currentTopicId = 0;           // 当前主题ID
    private String currentNode = "hot";       // 当前节点，默认为热门

    // 分页按钮
    private JButton prevButton;
    private JButton nextButton;

    // 节点按钮
    private JButton hotButton;      // 热门按钮
    private JButton techButton;     // 技术按钮
    private JButton creativeButton; // 创意按钮
    private JButton playButton;     // 好玩按钮
    private JButton hotTopicsButton;// 最热按钮
    private JButton allButton;      // 全部按钮

    /**
     * 主题信息类
     */
    private static class TopicInfo {
        final int id;           // 主题ID
        final String title;     // 主题标题
        final int replies;      // 回复数

        TopicInfo(int id, String title, int replies) {
            this.id = id;
            this.title = title;
            this.replies = replies;
        }
    }

    /**
     * 构造函数
     */
    public V2EXNewsPanel(Project project) {
        this.project = project;
        
        // 初始化 HTTP 客户端
        this.clientBuilder = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(30))
            .readTimeout(Duration.ofSeconds(30))
            .writeTimeout(Duration.ofSeconds(30));

        // 初始化主面板
        mainPanel = new JPanel(new BorderLayout());
        
        // 创建工具栏
        JPanel toolbar = createToolbar();
        mainPanel.add(toolbar, BorderLayout.NORTH);
        
        // 创建内容面板
        contentPanel = new JPanel(new BorderLayout());
        contentArea = new JTextPane();
        contentArea.setEditable(false);
        contentArea.setMargin(JBUI.insets(5));
        contentArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        
        // 设置滚动面板
        JBScrollPane scrollPane = new JBScrollPane(contentArea);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // 初始加载内容
        refreshContent(null);
    }

    /**
     * 创建工具栏
     */
    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // 节点按钮组
        JPanel nodeButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        nodeButtonsPanel.setBorder(BorderFactory.createTitledBorder("节点"));
        
        // 节点按钮
        hotButton = new JButton(V2ViewerBundle.message("node.hot"));
        techButton = new JButton(V2ViewerBundle.message("node.tech"));
        creativeButton = new JButton(V2ViewerBundle.message("node.creative"));
        playButton = new JButton(V2ViewerBundle.message("node.play"));
        hotTopicsButton = new JButton(V2ViewerBundle.message("node.hot_topics"));
        allButton = new JButton(V2ViewerBundle.message("node.all"));
        
        hotButton.addActionListener(e -> {
            currentNode = "hot";
            refreshContent(null);
            updateNodeButtons();
        });
        
        techButton.addActionListener(e -> {
            currentNode = "tech";
            refreshContent(null);
            updateNodeButtons();
        });

        creativeButton.addActionListener(e -> {
            currentNode = "creative";
            refreshContent(null);
            updateNodeButtons();
        });

        playButton.addActionListener(e -> {
            currentNode = "play";
            refreshContent(null);
            updateNodeButtons();
        });

        hotTopicsButton.addActionListener(e -> {
            currentNode = "hot_topics";
            refreshContent(null);
            updateNodeButtons();
        });

        allButton.addActionListener(e -> {
            currentNode = "all";
            refreshContent(null);
            updateNodeButtons();
        });
        
        // 添加节点按钮到节点面板
        nodeButtonsPanel.add(hotButton);
        nodeButtonsPanel.add(techButton);
        nodeButtonsPanel.add(creativeButton);
        nodeButtonsPanel.add(playButton);
        nodeButtonsPanel.add(hotTopicsButton);
        nodeButtonsPanel.add(allButton);
        
        // 将节点按钮组添加到工具栏
        toolbar.add(nodeButtonsPanel);
        
        // 添加粗分隔符
        JSeparator thickSeparator = new JSeparator(SwingConstants.VERTICAL);
        thickSeparator.setPreferredSize(new Dimension(2, 30));
        toolbar.add(thickSeparator);
        
        // 操作按钮组
        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        
        // 刷新按钮
        JButton refreshButton = new JButton(V2ViewerBundle.message("action.refresh"));
        refreshButton.addActionListener(this::refreshContent);
        actionButtonsPanel.add(refreshButton);
        
        // 返回按钮
        JButton backButton = new JButton(V2ViewerBundle.message("action.back"));
        backButton.addActionListener(e -> showTopicList());
        actionButtonsPanel.add(backButton);
        
        // 分页按钮
        prevButton = new JButton(V2ViewerBundle.message("action.prev.page"));
        nextButton = new JButton(V2ViewerBundle.message("action.next.page"));
        prevButton.setEnabled(false);  // 初始状态禁用
        nextButton.setEnabled(false);  // 初始状态禁用
        prevButton.addActionListener(e -> showPreviousPage());
        nextButton.addActionListener(e -> showNextPage());
        actionButtonsPanel.add(prevButton);
        actionButtonsPanel.add(nextButton);
        
        // 添加操作按钮组到工具栏
        toolbar.add(actionButtonsPanel);
        
        updateNodeButtons();
        return toolbar;
    }

    /**
     * 更新节点按钮状态
     */
    private void updateNodeButtons() {
        hotButton.setEnabled(!currentNode.equals("hot"));
        techButton.setEnabled(!currentNode.equals("tech"));
        creativeButton.setEnabled(!currentNode.equals("creative"));
        playButton.setEnabled(!currentNode.equals("play"));
        hotTopicsButton.setEnabled(!currentNode.equals("hot_topics"));
        allButton.setEnabled(!currentNode.equals("all"));
    }

    /**
     * 获取代理设置
     */
    public static Proxy getProxy(V2EXSettings settings) {
        if (!settings.useProxy || settings.proxyHost.isEmpty()) {
            return Proxy.NO_PROXY;
        }

        Proxy.Type proxyType = "SOCKS".equals(settings.proxyType) 
            ? Proxy.Type.SOCKS 
            : Proxy.Type.HTTP;

        return new Proxy(
            proxyType,
            new InetSocketAddress(settings.proxyHost, settings.proxyPort)
        );
    }

    /**
     * 获取面板内容
     */
    public JComponent getContent() {
        return mainPanel;
    }

    /**
     * 刷新内容
     */
    private void refreshContent(ActionEvent e) {
        currentTopics.clear();
        isShowingList = true;
        
        // 显示加载状态
        showLoadingState();
        
        // 异步加载主题列表
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                V2EXSettings settings = V2EXSettings.getInstance();
                String token = settings.apiToken;
                if (token.isEmpty()) {
                    return V2ViewerBundle.message("error.no.token");
                }

                OkHttpClient client = clientBuilder.proxy(getProxy(settings)).build();
                Request request = new Request.Builder()
                    .url(getNodeApiUrl())
                    .header("Authorization", "Bearer " + token)
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        return V2ViewerBundle.message("error.request", 
                            response.code() + " " + response.message());
                    }

                    JSONArray topics = new JSONArray(response.body().string());
                    return formatTopicList(topics);
                } catch (IOException ex) {
                    return V2ViewerBundle.message("error.loading", ex.getMessage());
                }
            }

            @Override
            protected void done() {
                try {
                    updateContent(get());
                } catch (Exception ex) {
                    updateContent(V2ViewerBundle.message("error.loading", ex.getMessage()));
                }
            }
        };
        
        worker.execute();
    }

    /**
     * 显示加载状态
     */
    private void showLoadingState() {
        contentPanel.removeAll();
        JPanel loadingPanel = new JPanel(new GridBagLayout());
        JLabel loadingLabel = new JLabel(V2ViewerBundle.message("loading"), SwingConstants.CENTER);
        loadingPanel.add(loadingLabel);
        contentPanel.add(loadingPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * 格式化主题列表
     */
    private String formatTopicList(JSONArray topics) {
        StringBuilder content = new StringBuilder();
        currentTopics.clear();
        
        for (int i = 0; i < topics.length(); i++) {
            JSONObject topic = topics.getJSONObject(i);
            int id = topic.getInt("id");
            String title = topic.getString("title");
            int replies = topic.getInt("replies");
            
            currentTopics.add(new TopicInfo(id, title, replies));
            content.append(String.format("%d. %s [%d回复]\n", i + 1, title, replies));
        }
        
        return content.toString();
    }

    /**
     * 显示主题列表
     */
    private void showTopicList() {
        if (!isShowingList) {
            isShowingList = true;
            StringBuilder content = new StringBuilder();
            for (int i = 0; i < currentTopics.size(); i++) {
                TopicInfo topic = currentTopics.get(i);
                content.append(String.format("%d. %s [%d回复]\n", 
                    i + 1, topic.title, topic.replies));
            }
            updateContent(content.toString());
            updatePaginationButtons();
        }
    }

    /**
     * 更新内容显示
     */
    private void updateContent(String text) {
        if (isShowingList) {
            setupTopicLinks();
        } else {
            contentPanel.removeAll();
            contentArea.setText(text);
            JBScrollPane scrollPane = new JBScrollPane(contentArea);
            scrollPane.setBorder(JBUI.Borders.empty(5));
            contentPanel.add(scrollPane, BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
            updatePaginationButtons();
        }
    }

    /**
     * 设���主题链接
     */
    private void setupTopicLinks() {
        contentPanel.removeAll();
        JPanel linksPanel = new JPanel();
        linksPanel.setLayout(new BoxLayout(linksPanel, BoxLayout.Y_AXIS));

        for (int i = 0; i < currentTopics.size(); i++) {
            final TopicInfo topic = currentTopics.get(i);
            ActionListener listener = e -> showTopicContent(topic.id);
            
            ActionLink link = new ActionLink(
                String.format("%d. %s [%d回复]", i + 1, topic.title, topic.replies),
                listener
            );
            link.setAlignmentX(Component.LEFT_ALIGNMENT);
            linksPanel.add(link);
            linksPanel.add(Box.createVerticalStrut(5));
        }

        JBScrollPane scrollPane = new JBScrollPane(linksPanel);
        scrollPane.setBorder(JBUI.Borders.empty(5));
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * 显示上一页
     */
    private void showPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            showTopicContent(currentTopicId);
        }
    }

    /**
     * 显示下一页
     */
    private void showNextPage() {
        if ((currentPage * REPLIES_PER_PAGE) < totalReplies) {
            currentPage++;
            showTopicContent(currentTopicId);
        }
    }

    /**
     * 显示主题内容
     */
    private void showTopicContent(int topicId) {
        if (currentTopicId != topicId) {
            currentPage = 1;
        }
        currentTopicId = topicId;
        isShowingList = false;
        
        // 显示加载状态
        showLoadingState();
        
        // 异步加载主题内容
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                V2EXSettings settings = V2EXSettings.getInstance();
                String token = settings.apiToken;
                if (token.isEmpty()) {
                    return V2ViewerBundle.message("error.no.token");
                }

                OkHttpClient client = clientBuilder.proxy(getProxy(settings)).build();
                
                // 获取主题内容
                Request topicRequest = new Request.Builder()
                    .url("https://www.v2ex.com/api/topics/show.json?id=" + topicId)
                    .header("Authorization", "Bearer " + token)
                    .build();

                String topicContent;
                try (Response response = client.newCall(topicRequest).execute()) {
                    if (!response.isSuccessful()) {
                        return V2ViewerBundle.message("error.request", 
                            response.code() + " " + response.message());
                    }

                    JSONArray topics = new JSONArray(response.body().string());
                    if (topics.length() == 0) {
                        return V2ViewerBundle.message("error.topic.not.found");
                    }
                    
                    JSONObject topic = topics.getJSONObject(0);
                    totalReplies = topic.getInt("replies");
                    
                    StringBuilder content = new StringBuilder();
                    content.append(topic.getString("title")).append("\n\n");
                    content.append(topic.getString("content")).append("\n\n");
                    content.append("节点: ").append(topic.getJSONObject("node").getString("title")).append("\n");
                    content.append("作者: ").append(topic.getJSONObject("member").getString("username")).append("\n");
                    content.append("回复: ").append(totalReplies).append("\n\n");
                    content.append("-------------------\n\n");
                    topicContent = content.toString();
                }

                // 获取回复内容
                Request repliesRequest = new Request.Builder()
                    .url(String.format("https://www.v2ex.com/api/replies/show.json?topic_id=%d&p=%d", 
                        topicId, currentPage))
                    .header("Authorization", "Bearer " + token)
                    .build();

                try (Response response = client.newCall(repliesRequest).execute()) {
                    if (!response.isSuccessful()) {
                        return topicContent + V2ViewerBundle.message("error.replies.failed", response.code());
                    }

                    JSONArray replies = new JSONArray(response.body().string());
                    StringBuilder repliesContent = new StringBuilder(topicContent);
                    
                    // 添加分页信息
                    int totalPages = (totalReplies + REPLIES_PER_PAGE - 1) / REPLIES_PER_PAGE;
                    repliesContent.append(V2ViewerBundle.message("page.info", currentPage, totalPages)).append("\n\n");
                    
                    // 添加回复内容
                    for (int i = 0; i < replies.length(); i++) {
                        JSONObject reply = replies.getJSONObject(i);
                        repliesContent.append(String.format("#%d %s:\n", 
                            (currentPage - 1) * REPLIES_PER_PAGE + i + 1,
                            reply.getJSONObject("member").getString("username")
                        ));
                        repliesContent.append(reply.getString("content")).append("\n\n");
                        if (i < replies.length() - 1) {
                            repliesContent.append("-------------------\n\n");
                        }
                    }
                    
                    return repliesContent.toString();
                }
            }

            @Override
            protected void done() {
                try {
                    updateContent(get());
                } catch (Exception ex) {
                    updateContent(V2ViewerBundle.message("error.loading", ex.getMessage()));
                }
            }
        };
        
        worker.execute();
    }

    /**
     * 更新分页按钮状态
     */
    private void updatePaginationButtons() {
        prevButton.setEnabled(!isShowingList && currentPage > 1);
        nextButton.setEnabled(!isShowingList && (currentPage * REPLIES_PER_PAGE) < totalReplies);
    }

    /**
     * 获取节点对应的API URL
     */
    private String getNodeApiUrl() {
        switch (currentNode) {
            case "hot":
                return "https://www.v2ex.com/api/topics/hot.json";
            case "tech":
                return "https://www.v2ex.com/api/topics/show.json?node_name=tech";
            case "creative":
                return "https://www.v2ex.com/api/topics/show.json?node_name=creative";
            case "play":
                return "https://www.v2ex.com/api/topics/show.json?node_name=play";
            case "hot_topics":
                return "https://www.v2ex.com/api/topics/hot.json";
            case "all":
                return "https://www.v2ex.com/api/topics/latest.json";
            default:
                return "https://www.v2ex.com/api/topics/hot.json";
        }
    }
} 