package com.namelessju.scathapro.gui.menus;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.BooleanSettingButton;
import com.namelessju.scathapro.gui.elements.DoneButton;
import com.namelessju.scathapro.managers.Config;

import net.minecraft.client.gui.GuiScreen;

public class MiscSettingsGui extends ScathaProGui
{
    public MiscSettingsGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
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
        buttonList.add(new BooleanSettingButton(504704804, width / 2 + 5, height / 6 + 24 - 12, 150, 20, "Bestiary Kills Parsing", Config.Key.automaticStatsParsing));
        
        buttonList.add(new BooleanSettingButton(504704805, width / 2 - 155, height / 6 + 48 - 12, 150, 20, "Chat Copy Button", Config.Key.chatCopy));
        
        buttonList.add(new DoneButton(504704899, width / 2 - 100, height / 6 + 168, 200, 20, "Done", this));
    }
}
