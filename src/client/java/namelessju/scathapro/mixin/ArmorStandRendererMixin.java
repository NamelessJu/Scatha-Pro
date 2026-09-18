package namelessju.scathapro.mixin;

import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.managers.detectors.entities.detected.DetectedWorm;
import namelessju.scathapro.miscellaneous.data.mixindata.IArmorStandRenderStateData;
import namelessju.scathapro.miscellaneous.data.mixindata.IWormArmorStandData;
import namelessju.scathapro.util.TimeUtil;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorStandRenderer.class)
public abstract class ArmorStandRendererMixin
{
    @Inject(
        method = "extractRenderState(Lnet/minecraft/world/entity/decoration/ArmorStand;Lnet/minecraft/client/renderer/entity/state/ArmorStandRenderState;F)V",
        at = @At("TAIL")
    )
    private void onExtractRenderState(ArmorStand entity, ArmorStandRenderState state, float partialTicks, CallbackInfo ci)
    {
        IArmorStandRenderStateData extraData = ((IArmorStandRenderStateData) state);
        IWormArmorStandData wormData = (IWormArmorStandData) entity;
        DetectedWorm worm = wormData.scathaPro$getWorm();


        if (worm != null)
        {
            extraData.scathaPro$setIsWorm(worm.isScatha);
        }

        extraData.scathaPro$setWormSegmentType(wormData.scathaPro$getWormSegmentType());


        if (!ScathaPro.instance().config.worms.showLifetimeLeft.get())
        {
            extraData.scathaPro$setWormLifetimeLeft(-1f);
            return;
        }

        if (!wormData.scathaPro$isWormNametag()) return;
        wormData.scathaPro$getWorm();
        if (worm == null) return;

        extraData.scathaPro$setWormLifetimeLeft(
            (float) (Constants.wormLifetime - (TimeUtil.getEpochMilliseconds() - worm.spawnTime)) / 1000f
        );
    }
}