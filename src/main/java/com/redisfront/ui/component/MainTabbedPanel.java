package com.redisfront.ui.component;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.components.FlatLabel;
import com.formdev.flatlaf.extras.components.FlatToolBar;
import com.redisfront.commons.constant.Enum;
import com.redisfront.commons.constant.UI;
import com.redisfront.commons.func.Fn;
import com.redisfront.commons.util.FutureUtils;
import com.redisfront.commons.util.LocaleUtils;
import com.redisfront.model.ClusterNode;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.RedisBasicService;
import com.redisfront.ui.form.fragment.DataChartsForm;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainTabbedPanel extends JPanel {

    private final JTabbedPane contentPanel;
    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    public static MainTabbedPanel newInstance(ConnectInfo connectInfo) {
        return new MainTabbedPanel(connectInfo);
    }

    public void shutdownScheduled() {
        scheduledExecutor.shutdown();
        var component = contentPanel.getComponentAt(2);
        if (component instanceof ChartsPanel chartsPanel) {
            chartsPanel.shutDownScheduledExecutorService();
        }

    }

    public MainTabbedPanel(ConnectInfo connectInfo) {
        setLayout(new BorderLayout());
        contentPanel = new JTabbedPane() {
            @Override
            public void updateUI() {
                super.updateUI();
                var count = getTabCount();
                if (count > 2) {
                    contentPanel.setTitleAt(0, LocaleUtils.getMessageFromBundle("MainTabbedPanel.contentPanel.RedisTerminal.title"));
                    contentPanel.setTitleAt(1, LocaleUtils.getMessageFromBundle("MainTabbedPanel.contentPanel.DataSplitPanel.title"));
                    contentPanel.setTitleAt(2, LocaleUtils.getMessageFromBundle("MainTabbedPanel.contentPanel.DataChartsForm.title"));
                }
            }
        };
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.CENTER);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS, false);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_TRAILING);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_CENTER);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);

        FlatToolBar leftToolBar = new FlatToolBar();
        var leftToolBarLayout = new FlowLayout();
        leftToolBarLayout.setAlignment(FlowLayout.CENTER);
        leftToolBar.setLayout(leftToolBarLayout);
        leftToolBar.setPreferredSize(new Dimension(50, -1));

        //host info
        var hostInfo = new JLabel(UI.CONTENT_TAB_HOST_ICON) {
            @Override
            public void updateUI() {
                super.updateUI();
                var buf = new StringBuilder(1500);
                buf.append("<html><style>");
                buf.append("td { padding: 0 10 0 0; }");
                buf.append("</style><table>");
                var serverInfo = RedisBasicService.service.getServerInfo(connectInfo);
                var version = (String) serverInfo.get("redis_version");
                appendRow(buf, LocaleUtils.getMessageFromBundle("MainTabbedPanel.redisVersion.title"), version);

                var port = (String) serverInfo.get("tcp_port");
                appendRow(buf, LocaleUtils.getMessageFromBundle("MainTabbedPanel.tcpPort.title"), port);

                var os = (String) serverInfo.get("os");
                appendRow(buf, LocaleUtils.getMessageFromBundle("MainTabbedPanel.os.title"), os);

                var redisMode = (String) serverInfo.get("redis_mode");
                appendRow(buf, LocaleUtils.getMessageFromBundle("MainTabbedPanel.redisMode.title"), redisMode);

                var configFile = (String) serverInfo.get("config_file");
                appendRow(buf, LocaleUtils.getMessageFromBundle("MainTabbedPanel.configFile.title"), configFile);

                if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
                    List<ClusterNode> clusterNodes = RedisBasicService.service.getClusterNodes(connectInfo);
                    clusterNodes.forEach(s -> appendRow(buf, s.flags().toUpperCase(), s.ipAndPort()));
                }

                buf.append("</td></tr>");
                buf.append("</table></html>");
                setToolTipText(buf.toString());
                setText(connectInfo.host() + ":" + connectInfo.port() + " - " + LocaleUtils.getMessageFromBundle(connectInfo.redisModeEnum().modeName));
            }
        };
        leftToolBar.add(hostInfo);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_LEADING_COMPONENT, leftToolBar);

        //host info
        var rightToolBar = new FlatToolBar();
        var rightToolBarLayout = new FlowLayout();
        rightToolBarLayout.setAlignment(FlowLayout.CENTER);
        rightToolBar.setLayout(rightToolBarLayout);
        rightToolBar.setPreferredSize(new Dimension(50, -1));

        //keysInfo
        var keysInfo = new FlatLabel();
        keysInfo.setText("0");
        keysInfo.setIcon(UI.CONTENT_TAB_KEYS_ICON);
        rightToolBar.add(keysInfo);

        //cupInfo
        var cupInfo = new FlatLabel();
        cupInfo.setText("0");
        cupInfo.setIcon(UI.CONTENT_TAB_CPU_ICON);
        rightToolBar.add(cupInfo);

        //memoryInfo
        var memoryInfo = new FlatLabel();
        memoryInfo.setText("0.0");
        memoryInfo.setIcon(UI.CONTENT_TAB_MEMORY_ICON);
        rightToolBar.add(memoryInfo);

        threadInit(connectInfo, keysInfo, cupInfo, memoryInfo);

        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TRAILING_COMPONENT, rightToolBar);
        contentPanel.addTab(LocaleUtils.getMessageFromBundle("MainTabbedPanel.contentPanel.RedisTerminal.title"), UI.CONTENT_TAB_COMMAND_ICON, RedisTerminal.newInstance(connectInfo));
        contentPanel.addTab(LocaleUtils.getMessageFromBundle("MainTabbedPanel.contentPanel.DataSplitPanel.title"), UI.CONTENT_TAB_DATA_ICON, DataSplitPanel.newInstance(connectInfo));
        contentPanel.addTab(LocaleUtils.getMessageFromBundle("MainTabbedPanel.contentPanel.DataChartsForm.title"), UI.CONTENT_TAB_INFO_ICON, DataChartsForm.getInstance(connectInfo));
        contentPanel.setSelectedIndex(1);


        //tab 切换事件
        contentPanel.addChangeListener(e -> {
            var tabbedPane = (JTabbedPane) e.getSource();
            var component = tabbedPane.getSelectedComponent();
            if (component instanceof RedisTerminal terminal) {
                terminal.ping();
                chartsFormInit();
            }
            if (component instanceof DataSplitPanel dataSplitPanel) {
                dataSplitPanel.ping();
                chartsFormInit();
            }
            if (component instanceof DataChartsForm chartsForm) {
                chartsForm.scheduleInit();
            }
        });

        add(contentPanel, BorderLayout.CENTER);

    }

    private void chartsFormInit() {

        if (contentPanel.getTabCount() > 2 && contentPanel.getComponentAt(2) instanceof DataChartsForm dataChartsForm) {
            dataChartsForm.scheduleInit();
        }
    }

    private void threadInit(ConnectInfo connectInfo, FlatLabel keysInfo, FlatLabel cupInfo, FlatLabel memoryInfo) {
        scheduledExecutor.scheduleAtFixedRate(() -> {
                    CompletableFuture<Void> keyInfoFuture = FutureUtils.supplyAsync(() -> {
                        var keyInfo = new String[2];
                        if (Fn.notEqual(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
                            var keySpace = RedisBasicService.service.getKeySpace(connectInfo);
                            var count = keySpace.values().stream()
                                    .map(value -> ((String) value).split(",")[0].split("=")[1])
                                    .map(Integer::parseInt).reduce(Integer::sum).orElse(0);
                            keyInfo[0] = String.valueOf(count);
                            var buf = new StringBuilder(200);
                            buf.append("<html><style>");
                            buf.append("td { padding: 0 10 0 0; }");
                            buf.append("</style>");
                            buf.append("<p>");
                            buf.append(LocaleUtils.getMessageFromBundle("MainTabbedPanel.keyInfoFuture.keySize.title")).append(count);
                            if (count > 0) {
                                buf.append("</p>");
                                buf.append("<hr>");
                                buf.append("</hr>");
                                buf.append("<table>");
                                keySpace.forEach((key, value) -> appendRow(buf, key, String.valueOf(value)));
                                buf.append("</td></tr>");
                                buf.append("</table>");
                                buf.append("<hr>");
                                buf.append("</hr>");
                            }
                            buf.append("</html>");
                            keyInfo[1] = buf.toString();
                        } else {
                            keyInfo[0] = String.valueOf(RedisBasicService.service.dbSize(connectInfo));
                            keyInfo[1] = LocaleUtils.getMessageFromBundle("MainTabbedPanel.keyInfoFuture.keySize.title") + keyInfo[0];
                        }
                        return keyInfo;
                    }, s ->
                            SwingUtilities.invokeLater(() -> {
                                keysInfo.setText(s[0]);
                                keysInfo.setToolTipText(s[1]);
                            }));

                    CompletableFuture<Void> opsInfoFuture = FutureUtils.supplyAsync(() -> RedisBasicService.service.getStatInfo(connectInfo), stats ->
                            SwingUtilities.invokeLater(() -> {
                                cupInfo.setText((String) stats.get("instantaneous_ops_per_sec"));
                                cupInfo.setToolTipText(LocaleUtils.getMessageFromBundle("MainTabbedPanel.opsInfoFuture.opsPerSec.title") + stats.get("instantaneous_ops_per_sec"));
                            }));

                    CompletableFuture<Void> memoryFuture = FutureUtils.supplyAsync(() -> RedisBasicService.service.getMemoryInfo(connectInfo), memory ->
                            SwingUtilities.invokeLater(() -> {
                                memoryInfo.setText((Fn.isNotNull(memory.get("used_memory_human")) ? (String) memory.get("used_memory_human") : "0"));
                                memoryInfo.setToolTipText(LocaleUtils.getMessageFromBundle("MainTabbedPanel.memoryFuture.usedMemoryHuman.title") + (Fn.isNotNull(memory.get("used_memory_human")) ? memory.get("used_memory_human") : 0));
                            }));

                    CompletableFuture.anyOf(keyInfoFuture, opsInfoFuture, memoryFuture);

                }
                , 0, 5, TimeUnit.SECONDS);
    }

    private void appendRow(StringBuilder buf, String key, String value) {
        buf.append("<tr><td valign=\"top\">")
                .append(key)
                .append(":</td><td>")
                .append(value)
                .append("</td></tr>");
    }


}
