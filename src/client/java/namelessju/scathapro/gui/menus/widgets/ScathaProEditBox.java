package namelessju.scathapro.gui.menus.widgets;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import namelessju.scathapro.util.TextUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.PreeditEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Modified version of the vanilla EditBox to add support for formatting code highlighting
 * and improving some things that I dislike about that implementation
 */
@NullMarked
public class ScathaProEditBox extends AbstractWidget
{
    private static final WidgetSprites SPRITES = new WidgetSprites(
        Identifier.withDefaultNamespace("widget/text_field"), Identifier.withDefaultNamespace("widget/text_field_highlighted")
    );
    private final Font font;
    private String rawValue = "";
    private int maxLength = 32;
    private boolean bordered = true;
    private boolean canLoseFocus = true;
    private boolean isEditable = true;
    private boolean centered = false;
    private boolean textShadow = true;
    private boolean invertHighlightedTextColor = true;
    private int displayPos;
    private int cursorPos;
    private int highlightPos;
    private int textColor = 0xffe0e0e0;
    private int textColorUneditable = 0xff707070;
    private @Nullable String suggestion;
    private @Nullable Consumer<String> responder;
    private @Nullable Component hint;
    private @Nullable IMEPreeditOverlay preeditOverlay;
    private long focusedTime = Util.getMillis();
    private int textX;
    private int textY;

    private Style baseStyle = Style.EMPTY;
    private boolean previewFormattingSequences = false;
    private Component formattedValue = Component.empty();

    public ScathaProEditBox(final Font font, final Component narration)
    {
        this(font, 150, 20, narration);
    }

    public ScathaProEditBox(final Font font, final int width, final int height, final Component narration)
    {
        this(font, 0, 0, width, height, narration);
    }

    public ScathaProEditBox(final Font font, final int x, final int y, final int width, final int height, final Component narration)
    {
        this(font, x, y, width, height, null, narration);
    }

    public ScathaProEditBox(final Font font, final int x, final int y, final int width, final int height, final @Nullable EditBox oldBox, final Component narration)
    {
        super(x, y, width, height, narration);
        this.font = font;
        if (oldBox != null)
        {
            this.setValue(oldBox.getValue());
        }

        this.updateTextPosition();
    }

    public void setResponder(final @Nullable Consumer<String> responder)
    {
        this.responder = responder;
    }

    @Override
    protected MutableComponent createNarrationMessage()
    {
        Component message = this.getMessage();
        return Component.translatable("gui.narrate.editBox", message, this.rawValue);
    }

    public void setValue(final String value)
    {
        if (value.length() > this.maxLength)
        {
            this.rawValue = value.substring(0, this.maxLength);
        }
        else this.rawValue = value;

        this.onValueChange();
        this.moveCursorToEnd(false);
        this.setHighlightPos(this.cursorPos);
    }

    public String getValue()
    {
        return this.rawValue;
    }

    public String getHighlighted()
    {
        int start = Math.min(this.cursorPos, this.highlightPos);
        int end = Math.max(this.cursorPos, this.highlightPos);
        return this.rawValue.substring(start, end);
    }

    @Override
    public void setX(final int x)
    {
        super.setX(x);
        this.updateTextPosition();
    }

    @Override
    public void setY(final int y)
    {
        super.setY(y);
        this.updateTextPosition();
    }

    public void insertText(final String input)
    {
        int start = Math.min(this.cursorPos, this.highlightPos);
        int end = Math.max(this.cursorPos, this.highlightPos);
        int maxInsertionLength = this.maxLength - this.rawValue.length() - (start - end);
        if (maxInsertionLength > 0)
        {
            String text = StringUtil.filterText(input);
            int insertionLength = text.length();
            if (maxInsertionLength < insertionLength)
            {
                if (Character.isHighSurrogate(text.charAt(maxInsertionLength - 1)))
                {
                    maxInsertionLength--;
                }

                text = text.substring(0, maxInsertionLength);
                insertionLength = maxInsertionLength;
            }

            this.rawValue = new StringBuilder(this.rawValue).replace(start, end, text).toString();
            this.onValueChange();
            this.setCursorPosition(start + insertionLength);
            this.setHighlightPos(this.cursorPos);
        }
    }

