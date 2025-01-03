package com.namelessju.scathapro.gui.menus;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.BooleanSettingButton;
import com.namelessju.scathapro.managers.Config;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;

public class AchievementSettingsGui extends ScathaProGui
{
    private BooleanSettingButton unlockAlertsButton, repeatUnlockAlertsButton;
    
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
        
        addGridButton(new BooleanSettingButton(1, 0, 0, 0, 0, "Show Non-Unlocked Bonus Achievements", Config.Key.bonusAchievementsShown), GridElementMode.FULL_WIDTH);
        addGridButton(new BooleanSettingButton(2, 0, 0, 0, 0, "Hide Unlocked Achievements", Config.Key.hideUnlockedAchievements), GridElementMode.FULL_WIDTH);
        addGridButton(new BooleanSettingButton(5, 0, 0, 0, 0, "Show Repeat Counts And Progress", Config.Key.repeatCountsShown), GridElementMode.FULL_WIDTH);
        addGridGap();
        addGridButton(unlockAlertsButton = new BooleanSettingButton(3, 0, 0, 0, 0, "Achievement Unlock Alerts", Config.Key.playAchievementAlerts), GridElementMode.FULL_WIDTH);
        addGridButton(repeatUnlockAlertsButton = new BooleanSettingButton(4, 0, 0, 0, 0, "Achievement Repeat Unlock Alerts", Config.Key.playRepeatAchievementAlerts), GridElementMode.FULL_WIDTH);
        
        addDoneButton();
        
        updateRepeatUnlockAlertsButton();
    }
    
    @Override
    protected void actionPerformed(GuiButton button)
    {
        super.actionPerformed(button);
        
        switch (button.id)
        {
            case 1:
                scathaPro.getAchievementManager().updateBonusTypeVisibility();
                break;
            case 3:
            case 4:
                updateRepeatUnlockAlertsButton();
                break;
        }
    }
    
    private void updateRepeatUnlockAlertsButton()
    {
        boolean generalAlertsEnabled = unlockAlertsButton.isSettingEnabled();
        repeatUnlockAlertsButton.enabled = generalAlertsEnabled;
        repeatUnlockAlertsButton.getTooltip().setText(generalAlertsEnabled ? null : EnumChatFormatting.YELLOW + "Requires generic achievement\n" + EnumChatFormatting.YELLOW + "unlock alerts to be enabled");
    }
}
