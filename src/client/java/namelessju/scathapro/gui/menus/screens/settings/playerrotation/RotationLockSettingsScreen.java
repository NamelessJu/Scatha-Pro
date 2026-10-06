package namelessju.scathapro.gui.menus.screens.settings.playerrotation;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.widgets.KeyMappingButton;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.NonNull;

public class RotationLockSettingsScreen extends PlayerRotationSettingsScreen
{
    public RotationLockSettingsScreen(ScathaPro scathaPro, Screen parentScreen)
    {
        super(scathaPro, "Rotation Lock Settings", parentScreen);
    }

    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader();

        GridBuilder gridBuilder = new GridBuilder();
        gridBuilder.addFullWidth(new KeyMappingButton(
            0, 0, 0, 0, scathaPro.inputManager.lockRotationKeyMapping
        ));
        gridBuilder.addSingleCell(integerConfigSlider(
            "Lock Offset X", -50, 50, config.miscellaneous.rotationLockIconOffsetX, null
        ));
        gridBuilder.addSingleCell(integerConfigSlider(
            "Lock Offset Y", -50, 50, config.miscellaneous.rotationLockIconOffsetY, null
        ));
        addDoneButtonFooterWithWidget(gridBuilder.getGrid());
    }
}