    private void onValueChange()
    {
        if (this.responder != null) this.responder.accept(rawValue);
        this.updateTextPosition();
        updateFormattedValue();
    }

    private void deleteText(final int dir, final boolean wholeWord)
    {
        if (wholeWord)
        {
            this.deleteWords(dir);
        }
        else
        {
            this.deleteChars(dir);
        }
    }

    public void deleteWords(final int dir)
    {
        if (!this.rawValue.isEmpty())
        {
            if (this.highlightPos != this.cursorPos)
            {
                this.insertText("");
            }
            else
            {
                this.deleteCharsToPos(this.getWordPosition(dir));
            }
        }
    }

    public void deleteChars(final int dir)
    {
        this.deleteCharsToPos(this.getCursorPos(dir));
    }

    public void deleteCharsToPos(final int pos)
    {
        if (!this.rawValue.isEmpty())
        {
            if (this.highlightPos != this.cursorPos)
            {
                this.insertText("");
            }
            else
            {
                int start = Math.min(pos, this.cursorPos);
                int end = Math.max(pos, this.cursorPos);
                if (start != end)
                {
                    this.rawValue = new StringBuilder(this.rawValue).delete(start, end).toString();
                    this.onValueChange();
                    this.setCursorPosition(start);
                    this.moveCursorTo(start, false);
                }
            }
        }
    }

    public int getWordPosition(final int dir)
    {
        return this.getWordPosition(dir, this.getCursorPosition());
    }

    private int getWordPosition(final int dir, final int from)
    {
        int result = from;
        boolean reverse = dir < 0;
        int abs = Math.abs(dir);

        for (int i = 0; i < abs; i++)
        {
            if (!reverse)
            {
                int length = this.rawValue.length();
                result = this.rawValue.indexOf(' ', result);
                if (result == -1)
                {
                    result = length;
                }
                else
                {
                    while (result < length && this.rawValue.charAt(result) == ' ')
                    {
                        result++;
                    }
                }
            }
            else
            {
                while (result > 0 && this.rawValue.charAt(result - 1) == ' ')
                {
                    result--;
                }

                while (result > 0 && this.rawValue.charAt(result - 1) != ' ')
                {
                    result--;
                }
            }
        }

        return result;
    }

    public void moveCursor(final int dir, final boolean hasShiftDown)
    {
        this.moveCursorTo(this.getCursorPos(dir), hasShiftDown);
    }

    private int getCursorPos(final int dir)
    {
        return Util.offsetByCodepoints(this.rawValue, this.cursorPos, dir);
    }

    public void moveCursorTo(final int dir, final boolean extendSelection)
    {
        this.setCursorPosition(dir);
        if (!extendSelection)
        {
            this.setHighlightPos(this.cursorPos);
        }

        this.updateTextPosition();
    }

    public void setCursorPosition(final int pos)
    {
        this.cursorPos = Mth.clamp(pos, 0, this.rawValue.length());
        this.scrollTo(this.cursorPos);
    }

    public void moveCursorToStart(final boolean hasShiftDown)
    {
        this.moveCursorTo(0, hasShiftDown);
    }

    public void moveCursorToEnd(final boolean hasShiftDown)
    {
        this.moveCursorTo(this.rawValue.length(), hasShiftDown);
    }

