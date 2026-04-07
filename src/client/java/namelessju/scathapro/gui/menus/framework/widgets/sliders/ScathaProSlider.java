package namelessju.scathapro.gui.menus.framework.widgets.sliders;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class ScathaProSlider<T> extends AbstractSliderButton
{
    private final @NonNull Component name;
    private @NonNull Function<T, Component> valueComponentSupplier;
    private final @NonNull Consumer<T> onValueChanged;
    private @Nullable Consumer<T> clickListener = null;
    
    private double previousProgress = -1D;
    
    public ScathaProSlider(int x, int y, int width, int height,
                           @NonNull Component name, @NonNull Function<T, Component> valueComponentSupplier,
                           @NonNull Consumer<T> onValueChanged
    )
    {
        super(x, y, width, height, Component.empty(), 0);
        this.name = name;
        this.valueComponentSupplier = valueComponentSupplier;
        this.onValueChanged = onValueChanged;
    }
    
    @SuppressWarnings("unchecked")
    public <S extends ScathaProSlider<T>> S setValueComponentSupplier(@NonNull Function<T, Component> valueComponentSupplier)
    {
        this.valueComponentSupplier = valueComponentSupplier;
        updateMessage();
        return (S) this;
    }
    
    @SuppressWarnings("unchecked")
    public <S extends ScathaProSlider<T>> S setClickListener(@Nullable Consumer<T> clickListener)
    {
        this.clickListener = clickListener;
        return (S) this;
    }
    
    @Override
    protected void applyValue()
    {
        handleUpdate();
    }
    
    @Override
    public void updateMessage()
    {
        this.setMessage(Component.empty()
            .append(name)
            .append(Component.literal(": "))
            .append(valueComponentSupplier.apply(getMappedValue()))
        );
    }
    
    @Override
    public void onClick(@NonNull MouseButtonEvent mouseButtonEvent, boolean bl)
    {
        super.onClick(mouseButtonEvent, bl);
        if (clickListener != null) clickListener.accept(getMappedValue());
    }
    
    private void handleUpdate()
    {
        snapProgress();
        value = Mth.clamp(value, 0D, 1D);
        if (previousProgress >= 0D && !Mth.equal(value, previousProgress))
        {
            onValueChanged.accept(getMappedValue());
        }
        previousProgress = value;
    }
    
    public T getMappedValue()
    {
        return progressToValue();
    }
    
    public void setMappedValue(T mappedValue)
    {
        this.value = Mth.clamp(valueToProgress(mappedValue), 0D, 1D);
        handleUpdate();
        updateMessage();
    }
    
    protected abstract T progressToValue();
    protected abstract double valueToProgress(T floatValue);
    protected abstract void snapProgress();
}
