package namelessju.scathapro.achievements;

import java.util.HashMap;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.events.AchievementUnlockedEvent;
import namelessju.scathapro.managers.Config;
import namelessju.scathapro.util.TimeUtil;
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

    public UnlockedAchievement unlockAchievement(Achievement achievement)
    {
        return unlockAchievement(achievement, -1);
    }
    
    public UnlockedAchievement unlockAchievement(Achievement achievement, int goalReachedCount)
    {
    	UnlockedAchievement unlockedAchievement = getUnlockedAchievement(achievement);
    	
        if (goalReachedCount == 0) return unlockedAchievement;
        
        if (unlockedAchievement == null)
    	{
        	unlockedAchievement = new UnlockedAchievement(achievement, TimeUtil.now());
            unlockedAchievements.put(achievement.getID(), unlockedAchievement);
    	}
        else
        {
        	if (!achievement.isRepeatable) return unlockedAchievement;
        	int repeatCount = unlockedAchievement.getRepeatCount();
        	if (goalReachedCount > 0)
        	{
        	    int newRepeatCount = goalReachedCount - 1;
        	    if (newRepeatCount <= repeatCount) return unlockedAchievement;
        	    unlockedAchievement.setRepeatCount(newRepeatCount);
        	}
        	else unlockedAchievement.setRepeatCount(repeatCount + 1);
        }
        
        scathaPro.getPersistentData().saveAchievements();
        MinecraftForge.EVENT_BUS.post(new AchievementUnlockedEvent(unlockedAchievement));
        
        return unlockedAchievement;
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
        Achievement.Type.BONUS.visibilityOverride = scathaPro.getConfig().getBoolean(Config.Key.bonusAchievementsShown);
    }
}
