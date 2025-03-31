package com.namelessju.scathapro.gui.elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.namelessju.scathapro.util.TextUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StringUtils;

public class ScathaProTextField extends Gui implements IGuiElement, ITooltipElement
{
    private final int id;
    private final FontRenderer fontRenderer;
    private String text = "";
    private int maxStringLength = 32;
    private int cursorBlinkCounter = 0;
    private boolean isFocused = false;
    private boolean isEnabled = true;
    private int lineScrollOffset = 0;
    private int cursorPosition = 0;
    private int selectionEnd = 0;
    private boolean visible = true;
    private Predicate<String> textPredicate = Predicates.<String>alwaysTrue();

    public int xPosition;
    public int yPosition;
    public int width;
    public int height;
    
    private final Tooltip tooltip = new Tooltip();
    private String defaultFormatting = null;
    private String placeholder = null;
    private boolean supportsFormatting = false;
    private String formattedText = "";
    
    public ScathaProTextField(int componentId, int x, int y, int width, int height)
    {
        this.id = componentId;
        this.fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        this.xPosition = x;
        this.yPosition = y;
        this.width = width;
        this.height = height;
    }
    
    @Override
    public void setElementX(int x)
    {
        this.xPosition = x;
    }
    
    @Override
    public void setElementY(int y)
    {
        this.yPosition = y;
    }
    
    @Override
    public void setElementWidth(int width)
    {
        this.width = width;
    }
    
    @Override
    public void setElementHeight(int height)
    {
        this.height = height;
    }
    
    @Override
    public int getElementX()
    {
        return this.xPosition;
    }
    
    @Override
    public int getElementY()
    {
        return this.yPosition;
    }
    
    @Override
    public int getElementHeight()
    {
        return this.height;
    }
    
    @Override
    public int getElementWidth()
    {
        return this.width;
    }
    
    @Override
    public void elementTick()
    {
        ++this.cursorBlinkCounter;
    }
    
    @Override
    public void elementKeyTyped(char character, int code)
    {
        this.textboxKeyTyped(character, code);
    }
    
    @Override
    public void elementDraw(int mouseX, int mouseY)
    {
        this.drawTextBox(mouseX, mouseY);
    }
    
    @Override
    public Tooltip getTooltip()
    {
        return tooltip;
    }
    
    public ScathaProTextField setDefaultFormatting(String formatting)
    {
        this.defaultFormatting = formatting;
        return this;
    }
    
    public ScathaProTextField setPlaceholder(String placeholder)
    {
        this.placeholder = placeholder;
        return this;
    }
    
    public ScathaProTextField setSupportsFormatting(boolean value)
    {
        this.supportsFormatting = value;
        if (this.supportsFormatting) updateFormattedText();
        if (this.text.length() > 0) this.setSelectionPos(this.selectionEnd);
        return this;
    }
    
    public void updateFormattedText()
    {
        if (!supportsFormatting)
        {
            this.formattedText = "";
            return;
        }
        
        Matcher formattingCodeMatcher = Pattern.compile("&" + TextUtil.formattingCodesRegex).matcher(this.text);
        
        String formattedText = this.text;
        int indexOffset = 0; 
        String previousStyling = "";
        while (formattingCodeMatcher.find())
        {
            int index = formattingCodeMatcher.start() + indexOffset;
            String code = formattingCodeMatcher.group();
            
            String realFormatting = code.replace("&", TextUtil.formattingStartCharacter);
            String codeFormatted = EnumChatFormatting.DARK_GRAY + code + EnumChatFormatting.RESET + (this.defaultFormatting != null ? this.defaultFormatting : "") + ("&r".equals(code) ? "" : previousStyling + realFormatting);
            
            if (!TextUtil.isFancyFormattingCode(realFormatting.charAt(1))) previousStyling = "";
            previousStyling += realFormatting;
            
            formattedText = formattedText.substring(0, index) + codeFormatted + formattedText.substring(index + code.length());
            
            indexOffset += codeFormatted.length() - code.length();
        }
        
        this.formattedText = formattedText;
    }
    
