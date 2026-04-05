package namelessju.scathapro;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import namelessju.scathapro.miscellaneous.data.enums.Rarity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.UUID;

public class Constants
{
    private Constants() {}
    
    public static final int pingTreshold = 2000;
    
    /** The numbers of ticks after opening a chest GUI at which the parser tries to parse it's contents */
    public static final int[] chestGuiParserTickCounts = new int[] {4, 10, 20, 40, 60, 100};
    
    public static final int wormSpawnCooldown = 30_000;
    public static final int wormLifetime = 30_000;
    
    public static final float scathaPetBaseChanceRare = 0.00_24f;
    public static final float scathaPetBaseChanceEpic = 0.00_12f;
    public static final float scathaPetBaseChanceLegendary = 0.00_04f;
    
    public static final int maxLegitPetDropsAmount = 9999;
    /** Dry streak gets invalidated if the mod's and the bestiary's Scatha kills differ more than this threshold */
    public static final int dryStreakMaxAllowedScathaKillsDeviation = 10;
    
    // These are the wall block coordinates
    public static final int crystalHollowsBoundsMin = 201;
    public static final int crystalHollowsBoundsMax = 824;

    public static final int tunnelVisionEffectDuration = 30_000;
    
    public static float applyShurikenMagicFind(float originalMagicFind)
    {
        if (originalMagicFind < 0f) return originalMagicFind;
        return originalMagicFind * 1.05f;
    }
    
    private static final String wormHeadTextureDefault = "ewogICJ0aW1lc3RhbXAiIDogMTYyMDQ0NTc2NDQ1MSwKICAicHJvZmlsZUlkIiA6ICJmNDY0NTcxNDNkMTU0ZmEwOTkxNjBlNGJmNzI3ZGNiOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWxhcGFnbzA1IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RmMDNhZDk2MDkyZjNmNzg5OTAyNDM2NzA5Y2RmNjlkZTZiNzI3YzEyMWIzYzJkYWVmOWZmYTFjY2FlZDE4NmMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==";
    
