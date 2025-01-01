package com.namelessju.scathapro.gui.menus;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.BooleanSettingButton;
import com.namelessju.scathapro.gui.elements.ScappaModeButton;
import com.namelessju.scathapro.gui.elements.SubMenuButton;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.util.TextUtil;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;

public class MiscSettingsGui extends ScathaProGui
{
    public MiscSettingsGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
    }

    @Override
    public String getTitle()
    {
        return "Miscellaneous Settings";
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        addGridButton(new BooleanSettingButton(1, 0, 0, 0, 0, "Drop Dry Streak Msg.", Config.Key.dryStreakMessage));
        
        addGridButton(new SubMenuButton(10, 0, 0, 0, 0, "Drop Message Extension...", this, DropMessageExtensionGui.class));
        
        BooleanSettingButton wormSpawnTimerMessageButton = new BooleanSettingButton(2, 0, 0, 0, 0, "Worm Spawn Timer Msg.", Config.Key.wormSpawnTimer);
        wormSpawnTimerMessageButton.getTooltip().setText(EnumChatFormatting.GRAY + "Sends a message with the elapsed time since the previous worm spawn when you spawn one");
        addGridButton(wormSpawnTimerMessageButton);
        
        BooleanSettingButton dailyStreakMessageButton = new BooleanSettingButton(8, 0, 0, 0, 0, "Daily Streak Message", Config.Key.dailyScathaFarmingStreakMessage);
        dailyStreakMessageButton.getTooltip().setText(EnumChatFormatting.GRAY + "Sends a message when your daily\nScatha farming streak changes");
        addGridButton(dailyStreakMessageButton);
        
        addGridButton(new BooleanSettingButton(12, 0, 0, 0, 0, "Hide Worm Approaching Message", Config.Key.hideWormSpawnMessage), GridElementMode.FULL_WIDTH);
        
        addGridGap();
        
        addGridButton(new BooleanSettingButton(7, 0, 0, 0, 0, "Short Chat Prefix", Config.Key.shortChatPrefix));
        
        BooleanSettingButton chatCopySettingButton = new BooleanSettingButton(6, 0, 0, 0, 0, "Chat Copy Button", Config.Key.chatCopy);
        chatCopySettingButton.getTooltip().setText(EnumChatFormatting.GRAY + "Adds a clickable icon behind each chat message that copies the message into the input field");
        addGridButton(chatCopySettingButton);
        
        BooleanSettingButton parseBestiaryButton = new BooleanSettingButton(5, 0, 0, 0, 0, "Read Bestiary Kills", Config.Key.automaticStatsParsing);
        parseBestiaryButton.getTooltip().setText(EnumChatFormatting.GRAY + "Automatically updates the overlay kill counters when you open the bestiary if they don't match");
        addGridButton(parseBestiaryButton);
        
        BooleanSettingButton highContrastColorsButton = new BooleanSettingButton(9, 0, 0, 0, 0, "High Contrast Colors", Config.Key.highContrastColors);
        highContrastColorsButton.getTooltip().setText(EnumChatFormatting.GRAY + "Turns gray overlay\nand title text to white");
        addGridButton(highContrastColorsButton);
        
        if (scathaPro.variables.scappaModeUnlocked)
        {
            addGridGap();
            ScappaModeButton scappaModeButton = new ScappaModeButton(11, 0, 0, 0, 0);
            scappaModeButton.getTooltip().setText(TextUtil.getRainbowText("Scappa"));
            addGridButton(scappaModeButton, GridElementMode.FULL_WIDTH);
        }
        
        addDoneButton();
    }
    
    @Override
    protected void actionPerformed(GuiButton button)
    {
        super.actionPerformed(button);
        
        switch (button.id)
        {
            case 9:
                scathaPro.getOverlay().updateContrast();
                break;
        }
    }
}
