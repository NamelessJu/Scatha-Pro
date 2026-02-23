package namelessju.scathapro.gui.menus.framework.widgets.sliders;

import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;
import java.util.function.Function;

public class ValueListSlider<T> extends ScathaProSlider<T>
{
    private final T[] values;
    
    public ValueListSlider(
        int x, int y,
        int width, int height,
        @NotNull Component name,
        @NotNull Function<T, Component> valueComponentProvider,
        @NotNull T[] values,
        @NotNull T initialValue,
        @NotNull Consumer<T> onValueChanged
    )
    {
        super(x, y, width, height, name, valueComponentProvider, onValueChanged);
        this.values = values;
        setMappedValue(initialValue);
    }
    
    @Override
    protected T progressToValue()
    {
        return values[Mth.clamp(getCurrentIndex(), 0, values.length - 1)];
    }
    
    @Override
    protected double valueToProgress(T floatValue)
    {
        int index = -1;
        for (int i = 0; i < values.length; i++)
        {
            if (floatValue.equals(values[i]))
            {
                index = i;
                break;
            }
        }
        if (index < 0 || values.length == 1) return 0D;
        return (double) index / (values.length - 1);
    }
    
    @Override
    protected void snapProgress()
    {
        int intRange = values.length - 1;
        value = (double) Math.round(intRange * value) / intRange;
    }
    
    @Override
    public boolean keyPressed(@NonNull KeyEvent keyEvent)
    {
        double valueBefore = value;
        if (super.keyPressed(keyEvent))
        {
            value = valueBefore;
            if (keyEvent.isLeft() || keyEvent.isRight())
            {
                setIndex(getCurrentIndex() + (keyEvent.isLeft() ? -1 : 1));
            }
            
            return true;
        }
        
        return false;
    }
    
    public void setIndex(int index)
    {
        setMappedValue(getValueFromIndex(index));
    }
    
    public int getCurrentIndex()
    {
        return (int) Math.round((values.length - 1) * value);
    }
    
    public T getValueFromIndex(int index)
    {
        return values[Mth.clamp(index, 0, values.length - 1)];
    }
}
