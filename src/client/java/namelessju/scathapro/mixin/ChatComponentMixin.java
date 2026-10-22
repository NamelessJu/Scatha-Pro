package namelessju.scathapro.mixin;

import namelessju.scathapro.ScathaPro;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin
{
    @ModifyVariable(
        method = "addMessage",
        at = @At("HEAD"),
        order = 9999,
        argsOnly = true,
        name = "contents"
    )
    private Component modifyMessageLate(Component contents)
    {
        return ScathaPro.instance().chatParser.beforeMessageAddedLate(contents);
    }
}