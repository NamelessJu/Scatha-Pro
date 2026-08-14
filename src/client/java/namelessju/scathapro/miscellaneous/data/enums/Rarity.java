package namelessju.scathapro.miscellaneous.data.enums;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

public enum Rarity
{
    UNKNOWN("Unknown Rarity", TextColor.GRAY, Style.EMPTY.withItalic(true)),
    RARE("Rare", TextColor.BLUE, null),
    EPIC("Epic", TextColor.DARK_PURPLE, null),
    LEGENDARY("Legendary", TextColor.GOLD, null);

    public static final Rarity[] KNOWN_RARITIES = Arrays.stream(values()).filter(rarity -> rarity != UNKNOWN).toArray(Rarity[]::new);

    public final @NonNull String rarityName;
    public final @NonNull Style style;
    public final int color;

    Rarity(@NonNull String rarityName, TextColor color, @Nullable Style style)
    {
        this.rarityName = rarityName;
        this.style = (style != null ? style : Style.EMPTY).withColor(color);
        this.color = ARGB.opaque(color.getValue());
    }

    /**
     * @return The tier string as used by Hypixel in pet item data
     */
    public @NonNull String getTierString()
    {
        return name();
    }
}