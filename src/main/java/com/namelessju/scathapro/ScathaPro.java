package com.namelessju.scathapro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.commands.ChancesCommand;
import com.namelessju.scathapro.commands.MainCommand;
import com.namelessju.scathapro.eventlisteners.APIListeners;
import com.namelessju.scathapro.eventlisteners.GuiListeners;
import com.namelessju.scathapro.eventlisteners.LoopListeners;
import com.namelessju.scathapro.eventlisteners.MiscListeners;
import com.namelessju.scathapro.commands.DevCommand;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = ScathaPro.MODID, version = ScathaPro.VERSION, name = ScathaPro.MODNAME, clientSideOnly = true)
public class ScathaPro
{
    public static final String MODNAME = "Scatha-Pro";
    public static final String MODID = "scathapro";
    public static final String VERSION = "1.2";
    
    public static final String CHATPREFIX = EnumChatFormatting.GRAY + MODNAME + ": " + EnumChatFormatting.RESET;
    
    
    @Instance(value = MODID)
    private static ScathaPro instance;

    public final Logger logger = LogManager.getLogger(MODID);
    
    
    public GuiScreen openGuiNextTick = null;
    
    public long lastWorldJoinTime = -1;
    public List<Integer> registeredWorms = new ArrayList<Integer>();
    public List<Worm> activeWorms = new ArrayList<Worm>();
    public boolean inBedrockWallRange = false;
    public HashMap<Integer, Integer> previousScathaPets = null;
    
    public ItemStack lastProjectileWeaponUsed = null;
    public long lastWormAttackTime = -1;

    public boolean showFakeBan = false;
    
    public int overallRegularWormKills = -1;
    public int overallScathaKills = -1;
    
    public int regularWormKills = 0;
    public int scathaKills = 0;
    
    public int wormStreak = 0; // positive -> scatha streak; negative -> regular worm streak
    
    public int rarePetDrops = 0;
    public int epicPetDrops = 0;
    public int legendaryPetDrops = 0;
    
    public long lastProfilesDataRequestTime = -1;
    public boolean repeatProfilesDataRequest = true;
    

    public static ScathaPro getInstance() {
        return instance;
    }
    
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new LoopListeners());
        MinecraftForge.EVENT_BUS.register(new APIListeners());
        MinecraftForge.EVENT_BUS.register(new GuiListeners());
        MinecraftForge.EVENT_BUS.register(new MiscListeners());
        
        ClientCommandHandler.instance.registerCommand(new MainCommand());
        ClientCommandHandler.instance.registerCommand(new ChancesCommand());
        ClientCommandHandler.instance.registerCommand(new DevCommand());
    }
    
    
    public void updateKillAchievements() {
        
        int lobbyWormKills = regularWormKills + scathaKills;
        Achievement.lobby_kills_1.setProgress(lobbyWormKills);
        Achievement.lobby_kills_2.setProgress(lobbyWormKills);
        Achievement.lobby_kills_3.setProgress(lobbyWormKills);
        
        int highestRegularWormKills = Math.max(lobbyWormKills, overallRegularWormKills + overallScathaKills);
        Achievement.worm_kills_1.setProgress(highestRegularWormKills);
        Achievement.worm_kills_2.setProgress(highestRegularWormKills);
        Achievement.worm_kills_3.setProgress(highestRegularWormKills);
        Achievement.worm_kills_4.setProgress(highestRegularWormKills);
        Achievement.worm_kills_5.setProgress(highestRegularWormKills);
        Achievement.worm_kills_6.setProgress(highestRegularWormKills);
        
        int highestScathaKills = Math.max(scathaKills, overallScathaKills);
        Achievement.scatha_kills_1.setProgress(highestScathaKills);
        Achievement.scatha_kills_2.setProgress(highestScathaKills);
        Achievement.scatha_kills_3.setProgress(highestScathaKills);
        Achievement.scatha_kills_4.setProgress(highestScathaKills);
        Achievement.scatha_kills_5.setProgress(highestScathaKills);
        Achievement.scatha_kills_6.setProgress(highestScathaKills);
    }
    
    public void updateSpawnAchievements() {
        int scathaStreak = Math.max(0, wormStreak);
        Achievement.scatha_streak_1.setProgress(scathaStreak);
        Achievement.scatha_streak_2.setProgress(scathaStreak);
        Achievement.scatha_streak_3.setProgress(scathaStreak);
        Achievement.scatha_streak_4.setProgress(scathaStreak);
        
        int regularWormStreak = Math.max(0, -wormStreak);
        Achievement.regular_worm_streak_1.setProgress(regularWormStreak);
        Achievement.regular_worm_streak_2.setProgress(regularWormStreak);
        Achievement.regular_worm_streak_3.setProgress(regularWormStreak);
    }
    
    public void updatePetDropAchievements() {
        Achievement.scatha_pet_drop_1_rare.setProgress(rarePetDrops);
        Achievement.scatha_pet_drop_2_rare.setProgress(rarePetDrops);
        Achievement.scatha_pet_drop_3_rare.setProgress(rarePetDrops);
        
        Achievement.scatha_pet_drop_1_epic.setProgress(epicPetDrops);
        Achievement.scatha_pet_drop_2_epic.setProgress(epicPetDrops);
        Achievement.scatha_pet_drop_3_epic.setProgress(epicPetDrops);
        
        Achievement.scatha_pet_drop_1_legendary.setProgress(legendaryPetDrops);
        Achievement.scatha_pet_drop_2_legendary.setProgress(legendaryPetDrops);
        Achievement.scatha_pet_drop_3_legendary.setProgress(legendaryPetDrops);
        
        Achievement.scatha_pet_drop_each.setProgress(
                (rarePetDrops > 0 ? 1 : 0)
                +
                (epicPetDrops > 0 ? 1 : 0)
                +
                (legendaryPetDrops > 0 ? 1 : 0)
        );
        
        int totalPetDrops = rarePetDrops + epicPetDrops + legendaryPetDrops;
        Achievement.scatha_pet_drop_any_1.setProgress(totalPetDrops);
        Achievement.scatha_pet_drop_any_2.setProgress(totalPetDrops);
    }
    
    
    public void resetPreviousScathaPets() {
        previousScathaPets = null;
    }
    
    public boolean profilesDataRequestNeeded() {
        return Util.inCrystalHollows() && (overallRegularWormKills < 0 || overallScathaKills < 0);
    }
}
