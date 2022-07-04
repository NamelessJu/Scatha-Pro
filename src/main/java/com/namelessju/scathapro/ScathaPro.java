package com.namelessju.scathapro;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.achievements.AchievementManager;
import com.namelessju.scathapro.commands.ChancesCommand;
import com.namelessju.scathapro.commands.MainCommand;
import com.namelessju.scathapro.eventlisteners.APIListeners;
import com.namelessju.scathapro.eventlisteners.GuiListeners;
import com.namelessju.scathapro.eventlisteners.LoopListeners;
import com.namelessju.scathapro.commands.DevCommand;
import com.namelessju.scathapro.overlay.OverlayContainer;
import com.namelessju.scathapro.overlay.OverlayElement;
import com.namelessju.scathapro.overlay.OverlayImage;
import com.namelessju.scathapro.overlay.OverlayText;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = ScathaPro.MODID, version = ScathaPro.VERSION, name = ScathaPro.MODNAME, clientSideOnly = true)
public class ScathaPro
{
    public static final String MODNAME = "Scatha-Pro";
    public static final String MODID = "scathapro";
    public static final String VERSION = "1.2_PreRelease_3_DEV";
    
    public static final String CHATPREFIX = EnumChatFormatting.GRAY + MODNAME + ": " + EnumChatFormatting.RESET;
    
    private static final String persistentDataPetDropsKey = "petDrops";
    
    @Instance(value = MODID)
    private static ScathaPro instance;

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Config config = Config.getInstance();
    public final Logger logger = LogManager.getLogger(MODID);
    
    
    private final OverlayContainer uiOverlay;
    private final OverlayImage overlayScathaPetImage;
    private final OverlayContainer overlayKillsContainer;
    private final OverlayText overlayOverallWormKillsText;
    private final OverlayText overlayRegularWormKillsText;
    private final OverlayText overlayOverallScathaKillsText;
    private final OverlayText overlayScathaKillsText;
    private final OverlayText overlayOverallTotalKillsText;
    private final OverlayText overlayTotalKillsText;
    private final OverlayText overlayWormStreakText;
    private final OverlayText overlayCoordsText;
    private final OverlayText overlayDayText;
    private final OverlayText overlayDayWarningText;
    
    
    public GuiScreen openGuiNextTick = null;
    
    public long lastWorldJoinTime = -1;
    public List<Integer> registeredWorms = new ArrayList<Integer>();
    public List<Worm> activeWorms = new ArrayList<Worm>();
    public boolean inBedrockWallRange = false;
    public HashMap<Integer, Integer> previousScathaPets = null;
    
    public ItemStack lastProjectileWeaponUsed = null;
    public long lastWormAttackTime = -1;
    
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

    private long lastPreAlertTime = -1;
    
    private boolean persistentDataLoaded = false;
    

    public static ScathaPro getInstance() {
        return instance;
    }
    
