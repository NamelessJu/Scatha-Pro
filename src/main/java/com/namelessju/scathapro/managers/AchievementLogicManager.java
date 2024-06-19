package com.namelessju.scathapro.managers;

import java.time.LocalDate;

import com.namelessju.scathapro.GlobalVariables;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.achievements.AchievementManager;
import com.namelessju.scathapro.miscellaneous.OverlayStats;
import com.namelessju.scathapro.util.TimeUtil;

public class AchievementLogicManager
{
    private final ScathaPro scathaPro;
    private final GlobalVariables variables;
    
    public AchievementLogicManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
        this.variables = scathaPro.variables;
    }
    
    public void updateKillAchievements()
    {
        int lobbyWormKills = OverlayStats.PER_LOBBY.regularWormKills + OverlayStats.PER_LOBBY.scathaKills;
        Achievement.lobby_kills_1.setProgress(lobbyWormKills);
        Achievement.lobby_kills_2.setProgress(lobbyWormKills);
        Achievement.lobby_kills_3.setProgress(lobbyWormKills);
        
        int highestWormKills = Math.max(lobbyWormKills, variables.regularWormKills + variables.scathaKills);
        Achievement.worm_bestiary_max.setProgress(highestWormKills);
        Achievement.worm_kills_1.setProgress(highestWormKills);
        Achievement.worm_kills_2.setProgress(highestWormKills);
        Achievement.worm_kills_3.setProgress(highestWormKills);
        Achievement.worm_kills_4.setProgress(highestWormKills);
        Achievement.worm_kills_5.setProgress(highestWormKills);
        Achievement.worm_kills_6.setProgress(highestWormKills);
        
        int highestScathaKills = Math.max(OverlayStats.PER_LOBBY.regularWormKills, variables.scathaKills);
        Achievement.scatha_kills_1.setProgress(highestScathaKills);
        Achievement.scatha_kills_2.setProgress(highestScathaKills);
        Achievement.scatha_kills_3.setProgress(highestScathaKills);
        Achievement.scatha_kills_4.setProgress(highestScathaKills);
        Achievement.scatha_kills_5.setProgress(highestScathaKills);
    }
    
    public void updateSpawnAchievements()
    {
        int scathaStreak = Math.max(0, OverlayStats.PER_LOBBY.scathaSpawnStreak);
        Achievement.scatha_streak_1.setProgress(scathaStreak);
        Achievement.scatha_streak_2.setProgress(scathaStreak);
        Achievement.scatha_streak_3.setProgress(scathaStreak);
        Achievement.scatha_streak_4.setProgress(scathaStreak);
        
        int regularWormStreak = Math.max(0, -OverlayStats.PER_LOBBY.scathaSpawnStreak);
        Achievement.regular_worm_streak_1.setProgress(regularWormStreak);
        Achievement.regular_worm_streak_2.setProgress(regularWormStreak);
        Achievement.regular_worm_streak_3.setProgress(regularWormStreak);
    }
    
    public void updatePetDropAchievements()
    {
        Achievement.scatha_pet_drop_1_rare.setProgress(variables.rarePetDrops);
        Achievement.scatha_pet_drop_2_rare.setProgress(variables.rarePetDrops);
        Achievement.scatha_pet_drop_3_rare.setProgress(variables.rarePetDrops);
        
        Achievement.scatha_pet_drop_1_epic.setProgress(variables.epicPetDrops);
        Achievement.scatha_pet_drop_2_epic.setProgress(variables.epicPetDrops);
        Achievement.scatha_pet_drop_3_epic.setProgress(variables.epicPetDrops);
        
        Achievement.scatha_pet_drop_1_legendary.setProgress(variables.legendaryPetDrops);
        Achievement.scatha_pet_drop_2_legendary.setProgress(variables.legendaryPetDrops);
        Achievement.scatha_pet_drop_3_legendary.setProgress(variables.legendaryPetDrops);
        
        Achievement.scatha_pet_drop_each.setProgress(
                (variables.rarePetDrops > 0 ? 1 : 0)
                +
                (variables.epicPetDrops > 0 ? 1 : 0)
                +
                (variables.legendaryPetDrops > 0 ? 1 : 0)
        );
        
        int totalPetDrops = variables.rarePetDrops + variables.epicPetDrops + variables.legendaryPetDrops;
        Achievement.scatha_pet_drop_any_1.setProgress(totalPetDrops);
        Achievement.scatha_pet_drop_any_2.setProgress(totalPetDrops);
        Achievement.scatha_pet_drop_any_3.setProgress(totalPetDrops);
        Achievement.scatha_pet_drop_any_4.setProgress(totalPetDrops);
    }
    
    public void updateProgressAchievements()
    {
        int achievementsCount = 0;
        int unlockedAchievementsCount = 0;
        
        Achievement[] achievements = AchievementManager.getAllAchievements();
        
        for (int i = 0; i < achievements.length; i ++)
        {
            Achievement a = achievements[i];
            if (a.type.visibility == Achievement.Type.Visibility.VISIBLE)
            {
                achievementsCount ++;
                if (scathaPro.getAchievementManager().isAchievementUnlocked(a)) unlockedAchievementsCount ++;
            }
        }
        
        float unlockedAchievementsPercentage = (float) unlockedAchievementsCount / achievementsCount;
        if (unlockedAchievementsPercentage >= 1f) Achievement.achievements_unlocked_all.setProgress(Achievement.achievements_unlocked_all.goal);
        else if (unlockedAchievementsPercentage >= 0.5f) Achievement.achievements_unlocked_half.setProgress(Achievement.achievements_unlocked_half.goal);
    }
    
    public void updateDailyScathaStreakAchievements()
    {
        Achievement.scatha_farming_streak_1.setProgress(variables.scathaFarmingStreak);
        Achievement.scatha_farming_streak_2.setProgress(variables.scathaFarmingStreak);
        Achievement.scatha_farming_streak_3.setProgress(variables.scathaFarmingStreak);
        Achievement.scatha_farming_streak_4.setProgress(variables.scathaFarmingStreak);
        Achievement.scatha_farming_streak_5.setProgress(variables.scathaFarmingStreak);
        
        
        if (variables.lastScathaFarmedDate != null)
        {
            LocalDate today = TimeUtil.today();
            int lastFarmedDayOfWeek = today.getDayOfWeek().getValue(); // 1-7 = Mon-Sun
            if (variables.lastScathaFarmedDate.equals(today.minusDays(1))) lastFarmedDayOfWeek -= 1; // last farmed yesterday
            else if (!variables.lastScathaFarmedDate.equals(today)) lastFarmedDayOfWeek = -1; // not farmed today or yesterday => streak broken
            
            int businessDaysFarmingProgress = 0;
            if (lastFarmedDayOfWeek > 0 && lastFarmedDayOfWeek <= 5 && variables.scathaFarmingStreak >= lastFarmedDayOfWeek)
            {
                businessDaysFarmingProgress = lastFarmedDayOfWeek;
            }
            Achievement.scatha_farming_streak_business_days.setProgress(businessDaysFarmingProgress);
            
            int lastFarmedDayOfWeekend = lastFarmedDayOfWeek - 5; // 1, 2 = Sat, Sun 
            int weekendFarmingProgress = 0;
            if (lastFarmedDayOfWeekend > 0 && variables.scathaFarmingStreak >= lastFarmedDayOfWeekend)
            {
                weekendFarmingProgress = lastFarmedDayOfWeekend;
            }
            Achievement.scatha_farming_streak_weekend.setProgress(weekendFarmingProgress);
        }
        else
        {
            Achievement.scatha_farming_streak_business_days.setProgress(0);
            Achievement.scatha_farming_streak_weekend.setProgress(0);
        }
    }
}
