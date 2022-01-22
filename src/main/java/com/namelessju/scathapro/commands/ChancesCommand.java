package com.namelessju.scathapro.commands;

import java.util.ArrayList;
import java.util.List;

import com.namelessju.scathapro.Util;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

public class ChancesCommand extends CommandBase {
    
    @Override
    public String getCommandName() {
        return "scathachances";
    }
    
    @Override
    public List<String> getCommandAliases() {
        List<String> aliases = new ArrayList<String>();
        aliases.add("scacha");
        return aliases;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/scathachances <magicfind>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {

        int magicFind = args.length > 0 ? Math.max(CommandBase.parseInt(args[0]), 0) : 0;
        
        float rareChance = Math.min(Util.getChanceWithMF(0.0006f, magicFind), 1f);
        float epicChance = Math.min(Util.getChanceWithMF(0.0003f, magicFind), 1f);
        float legendaryChance = Math.min(Util.getChanceWithMF(0.0001f, magicFind), 1f);
        float anyChance = Math.min(Util.getChanceWithMF(0.001f, magicFind), 1f);
        
        int rareKills = (int) Math.ceil(1 / rareChance);
        int epicKills = (int) Math.ceil(1 / epicChance);
        int legendaryKills = (int) Math.ceil(1 / legendaryChance);
        int anyKills = (int) Math.ceil(1 / anyChance);
        
        Util.sendModChatMessage(
                (magicFind > 0
                    ? "Scatha pet drop chances with " + EnumChatFormatting.AQUA + Util.getUnicodeString("272F") + " " + magicFind + " magic find" + EnumChatFormatting.RESET + ":\n"
                    : "Scatha pet drop base chances:\n"
                )
                + "Any: " + Util.numberToString(anyChance * 100, 3) + "% " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC +"(~" + anyKills + " kills)"+ EnumChatFormatting.RESET + "\n"
                + EnumChatFormatting.BLUE + "Rare: " + Util.numberToString(rareChance * 100, 3) + "% " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "(~" + rareKills + " kills)" + EnumChatFormatting.RESET + "\n"
                + EnumChatFormatting.DARK_PURPLE + "Epic: " + Util.numberToString(epicChance * 100, 3) + "% " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "(~" + epicKills + " kills)" + EnumChatFormatting.RESET + "\n"
                + EnumChatFormatting.GOLD + "Legendary: " + Util.numberToString(legendaryChance * 100, 3) + "% " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC +"(~" + legendaryKills + " kills)" + EnumChatFormatting.RESET
        );
    }
}
