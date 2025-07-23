package namelessju.scathapro.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import namelessju.scathapro.ScathaPro;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S03PacketTimeUpdate;

@Mixin(NetHandlerPlayClient.class)
public class NetHandlerPlayClientMixin
{
    @Inject(method = "handleTimeUpdate", at=@At("HEAD"))
    public void injectHandleTimeUpdate(S03PacketTimeUpdate packetIn, CallbackInfo callbackInfo)
    {
        ScathaPro scathaPro = ScathaPro.getInstance();
        if (scathaPro.isInCrystalHollows() && scathaPro.variables.lastCrystalHollowsDay == -1)
        {
            scathaPro.logDebug("First Crystal Hollows time update packet received");
            scathaPro.variables.lastCrystalHollowsDay = -2;
        }
    }
}
