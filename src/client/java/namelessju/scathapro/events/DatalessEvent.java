package namelessju.scathapro.events;

import namelessju.scathapro.ScathaPro;

import java.util.ArrayList;

public class DatalessEvent
{
    private final ArrayList<Listener> listeners = new ArrayList<>(1);
    
    public void addListener(Listener listener)
    {
        listeners.add(listener);
    }
    
    public void trigger(ScathaPro scathaPro)
    {
        for (Listener listener : listeners)
        {
            listener.onTriggered(scathaPro);
        }
    }
    
    @FunctionalInterface
    public interface Listener
    {
        void onTriggered(ScathaPro scathaPro);
    }
}