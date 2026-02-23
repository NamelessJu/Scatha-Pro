package namelessju.scathapro.miscellaneous;

import java.util.Iterator;

/**
 * A wrapper for iterators to make sure entries can't be removed
 */
public record IteratorWrapperImmutable<T>(Iterator<T> iterator) implements Iterator<T>
{
    @Override
    public boolean hasNext()
    {
        return iterator.hasNext();
    }
    
    @Override
    public T next()
    {
        return iterator.next();
    }
}
