package com.namelessju.scathapro.commands;

import java.util.ArrayList;
import java.util.List;

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
        return "/" + COMMAND_NAME + " [\"help\"]";
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
            MessageUtil.sendModChatMessage(
                    EnumChatFormatting.GOLD + "All commands:\n"
                    + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.WHITE + "/" + COMMAND_NAME + " " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "(alias /sp)" + EnumChatFormatting.WHITE + " [\"help\"]:" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + " Shows this help message\n"
                    + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.WHITE + "/" + ChancesCommand.COMMAND_NAME + " " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "(alias /scacha)" + EnumChatFormatting.WHITE + " [magic find] [pet luck] [Scatha kills]:" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + " Check/calculate Scatha pet drop chances\n"
                    + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.WHITE + "/" + COMMAND_NAME + " settings:" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + " Opens the settings menu\n"
                    + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.WHITE + "/" + COMMAND_NAME + " achievements:" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + " Opens the achievements menu\n"
                    + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.WHITE + "/" + COMMAND_NAME + " setPetDrops <rare amount> <epic amount> <legendary amount>:" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + " Add pets you dropped previously to your counter\n"
                    + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.WHITE + "/" + COMMAND_NAME + " backup:" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + " Creates a backup of your persistent data\n"
                    + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.WHITE + "/" + COMMAND_NAME + " checkUpdate:" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + " Check for an update\n"
                    + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.WHITE + "/" + COMMAND_NAME + " resetSettings:" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + " Reset all settings"
            );
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
            if (args.length > 3)
            {
                int rare = CommandBase.parseInt(args[1]);
                int epic = CommandBase.parseInt(args[2]);
                int legendary = CommandBase.parseInt(args[3]);
                
                if (rare > 9999 || epic > 9999 || legendary > 9999) throw new CommandException("Pet drop amount too large! Maximum allowed amount is 9999.");
                if (rare < 0 || epic < 0 || legendary < 0) throw new CommandException("Pet drop amount cannot be negative!");
                
                scathaPro.variables.rarePetDrops = rare;
                scathaPro.variables.epicPetDrops = epic;
                scathaPro.variables.legendaryPetDrops = legendary;
                
                scathaPro.variables.scathaKillsAtLastDrop = -1;
                
                MessageUtil.sendModChatMessage("Pet drops changed");
                
                scathaPro.persistentData.savePetDrops();
                scathaPro.overlayManager.updatePetDrops();
                scathaPro.overlayManager.updateScathaKillsSinceLastDrop();
                scathaPro.updatePetDropAchievements();
            }
            else throw new CommandException("Missing values: /" + COMMAND_NAME + " setPetDrops <rare> <epic> <legendary>");
        }
        
        else if (subCommand.equalsIgnoreCase("backup"))
        {
            PersistentData persistentData = scathaPro.persistentData;
            persistentData.saveData();
            persistentData.backup();
        }
        
        else if (subCommand.equalsIgnoreCase("checkUpdate"))
        {
            MessageUtil.sendModChatMessage(EnumChatFormatting.GRAY + "Checking for update...");
            UpdateChecker.checkForUpdate(true);
        }
        
        else if (subCommand.equalsIgnoreCase("resetSettings"))
        {
            Config config = scathaPro.config;
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
            else
            {
                MessageUtil.sendModErrorMessage("Failed to open persistent data file in file explorer");
            }
        }
        
        else if (subCommand.equalsIgnoreCase("devMode"))
        {
            if (args.length > 1)
            {
                boolean enabled = CommandBase.parseBoolean(args[1]);

                Config config = scathaPro.config;
                config.set(Config.Key.devMode, enabled);
                config.save();
                
                MessageUtil.sendModChatMessage("Developer mode " + (enabled ? "enabled" : "disabled"));
            }
            else throw new CommandException("Missing values: /" + COMMAND_NAME + " devMode <true/false>");
        }
        
        else throw new CommandException("Invalid subcommand - Get a list of all subcommands using " + getCommandUsage(null));
    }
}
