package namelessju.scathapro.managers;

import java.io.File;
import java.time.LocalDate;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.achievements.Achievement;
import namelessju.scathapro.achievements.AchievementManager;
import namelessju.scathapro.achievements.UnlockedAchievement;
import namelessju.scathapro.events.DailyScathaFarmingStreakChangedEvent;
import namelessju.scathapro.events.DailyStatsResetEvent;
import namelessju.scathapro.miscellaneous.enums.WormStatsType;
import namelessju.scathapro.util.FileUtil;
import namelessju.scathapro.util.JsonUtil;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.TimeUtil;
import namelessju.scathapro.util.Util;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;

public class PersistentData
{
    public static final File file = SaveManager.getSaveFile("persistentData.json");
    
    private static final String unlockedAchievementsKey = "unlockedAchievements";
    private static final String petDropsKey = "petDrops";
    private static final String wormKillsKey = "wormKills";
    private static final String dayKey = "daily";
    private static final String profileStatsKey = "profileStats";
    
    
    private final ScathaPro scathaPro;
    
    private JsonObject data = new JsonObject();
    private UUID loadedPlayerUuid = null;
    private String loadedPlayerUuidString = null;
    
    public PersistentData(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
    }
    
    public void loadFile()
    {
        if (!file.exists()) return;
        
        if (!file.canRead())
        {
            scathaPro.logError("Couldn't load persistent data (file can't be read by this application)");
            onLoadError();
            return;
        }
        
        String jsonString = FileUtil.readFile(file);
        if (jsonString == null)
        {
            scathaPro.logError("Couldn't load persistent data (failed to read file)");
            onLoadError();
            return;
        }
        
        JsonElement dataJson;
        
        try
        {
            dataJson = new JsonParser().parse(jsonString);
        }
        catch (Exception E)
        {
            scathaPro.logError("Couldn't load persistent data (invalid JSON format)");
            onLoadError();
            return;
        }
            
        if (dataJson.isJsonObject())
        {
            data = dataJson.getAsJsonObject();
            
            loadCurrentPlayer();
            
            if (updatePlayerSaveData()) loadCurrentPlayer();
            
            scathaPro.log("Persistent data loaded");
        }
        else
        {
            scathaPro.logError("Couldn't load persistent data (JSON root is not an object)");
            onLoadError();
        }
    }
    
    public void loadCurrentPlayer()
    {
        loadedPlayerUuid = Util.getPlayerUUID();
        loadedPlayerUuidString = Util.getUUIDString(loadedPlayerUuid);

        scathaPro.variables.cheaterDetected = false;
        
        loadPetDrops();
        loadWormKills();
        loadDayData();
        loadAchievements();
        loadProfileStats();
        loadMiscData();
        loadGlobalData();
        
        scathaPro.getAchievementLogicManager().updateAchievementsAfterDataLoading();
        
        if (loadedPlayerUuid == null)
        {
        	TextUtil.sendModErrorMessage("Player data couldn't be loaded, your session is offline!");
        }
    }
    
    /**
     * Checks if the loaded player matches the current client's logged in player and loads the correct data if necessary 
     */
    public void updateLoadedPlayer()
    {
        if (!Util.getPlayerUUID().equals(loadedPlayerUuid))
        {
            loadCurrentPlayer();
        }
    }
    
