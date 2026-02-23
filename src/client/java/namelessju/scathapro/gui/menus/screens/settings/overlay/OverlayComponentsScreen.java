package namelessju.scathapro.gui.menus.screens.settings.overlay;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.screens.ConfigScreen;
import namelessju.scathapro.gui.menus.widgets.OverlayElementList;
import namelessju.scathapro.gui.overlay.elements.GuiElement;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.MenuTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.apache.commons.compress.utils.Lists;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class OverlayComponentsScreen extends ConfigScreen
{
    private Button previewHoverArea;
    
    private final GuiElementTooltip guiElementTooltipComponent;
    private Tooltip descriptionTooltip = null;
    private OverlayElementList elementList;
    
    public OverlayComponentsScreen(ScathaPro scathaPro, Screen parentScreen)
    {
        super(scathaPro, "Overlay Components", parentScreen);
        guiElementTooltipComponent = new GuiElementTooltip(scathaPro);
    }
    
    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader(previewHoverArea = Button.builder(Component.literal("[Hover] Preview"), button -> {}).width(100).build());
        
        elementList = addScrollList(new OverlayElementList(scathaPro, this, layout));
        
        addDoneButtonFooter();
    }
    
    @Override
    public void tick()
    {
        super.tick();
        
        scathaPro.mainOverlay.requestUpdateTick();
    }
    
    @Override
    public void render(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        
        if (previewHoverArea.isHovered())
        {
            descriptionTooltip = null;
            guiElementTooltipComponent.setContent(scathaPro.mainOverlay.getMainElement(), true);
            renderElementTooltip(guiGraphics, mouseX, mouseY, new BelowWidgetTooltipPositioner(previewHoverArea.getRectangle()));
        }
        else
        {
            OverlayElementList.ToggleableElementEntry hoveredEntry = elementList.getHoveredElementEntry();
            if (hoveredEntry == null) return;
            
            descriptionTooltip = hoveredEntry.descriptionTooltip;
            guiElementTooltipComponent.setContent(hoveredEntry.toggleableElement.element(), false);
            renderElementTooltip(guiGraphics, mouseX, mouseY, new MenuTooltipPositioner(hoveredEntry.button.getRectangle()));
        }
    }
    
    private void renderElementTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, ClientTooltipPositioner positioner)
    {
        guiGraphics.renderTooltip(font, getTooltipComponents(), mouseX, mouseY, positioner, null);
    }
    
    private List<ClientTooltipComponent> getTooltipComponents()
    {
        List<ClientTooltipComponent> components = Lists.newArrayList();
        components.add(guiElementTooltipComponent);
        if (descriptionTooltip != null)
        {
            components.addAll(
                descriptionTooltip.toCharSequence(minecraft).stream()
                .map(ClientTooltipComponent::create)
                .toList()
            );
        }
        return components;
    }
    
    private static class GuiElementTooltip implements ClientTooltipComponent
    {
        private final Identifier tooltipBackgroundIdentifier = Identifier.withDefaultNamespace("textures/block/stone.png");
        @SuppressWarnings("FieldCanBeLocal")
        private final int tooltipBackgroundTextureWidth = 16;
        @SuppressWarnings("FieldCanBeLocal")
        private final int tooltipBackgroundTextureHeight = 16;
        
        private final int tooltipBackgroundPaddingHorizontal = 6;
        private final int tooltipBackgroundPaddingVertical = 5;
        
        
        private final ScathaPro scathaPro;
        
        private GuiElement guiElement = null;
        private boolean hasBackgroundTexture = false;
        private int width = 0;
        private int height = 0;
        
        public GuiElementTooltip(ScathaPro scathaPro)
        {
            this.scathaPro = scathaPro;
        }
        
        public void setContent(GuiElement guiElement, boolean hasBackgroundTexture)
        {
            this.hasBackgroundTexture = hasBackgroundTexture;
            if (guiElement == this.guiElement) return;
            this.guiElement = guiElement;
            this.width = guiElement.getWidth() + (hasBackgroundTexture ? tooltipBackgroundPaddingHorizontal * 2 : 0);
            this.height = guiElement.getHeight() + (hasBackgroundTexture ? tooltipBackgroundPaddingVertical * 2 : 0);
        }
        
        @Override
        public int getWidth(@NonNull Font font)
        {
            return width;
        }
        
        @Override
        public int getHeight(@NonNull Font font)
        {
            return height + 1;
        }
        
        @Override
        public void renderImage(@NonNull Font font, int x, int y, int width, int height, @NonNull GuiGraphics guiGraphics)
        {
            if (guiElement == null) return;
            
            if (hasBackgroundTexture)
            {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, tooltipBackgroundIdentifier,
                    x - 1, y - 1, 0f, 0f,
                    width + 2, height + 2,
                    width / 2 - 1, height / 2 - 1,
                    tooltipBackgroundTextureWidth, tooltipBackgroundTextureHeight
                );
            }
            
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate(x, y);
            if (hasBackgroundTexture) guiGraphics.pose().translate(tooltipBackgroundPaddingHorizontal, tooltipBackgroundPaddingVertical);
            guiElement.forceRender(guiGraphics, scathaPro.minecraft.getDeltaTracker(), false, false, null);
            guiGraphics.pose().popMatrix();
        }
    }
    
    private record BelowWidgetTooltipPositioner(ScreenRectangle widgetRectangle) implements ClientTooltipPositioner
    {
        @Override
        public @NonNull Vector2ic positionTooltip(int screenWidth, int screenHeight, int mouseX, int mouseY, int tooltipWidth, int tooltipHeight)
        {
            return new Vector2i(widgetRectangle.getCenterInAxis(ScreenAxis.HORIZONTAL) - tooltipWidth / 2, widgetRectangle.bottom() + 5);
        }
    }
}
