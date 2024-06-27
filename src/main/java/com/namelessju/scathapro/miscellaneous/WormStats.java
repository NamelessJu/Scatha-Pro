package com.namelessju.scathapro.miscellaneous;

import com.namelessju.scathapro.gui.elements.MultiOptionButton;

public enum WormStats implements MultiOptionButton.IOption<String>
{
    PER_LOBBY("Lobby", "lobby"),
    PER_SESSION("Session", "session"),
    PER_DAY("IRL Day", "day");
    
    
    public static void addRegularWormSpawn()
    {
        for (WormStats stats : WormStats.values())
        {
            if (stats.scathaSpawnStreak > 0) stats.scathaSpawnStreak = 0;
            stats.scathaSpawnStreak --;
        }
    }
    
    public static void addScathaSpawn()
    {
        for (WormStats stats : WormStats.values())
        {
            if (stats.scathaSpawnStreak < 0) stats.scathaSpawnStreak = 0;
            stats.scathaSpawnStreak ++;
        }
    }
    
    public static void addRegularWormKill()
    {
        for (WormStats stats : WormStats.values())
            stats.regularWormKills ++;
    }
    
    public static void addScathaKill()
    {
        for (WormStats stats : WormStats.values())
            stats.scathaKills ++;
    }
    
    public static void resetForNewLobby()
    {
        PER_LOBBY.regularWormKills = 0;
        PER_LOBBY.scathaKills = 0;
        PER_LOBBY.scathaSpawnStreak = 0;
    }
    
    
    private String displayString;
    private String id;
    
    WormStats(String displayString, String id)
    {
        this.displayString = displayString;
        this.id = id;
    }
    
    public int regularWormKills = 0;
    public int scathaKills = 0;
    public int scathaSpawnStreak = 0; // positive = Scatha streak; negative = regular worm streak
    
    @Override
    public String getOptionName()
    {
        return displayString;
    }

    @Override
    public String getOptionValue()
    {
        return id;
    }
}