package namelessju.scathapro.mixin;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.events.ScathaProEvents;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class ConnectionMixin
{
    @Inject(
        method = "disconnect(Lnet/minecraft/network/DisconnectionDetails;)V",
        at = @At("HEAD")
    )
    private void onDisconnect(DisconnectionDetails details, CallbackInfo ci)
    {
        if (!isConnected()) return;

        ScathaProEvents.serverLeftEvent.trigger(ScathaPro.instance());
    }

    @Shadow
    public abstract boolean isConnected();
}