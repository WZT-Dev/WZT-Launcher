package fr.skyost.launcher;

import java.awt.Color;

import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

import com.pagosoft.plaf.PgsTheme;

public class LauncherTheme extends PgsTheme {

	public LauncherTheme() {
		super("Skyolauncher Theme", new ColorUIResource(242, 241, 238), new ColorUIResource(0, 90, 140), new ColorUIResource(0, 90, 140), new ColorUIResource(35, 33, 29), new ColorUIResource(Color.LIGHT_GRAY), new ColorUIResource(23, 124, 181), Color.WHITE, new Color(0, 90, 140));
		UIManager.put("ProgressBar.selectionForeground", Color.WHITE);
		UIManager.put("ProgressBar.selectionBackground", Color.WHITE);
	}

}
