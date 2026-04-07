package namelessju.scathapro.mixin;

import namelessju.scathapro.ScathaPro;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
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
        method = "addMessage",
        at = @At("HEAD"),
        order = -10000, // inject before @ModifyVariable mixins!
        cancellable = true
    )
    private void beforeAddMessage(Component contents, MessageSignature signature, GuiMessageSource source, GuiMessageTag tag, CallbackInfo ci)
    {
        if (ScathaPro.getInstance().chatParser.shouldCancelMessage(contents))
        {
            ci.cancel();
            if (ScathaPro.LOGGER.isDebugEnabled())
            {
                ScathaPro.LOGGER.debug("Cancelled message: \"{}\"", StringDecomposer.getPlainText(contents));
            }
        }
    }
    
    @ModifyVariable(
        method = "addMessage",
        at = @At("HEAD"),
        order = -9999,
        argsOnly = true,
        name = "contents"
    )
    private Component modifyMessageEarly(Component contents)
    {
        return ScathaPro.getInstance().chatParser.beforeMessageAddedEarly(contents);
    }
    
    @ModifyVariable(
        method = "addMessage",
        at = @At("HEAD"),
        order = 9999,
        argsOnly = true,
        name = "contents"
    )
    private Component modifyMessageLate(Component contents)
    {
        return ScathaPro.getInstance().chatParser.beforeMessageAddedLate(contents);
    }
}
