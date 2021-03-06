package ro.wzt.launcher.frames;

import ro.wzt.launcher.LauncherConstants;
import ro.wzt.launcher.ProfilesManager;
import ro.wzt.launcher.UsersManager;
import ro.wzt.launcher.UsersManager.User;
import ro.wzt.launcher.WZTLauncher;
import ro.wzt.launcher.frames.ProfileFrame.ProfileChangesListener;
import ro.wzt.launcher.tasks.AuthUser.AuthSession;
import ro.wzt.launcher.tasks.GameTasks;
import ro.wzt.launcher.tasks.GameTasks.GameTasksListener;
import ro.wzt.launcher.tasks.RefreshToken;
import ro.wzt.launcher.tasks.RefreshToken.RefreshTokenListener;
import ro.wzt.launcher.tasks.ServicesStatus;
import ro.wzt.launcher.tasks.ServicesStatus.ServiceStatusListener;
import ro.wzt.launcher.utils.Utils;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Timer;

public class LauncherFrame extends JFrame implements ProfileChangesListener, ServiceStatusListener, GameTasksListener, RefreshTokenListener {

    private static final long serialVersionUID = 1L;
    private final ProfileFrame profileEditor = new ProfileFrame(this);
    private final JComboBox<String> cboxProfile = new JComboBox<String>() {

        private static final long serialVersionUID = 1L;

        {
            for (final String profile : ProfilesManager.getProfilesName()) {
                addItem(profile);
            }
            if (WZTLauncher.config.latestProfile != null) {
                setSelectedItem(WZTLauncher.config.latestProfile.toString());
            }
        }

    };
    private final HashMap<String, JLabel> status = new HashMap<String, JLabel>();
    private final JButton btnDeleteProfile = new JButton("Sterge profilul...");
    private final JButton btnEditProfile = new JButton("Editeaza profilul...");
    private final JButton btnPlay = new JButton("Joaca") {

        private static final long serialVersionUID = 1L;

        {
            setFont(getFont().deriveFont(Font.BOLD));
        }

    };
    private boolean tokensRefreshed = true;

