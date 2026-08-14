package namelessju.scathapro.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import namelessju.scathapro.miscellaneous.data.mixindata.IArmorStandRenderStateData;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.UnicodeSymbol;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.math.RoundingMode;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin
{
    @Inject(
        method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V",
            ordinal = 0
        )
    )
    private void afterRenderNameDisplay(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, int offset, CallbackInfo ci)
    {
        IArmorStandRenderStateData data = null;
        if (state instanceof IArmorStandRenderStateData stateData) data = stateData;
        if (data == null) return;

        if (data.scathapro$getWormLifetimeLeft() < 0f) return;

        Component component = Component.literal(
            UnicodeSymbol.hourglass + " "
                + TextUtil.numberToString(data.scathapro$getWormLifetimeLeft(), 1, true, RoundingMode.UP)
                + "s"
        ).withColor(
            data.scathapro$getWormLifetimeLeft() < 5f ? TextColor.RED : (
                data.scathapro$getWormLifetimeLeft() < 15f ? TextColor.YELLOW : TextColor.GREEN
            )
        );

        poseStack.translate(0.0F, 9.0F * 1.15F * 0.025F, 0.0F);
        submitNodeCollector.submitNameTag(poseStack, state.nameTagAttachment, offset, component, !state.isDiscrete, state.lightCoords, camera);
    }
}