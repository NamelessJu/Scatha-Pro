package namelessju.scathapro.miscellaneous.data.enums;

public enum DropMessageRarityMode
{
    SUFFIX("Suffix", false, true),
    PREFIX("Prefix", true, true),
    PREFIX_NO_BRACKETS("Prefix (No Brackets)", true, false);
    
    public final boolean hasBrackets;
    public final boolean isPrefix;
    
    private final String displayName;
    
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

