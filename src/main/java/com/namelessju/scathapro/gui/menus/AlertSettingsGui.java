package com.namelessju.scathapro.gui.menus;

import java.io.IOException;

import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.BooleanSettingButton;
import com.namelessju.scathapro.gui.elements.DoneButton;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiSlider;

public class AlertSettingsGui extends ScathaProGui implements GuiSlider.ISlider {
    
    @Override
    public String getTitle() {
        return "Alert Settings";
    }
    

    public AlertSettingsGui(GuiScreen parentGui) {
        super(parentGui);
    }

    @Override
    public void initGui() {
        super.initGui();
        
        double volume = Config.instance.getDouble(Config.Key.alertVolume);
        buttonList.add(new GuiSlider(504704401, width / 2 - 155, height / 6 - 12, 310, 20, "Alert Volume: ", "%", 0, 100, volume * 100, false, true, this));

        buttonList.add(new BooleanSettingButton(504704404, width / 2 - 155, height / 6 + 48 - 6, 150, 20, "Worm Pre-Spawn Alert", Config.Key.wormPreAlert));
        buttonList.add(new BooleanSettingButton(504704402, width / 2 + 5, height / 6 + 48 - 6, 150, 20, "Worm Spawn Alert", Config.Key.wormAlert));
        buttonList.add(new BooleanSettingButton(504704403, width / 2 - 155, height / 6 + 72 - 6, 150, 20, "Scatha Spawn Alert", Config.Key.scathaAlert));
        buttonList.add(new BooleanSettingButton(504704405, width / 2 + 5, height / 6 + 72 - 6, 150, 20, "Bedrock Wall Alert", Config.Key.wallAlert));
        buttonList.add(new BooleanSettingButton(504704406, width / 2 - 155, height / 6 + 96 - 6, 150, 20, "Scatha Pet Drop Alert", Config.Key.petAlert));
        buttonList.add(new BooleanSettingButton(504704407, width / 2 + 5, height / 6 + 96 - 6, 150, 20, "Goblin Spawn Alert", Config.Key.goblinAlert));
        
        buttonList.add(new DoneButton(504704499, width / 2 - 100, height / 6 + 168, 200, 20, "Done", this));
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
    	super.actionPerformed(button);
    	
        if (button.enabled) {
            switch (button.id) {
            	
                case 504704406:
                    if (Config.instance.getBoolean(Config.Key.petAlert)) {
                    	ScathaPro.getInstance().resetPreviousScathaPets();
                    }
                    break;
            }
        }
    }

    @Override
    public void onChangeSliderValue(GuiSlider slider) {
        if (slider.enabled) {
            switch (slider.id) {
                case 504704401:
                    double volume = (double) slider.getValueInt() / 100;
                    
                    Config.instance.set(Config.Key.alertVolume, volume);
                    Config.instance.save();
                    break;
            }
        }
    }
    
}
