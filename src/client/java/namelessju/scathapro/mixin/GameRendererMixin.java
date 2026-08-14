package namelessju.scathapro.mixin;

import namelessju.scathapro.ScathaPro;
import net.minecraft.client.DeltaTracker;
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

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin
{
    @Shadow @Final
    private SubmitNodeStorage handAndScreenSubmitNodeStorage;
    @Shadow @Final
    private FeatureRenderDispatcher featureRenderDispatcher;
    @Shadow @Final
    private ProjectionMatrixBuffer hud3dProjectionMatrixBuffer;
    @Shadow @Final
    private Projection hudProjection;

    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderBuffers;endFrame()V"
        )
    )
    private void renderItemPopup(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci)
    {
        ScathaPro.getInstance().itemPopupRenderer.render(
            hud3dProjectionMatrixBuffer, hudProjection,
            handAndScreenSubmitNodeStorage, deltaTracker,
            featureRenderDispatcher
        );
    }
}