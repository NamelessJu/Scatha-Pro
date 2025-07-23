package namelessju.scathapro.mixins;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;

@Mixin(GuiNewChat.class)
public interface GuiNewChatAccessor
{
    @Accessor("chatLines")
    List<ChatLine> getChatLines$scathapro();
}
