package com.namelessju.scathapro.eventlisteners;

import java.util.List;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.ImageButton;
import com.namelessju.scathapro.gui.menus.AchievementsGui;
import com.namelessju.scathapro.gui.menus.SettingsGui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GuiListeners
{
    private final ScathaPro scathaPro;
    private final Minecraft mc;
    
    private ImageButton achievementMenuButton = null;
    
    public GuiListeners(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
        mc = scathaPro.getMinecraft();
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGuiInitPost(GuiScreenEvent.InitGuiEvent.Post event)
    {
        if (event.isCanceled()) return;
        
        // Settings Button
        
        if (event.gui instanceof GuiOptions)
        {
            GuiButton modMenuButton = new GuiButton(504703001, event.gui.width / 2 - 155, event.gui.height / 6 + 24 - 6, 150, 20, ScathaPro.MODNAME + " Settings...");
            event.buttonList.add(modMenuButton);
            
            if (isButtonOverlapping(modMenuButton, event.buttonList))
            {
                modMenuButton.xPosition = event.gui.width / 2 + 5;
                
                if (isButtonOverlapping(modMenuButton, event.buttonList))
                {
                    modMenuButton.xPosition = 5;
                    modMenuButton.yPosition = 5;
                }
            }
            
            return;
        }
        
        // Achievements Menu Button
        
        if (event.gui instanceof GuiIngameMenu)
        {
            achievementMenuButton = new ImageButton(504703002, event.gui.width / 2 - 100 - 24, event.gui.height / 4 + 48 - 16, 20, 20, "gui/achievements/button_icon.png", 64, 64, 0.2f);
            achievementMenuButton.setTooltip(EnumChatFormatting.GRAY + ScathaPro.MODNAME + " Achievements");
            
            if (isButtonOverlapping(achievementMenuButton, event.buttonList))
            {
                achievementMenuButton.xPosition = 5;
                achievementMenuButton.yPosition = event.gui.height - 5 - achievementMenuButton.height;
                
                if (isButtonOverlapping(achievementMenuButton, event.buttonList))
                {
                    achievementMenuButton.xPosition = event.gui.width - 5 - achievementMenuButton.width;
                    achievementMenuButton.yPosition = event.gui.height - 5 - achievementMenuButton.height;
                }
            }
            
            event.buttonList.add(achievementMenuButton);
            
            return;
        }
        else achievementMenuButton = null;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGuiDrawPost(GuiScreenEvent.DrawScreenEvent.Post event)
    {
        if (event.isCanceled()) return;
        if (event.gui instanceof GuiIngameMenu && achievementMenuButton.isMouseOver())
        {
            net.minecraftforge.fml.client.config.GuiUtils.drawHoveringText(achievementMenuButton.getTooltipLines(),
                event.mouseX, event.mouseY, event.gui.width, event.gui.height, -1, mc.fontRendererObj);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGuiAction(GuiScreenEvent.ActionPerformedEvent.Pre event)
    {
        if (event.isCanceled()) return;
        switch (event.button.id)
        {
            case 504703001:
                mc.displayGuiScreen(new SettingsGui(scathaPro, event.gui));
                break;
            
            case 504703002:
                mc.displayGuiScreen(new AchievementsGui(scathaPro, event.gui));
                break;
        }
    }
    
    private boolean isButtonOverlapping(GuiButton button, List<GuiButton> buttonList)
    {
        for (GuiButton otherButton : buttonList)
        {
            if
            (
                otherButton != button &&
                button.xPosition < otherButton.xPosition + otherButton.width &&
                button.xPosition + button.width > otherButton.xPosition &&
                button.yPosition < otherButton.yPosition + otherButton.height &&
                button.height + button.yPosition > otherButton.yPosition
            )
            {
                return true;
            }
        }
        return false;
    }
}
