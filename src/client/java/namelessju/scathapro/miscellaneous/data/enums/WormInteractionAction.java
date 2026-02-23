package namelessju.scathapro.miscellaneous.data.enums;

public enum WormInteractionAction
{
    WARDROBE("Open Wardrobe"),
    SKYBLOCK_MENU("Open SkyBlock Menu");
    
    private final String displayString;
    
    WormInteractionAction(String displayString)
    {
        this.displayString = displayString;
    }
    
    @Override
    public String toString()
    {
        return displayString;
    }
}
