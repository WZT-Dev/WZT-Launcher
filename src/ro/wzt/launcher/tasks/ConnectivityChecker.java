package ro.wzt.launcher.tasks;

import ro.wzt.launcher.LauncherConstants;
import ro.wzt.launcher.WZTLauncher;
import ro.wzt.launcher.utils.ConnectionUtils;
import ro.wzt.launcher.utils.LogUtils;

import java.util.logging.Level;

public class ConnectivityChecker extends Thread {

    @Override
    public final void run() {
        LogUtils.log(Level.INFO, LauncherConstants.CONNECTIVITY_CHECKER_PREFIX + "Waiting for the connectivity checker...");
        try {
            WZTLauncher.isOnline = ConnectionUtils.isOnline(LauncherConstants.CONNECTIVITY_CHECKER_URLS);
        } catch (final Exception ex) {
            ex.printStackTrace();
            WZTLauncher.isOnline = false;
        }
        LogUtils.log(Level.INFO, LauncherConstants.CONNECTIVITY_CHECKER_PREFIX + "Done.");
    }

    public final void waitForThread() throws InterruptedException {
        this.join();
    }

}
