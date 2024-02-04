package com.namelessju.scathapro.commands;

import java.util.Arrays;
import java.util.List;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.achievements.AchievementManager;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.util.MessageUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;

public class DevCommand extends CommandBase
{
    public static final String COMMAND_NAME = "scathadev";
    
    private final ScathaPro scathaPro;
    
    public DevCommand(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
    }

    @Override
    public String getCommandName()
    {
        return COMMAND_NAME;
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/" + COMMAND_NAME + " <subcommand> [parameters...]";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return scathaPro.config.getBoolean(Config.Key.devMode) ? 0 : 9;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length <= 0) throw new CommandException("Missing subcommand: " + getCommandUsage(null));
        
        String subCommand = args[0];
        
        if (subCommand.equalsIgnoreCase("getEntities"))
        {
            Entity senderEntity = sender.getCommandSenderEntity();
            if (senderEntity != null && senderEntity instanceof EntityPlayer)
            {
                EntityPlayer player = (EntityPlayer) senderEntity;
                List<Entity> nearbyEntities = player.worldObj.getEntitiesWithinAABB(Entity.class, player.getEntityBoundingBox().expand(5f, 5f, 5f));
                
                StringBuilder entityString = new StringBuilder();
                entityString.append("[");
                for (int i = 0; i < nearbyEntities.size(); i ++)
                {
                    Entity e = nearbyEntities.get(i);
                    String entityName = e.getName();
                    
                    NBTTagCompound nbt = new NBTTagCompound();
                    e.writeToNBT(nbt);
                    
                    if (i > 0) entityString.append(", ");
                    entityString.append("{type: \""+e.getClass().getSimpleName()+"\", name: \""+entityName+"\", nbt: \""+(nbt != null ? nbt.toString() : "")+"\"}");
                }
                entityString.append("]");
                
                Util.copyToClipboard(entityString.toString());
                
                MessageUtil.sendModChatMessage("Nearby entities copied to clipboard");
            }
            else throw new CommandException("Command sender is not a player");
        }
        
        else if (subCommand.equalsIgnoreCase("getItem"))
        {
            Entity senderEntity = sender.getCommandSenderEntity();
            if (senderEntity != null && senderEntity instanceof EntityPlayer)
            {
                EntityPlayer player = (EntityPlayer) senderEntity;
                ItemStack heldItem = player.getCurrentEquippedItem();
                
                if (heldItem != null)
                {
                    String displayName = heldItem.getDisplayName();
                    NBTTagCompound nbt = heldItem.getTagCompound();
                    
                    String itemString = "{name:\""+displayName+"\", nbt:"+(nbt != null ? nbt.toString() : "{}")+"}";
                    
                    Util.copyToClipboard(itemString);
    
                    sender.addChatMessage(new ChatComponentText(ScathaPro.CHATPREFIX + "Held item copied to clipboard"));
                }
                else throw new CommandException("You are not holding an item");
            }
            else throw new CommandException("Command sender is not a player");
        }
        
        else if (subCommand.equalsIgnoreCase("achievementsUnlockAll"))
        {
            Achievement[] achievements = AchievementManager.getAllAchievements();
            for (int i = 0; i < achievements.length; i ++)
            {
                achievements[i].unlock();
            }
        }
        
        else if (subCommand.equalsIgnoreCase("trigger"))
        {
            if (args.length < 2) throw new CommandException("Missing trigger argument: /" + COMMAND_NAME + " trigger <trigger name>");
            String trigger = args[1];
            String[] triggerArguments = Arrays.copyOfRange(args, 2, args.length);
            
            if (scathaPro.devTrigger(trigger, triggerArguments))
            {
                sender.addChatMessage(new ChatComponentText("Triggered \"" + trigger + "\""));
            }
            else
            {
                MessageUtil.sendModErrorMessage("Trigger \"" + trigger + "\" doesn't exist");
            }
        }
        
        else throw new CommandException("Invalid subcommand");
    }

    
}
