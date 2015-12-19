package ro.wzt.launcher;

import com.pagosoft.plaf.PgsLookAndFeel;
import ro.wzt.launcher.UsersManager.User;
import ro.wzt.launcher.frames.ConsoleFrame;
import ro.wzt.launcher.frames.LauncherFrame;
import ro.wzt.launcher.tasks.ConnectivityChecker;
import ro.wzt.launcher.tasks.RefreshToken;
import ro.wzt.launcher.tasks.VanillaImporter;
import ro.wzt.launcher.utils.JSONObject.ObjectType;
import ro.wzt.launcher.utils.LogUtils;
import ro.wzt.launcher.utils.SystemManager;
import ro.wzt.launcher.utils.SystemManager.OS;
import ro.wzt.launcher.utils.Utils;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

//import ro.wzt.launcher.tasks.AutoUpdater;

public class WZTLauncher {

    public static final SystemManager SYSTEM = new SystemManager();

    public static LauncherConfig config;
    public static ConsoleFrame console;
    public static Boolean isOnline;

    public static void main(final String[] args) {
        try {
            final ConnectivityChecker checker = new ConnectivityChecker();
            checker.start();
            checker.waitForThread();
            LogUtils.log(null, null);
            config = new LauncherConfig("launcher");
            final File mcDir = SYSTEM.getMinecraftDirectory();
            mcDir.mkdirs();
            final List<String> argsList = Arrays.asList(args);
            PgsLookAndFeel.setCurrentTheme(new LauncherTheme());
            UIManager.setLookAndFeel(new PgsLookAndFeel());
            Utils.setUIFont(new FontUIResource(LauncherConstants.LAUNCHER_FONT));
            if (SYSTEM.getPlatform().getOS() == OS.LINUX) {
                JFrame.setDefaultLookAndFeelDecorated(true);
                JDialog.setDefaultLookAndFeelDecorated(true);
            }
            final File appDir = SYSTEM.getApplicationDirectory();
            if (!appDir.exists()) {
                appDir.mkdir();
            }
            console = new ConsoleFrame();
            console.setVisible(true);
            System.setErr(new PrintStream(new LogUtils.ErrorOutputStream()));
            LogUtils.log(Level.INFO, LauncherConstants.LAUNCHER_PREFIX + Utils.buildTitle(isOnline));
            LogUtils.log(null, null);
            LogUtils.log(Level.INFO, LauncherConstants.LAUNCHER_PREFIX + "Loading profiles...");
            if (ObjectType.PROFILE.directory.exists()) {
                for (final File profileFile : ObjectType.PROFILE.directory.listFiles()) {
                    final String fileName = profileFile.getName();
                    final ProfilesManager.LauncherProfile profile = new ProfilesManager.LauncherProfile(fileName.substring(0, fileName.lastIndexOf(".")));
                    ProfilesManager.addProfile(profile);
                }
            } else {
                ObjectType.PROFILE.directory.mkdirs();
            }
            LogUtils.log(Level.INFO, LauncherConstants.LAUNCHER_PREFIX + "Done.");
            LogUtils.log(Level.INFO, LauncherConstants.LAUNCHER_PREFIX + "Loading users...");
            final List<User> onlineUsers = new ArrayList<>();
            if (ObjectType.USER.directory.exists()) {
                for (final File userFile : ObjectType.USER.directory.listFiles()) {
                    final String fileName = userFile.getName();
                    final User user = new User(fileName.substring(0, fileName.lastIndexOf(".")));
                    UsersManager.addUser(user);
                    if (user.isOnline) {
                        onlineUsers.add(user);
                    }
                }
            } else {
                ObjectType.USER.directory.mkdir();
            }
            LogUtils.log(Level.INFO, LauncherConstants.LAUNCHER_PREFIX + "Done.");
            final File vanillaData = new File(mcDir, "launcher_profiles.json");
            if (vanillaData.exists() && vanillaData.isFile() && !config.vanillaDataImported) {
                new VanillaImporter(vanillaData).start();
            }
            new LauncherFrame().setVisible(true);
            if (onlineUsers.size() != 0) {
                new RefreshToken(onlineUsers.toArray(new User[onlineUsers.size()])).start();
            }
            //new AutoUpdater().start();
        } catch (final Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, ex.getClass().getName(), "Error !", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

}
