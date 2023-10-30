package com.namelessju.scathapro.alertmodes;

import com.namelessju.scathapro.ScathaPro;

public class PresetAlertMode extends AlertMode {

	public PresetAlertMode(String id, String name) {
		super(id, name);
	}
	
	
    public String getIconPath() {
    	return "overlay/mode_icons/" + id + ".png";
    }

    
    public void playBedrockWallSound() {
    	if(!tryPlayAlertSound(getPresetSoundPath("bedrock_wall"))) super.playBedrockWallSound();
    }
    
    public void playWormPrespawnSound() {
    	if(!tryPlayAlertSound(getPresetSoundPath("prespawn"))) super.playWormPrespawnSound();
    }
    
    public void playWormSpawnSound() {
    	if(!tryPlayAlertSound(getPresetSoundPath("worm"))) super.playWormSpawnSound();
    }
    
    public void playScathaSpawnSound() {
    	if(!tryPlayAlertSound(getPresetSoundPath("scatha"))) super.playScathaSpawnSound();
    }
    
    public void playScathaPetDropSound() {
    	if(!tryPlayAlertSound(getPresetSoundPath("pet_drop"))) super.playScathaPetDropSound();
    }
    
    public void playGoblinSpawnSound() {
    	if(!tryPlayAlertSound(getPresetSoundPath("goblin"))) super.playGoblinSpawnSound();
    }
    
    private String getPresetSoundPath(String soundId) {
    	return ScathaPro.MODID + ":alert_modes." + id + "." + soundId;
    }
    
}
