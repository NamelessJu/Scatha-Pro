package namelessju.scathapro.gui.menus.screens.settings.playerrotation;

import namelessju.scathapro.ScathaPro;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import org.jspecify.annotations.NonNull;

public class PitchYawDisplaySettingsScreen extends PlayerRotationSettingsScreen
{
    public PitchYawDisplaySettingsScreen(ScathaPro scathaPro, Screen parentScreen)
    {
        super(scathaPro, "Pitch/Yaw Display Settings", parentScreen);
    }

    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader();

        GridBuilder gridBuilder = new GridBuilder();
        gridBuilder.addSingleCell(booleanConfigButton("Pitch/Yaw Display", config.miscellaneous.rotationAnglesEnabled));
        gridBuilder.addSingleCell(booleanConfigButton("Show Yaw Only", config.miscellaneous.rotationAnglesYawOnly,
            _ -> Tooltip.create(Component.literal("Yaw = left/right rotation").withColor(TextColor.GRAY)),
            null
        ));
        gridBuilder.addSingleCell(integerConfigSlider(
            "Decimal Places", 0, 3,
            config.miscellaneous.rotationAnglesDecimalPlaces, null
        ));
        gridBuilder.addSingleCell(booleanConfigButton("Shorter Yaw", config.miscellaneous.rotationAnglesMinimalYawEnabled,
            _ -> Tooltip.create(Component.literal("Hides the tens and hundreds places of the yaw value").withColor(TextColor.GRAY)),
            null
        ));
        addDoneButtonFooterWithWidget(gridBuilder.getGrid());
    }
}