package com.namelessju.scathapro.gui.menus;

import java.io.IOException;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.BooleanSettingButton;
import com.namelessju.scathapro.managers.Config;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

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
        
        addGridButton(new BooleanSettingButton(6, 0, 0, 0, 0, "Chat Copy Button", Config.Key.chatCopy));
        addGridButton(new BooleanSettingButton(5, 0, 0, 0, 0, "Read Bestiary Kills", Config.Key.automaticStatsParsing));
        addGridGap();
        addGridButton(new BooleanSettingButton(1, 0, 0, 0, 0, "Drop Dry Streak Msg.", Config.Key.dryStreakMessage));
        addGridButton(new BooleanSettingButton(10, 0, 0, 0, 0, "Add Rarity To Drop Msg.", Config.Key.scathaPetDropMessageExtension));
        addGridButton(new BooleanSettingButton(2, 0, 0, 0, 0, "Worm Spawn Timer Msg.", Config.Key.wormSpawnTimer));
        addGridButton(new BooleanSettingButton(8, 0, 0, 0, 0, "Daily Streak Message", Config.Key.dailyScathaFarmingStreakMessage));
        addGridGap();
        addGridButton(new BooleanSettingButton(7, 0, 0, 0, 0, "Short Chat Prefix", Config.Key.shortChatPrefix));
        addGridButton(new BooleanSettingButton(9, 0, 0, 0, 0, "High Contrast Colors", Config.Key.highContrastColors));
        
        addDoneButton();
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);
        
        if (button.enabled && button.id == 9)
        {
            scathaPro.getOverlay().updateContrast();
        }
    }
}
