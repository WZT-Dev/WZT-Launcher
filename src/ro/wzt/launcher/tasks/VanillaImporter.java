package ro.wzt.launcher.tasks;

import com.google.gson.Gson;
import ro.wzt.launcher.LauncherConstants;
import ro.wzt.launcher.UsersManager;
import ro.wzt.launcher.WZTLauncher;
import ro.wzt.launcher.frames.UserFrame;
import ro.wzt.launcher.utils.Utils;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VanillaImporter extends Thread {

    private final File vanillaData;

    public VanillaImporter(final File vanillaData) {
        this.vanillaData = vanillaData;
    }

    @Override
    public void run() {
        try {
            final List<VanillaUser> users = new ArrayList<VanillaUser>();
            final VanillaData data = new Gson().fromJson(Utils.getFileContent(vanillaData, null), VanillaData.class);
            for (final VanillaUser vanillaUser : data.authenticationDatabase.values()) {
                if (UsersManager.getUserByAccountName(vanillaUser.username) == null) {
                    users.add(vanillaUser);
                }
            }
            if (users.size() > 0) {
                final JLabel desc = new JLabel("Vanilla launcher's user(s) detected. Would you like to import them ?");
                final JCheckBox notAskAgain = new JCheckBox("Do not ask again.");
                final int response = JOptionPane.showConfirmDialog(null, new Object[]{desc, notAskAgain}, LauncherConstants.LAUNCHER_NAME, JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (notAskAgain.isSelected() || response == JOptionPane.OK_OPTION) {
                    WZTLauncher.config.vanillaDataImported = true;
                    WZTLauncher.config.save();
                    if (response == JOptionPane.OK_OPTION) {
                        for (final VanillaUser vanillaUser : users) {
                            new UserFrame(null, vanillaUser.username).setVisible(true);
                        }
                    }
                }
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    public class VanillaData {

        //public HashMap<String, VanillaProfile> profiles;
        public HashMap<String, VanillaUser> authenticationDatabase;

    }

	/*
	public class VanillaProfile {
		
		public String name;
		public String playerUUID;
		public File gameDir;
		
	}
	*/

    public class VanillaUser {

        //public String displayName;
        //public String uuid;
        public String username;
        //public List<Property> properties;

    }

}