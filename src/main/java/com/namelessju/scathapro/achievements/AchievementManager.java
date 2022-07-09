package com.namelessju.scathapro.achievements;

import java.util.ArrayList;

import com.namelessju.scathapro.PersistentData;
import com.namelessju.scathapro.Util;

import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

public class AchievementManager {
    
    public static final AchievementManager instance = new AchievementManager();
    
    public ArrayList<UnlockedAchievement> unlockedAchievements = new ArrayList<UnlockedAchievement>();
    
    private long lastAchievementUnlockTime = -1;
    
    private AchievementManager() {}
    
    
    public void unlockAchievement(Achievement achievement) {
        if (!isAchievementUnlocked(achievement)) {
            long now = Util.getCurrentTime();
            
            unlockedAchievements.add(new UnlockedAchievement(achievement, Util.getCurrentTime()));
            
            PersistentData.instance.saveAchievements();
            
            ChatComponentText chatMessage = new ChatComponentText(
                    (
                            achievement.type.string != null
                            ? achievement.type.string + EnumChatFormatting.RESET + EnumChatFormatting.GREEN + " achievement"
                            : EnumChatFormatting.GREEN + "Achievement"
                    )
                    + " unlocked" + EnumChatFormatting.GRAY + " - "
            );
            
            ChatComponentText achievementComponent = new ChatComponentText(EnumChatFormatting.GOLD.toString() + EnumChatFormatting.ITALIC + achievement.name);
            ChatStyle achievementStyle = new ChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(achievement.name + "\n" + EnumChatFormatting.GRAY + achievement.description)));
            achievementComponent.setChatStyle(achievementStyle);
            
            chatMessage.appendSibling(achievementComponent);
            
            Util.sendModChatMessage(chatMessage);
            
            if (now >= lastAchievementUnlockTime + 1000) {
                switch (achievement.type) {
                    case SECRET:
                        Util.playSoundAtPlayer("fireworks.twinkle", 1f, 0.75f);
                        Util.playSoundAtPlayer("random.successful_hit", 0.75f, 0f);
                        break;
                    case HIDDEN:
                        Util.playSoundAtPlayer("fireworks.twinkle", 1f, 0.9f);
                        Util.playSoundAtPlayer("random.levelup", 1f, 1.25f);
                        break;
                    default:
                        Util.playSoundAtPlayer("fireworks.twinkle", 1f, 1f);
                        Util.playSoundAtPlayer("note.pling", 0.5f, 2f);
                        break;
                }
            }
            
            lastAchievementUnlockTime = now;
        }
    }
    
    public boolean isAchievementUnlocked(Achievement achievement) {
        return getUnlockedAchievement(achievement) != null;
    }
    
    public UnlockedAchievement getUnlockedAchievement(Achievement achievement) {
        for (UnlockedAchievement unlockedAchievement : unlockedAchievements) {
            if (unlockedAchievement.achievement == achievement) return unlockedAchievement;
        }
        return null;
    }
    
    public static Achievement[] getAllAchievements() {
        return Achievement.values();
    }
}
