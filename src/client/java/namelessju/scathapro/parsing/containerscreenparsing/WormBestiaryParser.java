package namelessju.scathapro.parsing.containerscreenparsing;

import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.files.PersistentData;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.UnicodeSymbol;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;

public class WormBestiaryParser extends ContainerScreenParser
{
    private static final int SLOT_GENERIC = 4, SLOT_REGULAR_WORMS = 21, SLOT_SCATHAS = 23, SLOT_RANKING = 50;

    public WormBestiaryParser(ScathaPro scathaPro)
    {
        super(scathaPro);
    }

    @Override
    public boolean shouldParse(String screenTitle)
    {
        return screenTitle.equals("Crystal Hollows " + UnicodeSymbol.hypixelArrowRight + " Stoneworm");
    }

    @Override
    public int[] getSlotNumbers()
    {
        return new int[] {SLOT_GENERIC, SLOT_REGULAR_WORMS, SLOT_SCATHAS, SLOT_RANKING};
    }

    @Override
    public void tryParse(ItemStack itemStack, int slotNumber)
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

            case SLOT_RANKING:
                parseRanking(itemStack, scathaPro);
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
        try
        {
            magicFindLine = magicFindLine.substring(1).split(" ", 2)[0];
        }
        catch (Exception e)
        {
            ScathaPro.LOGGER.error("Worm Bestiary Parser: Found Magic Find entry but failed to parse number! (\"{}\")", magicFindLine);
        }

        Float bestiaryMagicFind = TextUtil.parseFloat(magicFindLine);
        if (bestiaryMagicFind == null) return;

        float currentBestiaryMagicFind = scathaPro.getProfileData().wormBestiaryMagicFind.getOr(-1f);
        if (bestiaryMagicFind >= 0f && bestiaryMagicFind != currentBestiaryMagicFind)
        {
            scathaPro.getProfileData().wormBestiaryMagicFind.set(bestiaryMagicFind);
            scathaPro.persistentData.save();
            scathaPro.mainOverlay.updateProfileStats();

            scathaPro.chatManager.sendChatMessage(Component.empty().withColor(TextColor.GRAY)
                .append("Updated saved Stoneworm bestiary Magic Find (")
                .append(TextUtil.numberToComponentOrObf(currentBestiaryMagicFind))
                .append(" " + UnicodeSymbol.hypixelArrowRight + " " + TextUtil.numberToString(bestiaryMagicFind, 2) + ")")
            );
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
        try
        {
            killsLine = killsLine.substring(7).replace(",", "");
        }
        catch (Exception e)
        {
            ScathaPro.LOGGER.error("Worm Bestiary Parser: Found kills entry but failed to parse number! (\"{}\", slot {})", killsLine, slotNumber);
        }

        Integer kills = TextUtil.parseInt(killsLine);
        if (kills == null || kills < 0) return;

        PersistentData.ProfileData profileData = scathaPro.getProfileData();
        switch (slotNumber)
        {
            case SLOT_REGULAR_WORMS:
                int currentRegularWormKills = profileData.stonewormKills.get();
                if (kills != currentRegularWormKills)
                {
                    profileData.stonewormKills.set(kills);
                    scathaPro.persistentData.save();
                    scathaPro.mainOverlay.updateWormKills();

                    scathaPro.chatManager.sendChatMessage(Component.empty().withColor(TextColor.GRAY)
                        .append("Updated Stoneworm kills from bestiary (")
                        .append(TextUtil.numberToComponentOrObf(currentRegularWormKills))
                        .append(" " + UnicodeSymbol.hypixelArrowRight + " " + TextUtil.numberToString(kills, 2) + ")")
                    );
                }
                break;

            case SLOT_SCATHAS:
                int currentScathaKills = profileData.scathaKills.get();
                if (kills != currentScathaKills)
                {
                    profileData.scathaKills.set(kills);

                    if (Math.abs(kills - currentScathaKills) >= Constants.dryStreakMaxAllowedScathaKillsDeviation)
                    {
                        profileData.isPetDropDryStreakInvalidated.set(true);
                    }

                    scathaPro.persistentData.save();
                    scathaPro.mainOverlay.updateScathaKills();

                    scathaPro.achievementLogicManager.updateDryStreakAchievements(false);

                    scathaPro.chatManager.sendChatMessage(Component.empty().withColor(TextColor.GRAY)
                        .append("Updated Scatha kills from bestiary (")
                        .append(TextUtil.numberToComponentOrObf(currentScathaKills))
                        .append(" " + UnicodeSymbol.hypixelArrowRight + " " + TextUtil.numberToString(kills, 2) + ")")
                    );
                }
                break;
        }
    }

    private void parseRanking(ItemStack itemStack, ScathaPro scathaPro)
    {
        String line = searchLoreWithExpectedIndex(itemStack, 1, ln -> ln.startsWith("#"));
        if (line == null) return;

        line = line.substring(1).split(" ", 2)[0].replace(",", "");
        int newRank = -1;
        try
        {
            newRank = Integer.parseInt(line);
        }
        catch (NumberFormatException ignored) {}
        if (newRank < 1) return;

        int currentRank = scathaPro.getProfileData().bestiaryRank.get();
        if (newRank != currentRank)
        {
            scathaPro.getProfileData().bestiaryRank.set(newRank);
            scathaPro.persistentData.save();
            scathaPro.chatManager.sendChatMessage(Component.empty().withColor(TextColor.GRAY)
                .append("Updated saved Stoneworm bestiary rank (#")
                .append(TextUtil.numberToComponentOrObf(currentRank))
                .append(" " + UnicodeSymbol.hypixelArrowRight + " #" + TextUtil.numberToString(newRank) + ")")
            );
        }
    }
}