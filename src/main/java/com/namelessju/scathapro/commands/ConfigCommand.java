package com.namelessju.scathapro.commands;

import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.ScathaPro;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ConfigCommand extends CommandBase {
	
	Config config = Config.getInstance();

	@Override
	public String getCommandName() {
		return "scathapro";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "scathapro help";
	}

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if (args.length > 0) {
			String cfg = args[0];
			
			if (cfg.equalsIgnoreCase("help")) {
				sender.addChatMessage(new ChatComponentText(
						ScathaPro.CHATPREFIX + EnumChatFormatting.RESET + "/scathapro <configkey> <values...>\n" +
						"All configurations:\n" +
						"- overlay <x> <y>\n" +
						"- chatCopy <true/false>\n" +
						"- memeMode <true/false>"
				));
			}
			
			else if (cfg.equalsIgnoreCase("overlay")) {
				if (args.length > 1 && args[1].equalsIgnoreCase("default")) {
					config.reset(Config.Key.overlayX);
					config.reset(Config.Key.overlayY);
					config.save();
					
					sender.addChatMessage(new ChatComponentText(ScathaPro.CHATPREFIX + "Overlay reset"));
				}
				else if (args.length > 2) {
					double x = CommandBase.parseDouble(args[1]);
					double y = CommandBase.parseDouble(args[2]);
					
					config.set(Config.Key.overlayX, x);
					config.set(Config.Key.overlayY, y);
					config.save();
					
					sender.addChatMessage(new ChatComponentText(ScathaPro.CHATPREFIX + "Overlay set to " + (x * 100) + "% " + (y * 100) + "%"));
				}
				else throw new CommandException("Missing arguments: /scathapro overlay <x> <y>");
			}
			
			else if (cfg.equalsIgnoreCase("memeMode")) {
				if (args.length > 1) {
					if (args[1].equalsIgnoreCase("default")) {
						config.reset(Config.Key.memeMode);
						config.save();
						
						sender.addChatMessage(new ChatComponentText(ScathaPro.CHATPREFIX + "Meme mode reset to default (" + Config.Key.memeMode.getDefaultValue() + ")"));
					}
					else {
						boolean enabled = CommandBase.parseBoolean(args[1]);
						
						config.set(Config.Key.memeMode, enabled);
						config.save();
						
						sender.addChatMessage(new ChatComponentText(ScathaPro.CHATPREFIX + "Meme mode " + (enabled ? "enabled" : "disabled")));
					}
				}
				else throw new CommandException("Missing arguments: /scathapro memeMode <true/false>");
			}
			
			else if (cfg.equalsIgnoreCase("chatCopy")) {
				if (args.length > 1) {
					if (args[1].equalsIgnoreCase("default")) {
						config.reset(Config.Key.chatCopy);
						config.save();
						
						sender.addChatMessage(new ChatComponentText(ScathaPro.CHATPREFIX + "Chat copy button reset to default (" + Config.Key.chatCopy.getDefaultValue() + ")"));
					}
					else {
						boolean enabled = CommandBase.parseBoolean(args[1]);
						
						config.set(Config.Key.chatCopy, enabled);
						config.save();
						
						sender.addChatMessage(new ChatComponentText(ScathaPro.CHATPREFIX + "Chat copy button " + (enabled ? "enabled" : "disabled")));
					}
				}
				else throw new CommandException("Missing arguments: /scathapro chatCopy <true/false>");
			}
			
			else if (cfg.equalsIgnoreCase("devMode")) {
				if (args.length > 1) {
					if (args[1].equalsIgnoreCase("default")) {
						config.reset(Config.Key.devMode);
						config.save();
						
						sender.addChatMessage(new ChatComponentText(ScathaPro.CHATPREFIX + "Developer mode reset to default (" + Config.Key.devMode.getDefaultValue() + ")"));
					}
					else {
						boolean enabled = CommandBase.parseBoolean(args[1]);
						
						config.set(Config.Key.devMode, enabled);
						config.save();
						
						sender.addChatMessage(new ChatComponentText(ScathaPro.CHATPREFIX + "Developer mode " + (enabled ? "enabled" : "disabled")));
					}
				}
				else throw new CommandException("Missing arguments: /scathapro devMode <true/false>");
			}
			
			else  throw new CommandException("Invalid config key");
		}
		else throw new CommandException("Missing config key: /scathapro <configkey> <values...>");
	}

    
}
