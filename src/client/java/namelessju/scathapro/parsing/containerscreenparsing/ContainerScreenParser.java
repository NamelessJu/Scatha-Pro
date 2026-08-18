package namelessju.scathapro.parsing.containerscreenparsing;

import namelessju.scathapro.ScathaPro;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringDecomposer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;
import java.util.function.Predicate;

public abstract class ContainerScreenParser
{
    protected final ScathaPro scathaPro;

    public boolean enabled = true;
    protected boolean requiresFilledSlots = true;

    public ContainerScreenParser(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
    }

    public abstract boolean shouldParse(String screenTitle);
    public abstract int[] getSlotNumbers();

    public final boolean requiresFilledSlots()
    {
        return requiresFilledSlots;
    }

    public void onStartParsing() {}
    public abstract void tryParse(ItemStack itemStack, int slotNumber);
    public void onFinishParsing() {}

    protected String searchLoreWithExpectedIndex(ItemStack itemStack, int expectedLoreIndex, Predicate<String> linePredicate)
    {
        ItemLore itemLore = itemStack.get(DataComponents.LORE);
        if (itemLore == null) return null;

        List<Component> loreLines = itemLore.lines();
        String line = null;
        int loreIndex = expectedLoreIndex;
        boolean isExpectedIndexCheck = true;
        while (loreIndex < loreLines.size())
        {
            if (isExpectedIndexCheck || loreIndex != expectedLoreIndex)
            {
                line = StringDecomposer.getPlainText(loreLines.get(loreIndex));
                if (linePredicate.test(line)) break;
                line = null;
            }

            if (isExpectedIndexCheck)
            {
                isExpectedIndexCheck = false;
                loreIndex = 0;
                continue;
            }

            loreIndex ++;
        }

        return line;
    }
}