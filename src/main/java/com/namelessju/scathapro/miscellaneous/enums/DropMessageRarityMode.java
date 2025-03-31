package com.namelessju.scathapro.miscellaneous.enums;

public enum DropMessageRarityMode
{
    SUFFIX("Suffix", false, true), PREFIX("Prefix", true, true), PREFIX_NO_BRACKETS("Prefix (No Brackets)", true, false);
    
    public final String displayName;
    public final boolean hasBrackets;
    public final boolean isPrefix;
    
    DropMessageRarityMode(String displayName, boolean isPrefix, boolean hasBrackets)
    {
        this.displayName = displayName;
        this.isPrefix = isPrefix;
        this.hasBrackets = hasBrackets;
    }
    
    @Override
    public String toString()
    {
        return displayName;
    }
}