    public ScathaPro() {
        
        // Setup UI overlay
        
        uiOverlay = new OverlayContainer(0, 0, 1f);
        uiOverlay.padding = 5;
        uiOverlay.backgroundColor = 0x50000000;
        
        OverlayContainer titleContainer = new OverlayContainer(0, 0, 1f);
        titleContainer.add(overlayScathaPetImage = new OverlayImage(null, 256, 256, 0, 0, 0.042f));
        
        titleContainer.add(new OverlayText("Scatha Farming:", Util.Color.GOLD.getValue(), 16, 0, 1.3f));
        
        uiOverlay.add(titleContainer);
        

        overlayKillsContainer = new OverlayContainer(0, 16, 1f);
        OverlayContainer overlayKillsSubContainer = new OverlayContainer(0, 0, 1f);
        
        OverlayText overlayWormKillsTitle = new OverlayText("Worms", Util.Color.YELLOW.getValue(), 20, 0, 1f);
        overlayWormKillsTitle.setAlignment(OverlayElement.Alignment.CENTER);
        overlayKillsSubContainer.add(overlayWormKillsTitle);
        overlayKillsSubContainer.add(new OverlayImage("overlay/worm.png", 512, 256, 0, 10, 0.08f));
        overlayKillsSubContainer.add(overlayRegularWormKillsText = new OverlayText(null, Util.Color.GRAY.getValue(), 20, 22, 1f));
        overlayRegularWormKillsText.setAlignment(OverlayElement.Alignment.CENTER);
        overlayKillsSubContainer.add(overlayOverallWormKillsText = new OverlayText(null, Util.Color.WHITE.getValue(), 20, 11, 1f));
        overlayOverallWormKillsText.setAlignment(OverlayElement.Alignment.CENTER);

        OverlayText overlayScathaKillsTitle = new OverlayText("Scathas", Util.Color.YELLOW.getValue(), 66, 0, 1f);
        overlayScathaKillsTitle.setAlignment(OverlayElement.Alignment.CENTER);
        overlayKillsSubContainer.add(overlayScathaKillsTitle);
        overlayKillsSubContainer.add(new OverlayImage("overlay/scatha.png", 512, 256, 46, 10, 0.08f));
        overlayKillsSubContainer.add(overlayScathaKillsText = new OverlayText(null, Util.Color.GRAY.getValue(), 66, 22, 1f));
        overlayScathaKillsText.setAlignment(OverlayElement.Alignment.CENTER);
        overlayKillsSubContainer.add(overlayOverallScathaKillsText = new OverlayText(null, Util.Color.WHITE.getValue(), 66, 11, 1f));
        overlayOverallScathaKillsText.setAlignment(OverlayElement.Alignment.CENTER);

        overlayKillsContainer.add(new OverlayText("Total", Util.Color.WHITE.getValue(), 95, 0, 1f));
        
        overlayKillsContainer.add(overlayOverallTotalKillsText = new OverlayText(null, Util.Color.WHITE.getValue(), 95, 11, 1f));
        overlayKillsContainer.add(overlayTotalKillsText = new OverlayText(null, Util.Color.GRAY.getValue(), 95, 22, 1f));
        
        overlayKillsContainer.add(overlayKillsSubContainer);
        uiOverlay.add(overlayKillsContainer);
        
        uiOverlay.add(overlayWormStreakText = new OverlayText(null, Util.Color.GRAY.getValue(), 0, 51, 1f));
        
        uiOverlay.add(overlayCoordsText = new OverlayText(null, Util.Color.GRAY.getValue(), 0, 64, 1f));
        
        uiOverlay.add(overlayDayText = new OverlayText(null, Util.Color.WHITE.getValue(), 0, 74, 1f));
        uiOverlay.add(overlayDayWarningText = new OverlayText(null, Util.Color.GRAY.getValue(), 0, 84, 1f));
    }
    
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new LoopListeners());;
        MinecraftForge.EVENT_BUS.register(new APIListeners());
        MinecraftForge.EVENT_BUS.register(new GuiListeners());
        
        ClientCommandHandler.instance.registerCommand(new MainCommand());
        ClientCommandHandler.instance.registerCommand(new ChancesCommand());
        ClientCommandHandler.instance.registerCommand(new DevCommand());
    }
    
    
    @SubscribeEvent
    public void onWorldJoin(EntityJoinWorldEvent e) {
        Entity entity = e.entity;

        if (entity == mc.thePlayer) {
            
            // Load data
            
            if (!persistentDataLoaded) {
                PersistentData.getInstance().loadData();
                AchievementManager.getInstance().loadAchievements();
                loadPetDrops();
                
                persistentDataLoaded = true;
            }
            
            // Reset
            
            wormStreak = 0;

            registeredWorms.clear();
            activeWorms.clear();
            
            regularWormKills = 0;
            scathaKills = 0;
            
            inBedrockWallRange = false;
            
            resetPreviousScathaPets();
            
            repeatProfilesDataRequest = true;
            
            lastWorldJoinTime = Util.getCurrentTime();
            
            // Update overlay
            
            updateOverlayFull();
            
            // Update achievements
            
            updateKillAchievements();
            updateSpawnAchievements();
            updatePetDropAchievements();
            
            Achievement.crystal_hollows_time_1.setProgress(0);
            Achievement.crystal_hollows_time_2.setProgress(0);
            Achievement.crystal_hollows_time_3.setProgress(0);
        }
    }
    
    @SubscribeEvent
    public void onAttack(AttackEntityEvent e) { // Worm attack detection
        if (e.target instanceof EntityArmorStand) {
            EntityArmorStand entity = (EntityArmorStand) e.target;
            World world = entity.worldObj;
            
            ItemStack helmetItem = entity.getEquipmentInSlot(4);
            if (helmetItem != null && Util.isWormSkull(helmetItem) || config.getBoolean(Config.Key.devMode)) {
                
                List<EntityArmorStand> nearbyArmorStands = world.getEntitiesWithinAABB(EntityArmorStand.class, entity.getEntityBoundingBox().expand(8f, 8f, 8f));
                
                for (int i = 0; i < nearbyArmorStands.size(); i ++) {
                    
                    EntityArmorStand armorStand = nearbyArmorStands.get(i);
                    
                    int entityID = armorStand.getEntityId();
                    Worm worm = Worm.getByID(entityID);
                    
                    if (worm != null) {
                        ItemStack weapon = null;
                        if (mc.thePlayer != null) weapon = mc.thePlayer.getHeldItem();
                        worm.attack(weapon);
                        break;
                    }
                }
            }
        }
    }
    
    @SubscribeEvent
    public void onInteractItem(PlayerInteractEvent e) {
        ItemStack heldItem = e.entityPlayer.getHeldItem();
        if (heldItem != null) {
            if (heldItem.getItem() == Items.fishing_rod || heldItem.getItem() == Items.bow)
                lastProjectileWeaponUsed = heldItem;
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onChatReceived(ClientChatReceivedEvent e) {
        if (e.type == 2) return;
        
        String unformattedText = StringUtils.stripControlCodes(e.message.getUnformattedText());
        
        // Automatically update API key when generating new one
        
        if (unformattedText.startsWith("Your new API key is ") && e.message.getSiblings().size() >= 1) {
            String apiKey = e.message.getSiblings().get(0).getChatStyle().getChatClickEvent().getValue();
            config.set(Config.Key.apiKey, apiKey);
            config.save();
            
            Util.sendModChatMessage("Automatically updated API key to " + apiKey);
            
            if (profilesDataRequestNeeded()) API.requestProfilesData();
        }
        
        // Add copy button
        
        Util.addChatCopyButton(e.message);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSound(PlaySoundEvent e) {
        long now = Util.getCurrentTime();
        if (config.getBoolean(Config.Key.wormPreAlert) && (Util.inCrystalHollows() && e.sound.getPitch() == 2.0952382f || config.getBoolean(Config.Key.devMode) && e.sound.getPitch() >= 2f) && e.name.equals("mob.spider.step") && now - lastPreAlertTime > 2500) {
            mc.ingameGUI.displayTitle(null, null, 0, 20, 5);
            mc.ingameGUI.displayTitle(null, EnumChatFormatting.YELLOW + "Worm about to spawn...", 0, 0, 0);
            mc.ingameGUI.displayTitle("", null, 0, 0, 0);
            
            if (!Util.playModeSound("alert.prespawn")) Util.playSoundAtPlayer("random.orb", 1f, 0.5f);
            
            lastPreAlertTime = now;
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTooltip(ItemTooltipEvent e) {
        if (config.getBoolean(Config.Key.devMode)) {
            ItemStack item = e.itemStack;
    
            if (item != null) {
                String skyblockItemID = Util.getSkyblockItemID(item);
                if (skyblockItemID != null) e.toolTip.add(EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY + skyblockItemID + EnumChatFormatting.RESET);
            }
        }
    }

    
    public void drawOverlay() {
        uiOverlay.draw();
    }
    
    public void updateOverlayFull() {
        updateOverlayPosition();
        updateOverlayScale();
        updateOverlayVisibility();
        
        updateOverlayScathaPetImage();

        updateOverlayWormKills();
        updateOverlayScathaKills();
        updateOverlayTotalKills();

        updateOverlayWormStreak();
        updateOverlayCoords();
        updateOverlayDay();
    }

    public void updateOverlayPosition() {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
    
        final double[] overlayPositionPercentage = {config.getDouble(Config.Key.overlayX), config.getDouble(Config.Key.overlayY)};
        final int[] overlayPosition = {
                overlayPositionPercentage[0] >= 0 ? (int) Math.round(scaledResolution.getScaledWidth() * overlayPositionPercentage[0]) : 10,
                overlayPositionPercentage[1] >= 0 ? (int) Math.round(scaledResolution.getScaledHeight() * overlayPositionPercentage[1]) : 10,
        };
        final int[] translation = {
                overlayPositionPercentage[0] >= 0 ? (int) Math.round(-uiOverlay.getWidth() * overlayPositionPercentage[0]) : 0,
                overlayPositionPercentage[1] >= 0 ? (int) Math.round(-uiOverlay.getHeight() * overlayPositionPercentage[1]) : 0
        };
        uiOverlay.setPosition(overlayPosition[0] + translation[0], overlayPosition[1] + translation[1]);
    
        if (overlayPositionPercentage[0] == 0.5) {
            uiOverlay.setContentAlignment(OverlayElement.Alignment.CENTER);
            overlayKillsContainer.setContentAlignment(OverlayElement.Alignment.LEFT);
        }
        else if (overlayPositionPercentage[0] > 0.5) {
            uiOverlay.setContentAlignment(OverlayElement.Alignment.RIGHT);
            overlayKillsContainer.setContentAlignment(OverlayElement.Alignment.RIGHT);
        }
        else {
            uiOverlay.setContentAlignment(OverlayElement.Alignment.LEFT);
            overlayKillsContainer.setContentAlignment(OverlayElement.Alignment.LEFT);
        }
    }
    
    public void updateOverlayScale() {
        uiOverlay.setScale((float) config.getDouble(Config.Key.overlayScale));
    }
    
    public void updateOverlayVisibility() {
        uiOverlay.setVisible(config.getBoolean(Config.Key.overlay));
    }
    
    public void updateOverlayScathaPetImage() {
        String petImage = "overlay/mode_icons/";
        
        switch (config.getInt(Config.Key.mode)) {
            case 1:
                petImage += "meme.png";
                break;
            case 2:
                petImage += "anime.png";
                break;
            default:
                petImage += "default.png";
                break;
        }

        overlayScathaPetImage.setImage(petImage, 256, 256);
    }
    
    public void updateOverlayWormKills() {
        World world = mc.theWorld;
        
        overlayRegularWormKillsText.setText(Util.numberToString(world != null ? regularWormKills : 0));
        overlayOverallWormKillsText.setText(overallRegularWormKills >= 0 ? Util.numberToString(overallRegularWormKills) : EnumChatFormatting.OBFUSCATED + "?");
    }
    
    public void updateOverlayScathaKills() {
        World world = mc.theWorld;
        
        overlayScathaKillsText.setText(Util.numberToString(world != null ? scathaKills : 0));
        overlayOverallScathaKillsText.setText(overallScathaKills >= 0 ? Util.numberToString(overallScathaKills) : EnumChatFormatting.OBFUSCATED + "?");
    }
    
    public void updateOverlayTotalKills() {
        World world = mc.theWorld;

        int totalKills = world != null ? regularWormKills + scathaKills : 0;
        int overallTotalKills = overallRegularWormKills >= 0 && overallScathaKills >= 0 ? overallRegularWormKills + overallScathaKills : -1;
        
        int percentage = totalKills > 0 ? (int) Math.round(((float) scathaKills / totalKills) * 100) : -1;
        int overallPercentage = overallTotalKills > 0 ? (int) Math.round(((float) overallScathaKills / overallTotalKills) * 100) : -1;

        overlayTotalKillsText.setText(EnumChatFormatting.RESET + Util.numberToString(totalKills) + (percentage >= 0 ? EnumChatFormatting.GRAY.toString() + EnumChatFormatting.ITALIC + " (" + percentage + "%)" : ""));
        overlayOverallTotalKillsText.setText(overallTotalKills >= 0 ? EnumChatFormatting.RESET + Util.numberToString(overallTotalKills) + (overallPercentage >= 0 ? EnumChatFormatting.GRAY.toString() + EnumChatFormatting.ITALIC + " (" + overallPercentage + "%)" : "") : EnumChatFormatting.OBFUSCATED + "?");
    }
    
    public void updateOverlayWormStreak() {
        overlayWormStreakText.setText(
                wormStreak != 0
                ? (
                        wormStreak > 0
                        ? "Scatha streak: " + Util.numberToString(wormStreak)
                        : "No scatha for " + Util.numberToString(-wormStreak) + " " + (-wormStreak == 1 ? "spawn" : "spawns")
                )
                : "No worms killed yet"
        );
    }
    
    public void updateOverlayCoords() {
        EntityPlayer player = mc.thePlayer;
        
        String facingAxis = "";
        
        int facing = player != null ? Util.getFacing(player) : 1;
        
        switch (facing) {
            case 0:
                facingAxis = "-Z";
                break;
            case 1:
                facingAxis = "+X";
                break;
            case 2:
                facingAxis = "+Z";
                break;
            case 3:
                facingAxis = "-X";
                break;
        }
        
        String coordinatesString = player != null ? (int) Math.floor(player.posX) + " "  + (int) Math.floor(player.posY) + " "  + (int) Math.floor(player.posZ) : "0 0 0";
        
        overlayCoordsText.setText(EnumChatFormatting.RESET + coordinatesString + " " + EnumChatFormatting.ITALIC + facingAxis);
    }
    
    public void updateOverlayDay() {
        World world = mc.theWorld;
        
        int worldDay = world != null ? (int) Math.floor(world.getWorldTime() / 24000f) : 0;
        long lobbyTime = world != null && Util.inCrystalHollows() ? Util.getCurrentTime() - lastWorldJoinTime : 0L;
        SimpleDateFormat timerFormat = new SimpleDateFormat("HH:mm:ss");
        timerFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        overlayDayText.setText(EnumChatFormatting.RESET + "Day " + worldDay + " " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "(" + timerFormat.format(lobbyTime) + ")");
        
        if (worldDay >= 14 && Util.inCrystalHollows()) {
            if (worldDay >= 15) {
                overlayDayWarningText.setText(EnumChatFormatting.BOLD + "Lobby about to close!");
                overlayDayWarningText.setColor(Util.Color.DARK_RED.getValue());
            }
            else {
                overlayDayWarningText.setText("Lobby closing soon");
                overlayDayWarningText.setColor(Util.Color.RED.getValue());
            }
            overlayDayWarningText.setVisible(true);
        }
        else overlayDayWarningText.setVisible(false);
    }

    
    public void openGuiNextTick(GuiScreen gui) {
        openGuiNextTick = gui;
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
    
    
    public void loadPetDrops() {
        String errorPrefix = "Couldn't load pet drops: ";
        
        JsonElement petDropsJson = PersistentData.getInstance().get(persistentDataPetDropsKey);
        if (petDropsJson != null && petDropsJson instanceof JsonObject) {
            JsonObject petDropsJsonObject = petDropsJson.getAsJsonObject();
            
            JsonElement rareDropsJson = petDropsJsonObject.get("rare");
            if (rareDropsJson instanceof JsonPrimitive) {
                JsonPrimitive rareDropsJsonPrimitive = rareDropsJson.getAsJsonPrimitive();
                if (rareDropsJsonPrimitive.isNumber())
                    rarePetDrops = rareDropsJsonPrimitive.getAsInt();
                else logger.log(Level.WARN, errorPrefix + "Rare drops JSON isn't a number");
            }
            else logger.log(Level.WARN, errorPrefix + "Rare drops JSON isn't a primitive");
            
            JsonElement epicDropsJson = petDropsJsonObject.get("epic");
            if (epicDropsJson instanceof JsonPrimitive) {
                JsonPrimitive epicDropsJsonPrimitive = epicDropsJson.getAsJsonPrimitive();
                if (epicDropsJsonPrimitive.isNumber())
                    epicPetDrops = epicDropsJsonPrimitive.getAsInt();
                else logger.log(Level.WARN, errorPrefix + "Epic drops JSON isn't a number");
            }
            else logger.log(Level.WARN, errorPrefix + "Epic drops JSON isn't a primitive");
            
            JsonElement legendaryDropsJson = petDropsJsonObject.get("legendary");
            if (legendaryDropsJson instanceof JsonPrimitive) {
                JsonPrimitive legendaryDropsJsonPrimitive = legendaryDropsJson.getAsJsonPrimitive();
                if (legendaryDropsJsonPrimitive.isNumber())
                    legendaryPetDrops = legendaryDropsJsonPrimitive.getAsInt();
                else logger.log(Level.WARN, errorPrefix + "Legendary drops JSON isn't a number");
            }
            else logger.log(Level.WARN, errorPrefix + "Legendary drops JSON isn't a primitive");
        }
        else logger.log(Level.WARN, errorPrefix + "Pet drops JSON isn't an object");
    }
    
    public void savePetDrops() {
        JsonObject petDropsJsonObject = new JsonObject();
        
        petDropsJsonObject.add("rare", new JsonPrimitive(rarePetDrops));
        petDropsJsonObject.add("epic", new JsonPrimitive(epicPetDrops));
        petDropsJsonObject.add("legendary", new JsonPrimitive(legendaryPetDrops));
        
        PersistentData.getInstance().set(persistentDataPetDropsKey, petDropsJsonObject);
        
        PersistentData.getInstance().saveData();
    }
    
    public void resetPreviousScathaPets() {
        previousScathaPets = null;
    }
    
    public boolean profilesDataRequestNeeded() {
        return Util.inCrystalHollows() && (overallRegularWormKills < 0 || overallScathaKills < 0);
    }
}
