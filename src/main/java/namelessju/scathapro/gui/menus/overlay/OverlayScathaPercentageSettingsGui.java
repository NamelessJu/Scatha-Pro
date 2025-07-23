package namelessju.scathapro.gui.menus.overlay;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.elements.BooleanSettingButton;
import namelessju.scathapro.gui.elements.ScathaProSlider;
import namelessju.scathapro.managers.Config;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiSlider;

public class OverlayScathaPercentageSettingsGui extends OverlaySettingsGui implements GuiSlider.ISlider
{
    public OverlayScathaPercentageSettingsGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
    }
    
    @Override
    public String getTitle()
    {
        return "Overlay Scatha Percentage Settings";
    }
    
    @Override
    public void initGui()
    {
        super.initGui();

        elements.add(new ScathaProSlider(1, width / 2 - 155, height - 45 - 72 - 6, 310, 20, "Scatha Percentage Decimal Places: ", "", 0, 3, config.getInt(Config.Key.scathaPercentageDecimalDigits), false, true, this));
        elements.add(new ScathaProSlider(2, width / 2 - 155, height - 45 - 48 - 6, 150, 20, "Amount Duration: ", "s", 1, 10, config.getInt(Config.Key.scathaPercentageCycleAmountDuration), false, true, this));
        elements.add(new ScathaProSlider(3, width / 2 + 5, height - 45 - 48 - 6, 150, 20, "Percentage Duration: ", "s", 0, 10, config.getInt(Config.Key.scathaPercentageCyclePercentageDuration), false, true, this));
        elements.add(new BooleanSettingButton(4, width / 2 - 155, height - 45 - 24 - 6, 310, 20, "Move Behind Total Kills", Config.Key.scathaPercentageAlternativePosition));
        
        addDoneButton(width / 2 - 100, height - 45, 200, 20);
    }
    
    @Override
    protected void actionPerformed(GuiButton button)
    {
        super.actionPerformed(button);
        
        if (button.id == 4)
        {
            if (scathaPro.getConfig().getBoolean(Config.Key.scathaPercentageAlternativePosition)) scathaPro.getOverlay().updateScathaKills();
            else scathaPro.getOverlay().updateTotalKills();
        }
    }
    
    @Override
    public void onChangeSliderValue(GuiSlider slider)
    {
        if (!slider.enabled) return;
        switch (slider.id)
        {
            case 1:
                config.set(Config.Key.scathaPercentageDecimalDigits, slider.getValueInt());
                
                scathaPro.getOverlay().updateTotalKills();
                break;
                
            case 2:
                config.set(Config.Key.scathaPercentageCycleAmountDuration, slider.getValueInt());
                break;

            case 3:
                config.set(Config.Key.scathaPercentageCyclePercentageDuration, slider.getValueInt());
                break;
        }
    }
}
