package com.namelessju.scathapro.managers;

import java.time.LocalDate;

import com.namelessju.scathapro.Constants;
import com.namelessju.scathapro.GlobalVariables;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.achievements.AchievementManager;
import com.namelessju.scathapro.achievements.Achievement.Type.Visibility;
import com.namelessju.scathapro.entitydetection.detectedentities.DetectedWorm;
import com.namelessju.scathapro.events.WormSpawnEvent;
import com.namelessju.scathapro.miscellaneous.enums.WormStatsType;
import com.namelessju.scathapro.parsing.ScoreboardParser;
import com.namelessju.scathapro.util.JsonUtil;
import com.namelessju.scathapro.util.NBTUtil;
import com.namelessju.scathapro.util.TimeUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class AchievementLogicManager
{
    // Note: doesn't contain all achievement logic, just a bunch of methods that handle multiple similar achievements at once
    
    private final ScathaPro scathaPro;
    private final GlobalVariables variables;
    
    public AchievementLogicManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
        this.variables = scathaPro.variables;
    }
    
    /**
     * Updates achievement progresses to the saved data without triggering unlocks
     */
    public void updateAchievementsAfterDataLoading()
    {
        updateTotalKillsAchievements(false);
        updateDryStreakAchievements(false);
        updateKillsTodayAchievements(false);
        updatePetDropAchievements(false);
        updateDailyScathaStreakAchievements(false);
        
        if (scathaPro.variables.scathaKillsAtLastDrop >= 0 && scathaPro.variables.scathaKills >= 0 && scathaPro.variables.scathaKills == scathaPro.variables.scathaKillsAtLastDrop)
        {
            Achievement.scatha_pet_drop_b2b.setProgress(1f, false);
        }
        
        // KEEP LAST
        updateProgressAchievements(false);
    }
    
    public void updateKillsAchievements()
    {
        int lobbyWormKills = WormStatsType.PER_LOBBY.regularWormKills + WormStatsType.PER_LOBBY.scathaKills;
        Achievement.lobby_kills_1.setProgress(lobbyWormKills);
        Achievement.lobby_kills_2.setProgress(lobbyWormKills);
        Achievement.lobby_kills_3.setProgress(lobbyWormKills);
        
        updateTotalKillsAchievements(true);
        updateDryStreakAchievements(true);
        updateKillsTodayAchievements(true);
    }
    
    public void updateTotalKillsAchievements(boolean allowUnlock)
    {
        int highestWormKills = Math.max(WormStatsType.PER_SESSION.regularWormKills + WormStatsType.PER_SESSION.scathaKills, variables.regularWormKills + variables.scathaKills);
        Achievement.worm_bestiary_max.setProgress(highestWormKills, allowUnlock);
        Achievement.worm_kills_1.setProgress(highestWormKills, allowUnlock);
        Achievement.worm_kills_2.setProgress(highestWormKills, allowUnlock);
        Achievement.worm_kills_3.setProgress(highestWormKills, allowUnlock);
        Achievement.worm_kills_4.setProgress(highestWormKills, allowUnlock);
        Achievement.worm_kills_5.setProgress(highestWormKills, allowUnlock);
        Achievement.worm_kills_6.setProgress(highestWormKills, allowUnlock);
        
        int highestScathaKills = Math.max(WormStatsType.PER_SESSION.scathaKills, variables.scathaKills);
        Achievement.scatha_kills_1.setProgress(highestScathaKills, allowUnlock);
        Achievement.scatha_kills_2.setProgress(highestScathaKills, allowUnlock);
        Achievement.scatha_kills_3.setProgress(highestScathaKills, allowUnlock);
        Achievement.scatha_kills_4.setProgress(highestScathaKills, allowUnlock);
        Achievement.scatha_kills_5.setProgress(highestScathaKills, allowUnlock);
    }
    
    public void updateKillsTodayAchievements()
    {
        updateKillsTodayAchievements(true);
    }
    public void updateKillsTodayAchievements(boolean allowUnlock)
    {
        int totalWormKillsToday = WormStatsType.PER_DAY.regularWormKills + WormStatsType.PER_DAY.scathaKills;
        Achievement.day_kills_1.setProgress(totalWormKillsToday);
        Achievement.day_kills_2.setProgress(totalWormKillsToday);
        Achievement.day_kills_3.setProgress(totalWormKillsToday);
    }
    
    public void updateDryStreakAchievements()
    {
        updateDryStreakAchievements(true);
    }
    public void updateDryStreakAchievements(boolean allowUnlock)
    {
        int dryStreak = 0;
        if (!scathaPro.variables.dropDryStreakInvalidated)
        {
            if (scathaPro.variables.scathaKillsAtLastDrop < 0) dryStreak = scathaPro.variables.scathaKills;
            else dryStreak = scathaPro.variables.scathaKills - scathaPro.variables.scathaKillsAtLastDrop;
            if (dryStreak < 0) dryStreak = 0;
        }
        
        Achievement.scatha_pet_drop_dry_streak_1.setProgress(dryStreak, allowUnlock);
        Achievement.scatha_pet_drop_dry_streak_2.setProgress(dryStreak, allowUnlock);
    }
    
    public void updateSpawnAchievements(WormSpawnEvent spawnEvent)
    {
        int scathaStreak = Math.max(0, WormStatsType.PER_LOBBY.scathaSpawnStreak);
        Achievement.scatha_streak_1.setProgress(scathaStreak);
        Achievement.scatha_streak_2.setProgress(scathaStreak);
        Achievement.scatha_streak_3.setProgress(scathaStreak);
        Achievement.scatha_streak_4.setProgress(scathaStreak);
        
        int regularWormStreak = Math.max(0, -WormStatsType.PER_LOBBY.scathaSpawnStreak);
        Achievement.regular_worm_streak_1.setProgress(regularWormStreak);
        Achievement.regular_worm_streak_2.setProgress(regularWormStreak);
        Achievement.regular_worm_streak_3.setProgress(regularWormStreak);
        
        if (spawnEvent != null && spawnEvent.worm.isScatha
            && spawnEvent.timeSincePreviousSpawn >= Constants.wormSpawnCooldown - 1000L // 1s grace period for ping differences
            && spawnEvent.timeSincePreviousSpawn < Constants.wormSpawnCooldown + 3000L)
        {
            Achievement.scatha_spawn_time_cooldown_end.unlock();
        }
        
        handleAnomalousDesireRecoverAchievement();
    }
    
    public void handleAnomalousDesireRecoverAchievement()
    {
        if (scathaPro.variables.anomalousDesireWastedForRecovery)
        {
            scathaPro.variables.anomalousDesireWastedForRecovery = false;
            
            if (scathaPro.variables.anomalousDesireStartTime >= 0L && TimeUtil.now() - scathaPro.variables.anomalousDesireStartTime < Constants.anomalousDesireEffectDuration)
            {
                Achievement.anomalous_desire_recover.unlock();
            }
        }
    }
    
    public void updateScathaSpawnAchievements(long now, DetectedWorm worm)
    {
        // Time achievements
        
        if (now - scathaPro.variables.lastWorldJoinTime <= Achievement.scatha_spawn_time.goal * 60 * 1000)
        {
            Achievement.scatha_spawn_time.unlock();
        }
        
        // Height achievements
        
        if (worm.getEntity().posY > 186) Achievement.scatha_spawn_chtop.unlock();
        else if (worm.getEntity().posY < 32.5) Achievement.scatha_spawn_chbottom.unlock();
        
        // Scoreboard achievements
        
        if (!scathaPro.getAchievementManager().isAchievementUnlocked(Achievement.scatha_spawn_heat_burning))
        {
            if (ScoreboardParser.parseHeat() >= 99) Achievement.scatha_spawn_heat_burning.unlock();
        }
        
        // Player dependent achievements
        
        EntityPlayer player = scathaPro.getMinecraft().thePlayer;
        if (player != null)
        {
            ItemStack helmetItem = scathaPro.getMinecraft().thePlayer.getCurrentArmor(3);
            String skyblockItemID = NBTUtil.getSkyblockItemID(helmetItem);
            if (skyblockItemID != null && skyblockItemID.equals("PET"))
            {
                NBTTagCompound skyblockNbt = NBTUtil.getSkyblockTagCompound(helmetItem);
                String petType = JsonUtil.getString(JsonUtil.parseObject(skyblockNbt.getString("petInfo")), "type");
                if (petType != null && petType.equals("SCATHA"))
                {
                    Achievement.scatha_spawn_scatha_helmet.unlock();
                }
            }
        }
    }

    public void updatePetDropAchievements()
    {
        updatePetDropAchievements(true);
    }
    public void updatePetDropAchievements(boolean allowUnlock)
    {
        Achievement.scatha_pet_drop_1_rare.setProgress(variables.rarePetDrops, allowUnlock);
        Achievement.scatha_pet_drop_2_rare.setProgress(variables.rarePetDrops, allowUnlock);
        Achievement.scatha_pet_drop_3_rare.setProgress(variables.rarePetDrops, allowUnlock);
        
        Achievement.scatha_pet_drop_1_epic.setProgress(variables.epicPetDrops, allowUnlock);
        Achievement.scatha_pet_drop_2_epic.setProgress(variables.epicPetDrops, allowUnlock);
        Achievement.scatha_pet_drop_3_epic.setProgress(variables.epicPetDrops, allowUnlock);
        
        Achievement.scatha_pet_drop_1_legendary.setProgress(variables.legendaryPetDrops, allowUnlock);
        Achievement.scatha_pet_drop_2_legendary.setProgress(variables.legendaryPetDrops, allowUnlock);
        Achievement.scatha_pet_drop_3_legendary.setProgress(variables.legendaryPetDrops, allowUnlock);
        
        Achievement.scatha_pet_drop_each.setProgress(
                (variables.rarePetDrops > 0 ? 1 : 0)
                +
                (variables.epicPetDrops > 0 ? 1 : 0)
                +
                (variables.legendaryPetDrops > 0 ? 1 : 0)
                ,
                allowUnlock
        );
        
        int totalPetDrops = variables.rarePetDrops + variables.epicPetDrops + variables.legendaryPetDrops;
        Achievement.scatha_pet_drop_any_1.setProgress(totalPetDrops, allowUnlock);
        Achievement.scatha_pet_drop_any_2.setProgress(totalPetDrops, allowUnlock);
        Achievement.scatha_pet_drop_any_3.setProgress(totalPetDrops, allowUnlock);
        Achievement.scatha_pet_drop_any_4.setProgress(totalPetDrops, allowUnlock);
    }

    public void updateProgressAchievements()
    {
        updateProgressAchievements(true);
    }
    public void updateProgressAchievements(boolean allowUnlock)
    {
        int achievementsCount = 0;
        int unlockedAchievementsCount = 0;
        
        Achievement[] achievements = AchievementManager.getAllAchievements();
        
        for (int i = 0; i < achievements.length; i ++)
        {
            Achievement a = achievements[i];
            if (a.type.visibility != Visibility.HIDDEN)
            {
                achievementsCount ++;
                if (scathaPro.getAchievementManager().isAchievementUnlocked(a)) unlockedAchievementsCount ++;
            }
        }
        
        float unlockedAchievementsPercentage = (float) unlockedAchievementsCount / achievementsCount;
        if (unlockedAchievementsPercentage >= 1f) Achievement.achievements_unlocked_all.setProgress(Achievement.achievements_unlocked_all.goal, allowUnlock);
        else if (unlockedAchievementsPercentage >= 0.5f) Achievement.achievements_unlocked_half.setProgress(Achievement.achievements_unlocked_half.goal, allowUnlock);
    }
    
    public void updateDailyScathaStreakAchievements()
    {
        updateDailyScathaStreakAchievements(true);
    }
    public void updateDailyScathaStreakAchievements(boolean allowUnlock)
    {
        Achievement.scatha_farming_streak_1.setProgress(variables.scathaFarmingStreak, allowUnlock);
        Achievement.scatha_farming_streak_2.setProgress(variables.scathaFarmingStreak, allowUnlock);
        Achievement.scatha_farming_streak_3.setProgress(variables.scathaFarmingStreak, allowUnlock);
        Achievement.scatha_farming_streak_4.setProgress(variables.scathaFarmingStreak, allowUnlock);
        Achievement.scatha_farming_streak_5.setProgress(variables.scathaFarmingStreak, allowUnlock);
        
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
            Achievement.scatha_farming_streak_business_days.setProgress(businessDaysFarmingProgress, allowUnlock);
            
            int lastFarmedDayOfWeekend = lastFarmedDayOfWeek - 5; // 1, 2 = Sat, Sun 
            int weekendFarmingProgress = 0;
            if (lastFarmedDayOfWeekend > 0 && variables.scathaFarmingStreak >= lastFarmedDayOfWeekend)
            {
                weekendFarmingProgress = lastFarmedDayOfWeekend;
            }
            Achievement.scatha_farming_streak_weekend.setProgress(weekendFarmingProgress, allowUnlock);
        }
        else
        {
            Achievement.scatha_farming_streak_business_days.setProgress(0, allowUnlock);
            Achievement.scatha_farming_streak_weekend.setProgress(0, allowUnlock);
        }
    }
}
