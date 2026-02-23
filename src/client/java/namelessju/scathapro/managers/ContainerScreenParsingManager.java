package namelessju.scathapro.managers;

import com.google.common.collect.Lists;
import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.parsing.containerscreenparsing.ContainerScreenParser;
import namelessju.scathapro.parsing.containerscreenparsing.ProfileStatsParser;
import namelessju.scathapro.parsing.containerscreenparsing.WormBestiaryParser;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.util.StringDecomposer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ContainerScreenParsingManager
{
    private final ScathaPro scathaPro;
    
    private final List<ContainerScreenParser> parsers = Lists.newArrayList();
    private final List<ActiveParser> activeParsers = Lists.newArrayList();
    
    private ContainerScreen lastCheckedChest = null;
    private int guiTickCounter = 0;
    private int currentParsingTickCountIndex = -1;
    
    public final WormBestiaryParser wormBestiaryParser;
    public final ProfileStatsParser profileStatsParser;
    
    public ContainerScreenParsingManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
        
        registerParser(wormBestiaryParser = new WormBestiaryParser());
        registerParser(profileStatsParser = new ProfileStatsParser());
    }
    
    public void registerParser(ContainerScreenParser parser)
    {
        parsers.add(parser);
    }
    
    public void tick()
    {
        Screen currentScreen = scathaPro.minecraft.screen;
        if (!(currentScreen instanceof ContainerScreen containerScreen))
        {
            lastCheckedChest = null;
            return;
        }
        
        if (lastCheckedChest != null && lastCheckedChest == containerScreen)
        {
            if (currentParsingTickCountIndex < 0 || currentParsingTickCountIndex >= Constants.chestGuiParserTickCounts.length) return;
        }
        else
        {
            ScathaPro.LOGGER.debug(getLogMsg("New chest screen opened"));
            
            activeParsers.clear();
            lastCheckedChest = containerScreen;
            guiTickCounter = -1; // -1 so this tick is 0 after incrementing
            currentParsingTickCountIndex = 0;
            
            updateParserStates();
            
            String chestName = StringDecomposer.getPlainText(containerScreen.getTitle());
            for (ContainerScreenParser parser : parsers)
            {
                ActiveParser activeParser = ActiveParser.tryMakeParser(chestName, parser, scathaPro);
                if (activeParser != null)
                {
                    activeParsers.add(activeParser);
                    parser.onStartParsing();
                }
            }
            int activeParserCount = activeParsers.size();
            if (activeParserCount == 0)
            {
                ScathaPro.LOGGER.debug(getLogMsg("No (enabled) parsers found for chest screen with name \"" + chestName + "\""));
                currentParsingTickCountIndex = -1;
                return;
            }
            else ScathaPro.LOGGER.debug(getLogMsg("Found " + activeParserCount + " parsers for chest screen with name \"" + chestName + "\""));
        }
        
        guiTickCounter ++;
        
        if (guiTickCounter >= Constants.chestGuiParserTickCounts[currentParsingTickCountIndex])
        {
            ScathaPro.LOGGER.debug(getLogMsg("Parsing try #" + (currentParsingTickCountIndex + 1) + " (" + Constants.chestGuiParserTickCounts[currentParsingTickCountIndex] + " ticks after opening GUI)"));
            
            currentParsingTickCountIndex ++;
            
            Container container = containerScreen.getMenu().getContainer();
            
            boolean allParsersFinished = true;
            for (ActiveParser activeParser : activeParsers)
            {
                boolean parserFinished = activeParser.parse(container);
                if (parserFinished) activeParser.parser.onFinishParsing();
                else allParsersFinished = false;
            }
            
            if (allParsersFinished)
            {
                ScathaPro.LOGGER.debug(getLogMsg("All parsers finished"));
                currentParsingTickCountIndex = -1;
            }
            else if (currentParsingTickCountIndex >= Constants.chestGuiParserTickCounts.length)
            {
                ScathaPro.LOGGER.warn(getLogMsg("Ran out of parsing tries without all parsers finishing"));
            }
        }
    }
    
    private void updateParserStates()
    {
        wormBestiaryParser.enabled = scathaPro.config.miscellaneous.automaticStatsParsingEnabled.get();
    }
    
    private static String getLogMsg(String text)
    {
        return "Chest Screen Parsing: " + text;
    }
    
    
    private static class ActiveParser
    {
        private final ScathaPro scathaPro;
        
        public final ContainerScreenParser parser;
        public final int[] slots;
        public final boolean[] finishedSlots;
        
        public static ActiveParser tryMakeParser(String chestName, ContainerScreenParser parser, ScathaPro scathaPro)
        {
            if (parser.enabled && chestName.equals(parser.getScreenTitle())) return new ActiveParser(parser, scathaPro);
            return null;
        }
        
        private ActiveParser(ContainerScreenParser parser, ScathaPro scathaPro)
        {
            this.scathaPro = scathaPro;
            
            this.parser = parser;
            this.slots = parser.getSlotNumbers();
            this.finishedSlots = new boolean[slots.length];
        }
        
        public boolean parse(Container container)
        {
            boolean finishedAllSlots = true;
            
            for (int slotNumberIndex = 0; slotNumberIndex < slots.length; slotNumberIndex ++)
            {
                if (finishedSlots[slotNumberIndex]) continue;
                
                int slotNumber = slots[slotNumberIndex];
                
                if (0 <= slotNumber && slotNumber < container.getContainerSize())
                {
                    ItemStack itemStack = container.getItem(slotNumber);
                    if (itemStack.isEmpty())
                    {
                        finishedAllSlots = false;
                        continue;
                    }
                    parser.tryParse(itemStack, slotNumber, scathaPro);
                }
                else ScathaPro.LOGGER.warn(ContainerScreenParsingManager.getLogMsg("Slot index " + slotNumber + " out of bounds for screen \"" + parser.getScreenTitle() + "\""));
                finishedSlots[slotNumberIndex] = true;
            }
            
            return finishedAllSlots;
        }
    }
}
