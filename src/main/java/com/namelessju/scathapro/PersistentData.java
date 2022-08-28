package com.namelessju.scathapro;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import org.apache.logging.log4j.Level;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.achievements.AchievementManager;
import com.namelessju.scathapro.achievements.UnlockedAchievement;
import com.namelessju.scathapro.util.ChatUtil;
import com.namelessju.scathapro.util.JsonUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraftforge.common.config.Configuration.UnicodeInputStreamReader;

public class PersistentData {

    public static final PersistentData instance = new PersistentData();
    
    private static final File saveFile = Util.getModFile("persistentData.json");
    private static final String unlockedAchievementsKey = "unlockedAchievements";
    private static final String petDropsKey = "petDrops";

    public static final ScathaPro scathaPro = ScathaPro.getInstance();
    
    private JsonObject data = new JsonObject();
    
    private PersistentData() {}

    public void loadData() {
        if (saveFile.exists() && saveFile.canRead()) {
            try {
                UnicodeInputStreamReader inputStream = new UnicodeInputStreamReader(new FileInputStream(saveFile), "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStream);
    
                StringBuilder jsonBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
                bufferedReader.close();
                
                JsonElement dataJson = new JsonParser().parse(jsonBuilder.toString());
                if (dataJson.isJsonObject()) {
                    data = dataJson.getAsJsonObject();
                    
                    String uuid = Util.getPlayerUUIDString(); 
                    if (uuid != null && (data.get("unlockedAchievements") != null || data.get("petDrops") != null)) {
                        JsonObject oldData = data;
                        data = new JsonObject();
                        data.add(uuid, oldData);
                    }
                    
                    loadAchievements();
                    loadPetDrops();
                }
                else ScathaPro.getInstance().logger.log(Level.ERROR, "Couldn't load persistent data (JSON root is not an object)");
            }
            catch (Exception e) {
                ScathaPro.getInstance().logger.log(Level.ERROR, "Error while trying to load persistent data (" + e.getClass().getSimpleName() + ")");
            }
        }
    }
    
    public void saveData() {
        if (Util.getPlayerUUIDString() != null) {
            try {
                FileOutputStream outputStream = new FileOutputStream(saveFile);
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
    
                bufferedWriter.write(data.toString());
                bufferedWriter.close();
            }
            catch (Exception e) {
                ScathaPro.getInstance().logger.log(Level.ERROR, "Error while trying to save persistent data");
            }
        }
        else ChatUtil.sendModErrorMessage("Your session is offline, so data won't be saved!");
    }
    
    public JsonObject getCurrentPlayerObject() {
        JsonElement playerElement = data.get(Util.getPlayerUUIDString());
        
        if (playerElement != null && playerElement.isJsonObject())
            return playerElement.getAsJsonObject();
        
        return null; 
    }

    public boolean set(String path, JsonElement value) {
        String[] pathNodes = path.split("/");

        String uuid = Util.getPlayerUUIDString();
        if (uuid == null) return false;
        
        JsonElement currentElement = data.get(uuid);
        
        if (currentElement == null) {
            currentElement = new JsonObject();
            data.add(uuid, currentElement);
        }
        
        for (int i = 0; i < pathNodes.length; i ++) {
            if (i == pathNodes.length - 1) {
                if (currentElement.isJsonObject()) {
                    currentElement.getAsJsonObject().add(pathNodes[i], value);
                    return true;
                }
                else break;
            }
            else if (currentElement.isJsonObject()) {
                JsonElement nextElement = currentElement.getAsJsonObject().get(pathNodes[i]);
                currentElement = nextElement;
            }
            else break;
        }
        
        return false;
    }
    
    
    public void loadAchievements() {
        JsonArray achievementsJsonArray = JsonUtil.getJsonArray(PersistentData.instance.getCurrentPlayerObject(), unlockedAchievementsKey);
        
        if (achievementsJsonArray != null) {
        
            AchievementManager.instance.unlockedAchievements.clear();
            
            for (JsonElement achievementJsonElement : achievementsJsonArray) {
                
                String achievementID = JsonUtil.getString(achievementJsonElement, "achievementID");
                Long unlockedAtTimestamp = (Long) JsonUtil.getNumber(achievementJsonElement, "unlockedAt");
                
                if (achievementID != null && unlockedAtTimestamp != null) {
                    
                    Achievement achievement = Achievement.getByID(achievementID);

                    if (achievement != null && AchievementManager.instance.isAchievementUnlocked(achievement)) {
                        
                        if (unlockedAtTimestamp > Util.getCurrentTime() || unlockedAtTimestamp < 1640991600000L)
                            scathaPro.showFakeBan = true;
                        
                        AchievementManager.instance.unlockedAchievements.add(new UnlockedAchievement(achievement, unlockedAtTimestamp));
                        achievement.setProgress(achievement.goal);
                    }
                }
            }
        }
    }
    
    public void saveAchievements() {
        JsonArray unlockedAchievementsJson = new JsonArray();
        
        for (UnlockedAchievement unlockedAchievement : AchievementManager.instance.unlockedAchievements) {
            JsonObject achievementObject = new JsonObject();
            achievementObject.add("achievementID", new JsonPrimitive(unlockedAchievement.achievement.getID()));
            achievementObject.add("unlockedAt", new JsonPrimitive(unlockedAchievement.unlockedAtTimestamp));
            unlockedAchievementsJson.add(achievementObject);
        }
        
        PersistentData.instance.set(unlockedAchievementsKey, unlockedAchievementsJson);
        
        PersistentData.instance.saveData();
    }
    
    
    public void loadPetDrops() {
        JsonObject petDropsJson = JsonUtil.getJsonObject(PersistentData.instance.getCurrentPlayerObject(), petDropsKey);
        
        if (petDropsJson != null) {
            
            Integer rareDrops = (Integer) JsonUtil.getNumber(petDropsJson, "rare");
            scathaPro.rarePetDrops = rareDrops;
            
            Integer epicDrops = (Integer) JsonUtil.getNumber(petDropsJson, "epic");
            scathaPro.epicPetDrops = epicDrops;
            
            Integer legendaryDrops = (Integer) JsonUtil.getNumber(petDropsJson, "legendary");
            scathaPro.legendaryPetDrops = legendaryDrops;
            
            
            if (scathaPro.rarePetDrops > 9999 || scathaPro.rarePetDrops < 0) scathaPro.showFakeBan = true;
            
            
            Integer scathaKillsAtLastDrop = (Integer) JsonUtil.getNumber(petDropsJson, "scathaKillsAtLastDrop");
            scathaPro.scathaKillsAtLastDrop = scathaKillsAtLastDrop;
        }
    }
    
    public void savePetDrops() {
        JsonObject petDropsJsonObject = new JsonObject();
        
        petDropsJsonObject.add("rare", new JsonPrimitive(scathaPro.rarePetDrops));
        petDropsJsonObject.add("epic", new JsonPrimitive(scathaPro.epicPetDrops));
        petDropsJsonObject.add("legendary", new JsonPrimitive(scathaPro.legendaryPetDrops));
        
        petDropsJsonObject.add("scathaKillsAtLastDrop", new JsonPrimitive(scathaPro.scathaKillsAtLastDrop));
        
        PersistentData.instance.set(petDropsKey, petDropsJsonObject);
        
        PersistentData.instance.saveData();
    }
}
