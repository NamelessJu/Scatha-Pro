package namelessju.scathapro.gui.menus.screens.settings.alerts;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.widgets.sliders.FloatSlider;
import namelessju.scathapro.gui.menus.framework.screens.ConfigScreen;
import namelessju.scathapro.gui.overlay.elements.GuiElement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class AlertTitleSettingsScreen extends ConfigScreen
{
    public AlertTitleSettingsScreen(ScathaPro scathaPro, Screen parentScreen)
    {
        super(scathaPro, "Alert Title Position", parentScreen);
    }
    
    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader();
        
        GridBuilder gridBuilder = new GridBuilder();
        gridBuilder.addSingleCell(floatConfigSlider("X Position", 0f, 1f, config.alerts.titlePositionX, null))
            .setStepSize(0.01f).setValueComponentSupplier(FloatSlider.PERCENTAGE_COMPONENT_SUPPLIER);
        gridBuilder.addSingleCell(floatConfigSlider("Y Position", 0f, 1f, config.alerts.titlePositionY, null))
            .setStepSize(0.01f).setValueComponentSupplier(FloatSlider.PERCENTAGE_COMPONENT_SUPPLIER);
        gridBuilder.addSingleCell(floatConfigSlider("Scale", 0.25f, 1.75f, config.alerts.titleScale, null))
            .setStepSize(0.05f).setValueComponentSupplier(FloatSlider.PERCENTAGE_COMPONENT_SUPPLIER);
        gridBuilder.addSingleCell(nullableEnumCycleButton(
            GuiElement.Alignment.class, "Alignment", config.alerts.titleAlignmentOverride, "Automatic",
            null, null
        ));
        addDoneButtonFooterWithWidget(gridBuilder.getGrid());
    }
    
    @Override
    public void renderBackground(@NonNull GuiGraphics guiGraphics, int i, int j, float f)
    {
        super.renderBackground(guiGraphics, i, j, f);
        
        scathaPro.alertTitleOverlay.renderStaticComponents(
            guiGraphics, minecraft.getDeltaTracker(),
            Component.literal("Example Title").withStyle(ChatFormatting.GREEN),
            Component.literal("This is what alerts will look like").withStyle(ChatFormatting.GRAY)
        );
    }
}
