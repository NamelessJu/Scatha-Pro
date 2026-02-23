package namelessju.scathapro.files.framework;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.util.FileUtil;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public abstract class ScathaProFile
{
    protected final ScathaPro scathaPro;
    private final Path relativeFilePath;
    
    public ScathaProFile(ScathaPro scathaPro, Path relativeFilePath)
    {
        this.scathaPro = scathaPro;
        this.relativeFilePath = relativeFilePath;
    }
    
    protected abstract void deserialize(@Nullable String content);
    protected abstract @NonNull String serialize();
    
    public File getFile()
    {
        return scathaPro.getSaveDirectoryPath().resolve(relativeFilePath).toFile();
    }
    
    public void load()
    {
        String fileContent = null;
        File file = getFile();
        try
        {
            fileContent = FileUtil.readFile(file);
        }
        catch (Exception e)
        {
            ScathaPro.LOGGER.error("Failed to load file \"{}\":", getClass().getSimpleName(), e);
        }
        deserialize(fileContent);
    }
    
    public void save()
    {
        String fileContent = serialize();
        try
        {
            FileUtil.writeFile(getFile(), fileContent);
        }
        catch (IOException e)
        {
            ScathaPro.LOGGER.error("Failed to save file \"{}\":", getClass().getSimpleName(), e);
        }
    }
}
