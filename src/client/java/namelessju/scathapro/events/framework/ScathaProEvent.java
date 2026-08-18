package namelessju.scathapro.events.framework;

import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;

@NullMarked
public abstract class ScathaProEvent<T>
{
    protected final ArrayList<T> listeners = new ArrayList<>(1);

    public final void addListener(T listener)
    {
        listeners.add(listener);
    }
    public final void removeListener(T listener)
    {
        listeners.remove(listener);
    }
    public final int getListenerCount()
    {
        return listeners.size();
    }
}