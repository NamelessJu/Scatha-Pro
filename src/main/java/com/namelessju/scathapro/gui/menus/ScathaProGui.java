package com.namelessju.scathapro.gui.menus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.DoneButton;
import com.namelessju.scathapro.gui.elements.IGuiElement;
import com.namelessju.scathapro.gui.elements.ScathaProLabel;
import com.namelessju.scathapro.gui.elements.ScathaProTextField;
import com.namelessju.scathapro.gui.elements.ITooltipElement;
import com.namelessju.scathapro.gui.lists.ScathaProGuiList;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.client.config.GuiUtils;

public abstract class ScathaProGui extends GuiScreen
{
    @Deprecated // discourage usage of vanilla element lists
    protected final List<GuiButton> buttonList = null;
    @Deprecated // discourage usage of vanilla element lists
    protected final List<GuiLabel> labelList = null;
    
    public final ScathaPro scathaPro;
    public final GuiScreen parentGui;
    
    public abstract String getTitle();
    
    public boolean hasBackground()
    {
        return true;
    }
    
    protected void drawCustomBackground() {}
    
    
    public ITooltipElement.Tooltip tooltipToRender = null;
    
    private ScathaProLabel titleLabel = null;
    protected final List<IGuiElement> elements = Lists.<IGuiElement>newArrayList();
    protected ScathaProGuiList scrollList = null;
    
    private IGuiElement clickingElement = null;
    
    private int currentGridPositionY = 0;
    private int currentGridPositionX = 0;
    private int currentGridLineHeight = 0;
    
