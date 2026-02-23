package namelessju.scathapro.mixin;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.util.TextUtil;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.util.StringDecomposer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin
{
    @Inject(
        method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
        at = @At("HEAD"),
        order = -10000, // inject before @ModifyVariable mixins!
        cancellable = true
    )
    private void beforeAddMessage(Component message, MessageSignature messageSignature, GuiMessageTag guiMessageTag, CallbackInfo ci)
    {
        if (ScathaPro.getInstance().chatManager.shouldCancelMessage(message))
        {
            ci.cancel();
            if (ScathaPro.LOGGER.isDebugEnabled())
            {
                ScathaPro.LOGGER.debug("Cancelled message: \"{}\"", StringDecomposer.getPlainText(message));
            }
        }
    }
    
    @ModifyVariable(
        method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
        at = @At("HEAD"),
        order = -9999,
        argsOnly = true,
        ordinal = 0
    )
    private Component modifyMessageEarly(Component message)
    {
        return ScathaPro.getInstance().chatManager.onMessageAddedEarly(message);
    }
    
    @ModifyVariable(
        method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
        at = @At("HEAD"),
        order = 9999,
        argsOnly = true,
        ordinal = 0
    )
    private Component modifyMessageLate(Component message)
    {
        return ScathaPro.getInstance().chatManager.onMessageAddedLate(message);
    }
}
