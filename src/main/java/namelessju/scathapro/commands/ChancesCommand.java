package namelessju.scathapro.commands;

import java.util.List;

import com.google.common.collect.Lists;

import namelessju.scathapro.Constants;
import namelessju.scathapro.util.NBTUtil;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.UnicodeSymbol;
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
        return "/" + COMMAND_NAME + " help";
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
            TextUtil.sendModChatMessage(Constants.msgHighlightingColor + "Scatha chances calculator:");
            MainCommand.sendHelpMessageEntry(COMMAND_NAME, null, "/scacha", "Shows the base Scatha pet drop chances", true);
            MainCommand.sendHelpMessageEntry(COMMAND_NAME, "<EMF/Magic Find> (<Pet Luck>)", null, "Calculates specific Scatha pet drop chances");
            MainCommand.sendHelpMessageEntry(COMMAND_NAME, "<Magic Find> <Pet Luck> <Scatha kills>", null, "Calculates the chances for dropping at least one Scatha pet during the process of killing that many Scathas");
            TextUtil.sendModChatMessage(EnumChatFormatting.YELLOW + "Remember to add worm bestiary Magic Find to your total Magic Find number!", false);
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
            rareChance = Math.min(calculatePetChance(rareChance, magicFind, petLuck, looting), 1f);
            epicChance = Math.min(calculatePetChance(epicChance, magicFind, petLuck, looting), 1f);
            legendaryChance = Math.min(calculatePetChance(legendaryChance, magicFind, petLuck, looting), 1f);
        }
        
        float anyChance = rareChance + epicChance + legendaryChance;
        
        
        String attributesString = "";
        if (petLuck <= 0)
        {
            attributesString = EnumChatFormatting.BLUE.toString() + UnicodeSymbol.magicFind + "/" + UnicodeSymbol.petLuck + " " + TextUtil.numberToString(magicFind, 2) + " Effective Magic Find" + Constants.msgHighlightingColor;
        }
        else
        {
            attributesString = EnumChatFormatting.AQUA.toString() + UnicodeSymbol.magicFind + " " + TextUtil.numberToString(magicFind, 2) + " Magic Find" + Constants.msgHighlightingColor + (looting > 0 ? ", " : " and ") + EnumChatFormatting.LIGHT_PURPLE + UnicodeSymbol.petLuck + " " + TextUtil.numberToString(petLuck, 2) + " Pet Luck" + Constants.msgHighlightingColor;
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
                + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.RESET + "Any: " + (anyChance >= 0.999995f ? TextUtil.numberToString(99.999f, 3) : TextUtil.numberToString(anyChance * 100, 3)) + "% " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC +"(" + anyKills + " Scatha kills on average)" + EnumChatFormatting.RESET + "\n"
                + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.BLUE + "Rare: " + (rareChance >= 0.999995f ? TextUtil.numberToString(99.999f, 3) : TextUtil.numberToString(rareChance * 100, 3)) + "% " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "(" + rareKills + " Scatha kills on average)" + EnumChatFormatting.RESET + "\n"
                + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.DARK_PURPLE + "Epic: " + (epicChance >= 0.999995f ? TextUtil.numberToString(99.999f, 3) : TextUtil.numberToString(epicChance * 100, 3)) + "% " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "(" + epicKills + " Scatha kills on average)" + EnumChatFormatting.RESET + "\n"
                + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.GOLD + "Legendary: " + (legendaryChance >= 0.999995f ? TextUtil.numberToString(99.999f, 3) : TextUtil.numberToString(legendaryChance * 100, 3)) + "% " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC +"(" + legendaryKills + " Scatha kills on average)"
            );
            TextUtil.sendChatDivider();
        }
        else
        {
            float anyChanceAtKills = (float) (1 - Math.pow(1 - anyChance, kills));
            float rareChanceAtKills = (float) (1 - Math.pow(1 - rareChance, kills));
            float epicChanceAtKills = (float) (1 - Math.pow(1 - epicChance, kills));
            float legendaryChanceAtKills = (float) (1 - Math.pow(1 - legendaryChance, kills));
            
            ChatComponentText descriptionMessage = new ChatComponentText(Constants.msgHighlightingColor + "You have the following chances to drop at least 1 Scatha pet while killing " + EnumChatFormatting.RED + UnicodeSymbol.crossedSwords + " " + kills + " Scatha" + (kills == 1 ? "" : "s") + Constants.msgHighlightingColor + " with " + attributesString + ":");
            descriptionMessage.setChatStyle(new ChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "Note:\n" + EnumChatFormatting.RESET + "Killing more Scathas does NOT\n" + EnumChatFormatting.RESET + "increase the drop chance per kill!\n" + EnumChatFormatting.GRAY + "However, the more often you roll\n" + EnumChatFormatting.GRAY + "the chance (by killing more Scathas)\n" + EnumChatFormatting.GRAY + "the higher the chance to drop\n" + EnumChatFormatting.GRAY + "a Scatha pet overall gets!"))));

            TextUtil.sendChatDivider();
            TextUtil.sendModChatMessage(descriptionMessage);
            TextUtil.sendModChatMessage(
                EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.RESET + "Any: " + (anyChanceAtKills >= 0.999995f ? TextUtil.numberToString(99.999f, 3) : TextUtil.numberToString(anyChanceAtKills * 100, 3)) + "%"+ EnumChatFormatting.RESET + "\n"
                + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.BLUE + "Rare: " + (rareChanceAtKills >= 0.999995f ? TextUtil.numberToString(99.999f, 3) : TextUtil.numberToString(rareChanceAtKills * 100, 3)) + "%" + EnumChatFormatting.RESET + "\n"
                + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.DARK_PURPLE + "Epic: " + (epicChanceAtKills >= 0.999995f ? TextUtil.numberToString(99.999f, 3) : TextUtil.numberToString(epicChanceAtKills * 100, 3)) + "%" + EnumChatFormatting.RESET + "\n"
                + EnumChatFormatting.DARK_GRAY + " - " + EnumChatFormatting.GOLD + "Legendary: " + (legendaryChanceAtKills >= 0.999995f ? TextUtil.numberToString(99.999f, 3) : TextUtil.numberToString(legendaryChanceAtKills * 100, 3)) + "%"
            , false);
            TextUtil.sendChatDivider();
        }
    }
    
    
    private float calculatePetChance(float initialChance, float magicFind, float petLuck, int looting)
    {
        return initialChance * (1f + (magicFind + petLuck)/100f) * (1 + looting * 0.15f);
    }
}
