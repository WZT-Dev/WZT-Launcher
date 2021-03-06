package ro.wzt.launcher.tasks;

import com.google.gson.Gson;
import ro.wzt.launcher.LauncherConstants;
import ro.wzt.launcher.WZTLauncher;
import ro.wzt.launcher.utils.ConnectionUtils;

import java.util.*;
import java.util.Map.Entry;

public class ServicesStatus extends TimerTask {

    private final Collection<String> services;
    private static final List<ServiceStatusListener> listeners = new ArrayList<ServiceStatusListener>();

    public ServicesStatus(final String... services) {
        this(Arrays.asList(services));
    }

    public ServicesStatus(final Collection<String> services) {
        this.services = services;
    }

    @Override
    public void run() {
        final HashMap<String, Boolean> result = new HashMap<String, Boolean>();
        try {
            for (final ServiceStatusListener listener : listeners) {
                listener.onStatusCheckBegin();
            }
            if (WZTLauncher.isOnline) {
                final HashMap<?, ?>[] responses = new Gson().fromJson(ConnectionUtils.httpGet(LauncherConstants.STATUS_CHECK_URL, null), HashMap[].class);
                for (final HashMap<?, ?> response : responses) {
                    for (final Entry<?, ?> entry : response.entrySet()) {
                        final String service = (String) entry.getKey();
                        if (services.contains(service)) {
                            result.put(service, entry.getValue().equals("green"));
                        }
                    }
                }
                notifyListeners(true, result);
            } else {
                notifyListeners(false, result);
            }
        } catch (final Exception ex) {
            notifyListeners(false, result);
            ex.printStackTrace();
        }
    }

    public final void notifyListeners(final boolean success, final HashMap<String, Boolean> result) {
        if (!success) {
            for (final String service : services) {
                result.put(service, false);
            }
        }
        for (final ServiceStatusListener listener : listeners) {
            listener.onStatusCheckFinished(result);
        }
    }

    public static final void addListener(final ServiceStatusListener listener) {
        listeners.add(listener);
    }

    public interface ServiceStatusListener {

        public void onStatusCheckBegin();

        public void onStatusCheckFinished(final HashMap<String, Boolean> servicesStatus);

    }

}
