package com.namelessju.scathapro.gui.elements;

import java.awt.FileDialog;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.Level;

import com.google.common.io.Files;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.alertmodes.Alert;
import com.namelessju.scathapro.alertmodes.AlertTitle;
import com.namelessju.scathapro.alertmodes.customalertmode.CustomAlertModeManager;
import com.namelessju.scathapro.util.JsonUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CustomAlertModeEditGuiList extends ScathaProGuiList
{
	@Override
	protected boolean areEntriesSelectable() {
		return false;
	}

	private final CustomAlertModeManager customAlertModeManager;

	private final String customModeId;
	private final JsonObject modeProperties;
	
	private FileDialog activeFileDialog = null;
	
	public CustomAlertModeEditGuiList(GuiScreen gui, String customModeId) {
		super(gui, 75, gui.height - 40, 85);
		
		customAlertModeManager = ScathaPro.getInstance().customAlertModeManager;
		
		this.customModeId = customModeId;
		modeProperties = customAlertModeManager.loadSubmodeProperties(customModeId);
		
		Alert[] alerts = Alert.values();
		for (Alert alert : alerts) {
			listEntries.add(new AlertEditEntry(alert));
		}
	}
	
	public void resize() {
        this.width = gui.width;
        this.height = gui.height;
        this.bottom = gui.height - 32;
        this.right = this.width;
	}
	
	public void onGuiClosed() {
		if (activeFileDialog != null) {
			activeFileDialog.setVisible(false);
		}

		Alert[] alerts = Alert.values();
		for (Alert alert : alerts) {
			alert.stopSound();
		}
	}
	
	public boolean saveChanges() {
		boolean reloadRequired = false;
		for (ListEntry entry : listEntries) {
			if (entry instanceof ISaveableEntry) {
				if (((ISaveableEntry) entry).saveChanges()) reloadRequired = true;
			}
		}
		return reloadRequired;
	}

    @SideOnly(Side.CLIENT)
    public interface ISaveableEntry
    {
    	/**
    	 * @return <code>boolean</code> Whether reloading resources is required
    	 */
    	public boolean saveChanges();
    }
	
    @SideOnly(Side.CLIENT)
    public class AlertEditEntry extends ListEntry implements ISaveableEntry
    {
    	private final File alertAudioFile;
    	private final File audioResetFile = new File("");
    	
    	public final Alert alert;
    	
    	private final GuiButton audioFileButton;
    	private final GuiButton resetAudioButton;
    	private final ScathaProTextField titleTextField;
    	private final ScathaProTextField subtitleTextField;

    	private String initialTitle;
    	private String initialSubtitle;
    	
    	private File newSoundFile = null;
    	
        private AlertEditEntry(Alert alert)
        {
            this.alert = alert;
            alertAudioFile = CustomAlertModeManager.getAlertAudioFile(customModeId, alert);
            
            addLabel(alert.alertName, 0, 5, getListWidth(), 10);
            
            addButton(audioFileButton = new GuiButton(0, 0, 20, getListWidth() - 110, 20, "Choose audio..."));
            
            addButton(resetAudioButton = new GuiButton(2, getListWidth() - 105, 20, 50, 20, "Reset"));
            resetAudioButton.enabled = alertAudioFile.exists();
            
            GuiButton btnPlayAudio = new GuiButton(1, getListWidth() - 50, 20, 50, 20, "Play");
            btnPlayAudio.enabled = customModeId.equals(customAlertModeManager.getCurrentSubmodeId());
            addButton(btnPlayAudio);
            
            
            AlertTitle defaultTitle = alert.getDefaultTitle();
            boolean canEditTitle = defaultTitle.title != null;
            boolean canEditSubtitle = defaultTitle.subtitle != null;
            initialTitle = JsonUtil.getString(modeProperties, "titles/" + alert.alertId + "/title");
            if (initialTitle == null) initialTitle = "";
            initialSubtitle = JsonUtil.getString(modeProperties, "titles/" + alert.alertId + "/subtitle");
            if (initialSubtitle == null) initialSubtitle = "";
            
            addLabel((canEditTitle ? EnumChatFormatting.GRAY : EnumChatFormatting.DARK_GRAY) + "Title", 0, 45, getListWidth() / 2 - 5, 10);
            
            titleTextField = new ScathaProTextField(0, mc.fontRendererObj, 0, 55, getListWidth() / 2 - 5, 20);
            if (canEditTitle) titleTextField.setText(initialTitle);
            else titleTextField.setEnabled(false);
            titleTextField.setPlaceholder(canEditTitle ? defaultTitle.title : EnumChatFormatting.ITALIC + "(cannot set)");
        	addTextField(titleTextField);
        	
            addLabel((canEditSubtitle ? EnumChatFormatting.GRAY : EnumChatFormatting.DARK_GRAY) + "Subtitle", getListWidth() / 2 + 5, 45, getListWidth() / 2 - 5, 10);
            
            subtitleTextField = new ScathaProTextField(0, mc.fontRendererObj, getListWidth() / 2 + 5, 55, getListWidth() / 2 - 5, 20);
            if (canEditSubtitle) subtitleTextField.setText(initialSubtitle);
            else subtitleTextField.setEnabled(false);
            subtitleTextField.setPlaceholder(canEditSubtitle ? defaultTitle.subtitle : EnumChatFormatting.ITALIC + "(cannot set)");
        	addTextField(subtitleTextField);
        }
        
		@Override
		protected void onButtonPressed(GuiButton button) {
			switch (button.id) {
				case 0:
					if (activeFileDialog != null) {
						activeFileDialog.setVisible(false);
					}
					
					Thread thread = new Thread(new Runnable() {
						@Override
						public void run() {
							FileDialog fileDialog = new FileDialog((java.awt.Frame) null);
							fileDialog.setMultipleMode(false);
							
							boolean mp3Supported = false;
							fileDialog.setFile((mp3Supported ? "*.mp3;" : "") + "*.ogg");
							
							fileDialog.setTitle("Select " + alert.alertName + " audio...");
							try {
								InputStream iconInputStream = Minecraft.getMinecraft().mcDefaultResourcePack.getInputStreamAssets(new ResourceLocation("icons/icon_32x32.png")); 
								fileDialog.setIconImage(TextureUtil.readBufferedImage(iconInputStream));
							} catch (Exception e) {}
							
							activeFileDialog = fileDialog;
							
							fileDialog.setVisible(true);
							
							File[] selectedFiles = fileDialog.getFiles();
							if (selectedFiles.length > 0) {
								onAudioFileChosen(selectedFiles[0]);
							}
							if (activeFileDialog == fileDialog) {
								activeFileDialog = null;
							}
						}
					});
					thread.start();
	                break;
	            
				case 1:
					if (alert.isSoundPlaying()) alert.stopSound();
					else alert.playSound();
	                break;
	            
				case 2:
					resetSound();
					break;
			}
		}
		
		private void onAudioFileChosen(File file) {
			newSoundFile = file;
			updateAudioFileButton(file.getName());
			resetAudioButton.enabled = true;
		}
		
		private void resetSound() {
			newSoundFile = audioResetFile;
			updateAudioFileButton(EnumChatFormatting.ITALIC + "Default");
			resetAudioButton.enabled = false;
		}
		
		private void updateAudioFileButton(String text) {
			audioFileButton.displayString = EnumChatFormatting.RESET + text + EnumChatFormatting.RESET + EnumChatFormatting.YELLOW + EnumChatFormatting.ITALIC + " (unsaved)";
		}
		
		@Override
		public boolean saveChanges()
		{
			boolean reloadRequired = false;
			
			// Title
			
			boolean titleChanged = false;
			
			String newTitle = titleTextField.getText().trim();
			if (alert.hasTitle() && !newTitle.equals(initialTitle)) {
				JsonUtil.set(modeProperties, "titles/" + alert.alertId + "/title", new JsonPrimitive(newTitle));
				titleChanged = true;
			}
			
			String newSubtitle = subtitleTextField.getText().trim();
			if (alert.hasSubtitle() && !newSubtitle.equals(initialSubtitle)) {
				JsonUtil.set(modeProperties, "titles/" + alert.alertId + "/subtitle", new JsonPrimitive(newSubtitle));
				titleChanged = true;
			}
			
			if (titleChanged) {
				customAlertModeManager.saveSubmodeProperties(customModeId, modeProperties);
				if (customAlertModeManager.isSubmodeActive(customModeId)) customAlertModeManager.loadCurrentSubmodeProperties();
			}
			
			// Sound
			
			if (newSoundFile != null)
			{
				if (newSoundFile == audioResetFile) {
					if (alertAudioFile.exists()) {
						if (customAlertModeManager.isSubmodeActive(customModeId)) Minecraft.getMinecraft().getSoundHandler().unloadSounds();
						alertAudioFile.delete();
						reloadRequired = true;
					}
				}
				else {
					
					if (!newSoundFile.getName().endsWith(".ogg")) {
						// TODO: add .mp3 to .ogg converter
						ScathaPro.getInstance().logger.log(Level.WARN, alert.alertName + " audio file is not an .ogg file!");
						return false;
					}
					
					if (alertAudioFile.exists()) {
						if (customAlertModeManager.isSubmodeActive(customModeId)) Minecraft.getMinecraft().getSoundHandler().unloadSounds();
					}
					else {
						alertAudioFile.getParentFile().mkdirs();
					}
					
					try {
						Files.copy(newSoundFile, alertAudioFile);
						reloadRequired = true;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			return reloadRequired;
		}
    }

}
