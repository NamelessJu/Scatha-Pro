package namelessju.scathapro.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import namelessju.scathapro.ScathaPro;
import net.minecraft.client.renderer.special.PlayerHeadSpecialRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PlayerHeadSpecialRenderer.class)
public class PlayerHeadSpecialRendererMixin
{
    @ModifyArg(
        method = "extractArgument(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/renderer/PlayerSkinRenderCache$RenderInfo;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/PlayerSkinRenderCache;getOrDefault(Lnet/minecraft/world/item/component/ResolvableProfile;)Lnet/minecraft/client/renderer/PlayerSkinRenderCache$RenderInfo;",
            ordinal = 0
        )
    )
    private ResolvableProfile replaceProfile(ResolvableProfile profile, @Local(argsOnly = true, name = "stack") ItemStack stack)
    {
        return ScathaPro.getInstance().playerHeadRenderingReplacementManager.replaceItemProfile(profile, stack);
    }
}