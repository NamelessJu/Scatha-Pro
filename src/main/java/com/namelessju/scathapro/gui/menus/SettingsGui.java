package com.namelessju.scathapro.gui.menus;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.BooleanSettingButton;
import com.namelessju.scathapro.gui.elements.DoneButton;
import com.namelessju.scathapro.gui.elements.SubMenuButton;
import com.namelessju.scathapro.managers.Config;

import net.minecraft.client.gui.GuiScreen;

public class SettingsGui extends ScathaProGui
{
    public SettingsGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
    }

    @Override
    public String getTitle()
    {
        return "Settings";
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        buttonList.add(new BooleanSettingButton(504704001, width / 2 - 155, height / 6 - 12, 150, 20, "Autom. Update Checks", Config.Key.automaticUpdateChecks));
        buttonList.add(new BooleanSettingButton(504704002, width / 2 + 5, height / 6 - 12, 150, 20, "Automatic Backups", Config.Key.automaticBackups));
        
        buttonList.add(new SubMenuButton(504704003, width / 2 - 155, height / 6 + 48 - 6, 150, 20, "Overlay...", this, OverlaySettingsGui.class));
        buttonList.add(new SubMenuButton(504704004, width / 2 + 5, height / 6 + 48 - 6, 150, 20, "Alerts...", this, AlertSettingsGui.class));
        buttonList.add(new SubMenuButton(504704005, width / 2 - 155, height / 6 + 72 - 6, 150, 20, "Sounds...", this, SoundSettingsGui.class));
        buttonList.add(new SubMenuButton(504704006, width / 2 + 5, height / 6 + 72 - 6, 150, 20, "Miscellaneous...", this, MiscSettingsGui.class));
        
        buttonList.add(new DoneButton(504704099, width / 2 - 100, height / 6 + 168, 200, 20, "Done", this));
    }
}
