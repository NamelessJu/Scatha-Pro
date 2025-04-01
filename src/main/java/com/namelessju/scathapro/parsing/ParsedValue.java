package com.namelessju.scathapro.parsing;

public class ParsedValue<T>
{
    private T value = null;
    
    public T getValue(T nullReplacementValue)
    {
        return value != null ? value : nullReplacementValue;
    }
    
    public void setValue(T value)
    {
        this.value = value;
    }
    
    public boolean hasValue()
    {
        return value != null;
    }
}