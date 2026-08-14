package namelessju.scathapro.files.framework;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.util.FileUtil;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public abstract class ScathaProFile
{
    protected final ScathaPro scathaPro;
    private final File file;

    public ScathaProFile(ScathaPro scathaPro, File file)
    {
        this.scathaPro = scathaPro;
        this.file = file;
    }

    protected abstract void deserialize(@Nullable String content);
    protected abstract @NonNull String serialize();

    public File getFile()
    {
        return file;
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