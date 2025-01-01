package com.namelessju.scathapro.gui.lists;

import com.namelessju.scathapro.gui.elements.BooleanSettingButton;
import com.namelessju.scathapro.gui.elements.ScathaProButton;
import com.namelessju.scathapro.gui.menus.ScathaProGui;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.overlay.Overlay.ToggleableOverlayElement;

public class OverlayComponentsGuiList extends ScathaProGuiList
{
    public OverlayComponentsGuiList(ScathaProGui gui)
    {
        super(gui, 63, gui.height - 40, 25);
        
        this.listEntries.add(new OverlayBackgroundEntry());
        
        for (ToggleableOverlayElement toggleableElement : gui.scathaPro.getOverlay().toggleableOverlayElements)
        {
            this.listEntries.add(new OverlayToggleableElementEntry(toggleableElement));
        }
    }
    
    
    private class OverlayBackgroundEntry extends ListEntry
    {
        public OverlayBackgroundEntry()
        {
            addElement(new BooleanSettingButton(0, 0, 2, getListWidth(), 20, "Background", Config.Key.overlayBackgroundEnabled));
        }
    }
    
    private class OverlayToggleableElementEntry extends ListEntry
    {
        private ToggleableOverlayElement toggleableElement;
        private ScathaProButton button;
        
        public OverlayToggleableElementEntry(ToggleableOverlayElement toggleableElement)
        {
            this.toggleableElement = toggleableElement;
            
            addElement(button = new ScathaProButton(0, 0, 2, getListWidth(), 20, ""));
            updateButtonText();
        }
        
        private void updateButtonText()
        {
            button.displayString = toggleableElement.elementName + ": " + (toggleableElement.isVisible() ? "ON" : "OFF");
        }
        
        @Override
        protected void onButtonPressed(ScathaProButton button)
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