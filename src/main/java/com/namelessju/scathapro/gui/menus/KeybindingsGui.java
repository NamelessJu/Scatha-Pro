package com.namelessju.scathapro.gui.menus;

import java.io.IOException;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.lists.KeybindingsGuiList;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;

public class KeybindingsGui extends ScathaProGui
{
    public KeyBinding changingKeybinding = null;
    
    public KeybindingsGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
    }
    
    @Override
    public String getTitle()
    {
        return "Key Bindings";
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        scrollList = new KeybindingsGuiList(this);
        addScrollListDoneButton();
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if (changingKeybinding != null)
        {
            mc.gameSettings.setOptionKeyBinding(changingKeybinding, -100 + mouseButton);
            changingKeybinding = null;
            KeyBinding.resetKeyBindingArrayAndHash();
        }
        
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (changingKeybinding != null)
        {
            if (keyCode == 1)
            {
                mc.gameSettings.setOptionKeyBinding(changingKeybinding, 0);
            }
            else if (keyCode != 0)
            {
                mc.gameSettings.setOptionKeyBinding(changingKeybinding, keyCode);
            }
            else if (typedChar > 0)
            {
                mc.gameSettings.setOptionKeyBinding(changingKeybinding, typedChar + 256);
            }

            changingKeybinding = null;
            KeyBinding.resetKeyBindingArrayAndHash();
        }
        else
        {
            super.keyTyped(typedChar, keyCode);
        }
    }
}
