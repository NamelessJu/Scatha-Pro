package com.namelessju.scathapro.alertmodes;

import com.namelessju.scathapro.alertmodes.customalertmode.CustomAlertModeManager;
import com.namelessju.scathapro.util.SoundUtil;

public class CustomAlertMode extends AlertMode {

	public CustomAlertMode(String id, String name) {
		super(id, name);
	}
	
    private String propertyOrDefault(String propertyJsonPath, String defaultString) {
    	String property = CustomAlertModeManager.instance.getSubModeProperty(propertyJsonPath);
    	return property != null ? property : defaultString;
    }
	
	
    public String getIconPath() {
    	return "overlay/mode_icons/custom.png";
    }

    
    public void playBedrockWallSound() {
    	if (!tryPlayAlertSound(CustomAlertModeManager.getResourceName("bedrock_wall"))) super.playBedrockWallSound();
    }
    
    public void playWormPrespawnSound() {
    	if (!tryPlayAlertSound(CustomAlertModeManager.getResourceName("prespawn"))) super.playWormPrespawnSound();
    }
    
    public void playWormSpawnSound() {
    	if (!tryPlayAlertSound(CustomAlertModeManager.getResourceName("worm"))) super.playWormSpawnSound();
    }
    
    public void playScathaSpawnSound() {
    	if (!tryPlayAlertSound(CustomAlertModeManager.getResourceName("scatha"))) super.playScathaSpawnSound();
    }
    
    public void playScathaPetDropSound() {
    	if (!tryPlayAlertSound(CustomAlertModeManager.getResourceName("pet_drop"))) super.playScathaPetDropSound();
    }
    
    public void playGoblinSpawnSound() {
    	if (!tryPlayAlertSound(CustomAlertModeManager.getResourceName("goblin"))) super.playGoblinSpawnSound();
    }
    
    protected boolean tryPlayAlertSound(String id) {
    	if (SoundUtil.soundExists(id)) {
    		playAlertSound(id);
    		return true;
    	}
    	else {
    		// ScathaPro.getInstance().logger.log(Level.INFO, "Tried to play unknown custom alert mode sound \"" + id + "\"");
    		return false;
    	}
    }
    

    public String getBedrockWallSubtitle() {
    	return propertyOrDefault("titles/bedrockWall/subtitle", super.getBedrockWallSubtitle());
    }
    
    public String getWormPrespawnSubtitle() {
    	return propertyOrDefault("titles/wormPrespawn/subtitle", super.getWormPrespawnSubtitle());
    }
    
    public String getWormSpawnTitle() {
    	return propertyOrDefault("titles/wormSpawn/title", super.getWormSpawnTitle());
    }
    public String getWormSpawnSubtitle() {
    	return propertyOrDefault("titles/wormSpawn/subtitle", super.getWormSpawnSubtitle());
    }
    
    public String getScathaSpawnTitle() {
    	return propertyOrDefault("titles/scathaSpawn/title", super.getScathaSpawnTitle());
    }
    public String getScathaSpawnSubtitle() {
    	return propertyOrDefault("titles/scathaSpawn/subtitle", super.getScathaSpawnSubtitle());
    }
    
    public String getScathaPetDropTitle() {
    	return propertyOrDefault("titles/scathaPetDrop/title", super.getScathaPetDropTitle());
    }
}
