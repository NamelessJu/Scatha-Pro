package com.namelessju.scathapro.commands;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.achievements.AchievementManager;
import com.namelessju.scathapro.alerts.Alert;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.managers.UpdateChecker;
import com.namelessju.scathapro.miscellaneous.enums.Rarity;
import com.namelessju.scathapro.util.TextUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;

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
        return Lists.newArrayList("spdev");
    }
    
    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/" + COMMAND_NAME + " <sub-command> (parameters...)";
    }
    
    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }
    
    private boolean devTrigger(String trigger, String[] arguments) throws CommandException
    {
        if (trigger.equalsIgnoreCase("drop"))
        {
            Rarity rarity;
            if (arguments.length > 0)
            {
                try
                {
                    rarity = Rarity.valueOf(arguments[0].toUpperCase());
                }
                catch (Exception e)
                {
                    throw new CommandException("Invalid rarity!");
                }
            }
            else rarity = Rarity.LEGENDARY;
            
            TextUtil.sendPetDropMessage(rarity, 300);
            return true;
        }
        else if (trigger.equalsIgnoreCase("heat"))
        {
            Alert.highHeat.play();
            return true;
        }
        
        return false;
    }
    
    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length > 0)
        {
            boolean isBoolean = false;
            boolean enabled = false;
            try
            {
                enabled = CommandBase.parseBoolean(args[0]);
                isBoolean = true;
            }
            catch (CommandException ignored) {}
            
            if (isBoolean)
            {
                scathaPro.getConfig().set(Config.Key.devMode, enabled);
                scathaPro.getConfig().save();
                
                TextUtil.sendDevModeMessage("Developer mode " + (enabled ? "enabled" : "disabled"));
                
                return;
            }
        }
        else
        {
            boolean enabled = scathaPro.getConfig().getBoolean(Config.Key.devMode);
            TextUtil.sendDevModeMessage("Developer mode is currently " + (enabled ? "enabled" : "disabled"));
            return;
        }
        
        if (!scathaPro.getConfig().getBoolean(Config.Key.devMode))
        {
            ChatComponentTranslation errorMessage = new ChatComponentTranslation("commands.generic.permission");
            errorMessage.getChatStyle().setColor(EnumChatFormatting.RED);
            sender.addChatMessage(errorMessage);
            return;
        }

        if (args.length <= 0) throw new CommandException("Missing sub-command: " + getCommandUsage(null));
        
        String subCommand = args[0];
        
        if (subCommand.equalsIgnoreCase("getEntities"))
        {
            Entity senderEntity = sender.getCommandSenderEntity();
            if (!(senderEntity instanceof EntityPlayer)) throw new CommandException("Command sender is not a player");
            
            EntityPlayer player = (EntityPlayer) senderEntity;
            List<Entity> nearbyEntities = player.worldObj.getEntitiesWithinAABB(Entity.class, player.getEntityBoundingBox().expand(5f, 5f, 5f));
            
            StringBuilder entityString = new StringBuilder("[");
            for (int i = 0; i < nearbyEntities.size(); i ++)
            {
                Entity e = nearbyEntities.get(i);
                String entityName = e.getName();
                
                NBTTagCompound nbt = new NBTTagCompound();
                e.writeToNBT(nbt);
                
                if (i > 0) entityString.append(", ");
                
                JsonObject dataJson = new JsonObject();
                dataJson.addProperty("type", e.getClass().getSimpleName());
                dataJson.addProperty("name", entityName);
                dataJson.addProperty("nbt", nbt.toString());
                dataJson.addProperty("ticksExisted", e.ticksExisted);
                entityString.append(dataJson.toString());
            }
            entityString.append("]");
            
            Util.copyToClipboard(entityString.toString());
            
            TextUtil.sendDevModeMessage("Data of nearby entities copied to clipboard");
        }
        
        else if (subCommand.equalsIgnoreCase("getItem"))
        {
            Entity senderEntity = sender.getCommandSenderEntity();
            if (senderEntity instanceof EntityPlayer)
            {
                EntityPlayer player = (EntityPlayer) senderEntity;
                ItemStack heldItem = player.getCurrentEquippedItem();
                
                if (heldItem != null)
                {
                    String displayName = heldItem.getDisplayName();
                    NBTTagCompound nbt = heldItem.getTagCompound();
                    
                    String itemString = "{name:\""+displayName+"\", nbt:"+(nbt != null ? nbt.toString() : "{}")+"}";
                    
                    Util.copyToClipboard(itemString);

                    TextUtil.sendDevModeMessage("Data of held item copied to clipboard");
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
                
                TextUtil.sendDevModeMessage("All achievements unlocked");
            }
            else
            {
                Achievement achievement = Achievement.getByID(args[1]);
                if (achievement == null) throw new CommandException("No achievement with the ID \"" + args[1] + "\" exists");
                
                if (!ScathaPro.getInstance().getAchievementManager().isAchievementUnlocked(achievement))
                {
                    achievement.unlock();
                    TextUtil.sendDevModeMessage("Achievement \"" + achievement.achievementName + "\" unlocked");
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
                for (Achievement achievement : achievements)
                {
                    ScathaPro.getInstance().getAchievementManager().revokeAchievement(achievement);
                }
                
                TextUtil.sendDevModeMessage("All achievements revoked");
            }
            else
            {
                Achievement achievement = Achievement.getByID(args[1]);
                if (achievement == null) throw new CommandException("No achievement with the ID \"" + args[1] + "\" exists");
                
                if (ScathaPro.getInstance().getAchievementManager().revokeAchievement(achievement))
                {
                    TextUtil.sendDevModeMessage("Achievement \"" + achievement.achievementName + "\" revoked");
                }
                else throw new CommandException("Achievement \"" + achievement.achievementName + "\" isn't unlocked");
            }
        }
        
        else if (subCommand.equalsIgnoreCase("compVer"))
        {
            if (args.length < 3) throw new CommandException("Missing arguments: /" + COMMAND_NAME + " compVer <version 1> <version 2>");
            int comp = UpdateChecker.compareVersions(args[1], args[2]);
            if (comp > 0) TextUtil.sendDevModeMessage(args[1] + " < " + args[2]);
            else if (comp < 0) TextUtil.sendDevModeMessage(args[1] + " > " + args[2]);
            else TextUtil.sendDevModeMessage(args[1] + " = " + args[2]);
        }
        
        else if (subCommand.equalsIgnoreCase("trigger"))
        {
            if (args.length < 2) throw new CommandException("Missing argument: /" + COMMAND_NAME + " trigger <trigger name>");
            String trigger = args[1];
            String[] triggerArguments = Arrays.copyOfRange(args, 2, args.length);
            
            if (devTrigger(trigger, triggerArguments))
            {
                TextUtil.sendDevModeMessage("Triggered \"" + trigger + "\"");
            }
            else throw new CommandException("Trigger \"" + trigger + "\" doesn't exist");
        }
        
        else throw new CommandException("Invalid sub-command");
    }

    
}
