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
        return "/scathachances [magic find] [pet luck] [kills]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {

        int magicFind = args.length > 0 ? Math.max(CommandBase.parseInt(args[0]), 0) : 0;
        int petLuck = args.length > 1 ? Math.max(CommandBase.parseInt(args[1]), 0) : 0;
        int kills = args.length > 2 ? Math.max(CommandBase.parseInt(args[2]), 0) : 0;

        float anyChance = Math.min(Util.calculatePetChance(0.004f, magicFind, petLuck), 1f);
        float rareChance = Math.min(Util.calculatePetChance(0.0024f, magicFind, petLuck), 1f);
        float epicChance = Math.min(Util.calculatePetChance(0.0012f, magicFind, petLuck), 1f);
        float legendaryChance = Math.min(Util.calculatePetChance(0.0004f, magicFind, petLuck), 1f);
        
        if (kills > 0) {
            float anyChanceAtKills = (float) (1 - Math.pow(1 - anyChance, kills));
            float rareChanceAtKills = (float) (1 - Math.pow(1 - rareChance, kills));
            float epicChanceAtKills = (float) (1 - Math.pow(1 - epicChance, kills));
            float legendaryChanceAtKills = (float) (1 - Math.pow(1 - legendaryChance, kills));
            
            Util.sendModChatMessage(
                    "Chances for dropping at least one scatha pet after " + EnumChatFormatting.RED + kills + " kill" + (kills == 1 ? "" : "s") + EnumChatFormatting.RESET + " with " + EnumChatFormatting.AQUA + Util.getUnicodeString("272F") + " " + magicFind + " magic find" + EnumChatFormatting.RESET + " and " + EnumChatFormatting.LIGHT_PURPLE + Util.getUnicodeString("2663") + " " + petLuck + " pet luck" + EnumChatFormatting.RESET + ":\n"
                    + "Any: " + Util.numberToString(anyChanceAtKills * 100, 3) + "%"+ EnumChatFormatting.RESET + "\n"
                    + EnumChatFormatting.BLUE + "Rare: " + Util.numberToString(rareChanceAtKills * 100, 2) + "%" + EnumChatFormatting.RESET + "\n"
                    + EnumChatFormatting.DARK_PURPLE + "Epic: " + Util.numberToString(epicChanceAtKills * 100, 2) + "%" + EnumChatFormatting.RESET + "\n"
                    + EnumChatFormatting.GOLD + "Legendary: " + Util.numberToString(legendaryChanceAtKills * 100, 2) + "%" + EnumChatFormatting.RESET
            );
        }
        else {
            boolean specificChances = magicFind > 0 || petLuck > 0;
            
            int rareKills = (int) Math.ceil(1 / rareChance);
            int epicKills = (int) Math.ceil(1 / epicChance);
            int legendaryKills = (int) Math.ceil(1 / legendaryChance);
            int anyKills = (int) Math.ceil(1 / anyChance);
            
            Util.sendModChatMessage(
                    (specificChances
                        ? "Scatha pet drop chances with " + EnumChatFormatting.AQUA + Util.getUnicodeString("272F") + " " + magicFind + " magic find" + EnumChatFormatting.RESET + " and " + EnumChatFormatting.LIGHT_PURPLE + Util.getUnicodeString("2663") + " " + petLuck + " pet luck" + EnumChatFormatting.RESET + ":\n"
                        : "Scatha pet drop base chances:\n"
                    )
                    + "Any: " + Util.numberToString(anyChance * 100, 3) + "% " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC +"(~" + anyKills + " kills)" + EnumChatFormatting.RESET + "\n"
                    + EnumChatFormatting.BLUE + "Rare: " + Util.numberToString(rareChance * 100, 3) + "% " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "(~" + rareKills + " kills)" + EnumChatFormatting.RESET + "\n"
                    + EnumChatFormatting.DARK_PURPLE + "Epic: " + Util.numberToString(epicChance * 100, 3) + "% " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "(~" + epicKills + " kills)" + EnumChatFormatting.RESET + "\n"
                    + EnumChatFormatting.GOLD + "Legendary: " + Util.numberToString(legendaryChance * 100, 3) + "% " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC +"(~" + legendaryKills + " kills)" + EnumChatFormatting.RESET
                    + (!specificChances
                            ?  "\n" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "/scathachances <magic find> <pet luck> [scatha kills]" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY + " for specific chances"+ EnumChatFormatting.RESET
                    : "")
            ); 
        }
    }
}
