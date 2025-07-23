package namelessju.scathapro.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.managers.Config;
import namelessju.scathapro.managers.Config.Key;
import namelessju.scathapro.miscellaneous.enums.DropMessageRarityMode;
import namelessju.scathapro.miscellaneous.enums.DropMessageStatsMode;
import namelessju.scathapro.miscellaneous.enums.Rarity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;

public abstract class TextUtil
{
    public static final String formattingStartCharacter = "\u00a7";
    public static final String formattingCodesRegex = "[0-9a-fA-Fk-oK-OrR]";
    public static final String formattingColorCodesRegex = "[0-9a-fA-F]";
    
    // public static final ChatComponentText dividerComponent = new ChatComponentText(Constants.msgHighlightingColor + EnumChatFormatting.BOLD + new String(new char[64]).replace("\0", Util.getUnicodeString("25AC")));
    public static final ChatComponentText chatDividerComponent = new ChatComponentText("");
    
    
    public static EntityPlayer getPlayerForChat()
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player != null && player.worldObj != null) return player;
        return null;
    }
    
    public static void sendChatMessage(IChatComponent chatComponent)
    {
        EntityPlayer player = getPlayerForChat();
        if (player != null)
        {
            ClientChatReceivedEvent event = new ClientChatReceivedEvent((byte) 1, chatComponent);
            MinecraftForge.EVENT_BUS.post(event);
            if (!event.isCanceled()) player.addChatMessage(event.message);
        }
        else
        {
            ScathaPro.getInstance().variables.cachedChatMessages.add(chatComponent);
            ScathaPro.getInstance().logDebug("Chat message cached: " + chatComponent.getFormattedText());
        }
    }
    
    public static void sendModChatMessage(String message)
    {
        sendModChatMessage(message, true);
    }
    
    public static void sendModChatMessage(String message, boolean prefix)
    {
        String[] lines = splitOnLineBreaks(message);
        for (int i = 0; i < lines.length; i ++)
        {
            String line = lines[i];
            sendModChatMessage(new ChatComponentText(EnumChatFormatting.RESET + line), prefix && i == 0);
        }
    }
    
    public static void sendModChatMessage(IChatComponent chatComponent)
    {
        sendModChatMessage(chatComponent, true);
    }
    
    public static void sendModChatMessage(IChatComponent chatComponent, boolean prefix)
    {
        sendChatMessage(prefix ? getModMessageComponent(chatComponent) : chatComponent);
    }
    
    public static void sendCrystalHollowsMessage(IChatComponent chatComponent)
    {
        if (ScathaPro.getInstance().isInCrystalHollows()) sendChatMessage(chatComponent);
        else ScathaPro.getInstance().variables.cachedCrystalHollowsMessages.add(chatComponent);
    }
    
    public static IChatComponent getModMessageComponent(IChatComponent component)
    {
        IChatComponent messageComponent = new ChatComponentText("");
        
        String prefixText = ScathaPro.getInstance().getConfig().getBoolean(Config.Key.shortChatPrefix) ? Constants.chatPrefixShort : Constants.chatPrefix;
        messageComponent.appendSibling(new ChatComponentText(prefixText));
        
        messageComponent.appendSibling(component);
        
        return messageComponent;
    }

    public static IChatComponent getModMessageComponent(String message)
    {
        return getModMessageComponent(new ChatComponentText(message));
    }
    
    public static void sendModErrorMessage(String errorMessage)
    {
        sendModChatMessage(EnumChatFormatting.RED + errorMessage);
    }

    public static void sendDevModeMessage(String message)
    {
        String[] lines = splitOnLineBreaks(message);
        for (int i = 0; i < lines.length; i ++)
        {
            String line = lines[i];
            if (i == 0)
            {
                line = Constants.chatPrefixDev + line;
            }
            sendModChatMessage(new ChatComponentText(EnumChatFormatting.RESET + line), false);
        }
    }
    
    public static void sendPetDropMessage(Rarity rarity, int magicFind)
    {
        char rarityColorCode;
        switch (rarity)
        {
            case RARE:
                rarityColorCode = '9';
                break;
            case EPIC:
                rarityColorCode = '5';
                break;
            case LEGENDARY:
                rarityColorCode = '6';
                break;
            default:
                rarityColorCode = 'f';
        }
        
        String f = TextUtil.formattingStartCharacter;
        sendChatMessage(new ChatComponentText(f+"6"+f+"lPET DROP! "+f+"r"+f+rarityColorCode+"Scatha "+f+"r"+f+"b"+"(+"+f+"r"+f+"b"+magicFind+"% "+f+"r"+f+"b"+UnicodeSymbol.magicFind+" Magic Find"+f+"r"+f+"b"+")"));
    }
    
    public static void sendChatDivider()
    {
        // Prevent consecutive dividers
        List<ChatLine> chatLines = ScathaPro.getInstance().variables.chatLines;
        if (chatLines != null && chatLines.size() > 0)
        {
            String lastChatLine = chatLines.get(0).getChatComponent().getFormattedText();
            if (StringUtils.stripControlCodes(lastChatLine).equals(StringUtils.stripControlCodes(chatDividerComponent.getFormattedText())))
            {
                return;
            }
        }
        
        sendChatMessage(chatDividerComponent);
    }
    
    public static void addChatCopyButton(IChatComponent message)
    {
        if (!ScathaPro.getInstance().getConfig().getBoolean(Config.Key.chatCopy)) return;
        
        String unformattedText = StringUtils.stripControlCodes(message.getFormattedText());
        if (unformattedText.replace(" ", "").isEmpty()) return; 
        
        ChatComponentText chatCopyButtonComponent = new ChatComponentText(EnumChatFormatting.RESET.toString() + EnumChatFormatting.DARK_GRAY + "\u270D");
        ChatStyle style = new ChatStyle()
                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GRAY + "Click to copy message\n" + EnumChatFormatting.GRAY + "into chat input field")))
                .setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, unformattedText.replaceAll("\n+", " ")));
        chatCopyButtonComponent.setChatStyle(style);
        
        message.appendText(EnumChatFormatting.RESET + " ");
        message.appendSibling(chatCopyButtonComponent);
        
        message = fixTextComponentBold(message);
    }

    public static String numberToString(int number)
    {
        return numberToString(number, 0);
    }
    public static String numberToString(double number, int maxDecimalPlaces)
    {
        return numberToString(number, maxDecimalPlaces, false);
    }
    public static String numberToString(double number, int maxDecimalPlaces, boolean showTrailingDecimalZeros)
    {
        return numberToString(number, maxDecimalPlaces, showTrailingDecimalZeros, RoundingMode.HALF_UP);
    }
    public static String numberToString(double number, int maxDecimalPlaces, boolean showTrailingDecimalZeros, RoundingMode roundingMode)
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
    
    /**
     * If the number is equal to or greater than 0 returns it as a string, otherwise returns a single obfuscated character
     */
    public static String getObfNrStr(int number)
    {
        return number >= 0 ? TextUtil.numberToString(number) : EnumChatFormatting.OBFUSCATED + "?" + EnumChatFormatting.RESET;
    }
    /**
     * If the number is equal to or greater than 0 returns a formatted number string with a maximum of 2 decimal places (using half up rounding), otherwise returns a single obfuscated character
     */
    public static String getObfNrStr(double number)
    {
        return getObfNrStr(number, 2, false, RoundingMode.HALF_UP);
    }
    /**
     * If the number is equal to or greater than 0 returns a formatted number string, otherwise returns a single obfuscated character
     */
    public static String getObfNrStr(double number, int maxDecimalPlaces, boolean showTrailingDecimalZeros, RoundingMode roundingMode)
    {
        return number >= 0D ? TextUtil.numberToString(number, maxDecimalPlaces, showTrailingDecimalZeros, roundingMode) : EnumChatFormatting.OBFUSCATED + "?" + EnumChatFormatting.RESET;
    }
    
    public static Integer parseInt(String integerString)
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
    
    public static Float parseFloat(String integerString)
    {
        integerString = integerString.trim();
        char lastCharLowerCase = Character.toLowerCase(integerString.charAt(integerString.length() - 1));
        if (lastCharLowerCase == 'f') return null;
        
        try
        {
            return Float.parseFloat(integerString);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    /**
     * Fixes chat messages that contain bold text since the implementation of
     * FontRenderer#getStringWidth() doesn't clear the boldness after a color code
     * but the actual visual behaviour does, which leads to hover & click events being offset
     */
    public static IChatComponent fixTextComponentBold(IChatComponent component)
    {
        IChatComponent newComponent;
        
        if (component instanceof ChatComponentText)
        {
            ChatComponentText originalComponent = (ChatComponentText) component;
            final String f = formattingStartCharacter;
            String fixedText = originalComponent.getChatComponentText_TextValue().replaceAll("(?<!"+f+"r)"+f+"(?="+formattingColorCodesRegex+")", f+"r"+f);
            newComponent = new ChatComponentText(fixedText);
        }
        else return component;
        
        for (IChatComponent sibling : component.getSiblings())
        {
            newComponent.appendSibling(fixTextComponentBold(sibling));
        }
        
        newComponent.setChatStyle(component.getChatStyle());
        return newComponent;
    }
    
    public static void displayTitle(String title, String subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks)
    {
        if (title != null || subtitle != null) ScathaPro.getInstance().getAlertTitleOverlay().clearTitle();
        
        Minecraft mc = Minecraft.getMinecraft();
        mc.ingameGUI.displayTitle(null, null, fadeInTicks, stayTicks, fadeOutTicks);
        mc.ingameGUI.displayTitle(null, subtitle, 0, 0, 0);
        mc.ingameGUI.displayTitle(title, null, 0, 0, 0);
    }
    
    public static void clearTitle()
    {
        Minecraft.getMinecraft().ingameGUI.displayTitle(null, null, -1, -1, -1);
    }
    
    public static EnumChatFormatting contrastableGray()
    {
        return handleContrast(EnumChatFormatting.GRAY);
    }
    
    public static EnumChatFormatting handleContrast(EnumChatFormatting color)
    {
        if (color.getColorIndex() < 0) return color; // Not a color, return as is
        if (ScathaPro.getInstance().getConfig().getBoolean(Config.Key.highContrastColors)) return EnumChatFormatting.WHITE;
        return color;
    }

    public static String[] splitOnLineBreaks(String string)
    {
        return splitOnLineBreaks(string, true);
    }
    
    public static String[] splitOnLineBreaks(String string, boolean applyFormattingFromPreviousLines)
    {
        String[] lines = string.split("\\r?\\n|\\r");
        
        if (applyFormattingFromPreviousLines)
        {
            for (int i = 1; i < lines.length; i ++)
            {
                String formatting = "";
                Matcher formattingCodeMatcher = Pattern.compile(TextUtil.formattingStartCharacter + TextUtil.formattingCodesRegex).matcher(lines[i - 1]);
                while (formattingCodeMatcher.find())
                {
                    String code = formattingCodeMatcher.group();
                    char codeChar = code.charAt(1);
                    boolean isResetCode = codeChar == 'r' || codeChar == 'R';
                    if (isResetCode || TextUtil.isColorFormattingCode(codeChar))
                    {
                        formatting = isResetCode ? "" : code;
                        continue;
                    }
                    formatting += code;
                }
                
                lines[i] = formatting + lines[i];
            }
        }
        
        return lines;
    }
    
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
    
    public static boolean isFormattingSequence(String string)
    {
        return string.matches(formattingStartCharacter + formattingCodesRegex);
    }
    
    public static boolean isColorFormattingCode(char formattingCode)
    {
        if ('0' <= formattingCode && formattingCode <= '9') return true;
        formattingCode = Character.toLowerCase(formattingCode);
        return 'a' <= formattingCode && formattingCode <= 'f';
    }
    
    public static boolean isFancyFormattingCode(char formattingCode)
    {
        formattingCode = Character.toLowerCase(formattingCode);
        return 'k' <= formattingCode && formattingCode <= 'o';
    }
    
    public static boolean isBoldFormattingCode(char formattingCode)
    {
        return formattingCode == 'l' || formattingCode == 'L';
    }
    
    /**
     * Fixed version of {@link net.minecraft.client.gui.FontRenderer#getStringWidth(String text)}
     */
    public static int getStringWidth(String text)
    {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        
        if (fontRenderer == null || text == null)
        {
            return 0;
        }
        else
        {
            int width = 0;
            boolean isBold = false;

            for (int i = 0; i < text.length(); i++)
            {
                char currentChar = text.charAt(i);
                int charWidth = fontRenderer.getCharWidth(currentChar);
                
                if (charWidth < 0 && i < text.length() - 1)
                {
                    i ++;
                    currentChar = text.charAt(i);
                    
                    if (isBoldFormattingCode(currentChar))
                    {
                        isBold = true;
                    }
                    else if (!isFancyFormattingCode(currentChar)) // FIXED: resets boldness at all non-fancy styling characters instead of just the reset formatting code
                    {
                        isBold = false;
                    }

                    charWidth = 0;
                }
                
                width += charWidth;

                if (isBold && charWidth > 0)
                {
                    width ++;
                }
            }

            return width;
        }
    }

    /**
     * @see TextUtil#trimStringToWidth(String text, int width, boolean reverse)
     */
    public static String trimStringToWidth(String text, int width)
    {
        return trimStringToWidth(text, width, false);
    }
    
    /**
     * Fixed version of {@link net.minecraft.client.gui.FontRenderer#trimStringToWidth(String text, int width, boolean reverse)}
     */
    public static String trimStringToWidth(String text, int width, boolean reverse)
    {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        if (text == null || fontRenderer == null) return text;
        
        StringBuilder stringBuilder = new StringBuilder();
        int totalWidth = 0;
        int startIndex = reverse ? text.length() - 1 : 0;
        int processingDirection = reverse ? -1 : 1;
        boolean isFormattingCode = false;
        boolean isBold = false;
        
        for (int i = startIndex; i >= 0 && i < text.length() && totalWidth < width; i += processingDirection)
        {
            char currentChar = text.charAt(i);
            int charWidth = fontRenderer.getCharWidth(currentChar);
            
            if (isFormattingCode)
            {
                isFormattingCode = false;

                if (isBoldFormattingCode(currentChar))
                {
                    isBold = true;
                }
                else if (!isFancyFormattingCode(currentChar)) // FIXED: resets boldness at all non-fancy styling characters instead of just the reset formatting code
                {
                    isBold = false;
                }
            }
            else if (charWidth < 0)
            {
                isFormattingCode = true;
            }
            else
            {
                totalWidth += charWidth;

                if (isBold)
                {
                    totalWidth ++;
                }
            }

            if (totalWidth > width)
            {
                break;
            }

            if (reverse) stringBuilder.insert(0, currentChar);
            else stringBuilder.append(currentChar);
        }

        return stringBuilder.toString();
    }
    
    public static String getColorSeriesText(String text, EnumChatFormatting[] formattingOrder)
    {
        StringBuilder formattedString = new StringBuilder();
        int formattingIndex = 0;
        for (int i = 0; i < text.length(); i ++)
        {
            char c = text.charAt(i);
            if (c != ' ')
            {
                formattedString.append(formattingOrder[formattingIndex].toString() + c);
                formattingIndex = (formattingIndex + 1) % formattingOrder.length;
            }
            else formattedString.append(c);
        }
        return formattedString.toString();
    }
    
    public static String getRainbowText(String text)
    {
        return getColorSeriesText(text, new EnumChatFormatting[] {EnumChatFormatting.RED, EnumChatFormatting.GOLD, EnumChatFormatting.YELLOW, EnumChatFormatting.GREEN, EnumChatFormatting.DARK_AQUA, EnumChatFormatting.DARK_PURPLE});
    }
    
    public static IChatComponent extendPetDropMessage(String message)
    {
        Config config = ScathaPro.getInstance().getConfig();
        
        String f = TextUtil.formattingStartCharacter;
        String aqua = f+"r"+f+"b";
        String patternString = f+"6"+f+"lPET DROP! "+f+"r"+f+"([a-f0-9])Scatha "+aqua+"\\(\\+"+aqua+"(\\d+)% "+aqua+UnicodeSymbol.magicFind+" Magic Find"+aqua+"\\)";
        Matcher messageMatcher = Pattern.compile(patternString).matcher(message);
        if (messageMatcher.find())
        {
            String rarityColorCode = messageMatcher.group(1);
            String magicFind = messageMatcher.group(2);
            
            // Add pet rarity
            
            String petNameString = f+rarityColorCode+"Scatha";
            
            DropMessageRarityMode rarityMode = config.getEnum(Config.Key.dropMessageRarityMode, DropMessageRarityMode.class);
            if (rarityMode != null)
            {
                String rarityText = null;
                if (rarityColorCode.equals("9")) rarityText = "Rare";
                else if (rarityColorCode.equals("5")) rarityText = "Epic";
                else if (rarityColorCode.equals("6")) rarityText = "Legendary";
                
                if (rarityText != null)
                {
                    String rarityString = rarityText;
                    if (config.getBoolean(Key.dropMessageRarityUppercase)) rarityString = EnumChatFormatting.BOLD + rarityString.toUpperCase();
                    String rarityStringColor = config.getBoolean(Key.dropMessageRarityColored) ? (f+rarityColorCode) : EnumChatFormatting.DARK_GRAY.toString();
                    
                    if (rarityMode.hasBrackets) rarityString = rarityStringColor+"["+rarityString+EnumChatFormatting.RESET+rarityStringColor+"]";
                    else rarityString = rarityStringColor+rarityString+EnumChatFormatting.RESET;
                    
                    if (rarityMode.isPrefix) petNameString = rarityString + " " + petNameString;
                    else petNameString = petNameString + " " + rarityString;
                }
                else ScathaPro.getInstance().logWarning("Scatha pet drop extension: encountered unknown rarity color code \"" + f + rarityColorCode + "\"!");
            }
            
            // Extend Stats
            
            ChatComponentText statsComponent;
            
            boolean statsAbbreviated = config.getBoolean(Config.Key.dropMessageStatAbbreviations);
            
            String magicFindStatNameString = statsAbbreviated ? "MF" : "Magic Find";
            String magicFindString;
            if (config.getBoolean(Config.Key.dropMessageCleanMagicFind)) magicFindString = EnumChatFormatting.AQUA + magicFind + " " + UnicodeSymbol.magicFind + " " + magicFindStatNameString;
            else magicFindString = EnumChatFormatting.AQUA + "+" + magicFind + "% " + UnicodeSymbol.magicFind + " " + magicFindStatNameString;
            
            DropMessageStatsMode statsMode = config.getEnum(Config.Key.dropMessageStatsMode, DropMessageStatsMode.class);
            if (statsMode != null)
            {
                statsComponent = new ChatComponentText(EnumChatFormatting.GRAY + "(");
                boolean statElementAdded = false;
                
                if (statsMode.showMagicFind)
                {
                    statsComponent.appendSibling(new ChatComponentText(magicFindString));
                    statElementAdded = true;
                }

                if (statsMode.showPetLuck)
                {
                    if (statElementAdded) statsComponent.appendSibling(new ChatComponentText(EnumChatFormatting.GRAY + ", "));
                    
                    ChatComponentText petLuckNumberComponent;
                    int petLuck = (int) Math.floor(ScathaPro.getInstance().variables.petLuck);
                    if (petLuck >= 0) petLuckNumberComponent = new ChatComponentText(EnumChatFormatting.LIGHT_PURPLE + Integer.toString(petLuck));
                    else petLuckNumberComponent = new ChatComponentText(EnumChatFormatting.LIGHT_PURPLE.toString() + EnumChatFormatting.OBFUSCATED + "?");
                    /*
                    petLuckNumberComponent.getChatStyle()
                        .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GRAY + "Click or use \"/sp profileStats update\"\n" + EnumChatFormatting.GRAY + "to set the displayed Pet Luck value")))
                        .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sp profileStats update"));
                    */
                    statsComponent.appendSibling(petLuckNumberComponent);
                    
                    String petLuckStatNameString = statsAbbreviated ? "PL" : "Pet Luck";
                    statsComponent.appendSibling(new ChatComponentText(EnumChatFormatting.LIGHT_PURPLE + " " + UnicodeSymbol.petLuck + " " + petLuckStatNameString));
                    statElementAdded = true;
                }
                
                if (statsMode.emfMode != null)
                {
                    if (statElementAdded) statsComponent.appendSibling(new ChatComponentText(EnumChatFormatting.GRAY + ", "));
                    
                    int magicFindValue = -1;
                    try
                    {
                        magicFindValue = Integer.parseInt(magicFind);
                    }
                    catch (NumberFormatException ignored) {}
                    
                    int petLuck = (int) Math.floor(ScathaPro.getInstance().variables.petLuck);

                    ChatComponentText emfNumberComponent;
                    if (magicFindValue < 0)
                    {
                        emfNumberComponent = new ChatComponentText(EnumChatFormatting.RED.toString() + EnumChatFormatting.OBFUSCATED + "?");
                        emfNumberComponent.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.RED + "Failed to read Magic Find\n" + EnumChatFormatting.RED + "value from message!")));
                    }
                    else
                    {
                        if (petLuck < 0) emfNumberComponent = new ChatComponentText(EnumChatFormatting.BLUE.toString() + EnumChatFormatting.OBFUSCATED + "?");
                        else emfNumberComponent = new ChatComponentText(EnumChatFormatting.BLUE + Integer.toString(magicFindValue + petLuck));
                        /*
                        emfNumberComponent.getChatStyle()
                            .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GRAY + "Click or use \"/sp profileStats update\"\n" + EnumChatFormatting.GRAY + "to set the displayed EMF value")))
                            .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sp profileStats update"));
                        */
                    }
                    
                    String emfString;
                    switch (statsMode.emfMode)
                    {
                        case FULL:
                            emfString = "Effective Magic Find";
                            break;
                        case SHORT:
                        default:
                            emfString = "EMF";
                    }
                    
                    statsComponent.appendSibling(emfNumberComponent);
                    statsComponent.appendSibling(new ChatComponentText(EnumChatFormatting.BLUE + " " + emfString));
                    
                    statElementAdded = true;
                }
                
                if (statElementAdded) statsComponent.appendSibling(new ChatComponentText(EnumChatFormatting.GRAY + ")"));
                else statsComponent = null;
            }
            else statsComponent = new ChatComponentText(EnumChatFormatting.AQUA + "(" + magicFindString + EnumChatFormatting.AQUA + ")");
            
            // New message
            
            ChatComponentText newMessage = new ChatComponentText(EnumChatFormatting.GOLD.toString()+EnumChatFormatting.BOLD+"PET DROP! "+EnumChatFormatting.RESET+petNameString);
            if (statsComponent != null)
            {
                newMessage.appendSibling(new ChatComponentText(" "));
                newMessage.appendSibling(statsComponent);
            }
            return newMessage;
        }
        
        return null;
    }
    
    
    private TextUtil() {}
}
