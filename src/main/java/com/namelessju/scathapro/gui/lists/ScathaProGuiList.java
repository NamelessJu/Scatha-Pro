package com.namelessju.scathapro.gui.lists;

import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;

import java.util.List;

import com.google.common.collect.Lists;
import com.namelessju.scathapro.gui.elements.IGuiElement;
import com.namelessju.scathapro.gui.elements.ScathaProButton;
import com.namelessju.scathapro.gui.elements.ScathaProTextField;
import com.namelessju.scathapro.gui.menus.ScathaProGui;

public abstract class ScathaProGuiList extends GuiListExtended 
{
    protected final ScathaProGui gui;
    protected final List<ListEntry> listEntries = Lists.<ListEntry>newArrayList();

    
    public ScathaProGuiList(ScathaProGui gui, int entryHeight)
    {
        this(gui, 37, gui.height - 40, entryHeight);
    }
    
    public ScathaProGuiList(ScathaProGui gui, int listTopY, int listBottomY, int entryHeight)
    {
        super(gui.mc, gui.width, gui.height, listTopY, listBottomY, entryHeight);
        this.gui = gui;
    }
    
    @Override
    protected int getSize()
    {
        return listEntries.size();
    }
    
    @Override
    public IGuiListEntry getListEntry(int index)
    {
        return listEntries.get(index);
    }

    @Override
    protected int getScrollBarX()
    {
        return this.width / 2 + getListWidth() / 2 + 2 + 5;
    }

    @Override
    public int getListWidth()
    {
        return 310;
    }
    
