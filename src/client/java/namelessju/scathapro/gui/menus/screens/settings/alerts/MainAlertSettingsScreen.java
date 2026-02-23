package namelessju.scathapro.gui.menus.screens.settings.alerts;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.alerts.alertmodes.AlertMode;
import namelessju.scathapro.alerts.alertmodes.customalertmode.CustomAlertMode;
import namelessju.scathapro.gui.menus.framework.screens.ConfigScreen;
import namelessju.scathapro.gui.menus.screens.settings.alerts.customalertmode.CustomAlertModeScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class MainAlertSettingsScreen extends ConfigScreen
{
    private Button customModeButton;
    
    public MainAlertSettingsScreen(ScathaPro scathaPro, Screen parentScreen)
    {
        super(scathaPro, "Alert Settings", parentScreen);
    }
    
    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader();
        
        GridBuilder gridBuilder = new GridBuilder();
        
        CycleButton<AlertMode> modeButton = CycleButton.<AlertMode>builder(value -> Component.literal(value.name))
            .withValues(scathaPro.alertModeManager.getAllModes())
            .withInitialValue(scathaPro.alertModeManager.getCurrentMode())
            .withTooltip(value -> Tooltip.create(
                Component.literal("Plays different sounds\n(and titles in custom mode)").withStyle(ChatFormatting.GRAY)
            ))
            .create(Component.literal("Alert Mode"), (button, value) -> {
                config.alerts.mode.set(value.id);
                scathaPro.mainOverlay.updateScathaPetImage();
                updateCustomModeButton();
            });
        gridBuilder.addSingleCell(modeButton);
        
        gridBuilder.addSingleCell(customModeButton = subScreenButton("Custom Alert Modes...", CustomAlertModeScreen::new));
        updateCustomModeButton();
        
        gridBuilder.addSingleCell(subScreenButton("Alert Configuration...", AlertConfigurationScreen::new));
        gridBuilder.addSingleCell(subScreenButton("Alert Title Position...", AlertTitleSettingsScreen::new));
        
        gridBuilder.addToContent(layout);
        
        addDoneButtonFooter();
    }
    
    private void updateCustomModeButton()
    {
        if (scathaPro.alertModeManager.getCurrentMode() instanceof CustomAlertMode)
        {
            customModeButton.active = true;
            customModeButton.setTooltip(null);
        }
        else
        {
            customModeButton.active = false;
            customModeButton.setTooltip(Tooltip.create(
                Component.literal("Select \"Custom\" to access\ncustom alert mode settings").withStyle(ChatFormatting.YELLOW)
            ));
        }
    }
}
