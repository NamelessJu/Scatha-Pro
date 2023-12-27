package com.namelessju.scathapro.alertmodes;

import com.namelessju.scathapro.util.SoundUtil;

import net.minecraft.util.ResourceLocation;

public enum Alert {

	bedrockWall("bedrock_wall", "note.pling", 1f, 0.5f, new AlertTitle(null, "Close to bedrock wall"), 3, 20, 5),
	wormPrespawn("worm_prespawn", "random.orb", 1f, 0.5f, new AlertTitle(null, "Worm about to spawn..."), 0, 20, 5),
	wormSpawn("worm_spawn", "random.levelup", 1f, 0.5f, new AlertTitle("Worm", "Just a regular worm..."), 5, 20, 5),
	scathaSpawn("scatha_spawn", "random.levelup", 1f, 0.8f, new AlertTitle("Scatha", "Pray to RNGesus!"), 0, 40, 10),
	scathaPetDrop("scatha_pet_drop", "mob.wither.death", 0.75f, 0.8f, new AlertTitle("Scatha Pet Drop!", null), 0, 130, 20),
	goblinSpawn("goblin_spawn", "random.levelup", 1f, 1.5f, new AlertTitle("Goblin", null), 2, 30, 10);
	
	
	public final String alertId;
	
	private final String defaultSoundName;
	private final float defaultSoundVolume;
	private final float defaultSoundPitch;
	private final AlertTitle defaultTitle;
	private final int titleFadeInTicks;
	private final int titleStayTicks;
	private final int titleFadeOutTicks;
	
	Alert(String alertId, String defaultSoundName, float defaultSoundVolume, float defaultSoundPitch, AlertTitle defaultTitle, int titleFadeInTicks, int titleStayTicks, int titleFadeOutTicks) {
		this.alertId = alertId;
		
		this.defaultSoundName = defaultSoundName;
		this.defaultSoundVolume = defaultSoundVolume;
		this.defaultSoundPitch = defaultSoundPitch;
		this.defaultTitle = defaultTitle;
		this.titleFadeInTicks = titleFadeInTicks;
		this.titleStayTicks = titleStayTicks;
		this.titleFadeOutTicks = titleFadeOutTicks;
	}
	

	public void play() {
		play(null);
	}
	public void play(String details) {
		playSound();
		displayTitle(details);
	}
	
	
	public void playSound() {
		ResourceLocation soundParentLocation = AlertModeManager.getCurrentMode().getSoundBaseResourceLocation();
		
		if (soundParentLocation == null) {
			playDefaultSound();
			return;
		}
		
		String soundPath = soundParentLocation.toString() + (soundParentLocation.getResourcePath().isEmpty() ? "" : ".") + alertId;
		if (SoundUtil.soundExists(soundPath)) SoundUtil.playSound(soundPath);
		else playDefaultSound();
	}
	
	public void playDefaultSound() {
		SoundUtil.playSound(defaultSoundName, defaultSoundVolume, defaultSoundPitch);
	}
	
	
	public void displayTitle(String details) {
		AlertTitle modeTitle = AlertModeManager.getCurrentMode().getTitle(alertId);
		if (modeTitle != null) {
			AlertTitle title = new AlertTitle(this.hasTitle() ? modeTitle.title : defaultTitle.title, this.hasSubtitle() ? modeTitle.subtitle : (details != null ? details : defaultTitle.subtitle));
			title.display(titleFadeInTicks, titleStayTicks, titleFadeOutTicks);
		}
		else defaultTitle.display(titleFadeInTicks, titleStayTicks, titleFadeOutTicks);
	}
	
	public boolean hasTitle() {
		return defaultTitle.title != null;
	}
	
	public boolean hasSubtitle() {
		return defaultTitle.subtitle != null;
	}
}
