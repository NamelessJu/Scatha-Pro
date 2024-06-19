package com.namelessju.scathapro.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.namelessju.scathapro.Constants;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.achievements.AchievementManager;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.managers.UpdateChecker;
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
    public List<String> getCommandAliases()
    {
        List<String> aliases = new ArrayList<String>();
        aliases.add("spdev");
        return aliases;
    }
    
    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/" + COMMAND_NAME + " <sub-command> (parameters...)";
    }
    
    @Override
    public int getRequiredPermissionLevel()
    {
        return scathaPro.getConfig().getBoolean(Config.Key.devMode) ? 0 : 9;
    }
    
    private boolean devTrigger(String trigger, String[] arguments) throws CommandException
    {
        return false;
    }
    
    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length <= 0) throw new CommandException("Missing sub-command: " + getCommandUsage(null));
        
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
    
                    sender.addChatMessage(new ChatComponentText(Constants.chatPrefix + "Held item copied to clipboard"));
                }
                else throw new CommandException("You are not holding an item");
            }
            else throw new CommandException("Command sender is not a player");
        }
        
        else if (subCommand.equalsIgnoreCase("unlockAchievement") || subCommand.equalsIgnoreCase("unlockAch"))
        {
            if (args.length < 2) throw new CommandException("Missing argument: /" + COMMAND_NAME + " unlockAchievement <achievement ID>");
            
            if (args[1].equals("*"))
            {
                Achievement[] achievements = AchievementManager.getAllAchievements();
                for (int i = 0; i < achievements.length; i ++)
                {
                    achievements[i].unlock();
                }
                
                MessageUtil.sendModChatMessage("All achievements unlocked");
            }
            else
            {
                Achievement achievement = Achievement.getByID(args[1]);
                if (achievement == null) throw new CommandException("No achievement with the ID \"" + args[1] + "\" exists");
                
                if (!ScathaPro.getInstance().getAchievementManager().isAchievementUnlocked(achievement))
                {
                    achievement.unlock();
                    MessageUtil.sendModChatMessage("Achievement \"" + achievement.achievementName + "\" unlocked");
                }
                else throw new CommandException("Achievement \"" + achievement.achievementName + "\" is already unlocked");
            }
        }
        
        else if (subCommand.equalsIgnoreCase("revokeAchievement") || subCommand.equalsIgnoreCase("revokeAch"))
        {
            if (args.length < 2) throw new CommandException("Missing argument: /" + COMMAND_NAME + " revokeAchievement <achievement ID>");
            
            if (args[1].equals("*"))
            {
                Achievement[] achievements = AchievementManager.getAllAchievements();
                for (int i = 0; i < achievements.length; i ++)
                {
                    ScathaPro.getInstance().getAchievementManager().revokeAchievement(achievements[i]);
                }
                
                MessageUtil.sendModChatMessage("All achievements revoked");
            }
            else
            {
                Achievement achievement = Achievement.getByID(args[1]);
                if (achievement == null) throw new CommandException("No achievement with the ID \"" + args[1] + "\" exists");
                
                if (ScathaPro.getInstance().getAchievementManager().revokeAchievement(achievement))
                {
                    MessageUtil.sendModChatMessage("Achievement \"" + achievement.achievementName + "\" revoked");
                }
                else throw new CommandException("Achievement \"" + achievement.achievementName + "\" isn't unlocked");
            }
        }
        
        else if (subCommand.equalsIgnoreCase("compVer"))
        {
            if (args.length < 3) throw new CommandException("Missing arguments: /" + COMMAND_NAME + " compVer <version 1> <version 2>");
            int comp = UpdateChecker.compareVersions(args[1], args[2]);
            if (comp > 0) MessageUtil.sendModChatMessage(args[1] + " < " + args[2]);
            else if (comp < 0) MessageUtil.sendModChatMessage(args[1] + " > " + args[2]);
            else MessageUtil.sendModChatMessage(args[1] + " = " + args[2]);
        }
        
        else if (subCommand.equalsIgnoreCase("trigger"))
        {
            if (args.length < 2) throw new CommandException("Missing argument: /" + COMMAND_NAME + " trigger <trigger name>");
            String trigger = args[1];
            String[] triggerArguments = Arrays.copyOfRange(args, 2, args.length);
            
            if (devTrigger(trigger, triggerArguments))
            {
                sender.addChatMessage(new ChatComponentText("Triggered \"" + trigger + "\""));
            }
            else
            {
                MessageUtil.sendModErrorMessage("Trigger \"" + trigger + "\" doesn't exist");
            }
        }
        
        else throw new CommandException("Invalid sub-command");
    }

    
}
