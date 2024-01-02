package com.namelessju.scathapro.achievements;

import java.util.ArrayList;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.events.AchievementUnlockedEvent;
import com.namelessju.scathapro.util.Util;

import net.minecraftforge.common.MinecraftForge;

public class AchievementManager {
    
    public ArrayList<UnlockedAchievement> unlockedAchievements = new ArrayList<UnlockedAchievement>();
    
    public void unlockAchievement(Achievement achievement) {
        if (!isAchievementUnlocked(achievement)) {
            unlockedAchievements.add(new UnlockedAchievement(achievement, Util.getCurrentTime()));
            
            ScathaPro.getInstance().persistentData.saveAchievements();
            
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
