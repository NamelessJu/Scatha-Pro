package com.namelessju.scathapro.commands;

import java.util.ArrayList;
import java.util.List;

import com.namelessju.scathapro.Constants;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.menus.AchievementsGui;
import com.namelessju.scathapro.gui.menus.SettingsGui;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.managers.PersistentData;
import com.namelessju.scathapro.managers.UpdateChecker;
import com.namelessju.scathapro.util.MessageUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

public class MainCommand extends CommandBase
{
    public static final String COMMAND_NAME = "scathapro";
    
    private final ScathaPro scathaPro;
    
    public MainCommand(ScathaPro scathaPro)
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
        aliases.add("sp");
        return aliases;
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/" + COMMAND_NAME + " (\"help\")";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length <= 0 || args[0].equalsIgnoreCase("help"))
        {
            MessageUtil.sendChatDivider();
            MessageUtil.sendModChatMessage(
                Constants.msgHighlightingStyle + "All commands:\n"
                + EnumChatFormatting.WHITE + "/" + COMMAND_NAME + " " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "(alias: /sp)" + EnumChatFormatting.WHITE + " (\"help\"):" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + " Shows this help message\n"
                + EnumChatFormatting.WHITE + "/" + ChancesCommand.COMMAND_NAME + " " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "(alias: /scacha)" + EnumChatFormatting.WHITE + " (\"help\"):" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + " Check/calculate Scatha pet drop chances\n"
                + EnumChatFormatting.WHITE + "/" + COMMAND_NAME + " settings:" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + " Opens the settings menu\n"
                + EnumChatFormatting.WHITE + "/" + COMMAND_NAME + " achievements:" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + " Opens the achievements menu\n"
                + EnumChatFormatting.WHITE + "/" + COMMAND_NAME + " setPetDrops <rare> <epic> <legendary>:" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + " Add pets you dropped previously to your counter\n"
                + EnumChatFormatting.WHITE + "/" + COMMAND_NAME + " toggleOverlay " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "(alias: /sp to)" + EnumChatFormatting.WHITE + ":" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + " Toggles the overlay visibility\n"
                + EnumChatFormatting.WHITE + "/" + COMMAND_NAME + " backup:" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + " Creates a backup of your persistent data\n"
                + EnumChatFormatting.WHITE + "/" + COMMAND_NAME + " checkUpdate:" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + " Check for an update\n"
                + EnumChatFormatting.WHITE + "/" + COMMAND_NAME + " resetSettings:" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + " Reset all settings\n"
                + EnumChatFormatting.WHITE + "/" + COMMAND_NAME + " persistentDataFile:" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + " Open the persistent data file in the file explorer"
            );
            MessageUtil.sendChatDivider();
            return;
        }
        
        String subCommand = args[0];
        
        if (subCommand.equalsIgnoreCase("settings") || subCommand.equalsIgnoreCase("config"))
        {
            scathaPro.variables.openGuiNextTick = new SettingsGui(scathaPro, null);
        }
        
        else if (subCommand.equalsIgnoreCase("achievements"))
        {
            scathaPro.variables.openGuiNextTick = new AchievementsGui(scathaPro, null);
        }
        
        else if (subCommand.equalsIgnoreCase("setPetDrops"))
        {
            if (args.length <= 3) throw new CommandException("Missing arguments: /" + COMMAND_NAME + " setPetDrops <rare> <epic> <legendary>");
            
            int rare = CommandBase.parseInt(args[1]);
            int epic = CommandBase.parseInt(args[2]);
            int legendary = CommandBase.parseInt(args[3]);

            if (rare < 0 || epic < 0 || legendary < 0) throw new CommandException("Scatha pet drop amount cannot be negative!");
            
            if (rare > Constants.maxLegitPetDropsAmount || epic > Constants.maxLegitPetDropsAmount || legendary > Constants.maxLegitPetDropsAmount)
            {
                throw new CommandException("Scatha pet drop amount too large! Maximum allowed amount is \"" + Constants.maxLegitPetDropsAmount + "\".");
            }
            
            scathaPro.variables.rarePetDrops = rare;
            scathaPro.variables.epicPetDrops = epic;
            scathaPro.variables.legendaryPetDrops = legendary;
            
            scathaPro.getPersistentData().savePetDrops();
            scathaPro.getOverlay().updatePetDrops();
            
            MessageUtil.sendModChatMessage(EnumChatFormatting.RESET + "Scatha pet drop amounts changed\n" + EnumChatFormatting.GRAY + "(Achievements will update on next game start or Scatha pet drop)");
        }
        
        else if (subCommand.equalsIgnoreCase("toggleOverlay") || subCommand.equalsIgnoreCase("to"))
        {
            boolean overlayVisible = scathaPro.getOverlay().toggleVisibility();
            MessageUtil.sendModChatMessage(EnumChatFormatting.RESET + "Overlay " + (overlayVisible ? "enabled" : "disabled"));
        }
        
        else if (subCommand.equalsIgnoreCase("backup"))
        {
            scathaPro.getPersistentData().saveData();
            scathaPro.getPersistentData().backup();
        }
        
        else if (subCommand.equalsIgnoreCase("checkUpdate"))
        {
            MessageUtil.sendModChatMessage(EnumChatFormatting.GRAY + "Checking for update...");
            UpdateChecker.checkForUpdate(true);
        }
        
        else if (subCommand.equalsIgnoreCase("resetSettings"))
        {
            Config config = scathaPro.getConfig();
            for (Config.Key key : Config.Key.values())
            {
                config.reset(key);
            }
            config.save();
            
            if (config.getBoolean(Config.Key.scathaPetDropAlert)) scathaPro.variables.previousScathaPets = null;
            
            MessageUtil.sendModChatMessage("All settings reset");
        }
        
        else if (subCommand.equalsIgnoreCase("persistentDataFile"))
        {
            if (Util.openFileInExplorer(PersistentData.saveFile))
            {
                MessageUtil.sendModChatMessage("Persistent data file opened in file explorer");
            }
            else MessageUtil.sendModErrorMessage("Failed to open persistent data file in file explorer");
        }
        
        else if (subCommand.equalsIgnoreCase("debugLogs"))
        {
            boolean enabled = !scathaPro.getConfig().getBoolean(Config.Key.debugLogs);
            scathaPro.getConfig().set(Config.Key.debugLogs, enabled);
            scathaPro.getConfig().save();
            
            MessageUtil.sendModChatMessage("Debug logs " + (enabled ? "enabled" : "disabled"));
        }
        
        else if (subCommand.equalsIgnoreCase("devMode"))
        {
            boolean enabled = !scathaPro.getConfig().getBoolean(Config.Key.devMode);
            scathaPro.getConfig().set(Config.Key.devMode, enabled);
            scathaPro.getConfig().save();
            
            MessageUtil.sendModChatMessage("Developer mode " + (enabled ? "enabled" : "disabled"));
        }
        
        else throw new CommandException("Invalid sub-command - Get a list of all sub-commands using " + getCommandUsage(null));
    }
}
