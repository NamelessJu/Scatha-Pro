package namelessju.scathapro.gui.menus.widgets;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.screens.ConfigScreen;
import namelessju.scathapro.gui.menus.framework.widgets.lists.ScathaProGuiList;
import namelessju.scathapro.gui.menus.framework.widgets.sliders.FloatSlider;
import namelessju.scathapro.gui.overlay.MainOverlay;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;

public class OverlayContentList extends ScathaProGuiList
{
    public OverlayContentList(ScathaPro scathaPro, Screen screen, HeaderAndFooterLayout layout)
    {
        super(scathaPro.minecraft, screen, layout);

        FloatSlider backgroundSlider = ConfigScreen.floatConfigSlider(
            "Darkened Background Opacity", 0f, 1f,
            scathaPro.config.overlay.backgroundOpacity, null
        ).setStepSize(0.01f).setValueComponentSupplier(FloatSlider.PERCENTAGE_COMPONENT_SUPPLIER_WITH_OFF);
        backgroundSlider.setSize(getRowWidth(), DEFAULT_ENTRY_CONTENT_HEIGHT);
        addEntry(new Entry(backgroundSlider));

        CycleButton<Boolean> iconsButton = ConfigScreen.booleanConfigButton(
            "Text Icons", scathaPro.config.overlay.iconsEnabled
        );
        iconsButton.setSize(getRowWidth(), DEFAULT_ENTRY_CONTENT_HEIGHT);
        addEntry(new Entry(iconsButton));

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

            addChild(button = ConfigScreen.booleanConfigButton(toggleableElement.elementName(), toggleableElement.configValue(), null, null));
            button.setSize(getRowWidth(), DEFAULT_ENTRY_CONTENT_HEIGHT);

            descriptionTooltip = toggleableElement.description() != null ? Tooltip.create(toggleableElement.description()) : null;
        }
    }
}