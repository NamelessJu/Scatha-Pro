package com.namelessju.scathapro.commands;

import java.util.ArrayList;
import java.util.List;

import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.OverlayManager;
import com.namelessju.scathapro.PersistentData;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.menus.AchievementsGui;
import com.namelessju.scathapro.gui.menus.SettingsGui;
import com.namelessju.scathapro.util.ChatUtil;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

public class MainCommand extends CommandBase {
    
    @Override
    public String getCommandName() {
        return "scathapro";
    }
    
    @Override
    public List<String> getCommandAliases() {
        List<String> aliases = new ArrayList<String>();
        aliases.add("sp");
        return aliases;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/scathapro help";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            ChatUtil.sendModChatMessage(
                    "All commands:\n"
                    + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.WHITE + "/scathapro " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "(alias /sp)" + EnumChatFormatting.WHITE + " (help):" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + " Shows this help message\n"
                    + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.WHITE + "/scathachances " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "(alias /scacha)" + EnumChatFormatting.WHITE + ":" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + " Check/calculate Scatha pet drop chances\n"
                    + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.WHITE + "/scathapro settings:" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + " Opens the settings menu\n"
                    + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.WHITE + "/scathapro achievements:" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + " Opens the achievements menu\n"
                    + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.WHITE + "/scathapro setPetDrops <rare> <epic> <legendary>:" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + " Add pets you dropped previously to your counter\n"
                    + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.WHITE + "/scathapro resetConfig:" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + " Reset all settings"
            );
        }
        
        else {
            String subCommand = args[0];
            
            if (subCommand.equalsIgnoreCase("settings") || subCommand.equalsIgnoreCase("config")) {
                ScathaPro.getInstance().openGuiNextTick = new SettingsGui(null);
            }
            
            else if (subCommand.equalsIgnoreCase("achievements")) {
                ScathaPro.getInstance().openGuiNextTick = new AchievementsGui(null);
            }
            
            else if (subCommand.equalsIgnoreCase("setPetDrops")) {
                if (args.length > 3) {
                    int rare = CommandBase.parseInt(args[1]);
                    int epic = CommandBase.parseInt(args[2]);
                    int legendary = CommandBase.parseInt(args[3]);
                    
                    if (rare > 9999 || epic > 9999 || legendary > 9999) throw new CommandException("Pet drop amount too large! Maximum allowed amount is 9999.");
                    if (rare < 0 || epic < 0 || legendary < 0) throw new CommandException("Pet drop amount cannot be negative!");
                    
                    ScathaPro scathaPro = ScathaPro.getInstance();
                    scathaPro.rarePetDrops = rare;
                    scathaPro.epicPetDrops = epic;
                    scathaPro.legendaryPetDrops = legendary;
                    
                    ChatUtil.sendModChatMessage("Pet drops changed");
                    
                    PersistentData.instance.savePetDrops();
                    OverlayManager.instance.updatePetDrops();
                    scathaPro.updatePetDropAchievements();
                }
                else throw new CommandException("Missing values: /scathapro setPetDrops <rare> <epic> <legendary>");
            }
            
            else if (subCommand.equalsIgnoreCase("resetConfig")) {
                for (Config.Key key : Config.Key.values())
                    Config.instance.reset(key);
                Config.instance.save();
                
                if (Config.instance.getBoolean(Config.Key.petAlert)) ScathaPro.getInstance().resetPreviousScathaPets();
    
                ChatUtil.sendModChatMessage("All settings reset");
            }
            
            else if (subCommand.equalsIgnoreCase("devMode")) {
                if (args.length > 1) {
                    boolean enabled = CommandBase.parseBoolean(args[1]);
                    
                    Config.instance.set(Config.Key.devMode, enabled);
                    Config.instance.save();
                    
                    ChatUtil.sendModChatMessage("Developer mode " + (enabled ? "enabled" : "disabled"));
                }
                else throw new CommandException("Missing values: /scathapro devMode <true/false>");
            }
            
            else throw new CommandException("Invalid subcommand - Get a list of all subcommands using /scathapro help");
        }
    }
}
