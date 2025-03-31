package com.namelessju.scathapro.gui.menus;

import com.google.common.base.Predicate;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.ScathaProLabel;
import com.namelessju.scathapro.gui.elements.ScathaProTextField;
import com.namelessju.scathapro.util.TextUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;

public class AverageMoneyGui extends ScathaProGui
{
    private ScathaProLabel resultLabel;
    private NumberTextField scathaPetPriceRareInput;
    private NumberTextField scathaPetPriceEpicInput;
    private NumberTextField scathaPetPriceLegendaryInput;
    private NumberTextField magicFindInput;
    private NumberTextField petLuckInput;
    private NumberTextField scathaRateInput;
    
    public AverageMoneyGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
    }
    
    @Override
    public String getTitle()
    {
        return "Average Scatha Money Calculator";
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        elements.add(setGridPosition(new ScathaProLabel(0, 0, 0, 0, "Scatha Pet Prices (In Million):", Util.Color.YELLOW), GridElementMode.FULL_WIDTH));
        gridNewLine(2);
        elements.add(setGridPosition(scathaPetPriceRareInput = (NumberTextField) new NumberTextField(1, 0, 0, 97, 0).setPlaceholder("Rare").setDefaultFormatting(EnumChatFormatting.BLUE.toString()), GridElementMode.CUSTOM_X));
        setDefaultNumber(scathaPetPriceRareInput, scathaPro.variables.avgMoneyCalcScathaPriceRare);
        elements.add(setGridPosition(scathaPetPriceEpicInput = (NumberTextField) new NumberTextField(2, 10, 0, 96, 0).setPlaceholder("Epic").setDefaultFormatting(EnumChatFormatting.DARK_PURPLE.toString()), GridElementMode.CUSTOM_X));
        setDefaultNumber(scathaPetPriceEpicInput, scathaPro.variables.avgMoneyCalcScathaPriceEpic);
        elements.add(setGridPosition(scathaPetPriceLegendaryInput = (NumberTextField) new NumberTextField(3, 10, 0, 97, 0).setPlaceholder("Legendary").setDefaultFormatting(EnumChatFormatting.GOLD.toString()), GridElementMode.CUSTOM_X));
        setDefaultNumber(scathaPetPriceLegendaryInput, scathaPro.variables.avgMoneyCalcScathaPriceLegendary);
        
        addGridGap();
        
        elements.add(setGridPosition(new ScathaProLabel(0, 0, 0, 97, "Magic Find:", Util.Color.AQUA), GridElementMode.CUSTOM_X));
        elements.add(setGridPosition(new ScathaProLabel(0, 10, 0, 96, "Pet Luck:", Util.Color.LIGHT_PURPLE), GridElementMode.CUSTOM_X));
        elements.add(setGridPosition(new ScathaProLabel(0, 10, 0, 97, "Scathas Per Hour:", Util.Color.RED), GridElementMode.CUSTOM_X));
        gridNewLine(2);
        elements.add(setGridPosition(magicFindInput = (NumberTextField) new NumberTextField(4, 0, 0, 97, 0).setPlaceholder("0").setDefaultFormatting(EnumChatFormatting.AQUA.toString()), GridElementMode.CUSTOM_X));
        setDefaultNumber(magicFindInput, scathaPro.variables.avgMoneyCalcMagicFind);
        elements.add(setGridPosition(petLuckInput = (NumberTextField) new NumberTextField(5, 10, 0, 96, 0).setPlaceholder("0").setDefaultFormatting(EnumChatFormatting.LIGHT_PURPLE.toString()), GridElementMode.CUSTOM_X));
        setDefaultNumber(petLuckInput, scathaPro.variables.avgMoneyCalcPetLuck);
        elements.add(setGridPosition(scathaRateInput = (NumberTextField) new NumberTextField(6, 10, 0, 97, 0).setPlaceholder("0").setDefaultFormatting(EnumChatFormatting.RED.toString()), GridElementMode.CUSTOM_X));
        setDefaultNumber(scathaRateInput, scathaPro.variables.avgMoneyCalcScathaRate);
        
        gridNewLine(20);
        
        elements.add(setGridPosition(resultLabel = new ScathaProLabel(0, 0, 0, 0, "[RESULT]"), GridElementMode.FULL_WIDTH));
        elements.add(setGridPosition(new ScathaProLabel(0, 0, 0, 0, "This is just the earned money from pet drops.\nPassive money from mined blocks etc. isn't included in\nthis calculation and gets added on top of this result!", Util.Color.GRAY), GridElementMode.FULL_WIDTH));
        
        addDoneButton();
        
        
        calculate();
    }
    
    private void setDefaultNumber(NumberTextField textField, float number)
    {
        if (number < 0f) return;
        textField.setText(TextUtil.numberToString(number, 3, false));
    }
    
    @Override
    public void onGuiClosed()
    {
        scathaPro.variables.avgMoneyCalcScathaPriceRare = getSaveValue(scathaPetPriceRareInput);
        scathaPro.variables.avgMoneyCalcScathaPriceEpic = getSaveValue(scathaPetPriceEpicInput);
        scathaPro.variables.avgMoneyCalcScathaPriceLegendary = getSaveValue(scathaPetPriceLegendaryInput);
        scathaPro.variables.avgMoneyCalcMagicFind = getSaveValue(magicFindInput);
        scathaPro.variables.avgMoneyCalcPetLuck = getSaveValue(petLuckInput);
        scathaPro.variables.avgMoneyCalcScathaRate = getSaveValue(scathaRateInput);
        
        scathaPro.getPersistentData().saveGlobalData();
    }
    
    private float getSaveValue(NumberTextField textField)
    {
        if (textField.getText().isEmpty()) return -1f;
        return floatOrZero(textField.getValue());
    }
    
    @Override
    protected void onTextFieldTyped(ScathaProTextField textField)
    {
        calculate();
    }
    
    private void calculate()
    {
        float priceRare = floatOrZero(scathaPetPriceRareInput.getValue());
        float priceEpic = floatOrZero(scathaPetPriceEpicInput.getValue());
        float priceLegendary = floatOrZero(scathaPetPriceLegendaryInput.getValue());
        float priceAverage = (6f*priceRare + 3f*priceEpic + priceLegendary) / 10f;
        
        float emf = floatOrZero(magicFindInput.getValue()) + floatOrZero(petLuckInput.getValue());
        
        float scathaSpawns = floatOrZero(scathaRateInput.getValue());
        
        float result = 0f;
        if (scathaSpawns > 0f) result = priceAverage / ((100f/(0.4f * (1f + emf/100f)))/scathaSpawns);
        
        resultLabel.setText(EnumChatFormatting.RESET + "Average Scatha farming profit: " + EnumChatFormatting.UNDERLINE + TextUtil.numberToString(result, 2) + " M coins/h");
    }
    
    private float floatOrZero(Float f)
    {
        return f != null ? f.floatValue() : 0f;
    }
    
    
    private class NumberTextField extends ScathaProTextField
    {
        public NumberTextField(int componentId, int x, int y, int width, int height)
        {
            super(componentId, x, y, width, height);
            
            super.setTextPredicate(new Predicate<String>() {
                @Override
                public boolean apply(String input)
                {
                    input = input.toLowerCase();
                    return !input.contains("-") && !input.contains("d") && !input.contains("f") && getValue(input) != null;
                }
            });
        }
        
        @Override
        public void setTextPredicate(Predicate<String> predicate) {}
        
        public Float getValue()
        {
            return this.getValue(getText());
        }
        
        private Float getValue(String input)
        {
            if (input.isEmpty() || input.equals(".")) return 0f;
            
            try
            {
                float value = Float.parseFloat(input);
                
                if (!Float.isFinite(value)) return null;
                
                return value;
            }
            catch (NumberFormatException e)
            {
                return null;
            }
        }
    }
}
