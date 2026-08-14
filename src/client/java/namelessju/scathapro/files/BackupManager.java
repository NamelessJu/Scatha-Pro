package namelessju.scathapro.files;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.util.FileUtil;
import net.minecraft.network.chat.*;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SuppressWarnings("ResultOfMethodCallIgnored") // ignore mkdirs() result warnings
public class BackupManager
{
    private final ScathaPro scathaPro;

    private final File backupDirectory;

    public BackupManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
        backupDirectory = scathaPro.getConfigDirectoryPath().resolve(ScathaPro.MOD_ID + "_backups").toFile();
    }

    public void backup()
    {
        backup(null);
    }

    public void backup(@Nullable String name)
    {
        File saveDirectory = scathaPro.getSaveDirectoryPath().toFile();
        if (!saveDirectory.exists()) return;
        File backupFile = getUniqueBackupFile(name, "zip");
        boolean success = FileUtil.zip(saveDirectory, backupFile.getAbsolutePath(), true);
        sendBackupMessage(success, backupFile);
    }

    public void backupPersistentData()
    {
        backupPersistentData(null);
    }

    public void backupPersistentData(String name)
    {
        File persistentDataFile = scathaPro.persistentData.getFile();
        if (!persistentDataFile.exists()) return;
        File backupFile = getUniqueBackupFile("persistentData"  + (name != null ? "_" + name : ""), "json");
        boolean success = FileUtil.copy(persistentDataFile, backupFile);
        sendBackupMessage(success, backupFile);
    }

    private File getUniqueBackupFile(String name, String extension)
    {
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd_HH-mm-ss");

        backupDirectory.mkdirs();

        return FileUtil.getUniqueFile(backupDirectory, date.format(formatter) + (name != null ? "_" + name : "") + "." + extension);
    }

    private void sendBackupMessage(boolean success, File backupFile)
    {
        if (success)
        {
            scathaPro.chatManager.sendChatMessage(Component.literal("Created backup as ")
                .append(Component.literal(backupFile.getName()).setStyle(Style.EMPTY
                    .withUnderlined(true)
                    .withHoverEvent(new HoverEvent.ShowText(Component.literal("Open backup folder").withColor(TextColor.GRAY)))
                    .withClickEvent(new ClickEvent.OpenFile(backupDirectory))
                )));
        }
        else
        {
            scathaPro.chatManager.sendChatErrorMessage("Couldn't create backup: Failed to write the file");
        }
    }
}