    public ScathaProGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        this.scathaPro = scathaPro;
        this.parentGui = parentGui;
    }
    
    protected void onTextFieldTyped(ScathaProTextField textField) {}
    
    protected void openGui(GuiScreen gui)
    {
        scathaPro.getMinecraft().displayGuiScreen(gui);
    }
    
    public void openParentGui()
    {
        openGui(parentGui);
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        elements.clear();
        
        currentGridPositionY = 0;
        currentGridPositionX = 0;
        currentGridLineHeight = 0;
        
        String title = getTitle();
        if (title != null && !title.replace(" ", "").isEmpty())
        {
            titleLabel = new ScathaProLabel(1, width / 2 - 155, 15, 310, ScathaPro.DYNAMIC_MODNAME + " - " + title).setCentered();
        }
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        tooltipToRender = null;
        
        if (hasBackground())
        {
            drawDefaultBackground();
        }
        else if (mc.theWorld == null)
        {
            drawBackground(0);
            MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.BackgroundDrawnEvent(this));
        }
        
        drawCustomBackground();
        
        if (scrollList != null) scrollList.drawScreen(mouseX, mouseY, partialTicks);
        
        super.drawScreen(mouseX, mouseY, partialTicks);
        
        if (titleLabel != null) titleLabel.elementDraw(mouseX, mouseY);
        
        for (IGuiElement element : elements)
        {
            element.elementDraw(mouseX, mouseY);
        }
        
        if (tooltipToRender != null)
        {
            String[] tooltipLines = tooltipToRender.getTextLines();
            if (tooltipLines != null && tooltipLines.length > 0)
            {
                ScathaProGui.drawTooltip(this, fontRendererObj, mouseX, mouseY, tooltipLines, tooltipToRender.getMaxWidth());
            }
        }
    }
    
    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        if (scrollList != null) scrollList.handleMouseInput();
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if (mouseButton == 0)
        {
            if (scrollList != null)
            {
                scrollList.mouseClicked(mouseX, mouseY, mouseButton);
            }
            
            for (IGuiElement element : elements)
            {
                if (element.elementMouseClicked(mouseX, mouseY, mouseButton))
                {
                    this.clickingElement = element;
                    if (element instanceof GuiButton) actionPerformed((GuiButton) element);
                    if (mc.currentScreen != this) return;
                }
            }
        }
    }
    
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int releaseButton)
    {
        if (releaseButton == 0)
        {
            if (scrollList != null && scrollList.mouseReleased(mouseX, mouseY, releaseButton))
            {
                return;
            }
            
            if (clickingElement != null)
            {
                clickingElement.elementMouseReleased(mouseX, mouseY);
                clickingElement = null;
            }
        }
    }
    
    @Override
    protected void actionPerformed(GuiButton button) { }
    
    @Override
    protected void keyTyped(char character, int code) throws IOException
    {
        for (IGuiElement element : elements)
        {
            element.elementKeyTyped(character, code);
            
            if (element instanceof ScathaProTextField)
            {
                ScathaProTextField textField = (ScathaProTextField) element;
                if (textField.isFocused()) onTextFieldTyped(textField);
            }
        }
        
        if (scrollList != null)
        {
            scrollList.keyTyped(character, code);
        }
        
        super.keyTyped(character, code);
    }

    @Override
    public void updateScreen()
    {
        for (IGuiElement element : elements)
        {
            element.elementTick();
        }
        
        if (scrollList != null) scrollList.tick();
        
        super.updateScreen();
    }
    
    @Override
    public boolean doesGuiPauseGame()
    {
        return true;
    }

    
    public enum GridElementMode
    {
        HALF_WIDTH, FULL_WIDTH, CUSTOM_X;
    }

    protected <T extends GuiButton & IGuiElement> void addGridButton(T button)
    {
        addGridButton(button, GridElementMode.HALF_WIDTH);
    }
    
    protected <T extends GuiButton & IGuiElement> void addGridButton(T button, GridElementMode mode)
    {
        elements.add(setGridPosition(button, mode));
    }
    
    protected <T extends IGuiElement> T setGridPosition(T element, GridElementMode mode)
    {
        if (element == null) return null;
        
        switch (mode)
        {
            case HALF_WIDTH:
                element.setElementX(10);
                element.setElementWidth(150);
                break;
            case FULL_WIDTH:
                element.setElementX(0);
                element.setElementWidth(310);
                break;
            default: break;
        }
        
        if (currentGridPositionX > 0) currentGridPositionX += element.getElementX();
        if (currentGridPositionX + element.getElementWidth() > 310) gridNewLine();
        element.setElementX(width / 2 - 155 + currentGridPositionX);
        currentGridPositionX += element.getElementWidth();
        
        element.setElementY(height / 6 - 12 + currentGridPositionY);
        if (element.getElementHeight() <= 0) element.setElementHeight(20);
        currentGridLineHeight = Math.max(currentGridLineHeight, element.getElementHeight());
        
        return element;
    }
    
    protected void gridNewLine()
    {
        gridNewLine(4);
    }
    
    protected void gridNewLine(int spacing)
    {
        currentGridPositionX = 0;
        currentGridPositionY += currentGridLineHeight + spacing;
        currentGridLineHeight = 0;
    }
    
    protected void addGridGap()
    {
        gridNewLine(10);
    }
    
    protected DoneButton addDoneButton()
    {
        return addDoneButton(width / 2 - 100, height / 6 + 168, 200, 20);
    }
    
    protected DoneButton addDoneButton(int x, int y, int width, int height)
    {
        return addDoneButton("Done", x, y, width, height);
    }
    
    protected DoneButton addDoneButton(String text, int x, int y, int width, int height)
    {
        DoneButton button;
        elements.add(button = new DoneButton(999, x, y, width, height, text, this));
        return button;
    }
    
    protected DoneButton addScrollListDoneButton()
    {
        return addDoneButton(this.width / 2 - 100, this.height - 30, 200, 20);
    }
    
    
    protected static void setSliderTextDefault(GuiSlider slider)
    {
        slider.displayString = slider.dispString + "default";
    }
    
    public static void drawTooltip(GuiScreen screen, FontRenderer fontRenderer, int mouseX, int mouseY, String[] textLines, int maxTextWidth)
    {
        if (textLines.length == 0) return;
        
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        
        int tooltipTextWidth = 0;
        for (String textLine : textLines)
        {
            int textLineWidth = fontRenderer.getStringWidth(textLine);
            if (textLineWidth > tooltipTextWidth)
            {
                tooltipTextWidth = textLineWidth;
            }
        }
        
        boolean needsWrap = false;
        
        if (maxTextWidth > 0 && tooltipTextWidth > maxTextWidth)
        {
            tooltipTextWidth = maxTextWidth;
            needsWrap = true;
        }

        int tooltipX = mouseX + 12;
        
        if (needsWrap)
        {
            int wrappedTooltipWidth = 0;
            List<String> wrappedTextLines = new ArrayList<String>();
            for (int i = 0; i < textLines.length; i++)
            {
                String textLine = textLines[i];
                List<String> wrappedLine = fontRenderer.listFormattedStringToWidth(textLine, tooltipTextWidth);
                for (String line : wrappedLine)
                {
                    int lineWidth = fontRenderer.getStringWidth(line);
                    if (lineWidth > wrappedTooltipWidth)
                    {
                        wrappedTooltipWidth = lineWidth;
                    }
                    wrappedTextLines.add(line);
                }
            }
            tooltipTextWidth = wrappedTooltipWidth;
            textLines = wrappedTextLines.toArray(new String[0]);
        }
        
        needsWrap = false;
        
        int tempWidth = tooltipTextWidth;
        if (tooltipX + tooltipTextWidth + 4 > screen.width)
        {
            tempWidth = screen.width - 12 - 4 - mouseX;
            needsWrap = true;
        }
        boolean wrappedLeft = false;
        if (needsWrap && mouseX > screen.width / 2)
        {
            needsWrap = false;
            tooltipX = mouseX - 12 - tooltipTextWidth;
            tempWidth = tooltipTextWidth;
            if (tooltipX < 4)
            {
                tempWidth = mouseX - 12 - 4;
                needsWrap = true;
                wrappedLeft = true;
            }
        }
        tooltipTextWidth = tempWidth;
        
        if (needsWrap)
        {
            int wrappedTooltipWidth = 0;
            List<String> wrappedTextLines = new ArrayList<String>();
            for (int i = 0; i < textLines.length; i++)
            {
                String textLine = textLines[i];
                List<String> wrappedLine = fontRenderer.listFormattedStringToWidth(textLine, tooltipTextWidth);
                for (String line : wrappedLine)
                {
                    int lineWidth = fontRenderer.getStringWidth(line);
                    if (lineWidth > wrappedTooltipWidth)
                    {
                        wrappedTooltipWidth = lineWidth;
                    }
                    wrappedTextLines.add(line);
                }
            }
            tooltipTextWidth = wrappedTooltipWidth;
            textLines = wrappedTextLines.toArray(new String[0]);
        }
        
        if (wrappedLeft)
        {
            tooltipX = mouseX - 12 - tooltipTextWidth;
        }
        
        int tooltipY = mouseY - 12;
        int tooltipHeight = 8;
        
        if (textLines.length > 1)
        {
            tooltipHeight += (textLines.length - 1) * 10;
        }
        
        if (tooltipY + tooltipHeight + 6 > screen.height)
        {
            tooltipY = screen.height - tooltipHeight - 6;
        }
        
        drawTooltipBox(tooltipX, tooltipY, tooltipTextWidth, tooltipHeight);
        
        for (int lineNumber = 0; lineNumber < textLines.length; ++lineNumber)
        {
            String line = textLines[lineNumber];
            fontRenderer.drawStringWithShadow(line, tooltipX, tooltipY, -1);
            tooltipY += 10;
        }
        
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableRescaleNormal();
    }
    
    public static void drawTooltipBox(int x, int y, int width, int height)
    {
        final int zLevel = 300;
        final int backgroundColor = 0xF0100010;
        GuiUtils.drawGradientRect(zLevel, x - 3, y - 4, x + width + 3, y - 3, backgroundColor, backgroundColor);
        GuiUtils.drawGradientRect(zLevel, x - 3, y + height + 3, x + width + 3, y + height + 4, backgroundColor, backgroundColor);
        GuiUtils.drawGradientRect(zLevel, x - 3, y - 3, x + width + 3, y + height + 3, backgroundColor, backgroundColor);
        GuiUtils.drawGradientRect(zLevel, x - 4, y - 3, x - 3, y + height + 3, backgroundColor, backgroundColor);
        GuiUtils.drawGradientRect(zLevel, x + width + 3, y - 3, x + width + 4, y + height + 3, backgroundColor, backgroundColor);
        final int borderColorStart = 0x505000FF;
        final int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
        GuiUtils.drawGradientRect(zLevel, x - 3, y - 3 + 1, x - 3 + 1, y + height + 3 - 1, borderColorStart, borderColorEnd);
        GuiUtils.drawGradientRect(zLevel, x + width + 2, y - 3 + 1, x + width + 3, y + height + 3 - 1, borderColorStart, borderColorEnd);
        GuiUtils.drawGradientRect(zLevel, x - 3, y - 3, x + width + 3, y - 3 + 1, borderColorStart, borderColorStart);
        GuiUtils.drawGradientRect(zLevel, x - 3, y + height + 2, x + width + 3, y + height + 3, borderColorEnd, borderColorEnd);
    }
}
