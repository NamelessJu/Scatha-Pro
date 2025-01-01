package com.namelessju.scathapro.commands;

import java.util.List;

import com.google.common.collect.Lists;
import com.namelessju.scathapro.Constants;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.menus.AchievementsGui;
import com.namelessju.scathapro.gui.menus.SettingsGui;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.managers.PersistentData;
import com.namelessju.scathapro.managers.SaveManager;
import com.namelessju.scathapro.managers.UpdateChecker;
import com.namelessju.scathapro.util.TextUtil;
import com.namelessju.scathapro.util.TimeUtil;
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
        return Lists.newArrayList("sp");
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
            int page = args.length > 1 ? CommandBase.parseInt(args[1]) : 1;
            
            if (page < 1 || page > 2) throw new CommandException("Invalid page number (only 1 and 2 allowed)");
            
            TextUtil.sendChatDivider();
            TextUtil.sendModChatMessage(Constants.msgHighlightingColor + ScathaPro.MODNAME + " commands:");
            
            switch (page)
            {
                case 1:
                    sendHelpMessageEntry(COMMAND_NAME, "(\"help\")", "/sp", "Shows this help message");
                    sendHelpMessageEntry(null, "settings", null, "Opens the mod's settings menu");
                    sendHelpMessageEntry(null, "achievements", null, "Opens the achievements menu");
                    sendHelpMessageEntry(ChancesCommand.COMMAND_NAME, "(\"help\")", "/scacha", "Check/calculate Scatha pet drop chances");
                    sendHelpMessageEntry(AverageMoneyCommand.COMMAND_NAME, null, "/sp averageMoney/avgMoney", "Calculate average Scatha farming profits");
                    sendHelpMessageEntry(null, "dailyStreak", "daily, streak", "Shows information about your daily Scatha farming streak");
                    break;
                
                case 2:
                    sendHelpMessageEntry(null, "setPetDrops <rare> <epic> <legendary>", null, "Set your pet drop counter to the specified numbers");
                    sendHelpMessageEntry(null, "toggleOverlay", "to", "Toggles the overlay visibility");
                    sendHelpMessageEntry(null, "checkUpdate", null, "Check for mod updates");
                    sendHelpMessageEntry(null, "resetSettings", null, "Reset all settings");
                    sendHelpMessageEntry(null, "backup/backupPersistentData", null, "Creates a backup of this mod's save folder/persistent data file");
                    sendHelpMessageEntry(null, "persistentDataFile", null, "Open the mod's persistent data file path in the file explorer");
                    break;
                
                default:
                    TextUtil.sendModChatMessage(EnumChatFormatting.RED + "Something went wrong! (invalid page number)", false);
            }
            
            TextUtil.sendModChatMessage(Constants.msgHighlightingColor + "Help page " + page + "/2 - /sp help <page>", false);
            
            TextUtil.sendChatDivider();
            return;
        }
        
        String subCommand = args[0];
        
        if (subCommand.equalsIgnoreCase("averagemoney") || subCommand.equalsIgnoreCase("avgmoney"))
        {
            scathaPro.commandRegistry.averageMoneyCommand.processCommand(sender, new String[0]);
        }
        
        else if (subCommand.equalsIgnoreCase("settings") || subCommand.equalsIgnoreCase("config"))
        {
            scathaPro.variables.openGuiNextTick = new SettingsGui(scathaPro, null);
        }
        
        else if (subCommand.equalsIgnoreCase("achievements"))
        {
            scathaPro.variables.openGuiNextTick = new AchievementsGui(scathaPro, null);
        }
        
        else if (subCommand.equalsIgnoreCase("dailyStreak") || subCommand.equalsIgnoreCase("daily") || subCommand.equalsIgnoreCase("streak"))
        {
            boolean farmedToday = scathaPro.variables.lastScathaFarmedDate != null && scathaPro.variables.lastScathaFarmedDate.equals(TimeUtil.today());
            
            TextUtil.sendChatDivider();
            TextUtil.sendModChatMessage(
                Constants.msgHighlightingColor + "Daily Scatha farming streak:\n"
                + EnumChatFormatting.RESET + EnumChatFormatting.WHITE + "Current streak: " + EnumChatFormatting.GREEN + scathaPro.variables.scathaFarmingStreak + " day" + (scathaPro.variables.scathaFarmingStreak != 1 ? "s" : "") + "\n"
                + EnumChatFormatting.RESET + EnumChatFormatting.WHITE + "Highest streak: " + EnumChatFormatting.GOLD + scathaPro.variables.scathaFarmingStreakHighscore + " day" + (scathaPro.variables.scathaFarmingStreakHighscore != 1 ? "s" : "") + "\n"
                + EnumChatFormatting.RESET + (farmedToday ? EnumChatFormatting.GREEN + "\u2714" + " You have farmed Scathas today!" : EnumChatFormatting.RED + "\u2716" + " You haven't farmed Scathas today yet...")
            );
            TextUtil.sendChatDivider();
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
            
            boolean anyRarityDecreased = false;
            if (rare < scathaPro.variables.rarePetDrops) anyRarityDecreased = true;
            else if (epic < scathaPro.variables.epicPetDrops) anyRarityDecreased = true;
            else if (legendary < scathaPro.variables.legendaryPetDrops) anyRarityDecreased = true;
            
            scathaPro.variables.rarePetDrops = rare;
            scathaPro.variables.epicPetDrops = epic;
            scathaPro.variables.legendaryPetDrops = legendary;
            
            scathaPro.getPersistentData().savePetDrops();
            scathaPro.getOverlay().updatePetDrops();
            
            TextUtil.sendModChatMessage(EnumChatFormatting.RESET + "Scatha pet drop amounts changed\n" + EnumChatFormatting.GRAY + "(Achievements will update on next game start or Scatha pet drop)");
            
            if (anyRarityDecreased)
            {
                scathaPro.variables.scathaKillsAtLastDrop = -1;
                scathaPro.getOverlay().updateScathaKillsSinceLastDrop();
                TextUtil.sendModChatMessage(EnumChatFormatting.YELLOW + "\"Scathas since last pet drop\" counter was cleared as a rarity's pet drop amount was decreased");
            }
        }
        
        else if (subCommand.equalsIgnoreCase("toggleOverlay") || subCommand.equalsIgnoreCase("to"))
        {
            boolean overlayVisible = scathaPro.getOverlay().toggleVisibility();
            TextUtil.sendModChatMessage(EnumChatFormatting.RESET + "Overlay " + (overlayVisible ? "enabled" : "disabled"));
        }
        
        else if (subCommand.equalsIgnoreCase("backup"))
        {
            scathaPro.getPersistentData().saveData();
            SaveManager.backup();
        }
        
        else if (subCommand.equalsIgnoreCase("backupPersistentData"))
        {
            scathaPro.getPersistentData().saveData();
            SaveManager.backupPersistentData();
        }
        
        else if (subCommand.equalsIgnoreCase("checkUpdate"))
        {
            TextUtil.sendModChatMessage(EnumChatFormatting.GRAY + "Checking for update...");
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
            
            TextUtil.sendModChatMessage("All settings reset");
        }
        
        else if (subCommand.equalsIgnoreCase("persistentDataFile"))
        {
            if (Util.openFileInExplorer(PersistentData.file))
            {
                TextUtil.sendModChatMessage("Persistent data file opened in file explorer");
            }
            else TextUtil.sendModErrorMessage("Failed to open persistent data file in file explorer");
        }
        
        else if (subCommand.equalsIgnoreCase("debugLogs"))
        {
            if (args.length > 1)
            {
                boolean enabled = CommandBase.parseBoolean(args[1]);
                
                scathaPro.getConfig().set(Config.Key.debugLogs, enabled);
                scathaPro.getConfig().save();
                
                TextUtil.sendModChatMessage("Debug logs " + (enabled ? "enabled" : "disabled"));
            }
            else
            {
                boolean enabled = scathaPro.getConfig().getBoolean(Config.Key.debugLogs);
                TextUtil.sendModChatMessage("Debug logs are currently " + (enabled ? "enabled" : "disabled"));
            }
        }
        
        else throw new CommandException("Invalid sub-command - Get a list of all sub-commands using " + getCommandUsage(null));
    }
    
    private void sendHelpMessageEntry(String commandName, String parameters, String alias, String description)
    {
        TextUtil.sendModChatMessage(
            EnumChatFormatting.WHITE + "/" + (commandName != null ? commandName : "sp")
            + (parameters != null ? " " + parameters : "")
            + (alias != null ? " " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "(alias: " + alias + ")" : "")
            + EnumChatFormatting.WHITE + ":"
            + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + " " + description
            ,
            false
        );
    }
}
