package com.namelessju.scathapro.gui.menus;

import java.io.IOException;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.alerts.alertmodes.AlertMode;
import com.namelessju.scathapro.alerts.alertmodes.AlertModeButtonOption;
import com.namelessju.scathapro.alerts.alertmodes.customalertmode.CustomAlertMode;
import com.namelessju.scathapro.gui.elements.CycleButton;
import com.namelessju.scathapro.gui.elements.CycleButton.IOptionChangedListener;
import com.namelessju.scathapro.gui.elements.ScathaProButton;
import com.namelessju.scathapro.gui.elements.SubMenuButton;
import com.namelessju.scathapro.gui.lists.AlertsGuiList;
import com.namelessju.scathapro.managers.Config;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;

public class AlertSettingsGui extends ScathaProGui
{
    private ScathaProButton customAlertModeEditButton;
    
    private boolean modeChanged = false;
    
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
        
        elements.add(new CycleButton<AlertMode>(1, width / 2 - 155, 35, 150, 20, "Alert Mode", AlertModeButtonOption.getAllOptions(), scathaPro.getAlertModeManager().getCurrentMode(), new IOptionChangedListener<AlertMode>() {
            @Override
            public void onChange(CycleButton<AlertMode> button)
            {
                Config config = scathaPro.getConfig();
                config.set(Config.Key.mode, button.getSelectedValue().id);
                config.save();
            }
        }));
        elements.add(customAlertModeEditButton = new SubMenuButton(2, width / 2 + 5, 35, 150, 20, "Custom Alert Modes...", this, CustomAlertModeGui.class));
        updateModeButtons();
        
        scrollList = new AlertsGuiList(this);
        
        addDoneButton(this.width / 2 - 100, this.height - 30, 200, 20);
    }
    
    @Override
    protected void actionPerformed(GuiButton button)
    {
        super.actionPerformed(button);
        
        switch (button.id)
        {
            case 1:
                modeChanged = true;
                break;
        }
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        
        if (modeChanged)
        {
            updateModeButtons();
            modeChanged = false;
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
