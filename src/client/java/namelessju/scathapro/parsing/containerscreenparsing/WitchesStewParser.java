package namelessju.scathapro.parsing.containerscreenparsing;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.files.PersistentData;
import namelessju.scathapro.miscellaneous.data.enums.WitchesStew;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.UnicodeSymbol;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import net.minecraft.util.StringDecomposer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.math.RoundingMode;
import java.util.ListIterator;
import java.util.Locale;

// Note: this parser (as the other ones) only gets run
// when the screen opens, but not when the filter changes.
// This shouldn't be a problem however, as AFAIK the menu
// always opens unfiltered and should therefore always be
// able to detect all stews!
public class WitchesStewParser extends ContainerScreenParser
{
    private float previousTotalMagicFind;

    public WitchesStewParser(ScathaPro scathaPro)
    {
        super(scathaPro);
        this.requiresFilledSlots = false;
    }

    @Override
    public boolean shouldParse(String screenTitle)
    {
        return screenTitle.equals("Witches Stew");
    }

    @Override
    public int[] getSlotNumbers()
    {
        int[] slots = new int[21];
        for (int i = 0; i < slots.length; i ++)
        {
            slots[i] = 10 + i + ((i/7) * 2);
        }
        return slots;
    }

    @Override
    public void onStartParsing()
    {
        previousTotalMagicFind = scathaPro.getProfileData().witchesStewsEaten.getMagicFind();
    }

    @Override
    public void tryParse(ItemStack itemStack, int slotNumber)
    {
        for (WitchesStew stew : WitchesStew.values())
        {
            tryParseStew(itemStack, stew);
        }
    }

    @Override
    public void onFinishParsing()
    {
        float newTotalMagicFind = scathaPro.getProfileData().witchesStewsEaten.getMagicFind();
        if (!Mth.equal(newTotalMagicFind, previousTotalMagicFind))
        {
            scathaPro.chatManager.sendChatMessage(Component.empty().withColor(TextColor.GRAY)
                .append("Updated Scatha Magic Find from Witches Stews (")
                .append(TextUtil.numberToComponentOrObf(previousTotalMagicFind, 2, false, RoundingMode.HALF_UP))
                .append(" " + UnicodeSymbol.hypixelArrowRight + " ")
                .append(TextUtil.numberToComponentOrObf(newTotalMagicFind, 2, false, RoundingMode.HALF_UP))
                .append(")")
            );
        }

        scathaPro.persistentData.save();
        scathaPro.mainOverlay.updateProfileStats();
    }

    private void tryParseStew(ItemStack itemStack, WitchesStew stew)
    {
        Component name = itemStack.get(DataComponents.CUSTOM_NAME);
        if (name == null || !stew.stewName.equals(StringDecomposer.getPlainText(name).trim()))
        {
            return;
        }

        ItemLore lore = itemStack.get(DataComponents.LORE);
        if (lore != null)
        {
            ListIterator<Component> iterator = lore.lines().listIterator(lore.lines().size());
            boolean eaten = false;
            while (iterator.hasPrevious())
            {
                Component line = iterator.previous();
                if ("you've already eaten this stew!".equals(StringDecomposer.getPlainText(line).trim().toLowerCase(Locale.ROOT)))
                {
                    eaten = true;
                    break;
                }
            }

            PersistentData.ProfileData profileData = scathaPro.getProfileData();
            if (eaten) profileData.witchesStewsEaten.setUnlocked(stew);
            else profileData.witchesStewsEaten.removeUnlocked(stew);
        }
    }
}