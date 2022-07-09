package com.namelessju.scathapro.gui;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.Util;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.achievements.Achievement.Type;
import com.namelessju.scathapro.achievements.AchievementManager;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

public class AchievementsGui extends ScathaProGui {
    
    private static final ResourceLocation progressBarResourceLocation = new ResourceLocation(ScathaPro.MODID, "textures/gui/achievements/progress_bar.png");
    
    private static final int scrollDistance = 30;
    private static final int cardSpacing = AchievementCard.cardHeight + 5;
    
    private ScaledResolution scaledResolution;
    
    private int scrollBoundaryStart, scrollBoundaryHeight;
    
    private AchievementCard[] achievementCards;
    private float scroll = 0;
    
    private float scrollDragStart = -1;

    private class AchievementCard {
        public static final int cardHeight = 37;
        public static final int cardWidth = 310;
        public static final int cardPadding = 5;

        int listIndex;
        private final Achievement achievement;
        
        public AchievementCard(int listIndex, Achievement achievement) {
            this.listIndex = listIndex;
            this.achievement = achievement;
        }
        
        public void draw() {
            int x = width / 2 - cardWidth / 2;
            int y = Math.round(scrollBoundaryStart + listIndex * cardSpacing - scroll);
            
            if (y + AchievementCard.cardHeight >= scrollBoundaryStart && y < scrollBoundaryStart + scrollBoundaryHeight) {
                
                boolean unlocked = AchievementManager.instance.isAchievementUnlocked(achievement);
                
                Gui.drawRect(x, y, x + cardWidth, y + cardHeight, 0xA0101012);
                
                if (unlocked) {
                    fontRendererObj.drawString(EnumChatFormatting.RESET.toString() + EnumChatFormatting.GREEN + EnumChatFormatting.BOLD + achievement.name + (achievement.type.string != null ? EnumChatFormatting.RESET.toString() + EnumChatFormatting.GREEN + " [" + achievement.type.string + EnumChatFormatting.RESET + EnumChatFormatting.GREEN + "]" : ""), x + cardPadding, y + cardPadding, Util.Color.WHITE.getValue(), true);

                    String unlockedString = EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(AchievementManager.instance.getUnlockedAchievement(achievement).unlockedAtTimestamp));
                    fontRendererObj.drawString(unlockedString, x + cardWidth - cardPadding - fontRendererObj.getStringWidth(unlockedString), y + cardPadding, Util.Color.WHITE.getValue(), true);
                }
                else fontRendererObj.drawString(EnumChatFormatting.RESET + achievement.name + (achievement.type.string != null ? " [" + achievement.type.string + EnumChatFormatting.RESET + "]" : ""), x + cardPadding, y + cardPadding, Util.Color.WHITE.getValue(), true);
                fontRendererObj.drawString(EnumChatFormatting.GRAY + (achievement.type == Achievement.Type.SECRET && !unlocked ? EnumChatFormatting.OBFUSCATED.toString() : "") + achievement.description, x + cardPadding, y + cardPadding + 12, Util.Color.WHITE.getValue(), true);

                GlStateManager.color(1f, 1f, 1f, 1f);
                mc.getTextureManager().bindTexture(progressBarResourceLocation);
                
                int barWidth = 300;
                drawModalRectWithCustomSizedTexture(x + cardPadding, y + cardPadding + 24, 0, 0, barWidth, 3, 512, 16);
                
                int progress = Math.round(barWidth * Math.min(achievement.getProgress() / achievement.goal, 1));
                if (progress >= barWidth) drawModalRectWithCustomSizedTexture(x + cardPadding, y + cardPadding + 24, 0, 8, barWidth, 3, 512, 16);
                else if (progress > 0) drawModalRectWithCustomSizedTexture(x + cardPadding, y + cardPadding + 24, 0, 4, progress, 3, 512, 16);

                String progressString = (AchievementManager.instance.isAchievementUnlocked(achievement) ? EnumChatFormatting.GREEN : EnumChatFormatting.YELLOW).toString() + (achievement.type == Achievement.Type.SECRET && !unlocked ? "?/?" : Util.numberToString(Math.min(achievement.getProgress(), achievement.goal), 2) + "/" + Util.numberToString(achievement.goal, 2));
                fontRendererObj.drawString(progressString, x + cardPadding + barWidth - fontRendererObj.getStringWidth(progressString), y + cardPadding + 12, Util.Color.WHITE.getValue(), true);
            }
        }
    }
    
    @Override
    public String getTitle() {
        return "Achievements";
    }

    public AchievementsGui(GuiScreen parentGui) {
        super(parentGui);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        
        scaledResolution = new ScaledResolution(mc);
        float heightFactor = (height * scaledResolution.getScaleFactor()) / 1080f;

        buttonList.add(new GuiButton(504704399, width / 2 - 100, Math.round(height - 20 - 50 * heightFactor), 200, 20, "Close"));
        
        scrollBoundaryStart = Math.round(47 - 6);
        scrollBoundaryHeight = Math.round(height - scrollBoundaryStart - 20 - 50 * heightFactor - 10);
        
        Achievement[] achievementList = AchievementManager.getAllAchievements();
        ArrayList<Achievement> visibleAchievements = new ArrayList<Achievement>();
        
        Hashtable<Achievement.Type, Integer> achievementCount = new Hashtable<Type, Integer>();
        Hashtable<Achievement.Type, Integer> unlockedAchievementCount = new Hashtable<Type, Integer>();

        for (int i = 0; i < achievementList.length; i ++) {
            Achievement achievement = achievementList[i];
            
            boolean unlocked = AchievementManager.instance.isAchievementUnlocked(achievement);
            
            if (achievement.type != Achievement.Type.HIDDEN || unlocked) visibleAchievements.add(achievement);

            Integer currentTypeCount = achievementCount.get(achievement.type);
            achievementCount.put(achievement.type, getNullableInteger(currentTypeCount) + 1);
            
            if (unlocked) {
                Integer currentTypeUnlockedCount = unlockedAchievementCount.get(achievement.type);
                unlockedAchievementCount.put(achievement.type, (currentTypeUnlockedCount != null ? currentTypeUnlockedCount : 0) + 1);
            }
        }
        
        int unlockedHiddenAchievements = getNullableInteger(unlockedAchievementCount.get(Achievement.Type.HIDDEN));
        
        GuiLabel progressLabel = new GuiLabel(fontRendererObj, 1, width / 2 - 155, 28, 310, 10, Util.Color.WHITE.getValue()).setCentered();
        progressLabel.func_175202_a("Unlocked: " + getNullableInteger(unlockedAchievementCount.get(Achievement.Type.NORMAL)) + "/" + getNullableInteger(achievementCount.get(Achievement.Type.NORMAL)) + " (+ " + getNullableInteger(unlockedAchievementCount.get(Achievement.Type.SECRET)) + "/" + getNullableInteger(achievementCount.get(Achievement.Type.SECRET)) + " Secret" + (unlockedHiddenAchievements > 0 ? " & " + unlockedHiddenAchievements + " HIDDEN" : "") + ")");
        labelList.add(progressLabel);
        
        achievementCards = new AchievementCard[visibleAchievements.size()];
        
        for (int i = 0; i < visibleAchievements.size(); i ++)
            achievementCards[i] = new AchievementCard(i, visibleAchievements.get(i));
        
        setScroll(getScroll()); // clamp scroll on resolution change
    }
    
    private int getNullableInteger(Integer integer) {
        return integer != null ? integer.intValue() : 0;
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled && button.id == 504704399)
            openParentGui();
    }
    
    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        
        if (Mouse.isButtonDown(0)) {
            if (scrollDragStart < 0) scrollDragStart = mouseY + scroll;
            else setScroll(scrollDragStart - mouseY);
        }
        else scrollDragStart = -1f;
        
        int scrollDelta = Mouse.getEventDWheel();
        if (scrollDelta != 0) setScroll(getScroll() - Math.signum(scrollDelta) * scrollDistance);
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);

        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(0, (height - scrollBoundaryStart - scrollBoundaryHeight) * scaledResolution.getScaleFactor(), width * scaledResolution.getScaleFactor(), scrollBoundaryHeight * scaledResolution.getScaleFactor());
        for (AchievementCard card : achievementCards) {
            card.draw();
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        
        int scrollHeight = (achievementCards.length - 1) * cardSpacing - scrollBoundaryHeight + AchievementCard.cardHeight;
        int scrollBarHeight = Math.round((scrollBoundaryHeight / (float) scrollHeight) * scrollBoundaryHeight);
        int scrollBarOffset = Math.round((scrollBoundaryHeight - scrollBarHeight) * (scroll / scrollHeight));
        Gui.drawRect(width / 2 + AchievementCard.cardWidth / 2 + 2, scrollBoundaryStart + scrollBarOffset, width / 2 + AchievementCard.cardWidth / 2 + 4, scrollBoundaryStart + scrollBarOffset + scrollBarHeight, 0xB0000002);
    }
    
    public void setScroll(float scroll) {
        if (scroll < 0) scroll = 0;
        else if (scroll > (achievementCards.length - 1) * cardSpacing - scrollBoundaryHeight + AchievementCard.cardHeight)
            scroll = (achievementCards.length - 1) * cardSpacing + AchievementCard.cardHeight > scrollBoundaryHeight
                    ? (achievementCards.length - 1) * cardSpacing - scrollBoundaryHeight + AchievementCard.cardHeight
                    : 0;
        this.scroll = scroll;
    }
    
    public float getScroll() {
        return scroll;
    }

}
