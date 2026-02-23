package namelessju.scathapro.mixin;

import namelessju.scathapro.ScathaPro;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin
{
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
}
