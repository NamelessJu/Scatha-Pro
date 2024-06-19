package com.namelessju.scathapro.gui.menus;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.DoneButton;
import com.namelessju.scathapro.gui.elements.IClickActionButton;
import com.namelessju.scathapro.gui.elements.ScathaProButton;
import com.namelessju.scathapro.gui.elements.ScathaProGuiList;
import com.namelessju.scathapro.gui.elements.ScathaProTextField;
import com.namelessju.scathapro.util.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.config.GuiSlider;

public abstract class ScathaProGui extends GuiScreen
{
    protected static void setSliderTextDefault(GuiSlider slider)
    {
        slider.displayString = slider.dispString + "default";
    }
    
    
    public final ScathaPro scathaPro;
    
    public abstract String getTitle();
    
    public boolean hasBackground()
    {
        return true;
    }
    
    protected void drawCustomBackground() {}
    
    
    public ScathaProButton hoveredButton = null; // Currently only used to render the button tooltip
    
    protected final List<ScathaProTextField> textFieldList = Lists.<ScathaProTextField>newArrayList();
    protected final List<GuiLabel> labelList = Lists.<GuiLabel>newArrayList();
    protected ScathaProGuiList scrollList;
    
    private int currentGridPositionY;
    private boolean isCurrentGridPositionRightColumn;
    
    private GuiScreen parentGui;

    public ScathaProGui(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
    }
    
    public ScathaProGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        this(scathaPro);
        this.parentGui = parentGui;
    }
    
    protected void textFieldTyped(ScathaProTextField textField) {}
    
    protected void openGui(GuiScreen gui)
    {
        Minecraft.getMinecraft().displayGuiScreen(gui);
    }
    
    public void openParentGui()
    {
        openGui(parentGui);
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        textFieldList.clear();
        labelList.clear();
        
        currentGridPositionY = -1;
        isCurrentGridPositionRightColumn = false;
        
        String title = getTitle();
        if (title != null && !title.replace(" ", "").isEmpty())
        {
            GuiLabel titleLabel = new GuiLabel(fontRendererObj, 1, width / 2 - 155, 15, 310, 10, Util.Color.WHITE.getValue()).setCentered();
            titleLabel.func_175202_a("Scatha-Pro - " + title);
            labelList.add(titleLabel);
        }
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        hoveredButton = null;
        
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
        
        for (ScathaProTextField textField : textFieldList)
        {
            textField.drawTextBox();
        }
        
        for (GuiLabel label : labelList)
        {
            label.drawLabel(mc, mouseX, mouseY);
        }
        
        
        if (hoveredButton != null)
        {
            List<String> tooltipLines = hoveredButton.getTooltipLines();
            if (tooltipLines != null && tooltipLines.size() > 0)
            {
                net.minecraftforge.fml.client.config.GuiUtils.drawHoveringText(tooltipLines, mouseX, mouseY, width, height, -1, fontRendererObj);
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
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0 && scrollList != null)
        {
            scrollList.mouseClicked(mouseX, mouseY, mouseButton);
        }
        
        for (ScathaProTextField textField : textFieldList)
        {
            textField.mouseClicked(mouseX, mouseY, mouseButton);
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        if (state != 0 || scrollList == null || !scrollList.mouseReleased(mouseX, mouseY, state))
        {
            super.mouseReleased(mouseX, mouseY, state);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled && button instanceof IClickActionButton)
        {
            ((IClickActionButton) button).click();
        }
    }
    
    @Override
    protected void keyTyped(char character, int code) throws IOException
    {
        for (ScathaProTextField textField : textFieldList)
        {
            textField.textboxKeyTyped(character, code);
            if (textField.isFocused()) textFieldTyped(textField);
        }
        
        if (scrollList != null)
        {
            scrollList.keyTyped(character, code);
        }
        
        super.keyTyped(character, code);
    }

    @Override
    public void updateScreen() {
        for (ScathaProTextField textField : textFieldList)
        {
            textField.updateCursorCounter();
        }
        
        super.updateScreen();
    }
    
    @Override
    public boolean doesGuiPauseGame()
    {
        return true;
    }
    
    protected void addDoneButton()
    {
        addDoneButton(width / 2 - 100, height / 6 + 168, 200, 20);
    }
    
    protected void addDoneButton(int x, int y, int width, int height)
    {
        addDoneButton("Done", x, y, width, height);
    }
    
    protected void addDoneButton(String text, int x, int y, int width, int height)
    {
        buttonList.add(new DoneButton(999, x, y, width, height, text, this));
    }
    
    protected void addGridButton(GuiButton button)
    {
        addGridButton(button, false);
    }
    
    protected void addGridButton(GuiButton button, boolean doubleWidth)
    {
        if (currentGridPositionY < 0) currentGridPositionY = height / 6 - 12;
        if (doubleWidth && isCurrentGridPositionRightColumn)
        {
            currentGridPositionY += 24;
            isCurrentGridPositionRightColumn = false;
        }
        int currentGridPositionX = width / 2 + (isCurrentGridPositionRightColumn ? 5 : -155);
        
        button.xPosition = currentGridPositionX;
        button.yPosition = currentGridPositionY;
        button.width = doubleWidth ? 310 : 150;
        button.height = 20;
        buttonList.add(button);
        
        if (isCurrentGridPositionRightColumn || doubleWidth) currentGridPositionY += 24;
        if (!doubleWidth) isCurrentGridPositionRightColumn = !isCurrentGridPositionRightColumn;
    }
    
    protected void addGridGap()
    {
        if (currentGridPositionY < 0) return;
        currentGridPositionY += 6;
        isCurrentGridPositionRightColumn = false;
    }
}
