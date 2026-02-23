package namelessju.scathapro.util;

import namelessju.scathapro.ScathaPro;
import org.apache.commons.io.IOUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileUtil
{
    public static @Nullable String readFile(@NonNull File file) throws IOException
    {
        if (!file.exists() || !file.isFile() || !file.canRead()) return null;
        
        return readInputStream(new FileInputStream(file));
    }
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void writeFile(@NonNull File file, @NonNull String content) throws IOException
    {
        File directory = file.getParentFile();
        if (directory != null) directory.mkdirs();
        
        BufferedWriter bufferedWriter = null;
        try
        {
            FileOutputStream outputStream = new FileOutputStream(file);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            bufferedWriter.write(content);
        }
        finally
        {
            IOUtils.closeQuietly(bufferedWriter);
        }
    }
    
    public static String readInputStream(@Nullable InputStream inputStream) throws IOException
    {
        if (inputStream == null) return null;
        
        BufferedReader bufferedReader = null;
        try
        {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            
            StringBuilder stringBuilder = new StringBuilder();
            
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                if (!stringBuilder.isEmpty()) stringBuilder.append("\n");
                stringBuilder.append(line);
            }
            bufferedReader.close();
            
            return stringBuilder.toString();
        }
        finally
        {
            IOUtils.closeQuietly(bufferedReader);
        }
    }
    
    public static boolean deleteDirectoryRecursive(File directory)
    {
        File[] children = directory.listFiles();
        if (children != null)
        {
            for (File file : children)
            {
                deleteDirectoryRecursive(file);
            }
        }
        
        return directory.delete();
    }
    
    public static boolean move(File source, File destination)
    {
        File directory = destination.getParentFile();
        if (!directory.exists())
        {
            if (!directory.mkdirs()) return false;
        }
        
        return source.renameTo(destination);
    }
    
    public static void moveToDirectory(File source, File directory)
    {
        move(source, new File(directory, source.getName()));
    }
    
    public static boolean copy(File source, File copyFile)
    {
        try
        {
            Files.copy(source.toPath(), copyFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
    }
    
    /**
     * Returns a file reference that makes sure the file doesn't exist yet by adding a number to the end of the file name if required
     */
    public static File getUniqueFile(File directory, String fileName)
    {
        String fileExtension = "";
        int fileExtensionIndex = fileName.lastIndexOf('.');
        if (fileExtensionIndex >= 0)
        {
            fileExtension = fileName.substring(fileExtensionIndex);
            fileName = fileName.substring(0, fileExtensionIndex);
        }
        
        File file = null;
        int v = 0;
        while (file == null)
        {
            File currentFile = new File(directory, fileName + ((++v) > 1 ? "_" + v : "") + fileExtension);
            if (!currentFile.exists()) file = currentFile;
        }
        return file;
    }
    
    
    public static boolean unzip(File zipFile, Path targetDirectory, Predicate<String> zipEntryFilter) {
        targetDirectory = targetDirectory.toAbsolutePath();
        File targetDirectoryFile = targetDirectory.toFile();
        if (targetDirectoryFile.exists() && !targetDirectoryFile.isDirectory())
        {
            ScathaPro.LOGGER.error("Couldn't unzip zip file: target path is not a directory: {}", targetDirectory);
            return false;
        }
        
        ZipInputStream zipInputStream = null;
        try
        {
            zipInputStream = new ZipInputStream(new FileInputStream(zipFile));
            for (ZipEntry zipEntry; (zipEntry = zipInputStream.getNextEntry()) != null;) {
                if (zipEntryFilter != null && !zipEntryFilter.test(zipEntry.getName())) continue;
                
                Path resolvedPath = targetDirectory.resolve(zipEntry.getName()).normalize();
                if (!resolvedPath.startsWith(targetDirectory))
                {
                    ScathaPro.LOGGER.warn("Encountered and skipped zip-file entry with illegal path: {}", zipEntry.getName());
                    continue;
                }
                
                if (zipEntry.isDirectory())
                {
                    Files.createDirectories(resolvedPath);
                }
                else
                {
                    Files.createDirectories(resolvedPath.getParent());
                    Files.copy(zipInputStream, resolvedPath);
                }
            }
            
            zipInputStream.close();
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        if (zipInputStream != null)
        {
            try
            {
                zipInputStream.close();
            }
            catch (IOException ignored) { }
        }
        
        return false;
    }
    
    public static boolean zip(File sourcePath, String targetPath, boolean includeInitialDirectory)
    {
        ZipOutputStream zos = null;
        try
        {
            zos = new ZipOutputStream(new FileOutputStream(targetPath));
            if (sourcePath.isDirectory()) zipDirectory(sourcePath, includeInitialDirectory ? "" : null, zos);
            else zipFile(sourcePath, null, zos);
            zos.flush();
            zos.close();
            
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        if (zos != null)
        {
            try
            {
                zos.close();
            }
            catch (IOException ignored) {}
        }
        
        return false;
    }

    private static void zipDirectory(File folder, String parentDirectory, ZipOutputStream zos) throws IOException
    {
        String newPath;
        if (parentDirectory != null)
        {
            newPath = (!parentDirectory.isEmpty() ? parentDirectory + "/" : "") + folder.getName();
        }
        else newPath = "";
        
        File[] files = folder.listFiles();
        if (files == null)
        {
            ScathaPro.LOGGER.error("Couldn't zip folder: failed to list files for: {}", folder.getAbsolutePath());
            return;
        }
        for (File file : files)
        {
            if (file.isDirectory()) zipDirectory(file, newPath, zos);
            else zipFile(file, newPath, zos);
        }
    }
    
    private static void zipFile(File file, String parentDirectory, ZipOutputStream zos) throws IOException
    {
        zos.putNextEntry(new ZipEntry((parentDirectory != null && !parentDirectory.isEmpty() ? parentDirectory + "/" : "") + file.getName()));
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        byte[] byteBuffer = new byte[1024];
        int read;
        while ((read = bis.read(byteBuffer)) != -1) zos.write(byteBuffer, 0, read);
        bis.close();
        zos.closeEntry();
    }
    
    public static void openFileInExplorer(File file) throws IOException
    {
        if (file.isFile())
        {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win"))
            {
                new ProcessBuilder("explorer.exe", "/select,", file.getAbsolutePath()).start();
            }
            else if (os.contains("mac"))
            {
                new ProcessBuilder("open", "-R", file.getAbsolutePath()).start();
            }
            else Desktop.getDesktop().open(file.getParentFile());
        }
        else Desktop.getDesktop().open(file);
    }
    
    
    private FileUtil() {}
}
