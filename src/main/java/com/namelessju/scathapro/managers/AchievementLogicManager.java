package com.namelessju.scathapro.managers;

import java.time.LocalDate;
import java.util.Collection;

import com.namelessju.scathapro.GlobalVariables;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.achievements.AchievementManager;
import com.namelessju.scathapro.entitydetection.detectedentities.DetectedWorm;
import com.namelessju.scathapro.events.WormSpawnEvent;
import com.namelessju.scathapro.miscellaneous.WormStats;
import com.namelessju.scathapro.util.JsonUtil;
import com.namelessju.scathapro.util.NBTUtil;
import com.namelessju.scathapro.util.TimeUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.StringUtils;

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
        int lobbyWormKills = WormStats.PER_LOBBY.regularWormKills + WormStats.PER_LOBBY.scathaKills;
        Achievement.lobby_kills_1.setProgress(lobbyWormKills);
        Achievement.lobby_kills_2.setProgress(lobbyWormKills);
        Achievement.lobby_kills_3.setProgress(lobbyWormKills);
        
        int highestWormKills = Math.max(WormStats.PER_SESSION.regularWormKills + WormStats.PER_SESSION.scathaKills, variables.regularWormKills + variables.scathaKills);
        Achievement.worm_bestiary_max.setProgress(highestWormKills);
        Achievement.worm_kills_1.setProgress(highestWormKills);
        Achievement.worm_kills_2.setProgress(highestWormKills);
        Achievement.worm_kills_3.setProgress(highestWormKills);
        Achievement.worm_kills_4.setProgress(highestWormKills);
        Achievement.worm_kills_5.setProgress(highestWormKills);
        Achievement.worm_kills_6.setProgress(highestWormKills);
        
        int highestScathaKills = Math.max(WormStats.PER_SESSION.scathaKills, variables.scathaKills);
        Achievement.scatha_kills_1.setProgress(highestScathaKills);
        Achievement.scatha_kills_2.setProgress(highestScathaKills);
        Achievement.scatha_kills_3.setProgress(highestScathaKills);
        Achievement.scatha_kills_4.setProgress(highestScathaKills);
        Achievement.scatha_kills_5.setProgress(highestScathaKills);
        
        int dryStreak = scathaPro.variables.scathaKills - scathaPro.variables.scathaKillsAtLastDrop;
        if (dryStreak < 0) dryStreak = 0;
        Achievement.scatha_pet_drop_dry_streak_1.setProgress(dryStreak);
        Achievement.scatha_pet_drop_dry_streak_2.setProgress(dryStreak);
        
        updateKillsTodayAchievements();
    }
    
    public void updateSpawnAchievements(WormSpawnEvent spawnEvent)
    {
        int scathaStreak = Math.max(0, WormStats.PER_LOBBY.scathaSpawnStreak);
        Achievement.scatha_streak_1.setProgress(scathaStreak);
        Achievement.scatha_streak_2.setProgress(scathaStreak);
        Achievement.scatha_streak_3.setProgress(scathaStreak);
        Achievement.scatha_streak_4.setProgress(scathaStreak);
        
        int regularWormStreak = Math.max(0, -WormStats.PER_LOBBY.scathaSpawnStreak);
        Achievement.regular_worm_streak_1.setProgress(regularWormStreak);
        Achievement.regular_worm_streak_2.setProgress(regularWormStreak);
        Achievement.regular_worm_streak_3.setProgress(regularWormStreak);
        
        if (spawnEvent != null && spawnEvent.worm.isScatha
            && spawnEvent.timeSincePreviousSpawn >= 30000L && spawnEvent.timeSincePreviousSpawn < 33000L)
        {
            Achievement.scatha_spawn_time_cooldown_end.unlock();
        }
    }
    
    public void handleScathaSpawnAchievements(long now, DetectedWorm worm)
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
            scathaPro.logDebug("Checking for scoreboard heat value...");
            
            Scoreboard scoreboard = scathaPro.getMinecraft().theWorld.getScoreboard();
            ScoreObjective sidebarObjective = scoreboard.getObjectiveInDisplaySlot(1);
            if (sidebarObjective != null)
            {
                scathaPro.logDebug("Scoreboard objective found in sidebar: \"" + sidebarObjective.getDisplayName() + "\"");
                
                Collection<Score> scores = scoreboard.getSortedScores(sidebarObjective);
                for (Score score : scores)
                {
                    String playerName = score.getPlayerName();
                    ScorePlayerTeam playerTeam = scoreboard.getPlayersTeam(playerName);
                    String formattedScoreText = ScorePlayerTeam.formatPlayerName(playerTeam, playerName);
                    String unformattedText = StringUtils.stripControlCodes(formattedScoreText.replace(playerName, ""));
                    
                    scathaPro.logDebug("Scoreboard line: \"" + unformattedText + "\"");
                    
                    if (unformattedText.startsWith("Heat:"))
                    {
                        String valueString = unformattedText.substring(5).trim();
                        
                        // remove non-digit characters from left
                        while (valueString.length() > 0)
                        {
                            char firstChar = valueString.charAt(0);
                            if (firstChar >= '0' && firstChar <= '9') break;
                            if (valueString.startsWith("IMMUNE"))
                            {
                                valueString = null;
                                break;
                            }
                            valueString = valueString.substring(1).trim();
                        }
                        
                        // remove non-digit characters from right
                        while (valueString != null && valueString.length() > 0)
                        {
                            char lastChar = valueString.charAt(valueString.length() - 1);
                            if (lastChar >= '0' && lastChar <= '9') break;
                            valueString = valueString.substring(0, valueString.length() - 1).trim();
                        }
                        
                        if (valueString != null && !valueString.isEmpty())
                        {
                            int heat = -1;
                            try
                            {
                                heat = Integer.parseInt(valueString);
                                scathaPro.logDebug("Scoreboard heat entry found - value: " + heat);
                            }
                            catch (NumberFormatException exception)
                            {
                                scathaPro.logError("Error while parsing scoreboard heat value: \"" + unformattedText + "\" couldn't be parsed to an int");
                            }
                            
                            if (heat >= 90) Achievement.scatha_spawn_heat_burning.unlock();
                        }
                        else
                        {
                            scathaPro.logDebug("Scoreboard heat entry found, but has no int value: \"" + unformattedText + "\"");
                        }
                        
                        break;
                    }
                }
            }
            else scathaPro.logDebug("No scoreboard objective in sidebar found");
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
    
    public void updateKillsTodayAchievements()
    {
        int totalWormKillsToday = WormStats.PER_DAY.regularWormKills + WormStats.PER_DAY.scathaKills;
        Achievement.day_kills_1.setProgress(totalWormKillsToday);
        Achievement.day_kills_2.setProgress(totalWormKillsToday);
        Achievement.day_kills_3.setProgress(totalWormKillsToday);
    }
}
