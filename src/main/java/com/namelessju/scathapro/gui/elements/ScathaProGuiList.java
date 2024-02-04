package com.namelessju.scathapro.gui.elements;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;

import java.util.List;

import com.google.common.collect.Lists;
import com.namelessju.scathapro.util.Util;

public abstract class ScathaProGuiList extends GuiListExtended
{
    protected final GuiScreen gui;
    protected final List<ListEntry> listEntries = Lists.<ListEntry>newArrayList();

    
    public ScathaProGuiList(GuiScreen gui, int entryHeight)
    {
        this(gui, 40, gui.height - 40, entryHeight);
    }
    
    public ScathaProGuiList(GuiScreen gui, int listTopY, int listBottomY, int entryHeight)
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
    
    public abstract class ListEntry implements IGuiListEntry
    {
        private abstract class EntryGuiElement
        {
            protected final int localX;
            protected final int localY;
            
            private EntryGuiElement(int localX, int localY)
            {
                this.localX = localX;
                this.localY = localY;
            }
            
            public abstract void updatePosition(int listX, int listY);
        }
        
        private class EntryButton extends EntryGuiElement
        {
            public final GuiButton button;
            
            public EntryButton(GuiButton button)
            {
                super(button.xPosition, button.yPosition);
                this.button = button;
            }
            
            public void updatePosition(int listX, int listY)
            {
                button.xPosition = listX + localX;
                button.yPosition = listY + localY;
            }
        }
        
        private class EntryLabel extends EntryGuiElement
        {
            public final GuiLabel label;
            
            public EntryLabel(GuiLabel label)
            {
                super(label.field_146162_g, label.field_146174_h);
                this.label = label;
            }
            
            public void updatePosition(int listX, int listY)
            {
                label.field_146162_g = listX + localX;
                label.field_146174_h = listY + localY;
            }
        }
        
        private class EntryTextField extends EntryGuiElement
        {
            public final ScathaProTextField textField;
            
            public EntryTextField(ScathaProTextField textField)
            {
                super(textField.xPosition, textField.yPosition);
                this.textField = textField;
            }
            
            public void updatePosition(int listX, int listY)
            {
                textField.xPosition = listX + localX;
                textField.yPosition = listY + localY;
            }
        }
        
        
        private final List<EntryButton> entryButtons = Lists.<EntryButton>newArrayList();
        private final List<EntryLabel> entryLabels = Lists.<EntryLabel>newArrayList();
        private final List<EntryTextField> entryTextFields = Lists.<EntryTextField>newArrayList();
        
        public void addButton(GuiButton button)
        {
            entryButtons.add(new EntryButton(button));
        }
        
        public GuiLabel addLabel(String text, int x, int y, int width, int height)
        {
            GuiLabel label = new GuiLabel(mc.fontRendererObj, 0, x, y, width, height, Util.Color.WHITE.getValue());
            label.func_175202_a(text);
            entryLabels.add(new EntryLabel(label));
            return label;
        }
        
        public void addTextField(ScathaProTextField textField)
        {
            entryTextFields.add(new EntryTextField(textField));
        }

        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
        {
            for (EntryButton entryButton : entryButtons)
            {
                entryButton.updatePosition(x, y);
                entryButton.button.drawButton(mc, mouseX, mouseY);
            }
            
            for (EntryLabel entryLabel : entryLabels)
            {
                entryLabel.updatePosition(x, y);
                entryLabel.label.drawLabel(mc, mouseX, mouseY);
            }
            
            for (EntryTextField entryTextField : entryTextFields)
            {
                entryTextField.updatePosition(x, y);
                entryTextField.textField.drawTextBox();
            }
        }
        
        public boolean mousePressed(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {
            for (EntryButton entryButton : entryButtons)
            {
                GuiButton button = entryButton.button;
                if (button.mousePressed(mc, x, y))
                {
                    button.playPressSound(mc.getSoundHandler());
                    if (button instanceof IClickActionButton) ((IClickActionButton) button).click();
                    onButtonPressed(button);
                    return true;
                }
            }
            
            return false;
        }
        
        /**
         * Similar to `mousePressed(...)`, but is always called regardless of whether the mouse is over this entry or not
         */
        public boolean mousePressedAlways(int x, int y, int mouseEvent)
        {
            boolean elementClicked = false;
            
            for (EntryTextField entryTextField : entryTextFields)
            {
                if (isMouseYWithinSlotBounds(mouseY))
                {
                    entryTextField.textField.mouseClicked(x, y, mouseEvent);
                    // ^ Why doesn't this return a boolean like buttons do...
                    boolean textFieldClicked = x >= entryTextField.textField.xPosition && x < entryTextField.textField.xPosition + entryTextField.textField.width && y >= entryTextField.textField.yPosition && y < entryTextField.textField.yPosition + entryTextField.textField.height;
                    if (textFieldClicked) elementClicked = true;
                }
                else
                {
                    entryTextField.textField.setFocused(false);
                }
            }
            
            return elementClicked;
        }
        
        public void keyTyped(char character, int code)
        {
            for (EntryTextField entryTextField : entryTextFields)
            {
                entryTextField.textField.textboxKeyTyped(character, code);
            }
        }
        
        protected void onButtonPressed(GuiButton button) { }
        
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {
            for (EntryButton entryButton : entryButtons)
            {
                entryButton.button.mouseReleased(x, y);
            }
        }
        
        public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_) {}
    }
}