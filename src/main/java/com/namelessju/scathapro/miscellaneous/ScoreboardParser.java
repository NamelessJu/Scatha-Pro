package com.namelessju.scathapro.miscellaneous;

import java.util.Collection;
import java.util.function.Predicate;

import com.namelessju.scathapro.ScathaPro;

import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.StringUtils;

public class ScoreboardParser
{
    private static ScathaPro scathaPro = ScathaPro.getInstance();
    
    private static class ValueContainer<T>
    {
        private T value = null;
        
        public T getValue(T nullValue)
        {
            return value != null ? value : nullValue;
        }
        
        public void setValue(T value)
        {
            this.value = value;
        }
    }
    
    public static int parseHeat()
    {
        // scathaPro.logDebug("Parsing scoreboard heat value...");
        
        ValueContainer<Integer> heat = new ValueContainer<Integer>();
        
        parse(new Predicate<String>() {
            @Override
            public boolean test(String text)
            {
                if (text.startsWith("Heat:"))
                {
                    String valueString = text.substring(5).trim();
                    
                    // remove non-number characters from left
                    while (valueString.length() > 0)
                    {
                        char firstChar = valueString.charAt(0);
                        if (firstChar >= '0' && firstChar <= '9' && firstChar != '.') break;
                        if (valueString.startsWith("IMMUNE"))
                        {
                            valueString = null;
                            break;
                        }
                        valueString = valueString.substring(1).trim();
                    }
                    
                    // remove non-digit characters from right
                    while (valueString != null && valueString.length() > 0)
                    {
                        char lastChar = valueString.charAt(valueString.length() - 1);
                        if (lastChar >= '0' && lastChar <= '9') break;
                        valueString = valueString.substring(0, valueString.length() - 1).trim();
                    }
                    
                    if (valueString != null && !valueString.isEmpty())
                    {
                        try
                        {
                            heat.setValue(Integer.parseInt(valueString));
                            // scathaPro.logDebug("Scoreboard heat entry found - value: " + heat);
                        }
                        catch (NumberFormatException exception)
                        {
                            scathaPro.logDebug("Error while parsing scoreboard heat value: \"" + text + "\" couldn't be parsed to an int");
                        }
                    }
                    else
                    {
                        // scathaPro.logDebug("Scoreboard heat entry found, but has no int value: \"" + text + "\"");
                    }
                    
                    return true;
                }
                
                return false;
            }
        });
        
        return heat.getValue(-1);
    }
    
    private static void parse(Predicate<String> scorePredicate)
    {
        // scathaPro.logDebug("Parsing scoreboard...");
        
        Scoreboard scoreboard = scathaPro.getMinecraft().theWorld.getScoreboard();
        
        ScoreObjective sidebarObjective = scoreboard.getObjectiveInDisplaySlot(1);
        if (sidebarObjective != null)
        {
            // scathaPro.logDebug("Scoreboard objective found in sidebar: \"" + sidebarObjective.getDisplayName() + "\"");
            
            Collection<Score> scores = scoreboard.getSortedScores(sidebarObjective);
            for (Score score : scores)
            {
                String playerName = score.getPlayerName();
                ScorePlayerTeam playerTeam = scoreboard.getPlayersTeam(playerName);
                String formattedScoreText = ScorePlayerTeam.formatPlayerName(playerTeam, playerName);
                String unformattedText = StringUtils.stripControlCodes(formattedScoreText.replace(playerName, ""));
                
                // scathaPro.logDebug("Scoreboard line: \"" + unformattedText + "\"");
                
                if (scorePredicate.test(unformattedText)) return;
            }
        }
        // else scathaPro.logDebug("No scoreboard objective in sidebar found");
    }
    
    
    private ScoreboardParser() {}
}
