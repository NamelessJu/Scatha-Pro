package com.namelessju.scathapro;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Collection;
import java.util.Iterator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Util {

	public static long getCurrentTime() {
		return System.currentTimeMillis();
	}
	
	public static boolean inCrystalHollows() {
		boolean inCrystalHollows = false;
		
		Collection<NetworkPlayerInfo> playerInfos = Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap();
		
		for (Iterator<NetworkPlayerInfo> iterator = playerInfos.iterator(); iterator.hasNext();) {
			NetworkPlayerInfo p = iterator.next();
			IChatComponent displayName = p.getDisplayName();
			
			if (displayName != null && displayName.getUnformattedText().contains("Area:") && displayName.getUnformattedText().contains("Crystal Hollows")) {
				inCrystalHollows = true;
				break;
			}
		}
		
		return inCrystalHollows;
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
	
	public static String intToAbbreviatedString(int i) {
		if (i >= 1000000000) return String.format("%.1f", i / 1000000000f) + "B";
		else if (i >= 1000000) return String.format("%.1f", i / 1000000f) + "M";
		else if (i >= 1000) return String.format("%.1f", i / 1000f) + "k";
		return Integer.toString(i);
	}
	
	public static void copyToClipboard(String str) {
		StringSelection selection = new StringSelection(str);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(selection, selection);
	}
	
	public static String getSkyblockItemID(ItemStack item) {
        if (item != null && item.hasTagCompound()) {
	        NBTTagCompound skyblockData = item.getSubCompound("ExtraAttributes", false);
	        if (skyblockData != null) {
	            String id = skyblockData.getString("id");
	            if (!id.equals("")) return id;
	        }
        }
        return null;
    }
}
