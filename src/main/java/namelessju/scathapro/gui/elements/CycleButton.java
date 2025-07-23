package namelessju.scathapro.gui.elements;

import net.minecraft.client.gui.GuiScreen;

public class CycleButton<T> extends ScathaProButton implements IClickActionButton
{
    public static interface IOptionChangedListener<U>
    {
        public void onChange(CycleButton<U> button);
    }
    
    public String text;
    public IOption<T>[] options;
    public IOptionChangedListener<T> changeListener;
    public String nullOptionName = null;
    
    private int selectedOptionIndex = 0;
    
    public CycleButton(int buttonId, int x, int y, int widthIn, int heightIn, String text, IOption<T>[] options, T startValue, IOptionChangedListener<T> changeListener)
    {
        super(buttonId, x, y, widthIn, heightIn, "");
        
        this.text = text;
        this.options = options;
        this.changeListener = changeListener;
        
        for (int i = 0; i < options.length; i++)
        {
            T value = options[i].getOptionValue();
            if (value != null && value.equals(startValue) || value == null && startValue == null)
            {
                selectedOptionIndex = i;
                break;
            }
        }
        
        updateText();
    }
    
    public void updateText()
    {
        this.displayString = text + ": " + getSelectedOptionName();
    }
    
    @Override
    public void click()
    {
        if (options.length <= 0) return;
        if (GuiScreen.isShiftKeyDown()) selectedOptionIndex --;
        else selectedOptionIndex ++;
        selectedOptionIndex = selectedOptionIndex % options.length;
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
        IOption<T> selectedOption = getSelectedOption();
        String name = selectedOption != null ? selectedOption.getOptionName() : null;
        if (name == null) return nullOptionName != null ? nullOptionName : "OFF";
        return name;
    }
    
    public T getSelectedValue()
    {
        IOption<T> selectedOption = getSelectedOption();
        if (selectedOption == null) return null;
        return selectedOption.getOptionValue();
    }
    
    public void setNullOptionName(String name)
    {
        nullOptionName = name;
        if (getSelectedValue() == null) updateText();
    }
    
    
    public static interface IOption<U>
    {
        public String getOptionName();
        public U getOptionValue();
    }
    
    public static class EnumOption<T extends Enum<T>> implements IOption<T>
    {
        @SuppressWarnings("unchecked")
        public static <U extends Enum<U>> EnumOption<U>[] from(Class<U> enumClass, boolean includeNull)
        {
            U[] enumValues = enumClass.getEnumConstants();
            int count = enumValues.length;
            if (includeNull) count ++;
            EnumOption<U>[] options = new EnumOption[count];
            int i = 0, j = 0;
            if (includeNull) options[j++] = new EnumOption<U>(null);
            while (i < enumValues.length) options[j++] = new EnumOption<U>(enumValues[i++]);
            return options;
        }
        
        final T value;
        
        public EnumOption(T value)
        {
            this.value = value;
        }
        
        @Override
        public String getOptionName()
        {
            return value != null ? value.toString() : null;
        }
        
        @Override
        public T getOptionValue()
        {
            return value;
        }
    }
    
    public static class IntegerOption implements IOption<Integer>
    {
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
        public String getOptionName()
        {
            return String.valueOf(value);
        }

        @Override
        public Integer getOptionValue()
        {
            return value;
        }
    }
}
