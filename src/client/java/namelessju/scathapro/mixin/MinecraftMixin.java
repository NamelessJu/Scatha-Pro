package namelessju.scathapro.mixin;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.events.ScathaProEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin
{
    @Shadow @Nullable
    public LocalPlayer player;

    @Unique
    private boolean wasInLevelBefore = false;

    @Inject(
        method = "onGameLoadFinished",
        at = @At("RETURN")
    )
    private void afterGameLoadFinished(CallbackInfo ci)
    {
        ScathaPro.getInstance().onMinecraftLoaded();
    }

    @Inject(
        method = "tick",
        at = @At("RETURN")
    )
    private void afterTick(CallbackInfo ci)
    {
        ScathaPro.getInstance().tick();
    }

    @Inject(
        method = "startAttack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"
        )
    )
    private void onAttack(CallbackInfoReturnable<Boolean> cir)
    {
        assert player != null;
        ScathaPro.getInstance().coreManager.projectileWormHitDetector.checkShortbowHitFired(player.getItemInHand(InteractionHand.MAIN_HAND));
    }

    @Inject(
        method = "updateLevelInEngines(Lnet/minecraft/client/multiplayer/ClientLevel;Z)V",
        at = @At("TAIL")
    )
    private void afterLevelChange(ClientLevel level, boolean stopSound, CallbackInfo ci)
    {
        if (wasInLevelBefore) ScathaProEvents.worldLeftEvent.trigger(ScathaPro.getInstance());
        wasInLevelBefore = level != null;
    }
}