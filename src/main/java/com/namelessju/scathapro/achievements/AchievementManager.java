package com.namelessju.scathapro.achievements;

import java.util.ArrayList;

import com.namelessju.scathapro.PersistentData;
import com.namelessju.scathapro.events.AchievementUnlockedEvent;
import com.namelessju.scathapro.util.Util;

import net.minecraftforge.common.MinecraftForge;

public class AchievementManager {
    
    public static final AchievementManager instance = new AchievementManager();
    
    public ArrayList<UnlockedAchievement> unlockedAchievements = new ArrayList<UnlockedAchievement>();
    
    private AchievementManager() {}
    
    
    public void unlockAchievement(Achievement achievement) {
        if (!isAchievementUnlocked(achievement)) {
            unlockedAchievements.add(new UnlockedAchievement(achievement, Util.getCurrentTime()));
            
            PersistentData.instance.saveAchievements();
            
            MinecraftForge.EVENT_BUS.post(new AchievementUnlockedEvent(achievement));
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
