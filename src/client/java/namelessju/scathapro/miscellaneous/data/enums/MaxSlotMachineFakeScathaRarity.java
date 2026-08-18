package namelessju.scathapro.miscellaneous.data.enums;

import namelessju.scathapro.miscellaneous.data.IDisplayable;
import net.minecraft.network.chat.CommonComponents;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public enum MaxSlotMachineFakeScathaRarity implements IDisplayable
{
    NONE(null), RARE(Rarity.RARE), EPIC(Rarity.EPIC), LEGENDARY(Rarity.LEGENDARY);

    private final @Nullable Rarity rarity;

    MaxSlotMachineFakeScathaRarity(@Nullable Rarity rarity)
    {
        this.rarity = rarity;
    }

    @Override
    public String getDisplayName()
    {
        return rarity != null ? rarity.rarityName : CommonComponents.OPTION_OFF.getString();
    }
}