    /**
     * @return Whether player data should be reloaded
     */
    public boolean updatePlayerSaveData()
    {
        JsonObject playerData = getCurrentPlayerObject();
        if (playerData == null) return false;
        
        boolean reloadRequired = false;
        boolean saveRequired = false;
        
        // Update old save files that weren't per-account yet
        
        JsonElement unlockedAchievementsOld = data.get("unlockedAchievements");
        if (unlockedAchievementsOld != null && JsonUtil.getJsonElement(playerData, "unlockedAchievements") == null)
        {
            setInCurrentPlayer("unlockedAchievements", unlockedAchievementsOld);
            data.remove("unlockedAchievements");
            saveRequired = true;
            reloadRequired = true;
        }
        JsonElement petDropsOld = data.get("petDrops");
        if (petDropsOld != null && JsonUtil.getJsonElement(playerData, "petDrops") == null)
        {
            setInCurrentPlayer("petDrops", petDropsOld);
            data.remove("petDrops");
            saveRequired = true;
            reloadRequired = true;
        }
        
        // Update / remove old keys
        
        if (playerData.get("scappaModeV2Unlocked") != null)
        {
            Boolean scappaModeV2Unlocked = JsonUtil.getBoolean(playerData, "scappaModeV2Unlocked");
            if (scappaModeV2Unlocked != null && scappaModeV2Unlocked == true && JsonUtil.getJsonElement(playerData, "misc/unlockables/scappaModeUnlocked") == null)
            {
                JsonUtil.set(playerData, "misc/unlockables/scappaModeUnlocked", new JsonPrimitive(true));
                reloadRequired = true;
            }
            
            playerData.remove("scappaModeV2Unlocked");
            saveRequired = true;
        }
        
        if (playerData.get("scappaModeUnlocked") != null)
        {
            playerData.remove("scappaModeUnlocked");
            saveRequired = true;
        }
        
        if (saveRequired)
        {
            saveData();
            scathaPro.log("Persistent player data updated");
        }
        
        return reloadRequired;
    }
    
    public void saveData()
    {
        if (!FileUtil.writeFile(file, data.toString()))
        {
            scathaPro.logError("Error while trying to save persistent data");
        }
    }
    
    public JsonObject getCurrentPlayerObject()
    {
        JsonElement playerElement = data.get(loadedPlayerUuidString);
        
        if (playerElement != null && playerElement.isJsonObject())
        {
            return playerElement.getAsJsonObject();
        }
        
        return null;
    }
    
    public JsonObject getData()
    {
        return data;
    }

    public boolean setInCurrentPlayer(String path, JsonElement value)
    {
        if (loadedPlayerUuidString == null) return false;
        
        JsonElement playerData = data.get(loadedPlayerUuidString);
        if (playerData == null || !playerData.isJsonObject())
        {
            playerData = new JsonObject();
            data.add(loadedPlayerUuidString, playerData);
        }
        
        JsonUtil.set(playerData.getAsJsonObject(), path, value);
        return true;
    }
    
    
    private void loadAchievements()
    {
        AchievementManager achievementManager = scathaPro.getAchievementManager();
        achievementManager.clearUnlockedAchievements();
        
        JsonObject playerData = getCurrentPlayerObject();
        if (playerData == null) return;
        
        JsonArray achievementsJsonArray = JsonUtil.getJsonArray(playerData, unlockedAchievementsKey);
        if (achievementsJsonArray != null)
        {
            long now = TimeUtil.now();
            
            for (JsonElement achievementJsonElement : achievementsJsonArray)
            {
                String achievementID = JsonUtil.getString(achievementJsonElement, "achievementID");
                Long unlockedAtTimestamp = JsonUtil.getLong(achievementJsonElement, "unlockedAt");
                
                if (achievementID != null && unlockedAtTimestamp != null)
                {
                    Achievement achievement = Achievement.getByID(achievementID);
                    
                    if (achievement != null && !achievementManager.isAchievementUnlocked(achievement))
                    {
                        if (unlockedAtTimestamp > now || unlockedAtTimestamp < 1640991600000L)
                        {
                            scathaPro.variables.cheaterDetected = true;
                        }
                        
                        UnlockedAchievement unlockedAchievement = new UnlockedAchievement(achievement, unlockedAtTimestamp);
                		
                        if (achievement.isRepeatable)
                    	{
                        	Integer repeatCount = JsonUtil.getInt(achievementJsonElement, "repeatCount");
                        	if (repeatCount != null)
                        	{
	                        	if (repeatCount > 0) unlockedAchievement.setRepeatCount(repeatCount);
	                        	else
                        	    {
	                        	    unlockedAchievement.setRepeatCount(0);
	                        	    if (repeatCount < 0) scathaPro.variables.cheaterDetected = true;
                        	    }
                        	}
                    	}
                        
                        achievementManager.addUnlockedAchievement(unlockedAchievement);
                    }
                }
            }
        }
    }
    
