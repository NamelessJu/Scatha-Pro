package namelessju.scathapro.events.listeners;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.UpdateChecker;
import namelessju.scathapro.achievements.Achievement;
import namelessju.scathapro.events.ScathaProEvents;
import namelessju.scathapro.files.PersistentData;
import namelessju.scathapro.parsing.ScoreboardParser;
import namelessju.scathapro.util.TimeUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;

public final class ScathaProTickListeners
{
    private ScathaProTickListeners() {}
    
    public static void register()
    {
        ScathaProEvents.firstSessionIngameTickEvent.addListener(ScathaProTickListeners::onFirstIngameTick);
        ScathaProEvents.crystalHollowsTickEvent.addListener(ScathaProTickListeners::onCrystalHollowsTick);
    }
    
    private static int heatCheckTickTimer = 0;
    
    private static void onFirstIngameTick(ScathaPro scathaPro)
    {
        if (scathaPro.config.miscellaneous.automaticUpdateCheckEnabled.get())
        {
            UpdateChecker.checkForUpdate(scathaPro, false);
        }
        
        scathaPro.achievementLogicManager.updatePetDropAchievements();
        scathaPro.achievementLogicManager.updateProgressAchievements();
        scathaPro.achievementLogicManager.updateDailyScathaStreakAchievements();
    }
    
    private static void onCrystalHollowsTick(ScathaPro scathaPro, ScathaProEvents.CrystalHollowsTickEventData data)
    {
        // TODO: move most of this into core manager
        
        long now = TimeUtil.now();
        
        if (data.isFirstTick())
        {
            heatCheckTickTimer = 0;
            
            if (scathaPro.getProfileData().regularWormKills.get() == 0 && scathaPro.getProfileData().scathaKills.get() == 0
                && scathaPro.config.miscellaneous.automaticStatsParsingEnabled.get())
            {
                scathaPro.chatManager.sendChatMessage(
                    Component.literal("Open the worm bestiary once to load previous worm kills into the overlay!")
                    .withStyle(ChatFormatting.YELLOW)
                );
            }
            
            scathaPro.coreManager.updateScathaFarmingStreak(false);
            
            if (scathaPro.config.sounds.muteCrystalHollowsSounds.get())
            {
                String settingsCommand = "/" + scathaPro.mainCommand.getCommandName() + " settings";
                scathaPro.chatManager.sendChatMessage(Component.empty().withStyle(ChatFormatting.GRAY)
                    .append("Reminder: You've muted Crystal Hollows sounds! Only " + scathaPro.getModDisplayName()
                        + " sounds will play - you can disable this in ")
                    .append(Component.literal(settingsCommand).setStyle(Style.EMPTY
                        .withUnderlined(true)
                        .withHoverEvent(new HoverEvent.ShowText(
                            Component.literal("Open " + scathaPro.getModDisplayName() + " settings").withStyle(ChatFormatting.GRAY)
                        ))
                        .withClickEvent(new ClickEvent.RunCommand(settingsCommand))
                    ))
                    .append("!")
                );
            }
        }
        
        // Sneak start
        
        boolean isCrouching = scathaPro.minecraft.player != null && scathaPro.minecraft.player.isCrouching();
        if (!scathaPro.coreManager.crouchingBefore && isCrouching)
        {
            scathaPro.coreManager.lastCrouchStartTime = now;
        }
        scathaPro.coreManager.crouchingBefore = isCrouching;
        
        
        // Achievements
        
        float hours = (now - scathaPro.coreManager.lastWorldJoinTime) / (1000f*60*60);
        Achievement.crystal_hollows_time_1.setProgress(hours);
        Achievement.crystal_hollows_time_2.setProgress(hours);
        Achievement.crystal_hollows_time_3.setProgress(hours);
        
        
        // Heat check
        
        heatCheckTickTimer ++;
        if (heatCheckTickTimer > 3*20)
        {
            heatCheckTickTimer = 0;
            
            if (scathaPro.config.alerts.highHeatAlertEnabled.get())
            {
                int newHeat = ScoreboardParser.parseHeat(scathaPro.minecraft).orElse(-1);
                if (newHeat > 0)
                {
                    int triggerValue = scathaPro.config.alerts.highHeatAlertTriggerValue.get();
                    if (newHeat >= triggerValue && scathaPro.coreManager.lastHeat >= 0 && scathaPro.coreManager.lastHeat < triggerValue)
                    {
                        scathaPro.alertManager.highHeatAlert.play();
                    }
                }
                
                scathaPro.coreManager.lastHeat = newHeat;
            }
            else scathaPro.coreManager.lastHeat = -1;
        }
    }
}
