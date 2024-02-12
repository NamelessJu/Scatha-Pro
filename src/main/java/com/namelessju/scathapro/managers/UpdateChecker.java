package com.namelessju.scathapro.managers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.namelessju.scathapro.util.MessageUtil;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.util.JsonUtil;

import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

public class UpdateChecker
{
    private static final String GITHUB_API_RELEASES_LATEST_URL = "https://api.github.com/repos/NamelessJu/Scatha-Pro/releases/latest";
    private static final String MODRINTH_VERSIONS_URL = "https://modrinth.com/mod/scatha-pro/version/";
    
    
    public static void checkForUpdate(final boolean showAllResults)
    {
        new Thread(new Runnable() {
            public void run()
            {
                try
                {
                    URL url = new URL(GITHUB_API_RELEASES_LATEST_URL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();
        
                    int code = connection.getResponseCode();
                    
                    if (code == 200)
                    {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null)
                        {
                            response.append(line + "\n");
                        }
                        
                        connection.disconnect();

                        JsonElement json = new JsonParser().parse(response.toString());
                        
                        if (json.isJsonObject())
                        {
                            JsonObject versionObject = json.getAsJsonObject();
                            
                            String versionTag = JsonUtil.getString(versionObject, "tag_name");
                            if (versionTag == null) return;
                            
                            int comparison = compareVersions(ScathaPro.VERSION, versionTag);
                            if (comparison > 0)
                            {
                                // String updateLink = JsonUtil.getString(versionObject, "html_url");
                                String updateLink = MODRINTH_VERSIONS_URL + versionTag;
                                
                                ChatComponentText updateNotice = new ChatComponentText(EnumChatFormatting.GOLD.toString() + EnumChatFormatting.ITALIC + "A new version (" + versionTag + ") is available. You can download it ");
                                
                                ChatComponentText downloadLink = new ChatComponentText(EnumChatFormatting.BLUE.toString() + EnumChatFormatting.ITALIC + EnumChatFormatting.UNDERLINE + "here");
                                if (updateLink != null)
                                {
                                    ChatStyle style = new ChatStyle()
                                            .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GRAY + updateLink)))
                                            .setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, updateLink));
                                    downloadLink.setChatStyle(style);
                                }
                                /*
                                else
                                {
                                    ChatStyle style = new ChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.RED + "No download link found")));
                                    downloadLink.setChatStyle(style);
                                }
                                */
                                updateNotice.appendSibling(downloadLink);
                                
                                updateNotice.appendText(EnumChatFormatting.RESET.toString() + EnumChatFormatting.GOLD + EnumChatFormatting.ITALIC + ".");
                                
                                MessageUtil.sendModChatMessage(updateNotice);
                            }
                            else
                            {
                                if (showAllResults)
                                {
                                    if (comparison < 0) MessageUtil.sendModChatMessage("Your version is newer than the latest public release");
                                    else MessageUtil.sendModChatMessage(EnumChatFormatting.GREEN + "You're using the newest version!");
                                }
                            }
                            
                            return;
                        }
                        else ScathaPro.getInstance().logError("Couldn't check for update (response is not an object)");
                    }
                    else ScathaPro.getInstance().logError("Couldn't check for update (response code " + code + ")");
                }
                catch (MalformedURLException e)
                {
                    ScathaPro.getInstance().logError("Update checker URL is malformed");
                }
                catch (IOException e)
                {
                    ScathaPro.getInstance().logError("Couldn't read API response while checking for update");
                }
                
                MessageUtil.sendModErrorMessage("Error while checking for update!");
            }
        }).start();
    }
    
    /**
     * @return If <code>to</code> is newer than <code>from</code> returns 1, if <code>to</code> is older than <code>from</code> returns -1, if both are the same returns 0
     */
    public static int compareVersions(String from, String to)
    {
        from = getComparableVersion(from);
        to = getComparableVersion(to);
        
        String[] fromParts = from.split("\\.");
        String[] toParts = to.split("\\.");
        
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
            boolean fromIsPreRelease = fromString != null && (fromString.equalsIgnoreCase("pre") || fromString.equalsIgnoreCase("prerelease") || fromString.equalsIgnoreCase("pre-release"));
            boolean toIsPreRelease = toString != null && (toString.equalsIgnoreCase("pre") || toString.equalsIgnoreCase("prerelease") || toString.equalsIgnoreCase("pre-release"));
            if (fromIsPreRelease && !toIsPreRelease) return 1;
            else if (!fromIsPreRelease && toIsPreRelease) return -1;
            else if (fromIsPreRelease && toIsPreRelease) continue;
            
            // both ints or both strings
            else if (fromInt >= 0 && toInt >= 0 && fromInt != toInt) return (int) Math.signum(toInt - fromInt);
            else if (fromString != null && toString != null) return (int) Math.signum(toString.compareTo(fromString));
            
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