    public void saveAchievements()
    {
        JsonArray unlockedAchievementsJson = new JsonArray();
        
        UnlockedAchievement[] unlockedAchievements = scathaPro.getAchievementManager().getAllUnlockedAchievements();
        for (UnlockedAchievement unlockedAchievement : unlockedAchievements)
        {
            JsonObject achievementObject = new JsonObject();
            achievementObject.add("achievementID", new JsonPrimitive(unlockedAchievement.achievement.getID()));
            achievementObject.add("unlockedAt", new JsonPrimitive(unlockedAchievement.unlockedAtTimestamp));
            if (unlockedAchievement.achievement.isRepeatable && unlockedAchievement.getRepeatCount() > 0)
        	{
            	achievementObject.add("repeatCount", new JsonPrimitive(unlockedAchievement.getRepeatCount()));
        	}
            unlockedAchievementsJson.add(achievementObject);
        }
        
        setInCurrentPlayer(unlockedAchievementsKey, unlockedAchievementsJson);
        saveData();
    }
    
    
    private void loadPetDrops()
    {
        scathaPro.variables.rarePetDrops = 0;
        scathaPro.variables.epicPetDrops = 0;
        scathaPro.variables.legendaryPetDrops = 0;
        scathaPro.variables.scathaKillsAtLastDrop = -1;
        scathaPro.variables.dropDryStreakInvalidated = false;
        
        JsonObject playerData = getCurrentPlayerObject();
        if (playerData == null) return;
        
        Integer rarePetDrops = JsonUtil.getInt(playerData, petDropsKey + "/rare");
        Integer epicPetDrops = JsonUtil.getInt(playerData, petDropsKey + "/epic");
        Integer legendaryPetDrops = JsonUtil.getInt(playerData, petDropsKey + "/legendary");
        
        if (rarePetDrops != null) scathaPro.variables.rarePetDrops = rarePetDrops;
        if (epicPetDrops != null) scathaPro.variables.epicPetDrops = epicPetDrops;
        if (legendaryPetDrops != null) scathaPro.variables.legendaryPetDrops = legendaryPetDrops;
        
        if
        (
            scathaPro.variables.rarePetDrops > Constants.maxLegitPetDropsAmount || scathaPro.variables.rarePetDrops < 0
            || scathaPro.variables.epicPetDrops > Constants.maxLegitPetDropsAmount || scathaPro.variables.epicPetDrops < 0
            || scathaPro.variables.legendaryPetDrops > Constants.maxLegitPetDropsAmount || scathaPro.variables.legendaryPetDrops < 0
        )
        {
            scathaPro.variables.cheaterDetected = true;
        }
        
        Integer scathaKillsAtLastDrop = JsonUtil.getInt(playerData, petDropsKey + "/scathaKillsAtLastDrop");
        if (scathaKillsAtLastDrop != null) scathaPro.variables.scathaKillsAtLastDrop = scathaKillsAtLastDrop;

        Boolean dropDryStreakInvalidated = JsonUtil.getBoolean(playerData, petDropsKey + "/dropDryStreakInvalidated");
        if (dropDryStreakInvalidated != null) scathaPro.variables.dropDryStreakInvalidated = dropDryStreakInvalidated;
        
        scathaPro.getOverlay().updatePetDrops();
        scathaPro.getOverlay().updateScathaKillsSinceLastDrop();
    }
    
