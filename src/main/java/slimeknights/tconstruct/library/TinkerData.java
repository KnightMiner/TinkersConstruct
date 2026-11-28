package slimeknights.tconstruct.library;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.network.codec.MantleStreamCodecs;
import slimeknights.mantle.registration.deferred.AttachmentTypeDeferredRegister;
import slimeknights.mantle.registration.deferred.DataComponentTypeDeferredRegister;
import slimeknights.mantle.registration.holder.DeferredAttachment;
import slimeknights.mantle.registration.holder.DeferredDataComponent;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.gadgets.item.PiggybackHandler;
import slimeknights.tconstruct.library.fluid.item.FluidComponents;
import slimeknights.tconstruct.library.fluid.item.SimpleFluidContent;
import slimeknights.tconstruct.library.materials.MaterialVariant;
import slimeknights.tconstruct.library.modifiers.ModifierHolder;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.recipe.modifiers.adding.variant.SwappableString;
import slimeknights.tconstruct.library.recipe.worktable.ModifierSetWorktableRecipe;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.component.ModifierEntries;
import slimeknights.tconstruct.library.tools.component.ModifierSlots;
import slimeknights.tconstruct.library.tools.component.ResourceMap;
import slimeknights.tconstruct.library.tools.component.SetComponent;
import slimeknights.tconstruct.library.tools.component.SimpleItemContent;
import slimeknights.tconstruct.library.tools.component.StatMultipliers;
import slimeknights.tconstruct.library.tools.component.StatValues;
import slimeknights.tconstruct.library.tools.component.ToolMaterials;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.stack.ToolStack;

/** Handles registration of data components and data attachments added by Tinkers. */
public class TinkerData {
  private TinkerData() {}

  private static final DataComponentTypeDeferredRegister DATA_COMPONENTS = new DataComponentTypeDeferredRegister(Registries.DATA_COMPONENT_TYPE, TConstruct.MOD_ID);
  private static final AttachmentTypeDeferredRegister ATTACHMENTS = new AttachmentTypeDeferredRegister(TConstruct.MOD_ID);

  @Internal
  public static void register(IEventBus bus) {
    DATA_COMPONENTS.register(bus);
    ATTACHMENTS.register(bus);
    bus.addListener(FMLCommonSetupEvent.class, TinkerData::commonSetup);
  }

  /** Handles modifiers storing extra tool data on entities. */
  public static final DeferredAttachment<ModDataNBT> PERSISTENT_DATA = ATTACHMENTS.registerType("persistent_data", () -> AttachmentType.builder(ModDataNBT::new).serialize(ModDataNBT.CODEC).sync(ModDataNBT.STREAM_CODEC).copyOnDeath());
  /** Handles syncing the entity list for piggybackpacks */
  public static final DeferredAttachment<PiggybackHandler> PIGGYBACK_HANDLER = ATTACHMENTS.registerType("piggyback_handler", () -> AttachmentType.builder(PiggybackHandler::new));
  /** Stores the current potion on a projectile, typically from arrows. Mirrors {@link DataComponents#POTION_CONTENTS} */
  public static final DeferredAttachment<PotionContents> PROJECTILE_POTION = ATTACHMENTS.registerType("projectile_potion", () -> AttachmentType.builder(() -> PotionContents.EMPTY).serialize(PotionContents.CODEC).sync(PotionContents.STREAM_CODEC));


  /* Tank */

  /** Stores a fluid and its components without amount. Used in {@link slimeknights.tconstruct.smeltery.item.CopperCanItem} */
  public static final DeferredDataComponent<FluidComponents> FLUID = DATA_COMPONENTS.registerPersistent("fluid", FluidComponents.CODEC, FluidComponents.STREAM_CODEC);
  /** Data component for standard fluid on an item with amount. Stored directly as a fluid stack. See {@link slimeknights.tconstruct.library.tools.capability.fluid.ToolTankHelper} for some utilities. */
  public static final DeferredDataComponent<SimpleFluidContent> TANK = DATA_COMPONENTS.registerPersistent("tank", SimpleFluidContent.CODEC, SimpleFluidContent.STREAM_CODEC);


