package com.namelessju.scathapro.gui.menus;

import java.io.IOException;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.BooleanSettingButton;
import com.namelessju.scathapro.gui.elements.SubMenuButton;
import com.namelessju.scathapro.managers.Config;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class OverlayGeneralSettingsGui extends OverlaySettingsGui
{
    public OverlayGeneralSettingsGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
    }
    
    @Override
    public String getTitle()
    {
        return "Overlay Settings";
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        buttonList.add(new BooleanSettingButton(1, width / 2 - 155, height - 45 - 48 - 6, 150, 20, "UI Overlay", Config.Key.overlayEnabled));
        buttonList.add(new SubMenuButton(2, width / 2 + 5, height - 45 - 48 - 6, 150, 20, "Position...", this, OverlayPositionSettingsGui.class));
        buttonList.add(new SubMenuButton(3, width / 2 - 155, height - 45 - 24 - 6, 150, 20, "Components...", this, OverlayElementsSettingsGui.class));
        buttonList.add(new SubMenuButton(4, width / 2 + 5, height - 45 - 24 - 6, 150, 20, "Miscellaneous...", this, OverlayMiscSettingsGui.class));
        
        addDoneButton(width / 2 - 100, height - 45, 200, 20);
        
        overlay.updateOverlayFull();
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);
        
        if (button.enabled)
        {
            switch (button.id)
            {
                case 1:
                    overlay.updateVisibility();
                    break;
            }
        }
    }
}
