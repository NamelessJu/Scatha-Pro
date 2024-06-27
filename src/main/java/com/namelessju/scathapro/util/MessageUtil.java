package com.namelessju.scathapro.util;

import java.text.SimpleDateFormat;
import java.util.Date;

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

public abstract class MessageUtil
{
    // public static final ChatComponentText dividerComponent = new ChatComponentText(Constants.msgHighlightingColor + EnumChatFormatting.BOLD + new String(new char[64]).replace("\0", Util.getUnicodeString("25AC"))); 
    public static final ChatComponentText dividerComponent = new ChatComponentText("");
    
    
    public static EntityPlayer getPlayerInWorld()
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player != null && player.worldObj != null) return player;
        return null;
    }
    
    public static void sendChatMessage(IChatComponent chatComponent)
    {
        EntityPlayer player = getPlayerInWorld();
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
        String[] lines = message.split("(\\r\\n)|(\\n)|(\\r)");
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
    
    public static void sendChatDivider()
    {
        if (ScathaPro.getInstance().variables.lastChatMessageIsDivider) return; // prevent consecutive dividers
        sendChatMessage(dividerComponent);
    }
    
    public static void addChatCopyButton(IChatComponent message)
    {
        if (!ScathaPro.getInstance().getConfig().getBoolean(Config.Key.chatCopy)) return;
        
        String unformattedText = StringUtils.stripControlCodes(message.getUnformattedText());
        if (unformattedText.replace(" ", "").isEmpty()) return; 
        
        ChatComponentText chatCopyButtonComponent = new ChatComponentText(EnumChatFormatting.DARK_GRAY + getUnicodeString("270D"));
        ChatStyle style = new ChatStyle()
                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GRAY + "Copy message")))
                .setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, unformattedText.replace("\n", " ")));
        chatCopyButtonComponent.setChatStyle(style);
        
        message.appendText(EnumChatFormatting.RESET + " ");
        message.appendSibling(chatCopyButtonComponent);
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
    
    /**
     * Formats a timestamp the same way vanilla Minecraft does it
     * @param timestamp The Unix timestamp in milliseconds to format
     */
    public static String formatDateTime(long timestamp)
    {
        return new SimpleDateFormat().format(new Date(timestamp));
    }
    
    public static String getUnicodeString(String hexValue)
    {
        try
        {
            return Character.toString((char) Integer.parseInt(hexValue, 16));
        }
        catch (NumberFormatException e)
        {
            return Character.toString((char) 65533); // "replacement character" unicode symbol
        }
    }

    public static String preventOverflow(String original, int maxWidth)
    {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        int strWidth = fontRenderer.getStringWidth(original);
        int ellipsisWidth = fontRenderer.getStringWidth("...");

        if (strWidth > maxWidth && strWidth > ellipsisWidth)
        {
            return fontRenderer.trimStringToWidth(original, maxWidth - ellipsisWidth).trim() + "...";
        }
        return original;
    }
    

    private MessageUtil() {}
}
