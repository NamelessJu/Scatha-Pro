package namelessju.scathapro.mixin;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.events.ScathaProEvents;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin
{
    @Inject(
        method = "useItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;ensureHasSentCarriedItem()V"
        )
    )
    private void onUseItem(Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir)
    {
        if (player instanceof LocalPlayer localPlayer)
        {
            ScathaProEvents.useItemEvent.trigger(ScathaPro.getInstance(), new ScathaProEvents.UseItemEventData(
                localPlayer, localPlayer.getItemInHand(interactionHand)
            ));
        }
    }
}
