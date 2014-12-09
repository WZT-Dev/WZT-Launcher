package ro.wzt.launcher.tasks;

import ro.wzt.launcher.LauncherConstants;
import ro.wzt.launcher.frames.ChangelogFrame;
import ro.wzt.launcher.utils.ConnectionUtils;
import ro.wzt.launcher.utils.LogUtils;

import java.util.logging.Level;

public class ChangelogDownloader extends Thread {

    @Override
    public final void run() {
        final ChangelogFrame frame = ChangelogFrame.getInstance();
        if (!frame.isChangeLogDownloaded()) {
            try {
                LogUtils.log(Level.INFO, LauncherConstants.CHANGELOG_DOWNLOADER_PREFIX + "Downloading changelog...");
                final String changelog = ConnectionUtils.httpGet(LauncherConstants.CHANGELOG_URL, System.lineSeparator());
                LogUtils.log(Level.INFO, LauncherConstants.CHANGELOG_DOWNLOADER_PREFIX + "Done.");
                frame.setChangelog(changelog);
                frame.setChangeLogDownloaded(true);
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        }
        frame.setVisible(true);
    }

}
