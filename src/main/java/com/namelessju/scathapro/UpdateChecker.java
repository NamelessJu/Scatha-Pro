package com.namelessju.scathapro;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.logging.log4j.Level;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.namelessju.scathapro.util.MessageUtil;
import com.namelessju.scathapro.util.JsonUtil;

import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

public class UpdateChecker {
    
    private final static String releasesApiUrl = "https://api.github.com/repos/NamelessJu/Scatha-Pro/releases/latest";

    public static void checkForUpdate(final boolean showAllResults) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    URL url = new URL(releasesApiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();
        
                    int code = connection.getResponseCode();
                    
                    if (code == 200) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line + "\n");
                        }
                        
                        connection.disconnect();

                        JsonElement json = new JsonParser().parse(response.toString());
                        
                        if (json.isJsonObject()) {
                            JsonObject versionObject = json.getAsJsonObject();
                            
                            String versionTag = JsonUtil.getString(versionObject, "tag_name");
                            String updateUrl = JsonUtil.getString(versionObject, "html_url");
                            
                            if (versionTag != null) {
                            	int comparison = compareVersions(ScathaPro.VERSION, versionTag);
                            	
                                if (comparison > 0) {
                                    ChatComponentText updateNotice = new ChatComponentText(EnumChatFormatting.GOLD.toString() + EnumChatFormatting.ITALIC + "A new version (" + versionTag + ") is available. You can download it ");
                                    
                                    ChatComponentText downloadLink = new ChatComponentText(EnumChatFormatting.BLUE.toString() + EnumChatFormatting.ITALIC + EnumChatFormatting.UNDERLINE + "here");
                                    if (updateUrl != null) {
                                        ChatStyle style = new ChatStyle()
                                                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GRAY + updateUrl)))
                                                .setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, updateUrl));
                                        downloadLink.setChatStyle(style);
                                    }
                                    else {
                                        ChatStyle style = new ChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.RED + "No download found")));
                                        downloadLink.setChatStyle(style);
                                    }
                                    updateNotice.appendSibling(downloadLink);
                                    
                                    updateNotice.appendText(EnumChatFormatting.RESET.toString() + EnumChatFormatting.GOLD + EnumChatFormatting.ITALIC + ".");
                                    
                                    MessageUtil.sendModChatMessage(updateNotice);
                                }
                                else {
                                	if (showAllResults) {
                                    	if (comparison < 0) MessageUtil.sendModChatMessage("Your version is newer than the latest public release");
                                    	else MessageUtil.sendModChatMessage(EnumChatFormatting.GREEN + "You're using the newest version!");
                                	}
                                }
                            }
                            return; // Return with no error
                        }
                        else ScathaPro.getInstance().logger.log(Level.ERROR, "Couldn't check for update (response is not an object)");
                    }
                    else ScathaPro.getInstance().logger.log(Level.ERROR, "Couldn't check for update (response code " + code + ")");
                }
                catch (MalformedURLException e) {
                    ScathaPro.getInstance().logger.log(Level.ERROR, "Update checker URL is malformed");
                }
                catch (IOException e) {
                    ScathaPro.getInstance().logger.log(Level.ERROR, "Couldn't read API response while checking for update");
                }
                
                MessageUtil.sendModErrorMessage("Error while checking for update!");
            }
        }).start();
    }
    
    public static int compareVersions(String from, String to) {
        from = getComparableVersion(from);
        to = getComparableVersion(to);
        
        String[] fromParts = from.split("\\.");
        String[] toParts = to.split("\\.");
        
        for (int i = 0; (i < fromParts.length || i < toParts.length); i ++) {
            
            int fromInt = -1;
            int toInt = -1;
            String fromString = null;
            String toString = null;
            
            
            if (i < fromParts.length) {
                if (fromParts[i].isEmpty()) fromInt = 0;
                else {
                    try {
                        fromInt = Integer.parseInt(fromParts[i]);
                    }
                    catch (Exception e) {
                        fromString = fromParts[i];
                    }
                }
            }
            
            if (i < toParts.length) {
                if (toParts[i].isEmpty()) toInt = 0;
                else {
                    try {
                        toInt = Integer.parseInt(toParts[i]);
                    }
                    catch (Exception e) {
                        toString = toParts[i];
                    }
                }
            }
            
            // Pre-releases
            boolean fromIsPreRelease = fromString != null && (fromString.equalsIgnoreCase("pre") || fromString.equalsIgnoreCase("prerelease"));
            boolean toIsPreRelease = toString != null && (toString.equalsIgnoreCase("pre") || toString.equalsIgnoreCase("prerelease"));
            if (fromIsPreRelease && !toIsPreRelease) return 1;
            else if (!fromIsPreRelease && toIsPreRelease) return -1;
            else if (fromIsPreRelease && toIsPreRelease) continue;
            
            // from or to empty
            if (fromInt < 0 && fromString == null) return 1;
            else if (toInt < 0 && toString == null) return -1;
            
            // both ints or both strings
            else if (fromInt >= 0 && toInt >= 0 && fromInt != toInt) return (int) Math.signum(toInt - fromInt);
            else if (fromString != null && toString != null && !fromString.equals(toString)) return (int) Math.signum(toString.compareTo(fromString));
            
            // string and int mixed
            else if (fromInt >= 0 && toString != null) return -1;
            else if (fromString != null && toInt >= 0) return 1;
        }
        
        return 0;
    }
    
    private static String getComparableVersion(String version) {
        version = version.replaceAll("[,_\\-\\+]", ".");
        
        boolean previousCharacterIsNumerical = false;
        boolean previousCharacterIsDot = false;
        for (int i = 0; i < version.length(); i ++) {
            char c = version.charAt(i);
            
            if (c != '.') {
                boolean isNumerical = '0' <= c && c <= '9';
                
                if (i > 0 && !previousCharacterIsDot && previousCharacterIsNumerical != isNumerical) {
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