  /* Parts and modifiers */
  /** Holds a single modifier on an item. Used for modifier crystals */
  public static final DeferredDataComponent<ModifierHolder> MODIFIER = DATA_COMPONENTS.registerPersistent("modifier", ModifierHolder.CODEC, ModifierHolder.STREAM_CODEC);
  /** Holds a single material on an item. Used for tool parts and other material items */
  public static final DeferredDataComponent<MaterialVariant> MATERIAL = DATA_COMPONENTS.registerPersistent("material", MaterialVariant.CODEC, MaterialVariant.STREAM_CODEC);
  /** Holds a single slot type on an item. Used for the creative slot item */
  public static final DeferredDataComponent<SlotType> SLOT = DATA_COMPONENTS.registerPersistent("modifier_slot", SlotType.CODEC, SlotType.STREAM_CODEC);
  /** Holds a string variant. Used by {@link slimeknights.tconstruct.tools.item.CrystalshotItem} for the texture variants. */
  public static final DeferredDataComponent<String> VARIANT = DATA_COMPONENTS.registerPersistent("variant", ExtraCodecs.NON_EMPTY_STRING, ByteBufCodecs.STRING_UTF8);

  /** Marker for tools and tool parts to indicate they are for display. Reduces the amount of data in the tooltip related to materials, which are likely render materials. */
  public static final DeferredDataComponent<Unit> DISPLAY = DATA_COMPONENTS.registerUnit("display");

  /* Tool */
  /** Marker used by {@link slimeknights.tconstruct.library.tools.stack.ToolStack} to indicate the tool is actively rebuilding. Suppresses some behaviors such as component verification. */
  public static final DeferredDataComponent<Unit> TOOL_REBUILDING = DATA_COMPONENTS.registerTransient("tool_rebuilding", MantleStreamCodecs.UNIT);

  /** If true, the tool is currently broken and should not perform any actions that require durability. */
  public static final DeferredDataComponent<Unit> BROKEN = DATA_COMPONENTS.registerUnit("broken");
  /** Holds a single material on an item. Used for tool parts and other material items */
  public static final DeferredDataComponent<ToolMaterials> MATERIALS = DATA_COMPONENTS.registerPersistent("materials", ToolMaterials.CODEC, ToolMaterials.STREAM_CODEC);

  /** Holds the list of modifiers from recipes, such as upgrades or abilities */
  public static final DeferredDataComponent<ModifierEntries> CRAFTED_MODIFIERS = DATA_COMPONENTS.registerPersistent("crafted_modifiers", ModifierEntries.CODEC, ModifierEntries.STREAM_CODEC);
  /** Holds the combined list of modifiers, including {@link #CRAFTED_MODIFIERS} and traits, sorted by priority. */
  public static final DeferredDataComponent<ModifierEntries> ALL_MODIFIERS = DATA_COMPONENTS.registerPersistent("all_modifiers", ModifierEntries.CODEC, ModifierEntries.STREAM_CODEC);

  /** Holds a cache of the current stats for the tool */
  public static final DeferredDataComponent<StatValues> TOOL_STATS = DATA_COMPONENTS.registerPersistent("tool_stats", StatValues.CODEC, StatValues.STREAM_CODEC);
  /** Holds a cache of stat multipliers on the tool */
  public static final DeferredDataComponent<StatMultipliers> STAT_MULTIPLIERS = DATA_COMPONENTS.registerPersistent("stats_multipliers", StatMultipliers.CODEC, StatMultipliers.STREAM_CODEC);

  /** Modifier slots on a tool from the tool or modifier data. Will be refreshed whenever modifiers rebuild. Will typically be positive. */
  public static final DeferredDataComponent<ModifierSlots> VOLATILE_SLOTS = DATA_COMPONENTS.registerPersistent("volatile_slots", ModifierSlots.CODEC, ModifierSlots.STREAM_CODEC);
  /** Modifier slots on the tool due to recipes or cheats. Will typically be negative. */
  public static final DeferredDataComponent<ModifierSlots> PERSISTENT_SLOTS = DATA_COMPONENTS.registerPersistent("persistent_slots", ModifierSlots.CODEC, ModifierSlots.STREAM_CODEC);

