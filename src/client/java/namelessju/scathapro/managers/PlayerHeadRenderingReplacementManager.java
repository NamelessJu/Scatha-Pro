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
import namelessju.scathapro.util.TimeUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

// Mouthful of a class name
@NullMarked
public class PlayerHeadRenderingReplacementManager
{
    private static final ResolvableProfile scathaHeadProfile = createProfile(Constants.scathaHeadTextureDefault);
    private static final ResolvableProfile scathaTailProfile = createProfile(Constants.scathaTailTextureDefault);

    private static final ResolvableProfile halloweenScathaHeadProfile = createProfile("ewogICJ0aW1lc3RhbXAiIDogMTc5MTEzMjYyMjUwOSwKICAicHJvZmlsZUlkIiA6ICJlOWJlMzk4NGIwOTc0MGM5OGZiNGQ4YWFlYjJiNDgzOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJOYW1lbGVzc0p1IiwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzk0OGM1OGUxMjRhMzU0MDNjMjFhMzEwYzFkZjAwYWE4MjM1NTNiN2MxMzI2ZGFkNWJkZTc1OTA0MmQ5MzA2MCIKICAgIH0KICB9Cn0");
    private static final ResolvableProfile halloweenStonewormHeadProfile = createProfile("ewogICJ0aW1lc3RhbXAiIDogMTc5MTEzMjY4MzMyNSwKICAicHJvZmlsZUlkIiA6ICJlOWJlMzk4NGIwOTc0MGM5OGZiNGQ4YWFlYjJiNDgzOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJOYW1lbGVzc0p1IiwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzU1N2JlMTZiZjVjNzkxZjE3NDgwNmYzNzM3YWM0YzBjYThjMGY0NTk5MDgxMDQ3ZjNlZDNlOTJkMmVhODg4MzAiCiAgICB9CiAgfQp9");
    private static final ResolvableProfile christmasScathaHeadProfile = createProfile("ewogICJ0aW1lc3RhbXAiIDogMTc5MTEzNzMyMTI2NCwKICAicHJvZmlsZUlkIiA6ICJlOWJlMzk4NGIwOTc0MGM5OGZiNGQ4YWFlYjJiNDgzOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJOYW1lbGVzc0p1IiwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2ZjYmQ3OWNhNzU0YjNjZmIzZTU3MDlkZThhOWZmMzkyZTU0ZjI0ZmQ4YWFmNzk4NDA5ZTNiMmQ3ODEzNGU2OTEiCiAgICB9CiAgfQp9");
    private static final ResolvableProfile christmasScathaTailProfile = createProfile("ewogICJ0aW1lc3RhbXAiIDogMTc5MTI5OTU5MjU1OSwKICAicHJvZmlsZUlkIiA6ICJlOWJlMzk4NGIwOTc0MGM5OGZiNGQ4YWFlYjJiNDgzOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJOYW1lbGVzc0p1IiwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2E2ODUxMjRlNzllNzBlNmQ5ZmE2NmEzOTUxNjhjZTM2MGI5YjEyYjExMDliMTliNWM5NzU0ZGZmMjcwMGI1N2MiCiAgICB9CiAgfQp9");
    private static final ResolvableProfile christmasStonewormHeadProfile = createProfile("ewogICJ0aW1lc3RhbXAiIDogMTc5MTEzNzg1Mzc5OCwKICAicHJvZmlsZUlkIiA6ICJlOWJlMzk4NGIwOTc0MGM5OGZiNGQ4YWFlYjJiNDgzOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJOYW1lbGVzc0p1IiwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2MyM2JjNTI3M2Q3YjE5M2QzOTE4Y2ViOTg4ZDdkZDU1MzUyODgzY2ZlMmMwY2UzODIxNzE2ZmMzYTlmMjFhY2MiCiAgICB9CiAgfQp9");
    private static final ResolvableProfile christmasStonewormTailProfile = createProfile("ewogICJ0aW1lc3RhbXAiIDogMTc5MTMwNDEzMjg0MywKICAicHJvZmlsZUlkIiA6ICJlOWJlMzk4NGIwOTc0MGM5OGZiNGQ4YWFlYjJiNDgzOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJOYW1lbGVzc0p1IiwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzcxZjBhODE4ZTg3MDVlYTM3NzdjOGIyYjRhMmY0MGRlNTkwOWE2OGY2NTRjYjM5MDIyMDg5MzkzODY4YzdhYzAiCiAgICB9CiAgfQp9");

