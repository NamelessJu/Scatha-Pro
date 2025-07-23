package namelessju.scathapro.miscellaneous;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import namelessju.scathapro.ScathaPro;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraftforge.fml.client.FMLClientHandler;

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
        
        Minecraft mc = Minecraft.getMinecraft();
        
        if (activeFileChooser == null) fullscreenBeforeOpeningFileDialog = mc.gameSettings.fullScreen;
        
        final JFrame thisFrame = new JFrame(title);
        try
        {
            IResourcePack resourcePack = FMLClientHandler.instance().getResourcePackFor(ScathaPro.MODID);
            if (resourcePack != null) thisFrame.setIconImage(resourcePack.getPackImage());
        } catch (IOException e) {}
        thisFrame.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        thisFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e)
            {
                if (activeFileChooser.dialogFrame == thisFrame)
                {
                    activeFileChooser = null;
                    if (!mc.gameSettings.fullScreen && fullscreenBeforeOpeningFileDialog) ScathaPro.getInstance().variables.runNextTick.add(new Runnable() {
                        @Override
                        public void run()
                        {
                            mc.toggleFullscreen();
                        }
                    });
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
        catch (Exception e) {}
        
        final JFileChooser fileChooser = systemLookFileChooser != null ? systemLookFileChooser : new CustomFileChooser();
        
        fileChooser.setDialogType(this.isSaveDialog ? JFileChooser.SAVE_DIALOG : JFileChooser.OPEN_DIALOG);
        if (this.isSaveDialog && this.suggestedFileName != null && this.suggestedFileName.length() > 0)
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
                    if (extensionsString.length() > 0) extensionsString.append(", ");
                    extensionsString.append("*." + ext);
                }
                if (extensionsString.length() > 0) return extensionsString.toString();
                return "Nothing";
            }
        });
        fileChooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION))
                {
                    File selectedFile = fileChooser.getSelectedFile();
                    fileChosenCallback.accept(selectedFile);
                }
                
                close();
            }
        });
        
        thisFrame.add(fileChooser);
        thisFrame.pack();
        
        if (activeFileChooser != null) activeFileChooser.dispose();
        activeFileChooser = this;
        
        this.dialogFrame = thisFrame;
        thisFrame.setVisible(true);
        thisFrame.setSize(800, 500);
        thisFrame.setLocationRelativeTo(null);
        
        if (mc.gameSettings.fullScreen) mc.toggleFullscreen();
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
    
    
    private class CustomFileChooser extends JFileChooser
    {
        private static final long serialVersionUID = 1L;
        
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
                catch (Exception e) {}
                if (result == null) result = showReplaceConfirmation();
                
                switch(result)
                {
                    case JOptionPane.YES_OPTION:
                        super.approveSelection();
                    default:
                        return;
                }
            }
            
            super.approveSelection();
        }
        
        private int showReplaceConfirmation()
        {
            return JOptionPane.showConfirmDialog(this, "A file with this name already exists at this location, do you want to replace it?", "Replace File", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
        }
    }
}
