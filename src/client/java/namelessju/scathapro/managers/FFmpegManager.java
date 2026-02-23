package namelessju.scathapro.managers;

import namelessju.scathapro.ScathaPro;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class FFmpegManager
{
    private final ScathaPro scathaPro;
    private final String executablePath;
    private final boolean isWindows;
    
    public FFmpegManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
        isWindows = System.getProperty("os.name").startsWith("Windows");
        executablePath = searchForExecutablePath();
    }
    
    public void convertToOgg(String sourcePath, String targetPath, Consumer<Boolean> consumer)
    {
        runWithArgs(new String[] {"-y", "-i", sourcePath, "-map_metadata", "-1", "-map", "0:a", "-c:a", "libvorbis", targetPath}, consumer);
    }
    
    public void runWithArgs(String[] arguments, Consumer<Boolean> consumer)
    {
        CompletableFuture.supplyAsync(() -> execute(arguments)).thenAccept(consumer);
    }
    
    private boolean execute(String... arguments)
    {
        if (executablePath == null)
        {
            ScathaPro.LOGGER.debug("Couldn't execute FFmpeg: No installation found");
            return false;
        }
        
        try
        {
            String[] commandArray = Stream.concat(Stream.of(executablePath), Arrays.stream(arguments)).toArray(String[]::new);
            if (ScathaPro.LOGGER.isDebugEnabled())
            {
                ScathaPro.LOGGER.debug("Executing FFmpeg command: {}", String.join(" ", commandArray));
            }
            Process process = Runtime.getRuntime().exec(commandArray);
            
            StringBuilder errorMessageBuilder = new StringBuilder();
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream())))
            {
                for (String line; (line = errorReader.readLine()) != null;)
                {
                    if (!errorMessageBuilder.isEmpty()) errorMessageBuilder.append("\n");
                    errorMessageBuilder.append(line);
                }
            }
            if (process.waitFor() == 0) return true;
            
            ScathaPro.LOGGER.error("FFmpeg error:\n{}", errorMessageBuilder);
            return false;
        }
        catch (IOException | InterruptedException e)
        {
            ScathaPro.LOGGER.error("Failed to execute FFmpeg", e);
            return false;
        }
    }
    
    public boolean isFFmpegInstalled()
    {
        return executablePath != null;
    }
    
    private String searchForExecutablePath()
    {
        File ffmpegFile;
        
        // exe in mod folder
        File modFolder = new File(scathaPro.minecraft.gameDirectory, "mods");
        ffmpegFile = new File(modFolder, getOsSpecificExecutableFilename("scathapro-ffmpeg"));
        if (ffmpegFile.exists())
        {
            ScathaPro.LOGGER.info("FFmpeg installation found in mods folder");
            return ffmpegFile.getAbsolutePath();
        }
        
        // path variable
        String envPaths = System.getenv("PATH");
        for (String path : envPaths.split(";"))
        {
            if (path.isEmpty()) continue;
            ffmpegFile = new File(path, getOsSpecificExecutableFilename("ffmpeg"));
            if (ffmpegFile.exists())
            {
                ScathaPro.LOGGER.info("FFmpeg installation found in PATH environment variable");
                return ffmpegFile.getAbsolutePath();
            }
        }
        
        ScathaPro.LOGGER.warn("No FFmpeg installation found");
        return null;
    }
    
    private String getOsSpecificExecutableFilename(String fileName)
    {
        return isWindows ? fileName + ".exe" : fileName;
    }
}

