package namelessju.scathapro.gui.menus.screens.settings;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.screens.ConfigScreen;
import namelessju.scathapro.gui.menus.screens.settings.alerts.MainAlertSettingsScreen;
import namelessju.scathapro.gui.menus.screens.settings.overlay.MainOverlaySettingsScreen;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.NonNull;

public class MainSettingsScreen extends ConfigScreen
{
    public MainSettingsScreen(ScathaPro scathaPro, Screen parentScreen)
    {
        super(scathaPro, "Settings", parentScreen);
    }
    
    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader(
            booleanConfigButton("Autom. Update Checks", scathaPro.config.miscellaneous.automaticUpdateCheckEnabled),
            booleanConfigButton("Automatic Backups", scathaPro.config.miscellaneous.automaticBackupsEnabled)
        );
        
        GridBuilder gridBuilder = new GridBuilder();
        gridBuilder.addSingleCell(subScreenButton("UI Overlay...", MainOverlaySettingsScreen::new));
        gridBuilder.addSingleCell(subScreenButton("Alerts...", MainAlertSettingsScreen::new));
        gridBuilder.addSingleCell(subScreenButton("Player Rotation...", PlayerRotationSettingsScreen::new));
        gridBuilder.addSingleCell(subScreenButton("Drop Message Extension...", DropMessageExtensionSettingsScreen::new));
        gridBuilder.addSingleCell(subScreenButton("Chat Messages...", ChatMessageSettingsScreen::new));
        gridBuilder.addSingleCell(subScreenButton("Sounds...", SoundSettingsScreen::new));
        gridBuilder.addSingleCell(subScreenButton("Achievement Settings...", AchievementSettingsScreen::new));
        gridBuilder.addSingleCell(subScreenButton("Miscellaneous...", MiscellaneousSettingsScreen::new));
        gridBuilder.addToContent(layout);
        
        addDoneButtonFooter();
    }
}
