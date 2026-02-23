package namelessju.scathapro.mixin;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.screens.FakeBanScreen;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
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
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;renderEffects(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
            ordinal = 0,
            shift = At.Shift.AFTER
        )
    )
    private void onRenderOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci)
    {
        ScathaPro.getInstance().mainOverlay.renderIfAllowed(guiGraphics, deltaTracker);
    }
    
    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;renderTitle(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
            ordinal = 0,
            shift = At.Shift.AFTER
        )
    )
    private void afterRenderTitle(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci)
    {
        ScathaPro.getInstance().alertTitleOverlay.render(guiGraphics, deltaTracker);
    }
    
    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
            ordinal = 0
        )
    )
    private void beforeRenderCrosshair(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci)
    {
        ScathaPro.getInstance().crosshairOverlay.render(guiGraphics, deltaTracker);
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
        method = "render",
        at = @At("HEAD"),
        cancellable = true
    )
    private void beforeRender(CallbackInfo ci)
    {
        if (Minecraft.getInstance().screen instanceof FakeBanScreen)
        {
            ci.cancel();
        }
    }
    
}
