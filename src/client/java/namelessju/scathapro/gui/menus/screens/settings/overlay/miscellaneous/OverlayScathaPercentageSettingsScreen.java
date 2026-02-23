package namelessju.scathapro.gui.menus.screens.settings.overlay.miscellaneous;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.widgets.sliders.IntegerSlider;
import namelessju.scathapro.gui.menus.screens.settings.overlay.OverlaySettingsScreen;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.function.Function;

public class OverlayScathaPercentageSettingsScreen extends OverlaySettingsScreen
{
    public OverlayScathaPercentageSettingsScreen(ScathaPro scathaPro, Screen parentScreen)
    {
        super(scathaPro, "Overlay Scatha Percentage Settings", parentScreen);
    }
    
    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader();
        
        Function<Integer, Component> secondsValueComponentSupplier
            = value -> value > 0
                ? Component.empty().append(IntegerSlider.COMPONENT_SUPPLIER.apply(value)).append("s")
                : CommonComponents.OPTION_OFF;
        
        GridBuilder gridBuilder = new GridBuilder();
        gridBuilder.addSingleCell(integerConfigSlider("Decimal Places", 0, 3,
            config.overlay.scathaPercentageDecimalPlaces, value -> scathaPro.mainOverlay.updateTotalKills()));
        gridBuilder.addSingleCell(integerConfigSlider("Amount Duration", 1, 10,
            config.overlay.scathaPercentageCycleAmountDuration, null))
            .setValueComponentSupplier(secondsValueComponentSupplier);
        gridBuilder.addSingleCell(integerConfigSlider("Percentage Duration", 0, 10,
            config.overlay.scathaPercentageCyclePercentageDuration, null))
            .setValueComponentSupplier(secondsValueComponentSupplier);
        gridBuilder.addSingleCell(booleanConfigButton("Move Behind Total Kills",
            config.overlay.scathaPercentageAlternativePositionEnabled, null,
            (button, enabled) -> {
                if (enabled) scathaPro.mainOverlay.updateScathaKills();
                else scathaPro.mainOverlay.updateTotalKills();
            }));
        addDoneButtonFooterWithWidget(gridBuilder.getGrid());
    }
}