    private static ResolvableProfile createProfile(String textureBase64)
    {
        return ResolvableProfile.createResolved(new GameProfile(
            new UUID(0L, 0L), "",
            new PropertyMap(ImmutableMultimap.of("textures", new Property("textures", textureBase64)))
        ));
    }


    private final Config config;

    public PlayerHeadRenderingReplacementManager(ScathaPro scathaPro)
    {
        this.config = scathaPro.config;
    }

    public ResolvableProfile replaceEntityProfile(ResolvableProfile profile, IArmorStandRenderStateData data)
    {
        if (!data.scathaPro$isWorm() || data.scathaPro$getWormSegmentType() == null) return profile;

        return replaceProfile(profile, data.scathaPro$isScatha(), data.scathaPro$getWormSegmentType() == WormSegmentType.HEAD);
    }

    public ResolvableProfile replaceItemProfile(ResolvableProfile profile, ItemStack itemStack)
    {
        ItemLore lore = itemStack.get(DataComponents.LORE);
        // bestiary entries lore starts with "Mob Types: [...]"
        if (lore != null && !lore.lines().isEmpty() && lore.lines().getFirst().getString().startsWith("Mob"))
        {
            Component itemName = itemStack.getCustomName();
            if (itemName != null)
            {
                String unformattedName = itemName.getString();
                if (unformattedName.contains("Scatha"))
                {
                    return replaceProfile(profile, true, true);
                }
                if (unformattedName.contains("Stoneworm"))
                {
                    return replaceProfile(profile, false, true);
                }
            }
        }

        return profile;
    }

    private ResolvableProfile replaceProfile(ResolvableProfile profile, boolean isScatha, boolean isHead)
    {
        if (config.worms.seasonalWormTexturesEnabled.get())
        {
            ResolvableProfile seasonalProfile = getHalloweenProfile(isScatha, isHead);
            if (seasonalProfile != null) return seasonalProfile;
            seasonalProfile = getChristmasProfile(isScatha, isHead);
            if (seasonalProfile != null) return seasonalProfile;
        }

        if (isHead)
        {
            if (isScatha)
            {
                ResolvableProfile scathaPlayerHeadProfile = config.worms.scathaPlayerHeadProfile.value;
                if (scathaPlayerHeadProfile != null) return scathaPlayerHeadProfile;
            }
            else
            {
                ResolvableProfile wormPlayerHeadProfile = config.worms.regularWormPlayerHeadProfile.value;
                if (wormPlayerHeadProfile != null) return wormPlayerHeadProfile;
            }
        }

        if (!isScatha && config.worms.revertRegularWormTexture.get())
        {
            if (isHead) return scathaHeadProfile;
            else return scathaTailProfile;
        }

        return profile;
    }

    private @Nullable ResolvableProfile getHalloweenProfile(boolean isScatha, boolean isHead)
    {
        if (!isHead || !TimeUtil.isHalloween()) return null;

        if (isScatha) return halloweenScathaHeadProfile;
        else return halloweenStonewormHeadProfile;
    }

    private @Nullable ResolvableProfile getChristmasProfile(boolean isScatha, boolean isHead)
    {
        if (!TimeUtil.isChristmas()) return null;

        if (isScatha)
        {
            if (isHead) return christmasScathaHeadProfile;
            else return christmasScathaTailProfile;
        }
        else
        {
            if (isHead) return christmasStonewormHeadProfile;
            else return christmasStonewormTailProfile;
        }
    }
}