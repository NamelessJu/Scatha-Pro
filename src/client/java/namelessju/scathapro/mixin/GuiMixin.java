package namelessju.scathapro.mixin;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.screens.FakeBanScreen;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin
{
    @Shadow private int titleTime;
    
    @Inject(
        method = "extractRenderState",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;extractEffects(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            ordinal = 0,
            shift = At.Shift.AFTER
        )
    )
    private void extractMainOverlay(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci)
    {
        ScathaPro.getInstance().mainOverlay.extractRenderStateIfVisible(graphics, deltaTracker);
    }
    
    @Inject(
        method = "extractRenderState",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;extractTitle(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            ordinal = 0,
            shift = At.Shift.AFTER
        )
    )
    private void afterExtractTitle(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci)
    {
        ScathaPro.getInstance().alertTitleOverlay.extractRenderState(graphics, deltaTracker);
    }
    
    @Inject(
        method = "extractRenderState",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;extractCrosshair(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            ordinal = 0,
            shift = At.Shift.AFTER
        )
    )
    private void afterExtractCrosshair(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci)
    {
        ScathaPro.getInstance().crosshairOverlay.extractRenderState(graphics, deltaTracker);
    }
    
    @Inject(
        method = "setTitle",
        at = @At("RETURN")
    )
    private void onSetTitle(CallbackInfo ci)
    {
        if (this.titleTime > 0)
        {
            ScathaPro.getInstance().alertTitleOverlay.clearTitle();
        }
    }
    
    @Inject(
        method = "extractRenderState",
        at = @At("HEAD"),
        cancellable = true
    )
    private void beforeExtractRenderState(CallbackInfo ci)
    {
        if (Minecraft.getInstance().screen instanceof FakeBanScreen)
        {
            ci.cancel();
        }
    }
    
}