    private static final String[] wormHeadTextures = new String[] {
        /* default: */ wormHeadTextureDefault,
        /* plum: */ "ewogICJ0aW1lc3RhbXAiIDogMTc2MTM4MDMwNzg5OSwKICAicHJvZmlsZUlkIiA6ICJiYWNlNWU3MGIzOGM0YjNhYmRkODU5NGY3YjE2Njg1NyIsCiAgInByb2ZpbGVOYW1lIiA6ICJBbG1pZ2h0eUJ1bm55eSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80ZjdlYTNiYTY1OTA1NDQ5NjE1NDJhMTYyNTFkMTg1NzZjNWEzZDVkOWJhYjQ2MzgxMTNkOGQ1ZTI5Mzk2NDFjIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
        /* cherry: */ "ewogICJ0aW1lc3RhbXAiIDogMTc2MTU0MjE5ODI3MCwKICAicHJvZmlsZUlkIiA6ICIxMzEzZGFmMDc2OGQ0YmQ5Yjc1ODJkMGI1NWUwZGQxNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJMZW50aWNjaGllIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzlmZDZmYzgxOWE1NWQyN2IxNTJhZWVlODIyNDc1OTYxNzY3ZWQxZjYzMTdkYjk2OWIwMzg0ZTY4MWUxYjVjZGMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
        /* orange: */ "ewogICJ0aW1lc3RhbXAiIDogMTc2MTQ2MTc5ODA0MywKICAicHJvZmlsZUlkIiA6ICJkZWFmNjAxNDU1NDY0MGU5YWJmNmUyMWZiZDgyZDU2NiIsCiAgInByb2ZpbGVOYW1lIiA6ICJBaWxvcnlLIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzJmYzkyYTdkYTFjMDM3NGU4ZWM2YzY0YmI2OWFiNTk0NzAwODIxNzk5MzFmYzgyN2QyOTFmMzM2YjUxYTNhZDUiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
        /* lemon: */ "ewogICJ0aW1lc3RhbXAiIDogMTc2MTU0NTU4Njk2MCwKICAicHJvZmlsZUlkIiA6ICIyY2Y2MzExZjUyMTM0NTE2YTEyNTY3NWUwMzk3NmU2MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJmaWdodHN0b2NrIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzg5MmJiYTgzNWNjMmU1ZTA3NGIyOGZmNWI4OWRhMDY1MTBlZmViYjJhZjdiODIxMGY2YzcyOGM0OTNiOTZkNGUiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
        /* milk: */ "ewogICJ0aW1lc3RhbXAiIDogMTc2MTU0MzM4NTkwNSwKICAicHJvZmlsZUlkIiA6ICJlYzU4NzQxMjM2M2E0ZTQ0OTEwNzJjMWU5M2RkOGMzOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJ3aXJlZGZyYW1lIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzgwZjI2ZWEzMTk4YTc3YTcxYzhiMzU2Y2UzOTdkYzc5ZWIzOTAxODg3NzA0NmUyMjM1OWFjNWNhNjgyYjBhYmMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
        /* lime: */ "ewogICJ0aW1lc3RhbXAiIDogMTczOTI1MjkyNzU3NywKICAicHJvZmlsZUlkIiA6ICIzMzU3MWJiY2UyMDE0MTRiYmNkMDYyMjEyZTI4MjBlMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGFkb21JbmF0b3I0NzgiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzU4ZjVkZmEwODZkYTRlOTQyZGU3YjQwYjRhMjBjMDA3NWM0YzAyMzZkNWQ2YWRjYzIzNGYxNGUzNzk0NTI2YSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
        /* apple: */ "ewogICJ0aW1lc3RhbXAiIDogMTc2MTM3ODg3MTY2MSwKICAicHJvZmlsZUlkIiA6ICJhODMwMTUxNzNlOGQ0MWEyOGY0ZDk1ZDM2Njc4MGJmNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJTdGFybGl0R2FtZXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2Y4ZjNlZjE1OTU0ZmQ1MzgyNzY0NTZmY2JmYzNhZWUyMzk0ZjM5ZmY2MzNlMjFkNzQ3OTRiMDZhZjRiYTFiZCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
        /* licorice: */ "ewogICJ0aW1lc3RhbXAiIDogMTc2MTM2NTY4MjI0NywKICAicHJvZmlsZUlkIiA6ICI3MjU1MDA3NjQzYzQ0YTZiYjM3MjJlNzc3OTk5OTFkOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJ4M250YW55IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2ZlMDE4MjZlYTAwOGQxNzI5MGJkZmI3MmEzNDJiNTJmZDEyMmU5MDZkNzVhM2E5ZWI4MDdhMDdmY2Y5ZGFlMGMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
        /* blueberry: */ "ewogICJ0aW1lc3RhbXAiIDogMTc2MTQ2MDE5MTMzNSwKICAicHJvZmlsZUlkIiA6ICJkOWYxZDI2ZWFkYjg0MjQ4OTI3NjU1NzYzNWNiMzQyMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJCb2JPbGRmbHkiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODUzYWZlYWJmZDM1NDhmMmM3NzViNDUyNjIyMjJhOWRlOGU4NjRiMTE5Yjg0NmYzZDE2MTZhNjQyNGUyNzIxZSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
        /* punch: */ "ewogICJ0aW1lc3RhbXAiIDogMTc2MTM2NzE5ODYzMCwKICAicHJvZmlsZUlkIiA6ICIxNjEzYmM0MWY4NTA0MWM0YjA0Y2UyODJhNTlmZmJlNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJTaGFyX2QiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzc5OGU4ZGY2ZjNkOThlMWVkNTFkNzA3NGRiYjY4MDgwNTc5MzI2YjMwYTliMWMwZmJiNjAzMGY0NWYyYzlhZiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
        /* bubblegum: */ "ewogICJ0aW1lc3RhbXAiIDogMTc2MTM2NDExNTQ4MiwKICAicHJvZmlsZUlkIiA6ICJhYzY1NDYwOWVkZjM0ODhmOTM0ZWNhMDRmNjlkNGIwMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJzcGFjZUd1cmxTa3kiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjkwNjM0YzQ5YmZkZWU5YjNhZDI3YzEyNzc5OTg2ZGI3MTE1NzE5YThkOTU4MmU4ZGM2NjczYjc0NGVlOTY2NCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
        /* watermelon: */ "ewogICJ0aW1lc3RhbXAiIDogMTc2MTQ2MjkwMzkzNSwKICAicHJvZmlsZUlkIiA6ICI0M2NmNWJkNjUyMDM0YzU5ODVjMDIwYWI3NDE0OGQxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJrYW1pbDQ0NSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS81MmEwZDJlZmI4Nzk5NGE4ZjYyY2QyODQ2NTAwNTFkMDlmYWMwYTlmMTVlMjI3MTExMDJlZjU5NzdmMDNkMmI0IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
        /* mulberry: */ "ewogICJ0aW1lc3RhbXAiIDogMTc2MTQ1ODUzNDIxMCwKICAicHJvZmlsZUlkIiA6ICI0OThjYTc2ZGYwODM0NzhmOGY0NjdjOGY1OTQwMjk1MiIsCiAgInByb2ZpbGVOYW1lIiA6ICJHdWx0cm8iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjkzNjZmMTU3YzQ0MTJhOTBjNjljMGYwNjUwYzUwYjhlNGM1ODkzOWQ4YTdlMDJlNWI1NDMyNzRiMzgyNDkzZCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
        /* grape: */ "ewogICJ0aW1lc3RhbXAiIDogMTc2MTUzNzc0NTYxNCwKICAicHJvZmlsZUlkIiA6ICIzNmU5MTE1YzBjYzc0ZjhkOTdmOGFjNjA1ZGMxNGVkYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJEYXJnaVYiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDI5YWE1Yzg3MmUwMzBlNWE0MzRiYzI1MDViNmFjZmE4MDhmY2Y5ZTg1OGFlMTYwOTFjMTc2MDA0ZDBkMDA1IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
        /* choco: */ "ewogICJ0aW1lc3RhbXAiIDogMTc2MTM2ODk1NjEyNSwKICAicHJvZmlsZUlkIiA6ICJhODc1ZTI3NjZjOTc0N2Y5OTM3YzBmMzNhMWQ3N2JmMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJzcGlmZnRvcGlhMTAiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzBiZjliZjAzODExZTllZGM5ZDVkNjA2NmU5ZjBlMDJiMDc5MWZhNGVkOGQ2NDU2NTc0YjY4Mjk0NTUxNTU5OSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9"
    };
    private static final String[] wormBodyTextures = new String[] {
        /* default: */ "ewogICJ0aW1lc3RhbXAiIDogMTYyNTA3MjMxNDE2OCwKICAicHJvZmlsZUlkIiA6ICIwNWQ0NTNiZWE0N2Y0MThiOWI2ZDUzODg0MWQxMDY2MCIsCiAgInByb2ZpbGVOYW1lIiA6ICJFY2hvcnJhIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzk2MjQxNjBlYjk5YmRjNjUxZGEzOGRiOTljZDdjMDlmMWRhNjY5ZWQ4MmI5Y2JjMjgyODc0NmU2NTBjNzY1ZGEiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
        /* plum: */ "ewogICJ0aW1lc3RhbXAiIDogMTc2MTM3OTU5MzE5NywKICAicHJvZmlsZUlkIiA6ICIwNDg0N2ZjNWM5YjY0NTQ1YjI1ZWJkYmJiNzdjNjg2NSIsCiAgInByb2ZpbGVOYW1lIiA6ICJOYXFsdWEiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjNiMzBjNTk2NDI5OTNjNzMwN2VjM2VkMzgzN2U2OWEwMWZmZjg3Y2Y1MWE5OGJkOWRjMzZmMWY4NTRiNDY0OSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
        /* cherry: */ "ewogICJ0aW1lc3RhbXAiIDogMTc2MTU0MTU5OTE2MSwKICAicHJvZmlsZUlkIiA6ICJhNzdkNmQ2YmFjOWE0NzY3YTFhNzU1NjYxOTllYmY5MiIsCiAgInByb2ZpbGVOYW1lIiA6ICIwOEJFRDUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmY3MDE3MzM1MzJmODJmZjY2YzYwOTUyZjIyNjVhYTVmNGI1ZmE3ZWJjNWIxYTY5NDBlNWJmYzk1ZDBiNGNlOSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
        /* orange: */ "ewogICJ0aW1lc3RhbXAiIDogMTc2MTQ2MDg2OTQ1MSwKICAicHJvZmlsZUlkIiA6ICJhODc1ZTI3NjZjOTc0N2Y5OTM3YzBmMzNhMWQ3N2JmMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJzcGlmZnRvcGlhMTAiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjM3NTdjMzU2YzE1Yjg0NDI3YWU1MjMxMDkxMGIyYjY4YWYzYTg2MzFkZmNjMGIzYWI5Yzc5MDFhYTFjNDIzMSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
        /* lemon: */ "ewogICJ0aW1lc3RhbXAiIDogMTc2MTU0NDk2Mjc0MiwKICAicHJvZmlsZUlkIiA6ICI4YjBiZmE5YzE4YmY0ZThiOTgzYzQ5ZGMzM2FmZWU3NSIsCiAgInByb2ZpbGVOYW1lIiA6ICJQcmltb3JpbmEiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2I3YjQ4ZjdkMjQyOTJjYWJlNzU5ZTVlNjhmZTgwYWYwNGFiM2NmNjdjMTA3ZTU1MzZjZjE5NGViYzM5Mzg5MiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
        /* milk: */ "ewogICJ0aW1lc3RhbXAiIDogMTc2MTU0Mjg0MDUzMSwKICAicHJvZmlsZUlkIiA6ICIzY2I3YTA3YWY3ZjM0ZWZiYTlkNGI4ODQ3NDM4Mzc0ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJaZW4xXzAwMSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kYzRmNzFkOGQ3MjA5OWI2NDg3MTliOTA1MTkxNTgzNjI0MWQ2NGE1ODIxNzAzZGM5YzYzMmU1OThkOGExNTRmIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
        /* lime: */ "ewogICJ0aW1lc3RhbXAiIDogMTczOTI0NDM4NjQ4NSwKICAicHJvZmlsZUlkIiA6ICJmNTBjOGRkN2FiN2Y0ZmUyYWI4ZGI1M2NjYzRiYWQxMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJtYWNoYWRvVF9UIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzlkYzEzODFiYTE1NjMzNWYzNmNhZTQwYjk3MDNlODgzZWVmNWFhOTFmNTE3N2Q4YWI5OTY5NDQ1MDBiYTllNmYiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
        /* apple: */ "ewogICJ0aW1lc3RhbXAiIDogMTc2MTM3ODI2NTU1MCwKICAicHJvZmlsZUlkIiA6ICJiMGQ0YjI4YmMxZDc0ODg5YWYwZTg2NjFjZWU5NmFhYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNaW5lU2tpbl9vcmciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODlhODk2NDI1NmQ2NjRjZGVmNzlhYzc5MjBhODY0ODU2NjdhYTYyM2Q2YjE1Y2NiMzQxZWJhNzQwN2VkMmZlNCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
        /* licorice: */ "ewogICJ0aW1lc3RhbXAiIDogMTc2MTM2NTEzNTExNywKICAicHJvZmlsZUlkIiA6ICIzZDU1OGQ3Y2NmZjk0ODdkYWE1MzhkMjM4NGE3OWFkZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJDcnlwdGljTG9zZXIxMyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9hN2E2ZDEyOTNlNTA0NmU2MGI5ZGFjN2RhOTM2OWJhZWY1YTA3YTg1ZjM4MjVlZTdhNzlmMTc0ZWViOWFjMjExIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
        /* blueberry: */ "ewogICJ0aW1lc3RhbXAiIDogMTc2MTQ1OTM5Nzc1OCwKICAicHJvZmlsZUlkIiA6ICI3ZGY4NmY1MWFjZmI0MjQzYTkzNDQ1OTAyZDEzYTc0MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNclJpcHRpZGUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTRhNTU4YWJiYmVmM2YwNWNjMzk4MDFmOGJlOGFiOGMzMTM2ZjYyNzQ0MTdlMzAxMGZkM2M1NjFlMzBjYTczMiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
        /* punch: */ "ewogICJ0aW1lc3RhbXAiIDogMTc2MTM2NjU0MzU3NSwKICAicHJvZmlsZUlkIiA6ICJkYjZiYWRlN2NjMzI0MjM4YjU3OTQ4NzMxNTBkNjA1MiIsCiAgInByb2ZpbGVOYW1lIiA6ICJRdWFudHVtQmxvY2tlciIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS83ODZmNTUwM2I3NGNlZjkyZGU2MjBhMWQyOWZhZTVhMjBhMjYyMzI3OTljNzI4M2UzNmQ3MmM3YzA5NzAwZGJjIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
        /* bubblegum: */ "ewogICJ0aW1lc3RhbXAiIDogMTc2MTM2MzM1NDY5NSwKICAicHJvZmlsZUlkIiA6ICJkNDAwODgyZmY3OGQ0ZGVhYjliMGNlMTc2YmQ1ZTQyMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJQcmF4aWJldGwiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTdkYzU1MzAwZmVmNDRlNGY2OTMzNzg2YWJiOTcxNjg1MTNiMjU4Y2M3MDNiNjM1NGI5ZjAxZjk4OTk0NjZlMCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
        /* watermelon: */ "ewogICJ0aW1lc3RhbXAiIDogMTc2MTQ2MjM3MjAzMiwKICAicHJvZmlsZUlkIiA6ICIyOGQyZDFmZDEyNGY0NGMyOGYxZDgwNDY4NGFkOTA2ZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJzcGlmZnRvcGlhNyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jZTg2NGU5NjFhMDMzNDVhMDFmZDZmZmVlNDk0YjM4N2ZmNWFhYTc0ZjJiY2U5ZTU5OGVmZTNhNDE5OWUxOWYzIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
        /* mulberry: */ "ewogICJ0aW1lc3RhbXAiIDogMTc2MTQ1NzM2NjY2MiwKICAicHJvZmlsZUlkIiA6ICJiMGQ0YjI4YmMxZDc0ODg5YWYwZTg2NjFjZWU5NmFhYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNaW5lU2tpbl9vcmciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmM5NGVmNzFiMjJmMTYwYzFmYWMyYTVlYTNkNmYzOGYzYzVmNmQxNjg2ZWMwOWY2ODAxNWI4OTZiZGZlMjUzZiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
        /* grape: */ "ewogICJ0aW1lc3RhbXAiIDogMTc2MTU0MDg4MjkyMiwKICAicHJvZmlsZUlkIiA6ICI0N2U2MjJjNmE1OWU0NmNhOWU3OGNjYzE1ZDliNzhhZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJBbnRpbGFmTVgiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGMyMGRlMWJlYjQyMzBjOWNmZDQ0MmEwZjY5MDY4NjVmZDlhOGVkYTJkN2NmOGM0NDcxMzc0M2QzN2FhNjVhNiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
        /* choco: */ "ewogICJ0aW1lc3RhbXAiIDogMTc2MTM2Nzk3MTM1OSwKICAicHJvZmlsZUlkIiA6ICI3MzFiOTdlYTI1MWM0ZjNmYTk0OTEwY2RkMmQwOTU4YiIsCiAgInByb2ZpbGVOYW1lIiA6ICJJbU5vdEFDYXQ2IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzE5ZjVkNjY0YjliODVhNDU0MTYzYzU5NTE4MDg1OGIwNWRkNWJjZmJkOTBjMzY5Y2Y2ZWU0M2EyMzdlNDMxOTgiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ=="
    };
    
