package namelessju.scathapro.mixin;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.screens.FakeBanScreen;
import namelessju.scathapro.gui.menus.screens.ScathaDropsSlotMachinePreviewScreen;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >= 26.2 {
import net.minecraft.client.gui.Hud;

@Mixin(Hud.class)
//? } else {
/*import net.minecraft.client.gui.Gui;

@Mixin(Gui.class)
*///? }
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
            ScathaPro.instance().alertTitleOverlay.clearTitle();
        }
    }

    @Inject(
        method = "extractRenderState",
        at = @At("HEAD"),
        cancellable = true
    )
    private void beforeExtractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci)
    {
        Screen currentScreen = Minecraft.getInstance().gui.screen();
        if (currentScreen instanceof FakeBanScreen)
        {
            ci.cancel();
        }

        if (!(currentScreen instanceof ScathaDropsSlotMachinePreviewScreen))
        {
            ScathaPro.instance().scathaDropsSlotMachineManager.extractHudRenderState(graphics, deltaTracker);
        }
        if (ScathaPro.instance().scathaDropsSlotMachineManager.shouldHideDrops())
        {
            extractCameraOverlays(graphics, deltaTracker);
            ci.cancel();
        }
    }

    @Inject(
        method = "extractRenderState",
        at = @At(
            value = "INVOKE",
            //? if >= 26.2 {
            target = "Lnet/minecraft/client/gui/Hud;extractEffects(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            //? } else {
            /*target = "Lnet/minecraft/client/gui/Gui;extractEffects(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            *///? }
            ordinal = 0,
            shift = At.Shift.AFTER
        )
    )
    private void extractMainOverlay(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci)
    {
        ScathaPro.instance().mainOverlay.extractRenderStateIfVisible(graphics, deltaTracker);
    }

    @Inject(
        method = "extractRenderState",
        at = @At(
            value = "INVOKE",
            //? if >= 26.2 {
            target = "Lnet/minecraft/client/gui/Hud;extractTitle(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            //? } else {
            /*target = "Lnet/minecraft/client/gui/Gui;extractTitle(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            *///? }
            ordinal = 0,
            shift = At.Shift.AFTER
        )
    )
    private void afterExtractTitle(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci)
    {
        ScathaPro.instance().alertTitleOverlay.extractRenderState(graphics, deltaTracker);
    }

    @Inject(
        method = "extractRenderState",
        at = @At(
            value = "INVOKE",
            //? if >= 26.2 {
            target = "Lnet/minecraft/client/gui/Hud;extractCrosshair(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            //? } else {
            /*target = "Lnet/minecraft/client/gui/Gui;extractCrosshair(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            *///? }
            ordinal = 0,
            shift = At.Shift.AFTER
        )
    )
    private void afterExtractCrosshair(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci)
    {
        ScathaPro.instance().crosshairOverlay.extractRenderState(graphics, deltaTracker);
    }

    @Shadow
    protected abstract void extractCameraOverlays(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker);
}