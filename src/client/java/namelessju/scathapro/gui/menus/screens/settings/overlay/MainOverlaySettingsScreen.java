package namelessju.scathapro.gui.menus.screens.settings.overlay;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.miscellaneous.data.enums.SecondaryWormStatsType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class MainOverlaySettingsScreen extends OverlaySettingsScreen
{
    CycleButton<Boolean> showImmediatelyButton;

    public MainOverlaySettingsScreen(ScathaPro scathaPro, Screen parentScreen)
    {
        super(scathaPro, "UI Overlay Settings", parentScreen);
    }

    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader();

        GridBuilder gridBuilder = new GridBuilder();
        gridBuilder.addSingleCell(booleanConfigButton("Scatha Farming Overlay", config.overlay.enabled));
        gridBuilder.addSingleCell(showImmediatelyButton = booleanConfigButton("Show Immediately", config.overlay.showImmediately,
            _ -> Tooltip.create(Component.literal("""
                Usually the overlay is hidden until you spawn the first worm of a lobby.
                Enabling this will show it immediately upon entering the Crystal Hollows."""
            ).withStyle(ChatFormatting.GRAY)),
            (_, isEnabled) -> {
                if (isEnabled) scathaPro.mainOverlay.setShown(true);
            }
        ));
        gridBuilder.addSingleCell(enumCycleButton(
            SecondaryWormStatsType.class, "Worm Stats Per", config.overlay.statsType, null, null
        ));
        gridBuilder.addSingleCell(subScreenButton("Position...", OverlayPositionScreen::new));
        gridBuilder.addSingleCell(subScreenButton("Content...", OverlayContentScreen::new));
        gridBuilder.addSingleCell(subScreenButton("Scatha Percentage...", OverlayScathaPercentageSettingsScreen::new));
        addDoneButtonFooterWithWidget(gridBuilder.getGrid());
    }
}