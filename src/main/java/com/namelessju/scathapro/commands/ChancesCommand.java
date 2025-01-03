package com.namelessju.scathapro.commands;

import java.util.List;

import com.google.common.collect.Lists;
import com.namelessju.scathapro.Constants;
import com.namelessju.scathapro.util.TextUtil;
import com.namelessju.scathapro.util.NBTUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.HoverEvent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

public class ChancesCommand extends CommandBase
{
    public static final String COMMAND_NAME = "scathachances";
    
    @Override
    public String getCommandName()
    {
        return COMMAND_NAME;
    }
    
    @Override
    public List<String> getCommandAliases()
    {
        return Lists.newArrayList("scacha");
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/" + COMMAND_NAME + " (magic find) (pet luck) (Scatha kills)";
    }
    
    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }
    
    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        EntityPlayer player = sender instanceof EntityPlayer ? (EntityPlayer) sender : null;
        
        if (args.length > 0 && args[0].equalsIgnoreCase("help"))
        {
            TextUtil.sendChatDivider();
            TextUtil.sendModChatMessage(
                Constants.msgHighlightingColor + "Scatha chances calculator:\n"
                + EnumChatFormatting.WHITE + "/" + COMMAND_NAME + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + " (alias /scacha)" + EnumChatFormatting.WHITE + ":" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + " Shows the base Scatha pet drop chances\n"
                + EnumChatFormatting.WHITE + "/" + COMMAND_NAME + " <Magic Find> (Pet Luck): " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "Calculates specific Scatha pet drop chances\n"
                + EnumChatFormatting.WHITE + "/" + COMMAND_NAME + " <Magic Find> <Pet Luck> <Scatha kills>: " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "Calculates the chances for dropping at least one Scatha pet during the process of killing x Scathas\n"
                + EnumChatFormatting.YELLOW + EnumChatFormatting.ITALIC + "Remember to add worm bestiary Magic Find to your total Magic Find number!"
            );
            TextUtil.sendChatDivider();
            return;
        }
        
        float magicFind = args.length > 0 ? (float) Math.max(CommandBase.parseDouble(args[0]), 0) : 0f;
        float petLuck = args.length > 1 ? (float) Math.max(CommandBase.parseDouble(args[1]), 0) : 0f;
        int kills = args.length > 2 ? CommandBase.parseInt(args[2]) : 0;
        
        if (kills < 0) throw new CommandException("Scatha kills must be larger than 1!");
        if (kills == 1) throw new CommandException("Scatha kills must be larger than 1! (leave the argument out instead of entering 1)");
        
        // Looting doesn't seem to work on (Scatha) pet drops,
        // but this secret parameter keeps it in the mod in case it gets fixed/changed by Hypixel
        boolean useLooting = args.length > 3 ? CommandBase.parseBoolean(args[3]) : false;
        int looting = 0;
        if (useLooting)
        {
            NBTTagCompound enchantments = NBTUtil.getSkyblockTagCompound(player != null ? player.getHeldItem() : null, "enchantments");
            if (enchantments != null) looting = enchantments.getInteger("looting");
        }
        

        boolean specificChances = magicFind > 0 || petLuck > 0 || useLooting;
        
        float rareChance = Constants.scathaPetBaseChanceRare;
        float epicChance = Constants.scathaPetBaseChanceEpic;
        float legendaryChance = Constants.scathaPetBaseChanceLegendary;
        
        if (kills > 0 || specificChances)
        {
            rareChance = Math.min(Util.calculatePetChance(rareChance, magicFind, petLuck, looting), 1f);
            epicChance = Math.min(Util.calculatePetChance(epicChance, magicFind, petLuck, looting), 1f);
            legendaryChance = Math.min(Util.calculatePetChance(legendaryChance, magicFind, petLuck, looting), 1f);
        }
        
        float anyChance = rareChance + epicChance + legendaryChance;
        
        
        String attributesString = "";
        if (petLuck <= 0)
        {
            attributesString = EnumChatFormatting.AQUA + "\u272F" + EnumChatFormatting.GRAY + "/" + EnumChatFormatting.LIGHT_PURPLE + "\u2663 " + EnumChatFormatting.AQUA + Util.numberToString(magicFind, 2) + EnumChatFormatting.LIGHT_PURPLE + " Effective " + EnumChatFormatting.AQUA + "Magic Find" + Constants.msgHighlightingColor;
        }
        else
        {
            attributesString = EnumChatFormatting.AQUA + "\u272F" + " " + Util.numberToString(magicFind, 2) + " Magic Find" + Constants.msgHighlightingColor + (looting > 0 ? ", " : " and ") + EnumChatFormatting.LIGHT_PURPLE + "\u2663" + " " + Util.numberToString(petLuck, 2) + " Pet Luck" + Constants.msgHighlightingColor;
        }
        if (looting > 0)
        {
            attributesString += " and " + EnumChatFormatting.BLUE + "looting " + looting + EnumChatFormatting.GRAY + " (held item)" + Constants.msgHighlightingColor;
        }
        
        
        if (kills == 0)
        {
            int rareKills = (int) Math.ceil(1 / rareChance);
            int epicKills = (int) Math.ceil(1 / epicChance);
            int legendaryKills = (int) Math.ceil(1 / legendaryChance);
            int anyKills = (int) Math.ceil(1 / anyChance);

            TextUtil.sendChatDivider();
            TextUtil.sendModChatMessage(
                Constants.msgHighlightingColor + (
                        specificChances
                        ? "Scatha pet drop chances with " + attributesString + ":\n"
                        : "Scatha pet drop base chances:\n"
                )
                + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.RESET + "Any: " + (anyChance >= 0.999995f ? Util.numberToString(99.999f, 3) : Util.numberToString(anyChance * 100, 3)) + "% " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC +"(" + anyKills + " Scatha kills on average)" + EnumChatFormatting.RESET + "\n"
                + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.BLUE + "Rare: " + (rareChance >= 0.999995f ? Util.numberToString(99.999f, 3) : Util.numberToString(rareChance * 100, 3)) + "% " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "(" + rareKills + " Scatha kills on average)" + EnumChatFormatting.RESET + "\n"
                + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.DARK_PURPLE + "Epic: " + (epicChance >= 0.999995f ? Util.numberToString(99.999f, 3) : Util.numberToString(epicChance * 100, 3)) + "% " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "(" + epicKills + " Scatha kills on average)" + EnumChatFormatting.RESET + "\n"
                + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.GOLD + "Legendary: " + (legendaryChance >= 0.999995f ? Util.numberToString(99.999f, 3) : Util.numberToString(legendaryChance * 100, 3)) + "% " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC +"(" + legendaryKills + " Scatha kills on average)"
            );
            TextUtil.sendChatDivider();
        }
        else
        {
            float anyChanceAtKills = (float) (1 - Math.pow(1 - anyChance, kills));
            float rareChanceAtKills = (float) (1 - Math.pow(1 - rareChance, kills));
            float epicChanceAtKills = (float) (1 - Math.pow(1 - epicChance, kills));
            float legendaryChanceAtKills = (float) (1 - Math.pow(1 - legendaryChance, kills));
            
            ChatComponentText descriptionMessage = new ChatComponentText(Constants.msgHighlightingColor + "You have the following chances to drop at least 1 Scatha pet while killing " + EnumChatFormatting.RED + "\u2694" + " " + kills + " Scatha" + (kills == 1 ? "" : "s") + Constants.msgHighlightingColor + " with " + attributesString + ":");
            descriptionMessage.setChatStyle(new ChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.RED + "Killing more Scathas does NOT increase the drop chance per kill!\n" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY + "However, the more often you roll the chance (by killing more Scathas) the higher the chance to drop a Scatha pet overall gets!"))));

            TextUtil.sendChatDivider();
            TextUtil.sendModChatMessage(descriptionMessage);
            TextUtil.sendModChatMessage(
                EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.RESET + "Any: " + (anyChanceAtKills >= 0.999995f ? Util.numberToString(99.999f, 3) : Util.numberToString(anyChanceAtKills * 100, 3)) + "%"+ EnumChatFormatting.RESET + "\n"
                + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.BLUE + "Rare: " + (rareChanceAtKills >= 0.999995f ? Util.numberToString(99.999f, 3) : Util.numberToString(rareChanceAtKills * 100, 3)) + "%" + EnumChatFormatting.RESET + "\n"
                + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.DARK_PURPLE + "Epic: " + (epicChanceAtKills >= 0.999995f ? Util.numberToString(99.999f, 3) : Util.numberToString(epicChanceAtKills * 100, 3)) + "%" + EnumChatFormatting.RESET + "\n"
                + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.GOLD + "Legendary: " + (legendaryChanceAtKills >= 0.999995f ? Util.numberToString(99.999f, 3) : Util.numberToString(legendaryChanceAtKills * 100, 3)) + "%"
            , false);
            TextUtil.sendChatDivider();
        }
    }
}
