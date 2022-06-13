package cn.devcms.redisfront.common.base;

import cn.devcms.redisfront.common.func.Fn;
import cn.devcms.redisfront.model.ConnectInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public abstract class AbstractTerminalComponent extends JPanel implements KeyListener, CaretListener {
    private final JTextArea terminal;
    private int lastSelectionStart = 0;
    private int currentDot = -1;
    private int currentKeyCode = 0;
    private boolean allowInputFlag = false;
    private boolean consumeFlag = false;

    public AbstractTerminalComponent() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        terminal = new JTextArea();
        terminal.requestFocus();
        terminal.setCaretColor(Color.WHITE);
        terminal.setForeground(Color.WHITE);
        terminal.setBackground(Color.BLACK);
        terminal.addKeyListener(this);
        terminal.addCaretListener(this);
        add(new JScrollPane(terminal), BorderLayout.CENTER);
    }

    protected abstract void inputProcessHandler(String input);

    protected abstract ConnectInfo connectInfo();

    protected abstract String databaseName();

    protected void println(String message) {
        terminal.append(message);
        terminal.append("\n");
        lastSelectionStart = terminal.getSelectionEnd();
    }

    protected void print(String message) {
        terminal.append(message);
        lastSelectionStart = terminal.getSelectionEnd();
    }

    protected void printConnectedSuccessMessage() {
        this.println("");
        this.println("connection ".concat(connectInfo().host()).concat(":") + connectInfo().port() + " redis server success...");
        this.println("");
        this.print(connectInfo().host().concat(":").concat(String.valueOf(connectInfo().port())).concat(Fn.equal("0", databaseName()) ? "" : "[" + databaseName() + "]").concat(">"));
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (consumeFlag) {
            e.consume();
            return;
        }
        if (currentKeyCode == KeyEvent.VK_ENTER && e.getKeyChar() == '\n') {
            var subStartLength = lastSelectionStart;
            var subEndLength = terminal.getSelectionEnd() - 1;
            if (subStartLength < subEndLength) {
                String input = terminal.getText().substring(subStartLength, subEndLength);
                this.inputProcessHandler(input);
            }
            this.print("\n");
            this.print(connectInfo().host().concat(":").concat(connectInfo().port().toString()).concat(Fn.equal("0", databaseName()) ? "" : "[" + databaseName() + "]").concat(">"));
        } else if (currentKeyCode == KeyEvent.VK_ENTER && e.getKeyChar() != '\n') {
            e.consume();
            terminal.setText(terminal.getText().concat(String.valueOf(e.getKeyChar())));
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        currentKeyCode = e.getKeyCode();
        if ((currentKeyCode == KeyEvent.VK_BACK_SPACE || currentKeyCode == KeyEvent.VK_ENTER || currentKeyCode == KeyEvent.VK_UP || currentKeyCode == KeyEvent.VK_DOWN || currentKeyCode == KeyEvent.VK_LEFT) && currentDot <= lastSelectionStart) {
            e.consume();
            consumeFlag = true;
        }
        if (allowInputFlag) {
            consumeFlag = false;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (consumeFlag) {
            e.consume();
        }
    }

    public void caretUpdate(CaretEvent e) {
        currentDot = e.getDot();
        allowInputFlag = (currentDot >= lastSelectionStart);
    }

}

