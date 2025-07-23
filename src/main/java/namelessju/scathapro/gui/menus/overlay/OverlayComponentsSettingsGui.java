package namelessju.scathapro.gui.menus.overlay;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.elements.HoverArea;
import namelessju.scathapro.gui.lists.OverlayComponentsGuiList;
import namelessju.scathapro.gui.menus.ConfigGui;
import namelessju.scathapro.gui.menus.ScathaProGui;
import namelessju.scathapro.overlay.Overlay;
import namelessju.scathapro.overlay.Overlay.ToggleableOverlayElement;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.Util;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class OverlayComponentsSettingsGui extends ConfigGui
{
    public final ResourceLocation stoneTexture = new ResourceLocation("textures/blocks/stone.png");
    
    
    private boolean showFullPreview = false;
    private HoverArea previewHoverArea;

    public OverlayComponentsSettingsGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
    }
    
    @Override
    public String getTitle()
    {
        return "Overlay Components";
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        scrollList = new OverlayComponentsGuiList(this);
        
        elements.add(previewHoverArea = new HoverArea(1, width / 2 - 75, 35, 150, 20, "Preview"));
        
        addDoneButton(this.width / 2 - 100, this.height - 30, 200, 20);
    }
    
    @Override
    public void updateScreen()
    {
        super.updateScreen();
        
        if (showFullPreview)
        {
            scathaPro.getOverlay().updateRealtimeElements();
        }
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
        
        Overlay overlay = scathaPro.getOverlay();
        
        showFullPreview = previewHoverArea.isHovered(mouseX, mouseY);
        if (showFullPreview)
        {
            overlay.updateRealtimeElements();
            
            int overlayWidth = overlay.getWidth();
            int overlayHeight = overlay.getHeight();
            int overlayX = width / 2 - overlayWidth / 2;
            int y = 60;
            int tooltipX = overlayX - 8;
            int tooltipWidth = overlayWidth + 16;
            int tooltipHeight = overlayHeight + 8;
            
            ScathaProGui.drawTooltipBox(tooltipX, y, tooltipWidth, tooltipHeight);
            
            Util.startImageRendering();
            mc.getTextureManager().bindTexture(stoneTexture);
            Gui.drawScaledCustomSizeModalRect(tooltipX - 1, y - 1, 0f, 0f, tooltipWidth / 2 - 1, tooltipHeight / 2 - 1, tooltipWidth + 2, tooltipHeight + 2, 16f, 16f);
            Util.endImageRendering();
            
            overlay.drawOverlayUntransformedAt(overlayX, y + 4);
        }
        else
        {
            ToggleableOverlayElement hoveredElement = ((OverlayComponentsGuiList) scrollList).getHoveredElement();
            if (hoveredElement != null)
            {
                overlay.updateRealtimeElements();
                
                boolean visibleBefore = hoveredElement.isVisible();
                hoveredElement.setVisible(true);
                
                int elementHeight = hoveredElement.element.getHeight();
                
                GlStateManager.pushMatrix();
                GlStateManager.translate(mouseX + 12, mouseY - 12, 0);
                boolean hasDescription = hoveredElement.description != null;
                int tooltipWidth = Math.max(hoveredElement.element.getWidth(), TextUtil.getStringWidth(hoveredElement.description));
                ScathaProGui.drawTooltipBox(0, 0, tooltipWidth, hasDescription ? elementHeight + 5 + 8 : elementHeight);
                hoveredElement.element.draw(false, false, null);
                if (hasDescription) fontRendererObj.drawString(hoveredElement.description, 0, elementHeight + 5, Util.Color.WHITE);
                GlStateManager.popMatrix();
                
                
                hoveredElement.setVisible(visibleBefore);
            }
        }
    }
    
    @Override
    public void onGuiClosed()
    {
        scathaPro.getOverlay().saveToggleableElementStates();
        
        super.onGuiClosed();
    }
}
