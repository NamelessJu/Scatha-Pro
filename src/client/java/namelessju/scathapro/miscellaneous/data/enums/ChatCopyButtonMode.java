package namelessju.scathapro.miscellaneous.data.enums;

import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.UnicodeSymbol;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

public enum ChatCopyButtonMode
{
    COPY_TO_CLIPBOARD("Copy To Clipboard",
        messageText -> Component.literal(UnicodeSymbol.clipboard)
            .withStyle(ChatFormatting.DARK_GRAY)
            .withStyle(Style.EMPTY
                .withHoverEvent(new HoverEvent.ShowText(
                    Component.literal("Click to copy message\ntext into clipboard").withStyle(ChatFormatting.GRAY)
                ))
                .withClickEvent(new ClickEvent.CopyToClipboard(messageText))
            )
    ),
    SUGGEST_MESSAGE("Suggest Message",
        messageText -> Component.literal(String.valueOf(UnicodeSymbol.writingHand))
            .withStyle(ChatFormatting.DARK_GRAY)
            .withStyle(Style.EMPTY
                .withHoverEvent(new HoverEvent.ShowText(
                    Component.literal("Click to copy message\ntext into chat input").withStyle(ChatFormatting.GRAY)
                ))
                .withClickEvent(new ClickEvent.SuggestCommand(messageText.replaceAll(TextUtil.NEW_LINE_REGEX + "+", " ")))
            )
    );
    
    public final @NonNull String displayName;
    public final @NonNull Function<String, Component> buttonComponentBuilder;
    
    ChatCopyButtonMode(@NonNull String displayName, @NonNull Function<String, Component> buttonComponentBuilder)
    {
        this.displayName = displayName;
        this.buttonComponentBuilder = buttonComponentBuilder;
    }
    
    @Override
    public String toString()
    {
        return displayName;
    }
}
