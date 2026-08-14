package namelessju.scathapro.managers;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.files.Config;
import namelessju.scathapro.miscellaneous.data.enums.WormSegmentType;
import namelessju.scathapro.miscellaneous.data.mixindata.IArmorStandRenderStateData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

// Mouthful of a class name
@NullMarked
public class PlayerHeadRenderingReplacementManager
{
    private static final ResolvableProfile scathaHeadProfile = createProfile(Constants.scathaHeadTextureDefault);
    private static final ResolvableProfile scathaTailProfile = createProfile(Constants.scathaTailTextureDefault);

    private final Config config;

    public PlayerHeadRenderingReplacementManager(ScathaPro scathaPro)
    {
        this.config = scathaPro.config;
    }

    public ResolvableProfile replaceEntityProfile(ResolvableProfile profile, IArmorStandRenderStateData data)
    {
        if (!data.scathapro$isWorm() || data.scathapro$getWormSegmentType() == null) return profile;

        return replaceProfile(profile, data.scathapro$isScatha(), data.scathapro$getWormSegmentType() == WormSegmentType.HEAD);
    }

    public ResolvableProfile replaceItemProfile(ResolvableProfile profile, ItemStack itemStack)
    {
        Component itemName = itemStack.getCustomName();
        if (itemName != null)
        {
            String unformattedName = itemName.getString();
            if (unformattedName.equals("[Lv10] Scatha"))
            {
                return replaceProfile(profile, true, true);
            }
            if (unformattedName.contains("Stoneworm"))
            {
                return replaceProfile(profile, false, true);
            }
        }

        return profile;
    }

    private ResolvableProfile replaceProfile(ResolvableProfile profile, boolean isScatha, boolean isHead)
    {
        if (isScatha)
        {
            if (!isHead) return profile;
            ResolvableProfile scathaPlayerHeadProfile = config.worms.scathaPlayerHeadProfile.value;
            if (scathaPlayerHeadProfile != null) return scathaPlayerHeadProfile;
        }
        else
        {
            if (isHead)
            {
                ResolvableProfile wormPlayerHeadProfile = config.worms.regularWormPlayerHeadProfile.value;
                if (wormPlayerHeadProfile != null) return wormPlayerHeadProfile;
            }

            if (config.worms.revertRegularWormTexture.get())
            {
                if (isHead) return scathaHeadProfile;
                else return scathaTailProfile;
            }
        }

        return profile;
    }

    private static ResolvableProfile createProfile(String textureBase64)
    {
        return ResolvableProfile.createResolved(new GameProfile(
            new UUID(0L, 0L), "",
            new PropertyMap(ImmutableMultimap.of("textures", new Property("textures", textureBase64)))
        ));
    }
}