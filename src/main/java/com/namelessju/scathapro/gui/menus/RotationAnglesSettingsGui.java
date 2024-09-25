package com.namelessju.scathapro.gui.menus;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.BooleanSettingButton;
import com.namelessju.scathapro.managers.Config;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.client.config.GuiSlider;

public class RotationAnglesSettingsGui extends ScathaProGui implements GuiSlider.ISlider
{
    private final Config config;
    
    public RotationAnglesSettingsGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
        
        this.config = scathaPro.getConfig();
    }

    @Override
    public String getTitle()
    {
        return "Pitch/Yaw Display Settings";
    }
    
    @Override
    public void initGui()
    {
        super.initGui();

        addGridButton(new BooleanSettingButton(1, 0, 0, 0, 0, "Pitch/Yaw Display", Config.Key.showRotationAngles));
        addGridButton(new BooleanSettingButton(2, 0, 0, 0, 0, "Show Yaw Only", Config.Key.rotationAnglesYawOnly).setTooltip(EnumChatFormatting.GRAY + "Yaw = left/right rotation"));
        addGridButton(new GuiSlider(3, 0, 0, 0, 0, "Decimal Places: ", "", 0, 3, config.getInt(Config.Key.rotationAnglesDecimalDigits), false, true, this));
        addGridGap();
        addGridButton(new BooleanSettingButton(4, 0, 0, 0, 0, "Yaw: Show Ones And Lower Places Only", Config.Key.rotationAnglesMinimalYaw).setTooltip(EnumChatFormatting.GRAY + "Allows for precisely aligning your angle to the cardinal directions with the least amount of screen clutter"), true);
        
        addDoneButton();
    }

    @Override
    public void onChangeSliderValue(GuiSlider slider)
    {
        if (!slider.enabled) return;
        switch (slider.id)
        {
            case 3:
                config.set(Config.Key.rotationAnglesDecimalDigits, slider.getValueInt());
                config.save();
                break;
        }
    }
}
