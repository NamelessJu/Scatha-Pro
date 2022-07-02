package com.namelessju.scathapro.achievements;

import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.namelessju.scathapro.PersistentData;
import com.namelessju.scathapro.Util;

import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

public class AchievementManager {
    
    private static final AchievementManager instance = new AchievementManager();
    private static final String persistentDataKey = "unlockedAchievements";
    
    private final PersistentData persistentData = PersistentData.getInstance();
    
    private ArrayList<UnlockedAchievement> unlockedAchievements = new ArrayList<UnlockedAchievement>();
    
    private AchievementManager() {}

    public static AchievementManager getInstance() {
        return instance;
    }
    
    public void unlockAchievement(Achievement achievement) {
        if (!isAchievementUnlocked(achievement)) {
            unlockedAchievements.add(new UnlockedAchievement(achievement, Util.getCurrentTime()));
            saveAchievements();
            
            ChatComponentText chatMessage = new ChatComponentText(
                    (
                            achievement.hidden
                            ? EnumChatFormatting.AQUA.toString() + EnumChatFormatting.ITALIC + "SECRET" + EnumChatFormatting.RESET + EnumChatFormatting.GREEN + " achievement"
                            : EnumChatFormatting.GREEN + "Achievement"
                    )
                    + " unlocked" + EnumChatFormatting.GRAY + " - "
            );
            
            ChatComponentText achievementComponent = new ChatComponentText(EnumChatFormatting.GOLD.toString() + EnumChatFormatting.ITALIC + achievement.name);
            ChatStyle achievementStyle = new ChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(achievement.name + "\n" + EnumChatFormatting.GRAY + achievement.description)));
            achievementComponent.setChatStyle(achievementStyle);
            
            chatMessage.appendSibling(achievementComponent);

            Util.sendModChatMessage(chatMessage);

            if (achievement.hidden) Util.playModSoundAtPlayer("other.achievement_hidden");
            else Util.playModSoundAtPlayer("other.achievement");
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
    
    public void loadAchievements() {
        JsonElement achievementsJson = persistentData.get(persistentDataKey);
        if (achievementsJson != null && achievementsJson instanceof JsonArray) {
            JsonArray achievementsJsonArray = achievementsJson.getAsJsonArray();
            
            unlockedAchievements.clear();
            
            for (JsonElement achievementObjectJson : achievementsJsonArray) {
                if (achievementObjectJson instanceof JsonObject) {
                    JsonObject achievementObject = achievementObjectJson.getAsJsonObject();
                    
                    JsonElement achievementJson = achievementObject.get("achievementID");
                    if (achievementJson instanceof JsonPrimitive) {
                        JsonPrimitive achievementJsonPrimitive = achievementJson.getAsJsonPrimitive();
                        if (achievementJsonPrimitive.isString()) {
                            Achievement achievement = Achievement.getByID(achievementJsonPrimitive.getAsString());
                            
                            if (isAchievementUnlocked(achievement)) return;
                            
                            long unlockedAtTimestamp = -1; 
                            JsonElement unlockedAtJson = achievementObject.get("unlockedAt");
                            if (unlockedAtJson instanceof JsonPrimitive) {
                                JsonPrimitive unlockedAtJsonPrimitive = unlockedAtJson.getAsJsonPrimitive();
                                if (unlockedAtJsonPrimitive.isNumber()) unlockedAtTimestamp = unlockedAtJsonPrimitive.getAsLong();
                            }

                            if (achievement != null && unlockedAtTimestamp >= 0) {
                                unlockedAchievements.add(new UnlockedAchievement(achievement, unlockedAtTimestamp));
                                achievement.setProgress(achievement.goal);
                            }
                        }
                    }
                }
            }
        }
    }
    
    public void saveAchievements() {
        JsonArray unlockedAchievementsJson = new JsonArray();
        
        for (UnlockedAchievement unlockedAchievement : unlockedAchievements) {
            JsonObject achievementObject = new JsonObject();
            achievementObject.add("achievementID", new JsonPrimitive(unlockedAchievement.achievement.getID()));
            achievementObject.add("unlockedAt", new JsonPrimitive(unlockedAchievement.unlockedAtTimestamp));
            unlockedAchievementsJson.add(achievementObject);
        }
        
        persistentData.set(persistentDataKey, unlockedAchievementsJson);
        
        persistentData.saveData();
    }
    
    public static Achievement[] getAllAchievements() {
        return Achievement.values();
    }
}
