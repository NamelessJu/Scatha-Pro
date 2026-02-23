package namelessju.scathapro.gui.menus.screens.settings.alerts;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.alerts.Alert;
import namelessju.scathapro.gui.menus.framework.widgets.lists.TwoColumnGuiList;
import namelessju.scathapro.gui.menus.framework.widgets.sliders.IntegerSlider;
import namelessju.scathapro.gui.menus.framework.screens.ConfigScreen;
import namelessju.scathapro.miscellaneous.data.enums.OldLobbyAlertTriggerMode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

public class AlertConfigurationScreen extends ConfigScreen
{
    private IntegerSlider antiSleepIntervalMinSlider, antiSleepIntervalMaxSlider;
    
    public AlertConfigurationScreen(ScathaPro scathaPro, Screen parentScreen)
    {
        super(scathaPro, "Alert Configuration", parentScreen);
    }
    
    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader();
        
        TwoColumnGuiList list = addScrollList();
        for (Alert alert : scathaPro.alertManager)
        {
            list.addTitle(Component.literal(alert.alertName));
            CycleButton<Boolean> enabledButton = booleanConfigButton(
                "Enabled", alert.configValue,
                value -> alert.description != null ? Tooltip.create(
                    Component.empty().withStyle(ChatFormatting.GRAY).append(alert.description)
                ) : null,
                (button, value) -> onAlertToggled(alert, value)
            );
            
            AbstractWidget[] settingsWidgets = getAlertSettingsWidgets(alert);
            if (settingsWidgets != null)
            {
                if (settingsWidgets.length % 2 == 0) list.addDoubleColumn(enabledButton);
                else list.addSingleColumn(enabledButton);
                for (AbstractWidget widget : settingsWidgets)
                {
                    list.addSingleColumn(widget);
                }
            }
            else list.addDoubleColumn(enabledButton);
        }
        
        addDoneButtonFooter();
    }
    
    private void onAlertToggled(Alert alert, boolean enabled)
    {
        if (alert == scathaPro.alertManager.antiSleepAlert
            && enabled)
        {
            scathaPro.coreManager.antiSleepAlertTickTimer = 0;
            scathaPro.coreManager.setRandomAntiSleepAlertTriggerMinutes();
        }
    }
    
    private @Nullable AbstractWidget[] getAlertSettingsWidgets(Alert alert)
    {
        if (alert == scathaPro.alertManager.bedrockWallAlert)
        {
            return new AbstractWidget[] {
                integerConfigSlider("Trigger Distance", 0, 50, config.alerts.bedrockWallAlertTriggerDistance, null)
                    .setValueComponentSupplier(value -> Component.empty()
                        .append(IntegerSlider.COMPONENT_SUPPLIER.apply(value))
                        .append(value == 1 ? " Block" : " Blocks"))
            };
        }
        
        if (alert == scathaPro.alertManager.oldLobbyAlert)
        {
            return new AbstractWidget[] {
                integerConfigSlider(
                    "Trigger Day", 1, 30, config.alerts.oldLobbyAlertTriggerDay, null
                ),
                
                CycleButton.<OldLobbyAlertTriggerMode>builder(value -> Component.literal(value.toString()))
                    .withValues(OldLobbyAlertTriggerMode.values())
                    .withInitialValue(config.alerts.oldLobbyAlertTriggerMode.get())
                    .create(Component.literal("Trigger On"),
                        (button, value)
                            -> config.alerts.oldLobbyAlertTriggerMode.set(value)
                    )
            };
        }
        
        if (alert == scathaPro.alertManager.highHeatAlert)
        {
            return new AbstractWidget[] {
                integerConfigSlider("Trigger Heat Value", 90, 100, config.alerts.highHeatAlertTriggerValue, null)
            };
        }
        
        if (alert == scathaPro.alertManager.antiSleepAlert)
        {
            Function<Integer, Component> valueComponentSupplier = value -> Component.empty()
                .append(IntegerSlider.COMPONENT_SUPPLIER.apply(value))
                .append(value == 1 ? " Minute" : " Minutes");
            
            return new AbstractWidget[] {
                antiSleepIntervalMinSlider = integerConfigSlider(
                    "Interval Minimum", 0, 60, config.alerts.antiSleepAlertIntervalMin,
                    value -> {
                        config.alerts.antiSleepAlertIntervalMin.set(value);
                        
                        if (config.alerts.antiSleepAlertIntervalMax.get() < value)
                        {
                            config.alerts.antiSleepAlertIntervalMax.set(value);
                            antiSleepIntervalMaxSlider.setMappedValue(value);
                        }
                    }
                ).setValueComponentSupplier(valueComponentSupplier),
                
                antiSleepIntervalMaxSlider = integerConfigSlider(
                    "Interval Maximum", 1, 60, config.alerts.antiSleepAlertIntervalMax,
                    value -> {
                        config.alerts.antiSleepAlertIntervalMax.set(value);
                        
                        if (config.alerts.antiSleepAlertIntervalMin.get() > value)
                        {
                            config.alerts.antiSleepAlertIntervalMin.set(value);
                            antiSleepIntervalMinSlider.setMappedValue(value);
                        }
                    }
                ).setValueComponentSupplier(valueComponentSupplier)
            };
        }
        
        return null;
    }
}
