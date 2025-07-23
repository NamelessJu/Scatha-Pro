package namelessju.scathapro.parsing;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.UnicodeSymbol;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldSettings;

public class PlayerListParser
{
    private static final Ordering<NetworkPlayerInfo> playerListOrdering = Ordering.from(new PlayerComparator());
    
    static class PlayerComparator implements Comparator<NetworkPlayerInfo>
    {
        public int compare(NetworkPlayerInfo playerInfoA, NetworkPlayerInfo playerInfoB)
        {
            ScorePlayerTeam scoreplayerteam = playerInfoA.getPlayerTeam();
            ScorePlayerTeam scoreplayerteam1 = playerInfoB.getPlayerTeam();
            return ComparisonChain.start().compareTrueFirst(playerInfoA.getGameType() != WorldSettings.GameType.SPECTATOR, playerInfoB.getGameType() != WorldSettings.GameType.SPECTATOR).compare(scoreplayerteam != null ? scoreplayerteam.getRegisteredName() : "", scoreplayerteam1 != null ? scoreplayerteam1.getRegisteredName() : "").compare(playerInfoA.getGameProfile().getName(), playerInfoB.getGameProfile().getName()).result();
        }
    }

    
    public static String parseArea()
    {
        ParsedValue<String> areaName = new ParsedValue<String>();

        Consumer<String> listEntryConsumer = name -> {
            if (name != null && name.length() > 6 && name.startsWith("Area:")) areaName.setValue(name.substring(6));
        };
        
        parseIndex(21, listEntryConsumer);
        if (!areaName.hasValue()) parseIndex(41, listEntryConsumer);
        
        return areaName.getValue(null);
    }
    
    public static void parseProfileStats()
    {
        ParsedValue<Integer> parsedMagicFind = new ParsedValue<Integer>();
        ParsedValue<Integer> parsedPetLuck = new ParsedValue<Integer>();
        
        parseWidgets(new Predicate<String>() {
            private boolean statsWidgetFound = false;
            private boolean magicFindFound = false;
            private boolean petLuckFound = false;
            
            @Override
            public boolean test(String name)
            {
                // ScathaPro.getInstance().logDebug("Player list entry: \"" + name + "\"");
                
                if (!statsWidgetFound)
                {
                    // ScathaPro.getInstance().logDebug("Searching for stats widget...");
                    
                    if (name.isEmpty())
                    {
                        // ScathaPro.getInstance().logDebug("Entry is empty -> skipped");
                        return false;
                    }
                    
                    if (name.equals("Stats:"))
                    {
                        statsWidgetFound = true;
                        // ScathaPro.getInstance().logDebug("Stats widget found!");
                    }
                    
                    return false;
                }
                
                if (name.isEmpty() || name.charAt(0) != ' ')
                {
                    // ScathaPro.getInstance().logDebug("Empty or non-intented entry -> stats widget ended -> parsing finished");
                    return true;
                }
                
                // ScathaPro.getInstance().logDebug("Checking for MF and PL values...");
                if (!magicFindFound && parseNamedIntValue(name, "Magic Find", parsedMagicFind)) magicFindFound = true;
                if (!petLuckFound && parseNamedIntValue(name, "Pet Luck", parsedPetLuck)) petLuckFound = true;
                
                // ScathaPro.getInstance().logDebug("MF found: " + magicFindFound + ", PL found: " + petLuckFound);
                
                return magicFindFound && petLuckFound;
            }
        });
        
        ScathaPro scathaPro = ScathaPro.getInstance();
        
        int magicFind = parsedMagicFind.getValue(-1);
        if (magicFind >= 0 && magicFind > scathaPro.variables.magicFind)
        {
            TextUtil.sendModChatMessage(EnumChatFormatting.GRAY + "Updated Magic Find from tab list (" + TextUtil.getObfNrStr(scathaPro.variables.magicFind) + " " + UnicodeSymbol.heavyArrowRight + " " + TextUtil.numberToString(magicFind, 2) + ")");
            scathaPro.variables.magicFind = magicFind;
            scathaPro.getPersistentData().saveProfileStats();
            scathaPro.getOverlay().updateProfileStats();
        }
        
        int petLuck = parsedPetLuck.getValue(-1);
        if (petLuck >= 0 && petLuck > scathaPro.variables.petLuck)
        {
            TextUtil.sendModChatMessage(EnumChatFormatting.GRAY + "Updated Pet Luck from tab list (" + TextUtil.getObfNrStr(scathaPro.variables.petLuck) + " " + UnicodeSymbol.heavyArrowRight + " " + TextUtil.numberToString(petLuck, 2) + ")");
            scathaPro.variables.petLuck = petLuck;
            scathaPro.getPersistentData().saveProfileStats();
            scathaPro.getOverlay().updateProfileStats();
        }
    }
    
