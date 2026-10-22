package namelessju.scathapro.mixin;

import namelessju.scathapro.ScathaPro;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringDecomposer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatListener.class)
public class ChatListenerMixin
{
    @Inject(
        method = "handleSystemMessage",
        at = @At("HEAD"),
        order = -10000, // inject before @ModifyVariable mixin to prevent unnecessary parsing
        cancellable = true
    )
    private void cancelMessage(Component message, boolean remote, CallbackInfo ci)
    {
        if (ScathaPro.instance().chatParser.shouldCancelSystemMessage(message))
        {
            ci.cancel();
            if (ScathaPro.LOGGER.isDebugEnabled())
            {
                ScathaPro.LOGGER.debug("Cancelled message: \"{}\"", StringDecomposer.getPlainText(message));
            }
        }
    }

    @ModifyVariable(
        method = "handleSystemMessage",
        at = @At("HEAD"),
        order = -9999,
        argsOnly = true,
        name = "message"
    )
    private Component modifyMessageEarly(Component message)
    {
        return ScathaPro.instance().chatParser.beforeHandleSystemMessage(message);
    }
}