    public void savePetDrops()
    {
        JsonObject petDropsJsonObject = new JsonObject();
        
        petDropsJsonObject.add("rare", new JsonPrimitive(scathaPro.variables.rarePetDrops));
        petDropsJsonObject.add("epic", new JsonPrimitive(scathaPro.variables.epicPetDrops));
        petDropsJsonObject.add("legendary", new JsonPrimitive(scathaPro.variables.legendaryPetDrops));
        petDropsJsonObject.add("scathaKillsAtLastDrop", new JsonPrimitive(scathaPro.variables.scathaKillsAtLastDrop));
        petDropsJsonObject.add("dropDryStreakInvalidated", new JsonPrimitive(scathaPro.variables.dropDryStreakInvalidated));
        
        setInCurrentPlayer(petDropsKey, petDropsJsonObject);
        saveData();
    }
    
    
    private void loadWormKills()
    {
        scathaPro.variables.regularWormKills = 0;
        scathaPro.variables.scathaKills = 0;
        
        JsonObject playerData = getCurrentPlayerObject();
        if (playerData == null) return;
        
        Integer regularWormKills = JsonUtil.getInt(playerData, wormKillsKey + "/regularWorms");
        Integer scathaKills = JsonUtil.getInt(playerData, wormKillsKey + "/scathas");
        
        if (regularWormKills != null) scathaPro.variables.regularWormKills = regularWormKills;
        if (scathaKills != null) scathaPro.variables.scathaKills = scathaKills;
        
        scathaPro.getOverlay().updateWormKills();
        scathaPro.getOverlay().updateScathaKills();
    }
    
    public void saveWormKills()
    {
        JsonObject wormKillsJsonObject = new JsonObject();
        
        wormKillsJsonObject.add("regularWorms", new JsonPrimitive(scathaPro.variables.regularWormKills));
        wormKillsJsonObject.add("scathas", new JsonPrimitive(scathaPro.variables.scathaKills));
        
        setInCurrentPlayer(wormKillsKey, wormKillsJsonObject);
        saveData();
    }
    
    
    private void loadDayData()
    {
        scathaPro.variables.lastPlayedDate = null;
        WormStatsType.PER_DAY.regularWormKills = 0;
        WormStatsType.PER_DAY.scathaKills = 0;
        WormStatsType.PER_DAY.scathaSpawnStreak = 0;
        scathaPro.variables.scathaFarmingStreak = 0;
        scathaPro.variables.scathaFarmingStreakHighscore = 0;
        scathaPro.variables.lastScathaFarmedDate = null;
        
        JsonObject playerData = getCurrentPlayerObject();
        if (playerData == null) return;
        
        JsonObject dayData = JsonUtil.getJsonObject(playerData, dayKey);
        
        scathaPro.variables.lastPlayedDate = TimeUtil.parseDate(JsonUtil.getString(dayData, "lastPlayed"));
        
        WormStatsType.PER_DAY.regularWormKills = Util.intOrZero(JsonUtil.getInt(dayData, "stats/wormKills/regularWorms"));
        WormStatsType.PER_DAY.scathaKills = Util.intOrZero(JsonUtil.getInt(dayData, "stats/wormKills/scathas"));
        WormStatsType.PER_DAY.scathaSpawnStreak = Util.intOrZero(JsonUtil.getInt(dayData, "stats/scathaSpawnStreak"));
        
        scathaPro.variables.scathaFarmingStreak = Util.intOrZero(JsonUtil.getInt(dayData, "scathaFarming/streak"));
        scathaPro.variables.scathaFarmingStreakHighscore = Util.intOrZero(JsonUtil.getInt(dayData, "scathaFarming/streakHighscore"));
        
        scathaPro.variables.lastScathaFarmedDate = TimeUtil.parseDate(JsonUtil.getString(dayData, "scathaFarming/lastFarmed"));
    }
    
