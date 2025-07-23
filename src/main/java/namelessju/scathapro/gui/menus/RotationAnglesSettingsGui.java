package namelessju.scathapro.gui.menus;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.elements.BooleanSettingButton;
import namelessju.scathapro.gui.elements.ScathaProSlider;
import namelessju.scathapro.managers.Config;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.client.config.GuiSlider;

public class RotationAnglesSettingsGui extends ConfigGui implements GuiSlider.ISlider
{
    private ScathaProSlider alternativeSensitivitySlider;
    
    public RotationAnglesSettingsGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
    }

    @Override
    public String getTitle()
    {
        return "Player Rotation Settings";
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        double alternativeSensitivity = config.getDouble(Config.Key.alternativeSensitivity);
        alternativeSensitivitySlider = new ScathaProSlider(4, 0, 0, 0, 0, "Alternative Sensitivity: ", "%", 0f, 1f, alternativeSensitivity, false, true, this);
        alternativeSensitivitySlider.getTooltip().setText(EnumChatFormatting.GRAY + "The key binding of the same name replaces the main sensitivity with this one while the key is being held");
        elements.add(setGridPosition(alternativeSensitivitySlider, GridElementMode.FULL_WIDTH));
        updateAlternativeSensitivitySliderText();
        
        addGridGap();

        addGridButton(new BooleanSettingButton(1, 0, 0, 0, 0, "Pitch/Yaw Display", Config.Key.showRotationAngles));
        
        BooleanSettingButton yawOnlyButton;
        addGridButton(yawOnlyButton = new BooleanSettingButton(2, 0, 0, 0, 0, "Show Yaw Only", Config.Key.rotationAnglesYawOnly));
        yawOnlyButton.getTooltip().setText(EnumChatFormatting.GRAY + "Yaw = left/right rotation");
        
        addGridButton(new ScathaProSlider(3, 0, 0, 0, 0, "Display Decimal Places: ", "", 0, 3, config.getInt(Config.Key.rotationAnglesDecimalDigits), false, true, this));
        
        BooleanSettingButton minimalYawButton;
        addGridButton(minimalYawButton = new BooleanSettingButton(4, 0, 0, 0, 0, "Shorter Yaw", Config.Key.rotationAnglesMinimalYaw));
        minimalYawButton.getTooltip().setText(EnumChatFormatting.GRAY + "Hides the tens and hundreds\nplaces of the yaw value", 200);
        
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
                break;
            
            case 4:
                config.set(Config.Key.alternativeSensitivity, slider.getValue());
                updateAlternativeSensitivitySliderText();
                break;
        }
    }
    
    private void updateAlternativeSensitivitySliderText()
    {
        int value = (int) Math.round(alternativeSensitivitySlider.getValue() * 200);
        alternativeSensitivitySlider.displayString = alternativeSensitivitySlider.dispString
            + (value == 0 ? I18n.format("options.sensitivity.min")
                :  (value == 200 ? I18n.format("options.sensitivity.max")
                    : value + alternativeSensitivitySlider.suffix
                )
            );
    }
}
