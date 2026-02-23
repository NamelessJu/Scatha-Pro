package namelessju.scathapro.miscellaneous.data.enums;

public enum DropMessageStatMode
{
    NUMBER_ONLY("Number"),
    SHORT_NAME("Abbreviated"),
    FULL_NAME("Full Name");
    
    public final String displayName;
    
    DropMessageStatMode(String displayName)
    {
        this.displayName = displayName;
    }
    
    @Override
    public String toString()
    {
        return displayName;
    }
}
