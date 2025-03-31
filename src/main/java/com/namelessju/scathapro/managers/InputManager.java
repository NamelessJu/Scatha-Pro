package com.namelessju.scathapro.managers;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.miscellaneous.enums.WormStatsType;
import com.namelessju.scathapro.util.TextUtil;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MouseHelper;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class InputManager
{
    private static final String KEYBINDING_CATEGORY_ROOT = "key.categories." + ScathaPro.MODID + ".";
    
    private final ScathaPro scathaPro;
    
    public static enum KeybindingCategory
    {
        MAIN("main"),
        SCREENSHOTS("screenshots");
        
        private String categoryKey;
        
        KeybindingCategory(String categoryKey)
        {
            this.categoryKey = categoryKey;
        }
        
        public String getCategoryID()
        {
            return KEYBINDING_CATEGORY_ROOT + categoryKey;
        }
    }
    
    
    private KeyBinding lockRotationKeybinding;
    private KeyBinding toggleOverlayKeybinding;
    private KeyBinding toggleRotationAnglesKeybinding;
    private KeyBinding cycleSelectedWormStatsTypeKeybinding;
    
    private KeyBinding overlayScreenshotKeybinding;
    private KeyBinding chatScreenshotKeybinding;

    
    private final NoDeltaMouseHelper noDeltaMouseHelper = new NoDeltaMouseHelper(); 
    private boolean rotationLocked = false;
    private MouseHelper previousMouseHelper = null;
    
    public InputManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
    }
    
    public void register()
    {
        lockRotationKeybinding = registerKeybind("lockRotation", KeybindingCategory.MAIN);
        toggleOverlayKeybinding = registerKeybind("toggleOverlay", KeybindingCategory.MAIN);
        toggleRotationAnglesKeybinding = registerKeybind("toggleRotationAngles", KeybindingCategory.MAIN);
        cycleSelectedWormStatsTypeKeybinding = registerKeybind("cycleSelectedWormStatsType", KeybindingCategory.MAIN);
        
        overlayScreenshotKeybinding = registerKeybind("overlayScreenshot", KeybindingCategory.SCREENSHOTS);
        chatScreenshotKeybinding = registerKeybind("chatScreenshot", KeybindingCategory.SCREENSHOTS);
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
    
    private KeyBinding registerKeybind(String id, KeybindingCategory category)
    {
        return registerKeybind(id, category, 0);
    }
    private KeyBinding registerKeybind(String id, KeybindingCategory category, int defaultKey)
    {
        KeyBinding keybinding = new KeyBinding("key." + ScathaPro.MODID + "." + id, defaultKey, category.getCategoryID());
        ClientRegistry.registerKeyBinding(keybinding);
        return keybinding;
    }
    
    
    public static boolean isScathaProKeybinding(KeyBinding keybinding)
    {
        return keybinding.getKeyCategory().startsWith(KEYBINDING_CATEGORY_ROOT);
    }
    
    
    private static class NoDeltaMouseHelper extends MouseHelper
    {
        // Do nothing on mouse delta change to prevent input
        @Override
        public void mouseXYChange() { }
    }
}
