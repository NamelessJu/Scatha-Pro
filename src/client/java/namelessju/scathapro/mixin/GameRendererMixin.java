package namelessju.scathapro.mixin;

import namelessju.scathapro.ScathaPro;
//? if >= 26.3 {
import net.minecraft.client.Minecraft;
//? } else {
/*import net.minecraft.client.DeltaTracker;
 *///? }
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if <= 26.1.2
//import net.minecraft.client.renderer.RenderBuffers;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin
{
    @Shadow @Final
    //? if >= 26.2 {
    private SubmitNodeStorage handAndScreenSubmitNodeStorage;
    //? } else {
    /*private SubmitNodeStorage submitNodeStorage;
    *///? }
    @Shadow @Final
    private FeatureRenderDispatcher featureRenderDispatcher;
    @Shadow @Final
    private ProjectionMatrixBuffer hud3dProjectionMatrixBuffer;
    @Shadow @Final
    private Projection hudProjection;

    //? if <= 26.1.2 {
    /*@Shadow @Final
    private RenderBuffers renderBuffers;
    *///? }

    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            //? if >= 26.2 {
            target = "Lnet/minecraft/client/renderer/RenderBuffers;endFrame()V"
            //? } else {
            /*target = "Lnet/minecraft/client/gui/render/GuiRenderer;endFrame()V",
            shift = At.Shift.AFTER
            *///? }
        )
    )
    //? if >= 26.3 {
    private void renderItemPopup(CallbackInfo ci)
    //?} else {
    /*private void renderItemPopup(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci)
    *///? }
    {
        ScathaPro.instance().itemPopupRenderer.render(
            hud3dProjectionMatrixBuffer, hudProjection,
            //? if >= 26.2 {
            handAndScreenSubmitNodeStorage,
            //? } else {
            /*submitNodeStorage,
            *///? }
            //? if >= 26.3 {
            Minecraft.getInstance().getDeltaTracker(),
            //? } else {
            /*deltaTracker,
            *///? }
            featureRenderDispatcher
            //? if <= 26.1.2
            //, renderBuffers
        );
    }
}