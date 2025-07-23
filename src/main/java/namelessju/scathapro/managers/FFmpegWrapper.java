package namelessju.scathapro.managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

import namelessju.scathapro.ScathaPro;
import net.minecraftforge.fml.common.Loader;

public class FFmpegWrapper
{
    private static String executablePath = null;
    private static boolean isWindows = false;
    
    static
    {
        isWindows = System.getProperty("os.name").startsWith("Windows");
        executablePath = searchForExecutablePath();
    }
    
    public static void convertToOgg(String sourcePath, String targetPath, Consumer<Boolean> consumer)
    {
        runWithArgs(new String[] {"-y", "-i", sourcePath, "-map_metadata", "-1", "-map", "0:a", "-c:a", "libvorbis", targetPath}, consumer);
    }
    
    public static void runWithArgs(String[] arguments, Consumer<Boolean> consumer)
    {
        CompletableFuture.supplyAsync(() -> execute(arguments)).thenAccept(consumer);
    }
    
    private static boolean execute(String... arguments)
    {
        if (executablePath == null)
        {
            ScathaPro.getInstance().logError("Couldn't execute FFmpeg: No installation found");
            return false;
        }
        
        try
        {
            String[] commandArray = Stream.concat(Stream.of(executablePath), Arrays.stream(arguments)).toArray(String[]::new);
            ScathaPro.getInstance().logDebug("Executing FFmpeg command: " + String.join(" ", commandArray));
            Process process = Runtime.getRuntime().exec(commandArray);
            
            StringBuilder errorMessageBuilder = new StringBuilder();
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream())))
            {
                for (String line; (line = errorReader.readLine()) != null;)
                {
                    if (errorMessageBuilder.length() > 0) errorMessageBuilder.append("\n");
                    errorMessageBuilder.append(line);
                }
            }
            if (process.waitFor() == 0) return true;
            
            ScathaPro.getInstance().logError(errorMessageBuilder.toString());
            return false;
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean isFFmpegInstalled()
    {
        return executablePath != null;
    }
    
    private static String searchForExecutablePath()
    {
        File ffmpegFile;
        
        // exe in mod folder
        File minecraftDir = Loader.instance().getConfigDir().getParentFile();
        File modFolder = new File(minecraftDir, "mods");
        ffmpegFile = new File(modFolder, getOsSpecificExecutableFilename("scathapro-ffmpeg"));
        if (ffmpegFile.exists())
        {
            String filePath = ffmpegFile.getAbsolutePath();
            ScathaPro.getInstance().log("FFmpeg installation found in mods folder (" + filePath + ")");
            return filePath;
        }
        
        // path variable
        String envPaths = System.getenv("PATH");
        for (String path : envPaths.split(";"))
        {
            if (path.isEmpty()) continue;
            ffmpegFile = new File(path, getOsSpecificExecutableFilename("ffmpeg"));
            if (ffmpegFile.exists())
            {
                String filePath = ffmpegFile.getAbsolutePath();
                ScathaPro.getInstance().log("FFmpeg installation found in PATH environment variable (" + filePath + ")");
                return filePath;
            }
        }
        
        ScathaPro.getInstance().logWarning("No FFmpeg installation found, custom mode will not support audio file conversions!");
        return null;
    }
    
    private static String getOsSpecificExecutableFilename(String fileName)
    {
        return isWindows ? fileName + ".exe" : fileName;
    }
}

