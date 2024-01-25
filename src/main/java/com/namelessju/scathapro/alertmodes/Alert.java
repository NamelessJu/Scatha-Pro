package com.namelessju.scathapro.alertmodes;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.util.SoundUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

public enum Alert {

	bedrockWall("bedrock_wall", "Bedrock Wall Alert", new DefaultAlertSound("note.pling", 1f, 0.5f), new AlertTitle(null, "Close to bedrock wall", null, EnumChatFormatting.GRAY.toString(), 5, 20, 5)),
	wormPrespawn("worm_prespawn", "Worm Prespawn Alert", new DefaultAlertSound("random.orb", 1f, 0.5f), new AlertTitle(null, "Worm about to spawn...", null, EnumChatFormatting.YELLOW.toString(), 0, 20, 5)),
	wormSpawn("worm_spawn", "Worm Spawn Alert", new DefaultAlertSound("random.levelup", 1f, 0.5f), new AlertTitle("Worm", "Just a regular worm...", EnumChatFormatting.YELLOW.toString(), EnumChatFormatting.GRAY.toString(), 5, 20, 5)),
	scathaSpawn("scatha_spawn", "Scatha Spawn Alert", new DefaultAlertSound("random.levelup", 1f, 0.8f), new AlertTitle("Scatha", "Pray to RNGesus!", EnumChatFormatting.RED.toString(), EnumChatFormatting.GRAY.toString(), 0, 40, 10)),
	scathaPetDrop("scatha_pet_drop", "Scatha Pet Drop Alert", new DefaultAlertSound("mob.wither.death", 0.75f, 0.8f), new AlertTitle("Scatha Pet Drop!", null, EnumChatFormatting.YELLOW.toString(), null, 0, 130, 20)),
	goblinSpawn("goblin_spawn", "Goblin Spawn Alert", new DefaultAlertSound("random.levelup", 1f, 1.5f), new AlertTitle("Goblin", null, EnumChatFormatting.GREEN.toString(), null, 0, 30, 10));
	
	
	private static class DefaultAlertSound {
		public final String soundId;
		public final float volume;
		public final float pitch;
		
		public DefaultAlertSound(String soundId, float volume, float pitch) {
			this.soundId = soundId;
			this.volume = volume;
			this.pitch = pitch;
		}
	}
	
	
	public final String alertId;
	public final String alertName;
	
	private final DefaultAlertSound defaultSound;
	private final AlertTitle defaultTitle;
	
	private ISound lastSound = null;
	
	Alert(String alertId, String alertName, DefaultAlertSound defaultSound, AlertTitle defaultTitle) {
		this.alertId = alertId;
		this.alertName = alertName;
		
		this.defaultSound = defaultSound;
		this.defaultTitle = defaultTitle;
	}
	

	public void play() {
		play(null);
	}
	public void play(String details) {
		playSound();
		displayTitle(details);
	}
	
	
	public void playSound()
	{
		stopSound();
		
		ResourceLocation soundParentLocation = ScathaPro.getInstance().alertModeManager.getCurrentMode().getSoundBaseResourceLocation();
		if (soundParentLocation != null)
		{
			String soundPath = soundParentLocation.toString() + (soundParentLocation.getResourcePath().isEmpty() ? "" : ".") + alertId;
			if (SoundUtil.soundExists(soundPath)) {
				lastSound = SoundUtil.playSound(soundPath);
				return;
			}
		}
		
		lastSound = SoundUtil.playSound(defaultSound.soundId, defaultSound.volume, defaultSound.pitch);
	}
	
	public void stopSound()
	{
		if (isSoundPlaying()) Minecraft.getMinecraft().getSoundHandler().stopSound(lastSound);
	}
	
	public boolean isSoundPlaying()
	{
		return lastSound != null && Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(lastSound);
	}
	
	
	public void displayTitle(String details) {
		AlertTitle modeTitle = ScathaPro.getInstance().alertModeManager.getCurrentMode().getTitle(alertId);
		AlertTitle title = defaultTitle.apply(modeTitle, details);
		title.display();
	}
	
	public boolean hasTitle() {
		return defaultTitle.title != null;
	}
	
	public boolean hasSubtitle() {
		return defaultTitle.subtitle != null;
	}
	
	public AlertTitle getDefaultTitle() {
		return defaultTitle.apply(null, null);
	}
}
