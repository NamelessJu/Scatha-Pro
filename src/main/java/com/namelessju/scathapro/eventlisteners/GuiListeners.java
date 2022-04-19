package com.namelessju.scathapro.eventlisteners;

import java.util.List;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.AchievementsGui;
import com.namelessju.scathapro.gui.ImageButton;
import com.namelessju.scathapro.gui.SettingsGui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GuiListeners {
    
    Minecraft mc = Minecraft.getMinecraft();
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGuiInitPost(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.gui instanceof GuiOptions) {
            GuiButton modMenuButton = new GuiButton(504704000, event.gui.width / 2 - 155, event.gui.height / 6 + 24 - 6, 150, 20, ScathaPro.MODNAME);
            
            if (isButtonOverlapping(modMenuButton, event.buttonList)) {
                modMenuButton.xPosition = event.gui.width / 2 + 5;
                
                if (isButtonOverlapping(modMenuButton, event.buttonList)) {
                    modMenuButton.xPosition = 5;
                    modMenuButton.yPosition = 5;
                    modMenuButton.width = 100;
                }
            }
            
            event.buttonList.add(modMenuButton);
        }
        else if (event.gui instanceof GuiIngameMenu) {
            event.buttonList.add(new ImageButton(504704001, event.gui.width / 2 - 100 - 24, event.gui.height / 4 + 48 - 16, 20, 20, "achievements_icon.png", 64, 64, 0.2f));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGuiAction(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (event.button.id == 504704000) mc.displayGuiScreen(new SettingsGui(event.gui));
        else if (event.button.id == 504704001) mc.displayGuiScreen(new AchievementsGui(event.gui));
    }
    
    private boolean isButtonOverlapping(GuiButton button, List<GuiButton> buttonList) {
        for (GuiButton otherButton : buttonList) {
            if (otherButton != button &&
                    button.xPosition < otherButton.xPosition + otherButton.width &&
                    button.xPosition + button.width > otherButton.xPosition &&
                    button.yPosition < otherButton.yPosition + otherButton.height &&
                    button.height + button.yPosition > otherButton.yPosition)
                return true;
        }
        return false;
    }
}
