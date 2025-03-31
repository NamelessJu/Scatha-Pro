package com.namelessju.scathapro.parsing.chestguiparsing;

import java.util.List;

import com.google.common.collect.Lists;
import com.namelessju.scathapro.Constants;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.managers.Config;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class ChestGuiParsingManager
{
    private ScathaPro scathaPro;
    
    private ChestGuiParser[] parsers = new ChestGuiParser[0];
    private List<ActiveParser> activeParsers = Lists.newArrayList();
    
    private GuiChest lastCheckedChest = null;
    private int guiTickCounter = 0;
    private int currentParsingTickCountIndex = -1;
    
    public final WormBestiaryParser wormBestiaryParser;
    public final ProfileStatsParser profileStatsParser;
    
    public ChestGuiParsingManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
        
        registerParser(wormBestiaryParser = new WormBestiaryParser());
        registerParser(profileStatsParser = new ProfileStatsParser());
    }
    
    public void registerParser(ChestGuiParser parser)
    {
        ChestGuiParser[] parsers = new ChestGuiParser[this.parsers.length + 1];
        for (int i = 0; i < this.parsers.length; i++) parsers[i] = this.parsers[i];
        parsers[parsers.length - 1] = parser;
        this.parsers = parsers;
    }
    
    public void tick()
    {
        GuiScreen openGui = scathaPro.getMinecraft().currentScreen;
        if (openGui == null || !(openGui instanceof GuiChest))
        {
            lastCheckedChest = null;
            return;
        }
        GuiChest chestGui = (GuiChest) openGui;
        
        IInventory chestInventory = ((ContainerChest) chestGui.inventorySlots).getLowerChestInventory();
        
        if (lastCheckedChest != null && lastCheckedChest == chestGui)
        {
            if (currentParsingTickCountIndex < 0 || currentParsingTickCountIndex >= Constants.chestGuiParserTickCounts.length) return;
        }
        else
        {
            scathaPro.logDebug(getLogMsg("New chest GUI opened"));
            
            activeParsers.clear();
            lastCheckedChest = chestGui;
            guiTickCounter = -1; // -1 so this tick is 0 after incrementing
            currentParsingTickCountIndex = 0;
            
            updateParserStates();
            
            if (chestInventory.hasCustomName())
            {
                String chestName = chestInventory.getDisplayName().getUnformattedText();
                for (ChestGuiParser parser : parsers)
                {
                    ActiveParser activeParser = ActiveParser.tryMakeParser(chestName, parser, scathaPro);
                    if (activeParser != null)
                    {
                        activeParsers.add(activeParser);
                        parser.onStartParsing();
                    }
                }
                int activeParserCount = activeParsers.size();
                if (activeParserCount <= 0)
                {
                    scathaPro.logDebug(getLogMsg("No (enabled) parsers found for GUI with name \"" + chestName + "\""));
                    currentParsingTickCountIndex = -1;
                    return;
                }
                scathaPro.logDebug(getLogMsg("Found " + activeParserCount + " parsers for GUI with name \"" + chestName + "\""));
            }
            else
            {
                scathaPro.logDebug(getLogMsg("Chest has no custom name, no parsing required"));
                currentParsingTickCountIndex = -1;
                return;
            }
        }
        
        guiTickCounter ++;
        
        if (guiTickCounter >= Constants.chestGuiParserTickCounts[currentParsingTickCountIndex])
        {
            scathaPro.logDebug(getLogMsg("Parsing try #" + (currentParsingTickCountIndex + 1) + " (" + Constants.chestGuiParserTickCounts[currentParsingTickCountIndex] + " ticks after opening GUI)"));
            
            currentParsingTickCountIndex ++;
            
            boolean allParsersFinished = true;
            for (ActiveParser activeParser : activeParsers)
            {
                boolean parserFinished = activeParser.parse(chestInventory);
                if (parserFinished) activeParser.parser.onFinishParsing();
                else allParsersFinished = false;
            }
            
            if (allParsersFinished)
            {
                scathaPro.logDebug(getLogMsg("All parsers finished"));
                currentParsingTickCountIndex = -1;
            }
            else if (currentParsingTickCountIndex >= Constants.chestGuiParserTickCounts.length)
            {
                scathaPro.logWarning(getLogMsg("Ran out of parsing tries without all parsers finishing"));
            }
        }
    }
    
    private void updateParserStates()
    {
        wormBestiaryParser.enabled = scathaPro.getConfig().getBoolean(Config.Key.automaticWormStatsParsing);
    }
    
    private static String getLogMsg(String text)
    {
        return "Chest GUI Parsing: " + text;
    }
    
    
    private static class ActiveParser
    {
        private final ScathaPro scathaPro;
        
        public final ChestGuiParser parser;
        public final int[] slots;
        public final boolean[] finishedSlots;
        
        public static ActiveParser tryMakeParser(String chestName, ChestGuiParser parser, ScathaPro scathaPro)
        {
            if (parser.enabled && chestName.equals(parser.getGuiName())) return new ActiveParser(parser, scathaPro);
            return null;
        }
        
        private ActiveParser(ChestGuiParser parser, ScathaPro scathaPro)
        {
            this.scathaPro = scathaPro;
            
            this.parser = parser;
            this.slots = parser.getSlotNumbers();
            this.finishedSlots = new boolean[slots.length];
        }
        
        public boolean parse(IInventory chestInventory)
        {
            boolean finishedAllSlots = true;
            
            for (int slotNumberIndex = 0; slotNumberIndex < slots.length; slotNumberIndex ++)
            {
                if (finishedSlots[slotNumberIndex]) continue;
                
                int slotNumber = slots[slotNumberIndex];
                
                if (0 <= slotNumber && slotNumber < chestInventory.getSizeInventory())
                {
                    ItemStack itemStack = chestInventory.getStackInSlot(slotNumber);
                    if (itemStack == null)
                    {
                        finishedAllSlots = false;
                        continue;
                    }
                    parser.tryParse(itemStack, slotNumber, scathaPro);
                }
                else scathaPro.logWarning(ChestGuiParsingManager.getLogMsg("Slot index " + slotNumber + " out of bounds for GUI \"" + parser.getGuiName() + "\""));
                finishedSlots[slotNumberIndex] = true;
            }
            
            return finishedAllSlots;
        }
    }
}
