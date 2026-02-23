package namelessju.scathapro.miscellaneous.data.enums;

public enum SecondaryWormStatsType
{
    PER_LOBBY("Lobby"),
    PER_SESSION("Game Session"),
    PER_DAY("IRL Day");
    
    private final String displayString;
    
    SecondaryWormStatsType(String displayString)
    {
        this.displayString = displayString;
    }
    
    @Override
    public String toString()
    {
        return displayString;
    }
}