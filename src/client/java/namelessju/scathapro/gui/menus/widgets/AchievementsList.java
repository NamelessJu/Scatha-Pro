package namelessju.scathapro.gui.menus.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.achievements.Achievement;
import namelessju.scathapro.achievements.AchievementCategory;
import namelessju.scathapro.achievements.AchievementType;
import namelessju.scathapro.achievements.UnlockedAchievement;
import namelessju.scathapro.files.Config;
import namelessju.scathapro.managers.AchievementManager;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.TimeUtil;
import namelessju.scathapro.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.math.RoundingMode;
import java.util.*;

public class AchievementsList extends AbstractWidget
{
    private static final int CONTENT_WIDTH = 310;
    private static final int SCROLL_DISTANCE = 30;
    private static final int TEXT_SCROLL_SPEED = 40; // in pixels per second
    private static final int TEXT_SCROLL_STAY_DURATION = 70; // in pixels scrolled (affected by scroll speed)
    
    private static final Identifier progressBarIdentifier = ScathaPro.getIdentifier("textures/screen/achievements/progress_bar.png");
    private static final Identifier caretsIdentifier = ScathaPro.getIdentifier("textures/screen/achievements/dropdown_carets.png");
    
    private static final AchievementType[] extraAchievementTypes = {
        AchievementType.SECRET, AchievementType.BONUS, AchievementType.HIDDEN, AchievementType.LEGACY
    };
    
    
    private int contentHeight = 0;
    
    private final ScathaPro scathaPro;
    private final Font font;
    
    private AchievementListEntry[] listEntries;
    private Component unlockedAchievementsComponent;
    private AchievementListEntry hoveredEntry = null;
    private boolean scrollBarHovered = false;
    private boolean scrollBarRailHovered = false;
    private float scroll = 0;
    private DragMode dragMode = null;
    private int dragStartY = -1;
    private float dragStartScrolledY = -1f;
    
    private boolean repeatCountShown = false;
    
    
    public AchievementsList(int x, int y, int width, int height, ScathaPro scathaPro)
    {
        super(x, y, width, height, CommonComponents.EMPTY);
        
        this.scathaPro = scathaPro;
        this.font = scathaPro.minecraft.font;
        
        init();
    }
    
