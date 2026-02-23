package namelessju.scathapro.mixin;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.events.ScathaProEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin
{
    @Inject(
        method = "addEntity",
        at = @At("HEAD")
    )
    private void afterAddEntity(Entity entity, CallbackInfo ci)
    {
        if (entity instanceof LocalPlayer)
        {
            ScathaProEvents.playerAddedToWorldEvent.trigger(ScathaPro.getInstance());
        }
    }
}
