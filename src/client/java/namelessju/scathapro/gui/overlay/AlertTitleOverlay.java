package namelessju.scathapro.gui.overlay;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.alerts.title.DisplayableAlertTitle;
import namelessju.scathapro.gui.overlay.elements.*;
import namelessju.scathapro.gui.overlay.elements.GuiDynamicContainer;
import namelessju.scathapro.gui.overlay.elements.GuiText;
import namelessju.scathapro.files.Config;
import namelessju.scathapro.util.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class AlertTitleOverlay
{
    private final ScathaPro scathaPro;
    private final Config.AlertSettings alertSettings;
    
    private GuiDynamicContainer container;
    private GuiText mainTitleText, subTitleText;
    
    private int fadeInTicks = 0, stayTicks = 0, fadeOutTicks = 0;
    private int animationTicksLeft = 0;
    
    public AlertTitleOverlay(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
        alertSettings = scathaPro.config.alerts;
    }
    
    public void init()
    {
        container = new GuiDynamicContainer(0, 0, 1f, GuiDynamicContainer.Direction.VERTICAL);
        
        mainTitleText = new GuiText(scathaPro.minecraft.font, Util.Color.WHITE, 0, 0, 4f);
        subTitleText = new GuiText(scathaPro.minecraft.font, Util.Color.WHITE, 0, 14, 2f);
        
        container.add(mainTitleText);
        container.add(subTitleText);
        container.add(new GuiSpacing(0, 0, 0, 12, 1f));
    }
    
    public void displayTitle(DisplayableAlertTitle alertTitle)
    {
        displayTitle(alertTitle.title, alertTitle.subtitle, alertTitle.fadeInTicks, alertTitle.stayTicks, alertTitle.fadeOutTicks);
    }
    
    public void displayTitle(Component title, Component subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks)
    {
        scathaPro.minecraft.gui.clearTitles();
        
        if (title == null && subtitle == null
            || fadeInTicks <= 0 && stayTicks <= 0 && fadeOutTicks <= 0)
        {
            clearTitle();
            return;
        }
        
        mainTitleText.setText(title);
        subTitleText.setText(subtitle);
        
        this.fadeInTicks = fadeInTicks;
        this.stayTicks = stayTicks;
        this.fadeOutTicks = fadeOutTicks;
        animationTicksLeft = getTotalAnimationTicks();
    }
    
    public void clearTitle()
    {
        animationTicksLeft = 0;
        mainTitleText.setText((Component) null);
        subTitleText.setText((Component) null);
    }
    
    public void tick()
    {
        if (animationTicksLeft > 0)
        {
            animationTicksLeft --;
            if (animationTicksLeft <= 0) clearTitle();
        }
    }
    
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker)
    {
        if (animationTicksLeft <= 0) return;
        
        container.setScale(alertSettings.titleScale.get());
        container.setResponsivePosition(scathaPro.minecraft.getWindow(),
            alertSettings.titlePositionX.get(), alertSettings.titlePositionY.get(),
            0, 0, alertSettings.titleAlignmentOverride.get()
        );
        
        float partialAnimationTicksLeft = animationTicksLeft - deltaTracker.getGameTimeDeltaPartialTick(false);
        
        int opacity = 255;
        if (animationTicksLeft > fadeOutTicks + stayTicks) {
            float g = (float) (fadeInTicks + stayTicks + fadeOutTicks) - partialAnimationTicksLeft;
            opacity = (int) (g * 255.0F / (float)fadeInTicks);
        }
        
        if (animationTicksLeft <= fadeOutTicks) {
            opacity = (int) (partialAnimationTicksLeft * 255.0F / (float)fadeOutTicks);
        }
        
        opacity = Mth.clamp(opacity, 0, 255);
        updateColor(opacity);
        
        container.render(guiGraphics, deltaTracker);
    }
    
    public void renderStaticComponents(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Component titleText, Component subtitleText)
    {
        animationTicksLeft = 0;
        
        mainTitleText.setText(titleText);
        subTitleText.setText(subtitleText);
        
        container.setScale(alertSettings.titleScale.get());
        container.setResponsivePosition(scathaPro.minecraft.getWindow(),
            alertSettings.titlePositionX.get(), alertSettings.titlePositionY.get(),
            0, 0, alertSettings.titleAlignmentOverride.get()
        );
        
        updateColor(255);
        container.render(guiGraphics, deltaTracker);
    }
    
    private void updateColor(int opacity)
    {
        mainTitleText.setColor(ARGB.white(opacity / 255f));
        subTitleText.setColor(ARGB.white(opacity / 255f));
    }
    
    private int getTotalAnimationTicks()
    {
        return this.fadeInTicks + this.stayTicks + this.fadeOutTicks;
    }
}
