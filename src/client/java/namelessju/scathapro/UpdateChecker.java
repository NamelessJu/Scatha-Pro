package namelessju.scathapro;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import namelessju.scathapro.util.JsonUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;

public class UpdateChecker
{
    private static final String MODRINTH_PROJECT_ID = "lPe25xOt";
    private static final String MODRINTH_API_VERSIONS_ENDPOINT = "https://api.modrinth.com/v2/project/"+MODRINTH_PROJECT_ID+"/version";
    private static final String MODRINTH_VERSIONS_BASE_URL = "https://modrinth.com/mod/"+MODRINTH_PROJECT_ID+"/version/";
    private static final String MOD_LOADER = "fabric";
    
    public static void checkForUpdate(ScathaPro scathaPro, final boolean sendNoUpdateAvailableMessages)
    {
        new Thread(() -> {
            try
            {
                HttpURLConnection connection = (HttpURLConnection) URI.create(MODRINTH_API_VERSIONS_ENDPOINT + "?loaders=[%22"+ MOD_LOADER +"%22]").toURL().openConnection();
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.setDoOutput(false);
                connection.setRequestProperty("User-Agent", "NamelessJu/" + ScathaPro.MOD_NAME + "/" + ScathaPro.MOD_VERSION);
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Accept-Charset", "utf-8");
                connection.connect();
                
                int code = connection.getResponseCode();
                
                StringBuilder response = new StringBuilder();
                if (code == 200)
                {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null)
                    {
                        response.append(line).append("\n");
                    }
                    
                    connection.disconnect();
                    
                    JsonElement json = JsonUtil.parseJson(response.toString());
                    if (json instanceof JsonArray jsonArray)
                    {
                        if (jsonArray.isEmpty())
                        {
                            if (sendNoUpdateAvailableMessages) scathaPro.runNextTick(() -> scathaPro.chatManager.sendChatMessage(
                                Component.literal("Couldn't check for update: Modrinth doesn't contain any versions")
                                    .withStyle(ChatFormatting.YELLOW)
                            ));
                            return;
                        }
                        
                        String latestVersion = null;
                        
                        for (JsonElement versionElement : jsonArray)
                        {
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
                            int updateComparison = compareVersions(ScathaPro.MOD_VERSION, latestVersion);
                            
                            if (updateComparison > 0)
                            {
                                String updateLink = MODRINTH_VERSIONS_BASE_URL + latestVersion;
                                
                                String finalLatestVersion = latestVersion;
                                scathaPro.runNextTick(() -> scathaPro.chatManager.sendChatMessage(Component.empty().withStyle(ChatFormatting.GOLD)
                                    .append("A newer " + ScathaPro.MOD_NAME + " version (" + finalLatestVersion + ") is available! You can download it ")
                                    .append(Component.literal("here").setStyle(Style.EMPTY
                                        .withColor(ChatFormatting.BLUE).withUnderlined(true)
                                        .withHoverEvent(new HoverEvent.ShowText(Component.literal(updateLink).withStyle(ChatFormatting.GRAY)))
                                        .withClickEvent(new ClickEvent.OpenUrl(URI.create(updateLink)))
                                    ))
                                    .append(".")
                                ));
                            }
                            else
                            {
                                if (sendNoUpdateAvailableMessages)
                                {
                                    if (updateComparison < 0) scathaPro.runNextTick(() -> scathaPro.chatManager.sendChatMessage(
                                        Component.literal("Your version is newer than the latest public release")
                                            .withStyle(ChatFormatting.AQUA)
                                    ));
                                    else scathaPro.runNextTick(() -> scathaPro.chatManager.sendChatMessage(
                                        Component.literal("You're using the newest version!")
                                            .withStyle(ChatFormatting.GREEN)
                                    ));
                                }
                            }
                            
                            return;
                        }
                        else ScathaPro.LOGGER.error("Couldn't check for update: no versions found on API");
                    }
                    else ScathaPro.LOGGER.error("Couldn't check for update: response is not in expected format");
                }
                else
                {
                    
                    try
                    {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                        String line;
                        while ((line = reader.readLine()) != null)
                        {
                            response.append(line).append("\n");
                        }
                    }
                    catch (Exception ignored) {}
                    
                    connection.disconnect();
                    
                    ScathaPro.LOGGER.error("Couldn't check for update: response code {} - {}", code, !response.isEmpty() ? "response:\n" + response : "no error response");
                }
            }
            catch (Exception e)
            {
                ScathaPro.LOGGER.error("Failed to check for update:\n{}", e.toString());
            }
            
            scathaPro.runNextTick(() -> scathaPro.chatManager.sendErrorChatMessage("Error while checking for update!"));
        }).start();
    }
    
    /**
     * @return If <code>to</code> is newer than <code>from</code> returns 1, if <code>to</code> is older than <code>from</code> returns -1, if both are the same returns 0
     */
    public static int compareVersions(String from, String to)
    {
        if (from == null || to == null) throw new NullPointerException("Versions cannot be null");
        
        if (from.equals(to)) return 0;
        
        from = getProcessableVersion(from);
        to = getProcessableVersion(to);
        
        String[] fromParts = from.split("\\.");
        String[] toParts = to.split("\\.");
        
        for (int i = 0; (i < fromParts.length || i < toParts.length); i ++)
        {
            int fromInt = 0;
            int toInt = 0;
            String fromString = null;
            String toString = null;
            
            
            if (i < fromParts.length && !fromParts[i].isEmpty())
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
            
            if (i < toParts.length && !toParts[i].isEmpty())
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
            
            // Pre-releases
            boolean fromIsPreRelease = isPartPreRelease(fromString);
            boolean toIsPreRelease = isPartPreRelease(toString);
            if (fromIsPreRelease && !toIsPreRelease) return 1;
            else if (!fromIsPreRelease && toIsPreRelease) return -1;
            else if (fromIsPreRelease && toIsPreRelease) continue;
            
            // both ints or both strings
            else if (fromInt >= 0 && toInt >= 0 && fromInt != toInt)
            {
                int comparison = (int) Math.signum(toInt - fromInt);
                if (comparison != 0) return comparison;
            }
            else if (fromString != null && toString != null)
            {
                int comparison = (int) Math.signum(toString.compareTo(fromString));
                if (comparison != 0) return comparison;
            }
            
            // string and int mixed
            else if (fromString == null && toString != null) return -1;
            else if (fromString != null && toString == null) return 1;
        }
        
        return 0;
    }
    
    public static boolean isPreRelease(String version)
    {
        if (version == null) return false;
        
        version = getProcessableVersion(version);
        String[] parts = version.split("\\.");
        
        for (String part : parts)
        {
            if (isPartPreRelease(part)) return true;
        }
        
        return false;
    }
    
    private static String getProcessableVersion(String version)
    {
        version = version.replaceAll("[,_\\-+]", ".");
        
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
    
    private static boolean isPartPreRelease(String part)
    {
        return part != null && (
            part.equalsIgnoreCase("pre") || part.equalsIgnoreCase("prerelease")
                || part.equalsIgnoreCase("rc") || part.equalsIgnoreCase("dev")
        );
    }
}
