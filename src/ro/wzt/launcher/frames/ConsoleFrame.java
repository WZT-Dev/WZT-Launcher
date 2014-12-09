package ro.wzt.launcher.frames;

import ro.wzt.launcher.LauncherConstants;
import ro.wzt.launcher.WZTLauncher;
import ro.wzt.launcher.utils.LogUtils;

import javax.swing.*;
import java.awt.*;

public class ConsoleFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    public ConsoleFrame() {
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setIconImage(LauncherConstants.LAUNCHER_ICON);
        this.setTitle("WZT-Launcher | Consola");
        this.setType(Type.POPUP);
        this.setPreferredSize(new Dimension(510, 330));
        this.setLocation(WZTLauncher.config.consolePointX, WZTLauncher.config.consolePointY);
        final JTextArea txtrLogs = new JTextArea();
        txtrLogs.setEditable(false);
        txtrLogs.setFont(new Font("Lucida Console", Font.PLAIN, 14));
        txtrLogs.setBackground(Color.BLACK);
        txtrLogs.setForeground(Color.WHITE);
        txtrLogs.setWrapStyleWord(true);
        this.getContentPane().add(new JScrollPane(txtrLogs), BorderLayout.CENTER);
        LogUtils.setTextArea(txtrLogs);
        this.pack();
    }

}
