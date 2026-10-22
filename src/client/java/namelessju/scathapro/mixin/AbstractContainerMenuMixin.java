package namelessju.scathapro.mixin;

import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.miscellaneous.data.enums.HypixelEnvironment;
import namelessju.scathapro.util.TextUtil;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.inventory.Slot;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.math.RoundingMode;
import java.util.List;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerMenuMixin
{
    @Shadow @Nullable
    protected Slot hoveredSlot;

    @ModifyArg(
        method = "extractTooltip",
        at = @At(
            value = "INVOKE",
            //? >= 26.3 {
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;Z)V"
            //? } else {
            /*target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V"
            *///? }
        ),
        index = 1
    )
    private List<Component> onExtractLore(List<Component> lore)
    {
        // Modify Scatha pet drop chances in Stoneworm bestiary

        if (hoveredSlot == null || hoveredSlot.getContainerSlot() != 23) return lore;

        Component title = ((AbstractContainerScreen<?>)(Object)this).getTitle();
        if (!Constants.isStonewormBestiary(title.getString())) return lore;

        boolean isAlpha = ScathaPro.instance().hypixelContextManager.environment() == HypixelEnvironment.ALPHA;

        boolean chanceModified = false;
        for (Component line : lore)
        {
            List<Component> siblings = line.getSiblings();
            if (siblings.size() < 5) continue;
            if (!siblings.get(0).getString().equals(" \u25A0 ")
                || !siblings.get(2).getString().startsWith("Scatha")) continue;

            Style scathaWordStyle = siblings.get(2).getStyle();
            int rarityColor = scathaWordStyle.getColor() != null ? scathaWordStyle.getColor().getValue() : -1;
            siblings.set(4, Component.empty().withColor(TextColor.GREEN)
                .append(siblings.get(4).copy()
                    .setStyle(Style.EMPTY.withColor(TextColor.GRAY).withStrikethrough(true))
                )
                .append(" ")
                .append(TextUtil.numberToComponentOrObf(
                    switch (rarityColor)
                    {
                        case 5592575 -> (isAlpha ? Constants.scathaPetBaseChanceRareAlpha : Constants.scathaPetBaseChanceRare) * 100f;
                        case 11141290 -> (isAlpha ? Constants.scathaPetBaseChanceEpicAlpha : Constants.scathaPetBaseChanceEpic) * 100f;
                        case 16755200 -> (isAlpha ? Constants.scathaPetBaseChanceLegendaryAlpha : Constants.scathaPetBaseChanceLegendary) * 100f;
                        default -> -1;
                    },
                    3, true, RoundingMode.HALF_UP
                ))
                .append("%")
                .append(Component.literal("*").withColor(TextColor.GRAY))
            );
            chanceModified = true;
        }

        if (chanceModified)
        {
            lore.add(Component.empty());
            lore.add(Component.literal("*See \"/scathapro petDropChances\"").withColor(TextColor.DARK_GRAY));
        }

        return lore;
    }
}