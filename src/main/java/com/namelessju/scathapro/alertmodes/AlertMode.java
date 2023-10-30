package com.namelessju.scathapro.alertmodes;

import org.apache.logging.log4j.Level;

import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.util.SoundUtil;

public class AlertMode {
	
	public static final AlertMode DEFAULT = new AlertMode("normal", "Normal");
	
    
    public final String id;
    public final String name;
    
    protected AlertMode(String id, String name) {
        this.id = id;
        this.name = name;
    }
    
    
    public String getIconPath() {
    	return "overlay/scatha_pet.png";
    }

    
    public void playBedrockWallSound() {
    	playAlertSound("note.pling", 1f, 0.5f);
    }

    public void playWormPrespawnSound() {
    	playAlertSound("random.orb", 1f, 0.5f);
    }
    
    public void playWormSpawnSound() {
    	playAlertSound("random.levelup", 1f, 0.5f);
    }
    
    public void playScathaSpawnSound() {
    	playAlertSound("random.levelup", 1f, 0.8f);
    }
    
    public void playScathaPetDropSound() {
    	playAlertSound("mob.wither.death", 0.75f, 0.8f);
    }
    
    public void playGoblinSpawnSound() {
    	playAlertSound("random.levelup", 1f, 1.5f);
    }
    
    
    public String getBedrockWallSubtitle() {
    	return "Close to bedrock wall";
    }

    public String getWormPrespawnSubtitle() {
    	return "Worm about to spawn...";
    }
    
    public String getWormSpawnTitle() {
    	return "Worm";
    }
    public String getWormSpawnSubtitle() {
    	return "Just a regular worm...";
    }
    
    public String getScathaSpawnTitle() {
    	return "Scatha";
    }
    public String getScathaSpawnSubtitle() {
    	return "Pray to RNGesus!";
    }
    
    public String getScathaPetDropTitle() {
    	return "Pet Drop!";
    }
    
    
    protected boolean tryPlayAlertSound(String id) {
    	if (SoundUtil.soundExists(id)) {
    		playAlertSound(id);
    		return true;
    	}
    	else {
    		ScathaPro.getInstance().logger.log(Level.WARN, "Tried to play unavailable alert mode sound \"" + id + "\"");
    		return false;
    	}
    }

    protected void playAlertSound(String id) {
    	playAlertSound(id, 1f, 1f);
    }
    protected void playAlertSound(String id, float volume, float pitch) {
    	SoundUtil.playSound(id, volume * (float) Config.instance.getDouble(Config.Key.alertVolume), pitch);
    }
}
