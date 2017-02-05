package server.events;

/**
 * Created by Maximilian on 19.01.2017.
 * Fired when the Utilisation Checking is enabled or disabled on the GUI.
 */
public class SwitchUtilisationCheckingEvent implements IEvent{

    final boolean useChecking;

    public SwitchUtilisationCheckingEvent(boolean useChecking) {

        this.useChecking = useChecking;
    }

    public boolean isUseChecking() {
        return useChecking;
    }
}