package com.namelessju.scathapro.gui.menus.overlay;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.BooleanSettingButton;
import com.namelessju.scathapro.gui.elements.MultiOptionButton;
import com.namelessju.scathapro.managers.Config;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiSlider;

import java.io.IOException;

public class OverlayScathaPercentageSettingsGui extends OverlaySettingsGui implements GuiSlider.ISlider
{
    public OverlayScathaPercentageSettingsGui(ScathaPro scathaPro, GuiScreen parentGui) {
        super(scathaPro, parentGui);
    }

    @Override
    public String getTitle() {
        return "Overlay Scatha Percentage Settings";
    }

    @Override
    public void initGui()
    {
        super.initGui();

        buttonList.add(new MultiOptionButton<Integer>(1, width / 2 - 155, height - 45 - 72 - 6, 310, 20, "Scatha Percentage Decimal Places", MultiOptionButton.IntegerOption.range(0, 3), config.getInt(Config.Key.scathaPercentageDecimalDigits), new MultiOptionButton.IOptionChangedListener<Integer>() {
            @Override
            public void onChange(MultiOptionButton<Integer> button) {
                config.set(Config.Key.scathaPercentageDecimalDigits, button.getSelectedValue());
                config.save();
                scathaPro.getOverlay().updateTotalKills();
            }
        }));

        buttonList.add(new GuiSlider(2, width / 2 - 155, height - 45 - 48 - 6, 150, 20, "Amount Duration: ", "s", 1, 10, config.getInt(Config.Key.scathaPercentageCycleAmountDuration), false, true, this));
        buttonList.add(new GuiSlider(3, width / 2 + 5, height - 45 - 48 - 6, 150, 20, "Percentage Duration: ", "s", 1, 10, config.getInt(Config.Key.scathaPercentageCyclePercentageDuration), false, true, this));
        buttonList.add(new BooleanSettingButton(4, width / 2 - 155, height - 45 - 24 - 6, 310, 20, "Move Behind Total Kills", Config.Key.scathaPercentageAlternativePosition));

        addDoneButton(width / 2 - 100, height - 45, 200, 20);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);

        if (!button.enabled) return;
        if (button.id == 4 && scathaPro.getConfig().getBoolean(Config.Key.scathaPercentageAlternativePosition))
        {
            scathaPro.getOverlay().updateScathaKills();
        }
        else scathaPro.getOverlay().updateTotalKills();
    }

    @Override
    public void onChangeSliderValue(GuiSlider slider)
    {
        if (!slider.enabled) return;
        switch (slider.id)
        {
            case 2:
                config.set(Config.Key.scathaPercentageCycleAmountDuration, slider.getValueInt());
                config.save();
                break;

            case 3:
                config.set(Config.Key.scathaPercentageCyclePercentageDuration, slider.getValueInt());
                config.save();
                break;
        }
    }
}