    private void init()
    {
        Config config = scathaPro.config;
        repeatCountShown = config.achievements.listShowRepeatCounts.get();
        
        Achievement[] achievementList = AchievementManager.getAllAchievements();
        ArrayList<Achievement> visibleAchievements = new ArrayList<>();
        
        HashMap<AchievementType, Integer> achievementCount = new HashMap<>();
        HashMap<AchievementType, Integer> unlockedAchievementCount = new HashMap<>();
        
        for (Achievement achievement : achievementList)
        {
            boolean isUnlocked = scathaPro.getProfileData().unlockedAchievements.isUnlocked(achievement);
            
            if (
                (isTypeVisibleWhenLocked(achievement.type) || isUnlocked)
                    && (!isUnlocked || !config.achievements.listHideUnlockedAchievements.get())
            ) {
                visibleAchievements.add(achievement);
            }
            
            achievementCount.compute(achievement.type, (type, currentTypeCount)
                -> (currentTypeCount != null ? currentTypeCount : 0) + 1
            );
            
            if (isUnlocked)
            {
                unlockedAchievementCount.compute(achievement.type, (type, currentTypeUnlockedCount)
                    -> (currentTypeUnlockedCount != null ? currentTypeUnlockedCount : 0) + 1
                );
            }
        }
        
        ChatFormatting contrastableGray = TextUtil.contrastableGray(scathaPro);
        
        MutableComponent extraAchievementsComponent = Component.empty();
        for (int i = 0; i < extraAchievementTypes.length; i ++)
        {
            AchievementType achievementType = extraAchievementTypes[i];
            boolean isVisibleWhenLocked = isTypeVisibleWhenLocked(achievementType);
            
            int unlockedCount = Objects.requireNonNullElse(unlockedAchievementCount.get(achievementType), 0);
            
            if (isVisibleWhenLocked || unlockedCount > 0)
            {
                if (i > 0) extraAchievementsComponent.append(Component.literal(", ").withStyle(contrastableGray));
                
                MutableComponent unlockCountComponent = Component.empty();
                if (achievementType.style != null) unlockCountComponent.setStyle(achievementType.style);
                unlockCountComponent.append(String.valueOf(unlockedCount));
                if (isVisibleWhenLocked)
                {
                    int totalCount = Objects.requireNonNullElse(achievementCount.get(achievementType), 0);
                    unlockCountComponent.append("/" + totalCount);
                }
                unlockCountComponent.append(" " + achievementType.typeName);
                extraAchievementsComponent.append(unlockCountComponent);
            }
        }
        
        unlockedAchievementsComponent = Component.empty()
            .append(Component.literal(
                "Unlocked: "
                    + Objects.requireNonNullElse(unlockedAchievementCount.get(AchievementType.NORMAL), 0)
                    + "/" + Objects.requireNonNullElse(achievementCount.get(AchievementType.NORMAL), 0)
            ).withStyle(ChatFormatting.GREEN
            ))
            .append(Component.literal(", ").withStyle(contrastableGray))
            .append(extraAchievementsComponent);
        
        // Create entries
        
        HashMap<AchievementCategory, ArrayList<Achievement>> achievementsPerCategory = new HashMap<>();
        for (Achievement achievement : visibleAchievements)
        {
            // Note: category may be null, but this is intended behaviour and a supported key for HashMaps
            if (!achievementsPerCategory.containsKey(achievement.category))
            {
                achievementsPerCategory.put(achievement.category, Lists.newArrayList(achievement));
            }
            else achievementsPerCategory.get(achievement.category).add(achievement);
        }
        
        List<AchievementListEntry> entries = Lists.newArrayList();
        
        boolean categoriesCollapsed = !scathaPro.config.achievements.listPreOpenCategories.get();
        AchievementCategory[] allCategories = AchievementCategory.values();
        for (int i = 0; i < allCategories.length + 1; i ++)
        {
            AchievementCategory category = i < allCategories.length ? allCategories[i] : null;
            ArrayList<Achievement> categoryAchievements = achievementsPerCategory.get(category);
            if (categoryAchievements == null) continue;
            
            AchievementListEntry[] achievementCards = new AchievementListEntry[categoryAchievements.size()];
            for (int j = 0; j < categoryAchievements.size(); j ++)
            {
                achievementCards[j] = new AchievementCard(categoryAchievements.get(j));
            }
            
            entries.add(new ListTitle(category != null ? category.categoryName : "Uncategorized", achievementCards, categoriesCollapsed));
            Collections.addAll(entries, achievementCards);
        }
        
        listEntries = entries.toArray(new AchievementListEntry[0]);
        updateContentHeight();
    }
    
    private int getContentX()
    {
        return getX() + width/2 - CONTENT_WIDTH/2;
    }
    
    private boolean isTypeVisibleWhenLocked(AchievementType type)
    {
        return type.lockedVisibility != AchievementType.LockedVisibility.HIDDEN
            || type == AchievementType.BONUS && scathaPro.config.achievements.listShowBonusAchievements.get();
    }
    
    @Override
    protected void updateWidgetNarration(@NonNull NarrationElementOutput narrationElementOutput) {}
    
