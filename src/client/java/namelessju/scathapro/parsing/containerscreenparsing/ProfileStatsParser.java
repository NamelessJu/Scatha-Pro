package namelessju.scathapro.parsing.containerscreenparsing;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.files.PersistentData;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.UnicodeSymbol;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringDecomposer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;

public class ProfileStatsParser extends ContainerScreenParser
{
    public ProfileStatsParser()
    {
        this.enabled = false;
    }
    
    @Override
    public String getScreenTitle()
    {
        return "Your Equipment and Stats";
    }
    
    @Override
    public int[] getSlotNumbers()
    {
        return new int[] {25};
    }
    
    @Override
    public void tryParse(ItemStack itemStack, int slotNumber, ScathaPro scathaPro)
    {
        ItemLore itemLore = itemStack.get(DataComponents.LORE);
        if (itemLore == null) return;
        List<Component> loreLines = itemLore.lines();
        
        boolean magicFindFound = false;
        boolean petLuckFound = false;
        
        for (int i = 3; i < loreLines.size() && (!magicFindFound || !petLuckFound); i ++)
        {
            String loreLine = StringDecomposer.getPlainText(loreLines.get(i));
            
            // " # Magic Find 123"
            if (loreLine.length() >= 15 && loreLine.startsWith("Magic Find", 3))
            {
                magicFindFound = true;
                
                float magicFind;
                try
                {
                    loreLine = loreLine.substring(14).replace(",", "");
                    magicFind = Float.parseFloat(loreLine);
                }
                catch (Exception e)
                {
                    ScathaPro.LOGGER.error("Profile Stats Parser: Found Magic Find entry but failed to parse number! (\"{}\")", loreLine);
                    continue;
                }
                
                float currentMagicFind = scathaPro.getProfileData().magicFind.getOr(-1f);
                if (magicFind >= 0f && magicFind != currentMagicFind)
                {
                    scathaPro.getProfileData().magicFind.set(magicFind);
                    scathaPro.persistentData.save();
                    scathaPro.mainOverlay.updateProfileStats();
                    
                    scathaPro.chatManager.sendChatMessage(Component.empty().withStyle(ChatFormatting.GRAY)
                        .append("Updated saved Magic Find (")
                        .append(TextUtil.numberToComponentOrObf(currentMagicFind))
                        .append(" " + UnicodeSymbol.heavyArrowRight + " " + TextUtil.numberToString(magicFind, 2) + ")")
                    );
                }
            }
            
            // " # Pet Luck 123"
            else if (loreLine.length() >= 13 && loreLine.startsWith("Pet Luck", 3))
            {
                petLuckFound = true;
                
                float petLuck;
                try
                {
                    loreLine = loreLine.substring(12).replace(",", "");
                    petLuck = Float.parseFloat(loreLine);
                }
                catch (Exception e)
                {
                    ScathaPro.LOGGER.error("Profile Stats Parser: Found Pet Luck entry but failed to parse number! (\"{}\")", loreLine);
                    continue;
                }
                
                float currentPetLuck = scathaPro.getProfileData().petLuck.getOr(-1f);
                if (petLuck >= 0f && petLuck != currentPetLuck)
                {
                    scathaPro.getProfileData().petLuck.set(petLuck);
                    scathaPro.persistentData.save();
                    scathaPro.mainOverlay.updateProfileStats();
                    
                    scathaPro.chatManager.sendChatMessage(Component.empty().withStyle(ChatFormatting.GRAY)
                        .append("Updated saved Pet Luck (")
                        .append(TextUtil.numberToComponentOrObf(currentPetLuck))
                        .append(" " + UnicodeSymbol.heavyArrowRight + " " + TextUtil.numberToString(petLuck, 2) + ")")
                    );
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
