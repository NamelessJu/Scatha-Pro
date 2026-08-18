package namelessju.scathapro.gui.menus.screens.settings;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.screens.ConfigScreen;
import namelessju.scathapro.gui.menus.screens.settings.alerts.MainAlertSettingsScreen;
import namelessju.scathapro.gui.menus.screens.settings.overlay.MainOverlaySettingsScreen;
import namelessju.scathapro.miscellaneous.data.mixindata.IKeyBindsListExtraData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;
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
            booleanConfigButton("Autom. Update Checks", scathaPro.config.miscellaneous.automaticUpdateCheckEnabled,
                _ -> Tooltip.create(
                    Component.literal("Checks Modrinth for new mod versions once per game start")
                        .withStyle(ChatFormatting.GRAY)
                ), null
            ),
            booleanConfigButton("Automatic Backups", scathaPro.config.miscellaneous.automaticBackupsEnabled,
                _ -> Tooltip.create(
                    Component.literal("Creates a full backup of this mod's save files when a new version is installed")
                        .withStyle(ChatFormatting.GRAY)
                ), null
            )
        );

        GridBuilder gridBuilder = new GridBuilder();

        gridBuilder.addSingleCell(subScreenButton("UI Overlay...", MainOverlaySettingsScreen::new));
        gridBuilder.addSingleCell(subScreenButton("Alerts...", MainAlertSettingsScreen::new));

        gridBuilder.addSingleCell(subScreenButton("Worms...", WormSettingsScreen::new));
        gridBuilder.addSingleCell(subScreenButton("Scatha Pet Drop...", PetDropSettingsScreen::new));

        gridBuilder.addSingleCell(subScreenButton("Player Rotation...", PlayerRotationSettingsScreen::new));
        gridBuilder.addSingleCell(subScreenButton("Chat Messages...", ChatMessageSettingsScreen::new));

        gridBuilder.addSingleCell(subScreenButton("Sounds...", SoundSettingsScreen::new));
        gridBuilder.addSingleCell(subScreenButton("Achievement Settings...", AchievementSettingsScreen::new));

        gridBuilder.addSingleCell(subScreenButton("Key Binds...", (_, parent) -> {
            KeyBindsScreen keyBindsScreen = new KeyBindsScreen(parent, minecraft.options);
            ((IKeyBindsListExtraData) keyBindsScreen).scathapro$setScathaProOnly();
            return keyBindsScreen;
        }));
        gridBuilder.addSingleCell(subScreenButton("Miscellaneous...", MiscellaneousSettingsScreen::new));

        gridBuilder.addToContent(layout);

        addDoneButtonFooter();
    }
}