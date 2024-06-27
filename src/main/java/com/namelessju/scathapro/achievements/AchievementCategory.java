package com.namelessju.scathapro.achievements;

public enum AchievementCategory
{
    PROGRESS("Progress"),
    KILL_AMOUNTS("Kill Amount"),
    SCATHA_PET_DROPS("Scatha Pet Drop"),
    ALERT_MODES("Alert Mode"),
    HARD_STONE("Hard Stone"),
    LOBBY_TIMER("Lobby Timer"),
    WORM_LIFETIME("Worm Lifetime"),
    TIMEFRAME_KILLS("Worm Kills In Timeframe"),
    SCATHA_FARMING_STREAK("Scatha Farming Streak"),
    SPAWN_STREAK("Spawn Streak"),
    SCATHA_SPAWNS("Scatha Spawn"),
    WORM_HIT_KILL("Worm Hit/Kill"),
    MISCELLANEOUS("Miscellaneous");
    
    private final String name;
    
    AchievementCategory(String name)
    {
        this.name = name;
    }
    
    public static String getName(AchievementCategory category)
    {
        return (category != null ? category.name : "Uncategorized") + " Achievements";
    }
}
