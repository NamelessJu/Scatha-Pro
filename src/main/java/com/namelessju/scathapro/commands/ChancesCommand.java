package com.namelessju.scathapro.commands;

import java.util.ArrayList;
import java.util.List;

import com.namelessju.scathapro.util.ChatUtil;
import com.namelessju.scathapro.util.NBTUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

public class ChancesCommand extends CommandBase {
    
    private static final float rareBaseChance = 0.0024f;
    private static final float epicBaseChance = 0.0012f;
    private static final float legendaryBaseChance = 0.0004f;
    
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
        EntityPlayer player = sender instanceof EntityPlayer ? (EntityPlayer) sender : null;
        
        
        int magicFind = args.length > 0 ? Math.max(CommandBase.parseInt(args[0]), 0) : 0;
        int petLuck = args.length > 1 ? Math.max(CommandBase.parseInt(args[1]), 0) : 0;
        int kills = args.length > 2 ? Math.max(CommandBase.parseInt(args[2]), 0) : 0;
        
        int looting = 0;
        NBTTagCompound enchantments = NBTUtil.getSkyblockTagCompound(player != null ? player.getHeldItem() : null, "enchantments");
        if (enchantments != null)
            looting = enchantments.getInteger("looting");
        

        boolean specificChances = magicFind > 0 || petLuck > 0;
        
        float anyChance = rareBaseChance + epicBaseChance + legendaryBaseChance;
        float rareChance = rareBaseChance;
        float epicChance = epicBaseChance;
        float legendaryChance = legendaryBaseChance;
        
        if (kills > 0 || specificChances) {
            anyChance = Math.min(Util.calculatePetChance(anyChance, magicFind, petLuck, looting), 1f);
            rareChance = Math.min(Util.calculatePetChance(rareChance, magicFind, petLuck, looting), 1f);
            epicChance = Math.min(Util.calculatePetChance(epicChance, magicFind, petLuck, looting), 1f);
            legendaryChance = Math.min(Util.calculatePetChance(legendaryChance, magicFind, petLuck, looting), 1f);
        }
        
        
        String attributesString = EnumChatFormatting.AQUA + Util.getUnicodeString("272F") + " " + magicFind + " magic find" + EnumChatFormatting.RESET + (looting > 0 ? ", " : " and ") + EnumChatFormatting.LIGHT_PURPLE + Util.getUnicodeString("2663") + " " + petLuck + " pet luck" + EnumChatFormatting.RESET + (looting > 0 ? " and " + EnumChatFormatting.BLUE + "looting " + looting + EnumChatFormatting.GRAY + " (held item)" + EnumChatFormatting.RESET : "") + EnumChatFormatting.RESET;
        
        
        if (kills == 0) {
            
            int rareKills = (int) Math.ceil(1 / rareChance);
            int epicKills = (int) Math.ceil(1 / epicChance);
            int legendaryKills = (int) Math.ceil(1 / legendaryChance);
            int anyKills = (int) Math.ceil(1 / anyChance);
            
            ChatUtil.sendModChatMessage(
                    (specificChances
                        ? "Scatha pet drop chances with " + attributesString + ":\n"
                        : "Scatha pet drop base chances:\n"
                    )
                    + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.RESET + "Any: " + (anyChance >= 0.999995f ? Util.numberToString(99.999f, 3) : Util.numberToString(anyChance * 100, 3)) + "% " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC +"(~" + anyKills + " kills)" + EnumChatFormatting.RESET + "\n"
                    + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.BLUE + "Rare: " + (rareChance >= 0.999995f ? Util.numberToString(99.999f, 3) : Util.numberToString(rareChance * 100, 3)) + "% " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "(~" + rareKills + " kills)" + EnumChatFormatting.RESET + "\n"
                    + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.DARK_PURPLE + "Epic: " + (epicChance >= 0.999995f ? Util.numberToString(99.999f, 3) : Util.numberToString(epicChance * 100, 3)) + "% " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "(~" + epicKills + " kills)" + EnumChatFormatting.RESET + "\n"
                    + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.GOLD + "Legendary: " + (legendaryChance >= 0.999995f ? Util.numberToString(99.999f, 3) : Util.numberToString(legendaryChance * 100, 3)) + "% " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC +"(~" + legendaryKills + " kills)" + EnumChatFormatting.RESET
                    + (!specificChances
                            ?  "\n" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "/scathachances <magic find> <pet luck> [scatha kills]" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY + " for specific chances (+ hold a weapon for looting)"+ EnumChatFormatting.RESET
                    : "")
            );
        }
        else {
            float anyChanceAtKills = (float) (1 - Math.pow(1 - anyChance, kills));
            float rareChanceAtKills = (float) (1 - Math.pow(1 - rareChance, kills));
            float epicChanceAtKills = (float) (1 - Math.pow(1 - epicChance, kills));
            float legendaryChanceAtKills = (float) (1 - Math.pow(1 - legendaryChance, kills));
            
            ChatUtil.sendModChatMessage(
                    " Killing " + EnumChatFormatting.RED + Util.getUnicodeString("2694") + " " + kills + " scatha" + (kills == 1 ? "" : "s") + EnumChatFormatting.RESET + " with " + attributesString + " has the following chances to drop at least 1 scatha pet:\n"
                    + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.RESET + "Any: " + (anyChanceAtKills >= 0.999995f ? Util.numberToString(99.999f, 3) : Util.numberToString(anyChanceAtKills * 100, 3)) + "%"+ EnumChatFormatting.RESET + "\n"
                    + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.BLUE + "Rare: " + (rareChanceAtKills >= 0.999995f ? Util.numberToString(99.999f, 3) : Util.numberToString(rareChanceAtKills * 100, 3)) + "%" + EnumChatFormatting.RESET + "\n"
                    + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.DARK_PURPLE + "Epic: " + (epicChanceAtKills >= 0.999995f ? Util.numberToString(99.999f, 3) : Util.numberToString(epicChanceAtKills * 100, 3)) + "%" + EnumChatFormatting.RESET + "\n"
                    + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.GOLD + "Legendary: " + (legendaryChanceAtKills >= 0.999995f ? Util.numberToString(99.999f, 3) : Util.numberToString(legendaryChanceAtKills * 100, 3)) + "%" + EnumChatFormatting.RESET
            );
        }
    }
}
