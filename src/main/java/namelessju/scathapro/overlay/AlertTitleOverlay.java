package namelessju.scathapro.overlay;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.managers.Config;
import namelessju.scathapro.overlay.elements.DynamicOverlayContainer;
import namelessju.scathapro.overlay.elements.OverlayElement;
import namelessju.scathapro.overlay.elements.OverlaySpacing;
import namelessju.scathapro.overlay.elements.OverlayText;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.Util;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;

public class AlertTitleOverlay
{
    private final Config config;
    
    
    private final DynamicOverlayContainer container;
    private final OverlayText mainTitleText, subTitleText;
    
    private float positionX, positionY;
    private OverlayElement.Alignment contentAlignment = null;
    
    private int fadeInTicks = 0, stayTicks = 0, fadeOutTicks = 0;
    private int animationTicksLeft = 0;
    
    public AlertTitleOverlay(Config config)
    {
        this.config = config;
        
        container = new DynamicOverlayContainer(0, 0, 1f, DynamicOverlayContainer.Direction.VERTICAL);
        
        mainTitleText = new OverlayText("", Util.Color.WHITE, 0, 0, 4f);
        subTitleText = new OverlayText("", Util.Color.WHITE, 0, 14, 2f);
        
        container.add(mainTitleText);
        container.add(subTitleText);
        container.add(new OverlaySpacing(0, 0, 0, 12, 1f));
        
        updatePosition();
        updateScale();
        updateContentAlignment();
    }
    
    public void displayTitle(String titleText, String subtitleText, int fadeInTicks, int stayTicks, int fadeOutTicks)
    {
        if (titleText != null || subtitleText != null) TextUtil.clearTitle();
        
        mainTitleText.setText(titleText);
        subTitleText.setText(subtitleText);
        
        this.fadeInTicks = fadeInTicks;
        this.stayTicks = stayTicks;
        this.fadeOutTicks = fadeOutTicks;
        animationTicksLeft = getTotalAnimationTicks();
    }
    
    public void clearTitle()
    {
        animationTicksLeft = 0;
        mainTitleText.setText("");
        subTitleText.setText("");
    }
    
    public void tick()
    {
        if (animationTicksLeft > 0)
        {
            animationTicksLeft --;
            if (animationTicksLeft <= 0) clearTitle();
        }
    }
    
    public void draw(float partialTicks)
    {
        if (animationTicksLeft <= 0) return;
        
        
        container.setResponsivePosition(positionX, positionY, 0, 0, contentAlignment);
        
        
        int totalAnimationTicks = getTotalAnimationTicks();
        float partialAnimationTicksLeft = animationTicksLeft - partialTicks;
        
        float opacityPercentage = 1f;
        if (animationTicksLeft > fadeOutTicks + stayTicks)
        {
            opacityPercentage = fadeInTicks > 0 ? (totalAnimationTicks - partialAnimationTicksLeft) / fadeInTicks : 0f;
        }
        else if (animationTicksLeft <= fadeOutTicks)
        {
            opacityPercentage = fadeOutTicks > 0 ? partialAnimationTicksLeft / fadeOutTicks : 0f;
        }
        
        int opacity = (int) (MathHelper.clamp_float(opacityPercentage, 0f, 1f) * 255);
        if (opacity <= 8) return; // because Minecraft rendering...
        
        updateColor(opacity);
        
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        container.draw();
        GlStateManager.disableBlend();
    }
    
    public void drawStatic(String titleText, String subtitleText)
    {
        animationTicksLeft = 0;
        
        mainTitleText.setText(titleText);
        subTitleText.setText(subtitleText);
        
        container.setResponsivePosition(positionX, positionY, 0, 0, contentAlignment);
        
        updateColor(255);
        
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        container.draw();
        GlStateManager.disableBlend();
    }
    
    public void updatePosition()
    {
        Config config = ScathaPro.getInstance().getConfig();
        this.positionX = (float) config.getDouble(Config.Key.alertTitlePositionX);
        this.positionY = (float) config.getDouble(Config.Key.alertTitlePositionY);
    }
    
    public void updateScale()
    {
        container.setScale((float) config.getDouble(Config.Key.alertTitleScale));
    }
    
    public void updateContentAlignment()
    {
        this.contentAlignment = config.getEnum(Config.Key.alertTitleAlignment, OverlayElement.Alignment.class);
    }
    
    private void updateColor(int opacity)
    {
        int alpha = opacity << 24 & -16777216;
        int color = 16777215 | alpha;
        mainTitleText.setColor(color);
        subTitleText.setColor(color);
    }
    
    private int getTotalAnimationTicks()
    {
        return this.fadeInTicks + this.stayTicks + this.fadeOutTicks;
    }
}
