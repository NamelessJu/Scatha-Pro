package com.namelessju.scathapro.gui.menus;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class FakeBanGui extends GuiDisconnected
{
    private Runnable guiClosedCallback;
    
    public FakeBanGui(String reason)
    {
        this(reason, null);
    }
    
    public FakeBanGui(String reason, Runnable guiClosedCallback)
    {
        super(null, "connect.failed", new ChatComponentText(
                EnumChatFormatting.RESET.toString() + EnumChatFormatting.RED + "You are permanently banned from this server!\n\n"
                + EnumChatFormatting.GRAY + "Reason: " + EnumChatFormatting.WHITE + reason + "\n"
                + EnumChatFormatting.GRAY + "Find out more: " + EnumChatFormatting.AQUA + EnumChatFormatting.UNDERLINE + "https://hypixel.net/rules\n\n"
                + EnumChatFormatting.GRAY + "Ban ID: " + EnumChatFormatting.WHITE + "#URB4NN3D\n"
                + EnumChatFormatting.GRAY + "Sharing your Ban ID may affect the processing of your appeal!"
        ));
        
        this.guiClosedCallback = guiClosedCallback;
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
        
        if (button.id == 0 && guiClosedCallback != null) guiClosedCallback.run();
    }
}
