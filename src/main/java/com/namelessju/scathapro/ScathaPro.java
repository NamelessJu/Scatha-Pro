package com.namelessju.scathapro;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.namelessju.scathapro.commands.ConfigCommand;
import com.namelessju.scathapro.commands.DevCommand;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@SideOnly(Side.CLIENT)
@Mod(modid = ScathaPro.MODID, version = ScathaPro.VERSION, name = ScathaPro.MODNAME)
public class ScathaPro
{
    public static final String MODNAME = "Scatha-Pro";
    public static final String MODID = "scathapro";
    public static final String VERSION = "1.0";
	
	public static final String CHATPREFIX = EnumChatFormatting.GRAY + "[" + MODNAME + "] ";

	public final Minecraft mc = Minecraft.getMinecraft();
	private final Config config = Config.getInstance();
	
	
	private long lastWorldJoinTime = -1;
	private List<Worm> registeredWorms = new ArrayList<Worm>();
	private boolean inBedrockWallRange = false;
    private int[] previousScathaPets = null;
    
    
	private int wormKills = 0;
	private int scathaKills = 0;

	
	@EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);
        
        ClientCommandHandler.instance.registerCommand(new ConfigCommand());
        ClientCommandHandler.instance.registerCommand(new DevCommand());
    }
    
	
    @SubscribeEvent()
    public void onWorldJoin(EntityJoinWorldEvent e) { // Reset stuff on entering new world 
        Entity entity = e.entity;

        if (entity == mc.thePlayer) {
            wormKills = 0;
            scathaKills = 0;
            registeredWorms.clear();
            
            inBedrockWallRange = false;
            
            previousScathaPets = null;
            
            lastWorldJoinTime = Util.getCurrentTime();
        }
    }
	
    
    @SubscribeEvent
    public void renderString(RenderGameOverlayEvent event) // Render overlay
    {
    	if (event.type == ElementType.TEXT && (Util.inCrystalHollows() || Config.getInstance().getBoolean(Config.Key.devMode))) {
	    	EntityPlayer player = mc.thePlayer;
	    	if (player != null) {
	    		
	            ScaledResolution scaledresolution = new ScaledResolution(mc);
	    		
	    		final double[] overlayPositionPercentage = {config.getDouble(Config.Key.overlayX), config.getDouble(Config.Key.overlayY)};
	    		final double[] overlayPosition = {
	    				overlayPositionPercentage[0] >= 0 ? scaledresolution.getScaledWidth() * overlayPositionPercentage[0] : 10,
						overlayPositionPercentage[1] >= 0 ? scaledresolution.getScaledHeight() * overlayPositionPercentage[1] : 30,
				};
	    		
	    		int worldDay = (int) Math.floor(player.worldObj.getWorldTime() / 24000f);
	    		
		    	String coordsString = "Coords: " + (int) Math.floor(player.posX) + " "  + (int) Math.floor(player.posY) + " "  + (int) Math.floor(player.posZ) + "";
		    	String worldTimeString = "Day: " + worldDay;
		    	String totalKillsString = Util.intToAbbreviatedString(wormKills + scathaKills);
		    	String individualKillsString = "Worms: " + Util.intToAbbreviatedString(wormKills) + ", Scathas: " + Util.intToAbbreviatedString(scathaKills);
		        
		        String scathaTexture = config.getBoolean(Config.Key.memeMode) ? "scathagasm" : "scatha";
		
		    	
		    	FontRenderer fontRenderer = mc.fontRendererObj;
		        
		        GlStateManager.pushMatrix();
		        GlStateManager.translate(overlayPosition[0], overlayPosition[1], 0);
		        GlStateManager.scale(1.5f, 1.5f, 1f);
		        fontRenderer.drawString("Scatha Farming:", 0, 0, 16755200, true);
		        GlStateManager.popMatrix();

		        GlStateManager.pushMatrix();
		        GlStateManager.translate(overlayPosition[0], overlayPosition[1] + 20, 0);
		        GlStateManager.scale(1f, 1f, 1f);
		        fontRenderer.drawString(coordsString, 0, 0, 16777215, true);
		        GlStateManager.popMatrix();

		        GlStateManager.pushMatrix();
		        GlStateManager.translate(overlayPosition[0], overlayPosition[1] + 30, 0);
		        GlStateManager.scale(1f, 1f, 1f);
		        fontRenderer.drawString(worldTimeString, 0, 0, 16777215, true);
		        GlStateManager.popMatrix();

		        if (worldDay == 14) {
			        GlStateManager.pushMatrix();
			        GlStateManager.translate(overlayPosition[0] + fontRenderer.getStringWidth(worldTimeString) + 5, overlayPosition[1] + 30, 0);
			        GlStateManager.scale(1f, 1f, 1f);
		        	fontRenderer.drawString("World will close soon!", 0, 0, 16733525, true);
			        GlStateManager.popMatrix();
		        }
		        else if (worldDay >= 15) {
			        GlStateManager.pushMatrix();
			        GlStateManager.translate(overlayPosition[0] + fontRenderer.getStringWidth(worldTimeString) + 5, overlayPosition[1] + 30, 0);
			        GlStateManager.scale(1f, 1f, 1f);
		        	fontRenderer.drawString("World about to close!", 0, 0, 11141120, true);
			        GlStateManager.popMatrix();
		        }

		        GlStateManager.pushMatrix();
		        GlStateManager.translate(overlayPosition[0], overlayPosition[1] + 45, 0);
		        mc.renderEngine.bindTexture(new ResourceLocation(MODID, "textures/" + scathaTexture + ".png"));
		        GlStateManager.scale(0.12f, 0.12f, 0.12f); 
		        GuiIngame.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 128, 128, 128, 128);
		        GlStateManager.popMatrix();
		        
		        GlStateManager.pushMatrix();
		        GlStateManager.translate(overlayPosition[0] + 8 - fontRenderer.getStringWidth(totalKillsString) / 2, overlayPosition[1] + 49, 0);
		        GlStateManager.scale(1f, 1f, 1f);
		        fontRenderer.drawString(totalKillsString, 0, 0, 16777045, true);
		        GlStateManager.popMatrix();
		        
		        GlStateManager.pushMatrix();
		        GlStateManager.translate(overlayPosition[0] + 21, overlayPosition[1] + 49, 0);
		        GlStateManager.scale(1f, 1f, 1f);
		        fontRenderer.drawString(individualKillsString, 0, 0, 16777215, true);
		        GlStateManager.popMatrix();
	    	}
    	}
    }


	@SubscribeEvent
	public void onClientTick(ClientTickEvent event)
	{
		if (event.phase == TickEvent.Phase.START) {
			
			EntityPlayer player = mc.thePlayer;
			
	    	if (player != null) {
    			
				World world = player.worldObj;
				long now = Util.getCurrentTime();
				
				
				// Worm spawn detection
				
				List<EntityArmorStand> nearbyArmorStands = world.getEntitiesWithinAABB(EntityArmorStand.class, player.getEntityBoundingBox().expand(20f, 20f, 20f));
				
				for (int i = 0; i < nearbyArmorStands.size(); i ++) {
					
					EntityArmorStand e = nearbyArmorStands.get(i);
					int entityID = e.getEntityId();
					String entityNameUnformatted = net.minecraft.util.StringUtils.stripControlCodes(e.getName());
					
					if (Worm.getByID(registeredWorms, entityID) == null && entityNameUnformatted != null && (entityNameUnformatted.contains(Util.getUnicodeString("2764")) || config.getBoolean(Config.Key.devMode) && StringUtils.containsIgnoreCase(entityNameUnformatted,"dev"))) {
						if (StringUtils.containsIgnoreCase(entityNameUnformatted, "worm") && !StringUtils.containsIgnoreCase(entityNameUnformatted, "flaming")) {
							registeredWorms.add(new Worm(entityID, false));
							
							mc.ingameGUI.displayTitle(null, null, 5, 20, 5);
							mc.ingameGUI.displayTitle(null, EnumChatFormatting.GRAY + "Just a regular worm...", 0, 0, 0);
							mc.ingameGUI.displayTitle(EnumChatFormatting.YELLOW + "Worm", null, 0, 0, 0);
	
							if (config.getBoolean(Config.Key.memeMode)) player.playSound(MODID + ":meme.alert.worm", 1f, 1f);
							else player.playSound("random.orb", 1f, 0.5f);
						}
						else if (StringUtils.containsIgnoreCase(entityNameUnformatted, "scatha")) {
							registeredWorms.add(new Worm(entityID, true));
							
							mc.ingameGUI.displayTitle(null, null, 0, 40, 10);
							mc.ingameGUI.displayTitle(null, EnumChatFormatting.GRAY + "Pray to RNGesus!", 0, 0, 0);
							mc.ingameGUI.displayTitle(EnumChatFormatting.RED + "Scatha", null, 0, 0, 0);
							
							if (config.getBoolean(Config.Key.memeMode)) player.playSound(MODID + ":meme.alert.scatha", 1f, 1f);
							else player.playSound("random.orb", 1f, 0.8f);
						}
					}
				}
				
	
				// Worm kill detection
				
				for (int i = registeredWorms.size() - 1; i >= 0; i --) {
					Worm worm = registeredWorms.get(i);
					int entityID = worm.getEntityID();
					
					if (world.getEntityByID(entityID) == null) {	
						if (now - worm.getLastAttackTime() < 500) {
							if (worm.isScatha()) scathaKills ++;
							else wormKills ++;
						}
						
						registeredWorms.remove(worm);
					}
				}
				
				
				// Bedrock wall detection
				
				if (Util.inCrystalHollows() || config.getBoolean(Config.Key.devMode)) {
					int facing = (int) Math.floor(player.rotationYaw / 90 - 1.5f) % 4;
					if (facing < 0) facing += 4;
					
					int[] checkDirection = {0, 0};
					switch (facing) {
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
							mc.ingameGUI.displayTitle(null, null, 5, 20, 5);
							mc.ingameGUI.displayTitle(null, EnumChatFormatting.GRAY + "Close to bedrock wall", 0, 0, 0);
							mc.ingameGUI.displayTitle("", null, 0, 0, 0);
							
							if (config.getBoolean(Config.Key.memeMode)) player.playSound(MODID + ":meme.alert.bedrock_wall", 1f, 1f);
							else player.playSound("note.pling", 1f, 0.5f);
						}
					}
				}
				
				
				// Scatha pet drop detection
				
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
		    				
		    				player.playSound("mob.wither.death", 0.75f, 0.8f);
		    				player.playSound("random.chestopen", 1.5f, 0.95f);
			            }
					}
	
		            previousScathaPets = currentScathaPets;
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
    
    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent e) {
    	if (config.getBoolean(Config.Key.chatCopy)) { // Add copy button to all text messages
	    	if (e.type == 0 || e.type == 1) {
	    		ChatComponentText copyText = new ChatComponentText(EnumChatFormatting.DARK_GRAY + Util.getUnicodeString("270D"));
	    		ChatStyle style = new ChatStyle()
	    				.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GRAY + "Copy message")))
	    				.setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, net.minecraft.util.StringUtils.stripControlCodes(e.message.getUnformattedText())));
	    		copyText.setChatStyle(style);
	    		
	    		e.message.appendSibling(new ChatComponentText(" "));
	    		e.message.appendSibling(copyText);
	    	}
    	}
    }
    
    
    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent e) {
    	if (config.getBoolean(Config.Key.devMode)) {
    		
	    	ItemStack item = e.itemStack;
	
	        if (item != null) {
				String skyblockItemID = Util.getSkyblockItemID(item);
	        	if (skyblockItemID != null) e.toolTip.add(EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY + skyblockItemID + EnumChatFormatting.RESET);
	        }
    	}
    }
}
