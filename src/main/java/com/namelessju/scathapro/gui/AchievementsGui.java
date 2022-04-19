package com.namelessju.scathapro.gui;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.Util;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.achievements.AchievementManager;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

public class AchievementsGui extends ScathaProGui {
    
    private static final ResourceLocation progressBarResourceLocation = new ResourceLocation(ScathaPro.MODID, "textures/achievements_progress_bar.png");
    
    private static final int scrollDistance = 15;
    private static final int cardSpacing = AchievementCard.cardHeight + 5;
    
    private ScaledResolution scaledResolution;
    
    private int scrollBoundaryStart, scrollBoundaryHeight;
    
    private AchievementCard[] achievementCards;
    private float scroll = 0;
    
    private float scrollDragStart = -1;
    
    private int[] easterEggKeys = {31, 22, 31}; // = "sus"
    private int easterEggProgress = 0;
    private boolean easterEggTriggered = false;

    private class AchievementCard {
        public static final int cardHeight = 40;
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
                Gui.drawRect(x, y, x + cardWidth, y + cardHeight, 0xA0101015);
                if (AchievementManager.getInstance().isAchievementUnlocked(achievement)) {
                    String unlockedString = EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(AchievementManager.getInstance().getUnlockedAchievement(achievement).unlockedAtTimestamp));
                    fontRendererObj.drawString(unlockedString, x + cardWidth - cardPadding - fontRendererObj.getStringWidth(unlockedString), y + cardPadding, Util.Color.WHITE.getValue(), true);

                    fontRendererObj.drawString(EnumChatFormatting.RESET.toString() + EnumChatFormatting.GOLD + EnumChatFormatting.BOLD + achievement.name, x + cardPadding, y + cardPadding, Util.Color.WHITE.getValue(), true);
                }
                else fontRendererObj.drawString(achievement.name, x + cardPadding, y + cardPadding, Util.Color.WHITE.getValue(), true);
                fontRendererObj.drawString(EnumChatFormatting.GRAY + achievement.description, x + cardPadding, y + cardPadding + 12, Util.Color.WHITE.getValue(), true);

                GlStateManager.color(1f, 1f, 1f, 1f);
                mc.getTextureManager().bindTexture(progressBarResourceLocation);
                
                int barWidth = 300;
                drawModalRectWithCustomSizedTexture(x + cardPadding, y + cardPadding + 25, 0, 0, barWidth, 5, 512, 16);
                
                int progress = Math.round(barWidth * Math.min(achievement.getProgress() / achievement.goal, 1));
                if (progress >= barWidth) drawModalRectWithCustomSizedTexture(x + cardPadding, y + cardPadding + 25, 0, 10, barWidth, 5, 512, 16);
                else if (progress > 0) drawModalRectWithCustomSizedTexture(x + cardPadding, y + cardPadding + 25, 0, 5, progress, 5, 512, 16);

                String progressString = (AchievementManager.getInstance().isAchievementUnlocked(achievement) ? EnumChatFormatting.GREEN : EnumChatFormatting.YELLOW).toString() + Util.numberToString(Math.min(achievement.getProgress(), achievement.goal), 2) + "/" + Util.numberToString(achievement.goal, 2);
                GlStateManager.pushMatrix();
                GlStateManager.translate(x + cardPadding + barWidth - fontRendererObj.getStringWidth(progressString), y + cardPadding + 12, 0);
                GlStateManager.scale(1f, 1f, 1f);
                fontRendererObj.drawString(progressString, 0, 0, Util.Color.WHITE.getValue(), true);
                GlStateManager.popMatrix();
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
        
        scrollBoundaryStart = Math.round(30 + 20 * heightFactor - 6);
        scrollBoundaryHeight = Math.round(height - scrollBoundaryStart - 20 - 50 * heightFactor - 10);
        
        Achievement[] achievementList = AchievementManager.getAllAchievements();
        
        achievementCards = new AchievementCard[achievementList.length];
        
        for (int i = 0; i < achievementList.length; i ++) {
            achievementCards[i] = new AchievementCard(i, achievementList[i]);
        }
        
        setScroll(getScroll()); // clamp scroll on resolution change
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
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
        
        if (keyCode == easterEggKeys[easterEggProgress]) {
            easterEggProgress ++;
            
            if (easterEggProgress >= easterEggKeys.length) {
                easterEggTriggered = !easterEggTriggered;
                easterEggProgress = 0;
            }
        }
        else if (keyCode == easterEggKeys[0]) easterEggProgress = 1;
        else easterEggProgress = 0;
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
        
        if (easterEggTriggered) {
            GlStateManager.color(1f, 1f, 1f, 1f);
            mc.getTextureManager().bindTexture(progressBarResourceLocation);
            
            GlStateManager.pushMatrix();
            GlStateManager.translate(width/2 - 150, height/2 - 21, 0);
            GlStateManager.scale(6f, 6f, 1f);
            drawModalRectWithCustomSizedTexture(0, 0, 375, 4, 50, 7, 512, 16);
            GlStateManager.popMatrix();
        }
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
