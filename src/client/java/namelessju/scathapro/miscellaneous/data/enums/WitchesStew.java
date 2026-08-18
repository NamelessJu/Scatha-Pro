package namelessju.scathapro.miscellaneous.data.enums;

import namelessju.scathapro.miscellaneous.data.MagicFindSource;
import namelessju.scathapro.util.UnicodeSymbol;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public enum WitchesStew implements MagicFindSource
{
    SUBTERRANEAN(
        Component.literal(UnicodeSymbol.mobTypeIconSubterranean + " Subterranean").withStyle(ChatFormatting.GOLD),
        "Gemstone Goulash"
    ),
    ELUSIVE(
        Component.literal(UnicodeSymbol.mobTypeIconElusive + " Elusive").withStyle(ChatFormatting.LIGHT_PURPLE),
        "Effervescent Bouillon"
    ),
    SHIELDED(
        Component.literal(UnicodeSymbol.mobTypeIconShielded + " Shielded").withStyle(ChatFormatting.YELLOW),
        "Dusty Bisque"
    );

    public final @NonNull Component mobTypeComponent;
    public final @NonNull String stewName;

    WitchesStew(@NonNull Component mobTypeComponent, @NonNull String stewName)
    {
        this.mobTypeComponent = mobTypeComponent;
        this.stewName = stewName;
    }

    @Override
    public float getMagicFind(int level)
    {
        return 1f;
    }
}