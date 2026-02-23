package namelessju.scathapro.files.legacy;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.files.framework.ScathaProFile;
import namelessju.scathapro.util.FileUtil;
import namelessju.scathapro.util.JsonUtil;
import org.apache.commons.lang3.NotImplementedException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;

public class LegacyPersistentData extends ScathaProFile
{
    public LegacyPersistentData(ScathaPro scathaPro)
    {
        super(scathaPro, Path.of("persistentData.json"));
    }
    
    @Override
    protected void deserialize(@Nullable String content)
    {
        if (!(JsonUtil.parseJson(content) instanceof JsonObject jsonObject)) return;
        
        updatePlayerData(jsonObject);
        updateGlobalData(jsonObject);
        
        String jsonString = JsonUtil.toString(jsonObject, false);
        if (jsonString == null)
        {
            ScathaPro.LOGGER.error("Failed to convert legacy persistent data: Couldn't stringify JSON");
            return;
        }
        try
        {
            FileUtil.writeFile(scathaPro.persistentData.getFile(), jsonString);
        }
        catch (IOException e)
        {
            ScathaPro.LOGGER.error("Failed to convert legacy persistent data: Exception while writing file");
        }
    }
    
    private void updatePlayerData(JsonObject jsonObject)
    {
        JsonArray playersArray = new JsonArray();
        String[] keys = jsonObject.keySet().toArray(String[]::new);
        for (String playerUUID : keys)
        {
            if ("global".equalsIgnoreCase(playerUUID)) continue;
            
            JsonElement oldPlayerData = JsonUtil.remove(jsonObject, playerUUID);
            updateProfileData(oldPlayerData);
            
            JsonObject newPlayerData = new JsonObject();
            
            newPlayerData.add("playerUUID", new JsonPrimitive(playerUUID));
            
            JsonArray profilesArray = new JsonArray();
            profilesArray.add(oldPlayerData);
            newPlayerData.add("profiles", profilesArray);
            
            playersArray.add(newPlayerData);
        }
        jsonObject.add("players", playersArray);
    }
    
    private void updateProfileData(JsonElement profileData)
    {
        if (!(profileData instanceof JsonObject profileObject)) return;
        
        // Profile ID isn't set because this loads into the "null" = unknown profile
        
        move(profileObject, "petDrops.scathaKillsAtLastDrop", "dryStreak.scathaKillsAtLastDrop");
        move(profileObject, "petDrops.dropDryStreakInvalidated", "dryStreak.isDryStreakInvalidated");
        
        move(profileObject, "daily.lastPlayed", "realTime.lastPlayedDate");
        move(profileObject, "daily.stats", "realTime.wormStatsToday");
        move(profileObject, "daily.scathaFarming.streak", "realTime.scathaFarmingStreak.current");
        move(profileObject, "daily.scathaFarming.streakHighscore", "realTime.scathaFarmingStreak.highscore");
        move(profileObject, "daily.scathaFarming.lastFarmed", "realTime.lastScathaFarmedDate");
        JsonUtil.remove(profileObject, "daily");
    }
    
    /** Needs to be run <b>AFTER</b> updating the player data */
    private void updateGlobalData(JsonObject jsonObject)
    {
        move(jsonObject, "global.lastUsedVersion", "lastUsedModVersion");
        move(jsonObject, "global.averageMoneyCalculator", "averageMoneyCalculator");
        JsonUtil.remove(jsonObject, "global");
    }
    
    @Override
    protected @NonNull String serialize()
    {
        throw new NotImplementedException("This file cannot be saved");
    }
    
    private void move(JsonObject parentObject, String oldPath, String newPath)
    {
        JsonElement element = JsonUtil.remove(parentObject, oldPath);
        if (element == null) return;
        JsonUtil.set(parentObject, newPath, element);
    }
}
