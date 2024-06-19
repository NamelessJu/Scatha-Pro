package com.namelessju.scathapro.gui.menus;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.MultiOptionButton;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.miscellaneous.OverlayStats;

import net.minecraft.client.gui.GuiScreen;

public class OverlayMiscSettingsGui extends OverlaySettingsGui
{
    public OverlayMiscSettingsGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
    }

    @Override
    public String getTitle()
    {
        return "Overlay Miscellaneous Settings";
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        buttonList.add(new MultiOptionButton<String>(1, width / 2 - 155, height - 45 - 48 - 6, 310, 20, "Worm Stats Per", OverlayStats.values(), config.getString(Config.Key.statsType), new MultiOptionButton.IOptionChangedListener<String>() {
            @Override
            public void onChange(MultiOptionButton<String> button)
            {
                config.set(Config.Key.statsType, button.getSelectedValue());
                config.save();
                scathaPro.getOverlay().setStatsType((OverlayStats) button.getSelectedOption());
            }
        }));
        
        buttonList.add(new MultiOptionButton<Integer>(2, width / 2 - 155, height - 45 - 24 - 6, 310, 20, "Scatha Percentage Decimal Places", MultiOptionButton.IntegerOption.range(0, 3), config.getInt(Config.Key.scathaPercentageDecimalDigits), new MultiOptionButton.IOptionChangedListener<Integer>() {
            @Override
            public void onChange(MultiOptionButton<Integer> button)
            {
                config.set(Config.Key.scathaPercentageDecimalDigits, button.getSelectedValue());
                config.save();
                scathaPro.getOverlay().updateTotalKills();
            }
        }));
        
        addDoneButton(width / 2 - 100, height - 45, 200, 20);
    }
    
}
