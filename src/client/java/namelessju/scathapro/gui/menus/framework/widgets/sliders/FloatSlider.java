package namelessju.scathapro.gui.menus.framework.widgets.sliders;

import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;
import java.util.function.Function;

public class FloatSlider extends ScathaProSlider<Float>
{
    public static final Function<Float, Component> COMPONENT_SUPPLIER
        = value -> Component.literal(Float.toString(value));
    public static final Function<Float, Component> PERCENTAGE_COMPONENT_SUPPLIER
        = value -> Component.literal(Math.round(value * 100f) + "%");
    public static final Function<Float, Component> PERCENTAGE_COMPONENT_SUPPLIER_WITH_OFF
        = value -> value > 0f ? PERCENTAGE_COMPONENT_SUPPLIER.apply(value) : CommonComponents.OPTION_OFF;
    
    private final float min;
    private final float max;
    private float stepSize = -1f;
    
    public FloatSlider(
        int x, int y, int width, int height,
        @NonNull Component name,
        float min, float max, float initialValue,
        @NonNull Consumer<Float> onValueChanged
    )
    {
        super(x, y, width, height, name, COMPONENT_SUPPLIER, onValueChanged);
        this.min = min;
        this.max = max;
        setMappedValue(initialValue);
    }
    
    public FloatSlider setStepSize(float stepSize)
    {
        this.stepSize = stepSize;
        return this;
    }
    
    @Override
    protected Float progressToValue()
    {
        return (float) (min + ((max - min) * value));
    }
    
    @Override
    protected double valueToProgress(Float value)
    {
        return (value - min) / (max - min);
    }
    
    @Override
    protected void snapProgress()
    {
        if (stepSize <= 0f) return;
        float floatRange = (max - min);
        double relativeFloat = floatRange * value;
        relativeFloat = Math.round(relativeFloat / stepSize) * stepSize;
        value = relativeFloat / floatRange;
    }
    
    @Override
    public boolean keyPressed(@NonNull KeyEvent keyEvent)
    {
        double valueBefore = value;
        if (super.keyPressed(keyEvent))
        {
            if (stepSize <= 0f) return true;
            
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