    public LauncherFrame() {
        RefreshToken.addListener(this);
        GameTasks.addListener(this);
        ProfileFrame.addListener(this);
        ServicesStatus.addListener(this);
        this.setTitle(Utils.buildTitle(WZTLauncher.isOnline));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setIconImage(LauncherConstants.LAUNCHER_ICON);
        this.setLocation(WZTLauncher.config.launcherPointX, WZTLauncher.config.launcherPointY);
        this.setPreferredSize(new Dimension(540, 400));
        this.setResizable(false);
        final Container pane = this.getContentPane();
        pane.setBackground(new Color(0, 77, 119));
        final JLabel lblLogo = new JLabel(new ImageIcon(LauncherConstants.LAUNCHER_IMAGE));
        final JProgressBar prgBarDownload = new JProgressBar();
        prgBarDownload.setStringPainted(true);
        prgBarDownload.setVisible(false);
        btnPlay.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                final ProfilesManager.LauncherProfile profile = ProfilesManager.getProfile((String) cboxProfile.getSelectedItem());
                if (profile.user == null) {
                    JOptionPane.showMessageDialog(null, "Profilul nu a putut fi lansat: userul este null.", "Eroare", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                new GameTasks(profile, prgBarDownload).start();
            }

        });
        if (ProfilesManager.getProfiles().length == 0) {
            updateBtnPlay(false);
            btnDeleteProfile.setEnabled(false);
            btnEditProfile.setEnabled(false);
        }
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                try {
                    if (ProfilesManager.getProfiles().length != 0) {
                        WZTLauncher.config.latestProfile = cboxProfile.getSelectedItem().toString();
                    }
                    Point location;
                    if (WZTLauncher.console != null) {
                        location = WZTLauncher.console.getLocation();
                        WZTLauncher.config.consolePointX = location.x;
                        WZTLauncher.config.consolePointY = location.y;
                    }
                    location = LauncherFrame.this.getLocation();
                    WZTLauncher.config.launcherPointX = location.x;
                    WZTLauncher.config.launcherPointY = location.y;
                    WZTLauncher.config.save();
                    final File tempDir = WZTLauncher.SYSTEM.getLauncherTemporaryDirectory();
                    if (tempDir.exists()) {
                        tempDir.delete();
                    }
                } catch (final Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, ex.getClass().getName(), "Eroare", JOptionPane.ERROR_MESSAGE);
                }
            }

        });
        final JLabel lblMinecraftWebsite = new JLabel("Site oficial Minecraft:");
        lblMinecraftWebsite.setForeground(Color.BLACK);
        final JLabel lblMojangAuthServer = new JLabel("Server autentificare Mojang:");
        lblMojangAuthServer.setForeground(Color.BLACK);
        final JLabel lblMinecraftSkinsServer = new JLabel("Server skinuri Minecraft:");
        lblMinecraftSkinsServer.setForeground(Color.BLACK);
        final JLabel lblMinecraftWebsiteStatus = new JLabel();
        final JLabel lblMojangAuthServerStatus = new JLabel();
        final JLabel lblMinecraftSkinsServerStatus = new JLabel();
        status.put("minecraft.net", lblMinecraftWebsiteStatus);
        status.put("authserver.mojang.com", lblMojangAuthServerStatus);
        status.put("skins.minecraft.net", lblMinecraftSkinsServerStatus);
        new Timer().scheduleAtFixedRate(new ServicesStatus(status.keySet()), 0, 40000);
        final JButton btnAddNewProfile = new JButton("Adauga profil...");
        btnAddNewProfile.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                updateBtnPlay(false);
                profileEditor.loadProfile(null);
                profileEditor.setVisible(true);
            }

        });
        btnDeleteProfile.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                deleteProfile((String) cboxProfile.getSelectedItem());
            }

        });
        btnEditProfile.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                updateBtnPlay(false);
                profileEditor.loadProfile(ProfilesManager.getProfile((String) cboxProfile.getSelectedItem()));
                profileEditor.setVisible(true);
            }

        });
        final GroupLayout groupLayout = new GroupLayout(pane);
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addContainerGap().addComponent(cboxProfile, 0, 514, Short.MAX_VALUE).addContainerGap()).addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addGroup(groupLayout.createSequentialGroup().addContainerGap().addComponent(btnPlay, GroupLayout.DEFAULT_SIZE, 515, Short.MAX_VALUE)).addGroup(groupLayout.createSequentialGroup().addGap(10).addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addComponent(btnAddNewProfile, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED).addComponent(btnEditProfile, GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE).addPreferredGap(ComponentPlacement.RELATED).addComponent(btnDeleteProfile, GroupLayout.PREFERRED_SIZE, 179, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED)).addComponent(lblLogo, GroupLayout.DEFAULT_SIZE, 515, Short.MAX_VALUE).addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(lblMinecraftWebsite).addComponent(lblMinecraftSkinsServer).addComponent(lblMojangAuthServer)).addPreferredGap(ComponentPlacement.RELATED, 403, Short.MAX_VALUE).addGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addComponent(lblMojangAuthServerStatus).addGroup(groupLayout.createSequentialGroup().addComponent(lblMinecraftSkinsServerStatus).addPreferredGap(ComponentPlacement.RELATED)).addGroup(groupLayout.createSequentialGroup().addComponent(lblMinecraftWebsiteStatus).addPreferredGap(ComponentPlacement.RELATED))))))).addGap(9)).addGroup(groupLayout.createSequentialGroup().addContainerGap().addComponent(prgBarDownload, GroupLayout.DEFAULT_SIZE, 514, Short.MAX_VALUE).addContainerGap()));
        groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addContainerGap().addComponent(lblLogo).addGap(18).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblMinecraftWebsite).addComponent(lblMinecraftWebsiteStatus)).addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblMojangAuthServer).addComponent(lblMojangAuthServerStatus, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)).addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblMinecraftSkinsServer).addComponent(lblMinecraftSkinsServerStatus)).addGap(12).addComponent(prgBarDownload, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED, 34, Short.MAX_VALUE).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(btnAddNewProfile).addComponent(btnDeleteProfile).addComponent(btnEditProfile)).addGap(3).addComponent(cboxProfile, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(btnPlay).addContainerGap()));
        pane.setLayout(groupLayout);
        this.pack();
    }

    private final void deleteProfile(final String profileName) {
        ProfilesManager.getProfile(profileName).getFile().delete();
        ProfilesManager.removeProfileFromList(profileName);
        cboxProfile.removeItemAt(cboxProfile.getSelectedIndex());
        if (ProfilesManager.getProfiles().length == 0) {
            updateBtnPlay(false);
            btnDeleteProfile.setEnabled(false);
            btnEditProfile.setEnabled(false);
        }
    }

    @Override
    public void onStatusCheckBegin() {
        for (final JLabel label : status.values()) {
            label.setText("Asteapta...");
            label.setFont(label.getFont().deriveFont(Font.ITALIC));
            label.setForeground(Color.BLACK);
        }
    }

    @Override
    public void onStatusCheckFinished(final HashMap<String, Boolean> servicesStatus) {
        final Font font = LauncherConstants.LAUNCHER_FONT.deriveFont(Font.BOLD);
        for (final Entry<String, Boolean> entry : servicesStatus.entrySet()) {
            final JLabel label = status.get(entry.getKey());
            if (entry.getValue()) {
                label.setText("ONLINE");
                label.setForeground(new Color(255, 255, 255));
            } else {
                label.setText("INDISPONIBIL");
                label.setForeground(Color.RED);
            }
            label.setFont(font);
        }
    }

    @Override
    public void onGameTasksBegin() {
        updateBtnPlay(false);
    }

    @Override
    public void onGameTasksFinished(final boolean success, final ProfilesManager.LauncherProfile profile) {
        if (success && !profile.launcherVisible) {
            if (profile.logMinecraft) {
                for (final Frame frame : JFrame.getFrames()) {
                    frame.setVisible(false);
                }
            } else {
                System.exit(0);
            }
        }
        updateBtnPlay(true);
    }

    @Override
    public void onProfileChanged(final ProfilesManager.LauncherProfile oldProfile, final ProfilesManager.LauncherProfile newProfile) {
        profileEditor.setVisible(false);
        if (newProfile != null) {
            if (oldProfile != null) {
                deleteProfile(oldProfile.name);
            }
            cboxProfile.addItem(newProfile.name);
            cboxProfile.setSelectedItem(newProfile.name);
            ProfilesManager.setProfile(newProfile.name, newProfile);
            newProfile.save();
        }
        if (ProfilesManager.getProfiles().length >= 1) {
            updateBtnPlay(true);
            btnDeleteProfile.setEnabled(true);
            btnEditProfile.setEnabled(true);
        }
    }

    @Override
    public void onTokenTaskBegin() {
        tokensRefreshed = false;
        updateBtnPlay(false);
    }

    @Override
    public void onTokenTaskFinished(final HashMap<User, AuthSession> result) {
        for (final Entry<User, AuthSession> entry : result.entrySet()) {
            final AuthSession session = entry.getValue();
            if (session.selectedProfile != null) {
                final User oldUser = entry.getKey();
                final User newUser = new User(session.selectedProfile.name, session.selectedProfile.id, oldUser.accountName, true, session.accessToken, session.user.properties);
                profileEditor.model.removeElement(oldUser.username);
                oldUser.getFile().delete();
                UsersManager.removeUserFromList(oldUser.username, false);
                newUser.save();
                UsersManager.addUser(newUser);
                profileEditor.model.addElement(newUser.username);
                profileEditor.model.setSelectedItem(newUser.username);
            }
        }
        tokensRefreshed = true;
        updateBtnPlay(true);
    }

    private final void updateBtnPlay(final boolean enabled) {
        if (!enabled) {
            btnPlay.setText(ProfilesManager.getProfiles().length != 0 ? "Asteapta..." : "Creaza-ti un profil intai!");
            btnPlay.setEnabled(false);
        } else if (!profileEditor.isVisible() && tokensRefreshed && ProfilesManager.getProfiles().length != 0) {
            btnPlay.setEnabled(true);
            btnPlay.setText("Joaca");
        }
    }

}
