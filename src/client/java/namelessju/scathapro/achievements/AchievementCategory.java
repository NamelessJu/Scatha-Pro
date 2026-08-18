package namelessju.scathapro.achievements;

import org.jspecify.annotations.NonNull;

public enum AchievementCategory
{
    PROGRESS("Overall Achievement Progress"),
    KILL_AMOUNTS("Stoneworm & Scatha Kills"),
    ALONG_THE_WAY("Along The Way"),
    SCATHA_PET_DROPS("Scatha Pet Drops"),
    ALERT_MODES("Alert Modes"),
    HARD_STONE("Hard Stone Mined"),
    LOBBY_TIMER("Lobby Time"),
    TIMEFRAME_KILLS("Timeframed Worm Kills"),
    SCATHA_FARMING_STREAK("Scatha Farming Streak"),
    SPAWN_STREAK("Spawn Streak"),
    DILLY_DALLYING("Dilly Dallying"),
    MISCELLANEOUS("Miscellaneous");

    public final @NonNull String categoryName;

    AchievementCategory(@NonNull String name)
    {
        this.categoryName = name;
    }
}