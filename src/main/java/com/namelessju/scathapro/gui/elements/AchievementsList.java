package com.namelessju.scathapro.gui.elements;

import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.achievements.AchievementManager;
import com.namelessju.scathapro.achievements.Achievement.Type;
import com.namelessju.scathapro.util.MessageUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

public class AchievementsList extends Gui
{
    private static final ResourceLocation progressBarResourceLocation = new ResourceLocation(ScathaPro.MODID, "textures/gui/achievements/progress_bar.png");
    
    private static final Achievement.Type[] extraAchievementTypes = {Achievement.Type.SECRET, Achievement.Type.BONUS, Achievement.Type.HIDDEN, Achievement.Type.LEGACY};
    private static final int scrollDistance = 30;
    
    
    public int xPosition;
    public int yPosition;
    public int width;
    public int height;
    
    private Minecraft mc = Minecraft.getMinecraft();
    private FontRenderer fontRenderer = mc.fontRendererObj;
    private ScaledResolution scaledResolution = new ScaledResolution(mc);
    private AchievementManager achievementManager = ScathaPro.getInstance().getAchievementManager();
    
    private AchievementCard[] achievementCards;
    private String unlockedAchievementsString;
    private boolean hovered;
    private boolean scrollBarHovered = false;
    private boolean scrollBarRailHovered = false;
    private boolean clickingBefore = false;
    private float scroll = 0;
    private DragMode dragMode = DragMode.NONE;
    private float scrollDragStart = 0;
    

    private class AchievementCard
    {
        public static final int cardHeight = 37;
        public static final int cardPadding = 5;
        public static final int cardSpacing = 5;

        int listIndex;
        private final Achievement achievement;
        
        public AchievementCard(int listIndex, Achievement achievement)
        {
            this.listIndex = listIndex;
            this.achievement = achievement;
        }
        
        public void draw()
        {
            EnumChatFormatting contrastableGray = MessageUtil.contrastableGray();
            
            int cardX = xPosition;
            int cardY = Math.round(yPosition + listIndex * (cardHeight + cardSpacing) - scroll);
            
            if (cardY + cardHeight < yPosition || cardY >= yPosition + height) return;
            
            boolean unlocked = achievementManager.isAchievementUnlocked(achievement);
            boolean detailsHidden = achievement.type.visibility == Achievement.Type.Visibility.TITLE_ONLY && !unlocked;
            
            Gui.drawRect(cardX, cardY, cardX + width, cardY + cardHeight, 0xA008080A);
            
            if (unlocked)
            {
                fontRenderer.drawString((achievement.type.typeName != null ? EnumChatFormatting.RESET.toString() + EnumChatFormatting.GREEN + "[" + EnumChatFormatting.RESET + achievement.type.getFormattedName() + EnumChatFormatting.RESET + EnumChatFormatting.GREEN + "] " : "") + EnumChatFormatting.RESET.toString() + EnumChatFormatting.GREEN + EnumChatFormatting.BOLD + achievement.achievementName, cardX + cardPadding, cardY + cardPadding, Util.Color.WHITE.getValue(), true);
                
                String unlockedString = EnumChatFormatting.RESET.toString() + contrastableGray + MessageUtil.formatDateTime(achievementManager.getUnlockedAchievement(achievement).unlockedAtTimestamp);
                fontRenderer.drawString(unlockedString, cardX + width - cardPadding - fontRenderer.getStringWidth(unlockedString), cardY + cardPadding, Util.Color.WHITE.getValue(), true);
            }
            else
            {
                fontRenderer.drawString((achievement.type.typeName != null ? "[" + achievement.type.getFormattedName() + EnumChatFormatting.RESET + "] " : "") + EnumChatFormatting.RESET + achievement.achievementName, cardX + cardPadding, cardY + cardPadding, Util.Color.WHITE.getValue(), true);
            }
            fontRenderer.drawString(contrastableGray + (detailsHidden ? EnumChatFormatting.OBFUSCATED.toString() : "") + achievement.description, cardX + cardPadding, cardY + cardPadding + 12, Util.Color.WHITE.getValue(), true);
            
            GlStateManager.color(1f, 1f, 1f, 1f);
            mc.getTextureManager().bindTexture(progressBarResourceLocation);
            
            int barWidth = 300;
            drawModalRectWithCustomSizedTexture(cardX + cardPadding, cardY + cardPadding + 24, 0, 0, barWidth, 3, 512, 16);
            
            int progress = Math.round(barWidth * Math.min(achievement.getProgress() / achievement.goal, 1));
            if (progress >= barWidth) drawModalRectWithCustomSizedTexture(cardX + cardPadding, cardY + cardPadding + 24, 0, 8, barWidth, 3, 512, 16);
            else if (progress > 0) drawModalRectWithCustomSizedTexture(cardX + cardPadding, cardY + cardPadding + 24, 0, 4, progress, 3, 512, 16);
            
            String progressString;
            if (detailsHidden) progressString = EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.OBFUSCATED + "?" + EnumChatFormatting.RESET + EnumChatFormatting.YELLOW + "/" + EnumChatFormatting.OBFUSCATED + "?";
            else
            {
                progressString = Util.numberToString(Math.min(achievement.getProgress(), achievement.goal), 2) + "/" + Util.numberToString(achievement.goal, 2);
                if (unlocked) progressString = EnumChatFormatting.GREEN + progressString;
                else progressString = EnumChatFormatting.YELLOW + progressString;
            }
            fontRenderer.drawString(progressString, cardX + cardPadding + barWidth - fontRenderer.getStringWidth(progressString), cardY + cardPadding + 12, Util.Color.WHITE.getValue(), true);
        }
    }
    
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
        
