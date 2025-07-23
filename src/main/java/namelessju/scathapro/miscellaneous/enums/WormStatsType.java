package namelessju.scathapro.miscellaneous.enums;

public enum WormStatsType
{
    PER_LOBBY("Lobby"),
    PER_SESSION("Session"),
    PER_DAY("IRL Day");
    
    
    public static void addRegularWormSpawn()
    {
        for (WormStatsType stats : WormStatsType.values())
        {
            if (stats.scathaSpawnStreak > 0) stats.scathaSpawnStreak = 0;
            stats.scathaSpawnStreak --;
        }
    }
    
    public static void addScathaSpawn()
    {
        for (WormStatsType stats : WormStatsType.values())
        {
            if (stats.scathaSpawnStreak < 0) stats.scathaSpawnStreak = 0;
            stats.scathaSpawnStreak ++;
        }
    }
    
    public static void addRegularWormKill()
    {
        for (WormStatsType stats : WormStatsType.values())
            stats.regularWormKills ++;
    }
    
    public static void addScathaKill()
    {
        for (WormStatsType stats : WormStatsType.values())
            stats.scathaKills ++;
    }
    
    public static void resetForNewLobby()
    {
        PER_LOBBY.regularWormKills = 0;
        PER_LOBBY.scathaKills = 0;
        PER_LOBBY.scathaSpawnStreak = 0;
    }
    
    
    private String displayString;
    
    WormStatsType(String displayString)
    {
        this.displayString = displayString;
    }
    
    public int regularWormKills = 0;
    public int scathaKills = 0;
    public int scathaSpawnStreak = 0; // positive = Scatha streak; negative = regular worm streak
    
    @Override
    public String toString()
    {
        return displayString;
    }
}