package slimeknights.tconstruct.shared;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.registration.CreativeTabDeferredRegister;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.component.SearedTankBlock;
import slimeknights.tconstruct.smeltery.item.TankItem;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tools.TinkerToolParts;
import slimeknights.tconstruct.tools.TinkerTools;
import slimeknights.tconstruct.tools.stats.HeadMaterialStats;
import slimeknights.tconstruct.world.TinkerWorld;

/** Contains objects for all creative tabs registered by Tinkers. */
@SuppressWarnings("unused")
public class TinkerCreativeTabs {
  private static final CreativeTabDeferredRegister CREATIVE_TABS = new CreativeTabDeferredRegister(TConstruct.MOD_ID);

  /** Called during construction to initialize the registers for this mod */
  public static void register(IEventBus bus) {
    CREATIVE_TABS.register(bus);
  }

  /** Creative tab for general items, or those that lack another tab */
  public static final DeferredHolder<CreativeModeTab,CreativeModeTab> general = CREATIVE_TABS.registerTab(
    "general", builder -> builder.icon(TinkerCommons.materialsAndYou::toStack).displayItems(TinkerCommons::addTabItems));

  /** Creative tab for complete tools */
  public static final DeferredHolder<CreativeModeTab,CreativeModeTab> tabTools = CREATIVE_TABS.registerTab(
    "tools", builder -> builder
      .icon(() -> TinkerTools.pickaxe.get().getRenderTool())
      .displayItems(TinkerTools::addTabItems)
      .withTabsBefore(general.getId())
      .withSearchBar());

  /** Tab for all tool parts or tool components with many variants */
  public static final DeferredHolder<CreativeModeTab,CreativeModeTab> toolParts = CREATIVE_TABS.registerTab(
    "tool_parts", builder -> builder
      .icon(() -> {
        MaterialVariantId material;
        if (MaterialRegistry.isFullyLoaded()) {
          material = ToolBuildHandler.RANDOM.getMaterial(HeadMaterialStats.ID, RandomSource.create());
        } else {
          material = ToolBuildHandler.getRenderMaterial(0);
        }
        return TinkerToolParts.pickHead.get().withMaterialForDisplay(material);
      })
      .displayItems(TinkerToolParts::addTabItems)
      .withTabsBefore(tabTools.getId())
      .withSearchBar());

  /** Creative tab for smeltery, all contents related to the multiblocks */
  public static final DeferredHolder<CreativeModeTab,CreativeModeTab> smeltery = CREATIVE_TABS.registerTab(
    "smeltery", builder -> builder
      .icon(() -> new ItemStack(TinkerSmeltery.smelteryController))
      .displayItems(TinkerSmeltery::addTabItems)
      .withTabsBefore(toolParts.getId()));

  /** Creative tab for anything that is naturally found in the world */
  public static final DeferredHolder<CreativeModeTab,CreativeModeTab> world = CREATIVE_TABS.registerTab(
    "world", builder -> builder
      .icon(TinkerWorld.cobaltOre::toStack)
      .displayItems(TinkerWorld::addTabItems)
      .withTabsBefore(smeltery.getId()));

  /** Creative tab for general items, or those that lack another tab */
  public static final DeferredHolder<CreativeModeTab,CreativeModeTab> tables = CREATIVE_TABS.registerTab(
    "tables", builder -> builder
      .icon(TinkerTables.tinkersAnvil::toStack)
      .displayItems(TinkerTables::addTableVariants)
      .withTabsBefore(world.getId())
      .withSearchBar());

  /** Creative tab for general items, or those that lack another tab */
  public static final DeferredHolder<CreativeModeTab,CreativeModeTab> fluids = CREATIVE_TABS.registerTab(
    "fluids",builder -> builder
      .icon(() -> TankItem.fillTank(TinkerSmeltery.searedTank, SearedTankBlock.TankType.FUEL_GAUGE, TinkerFluids.moltenCobalt.get()))
      .displayItems(TinkerFluids::addFilledContainers)
      .withTabsBefore(tables.getId())
      .withSearchBar());
}
