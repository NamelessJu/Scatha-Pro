package namelessju.scathapro.parsing.chestguiparsing;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.util.NBTUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.StringUtils;

import java.util.function.Predicate;

public abstract class ChestGuiParser
{
    public boolean enabled = true;
    
    public abstract String getGuiName();
    public abstract int[] getSlotNumbers();
    
    public abstract void tryParse(ItemStack itemStack, int slotNumber, ScathaPro scathaPro);
    
    public void onStartParsing() {}
    public void onFinishParsing() {}
    
    protected String searchLoreWithExpectedIndex(ItemStack itemStack, int expectedLoreIndex, Predicate<String> linePredicate)
    {
        NBTTagList itemLore = NBTUtil.getLore(itemStack);
        if (itemLore == null) return null;
        
        String line = null;
        
        int loreIndex = expectedLoreIndex;
        boolean isExpectedIndexCheck = true;
        while (loreIndex < itemLore.tagCount())
        {
            if (isExpectedIndexCheck || loreIndex != expectedLoreIndex)
            {
                line = StringUtils.stripControlCodes(itemLore.getStringTagAt(loreIndex));
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
