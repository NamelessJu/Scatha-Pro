package com.namelessju.scathapro.gui.menus;

import java.io.IOException;

import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.util.TextUtil;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class FakeBanGui extends GuiDisconnected
{
    public FakeBanGui()
    {
        super(null, "connect.failed", new ChatComponentText(
                EnumChatFormatting.RESET.toString() + EnumChatFormatting.RED + "You are permanently banned from this server!\n\n"
                + EnumChatFormatting.GRAY + "Reason: " + EnumChatFormatting.WHITE + "Savefile Manipulation\n"
                + EnumChatFormatting.GRAY + "Find out more: " + EnumChatFormatting.AQUA + EnumChatFormatting.UNDERLINE + "https://hypixel.net/rules\n\n"
                + EnumChatFormatting.GRAY + "Ban ID: " + EnumChatFormatting.WHITE + "#UCH34T3R\n"
                + EnumChatFormatting.GRAY + "Sharing your Ban ID may affect the processing of your appeal!"
        ));
    }
    
    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }
    
    // Always draw dirt background
    @Override
    public void drawWorldBackground(int tint)
    {
        this.drawBackground(tint);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);
        
        if (button.id == 0)
        {
            Achievement.cheat.unlock();
            
            TextUtil.displayTitle("", EnumChatFormatting.GREEN + "We do a little trolling", 5, 60, 40);
        }
        
        super.actionPerformed(button);
    }
}
