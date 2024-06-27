package com.namelessju.scathapro.gui.menus;

import java.io.IOException;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.BooleanSettingButton;
import com.namelessju.scathapro.managers.Config;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class AchievementSettingsGui extends ScathaProGui
{
    @Override
    public String getTitle()
    {
        return "Achievement Settings";
    }
    
    public AchievementSettingsGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        addGridButton(new BooleanSettingButton(1, 0, 0, 0, 0, "Show Non-Unlocked Bonus Achievements", Config.Key.bonusAchievementsShown), true);
        addGridButton(new BooleanSettingButton(2, 0, 0, 0, 0, "Hide Unlocked Achievements", Config.Key.hideUnlockedAchievements), true);
        addGridGap();
        addGridButton(new BooleanSettingButton(3, 0, 0, 0, 0, "Achievement Unlock Alerts", Config.Key.playAchievementAlerts), true);
        
        addDoneButton();
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);
        
        if (button.enabled && button.id == 1)
        {
            scathaPro.getAchievementManager().updateBonusTypeVisibility();
        }
    }
    
}
