package namelessju.scathapro.files.framework;

import namelessju.scathapro.ScathaPro;
import org.jspecify.annotations.NonNull;

import java.io.File;

public abstract class ObjectRootJsonFile extends JsonFile<JsonFile.ObjectValue>
{
    public ObjectRootJsonFile(ScathaPro scathaPro, File file, boolean prettyPrintEnabled)
    {
        super(scathaPro, file, prettyPrintEnabled);
    }

    @Override
    protected @NonNull ObjectValue initializeRoot()
    {
        return new ObjectValue();
    }
}