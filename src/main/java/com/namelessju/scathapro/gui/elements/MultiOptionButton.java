package com.namelessju.scathapro.gui.elements;

import net.minecraft.client.gui.GuiButton;

public class MultiOptionButton<T> extends GuiButton implements IClickActionButton
{
    public static interface IOptionChangedListener<U>
    {
        public void onChange(MultiOptionButton<U> button);
    }
    
    public String text;
    public IOption<T>[] options;
    public IOptionChangedListener<T> changeListener;
    
    private int selectedOptionIndex = 0;
    
    public MultiOptionButton(int buttonId, int x, int y, int widthIn, int heightIn, String text, IOption<T>[] options, T startValue, IOptionChangedListener<T> changeListener)
    {
        super(buttonId, x, y, widthIn, heightIn, "");
        
        this.text = text;
        this.options = options;
        this.changeListener = changeListener;
        
        for (int i = 0; i < options.length; i++)
        {
            if (options[i].getValue().equals(startValue))
            {
                selectedOptionIndex = i;
                break;
            }
        }
        
        updateText();
    }
    
    public void updateText()
    {
        String selectedOptionName = getSelectedOptionName();
        this.displayString = text + ": " + (selectedOptionName != null ? selectedOptionName : "none");
    }
    
    @Override
    public void click()
    {
        if (options.length <= 0) return;
        selectedOptionIndex = (selectedOptionIndex + 1) % options.length;
        if (selectedOptionIndex < 0) selectedOptionIndex += options.length;
        
        updateText();
        
        changeListener.onChange(this);
    }
    
    public IOption<T> getSelectedOption()
    {
        if (options.length <= 0) return null;
        return options[selectedOptionIndex];
    }
    
    public String getSelectedOptionName()
    {
        if (options.length <= 0) return null;
        return options[selectedOptionIndex].getName();
    }
    
    public T getSelectedValue()
    {
        if (options.length <= 0) return null;
        return options[selectedOptionIndex].getValue();
    }
    
    
    public static interface IOption<U>
    {
        public String getName();
        public U getValue();
    }
    
    public static class IntegerOption implements IOption<Integer>
    {
        public static IntegerOption[] range(int min, int max)
        {
            int size = max - min + 1;
            if (size < 0) throw new IllegalArgumentException("IntegerOption max must be larger than min!");
            
            IntegerOption[] options = new IntegerOption[size];
            for (int i = 0; i < size ; i ++)
            {
                options[i] = new IntegerOption(min + i);
            }
            return options;
        }
        
        public static IntegerOption[] from(int[] values)
        {
            IntegerOption[] options = new IntegerOption[values.length];
            for (int i = 0; i < options.length; i ++)
            {
                options[i] = new IntegerOption(values[i]);
            }
            return options;
        }
        
        public int value;
        
        public IntegerOption(int value)
        {
            this.value = value;
        }

        @Override
        public String getName()
        {
            return String.valueOf(value);
        }

        @Override
        public Integer getValue()
        {
            return value;
        }
    }
}
