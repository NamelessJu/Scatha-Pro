package com.namelessju.scathapro.gui.menus;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.BooleanSettingButton;
import com.namelessju.scathapro.gui.elements.SubMenuButton;
import com.namelessju.scathapro.gui.menus.alerts.AlertSettingsGui;
import com.namelessju.scathapro.gui.menus.overlay.OverlayGeneralSettingsGui;
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
        
        addGridButton(new SubMenuButton(1, 0, 0, 0, 0, "Overlay...", this, OverlayGeneralSettingsGui.class));
        addGridButton(new SubMenuButton(2, 0, 0, 0, 0, "Alerts...", this, AlertSettingsGui.class));
        addGridButton(new SubMenuButton(7, 0, 0, 0, 0, "Pitch/Yaw Display...", this, RotationAnglesSettingsGui.class));
        addGridButton(new SubMenuButton(9, 0, 0, 0, 0, "Drop Message Extension...", this, DropMessageExtensionGui.class));
        addGridButton(new SubMenuButton(10, 0, 0, 0, 0, "Chat Messages...", this, ChatMessageSettingsGui.class));
        addGridButton(new SubMenuButton(3, 0, 0, 0, 0, "Sounds...", this, SoundSettingsGui.class));
        addGridButton(new SubMenuButton(8, 0, 0, 0, 0, "Key Bindings...", this, KeybindingsGui.class));
        addGridButton(new SubMenuButton(4, 0, 0, 0, 0, "Miscellaneous...", this, MiscSettingsGui.class));
        addGridGap();
        addGridButton(new BooleanSettingButton(5, 0, 0, 0, 0, "Autom. Update Checks", Config.Key.automaticUpdateChecks));
        addGridButton(new BooleanSettingButton(6, 0, 0, 0, 0, "Automatic Backups", Config.Key.automaticBackups));
        
        addDoneButton();
    }
}
