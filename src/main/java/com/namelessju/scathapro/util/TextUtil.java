package com.namelessju.scathapro.util;

import com.namelessju.scathapro.Constants;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.managers.Config;

import net.minecraft.client.Minecraft;
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
    public static final String formattingCodesRegex = "[0-9a-fk-or]";
    public static final String formattingColorCodesRegex = "[0-9a-f]";
    
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
        IChatComponent messageComponent = new ChatComponentText("");
        
        if (prefix)
        {
            String prefixText = ScathaPro.getInstance().getConfig().getBoolean(Config.Key.shortChatPrefix) ? Constants.chatPrefixShort : Constants.chatPrefix;
            messageComponent.appendSibling(new ChatComponentText(prefixText));
        }
        
        messageComponent.appendSibling(chatComponent);
        
        sendChatMessage(messageComponent);
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
    
    public static void sendChatDivider()
    {
        if (ScathaPro.getInstance().variables.lastChatMessageIsDivider) return; // prevent consecutive dividers
        sendChatMessage(chatDividerComponent);
    }
    
    public static void addChatCopyButton(IChatComponent message)
    {
        if (!ScathaPro.getInstance().getConfig().getBoolean(Config.Key.chatCopy)) return;
        
        String unformattedText = StringUtils.stripControlCodes(message.getFormattedText());
        if (unformattedText.replace(" ", "").isEmpty()) return; 
        
        ChatComponentText chatCopyButtonComponent = new ChatComponentText(EnumChatFormatting.RESET.toString() + EnumChatFormatting.DARK_GRAY + "\u270D");
        ChatStyle style = new ChatStyle()
                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GRAY + "Copy message")))
                .setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, unformattedText.replace("\n", " ")));
        chatCopyButtonComponent.setChatStyle(style);
        
        message.appendText(EnumChatFormatting.RESET + " ");
        message.appendSibling(chatCopyButtonComponent);
        
        message = fixTextComponentBold(message);
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
        Minecraft mc = Minecraft.getMinecraft();
        mc.ingameGUI.displayTitle(null, null, fadeInTicks, stayTicks, fadeOutTicks);
        mc.ingameGUI.displayTitle(null, subtitle, 0, 0, 0);
        mc.ingameGUI.displayTitle(title, null, 0, 0, 0);
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
        return string.split("\\r?\\n|\\r");
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
    
    /**
     * Returns whether the char is a fancy formatting code (e.g. bold, italic - not colors!)
     */
    public static boolean isFancyFormattingCode(char formattingCode)
    {
        return formattingCode >= 'k' && formattingCode <= 'o' || formattingCode >= 'K' && formattingCode <= 'O';
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

        for (int index = startIndex; index >= 0 && index < text.length() && totalWidth < width; index += processingDirection)
        {
            char currentChar = text.charAt(index);
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
    

    private TextUtil() {}
}
