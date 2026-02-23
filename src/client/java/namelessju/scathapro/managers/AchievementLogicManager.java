package namelessju.scathapro.managers;

import com.google.gson.JsonObject;
import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.achievements.Achievement;
import namelessju.scathapro.achievements.AchievementType;
import namelessju.scathapro.entitydetection.detectedentity.DetectedWorm;
import namelessju.scathapro.files.PersistentData;
import namelessju.scathapro.parsing.ScoreboardParser;
import namelessju.scathapro.util.JsonUtil;
import namelessju.scathapro.util.SkyblockItemUtil;
import namelessju.scathapro.util.TimeUtil;
import namelessju.scathapro.util.Util;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.time.LocalDate;

public class AchievementLogicManager
{
    // Note: doesn't contain all achievement logic, just a bunch of methods that handle multiple similar achievements at once
    
    private final ScathaPro scathaPro;
    
    public AchievementLogicManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
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
        
        int scathaKills = scathaPro.getProfileData().scathaKills.get();
        int scathaKillsAtLastDrop = scathaPro.getProfileData().scathaKillsAtLastDrop.getOr(-1);
        if (scathaKillsAtLastDrop >= 0 && scathaKills == scathaKillsAtLastDrop)
        {
            Achievement.scatha_pet_drop_b2b.setProgress(1f, false);
        }
        
