package com.namelessju.scathapro.gui.elements;

import java.lang.reflect.Constructor;

import org.apache.logging.log4j.Level;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.menus.ScathaProGui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SubMenuButton extends GuiButton implements IClickActionButton {
	
	public GuiScreen parentMenu;
	public Class<? extends ScathaProGui> menuClass;
	
    public SubMenuButton(int buttonId, int x, int y, int widthIn, int heightIn, String displayString, GuiScreen parentMenu, Class<? extends ScathaProGui> menuClass) {
        super(buttonId, x, y, widthIn, heightIn, displayString);
        
        this.parentMenu = parentMenu;
        this.menuClass = menuClass;
    }
    
	@Override
	public void click() {
		try {
			Constructor<? extends ScathaProGui> constructor = menuClass.getDeclaredConstructor(GuiScreen.class);
			GuiScreen screen = constructor.newInstance(parentMenu);
			Minecraft.getMinecraft().displayGuiScreen(screen);
		}
		catch (Exception e) {
			ScathaPro.getInstance().logger.log(Level.ERROR, "Error while trying to open menu from SubMenuButton:");
			e.printStackTrace();
		}
	}
}