    public void saveDailyStatsData()
    {
        JsonObject dayData = getDayData();
        JsonUtil.set(dayData, "stats/wormKills/regularWorms", new JsonPrimitive(WormStatsType.PER_DAY.regularWormKills));
        JsonUtil.set(dayData, "stats/wormKills/scathas", new JsonPrimitive(WormStatsType.PER_DAY.scathaKills));
        JsonUtil.set(dayData, "stats/scathaSpawnStreak", new JsonPrimitive(WormStatsType.PER_DAY.scathaSpawnStreak));
        saveData();
    }
    
    public void resetDailyStats()
    {
        scathaPro.variables.lastPlayedDate = TimeUtil.today();
        setInCurrentPlayer(dayKey + "/lastPlayed", new JsonPrimitive(TimeUtil.serializeDate(scathaPro.variables.lastPlayedDate)));
        
        WormStatsType.PER_DAY.regularWormKills = 0;
        WormStatsType.PER_DAY.scathaKills = 0;
        WormStatsType.PER_DAY.scathaSpawnStreak = 0;
        
        saveDailyStatsData();
        
        MinecraftForge.EVENT_BUS.post(new DailyStatsResetEvent());
        
        scathaPro.logDebug("Daily stats reset");
        
        if (TextUtil.getPlayerForChat() != null)
        {
            TextUtil.sendModChatMessage(Constants.msgHighlightingColor + "New IRL day started - per day stats reset");
        }
    }
    
    public void updateScathaFarmingStreak(boolean increase)
    {
        LocalDate today = TimeUtil.today();
        
        boolean streakUpdated = false;
        boolean highscoreUpdated = false;
        
        if (increase && (scathaPro.variables.scathaFarmingStreak == 0 || today.minusDays(1).equals(scathaPro.variables.lastScathaFarmedDate)))
        {
            scathaPro.variables.scathaFarmingStreak ++;
            streakUpdated = true;
            
            if (scathaPro.variables.scathaFarmingStreak > scathaPro.variables.scathaFarmingStreakHighscore)
            {
                scathaPro.variables.scathaFarmingStreakHighscore = scathaPro.variables.scathaFarmingStreak;
                highscoreUpdated = true;
            }
            
            if (scathaPro.getConfig().getBoolean(Config.Key.dailyScathaFarmingStreakMessage))
            {
                TextUtil.sendModChatMessage(Constants.msgHighlightingColor + "First Scatha kill of the day! You reached a daily Scatha farming streak of " + EnumChatFormatting.GREEN + scathaPro.variables.scathaFarmingStreak + " day" + (scathaPro.variables.scathaFarmingStreak != 1 ? "s" : "") + (highscoreUpdated ? EnumChatFormatting.GOLD + " (new highscore!)" : "") + EnumChatFormatting.GREEN + ".");
            }
        }
        else if (scathaPro.variables.lastScathaFarmedDate == null || !(scathaPro.variables.lastScathaFarmedDate.equals(today) || !increase && scathaPro.variables.lastScathaFarmedDate.equals(today.minusDays(1))))
        {
            int targetValue = increase ? 1 : 0;
            if (scathaPro.variables.scathaFarmingStreak != targetValue)
            {
                scathaPro.variables.scathaFarmingStreak = targetValue;
                streakUpdated = true;
                
                if (scathaPro.getConfig().getBoolean(Config.Key.dailyScathaFarmingStreakMessage) && scathaPro.variables.lastScathaFarmedDate != null)
                {
                    String message = EnumChatFormatting.RED + "You broke your daily Scatha farming streak!" + (increase ? (EnumChatFormatting.YELLOW + " Restarting the streak from 1.") : "");
                    TextUtil.sendCrystalHollowsMessage(TextUtil.getModMessageComponent(message));
                }
            }
        }
        
        if (streakUpdated)
        {
            JsonObject dayData = getDayData();
            JsonUtil.set(dayData, "scathaFarming/streak", new JsonPrimitive(scathaPro.variables.scathaFarmingStreak));
            
            if (increase)
            {
                scathaPro.variables.lastScathaFarmedDate = today;
                JsonUtil.set(dayData, "scathaFarming/lastFarmed", new JsonPrimitive(TimeUtil.serializeDate(scathaPro.variables.lastScathaFarmedDate)));
                
                if (highscoreUpdated) JsonUtil.set(dayData, "scathaFarming/streakHighscore", new JsonPrimitive(scathaPro.variables.scathaFarmingStreakHighscore));
            }
            
            saveData();
            
            MinecraftForge.EVENT_BUS.post(new DailyScathaFarmingStreakChangedEvent());
        }
    }
    