    private static void parseWidgets(Predicate<String> scorePredicate)
    {
        NetHandlerPlayClient netHandler = Minecraft.getMinecraft().getNetHandler();
        if (netHandler == null) return;
        
        Collection<NetworkPlayerInfo> playerInfoMap = netHandler.getPlayerInfoMap();
        if (playerInfoMap.size() < 40) return;
        
        List<NetworkPlayerInfo> list = playerListOrdering.<NetworkPlayerInfo>sortedCopy(playerInfoMap);
        for (int i = 44; i < Math.min(list.size(), 80); i ++)
        {
            if (i == 60) continue;
            
            NetworkPlayerInfo playerInfo = list.get(i);
            IChatComponent displayName = playerInfo.getDisplayName();
            String unformattedName = displayName != null ? displayName.getUnformattedText() : "";
            if (scorePredicate.test(unformattedName)) return;
        }
    }
    
    private static void parseIndex(int index, Consumer<String> playerListEntryConsumer)
    {
        NetHandlerPlayClient netHandler = Minecraft.getMinecraft().getNetHandler();
        if (netHandler == null) return;
        
        Collection<NetworkPlayerInfo> playerInfoMap = netHandler.getPlayerInfoMap();
        if (playerInfoMap.size() <= index) return;
        
        List<NetworkPlayerInfo> list = playerListOrdering.<NetworkPlayerInfo>sortedCopy(playerInfoMap);
        
        NetworkPlayerInfo playerInfo = list.get(index);
        IChatComponent displayName = playerInfo.getDisplayName();
        String unformattedName = displayName != null ? displayName.getUnformattedText() : "";
        playerListEntryConsumer.accept(unformattedName);
    }
    
    private static boolean parseNamedIntValue(String playerListLine, String valueName, ParsedValue<Integer> valueContainer)
    {
        // ScathaPro.getInstance().logDebug("Checking for player list int value \"" + valueName + "\"");
        if (playerListLine.substring(1, valueName.length() + 1).equalsIgnoreCase(valueName))
        {
            String valueString = playerListLine.substring(valueName.length() + 3);
            
            // ScathaPro.getInstance().logDebug("Value found: " + valueString);
            
            // remove non-number characters from left
            while (valueString.length() > 0)
            {
                char firstChar = valueString.charAt(0);
                if (firstChar >= '0' && firstChar <= '9') break;
                valueString = valueString.substring(1);
            }
            
            // remove non-digit characters from right
            while (valueString.length() > 0)
            {
                char lastChar = valueString.charAt(valueString.length() - 1);
                if (lastChar >= '0' && lastChar <= '9') break;
                valueString = valueString.substring(0, valueString.length() - 1);
            }
            
            // ScathaPro.getInstance().logDebug("Value before int parsing: " + valueString);
            
            if (!valueString.isEmpty())
            {
                try
                {
                    valueContainer.setValue(Integer.parseInt(valueString));
                    return true;
                }
                catch (NumberFormatException exception) {}
            }
        }
        // else ScathaPro.getInstance().logDebug("Value not found");
        
        return false;
    }
    
    
    private PlayerListParser() {}
}
