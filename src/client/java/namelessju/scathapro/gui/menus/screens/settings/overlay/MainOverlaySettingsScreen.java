package namelessju.scathapro.gui.menus.screens.settings.overlay;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.screens.settings.overlay.miscellaneous.OverlayMiscellaneousSettingsScreen;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.NonNull;

public class MainOverlaySettingsScreen extends OverlaySettingsScreen
{
    public MainOverlaySettingsScreen(ScathaPro scathaPro, Screen parentScreen)
    {
        super(scathaPro, "UI Overlay Settings", parentScreen);
    }
    
    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader();
        
        GridBuilder gridBuilder = new GridBuilder();
        gridBuilder.addSingleCell(booleanConfigButton("UI Overlay", config.overlay.enabled, null,
            (button, value) -> scathaPro.mainOverlay.updateVisibility()));
        gridBuilder.addSingleCell(subScreenButton("Position...", OverlayPositionScreen::new));
        gridBuilder.addSingleCell(subScreenButton("Components...", OverlayComponentsScreen::new));
        gridBuilder.addSingleCell(subScreenButton("Miscellaneous...", OverlayMiscellaneousSettingsScreen::new));
        addDoneButtonFooterWithWidget(gridBuilder.getGrid());
    }
}
