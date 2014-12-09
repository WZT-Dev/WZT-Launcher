package ro.wzt.launcher.tasks;

import com.google.gson.Gson;
import ro.wzt.launcher.LauncherConstants;
import ro.wzt.launcher.UsersManager;
import ro.wzt.launcher.WZTLauncher;
import ro.wzt.launcher.frames.UserFrame;
import ro.wzt.launcher.utils.ConnectionUtils;
import ro.wzt.launcher.utils.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public class RefreshToken extends Thread {

    private final UsersManager.User[] users;
    private static final List<RefreshTokenListener> listeners = new ArrayList<RefreshTokenListener>();

    public RefreshToken(final UsersManager.User... users) {
        this.users = users;
    }

    @Override
    public void run() {
        for (final RefreshTokenListener listener : listeners) {
            listener.onTokenTaskBegin();
        }
        final HashMap<UsersManager.User, AuthUser.AuthSession> result = new HashMap<UsersManager.User, AuthUser.AuthSession>();
        if (WZTLauncher.isOnline) {
            try {
                final Gson gson = new Gson();
                for (final UsersManager.User user : users) {
                    LogUtils.log(Level.INFO, LauncherConstants.REFRESH_TOKEN_PREFIX + "Refreshing access token for " + user.accountName + "...");
                    final String response = ConnectionUtils.httpJsonPost(LauncherConstants.REFRESH_TOKEN_URL, gson.toJson(new AuthUser.SimpleSession(user.accessToken, WZTLauncher.config.clientToken)));
                    final AuthUser.AuthSession session = gson.fromJson(response, AuthUser.AuthSession.class);
                    if (session.accessToken != null && session.clientToken != null) {
                        result.put(user, session);
                    } else {
                        final MojangError error = gson.fromJson(response, MojangError.class);
                        LogUtils.log(Level.SEVERE, LauncherConstants.REFRESH_TOKEN_PREFIX + "Unable to login : " + error.error);
                        LogUtils.log(Level.SEVERE, LauncherConstants.REFRESH_TOKEN_PREFIX + "Message : " + error.errorMessage);
                        if (error.cause != null) {
                            LogUtils.log(Level.SEVERE, LauncherConstants.REFRESH_TOKEN_PREFIX + "Cause : " + error.cause);
                        }
                        new UserFrame(null, user.accountName).setVisible(true);
                    }
                    LogUtils.log(Level.INFO, LauncherConstants.REFRESH_TOKEN_PREFIX + "Done.");
                }
            } catch (final Exception ex) {
                result.clear();
                ex.printStackTrace();
            }
        } else {
            LogUtils.log(Level.WARNING, LauncherConstants.REFRESH_TOKEN_PREFIX + "Cannot refresh your access token because you are offline !");
        }
        for (final RefreshTokenListener listener : listeners) {
            listener.onTokenTaskFinished(result);
        }
    }

    public static final void addListener(final RefreshTokenListener listener) {
        listeners.add(listener);
    }

    public interface RefreshTokenListener {

        public void onTokenTaskBegin();

        public void onTokenTaskFinished(final HashMap<UsersManager.User, AuthUser.AuthSession> result);

    }

    public class MojangError {

        public String error;
        public String errorMessage;
        public String cause;

    }

}
