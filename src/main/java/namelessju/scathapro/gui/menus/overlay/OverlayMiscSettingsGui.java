package namelessju.scathapro.gui.menus.overlay;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.elements.CycleButton;
import namelessju.scathapro.gui.elements.SubMenuButton;
import namelessju.scathapro.managers.Config;
import namelessju.scathapro.miscellaneous.enums.WormStatsType;
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
        
        elements.add(new CycleButton<WormStatsType>(1, width / 2 - 155, height - 45 - 48 - 6, 310, 20, "Worm Stats Per", CycleButton.EnumOption.from(WormStatsType.class, false), config.getEnum(Config.Key.statsType, WormStatsType.class), button -> {
            config.set(Config.Key.statsType, button.getSelectedValue().name());
            scathaPro.getOverlay().updateStatsType();
        }));
        
        elements.add(new SubMenuButton(2, width / 2 - 155, height - 45 - 24 - 6, 310, 20, "Scatha Percentage...", this, OverlayScathaPercentageSettingsGui.class));
        
        addDoneButton(width / 2 - 100, height - 45, 200, 20);
    }
}
