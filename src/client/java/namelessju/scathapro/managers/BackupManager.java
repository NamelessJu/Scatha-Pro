package namelessju.scathapro.managers;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.util.FileUtil;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;

@SuppressWarnings("ResultOfMethodCallIgnored") // ignore mkdirs() result warnings
public class BackupManager
{
    public static final Comparator<File> FILE_COMPARATOR_BY_MODIFIED_DESC = (a, b) -> Long.signum(b.lastModified() - a.lastModified());


    private final ScathaPro scathaPro;

    public final File backupDirectory;

    public BackupManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
        backupDirectory = scathaPro.getConfigDirectoryPath().resolve(ScathaPro.MOD_ID + "_backups").toFile();
    }

    public void backup()
    {
        backup(null, true);
    }

    public void backup(@Nullable String nameSuffix, boolean sendMessage)
    {
        File saveDirectory = scathaPro.getSaveDirectoryPath().toFile();
        if (!saveDirectory.exists()) return;
        File backupFile = getUniqueBackupFile("full", nameSuffix, "zip");
        boolean success = FileUtil.zip(saveDirectory, backupFile.getAbsolutePath(), true);

        int deletionCount = 0;
        int maxFullBackups = scathaPro.config.miscellaneous.fullBackupsLimit.get();
        if (maxFullBackups > 0 && success)
        {
            File[] otherFullBackupFiles = getBackupFiles(BackupType.FULL, file -> !file.equals(backupFile));
            if (otherFullBackupFiles != null)
            {
                Arrays.sort(otherFullBackupFiles, FILE_COMPARATOR_BY_MODIFIED_DESC);
                for (int i = maxFullBackups - 1; i < otherFullBackupFiles.length; i ++)
                {
                    if (otherFullBackupFiles[i].delete()) deletionCount ++;
                }
            }
        }

        if (sendMessage)
        {
            sendBackupMessage(success, backupFile);

            if (deletionCount >= 1)
            {
                scathaPro.chatManager.sendChatMessage(
                    Component.literal("Deleted " + deletionCount + " old full backup" + (deletionCount != 1 ? "s" : ""))
                        .withColor(TextColor.GRAY)
                );
            }
        }
    }

    public @Nullable File[] getBackupFiles(@Nullable BackupType typeFilter, @Nullable FileFilter filter)
    {
        return backupDirectory.listFiles(
            file -> (typeFilter == null || typeFilter == getBackupTypeFrom(file))
                && (filter == null || filter.accept(file))
        );
    }

    public void backupPersistentData()
    {
        backupPersistentData(null, true);
    }

    public void backupPersistentData(@Nullable String nameSuffix, boolean sendMessage)
    {
        File persistentDataFile = scathaPro.persistentData.getFile();
        if (!persistentDataFile.exists()) return;
        File backupFile = getUniqueBackupFile("persistentData", nameSuffix, "json");
        boolean success;
        try
        {
            Files.copy(persistentDataFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            success = true;
        }
        catch (IOException e)
        {
            ScathaPro.LOGGER.error("Failed to copy persistent data file for backup", e);
            success = false;
        }
        if (sendMessage) sendBackupMessage(success, backupFile);
    }

    private File getUniqueBackupFile(String baseName, @Nullable String nameSuffix, String extension)
    {
        backupDirectory.mkdirs();
        return FileUtil.getUniqueFile(backupDirectory,
            baseName + "_"
            + (nameSuffix != null ? nameSuffix : LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuu-MM-dd_HH-mm-ss")))
            + "." + extension
        );
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
        else scathaPro.chatManager.sendChatErrorMessage("Couldn't create backup: Failed to write the file");
    }

    public static @Nullable BackupType getBackupTypeFrom(File file)
    {
        String fileName = file.getName();
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex >= 0)
        {
            return switch (fileName.substring(lastDotIndex + 1))
            {
                case "zip" -> BackupManager.BackupType.FULL;
                case "json" -> BackupManager.BackupType.PERSISTENT_DATA;
                default -> null;
            };
        }
        else return null;
    }

    public void loadBackup(File file) throws Exception
    {
        switch (getBackupTypeFrom(file))
        {
            case FULL -> loadFullBackup(file);
            case PERSISTENT_DATA -> loadPersistentDataBackup(file);
            case null -> throw new Exception("File is not a backup");
        }
    }

    public void loadFullBackup(File file) throws IOException
    {
        if (!file.exists()) throw new IOException("File does not exist");

        Path scathaProDir = scathaPro.getSaveDirectoryPath();
        File scathaProDirFile = scathaProDir.toFile();
        if (scathaProDirFile.exists())
        {
            if (!FileUtil.deleteDirectoryRecursive(scathaProDirFile))
            {
                throw new IOException("Failed to delete existing save directory");
            }
        }
        if (!FileUtil.unzip(file, scathaProDir.getParent(), s -> s.startsWith("scathapro/")))
        {
            throw new IOException("Failed to extract backup zip file");
        }

        scathaPro.config.load();
        scathaPro.persistentData.load();
        scathaPro.persistentDataProfileManager.forceUpdate();
        scathaPro.customAlertModeManager.loadCurrentSubMode();
    }

    public void loadPersistentDataBackup(File file) throws IOException
    {
        if (!file.exists()) throw new IOException("File does not exist");

        Files.copy(file.toPath(), scathaPro.persistentData.getFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
        scathaPro.persistentData.load();
        scathaPro.persistentDataProfileManager.forceUpdate();
    }


    public enum BackupType
    {
        FULL("Full backup", "Contains all the data that the mod saves"),
        PERSISTENT_DATA("Persistent data", "Contains mostly profile dependent data like worm kills, pet drop amounts, achievements & more");

        public final String displayName;
        public final String tooltip;

        BackupType(String displayName, String tooltip)
        {
            this.displayName = displayName;
            this.tooltip = tooltip;
        }
    }
}