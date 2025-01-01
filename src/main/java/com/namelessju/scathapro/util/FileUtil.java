package com.namelessju.scathapro.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Charsets;
import com.namelessju.scathapro.ScathaPro;

public class FileUtil
{
    public static String readFile(File file)
    {
        if (file == null || !file.isFile() || !file.canRead()) return null;
        
        try
        {
            return readInputStream(new FileInputStream(file));
        }
        catch (FileNotFoundException e)
        {
            return null;
        }
    }
    
    public static boolean writeFile(File file, String content)
    {
        boolean success = false;
        
        BufferedWriter bufferedWriter = null;
        try
        {
            FileOutputStream outputStream = new FileOutputStream(file);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

            bufferedWriter.write(content);
            success = true;
        }
        catch (Exception e) { }
        finally
        {
            IOUtils.closeQuietly(bufferedWriter);
        }
        
        return success;
    }
    
    public static String readInputStream(InputStream inputStream)
    {
        if (inputStream == null) return null;
        
        BufferedReader bufferedReader = null;
        try
        {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, Charsets.UTF_8));
            
            StringBuilder stringBuilder = new StringBuilder();
            
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                stringBuilder.append(line);
            }
            bufferedReader.close();
            
            return stringBuilder.toString();
        }
        catch (Exception e)
        {
            return null;
        }
        finally
        {
            IOUtils.closeQuietly(bufferedReader);
        }
    }
    
    public static boolean deleteDirectoryRecursive(File directory)
    {
        File[] childs = directory.listFiles();
        if (childs != null)
        {
            for (File file : childs)
            {
                deleteDirectoryRecursive(file);
            }
        }
        
        return directory.delete();
    }
    
    public static boolean move(File source, File destination)
    {
        File directory = destination.getParentFile();
        if (!directory.exists()) directory.mkdirs();
        
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
    public static File getNonexistentFile(File directory, String fileName)
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
            ScathaPro.getInstance().logError("Couldn't unzip zip file: target path is not a directory: " + targetDirectory.toString());
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
                    ScathaPro.getInstance().logWarning("Encountered and skipped zip-file entry with illegal path: " + zipEntry.getName());
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

    private static void zipDirectory(File folder, String parentDirectory, ZipOutputStream zos) throws FileNotFoundException, IOException
    {
        String newPath;
        if (parentDirectory != null)
        {
            newPath = (parentDirectory.length() > 0 ? parentDirectory + "/" : "") + folder.getName();
        }
        else newPath = "";
        
        for (File file : folder.listFiles())
        {
            if (file.isDirectory()) zipDirectory(file, newPath, zos);
            else zipFile(file, newPath, zos);
        }
    }
    
    private static void zipFile(File file, String parentDirectory, ZipOutputStream zos) throws FileNotFoundException, IOException
    {
        zos.putNextEntry(new ZipEntry((parentDirectory != null && parentDirectory.length() > 0 ? parentDirectory + "/" : "") + file.getName()));
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        byte[] byteBuffer = new byte[1024];
        int read = 0;
        while ((read = bis.read(byteBuffer)) != -1) zos.write(byteBuffer, 0, read);
        bis.close();
        zos.closeEntry();
    }
    
    
    private FileUtil() {}
}
