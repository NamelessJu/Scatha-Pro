package namelessju.scathapro.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.miscellaneous.data.mixindata.IArmorStandRenderStateData;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.item.component.ResolvableProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(CustomHeadLayer.class)
public abstract class CustomHeadLayerMixin
{
    @ModifyArg(
        method = "resolveSkullRenderType",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/PlayerSkinRenderCache;getOrDefault(Lnet/minecraft/world/item/component/ResolvableProfile;)Lnet/minecraft/client/renderer/PlayerSkinRenderCache$RenderInfo;",
            ordinal = 0
        )
    )
    private ResolvableProfile replaceProfile(ResolvableProfile profile, @Local(argsOnly = true, name = "state") final LivingEntityRenderState state)
    {
        if (state instanceof IArmorStandRenderStateData extraData)
        {
            return ScathaPro.getInstance().playerHeadRenderingReplacementManager.replaceEntityProfile(profile, extraData);
        }

        return profile;
    }
}