        // KEEP LAST
        updateProgressAchievements(false);
    }
    
    public void updateKillsAchievements()
    {
        int lobbyWormKills = scathaPro.secondaryWormStatsManager.perLobbyStats.getRegularWormKills()
            + scathaPro.secondaryWormStatsManager.perLobbyStats.getScathaKills();
        Achievement.lobby_kills_1.setProgress(lobbyWormKills);
        Achievement.lobby_kills_2.setProgress(lobbyWormKills);
        Achievement.lobby_kills_3.setProgress(lobbyWormKills);
        
        updateTotalKillsAchievements(true);
        updateDryStreakAchievements(true);
        updateKillsTodayAchievements(true);
    }
    
    public void updateTotalKillsAchievements(boolean allowUnlock)
    {
        int highestWormKills = Math.max(
            scathaPro.secondaryWormStatsManager.perSessionStats.getRegularWormKills()
                + scathaPro.secondaryWormStatsManager.perSessionStats.getScathaKills(),
            scathaPro.getProfileData().regularWormKills.get() + scathaPro.getProfileData().scathaKills.get()
        );
        Achievement.worm_bestiary_max.setProgress(highestWormKills, allowUnlock);
        Achievement.worm_kills_1.setProgress(highestWormKills, allowUnlock);
        Achievement.worm_kills_2.setProgress(highestWormKills, allowUnlock);
        Achievement.worm_kills_3.setProgress(highestWormKills, allowUnlock);
        Achievement.worm_kills_4.setProgress(highestWormKills, allowUnlock);
        Achievement.worm_kills_5.setProgress(highestWormKills, allowUnlock);
        Achievement.worm_kills_6.setProgress(highestWormKills, allowUnlock);
        Achievement.worm_kills_7.setProgress(highestWormKills, allowUnlock);
        
        int highestScathaKills = Math.max(
            scathaPro.secondaryWormStatsManager.perSessionStats.getScathaKills(),
            scathaPro.getProfileData().scathaKills.get()
        );
        Achievement.scatha_kills_1.setProgress(highestScathaKills, allowUnlock);
        Achievement.scatha_kills_2.setProgress(highestScathaKills, allowUnlock);
        Achievement.scatha_kills_3.setProgress(highestScathaKills, allowUnlock);
        Achievement.scatha_kills_4.setProgress(highestScathaKills, allowUnlock);
        Achievement.scatha_kills_5.setProgress(highestScathaKills, allowUnlock);
        Achievement.scatha_kills_repeatable.setRepeatingProgress(Achievement.scatha_kills_5.goal, highestScathaKills, allowUnlock);
    }
    
    public void updateKillsTodayAchievements()
    {
        updateKillsTodayAchievements(true);
    }
    public void updateKillsTodayAchievements(boolean allowUnlock)
    {
        int totalWormKillsToday = scathaPro.secondaryWormStatsManager.perDayStats.getRegularWormKills()
            + scathaPro.secondaryWormStatsManager.perDayStats.getScathaKills();
        Achievement.day_kills_1.setProgress(totalWormKillsToday, allowUnlock);
        Achievement.day_kills_2.setProgress(totalWormKillsToday, allowUnlock);
        Achievement.day_kills_3.setProgress(totalWormKillsToday, allowUnlock);
    }
    
    public void updateDryStreakAchievements()
    {
        updateDryStreakAchievements(true);
    }
    public void updateDryStreakAchievements(boolean allowUnlock)
    {
        PersistentData.ProfileData profileData = scathaPro.getProfileData();
        
        int dryStreak = 0;
        if (!profileData.isPetDropDryStreakInvalidated.get())
        {
            int scathaKillsAtLastDrop = profileData.scathaKillsAtLastDrop.getOr(-1);
            if (scathaKillsAtLastDrop < 0) dryStreak = profileData.scathaKills.get();
            else dryStreak = profileData.scathaKills.get() - scathaKillsAtLastDrop;
            if (dryStreak < 0) dryStreak = 0;
        }
        
        Achievement.scatha_pet_drop_dry_streak_1.setProgress(dryStreak, allowUnlock);
        Achievement.scatha_pet_drop_dry_streak_2.setProgress(dryStreak, allowUnlock);
    }
    
    public void updateSpawnAchievements(DetectedWorm spawnedWorm)
    {
        int spawnStreak = scathaPro.secondaryWormStatsManager.perLobbyStats.getScathaSpawnStreak();
        
        int scathaStreak = Math.max(0, spawnStreak);
        Achievement.scatha_streak_1.setProgress(scathaStreak);
        Achievement.scatha_streak_2.setProgress(scathaStreak);
        Achievement.scatha_streak_3.setProgress(scathaStreak);
        Achievement.scatha_streak_4.setProgress(scathaStreak);
        
        int regularWormStreak = Math.max(0, -spawnStreak);
        Achievement.regular_worm_streak_1.setProgress(regularWormStreak);
        Achievement.regular_worm_streak_2.setProgress(regularWormStreak);
        Achievement.regular_worm_streak_3.setProgress(regularWormStreak);
        
        handleTunnelVisionRecoverAchievement();
    }
    
    public void handleTunnelVisionRecoverAchievement()
    {
        if (!scathaPro.coreManager.tunnelVisionWastedForRecovery) return;
        scathaPro.coreManager.tunnelVisionWastedForRecovery = false;
        
        if (scathaPro.coreManager.tunnelVisionStartTime >= 0L
            && TimeUtil.now() - scathaPro.coreManager.tunnelVisionStartTime < Constants.tunnelVisionEffectDuration)
        {
            Achievement.anomalous_desire_recover.unlock();
        }
    }
    
    public void updateScathaSpawnAchievements(DetectedWorm worm)
    {
        // Time achievements
        
        long now = TimeUtil.now();
        
        if (scathaPro.coreManager.lastWorldJoinTime >= 0
            && now - scathaPro.coreManager.lastWorldJoinTime <= Achievement.scatha_spawn_time.goal * 60 * 1000)
        {
            Achievement.scatha_spawn_time.unlock();
        }
        
        if (scathaPro.coreManager.lastWormSpawnTime >= 0L)
        {
            long timeSincePreviousSpawn = TimeUtil.now() - scathaPro.coreManager.lastWormSpawnTime;
            if (timeSincePreviousSpawn >= Constants.wormSpawnCooldown - 1000L // 1s grace period for ping differences
                && timeSincePreviousSpawn < Constants.wormSpawnCooldown + 3000L)
            {
                Achievement.scatha_spawn_time_cooldown_end.unlock();
            }
        }
        
        // Height achievements
        
        if (worm.entity.getY() > 186D) Achievement.scatha_spawn_chtop.unlock();
        else if (worm.entity.getY() < 32.5D) Achievement.scatha_spawn_chbottom.unlock();
        
        // Scoreboard achievements
        
        if (!scathaPro.getProfileData().unlockedAchievements.isUnlocked(Achievement.scatha_spawn_heat_burning))
        {
            if (ScoreboardParser.parseHeat(scathaPro.minecraft).orElse(-1) >= 99)
            {
                Achievement.scatha_spawn_heat_burning.unlock();
            }
        }
        
        // Player dependent achievements
        
        LocalPlayer player = scathaPro.minecraft.player;
        if (player != null)
        {
            ItemStack helmetItem = player.getItemBySlot(EquipmentSlot.HEAD);
            SkyblockItemUtil.getData(helmetItem, data -> {
                if (!Util.optionalValueEquals(data.getString(SkyblockItemUtil.KEY_ID), "PET")) return;
                data.getString(SkyblockItemUtil.KEY_PETINFO).ifPresent(petInfo -> {
                    if (!(JsonUtil.parseJson(petInfo) instanceof JsonObject petInfoParsed)) return;
                    
                    String petType = JsonUtil.getString(petInfoParsed, "type");
                    if (petType == null || !petType.equals("SCATHA")) return;
                    
                    Achievement.scatha_spawn_scatha_helmet.unlock();
                });
            });
        }
    }

    public void updatePetDropAchievements()
    {
        updatePetDropAchievements(true);
    }
    public void updatePetDropAchievements(boolean allowUnlock)
    {
        int rarePetDrops = scathaPro.getProfileData().rarePetDrops.get();
        int epicPetDrops = scathaPro.getProfileData().epicPetDrops.get();
        int legendaryPetDrops = scathaPro.getProfileData().legendaryPetDrops.get();
        
        Achievement.scatha_pet_drop_1_rare.setProgress(rarePetDrops, allowUnlock);
        Achievement.scatha_pet_drop_2_rare.setProgress(rarePetDrops, allowUnlock);
        Achievement.scatha_pet_drop_3_rare.setProgress(rarePetDrops, allowUnlock);
        
        Achievement.scatha_pet_drop_1_epic.setProgress(epicPetDrops, allowUnlock);
        Achievement.scatha_pet_drop_2_epic.setProgress(epicPetDrops, allowUnlock);
        Achievement.scatha_pet_drop_3_epic.setProgress(epicPetDrops, allowUnlock);
        
        Achievement.scatha_pet_drop_1_legendary.setProgress(legendaryPetDrops, allowUnlock);
        Achievement.scatha_pet_drop_2_legendary.setProgress(legendaryPetDrops, allowUnlock);
        Achievement.scatha_pet_drop_3_legendary.setProgress(legendaryPetDrops, allowUnlock);
        
        Achievement.scatha_pet_drop_each.setProgress(
                (rarePetDrops > 0 ? 1 : 0)
                +
                (epicPetDrops > 0 ? 1 : 0)
                +
                (legendaryPetDrops > 0 ? 1 : 0)
                ,
                allowUnlock
        );
        
        int totalPetDrops = rarePetDrops + epicPetDrops + legendaryPetDrops;
        Achievement.scatha_pet_drop_any_1.setProgress(totalPetDrops, allowUnlock);
        Achievement.scatha_pet_drop_any_2.setProgress(totalPetDrops, allowUnlock);
        Achievement.scatha_pet_drop_any_3.setProgress(totalPetDrops, allowUnlock);
        Achievement.scatha_pet_drop_any_4.setProgress(totalPetDrops, allowUnlock);
        Achievement.scatha_pet_drop_any_repeatable.setRepeatingProgress(Achievement.scatha_pet_drop_any_4.goal, totalPetDrops, allowUnlock);
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
        
        for (Achievement achievement : achievements)
        {
            if (achievement.type.lockedVisibility != AchievementType.LockedVisibility.HIDDEN)
            {
                achievementsCount++;
                if (scathaPro.getProfileData().unlockedAchievements.isUnlocked(achievement)) unlockedAchievementsCount++;
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
        int scathaFarmingStreak = scathaPro.getProfileData().scathaFarmingStreak.get();
        Achievement.scatha_farming_streak_1.setProgress(scathaFarmingStreak, allowUnlock);
        Achievement.scatha_farming_streak_2.setProgress(scathaFarmingStreak, allowUnlock);
        Achievement.scatha_farming_streak_3.setProgress(scathaFarmingStreak, allowUnlock);
        Achievement.scatha_farming_streak_4.setProgress(scathaFarmingStreak, allowUnlock);
        Achievement.scatha_farming_streak_5.setProgress(scathaFarmingStreak, allowUnlock);
        
        LocalDate lastScathaFarmedDate = scathaPro.getProfileData().lastScathaFarmedDate.get();
        if (lastScathaFarmedDate != null)
        {
            LocalDate today = TimeUtil.today();
            int lastFarmedDayOfWeek = today.getDayOfWeek().getValue(); // 1-7 = Mon-Sun
            if (lastScathaFarmedDate.equals(today.minusDays(1))) lastFarmedDayOfWeek -= 1; // last farmed yesterday
            else if (!lastScathaFarmedDate.equals(today)) lastFarmedDayOfWeek = -1; // not farmed today or yesterday => streak broken
            
            int businessDaysFarmingProgress = 0;
            if (lastFarmedDayOfWeek > 0 && lastFarmedDayOfWeek <= 5 && scathaFarmingStreak >= lastFarmedDayOfWeek)
            {
                businessDaysFarmingProgress = lastFarmedDayOfWeek;
            }
            Achievement.scatha_farming_streak_business_days.setProgress(businessDaysFarmingProgress, allowUnlock);
            
            int lastFarmedDayOfWeekend = lastFarmedDayOfWeek - 5; // 1, 2 = Sat, Sun 
            int weekendFarmingProgress = 0;
            if (lastFarmedDayOfWeekend > 0 && scathaFarmingStreak >= lastFarmedDayOfWeekend)
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
