package namelessju.scathapro.gui.elements;

import java.util.List;

import com.google.common.collect.Lists;

import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumChatFormatting;

public class ScathaProLabel extends Gui implements IGuiElement, ITooltipElement
{
    public int id;
    public int xPosition;
    public int yPosition;
    public int width;
    public int height;
    public boolean visible = true;
    public int color;
    protected List<String> textLines = Lists.<String>newArrayList();
    protected boolean centered;
    protected String suffix = null;
    
    private final Tooltip tooltip = new Tooltip();

    public ScathaProLabel(int id, int x, int y, int width, String initialText)
    {
        this(id, x, y, width, initialText, Util.Color.WHITE);
    }
    
    public ScathaProLabel(int id, int x, int y, int width, String initialText, int color)
    {
        this(id, x, y, width, Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * TextUtil.splitOnLineBreaks(initialText).length, initialText, color);
    }
    
    public ScathaProLabel(int id, int x, int y, int width, int height, String initialText)
    {
        this(id, x, y, width, height, initialText, Util.Color.WHITE);
    }
    
    public ScathaProLabel(int id, int x, int y, int width, int height, String initialText, int color)
    {
        this.id = id;
        this.xPosition = x;
        this.yPosition = y;
        this.width = width;
        this.height = height;
        this.centered = false;
        this.color = color;
        setText(initialText);
    }
    
    @Override
    public void setElementX(int x)
    {
        this.xPosition = x;
    }
    
    @Override
    public void setElementY(int y)
    {
        this.yPosition = y;
    }
    
    @Override
    public void setElementWidth(int width)
    {
        this.width = width;
    }
    
    @Override
    public void setElementHeight(int height)
    {
        this.height = height;
    }
    
    @Override
    public int getElementX()
    {
        return this.xPosition;
    }
    
    @Override
    public int getElementY()
    {
        return this.yPosition;
    }
    
    @Override
    public int getElementHeight()
    {
        return this.height;
    }
    
    @Override
    public int getElementWidth()
    {
        return this.width;
    }
    
    @Override
    public void elementDraw(int mouseX, int mouseY)
    {
        this.drawLabel(Minecraft.getMinecraft(), mouseX, mouseY);
    }
    
    public void setText(String text)
    {
        this.textLines.clear();
        addText(text);
    }
    
    public void addText(String text)
    {
        String[] lines = TextUtil.splitOnLineBreaks(text);
        for (String line : lines) this.textLines.add(line);
    }
    
    public void setSuffix(String suffix)
    {
        this.suffix = suffix;
    }
    
    public ScathaProLabel setCentered()
    {
        this.centered = true;
        return this;
    }
    
    public void drawLabel(Minecraft mc, int mouseX, int mouseY)
    {
        if (!this.visible) return;
        
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        int verticalMiddle = this.yPosition + this.height / 2;
        int lineCount = this.textLines.size();
        int yStart = verticalMiddle - lineCount * 10 / 2;
        
        for (int lineIndex = 0; lineIndex < lineCount; lineIndex ++)
        {
            String line = this.textLines.get(lineIndex);
            if (suffix != null && lineIndex == lineCount - 1)
            {
                line = EnumChatFormatting.RESET + line + suffix;
            }
            
            int lineY = yStart + lineIndex * 10;
            boolean hovered = mouseY >= lineY && mouseY < lineY + 10;
            int textWidth = hovered ? TextUtil.getStringWidth(line) : 0;
            
            if (this.centered)
            {
                int centerX = this.xPosition + this.width / 2;
                this.drawCenteredString(mc.fontRendererObj, line, centerX, lineY, this.color);
                
                if (hovered)
                {
                    int halfTextWidth = textWidth / 2;
                    hovered = mouseX >= centerX - halfTextWidth && mouseX < centerX + halfTextWidth;
                }
            }
            else
            {
                this.drawString(mc.fontRendererObj, line, this.xPosition, lineY, this.color);
                
                if (hovered) hovered = mouseX >= this.xPosition && mouseX < this.xPosition + textWidth;
            }
            
            if (hovered)
            {
                this.tooltip.requestRender();
            }
        }
    }
    
    @Override
    public Tooltip getTooltip()
    {
        return this.tooltip;
    }
}
