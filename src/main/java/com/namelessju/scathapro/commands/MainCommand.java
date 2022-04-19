package com.namelessju.scathapro.commands;

import java.util.ArrayList;
import java.util.List;

import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.Util;
import com.namelessju.scathapro.gui.AchievementsGui;
import com.namelessju.scathapro.gui.SettingsGui;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class MainCommand extends CommandBase {
    
    private Config config = Config.getInstance();
    
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
        return "/scathapro - Open the Scatha-Pro settings";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 0) {
            String subCommand = args[0];

            if (subCommand.equalsIgnoreCase("reset")) {
                for (Config.Key key : Config.Key.values()) { 
                    config.reset(key);
                }
                config.save();
                
                if (config.getBoolean(Config.Key.petAlert)) ScathaPro.getInstance().resetPreviousScathaPets();

                Util.sendModChatMessage("All settings reset");
                return;
            }
            
            else if (subCommand.equalsIgnoreCase("devMode")) {
                if (args.length > 1) {
                    boolean enabled = CommandBase.parseBoolean(args[1]);
                    
                    config.set(Config.Key.devMode, enabled);
                    config.save();
                    
                    Util.sendModChatMessage("Developer mode " + (enabled ? "enabled" : "disabled"));
                    return;
                }
                else throw new CommandException("Missing values: /scathapro devMode <true/false>");
            }
            
            else if (subCommand.equalsIgnoreCase("achievements")) {
                ScathaPro.getInstance().openGuiNextTick(new AchievementsGui(null));
                return;
            }
        }

        ScathaPro.getInstance().openGuiNextTick(new SettingsGui(null));
    }
}
