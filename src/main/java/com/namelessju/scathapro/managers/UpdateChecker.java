package com.namelessju.scathapro.managers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.namelessju.scathapro.util.TextUtil;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.util.JsonUtil;

import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

public class UpdateChecker
{
    private static final String MODRINTH_PROJECT_ID = "lPe25xOt";
    private static final String MODRINTH_API_VERSIONS_ENDPOINT = "https://api.modrinth.com/v2/project/"+MODRINTH_PROJECT_ID+"/version";
    private static final String MODRINTH_VERSIONS_BASE_URL = "https://modrinth.com/mod/"+MODRINTH_PROJECT_ID+"/version/";
    
    
    public static void checkForUpdate(final boolean showAllResults)
    {
        final String modLoader = "forge";
        final String mcVersion = "1.8.9";
        
        new Thread(new Runnable() {
            public void run()
            {
                try
                {
                    HttpURLConnection connection = (HttpURLConnection) new URL(MODRINTH_API_VERSIONS_ENDPOINT + "?loaders=[%22"+modLoader+"%22]&game_versions=[%22"+mcVersion+"%22]").openConnection();
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
                    connection.setDoOutput(false);
                    connection.setRequestProperty("User-Agent", "NamelessJu/" + ScathaPro.TRUE_MODNAME + "/" + ScathaPro.VERSION);
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Accept-Charset", "utf-8");
                    connection.connect();
                    
                    int code = connection.getResponseCode();
                    
                    if (code == 200)
                    {
                        StringBuilder response = new StringBuilder();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String line;
                        while ((line = reader.readLine()) != null)
                        {
                            response.append(line + "\n");
                        }
                        
                        connection.disconnect();
                        
                        JsonElement json = new JsonParser().parse(response.toString());
                        
                        if (json.isJsonArray())
                        {
                            String latestVersion = null;
                            
                            Iterator<JsonElement> versionsIterator = json.getAsJsonArray().iterator();
                            while (versionsIterator.hasNext())
                            {
                                JsonElement versionElement = versionsIterator.next();
                                if (!versionElement.isJsonObject()) continue;
                                JsonObject versionObject = versionElement.getAsJsonObject();
                                
                                if (!"release".equals(JsonUtil.getString(versionObject, "version_type"))) continue;
                                if (!"listed".equals(JsonUtil.getString(versionObject, "status"))) continue;
                                
                                String version = JsonUtil.getString(versionObject, "version_number");
                                if (version == null) continue;
                                
                                int comparison = latestVersion != null ? compareVersions(latestVersion, version) : 1;
                                if (comparison > 0) latestVersion = version;
                            }

                            if (latestVersion != null)
                            {
                                int updateComparison = compareVersions(ScathaPro.VERSION, latestVersion);
                                
                                if (updateComparison > 0)
                                {
                                    String updateLink = MODRINTH_VERSIONS_BASE_URL + latestVersion;
                                    
                                    ChatComponentText updateNotice = new ChatComponentText(EnumChatFormatting.GOLD.toString() + "A newer " + ScathaPro.TRUE_MODNAME + " version (" + latestVersion + ") is available! You can download it ");
                                    ChatComponentText downloadLink = new ChatComponentText(EnumChatFormatting.BLUE.toString() + EnumChatFormatting.UNDERLINE + "here");
                                    /*
                                    if (updateLink != null)
                                    {
                                    */
                                    ChatStyle style = new ChatStyle()
                                            .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GRAY + updateLink)))
                                            .setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, updateLink));
                                    downloadLink.setChatStyle(style);
                                    /*
                                    }
                                    else
                                    {
                                        ChatStyle style = new ChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.RED + "No download link found")));
                                        downloadLink.setChatStyle(style);
                                    }
                                    */
                                    updateNotice.appendSibling(downloadLink);
                                    updateNotice.appendText(EnumChatFormatting.RESET.toString() + EnumChatFormatting.GOLD + EnumChatFormatting.ITALIC + ".");
                                    
                                    TextUtil.sendModChatMessage(updateNotice);
                                }
                                else
                                {
                                    if (showAllResults)
                                    {
                                        if (updateComparison < 0) TextUtil.sendModChatMessage(EnumChatFormatting.AQUA + "Your version is newer than the latest public release");
                                        else TextUtil.sendModChatMessage(EnumChatFormatting.GREEN + "You're using the newest version!");
                                    }
                                }
                                
                                return;
                            }
                            else ScathaPro.getInstance().logError("Couldn't check for update: no versions found on API");
                        }
                        else ScathaPro.getInstance().logError("Couldn't check for update: response is not in expected format");
                    }
                    else
                    {
                        StringBuilder response = new StringBuilder();
                        
                        try
                        {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                            String line;
                            while ((line = reader.readLine()) != null)
                            {
                                response.append(line + "\n");
                            }
                        }
                        catch (Exception e) {}
                        
                        connection.disconnect();
                        
                        ScathaPro.getInstance().logError("Couldn't check for update: response code " + code + " - " + (response.length() > 0 ? "response:\n" + response.toString() : "no error response"));
                    }
                }
                catch (Exception e)
                {
                    ScathaPro.getInstance().logError("Failed to check for update:");
                    e.printStackTrace();
                }
                
                TextUtil.sendModErrorMessage("Error while checking for update!");
            }
        }).start();
    }
    
    /**
     * @return If <code>to</code> is newer than <code>from</code> returns 1, if <code>to</code> is older than <code>from</code> returns -1, if both are the same returns 0
     */
    public static int compareVersions(String from, String to)
    {
        if (from == null || to == null) throw new NullPointerException("Versions cannot be null");
        
        if (from.equals(to)) return 0;
        
        from = getComparableVersion(from);
        to = getComparableVersion(to);
        
        String[] fromParts = from.split("[\\.-]");
        String[] toParts = to.split("[\\.-]");
        
        for (int i = 0; (i < fromParts.length || i < toParts.length); i ++)
        {
            int fromInt = 0;
            int toInt = 0;
            String fromString = null;
            String toString = null;
            
            
            if (i < fromParts.length)
            {
                if (fromParts[i].isEmpty()) fromInt = 0;
                else
                {
                    try
                    {
                        fromInt = Integer.parseInt(fromParts[i]);
                    }
                    catch (Exception e)
                    {
                        fromString = fromParts[i];
                    }
                }
            }
            
            if (i < toParts.length)
            {
                if (toParts[i].isEmpty()) toInt = 0;
                else
                {
                    try
                    {
                        toInt = Integer.parseInt(toParts[i]);
                    }
                    catch (Exception e)
                    {
                        toString = toParts[i];
                    }
                }
            }
            
            // Pre-releases
            boolean fromIsPreRelease = fromString != null && (fromString.equalsIgnoreCase("pre") || fromString.equalsIgnoreCase("prerelease") || fromString.equalsIgnoreCase("rc"));
            boolean toIsPreRelease = toString != null && (toString.equalsIgnoreCase("pre") || toString.equalsIgnoreCase("prerelease") || toString.equalsIgnoreCase("rc"));
            if (fromIsPreRelease && !toIsPreRelease) return 1;
            else if (!fromIsPreRelease && toIsPreRelease) return -1;
            else if (fromIsPreRelease && toIsPreRelease) continue;
            
            // both ints or both strings
            else if (fromInt >= 0 && toInt >= 0 && fromInt != toInt)
            {
                int comparison = (int) Math.signum(toInt - fromInt);
                if (comparison == 0) continue;
                else return comparison;
            }
            else if (fromString != null && toString != null)
            {
                int comparison = (int) Math.signum(toString.compareTo(fromString));
                if (comparison == 0) continue;
                else return comparison;
            }
            
            // string and int mixed
            else if (fromString == null && toString != null) return -1;
            else if (fromString != null && toString == null) return 1;
        }
        
        return 0;
    }
    
    private static String getComparableVersion(String version)
    {
        version = version.replaceAll("[,_\\-\\+]", ".");
        
        boolean previousCharacterIsNumerical = false;
        boolean previousCharacterIsDot = false;
        for (int i = 0; i < version.length(); i ++)
        {
            char c = version.charAt(i);
            
            if (c != '.')
            {
                boolean isNumerical = '0' <= c && c <= '9';
                
                if (i > 0 && !previousCharacterIsDot && previousCharacterIsNumerical != isNumerical)
                {
                    version = version.substring(0, i) + "." + version.substring(i);
                    i ++;
                }
                
                previousCharacterIsNumerical = isNumerical;
                previousCharacterIsDot = false;
            }
            else previousCharacterIsDot = true;
        }
        
        return version;
    }
}
