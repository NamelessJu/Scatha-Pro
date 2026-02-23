package namelessju.scathapro;

import com.mojang.authlib.properties.Property;
import namelessju.scathapro.miscellaneous.data.enums.Rarity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.UUID;

public class Constants
{
    public static final int pingTreshold = 2000;
    
    /** The numbers of ticks after opening a chest GUI at which the parser tries to parse it's contents */
    public static final int[] chestGuiParserTickCounts = new int[] {4, 10, 20, 40, 60, 100};
    
    public static final int wormSpawnCooldown = 30000;
    public static final int wormLifetime = 30000;
    
    public static final float scathaPetBaseChanceRare = 0.0024f;
    public static final float scathaPetBaseChanceEpic = 0.0012f;
    public static final float scathaPetBaseChanceLegendary = 0.0004f;
    
    public static final int maxLegitPetDropsAmount = 9999;
    /** Dry streak gets invalidated if the mod's and the bestiary's Scatha kills differ more than this threshold */
    public static final int dryStreakMaxAllowedScathaKillsDeviation = 10;
    
    // These are the wall coordinates
    public static final int crystalHollowsBoundsMin = 201;
    public static final int crystalHollowsBoundsMax = 824;

    public static final int tunnelVisionEffectDuration = 30000;
    
    public static final UUID devUUID = UUID.fromString("e9be3984-b097-40c9-8fb4-d8aaeb2b4838");
    
    
    public static boolean isWormSkull(ItemStack item)
    {
        return isWormSkull(item, false);
    }
    
    public static boolean isWormSkull(ItemStack item, boolean headOnly)
    {
        if (item == null || item.getItem() != Items.PLAYER_HEAD) return false;
        
        ResolvableProfile profileInfo = item.get(DataComponents.PROFILE);
        if (profileInfo == null) return false;
        
        Collection<Property> textureProperties = profileInfo.partialProfile().properties().get("textures");
        for (Property textureProperty : textureProperties)
        {
            String textureBase64 = textureProperty.value();
            if (textureBase64 == null) continue;
            
            // Head
            if (textureBase64.equals("ewogICJ0aW1lc3RhbXAiIDogMTYyMDQ0NTc2NDQ1MSwKICAicHJvZmlsZUlkIiA6ICJmNDY0NTcxNDNkMTU0ZmEwOTkxNjBlNGJmNzI3ZGNiOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWxhcGFnbzA1IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RmMDNhZDk2MDkyZjNmNzg5OTAyNDM2NzA5Y2RmNjlkZTZiNzI3YzEyMWIzYzJkYWVmOWZmYTFjY2FlZDE4NmMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ=="))
                return true;
            // Body
            if (!headOnly && textureBase64.equals("ewogICJ0aW1lc3RhbXAiIDogMTYyNTA3MjMxNDE2OCwKICAicHJvZmlsZUlkIiA6ICIwNWQ0NTNiZWE0N2Y0MThiOWI2ZDUzODg0MWQxMDY2MCIsCiAgInByb2ZpbGVOYW1lIiA6ICJFY2hvcnJhIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzk2MjQxNjBlYjk5YmRjNjUxZGEzOGRiOTljZDdjMDlmMWRhNjY5ZWQ4MmI5Y2JjMjgyODc0NmU2NTBjNzY1ZGEiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ=="))
                return true;
        }
        
        return false;
    }
    
    public static @NonNull Component getPetDropMessage(@NonNull Rarity rarity)
    {
        return Component.empty()
            .append(Component.literal("PET DROP! ").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
            .append(Component.literal("Scatha").setStyle(rarity.style));
    }
    
    
    private Constants() {}
}
