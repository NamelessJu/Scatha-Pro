package com.namelessju.scathapro.gui.menus;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;
import com.namelessju.scathapro.gui.elements.IClickActionButton;
import com.namelessju.scathapro.gui.elements.ScathaProGuiList;
import com.namelessju.scathapro.util.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.config.GuiSlider;

public abstract class ScathaProGui extends GuiScreen {
    
    protected static void setSliderDefaultString(GuiSlider slider) {
        slider.displayString = slider.dispString + "default";
    }
    
    
    public abstract String getTitle();
    
    public boolean hasBackground() {
        return true;
    }
    
    protected void drawCustomBackground() {}
    
    
    protected final List<GuiTextField> textFieldList = Lists.<GuiTextField>newArrayList();
    protected final List<GuiLabel> labelList = Lists.<GuiLabel>newArrayList();
    protected ScathaProGuiList scrollList;
    
    private GuiScreen parentGui;
    
    public ScathaProGui(GuiScreen parentGui) {
        this.parentGui = parentGui;
    }
    
    protected void textFieldTyped(GuiTextField textField) {}
    
    protected void openGui(GuiScreen gui) {
        Minecraft.getMinecraft().displayGuiScreen(gui);
    }
    
    public void openParentGui() {
        openGui(parentGui);
    }
    
    @Override
    public void initGui() {
        super.initGui();
        
        textFieldList.clear();
        labelList.clear();
        
        String title = getTitle();
        if (title != null && !title.replace(" ", "").isEmpty()) {
            GuiLabel titleLabel = new GuiLabel(fontRendererObj, 1, width / 2 - 155, 15, 310, 10, Util.Color.WHITE.getValue()).setCentered();
            titleLabel.func_175202_a("Scatha-Pro " + title);
            labelList.add(titleLabel);
        }
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        if (hasBackground())
            drawDefaultBackground();
        else if (mc.theWorld == null) {
            drawBackground(0);
            MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.BackgroundDrawnEvent(this));
        }
        
        drawCustomBackground();
        
        if (scrollList != null) scrollList.drawScreen(mouseX, mouseY, partialTicks);
        
        super.drawScreen(mouseX, mouseY, partialTicks);
        
        for (GuiTextField textField : textFieldList) {
            textField.drawTextBox();
        }
        
        for (GuiLabel label : labelList) {
            label.drawLabel(mc, mouseX, mouseY);
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
        if (mouseButton == 0 && scrollList != null && scrollList.mouseClicked(mouseX, mouseY, mouseButton)) {
            return;
        }
        
        for (GuiTextField textField : textFieldList) {
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
    	if (button.enabled && button instanceof IClickActionButton) {
    		((IClickActionButton) button).click();
    	}
    }
    
    @Override
    protected void keyTyped(char character, int code) throws IOException {
        for (GuiTextField textField : textFieldList) {
            textField.textboxKeyTyped(character, code);
            if (textField.isFocused()) textFieldTyped(textField);
        }
        
        if (scrollList != null) {
        	scrollList.keyTyped(character, code);
        }
        
        super.keyTyped(character, code);
    }

    @Override
    public void updateScreen() {
        for (GuiTextField textField : textFieldList) {
            textField.updateCursorCounter();
        }
        
        super.updateScreen();
    }
    
    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}
