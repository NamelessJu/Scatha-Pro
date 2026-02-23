package namelessju.scathapro.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.managers.ChatManager;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.UnicodeSymbol;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

public class ScathaChancesCommand extends ScathaProCommand
{
    public ScathaChancesCommand(ScathaPro scathaPro)
    {
        super(scathaPro);
    }
    
    @Override
    public String getCommandName()
    {
        return "scathachances";
    }
    
    @Override
    protected String[] getAliases()
    {
        return new String[] {"scacha"};
    }
    
    @Override
    protected <T> void buildCommand(LiteralArgumentBuilder<T> builder, CommandBuildContext buildContext)
    {
        builder.executes(commandContext -> {
                calculateChances(0f, 0f, 0);
                return Command.SINGLE_SUCCESS;
            })
            .then(RequiredArgumentBuilder.<T, Float>argument("Magic Find", FloatArgumentType.floatArg(0))
                .executes(commandContext -> {
                    float magicFind = FloatArgumentType.getFloat(commandContext, "Magic Find");
                    calculateChances(magicFind, 0f, 0);
                    return Command.SINGLE_SUCCESS;
                })
                .then(RequiredArgumentBuilder.<T, Float>argument("Pet Luck", FloatArgumentType.floatArg(0))
                    .executes(commandContext -> {
                        float magicFind = FloatArgumentType.getFloat(commandContext, "Magic Find");
                        float petLuck = FloatArgumentType.getFloat(commandContext, "Pet Luck");
                        calculateChances(magicFind, petLuck, 0);
                        return Command.SINGLE_SUCCESS;
                    })
                    .then(RequiredArgumentBuilder.<T, Integer>argument("Scatha Kills", IntegerArgumentType.integer(2))
                        .executes(commandContext -> {
                            float magicFind = FloatArgumentType.getFloat(commandContext, "Magic Find");
                            float petLuck = FloatArgumentType.getFloat(commandContext, "Pet Luck");
                            int kills = IntegerArgumentType.getInteger(commandContext, "Scatha Kills");
                            calculateChances(magicFind, petLuck, kills);
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                )
            );
    }
    
    private void calculateChances(float magicFind, float petLuck, int kills)
    {
        boolean calculateSpecificChances = magicFind > 0 || petLuck > 0;
        
        float rareChance = Constants.scathaPetBaseChanceRare;
        float epicChance = Constants.scathaPetBaseChanceEpic;
        float legendaryChance = Constants.scathaPetBaseChanceLegendary;
        
        if (calculateSpecificChances)
        {
            rareChance = calculatePetChance(rareChance, magicFind, petLuck);
            epicChance = calculatePetChance(epicChance, magicFind, petLuck);
            legendaryChance = calculatePetChance(legendaryChance, magicFind, petLuck);
        }
        
        float anyChance = rareChance + epicChance + legendaryChance;
        
        // Clamp to the respective % of the any chance
        // (if any >= 100% then these should not go higher either)
        rareChance = Math.min(rareChance, rareChance/anyChance);
        epicChance = Math.min(epicChance, epicChance/anyChance);
        legendaryChance = Math.min(legendaryChance, legendaryChance/anyChance);
        
        anyChance = Math.min(1, anyChance);
        
        
        MutableComponent attributesComponent = Component.empty()
            .append(Component.literal(
                    UnicodeSymbol.magicFind + " " + TextUtil.numberToString(magicFind, 2) + " Magic Find"
                ).withStyle(ChatFormatting.AQUA)
            );
        if (petLuck > 0)
        {
            attributesComponent
                .append(" and ")
                .append(Component.literal(
                    UnicodeSymbol.petLuck + " " + TextUtil.numberToString(petLuck, 2) + " Pet Luck"
                    ).withStyle(ChatFormatting.LIGHT_PURPLE)
                );
        }
        
        
        if (kills == 0)
        {
            int rareKillsAverage = Mth.ceil(1 / rareChance);
            int epicKillsAverage = Mth.ceil(1 / epicChance);
            int legendaryKillsAverage = Mth.ceil(1 / legendaryChance);
            int anyKillsAverage = Mth.ceil(1 / anyChance);
            
            scathaPro.chatManager.sendChatDivider();
            scathaPro.chatManager.sendChatMessage(Component.empty()
                .append(Component.empty().setStyle(ChatManager.HIGHLIGHT_STYLE)
                    .append(
                        calculateSpecificChances
                        ? Component.empty().append("Scatha pet drop chances with ").append(attributesComponent).append(":\n")
                        : Component.literal("Scatha pet drop base chances:\n")
                    )
                )
                .append(Component.empty()
                    .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal("Any: " + TextUtil.numberToString(anyChance * 100, 3) + "% ")
                        .withStyle(ChatFormatting.WHITE))
                    .append(Component.literal("(" + anyKillsAverage + " Scatha kills on average)").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                    .append("\n")
                )
                .append(Component.empty()
                    .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal("Rare: " + TextUtil.numberToString(rareChance * 100, 3) + "% ")
                        .withStyle(ChatFormatting.BLUE))
                    .append(Component.literal("(" + rareKillsAverage + " Scatha kills on average)").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                    .append("\n")
                )
                .append(Component.empty()
                    .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal("Epic: " + TextUtil.numberToString(epicChance * 100, 3) + "% ")
                        .withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.literal("(" + epicKillsAverage + " Scatha kills on average)").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                    .append("\n")
                )
                .append(Component.empty()
                    .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal("Legendary: " + TextUtil.numberToString(legendaryChance * 100, 3) + "% ")
                        .withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("(" + legendaryKillsAverage + " Scatha kills on average)").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                )
            );
            scathaPro.chatManager.sendChatDivider();
        }
        else
        {
            float anyChanceAtKills = (float) (1 - Math.pow(1 - anyChance, kills));
            float rareChanceAtKills = (float) (1 - Math.pow(1 - rareChance, kills));
            float epicChanceAtKills = (float) (1 - Math.pow(1 - epicChance, kills));
            float legendaryChanceAtKills = (float) (1 - Math.pow(1 - legendaryChance, kills));
            
            scathaPro.chatManager.sendChatDivider();
            scathaPro.chatManager.sendChatMessage(Component.empty()
                .append(Component.empty().setStyle(
                    ChatManager.HIGHLIGHT_STYLE
                        .withHoverEvent(new HoverEvent.ShowText(Component.empty()
                            .append(Component.literal("Note:\n").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                            .append("Killing more Scathas does NOT\nincrease the drop chance per kill!\n")
                            .append(Component.literal("""
                                    However, the more often you roll
                                    the chance (by killing more Scathas)
                                    the higher the chance to drop
                                    a Scatha pet overall becomes!""")
                                .withStyle(ChatFormatting.GRAY)
                            )
                        ))
                    )
                    .append("You have the following chances to drop at least 1 Scatha pet during the process of killing ")
                    .append(Component.literal(UnicodeSymbol.crossedSwords + " " + kills + " Scatha" + (kills == 1 ? "" : "s"))
                        .withStyle(ChatFormatting.RED))
                    .append(" with ").append(attributesComponent).append(":\n")
                )
                .append(Component.empty()
                    .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal("Any: " + TextUtil.numberToString(Math.min(anyChanceAtKills * 100, 99.999f), 3) + "%")
                        .withStyle(ChatFormatting.WHITE))
                    .append("\n")
                )
                .append(Component.empty()
                    .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal("Rare: " + TextUtil.numberToString(Math.min(rareChanceAtKills * 100, 99.999f), 3) + "%")
                        .withStyle(ChatFormatting.BLUE))
                    .append("\n")
                )
                .append(Component.empty()
                    .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal("Epic: " + TextUtil.numberToString(Math.min(epicChanceAtKills * 100, 99.999f), 3) + "%")
                        .withStyle(ChatFormatting.DARK_PURPLE))
                    .append("\n")
                )
                .append(Component.empty()
                    .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal("Legendary: " + TextUtil.numberToString(Math.min(legendaryChanceAtKills * 100, 99.999f), 3) + "%")
                        .withStyle(ChatFormatting.GOLD))
                )
            );
            scathaPro.chatManager.sendChatDivider();
        }
    }
    
    private float calculatePetChance(float initialChance, float magicFind, float petLuck)
    {
        return initialChance * (1f + (magicFind + petLuck)/100f);
    }
}
