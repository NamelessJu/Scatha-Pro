package namelessju.scathapro.parsing.chestguiparsing;

import namelessju.scathapro.ScathaPro;
import net.minecraft.item.ItemStack;

public abstract class ChestGuiParser
{
    public boolean enabled = true;
    
    public abstract String getGuiName();
    public abstract int[] getSlotNumbers();
    
    public abstract void tryParse(ItemStack itemStack, int slotNumber, ScathaPro scathaPro);
    
    public void onStartParsing() {}
    public void onFinishParsing() {}
}
