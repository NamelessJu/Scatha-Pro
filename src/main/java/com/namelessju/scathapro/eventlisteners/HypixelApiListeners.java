package com.namelessju.scathapro.eventlisteners;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namelessju.scathapro.OverlayManager;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.HypixelApiManager.HypixelApiErrorEvent;
import com.namelessju.scathapro.HypixelApiManager.HypixelApiResponseEvent;
import com.namelessju.scathapro.util.ChatUtil;
import com.namelessju.scathapro.util.JsonUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

// Unused
public class HypixelApiListeners {
    
    ScathaPro scathaPro = ScathaPro.getInstance();
    
    @SubscribeEvent
    public void onAPIResponse(HypixelApiResponseEvent e) {
        String endpoint = e.endpoint;
        JsonObject json = e.json;
        
        if (endpoint.equals("profiles")) {
            JsonArray profilesJson = JsonUtil.getJsonArray(json, "profiles");
            
            if (profilesJson != null) {
                JsonObject profileJson = null;
                
                for (JsonElement profileJsonElement : profilesJson) {
                    if (profileJsonElement == null || !profileJsonElement.isJsonObject()) continue;
                    
                    boolean isSelected = JsonUtil.getBoolean(profileJsonElement, "selected");
                    
                    if (isSelected) {
                    	profileJson = JsonUtil.getJsonObject(JsonUtil.getJsonObject(profileJsonElement, "members"), Util.getPlayerUUIDString());
                        break;
                    }
                }
                
                if (profileJson != null) {
                    
                    JsonObject bestiaryJson = JsonUtil.getJsonObject(profileJson, "bestiary");
                    if (bestiaryJson != null) {
                        
                        Integer overallWormKills = JsonUtil.getInt(bestiaryJson, "kills_worm_5"); // why the HP in the variable name tho
                        if (overallWormKills != null)
                            scathaPro.overallRegularWormKills = overallWormKills;
                        
                        Integer overallScathaKills = JsonUtil.getInt(bestiaryJson, "kills_scatha_10");
                        if (overallScathaKills != null)
                            scathaPro.overallScathaKills = overallScathaKills;

                        
                        OverlayManager.instance.updateWormKills();
                        OverlayManager.instance.updateScathaKills();

                        scathaPro.updateKillAchievements();
                    }
                    
                    JsonObject collectionJson = JsonUtil.getJsonObject(profileJson, "collection");
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
    public void onAPIError(HypixelApiErrorEvent e) {
        String endpoint = e.endpoint;
        HypixelApiErrorEvent.ErrorType errorType = e.errorType;
        
        if (endpoint.equals("profiles")) {
            if (errorType != HypixelApiErrorEvent.ErrorType.REQUEST_LIMIT_REACHED)
                scathaPro.repeatProfilesDataRequest = false;
        }
    }

}
