package namelessju.scathapro.events.framework;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class DataEvent<T> extends ScathaProEvent<DataEvent.Listener<T>>
{
    public void trigger(T eventData)
    {
        for (Listener<T> listener : listeners)
        {
            listener.onTriggered(eventData);
        }
    }

    @FunctionalInterface
    public interface Listener<T>
    {
        void onTriggered(T eventData);
    }
}