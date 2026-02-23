package namelessju.scathapro.gui.menus.widgets;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.screens.ConfigScreen;
import namelessju.scathapro.gui.menus.framework.widgets.lists.ScathaProGuiList;
import namelessju.scathapro.gui.overlay.MainOverlay;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;

public class OverlayElementList extends ScathaProGuiList
{
    public OverlayElementList(ScathaPro scathaPro, Screen screen, HeaderAndFooterLayout layout)
    {
        super(scathaPro.minecraft, screen, layout);
        
        CycleButton<Boolean> backgroundButton = ConfigScreen.booleanConfigButton("Darkened Background",
            scathaPro.config.overlay.backgroundEnabled,
            null, (button, enabled) -> scathaPro.mainOverlay.updateBackground()
        );
        backgroundButton.setSize(getRowWidth(), DEFAULT_ENTRY_CONTENT_HEIGHT);
        addEntry(new Entry(backgroundButton));
        
        for (MainOverlay.ToggleableOverlayElement element : scathaPro.mainOverlay.toggleableElements)
        {
            addEntry(new ToggleableElementEntry(element));
        }
    }
    
    public ToggleableElementEntry getHoveredElementEntry()
    {
        if (super.getHovered() instanceof ToggleableElementEntry elementEntry)
        {
            return elementEntry.button.isHovered() ? elementEntry : null;
        }
        return null;
    }
    
    public class ToggleableElementEntry extends Entry
    {
        public final MainOverlay.ToggleableOverlayElement toggleableElement;
        public final CycleButton<Boolean> button;
        public final Tooltip descriptionTooltip;
        
        protected ToggleableElementEntry(MainOverlay.ToggleableOverlayElement toggleableElement)
        {
            this.toggleableElement = toggleableElement;
            
            addChild(button = ConfigScreen.booleanConfigButton(toggleableElement.elementName(), toggleableElement.configValue(), null,
                (button, enabled) -> toggleableElement.updateVisibility()
            ));
            button.setSize(getRowWidth(), DEFAULT_ENTRY_CONTENT_HEIGHT);
            
            descriptionTooltip = toggleableElement.description() != null ? Tooltip.create(toggleableElement.description()) : null;
        }
    }
}
