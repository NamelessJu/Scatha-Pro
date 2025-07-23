package namelessju.scathapro.managers;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.miscellaneous.ScathaProKeyBinding;
import namelessju.scathapro.miscellaneous.enums.WormStatsType;
import namelessju.scathapro.util.TextUtil;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MouseHelper;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class InputManager
{
    private final ScathaPro scathaPro;
    

    public ScathaProKeyBinding lockRotationKeybinding;
    public ScathaProKeyBinding toggleOverlayKeybinding;
    public ScathaProKeyBinding cycleSelectedWormStatsTypeKeybinding;

    public ScathaProKeyBinding toggleRotationAnglesKeybinding;
    public ScathaProKeyBinding alternativeSensitivityKeybinding;
    
    public ScathaProKeyBinding overlayScreenshotKeybinding;
    public ScathaProKeyBinding chatScreenshotKeybinding;

    
    private final NoDeltaMouseHelper noDeltaMouseHelper = new NoDeltaMouseHelper(); 
    private boolean rotationLocked = false;
    private MouseHelper previousMouseHelper = null;
    
    public InputManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
    }
    
    public void register()
    {
        lockRotationKeybinding = registerKeybind("lockRotation", ScathaProKeyBinding.Category.MAIN);
        toggleOverlayKeybinding = registerKeybind("toggleOverlay", ScathaProKeyBinding.Category.MAIN);
        cycleSelectedWormStatsTypeKeybinding = registerKeybind("cycleSelectedWormStatsType", ScathaProKeyBinding.Category.MAIN);

        toggleRotationAnglesKeybinding = registerKeybind("toggleRotationAngles", ScathaProKeyBinding.Category.PLAYER_ROTATION);
        alternativeSensitivityKeybinding = registerKeybind("alternativeSensitivity", ScathaProKeyBinding.Category.PLAYER_ROTATION);
        
        overlayScreenshotKeybinding = registerKeybind("overlayScreenshot", ScathaProKeyBinding.Category.SCREENSHOTS);
        chatScreenshotKeybinding = registerKeybind("chatScreenshot", ScathaProKeyBinding.Category.SCREENSHOTS);
    }
    
    public void onKeyInput()
    {
        if (lockRotationKeybinding.isPressed())
        {
            toggleCameraRotationLock();
            return;
        }
        
        if (toggleOverlayKeybinding.isPressed())
        {
            if (scathaPro.getOverlay().isOverlayDrawAllowed())
            {
                scathaPro.getOverlay().toggleVisibility();
            }
            return;
        }
        
        if (toggleRotationAnglesKeybinding.isPressed())
        {
            boolean enabled = scathaPro.getConfig().getBoolean(Config.Key.showRotationAngles);
            scathaPro.getConfig().set(Config.Key.showRotationAngles, !enabled);
            scathaPro.getConfig().save();
            return;
        }
        
        if (cycleSelectedWormStatsTypeKeybinding.isPressed())
        {
            WormStatsType statsType = scathaPro.getConfig().getEnum(Config.Key.statsType, WormStatsType.class);
            if (statsType == null) statsType = WormStatsType.values()[0];
            
            int statsTypeIndex = statsType.ordinal();
            statsTypeIndex = (statsTypeIndex + 1) % WormStatsType.values().length;
            
            statsType = WormStatsType.values()[statsTypeIndex];
            
            Config config = scathaPro.getConfig();
            config.set(Config.Key.statsType, statsType.name());
            config.save();
            scathaPro.getOverlay().updateStatsType();
            
            TextUtil.sendModChatMessage(EnumChatFormatting.GRAY + "Changed overlay worm stats to: Per " + statsType);
            return;
        }
        
        if (overlayScreenshotKeybinding.isPressed())
        {
            ScreenshotManager.takeOverlayScreenshot();
            return;
        }
        
        if (chatScreenshotKeybinding.isPressed())
        {
            ScreenshotManager.takeChatScreenshot();
            return;
        }
    }
    
    public void toggleCameraRotationLock()
    {
        if (rotationLocked) unlockCameraRotation();
        else lockCameraRotation();
    }
    
    public void lockCameraRotation()
    {
        if (scathaPro.getMinecraft().mouseHelper == noDeltaMouseHelper) return;
        
        rotationLocked = true;
        
        previousMouseHelper = scathaPro.getMinecraft().mouseHelper;
        scathaPro.getMinecraft().mouseHelper = noDeltaMouseHelper;
    }
    
    public void unlockCameraRotation()
    {
        if (scathaPro.getMinecraft().mouseHelper != noDeltaMouseHelper) return;

        rotationLocked = false;
        
        scathaPro.getMinecraft().mouseHelper = previousMouseHelper;
        // Consume any stored mouse delta to prevent mouse snapping
        previousMouseHelper.mouseXYChange();
        previousMouseHelper = null;
    }
    
    public boolean isRotationLocked()
    {
        return rotationLocked;
    }
    
    private ScathaProKeyBinding registerKeybind(String id, ScathaProKeyBinding.Category category)
    {
        return registerKeybind(id, category, 0);
    }
    private ScathaProKeyBinding registerKeybind(String id, ScathaProKeyBinding.Category category, int defaultKey)
    {
        ScathaProKeyBinding keybinding = new ScathaProKeyBinding(id, defaultKey, category);
        ClientRegistry.registerKeyBinding(keybinding);
        return keybinding;
    }
    
    
    private static class NoDeltaMouseHelper extends MouseHelper
    {
        // Do nothing on mouse delta change to prevent input
        @Override
        public void mouseXYChange() { }
    }
}