  /** Modifier actively being used on the tool, as set in {@link GeneralInteractionModifierHook}. */
  public static final DeferredDataComponent<ModifierId> ACTIVE_MODIFIER = DATA_COMPONENTS.registerPersistent("active_modifier", ModifierId.CODEC, ModifierId.STREAM_CODEC);
  /** Cached draw time for an item actively being used in ticks. Used by {@link slimeknights.tconstruct.library.tools.item.ranged.ModifiableLauncherItem} and {@link GeneralInteractionModifierHook} */
  public static final DeferredDataComponent<Integer> DRAWTIME = DATA_COMPONENTS.registerPersistent("drawtime", ExtraCodecs.POSITIVE_INT, ByteBufCodecs.VAR_INT);
  /** Current ammo stack in a crossbow. Size determines how many to fire. */
  public static final DeferredDataComponent<SimpleItemContent> CROSSBOW_AMMO = DATA_COMPONENTS.registerPersistent("crossbow_ammo", SimpleItemContent.CODEC, SimpleItemContent.STREAM_CODEC);
  /** Ammo stored while drawing back a bow or crossbow. May be set but empty to indicate we are drawing but are not visually displaying the ammo. */
  public static final DeferredDataComponent<SimpleItemContent> DRAWBACK_AMMO = DATA_COMPONENTS.registerPersistent("drawback_ammo", SimpleItemContent.CODEC, SimpleItemContent.STREAM_CODEC);
  /** Index on the tool that stores the material for the fishing hook. Used by {@link slimeknights.tconstruct.tools.modules.interaction.FishingModule} to set the material for the renderer. */
  public static final DeferredDataComponent<Integer> HOOK_MATERIAL = DATA_COMPONENTS.registerPersistent("hook_material", ExtraCodecs.NON_NEGATIVE_INT, ByteBufCodecs.VAR_INT);

  /* Modifiers */
  /** Set of resource locations used for various behavior flags. See {@link slimeknights.tconstruct.library.tools.component.VolatileFlags} */
  public static final DeferredDataComponent<SetComponent<ResourceLocation>> VOLATILE_FLAGS = DATA_COMPONENTS.registerPersistent("volatile_flags", SetComponent.codec(ResourceLocation.CODEC), SetComponent.streamCodec(ResourceLocation.STREAM_CODEC));
  /** Map of resource location to volatile integer. Used in place of modifier levels for some hardcoded interactions. See {@link slimeknights.tconstruct.library.tools.component.VolatileIntegers} */
  public static final DeferredDataComponent<ResourceMap<Integer>> VOLATILE_INTEGER = DATA_COMPONENTS.registerPersistent("volatile_integer", ResourceMap.codec(Codec.INT), ResourceMap.streamCodec(ByteBufCodecs.VAR_INT));

  /** Map of modifier ID to variant string. Used for crystalshot variants. */
  public static final DeferredDataComponent<ResourceMap<String>> MODIFIER_STRING = DATA_COMPONENTS.registerPersistent("modifier_string", ResourceMap.codec(SwappableString.VALID_CODEC), ResourceMap.streamCodec(ByteBufCodecs.STRING_UTF8));
  /** Map of modifier ID to {@link MaterialVariant}. Used for embellishments. */
  public static final DeferredDataComponent<ResourceMap<MaterialVariant>> MODIFIER_MATERIAL = DATA_COMPONENTS.registerPersistent("modifier_material", ResourceMap.codec(MaterialVariant.CODEC), ResourceMap.streamCodec(MaterialVariant.STREAM_CODEC));
  /** Map of modifier ID to slot type. Used for rebalanced. */
  public static final DeferredDataComponent<ResourceMap<SlotType>> MODIFIER_SLOT_TYPE = DATA_COMPONENTS.registerPersistent("modifier_slot_type", ResourceMap.codec(SlotType.CODEC), ResourceMap.streamCodec(SlotType.STREAM_CODEC));
  /** Map of modifier ID to variant integer. Used for headlight levels and dyed colors. */
  public static final DeferredDataComponent<ResourceMap<Integer>> MODIFIER_INTEGER = DATA_COMPONENTS.registerPersistent("modifier_integer", ResourceMap.codec(Codec.INT), ResourceMap.streamCodec(ByteBufCodecs.VAR_INT));

