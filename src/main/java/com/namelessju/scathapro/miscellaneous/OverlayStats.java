package com.namelessju.scathapro.miscellaneous;

import com.namelessju.scathapro.gui.elements.MultiOptionButton;

public enum OverlayStats implements MultiOptionButton.IOption<String>
{
    PER_LOBBY("Lobby", "lobby"),
    PER_SESSION("Session", "session");
    // PER_DAY("Day", "day");
    
    
    public static void addRegularWormSpawn()
    {
        for (OverlayStats stats : getAllStats())
            stats._addRegularWormSpawn();
    }
    
    public static void addScathaSpawn()
    {
        for (OverlayStats stats : getAllStats())
            stats._addScathaSpawn();
    }
    
    public static void addRegularWormKill()
    {
        for (OverlayStats stats : getAllStats())
            stats.regularWormKills ++;
    }
    
    public static void addScathaKill()
    {
        for (OverlayStats stats : getAllStats())
            stats.scathaKills ++;
    }
    
    public static void resetForNewLobby()
    {
        PER_LOBBY.regularWormKills = 0;
        PER_LOBBY.scathaKills = 0;
        PER_LOBBY.scathaSpawnStreak = 0;
    }
    
    private static OverlayStats[] getAllStats()
    {
        return OverlayStats.values();
    }
    
    
    private String displayString;
    private String id;
    
    OverlayStats(String displayString, String id)
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
    
    
    private void _addRegularWormSpawn()
    {
        if (scathaSpawnStreak > 0) scathaSpawnStreak = 0;
        scathaSpawnStreak --;
    }
    
    private void _addScathaSpawn()
    {
        if (scathaSpawnStreak < 0) scathaSpawnStreak = 0;
        scathaSpawnStreak ++;
    }
}