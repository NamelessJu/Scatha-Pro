package com.namelessju.scathapro.eventlisteners;

import java.util.List;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.ImageButton;
import com.namelessju.scathapro.gui.menus.AchievementsGui;
import com.namelessju.scathapro.gui.menus.SettingsGui;
import com.namelessju.scathapro.util.ChatUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class GuiListeners {
    
    Minecraft mc = Minecraft.getMinecraft();
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGuiInitPost(GuiScreenEvent.InitGuiEvent.Post event) {
    	
    	// Settings Button
    	
    	if (event.gui instanceof GuiOptions) {
            GuiButton modMenuButton = new GuiButton(504704000, event.gui.width / 2 - 155, event.gui.height / 6 + 24 - 6, 150, 20, ScathaPro.MODNAME + " Settings");
            
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
    	
    	// Achievements Menu Button
    	
        else if (event.gui instanceof GuiIngameMenu) {
            GuiButton achievementMenuButton = new ImageButton(504704001, event.gui.width / 2 - 100 - 24, event.gui.height / 4 + 48 - 16, 20, 20, "gui/achievements/button_icon.png", 64, 64, 0.2f);
            
            if (isButtonOverlapping(achievementMenuButton, event.buttonList)) {
                achievementMenuButton.xPosition = 5;
                achievementMenuButton.yPosition = event.gui.height - 5 - achievementMenuButton.height;
                
                if (isButtonOverlapping(achievementMenuButton, event.buttonList)) {
                    achievementMenuButton.xPosition = event.gui.width - 5 - achievementMenuButton.width;
                    achievementMenuButton.yPosition = event.gui.height - 5 - achievementMenuButton.height;
                }
            }
            
            event.buttonList.add(achievementMenuButton);
        }
    	
    }
    
    public void readChest(GuiScreen menu) {
    	if (!(menu instanceof GuiChest)) return;
    	
    	GuiChest chest = (GuiChest) menu;
    	IInventory chestInventory = ((ContainerChest) chest.inventorySlots).getLowerChestInventory();
    	
    	
        if (chestInventory.hasCustomName()) {
        	ChatUtil.sendModChatMessage("Chest name: \"" + chestInventory.getDisplayName().getUnformattedText() + "\"");
        }
        
    	
        StringBuilder inventoryDataStringBuilder = new StringBuilder("[");
        boolean firstIteration = true;
        for (int i = 0; i < chestInventory.getSizeInventory(); i ++) {
        	ItemStack stack = chestInventory.getStackInSlot(i);
        	
        	if (!firstIteration) inventoryDataStringBuilder.append(", ");
        	if (stack != null) {
        		String nbtString = stack.getTagCompound() != null ? stack.getTagCompound().toString().replace("\"", "\\\"") : "";
        		inventoryDataStringBuilder.append("{\"item\": \"" + stack.getItem().getRegistryName() + "\", \"nbt\": \"" + nbtString + "\"}");
        	}
        	else {
        		inventoryDataStringBuilder.append("{\"item\": null}");
        	}
        	if (firstIteration) firstIteration = false;
        }
        inventoryDataStringBuilder.append("]");
        
        Util.copyToClipboard(inventoryDataStringBuilder.toString());
        ChatUtil.sendModChatMessage("Chest menu data copied to clipboard");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGuiAction(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        switch (event.button.id) {
            case 504704000:
                mc.displayGuiScreen(new SettingsGui(event.gui));
                break;
                
            case 504704001:
                mc.displayGuiScreen(new AchievementsGui(event.gui));
                break;
        }
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
