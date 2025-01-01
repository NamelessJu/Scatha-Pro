package com.namelessju.scathapro.gui.menus;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class InfoMessageGui extends GuiScreen
{
    private final GuiScreen parentScreen;
    private final String messageLine1;
    private final String messageLine2;
    private final List<String> messageLine2WrappedLines = Lists.<String>newArrayList();
    private final String buttonText;

    public InfoMessageGui(GuiScreen parentScreen, String messageLine1, String messageLine2)
    {
        this(parentScreen, messageLine1, messageLine2, null);
    }
    
    public InfoMessageGui(GuiScreen parentScreen, String messageLine1, String messageLine2, String buttonText)
    {
        this.parentScreen = parentScreen;
        this.messageLine1 = messageLine1;
        this.messageLine2 = messageLine2;
        this.buttonText = buttonText;
    }
    
    public void initGui()
    {
        this.buttonList.add(new GuiButton(0, this.width / 2 - 50, this.height / 6 + 96, 100, 20, buttonText == null ? "OK" : buttonText));
        this.messageLine2WrappedLines.clear();
        this.messageLine2WrappedLines.addAll(this.fontRendererObj.listFormattedStringToWidth(this.messageLine2, Math.min(310, this.width - 50)));
    }
    
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 0)
        {
            mc.displayGuiScreen(parentScreen);
        }
    }
    
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        
        this.drawCenteredString(this.fontRendererObj, this.messageLine1, this.width / 2, 70, 16777215);
        
        int i = 90;
        for (String s : this.messageLine2WrappedLines)
        {
            this.drawCenteredString(this.fontRendererObj, s, this.width / 2, i, 16777215);
            i += this.fontRendererObj.FONT_HEIGHT;
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
