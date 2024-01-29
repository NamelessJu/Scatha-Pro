package com.namelessju.scathapro;

import java.util.HashMap;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

public class Variables
{
    public long lastWorldJoinTime = -1;
    public boolean inBedrockWallRange = false;
    public HashMap<Integer, Integer> previousScathaPets = null;
    
    public ItemStack lastProjectileWeaponUsed = null;
    
    public boolean showFakeBan = false;
    
    public long lastWormSpawnTime = -1;
    
    public int overallRegularWormKills = 0;
    public int overallScathaKills = 0;
    
    public int regularWormKills = 0;
    public int scathaKills = 0;
    
    // positive -> scatha streak, negative -> regular worm streak
    public int scathaStreak = 0;
    
    public long lastScathaKillTime = -1;
    
    public int rarePetDrops = 0;
    public int epicPetDrops = 0;
    public int legendaryPetDrops = 0;
    
    public int scathaKillsAtLastDrop = -1;
    
    public int hardstoneMined = 0;
    
    
    public GuiScreen openGuiNextTick = null;
}
