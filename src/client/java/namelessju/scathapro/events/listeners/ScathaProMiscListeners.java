package namelessju.scathapro.events.listeners;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.UpdateChecker;
import namelessju.scathapro.achievements.Achievement;
import namelessju.scathapro.events.*;
import namelessju.scathapro.managers.ChatManager;
import namelessju.scathapro.miscellaneous.data.enums.OldLobbyAlertTriggerMode;
import namelessju.scathapro.files.PersistentData;
import namelessju.scathapro.util.TimeUtil;
import net.minecraft.network.chat.Component;

public final class ScathaProMiscListeners
{
    private ScathaProMiscListeners() {}
    
    public static void register()
    {
        ScathaProEvents.newModVersionUsedEvent.addListener(ScathaProMiscListeners::onNewModVersionUsed);
        ScathaProEvents.achievementUnlockedEvent.addListener(ScathaProMiscListeners::onAchievementUnlocked);
        ScathaProEvents.crystalHollowsDayStartedEvent.addListener(ScathaProMiscListeners::onCrystalHollowsDayStarted);
        ScathaProEvents.scathaFarmingStreakChangedEvent.addListener(ScathaProMiscListeners::onScathaFarmingStreakChanged);
        ScathaProEvents.realDayStartedEvent.addListener(ScathaProMiscListeners::onRealDayStarted);
    }
    
    private static void onNewModVersionUsed(ScathaPro scathaPro, ScathaProEvents.NewModVersionUsedEventData data)
    {
        if (UpdateChecker.isPreRelease(data.newVersion()))
        {
            Achievement.play_mod_pre_release.unlock();
        }
        
        if (data.previousVersion() != null
            && UpdateChecker.compareVersions(data.previousVersion(), "1.3.2.1") >= 0)
        {
            Achievement.update_mod_to_v2.unlock();
        }
    }
    
    private static void onAchievementUnlocked(ScathaPro scathaPro, ScathaProEvents.AchievementUnlockedEventData data)
    {
        scathaPro.achievementLogicManager.updateProgressAchievements();
    }
    
    private static void onCrystalHollowsDayStarted(ScathaPro scathaPro, ScathaProEvents.CrystalHollowsDayStartedEventData data)
    {
        if (scathaPro.config.alerts.oldLobbyAlertEnabled.get()
            && OldLobbyAlertTriggerMode.ON_NEW_DAY.isActive(scathaPro.config))
        {
            int lobbyDay = data.day();
            if ((scathaPro.coreManager.lastOldLobbyAlertTriggerDay < 0 || lobbyDay < scathaPro.coreManager.lastOldLobbyAlertTriggerDay)
                && lobbyDay >= scathaPro.config.alerts.oldLobbyAlertTriggerDay.get())
            {
                scathaPro.alertManager.oldLobbyAlert.play();
                scathaPro.coreManager.lastOldLobbyAlertTriggerDay = lobbyDay;
            }
        }
    }
    
    private static void onRealDayStarted(ScathaPro scathaPro)
    {
        scathaPro.getProfileData().lastPlayedDate.set(TimeUtil.today());
        scathaPro.secondaryWormStatsManager.perDayStats.reset();
        scathaPro.persistentData.save();
        
        ScathaPro.LOGGER.debug("Daily stats reset");
        
        if (scathaPro.minecraft.level != null)
        {
            scathaPro.chatManager.sendChatMessage(
                Component.literal("New IRL day started - per day stats reset").setStyle(ChatManager.HIGHLIGHT_STYLE)
            );
        }
        
        scathaPro.coreManager.updateScathaFarmingStreak(false);
        
        scathaPro.mainOverlay.updateWormKills();
        scathaPro.mainOverlay.updateScathaKills();
        scathaPro.mainOverlay.updateTotalKills();
        scathaPro.mainOverlay.updateWormStreak();
        
        scathaPro.achievementLogicManager.updateKillsTodayAchievements();
    }
    
    private static void onScathaFarmingStreakChanged(ScathaPro scathaPro, ScathaProEvents.ScathaFarmingStreakChangedEventData data)
    {
        scathaPro.achievementLogicManager.updateDailyScathaStreakAchievements();
    }
}
