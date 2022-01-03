package com.namelessju.scathapro.commands;

import java.util.List;

import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.Util;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DevCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "scathadev";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "scathadev help";
	}

    @Override
    public int getRequiredPermissionLevel() {
        return Config.getInstance().getBoolean(Config.Key.devMode) ? 0 : 9;
    }

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if (args.length > 0) {
			String subCommand = args[0];
			
			if (subCommand.equals("help")) {
				sender.addChatMessage(new ChatComponentText(
						ScathaPro.CHATPREFIX + EnumChatFormatting.RESET + "/scathadev <subcommand> [values...]\n" +
						"All subcommands:\n" +
						"- getEntities\n" +
						"- getItem"
				));
			}
			
			else if (subCommand.equalsIgnoreCase("getEntities")) {
				Entity senderEntity = sender.getCommandSenderEntity();
				if (senderEntity != null && senderEntity instanceof EntityPlayer) {
					
					EntityPlayer player = (EntityPlayer) senderEntity;
					List<Entity> nearbyEntities = player.worldObj.getEntitiesWithinAABB(Entity.class, player.getEntityBoundingBox().expand(5f, 5f, 5f));

					StringBuilder entityString = new StringBuilder();
					entityString.append("[");
					for (int i = 0; i < nearbyEntities.size(); i ++) {
						Entity e = nearbyEntities.get(i);
						String entityName = e.getName();
						NBTTagCompound nbt = e.getEntityData();
						
						if (i > 0) entityString.append(", ");
						entityString.append("{name:\""+entityName+"\", nbt:"+(nbt != null ? nbt.toString() : "{}")+"}");
					}
					entityString.append("]");
					
					Util.copyToClipboard(entityString.toString());

					sender.addChatMessage(new ChatComponentText(ScathaPro.CHATPREFIX + "Nearby entities copied to clipboard"));
				}
				else throw new CommandException("You are not a player");
			}
			
			else if (subCommand.equalsIgnoreCase("getItem")) {
				Entity senderEntity = sender.getCommandSenderEntity();
				if (senderEntity != null && senderEntity instanceof EntityPlayer) {
					
					EntityPlayer player = (EntityPlayer) senderEntity;
					ItemStack heldItem = player.getCurrentEquippedItem();
					
					if (heldItem != null) {
						String displayName = heldItem.getDisplayName();
						NBTTagCompound nbt = heldItem.getTagCompound();
						
						String itemString = "{name:\""+displayName+"\", nbt:"+(nbt != null ? nbt.toString() : "{}")+"}";
						
						Util.copyToClipboard(itemString);
		
						sender.addChatMessage(new ChatComponentText(ScathaPro.CHATPREFIX + "Held item copied to clipboard"));
					}
					else throw new CommandException("You are not holding an item");
				}
				else throw new CommandException("You are not a player");
			}
			
			else throw new CommandException("Invalid subcommand");
		}
		else throw new CommandException("Missing subcommand: /scathadev <subcommand> [values...]");
	}

    
}
