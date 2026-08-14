package namelessju.scathapro.gui.menus.screens.settings.overlay;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.files.framework.JsonFile;
import namelessju.scathapro.gui.menus.framework.widgets.sliders.FloatSlider;
import namelessju.scathapro.gui.menus.framework.widgets.sliders.IntegerSlider;
import namelessju.scathapro.gui.overlay.elements.OverlayElement;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class OverlayPositionScreen extends OverlaySettingsScreen
{
    public OverlayPositionScreen(ScathaPro scathaPro, Screen parentScreen)
    {
        super(scathaPro, "Overlay Position", parentScreen);
    }

    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader();

        GridBuilder gridBuilder = new GridBuilder();
        gridBuilder.addSingleCell(positionSlider("X Position", config.overlay.positionX));
        gridBuilder.addSingleCell(positionSlider("Y Position", config.overlay.positionY));
        gridBuilder.addSingleCell(floatConfigSlider("Scale", 0.25f, 1.75f, config.overlay.scale, null))
            .setStepSize(0.05f).setValueComponentSupplier(FloatSlider.PERCENTAGE_COMPONENT_SUPPLIER);
        gridBuilder.addSingleCell(nullableEnumCycleButton(
            OverlayElement.Alignment.class, "Alignment", config.overlay.alignmentOverride, "Automatic", null, null
        ));
        addDoneButtonFooterWithWidget(gridBuilder.getGrid());
    }


    public static IntegerSlider positionSlider(String text, JsonFile.PrimitiveValueNullable<Float> configValue)
    {
        return new IntegerSlider(
            0, 0, 150, 20,
            Component.literal(text),
            -1, 100, configValue.getOptional().isPresent() ? Math.round(configValue.getOptional().get() * 100) : -1,
            value -> {
                if (value >= 0) configValue.set(value * 0.01f);
                else configValue.set(null);
            }
        ).setValueComponentSupplier(value -> {
            if (value >= 0) return FloatSlider.PERCENTAGE_COMPONENT_SUPPLIER.apply(value * 0.01f);
            return Component.literal("Default");
        });
    }
}