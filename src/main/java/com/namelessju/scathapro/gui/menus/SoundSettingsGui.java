package com.namelessju.scathapro.gui.menus;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.BooleanSettingButton;
import com.namelessju.scathapro.gui.elements.DoneButton;
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
        
        buttonList.add(new GuiSlider(504704701, width / 2 - 155, height / 6 - 12, 310, 20, ScathaPro.MODNAME + " Sounds Volume: ", "%", 0, 100, scathaPro.config.getDouble(Config.Key.soundsVolume) * 100, false, true, this));
        buttonList.add(new BooleanSettingButton(504704702, width / 2 - 155, height / 6 + 24 - 12, 310, 20, "Mute Crystal Hollows sounds", Config.Key.muteOtherSounds));
        
        buttonList.add(new DoneButton(504704799, width / 2 - 100, height / 6 + 168, 200, 20, "Done", this));
    }

    @Override
    public void onChangeSliderValue(GuiSlider slider)
    {
        if (slider.enabled)
        {
            switch (slider.id)
            {
                case 504704701:
                    double volume = (double) slider.getValueInt() / 100;
                    
                    Config config = scathaPro.config;
                    config.set(Config.Key.soundsVolume, volume);
                    config.save();
                    break;
            }
        }
    }
}
