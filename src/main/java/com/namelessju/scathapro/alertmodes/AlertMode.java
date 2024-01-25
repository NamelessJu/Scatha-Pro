package com.namelessju.scathapro.alertmodes;

import net.minecraft.util.ResourceLocation;

public class AlertMode {
	
	public static final AlertMode DEFAULT_MODE = new AlertMode("normal", "Normal");
	
	
    public final String id;
    public final String name;
    
    protected AlertMode(String id, String name) {
        this.id = id;
        this.name = name;
    }
    
    
    public String getIconPath() {
    	return "overlay/scatha_pet.png";
    }

    
    public ResourceLocation getSoundBaseResourceLocation() {
    	return null;
    }
    
    public AlertTitle getTitle(String alertId) {
    	return null;
    }
    
}