    @Override
    protected void renderWidget(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        int contentX = getContentX();
        
        this.hoveredEntry = null;
        if (listEntries.length > 0)
        {
            guiGraphics.scissorStack.push(getRectangle());
            for (AchievementListEntry listEntry : listEntries)
            {
                listEntry.render(guiGraphics, mouseX, mouseY, partialTicks);
                
                if (this.hoveredEntry == null && this.isHovered && listEntry.isHovered)
                {
                    this.hoveredEntry = listEntry;
                }
            }
            guiGraphics.scissorStack.pop();
        }
        else
        {
            String emptyListText;
            if (scathaPro.config.achievements.listHideUnlockedAchievements.get())
            {
                emptyListText = "You've unlocked all non-hidden achievements! \\o/";
            }
            else emptyListText = "No achievements found";
            
            guiGraphics.drawString(font, emptyListText,
                contentX + CONTENT_WIDTH/2 - font.width(emptyListText)/2,
                getY() + height/2 - Mth.ceil(font.lineHeight*0.5f),
                Util.Color.GRAY, true
            );
        }
        
        int scrollBarWidth = 2;
        int scrollBarHeight = getScrollBarHeight();
        int scrollBarX = contentX + CONTENT_WIDTH + 2;
        int scrollBarY = getY() + getScrollBarRelativeY();
        if (contentHeight > height)
        {
            guiGraphics.fill(scrollBarX, getY(), scrollBarX + scrollBarWidth, getY() + height, 0x70000000);
            guiGraphics.fill(scrollBarX, scrollBarY, scrollBarX + scrollBarWidth, scrollBarY + scrollBarHeight, 0xFFFFFFFF);
        }
        
        this.scrollBarRailHovered = mouseX >= scrollBarX - 2 && mouseY >= getY()
            && mouseX < scrollBarX + scrollBarWidth + 2&& mouseY < getY() + height;
        this.scrollBarHovered = mouseX >= scrollBarX - 2 && mouseY >= scrollBarY
            && mouseX < scrollBarX + scrollBarWidth + 2 && mouseY < scrollBarY + scrollBarHeight;
        
        
        guiGraphics.drawString(font, unlockedAchievementsComponent,
            contentX + CONTENT_WIDTH/2 - font.width(unlockedAchievementsComponent)/2,
            getY() - 13,
            Util.Color.WHITE, true
        );
        
        
        if (dragMode == DragMode.SCROLLBAR || dragMode == DragMode.LIST) guiGraphics.requestCursor(CursorTypes.RESIZE_NS);
        else if (scrollBarRailHovered) guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
    }
    
    private void updateContentHeight()
    {
        contentHeight = 0;
        for (AchievementListEntry entry : this.listEntries)
        {
            if (!entry.isVisible) continue;
            if (contentHeight > 0) contentHeight += AchievementListEntry.spacing;
            entry.setUnscrolledRelativeY(contentHeight);
            contentHeight += entry.entryHeight;
        }
        
        updateScroll();
    }
    
    @Override
    public void setX(int x)
    {
        super.setX(x);
        for (AchievementListEntry entry : listEntries)
        {
            entry.updateX();
        }
    }
    
    @Override
    public void setY(int y)
    {
        super.setY(y);
        for (AchievementListEntry entry : listEntries)
        {
            entry.updateY();
        }
    }
    
    @Override
    public void playDownSound(@NonNull SoundManager soundManager)
    {
        // don't play default click sound when widget is clicked anywhere
    }
    
    @Override
    public void onClick(@NonNull MouseButtonEvent mouseButtonEvent, boolean bl)
    {
        float mouseY = (float) mouseButtonEvent.y();
        if (scrollBarHovered)
        {
            dragStartScrolledY = mouseY - (getY() + getScrollBarRelativeY() + getScrollBarHeight() * 0.5f);
            dragMode = DragMode.SCROLLBAR;
        }
        else if (scrollBarRailHovered)
        {
            dragStartScrolledY = 0;
            dragMode = DragMode.SCROLLBAR;
        }
        else if (isHovered)
        {
            dragStartY = Math.round(mouseY);
            dragStartScrolledY = mouseY + scroll;
            dragMode = DragMode.CLICK_WAITING;
        }
    }
    
    @Override
    protected void onDrag(@NonNull MouseButtonEvent mouseButtonEvent, double d, double e)
    {
        float mouseY = (float) mouseButtonEvent.y();
        switch (dragMode)
        {
            case SCROLLBAR:
                int scrollBarHeight = getScrollBarHeight();
                float mouseYRelative = (mouseY - dragStartScrolledY) - (getY() + scrollBarHeight * 0.5f);
                float mouseProgress = height != scrollBarHeight ? mouseYRelative / (height - scrollBarHeight) : 0;
                setScroll(getScrollHeight() * mouseProgress);
                break;
            
            case CLICK_WAITING:
                if (Math.abs(mouseY - dragStartY) >= 5)
                {
                    dragMode = DragMode.LIST;
                }
                break;
            
            case LIST:
                setScroll(dragStartScrolledY - mouseY);
                break;
            
            case null, default: // nothing
        }
    }
    
