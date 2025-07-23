package namelessju.scathapro.eventlisteners;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.achievements.Achievement;
import namelessju.scathapro.achievements.AchievementCategory;
import namelessju.scathapro.achievements.UnlockedAchievement;
import namelessju.scathapro.alerts.Alert;
import namelessju.scathapro.events.AchievementUnlockedEvent;
import namelessju.scathapro.events.CrystalHollowsDayStartedEvent;
import namelessju.scathapro.events.DailyScathaFarmingStreakChangedEvent;
import namelessju.scathapro.events.DailyStatsResetEvent;
import namelessju.scathapro.events.ModUpdateEvent;
import namelessju.scathapro.events.RealDayStartedEvent;
import namelessju.scathapro.events.SkyblockAreaDetectedEvent;
import namelessju.scathapro.managers.Config;
import namelessju.scathapro.managers.SaveManager;
import namelessju.scathapro.miscellaneous.enums.OldLobbyAlertTriggerMode;
import namelessju.scathapro.miscellaneous.enums.SkyblockArea;
import namelessju.scathapro.util.SoundUtil;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.TimeUtil;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ScathaProMiscListeners extends ScathaProListener
{
    private long lastAchievementUnlockTime = -1;
    
    public ScathaProMiscListeners(ScathaPro scathaPro)
    {
        super(scathaPro);
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onModUpdate(ModUpdateEvent event)
    {
        if (scathaPro.getConfig().getBoolean(Config.Key.automaticBackups) && scathaPro.getPersistentData().getData().entrySet().size() > 0)
        {
            SaveManager.backup("update_" + (event.previousVersion != null ? event.previousVersion : "unknown") + "_to_" + event.newVersion);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onSkyblockAreaDetected(SkyblockAreaDetectedEvent event)
    {
        if (event.area != SkyblockArea.CRYSTAL_HOLLOWS) return;

        scathaPro.getPersistentData().updateScathaFarmingStreak(false);
        
        if (scathaPro.getConfig().getBoolean(Config.Key.muteCrystalHollowsSounds))
        {
            ChatComponentText chatComponent = new ChatComponentText(EnumChatFormatting.GRAY + "You've muted sounds in the Crystal Hollows! Only Scatha-Pro sounds will play - you can unmute other sounds again in ");
            
            ChatComponentText commandComponent = new ChatComponentText(EnumChatFormatting.GRAY.toString() + EnumChatFormatting.UNDERLINE + "/scathapro settings");
            commandComponent.setChatStyle(
                    new ChatStyle()
                    .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/scathapro settings"))
                    .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Open Scatha-Pro settings")))
            );
            chatComponent.appendSibling(commandComponent);
            
            chatComponent.appendSibling(new ChatComponentText(EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY + "!"));
            TextUtil.sendModChatMessage(chatComponent);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onAchievementUnlocked(AchievementUnlockedEvent event)
    {
        long now = TimeUtil.now();

        Achievement achievement = event.unlockedAchievement.achievement;
        boolean repeated = achievement.isRepeatable && event.unlockedAchievement.getRepeatCount() > 0;
        
        if (scathaPro.getConfig().getBoolean(Config.Key.playAchievementAlerts)
            && (!repeated || scathaPro.getConfig().getBoolean(Config.Key.playRepeatAchievementAlerts)))
        {
            String unlockTextFormatting;
            String unlockWord;
            if (repeated)
            {
                unlockTextFormatting = UnlockedAchievement.repeatFormatting;
                unlockWord = "repeated";
            }
            else
            {
                unlockTextFormatting = EnumChatFormatting.GREEN.toString();
                unlockWord = "unlocked";
            }
            ChatComponentText chatMessage = new ChatComponentText(
                    (
                            achievement.type.typeName != null
                            ? achievement.type.getFormattedName() + EnumChatFormatting.RESET + unlockTextFormatting + " achievement"
                            : unlockTextFormatting + "Achievement"
                    )
                    + " " + unlockWord + ": "
            );
            
            ChatComponentText achievementNameComponent = new ChatComponentText(EnumChatFormatting.GOLD.toString() + EnumChatFormatting.ITALIC + achievement.achievementName);
            
            ChatStyle achievementStyle = new ChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GREEN.toString() + EnumChatFormatting.BOLD + achievement.achievementName + "\n" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY + achievement.description + (achievement.isRepeatable ? "\n" + EnumChatFormatting.RESET + UnlockedAchievement.repeatFormatting + "[Repeatable]" + EnumChatFormatting.RESET : "") + "\n\n" + EnumChatFormatting.GOLD + AchievementCategory.getName(achievement.category))));
            achievementNameComponent.setChatStyle(achievementStyle);
            
            chatMessage.appendSibling(achievementNameComponent);
            if (repeated)
            {
                chatMessage.appendText(EnumChatFormatting.RESET.toString() + " " + event.unlockedAchievement.getRepeatCountUnlockString());
            }
            
            TextUtil.sendModChatMessage(chatMessage);
            
            if (now >= scathaPro.variables.lastWorldJoinTime + 1000 && now >= lastAchievementUnlockTime + 1000) // required or the game might crash when too many achievements are unlocked at once
            {
            	if (repeated) SoundUtil.playModSound("achievements.unlock", 0.85f, 1.259921f);
            	else
            	{
	                switch (achievement.type)
	                {
	                    case SECRET:
	                        SoundUtil.playModSound("achievements.unlock", 0.9f, 0.749154f);
	                        break;
	                    case BONUS:
	                        SoundUtil.playModSound("achievements.unlock_hidden", 0.75f, 1.259921f);
	                        break;
	                    case HIDDEN:
	                        SoundUtil.playModSound("achievements.unlock_hidden", 0.75f, 0.749154f);
	                        break;
	                    default:
	                        SoundUtil.playModSound("achievements.unlock", 0.9f, 1f);
	                }
            	}
            }
        }
        
        scathaPro.getAchievementLogicManager().updateProgressAchievements();

        
        lastAchievementUnlockTime = now;
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onCrystalHollowsDayStarted(CrystalHollowsDayStartedEvent event)
    {
        if (scathaPro.getConfig().getBoolean(Config.Key.oldLobbyAlert)
            && OldLobbyAlertTriggerMode.isActive(scathaPro.getConfig(), OldLobbyAlertTriggerMode.ON_NEW_DAY))
        {
            int lobbyDay = event.day;
            if ((scathaPro.variables.lastOldLobbyAlertTriggerDay < 0 || lobbyDay < scathaPro.variables.lastOldLobbyAlertTriggerDay)
                && lobbyDay >= scathaPro.getConfig().getInt(Config.Key.oldLobbyAlertTriggerDay))
            {
                Alert.oldLobby.play();
                scathaPro.variables.lastOldLobbyAlertTriggerDay = lobbyDay;
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRealDayStarted(RealDayStartedEvent event)
    {
        scathaPro.getPersistentData().resetDailyStats();
        scathaPro.getPersistentData().updateScathaFarmingStreak(false);
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onDailyScathaFarmingStreakChanged(DailyScathaFarmingStreakChangedEvent event)
    {
        scathaPro.getAchievementLogicManager().updateDailyScathaStreakAchievements();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onDailyStatsReset(DailyStatsResetEvent event)
    {
        scathaPro.getOverlay().updateWormKills();
        scathaPro.getOverlay().updateScathaKills();
        scathaPro.getOverlay().updateTotalKills();
        
        scathaPro.getAchievementLogicManager().updateKillsTodayAchievements();
    }
}
