package namelessju.scathapro.util;

import namelessju.scathapro.ScathaPro;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringDecomposer;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtil
{
    public static final String NEW_LINE_REGEX = "(?:\\r?\\n|\\r)";
    public static final ChatFormatting[] RAINBOW_TEXT_COLORS = new ChatFormatting[] {
        ChatFormatting.RED, ChatFormatting.GOLD, ChatFormatting.YELLOW, ChatFormatting.GREEN, ChatFormatting.DARK_AQUA, ChatFormatting.DARK_PURPLE
    };
    public static final Style ICON_STYLE = Style.EMPTY.withColor(ChatFormatting.WHITE).withoutShadow();

    private static final Pattern FORMATTING_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");
    private static final Pattern USER_FORMATTING_PATTERN = Pattern.compile("(?i)&[0-9A-FK-OR]");


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

    public static @NonNull FormattedText subStringFormatted(@NonNull FormattedText formattedText, int start)
    {
        return subStringFormatted(formattedText, start, Integer.MAX_VALUE);
    }

    public static @NonNull FormattedText subStringFormatted(@NonNull FormattedText formattedText, int start, int end)
    {
        MutableComponent subComponent = Component.empty();
        AtomicInteger currentLength = new AtomicInteger(0);
        formattedText.visit((style, part) -> {
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

    public static @NonNull Style styleWithFormattingCode(@NonNull Style style, char code)
    {
        return switch (Character.toLowerCase(code)) {
            case 'a' -> style.withColor(ChatFormatting.GREEN);
            case 'b' -> style.withColor(ChatFormatting.AQUA);
            case 'c' -> style.withColor(ChatFormatting.RED);
            case 'd' -> style.withColor(ChatFormatting.LIGHT_PURPLE);
            case 'e' -> style.withColor(ChatFormatting.YELLOW);
            case 'f' -> style.withColor(ChatFormatting.WHITE);
            case '0' -> style.withColor(ChatFormatting.BLACK);
            case '1' -> style.withColor(ChatFormatting.DARK_BLUE);
            case '2' -> style.withColor(ChatFormatting.DARK_GREEN);
            case '3' -> style.withColor(ChatFormatting.DARK_AQUA);
            case '4' -> style.withColor(ChatFormatting.DARK_RED);
            case '5' -> style.withColor(ChatFormatting.DARK_PURPLE);
            case '6' -> style.withColor(ChatFormatting.GOLD);
            case '7' -> style.withColor(ChatFormatting.GRAY);
            case '8' -> style.withColor(ChatFormatting.DARK_GRAY);
            case '9' -> style.withColor(ChatFormatting.BLUE);
            case 'k' -> style.withObfuscated(true);
            case 'l' -> style.withBold(true);
            case 'm' -> style.withStrikethrough(true);
            case 'n' -> style.withUnderlined(true);
            case 'o' -> style.withItalic(true);
            case 'r' -> Style.EMPTY;
            default -> style;
        };
    }

    @Contract("!null->!null;_->_")
    public static @Nullable String removeLegacyFormatting(final @Nullable String text)
    {
        return text == null ? null : FORMATTING_PATTERN.matcher(text).replaceAll("");
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
            StringDecomposer.iterateFormatted(string, Style.EMPTY, (_, charStyle, c) -> {
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
     * Converts a raw string with "&" formatting codes into a formatted component.
     * @param showCodes determines whether the formatting codes should stay included in the components text content
     */
    public static @NonNull Component userFormattedStringToComponent(@NonNull String string, boolean showCodes)
    {
        MutableComponent component = Component.empty();
        visitUserFormattedString(string, (formattingCode, formattedSequence) -> {
            if (showCodes && formattingCode != null)
            {
                component.append(Component.literal(formattingCode).withStyle(ChatFormatting.DARK_GRAY));
            }
            component.append(formattedSequence);
        });
        return component;
    }

    private static void visitUserFormattedString(@NonNull String string, BiConsumer<String, Component> consumer)
    {
        Matcher userFormattingMatcher = USER_FORMATTING_PATTERN.matcher(string);
        Style style = Style.EMPTY;
        String previousFormattingCode = null;
        int previousStartIndex = 0;
        while (userFormattingMatcher.find())
        {
            // consume previous segment
            consumer.accept(previousFormattingCode,
                Component.literal(string.substring(previousStartIndex, userFormattingMatcher.start()))
                    .setStyle(style)
            );

            style = TextUtil.styleWithFormattingCode(style, userFormattingMatcher.group().charAt(1));

            previousFormattingCode = userFormattingMatcher.group();
            previousStartIndex = userFormattingMatcher.end();
        }

        // consume remaining segment
        consumer.accept(previousFormattingCode,
            Component.literal(string.substring(previousStartIndex))
                .setStyle(style)
        );
    }

    /**
     * If the number is equal to or greater than 0 returns it as a string, otherwise returns a single obfuscated character
     */
    public static @NonNull Component numberToComponentOrObf(int number)
    {
        return number >= 0
            ? Component.literal(TextUtil.numberToString(number))
            : Component.literal("?").withStyle(Style.EMPTY.withObfuscated(true));
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
            : Component.literal("?").withStyle(Style.EMPTY.withObfuscated(true));
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

    public static @NonNull Component getColorSeriesText(@NonNull String text, @NonNull ChatFormatting[] orderedColors)
    {
        MutableComponent component = Component.empty();
        int formattingIndex = 0;
        for (int i = 0; i < text.length(); i ++)
        {
            char c = text.charAt(i);
            MutableComponent charComponent = Component.literal(String.valueOf(c));
            if (c != ' ')
            {
                charComponent.withStyle(orderedColors[formattingIndex]);
                formattingIndex = (formattingIndex + 1) % orderedColors.length;
            }
            component.append(charComponent);
        }
        return component;
    }

    public static @NonNull Component getRainbowText(@NonNull String text)
    {
        return getColorSeriesText(text, RAINBOW_TEXT_COLORS);
    }


    private TextUtil() {}
}