        Achievement[] achievementList = AchievementManager.getAllAchievements();
        ArrayList<Achievement> visibleAchievements = new ArrayList<Achievement>();
        
        HashMap<Achievement.Type, Integer> achievementCount = new HashMap<Type, Integer>();
        HashMap<Achievement.Type, Integer> unlockedAchievementCount = new HashMap<Type, Integer>();
        
        for (int i = 0; i < achievementList.length; i ++)
        {
            Achievement achievement = achievementList[i];
            
            boolean unlocked = achievementManager.isAchievementUnlocked(achievement);
            
            if (achievement.type.visibility != Achievement.Type.Visibility.HIDDEN || unlocked) visibleAchievements.add(achievement);
            
            Integer currentTypeCount = achievementCount.get(achievement.type);
            achievementCount.put(achievement.type, Util.intOrZero(currentTypeCount) + 1);
            
            if (unlocked)
            {
                Integer currentTypeUnlockedCount = unlockedAchievementCount.get(achievement.type);
                unlockedAchievementCount.put(achievement.type, (currentTypeUnlockedCount != null ? currentTypeUnlockedCount : 0) + 1);
            }
        }

        EnumChatFormatting contrastableGray = MessageUtil.contrastableGray();
        
        StringBuilder extraAchievementsString = new StringBuilder();
        for (int i = 0; i < extraAchievementTypes.length; i ++)
        {
            Achievement.Type achievementType = extraAchievementTypes[i];
            boolean isAlwaysVisible = achievementType.visibility != Achievement.Type.Visibility.HIDDEN;
            
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
        
        achievementCards = new AchievementCard[visibleAchievements.size()];
        
        for (int i = 0; i < visibleAchievements.size(); i ++)
        {
            achievementCards[i] = new AchievementCard(i, visibleAchievements.get(i));
        }
    }
    
    public void draw(int mouseX, int mouseY, int screenHeight)
    {
        this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
        
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(xPosition * scaledResolution.getScaleFactor(), (screenHeight - yPosition - height) * scaledResolution.getScaleFactor(), width * scaledResolution.getScaleFactor(), height * scaledResolution.getScaleFactor());
        for (AchievementCard card : achievementCards)
        {
            card.draw();
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        
        int scrollBarWidth = 2;
        int scrollBarHeight = getScrollBarHeight();
        int scrollBarX = xPosition + width + 2;
        int scrollBarY = yPosition + getScrollBarRelativeY();
        Gui.drawRect(scrollBarX, yPosition, scrollBarX + scrollBarWidth, yPosition + height, 0x70000000);
        Gui.drawRect(scrollBarX, scrollBarY, scrollBarX + scrollBarWidth, scrollBarY + scrollBarHeight, 0xFFFFFFFF);
        
        this.scrollBarRailHovered = mouseX >= scrollBarX - 2 && mouseY >= yPosition && mouseX < scrollBarX + scrollBarWidth + 2 && mouseY < yPosition + height;
        this.scrollBarHovered = mouseX >= scrollBarX - 2 && mouseY >= scrollBarY && mouseX < scrollBarX + scrollBarWidth + 2 && mouseY < scrollBarY + scrollBarHeight;

        
        fontRenderer.drawString(unlockedAchievementsString, xPosition + width/2 - fontRenderer.getStringWidth(unlockedAchievementsString)/2, yPosition - 12, Util.Color.WHITE.getValue(), true);
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
            if (scrollDelta != 0) setScroll(getScroll() - Math.signum(scrollDelta) * scrollDistance);
        }
    }
    
    
    public void setScroll(float scroll)
    {
        int contentHeight = getContentHeight();
        
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

    public int getContentHeight()
    {
        return Math.max(AchievementCard.cardHeight * achievementCards.length + (achievementCards.length - 1) * (AchievementCard.cardSpacing), 0);
    }
    
    public int getScrollHeight()
    {
        return Math.max(getContentHeight() - height, 0);
    }
    
    public int getScrollBarHeight()
    {
        return Math.round(Math.max(Math.min((float) height / getContentHeight(), 1f), 0.1f) * height);
    }
    
    public int getScrollBarRelativeY()
    {
        return Math.round((height - getScrollBarHeight()) * (scroll / getScrollHeight()));
    }
}
