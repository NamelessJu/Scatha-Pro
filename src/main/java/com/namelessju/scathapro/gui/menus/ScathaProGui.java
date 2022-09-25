package com.namelessju.scathapro.gui.menus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.namelessju.scathapro.util.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.client.config.GuiSlider;

public abstract class ScathaProGui extends GuiScreen {
    
    public String getTitle() {
        return "Unnamed GUI";
    }
    
    public boolean hasBackground() {
        return true;
    }
    
    protected static void setSliderDefaultString(GuiSlider slider) {
        slider.displayString = slider.dispString + "default";
    }
    
    protected static String getEnabledString(boolean enabled) {
        return enabled ? "ON" : "OFF";
    }
    
    
    protected List<GuiTextField> textFieldList = new ArrayList<GuiTextField>();
    protected List<GuiLabel> labelList = new ArrayList<GuiLabel>();

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
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(this));
        }
        
        drawCustomBackground();
        
        super.drawScreen(mouseX, mouseY, partialTicks);
        
        for (GuiTextField textField : textFieldList) {
            textField.drawTextBox();
        }
        
        for (GuiLabel label : labelList) {
            label.drawLabel(mc, mouseX, mouseY);
        }
    }
    
    protected void drawCustomBackground() {}

    @Override
    protected void mouseClicked(final int mouseX, final int mouseY, final int mouseEvent) throws IOException {
        for (GuiTextField textField : textFieldList) {
            textField.mouseClicked(mouseX, mouseY, mouseEvent);
        }

        super.mouseClicked(mouseX, mouseY, mouseEvent);
    }
    
    @Override
    protected void keyTyped(char character, int code) throws IOException {
        for (GuiTextField textField : textFieldList) {
            textField.textboxKeyTyped(character, code);
            if (textField.isFocused()) textFieldTyped(textField);
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
