package namelessju.scathapro.miscellaneous.data.enums;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import org.jspecify.annotations.NonNull;

public enum Rarity
{
    UNKNOWN("Unknown Rarity", ChatFormatting.GRAY, ChatFormatting.ITALIC),
    RARE("Rare", ChatFormatting.BLUE),
    EPIC("Epic", ChatFormatting.DARK_PURPLE),
    LEGENDARY("Legendary", ChatFormatting.GOLD);
    
    public final @NonNull String displayName;
    public final @NonNull Style style;
    
    Rarity(@NonNull String displayName, ChatFormatting @NonNull ... formattings)
    {
        this.displayName = displayName;
        this.style = Style.EMPTY.applyFormats(formattings);
    }
}