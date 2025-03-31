package com.namelessju.scathapro.managers;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.resources.IResourcePack;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class ReflectionManager
{
    public static List<ChatLine> getChatLines()
    {
        try
        {
            return ReflectionHelper.getPrivateValue(GuiNewChat.class, Minecraft.getMinecraft().ingameGUI.getChatGUI(), "field_146253_i");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    public static List<IResourcePack> getDefaultResourcePacks()
    {
        try
        {
            return ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "field_110449_ao", "defaultResourcePacks");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    
    private ReflectionManager() {}
}
