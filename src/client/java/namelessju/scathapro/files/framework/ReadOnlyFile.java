package namelessju.scathapro.files.framework;

import namelessju.scathapro.ScathaPro;
import org.apache.commons.lang3.NotImplementedException;
import org.jspecify.annotations.NonNull;

import java.io.File;

public abstract class ReadOnlyFile extends ScathaProFile
{
    public ReadOnlyFile(ScathaPro scathaPro, File file)
    {
        super(scathaPro, file);
    }

    @Override
    protected @NonNull String serialize()
    {
        throw new NotImplementedException("This file cannot be saved");
    }
}