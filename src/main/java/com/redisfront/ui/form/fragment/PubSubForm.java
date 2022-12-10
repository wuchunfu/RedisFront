package com.redisfront.ui.form.fragment;

import com.formdev.flatlaf.FlatClientProperties;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.redisfront.commons.constant.Enum;
import com.redisfront.commons.constant.UI;
import com.redisfront.commons.func.Fn;
import com.redisfront.commons.util.AlertUtils;
import com.redisfront.commons.util.JschUtils;
import com.redisfront.commons.util.LettuceUtils;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.RedisPubSubService;
import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.RedisClient;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.models.partitions.RedisClusterNode;
import io.lettuce.core.cluster.pubsub.RedisClusterPubSubListener;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * PubSubForm
 *
 * @author Jin
 */
public class PubSubForm extends JPanel implements RedisPubSubListener<String, String>, RedisClusterPubSubListener<String, String> {

    private RedisPubSubAsyncCommands<String, String> pubsub;
    private AbstractRedisClient redisClient;
    private JPanel rootPanel;
    private JToggleButton enableSubscribe;
    private JTextField channelField;
    private JTextField messageField;
    private JButton publishBtn;
    private JLabel messageNum;
    private JPanel listPanel;
    private JTextField subscribeChannel;
    private JTextArea messageList;
    private String lastSubscribeChanel;
    private final ConnectInfo connectInfo;

    public static PubSubForm newInstance(ConnectInfo connectInfo) {
        return new PubSubForm(connectInfo);
    }

    public PubSubForm(ConnectInfo connectInfo) {
        $$$setupUI$$$();
        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);
        this.connectInfo = connectInfo;
        channelField.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                JTextField jTextField = (JTextField) input;
                return Fn.isNotEmpty(jTextField.getText());
            }
        });
        subscribeChannel.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "请输入需要监听的通道名称！");

        enableSubscribe.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                JTextField jTextField = (JTextField) input;
                var ret = Fn.isNotEmpty(jTextField.getText());
                enableSubscribe.setEnabled(ret);
                return ret;
            }
        });
        enableSubscribe.addItemListener(e -> {
            var channel = subscribeChannel.getText();
            if (!enableSubscribe.isSelected()) {
                subscribeChannel.setFocusable(true);
                pubsub.unsubscribe(channel);
                enableSubscribe.setText("开启监听");
            } else {
                subscribeChannel.setFocusable(false);
                if (Fn.isEmpty(lastSubscribeChanel)) {
                    lastSubscribeChanel = channel;
                } else if (Fn.endsWith(lastSubscribeChanel, channel)) {
                    SwingUtilities.invokeLater(() -> messageNum.setText("0"));
                }
                pubsub.subscribe(channel);
                enableSubscribe.setText("取消监听");
            }
        });
        publishBtn.addActionListener(e -> {
            var count = RedisPubSubService.service.publish(connectInfo, channelField.getText(), messageField.getText());
            AlertUtils.showInformationDialog("成功发布 " + count + " 条消息！");
            messageField.setText("");
        });
    }

    public void openConnection() {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            var redisUrl = LettuceUtils.getRedisURI(connectInfo);
            redisClient = LettuceUtils.getRedisClusterClient(redisUrl, connectInfo);
            JschUtils.openSession(connectInfo, (RedisClusterClient) redisClient);
            var connection = ((RedisClusterClient) redisClient).connectPubSub();
            pubsub = connection.async();
        } else {
            JschUtils.openSession(connectInfo);
            redisClient = LettuceUtils.getRedisClient(connectInfo);
            var connection = (((RedisClient) redisClient).connectPubSub());
            pubsub = connection.async();
        }
        pubsub.getStatefulConnection().addListener(this);
    }

    public void disConnection() {
        enableSubscribe.setSelected(false);
        if (Fn.isNotNull(pubsub)) {
            pubsub.getStatefulConnection().closeAsync().thenRun(() -> redisClient.shutdownAsync().thenRun(JschUtils::closeSession));
        }
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        rootPanel.setLayout(new BorderLayout(0, 0));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 6, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel1, BorderLayout.NORTH);
        final JLabel label1 = new JLabel();
        label1.setText("消息数量:  ");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        enableSubscribe.setText("Subscribe");
        panel1.add(enableSubscribe, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        messageNum = new JLabel();
        messageNum.setText("0");
        panel1.add(messageNum, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        subscribeChannel = new JTextField();
        panel1.add(subscribeChannel, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("通道名称:");
        panel1.add(label2, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        listPanel = new JPanel();
        listPanel.setLayout(new BorderLayout(0, 0));
        rootPanel.add(listPanel, BorderLayout.CENTER);
        listPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JScrollPane scrollPane1 = new JScrollPane();
        listPanel.add(scrollPane1, BorderLayout.CENTER);
        messageList = new JTextArea();
        scrollPane1.setViewportView(messageList);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel2, BorderLayout.SOUTH);
        channelField = new JTextField();
        panel2.add(channelField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("通道");
        panel2.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("消息");
        panel2.add(label4, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        messageField = new JTextField();
        panel2.add(messageField, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        publishBtn = new JButton();
        publishBtn.setText("发布");
        panel2.add(publishBtn, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

    private void createUIComponents() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new BorderLayout());
        rootPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        enableSubscribe = new JToggleButton();
        enableSubscribe.setFocusable(false);
        enableSubscribe.setIcon(UI.SUBSCRIBE_ICON);
        enableSubscribe.setSelectedIcon(UI.UNSUBSCRIBE_ICON);
        enableSubscribe.setDisabledIcon(UI.SUBSCRIBE_ICON);
    }


    @Override
    public void message(String channel, String message) {
        String sb = "----------------------------------" + "\n" +
                "时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " " + "\n" + "通道: " + channel + " " + "\n" +
                "消息: " + message + "\n" + "\n";
        SwingUtilities.invokeLater(() -> {
            messageNum.setText(String.valueOf(Integer.parseInt(messageNum.getText()) + 1));
            messageList.append(sb);
        });
    }

    @Override
    public void message(String pattern, String channel, String message) {
        System.out.println("channel:" + channel + "  " + message);
    }

    @Override
    public void subscribed(String channel, long count) {
        System.out.println("channel:" + channel + "  " + count);
    }

    @Override
    public void psubscribed(String pattern, long count) {
        System.out.println("channel:" + pattern + "  " + count);
    }

    @Override
    public void unsubscribed(String channel, long count) {
        System.out.println("channel:" + channel + "  " + count);
    }

    @Override
    public void punsubscribed(String pattern, long count) {
        System.out.println("channel:" + pattern + "  " + count);
    }

    @Override
    public void message(RedisClusterNode node, String channel, String message) {

    }

    @Override
    public void message(RedisClusterNode node, String pattern, String channel, String message) {

    }

    @Override
    public void subscribed(RedisClusterNode node, String channel, long count) {

    }

    @Override
    public void psubscribed(RedisClusterNode node, String pattern, long count) {

    }

    @Override
    public void unsubscribed(RedisClusterNode node, String channel, long count) {

    }

    @Override
    public void punsubscribed(RedisClusterNode node, String pattern, long count) {

    }
}
