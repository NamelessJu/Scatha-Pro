package com.namelessju.scathapro.eventlisteners;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namelessju.scathapro.OverlayManager;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.API.APIErrorEvent;
import com.namelessju.scathapro.API.APIResponseEvent;
import com.namelessju.scathapro.util.ChatUtil;
import com.namelessju.scathapro.util.JsonUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class APIListeners {
    
    ScathaPro scathaPro = ScathaPro.getInstance();
    
    @SubscribeEvent
    public void onAPIResponse(APIResponseEvent e) {
        String endpoint = e.endpoint;
        JsonObject json = e.json;
        
        if (endpoint.equals("profiles")) {
            JsonArray profiles = JsonUtil.getJsonArray(json, "profiles");
            
            if (profiles != null) {
                JsonObject profilePlayerData = null;
                long latestSave = -1;
                for (JsonElement profile : profiles) {
                    if (profile == null || !profile.isJsonObject()) break;
                    
                    JsonObject playerData = JsonUtil.getJsonObject(JsonUtil.getJsonObject(profile, "members"), Util.getPlayerUUIDString());
                    Long lastSave = JsonUtil.getLong(playerData, "last_save");
                    
                    if (lastSave != null && lastSave > latestSave) {
                        latestSave = lastSave;
                        profilePlayerData = playerData;
                    }
                }
                
                if (profilePlayerData != null) {
                    
                    JsonObject bestiaryJson = JsonUtil.getJsonObject(profilePlayerData, "bestiary");
                    if (bestiaryJson != null) {
                        
                        Integer overallWormKills = JsonUtil.getInt(bestiaryJson, "kills_worm_5"); // why the HP in the variable name tho
                        if (overallWormKills != null)
                            scathaPro.overallRegularWormKills = overallWormKills;
                        
                        Integer overallScathaKills = JsonUtil.getInt(bestiaryJson, "kills_scatha_10");
                        if (overallScathaKills != null)
                            scathaPro.overallScathaKills = overallScathaKills;

                        
                        OverlayManager.instance.updateWormKills();
                        OverlayManager.instance.updateScathaKills();
                        OverlayManager.instance.updateTotalKills();
                        OverlayManager.instance.updateScathaKillsAtLastDrop();

                        scathaPro.updateKillAchievements();
                        
                    }
                    
                    JsonObject collectionJson = JsonUtil.getJsonObject(profilePlayerData, "collection");
                    if (collectionJson != null) {
                        
                        Integer hardStoneMined = JsonUtil.getInt(collectionJson, "HARD_STONE");
                        if (hardStoneMined != null) {
                            scathaPro.hardstoneMined = hardStoneMined;
                            
                            Achievement.hard_stone_mined_1.setProgress(scathaPro.hardstoneMined);
                            Achievement.hard_stone_mined_2.setProgress(scathaPro.hardstoneMined);
                            Achievement.hard_stone_mined_3.setProgress(scathaPro.hardstoneMined);
                        }
                        
                    }
                    
                    return;
                }
            }

            ChatUtil.sendModErrorMessage("Couldn't load data from Hypixel API: No skyblock profiles found");
            scathaPro.repeatProfilesDataRequest = false;
        }
    }
    
    @SubscribeEvent
    public void onAPIError(APIErrorEvent e) {
        String endpoint = e.endpoint;
        APIErrorEvent.ErrorType errorType = e.errorType;
        
        if (endpoint.equals("profiles")) {
            if (errorType != APIErrorEvent.ErrorType.REQUEST_LIMIT_REACHED)
                scathaPro.repeatProfilesDataRequest = false;
        }
    }

}