    public static boolean isWormPlayerHead(ItemStack item)
    {
        return isWormPlayerHead(item, false);
    }
    
    public static boolean isWormPlayerHead(ItemStack item, boolean checkWormHeadOnly)
    {
        if (item == null || item.getItem() != Items.PLAYER_HEAD) return false;
        
        ResolvableProfile profileInfo = item.get(DataComponents.PROFILE);
        if (profileInfo == null) return false;
        
        Collection<Property> textureProperties = profileInfo.partialProfile().properties().get("textures");
        for (Property textureProperty : textureProperties)
        {
            String textureBase64 = textureProperty.value();
            if (textureBase64 == null) continue;
            
            for (String texture : wormHeadTextures)
            {
                if (textureBase64.equals(texture))
                    return true;
            }
            
            if (checkWormHeadOnly) continue;
            
            for (String texture : wormBodyTextures)
            {
                if (textureBase64.equals(texture))
                    return true;
            }
        }
        
        return false;
    }
    
    
    public static final UUID devUUID = UUID.fromString("e9be3984-b097-40c9-8fb4-d8aaeb2b4838");
    
    
    public static @NonNull ItemStack generateScathaPetItem(@NonNull Rarity rarity)
    {
        ItemStack scathaPetItem = new ItemStack(Items.PLAYER_HEAD);
        scathaPetItem.applyComponents(DataComponentPatch.builder()
            .set(
                DataComponents.PROFILE,
                ResolvableProfile.createResolved(new GameProfile(Util.NIL_UUID, "", new PropertyMap(ImmutableMultimap.of(
                    "textures", new Property("textures", wormHeadTextureDefault)
                ))))
            )
            .set(DataComponents.CUSTOM_DATA, CustomData.EMPTY.update(data -> {
                // lets resource packs detect this item as a Scatha pet
                data.putString("id", "PET");
                data.putString("petInfo", "{\"type\":\"SCATHA\",\"tier\":\"" + rarity.getTierString() + "\"}");
            }))
            .build()
        );
        return scathaPetItem;
    }
    
    public static @NonNull Component generatePetDropMessage(@NonNull Rarity rarity)
    {
        return Component.empty()
            .append(Component.literal("PET DROP! ").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
            .append(Component.literal("Scatha").setStyle(rarity.style));
    }
}
