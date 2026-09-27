package namelessju.scathapro.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import namelessju.scathapro.ScathaPro;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >= 26.3 {
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
//? } else {
/*import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;

@Mixin(ItemInHandRenderer.class)
*///? }
public abstract class FirstPersonHandsAndItemsRendererMixin
{
    @Inject(
        //? if >= 26.2 {
        method = "submitHandsWithItems",
        //? } else {
        /*method = "renderHandsWithItems",
        *///? }
        at = @At("HEAD"),
        cancellable = true
    )
    private void beforeSubmitHandsWithItems(
        //? if >= 26.3 {
        float partialTicks, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState state, CallbackInfo ci
        //? } else {
        /*float frameInterp, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LocalPlayer player, int lightCoords, CallbackInfo ci
         *///? }
    )
    {
        if (ScathaPro.instance().scathaDropsGamblingReelManager.shouldHideDrops())
        {
            ci.cancel();
        }
    }
}