    /**
     * Returns a non-null reference to the day data object that can be modified without having to overwrite the whole object again
     */
    private JsonObject getDayData()
    {
        JsonObject dayData = JsonUtil.getJsonObject(getCurrentPlayerObject(), dayKey);
        if (dayData == null)
        {
            dayData = new JsonObject();
            setInCurrentPlayer(dayKey, dayData);
        }
        return dayData;
    }
    
    
    private void loadProfileStats()
    {
        scathaPro.variables.magicFind = -1f;
        scathaPro.variables.wormBestiaryMagicFind = -1f;
        scathaPro.variables.petLuck = -1f;
        
        JsonObject playerData = getCurrentPlayerObject();
        if (playerData == null) return;
        
        Double magicFind = JsonUtil.getDouble(playerData, profileStatsKey + "/magicFind");
        if (magicFind != null) scathaPro.variables.magicFind = magicFind.floatValue();
        Double wormBestiaryMagicFind = JsonUtil.getDouble(playerData, profileStatsKey + "/wormBestiaryMagicFind");
        if (wormBestiaryMagicFind != null) scathaPro.variables.wormBestiaryMagicFind = wormBestiaryMagicFind.floatValue();
        Double petLuck = JsonUtil.getDouble(playerData, profileStatsKey + "/petLuck");
        if (petLuck != null) scathaPro.variables.petLuck = petLuck.floatValue();
    }
    
    public void saveProfileStats()
    {
        JsonObject profileStatsObject = new JsonObject();
        
        profileStatsObject.add("magicFind", new JsonPrimitive(scathaPro.variables.magicFind));
        profileStatsObject.add("wormBestiaryMagicFind", new JsonPrimitive(scathaPro.variables.wormBestiaryMagicFind));
        profileStatsObject.add("petLuck", new JsonPrimitive(scathaPro.variables.petLuck));
        
        setInCurrentPlayer(profileStatsKey, profileStatsObject);
        saveData();
    }
    
    
    public void loadMiscData()
    {
        scathaPro.variables.scappaModeUnlocked = false;
        scathaPro.variables.lastAprilFoolsJokeShownYear = -1;
        scathaPro.variables.overlayIconGooglyEyesUnlocked = false;
        
        JsonObject playerJson = getCurrentPlayerObject();
        if (playerJson == null) return;
        
        JsonObject miscJson = JsonUtil.getJsonObject(playerJson, "misc");
        if (miscJson != null)
        {
            JsonObject unlockablesJson = JsonUtil.getJsonObject(miscJson, "unlockables");
            if (unlockablesJson != null)
            {
                Boolean scappaModeUnlocked = JsonUtil.getBoolean(unlockablesJson, "scappaModeUnlocked");
                if (scappaModeUnlocked != null) scathaPro.variables.scappaModeUnlocked = scappaModeUnlocked;
                
                Boolean overlayIconGooglyEyesUnlocked = JsonUtil.getBoolean(unlockablesJson, "overlayIconGooglyEyesUnlocked");
                if (overlayIconGooglyEyesUnlocked != null) scathaPro.variables.overlayIconGooglyEyesUnlocked = overlayIconGooglyEyesUnlocked;
            }
            
            Number lastAprilFoolsJokeShownYear = JsonUtil.getNumber(miscJson, "lastAprilFoolsJokeShownYear");
            if (lastAprilFoolsJokeShownYear != null)
            {
                scathaPro.variables.lastAprilFoolsJokeShownYear = lastAprilFoolsJokeShownYear.shortValue();
                if ((scathaPro.variables.lastAprilFoolsJokeShownYear >= 0 && scathaPro.variables.lastAprilFoolsJokeShownYear <= 2024)
                    || scathaPro.variables.lastAprilFoolsJokeShownYear >= 3000)
                {
                    scathaPro.variables.cheaterDetected = true;
                }
            }
        }
    }
    
