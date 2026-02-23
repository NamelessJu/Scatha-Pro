package namelessju.scathapro.gui.menus.screens.settings.overlay.miscellaneous;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.screens.settings.overlay.OverlaySettingsScreen;
import namelessju.scathapro.miscellaneous.data.enums.SecondaryWormStatsType;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class OverlayMiscellaneousSettingsScreen extends OverlaySettingsScreen
{
    public OverlayMiscellaneousSettingsScreen(ScathaPro scathaPro, Screen parentScreen)
    {
        super(scathaPro, "Overlay Miscellaneous Settings", parentScreen);
    }
    
    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader();
        
        GridBuilder gridBuilder = new GridBuilder();
        gridBuilder.addSingleCell(CycleButton
            .<SecondaryWormStatsType>builder(value -> Component.literal(value.toString()))
            .withValues(SecondaryWormStatsType.values())
            .withInitialValue(config.overlay.statsType.get())
            .create(Component.literal("Worm Stats Per"), (button, value) -> {
                config.overlay.statsType.set(value);
                scathaPro.mainOverlay.updateStatsType();
            })
        );
        gridBuilder.addSingleCell(subScreenButton("Scatha Percentage...", OverlayScathaPercentageSettingsScreen::new));
        addDoneButtonFooterWithWidget(gridBuilder.getGrid());
    }
}
