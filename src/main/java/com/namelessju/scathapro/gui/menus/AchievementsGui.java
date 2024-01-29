package com.namelessju.scathapro.gui.menus;

import java.io.IOException;

import org.lwjgl.input.Mouse;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.AchievementsList;
import com.namelessju.scathapro.gui.elements.DoneButton;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

public class AchievementsGui extends ScathaProGui
{
    private AchievementsList achievementsList;
    
    @Override
    public String getTitle()
    {
        return "Achievements";
    }

    public AchievementsGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        float heightFactor = (height * scaledResolution.getScaleFactor()) / 1080f;

        
        int achievementsListWidth = 310;
        int achievementsListX = width / 2 - achievementsListWidth / 2;
        int achievementsListY = 41;
        int achievementsListHeight = Math.round(height - achievementsListY - 20 - 50 * heightFactor - 10);
        achievementsList = new AchievementsList(achievementsListX, achievementsListY, achievementsListWidth, achievementsListHeight);
        
        buttonList.add(new DoneButton(504704399, width / 2 - 100, Math.round(height - 20 - 50 * heightFactor), 200, 20, "Close", this));
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);
        
        if (button.enabled && button.id == 504704399)
        {
            openParentGui();
        }
    }
    
    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        
        achievementsList.handleMouseInput(mouseY);
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
        
        achievementsList.draw(mouseX, mouseY, height);
    }

}
