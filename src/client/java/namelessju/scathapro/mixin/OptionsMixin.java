package namelessju.scathapro.mixin;


import com.google.common.collect.Lists;
import namelessju.scathapro.ScathaPro;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Options.class)
public abstract class OptionsMixin
{
    @Shadow @Final @Mutable
    public KeyMapping[] keyMappings;
    
    @Inject(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Options;load()V"
        )
    )
    private void injectKeyMappings(CallbackInfo info)
    {
        List<KeyMapping> updatedKeyMappings = Lists.newArrayList(keyMappings);
        updatedKeyMappings.addAll(ScathaPro.getInstance().inputManager.getKeyMappings());
        keyMappings = updatedKeyMappings.toArray(KeyMapping[]::new);
        ScathaPro.LOGGER.debug("Injected key mappings");
    }
}