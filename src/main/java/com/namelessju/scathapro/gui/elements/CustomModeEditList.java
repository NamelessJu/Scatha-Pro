package com.namelessju.scathapro.gui.elements;

import java.awt.FileDialog;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.Level;

import com.google.common.io.Files;
import com.google.gson.JsonObject;
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

public class CustomModeEditList extends ScathaProGuiList {

	private final CustomAlertModeManager customAlertModeManager;

	private final String customModeId;
	private final JsonObject modeProperties;
	
	private FileDialog activeFileDialog = null;
	
	public CustomModeEditList(GuiScreen gui, String customModeId) {
		super(gui, 85);
		
		customAlertModeManager = ScathaPro.getInstance().customAlertModeManager;
		
		this.customModeId = customModeId;
		modeProperties = customAlertModeManager.loadSubmodeProperties(customModeId);
		
		listEntries.add(new CustomModeNameEditEntry());
		
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
	}
	
	public boolean saveChanges() {
		boolean changesMade = false;
		for (ListEntry entry : listEntries) {
			if (entry instanceof ISaveableEntry) {
				if (((ISaveableEntry) entry).saveChanges()) changesMade = true;
			}
		}
		return changesMade;
	}

    @SideOnly(Side.CLIENT)
    public interface ISaveableEntry
    {
    	public boolean saveChanges();
    }
    
    @SideOnly(Side.CLIENT)
    public class CustomModeNameEditEntry extends ListEntry implements ISaveableEntry
    {
    	private String currentModeName;
    	private PlaceholderTextField nameTextField;
    	
        private CustomModeNameEditEntry()
        {
    		currentModeName = customAlertModeManager.getSubmodeName(customModeId);
    		if (currentModeName == null) currentModeName = "";
        	
            addLabel("Custom Mode Name", 0, 5, getListWidth(), 10);
            
            nameTextField = new PlaceholderTextField(0, mc.fontRendererObj, 0, 20, getListWidth(), 20);
            nameTextField.setText(currentModeName);
            nameTextField.setPlaceholder("<unnamed>");
        	addTextField(nameTextField);
        	
        }
		
		@Override
		public boolean saveChanges() {
			String newName = nameTextField.getText();
			if (newName == null) newName = "";
			else if (newName.replace(" ", "").isEmpty()) newName = "";
			
			if (!newName.equals(currentModeName)) {
				return true;
			}
			
			return false;
		}
    }
	
    @SideOnly(Side.CLIENT)
    public class AlertEditEntry extends ListEntry implements ISaveableEntry
    {
    	private final File alertAudioFile;
    	private final File audioResetFile = new File("");
    	
    	public final Alert alert;
    	
    	private final GuiButton audioFileButton;
    	private final GuiButton resetAudioButton;
    	private final PlaceholderTextField titleTextField;
    	private final PlaceholderTextField subtitleTextField;
    	
    	private File newSoundFile = null;
    	
        private AlertEditEntry(Alert alert)
        {
            this.alert = alert;
            alertAudioFile = CustomAlertModeManager.getAlertAudioFile(customModeId, alert);
            
            addLabel(alert.alertName, 0, 0, getListWidth(), 10);
            
            addButton(audioFileButton = new GuiButton(0, 0, 15, getListWidth() - 110, 20, "Choose audio..."));
            
            addButton(resetAudioButton = new GuiButton(2, getListWidth() - 105, 15, 50, 20, "Reset"));
            resetAudioButton.enabled = alertAudioFile.exists();
            
            GuiButton btnPlayAudio = new GuiButton(1, getListWidth() - 50, 15, 50, 20, "Play");
            btnPlayAudio.enabled = customModeId.equals(customAlertModeManager.getCurrentSubmodeId());
            addButton(btnPlayAudio);
            
            
            AlertTitle title = alert.getDefaultTitle();
        	String customTitle = JsonUtil.getString(modeProperties, "titles/" + alert.alertId + "/title");
        	String customSubtitle = JsonUtil.getString(modeProperties, "titles/" + alert.alertId + "/subtitle");
            addLabel(EnumChatFormatting.GRAY + "Title", 0, 40, getListWidth() / 2 - 5, 10);
            
            titleTextField = new PlaceholderTextField(0, mc.fontRendererObj, 0, 52, getListWidth() / 2 - 5, 20);
            if (customTitle != null) titleTextField.setText(customTitle);
            titleTextField.setPlaceholder(title.title != null ? title.title : EnumChatFormatting.ITALIC + "<none>");
            if (title.title == null) titleTextField.setEnabled(false);
        	addTextField(titleTextField);
        	
            addLabel(EnumChatFormatting.GRAY + "Subtitle", getListWidth() / 2 + 5, 40, getListWidth() / 2 - 5, 10);
            
            subtitleTextField = new PlaceholderTextField(0, mc.fontRendererObj, getListWidth() / 2 + 5, 52, getListWidth() / 2 - 5, 20);
            if (customSubtitle != null) subtitleTextField.setText(customSubtitle);
            subtitleTextField.setPlaceholder(title.subtitle != null ? title.subtitle : EnumChatFormatting.ITALIC + "<none>");
            if (title.subtitle == null) subtitleTextField.setEnabled(false);
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
							fileDialog.setFile("*.mp3;*.ogg");
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
								// TODO: bring MC window back to front 
								activeFileDialog = null;
							}
						}
					});
					thread.start();
	                break;
	            
				case 1:
	            	alert.playSound();
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
			audioFileButton.displayString = EnumChatFormatting.RESET + text + EnumChatFormatting.RESET + EnumChatFormatting.ITALIC + " (unsaved)";
		}
		
		@Override
		public boolean saveChanges() {
			boolean changesMade = false;
			if (newSoundFile != null)
			{
				if (newSoundFile == audioResetFile) {
					// ScathaPro.getInstance().customAlertModeManager.deleteResourceFileOnReload(alertAudioFile);
					
					if (alertAudioFile.exists()) {
						if (customAlertModeManager.getCurrentSubmodeId().equals(customModeId)) Minecraft.getMinecraft().getSoundHandler().unloadSounds();
						alertAudioFile.delete();
						changesMade = true;
					}
				}
				else {
					
					if (!newSoundFile.getName().endsWith(".ogg")) {
						// TODO: add .mp3 to .ogg converter
						ScathaPro.getInstance().logger.log(Level.WARN, alert.alertName + " audio file is not an .ogg file!");
						return false;
					}
					
					if (alertAudioFile.exists()) {
						if (customAlertModeManager.getCurrentSubmodeId().equals(customModeId)) Minecraft.getMinecraft().getSoundHandler().unloadSounds();
					}
					else {
						alertAudioFile.getParentFile().mkdirs();
					}
					
					try {
						Files.copy(newSoundFile, alertAudioFile);
						changesMade = true;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			return changesMade;
		}
    }

}
