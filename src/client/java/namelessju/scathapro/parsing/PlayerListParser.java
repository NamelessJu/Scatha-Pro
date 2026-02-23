package namelessju.scathapro.parsing;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.files.PersistentData;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.UnicodeSymbol;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringDecomposer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class PlayerListParser
{
    private PlayerListParser() {}
    
    private static final Comparator<PlayerInfo> PLAYER_COMPARATOR = Comparator.<PlayerInfo>comparingInt(playerInfo -> -playerInfo.getTabListOrder())
        .thenComparingInt(playerInfo -> playerInfo.getGameMode() == GameType.SPECTATOR ? 1 : 0)
        .thenComparing(playerInfo -> Optionull.mapOrDefault(playerInfo.getTeam(), PlayerTeam::getName, ""))
        .thenComparing(playerInfo -> playerInfo.getProfile().name(), String::compareToIgnoreCase);
    
    private static List<PlayerInfo> getPlayerInfos(Minecraft minecraft)
    {
        if (minecraft.player == null) return List.of();
        return minecraft.player.connection.getListedOnlinePlayers().stream().sorted(PLAYER_COMPARATOR).limit(80L).toList();
    }
    
    public static void parseProfileStats(ScathaPro scathaPro)
    {
        AtomicReference<Integer> parsedMagicFind = new AtomicReference<>();
        AtomicReference<Integer> parsedPetLuck = new AtomicReference<>();
        
        parseWidgets(scathaPro.minecraft, new Predicate<>() {
            private boolean statsWidgetFound = false;
            private boolean magicFindFound = false;
            private boolean petLuckFound = false;
            
            @Override
            public boolean test(String name)
            {
                ScathaPro.LOGGER.debug("Player list entry: \"{}\"", name);
                
                if (!statsWidgetFound)
                {
                    ScathaPro.LOGGER.debug("Searching for stats widget...");
                    
                    if (name.isEmpty())
                    {
                        ScathaPro.LOGGER.debug("Entry is empty -> skipped");
                        return false;
                    }
                    
                    if (name.equals("Stats:"))
                    {
                        statsWidgetFound = true;
                        ScathaPro.LOGGER.debug("Stats widget found!");
                    }
                    
                    return false;
                }
                
                if (name.isEmpty() || name.charAt(0) != ' ')
                {
                    ScathaPro.LOGGER.debug("Empty or non-intented entry -> stats widget ended -> parsing finished");
                    return true;
                }
                
                ScathaPro.LOGGER.debug("Checking for MF and PL values...");
                if (!magicFindFound) parseNamedIntValue(name, "Magic Find", value -> {
                    parsedMagicFind.set(value);
                    magicFindFound = true;
                });
                if (!petLuckFound) parseNamedIntValue(name, "Pet Luck", value -> {
                    parsedPetLuck.set(value);
                    petLuckFound = true;
                });
                
                ScathaPro.LOGGER.debug("MF found: {}, PL found: {}", magicFindFound, petLuckFound);
                
                return magicFindFound && petLuckFound;
            }
        });
        
        PersistentData.ProfileData profileData = scathaPro.getProfileData();
        boolean updated = false;
        
        int magicFind = Objects.requireNonNullElse(parsedMagicFind.get(), -1);
        if (magicFind >= 0 && magicFind > profileData.magicFind.getOr(0f))
        {
            scathaPro.chatManager.sendChatMessage(Component.empty().withStyle(ChatFormatting.GRAY)
                .append("Updated Magic Find from tab list (")
                .append(TextUtil.numberToComponentOrObf(profileData.magicFind.getOr(-1f)))
                .append(" " + UnicodeSymbol.heavyArrowRight + " " + TextUtil.numberToString(magicFind, 2) + ")"));
            profileData.magicFind.set((float) magicFind);
            updated = true;
        }
        
        int petLuck = Objects.requireNonNullElse(parsedPetLuck.get(), -1);
        if (petLuck >= 0 && petLuck > profileData.petLuck.getOr(0f))
        {
            scathaPro.chatManager.sendChatMessage(Component.empty().withStyle(ChatFormatting.GRAY)
                .append("Updated Pet Luck from tab list (")
                .append(TextUtil.numberToComponentOrObf(profileData.petLuck.getOr(-1f)))
                .append(" " + UnicodeSymbol.heavyArrowRight + " " + TextUtil.numberToString(petLuck, 2) + ")"));
            profileData.petLuck.set((float) petLuck);
            updated = true;
        }
        
        if (updated)
        {
            scathaPro.persistentData.save();
            scathaPro.mainOverlay.updateProfileStats();
        }
    }
    
    private static void parseWidgets(Minecraft minecraft, Predicate<String> scorePredicate)
    {
        List<PlayerInfo> playerInfos = getPlayerInfos(minecraft);
        if (playerInfos.size() < 25) return;
        // Info widgets start at index 20, but we can skip the first "Info"
        // header along with the area widget, since it's always first
        for (int i = 24; i < Math.min(playerInfos.size(), 80); i ++)
        {
            // Skip other "Info" headers
            if (i == 40 || i == 60) continue;
            
            PlayerInfo playerInfo = playerInfos.get(i);
            Component displayName = playerInfo.getTabListDisplayName();
            if (displayName == null) continue;
            if (scorePredicate.test(StringDecomposer.getPlainText(displayName))) return;
        }
    }
    
    private static void parseIndex(Minecraft minecraft, int index, Consumer<String> playerListEntryConsumer)
    {
        List<PlayerInfo> playerInfos = getPlayerInfos(minecraft);
        if (playerInfos.size() <= index) return;
        
        PlayerInfo playerInfo = playerInfos.get(index);
        Component displayName = playerInfo.getTabListDisplayName();
        if (displayName == null) return;
        playerListEntryConsumer.accept(StringDecomposer.getPlainText(displayName));
    }
    
    private static void parseNamedIntValue(String playerListLine, String valueName, Consumer<Integer> valueConsumer)
    {
        ScathaPro.LOGGER.debug("Checking for player list int value \"{}\"", valueName);
        if (playerListLine.substring(1, valueName.length() + 1).equalsIgnoreCase(valueName))
        {
            String valueString = playerListLine.substring(valueName.length() + 3);
            
            ScathaPro.LOGGER.debug("Value found: {}", valueString);
            
            // remove non-number characters from left
            while (!valueString.isEmpty())
            {
                char firstChar = valueString.charAt(0);
                if (firstChar >= '0' && firstChar <= '9') break;
                valueString = valueString.substring(1);
            }
            
            // remove non-digit characters from right
            while (!valueString.isEmpty())
            {
                char lastChar = valueString.charAt(valueString.length() - 1);
                if (lastChar >= '0' && lastChar <= '9') break;
                valueString = valueString.substring(0, valueString.length() - 1);
            }
            
            ScathaPro.LOGGER.debug("Value before int parsing: {}", valueString);
            
            if (!valueString.isEmpty())
            {
                try
                {
                    valueConsumer.accept(Integer.parseInt(valueString));
                }
                catch (NumberFormatException ignored) {}
            }
        }
        else ScathaPro.LOGGER.debug("Value not found");
    }
}
