package slimeknights.tconstruct.tools;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.registration.holder.DeferredBlockEntity;
import slimeknights.tconstruct.common.TinkerModule;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.registration.object.EnumItem;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.library.tools.part.IRepairKitItem;
import slimeknights.tconstruct.library.tools.part.ToolPartItem;
import slimeknights.tconstruct.library.tools.part.block.MaterialBlock;
import slimeknights.tconstruct.library.tools.part.block.MaterialBlockEntity;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.tools.item.FakeIngotItem;
import slimeknights.tconstruct.tools.item.FakeStorageBlockItem;
import slimeknights.tconstruct.tools.item.ModifierCrystalItem;
import slimeknights.tconstruct.tools.item.RepairKitItem;
import slimeknights.tconstruct.tools.stats.GripMaterialStats;
import slimeknights.tconstruct.tools.stats.HandleMaterialStats;
import slimeknights.tconstruct.tools.stats.HeadMaterialStats;
import slimeknights.tconstruct.tools.stats.LimbMaterialStats;
import slimeknights.tconstruct.tools.stats.PlatingMaterialStats;
import slimeknights.tconstruct.tools.stats.RepairStats;
import slimeknights.tconstruct.tools.stats.StatlessMaterialStats;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class TinkerToolParts extends TinkerModule {
  // repair kits
  public static final DeferredItem<RepairKitItem> repairKit = ITEMS.register("repair_kit", () -> new RepairKitItem(ITEM_PROPS));
  /** Fake ingot tool part for the sake of compat materials that lack an ingot form. Mainly used for compat alloys such as bronze which can be activated by their components. */
  public static final DeferredItem<FakeIngotItem> fakeIngot = ITEMS.register("fake_ingot", () -> new FakeIngotItem(ITEM_PROPS, 1, TinkerTags.Materials.COMPATABILITY_METALS));
  /** Fake block tool part for the sake of compat materials that lack an ingot form. Mainly used for compat alloys such as bronze which can be activated by their components. */
  public static final DeferredBlock<MaterialBlock> fakeStorageBlock = BLOCKS.register("fake_storage_block", () -> new MaterialBlock(metalBuilder(MapColor.COLOR_GRAY), MaterialBlockEntity::new), block -> new FakeStorageBlockItem(block, ITEM_PROPS, 9, TinkerTags.Materials.COMPATABILITY_BLOCKS));
  /** Same as {@link #fakeStorageBlock} but casted to an appropriate item interface form */
  public static final Supplier<IRepairKitItem> fakeStorageBlockItem = () -> (IRepairKitItem) fakeStorageBlock.asItem();

  // rock
  public static final DeferredItem<ToolPartItem> pickHead = ITEMS.register("pick_head", () -> new ToolPartItem(ITEM_PROPS, HeadMaterialStats.ID));
  public static final DeferredItem<ToolPartItem> hammerHead = ITEMS.register("hammer_head", () -> new ToolPartItem(ITEM_PROPS, HeadMaterialStats.ID));
  // axe
  public static final DeferredItem<ToolPartItem> smallAxeHead = ITEMS.register("small_axe_head", () -> new ToolPartItem(ITEM_PROPS, HeadMaterialStats.ID));
  public static final DeferredItem<ToolPartItem> broadAxeHead = ITEMS.register("broad_axe_head", () -> new ToolPartItem(ITEM_PROPS, HeadMaterialStats.ID));
  // blades
  public static final DeferredItem<ToolPartItem> smallBlade = ITEMS.register("small_blade", () -> new ToolPartItem(ITEM_PROPS, HeadMaterialStats.ID));
  public static final DeferredItem<ToolPartItem> broadBlade = ITEMS.register("broad_blade", () -> new ToolPartItem(ITEM_PROPS, HeadMaterialStats.ID));
  // plates
  public static final DeferredItem<ToolPartItem> adzeHead = ITEMS.register("adze_head", () -> new ToolPartItem(ITEM_PROPS, HeadMaterialStats.ID));
  public static final DeferredItem<ToolPartItem> largePlate = ITEMS.register("large_plate", () -> new ToolPartItem(ITEM_PROPS, HeadMaterialStats.ID));
  // bows
  public static final DeferredItem<ToolPartItem> bowLimb = ITEMS.register("bow_limb", () -> new ToolPartItem(ITEM_PROPS, LimbMaterialStats.ID));
  public static final DeferredItem<ToolPartItem> bowGrip = ITEMS.register("bow_grip", () -> new ToolPartItem(ITEM_PROPS, GripMaterialStats.ID));
  public static final DeferredItem<ToolPartItem> bowstring = ITEMS.register("bowstring", () -> new ToolPartItem(ITEM_PROPS, StatlessMaterialStats.BOWSTRING.getIdentifier()));
  // ammo
  public static final DeferredItem<ToolPartItem> arrowHead = ITEMS.register("arrow_head", () -> new ToolPartItem(ITEM_PROPS, StatlessMaterialStats.ARROW_HEAD.getIdentifier()));
  public static final DeferredItem<ToolPartItem> arrowShaft = ITEMS.register("arrow_shaft", () -> new ToolPartItem(ITEM_PROPS, StatlessMaterialStats.ARROW_SHAFT.getIdentifier()));
  public static final DeferredItem<ToolPartItem> fletching = ITEMS.register("fletching", () -> new ToolPartItem(ITEM_PROPS, StatlessMaterialStats.FLETCHING.getIdentifier()));
  // other parts
  public static final DeferredItem<ToolPartItem> toolBinding = ITEMS.register("tool_binding", () -> new ToolPartItem(ITEM_PROPS, StatlessMaterialStats.BINDING.getIdentifier()));
  public static final DeferredItem<ToolPartItem> toughBinding = ITEMS.register("tough_binding", () -> new ToolPartItem(ITEM_PROPS, StatlessMaterialStats.BINDING.getIdentifier()));
  public static final DeferredItem<ToolPartItem> toolHandle = ITEMS.register("tool_handle", () -> new ToolPartItem(ITEM_PROPS, HandleMaterialStats.ID));
  public static final DeferredItem<ToolPartItem> toughHandle = ITEMS.register("tough_handle", () -> new ToolPartItem(ITEM_PROPS, HandleMaterialStats.ID));
  // armor
  public static final EnumItem<ArmorItem.Type,ToolPartItem> plating = ITEMS.registerEnumItem(ArmorItem.Type.values(), "plating", type -> new ToolPartItem(ITEM_PROPS, PlatingMaterialStats.TYPES.get(type.ordinal()).getId()));
  public static final DeferredItem<ToolPartItem> maille = ITEMS.register("maille", () -> new ToolPartItem(ITEM_PROPS, StatlessMaterialStats.MAILLE.getIdentifier()));
  public static final DeferredItem<ToolPartItem> shieldCore = ITEMS.register("shield_core", () -> new ToolPartItem(ITEM_PROPS, StatlessMaterialStats.SHIELD_CORE.getIdentifier()));
  // slimesuit
  public static final DeferredItem<ToolPartItem> ribcage = ITEMS.register("ribcage", () -> new ToolPartItem(ITEM_PROPS, RepairStats.RIBCAGE.getId()));
  public static final DeferredItem<ToolPartItem> shell = ITEMS.register("shell", () -> new ToolPartItem(ITEM_PROPS, RepairStats.SHELL.getId()));
  public static final DeferredItem<ToolPartItem> laces = ITEMS.register("laces", () -> new ToolPartItem(ITEM_PROPS, RepairStats.LACES.getId()));

  // block entities
  public static final DeferredBlockEntity<MaterialBlockEntity> materialBlock = BLOCK_ENTITIES.register("material_block", MaterialBlockEntity::new, fakeStorageBlock);

  /** Adds all relevant items to the creative tab */
  @SuppressWarnings("unused")
  @Internal
  public static void addTabItems(ItemDisplayParameters itemDisplayParameters, CreativeModeTab.Output tab) {
    Consumer<ItemStack> output = tab::accept;
    accept(output, repairKit);
    // fake ingot is in materials tab, helps the illusion
    // small heads
    accept(output, pickHead);
    accept(output, smallAxeHead);
    accept(output, smallBlade);
    accept(output, adzeHead);
    // large heads
    accept(output, hammerHead);
    accept(output, broadAxeHead);
    accept(output, broadBlade);
    accept(output, largePlate);
    // binding and rods
    accept(output, toolHandle);
    accept(output, toolBinding);
    accept(output, toughHandle);
    accept(output, toughBinding);
    // ranged
    accept(output, bowLimb);
    accept(output, bowGrip);
    accept(output, bowstring);
    accept(output, arrowHead);
    accept(output, arrowShaft);
    accept(output, fletching);
    // plating, pair each one with the dummy plating item
    for (ArmorItem.Type type : ArmorItem.Type.values()) {
      tab.accept(TinkerSmeltery.dummyPlating.get(type));
      plating.get(type).addVariants(output, "");
    }
    accept(output, maille);
    accept(output, shieldCore);
    // slimesuit
    accept(output, ribcage);
    accept(output, shell);
    accept(output, laces);

    // end with modifier crystal dynamic listing
    ModifierCrystalItem.addVariants(output);
  }

  /** Adds a tool part to the tab */
  private static void accept(Consumer<ItemStack> output, Supplier<? extends IMaterialItem> item) {
    item.get().addVariants(output, "");
  }
}
