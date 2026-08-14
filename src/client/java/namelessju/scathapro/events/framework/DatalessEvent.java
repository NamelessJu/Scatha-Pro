package namelessju.scathapro.events.framework;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class DatalessEvent extends ScathaProEvent<DatalessEvent.Listener>
{
    public void trigger()
    {
        for (Listener listener : listeners)
        {
            listener.onTriggered();
        }
    }

    @FunctionalInterface
    public interface Listener
    {
        void onTriggered();
    }
}