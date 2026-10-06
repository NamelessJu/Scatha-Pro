package namelessju.scathapro.gui.menus.screens.settings.playerrotation;

import namelessju.scathapro.ScathaPro;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import org.jspecify.annotations.NonNull;

public class MainPlayerRotationSettingsScreen extends PlayerRotationSettingsScreen
{
    public MainPlayerRotationSettingsScreen(ScathaPro scathaPro, Screen parentScreen)
    {
        super(scathaPro, "Player Rotation Settings", parentScreen);
    }

    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader();

        GridBuilder gridBuilder = new GridBuilder();
        gridBuilder.addSingleCell(booleanConfigButton("Alternative Layout", config.miscellaneous.alternativeCrosshairLayoutEnabled,
            _ -> Tooltip.create(Component.literal("Makes space for the attack indicator").withColor(TextColor.GRAY)),
            null
        ));
        gridBuilder.addSingleCell(subScreenButton("Pitch/Yaw Display...", PitchYawDisplaySettingsScreen::new));
        gridBuilder.addSingleCell(subScreenButton("Rotation Lock...", RotationLockSettingsScreen::new));
        gridBuilder.addSingleCell(subScreenButton("Alternative Sensitivity...", AlternativeSensitivitySettingsScreen::new));
        addDoneButtonFooterWithWidget(gridBuilder.getGrid());
    }
}