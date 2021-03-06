package com.namelessju.scathapro;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StringUtils;

public abstract class Util {
    
    public enum Color {
        DARK_RED(11141120), RED(16733525), GOLD(16755200), YELLOW(16777045), DARK_GREEN(43520), GREEN(5635925), AQUA(5636095), DARK_AQUA(43690), DARK_BLUE(170), BLUE(5592575), LIGHT_PURPLE(16733695), DARK_PURPLE(11141290), WHITE(16777215), GRAY(11184810), DARK_GRAY(5592405), BLACK(0);
        
        private final int value;
        
        Color(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }

    public static void sendModChatMessage(String message) {
        sendModChatMessage(new ChatComponentText(message));
    }
    public static void sendModChatMessage(IChatComponent chatComponent) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player != null) {
            ChatComponentText chatComponentText = new ChatComponentText(ScathaPro.CHATPREFIX);
            chatComponentText.appendSibling(chatComponent);
            Util.addChatCopyButton(chatComponentText);
            player.addChatMessage(chatComponentText);
        }
    }
    
    public static void sendModErrorMessage(String errorMessage) {
        sendModChatMessage(EnumChatFormatting.RED + errorMessage);
    }
    
    public static void addChatCopyButton(IChatComponent message) {
        String unformattedText = StringUtils.stripControlCodes(message.getUnformattedText());
        
        if (Config.instance.getBoolean(Config.Key.chatCopy) && !unformattedText.replace(" ", "").isEmpty()) {
            ChatComponentText copyText = new ChatComponentText(EnumChatFormatting.DARK_GRAY + Util.getUnicodeString("270D"));
            ChatStyle style = new ChatStyle()
                    .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GRAY + "Copy message")))
                    .setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, unformattedText.replace("\n", " ")));
            copyText.setChatStyle(style);
            
            message.appendText(EnumChatFormatting.RESET + " ");
            message.appendSibling(copyText);
        }
    }

    public static void playSoundAtPlayer(String sound) {
        playSoundAtPlayer(sound, 1f, 1f);
    }
    public static void playSoundAtPlayer(String sound, float volume, float pitch) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player != null) player.playSound(sound, (float) (Config.instance.getDouble(Config.Key.volume) * volume), pitch);
    }

    public static void playModSoundAtPlayer(String sound) {
        playModSoundAtPlayer(sound, 1f, 1f);
    }
    public static void playModSoundAtPlayer(String sound, float volume, float pitch) {
        playSoundAtPlayer(ScathaPro.MODID + ":" + sound, volume, pitch);
    }

    public static boolean playModeSound(String path) {
        
        String modeFolder;
        
        switch (Config.instance.getInt(Config.Key.mode)) {
            case 1:
                modeFolder = "meme";
                break;
            case 2:
                modeFolder = "anime";
                break;
            default:
                return false;
        }

        playModSoundAtPlayer(modeFolder + "." + path);
        return true;
    }

	public static long getCurrentTime() {
		return System.currentTimeMillis();
	}
	
	public static String getUUIDString(UUID uuid) {
        if (uuid != null) return uuid.toString().replace("-", "").toLowerCase();
        return null;
	}

    public static String getPlayerUUIDString() {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player != null) {
            GameProfile gameProfile = player.getGameProfile();
            if (gameProfile != null)
                return getUUIDString(gameProfile.getId());
        }
        return null;
    }
	
	public static boolean inCrystalHollows() {
	    if (Config.instance.getBoolean(Config.Key.devMode)) return true;
	    
		boolean inCrystalHollows = false;
		
		NetHandlerPlayClient netHandler = Minecraft.getMinecraft().getNetHandler();
		if (netHandler != null) {
    		Collection<NetworkPlayerInfo> playerInfos = netHandler.getPlayerInfoMap();
    		
    		for (Iterator<NetworkPlayerInfo> iterator = playerInfos.iterator(); iterator.hasNext();) {
    			NetworkPlayerInfo p = iterator.next();
    			IChatComponent displayName = p.getDisplayName();
    			
    			if (displayName != null && displayName.getUnformattedText().contains("Area:") && displayName.getUnformattedText().contains("Crystal Hollows")) {
    				inCrystalHollows = true;
    				break;
    			}
    		}
		}
		
		return inCrystalHollows;
	}
	
	public static boolean isDeveloper(NetworkPlayerInfo playerInfo) {
	    if (playerInfo != null) {
	        UUID uuid = playerInfo.getGameProfile().getId();
	        if (uuid != null)
	            return getUUIDString(uuid).equals("e9be3984b09740c98fb4d8aaeb2b4838");
	    }
	    
	    return false;
	}
	
	public static boolean isWormSkull(ItemStack stack) {
		if (stack.getItem() == Items.skull && stack.hasTagCompound()) {
			NBTTagCompound skullOwner = stack.getTagCompound().getCompoundTag("SkullOwner");
			if (skullOwner != null && skullOwner.getBoolean("hypixelPopulated")) {
				NBTTagList textureList = skullOwner.getCompoundTag("Properties").getTagList("textures", 10);
				if (textureList.tagCount() > 0) {
					String textureBase64 = textureList.getCompoundTagAt(0).getString("Value");
					
					if (
							textureBase64.equals("ewogICJ0aW1lc3RhbXAiIDogMTYyNTA3MjMxNDE2OCwKICAicHJvZmlsZUlkIiA6ICIwNWQ0NTNiZWE0N2Y0MThiOWI2ZDUzODg0MWQxMDY2MCIsCiAgInByb2ZpbGVOYW1lIiA6ICJFY2hvcnJhIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzk2MjQxNjBlYjk5YmRjNjUxZGEzOGRiOTljZDdjMDlmMWRhNjY5ZWQ4MmI5Y2JjMjgyODc0NmU2NTBjNzY1ZGEiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==")
							||
							textureBase64.equals("ewogICJ0aW1lc3RhbXAiIDogMTYyMDQ0NTc2NDQ1MSwKICAicHJvZmlsZUlkIiA6ICJmNDY0NTcxNDNkMTU0ZmEwOTkxNjBlNGJmNzI3ZGNiOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWxhcGFnbzA1IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RmMDNhZDk2MDkyZjNmNzg5OTAyNDM2NzA5Y2RmNjlkZTZiNzI3YzEyMWIzYzJkYWVmOWZmYTFjY2FlZDE4NmMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==")
						)
						return true;
				}
			}
		}
		
		return false;
	}

    public static String numberToString(int number) {
        return numberToString(number, 0);
    }
	public static String numberToString(double number, int maxDecimalPlaces) {
	    DecimalFormatSymbols decimalSymbols = new DecimalFormatSymbols();
	    decimalSymbols.setDecimalSeparator('.');
        decimalSymbols.setGroupingSeparator(',');
	    DecimalFormat decimalFormat = new DecimalFormat("#,###.#", decimalSymbols);
	    decimalFormat.setMaximumFractionDigits(maxDecimalPlaces);
	    return decimalFormat.format(number);
	}
	
	public static String getUnicodeString(String hexValue) {
		return Character.toString((char) Integer.parseInt(hexValue, 16));
	}
	
	public static BlockPos entityBlockPos(Entity entity) {
		return new BlockPos(
			(int) Math.floor(entity.posX),
			(int) Math.floor(entity.posY),
			(int) Math.floor(entity.posZ)
		);
	}
	
	public static int getFacing(EntityPlayer player) {
        int facing = (int) Math.floor(player.rotationYaw / 90 - 1.5f) % 4;
        if (facing < 0) facing += 4;
        return facing;
	}
	
	public static void copyToClipboard(String str) {
		StringSelection selection = new StringSelection(str);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(selection, selection);
	}
    
    public static File getModFile(String relativePath) {
        File modFolder = new File(Minecraft.getMinecraft().mcDataDir, "mods/" + ScathaPro.MODID + "/");
        if (!modFolder.exists()) modFolder.mkdirs();
        
        return new File(modFolder, relativePath);
    }

    public static NBTTagCompound getSkyblockTagCompound(ItemStack item) {
        return getSkyblockTagCompound(item, null);
    }
    public static NBTTagCompound getSkyblockTagCompound(ItemStack item, String path) {
        if (item != null && item.hasTagCompound()) {
            NBTTagCompound currentCompound = item.getSubCompound("ExtraAttributes", false);
            if (currentCompound == null) return null;
            
            if (path != null) {
                String[] pathSegments = path.split("/");
                for (int i = 0; i < pathSegments.length; i ++) {
                    currentCompound = currentCompound.hasKey(pathSegments[i]) ? currentCompound.getCompoundTag(pathSegments[i]) : null;
                    if (i == pathSegments.length - 1) break;
                    else if (currentCompound == null) return null;
                }
            }
            
            return currentCompound;
        }
        
        return null;
    }
	
	public static String getSkyblockItemID(ItemStack item) {
        NBTTagCompound skyblockData = getSkyblockTagCompound(item);
        if (skyblockData != null) {
            String id = skyblockData.getString("id");
            if (!id.equals("")) return id;
        }
        return null;
    }
	
	public static float calculatePetChance(float initialChance, int magicFind, int petLuck, int looting) {
        
        float lootingMultiplier = 1f;
        switch (looting) {
            case 1:
                lootingMultiplier = 1.15f;
                break;
            case 2:
                lootingMultiplier = 1.3f;
                break;
            case 3:
                lootingMultiplier = 1.45f;
                break;
            case 4:
                lootingMultiplier = 1.6f;
                break;
            case 5:
                lootingMultiplier = 1.75f;
                break;
        }
        
	    return initialChance * (1 + (magicFind + petLuck)/100f) * lootingMultiplier;
	}
}
