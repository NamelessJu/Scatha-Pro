package namelessju.scathapro.gui.menus.screens.settings;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.screens.ConfigScreen;
import namelessju.scathapro.gui.menus.framework.widgets.sliders.FloatSlider;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class PlayerRotationSettingsScreen extends ConfigScreen
{
    public PlayerRotationSettingsScreen(ScathaPro scathaPro, Screen parentScreen)
    {
        super(scathaPro, "Player Rotation Settings", parentScreen);
    }
    
    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader();
        
        GridBuilder gridBuilder = new GridBuilder();
        gridBuilder.addFullWidth(
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
        ).setTooltip(Tooltip.create(
            Component.literal("The key binding of the same name replaces the main sensitivity with this one while the key is being held")
                .withStyle(ChatFormatting.GRAY)
        ));
        gridBuilder.addGap();
        gridBuilder.addSingleCell(booleanConfigButton("Pitch/Yaw Display", config.miscellaneous.rotationAnglesEnabled));
        gridBuilder.addSingleCell(booleanConfigButton("Show Yaw Only", config.miscellaneous.rotationAnglesYawOnly,
            value -> Tooltip.create(Component.literal("Yaw = left/right rotation").withStyle(ChatFormatting.GRAY)),
            null
        ));
        gridBuilder.addSingleCell(integerConfigSlider(
            "Decimal Places", 0, 3,
            config.miscellaneous.rotationAnglesDecimalPlaces, null
        ));
        gridBuilder.addSingleCell(booleanConfigButton("Shorter Yaw", config.miscellaneous.rotationAnglesMinimalYawEnabled,
            value -> Tooltip.create(Component.literal("Hides the tens and hundreds places of the yaw value").withStyle(ChatFormatting.GRAY)),
            null
        ));
        gridBuilder.addGap();
        gridBuilder.addFullWidth(booleanConfigButton("Alternative Crosshair Overlay Layout", config.miscellaneous.alternativeCrosshairLayoutEnabled,
            value -> Tooltip.create(Component.literal("Makes space for the attack indicator").withStyle(ChatFormatting.GRAY)),
            (button, value) -> scathaPro.crosshairOverlay.updateLayout()
        ));
        gridBuilder.addToContent(layout);
        
        addDoneButtonFooter();
    }
}
