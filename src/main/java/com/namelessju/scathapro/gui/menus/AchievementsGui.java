package com.namelessju.scathapro.gui.menus;

import java.io.IOException;

import org.lwjgl.input.Mouse;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.gui.elements.AchievementsList;
import com.namelessju.scathapro.gui.elements.BooleanSettingButton;
import com.namelessju.scathapro.gui.elements.DoneButton;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

public class AchievementsGui extends ScathaProGui
{
    private float heightFactor = 1f;
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
        initializeAchievementsList();

        ScaledResolution scaledResolution = new ScaledResolution(mc);
        heightFactor = (height * scaledResolution.getScaleFactor()) / 1080f;
        
        initializeAchievementsList();
        
        buttonList.add(new BooleanSettingButton(504704301, width / 2 - 100, Math.round(height - 24 - 50 * heightFactor), 200, 20, "Show Bonus Achievements", Config.Key.bonusAchievementsShown));
        
        buttonList.add(new DoneButton(504704399, width / 2 - 100, Math.round(height - 50 * heightFactor), 200, 20, "Close", this));
    }
    
    private void initializeAchievementsList()
    {
        int achievementsListWidth = 310;
        int achievementsListX = width / 2 - achievementsListWidth / 2;
        int achievementsListY = 41;
        int achievementsListHeight = Math.round(height - achievementsListY - 20 - 50 * heightFactor - 10);
        achievementsList = new AchievementsList(achievementsListX, achievementsListY, achievementsListWidth, achievementsListHeight);
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);
        
        if (button.enabled && button.id == 504704301)
        {
            scathaPro.getAchievementManager().updateBonusTypeVisibility();
            initializeAchievementsList();
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