    @Override
    public void onRelease(@NonNull MouseButtonEvent mouseButtonEvent)
    {
        if (dragMode == DragMode.CLICK_WAITING && this.hoveredEntry != null)
        {
            this.hoveredEntry.click();
        }
        
        dragMode = null;
    }
    
    @Override
	public boolean mouseScrolled(double d, double e, double scrollX, double scrollY)
    {
        if (scrollY != 0) setScroll(getScroll() - Mth.sign(scrollY) * SCROLL_DISTANCE);
        return true;
    }
    
    public void setScroll(float scroll)
    {
        if (scroll < 0) scroll = 0;
        else if (scroll > contentHeight - height)
        {
            scroll = contentHeight > height ? contentHeight - height : 0;
        }
        this.scroll = scroll;
    }
    
    public float getScroll()
    {
        return scroll;
    }
    
    public void updateScroll()
    {
        setScroll(getScroll());
    }
    
    public int getScrollHeight()
    {
        return Math.max(contentHeight - height, 0);
    }
    
    public int getScrollBarHeight()
    {
        return Math.round(Math.max(Math.min((float) height / contentHeight, 1f), 0.1f) * height);
    }
    
    public int getScrollBarRelativeY()
    {
        return Math.round((height - getScrollBarHeight()) * (scroll / getScrollHeight()));
    }
    
    public void added()
    {
        setScroll(0f);
        init();
    }
    
    
    private abstract class AchievementListEntry
    {
        public static final int spacing = 5;
        
        private final int relativeX;
        protected int entryX;
        private int relativeUnscrolledY;
        protected int entryUnscrolledY;
        protected final int entryWidth;
        protected int entryHeight;
        
        public boolean isVisible = true;
        
        protected boolean isHovered = false;
        
        public AchievementListEntry(int relativeX, int relativeUnscrolledY, int entryWidth, int entryHeight)
        {
            this.relativeX = relativeX;
            this.relativeUnscrolledY = relativeUnscrolledY;
            updateX();
            updateY();
            this.entryWidth = entryWidth;
            this.entryHeight = entryHeight;
        }
        
        public void updateX()
        {
            this.entryX = getContentX() + relativeX;
        }
        
        public void updateY()
        {
            this.entryUnscrolledY = getY() + relativeUnscrolledY;
        }
        
        public void render(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
        {
            if (!isVisible) return;
            int scrolledY = getScrolledY();
            isHovered = AchievementsList.this.isHovered && guiGraphics.containsPointInScissor(mouseX, mouseY)
                && new ScreenRectangle(entryX, scrolledY, entryWidth, entryHeight).containsPoint(mouseX, mouseY);
            if (scrolledY + entryHeight < getY() || scrolledY >= getY() + height) return;
            renderContent(guiGraphics, mouseX, mouseY, partialTicks);
        }
        
        protected abstract void renderContent(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks);
        
        public void click() {}
        
        protected void setUnscrolledRelativeY(int relativeUnscrolledY)
        {
            this.relativeUnscrolledY = relativeUnscrolledY;
            updateY();
        }
        
        public int getScrolledY()
        {
            return Math.round(entryUnscrolledY - scroll);
        }
    }
    
    private class ListTitle extends AchievementListEntry
    {
        private final Component component;
        private final AchievementListEntry[] categoryEntries;
        private boolean collapsed = false;
        
        public ListTitle(String text, AchievementListEntry[] categoryEntries, boolean collapsed)
        {
            super(0, 0, CONTENT_WIDTH, 20);
            
            this.component = Component.literal(text);
            this.categoryEntries = categoryEntries;
            setCollapsed(collapsed);
        }
        
        private void setCollapsed(boolean collapsed)
        {
            this.collapsed = collapsed;
            updateEntryVisibility();
        }
        
        private void updateEntryVisibility()
        {
            for (AchievementListEntry entry : categoryEntries)
            {
                entry.isVisible = !collapsed;
            }
        }
        
