package namelessju.scathapro.miscellaneous.data.enums;

import namelessju.scathapro.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Objects;

public enum Rarity
{
    UNKNOWN("Unknown Rarity", ChatFormatting.GRAY, ChatFormatting.ITALIC),
    RARE("Rare", ChatFormatting.BLUE),
    EPIC("Epic", ChatFormatting.DARK_PURPLE),
    LEGENDARY("Legendary", ChatFormatting.GOLD);
    
    public static final Rarity[] KNOWN_RARITIES = Arrays.stream(values()).filter(rarity -> rarity != UNKNOWN).toArray(Rarity[]::new);
    
    public final @NonNull String displayName;
    public final @NonNull Style style;
    public final int color;
    
    Rarity(@NonNull String displayName, ChatFormatting color, ChatFormatting @NonNull ... formattings)
    {
        this.displayName = displayName;
        this.style = Style.EMPTY.withColor(color).applyFormats(formattings);
        this.color = ARGB.opaque(Objects.requireNonNullElse(color.getColor(), Util.Color.WHITE));
    }
    
    public @NonNull String getTierString()
    {
        return name();
    }
}