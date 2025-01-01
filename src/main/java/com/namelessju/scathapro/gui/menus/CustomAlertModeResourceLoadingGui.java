package com.namelessju.scathapro.gui.menus;

import java.io.File;
import java.util.List;

import com.google.common.collect.Lists;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.alerts.alertmodes.customalertmode.ICustomAlertModeSaveable;
import com.namelessju.scathapro.managers.FFmpegWrapper;
import com.namelessju.scathapro.util.TextUtil;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;

public class CustomAlertModeResourceLoadingGui extends GuiScreen
{
    private final ScathaPro scathaPro = ScathaPro.getInstance();
    private final GuiScreen returnScreen;
    private String mainMessage = null;
    private String processMessage = null;
    private String progressMessage = null;
    private int ellipsisAnimationTicks = 0;

    private int state = -1;
    private ICustomAlertModeSaveable.SaveResults saveResults = null;
    private boolean forceResourceReload = false;
    
    private boolean conversionStarted = false;
    private int subMenuReturnState = -1;
    private int resourceReloadStartTimer = 0;

    private int currentConversionAudio = 0;
    private List<File> failedConversionFiles = Lists.newArrayList();

    public CustomAlertModeResourceLoadingGui(GuiScreen returnScreen)
    {
        this.returnScreen = returnScreen;
        forceResourceReload = true;
        setState(1);
    }
    
    public CustomAlertModeResourceLoadingGui(GuiScreen returnScreen, ICustomAlertModeSaveable.SaveResults saveResults)
    {
        this.returnScreen = returnScreen;

        if (saveResults != null)
        {
            this.saveResults = saveResults;
            if (saveResults.hasAudioConversions())
            {
                setState(0);
                return;
            }
        }
        
        setState(1);
    }
    
    @Override
    public void initGui()
    {
        if (subMenuReturnState >= 0)
        {
            setState(subMenuReturnState);
            subMenuReturnState = -1;
            return;
        }
        
        if (state == 0)
        {
            if (!FFmpegWrapper.isFFmpegInstalled())
            {
                state = -1;
                subMenuReturnState = 1;
                mc.displayGuiScreen(new InfoMessageGui(this, "FFmpeg not found", "No FFmpeg installation was found, which means that sounds cannot be converted to the required format (ogg).\n" + EnumChatFormatting.YELLOW + "You are only able to save files that are already in the ogg format!" + EnumChatFormatting.RESET + "\nClick \"Continue\" to acknowledge this and, if necessary, reload resources...", "Continue"));
                return;
            }
            
            convertNext();
        }
        
        if (ellipsisAnimationTicks >= 0) ellipsisAnimationTicks = 0;
    }
    
    private void convertNext()
    {
        if (saveResults == null || !saveResults.hasAudioConversions()) return;

        setState(0);
        
        ICustomAlertModeSaveable.SaveResults.AudioConversion[] audioConversions = saveResults.getAudioConversions();
        
        if (currentConversionAudio >= audioConversions.length)
        {
            progressMessage = EnumChatFormatting.GREEN.toString() + "Audio conversion finished";
            
            if (failedConversionFiles.size() > 0)
            {
                StringBuilder fileNamesString = new StringBuilder();
                for (File file : failedConversionFiles)
                {
                    if (fileNamesString.length() > 0) fileNamesString.append(", ");
                    fileNamesString.append(file.getName().replace(TextUtil.formattingStartCharacter, ""));
                }
                
                subMenuReturnState = 1;
                
                mc.displayGuiScreen(new InfoMessageGui(this, EnumChatFormatting.RED + "Audio conversions failed", EnumChatFormatting.RESET + "The following files couldn't be converted to .ogg:\n" + fileNamesString.toString() + "\n\n" + EnumChatFormatting.GRAY + "Make sure that your FFmpeg installation includes the libvorbis encoder!"));
                return;
            }
            
            setState(1);
            return;
        }
        else progressMessage = EnumChatFormatting.YELLOW.toString() + currentConversionAudio + "/" + audioConversions.length + " audio file" + (audioConversions.length > 1 ? "s" : "") + " processed";
        
        ICustomAlertModeSaveable.SaveResults.AudioConversion currentConversion = audioConversions[currentConversionAudio];
        if (currentConversion.isValid())
        {
            String sourceFileName = currentConversion.from.getName().replace(TextUtil.formattingStartCharacter, "");
            processMessage = "Converting \"" + sourceFileName + "\"";
            
            final int i = currentConversionAudio;
            FFmpegWrapper.convertToOgg(currentConversion.from.getAbsolutePath(), currentConversion.to.getAbsolutePath(), (value) -> {
                String msgBase = "Audio conversion at index " + i + " (\"" + sourceFileName + "\") ";
                if (value) scathaPro.logDebug(msgBase + "successful");
                else
                {
                    failedConversionFiles.add(currentConversion.from);
                    scathaPro.logError(msgBase + "FAILED");
                }
                convertNext();
            });
            
            currentConversionAudio ++;
        }
        else
        {
            scathaPro.logError("Audio conversion at index " + currentConversionAudio + " isn't valid");
            currentConversionAudio ++;
            convertNext();
        }
    }
    
    @Override
    public void updateScreen()
    {
        switch (state)
        {
            case 0:
                ellipsisAnimationTicks ++;
                if (ellipsisAnimationTicks >= 40)
                {
                    ellipsisAnimationTicks = ellipsisAnimationTicks % 40;
                }
                break;
                
            case 1:
                resourceReloadStartTimer ++;
                if (resourceReloadStartTimer < 5) break;
                
                scathaPro.getCustomAlertModeManager().reloadResourcePack();
                mc.displayGuiScreen(returnScreen);
                setState(2);
                break;
        }
    }
    
    private void setState(int state)
    {
        switch (state)
        {
            case 0:
                if (conversionStarted) break;
                conversionStarted = true;
                
                mainMessage = "Converting audio";
                currentConversionAudio = 0;
                failedConversionFiles.clear();
                break;
                
            case 1:
                
                
                if (!forceResourceReload && (saveResults == null || !saveResults.isResourceReloadRequired()))
                {
                    setState(2);
                    return;
                }
                
                mainMessage = "Reloading resources";
                processMessage = "Game is frozen while resources are loading, please wait!";
                resourceReloadStartTimer = 0;
                ellipsisAnimationTicks = -1;
                break;
                
            case 2:
                mc.displayGuiScreen(returnScreen);
                return;
        }
        this.state = state;
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        
        if (!StringUtils.isNullOrEmpty(mainMessage))
        {
            String ellipsis;
            if (ellipsisAnimationTicks >= 0)
            {
                ellipsis = "";
                for (int i = 10; i <= ellipsisAnimationTicks; i += 10) ellipsis += '.';
            }
            else
            {
                ellipsis = "...";
            }
            this.drawCenteredString(fontRendererObj, mainMessage + ellipsis, this.width / 2, this.height / 2 - 25, 0xFFFFFF);
        }
        if (!StringUtils.isNullOrEmpty(processMessage)) this.drawCenteredString(fontRendererObj, EnumChatFormatting.GRAY + processMessage, this.width / 2, this.height / 2 - 10, 0xFFFFFF);
        if (!StringUtils.isNullOrEmpty(progressMessage)) this.drawCenteredString(fontRendererObj, EnumChatFormatting.GRAY + progressMessage, this.width / 2, this.height / 2 + 15, 0xFFFFFF);
    }
}
