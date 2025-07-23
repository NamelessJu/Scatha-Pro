package namelessju.scathapro.parsing.chestguiparsing;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.util.NBTUtil;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.UnicodeSymbol;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;

public class ProfileStatsParser extends ChestGuiParser
{
    public ProfileStatsParser()
    {
        this.enabled = false;
    }
    
    @Override
    public String getGuiName()
    {
        return "SkyBlock Menu";
    }
    
    @Override
    public int[] getSlotNumbers()
    {
        return new int[] {13};
    }
    
    @Override
    public void tryParse(ItemStack itemStack, int slotNumber, ScathaPro scathaPro)
    {
        NBTTagList lore = NBTUtil.getLore(itemStack);
        if (lore == null) return;
        
        boolean magicFindFound = false;
        boolean petLuckFound = false;
        
        for (int i = 3; i < lore.tagCount() && (!magicFindFound || !petLuckFound); i ++)
        {
            String loreLine = StringUtils.stripControlCodes(lore.getStringTagAt(i));
            
            // " # Magic Find 123"
            if (loreLine.length() >= 15 && loreLine.substring(3, 13).equals("Magic Find"))
            {
                magicFindFound = true;
                
                loreLine = loreLine.substring(14).replace(",", "");
                float magicFind;
                try
                {
                    magicFind = Float.parseFloat(loreLine);
                }
                catch (NumberFormatException e)
                {
                    scathaPro.logError("Profile Stats Parser: Found Magic Find entry but failed to parse number! (\"" + loreLine + "\")");
                    continue;
                }
                
                if (magicFind != scathaPro.variables.magicFind)
                {
                    TextUtil.sendModChatMessage(EnumChatFormatting.GRAY + "Updated Magic Find (" + TextUtil.getObfNrStr(scathaPro.variables.magicFind) + " " + UnicodeSymbol.heavyArrowRight + " " + TextUtil.numberToString(magicFind, 2) + ")");
                    scathaPro.variables.magicFind = magicFind;
                    scathaPro.getPersistentData().saveProfileStats();
                    scathaPro.getOverlay().updateProfileStats();
                }
            }
            
            // " # Pet Luck 123"
            else if (loreLine.length() >= 13 && loreLine.substring(3, 11).equals("Pet Luck"))
            {
                petLuckFound = true;
                
                loreLine = loreLine.substring(12).replace(",", "");
                float petLuck;
                try
                {
                    petLuck = Float.parseFloat(loreLine);
                }
                catch (NumberFormatException e)
                {
                    scathaPro.logError("Profile Stats Parser: Found Pet Luck entry but failed to parse number! (\"" + loreLine + "\")");
                    continue;
                }
                
                if (petLuck != scathaPro.variables.petLuck)
                {
                    TextUtil.sendModChatMessage(EnumChatFormatting.GRAY + "Updated Pet Luck (" + TextUtil.getObfNrStr(scathaPro.variables.petLuck) + " " + UnicodeSymbol.heavyArrowRight + " " + TextUtil.numberToString(petLuck, 2) + ")");
                    scathaPro.variables.petLuck = petLuck;
                    scathaPro.getPersistentData().saveProfileStats();
                    scathaPro.getOverlay().updateProfileStats();
                }
            }
        }
    }
    
    @Override
    public void onFinishParsing()
    {
        this.enabled = false;
    }
}
