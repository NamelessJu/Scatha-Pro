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
        else Util.sendModErrorMessage("Your session is offline, so data won't be saved!");
    }
    
    public JsonElement get(String path) {
        String[] pathNodes = path.split("/");

        JsonElement currentElement = data.get(Util.getPlayerUUIDString());
        
        if (currentElement != null) {
            for (int i = 0; i < pathNodes.length; i ++) {
                if (currentElement != null && currentElement.isJsonObject()) {
                    JsonElement nextElement = currentElement.getAsJsonObject().get(pathNodes[i]);
                    currentElement = nextElement;
                }
                else return null;
            }
            
            return currentElement;
        }
        
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
    
    private JsonPrimitive getJsonPrimitive(String path) {
        JsonElement jsonElement = get(path);
        if (jsonElement instanceof JsonPrimitive)
            return (JsonPrimitive) jsonElement;
        return null;
    }
    
    public String getString(String path, String defaultValue) {
        JsonPrimitive jsonPrimitive = getJsonPrimitive(path);
        if (jsonPrimitive != null)
            return jsonPrimitive.getAsString();
        return defaultValue;
    }
    
    public int getInt(String path, int defaultValue) {
        JsonPrimitive jsonPrimitive = getJsonPrimitive(path);
        if (jsonPrimitive != null)
            return jsonPrimitive.getAsInt();
        return defaultValue;
    }
    
    public double getDouble(String path, double defaultValue) {
        JsonPrimitive jsonPrimitive = getJsonPrimitive(path);
        if (jsonPrimitive != null)
            return jsonPrimitive.getAsDouble();
        return defaultValue;
    }
    
    public boolean getBoolean(String path, boolean defaultValue) {
        JsonPrimitive jsonPrimitive = getJsonPrimitive(path);
        if (jsonPrimitive != null)
            return jsonPrimitive.getAsBoolean();
        return defaultValue;
    }
    
    
    
    public void loadAchievements() {
        JsonElement achievementsJson = PersistentData.instance.get(unlockedAchievementsKey);
        if (achievementsJson != null) {
            if (achievementsJson instanceof JsonArray) {
                JsonArray achievementsJsonArray = achievementsJson.getAsJsonArray();
                
                AchievementManager.instance.unlockedAchievements.clear();
                
                for (JsonElement achievementObjectJson : achievementsJsonArray) {
                    if (achievementObjectJson instanceof JsonObject) {
                        JsonObject achievementObject = achievementObjectJson.getAsJsonObject();
                        
                        JsonElement achievementJson = achievementObject.get("achievementID");
                        if (achievementJson instanceof JsonPrimitive) {
                            JsonPrimitive achievementJsonPrimitive = achievementJson.getAsJsonPrimitive();
                            if (achievementJsonPrimitive.isString()) {
                                Achievement achievement = Achievement.getByID(achievementJsonPrimitive.getAsString());
                                
                                if (AchievementManager.instance.isAchievementUnlocked(achievement)) return;
                                
                                long unlockedAtTimestamp = -1; 
                                JsonElement unlockedAtJson = achievementObject.get("unlockedAt");
                                if (unlockedAtJson instanceof JsonPrimitive) {
                                    JsonPrimitive unlockedAtJsonPrimitive = unlockedAtJson.getAsJsonPrimitive();
                                    if (unlockedAtJsonPrimitive.isNumber()) unlockedAtTimestamp = unlockedAtJsonPrimitive.getAsLong();
                                }
    
                                if (achievement != null && unlockedAtTimestamp >= 0) {
                                    if (unlockedAtTimestamp > Util.getCurrentTime() || unlockedAtTimestamp < 1640991600000L)
                                        scathaPro.showFakeBan = true;
                                    
                                    AchievementManager.instance.unlockedAchievements.add(new UnlockedAchievement(achievement, unlockedAtTimestamp));
                                    achievement.setProgress(achievement.goal);
                                }
                            }
                        }
                    }
                }
            }
            else scathaPro.logger.log(Level.WARN, "Couldn't load achievements: Achievements JSON isn't an array");
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
        String errorPrefix = "Couldn't load pet drops: ";
        
        JsonElement petDropsJson = PersistentData.instance.get(petDropsKey);
        if (petDropsJson != null) {
            if (petDropsJson instanceof JsonObject) {
                JsonObject petDropsJsonObject = petDropsJson.getAsJsonObject();
                
                JsonElement rareDropsJson = petDropsJsonObject.get("rare");
                if (rareDropsJson instanceof JsonPrimitive) {
                    JsonPrimitive rareDropsJsonPrimitive = rareDropsJson.getAsJsonPrimitive();
                    if (rareDropsJsonPrimitive.isNumber()) {
                        scathaPro.rarePetDrops = rareDropsJsonPrimitive.getAsInt();
                        if (scathaPro.rarePetDrops > 9999) scathaPro.showFakeBan = true;
                    }
                    else scathaPro.logger.log(Level.WARN, errorPrefix + "Rare drops JSON isn't a number");
                }
                else scathaPro.logger.log(Level.WARN, errorPrefix + "Rare drops JSON isn't a primitive");
                
                JsonElement epicDropsJson = petDropsJsonObject.get("epic");
                if (epicDropsJson instanceof JsonPrimitive) {
                    JsonPrimitive epicDropsJsonPrimitive = epicDropsJson.getAsJsonPrimitive();
                    if (epicDropsJsonPrimitive.isNumber()) {
                        scathaPro.epicPetDrops = epicDropsJsonPrimitive.getAsInt();
                        if (scathaPro.epicPetDrops > 9999) scathaPro.showFakeBan = true;
                    }
                    else scathaPro.logger.log(Level.WARN, errorPrefix + "Epic drops JSON isn't a number");
                }
                else scathaPro.logger.log(Level.WARN, errorPrefix + "Epic drops JSON isn't a primitive");
                
                JsonElement legendaryDropsJson = petDropsJsonObject.get("legendary");
                if (legendaryDropsJson instanceof JsonPrimitive) {
                    JsonPrimitive legendaryDropsJsonPrimitive = legendaryDropsJson.getAsJsonPrimitive();
                    if (legendaryDropsJsonPrimitive.isNumber()) {
                        scathaPro.legendaryPetDrops = legendaryDropsJsonPrimitive.getAsInt();
                        if (scathaPro.legendaryPetDrops > 9999) scathaPro.showFakeBan = true;
                    }
                    else scathaPro.logger.log(Level.WARN, errorPrefix + "Legendary drops JSON isn't a number");
                }
                else scathaPro.logger.log(Level.WARN, errorPrefix + "Legendary drops JSON isn't a primitive");
            }
            else scathaPro.logger.log(Level.WARN, errorPrefix + "Pet drops JSON isn't an object");
        }
    }
    
    public void savePetDrops() {
        JsonObject petDropsJsonObject = new JsonObject();
        
        petDropsJsonObject.add("rare", new JsonPrimitive(scathaPro.rarePetDrops));
        petDropsJsonObject.add("epic", new JsonPrimitive(scathaPro.epicPetDrops));
        petDropsJsonObject.add("legendary", new JsonPrimitive(scathaPro.legendaryPetDrops));
        
        PersistentData.instance.set(petDropsKey, petDropsJsonObject);
        
        PersistentData.instance.saveData();
    }
}