    /**
     * Returns the formatted text if formatting is supported, otherwise returns the regular text
     */
    private String getFormattableText()
    {
        return EnumChatFormatting.RESET.toString() + (defaultFormatting != null ? defaultFormatting : "") + (this.supportsFormatting ? this.formattedText : this.text);
    }
    
    public void drawTextBox(int mouseX, int mouseY)
    {
        if (!this.getVisible()) return;
        
        int outlineColor = this.isEnabled ? -6250336 : Util.Color.DARK_GRAY;
        drawRect(this.xPosition - 1, this.yPosition - 1, this.xPosition + this.width + 1, this.yPosition + this.height + 1, outlineColor);
        drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, -16777216);

        
        int textOffset = getFormattableStringWidthAtIndex(this.lineScrollOffset);
        int textBaseX = this.xPosition + 4; // X position without offset
        int textXWithOffset = textBaseX - textOffset;
        int textY = this.yPosition + (this.height - 8) / 2;
        
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GL11.glScissor((xPosition + 1) * scaledResolution.getScaleFactor(), Minecraft.getMinecraft().displayHeight - (yPosition + height) * scaledResolution.getScaleFactor(), (width - 2) * scaledResolution.getScaleFactor(), height * scaledResolution.getScaleFactor());
        GlStateManager.popMatrix();
        
        if (this.text.length() > 0)
        {
            this.fontRenderer.drawStringWithShadow(getFormattableText(), textXWithOffset, textY, this.isEnabled ? 14737632 : 7368816);
        }
        else if (this.placeholder != null)
        {
            this.fontRenderer.drawStringWithShadow(TextUtil.trimStringToWidth(this.placeholder, this.getInnerWidth()), textBaseX, textY, Util.Color.DARK_GRAY);
        }
        
        // Caret
        
        int caretX = textXWithOffset + getFormattableStringWidthAtIndex(this.cursorPosition);
        if (this.isFocused && this.cursorBlinkCounter / 10 % 2 == 0)
        {
            Gui.drawRect(caretX, textY - 1, caretX + 1, textY + this.fontRenderer.FONT_HEIGHT + 1, -3092272);
        }
        
        // Selection
        
