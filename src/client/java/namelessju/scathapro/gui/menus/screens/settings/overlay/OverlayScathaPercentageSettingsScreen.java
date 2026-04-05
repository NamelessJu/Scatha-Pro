package namelessju.scathapro.gui.menus.screens.settings.overlay;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.widgets.sliders.IntegerSlider;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class OverlayScathaPercentageSettingsScreen extends OverlaySettingsScreen
{
    private IntegerSlider cycleAmountDurationSlider;
    private IntegerSlider cyclePercentageDurationSlider;
    
    public OverlayScathaPercentageSettingsScreen(ScathaPro scathaPro, Screen parentScreen)
    {
        super(scathaPro, "Overlay Scatha Percentage Settings", parentScreen);
    }
    
    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader();
        
        GridBuilder gridBuilder = new GridBuilder();
        gridBuilder.addSingleCell(integerConfigSlider("Decimal Places", 0, 3,
            config.overlay.scathaPercentageDecimalPlaces, value -> scathaPro.mainOverlay.updateTotalKills()));
        gridBuilder.addSingleCell(booleanConfigButton("Move Behind Total Kills",
            config.overlay.scathaPercentageAlternativePositionEnabled, null,
            (button, enabled) -> {
                if (enabled) scathaPro.mainOverlay.updateScathaKills();
                else scathaPro.mainOverlay.updateTotalKills();
                updateCycleDurationSliders();
            }));
        gridBuilder.addSingleCell(cycleAmountDurationSlider = integerConfigSlider("Amount Duration", 1, 10,
            config.overlay.scathaPercentageCycleAmountDuration, null))
            .setValueComponentSupplier(IntegerSlider.SECONDS_COMPONENT_SUPPLIER_WITH_OFF);
        gridBuilder.addSingleCell(cyclePercentageDurationSlider = integerConfigSlider("Percentage Duration", 0, 10,
            config.overlay.scathaPercentageCyclePercentageDuration, null))
            .setValueComponentSupplier(IntegerSlider.SECONDS_COMPONENT_SUPPLIER_WITH_OFF);
        addDoneButtonFooterWithWidget(gridBuilder.getGrid());
        
        updateCycleDurationSliders();
    }
    
    private void updateCycleDurationSliders()
    {
        if (config.overlay.scathaPercentageAlternativePositionEnabled.get())
        {
            Tooltip tooltip = Tooltip.create(
                Component.literal("Doesn't apply when displayed behind total kills")
                    .withStyle(ChatFormatting.YELLOW)
            );
            
            cycleAmountDurationSlider.active = false;
            cycleAmountDurationSlider.setTooltip(tooltip);
            cyclePercentageDurationSlider.active = false;
            cyclePercentageDurationSlider.setTooltip(tooltip);
        }
        else
        {
            cycleAmountDurationSlider.active = true;
            cycleAmountDurationSlider.setTooltip(null);
            cyclePercentageDurationSlider.active = true;
            cyclePercentageDurationSlider.setTooltip(null);
        }
    }
}
