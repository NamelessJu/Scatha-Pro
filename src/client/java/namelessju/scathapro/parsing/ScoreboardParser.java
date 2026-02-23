package namelessju.scathapro.parsing;

import namelessju.scathapro.ScathaPro;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.util.StringDecomposer;
import net.minecraft.world.scores.*;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public final class ScoreboardParser
{
    private ScoreboardParser() {}
    
    private static final Comparator<PlayerScoreEntry> SCORE_DISPLAY_ORDER = Comparator.comparing(PlayerScoreEntry::value)
        .reversed()
        .thenComparing(PlayerScoreEntry::owner, String.CASE_INSENSITIVE_ORDER);
    
    public static Optional<Integer> parseHeat(Minecraft minecraft)
    {
        ScathaPro.LOGGER.debug("Parsing scoreboard heat value...");
        
        AtomicReference<Integer> heat = new AtomicReference<>();
        
        parse(minecraft, text -> {
            if (text.startsWith("Heat:"))
            {
                String valueString = text.substring(5).trim();
                
                // remove non-number characters from left
                while (!valueString.isEmpty())
                {
                    char firstChar = valueString.charAt(0);
                    if (firstChar >= '0' && firstChar <= '9') break;
                    if (valueString.startsWith("IMMUNE"))
                    {
                        valueString = null;
                        break;
                    }
                    valueString = valueString.substring(1).trim();
                }
                
                // remove non-digit characters from right
                while (valueString != null && !valueString.isEmpty())
                {
                    char lastChar = valueString.charAt(valueString.length() - 1);
                    if (lastChar >= '0' && lastChar <= '9') break;
                    valueString = valueString.substring(0, valueString.length() - 1).trim();
                }
                
                if (valueString != null && !valueString.isEmpty())
                {
                    try
                    {
                        heat.set(Integer.parseInt(valueString));
                        ScathaPro.LOGGER.debug("Scoreboard heat entry found - value: {}", heat);
                    }
                    catch (NumberFormatException exception)
                    {
                        ScathaPro.LOGGER.debug("Error while parsing scoreboard heat value: \"{}\" couldn't be parsed to an int", text);
                    }
                }
                else
                {
                    ScathaPro.LOGGER.debug("Scoreboard heat entry found, but has no int value: \"{}\"", text);
                }
                
                return true;
            }
            
            return false;
        });
        
        return Optional.ofNullable(heat.get());
    }
    
    private static void parse(Minecraft minecraft, Predicate<String> scorePredicate)
    {
        if (minecraft.getConnection() == null) return;
        
        ScathaPro.LOGGER.debug("Parsing scoreboard...");
        
        Scoreboard scoreboard = minecraft.getConnection().scoreboard();
        
        Objective sidebarObjective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (sidebarObjective != null)
        {
            ScathaPro.LOGGER.debug("Scoreboard objective found in sidebar: \"{}\"", sidebarObjective.getDisplayName());
            
            Component[] lines = scoreboard.listPlayerScores(sidebarObjective)
                .stream()
                .filter(playerScoreEntry -> !playerScoreEntry.isHidden())
                .sorted(SCORE_DISPLAY_ORDER)
                .limit(15L)
                .map(playerScoreEntry -> PlayerTeam.formatNameForTeam(
                    scoreboard.getPlayersTeam(playerScoreEntry.owner()),
                    playerScoreEntry.ownerName()
                ))
                .toArray(Component[]::new);
            for (Component line : lines)
            {
                String unformattedText = StringDecomposer.getPlainText(line);
                ScathaPro.LOGGER.debug("Scoreboard line: \"{}\"", unformattedText);
                if (scorePredicate.test(unformattedText)) return;
            }
        }
        else ScathaPro.LOGGER.debug("No scoreboard objective in sidebar found");
    }
}
