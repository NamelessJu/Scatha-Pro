package namelessju.scathapro.miscellaneous;

import namelessju.scathapro.ScathaPro;
import net.minecraft.client.Minecraft;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.function.Consumer;

public class FileChooser
{
    private static FileChooser activeFileChooser = null;
    private static boolean fullscreenBeforeOpeningFileDialog = false;
    
    public static void closeActiveDialog()
    {
        if (activeFileChooser != null) activeFileChooser.close();
    }
    
    
    public String title;
    public String[] allowedFileExtensions;
    public Consumer<File> fileChosenCallback;

    private boolean isSaveDialog = false;
    private String suggestedFileName;
    
    private JFrame dialogFrame = null;
    
    public FileChooser(String title, String[] allowedFileExtensions, Consumer<File> fileChosenCallback)
    {
        this.title = title;
        this.allowedFileExtensions = allowedFileExtensions;
        this.fileChosenCallback = fileChosenCallback;
    }
    
    public FileChooser makeSaveDialog(String suggestedFileName)
    {
        isSaveDialog = true;
        this.suggestedFileName = suggestedFileName;
        return this;
    }
    
    public void disableSaveDialog()
    {
        isSaveDialog = false;
    }
    
    public void show()
    {
        if (title == null || fileChosenCallback == null)
        {
            throw new IllegalStateException("FileChooser doesn't have all required fields set");
        }
        
        Minecraft minecraft = Minecraft.getInstance();
        
        if (activeFileChooser == null) fullscreenBeforeOpeningFileDialog = minecraft.getWindow().isFullscreen();
        
        final JFrame thisFrame = new JFrame(title);
        thisFrame.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        thisFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e)
            {
                if (activeFileChooser.dialogFrame == thisFrame)
                {
                    activeFileChooser = null;
                    if (!minecraft.getWindow().isFullscreen() && fullscreenBeforeOpeningFileDialog)
                        ScathaPro.getInstance().runNextTick(() -> minecraft.getWindow().toggleFullScreen());
                    fullscreenBeforeOpeningFileDialog = false;
                }
                
                dispose();
            }
        });
        
        JFileChooser systemLookFileChooser = null;
        try
        {
            LookAndFeel previousLookAndFeel = UIManager.getLookAndFeel();
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            systemLookFileChooser = new CustomFileChooser();
            UIManager.setLookAndFeel(previousLookAndFeel);
        }
        catch (Exception ignored) {}
        
        final JFileChooser fileChooser = systemLookFileChooser != null ? systemLookFileChooser : new CustomFileChooser();
        
        fileChooser.setDialogType(this.isSaveDialog ? JFileChooser.SAVE_DIALOG : JFileChooser.OPEN_DIALOG);
        if (this.isSaveDialog && this.suggestedFileName != null && !this.suggestedFileName.isEmpty())
        {
            fileChooser.setSelectedFile(new File(this.suggestedFileName));
        }

        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f)
            {
                if (f.isDirectory()) return true;
                for (String ext : allowedFileExtensions)
                {
                    if (ext == null) continue;
                    ext = ext.replace(".", "");
                    if (f.getName().endsWith("." + ext)) return true;
                }
                return false;
            }

            @Override
            public String getDescription()
            {
                StringBuilder extensionsString = new StringBuilder();
                for (String ext : allowedFileExtensions)
                {
                    if (ext == null) continue;
                    ext = ext.replace(".", "");
                    if (!extensionsString.isEmpty()) extensionsString.append(", ");
                    extensionsString.append("*.").append(ext);
                }
                if (!extensionsString.isEmpty()) return extensionsString.toString();
                return "Nothing";
            }
        });
        fileChooser.addActionListener(e -> {
            if (e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION))
            {
                File selectedFile = fileChooser.getSelectedFile();
                fileChosenCallback.accept(selectedFile);
            }
            
            close();
        });
        
        thisFrame.add(fileChooser);
        thisFrame.pack();
        
        if (activeFileChooser != null) activeFileChooser.dispose();
        activeFileChooser = this;
        
        this.dialogFrame = thisFrame;
        thisFrame.setVisible(true);
        thisFrame.setSize(800, 500);
        thisFrame.setLocationRelativeTo(null);
        
        if (minecraft.getWindow().isFullscreen()) minecraft.getWindow().toggleFullScreen();
        thisFrame.toFront();
        thisFrame.setAlwaysOnTop(true);
    }
    
    public void close()
    {
        if (dialogFrame == null) return;
        dialogFrame.dispatchEvent(new WindowEvent(dialogFrame, WindowEvent.WINDOW_CLOSING));
    }
    
    public void dispose()
    {
        if (dialogFrame != null)
        {
            if (activeFileChooser != null && activeFileChooser.dialogFrame == dialogFrame)
            {
                activeFileChooser = null;
            }
            
            dialogFrame.dispose();
            dialogFrame = null;
        }
    }
    
    
    private static class CustomFileChooser extends JFileChooser
    {
        @Override
        public void approveSelection()
        {
            File file = getSelectedFile();
            if (file.exists() && getDialogType() == SAVE_DIALOG)
            {
                Integer result = null;
                try
                {
                    LookAndFeel previousLookAndFeel = UIManager.getLookAndFeel();
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    result = showReplaceConfirmation();
                    UIManager.setLookAndFeel(previousLookAndFeel);
                }
                catch (Exception ignored) {}
                if (result == null) result = showReplaceConfirmation();
                
                if (result == JOptionPane.YES_OPTION)
                {
                    super.approveSelection();
                }
                return;
            }
            
            super.approveSelection();
        }
        
        private int showReplaceConfirmation()
        {
            return JOptionPane.showConfirmDialog(this, "A file with this name already exists at this location, do you want to replace it?", "Replace File", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
        }
    }
}