    @Override
    public boolean keyPressed(final KeyEvent event)
    {
        if (this.isActive() && this.isFocused())
        {
            switch (event.key())
            {
                case 259:
                    if (this.isEditable)
                    {
                        this.deleteText(-1, event.hasControlDownWithQuirk());
                    }

                    return true;
                case 260:
                case 264:
                case 265:
                case 266:
                case 267:
                case 261:
                    if (this.isEditable)
                    {
                        this.deleteText(1, event.hasControlDownWithQuirk());
                    }

                    return true;
                case 262:
                    if (event.hasControlDownWithQuirk())
                    {
                        this.moveCursorTo(this.getWordPosition(1), event.hasShiftDown());
                    }
                    else
                    {
                        this.moveCursor(1, event.hasShiftDown());
                    }

                    return true;
                case 263:
                    if (event.hasControlDownWithQuirk())
                    {
                        this.moveCursorTo(this.getWordPosition(-1), event.hasShiftDown());
                    }
                    else
                    {
                        this.moveCursor(-1, event.hasShiftDown());
                    }

                    return true;
                case 268:
                    this.moveCursorToStart(event.hasShiftDown());
                    return true;
                case 269:
                    this.moveCursorToEnd(event.hasShiftDown());
                    return true;
                default:
                    if (event.isSelectAll())
                    {
                        this.moveCursorToEnd(false);
                        this.setHighlightPos(0);
                        return true;
                    }
                    else if (event.isCopy())
                    {
                        Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                        return true;
                    }
                    else if (event.isPaste())
                    {
                        if (this.isEditable())
                        {
                            this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
                        }

                        return true;
                    }
                    else
                    {
                        if (event.isCut())
                        {
                            Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                            if (this.isEditable())
                            {
                                this.insertText("");
                            }

                            return true;
                        }

                        return false;
                    }
            }
        }
        else return false;
    }

    public boolean canConsumeInput()
    {
        return this.isActive() && this.isFocused() && this.isEditable();
    }

