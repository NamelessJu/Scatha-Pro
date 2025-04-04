package com.namelessju.scathapro.gui.menus.alerts;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.alerts.alertmodes.AlertMode;
import com.namelessju.scathapro.alerts.alertmodes.AlertModeButtonOption;
import com.namelessju.scathapro.alerts.alertmodes.customalertmode.CustomAlertMode;
import com.namelessju.scathapro.gui.elements.CycleButton;
import com.namelessju.scathapro.gui.elements.CycleButton.IOptionChangedListener;
import com.namelessju.scathapro.gui.menus.CustomAlertModeGui;
import com.namelessju.scathapro.gui.menus.ScathaProGui;
import com.namelessju.scathapro.gui.elements.ScathaProButton;
import com.namelessju.scathapro.gui.elements.SubMenuButton;
import com.namelessju.scathapro.managers.Config;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;

public class AlertSettingsGui extends ScathaProGui
{
    private ScathaProButton customAlertModeEditButton;
    
    public AlertSettingsGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
    }
    
    @Override
    public String getTitle()
    {
        return "Alert Settings";
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        CycleButton<AlertMode> modeButton = new CycleButton<AlertMode>(1, 0, 0, 0, 0, "Alert Mode", AlertModeButtonOption.getAllOptions(), scathaPro.getAlertModeManager().getCurrentMode(), new IOptionChangedListener<AlertMode>() {
            @Override
            public void onChange(CycleButton<AlertMode> button)
            {
                Config config = scathaPro.getConfig();
                config.set(Config.Key.mode, button.getSelectedValue().id);
                config.save();
                
                scathaPro.getOverlay().updateScathaPetImage();
            }
        });
        modeButton.getTooltip().setText(EnumChatFormatting.GRAY + "Each mode plays different sounds\n(and titles in custom mode)");
        addGridButton(modeButton);
        addGridButton(customAlertModeEditButton = new SubMenuButton(2, 0, 0, 0, 0, "Custom Alert Modes...", this, CustomAlertModeGui.class));
        updateModeButtons();
        
        addGridGap();
        
        addGridButton(new SubMenuButton(5, 0, 0, 0, 0, "Enable/Disable Alerts...", this, AlertStatesGui.class));
        addGridButton(new SubMenuButton(4, 0, 0, 0, 0, "Alert Configuration...", this, AlertConfigurationGui.class));
        addGridButton(new SubMenuButton(3, 0, 0, 0, 0, "Alert Title Position...", this, AlertTitleSettingsGui.class));
        
        addDoneButton();
    }
    
    @Override
    protected void actionPerformed(GuiButton button)
    {
        super.actionPerformed(button);
        
        switch (button.id)
        {
            case 1:
                updateModeButtons();
                break;
        }
    }
    
    private void updateModeButtons()
    {
        if (scathaPro.getAlertModeManager().getCurrentMode() instanceof CustomAlertMode)
        {
            customAlertModeEditButton.enabled = true;
            customAlertModeEditButton.getTooltip().setText(null);
        }
        else
        {
            customAlertModeEditButton.enabled = false;
            customAlertModeEditButton.getTooltip().setText(EnumChatFormatting.YELLOW + "Select \"Custom\" to access custom alert mode settings", 150);
        }
    }
}
