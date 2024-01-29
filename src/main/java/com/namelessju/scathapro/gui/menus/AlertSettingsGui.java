package com.namelessju.scathapro.gui.menus;

import java.io.IOException;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.BooleanSettingButton;
import com.namelessju.scathapro.gui.elements.DoneButton;
import com.namelessju.scathapro.managers.Config;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class AlertSettingsGui extends ScathaProGui
{
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
        
        buttonList.add(new BooleanSettingButton(504704404, width / 2 - 155, height / 6 - 12, 150, 20, "Worm Pre-Spawn Alert", Config.Key.wormPreAlert));
        buttonList.add(new BooleanSettingButton(504704402, width / 2 + 5, height / 6 - 12, 150, 20, "Worm Spawn Alert", Config.Key.wormAlert));
        buttonList.add(new BooleanSettingButton(504704403, width / 2 - 155, height / 6 + 24 - 12, 150, 20, "Scatha Spawn Alert", Config.Key.scathaAlert));
        buttonList.add(new BooleanSettingButton(504704405, width / 2 + 5, height / 6 + 24 - 12, 150, 20, "Bedrock Wall Alert", Config.Key.wallAlert));
        buttonList.add(new BooleanSettingButton(504704406, width / 2 - 155, height / 6 + 48 - 12, 150, 20, "Scatha Pet Drop Alert", Config.Key.petAlert));
        buttonList.add(new BooleanSettingButton(504704407, width / 2 + 5, height / 6 + 48 - 12, 150, 20, "Goblin Spawn Alert", Config.Key.goblinAlert));
        
        buttonList.add(new DoneButton(504704499, width / 2 - 100, height / 6 + 168, 200, 20, "Done", this));
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);
        
        if (button.enabled)
        {
            switch (button.id)
            {
                case 504704406:
                    if (scathaPro.config.getBoolean(Config.Key.petAlert))
                    {
                        scathaPro.variables.previousScathaPets = null;
                    }
                    break;
            }
        }
    }
    
}
