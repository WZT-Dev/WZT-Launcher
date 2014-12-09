package ro.wzt.launcher.frames;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

import ro.wzt.launcher.LauncherConstants;
import ro.wzt.launcher.WZTLauncher;
import ro.wzt.launcher.UsersManager;
import ro.wzt.launcher.UsersManager.User;
import ro.wzt.launcher.tasks.ChangelogDownloader;
import ro.wzt.launcher.tasks.UpdateVersions;
import ro.wzt.launcher.utils.Utils;
import ro.wzt.launcher.ProfilesManager;

public class ProfileFrame extends JDialog implements UserFrame.UserChangesListener, UpdateVersions.VersionsListener {

	private static final long serialVersionUID = 1L;
	private ProfilesManager.LauncherProfile loadedProfile;
	private static final Color BACKGROUND_COLOR = new Color(255, 255, 255);
	protected final JTextField txtfldProfileName = new JTextField();
	protected final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>() {

		private static final long serialVersionUID = 1L;
		{
			for(final String user : UsersManager.getUsernames()) {
				addElement(user);
			}
		}

	};
	protected final JTextField txtfldGameDir = new JTextField();
	protected final JTextField txtfldArguments = new JTextField();
	protected final JComboBox<String> cboxVersion = new JComboBox<String>();
	protected final JButton btnRefreshList = new JButton("Refresh...");
	protected final JCheckBox chckbxLeaveLauncherVisible = new JCheckBox("Nu inchide launcherul") {

		private static final long serialVersionUID = 1L;
		{
			setBackground(BACKGROUND_COLOR);
			setForeground(Color.BLACK);
		}

	};
	protected final JCheckBox chckbxLogMinecraft = new JCheckBox("Log Minecraft") {

		private static final long serialVersionUID = 1L;
		{
			setBackground(BACKGROUND_COLOR);
			setForeground(Color.BLACK);
		}

	};
	protected JLabel lblUseravatar = new JLabel("Avatar") {

		private static final long serialVersionUID = 1L;
		{
			setFont(getFont().deriveFont(Font.ITALIC));
			setForeground(Color.BLACK);
		}

	};
	protected final JButton btnSave = new JButton("Salveaza") {

		private static final long serialVersionUID = 1L;
		{
			setFont(getFont().deriveFont(Font.BOLD));
		}

	};
	private static final HashMap<String, BufferedImage> cache = new HashMap<String, BufferedImage>();
	public static final List<ProfileChangesListener> listeners = new ArrayList<ProfileChangesListener>();

	public ProfileFrame(final LauncherFrame parent) {
		this(parent, null);
	}

	/**
	 * @wbp.parser.constructor
	 */

