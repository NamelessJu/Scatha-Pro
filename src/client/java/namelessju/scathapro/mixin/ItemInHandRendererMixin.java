package namelessju.scathapro.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import namelessju.scathapro.ScathaPro;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin
{
    @Inject(
        method = "renderHandsWithItems",
        at = @At("HEAD"),
        cancellable = true
    )
    private void beforeSubmitHand(float frameInterp, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LocalPlayer player, int lightCoords, CallbackInfo ci)
    {
        if (ScathaPro.getInstance().scathaDropsSlotMachineManager.shouldHideDrops())
        {
            ci.cancel();
        }
    }
}