  // modifier sets
  /** Modifiers in this set will be hidden from {@link slimeknights.tconstruct.library.client.model.tools.ToolModel} */
  public static final DeferredDataComponent<SetComponent<ModifierId>> INVISIBLE_MODIFIERS = DATA_COMPONENTS.registerPersistent("invisible_modifiers", ModifierSetWorktableRecipe.MODIFIER_SET_CODEC, ModifierSetWorktableRecipe.MODIFIER_SET_STREAM_CODEC);
  /** Modifiers in this set will use right click for interactions instead of their default left click. */
  public static final DeferredDataComponent<SetComponent<ModifierId>> INTERACT_MODIFIERS = DATA_COMPONENTS.registerPersistent("interact_modifiers", ModifierSetWorktableRecipe.MODIFIER_SET_CODEC, ModifierSetWorktableRecipe.MODIFIER_SET_STREAM_CODEC);
  /** Modifiers in this set will use left click for interactions instead of their default right click. */
  public static final DeferredDataComponent<SetComponent<ModifierId>> ATTACK_MODIFIERS = DATA_COMPONENTS.registerPersistent("attack_modifiers", ModifierSetWorktableRecipe.MODIFIER_SET_CODEC, ModifierSetWorktableRecipe.MODIFIER_SET_STREAM_CODEC);

  /** Modifier slots added through the creative modifier. */
  public static final DeferredDataComponent<ModifierSlots> CREATIVE_SLOTS = DATA_COMPONENTS.registerPersistent("creative_slots", ModifierSlots.CODEC, ModifierSlots.STREAM_CODEC);
  /** Marker used by {@link slimeknights.tconstruct.library.modifiers.modules.behavior.InfinityModule} to inform bows that the ammo stack is infinite. */
  public static final DeferredDataComponent<Unit> INFINITY = DATA_COMPONENTS.registerTransient("infinity", MantleStreamCodecs.UNIT);
  /** Marker used by {@link slimeknights.tconstruct.tools.modules.interaction.sling.SlingKnockbackModule} to mark that the melee attack is from the module */
  public static final DeferredDataComponent<Unit> SLING_KNOCKBACK = DATA_COMPONENTS.registerTransient("sling_knockback", MantleStreamCodecs.UNIT);


  /** Handle any extra setup with data */
  private static void commonSetup(FMLCommonSetupEvent event) {
    // tool stack restrictions
    ToolStack.registerRestricted(DataComponents.DAMAGE);
    ToolStack.registerRestricted(
      TOOL_REBUILDING, BROKEN, MATERIALS,
      TOOL_STATS, STAT_MULTIPLIERS,
      PERSISTENT_SLOTS, VOLATILE_SLOTS, VOLATILE_FLAGS, TinkerData.VOLATILE_INTEGER,
      CRAFTED_MODIFIERS, ALL_MODIFIERS
    );
    // volatile data - these will be rebuilt, but we clear them anyway to prevent them being accessed during rebuild
    ToolStack.registerVolatile(TOOL_STATS, STAT_MULTIPLIERS, VOLATILE_SLOTS, VOLATILE_FLAGS, VOLATILE_INTEGER, ALL_MODIFIERS, HOOK_MATERIAL);
    // vanilla components - this only affects tool stacks so no harm marking it with our system
    ToolStack.registerVolatile(DataComponents.RARITY);
  }
}
