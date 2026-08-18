package namelessju.scathapro.managers.detectors;

import com.google.gson.JsonObject;
import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.events.ScathaProEvents;
import namelessju.scathapro.miscellaneous.data.ScathaPetDrop;
import namelessju.scathapro.miscellaneous.data.enums.Rarity;
import namelessju.scathapro.util.JsonUtil;
import namelessju.scathapro.util.SkyBlockItemUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ScathaDropsDetector
{
    private final ScathaPro scathaPro;

    private HashMap<Rarity, Integer> previousScathaPets = null;
    private int previousBlockBrans = -1;
    /** Stores newly received drops for a short time while waiting for a Scatha kill getting detected */
    private final List<Drop> cachedDrops = new ArrayList<>();

    public ScathaDropsDetector(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
    }

    public void detect(LocalPlayer player, long now)
    {
        HashMap<Rarity, Integer> currentScathaPets = new HashMap<>();
        AtomicInteger blockBrans = new AtomicInteger();

        NonNullList<ItemStack> nonEquipmentItems = player.getInventory().getNonEquipmentItems();
        for (int i = 0; i < nonEquipmentItems.size(); i++)
        {
            if (i == 8) continue; // No need to check the Skyblock menu

            ItemStack item = nonEquipmentItems.get(i);
            SkyBlockItemUtil.getData(item, skyBlockData -> {
                String skyBlockId = skyBlockData.getString(SkyBlockItemUtil.KEY_ID).orElse(null);
                if (skyBlockId == null) return;

                if (skyBlockId.equals(Constants.ItemID.blockBran))
                {
                    blockBrans.getAndAdd(item.count());
                    return;
                }

                if (!skyBlockId.equals(Constants.ItemID.pet)) return;
                skyBlockData.getString(SkyBlockItemUtil.KEY_PET_INFO).ifPresent(petInfo -> {
                    if (JsonUtil.parseJson(petInfo) instanceof JsonObject petInfoParsed)
                    {
                        String petType = JsonUtil.getString(petInfoParsed, "type");
                        if (petType == null || !petType.equals("SCATHA")) return;

                        String petTier = JsonUtil.getString(petInfoParsed, "tier");
                        Rarity rarity = Rarity.UNKNOWN;
                        if (petTier != null)
                        {
                            for (Rarity knownRarity : Rarity.KNOWN_RARITIES)
                            {
                                if (knownRarity.getTierString().equals(petTier))
                                {
                                    rarity = knownRarity;
                                    break;
                                }
                            }
                        }

                        currentScathaPets.compute(rarity, (_, currentRarityAmount)
                            -> (currentRarityAmount != null ? currentRarityAmount : 0) + item.getCount());
                    }
                });
            });
        }

        boolean killedScathaRecently = scathaPro.coreManager.lastScathaKillTime >= 0 && now - scathaPro.coreManager.lastScathaKillTime < Constants.pingThreshold;

        if (blockBrans.get() != previousBlockBrans)
        {
            if (previousBlockBrans != -1 && blockBrans.get() > previousBlockBrans)
            {
                cachedDrops.add(new ExtraItemDrop(
                    ScathaProEvents.ScathaExtraDropEventData.ItemType.DWARVEN_OS_BLOCK_BRAN, now
                ));
            }
            previousBlockBrans = blockBrans.get();
        }

        if (previousScathaPets != null)
        {
            Rarity newScathaPetRarity = null;
            for (Rarity rarity : currentScathaPets.keySet())
            {
                int currentRarityCount = currentScathaPets.get(rarity);
                Integer previousRarityCount = previousScathaPets.get(rarity);
                int difference = currentRarityCount - (previousRarityCount != null ? previousRarityCount : 0);
                if (difference > 0 && (newScathaPetRarity == null || rarity.ordinal() > newScathaPetRarity.ordinal()))
                {
                    newScathaPetRarity = rarity;
                }
            }
            if (newScathaPetRarity != null)
            {
                cachedDrops.add(new PetDrop(new ScathaPetDrop(newScathaPetRarity), now));
            }
        }
        previousScathaPets = currentScathaPets;

        for (int i = cachedDrops.size() - 1; i >= 0; i --) {
            Drop drop = cachedDrops.get(i);

            if (now - drop.dropTime >= Constants.pingThreshold)
            {
                cachedDrops.remove(i);
                continue;
            }

            if (killedScathaRecently)
            {
                cachedDrops.remove(i);
                drop.resultAction.accept(scathaPro);
            }
        }
    }

    public void reset()
    {
        previousScathaPets = null;
        previousBlockBrans = -1;
        cachedDrops.clear();
    }

    private static abstract class Drop
    {
        public final long dropTime;
        public final Consumer<ScathaPro> resultAction;

        public Drop(long dropTime, Consumer<ScathaPro> resultAction)
        {
            this.dropTime = dropTime;
            this.resultAction = resultAction;
        }
    }

    private static class PetDrop extends Drop
    {
        public PetDrop(ScathaPetDrop scathaPetDrop, long dropTime)
        {
            super(dropTime, scathaPro -> {
                if (scathaPro.config.miscellaneous.dropsSlotMachineEnabled.get())
                {
                    scathaPro.scathaDropsSlotMachineManager.setPetDrop(scathaPetDrop);
                }
                else scathaPetDrop.trigger(scathaPro);
            });
        }
    }

    private static class ExtraItemDrop extends Drop
    {
        public ExtraItemDrop(ScathaProEvents.ScathaExtraDropEventData.ItemType type, long dropTime)
        {
            super(dropTime, scathaPro -> ScathaProEvents.scathaExtraItemDropEvent.trigger(
                new ScathaProEvents.ScathaExtraDropEventData(scathaPro, type)
            ));
        }
    }
}