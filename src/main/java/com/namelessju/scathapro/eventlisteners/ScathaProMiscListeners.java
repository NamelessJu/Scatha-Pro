package com.namelessju.scathapro.eventlisteners;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.events.AchievementUnlockedEvent;
import com.namelessju.scathapro.events.DailyScathaFarmingStreakChangedEvent;
import com.namelessju.scathapro.events.ModUpdateEvent;
import com.namelessju.scathapro.events.SkyblockAreaDetectedEvent;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.miscellaneous.SkyblockArea;
import com.namelessju.scathapro.util.MessageUtil;
import com.namelessju.scathapro.util.SoundUtil;
import com.namelessju.scathapro.util.TimeUtil;

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
    public void onModUpdate(ModUpdateEvent e)
    {
        if (scathaPro.getConfig().getBoolean(Config.Key.automaticBackups) && scathaPro.getPersistentData().getData().entrySet().size() > 0)
        {
            scathaPro.getPersistentData().backup("update_" + (e.previousVersion != null ? "v" + e.previousVersion : "unknown") + "_to_v" + e.newVersion, true);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onSkyblockAreaDetected(SkyblockAreaDetectedEvent e)
    {
        if (e.area != SkyblockArea.CRYSTAL_HOLLOWS) return;
        
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
            MessageUtil.sendModChatMessage(chatComponent);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onAchievementUnlocked(AchievementUnlockedEvent e)
    {
        Achievement achievement = e.achievement;
        
        ChatComponentText chatMessage = new ChatComponentText(
                (
                        achievement.type.typeName != null
                        ? achievement.type.getFormattedName() + EnumChatFormatting.RESET + EnumChatFormatting.GREEN + " achievement"
                        : EnumChatFormatting.GREEN + "Achievement"
                )
                + " unlocked" + EnumChatFormatting.GRAY + " - "
        );
        
        ChatComponentText achievementComponent = new ChatComponentText(EnumChatFormatting.GOLD.toString() + EnumChatFormatting.ITALIC + achievement.achievementName);
        ChatStyle achievementStyle = new ChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.WHITE + achievement.achievementName + "\n" + EnumChatFormatting.GRAY + achievement.description)));
        achievementComponent.setChatStyle(achievementStyle);
        
        chatMessage.appendSibling(achievementComponent);
        
        MessageUtil.sendModChatMessage(chatMessage);
        
        long now = TimeUtil.now();
        
        if (now >= scathaPro.variables.lastWorldJoinTime + 1000 && now >= lastAchievementUnlockTime + 1000) // required or the game might crash when too many achievements are unlocked at once
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
        
        
        scathaPro.getAchievementLogicManager().updateProgressAchievements();

        
        lastAchievementUnlockTime = now;
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onDailyScathaFarmingStreakChanged(DailyScathaFarmingStreakChangedEvent e)
    {
        scathaPro.getAchievementLogicManager().updateDailyScathaStreakAchievements();
    }
}
