package com.namelessju.scathapro.managers;

import com.namelessju.scathapro.ScathaPro;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MouseHelper;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class InputManager
{
    private static final NoDeltaMouseHelper NO_DELTA_MOUSE_HELPER = new NoDeltaMouseHelper(); 
    
    private final ScathaPro scathaPro;
    
    private KeyBinding lockRotationKeybinding, toggleOverlayKeybinding, toggleRotationAnglesKeybinding;
    
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
    }
    
    public void onKeyInput()
    {
        if (lockRotationKeybinding.isPressed())
        {
            toggleCameraRotationLock();
            // MessageUtil.sendModChatMessage(EnumChatFormatting.GRAY + "Camera rotation " + (rotationLocked ? "locked" : "unlocked"));
        }
        else if (toggleOverlayKeybinding.isPressed())
        {
            scathaPro.getOverlay().toggleVisibility();
        }
        else if (toggleRotationAnglesKeybinding.isPressed())
        {
            boolean enabled = scathaPro.getConfig().getBoolean(Config.Key.showRotationAngles);
            scathaPro.getConfig().set(Config.Key.showRotationAngles, !enabled);
            scathaPro.getConfig().save();
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
        KeyBinding keybinding = new KeyBinding("key." + ScathaPro.MODID + "." + id, defaultKey, "key.categories." + ScathaPro.MODID + ".main");
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
