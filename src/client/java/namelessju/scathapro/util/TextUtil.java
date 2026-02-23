package namelessju.scathapro.util;

import namelessju.scathapro.ScathaPro;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringDecomposer;
import org.jspecify.annotations.NonNull;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class TextUtil
{
    public static final String NEW_LINE_REGEX = "(?:\\r?\\n|\\r)";
    
    
    public static Integer parseInt(@NonNull String integerString)
    {
        try
        {
            return Integer.parseInt(integerString);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
    
    public static Float parseFloat(@NonNull String floatString)
    {
        floatString = floatString.trim();
        char lastCharLowerCase = Character.toLowerCase(floatString.charAt(floatString.length() - 1));
        if (lastCharLowerCase == 'f') return null;
        
        try
        {
            return Float.parseFloat(floatString);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
    
    public static @NonNull String numberToString(int number)
    {
        return numberToString(number, 0);
    }
    public static @NonNull String numberToString(double number, int maxDecimalPlaces)
    {
        return numberToString(number, maxDecimalPlaces, false);
    }
    public static @NonNull String numberToString(double number, int maxDecimalPlaces, boolean showTrailingDecimalZeros)
    {
        return numberToString(number, maxDecimalPlaces, showTrailingDecimalZeros, RoundingMode.HALF_UP);
    }
    public static @NonNull String numberToString(double number, int maxDecimalPlaces, boolean showTrailingDecimalZeros, @NonNull RoundingMode roundingMode)
    {
        // fix rounding issues due to floating point imprecision by adding a small delta
        double fixDelta = 0.00001D;
        if (Math.abs(number) > fixDelta)
        {
            switch (roundingMode)
            {
                case CEILING:
                    number -= fixDelta;
                    break;
                case FLOOR:
                    number += fixDelta;
                    break;
                
                case UP:
                case HALF_UP:
                case HALF_EVEN:
                    if (number < 0) number += fixDelta;
                    else number -= fixDelta;
                    break;
                
                case DOWN:
                case HALF_DOWN:
                    if (number < 0) number -= fixDelta;
                    else number += fixDelta;
                    break;
                
                default: break;
            }
        }
        
        DecimalFormatSymbols decimalSymbols = new DecimalFormatSymbols();
        decimalSymbols.setDecimalSeparator('.');
        decimalSymbols.setGroupingSeparator(',');
        DecimalFormat decimalFormat = new DecimalFormat("#,###.#", decimalSymbols);
        decimalFormat.setMaximumFractionDigits(maxDecimalPlaces);
        decimalFormat.setRoundingMode(roundingMode);
        if (showTrailingDecimalZeros) decimalFormat.setMinimumFractionDigits(maxDecimalPlaces);
        return decimalFormat.format(number);
    }
    
    public static @NonNull Component subString(@NonNull Component component, int start, int end)
    {
        MutableComponent subComponent = Component.empty();
        AtomicInteger currentLength = new AtomicInteger(0);
        component.visit((style, part) -> {
            if (currentLength.get() >= end)
            {
                return FormattedText.STOP_ITERATION;
            }
            else if (currentLength.get() + part.length() > start)
            {
                subComponent.append(Component.literal(part.substring(
                    Math.max(start - currentLength.get(), 0),
                    Math.min(end - currentLength.get(), part.length())
                )).setStyle(style));
            }
            currentLength.addAndGet(part.length());
            return Optional.empty();
        }, Style.EMPTY);
        return subComponent;
    }
    
    /**
     * Converts formatted text into a component with any legacy formatting codes translated into styled child components<br>
     * Note: this doesn't preserve the hierarchy, however the style for each character will be correct
     */
    public static @NonNull Component convertLegacyFormatting(@NonNull FormattedText text)
    {
        MutableComponent convertedText = Component.empty();
        text.visit((style, string) -> {
            AtomicReference<Style> currentCharStyle = new AtomicReference<>(Style.EMPTY);
            AtomicReference<StringBuilder> stringBuilder = new AtomicReference<>(new StringBuilder());
            StringDecomposer.iterateFormatted(string, Style.EMPTY, (i, charStyle, c) -> {
                if (!charStyle.equals(currentCharStyle.get()))
                {
                    if (!stringBuilder.get().isEmpty())
                    {
                        convertedText.append(Component.literal(stringBuilder.toString()).setStyle(style.applyTo(currentCharStyle.get())));
                    }
                    currentCharStyle.set(charStyle);
                    stringBuilder.set(new StringBuilder());
                }
                stringBuilder.get().appendCodePoint(c);
                return true;
            });
            if (!stringBuilder.get().isEmpty())
            {
                convertedText.append(Component.literal(stringBuilder.toString()).setStyle(style.applyTo(currentCharStyle.get())));
            }
            return Optional.empty();
        }, Style.EMPTY);
        return convertedText;
    }
    
    /**
     * If the number is equal to or greater than 0 returns it as a string, otherwise returns a single obfuscated character
     */
    public static @NonNull Component numberToComponentOrObf(int number)
    {
        return number >= 0
            ? Component.literal(TextUtil.numberToString(number))
            : Component.literal("?").withStyle(ChatFormatting.OBFUSCATED);
    }
    /**
     * If the number is equal to or greater than 0 returns a formatted number string with a maximum of 2 decimal places (using half up rounding), otherwise returns a single obfuscated character
     */
    public static @NonNull Component numberToComponentOrObf(double number)
    {
        return numberToComponentOrObf(number, 2, false, RoundingMode.HALF_UP);
    }
    /**
     * If the number is equal to or greater than 0 returns a formatted number string, otherwise returns a single obfuscated character
     */
    public static @NonNull Component numberToComponentOrObf(double number, int maxDecimalPlaces, boolean showTrailingDecimalZeros, RoundingMode roundingMode)
    {
        return number >= 0D
            ? Component.literal(TextUtil.numberToString(number, maxDecimalPlaces, showTrailingDecimalZeros, roundingMode))
            : Component.literal("?").withStyle(ChatFormatting.OBFUSCATED);
    }
    
    public static @NonNull ChatFormatting contrastableGray(@NonNull ScathaPro scathaPro)
    {
        return handleContrast(scathaPro, ChatFormatting.GRAY);
    }
    
    public static @NonNull ChatFormatting handleContrast(@NonNull ScathaPro scathaPro, @NonNull ChatFormatting color)
    {
        if (scathaPro.config.accessibility.useHighContrastColors.get())
        {
            return ChatFormatting.WHITE;
        }
        return color;
    }
    
    /*
    public static String ellipsis(String original, int maxWidth)
    {
        int strWidth = getStringWidth(original);
        int ellipsisWidth = getStringWidth("...");

        if (strWidth > maxWidth)
        {
            if (maxWidth >= ellipsisWidth)
            {
                return trimStringToWidth(original, maxWidth - ellipsisWidth) + "...";
            }
            else return "";
        }
        return original;
    }
    */
    
    public static @NonNull Component getColorSeriesText(@NonNull String text, @NonNull ChatFormatting[] formattingOrder)
    {
        MutableComponent component = Component.empty();
        int formattingIndex = 0;
        for (int i = 0; i < text.length(); i ++)
        {
            char c = text.charAt(i);
            MutableComponent charComponent = Component.literal(String.valueOf(c));
            if (c != ' ')
            {
                charComponent.withStyle(formattingOrder[formattingIndex]);
                formattingIndex = (formattingIndex + 1) % formattingOrder.length;
            }
            component.append(charComponent);
        }
        return component;
    }
    
    public static @NonNull Component getRainbowText(@NonNull String text)
    {
        return getColorSeriesText(text, new ChatFormatting[] {
            ChatFormatting.RED, ChatFormatting.GOLD, ChatFormatting.YELLOW,
            ChatFormatting.GREEN, ChatFormatting.DARK_AQUA, ChatFormatting.DARK_PURPLE
        });
    }
    
    
    private TextUtil() {}
}
