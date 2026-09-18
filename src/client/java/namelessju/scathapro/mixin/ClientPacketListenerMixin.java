package namelessju.scathapro.mixin;

import namelessju.scathapro.ScathaPro;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin
{
    @Inject(
        method = "sendCommand",
        at = @At("HEAD")
    )
    private void onSendCommand(String command, CallbackInfo ci)
    {
        if (command.toLowerCase(Locale.ROOT).split(" ", 2)[0].startsWith("profileid"))
        {
            ScathaPro.LOGGER.debug("\"/profileid\" sent");
            ScathaPro.instance().hypixelContextManager.cancelProfileIdCommandTimer();
        }
    }
}