        @Override
        protected void renderContent(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
        {
            int scrolledY = getScrolledY();
            
            guiGraphics.fill(entryX, scrolledY, entryX + entryWidth, scrolledY + entryHeight, 0xA008080A);
            if (this.isHovered) guiGraphics.renderOutline(entryX, scrolledY, entryWidth, entryHeight, Util.Color.WHITE);
            
            guiGraphics.drawString(font, component,
                entryX + entryWidth/2 - font.width(component)/2, scrolledY + 6,
                Util.Color.GOLD, true
            );
            
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, caretsIdentifier,
                entryX + 7, scrolledY + 6,
                0, collapsed ? 0 : 8,
                8, 8,
                16, 16
            );
            
            if (this.isHovered) guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
        }
        
        @Override
        public void click()
        {
            setCollapsed(!collapsed);
            updateContentHeight();
            
            playButtonClickSound(scathaPro.minecraft.getSoundManager());
        }
    }
    
    private class AchievementCard extends AchievementListEntry
    {
        private static final int CARD_PADDING = 5;
        
        private final Achievement achievement;
        private final boolean isAchievementUnlocked;
        
        private final Component titleComponent;
        private final @Nullable Component unlockTimeComponent;
        private final MutableComponent progressComponent;
        private final Component descriptionComponent;
        
        public AchievementCard(Achievement achievement)
        {
            super(0, 0, CONTENT_WIDTH, 37);
            
            this.achievement = achievement;
            
            
            UnlockedAchievement unlockedAchievement = scathaPro.getProfileData().unlockedAchievements.getFor(achievement);
            isAchievementUnlocked = unlockedAchievement != null;
            boolean detailsHidden = achievement.type.lockedVisibility == AchievementType.LockedVisibility.TITLE_ONLY && !isAchievementUnlocked;
            
            ChatFormatting contrastableGray = TextUtil.contrastableGray(scathaPro);
            
            Component achievementTypeComponent = (
                achievement.type.typeName != null
                    ? Component.empty().append("[").append(achievement.type.getNameComponent()).append("] ")
                    : CommonComponents.EMPTY
            );
            Component achievementNameComponent = Component.literal(achievement.achievementName);
            
            if (isAchievementUnlocked)
            {
                achievementTypeComponent = Component.empty().append(achievementTypeComponent)
                    .withStyle(ChatFormatting.GREEN);
                achievementNameComponent = Component.empty().append(achievementNameComponent)
                    .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD);
                
                // Unlock time
                unlockTimeComponent = Component.literal(
                    TimeUtil.formatUnixDateTime(unlockedAchievement.unlockTimestamp)
                ).withStyle(contrastableGray);
            }
            else unlockTimeComponent = null;
            
            titleComponent = Component.empty().append(achievementTypeComponent).append(achievementNameComponent);
            
            if (detailsHidden)
            {
                progressComponent = Component.empty().withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal("?").withStyle(ChatFormatting.OBFUSCATED))
                    .append("/")
                    .append(Component.literal("?").withStyle(ChatFormatting.OBFUSCATED));
            }
            else
            {
                if (isAchievementUnlocked)
                {
                    progressComponent = Component.empty().withStyle(ChatFormatting.GREEN);
                    if (achievement.isRepeatable && repeatCountShown)
                    {
                        if (achievement.getProgress() >= 0.01f)
                        {
                            progressComponent.append(unlockedAchievement.getRepeatCountUnlockComponent())
                                .append(Component.literal(" " + TextUtil.numberToString(achievement.getClampedProgress(), 2, false, RoundingMode.FLOOR))
                                    .setStyle(UnlockedAchievement.repeatStyle));
                        }
                        else
                        {
                            progressComponent.append(unlockedAchievement.getRepeatCountUnlockComponent())
                                .append(" " + TextUtil.numberToString(achievement.goal, 2));
                        }
                    }
                    else progressComponent.append(TextUtil.numberToString(achievement.goal, 2));
                }
                else progressComponent = Component.empty().withStyle(ChatFormatting.YELLOW)
                    .append(TextUtil.numberToString(Math.min(achievement.getClampedProgress(), achievement.goal), 2, false, RoundingMode.FLOOR));
                
                progressComponent.append("/" + TextUtil.numberToString(achievement.goal, 2));
            }
            
