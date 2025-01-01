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
    private static final NoDeltaMouseHelper NO_DELTA_MOUSE_HELPER = new NoDeltaMouseHelper(); 
    private static final String KEYBINDING_CATEGORY = "key.categories." + ScathaPro.MODID + ".main";
    
    private final ScathaPro scathaPro;
    
    private KeyBinding lockRotationKeybinding,
        toggleOverlayKeybinding,
        toggleRotationAnglesKeybinding,
        cycleSelectedWormStatsType;
    
    private boolean rotationLocked = false;
    private MouseHelper previousMouseHelper = null;
    
    public InputManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
    }
    
    public void register()
    {
        lockRotationKeybinding = registerKeybind("lockRotation");
        toggleOverlayKeybinding = registerKeybind("toggleOverlay");
        toggleRotationAnglesKeybinding = registerKeybind("toggleRotationAngles");
        cycleSelectedWormStatsType = registerKeybind("cycleSelectedWormStatsType");
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
        
        if (cycleSelectedWormStatsType.isPressed())
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
    }
    
    public void toggleCameraRotationLock()
    {
        if (rotationLocked) unlockCameraRotation();
        else lockCameraRotation();
    }
    
    public void lockCameraRotation()
    {
        if (scathaPro.getMinecraft().mouseHelper == NO_DELTA_MOUSE_HELPER) return;
        
        rotationLocked = true;
        
        previousMouseHelper = scathaPro.getMinecraft().mouseHelper;
        scathaPro.getMinecraft().mouseHelper = NO_DELTA_MOUSE_HELPER;
    }
    
    public void unlockCameraRotation()
    {
        if (scathaPro.getMinecraft().mouseHelper != NO_DELTA_MOUSE_HELPER) return;

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

    private KeyBinding registerKeybind(String id)
    {
        return registerKeybind(id, 0);
    }
    private KeyBinding registerKeybind(String id, int defaultKey)
    {
        KeyBinding keybinding = new KeyBinding("key." + ScathaPro.MODID + "." + id, defaultKey, KEYBINDING_CATEGORY);
        ClientRegistry.registerKeyBinding(keybinding);
        return keybinding;
    }
    
    
    public static boolean isScathaProKeybinding(KeyBinding keybinding)
    {
        return keybinding.getKeyCategory().equals(KEYBINDING_CATEGORY);
    }
    
    
    private static class NoDeltaMouseHelper extends MouseHelper
    {
        // Do nothing on mouse delta change to prevent input
        @Override
        public void mouseXYChange() { }
    }
}
