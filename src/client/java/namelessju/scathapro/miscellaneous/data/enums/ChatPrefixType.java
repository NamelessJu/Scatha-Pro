package namelessju.scathapro.miscellaneous.data.enums;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.miscellaneous.data.CachedComponentProvider;
import namelessju.scathapro.miscellaneous.data.IDisplayable;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.UnicodeSymbol;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import org.jspecify.annotations.NonNull;

import java.util.function.Function;

public enum ChatPrefixType implements IDisplayable
{
    FULL_NAME_BRACKETS("Full Name (Brackets)",
        scathaPro -> Component.literal("[" + scathaPro.getModDisplayName() + "] ").withStyle(ChatFormatting.GRAY)
    ),
    FULL_NAME_COLON("Full Name (Colon)",
        scathaPro -> Component.literal(scathaPro.getModDisplayName() + ": ").withStyle(ChatFormatting.GRAY)
    ),
    ACRONYM_BRACKETS("Abbreviated (Brackets)",
        new CachedComponentProvider(Component.literal("[SP] ").withStyle(ChatFormatting.GRAY))
    ),
    ACRONYM_COLON("Abbreviated (Colon)",
        new CachedComponentProvider(Component.literal("SP: ").withStyle(ChatFormatting.GRAY))
    ),
    ICON_BRACKETS("Icon (Brackets)",
        scathaPro -> Component.empty().withStyle(ChatFormatting.DARK_GRAY)
            .append("[").append(getIconComponent(scathaPro)).append("] ")
    ),
    ICON_NO_DECORATION("Icon (Undecorated)",
        scathaPro -> Component.empty().append(getIconComponent(scathaPro)).append(" ")
    ),;

    public static Component getIconComponent(ScathaPro scathaPro)
    {
        return Component.literal(String.valueOf(UnicodeSymbol.modIcon)).withStyle(
            TextUtil.ICON_STYLE.withHoverEvent(
                new HoverEvent.ShowText(Component.literal(scathaPro.getModDisplayName()).withStyle(ChatFormatting.GRAY))
            )
        );
    }

    private final @NonNull String displayName;
    private final Function<@NonNull ScathaPro, @NonNull Component> componentFunction;

    ChatPrefixType(@NonNull String displayName, Function<@NonNull ScathaPro, @NonNull Component> componentFunction)
    {
        this.displayName = displayName;
        this.componentFunction = componentFunction;
    }

    @Override
    public @NonNull String getDisplayName()
    {
        return displayName;
    }

    public @NonNull Component getPrefix(@NonNull ScathaPro scathaPro)
    {
        return componentFunction.apply(scathaPro);
    }
}