            if (detailsHidden) descriptionComponent = Component.literal(achievement.description.replaceAll("\\S", "?"))
                .withStyle(contrastableGray, ChatFormatting.OBFUSCATED);
            else descriptionComponent = Component.literal(achievement.description).withStyle(contrastableGray);
        }

        @Override
        public void renderContent(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
        {
            int scrolledY = getScrolledY();
            
            // Background
            
            guiGraphics.fill(entryX, scrolledY, entryX + entryWidth, scrolledY + entryHeight, 0xA008080A);
            
            // Achievement name
            
            int maxNameWidth = entryWidth - CARD_PADDING * 2 - (unlockTimeComponent != null ? font.width(unlockTimeComponent) + 4 : 0);
            renderScrollingText(guiGraphics, titleComponent, entryX + CARD_PADDING, scrolledY + CARD_PADDING, maxNameWidth);
            
            // Unlock time
            
            if (unlockTimeComponent != null)
            {
                guiGraphics.drawString(font, unlockTimeComponent,
                    entryX + entryWidth - CARD_PADDING - font.width(unlockTimeComponent), scrolledY + CARD_PADDING,
                    Util.Color.WHITE, true
                );
            }
            
            // Progress Bar
            
            int progressBarWidth = 300;
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, progressBarIdentifier,
                entryX + CARD_PADDING, scrolledY + CARD_PADDING + 24,
                0, isAchievementUnlocked ? 8 : 0,
                progressBarWidth, 3,
                512, 16
            );
            float progressBarForegroundWidthFloat = progressBarWidth * Mth.clamp(achievement.getProgress() / achievement.goal, 0f, 1f);
            int progressBarForegroundWidth = progressBarForegroundWidthFloat > 0.001f && progressBarForegroundWidthFloat < 1f ? 1 : (int) (progressBarForegroundWidthFloat);
            if (progressBarForegroundWidth > 0)
            {
                if (isAchievementUnlocked)
                {
                    if (achievement.isRepeatable && repeatCountShown)
                    {
                        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, progressBarIdentifier,
                            entryX + CARD_PADDING, scrolledY + CARD_PADDING + 24,
                            0, 12,
                            progressBarForegroundWidth, 3,
                            512, 16
                        );
                    }
                }
                else
                {
                    guiGraphics.blit(RenderPipelines.GUI_TEXTURED, progressBarIdentifier,
                        entryX + CARD_PADDING, scrolledY + CARD_PADDING + 24,
                        0, 4,
                        progressBarForegroundWidth, 3,
                        512, 16
                    );
                }
            }
            
            // Progress Numbers
            
            guiGraphics.drawString(font, progressComponent,
                entryX + CARD_PADDING + progressBarWidth - font.width(progressComponent), scrolledY + CARD_PADDING + 12,
                Util.Color.WHITE, true
            );
            
            // Description
            
            int maxDescriptionWidth = entryWidth - CARD_PADDING * 2 - font.width(progressComponent) - 4;
            renderScrollingText(guiGraphics, descriptionComponent, entryX + CARD_PADDING, scrolledY + CARD_PADDING + 12, maxDescriptionWidth);
        }
    }
    
    private void renderScrollingText(@NonNull GuiGraphics guiGraphics, @NonNull Component component, int x, int y, int maxWidth)
    {
        int overflow = Math.max(font.width(component) - maxWidth, 0);
        int animatedOffset = overflow != 0 && TEXT_SCROLL_SPEED != 0
            ? Mth.clamp((int) ((TimeUtil.now() / (1000/ TEXT_SCROLL_SPEED)) % (overflow + TEXT_SCROLL_STAY_DURATION * 2)) - TEXT_SCROLL_STAY_DURATION, 0, overflow)
            : 0;
        if (overflow != 0)
        {
            guiGraphics.scissorStack.push(new ScreenRectangle(
                x, y, maxWidth, font.lineHeight
            ));
        }
        guiGraphics.drawString(font, component,
            x - animatedOffset, y,
            Util.Color.WHITE, true
        );
        if (overflow != 0) guiGraphics.scissorStack.pop();
    }
    
    
    private enum DragMode
    {
        CLICK_WAITING, LIST, SCROLLBAR
    }
}
