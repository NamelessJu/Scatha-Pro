package namelessju.scathapro.gui.menus.framework.widgets.sliders;

import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;
import java.util.function.Function;

public class IntegerSlider extends ScathaProSlider<Integer>
{
    public static final Function<Integer, Component> COMPONENT_SUPPLIER = value -> Component.literal(Integer.toString(value));
    
    private final int min;
    private final int max;
    private int stepSize = 1;
    
    public IntegerSlider(
        int x, int y, int width, int height,
        @NotNull Component name,
        int min, int max, int initialValue,
        @NotNull Consumer<Integer> onValueChanged
    )
    {
        super(x, y, width, height, name, COMPONENT_SUPPLIER, onValueChanged);
        this.min = min;
        this.max = max;
        setMappedValue(initialValue);
    }
    
    public IntegerSlider setStepSize(int stepSize)
    {
        this.stepSize = Math.max(1, stepSize);
        return this;
    }
    
    @Override
    protected Integer progressToValue()
    {
        return (min + (int) Math.round((max - min) * value));
    }
    
    @Override
    protected double valueToProgress(Integer value)
    {
        return (double) (value - min) / (max - min);
    }
    
    @Override
    protected void snapProgress()
    {
        int intRange = (max - min);
        double relativeInt = intRange * value;
        relativeInt = Math.round(relativeInt / stepSize) * stepSize;
        value = relativeInt / intRange;
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
                setMappedValue(progressToValue() + (keyEvent.isLeft() ? -stepSize : stepSize));
            }
            
            return true;
        }
        
        return false;
    }
}
