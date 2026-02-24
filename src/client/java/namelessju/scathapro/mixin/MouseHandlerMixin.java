package namelessju.scathapro.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import namelessju.scathapro.ScathaPro;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.OptionInstance;
import net.minecraft.util.SmoothDouble;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin
{
    @Shadow @Final private SmoothDouble smoothTurnX;
    @Shadow @Final private SmoothDouble smoothTurnY;
    
    @Inject(
        method = "turnPlayer",
        at = @At("HEAD"),
        cancellable = true
    )
    private void preventPlayerRotation(double d, CallbackInfo ci)
    {
        if (ScathaPro.getInstance().inputManager.isCameraRotationLocked())
        {
            smoothTurnX.reset();
            smoothTurnY.reset();
            ci.cancel();
        }
    }

    @WrapOperation(
        method = "turnPlayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;",
            ordinal = 0
        )
    )
    private Object modifySensitivity(OptionInstance<?> instance, Operation<?> original)
    {
        if (ScathaPro.getInstance().inputManager.isAlternativeSensitivityEnabled())
        {
            return (double) ScathaPro.getInstance().config.miscellaneous.alternativeSensitivity.get();
        }
        return original.call(instance);
    }
}