        if (this.cursorPosition != this.selectionEnd)
        {
            int selectionEndX = textXWithOffset + getFormattableStringWidthAtIndex(this.selectionEnd);
            this.drawSelection(caretX, textY - 1, selectionEndX - 1, textY + 1 + this.fontRenderer.FONT_HEIGHT);
        }
        
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        
        boolean hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
        if (hovered) this.tooltip.requestRender();
    }
    
    /**
     * Returns the length of the formattable string until the specified index (exclusive), ignoring any formatting codes for the index value.
     */
    private int getFormattableStringWidthAtIndex(int index)
    {
        String partialString = null;
        if (this.supportsFormatting)
        {
            String string = this.formattedText;
            int currentNonFormattingCharIndex = 0;
            for (int i = 0; i < string.length(); i ++)
            {
                if (currentNonFormattingCharIndex >= index)
                {
                    partialString = string.substring(0, i);
                    break;
                }
                if (i >= string.length() - 1)
                {
                    partialString = string;
                    break;
                }
                
                if (string.length() >= i + 2 && TextUtil.isFormattingSequence(string.substring(i, i + 2)))
                {
                    i++;
                    continue;
                }
                
                currentNonFormattingCharIndex ++;
            }
        }
        else partialString = this.text.substring(0, index);
        
        if (partialString == null) return 0;
        return TextUtil.getStringWidth(partialString);
    }
    
    private int getFormattableStringIndexAtWidth(int width)
    {
        return StringUtils.stripControlCodes(TextUtil.trimStringToWidth(getFormattableText(), width)).length();
    }
    
    @Override
    public boolean elementMouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        boolean isMouseOver = mouseX >= this.xPosition && mouseX < this.xPosition + this.width && mouseY >= this.yPosition && mouseY < this.yPosition + this.height;
        
        this.setFocused(this.isEnabled ? isMouseOver : false);

        if (this.isFocused && isMouseOver && mouseButton == 0)
        {
            int mouseXRelative = mouseX - this.xPosition - 4 + getFormattableStringWidthAtIndex(this.lineScrollOffset);
            
            String trimmedString = TextUtil.trimStringToWidth(getFormattableText(), mouseXRelative);
            int cursorIndex = StringUtils.stripControlCodes(trimmedString).length();
            if (cursorIndex < this.text.length())
            {
                char nextChar = this.text.charAt(cursorIndex);
                if (mouseXRelative - TextUtil.getStringWidth(trimmedString) >= fontRenderer.getCharWidth(nextChar) / 2f)
                {
                    cursorIndex ++;
                }
            }
            this.setCursorPosition(cursorIndex);
            
            this.cursorBlinkCounter = 0;
            
            return true;
        }
        
        return false;
    }

    public void setEnabled(boolean enabled)
    {
        this.isEnabled = enabled;
        
        if (!this.isEnabled) this.setFocused(false);
    }
    
    public void setText(String newText)
    {
        newText = StringUtils.stripControlCodes(newText);
        
        if (this.textPredicate.apply(newText))
        {
            if (newText.length() > this.maxStringLength)
            {
                this.text = newText.substring(0, this.maxStringLength);
            }
            else
            {
                this.text = newText;
            }
            
            updateFormattedText();

            this.setCursorPositionEnd();
        }
    }
    
    public String getText()
    {
        return this.text;
    }
    
    public String getSelectedText()
    {
        int i = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
        int j = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
        return this.text.substring(i, j);
    }

    public void setTextPredicate(Predicate<String> predicate)
    {
        this.textPredicate = predicate;
    }
    
    public void writeText(String text)
    {
        String s = "";
        String s1 = ChatAllowedCharacters.filterAllowedCharacters(StringUtils.stripControlCodes(text));
        int i = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
        int j = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
        int k = this.maxStringLength - this.text.length() - (i - j);
        int l = 0;
        
        if (this.text.length() > 0)
        {
            s = s + this.text.substring(0, i);
        }

        if (k < s1.length())
        {
            s = s + s1.substring(0, k);
            l = k;
        }
        else
        {
            s = s + s1;
            l = s1.length();
        }

        if (this.text.length() > 0 && j < this.text.length())
        {
            s = s + this.text.substring(j);
        }

        if (this.textPredicate.apply(s))
        {
            this.text = s;
            updateFormattedText();
            this.moveCursorBy(i - this.selectionEnd + l);
        }
    }
    
    public void deleteWords(int p_146177_1_)
    {
        if (this.text.length() != 0)
        {
            if (this.selectionEnd != this.cursorPosition)
            {
                this.writeText("");
            }
            else
            {
                this.deleteFromCursor(this.getNthWordFromCursor(p_146177_1_) - this.cursorPosition);
            }
        }
    }
    
    public void deleteFromCursor(int characterCount)
    {
        if (this.text.length() != 0)
        {
            if (this.selectionEnd != this.cursorPosition)
            {
                this.writeText("");
            }
            else
            {
                boolean flag = characterCount < 0;
                int i = flag ? this.cursorPosition + characterCount : this.cursorPosition;
                int j = flag ? this.cursorPosition : this.cursorPosition + characterCount;
                String s = "";

                if (i >= 0)
                {
                    s = this.text.substring(0, i);
                }

                if (j < this.text.length())
                {
                    s = s + this.text.substring(j);
                }

                if (this.textPredicate.apply(s))
                {
                    this.text = s;
                    
                    updateFormattedText();

                    if (flag)
                    {
                        this.moveCursorBy(characterCount);
                    }
                }
            }
        }
    }

    public int getId()
    {
        return this.id;
    }

    /**
     * see @getNthNextWordFromPos() params: N, position
     */
    public int getNthWordFromCursor(int n)
    {
        return this.getNthWordFromPos(n, this.getCursorPosition());
    }

    /**
     * gets the position of the nth word. N may be negative, then it looks backwards. params: N, position
     */
    public int getNthWordFromPos(int p_146183_1_, int p_146183_2_)
    {
        return this.func_146197_a(p_146183_1_, p_146183_2_, true);
    }

    public int func_146197_a(int p_146197_1_, int p_146197_2_, boolean p_146197_3_)
    {
        int i = p_146197_2_;
        boolean flag = p_146197_1_ < 0;
        int j = Math.abs(p_146197_1_);

        for (int k = 0; k < j; ++k)
        {
            if (!flag)
            {
                int l = this.text.length();
                i = this.text.indexOf(32, i);

                if (i == -1)
                {
                    i = l;
                }
                else
                {
                    while (p_146197_3_ && i < l && this.text.charAt(i) == 32)
                    {
                        ++i;
                    }
                }
            }
            else
            {
                while (p_146197_3_ && i > 0 && this.text.charAt(i - 1) == 32)
                {
                    --i;
                }

                while (i > 0 && this.text.charAt(i - 1) != 32)
                {
                    --i;
                }
            }
        }

        return i;
    }

    /**
     * Moves the text cursor by a specified number of characters and clears the selection
     */
    public void moveCursorBy(int p_146182_1_)
    {
        this.setCursorPosition(this.selectionEnd + p_146182_1_);
    }

    /**
     * sets the position of the cursor to the provided index
     */
    public void setCursorPosition(int p_146190_1_)
    {
        this.cursorPosition = MathHelper.clamp_int(p_146190_1_, 0, this.text.length());
        this.setSelectionPos(this.cursorPosition);
        this.cursorBlinkCounter = 0;
    }

    /**
     * sets the cursors position to the beginning
     */
    public void setCursorPositionZero()
    {
        this.setCursorPosition(0);
    }

    /**
     * sets the cursors position to after the text
     */
    public void setCursorPositionEnd()
    {
        this.setCursorPosition(this.text.length());
    }
    
    /**
     * Call this method from your GuiScreen to process the keys into the textbox
     */
    protected boolean textboxKeyTyped(char character, int keyCode)
    {
        if (!this.isFocused)
        {
            return false;
        }
        else if (GuiScreen.isKeyComboCtrlA(keyCode))
        {
            this.setCursorPositionEnd();
            this.setSelectionPos(0);
            return true;
        }
        else if (GuiScreen.isKeyComboCtrlC(keyCode))
        {
            GuiScreen.setClipboardString(this.getSelectedText());
            return true;
        }
        else if (GuiScreen.isKeyComboCtrlV(keyCode))
        {
            if (this.isEnabled)
            {
                this.writeText(GuiScreen.getClipboardString());
            }

            return true;
        }
        else if (GuiScreen.isKeyComboCtrlX(keyCode))
        {
            GuiScreen.setClipboardString(this.getSelectedText());

            if (this.isEnabled)
            {
                this.writeText("");
            }

            return true;
        }
        else
        {
            switch (keyCode)
            {
                case 14:
                    if (GuiScreen.isCtrlKeyDown())
                    {
                        if (this.isEnabled)
                        {
                            this.deleteWords(-1);
                        }
                    }
                    else if (this.isEnabled)
                    {
                        this.deleteFromCursor(-1);
                    }
                    return true;
                    
                case 199:
                    if (GuiScreen.isShiftKeyDown())
                    {
                        this.setSelectionPos(0);
                    }
                    else
                    {
                        this.setCursorPositionZero();
                    }
                    return true;
                    
                case 203:
                    if (GuiScreen.isShiftKeyDown())
                    {
                        if (GuiScreen.isCtrlKeyDown())
                        {
                            this.setSelectionPos(this.getNthWordFromPos(-1, this.getSelectionEnd()));
                        }
                        else
                        {
                            this.setSelectionPos(this.getSelectionEnd() - 1);
                        }
                    }
                    else if (GuiScreen.isCtrlKeyDown())
                    {
                        this.setCursorPosition(this.getNthWordFromCursor(-1));
                    }
                    else
                    {
                        this.moveCursorBy(-1);
                    }
                    return true;
                    
                case 205:
                    if (GuiScreen.isShiftKeyDown())
                    {
                        if (GuiScreen.isCtrlKeyDown())
                        {
                            this.setSelectionPos(this.getNthWordFromPos(1, this.getSelectionEnd()));
                        }
                        else
                        {
                            this.setSelectionPos(this.getSelectionEnd() + 1);
                        }
                    }
                    else if (GuiScreen.isCtrlKeyDown())
                    {
                        this.setCursorPosition(this.getNthWordFromCursor(1));
                    }
                    else
                    {
                        this.moveCursorBy(1);
                    }
                    return true;
                    
                case 207:
                    if (GuiScreen.isShiftKeyDown())
                    {
                        this.setSelectionPos(this.text.length());
                    }
                    else
                    {
                        this.setCursorPositionEnd();
                    }
                    return true;
                    
                case 211:
                    if (GuiScreen.isCtrlKeyDown())
                    {
                        if (this.isEnabled)
                        {
                            this.deleteWords(1);
                        }
                    }
                    else if (this.isEnabled)
                    {
                        this.deleteFromCursor(1);
                    }
                    return true;
                    
                default:
                    if (ChatAllowedCharacters.isAllowedCharacter(character))
                    {
                        if (this.isEnabled)
                        {
                            this.writeText(Character.toString(character));
                        }

                        return true;
                    }
                    else
                    {
                        return false;
                    }
            }
        }
    }
    
    private void drawSelection(int x, int y, int dx, int dy)
    {
        if (x < dx)
        {
            int i = x;
            x = dx;
            dx = i;
        }

        if (y < dy)
        {
            int j = y;
            y = dy;
            dy = j;
        }

        if (dx > this.xPosition + this.width)
        {
            dx = this.xPosition + this.width;
        }

        if (x > this.xPosition + this.width)
        {
            x = this.xPosition + this.width;
        }

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.enableColorLogic();
        GlStateManager.colorLogicOp(5387);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos((double)x, (double)dy, 0.0D).endVertex();
        worldrenderer.pos((double)dx, (double)dy, 0.0D).endVertex();
        worldrenderer.pos((double)dx, (double)y, 0.0D).endVertex();
        worldrenderer.pos((double)x, (double)y, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
    }

    public void setMaxStringLength(int p_146203_1_)
    {
        this.maxStringLength = p_146203_1_;

        if (this.text.length() > p_146203_1_)
        {
            this.text = this.text.substring(0, p_146203_1_);
            updateFormattedText();
        }
    }
    
    public int getMaxStringLength()
    {
        return this.maxStringLength;
    }
    
    public int getCursorPosition()
    {
        return this.cursorPosition;
    }
    
    public void setFocused(boolean focused)
    {
        if (focused && !this.isFocused)
        {
            this.cursorBlinkCounter = 0;
        }

        this.isFocused = focused;
    }
    
    public boolean isFocused()
    {
        return this.isFocused;
    }
    
    public int getSelectionEnd()
    {
        return this.selectionEnd;
    }
    
    public int getInnerWidth()
    {
        return this.width - 8;
    }
    
    public void setSelectionPos(int selectionIndex)
    {
        int textLength = this.text.length();
        
        if (selectionIndex > textLength) selectionIndex = textLength;
        if (selectionIndex < 0) selectionIndex = 0;

        this.selectionEnd = selectionIndex;

        if (this.fontRenderer != null)
        {
            if (this.lineScrollOffset > textLength)
            {
                this.lineScrollOffset = textLength;
            }
            
            String formattedText = getFormattableText();
            int width = this.getInnerWidth();
            if (selectionIndex == this.lineScrollOffset)
            {
                this.lineScrollOffset -= TextUtil.trimStringToWidth(formattedText, width, true).length();
            }
            else
            {
                int textWidthToOffset = getFormattableStringWidthAtIndex(this.lineScrollOffset);
                int textWidthToFieldEnd = textWidthToOffset + width;
                int indexAtFieldEnd = getFormattableStringIndexAtWidth(textWidthToFieldEnd);
                
                if (selectionIndex > indexAtFieldEnd)
                {
                    int textWidthToSelection = getFormattableStringWidthAtIndex(selectionIndex);
                    int scrollByWidth = textWidthToSelection - textWidthToFieldEnd;
                    int scrollToTextWidth = textWidthToOffset + scrollByWidth;
                    
                    this.lineScrollOffset = getFormattableStringIndexAtWidth(scrollToTextWidth) + 1;
                }
                else if (selectionIndex < this.lineScrollOffset)
                {
                    this.lineScrollOffset = selectionIndex;
                }
            }

            this.lineScrollOffset = MathHelper.clamp_int(this.lineScrollOffset, 0, textLength);
        }
    }
    
    public boolean getVisible()
    {
        return this.visible;
    }
    
    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }
}
