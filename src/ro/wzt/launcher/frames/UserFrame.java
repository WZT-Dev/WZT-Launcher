package ro.wzt.launcher.frames;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JPasswordField;
import javax.swing.JButton;

import ro.wzt.launcher.LauncherConstants;
import ro.wzt.launcher.UsersManager;
import ro.wzt.launcher.UsersManager.User;
import ro.wzt.launcher.tasks.AuthUser;
import ro.wzt.launcher.tasks.UserUUID;
import ro.wzt.launcher.tasks.UserUUID;

public class UserFrame extends JDialog {

	private static final long serialVersionUID = 1L;

	public final JButton btnLogIn = new JButton("Logheaza-te") {

		private static final long serialVersionUID = 1L;
		{
			setFont(getFont().deriveFont(Font.BOLD));
		}

	};
	private static final List<UserChangesListener> listeners = new ArrayList<UserChangesListener>();

	public UserFrame(final JFrame parent, final String account) {
		final Color background = new Color(255, 255, 255);
		this.setSize(315, 200);
		this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.setModal(true);
		this.setIconImage(LauncherConstants.LAUNCHER_ICON);
		this.setLocationRelativeTo(parent);
		this.setAlwaysOnTop(true);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		final Container pane = this.getContentPane();
		pane.setBackground(background);
		final JLabel lblUsername = new JLabel("Utilizator:");
		lblUsername.setForeground(Color.BLACK);
		final JTextField txtfldUsername = new JTextField();
		txtfldUsername.setColumns(10);
		final JLabel lblPassword = new JLabel("Parola:");
		lblPassword.setForeground(Color.BLACK);
		final JPasswordField pswrdfldPassword = new JPasswordField();
		pswrdfldPassword.setEchoChar('x');
		final JCheckBox chckbxOfflineMode = new JCheckBox("Crack (offline mode)");
		chckbxOfflineMode.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(final ItemEvent event) {
				final boolean visible = !chckbxOfflineMode.isSelected();
				pswrdfldPassword.setVisible(visible);
				lblPassword.setVisible(visible);
				if(visible) {
					btnLogIn.setText("Logheaza-te");
				}
				else {
					btnLogIn.setText("Salveaza");
				}
			}
		});
		if(account != null) {
			this.setTitle("WZT-Launcher | Logheaza-te");
			txtfldUsername.setText(account);
		}
		else {
			this.setTitle("Adauga un utilizator...");
		}
		chckbxOfflineMode.setBackground(background);
		chckbxOfflineMode.setForeground(Color.BLACK);
		btnLogIn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent event) {
				final String username = txtfldUsername.getText();
				if(!UsersManager.hasUser(username)) {
					btnLogIn.setEnabled(false);
					btnLogIn.setText("Please wait...");
					if(pswrdfldPassword.isVisible()) {
						new AuthUser(username, new String(pswrdfldPassword.getPassword()), UserFrame.this).start();
					}
					else {
						new UserUUID(username, UserFrame.this).start();
					}
				}
				else {
					JOptionPane.showMessageDialog((Component)event.getSource(), "Userul exista deja!", "Eroare", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		final GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addContainerGap().addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(btnLogIn, GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE).addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(lblUsername).addComponent(lblPassword)).addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(pswrdfldPassword, GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE).addComponent(txtfldUsername, GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE))).addComponent(chckbxOfflineMode)).addContainerGap()));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addContainerGap().addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblUsername).addComponent(txtfldUsername, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblPassword).addComponent(pswrdfldPassword, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(chckbxOfflineMode).addPreferredGap(ComponentPlacement.RELATED, 40, Short.MAX_VALUE).addComponent(btnLogIn).addContainerGap()));
		pane.setLayout(groupLayout);
	}

	public static final void addListener(final UserChangesListener listener) {
		listeners.add(listener);
	}

	public interface UserChangesListener {

		public void onUserSaved(final User user);

	}

	public final void saveAndNotifyListeners(final User user) {
		user.save();
		UsersManager.setUser(user.username, user);
		for(final UserChangesListener listener : listeners) {
			listener.onUserSaved(user);
		}
		this.dispose();
	}

}