	public ProfileFrame(final LauncherFrame parent, final ProfilesManager.LauncherProfile profile) {
		UpdateVersions.addListener(this);
		UserFrame.addListener(this);
		this.setModal(true);
		this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.setLocationRelativeTo(parent);
		this.setAlwaysOnTop(true);
		this.loadedProfile = profile;
		this.setIconImage(LauncherConstants.LAUNCHER_ICON);
		this.setTitle("WZT-Launcher | Editor de profile");
		this.setPreferredSize(new Dimension(720, 292));
		this.setType(Type.POPUP);
		final Container pane = this.getContentPane();
		pane.setBackground(BACKGROUND_COLOR);
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(final WindowEvent event) {
				for(final ProfileChangesListener listener : listeners) {
					listener.onProfileChanged(loadedProfile, null);
				}
			}

		});
		final JLabel lblProfileName = new JLabel("Nume profil:");
		lblProfileName.setForeground(Color.BLACK);
		txtfldProfileName.setColumns(10);
		final JLabel lblUser = new JLabel("Utilizator:");
		lblUser.setForeground(Color.BLACK);
		final JComboBox<String> cboxUser = new JComboBox<String>(model);
		cboxUser.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent event) {
				loadAvatar((String)cboxUser.getSelectedItem());
			}

		});
		final JButton btnAddAnUser = new JButton("Adauga un utilizator...");
		btnAddAnUser.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent event) {
				new UserFrame(parent, null).setVisible(true);
			}

		});
		final JButton btnDeleteThisUser = new JButton("Sterge utilizatorul...");
		btnDeleteThisUser.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent event) {
				final String username = (String)cboxUser.getSelectedItem();
				UsersManager.getUser(username).getFile().delete();
				UsersManager.removeUserFromList(username);
				cboxUser.removeItem(username);
			}

		});
		final JLabel lblGameDir = new JLabel("Folderul jocului:");
		lblGameDir.setForeground(Color.BLACK);
		txtfldGameDir.setColumns(10);
		txtfldGameDir.setEnabled(false);
		final JButton button = new JButton("...");
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent event) {
				final JFileChooser directoryChooser = new JFileChooser();
				directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				directoryChooser.showOpenDialog((Component)event.getSource());
				final File selectedFile = directoryChooser.getSelectedFile();
				if(selectedFile != null) {
					txtfldGameDir.setText(selectedFile.getPath());
				}
			}

		});
		final JLabel lblArguments = new JLabel("Argumente:");
		lblArguments.setForeground(Color.BLACK);
		txtfldArguments.setColumns(10);
		final JLabel lblVersion = new JLabel("Versiune:");
		lblVersion.setForeground(Color.BLACK);
		btnRefreshList.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent event) {
				refreshVersions();
			}

		});
		final JButton btnChangelog = new JButton("Changelog...");
		btnChangelog.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent event) {
				new ChangelogDownloader().start();
			}

		});
		btnSave.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent event) {
				final String profileName = txtfldProfileName.getText();
				final String username = (String)cboxUser.getSelectedItem();
				final String gameDirPath = txtfldGameDir.getText();
				final String arguments = txtfldArguments.getText();
				final String version = (String)cboxVersion.getSelectedItem();
				if(profileName.length() == 0 || username == null || gameDirPath.length() == 0 || version == null) {
					JOptionPane.showMessageDialog(null, "Completeaza toate campurile!", "WZT-Launcher | Editor de profile", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(!Utils.isValidFileName(profileName)) {
					JOptionPane.showMessageDialog(null, "Nume invalid!", "WZT-Launcher | Editor de profile", JOptionPane.ERROR_MESSAGE);
					return;
				}
				final File gameDir = new File(gameDirPath);
				if(!gameDir.exists()) {
					gameDir.mkdirs();
				}
				for(final ProfileChangesListener listener : listeners) {
					listener.onProfileChanged(loadedProfile, new ProfilesManager.LauncherProfile(profileName, UsersManager.getUser(username), gameDir, arguments.length() == 0 ? null : arguments, version, chckbxLeaveLauncherVisible.isSelected(), chckbxLogMinecraft.isSelected()));
				}
			}

		});
		final GroupLayout groupLayout = new GroupLayout(pane);
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addContainerGap().addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addComponent(chckbxLogMinecraft).addContainerGap()).addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(btnSave, GroupLayout.DEFAULT_SIZE, 684, Short.MAX_VALUE).addGroup(groupLayout.createSequentialGroup().addComponent(lblArguments).addPreferredGap(ComponentPlacement.RELATED).addComponent(txtfldArguments, GroupLayout.DEFAULT_SIZE, 614, Short.MAX_VALUE)).addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(lblProfileName).addComponent(lblUser).addComponent(lblGameDir).addComponent(lblVersion)).addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addGroup(groupLayout.createSequentialGroup().addComponent(txtfldGameDir, GroupLayout.DEFAULT_SIZE, 563, Short.MAX_VALUE).addPreferredGap(ComponentPlacement.RELATED).addComponent(button, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)).addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addComponent(txtfldProfileName, GroupLayout.DEFAULT_SIZE, 604, Short.MAX_VALUE).addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addComponent(cboxVersion, 0, 338, Short.MAX_VALUE).addComponent(cboxUser, 0, 338, Short.MAX_VALUE)).addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false).addComponent(btnChangelog, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(btnAddAnUser, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)).addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false).addComponent(btnRefreshList, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(btnDeleteThisUser, GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE)))).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(lblUseravatar)))).addComponent(chckbxLeaveLauncherVisible)).addContainerGap()))));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addContainerGap(22, Short.MAX_VALUE).addGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblProfileName).addComponent(txtfldProfileName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblUser).addComponent(cboxUser, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(btnDeleteThisUser).addComponent(btnAddAnUser)).addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblVersion).addComponent(cboxVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(btnChangelog).addComponent(btnRefreshList))).addComponent(lblUseravatar)).addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblGameDir).addComponent(button).addComponent(txtfldGameDir, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblArguments).addComponent(txtfldArguments, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(chckbxLeaveLauncherVisible).addPreferredGap(ComponentPlacement.RELATED).addComponent(chckbxLogMinecraft).addGap(9).addComponent(btnSave, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE).addContainerGap()));
		pane.setLayout(groupLayout);
		loadProfile(loadedProfile);
		refreshVersions();
		loadAvatar((String)cboxUser.getSelectedItem());
		this.pack();
	}

	public static final void addListener(final ProfileChangesListener listener) {
		listeners.add(listener);
	}

	public final void loadAvatar(final String username) {
		new Thread() {

			@Override
			public void run() {
				lblUseravatar.setIcon(null);
				lblUseravatar.setText("Se incarca...");
				BufferedImage image = cache.get(username);
				if(image == null) {
					try {
						image = ImageIO.read(new URL("https://minotar.net/helm/" + username + "/80.png"));
						new Timer().scheduleAtFixedRate(new TimerTask() {

							@Override
							public void run() {
								cache.remove(username);
							}

						}, 0, 30000);
						lblUseravatar.setIcon(new ImageIcon(image));
					}
					catch(final Exception ex) {
						lblUseravatar.setVisible(false);
						ex.printStackTrace();
					}
					lblUseravatar.setText(null);
				}
			}

		}.start();
	}

	public final void refreshVersions() {
		new Thread() {

			@Override
			public final void run() {
				if(WZTLauncher.isOnline) {
					new UpdateVersions().start();
				}
				else {
					new UpdateVersions(new File(txtfldGameDir.getText() + File.separator + "versiuni")).start();
				}
			}

		}.start();
	}

	public final void loadProfile(final ProfilesManager.LauncherProfile profile) {
		this.loadedProfile = profile;
		if(profile != null) {
			txtfldProfileName.setText(profile.name);
			if(profile.user != null) {
				model.setSelectedItem(UsersManager.getUserByID(profile.user).username);
			}
			txtfldGameDir.setText(profile.gameDirectory.getPath());
			txtfldArguments.setText(profile.arguments);
			cboxVersion.setSelectedItem(profile.version);
			chckbxLeaveLauncherVisible.setSelected(profile.launcherVisible);
			chckbxLogMinecraft.setSelected(profile.logMinecraft);
		}
		else {
			txtfldProfileName.setText("Profil nou");
			txtfldGameDir.setText(WZTLauncher.SYSTEM.getMinecraftDirectory().getPath());
			txtfldArguments.setText("-Xms512m -Xmx1024m");
		}
	}

	@Override
	public void onUserSaved(final User user) {
		if(model.getIndexOf(user.username) == -1) {
			model.addElement(user.username);
		}
		model.setSelectedItem(user.username);
	}

	@Override
	public void onVersionsCheckBegin() {
		btnRefreshList.setEnabled(false);
		btnSave.setEnabled(false);
		btnSave.setText("Asteapta...");
	}

	@Override
	public void onVersionsReceived(final UpdateVersions.VersionsResult result) {
		if(result != null) {
			for(final UpdateVersions.Version version : result.versions) {
				cboxVersion.addItem(version.id);
			}
			btnSave.setEnabled(true);
		}
		btnRefreshList.setEnabled(true);
		btnSave.setText("Save");
	}

	public interface ProfileChangesListener {

		public void onProfileChanged(final ProfilesManager.LauncherProfile oldProfile, final ProfilesManager.LauncherProfile newProfile);

	}

}
