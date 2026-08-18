package namelessju.scathapro.parsing.containerscreenparsing;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.miscellaneous.data.enums.ShardsAttribute;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.UnicodeSymbol;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.math.RoundingMode;

// Note: when the menu page changes, Hypixel opens a new
// ContainerScreen (as the container name shows the page number),
// which allows this parser to re-run
public class AttributesMenuParser extends ContainerScreenParser
{
    private float previousTotalMagicFind;

    public AttributesMenuParser(ScathaPro scathaPro)
    {
        super(scathaPro);
        this.requiresFilledSlots = false;
    }

    @Override
    public boolean shouldParse(String screenTitle)
    {
        return screenTitle.endsWith("Attribute Menu");
    }

    @Override
    public int[] getSlotNumbers()
    {
        int[] slots = new int[28];
        for (int i = 0; i < slots.length; i ++)
        {
            slots[i] = 10 + i + ((i/7) * 2);
        }
        return slots;
    }

    @Override
    public void onStartParsing()
    {
        previousTotalMagicFind = scathaPro.getProfileData().attributes.getMagicFind();
    }

    @Override
    public void tryParse(ItemStack itemStack, int slotNumber)
    {
        for (ShardsAttribute attribute : ShardsAttribute.values())
        {
            tryParseAttribute(itemStack, attribute);
        }
    }

    @Override
    public void onFinishParsing()
    {
        float newTotalMagicFind = scathaPro.getProfileData().attributes.getMagicFind();
        if (!Mth.equal(newTotalMagicFind, previousTotalMagicFind))
        {
            scathaPro.chatManager.sendChatMessage(Component.empty().withColor(TextColor.GRAY)
                .append("Updated Scatha Magic Find from Attributes (")
                .append(TextUtil.numberToComponentOrObf(previousTotalMagicFind, 2, false, RoundingMode.HALF_UP))
                .append(" " + UnicodeSymbol.hypixelArrowRight + " ")
                .append(TextUtil.numberToComponentOrObf(newTotalMagicFind, 2, false, RoundingMode.HALF_UP))
                .append(")")
            );
        }

        scathaPro.persistentData.save();
        scathaPro.mainOverlay.updateProfileStats();
    }

    private void tryParseAttribute(ItemStack itemStack, ShardsAttribute attribute)
    {
        Component name = itemStack.get(DataComponents.CUSTOM_NAME);
        if (name == null) return;
        String unformattedName = name.getString();
        if (!unformattedName.startsWith(attribute.attributeName)) return;

        String[] words = unformattedName.split(" ");
        if (words.length == 0) return;
        int level = switch (words[words.length - 1]) {
            case "I" -> 1;
            case "II" -> 2;
            case "III" -> 3;
            case "IV" -> 4;
            case "V" -> 5;
            case "VI" -> 6;
            case "VII" -> 7;
            case "VIII" -> 8;
            case "IX" -> 9;
            case "X" -> 10;
            default -> -1;
        };

        if (level < 1)
        {
            ScathaPro.LOGGER.error("Failed to parse attribute {}: encountered unexpected level string \"{}\"", attribute.attributeName, words[words.length - 1]);
            return;
        }
        scathaPro.getProfileData().attributes.setUnlocked(attribute, level);
    }
}