package com.namelessju.scathapro.gui.menus;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.BooleanSettingButton;
import com.namelessju.scathapro.gui.elements.DoneButton;
import com.namelessju.scathapro.gui.elements.MultiOptionButton;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.miscellaneous.SecondaryKillCounterType;

import net.minecraft.client.gui.GuiScreen;

public class MiscSettingsGui extends ScathaProGui
{
    private final Config config;
    
    public MiscSettingsGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
        this.config = scathaPro.config;
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
        
        buttonList.add(new BooleanSettingButton(504704801, width / 2 - 155, height / 6 - 12, 150, 20, "Drop Dry Streak Msg", Config.Key.dryStreakMessage));
        buttonList.add(new BooleanSettingButton(504704802, width / 2 + 5, height / 6 - 12, 150, 20, "Worm Spawn Timer Msg", Config.Key.wormSpawnTimer));
        
        buttonList.add(new BooleanSettingButton(504704803, width / 2 - 155, height / 6 + 24 - 12, 150, 20, "Rotation Angles", Config.Key.showRotationAngles));
        buttonList.add(new MultiOptionButton<Integer>(504704806, width / 2 + 5, height / 6 + 24 - 12, 150, 20, "Rot. Angles Dec. Digits", MultiOptionButton.IntegerOption.range(0, 3), config.getInt(Config.Key.rotationAnglesDecimalDigits), new MultiOptionButton.IOptionChangedListener<Integer>() {
            @Override
            public void onChange(MultiOptionButton<Integer> button)
            {
                config.set(Config.Key.rotationAnglesDecimalDigits, button.getSelectedValue());
                config.save();
            }
        }));
        
        buttonList.add(new BooleanSettingButton(504704804, width / 2 - 155, height / 6 + 48 - 12, 150, 20, "Bestiary Kills Parsing", Config.Key.automaticStatsParsing));
        buttonList.add(new BooleanSettingButton(504704805, width / 2 + 5, height / 6 + 48 - 12, 150, 20, "Chat Copy Button", Config.Key.chatCopy));
        
        buttonList.add(new MultiOptionButton<Integer>(504704807, width / 2 - 155, height / 6 + 72 - 12, 150, 20, "Scatha % Dec. Digits", MultiOptionButton.IntegerOption.range(0, 3), config.getInt(Config.Key.scathaPercentageDecimalDigits), new MultiOptionButton.IOptionChangedListener<Integer>() {
            @Override
            public void onChange(MultiOptionButton<Integer> button)
            {
                config.set(Config.Key.scathaPercentageDecimalDigits, button.getSelectedValue());
                config.save();
                scathaPro.overlayManager.updateTotalKills();
            }
        }));
        buttonList.add(new MultiOptionButton<String>(504704808, width / 2 + 5, height / 6 + 72 - 12, 150, 20, "Secondary Kill Counter", SecondaryKillCounterType.values(), config.getString(Config.Key.secondaryKillCounterType), new MultiOptionButton.IOptionChangedListener<String>() {
            @Override
            public void onChange(MultiOptionButton<String> button)
            {
                config.set(Config.Key.secondaryKillCounterType, button.getSelectedValue());
                config.save();
                scathaPro.overlayManager.setSecondaryKillCounterType((SecondaryKillCounterType) button.getSelectedOption());
            }
        }));
        
        buttonList.add(new DoneButton(504704899, width / 2 - 100, height / 6 + 168, 200, 20, "Done", this));
    }
}
