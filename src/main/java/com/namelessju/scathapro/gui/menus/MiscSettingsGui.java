package com.namelessju.scathapro.gui.menus;

import java.io.IOException;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.BooleanSettingButton;
import com.namelessju.scathapro.gui.elements.MultiOptionButton;
import com.namelessju.scathapro.managers.Config;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class MiscSettingsGui extends ScathaProGui
{
    private final Config config;
    
    public MiscSettingsGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
        this.config = scathaPro.getConfig();
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
        
        addGridButton(new BooleanSettingButton(3, 0, 0, 0, 0, "Rotation Angles", Config.Key.showRotationAngles));
        addGridButton(new BooleanSettingButton(6, 0, 0, 0, 0, "Chat Copy Button", Config.Key.chatCopy));
        addGridButton(new MultiOptionButton<Integer>(4, 0, 0, 0, 0, "Rot. Angles Dec. Places", MultiOptionButton.IntegerOption.range(0, 3), config.getInt(Config.Key.rotationAnglesDecimalDigits), new MultiOptionButton.IOptionChangedListener<Integer>() {
            @Override
            public void onChange(MultiOptionButton<Integer> button)
            {
                config.set(Config.Key.rotationAnglesDecimalDigits, button.getSelectedValue());
                config.save();
            }
        }));
        addGridButton(new BooleanSettingButton(5, 0, 0, 0, 0, "Read Bestiary Kills", Config.Key.automaticStatsParsing));
        addGridGap();
        addGridButton(new BooleanSettingButton(1, 0, 0, 0, 0, "Drop Dry Streak Msg.", Config.Key.dryStreakMessage));
        addGridButton(new BooleanSettingButton(2, 0, 0, 0, 0, "Worm Spawn Timer Msg.", Config.Key.wormSpawnTimer));
        addGridButton(new BooleanSettingButton(8, 0, 0, 0, 0, "Daily Scatha Farming Streak Message", Config.Key.dailyScathaFarmingStreakMessage), true);
        addGridGap();
        addGridButton(new BooleanSettingButton(7, 0, 0, 0, 0, "Short Chat Prefix", Config.Key.shortChatPrefix));
        addGridButton(new BooleanSettingButton(8, 0, 0, 0, 0, "High Contrast Colors", Config.Key.highContrastColors));
        
        addDoneButton();
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);
        
        if (button.enabled && button.id == 8)
        {
            scathaPro.getOverlay().updateContrast();
        }
    }
}
