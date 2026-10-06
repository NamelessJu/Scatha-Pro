package namelessju.scathapro.gui.menus.screens.settings.playerrotation;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.widgets.KeyMappingButton;
import namelessju.scathapro.gui.menus.framework.widgets.sliders.FloatSlider;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import org.jspecify.annotations.NonNull;

public class AlternativeSensitivitySettingsScreen extends PlayerRotationSettingsScreen
{
    public AlternativeSensitivitySettingsScreen(ScathaPro scathaPro, Screen parentScreen)
    {
        super(scathaPro, "Alternative Sensitivity Settings", parentScreen);
    }

    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader();

        GridBuilder gridBuilder = new GridBuilder();
        gridBuilder.addFullWidth(new KeyMappingButton(
            0, 0, 0, 0, scathaPro.inputManager.alternativeSensitivityKeyMapping
        ));
        gridBuilder.addFullWidthWidget(
                floatConfigSlider(
                    "Alternative Sensitivity", 0f, 1f,
                    config.miscellaneous.alternativeSensitivity, null
                )
                .setStepSize(0.005f)
                .setValueComponentSupplier(value -> {
                    if (value <= 0f) return Component.translatable("options.sensitivity.min");
                    if (value >= 1f) return Component.translatable("options.sensitivity.max");
                    return FloatSlider.PERCENTAGE_COMPONENT_SUPPLIER.apply(value * 2f);
                })
            )
            .setTooltip(Tooltip.create(
                Component.literal("The key binding of the same name replaces the main sensitivity with this one while the key is being held")
                    .withColor(TextColor.GRAY)
            ));
        addDoneButtonFooterWithWidget(gridBuilder.getGrid());
    }
}