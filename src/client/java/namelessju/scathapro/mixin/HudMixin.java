package namelessju.scathapro.mixin;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.screens.FakeBanScreen;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class HudMixin
{
    @Shadow
    private int titleTime;

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
    private void beforeExtractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci)
    {
        if (Minecraft.getInstance().gui.screen() instanceof FakeBanScreen)
        {
            ci.cancel();
        }

        ScathaPro.getInstance().scathaDropsSlotMachineManager.extractHudRenderState(graphics, deltaTracker);
        if (ScathaPro.getInstance().scathaDropsSlotMachineManager.shouldHideDrops())
        {
            extractCameraOverlays(graphics, deltaTracker);
            ci.cancel();
        }
    }

    @Inject(
        method = "extractRenderState",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Hud;extractEffects(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
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
            target = "Lnet/minecraft/client/gui/Hud;extractTitle(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
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
            target = "Lnet/minecraft/client/gui/Hud;extractCrosshair(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            ordinal = 0,
            shift = At.Shift.AFTER
        )
    )
    private void afterExtractCrosshair(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci)
    {
        ScathaPro.getInstance().crosshairOverlay.extractRenderState(graphics, deltaTracker);
    }

    @Shadow
    protected abstract void extractCameraOverlays(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker);
}