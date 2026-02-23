package namelessju.scathapro.events;

import namelessju.scathapro.ScathaPro;

import java.util.ArrayList;

public class Event<T>
{
    private final ArrayList<Listener<T>> listeners = new ArrayList<>(1);
    
    public void addListener(Listener<T> listener)
    {
        listeners.add(listener);
    }
    
    public void trigger(ScathaPro scathaPro, T eventData)
    {
        for (Listener<T> listener : listeners)
        {
            listener.onTriggered(scathaPro, eventData);
        }
    }
    
    @FunctionalInterface
    public interface Listener<T>
    {
        void onTriggered(ScathaPro scathaPro, T eventData);
    }
}