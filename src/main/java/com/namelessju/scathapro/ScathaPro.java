package com.namelessju.scathapro;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namelessju.scathapro.API.APIErrorEvent;
import com.namelessju.scathapro.API.APIResponseEvent;
import com.namelessju.scathapro.commands.ChancesCommand;
import com.namelessju.scathapro.commands.MainCommand;
import com.namelessju.scathapro.commands.DevCommand;
import com.namelessju.scathapro.gui.OverlaySettingsGui;
import com.namelessju.scathapro.gui.SettingsGui;
import com.namelessju.scathapro.overlay.OverlayContainer;
import com.namelessju.scathapro.overlay.OverlayElement;
import com.namelessju.scathapro.overlay.OverlayImage;
import com.namelessju.scathapro.overlay.OverlayText;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod(modid = ScathaPro.MODID, version = ScathaPro.VERSION, name = ScathaPro.MODNAME, clientSideOnly = true)
public class ScathaPro
{
    public static final String MODNAME = "Scatha-Pro";
    public static final String MODID = "scathapro";
    public static final String VERSION = "1.1";
    
    public static final String CHATPREFIX = EnumChatFormatting.GRAY + MODNAME + ": " + EnumChatFormatting.RESET;
    
    @Instance(value = MODID)
    private static ScathaPro instance;

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Config config = Config.getInstance();
    
    
    private final OverlayContainer uiOverlay;
    private final OverlayImage overlayScathaPetImage;
    private final OverlayContainer overlayKillsContainer;
    private final OverlayText overlayOverallWormKillsText;
    private final OverlayText overlayWormKillsText;
    private final OverlayText overlayOverallScathaKillsText;
    private final OverlayText overlayScathaKillsText;
    private final OverlayText overlayOverallTotalKillsText;
    private final OverlayText overlayTotalKillsText;
    private final OverlayText overlayCoordsText;
    private final OverlayText overlayDayText;
    private final OverlayText overlayDayWarningText;
    
    
    private boolean openSettingsGuiNextFrame = false;
    
    private long lastWorldJoinTime = -1;
    private List<Worm> registeredWorms = new ArrayList<Worm>();
    private boolean inBedrockWallRange = false;
    private int[] previousScathaPets = null;
    
    
    private int wormKills = 0;
    private int scathaKills = 0;
    
    private long lastProfilesDataRequestTime = -1;
    public boolean repeatProfilesDataRequest = true;
    
    private int overallWormKills = -1;
    private int overallScathaKills = -1;

