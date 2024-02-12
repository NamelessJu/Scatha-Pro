package com.namelessju.scathapro.gui.elements;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.alerts.alertmodes.customalertmode.CustomAlertModeManager;
import com.namelessju.scathapro.util.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumChatFormatting;

public class DeleteCustomAlertModeButton extends GuiButton implements IClickActionButton, GuiYesNoCallback {

    private final CustomAlertModeManager customAlertModeManager;
    
    private final String customModeId;
    private final GuiScreen returnGui;
    
    public DeleteCustomAlertModeButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, String customModeId, GuiScreen returnGui)
    {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        
        this.customModeId = customModeId;
        this.returnGui = returnGui;
        
        this.customAlertModeManager = ScathaPro.getInstance().getCustomAlertModeManager();
    }

    @Override
    public void click()
    {
        String customModeName = customAlertModeManager.getSubmodeDisplayName(customModeId);
        GuiYesNo confirmGui = new GuiYesNo(this, EnumChatFormatting.RESET + "Do you really want to delete \"" + customModeName + EnumChatFormatting.RESET + "\"?", "This cannot be undone!", 0);
        Minecraft.getMinecraft().displayGuiScreen(confirmGui);
    }
    
    @Override
    public void confirmClicked(boolean result, int sourceButtonId)
    {
        if (result)
        {
            customAlertModeManager.deleteSubmode(customModeId);
        }
        
        Minecraft.getMinecraft().displayGuiScreen(returnGui);
    }
    
    
    /**
     * Draws this button to the screen.
     */
    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.visible)
        {
            FontRenderer fontrenderer = mc.fontRendererObj;
            mc.getTextureManager().bindTexture(buttonTextures);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            int i = this.getHoverState(this.hovered);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.blendFunc(770, 771);
            this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + i * 20, this.width / 2, this.height);
            this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
            this.mouseDragged(mc, mouseX, mouseY);
            int j = 14737632;

            if (packedFGColour != 0)
            {
                j = packedFGColour;
            }
            else
            if (!this.enabled)
            {
                j = 10526880;
            }
            else if (this.hovered)
            {
                // j = 16777120;
                j = Util.Color.RED.getValue();
            }

            this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, j);
        }
    }
}
