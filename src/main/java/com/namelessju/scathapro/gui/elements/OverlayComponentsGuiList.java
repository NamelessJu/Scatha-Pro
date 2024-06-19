package com.namelessju.scathapro.gui.elements;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.overlay.Overlay.ToggleableOverlayElement;

public class OverlayComponentsGuiList extends ScathaProGuiList
{
    public OverlayComponentsGuiList(ScathaPro scathaPro, GuiScreen gui)
    {
        super(gui, 25);
        
        this.listEntries.add(new OverlayBackgroundEntry());
        
        for (ToggleableOverlayElement toggleableElement : scathaPro.getOverlay().toggleableOverlayElements)
        {
            this.listEntries.add(new OverlayToggleableElementEntry(toggleableElement));
        }
    }
    
    
    private class OverlayBackgroundEntry extends ListEntry
    {
        public OverlayBackgroundEntry()
        {
            addLabel("Background", 0, 5, getListWidth() - 75, 10);
            addButton(new BooleanSettingButton(0, getListWidth() - 70, 0, 70, 20, null, Config.Key.overlayBackgroundEnabled));
        }
    }
    
    private class OverlayToggleableElementEntry extends ListEntry
    {
        private ToggleableOverlayElement toggleableElement;
        private GuiButton button;
        
        public OverlayToggleableElementEntry(ToggleableOverlayElement toggleableElement)
        {
            this.toggleableElement = toggleableElement;
            
            addLabel(toggleableElement.elementName, 0, 5, getListWidth() - 75, 10);
            addButton(button = new GuiButton(0, getListWidth() - 70, 0, 70, 20, "?"));
            updateButtonText();
        }
        
        private void updateButtonText()
        {
            button.displayString = toggleableElement.isVisible() ? "ON" : "OFF";
        }
        
        @Override
        protected void onButtonPressed(GuiButton button)
        {
            switch (button.id)
            {
                case 0:
                    toggleableElement.toggle();
                    updateButtonText();
                    break;
            }
        }
    }
    
}