    private long lastPreAlertTime = -1;
    
    
    public ScathaPro() {
        
        // Setup UI overlay
        
        uiOverlay = new OverlayContainer(0, 0, 1f);
        uiOverlay.padding = 5;
        uiOverlay.backgroundColor = 0x50000000;
        
        OverlayContainer titleContainer = new OverlayContainer(0, 0, 1f);
        overlayScathaPetImage = new OverlayImage(null, 256, 256, 0, 0, 0.042f);
        titleContainer.add(overlayScathaPetImage);
        
        titleContainer.add(new OverlayText("Scatha Farming:", Util.Color.GOLD.getValue(), 16, 0, 1.3f));
        
        uiOverlay.add(titleContainer);
        

        overlayKillsContainer = new OverlayContainer(0, 16, 1f);
        OverlayContainer overlayKillsSubContainer = new OverlayContainer(0, 0, 1f);
        
        OverlayText overlayWormKillsTitle = new OverlayText("Worms", Util.Color.YELLOW.getValue(), 20, 0, 1f);
        overlayWormKillsTitle.setAlignment(OverlayElement.Alignment.CENTER);
        overlayKillsSubContainer.add(overlayWormKillsTitle);
        overlayKillsSubContainer.add(new OverlayImage("worm.png", 512, 256, 0, 10, 0.08f));
        overlayWormKillsText = new OverlayText(null, Util.Color.GRAY.getValue(), 20, 22, 1f);
        overlayWormKillsText.setAlignment(OverlayElement.Alignment.CENTER);
        overlayKillsSubContainer.add(overlayWormKillsText);
        overlayOverallWormKillsText = new OverlayText(null, Util.Color.WHITE.getValue(), 20, 11, 1f);
        overlayOverallWormKillsText.setAlignment(OverlayElement.Alignment.CENTER);
        overlayKillsSubContainer.add(overlayOverallWormKillsText);

        OverlayText overlayScathaKillsTitle = new OverlayText("Scathas", Util.Color.YELLOW.getValue(), 66, 0, 1f);
        overlayScathaKillsTitle.setAlignment(OverlayElement.Alignment.CENTER);
        overlayKillsSubContainer.add(overlayScathaKillsTitle);
        overlayKillsSubContainer.add(new OverlayImage("scatha.png", 512, 256, 46, 10, 0.08f));
        overlayScathaKillsText = new OverlayText(null, Util.Color.GRAY.getValue(), 66, 22, 1f);
        overlayScathaKillsText.setAlignment(OverlayElement.Alignment.CENTER);
        overlayKillsSubContainer.add(overlayScathaKillsText);
        overlayOverallScathaKillsText = new OverlayText(null, Util.Color.WHITE.getValue(), 66, 11, 1f);
        overlayOverallScathaKillsText.setAlignment(OverlayElement.Alignment.CENTER);
        overlayKillsSubContainer.add(overlayOverallScathaKillsText);

        overlayKillsContainer.add(new OverlayText("Total", Util.Color.WHITE.getValue(), 95, 0, 1f));
        
        overlayOverallTotalKillsText = new OverlayText(null, Util.Color.WHITE.getValue(), 95, 11, 1f);
        overlayKillsContainer.add(overlayOverallTotalKillsText);
        overlayTotalKillsText = new OverlayText(null, Util.Color.GRAY.getValue(), 95, 22, 1f);
        overlayKillsContainer.add(overlayTotalKillsText);
        
        overlayKillsContainer.add(overlayKillsSubContainer);
        uiOverlay.add(overlayKillsContainer);
        
        
        overlayCoordsText = new OverlayText(null, Util.Color.GRAY.getValue(), 0, 54, 1f);
        uiOverlay.add(overlayCoordsText);
        
        overlayDayText = new OverlayText(null, Util.Color.WHITE.getValue(), 0, 64, 1f);
        uiOverlay.add(overlayDayText);
        overlayDayWarningText = new OverlayText(null, Util.Color.GRAY.getValue(), 0, 74, 1f);
        uiOverlay.add(overlayDayWarningText);
    }
    
