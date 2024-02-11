package com.namelessju.scathapro.achievements;

import java.util.ArrayList;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.events.AchievementUnlockedEvent;
import com.namelessju.scathapro.util.Util;

import net.minecraftforge.common.MinecraftForge;

public class AchievementManager
{
    private final ScathaPro scathaPro;
    
    public ArrayList<UnlockedAchievement> unlockedAchievements = new ArrayList<UnlockedAchievement>();
    
    public AchievementManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
    }
    
    public void unlockAchievement(Achievement achievement)
    {
        if (!isAchievementUnlocked(achievement))
        {
            unlockedAchievements.add(new UnlockedAchievement(achievement, Util.getCurrentTime()));
            
            scathaPro.persistentData.saveAchievements();
            
            MinecraftForge.EVENT_BUS.post(new AchievementUnlockedEvent(achievement));
        }
    }
    
    public void revokeAchievement(Achievement achievement)
    {
        if (isAchievementUnlocked(achievement))
        {
            unlockedAchievements.remove(getUnlockedAchievement(achievement));
            achievement.setProgress(0f);
            
            scathaPro.persistentData.saveAchievements();
        }
    }
    
    public boolean isAchievementUnlocked(Achievement achievement)
    {
        return getUnlockedAchievement(achievement) != null;
    }
    
    public UnlockedAchievement getUnlockedAchievement(Achievement achievement)
    {
        for (UnlockedAchievement unlockedAchievement : unlockedAchievements)
        {
            if (unlockedAchievement.achievement == achievement) return unlockedAchievement;
        }
        
        return null;
    }
    
    public static Achievement[] getAllAchievements()
    {
        return Achievement.values();
    }
}
