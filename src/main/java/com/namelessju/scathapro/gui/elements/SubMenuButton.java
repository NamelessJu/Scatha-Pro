package com.namelessju.scathapro.gui.elements;

import java.lang.reflect.Constructor;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.menus.ScathaProGui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SubMenuButton extends GuiButton implements IClickActionButton
{
    public ScathaProGui parentMenu;
    public Class<? extends ScathaProGui> menuClass;
    
    public SubMenuButton(int buttonId, int x, int y, int widthIn, int heightIn, String displayString, ScathaProGui parentMenu, Class<? extends ScathaProGui> menuClass)
    {
        super(buttonId, x, y, widthIn, heightIn, displayString);
        
        this.parentMenu = parentMenu;
        this.menuClass = menuClass;
    }
    
    @Override
    public void click()
    {
        try
        {
            Constructor<? extends ScathaProGui> constructor = menuClass.getDeclaredConstructor(ScathaPro.class, GuiScreen.class);
            GuiScreen screen = constructor.newInstance(parentMenu.scathaPro, parentMenu);
            Minecraft.getMinecraft().displayGuiScreen(screen);
        }
        catch (Exception e)
        {
            ScathaPro.getInstance().logError("Error while trying to open menu from SubMenuButton");
            e.printStackTrace();
        }
    }
}
