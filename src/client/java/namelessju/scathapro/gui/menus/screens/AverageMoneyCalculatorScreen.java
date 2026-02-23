package namelessju.scathapro.gui.menus.screens;

import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.screens.LayoutScreen;
import namelessju.scathapro.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.NonNull;

import java.math.RoundingMode;

public class AverageMoneyCalculatorScreen extends LayoutScreen
{
    private StringWidget resultLabel;
    private EditBox scathaPetPriceRareInput, scathaPetPriceEpicInput, scathaPetPriceLegendaryInput;
    private EditBox magicFindInput, petLuckInput, scathaRateInput;
    
    public AverageMoneyCalculatorScreen(ScathaPro scathaPro, Screen parentScreen)
    {
        super(scathaPro, Component.literal("Average Scatha Money Calculator"), true, parentScreen);
    }
    
    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader();
        
        GridBuilder gridBuilder = new GridBuilder(3);
        
        gridBuilder.addFullWidth(new StringWidget(Component.literal("Scatha Pet Prices (In Million):").withStyle(ChatFormatting.YELLOW), font), true, false);
        gridBuilder.addSingleCell(scathaPetPriceRareInput = makeEditBox(
            "Rare Scatha Pet Price (In Million)", "Rare", Style.EMPTY.withColor(ChatFormatting.BLUE)
        ));
        gridBuilder.addSingleCell(scathaPetPriceEpicInput = makeEditBox(
            "Epic Scatha Pet Price (In Million)", "Epic", Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE)
        ));
        gridBuilder.addSingleCell(scathaPetPriceLegendaryInput = makeEditBox(
            "Legendary Scatha Pet Price (In Million)", "Legendary", Style.EMPTY.withColor(ChatFormatting.GOLD)
        ));
        gridBuilder.addMultipliedGap(0.5f);
        gridBuilder.addSingleCell(new StringWidget(Component.literal("Magic Find:").withStyle(ChatFormatting.AQUA), font), true, false);
        gridBuilder.addSingleCell(new StringWidget(Component.literal("Pet Luck:").withStyle(ChatFormatting.LIGHT_PURPLE), font), true, false);
        gridBuilder.addEmptyCell();
        gridBuilder.addSingleCell(magicFindInput = makeEditBox(
            "Magic Find", "0", Style.EMPTY.withColor(ChatFormatting.AQUA)
        )).setTooltip(Tooltip.create(Component.literal("Remember to add\nBestiary Magic Find!").withStyle(ChatFormatting.GRAY)));
        gridBuilder.addSingleCell(petLuckInput = makeEditBox(
            "Pet Luck", "0", Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE)
        ));
        gridBuilder.addSingleCell(Button.builder(Component.literal("Import Saved EMF"),
            button -> {
                setValue(magicFindInput, scathaPro.persistentDataProfileManager.getTotalMagicFind());
                setValue(petLuckInput, scathaPro.getProfileData().petLuck.getOr(-1f));
            }
        ).build()).setTooltip(Tooltip.create(Component.literal("""
                Sets Magic Find and Pet Luck
                from the saved Scatha farming
                MF, Bestiary MF and PL values""").withStyle(ChatFormatting.GRAY)));
        gridBuilder.addMultipliedGap(0.5f);
        gridBuilder.addFullWidth(new StringWidget(Component.literal("Scatha Kills Per Hour:").withStyle(ChatFormatting.RED), font), true, false);
        gridBuilder.addFullWidth(scathaRateInput = makeEditBox(
            "Scatha Kills Per Hour", null, Style.EMPTY.withColor(ChatFormatting.RED)
        ));
        gridBuilder.addMultipliedGap(0.5f);
        gridBuilder.addFullWidth(resultLabel = new StringWidget(Component.empty(), font), false, false);
        gridBuilder.addFullWidth(new MultiLineTextWidget(Component.literal("""
                This is just the earned money from pet drops alone.
                Passive money from mined blocks etc. isn't included in
                this calculation and gets added on top of this result!"""
            ).withStyle(ChatFormatting.GRAY), font
        ).setCentered(true), false, false);
        
        gridBuilder.addToContent(layout);
        
        addDoneButtonFooter();
    }
    
    @Override
    protected void init()
    {
        super.init();
        
        setValue(scathaPetPriceRareInput, scathaPro.persistentData.avgMoneyCalcScathaPriceRare.get());
        setValue(scathaPetPriceEpicInput, scathaPro.persistentData.avgMoneyCalcScathaPriceEpic.get());
        setValue(scathaPetPriceLegendaryInput, scathaPro.persistentData.avgMoneyCalcScathaPriceLegendary.get());
        setValue(magicFindInput, scathaPro.coreManager.avgMoneyCalcMagicFind);
        setValue(petLuckInput, scathaPro.coreManager.avgMoneyCalcPetLuck);
        setValue(scathaRateInput, scathaPro.coreManager.avgMoneyCalcScathaRate);
        
        calculate();
    }
    
    @Override
    public void removed()
    {
        super.removed();
        
        scathaPro.coreManager.avgMoneyCalcMagicFind = getEditBoxNumberValue(magicFindInput, -1f);
        scathaPro.coreManager.avgMoneyCalcPetLuck = getEditBoxNumberValue(petLuckInput, -1f);
        scathaPro.coreManager.avgMoneyCalcScathaRate = getEditBoxNumberValue(scathaRateInput, -1f);
        
        scathaPro.persistentData.avgMoneyCalcScathaPriceRare.set(getEditBoxNumberValue(scathaPetPriceRareInput, null));
        scathaPro.persistentData.avgMoneyCalcScathaPriceEpic.set(getEditBoxNumberValue(scathaPetPriceEpicInput, null));
        scathaPro.persistentData.avgMoneyCalcScathaPriceLegendary.set(getEditBoxNumberValue(scathaPetPriceLegendaryInput, null));
        scathaPro.persistentData.save();
    }
    
    private void calculate()
    {
        float priceRare = getEditBoxNumberValue(scathaPetPriceRareInput, 0f);
        float priceEpic = getEditBoxNumberValue(scathaPetPriceEpicInput, 0f);
        float priceLegendary = getEditBoxNumberValue(scathaPetPriceLegendaryInput, 0f);
        float scathaRate = getEditBoxNumberValue(scathaRateInput, 0f);
        
        float result = -1f;
        
        if (priceRare > 0f && priceEpic > 0f && priceLegendary > 0f && scathaRate > 0f)
        {
            float anyChance = Constants.scathaPetBaseChanceRare + Constants.scathaPetBaseChanceEpic + Constants.scathaPetBaseChanceLegendary;
            
            float priceAverage = (Constants.scathaPetBaseChanceRare / anyChance) * priceRare
                + (Constants.scathaPetBaseChanceEpic / anyChance) * priceEpic
                + (Constants.scathaPetBaseChanceLegendary / anyChance) * priceLegendary;
            
            float emf = getEditBoxNumberValue(magicFindInput, 0f) + getEditBoxNumberValue(petLuckInput, 0f);
            
            result = priceAverage / ((1f / (anyChance * (1f + emf / 100f))) / scathaRate);
        }
        
        resultLabel.setMessage(
            Component.literal("Average Scatha farming profit: ")
            .append(Component.empty().withStyle(ChatFormatting.UNDERLINE)
                .append(TextUtil.numberToComponentOrObf(result, 2, true, RoundingMode.HALF_UP))
                .append(" M coins/h")
            )
        );
        getLayout().arrangeElements();
    }
    
    private EditBox makeEditBox(String name, String hint, Style valueStyle)
    {
        EditBox editBox = new EditBox(font, 0, 0, Component.literal(name));
        editBox.setFilter(value -> {
            if (value.isEmpty() || value.equals(".")) return true;
            try
            {
                float numberValue = Float.parseFloat(value);
                return numberValue >= 0f;
            }
            catch (NumberFormatException e)
            {
                return false;
            }
        });
        if (hint != null) editBox.setHint(Component.literal(hint));
        if (valueStyle != null) editBox.addFormatter(new EditBox.TextFormatter()
        {
            @Override
            public @NonNull FormattedCharSequence format(@NonNull String string, int i)
            {
                return FormattedCharSequence.forward(string, valueStyle);
            }
        });
        editBox.setResponder(value -> calculate());
        return editBox;
    }
    
    private void setValue(EditBox editBox, Float initalValue)
    {
        if (initalValue != null && initalValue >= 0f)
        {
            editBox.setValue(TextUtil.numberToString(initalValue, 3, false));
        }
        else editBox.setValue("");
    }
    
    private Float getEditBoxNumberValue(EditBox editBox, Float defaultValue)
    {
        if (editBox.getValue().isBlank()) return defaultValue;
        try
        {
            return Float.parseFloat(editBox.getValue());
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }
}
