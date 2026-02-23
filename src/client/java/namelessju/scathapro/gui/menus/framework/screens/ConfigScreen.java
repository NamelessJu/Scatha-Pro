package namelessju.scathapro.gui.menus.framework.screens;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.widgets.sliders.FloatSlider;
import namelessju.scathapro.gui.menus.framework.widgets.sliders.IntegerSlider;
import namelessju.scathapro.files.Config;
import namelessju.scathapro.files.framework.JsonFile;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public abstract class ConfigScreen extends LayoutScreen
{
    protected final Config config;
    
    public ConfigScreen(ScathaPro scathaPro, String title, Screen parentScreen)
    {
        super(scathaPro, Component.literal(title), true, parentScreen);
        config = scathaPro.config;
    }
    
    @Override
    public void removed()
    {
        config.save();
    }
    
    
    public static CycleButton<Boolean> booleanConfigButton(String text, JsonFile.BooleanValue booleanValue)
    {
        return booleanConfigButton(text, booleanValue, null, null);
    }
    
    public static CycleButton<Boolean> booleanConfigButton(String text, JsonFile.BooleanValue booleanValue,
                                                           OptionInstance.@Nullable TooltipSupplier<Boolean> tooltipSupplier,
                                                           CycleButton.@Nullable OnValueChange<Boolean> onValueChanged)
    {
        CycleButton.Builder<Boolean> builder = booleanButtonBuilder(booleanValue.get());
        if (tooltipSupplier != null) builder.withTooltip(tooltipSupplier);
        return builder.create(Component.literal(text),
            (button, value) -> {
                booleanValue.set(value);
                if (onValueChanged != null) onValueChanged.onValueChange(button, value);
            }
        );
    }
    
    public static FloatSlider floatConfigSlider(String text, float min, float max,
                                                  JsonFile.PrimitiveValueWithDefault<Float> configValue,
                                                  @Nullable Consumer<Float> onValueChanged)
    {
        return new FloatSlider(
            0, 0, 150, 20,
            Component.literal(text),
            min, max, configValue.get(),
            value -> {
                configValue.set(value);
                if (onValueChanged != null) onValueChanged.accept(value);
            }
        );
    }
    
    public static IntegerSlider integerConfigSlider(String text, int min, int max,
                                                    JsonFile.PrimitiveValueWithDefault<Integer> configValue,
                                                    @Nullable Consumer<Integer> onValueChanged)
    {
        return new IntegerSlider(
            0, 0, 150, 20,
            Component.literal(text),
            min, max, configValue.get(),
            value -> {
                configValue.set(value);
                if (onValueChanged != null) onValueChanged.accept(value);
            }
        );
    }
    
    public static <T extends Enum<T>> CycleButton<Optional<T>> nullableEnumCycleButton(
        @NonNull Class<T> enumClass, @NonNull String text,
        JsonFile.@NonNull PrimitiveValueNullable<T> configValue, @Nullable String nullText,
        OptionInstance.@Nullable TooltipSupplier<Optional<T>> tooltipSupplier,
        CycleButton.@Nullable OnValueChange<Optional<T>> onValueChange
    )
    {
        return CycleButton
            .builder(
                optionalValue -> {
                    if (optionalValue.isPresent()) return Component.literal(optionalValue.get().toString());
                    else if (nullText != null) return Component.literal(nullText);
                    return CommonComponents.OPTION_OFF;
                },
                Optional.ofNullable(configValue.get())
            )
            .withValues(getNullableOptions(enumClass.getEnumConstants()))
            .withTooltip(tooltipSupplier != null ? tooltipSupplier : value -> null)
            .create(
                Component.literal(text),
                (button, optionalValue) -> {
                    configValue.set(optionalValue.orElse(null));
                    if (onValueChange != null) onValueChange.onValueChange(button, optionalValue);
                }
            );
    }
}