    public static ScathaPro getInstance() {
        return instance;
    }
    
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);
        
        ClientCommandHandler.instance.registerCommand(new MainCommand());
        ClientCommandHandler.instance.registerCommand(new ChancesCommand());
        ClientCommandHandler.instance.registerCommand(new DevCommand());
    }
    
    
    @SubscribeEvent
    public void onWorldJoin(EntityJoinWorldEvent e) {
        Entity entity = e.entity;

        if (entity == mc.thePlayer) {
            
            // Reset
            
            wormKills = 0;
            scathaKills = 0;
            registeredWorms.clear();
            
            inBedrockWallRange = false;
            
            resetPreviousScathaPets();
            
            repeatProfilesDataRequest = true;
            
            lastWorldJoinTime = Util.getCurrentTime();
            
            // Update overlay
            
            updateOverlayFull();
        }
    }
    
    
    @SubscribeEvent
    public void onAPIResponse(APIResponseEvent e) {
        String endpoint = e.endpoint;
        JsonObject json = e.json;
        
        if (endpoint.equals("profiles")) {
            JsonElement profiles = json.get("profiles");
            
            if (profiles instanceof JsonArray) {
                JsonObject profilePlayerData = null;
                long latestSave = -1;
                for (JsonElement profile : profiles.getAsJsonArray()) {
                    JsonObject playerData = profile.getAsJsonObject().get("members").getAsJsonObject().get(Util.getPlayerUUIDString()).getAsJsonObject();
                    JsonElement lastSaveJson = playerData.get("last_save");
                    
                    if (lastSaveJson != null) {
                        long lastSave = lastSaveJson.getAsLong();
                        if (lastSave > latestSave) {
                            latestSave = lastSave;
                            profilePlayerData = playerData;
                        }
                    }
                }
                
                if (profilePlayerData != null) {
                    JsonObject stats = profilePlayerData.get("stats").getAsJsonObject();
                    
                    JsonElement overallWormKillsJson = stats.get("kills_worm");
                    JsonElement overallScathaKillsJson = stats.get("kills_scatha");
                    
                    overallWormKills = overallWormKillsJson != null ? overallWormKillsJson.getAsInt() : 0;
                    overallScathaKills = overallScathaKillsJson != null ? overallScathaKillsJson.getAsInt() : 0;

                    updateOverlayWormKills();
                    updateOverlayScathaKills();
                    updateOverlayTotalKills();
                    return;
                }
            }
            
            Util.sendModErrorMessage("Couldn't load worm kills from Hypixel API: No skyblock profiles found");
            repeatProfilesDataRequest = false;
        }
    }
    
    @SubscribeEvent
    public void onAPIError(APIErrorEvent e) {
        String endpoint = e.endpoint;
        APIErrorEvent.ErrorType errorType = e.errorType;
        
        if (endpoint.equals("profiles")) {
            if (errorType != APIErrorEvent.ErrorType.REQUEST_LIMIT_REACHED)
                repeatProfilesDataRequest = false;
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGuiInitPost(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.gui instanceof GuiOptions) event.buttonList.add(new GuiButton(50470400, event.gui.width / 2 - 155, event.gui.height / 6 + 24 - 6, 150, 20, "Scatha-Pro"));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGuiAction(GuiScreenEvent.ActionPerformedEvent.Post event) {
        if (event.button.id == 50470400) mc.displayGuiScreen(new SettingsGui(event.gui));
    }
    
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void renderOverlay(RenderGameOverlayEvent.Post event) // Render overlay
    {
        if (event.type == ElementType.TEXT) {
            if (Config.getInstance().getBoolean(Config.Key.overlay) && (Util.inCrystalHollows() || Config.getInstance().getBoolean(Config.Key.devMode)) && !Minecraft.getMinecraft().gameSettings.showDebugInfo && !(Minecraft.getMinecraft().currentScreen instanceof OverlaySettingsGui))
                drawOverlay();
        }
    }


    @SubscribeEvent
    public void onClientTick(ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START) {
            
            if (openSettingsGuiNextFrame) {
                mc.displayGuiScreen(new SettingsGui(null));
                openSettingsGuiNextFrame = false;
            }
            
            
            EntityPlayer player = mc.thePlayer;
            
            if (player != null) {
                
                World world = player.worldObj;
                long now = Util.getCurrentTime();
                
                
                // Worm detection
                
                List<EntityArmorStand> nearbyArmorStands = world.getEntitiesWithinAABB(EntityArmorStand.class, player.getEntityBoundingBox().expand(20f, 20f, 20f));
                
                for (int i = 0; i < nearbyArmorStands.size(); i ++) {
                    
                    EntityArmorStand e = nearbyArmorStands.get(i);
                    int entityID = e.getEntityId();
                    String entityNameUnformatted = net.minecraft.util.StringUtils.stripControlCodes(e.getName());
                    
                    if (Worm.getByID(registeredWorms, entityID) == null && entityNameUnformatted != null && (entityNameUnformatted.contains(Util.getUnicodeString("2764")) || config.getBoolean(Config.Key.devMode) && StringUtils.containsIgnoreCase(entityNameUnformatted,"dev"))) {
                        if (StringUtils.containsIgnoreCase(entityNameUnformatted, "worm") && !StringUtils.containsIgnoreCase(entityNameUnformatted, "flaming")) {
                            registeredWorms.add(new Worm(entityID, false));

                            if (config.getBoolean(Config.Key.wormAlert)) {
                                mc.ingameGUI.displayTitle(null, null, 5, 20, 5);
                                mc.ingameGUI.displayTitle(null, EnumChatFormatting.GRAY + "Just a regular worm...", 0, 0, 0);
                                mc.ingameGUI.displayTitle(EnumChatFormatting.YELLOW + "Worm", null, 0, 0, 0);
        
                                if (!Util.playModeSound("alert.worm")) Util.playSoundAtPlayer("random.orb", 1f, 0.5f);
                            }
                        }
                        else if (StringUtils.containsIgnoreCase(entityNameUnformatted, "scatha")) {
                            registeredWorms.add(new Worm(entityID, true));
                            
                            if (config.getBoolean(Config.Key.scathaAlert)) {
                                mc.ingameGUI.displayTitle(null, null, 0, 40, 10);
                                mc.ingameGUI.displayTitle(null, EnumChatFormatting.GRAY + "Pray to RNGesus!", 0, 0, 0);
                                mc.ingameGUI.displayTitle(EnumChatFormatting.RED + "Scatha", null, 0, 0, 0);
                                
                                if (!Util.playModeSound("alert.scatha")) Util.playSoundAtPlayer("random.orb", 1f, 0.8f);
                            }
                        }
                    }
                }
                
    
                // Worm kill detection
                
                for (int i = registeredWorms.size() - 1; i >= 0; i --) {
                    Worm worm = registeredWorms.get(i);
                    int entityID = worm.getEntityID();
                    
                    if (world.getEntityByID(entityID) == null) {    
                        if (now - worm.getLastAttackTime() < 1000) {
                            if (worm.isScatha()) {
                                scathaKills ++;
                                if (overallScathaKills >= 0) overallScathaKills ++;
                                
                                updateOverlayScathaKills();
                            }
                            else {
                                wormKills ++;
                                if (overallWormKills >= 0) overallWormKills ++;

                                updateOverlayWormKills();
                            }
                            
                            updateOverlayTotalKills();
                        }
                        
                        registeredWorms.remove(worm);
                    }
                }
                
                
                // Bedrock wall detection

                if (config.getBoolean(Config.Key.wallAlert)) {
                    if (Util.inCrystalHollows() || config.getBoolean(Config.Key.devMode)) {
                        int[] checkDirection = {0, 0};
                        
                        switch (Util.getFacing(player)) {
                            case 0:
                                checkDirection[1] = -1;
                                break;
                            case 1:
                                checkDirection[0] = 1;
                                break;
                            case 2:
                                checkDirection[1] = 1;
                                break;
                            case 3:
                                checkDirection[0] = -1;
                                break;
                        }
                        
                        boolean bedrockFound = false;
                        boolean viewBlocked = false;
                        BlockPos playerPos = Util.entityBlockPos(player);
                        
                        for (int i = 0; i < 10; i ++) {
                            BlockPos checkPos = playerPos.add(checkDirection[0] * i, 1, checkDirection[1] * i);
                            Block block = world.getBlockState(checkPos).getBlock();
                            if (block == Blocks.bedrock) {
                                bedrockFound = true;
                                break;
                            }
                            else if (!viewBlocked && block != Blocks.air) viewBlocked = true;
                        }
                        
                        if (!bedrockFound) inBedrockWallRange = false;
                        else if (!inBedrockWallRange) {
                            inBedrockWallRange = true;
                            
                            if (viewBlocked) {
                                mc.ingameGUI.displayTitle(null, null, 3, 20, 5);
                                mc.ingameGUI.displayTitle(null, EnumChatFormatting.GRAY + "Close to bedrock wall", 0, 0, 0);
                                mc.ingameGUI.displayTitle("", null, 0, 0, 0);
                                
                                if (!Util.playModeSound("alert.bedrock_wall")) Util.playSoundAtPlayer("note.pling", 1f, 0.5f);
                            }
                        }
                    }
                }
                
                
                // Scatha pet drop detection

                if (config.getBoolean(Config.Key.petAlert)) {
                    if (lastWorldJoinTime >= 0 && now - lastWorldJoinTime > 3000) {
                        
                        ItemStack[] inventory = player.inventory.mainInventory;
                        
                        int[] currentScathaPets = {0, 0, 0};
        
                        for (int i = 0; i < inventory.length; i++) {
                            if (i == 8) continue;
    
                            ItemStack item = inventory[i];
                            
                            if (item != null) {
                                NBTTagCompound nbt = item.getTagCompound();
                                
                                if (nbt != null) {
                                    NBTTagCompound displayNbt = nbt.getCompoundTag("display");
                                    if (displayNbt != null) {
                                        String displayName = displayNbt.getString("Name");
                                        NBTTagList displayLoreList = displayNbt.getTagList("Lore", 8);
                                        
                                        StringBuilder displayLoreBuilder = new StringBuilder();
                                        for (int j = 0; j < displayLoreList.tagCount(); j ++) {
                                            String loreLine = displayLoreList.getStringTagAt(j);
                                            if (j > 0) displayLoreBuilder.append("\n");
                                            displayLoreBuilder.append(loreLine);
                                        }
                                        String displayLore = displayLoreBuilder.toString();
                                        
                                        String skyblockItemID = Util.getSkyblockItemID(item);
                                        
                                        if (skyblockItemID != null && skyblockItemID.equals("PET") && StringUtils.containsIgnoreCase(net.minecraft.util.StringUtils.stripControlCodes(displayName), "scatha") || config.getBoolean(Config.Key.devMode) && net.minecraft.util.StringUtils.stripControlCodes(displayName).equalsIgnoreCase("scatha pet")) {
                                            int rarity = -1;
                                            if (net.minecraft.util.StringUtils.stripControlCodes(displayLore).contains("RARE")) rarity = 0;
                                            else if (net.minecraft.util.StringUtils.stripControlCodes(displayLore).contains("EPIC")) rarity = 1;
                                            else if (net.minecraft.util.StringUtils.stripControlCodes(displayLore).contains("LEGENDARY")) rarity = 2;
                                            
                                            if (rarity >= 0 && rarity < currentScathaPets.length) currentScathaPets[rarity] += item.stackSize;
                                        }
                                    }
                                }
                            }
                        }
    
                        if (mc.currentScreen == null && previousScathaPets != null) {
                            int newScathaPet = -1;
                            
                            for (int i = 0; i < currentScathaPets.length; i ++) {
                                int currentRarityCount = currentScathaPets[i];
                                int previousRarityCount = previousScathaPets[i];
                                int difference = currentRarityCount - previousRarityCount;
                                if (difference > 0 && i > newScathaPet) newScathaPet = i;
                            }
                            
                            if (newScathaPet >= 0) {
                                mc.ingameGUI.displayTitle(null, null, 0, 60, 20);
                                
                                switch (newScathaPet) {
                                    case 0:
                                        mc.ingameGUI.displayTitle(null, EnumChatFormatting.BLUE + "RARE", 0, 0, 0);
                                        break;
                                    case 1:
                                        mc.ingameGUI.displayTitle(null, EnumChatFormatting.DARK_PURPLE + "EPIC", 0, 0, 0);
                                        break;
                                    case 2:
                                        mc.ingameGUI.displayTitle(null, EnumChatFormatting.GOLD + "LEGENDARY", 0, 0, 0);
                                        break;
                                    default:
                                        mc.ingameGUI.displayTitle(null, EnumChatFormatting.GRAY + "unknown rarity", 0, 0, 0);
                                }
                                
                                mc.ingameGUI.displayTitle(EnumChatFormatting.YELLOW + "Scatha Pet!", null, 0, 0, 0);

                                Util.playSoundAtPlayer("random.chestopen", 1.5f, 0.95f);
                                
                                if (!Util.playModeSound("alert.pet_drop")) Util.playSoundAtPlayer("mob.wither.death", 0.75f, 0.8f);
                            }
                        }
        
                        previousScathaPets = currentScathaPets;
                    }
                }
                
                
                // API request
                
                if (repeatProfilesDataRequest && now - lastWorldJoinTime > 3000 && (Util.inCrystalHollows() || config.getBoolean(Config.Key.devMode)) && now - lastProfilesDataRequestTime > 1000 * 60 * 5) {
                    lastProfilesDataRequestTime = now;
                    API.requestProfilesData();
                }
                
                
                // Update UI overlay
                
                if (Config.getInstance().getBoolean(Config.Key.overlay) && (Util.inCrystalHollows() || Config.getInstance().getBoolean(Config.Key.devMode)) && !Minecraft.getMinecraft().gameSettings.showDebugInfo) {
                    updateOverlayCoords();
                    updateOverlayDay();
                    
                    updateOverlayPosition();
                }
            }
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

                    if (armorStand == entity) continue;
                    
                    int entityID = armorStand.getEntityId();
                    Worm worm = Worm.getByID(registeredWorms, entityID);
                    
                    if (worm != null) {
                        worm.attack();
                        break;
                    }
                }
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChatReceived(ClientChatReceivedEvent e) {
        if (e.type == 2) return;
        
        String unformattedText = net.minecraft.util.StringUtils.stripControlCodes(e.message.getUnformattedText());
        
        // Automatically update API key when generating new one
        
        if (unformattedText.startsWith("Your new API key is ") && e.message.getSiblings().size() >= 1) {
            String apiKey = e.message.getSiblings().get(0).getChatStyle().getChatClickEvent().getValue();
            config.set(Config.Key.apiKey, apiKey);
            config.save();
            
            Util.sendModChatMessage("Automatically updated API key to " + apiKey);
            
            if (profilesDataRequestNeeded()) API.requestProfilesData();
        }
        
        // Add copy button
        
        if (config.getBoolean(Config.Key.chatCopy) && !unformattedText.replace(" ", "").isEmpty()) {
            ChatComponentText copyText = new ChatComponentText(EnumChatFormatting.DARK_GRAY + Util.getUnicodeString("270D"));
            ChatStyle style = new ChatStyle()
                    .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GRAY + "Copy message")))
                    .setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, unformattedText));
            copyText.setChatStyle(style);
            
            e.message.appendSibling(new ChatComponentText(EnumChatFormatting.RESET + " "));
            e.message.appendSibling(copyText);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSound(PlaySoundEvent e) {
        long now = Util.getCurrentTime();
        if (config.getBoolean(Config.Key.wormPreAlert) && (Util.inCrystalHollows() && e.sound.getPitch() == 2.0952382f || config.getBoolean(Config.Key.devMode) && e.sound.getPitch() >= 2f) && e.name.equals("mob.spider.step") && now - lastPreAlertTime > 2000) {
            mc.ingameGUI.displayTitle(null, null, 0, 20, 5);
            mc.ingameGUI.displayTitle(null, EnumChatFormatting.YELLOW + "Worm about to spawn...", 0, 0, 0);
            mc.ingameGUI.displayTitle("", null, 0, 0, 0);
            
            if (!Util.playModeSound("alert.prespawn")) Util.playSoundAtPlayer("note.bass", 1f, 2f);
            
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

        updateOverlayCoords();
        updateOverlayDay();
    }

    public void updateOverlayPosition() {
        ScaledResolution scaledresolution = new ScaledResolution(mc);
    
        final double[] overlayPositionPercentage = {config.getDouble(Config.Key.overlayX), config.getDouble(Config.Key.overlayY)};
        final int[] overlayPosition = {
                overlayPositionPercentage[0] >= 0 ? (int) Math.round(scaledresolution.getScaledWidth() * overlayPositionPercentage[0]) : 10,
                overlayPositionPercentage[1] >= 0 ? (int) Math.round(scaledresolution.getScaledHeight() * overlayPositionPercentage[1]) : 10,
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
        String petImage;
        
        switch (config.getInt(Config.Key.mode)) {
            case 1:
                petImage = "scathapet_meme.png";
                break;
            case 2:
                petImage = "scathapet_anime.png";
                break;
            default:
                petImage = "scathapet.png";
                break;
        }

        overlayScathaPetImage.setImage(petImage);
    }
    
    public void updateOverlayWormKills() {
        World world = mc.theWorld;
        
        overlayWormKillsText.setText(Util.numberToString(world != null ? wormKills : 0));
        overlayOverallWormKillsText.setText(overallWormKills >= 0 ? Util.numberToString(overallWormKills) : EnumChatFormatting.OBFUSCATED + "?");
    }
    
    public void updateOverlayScathaKills() {
        World world = mc.theWorld;
        
        overlayScathaKillsText.setText(Util.numberToString(world != null ? scathaKills : 0));
        overlayOverallScathaKillsText.setText(overallScathaKills >= 0 ? Util.numberToString(overallScathaKills) : EnumChatFormatting.OBFUSCATED + "?");
    }
    
    public void updateOverlayTotalKills() {
        World world = mc.theWorld;

        int totalKills = world != null ? wormKills + scathaKills : 0;
        int overallTotalKills = overallWormKills >= 0 && overallScathaKills >= 0 ? overallWormKills + overallScathaKills : -1;
        
        int percentage = totalKills > 0 ? (int) Math.round(((float) scathaKills / totalKills) * 100) : -1;
        int overallPercentage = overallTotalKills > 0 ? (int) Math.round(((float) overallScathaKills / overallTotalKills) * 100) : -1;

        overlayTotalKillsText.setText(EnumChatFormatting.RESET + Util.numberToString(totalKills) + (percentage >= 0 ? EnumChatFormatting.GRAY.toString() + EnumChatFormatting.ITALIC + " (" + percentage + "%)" : ""));
        overlayOverallTotalKillsText.setText(overallTotalKills >= 0 ? EnumChatFormatting.RESET + Util.numberToString(overallTotalKills) + (overallPercentage >= 0 ? EnumChatFormatting.GRAY.toString() + EnumChatFormatting.ITALIC + " (" + overallPercentage + "%)" : "") : EnumChatFormatting.OBFUSCATED + "?");
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
        long lobbyTime = world != null ? Util.getCurrentTime() - lastWorldJoinTime : 0L;
        SimpleDateFormat timerFormat = new SimpleDateFormat("HH:mm:ss");
        timerFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        overlayDayText.setText(EnumChatFormatting.RESET + "Day " + worldDay + " " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "(" + timerFormat.format(lobbyTime) + ")");
        
        if (worldDay >= 14) {
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

    
    public void openSettingsGui() {
        openSettingsGuiNextFrame = true;
    }
    
    
    public void resetPreviousScathaPets() {
        previousScathaPets = null;
    }
    
    public boolean profilesDataRequestNeeded() {
        return (Util.inCrystalHollows() || Config.getInstance().getBoolean(Config.Key.devMode)) && (overallWormKills < 0 || overallScathaKills < 0);
    }
}
