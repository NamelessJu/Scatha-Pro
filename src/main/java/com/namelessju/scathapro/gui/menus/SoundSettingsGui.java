package com.namelessju.scathapro.gui.menus;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.BooleanSettingButton;
import com.namelessju.scathapro.managers.Config;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiSlider;

public class SoundSettingsGui extends ScathaProGui implements GuiSlider.ISlider
{
    public SoundSettingsGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
    }

    @Override
    public String getTitle()
    {
        return "Sound Settings";
    }

    @Override
    public void initGui()
    {
        super.initGui();
        
        addGridButton(new GuiSlider(1, 0, 0, 0, 0, ScathaPro.MODNAME + " Sounds Volume: ", "%", 0, 100, scathaPro.getConfig().getDouble(Config.Key.soundsVolume) * 100, false, true, this), true);
        addGridButton(new BooleanSettingButton(2, 0, 0, 0, 0, "Mute Crystal Hollows Sounds", Config.Key.muteCrystalHollowsSounds), true);
        
        addDoneButton();
    }

    @Override
    public void onChangeSliderValue(GuiSlider slider)
    {
        if (slider.enabled)
        {
            switch (slider.id)
            {
                case 1:
                    double volume = (double) slider.getValueInt() / 100;
                    
                    Config config = scathaPro.getConfig();
                    config.set(Config.Key.soundsVolume, volume);
                    config.save();
                    break;
            }
        }
    }
}
