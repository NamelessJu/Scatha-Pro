package namelessju.scathapro.mixin;

import namelessju.scathapro.ScathaPro;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin
{
    @Inject(
        method = "play",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/resources/sounds/SoundInstance;resolve(Lnet/minecraft/client/sounds/SoundManager;)Lnet/minecraft/client/sounds/WeighedSoundEvents;",
            ordinal = 0,
            shift = At.Shift.AFTER
        ),
        cancellable = true
    )
    private void onPlaySound(SoundInstance instance, CallbackInfoReturnable<SoundEngine.PlayResult> cir)
    {
        boolean canPlay = ScathaPro.getInstance().soundParser.handlePlaySound(instance);
        if (!canPlay)
        {
            cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
            cir.cancel();
        }
    }
}
