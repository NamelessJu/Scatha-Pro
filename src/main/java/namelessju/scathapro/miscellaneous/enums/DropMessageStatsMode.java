package namelessju.scathapro.miscellaneous.enums;

public enum DropMessageStatsMode
{
    ADD_PET_LUCK("Add Pet Luck", true, true, null), ADD_PET_LUCK_AND_EMF("Add Pet Luck & EMF", true, true, EmfMode.SHORT), EMF_ONLY_FULL_NAME("Replace With EMF (Full Name)", false, false, EmfMode.FULL), EMF_ONLY("Replace With EMF", false, false, EmfMode.SHORT);
    
    public final String displayName;
    public final boolean showMagicFind;
    public final boolean showPetLuck;
    public final EmfMode emfMode;
    
    DropMessageStatsMode(String displayName, boolean showMagicFind, boolean showPetLuck, EmfMode emfMode)
    {
        this.displayName = displayName;
        this.showMagicFind = showMagicFind;
        this.showPetLuck = showPetLuck;
        this.emfMode = emfMode;
    }
    
    @Override
    public String toString()
    {
        return displayName;
    }
    
    public static enum EmfMode
    {
        FULL, SHORT;
    }
}
