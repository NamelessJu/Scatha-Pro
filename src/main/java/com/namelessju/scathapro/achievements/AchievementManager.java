package com.namelessju.scathapro.achievements;

import java.util.HashMap;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.events.AchievementUnlockedEvent;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.util.TimeUtil;

import net.minecraftforge.common.MinecraftForge;

public class AchievementManager
{
    private final ScathaPro scathaPro;
    
    private HashMap<String, UnlockedAchievement> unlockedAchievements = new HashMap<String, UnlockedAchievement>();
    
    public AchievementManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
        
        updateBonusTypeVisibility();
    }
    
    public void unlockAchievement(Achievement achievement)
    {
        if (isAchievementUnlocked(achievement)) return;
        
        unlockedAchievements.put(achievement.getID(), new UnlockedAchievement(achievement, TimeUtil.now()));
        scathaPro.getPersistentData().saveAchievements();
        
        MinecraftForge.EVENT_BUS.post(new AchievementUnlockedEvent(achievement));
    }
    
    public boolean revokeAchievement(Achievement achievement)
    {
        if (unlockedAchievements.remove(achievement.getID()) != null)
        {
            achievement.setProgress(0f);
            scathaPro.getPersistentData().saveAchievements();
            return true;
        }
        return false;
    }
    
    public boolean isAchievementUnlocked(Achievement achievement)
    {
        return getUnlockedAchievement(achievement) != null;
    }
    
    public UnlockedAchievement getUnlockedAchievement(Achievement achievement)
    {
        return unlockedAchievements.get(achievement.getID());
    }
    
    public void clearUnlockedAchievements()
    {
        unlockedAchievements.clear();
    }
    
    public void addUnlockedAchievement(UnlockedAchievement unlockedAchievement)
    {
        unlockedAchievements.put(unlockedAchievement.achievement.getID(), unlockedAchievement);
    }
    
    public UnlockedAchievement[] getAllUnlockedAchievements()
    {
        String[] ids = unlockedAchievements.keySet().toArray(new String[0]);
        UnlockedAchievement[] achievements = new UnlockedAchievement[ids.length];
        for (int i = 0; i < ids.length; i ++)
        {
            achievements[i] = unlockedAchievements.get(ids[i]);
        }
        return achievements;
    }
    
    public static Achievement[] getAllAchievements()
    {
        return Achievement.values();
    }
    
    public void updateBonusTypeVisibility()
    {
        boolean visible = scathaPro.getConfig().getBoolean(Config.Key.bonusAchievementsShown);
        Achievement.Type.BONUS.visibility = visible ? Achievement.Type.Visibility.VISIBLE : Achievement.Type.Visibility.HIDDEN;
    }
}
