package com.namelessju.scathapro.alertmodes;

import com.google.gson.JsonElement;
import com.namelessju.scathapro.alertmodes.customalertmode.CustomAlertModeManager;
import com.namelessju.scathapro.util.JsonUtil;

import net.minecraft.util.ResourceLocation;

public class CustomAlertMode extends AlertMode {

	public CustomAlertMode(String id, String name) {
		super(id, name);
	}
	
    public String getIconPath() {
    	return "overlay/mode_icons/custom.png";
    }
    
    
    public ResourceLocation getSoundBaseResourceLocation() {
    	return new ResourceLocation(CustomAlertModeManager.resourceDomain, "");
    }
    
    public AlertTitle getTitle(String alertId) {
    	JsonElement titlesJson = CustomAlertModeManager.instance.getCurrentSubmodePropertyJsonElement("titles");
    	if (titlesJson == null) return null;
    	String title = JsonUtil.getString(titlesJson, alertId + "/title");
    	String subtitle = JsonUtil.getString(titlesJson, alertId + "/subtitle");
    	return new AlertTitle(title, subtitle);
    }
}