    @Override
    public boolean charTyped(final CharacterEvent event)
    {
        if (!this.canConsumeInput())
        {
            return false;
        }

        if (event.isAllowedChatCharacter())
        {
            if (this.isEditable)
            {
                this.insertText(event.codepointAsString());
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean preeditUpdated(final @Nullable PreeditEvent event)
    {
        this.preeditOverlay = event != null ? new IMEPreeditOverlay(event, this.font, 9 + 1) : null;
        return true;
    }

    private int findClickedPositionInText(final MouseButtonEvent event)
    {
        int positionInText = Math.min(Mth.floor(event.x()) - this.textX, this.getInnerWidth());
        FormattedText displayed = TextUtil.subStringFormatted(formattedValue, this.displayPos);
        return this.displayPos + this.font.substrByWidth(displayed, positionInText).getString().length();
    }

    private void selectWord(final MouseButtonEvent event)
    {
        int clickedPosition = this.findClickedPositionInText(event);
        int wordStart = this.getWordPosition(-1, clickedPosition);
        int wordEnd = this.getWordPosition(1, clickedPosition);
        this.moveCursorTo(wordStart, false);
        this.moveCursorTo(wordEnd, true);
    }

    @Override
    public void onClick(final MouseButtonEvent event, final boolean doubleClick)
    {
        if (doubleClick) this.selectWord(event);
        else this.moveCursorTo(this.findClickedPositionInText(event), event.hasShiftDown());
    }

    @Override
    protected void onDrag(final MouseButtonEvent event, final double dx, final double dy)
    {
        this.moveCursorTo(this.findClickedPositionInText(event), true);
    }

    @Override
    public void playDownSound(final SoundManager soundManager) {}

    @Override
    public void extractWidgetRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a)
    {
        if (this.isVisible())
        {
            if (this.isBordered())
            {
                Identifier sprite = SPRITES.get(this.isActive(), this.isFocused());
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, this.getX(), this.getY(), this.getWidth(), this.getHeight());
            }

            int color = this.isEditable ? this.textColor : this.textColorUneditable;
            int relCursorPos = this.cursorPos - this.displayPos;
            int visibleLength = formattedValue.getString().length();
            boolean visibleIsEmpty = visibleLength == 0;
            FormattedText displayed = this.font.substrByWidth(TextUtil.subStringFormatted(formattedValue, this.displayPos), this.getInnerWidth());
            boolean cursorOnScreen = relCursorPos >= 0 && relCursorPos <= visibleLength;
            boolean showCursor = this.isFocused() && TextCursorUtils.isCursorVisible(Util.getMillis() - this.focusedTime) && cursorOnScreen;
            int drawX = this.textX;
            int relHighlightPos = Mth.clamp(this.highlightPos - this.displayPos, 0, visibleLength);
            if (!visibleIsEmpty)
            {
                FormattedText half = cursorOnScreen ? TextUtil.subStringFormatted(displayed, 0, relCursorPos) : displayed;
                graphics.text(this.font, Language.getInstance().getVisualOrder(half), drawX, this.textY, color, this.textShadow);
                drawX += this.font.width(half) + 1;
            }

            boolean insert = this.cursorPos < this.rawValue.length() || this.rawValue.length() >= this.getMaxLength();
            int cursorX = drawX;
            if (!cursorOnScreen)
            {
                cursorX = relCursorPos > 0 ? this.textX + this.width : this.textX;
            }
            else if (insert)
            {
                cursorX--;
                drawX--;
            }

            if (!visibleIsEmpty && cursorOnScreen && relCursorPos < visibleLength)
            {
                FormattedText half = TextUtil.subStringFormatted(displayed, relCursorPos);
                graphics.text(this.font, Language.getInstance().getVisualOrder(half), drawX, this.textY, color, this.textShadow);
            }

            if (this.hint != null && visibleIsEmpty)
            {
                graphics.text(this.font, this.hint, drawX, this.textY, color);
            }

            if (!insert && this.suggestion != null)
            {
                graphics.text(this.font, this.suggestion, cursorX - 1, this.textY, -8355712, this.textShadow);
            }

            if (relHighlightPos != relCursorPos)
            {
                int highlightX = this.textX + this.font.width(TextUtil.subStringFormatted(displayed, 0, relHighlightPos));
                graphics.textHighlight(
                    Math.min(cursorX, this.getX() + this.width),
                    this.textY - 1,
                    Math.min(highlightX - 1, this.getX() + this.width),
                    this.textY + 1 + 9,
                    this.invertHighlightedTextColor
                );
            }

            if (showCursor)
            {
                if (insert)
                {
                    TextCursorUtils.extractInsertCursor(graphics, cursorX, this.textY, color, 9 + 1);
                }
                else
                {
                    TextCursorUtils.extractAppendCursor(graphics, this.font, cursorX, this.textY, color, this.textShadow);
                }
            }

            if (this.isHovered())
            {
                graphics.requestCursor(this.isEditable() ? CursorTypes.IBEAM : CursorTypes.NOT_ALLOWED);
            }

            if (this.preeditOverlay != null)
            {
                this.preeditOverlay.updateInputPosition(cursorX, this.textY);
                graphics.setPreeditOverlay(this.preeditOverlay);
            }
        }
    }

    private void updateTextPosition()
    {
        FormattedText displayed = this.font.substrByWidth(TextUtil.subStringFormatted(formattedValue, this.displayPos), this.getInnerWidth());
        this.textX = this.getX() + (this.isCentered() ? (this.getWidth() - this.font.width(displayed)) / 2 : (this.bordered ? 4 : 0));
        this.textY = this.bordered ? this.getY() + (this.height - 8) / 2 : this.getY();
    }

    public void setMaxLength(final int maxLength)
    {
        this.maxLength = maxLength;
        if (this.rawValue.length() > maxLength)
        {
            this.rawValue = this.rawValue.substring(0, maxLength);
            this.onValueChange();
        }
    }

    private int getMaxLength()
    {
        return this.maxLength;
    }

    public int getCursorPosition()
    {
        return this.cursorPos;
    }

    public boolean isBordered()
    {
        return this.bordered;
    }

    public void setBordered(final boolean bordered)
    {
        this.bordered = bordered;
        this.updateTextPosition();
    }

    public void setTextColor(final int textColor)
    {
        this.textColor = textColor;
    }

    public void setTextColorUneditable(final int textColorUneditable)
    {
        this.textColorUneditable = textColorUneditable;
    }

    @Override
    public void setFocused(final boolean focused)
    {
        if (this.canLoseFocus || focused)
        {
            super.setFocused(focused);
            if (focused)
            {
                this.focusedTime = Util.getMillis();
            }

            if (this.isEditable())
            {
                Minecraft.getInstance().onTextInputFocusChange(this, focused);
            }
        }
    }

    private boolean isEditable()
    {
        return this.isEditable;
    }

    public void setEditable(final boolean isEditable)
    {
        if (this.isFocused())
        {
            Minecraft.getInstance().onTextInputFocusChange(this, isEditable);
        }

        this.isEditable = isEditable;
    }

    private boolean isCentered()
    {
        return this.centered;
    }

    public void setCentered(final boolean centered)
    {
        this.centered = centered;
        this.updateTextPosition();
    }

    public void setTextShadow(final boolean textShadow)
    {
        this.textShadow = textShadow;
    }

    public void setInvertHighlightedTextColor(final boolean invertHighlightedTextColor)
    {
        this.invertHighlightedTextColor = invertHighlightedTextColor;
    }

    public int getInnerWidth()
    {
        return this.isBordered() ? this.width - 8 : this.width;
    }

    public void setHighlightPos(final int pos)
    {
        this.highlightPos = Mth.clamp(pos, 0, this.rawValue.length());
        this.scrollTo(this.highlightPos);
    }

    private void scrollTo(final int pos)
    {
        this.displayPos = Math.min(this.displayPos, this.rawValue.length());
        int innerWidth = this.getInnerWidth();
        FormattedText displayed = this.font.substrByWidth(TextUtil.subStringFormatted(formattedValue, displayPos), innerWidth);
        int lastPos = displayed.getString().length() + this.displayPos;
        if (pos == this.displayPos)
        {
            // replacement for font.plainSubstrByWidth() with reverse=true
            List<StyledString> styledSegments = new ArrayList<>();
            formattedValue.visit((FormattedText.StyledContentConsumer<Void>) (style, contents) -> {
                styledSegments.add(new StyledString(contents, style));
                return Optional.empty();
            }, Style.EMPTY);
            int widthSum = 0;
            int charCount = 0;
            for (StyledString segment : styledSegments.reversed())
            {
                for (int i = segment.string().length() - 1; i >= 0; )
                {
                    int unitLength = i > 0 && Character.isLowSurrogate(segment.string().charAt(i))
                        && Character.isHighSurrogate(segment.string().charAt(i - 1))
                        ? 2 : 1;
                    String unit = segment.string().substring(i - unitLength + 1, i + 1);

                    widthSum += font.width(Component.literal(unit).setStyle(segment.style()));
                    if (widthSum > innerWidth)
                    {
                        widthSum = -1;
                        break;
                    }
                    charCount += unitLength;

                    i -= unitLength;
                }
                if (widthSum < 0) break;
            }
            this.displayPos = this.displayPos - charCount;
        }

        if (pos > lastPos)
        {
            this.displayPos += pos - lastPos;
        }
        else if (pos <= this.displayPos)
        {
            this.displayPos = this.displayPos - (this.displayPos - pos);
        }

        this.displayPos = Mth.clamp(this.displayPos, 0, this.rawValue.length());
    }

    public void setCanLoseFocus(final boolean canLoseFocus)
    {
        this.canLoseFocus = canLoseFocus;
    }

    public boolean isVisible()
    {
        return this.visible;
    }

    public void setVisible(final boolean visible)
    {
        this.visible = visible;
    }

    public void setSuggestion(final @Nullable String suggestion)
    {
        this.suggestion = suggestion;
    }

    public int getScreenX(final int charIndex)
    {
        return charIndex > this.rawValue.length() ? this.getX() : this.getX() + this.font.width(this.rawValue.substring(0, charIndex));
    }

    @Override
    public void updateWidgetNarration(final NarrationElementOutput output)
    {
        output.add(NarratedElementType.TITLE, this.createNarrationMessage());
    }

    public void setHint(final Component hint)
    {
        boolean hasNoStyle = hint.getStyle().equals(Style.EMPTY);
        this.hint = hasNoStyle ? hint.copy().withStyle(EditBox.DEFAULT_HINT_STYLE) : hint;
    }


    public void setBaseStyle(Style style)
    {
        this.baseStyle = style;
        updateFormattedValue();
    }

    public void setPreviewFormattingSequences(boolean value)
    {
        this.previewFormattingSequences = value;
        updateFormattedValue();
    }

    private void updateFormattedValue()
    {
        if (!previewFormattingSequences)
        {
            formattedValue = Component.literal(rawValue).setStyle(baseStyle);
            return;
        }

        formattedValue = Component.empty().setStyle(baseStyle)
            .append(TextUtil.userFormattedStringToComponent(rawValue, true));
    }


    private record StyledString(String string, Style style) {}
}