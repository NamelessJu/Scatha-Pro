package com.namelessju.scathapro.util;

import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.ScathaPro;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StringUtils;

public class ChatUtil {

    public static void sendModChatMessage(String message) {
        sendModChatMessage(new ChatComponentText(message));
    }
    public static void sendModChatMessage(IChatComponent chatComponent) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player != null) {
            ChatComponentText chatComponentText = new ChatComponentText(ScathaPro.CHATPREFIX);
            chatComponentText.appendSibling(chatComponent);
            addChatCopyButton(chatComponentText);
            player.addChatMessage(chatComponentText);
        }
    }
    
    public static void sendModErrorMessage(String errorMessage) {
        sendModChatMessage(EnumChatFormatting.RED + errorMessage);
    }
    
    public static void addChatCopyButton(IChatComponent message) {
        String unformattedText = StringUtils.stripControlCodes(message.getUnformattedText());
        
        if (Config.instance.getBoolean(Config.Key.chatCopy) && !unformattedText.replace(" ", "").isEmpty()) {
            ChatComponentText copyText = new ChatComponentText(EnumChatFormatting.DARK_GRAY + Util.getUnicodeString("270D"));
            ChatStyle style = new ChatStyle()
                    .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GRAY + "Copy message")))
                    .setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, unformattedText.replace("\n", " ")));
            copyText.setChatStyle(style);
            
            message.appendText(EnumChatFormatting.RESET + " ");
            message.appendSibling(copyText);
        }
    }

}
