package namelessju.scathapro.mixin;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import namelessju.scathapro.ScathaPro;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CachedPerspectiveProjectionMatrixBuffer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderBuffers;
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
    private Minecraft minecraft;
    @Shadow @Final
    private SubmitNodeStorage submitNodeStorage;
    @Shadow @Final
    private FeatureRenderDispatcher featureRenderDispatcher;
    @Shadow @Final
    private CachedPerspectiveProjectionMatrixBuffer hud3dProjectionMatrixBuffer;
    @Shadow @Final
    private Camera mainCamera;
    @Shadow @Final
    private RenderBuffers renderBuffers;
    
    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;applyCursor(Lcom/mojang/blaze3d/platform/Window;)V",
            shift = At.Shift.AFTER
        )
    )
    private void renderItemPopup(DeltaTracker deltaTracker, boolean bl, CallbackInfo ci)
    {
        float f = deltaTracker.getGameTimeDeltaPartialTick(true);
        RenderSystem.setProjectionMatrix(
            this.hud3dProjectionMatrixBuffer.getBuffer(
                this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight(), getFov(mainCamera, f, false)
            ),
            ProjectionType.PERSPECTIVE
        );
        //noinspection DataFlowIssue
        RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(this.minecraft.getMainRenderTarget().getDepthTexture(), 1D);
        ScathaPro.getInstance().itemPopupRenderer.render(submitNodeStorage, f);
        featureRenderDispatcher.renderAllFeatures();
        renderBuffers.bufferSource().endBatch();
    }
    
    @Shadow
    protected abstract float getFov(Camera camera, float f, boolean bl);
}
