package com.namelessju.scathapro.eventlisteners;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namelessju.scathapro.OverlayManager;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.Util;
import com.namelessju.scathapro.API.APIErrorEvent;
import com.namelessju.scathapro.API.APIResponseEvent;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class APIListeners {
    
    ScathaPro scathaPro = ScathaPro.getInstance();
    
    @SubscribeEvent
    public void onAPIResponse(APIResponseEvent e) {
        String endpoint = e.endpoint;
        JsonObject json = e.json;
        
        if (endpoint.equals("profiles")) {
            JsonElement profiles = json.get("profiles");
            
            if (profiles instanceof JsonArray) {
                JsonObject profilePlayerData = null;
                long latestSave = -1;
                for (JsonElement profile : profiles.getAsJsonArray()) {
                    JsonObject playerData = profile.getAsJsonObject().get("members").getAsJsonObject().get(Util.getPlayerUUIDString()).getAsJsonObject();
                    JsonElement lastSaveJson = playerData.get("last_save");
                    
                    if (lastSaveJson != null) {
                        long lastSave = lastSaveJson.getAsLong();
                        if (lastSave > latestSave) {
                            latestSave = lastSave;
                            profilePlayerData = playerData;
                        }
                    }
                }
                
                if (profilePlayerData != null) {
                    JsonObject stats = profilePlayerData.get("stats").getAsJsonObject();
                    
                    JsonElement overallWormKillsJson = stats.get("kills_worm");
                    JsonElement overallScathaKillsJson = stats.get("kills_scatha");
                    
                    scathaPro.overallRegularWormKills = overallWormKillsJson != null ? overallWormKillsJson.getAsInt() : 0;
                    scathaPro.overallScathaKills = overallScathaKillsJson != null ? overallScathaKillsJson.getAsInt() : 0;

                    OverlayManager.instance.updateWormKills();
                    OverlayManager.instance.updateScathaKills();
                    OverlayManager.instance.updateTotalKills();

                    scathaPro.updateKillAchievements();
                    return;
                }
            }
            
            Util.sendModErrorMessage("Couldn't load worm kills from Hypixel API: No skyblock profiles found");
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
