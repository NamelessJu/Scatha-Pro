package com.namelessju.scathapro.gui.menus;

import java.io.IOException;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.alerts.alertmodes.customalertmode.CustomAlertMode;
import com.namelessju.scathapro.gui.elements.AlertModeSettingButton;
import com.namelessju.scathapro.gui.elements.AlertsGuiList;
import com.namelessju.scathapro.gui.elements.DoneButton;
import com.namelessju.scathapro.gui.elements.SubMenuButton;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class AlertSettingsGui extends ScathaProGui
{
    private GuiButton alertModeSettingButton;
    private GuiButton customAlertModeEditButton;
    
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
        
        buttonList.add(alertModeSettingButton = new AlertModeSettingButton(504704401, width / 2 - 155, 35, 150, 20, "Alert Mode"));
        buttonList.add(customAlertModeEditButton = new SubMenuButton(504704402, width / 2 + 5, 35, 150, 20, "Custom Alert Modes...", this, CustomAlertModeGui.class));
        updateModeButtons();
        
        scrollList = new AlertsGuiList(this);
        
        buttonList.add(new DoneButton(504704499, this.width / 2 - 100, this.height - 30, 200, 20, "Done", this));
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);
        
        if (!button.enabled) return;
        
        switch (button.id)
        {
            case 504704401:
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
            /*
            If this is immediately done in actionPerformed(),
            the custom mode edit button might instantly get pressed
            if the mouse is over it while pressing the mode swapper button
            To fix this, the mode buttons are updated after all click checks are done
            */
            updateModeButtons();
            modeChanged = false;
        }
    }
    
    private void updateModeButtons()
    {
        if (scathaPro.getAlertModeManager().getCurrentMode() instanceof CustomAlertMode)
        {
            alertModeSettingButton.width = 150;
            customAlertModeEditButton.visible = true;
        }
        else
        {
            alertModeSettingButton.width = 310;
            customAlertModeEditButton.visible = false;
        }
    }
}
