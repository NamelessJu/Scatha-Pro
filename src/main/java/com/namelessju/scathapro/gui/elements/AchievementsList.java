package com.namelessju.scathapro.gui.elements;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.input.Mouse;

import com.google.common.collect.Lists;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.achievements.AchievementCategory;
import com.namelessju.scathapro.achievements.AchievementManager;
import com.namelessju.scathapro.achievements.UnlockedAchievement;
import com.namelessju.scathapro.achievements.Achievement.Type;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.miscellaneous.StackedScissorCheck;
import com.namelessju.scathapro.util.TextUtil;
import com.namelessju.scathapro.util.TimeUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class AchievementsList extends Gui
{
    private static final int SCROLL_DISTANCE = 30;
    private static final int DESCRIPTION_SCROLL_SPEED = 40; // in pixels per second
    private static final int DESCRIPTION_SCROLL_STAY_DURATION = 70; // in pixels scrolled (affected by scroll speed)
    
    private static final ResourceLocation progressBarResourceLocation = new ResourceLocation(ScathaPro.MODID, "textures/gui/achievements/progress_bar.png");
    
    private static final Achievement.Type[] extraAchievementTypes = {Achievement.Type.SECRET, Achievement.Type.BONUS, Achievement.Type.HIDDEN, Achievement.Type.LEGACY};

    
    private int entryIndex = 0;
    private int contentHeight = 0;
    
    public int xPosition;
    public int yPosition;
    public int width;
    public int height;
    
    private Minecraft mc = Minecraft.getMinecraft();
    private FontRenderer fontRenderer = mc.fontRendererObj;
    private AchievementManager achievementManager = ScathaPro.getInstance().getAchievementManager();
    
    private AchievementListEntry[] listEntries;
    private String unlockedAchievementsString;
    private boolean hovered;
    private boolean scrollBarHovered = false;
    private boolean scrollBarRailHovered = false;
    private boolean clickingBefore = false;
    private float scroll = 0;
    private DragMode dragMode = DragMode.NONE;
    private float scrollDragStart = 0;
    
    private boolean repeatCountShown = false; 
    
    
    private enum DragMode
    {
        NONE, LIST, SCROLLBAR;
    }
    
    
    public AchievementsList(int x, int y, int width, int height)
    {
        this.xPosition = x;
        this.yPosition = y;
        this.width = width;
        this.height = height;
        
        Config config = ScathaPro.getInstance().getConfig();
        
        Achievement[] achievementList = AchievementManager.getAllAchievements();
        ArrayList<Achievement> visibleAchievements = new ArrayList<Achievement>();
        
        HashMap<Achievement.Type, Integer> achievementCount = new HashMap<Type, Integer>();
        HashMap<Achievement.Type, Integer> unlockedAchievementCount = new HashMap<Type, Integer>();
        
        for (int i = 0; i < achievementList.length; i ++)
        {
            Achievement achievement = achievementList[i];
            
            boolean unlocked = achievementManager.isAchievementUnlocked(achievement);
            
            if (
                (achievement.type.isVisible() || unlocked)
                &&
                (!unlocked || !config.getBoolean(Config.Key.hideUnlockedAchievements))
            ) {
                visibleAchievements.add(achievement);
            }
            
            Integer currentTypeCount = achievementCount.get(achievement.type);
            achievementCount.put(achievement.type, Util.intOrZero(currentTypeCount) + 1);
            
            if (unlocked)
            {
                Integer currentTypeUnlockedCount = unlockedAchievementCount.get(achievement.type);
                unlockedAchievementCount.put(achievement.type, (currentTypeUnlockedCount != null ? currentTypeUnlockedCount : 0) + 1);
            }
        }

        EnumChatFormatting contrastableGray = TextUtil.contrastableGray();
        
        StringBuilder extraAchievementsString = new StringBuilder();
        for (int i = 0; i < extraAchievementTypes.length; i ++)
        {
            Achievement.Type achievementType = extraAchievementTypes[i];
            boolean isAlwaysVisible = achievementType.isVisible();
            
            int unlockedCount = Util.intOrZero(unlockedAchievementCount.get(achievementType));
            
            if (isAlwaysVisible || unlockedCount > 0)
            {
                if (i > 0) extraAchievementsString.append(EnumChatFormatting.RESET.toString() + contrastableGray + ", ");

                extraAchievementsString.append(achievementType.formatting + unlockedCount);
                
                if (isAlwaysVisible)
                {
                    int totalCount = Util.intOrZero(achievementCount.get(achievementType));
                    
                    extraAchievementsString.append("/");
                    extraAchievementsString.append(totalCount);
                }
                
                extraAchievementsString.append(" " + achievementType.typeName);
            }
        }
        
        unlockedAchievementsString = EnumChatFormatting.GREEN + "Unlocked: " + Util.intOrZero(unlockedAchievementCount.get(Achievement.Type.NORMAL)) + "/" + Util.intOrZero(achievementCount.get(Achievement.Type.NORMAL))
            + contrastableGray + " (+ " + extraAchievementsString.toString() + contrastableGray + ")";
        
        // Create entries
        
        HashMap<AchievementCategory, ArrayList<Achievement>> achievementsPerCategory = new HashMap<AchievementCategory, ArrayList<Achievement>>();
        for (int i = 0; i < visibleAchievements.size(); i ++)
        {
            Achievement achievement = visibleAchievements.get(i);
            // Note: category may be null, but this is intended behaviour and a supported key for HashMaps
            if (!achievementsPerCategory.containsKey(achievement.category))
            {
                achievementsPerCategory.put(achievement.category, Lists.newArrayList(achievement));
            }
            else achievementsPerCategory.get(achievement.category).add(achievement);
        }
        
        listEntries = new AchievementListEntry[visibleAchievements.size() + achievementsPerCategory.keySet().size()];
        
        AchievementCategory[] allCategories = AchievementCategory.values();
        for (int i = 0; i < allCategories.length + 1; i ++)
        {
            AchievementCategory category = i < allCategories.length ? allCategories[i] : null;
            ArrayList<Achievement> categoryAchievements = achievementsPerCategory.get(category);
            if (categoryAchievements == null) continue;
            
            addEntry(new ListTitle(AchievementCategory.getName(category)));
            
            for (int j = 0; j < categoryAchievements.size(); j ++)
            {
                addEntry(new AchievementCard(categoryAchievements.get(j)));
            }
        }
        
        repeatCountShown = config.getBoolean(Config.Key.repeatCountsShown);
    }
    
    private void addEntry(AchievementListEntry entry)
    {
        listEntries[entryIndex] = entry;
        entry.setUnscrolledRelativeY(contentHeight);
        
        if (entryIndex < listEntries.length - 1) contentHeight += AchievementListEntry.spacing;
        contentHeight += entry.entryHeight;
        
        entryIndex ++;
    }
    
    public void draw(int mouseX, int mouseY, int screenHeight)
    {
        this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
        
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        
        if (listEntries.length > 0)
        {
            StackedScissorCheck.pushCheck(xPosition, yPosition, width, height);
            for (AchievementListEntry listEntry : listEntries)
            {
                listEntry.draw();
            }
            StackedScissorCheck.clearStack();
        }
        else
        {
            String emptyListText;
            if (ScathaPro.getInstance().getConfig().getBoolean(Config.Key.hideUnlockedAchievements))
            {
                emptyListText = "You've unlocked all non-hidden achievements! \\o/";
            }
            else emptyListText = "No achievements found";
            
            fontRenderer.drawString(emptyListText, xPosition + width/2 - TextUtil.getStringWidth(emptyListText)/2, yPosition + height/2 - 4, Util.Color.GRAY.getValue(), true);
        }

        int scrollBarWidth = 2;
        int scrollBarHeight = getScrollBarHeight();
        int scrollBarX = xPosition + width + 2;
        int scrollBarY = yPosition + getScrollBarRelativeY();
        if (contentHeight > height)
        {
            Gui.drawRect(scrollBarX, yPosition, scrollBarX + scrollBarWidth, yPosition + height, 0x70000000);
            Gui.drawRect(scrollBarX, scrollBarY, scrollBarX + scrollBarWidth, scrollBarY + scrollBarHeight, 0xFFFFFFFF);
        }
        
        this.scrollBarRailHovered = mouseX >= scrollBarX - 2 && mouseY >= yPosition && mouseX < scrollBarX + scrollBarWidth + 2 && mouseY < yPosition + height;
        this.scrollBarHovered = mouseX >= scrollBarX - 2 && mouseY >= scrollBarY && mouseX < scrollBarX + scrollBarWidth + 2 && mouseY < scrollBarY + scrollBarHeight;
        
        
        fontRenderer.drawString(unlockedAchievementsString, xPosition + width/2 - TextUtil.getStringWidth(unlockedAchievementsString)/2, yPosition - 12, Util.Color.WHITE.getValue(), true);
    }
    
    public void handleMouseInput(int mouseY)
    {
        if (Mouse.isButtonDown(0))
        {
            if (!clickingBefore)
            {
                if (scrollBarHovered)
                {
                    scrollDragStart = mouseY - (yPosition + getScrollBarRelativeY() + getScrollBarHeight() * 0.5f);
                    dragMode = DragMode.SCROLLBAR;
                }
                else if (scrollBarRailHovered)
                {
                    scrollDragStart = 0;
                    dragMode = DragMode.SCROLLBAR;
                }
                else if (hovered)
                {
                    scrollDragStart = mouseY + scroll;
                    dragMode = DragMode.LIST;
                }
            }
            
            if (dragMode != DragMode.NONE)
            {
                switch (dragMode)
                {
                    case SCROLLBAR:
                        int scrollBarHeight = getScrollBarHeight();
                        float mouseYRelative = (mouseY - scrollDragStart) - (yPosition + scrollBarHeight * 0.5f);
                        float mouseProgress = height != scrollBarHeight ? mouseYRelative / (height - scrollBarHeight) : 0;
                        setScroll(getScrollHeight() * mouseProgress);
                        break;
                    default:
                        setScroll(scrollDragStart - mouseY);
                        break;
                }
            }
            
            clickingBefore = true;
        }
        else
        {
            clickingBefore = false;
            
            dragMode = DragMode.NONE;
        }

        if (hovered)
        {
            int scrollDelta = Mouse.getEventDWheel();
            if (scrollDelta != 0) setScroll(getScroll() - Math.signum(scrollDelta) * SCROLL_DISTANCE);
        }
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
    
    
    private abstract class AchievementListEntry
    {
        public static final int spacing = 5;
        
        public final int entryX;
        public int entryUnscrolledY;
        public final int entryWidth;
        public final int entryHeight;
        
        public AchievementListEntry(int relativeX, int relativeUnscrolledY, int entryWidth, int entryHeight)
        {
            this.entryX = xPosition + relativeX;
            setUnscrolledRelativeY(relativeUnscrolledY);
            this.entryWidth = entryWidth;
            this.entryHeight = entryHeight;
        }
        
        public void draw()
        {
            int scrolledY = getScrolledY();
            if (scrolledY + entryHeight < yPosition || scrolledY >= yPosition + height) return;
            draw(scrolledY);
        }
        
        protected abstract void draw(int scrolledY);
        
        public void setUnscrolledRelativeY(int relativeUnscrolledY)
        {
            this.entryUnscrolledY = yPosition + relativeUnscrolledY;
        }
        
        public int getScrolledY()
        {
            return (int) Math.round(entryUnscrolledY - scroll);
        }
    }
    
    private class ListTitle extends AchievementListEntry
    {
        private String text;
        
        public ListTitle(String text)
        {
            super(0, 0, width, 20);
            
            setText(text);
        }
        
        public void setText(String text)
        {
            this.text = EnumChatFormatting.GOLD.toString() + EnumChatFormatting.UNDERLINE + text;
        }
        
        protected void draw(int scrolledY)
        {
            Gui.drawRect(entryX, scrolledY, entryX + entryWidth, scrolledY + entryHeight, 0xA008080A);
            fontRenderer.drawString(text, entryX + entryWidth/2 - TextUtil.getStringWidth(text) / 2, scrolledY + 6, Util.Color.WHITE.getValue(), true);
        }
    }

    private class AchievementCard extends AchievementListEntry
    {
        public static final int cardPadding = 5;
        
        private final Achievement achievement;
        
        public AchievementCard(Achievement achievement)
        {
            super(0, 0, width, 37);
            
            this.achievement = achievement;
        }
        
        public void draw(int scrolledY)
        {
            EnumChatFormatting contrastableGray = TextUtil.contrastableGray();
            
            UnlockedAchievement unlockedAchievement = achievementManager.getUnlockedAchievement(achievement);
            boolean unlocked = unlockedAchievement != null;
            boolean detailsHidden = achievement.type.visibility == Achievement.Type.Visibility.TITLE_ONLY && !unlocked;
            
            Gui.drawRect(entryX, scrolledY, entryX + entryWidth, scrolledY + entryHeight, 0xA008080A);
            
            if (unlocked)
            {
                // Name
                fontRenderer.drawString(EnumChatFormatting.RESET + (achievement.type.typeName != null ? EnumChatFormatting.GREEN + "[" + EnumChatFormatting.RESET + achievement.type.getFormattedName() + EnumChatFormatting.RESET + EnumChatFormatting.GREEN + "] " : "") + EnumChatFormatting.RESET.toString() + EnumChatFormatting.GREEN + EnumChatFormatting.BOLD + achievement.achievementName, entryX + cardPadding, scrolledY + cardPadding, Util.Color.WHITE.getValue(), true);
                
                // Unlock time
                String unlockedString = EnumChatFormatting.RESET.toString() + contrastableGray + TimeUtil.formatUnixDateTime(achievementManager.getUnlockedAchievement(achievement).unlockedAtTimestamp);
                fontRenderer.drawString(unlockedString, entryX + entryWidth - cardPadding - TextUtil.getStringWidth(unlockedString), scrolledY + cardPadding, Util.Color.WHITE.getValue(), true);
            }
            else
            {
                // Name
                fontRenderer.drawString(EnumChatFormatting.RESET + (achievement.type.typeName != null ? "[" + achievement.type.getFormattedName() + EnumChatFormatting.RESET + "] " : "") + EnumChatFormatting.RESET + achievement.achievementName, entryX + cardPadding, scrolledY + cardPadding, Util.Color.WHITE.getValue(), true);
            }
            
            GlStateManager.color(1f, 1f, 1f, 1f);
            mc.getTextureManager().bindTexture(progressBarResourceLocation);
            
            int progressBarWidth = 300;
            float progressBarForegroundWidthFloat = progressBarWidth * MathHelper.clamp_float(achievement.getProgress() / achievement.goal, 0f, 1f);
            int progressBarForegroundWidth = progressBarForegroundWidthFloat > 0.001f && progressBarForegroundWidthFloat < 1f ? 1 : (int) (progressBarForegroundWidthFloat);
            if (unlocked)
            {
                drawModalRectWithCustomSizedTexture(entryX + cardPadding, scrolledY + cardPadding + 24, 0, 8, progressBarWidth, 3, 512, 16);
                if (achievement.isRepeatable && repeatCountShown && progressBarForegroundWidth > 0) drawModalRectWithCustomSizedTexture(entryX + cardPadding, scrolledY + cardPadding + 24, 0, 12, progressBarForegroundWidth, 3, 512, 16);
            }
            else
            {
                drawModalRectWithCustomSizedTexture(entryX + cardPadding, scrolledY + cardPadding + 24, 0, 0, progressBarWidth, 3, 512, 16);
                if (progressBarForegroundWidth > 0) drawModalRectWithCustomSizedTexture(entryX + cardPadding, scrolledY + cardPadding + 24, 0, 4, progressBarForegroundWidth, 3, 512, 16);
            }

            // Progress
            
            String progressString;
            if (detailsHidden) progressString = EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.OBFUSCATED + "?" + EnumChatFormatting.RESET + EnumChatFormatting.YELLOW + "/" + EnumChatFormatting.OBFUSCATED + "?";
            else
            {
                progressString = "/" + Util.numberToString(achievement.goal, 2);
                if (unlocked)
                {
                    if (achievement.isRepeatable && repeatCountShown)
                    {
                        if (achievement.getProgress() >= 0.01f)
                        {
                            progressString = unlockedAchievement.getRepeatCountUnlockString() + EnumChatFormatting.RESET + " " + UnlockedAchievement.repeatFormatting + Util.numberToString(achievement.getProgress(), 2, false, RoundingMode.FLOOR) + EnumChatFormatting.RESET + EnumChatFormatting.GREEN + progressString;
                        }
                        else
                        {
                            progressString = unlockedAchievement.getRepeatCountUnlockString() + EnumChatFormatting.RESET + " " + EnumChatFormatting.GREEN + Util.numberToString(achievement.goal, 2) + progressString;
                        }
                    }
                    else progressString = EnumChatFormatting.GREEN + Util.numberToString(achievement.goal, 2) + progressString;
                }
                else progressString = EnumChatFormatting.YELLOW + Util.numberToString(Math.min(achievement.getProgress(), achievement.goal), 2, false, RoundingMode.FLOOR) + progressString;
            }
            fontRenderer.drawString(progressString, entryX + cardPadding + progressBarWidth - TextUtil.getStringWidth(progressString), scrolledY + cardPadding + 12, Util.Color.WHITE.getValue(), true);
            
            // Description
            
            String descriptionString = contrastableGray + (detailsHidden ? EnumChatFormatting.OBFUSCATED.toString() : "") + achievement.description;
            
            int progressTextWidth = TextUtil.getStringWidth(progressString);
            int maxDescriptionWidth = entryWidth - cardPadding * 2 - progressTextWidth - 4;
            int descriptionOffset = Math.max(TextUtil.getStringWidth(descriptionString) - maxDescriptionWidth, 0);
            int animatedDescriptionOffset = descriptionOffset != 0 && DESCRIPTION_SCROLL_SPEED != 0 ? MathHelper.clamp_int((int) ((TimeUtil.getAnimationTime() / (1000/DESCRIPTION_SCROLL_SPEED)) % (descriptionOffset + DESCRIPTION_SCROLL_STAY_DURATION * 2)) - DESCRIPTION_SCROLL_STAY_DURATION, 0, descriptionOffset) : 0;
            if (descriptionOffset != 0) StackedScissorCheck.pushCheck(entryX + cardPadding, scrolledY + cardPadding + 12, maxDescriptionWidth, fontRenderer.FONT_HEIGHT);
            fontRenderer.drawString(descriptionString, entryX + cardPadding - animatedDescriptionOffset, scrolledY + cardPadding + 12, Util.Color.WHITE.getValue(), true);
            if (descriptionOffset != 0) StackedScissorCheck.popCheck();
        }
    }
}