    public void saveMiscData()
    {
        JsonObject miscDataObject = new JsonObject();
        
        JsonObject unlockablesObject = new JsonObject();
        if (scathaPro.variables.scappaModeUnlocked) unlockablesObject.add("scappaModeUnlocked", new JsonPrimitive(scathaPro.variables.scappaModeUnlocked));
        if (scathaPro.variables.overlayIconGooglyEyesUnlocked) unlockablesObject.add("overlayIconGooglyEyesUnlocked", new JsonPrimitive(scathaPro.variables.overlayIconGooglyEyesUnlocked));
        if (unlockablesObject.entrySet().size() > 0) miscDataObject.add("unlockables", unlockablesObject);
        
        if (scathaPro.variables.lastAprilFoolsJokeShownYear >= 0) miscDataObject.add("lastAprilFoolsJokeShownYear", new JsonPrimitive(scathaPro.variables.lastAprilFoolsJokeShownYear));
        
        setInCurrentPlayer("misc", miscDataObject);
        
        saveData();
    }
    
    
    public void loadGlobalData()
    {
        scathaPro.variables.avgMoneyCalcScathaPriceRare = -1f;
        scathaPro.variables.avgMoneyCalcScathaPriceEpic = -1f;
        scathaPro.variables.avgMoneyCalcScathaPriceLegendary = -1f;
        
        JsonObject globalData = JsonUtil.getJsonObject(data, "global");
        if (globalData == null) return;
        
        Double avgMoneyCalcScathaPriceRare = JsonUtil.getDouble(globalData, "averageMoneyCalculator/scathaPriceRare");
        if (avgMoneyCalcScathaPriceRare != null) scathaPro.variables.avgMoneyCalcScathaPriceRare = avgMoneyCalcScathaPriceRare.floatValue();
        Double avgMoneyCalcScathaPriceEpic = JsonUtil.getDouble(globalData, "averageMoneyCalculator/scathaPriceEpic");
        if (avgMoneyCalcScathaPriceEpic != null) scathaPro.variables.avgMoneyCalcScathaPriceEpic = avgMoneyCalcScathaPriceEpic.floatValue();
        Double avgMoneyCalcScathaPriceLegendary = JsonUtil.getDouble(globalData, "averageMoneyCalculator/scathaPriceLegendary");
        if (avgMoneyCalcScathaPriceLegendary != null) scathaPro.variables.avgMoneyCalcScathaPriceLegendary = avgMoneyCalcScathaPriceLegendary.floatValue();
    }
    
    public void saveGlobalData()
    {
        JsonObject globalData = JsonUtil.getJsonObject(data, "global");
        if (globalData == null) data.add("global", globalData = new JsonObject());
        
        JsonUtil.set(globalData, "averageMoneyCalculator/scathaPriceRare", new JsonPrimitive(scathaPro.variables.avgMoneyCalcScathaPriceRare));
        JsonUtil.set(globalData, "averageMoneyCalculator/scathaPriceEpic", new JsonPrimitive(scathaPro.variables.avgMoneyCalcScathaPriceEpic));
        JsonUtil.set(globalData, "averageMoneyCalculator/scathaPriceLegendary", new JsonPrimitive(scathaPro.variables.avgMoneyCalcScathaPriceLegendary));
        
        saveData();
    }
    
    
    private void onLoadError()
    {
        TextUtil.sendModErrorMessage("Error while loading persistent data, creating backup...");
        SaveManager.backupPersistentData("loadError");
    }
}
