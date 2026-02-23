package namelessju.scathapro.gui.menus.framework.screens;

import namelessju.scathapro.ScathaPro;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.apache.commons.compress.utils.Lists;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public abstract class ScathaProScreen extends Screen
{
    protected ScathaPro scathaPro;
    protected Screen parentScreen;
    protected final boolean addModTitle;
    protected final StringWidget titleWidget;
    
    public ScathaProScreen(ScathaPro scathaPro, Component titleComponent, boolean addModTitle, Screen parentScreen)
    {
        super(titleComponent);
        this.scathaPro = scathaPro;
        this.parentScreen = parentScreen;
        this.addModTitle = addModTitle;
        titleWidget = new StringWidget(CommonComponents.EMPTY, font);
    }
    
    @Override
    public @NonNull Component getTitle()
    {
        return addModTitle
            ? Component.empty().append(scathaPro.getModDisplayName() + " - ").append(super.getTitle())
            : super.getTitle();
    }
    
    public void updateTitleWidget()
    {
        titleWidget.setMessage(getTitle());
    }
    
    @Override
    public void added()
    {
        updateTitleWidget();
    }
    
    @Override
    public void onClose()
    {
        minecraft.setScreen(parentScreen);
    }
    
    
    protected static <T extends AbstractWidget> T wip(T widget)
    {
        widget.active = false;
        widget.setTooltip(Tooltip.create(Component.literal("Work in Progress").withStyle(ChatFormatting.YELLOW)));
        return widget;
    }
    
    public StringWidget label(int x, int y, Component component)
    {
        StringWidget label = new StringWidget(component, font);
        label.setPosition(x, y);
        return label;
    }
    
    private static final CycleButton.ValueListSupplier<Boolean> BOOLEAN_VALUES_SUPPLIER = CycleButton.ValueListSupplier.create(List.of(true, false));
    public static CycleButton.Builder<Boolean> booleanButtonBuilder(boolean initialValue)
    {
        return CycleButton.builder(
            value -> value ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF,
                initialValue
            )
            .withValues(BOOLEAN_VALUES_SUPPLIER);
    }
    
    public Button subScreenButton(String text, BiFunction<ScathaPro, Screen, Screen> screenConstructor)
    {
        return subScreenButtonBuilder(Component.literal(text), () -> screenConstructor.apply(scathaPro, this)).build();
    }
    
    public Button.Builder subScreenButtonBuilder(Component component, Supplier<Screen> screenSupplier)
    {
        return Button.builder(component, button -> scathaPro.minecraft.setScreen(screenSupplier.get()));
    }
    
    public Button doneButton()
    {
        return doneButton(CommonComponents.GUI_DONE, 200);
    }
    
    public Button doneButton(Component component, int width)
    {
        return Button.builder(component, button -> onClose()).width(width).build();
    }
    
    public static Button placeholderButton(String text)
    {
        return Button.builder(Component.literal(text), button -> {}).build();
    }
    
    public static <T> Collection<Optional<T>> getNullableOptions(T[] options)
    {
        List<Optional<T>> list = Lists.newArrayList();
        list.add(Optional.empty());
        for (T value : options) list.add(Optional.ofNullable(value));
        return list;
    }
}
