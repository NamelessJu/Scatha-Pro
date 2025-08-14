package namelessju.scathapro.parsing.chestguiparsing;

import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.util.NBTUtil;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.UnicodeSymbol;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;

public class WormBestiaryParser extends ChestGuiParser
{
    private static final int SLOT_GENERIC = 4, SLOT_REGULAR_WORMS = 21, SLOT_SCATHAS = 23;

    @Override
    public String getGuiName()
    {
        return "Crystal Hollows " + UnicodeSymbol.heavyArrowRight + " Worm";
    }

    @Override
    public int[] getSlotNumbers()
    {
        return new int[] {SLOT_GENERIC, SLOT_REGULAR_WORMS, SLOT_SCATHAS};
    }
    
    @Override
    public void tryParse(ItemStack itemStack, int slotNumber, ScathaPro scathaPro)
    {
        switch (slotNumber)
        {
            case SLOT_GENERIC:
                parseGenericSlot(itemStack, scathaPro);
                break;
            
            case SLOT_REGULAR_WORMS:
            case SLOT_SCATHAS:
                parseKills(itemStack, slotNumber, scathaPro);
                break;
        }
    }
    
    private void parseGenericSlot(ItemStack itemStack, ScathaPro scathaPro)
    {
        String magicFindLine = searchLoreWithExpectedIndex(
            itemStack,
            8,
            line -> line.endsWith("Magic Find")
        );
        if (magicFindLine == null) return;
        magicFindLine = magicFindLine.substring(1).split(" ", 2)[0];
        
        Float bestiaryMagicFind = TextUtil.parseFloat(magicFindLine);
        if (bestiaryMagicFind == null) return;
        
        if (scathaPro.variables.wormBestiaryMagicFind != bestiaryMagicFind)
        {
            float bestiaryMagicFindBefore = scathaPro.variables.wormBestiaryMagicFind;
            scathaPro.variables.wormBestiaryMagicFind = bestiaryMagicFind;
            scathaPro.getPersistentData().saveProfileStats();
            scathaPro.getOverlay().updateProfileStats();
            
            TextUtil.sendModChatMessage(EnumChatFormatting.GRAY + "Updated saved worm bestiary Magic Find (" + TextUtil.getObfNrStr(bestiaryMagicFindBefore) + EnumChatFormatting.GRAY + " " + UnicodeSymbol.heavyArrowRight + " " + TextUtil.numberToString(bestiaryMagicFind, 2) + ")");
        }
    }
    
    private void parseKills(ItemStack itemStack, int slotNumber, ScathaPro scathaPro)
    {
        String killsLine = searchLoreWithExpectedIndex(
            itemStack,
            10,
            line -> line.startsWith("Kills: ")
        );
        if (killsLine == null) return;
        killsLine = killsLine.substring(7).replace(",", "");
        
        Integer kills = TextUtil.parseInt(killsLine);
        if (kills == null) return;
        
        switch (slotNumber)
        {
            case SLOT_REGULAR_WORMS:
                if (scathaPro.variables.regularWormKills != kills)
                {
                    int killsBefore = scathaPro.variables.regularWormKills;
                    scathaPro.variables.regularWormKills = kills;
                    scathaPro.getOverlay().updateWormKills();
                    scathaPro.getPersistentData().saveWormKills();
                    TextUtil.sendModChatMessage(EnumChatFormatting.GRAY + "Updated regular worm kills from bestiary (" + TextUtil.getObfNrStr(killsBefore) + " " + UnicodeSymbol.heavyArrowRight + " " + kills + ")");
                }
                break;
            
            case SLOT_SCATHAS:
                if (scathaPro.variables.scathaKills != kills)
                {
                    int killsBefore = scathaPro.variables.scathaKills;
                    scathaPro.variables.scathaKills = kills;
                    
                    if (Math.abs(kills - killsBefore) >= Constants.dryStreakMaxAllowedScathaKillsDeviation)
                    {
                        scathaPro.variables.dropDryStreakInvalidated = true;
                        scathaPro.getPersistentData().savePetDrops();
                    }
                    
                    scathaPro.getPersistentData().saveWormKills();
                    scathaPro.getOverlay().updateScathaKills();
                    scathaPro.getAchievementLogicManager().updateDryStreakAchievements(false);
                    
                    TextUtil.sendModChatMessage(EnumChatFormatting.GRAY + "Updated Scatha kills from bestiary (" + TextUtil.getObfNrStr(killsBefore) + " " + UnicodeSymbol.heavyArrowRight + " " + kills + ")");
                }
                break;
        }
    }
}