    public void tick()
    {
        for (ListEntry entry : listEntries) entry.tick();
    }
    
    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseEvent)
    {
        boolean elementClicked = false;
        
        if (super.mouseClicked(mouseX, mouseY, mouseEvent)) elementClicked = true;
        
        for (ListEntry entry : listEntries)
        {
            if (entry.mousePressedAlways(mouseX, mouseY, mouseEvent))
            {
                elementClicked = true;
                setEnabled(false);
            }
        }
        
        return elementClicked;
    }
    
    public void keyTyped(char character, int code)
    {
        for (ListEntry entry : listEntries)
        {
            entry.keyTyped(character, code);
        }
    }
    
    @Override
    public void drawScreen(int mouseXIn, int mouseYIn, float partialTicks)
    {
        if (this.field_178041_q)
        {
            this.mouseX = mouseXIn;
            this.mouseY = mouseYIn;
            this.drawBackground();
            int i = this.getScrollBarX();
            int j = i + 6;
            this.bindAmountScrolled();
            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            if (mc.theWorld == null) this.drawContainerBackground(tessellator);
            int k = this.left + this.width / 2 - this.getListWidth() / 2;
            int l = this.top + 4 - (int) this.amountScrolled;
            
            if (this.hasListHeader)
            {
                this.drawListHeader(k, l, tessellator);
            }
            
            this.drawSelectionBox(k, l, mouseXIn, mouseYIn);
            GlStateManager.disableDepth();
            int i1 = 4;
            this.overlayBackground(0, this.top, 255, 255);
            this.overlayBackground(this.bottom, this.height, 255, 255);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
            GlStateManager.disableAlpha();
            GlStateManager.shadeModel(7425);
            GlStateManager.disableTexture2D();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldrenderer.pos((double)this.left, (double)(this.top + i1), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 0).endVertex();
            worldrenderer.pos((double)this.right, (double)(this.top + i1), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 0).endVertex();
            worldrenderer.pos((double)this.right, (double)this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos((double)this.left, (double)this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
            tessellator.draw();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldrenderer.pos((double)this.left, (double)this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos((double)this.right, (double)this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos((double)this.right, (double)(this.bottom - i1), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 0).endVertex();
            worldrenderer.pos((double)this.left, (double)(this.bottom - i1), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 0).endVertex();
            tessellator.draw();
            int j1 = this.func_148135_f();

            if (j1 > 0)
            {
                int k1 = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
                k1 = MathHelper.clamp_int(k1, 32, this.bottom - this.top - 8);
                int l1 = (int)this.amountScrolled * (this.bottom - this.top - k1) / j1 + this.top;

                if (l1 < this.top)
                {
                    l1 = this.top;
                }

                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                worldrenderer.pos((double)i, (double)this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos((double)j, (double)this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos((double)j, (double)this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos((double)i, (double)this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                tessellator.draw();
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                worldrenderer.pos((double)i, (double)(l1 + k1), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                worldrenderer.pos((double)j, (double)(l1 + k1), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                worldrenderer.pos((double)j, (double)l1, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                worldrenderer.pos((double)i, (double)l1, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                tessellator.draw();
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                worldrenderer.pos((double)i, (double)(l1 + k1 - 1), 0.0D).tex(0.0D, 1.0D).color(192, 192, 192, 255).endVertex();
                worldrenderer.pos((double)(j - 1), (double)(l1 + k1 - 1), 0.0D).tex(1.0D, 1.0D).color(192, 192, 192, 255).endVertex();
                worldrenderer.pos((double)(j - 1), (double)l1, 0.0D).tex(1.0D, 0.0D).color(192, 192, 192, 255).endVertex();
                worldrenderer.pos((double)i, (double)l1, 0.0D).tex(0.0D, 0.0D).color(192, 192, 192, 255).endVertex();
                tessellator.draw();
            }

            this.func_148142_b(mouseXIn, mouseYIn);
            GlStateManager.enableTexture2D();
            GlStateManager.shadeModel(7424);
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
        }
        
        if (!this.isMouseYWithinSlotBounds(mouseY))
        {
            gui.tooltipToRender = null;
        }
    }
    
    
    public abstract class ListEntry implements IGuiListEntry
    {
        private class EntryElement<T extends IGuiElement>
        {
            public final T element;
            
            protected int localX = 0;
            protected int localY = 0;
            
            public EntryElement(T element)
            {
                this.element = element;
                setLocalPosition(element.getElementX(), element.getElementY());
            }
            
            public void setLocalPosition(int x, int y)
            {
                localX = x;
                localY = y;
            }
            
            public void updatePosition(int listX, int listY)
            {
                element.setElementX(listX + localX);
                element.setElementY(listY + localY);
            }
        }
        
        
        private final List<EntryElement<IGuiElement>> entryElements = Lists.<EntryElement<IGuiElement>>newArrayList();
        
        public void addElement(IGuiElement element)
        {
            entryElements.add(new EntryElement<IGuiElement>(element));
        }
        
        public void tick()
        {
            for (EntryElement<IGuiElement> entryElement : entryElements)
            {
            	entryElement.element.elementTick();
            }
        }
        
        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
        {
            if (!isEntryWithinSlotBounds(y, slotHeight)) return;
            
            for (EntryElement<IGuiElement> entryElement : entryElements)
            {
                entryElement.updatePosition(x, y);
                entryElement.element.elementDraw(mouseX, mouseY);
            }
        }
        
        protected boolean isEntryWithinSlotBounds(int y, int slotHeight)
        {
            return y + slotHeight >= ScathaProGuiList.this.top && y <= ScathaProGuiList.this.bottom;
        }
        
        @Override
        public boolean mousePressed(int slotIndex, int x, int y, int mouseButton, int relativeX, int relativeY)
        {
            for (EntryElement<IGuiElement> entryElement : entryElements)
            {
                if (entryElement.element.elementMouseClicked(x, y, mouseButton))
                {
                    if (entryElement.element instanceof ScathaProButton) onButtonPressed((ScathaProButton) entryElement.element);
                    return true;
                }
            }
            
            return false;
        }
        
        /**
         * Similar to `mousePressed(...)`, but is always called regardless of whether the mouse is over this entry or not
         */
        public boolean mousePressedAlways(int x, int y, int mouseButton)
        {
            boolean elementClicked = false;

            for (EntryElement<IGuiElement> entryElement : entryElements)
            {
                if (entryElement.element instanceof ScathaProTextField)
                {
                    ScathaProTextField textField = (ScathaProTextField) entryElement.element;
                    if (isMouseYWithinSlotBounds(y))
                    {
                        if (textField.elementMouseClicked(x, y, mouseButton)) elementClicked = true;
                    }
                    else textField.setFocused(false);
                }
            }
            
            return elementClicked;
        }
        
        @Override
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {
            for (EntryElement<IGuiElement> entryElement : entryElements)
            {
            	entryElement.element.elementMouseReleased(x, y);
            }
        }
        
        public void keyTyped(char character, int code)
        {
            for (EntryElement<IGuiElement> entryElement : entryElements)
            {
            	entryElement.element.elementKeyTyped(character, code);
            	
                if (entryElement.element instanceof ScathaProTextField)
                {
                    ScathaProTextField textField = (ScathaProTextField) entryElement.element;
                    if (textField.isFocused()) onTextFieldTyped(textField);
                }
            }
        }
        
        protected void onTextFieldTyped(ScathaProTextField textField) {}
        protected void onButtonPressed(ScathaProButton button) {}
        
        @Override
        public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_) {}
    }
}