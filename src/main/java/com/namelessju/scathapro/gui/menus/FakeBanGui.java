package com.namelessju.scathapro.gui.menus;

import java.io.IOException;

import com.namelessju.scathapro.achievements.Achievement;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class FakeBanGui extends GuiDisconnected {

    public FakeBanGui() {
        super(null, "connect.failed", new ChatComponentText(
                EnumChatFormatting.RESET.toString() + EnumChatFormatting.RED + "You are permanently banned from this server!\n\n"
                + EnumChatFormatting.GRAY + "Reason: " + EnumChatFormatting.WHITE + "Scatha-Pro Savefile Manipulation\n"
                + EnumChatFormatting.GRAY + "Find out more: " + EnumChatFormatting.AQUA + EnumChatFormatting.UNDERLINE + "https://www.hypixel.net/scatha-pro\n\n"
                + EnumChatFormatting.GRAY + "Ban ID: " + EnumChatFormatting.WHITE + "#1CH34T3R\n"
                + EnumChatFormatting.GRAY + "Sharing your Ban ID may affect the processing of your appeal!"
        ));
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
        if (button.id == 0) {
            Achievement.cheat.setProgress(Achievement.cheat.goal);
            
            mc.ingameGUI.displayTitle(null, null, 5, 60, 40);
            mc.ingameGUI.displayTitle(null, EnumChatFormatting.GRAY + "We do a little trolling", 0, 0, 0);
            mc.ingameGUI.displayTitle(EnumChatFormatting.GREEN + "Just kidding!", null, 0, 0, 0);
        }
        
        super.actionPerformed(button);
    }
    
}
