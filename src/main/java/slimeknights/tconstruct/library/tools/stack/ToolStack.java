package slimeknights.tconstruct.library.tools.stack;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.MaterialVariant;
import slimeknights.tconstruct.library.materials.MaterialVariantId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.ModifierManager;
import slimeknights.tconstruct.library.modifiers.hook.build.ModifierTraitHook.TraitBuilder;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.SlotType.SlotCount;
import slimeknights.tconstruct.library.tools.component.CollectionComponent;
import slimeknights.tconstruct.library.tools.component.ModifierEntries;
import slimeknights.tconstruct.library.tools.component.ModifierSlots;
import slimeknights.tconstruct.library.tools.component.StatMultipliers;
import slimeknights.tconstruct.library.tools.component.StatValues;
import slimeknights.tconstruct.library.tools.component.ToolMaterials;
import slimeknights.tconstruct.library.tools.context.ToolRebuildContext;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.definition.ToolDefinitionData;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.definition.module.material.MissingMaterialsToolHook;
import slimeknights.tconstruct.library.tools.helper.TooltipUtil;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class handling parsing all tool related NBT
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ToolStack implements ToolStackView {
  /** Error messages for when there are not enough remaining modifiers */
  private static final String KEY_VALIDATE_SLOTS = TConstruct.makeTranslationKey("recipe", "modifier.validate_slots");

  /** Any data components in this set cannot be directly modified using {@link #set(DataComponentType, Object)}. */
  private static final Set<DataComponentType<?>> RESTRICTED_COMPONENTS = new HashSet<>();
  /** Any data components in this set will be cleared on stat rebuild and should only be modified during {@link slimeknights.tconstruct.library.modifiers.hook.build.VolatileDataModifierHook}. */
  private static final Set<DataComponentType<?>> VOLATILE_COMPONENTS = new HashSet<>();

  /** Registers a restricted data component. Meant to be called during {@link FMLCommonSetupEvent} */
  public static synchronized void registerRestricted(DataComponentType<?>... components) {
    Collections.addAll(RESTRICTED_COMPONENTS, components);
  }

  /** Registers a restricted data component. Meant to be called during {@link FMLCommonSetupEvent} */
  @SafeVarargs
  public static synchronized void registerRestricted(Holder<DataComponentType<?>>... components) {
    for (Holder<DataComponentType<?>> component : components) {
      RESTRICTED_COMPONENTS.add(component.value());
    }
  }

  /** Registers a volatile data component, automatically marked as restricted as well. Meant to be called during {@link FMLCommonSetupEvent} */
  public synchronized static void registerVolatile(DataComponentType<?>... components) {
    Collections.addAll(VOLATILE_COMPONENTS, components);
  }

  /** Registers a volatile data component, automatically marked as restricted as well. Meant to be called during {@link FMLCommonSetupEvent} */
  @SafeVarargs
  public static synchronized void registerVolatile(Holder<DataComponentType<?>>... components) {
    for (Holder<DataComponentType<?>> component : components) {
      VOLATILE_COMPONENTS.add(component.value());
    }
  }

  /** Checks if the component is a volatile type */
  public static boolean isVolatile(DataComponentType<?> component) {
    return VOLATILE_COMPONENTS.contains(component);
  }

  /** Checks if the component is a restricted type */
  public static boolean isRestricted(DataComponentType<?> component) {
    return RESTRICTED_COMPONENTS.contains(component);
  }

  /** Checks if the component is a volatile or restricted component type */
  private static boolean isDisallowed(DataComponentType<?> component) {
    return VOLATILE_COMPONENTS.contains(component) || RESTRICTED_COMPONENTS.contains(component);
  }

  /** Tag for extra arbitrary modifier data */
  @Deprecated(forRemoval = true)
  public static final String TAG_PERSISTENT_MOD_DATA = "tic_persistent";
  /** Tag for arbitrary modifier data rebuilt on stat rebuild */
  @Deprecated(forRemoval = true)
  public static final String TAG_VOLATILE_MOD_DATA = "tic_volatile_data";


  /** Internal item stack representation. Handles data components and the item form. */
  @Getter
  private final ItemStack stack;
  /** Tool definition from {@link IModifiable} */
  @Getter
  private final ToolDefinition definition;


  /* Constructors */

  /**
   * Creates a new tool stack from the given item and tool definition
   * @param item        Modifiable item instance
   * @param definition  Tool definition
   * @param count       Stack count
   * @return  Tool stack instance
   */
  public static ToolStack create(ItemLike item, ToolDefinition definition, int count) {
    return new ToolStack(new ItemStack(item, count), definition);
  }

  /**
   * Creates a new tool stack from the given item and tool definition
   * @param item        Modifiable item instance
   * @param definition  Tool definition
   * @return  Tool stack instance
   */
  public static ToolStack create(ItemLike item, ToolDefinition definition) {
    return create(item, definition, 1);
  }


  /**
   * Creates a new tool stack from the given item
   * @param item   Modifiable item instance
   * @param count  Stack count
   * @return  Tool stack instance
   */
  public static ToolStack create(IModifiable item, int count) {
    return create(item, item.getToolDefinition(), count);
  }

  /**
   * Creates a new tool stack from the given item
   * @param item  Modifiable item instance
   * @return  Tool stack instance
   */
  public static ToolStack create(IModifiable item) {
    return create(item, 1);
  }


  /* Stack view */

  /**
   * Creates a tool stack from the given item stack, sharing the component map
   * @param stack  Stack
   * @return  Tool stack
   */
  public static ToolStack from(ItemStack stack) {
    return new ToolStack(stack, IModifiable.getToolDefinition(stack.getItem()));
  }

  /**
   * Creates a tool stack from the given item stack, copying the component map
   * @param stack  Stack
   * @return  Tool stack
   */
  public static ToolStack copyFrom(ItemStack stack) {
    return from(stack.copy());
  }

  /**
   * Creates a tool stack from the given item stack, copying the component map with the given stack size
   * @param stack  Stack
   * @param count  New count
   * @return  Tool stack
   */
  public static ToolStack copyWithCount(ItemStack stack, int count) {
    return from(stack.copyWithCount(count));
  }

  /**
   * Creates a tool stack from the given item stack, copying the component map with the given max stack size. Used in many recipes.
   * @param stack  Stack
   * @param max    Max count. Count will be the smaller of current or max.
   * @return  Tool stack
   */
  public static ToolStack copyMaxCount(ItemStack stack, int max) {
    return from(stack.copyWithCount(Math.min(stack.getCount(), max)));
  }


  /* Copying */

  /** Creates a copy of this tool. */
  public ToolStack copy() {
    return new ToolStack(stack.copy(), definition);
  }

  /** Creates a copy of this tool with the given count */
  public ToolStack copyWithCount(int count) {
    return new ToolStack(stack.copyWithCount(count), definition);
  }

  /** Creates a copy of this tool with the given count */
  public ToolStack copyMaxCount(int max) {
    return new ToolStack(stack.copyWithCount(Math.min(stack.getCount(), max)), definition);
  }


  /* Stack properties */

  @Override
  public Item getItem() {
    return stack.getItem();
  }

  @Override
  public boolean isSameStack(ItemStack stack) {
    return this.stack == stack;
  }

  /** Gets the count of the underlying stack */
  public int getCount() {
    return stack.getCount();
  }

  /** Sets the count on the underlying stack */
  public void setCount(int count) {
    stack.setCount(count);
  }


  /* Components */

  @Override
  public DataComponentMap getComponents() {
    return stack.getComponents();
  }

  @Override
  public void applyComponents(DataComponentPatch patch) {
    stack.applyComponents(patch.forget(ToolStack::isDisallowed));
  }

  @Override
  public void applyComponents(DataComponentMap components) {
    stack.applyComponents(components.filter(type -> !isDisallowed(type)));
  }

  /** Validates that the given component can be set. Returns true and logs a message if disallowed */
  private boolean disallowedComponent(DataComponentType<?> componentType) {
    if (VOLATILE_COMPONENTS.contains(componentType)) {
      Config.COMMON.logBadToolStackComponents.log(() -> "Attempting to set volatile component " + componentType + " on a tool stack. Use the volatile data hook.");
      return true;
    }
    if (RESTRICTED_COMPONENTS.contains(componentType)) {
      Config.COMMON.logBadToolStackComponents.log(() -> "Attempting to set restricted component " + componentType + " on a tool stack. This is either forbidden in modifier hooks or has a dedicated setter.");
      return true;
    }
    return false;
  }

  @Nullable
  @Override
  public <T> T set(DataComponentType<? super T> componentType, @Nullable T value) {
    return disallowedComponent(componentType) ? null : stack.set(componentType, value);
  }

  @Nullable
  @Override
  public <T> T remove(DataComponentType<? extends T> componentType) {
    return disallowedComponent(componentType) ? null : stack.remove(componentType);
  }


  /* Durability */

  /**
   * Checks if this tool is currently broken
   * @return  True if broken
   */
  @Override
  public boolean isBroken() {
    return stack.has(TinkerData.BROKEN);
  }

  @Override
  public boolean isUnbreakable() {
    return stack.has(DataComponents.UNBREAKABLE);
  }

  /**
   * Sets the broken state on the tool
   * @param broken  New broken value
   */
  protected void setBrokenRaw(boolean broken) {
    if (broken) {
      stack.set(TinkerData.BROKEN, Unit.INSTANCE);
    } else {
      stack.remove(TinkerData.BROKEN);
    }
  }

  /**
   * Breaks the tool
   */
  protected void breakTool() {
    setDamage(getStats().getInt(ToolStats.DURABILITY));
  }

  /**
   * Gets damage, ignoring broken checks
   * @return  Damage ignoring broken state
   */
  protected int getDamageRaw() {
    return stack.getOrDefault(DataComponents.DAMAGE, 0);
  }

  @Override
  public int getDamage() {
    // if broken, return full damage
    int durability = getStats().getInt(ToolStats.DURABILITY);
    if (isBroken()) {
      return durability;
    }
    // ensure we never return a number larger than max
    return Math.min(getDamageRaw(), durability - 1);
  }

  @Override
  public int getCurrentDurability() {
    if (isBroken()) {
      return 0;
    }
    // ensure we never return a number smaller than 0
    return Math.max(0, getStats().getInt(ToolStats.DURABILITY) - getDamageRaw());
  }

  @Override
  public void setDamage(int damage) {
    int durability = getStats().getInt(ToolStats.DURABILITY);
    if (damage >= durability) {
      damage = Math.max(0, durability);
      setBrokenRaw(true);
    } else {
      setBrokenRaw(false);
    }
    stack.set(DataComponents.DAMAGE, damage);
  }


  /* Stats */

  @Override
  public StatValues getStats() {
    return stack.getOrDefault(TinkerData.TOOL_STATS, StatValues.EMPTY);
  }

  /**
   * Sets the tool stats, and stores it in NBT
   * @param stats  Stats instance
   */
  protected void setStats(StatValues stats) {
    stack.set(TinkerData.TOOL_STATS, stats);
    // if we no longer have enough durability, decrease the damage and mark it broken
    int newMax = stats.getInt(ToolStats.DURABILITY);
    if (getDamageRaw() >= newMax) {
      setDamage(newMax);
    }
  }

  @Override
  public StatMultipliers getMultipliers() {
    return stack.getOrDefault(TinkerData.STAT_MULTIPLIERS, StatMultipliers.EMPTY);
  }

  /**
   * Sets the tool multipliers, and stores it in NBT
   * @param multipliers  Stats instance
   */
  protected void setMultipliers(StatMultipliers multipliers) {
    CollectionComponent.set(stack, TinkerData.STAT_MULTIPLIERS, multipliers);
  }


  /* Materials */

  @Override
  public ToolMaterials getMaterials() {
    if (!definition.hasMaterials()) {
      return ToolMaterials.EMPTY;
    }
    return stack.getOrDefault(TinkerData.MATERIALS, ToolMaterials.EMPTY);
  }

  /**
   * Sets the materials without updating the tool stats
   * @param materials  New materials
   */
  protected void setMaterialsRaw(ToolMaterials materials) {
    CollectionComponent.set(stack, TinkerData.MATERIALS, materials);
  }

  /**
   * Sets the materials on this tool stack, updating tool stats
   * @param materials  New materials NBT
   */
  public void setMaterials(ToolMaterials materials) {
    setMaterialsRaw(materials);
    rebuildStats();
  }

  /**
   * Replaces the material at the given index
   * @param index        Index to replace
   * @param replacement  New material
   * @throws IndexOutOfBoundsException  If the index is invalid
   */
  public void replaceMaterial(int index, MaterialVariant replacement) {
    setMaterials(getMaterials().replaceMaterial(index, replacement));
  }

  /**
   * Replaces the material at the given index
   * @param index        Index to replace
   * @param replacement  New material
   * @throws IndexOutOfBoundsException  If the index is invalid
   */
  public void replaceMaterial(int index, MaterialVariantId replacement) {
    setMaterials(getMaterials().replaceMaterial(index, replacement));
  }


  /* Modifiers */

  @Override
  public ModifierEntries getCraftedModifiers() {
    return stack.getOrDefault(TinkerData.CRAFTED_MODIFIERS, ModifierEntries.EMPTY);
  }

  /**
   * Updates the upgrades list on the tool
   * @param modifiers  New upgrades
   */
  public void setCraftedModifiers(ModifierEntries modifiers) {
    CollectionComponent.set(stack, TinkerData.CRAFTED_MODIFIERS, modifiers);
    rebuildStats();
  }

  /**
   * Adds a single modifier to this tool
   * @param modifier  Modifier to add
   * @param level     Level to add
   */
  public void addModifier(ModifierId modifier, int level) {
    if (level <= 0) {
      throw new IllegalArgumentException("Invalid level, must be above 0");
    }
    setCraftedModifiers(getCraftedModifiers().withModifier(modifier, level));
  }

  /**
   * Adds a single modifier to this tool
   * @param modifier  Modifier to add
   * @param amount    Amount to add
   * @param needed    Amount needed for a full level
   */
  public void addModifierAmount(ModifierId modifier, int amount, int needed) {
    if (needed <= 0) {
      throw new IllegalArgumentException("Invalid needed, must be above 0");
    }
    if (amount > 0) {
      setCraftedModifiers(getCraftedModifiers().addAmount(modifier, amount, needed));
    }
  }

  /**
   * Removes a single modifier to this tool
   * @param modifier  Modifier to remove
   * @param level     Level to remove
   */
  public void removeModifier(ModifierId modifier, int level) {
    if (level <= 0) {
      throw new IllegalArgumentException("Invalid level, must be above 0");
    }
    setCraftedModifiers(getCraftedModifiers().withoutModifier(modifier, level));
  }

  @Override
  public ModifierEntries getModifiers() {
    return stack.getOrDefault(TinkerData.ALL_MODIFIERS, ModifierEntries.EMPTY);
  }

  /**
   * Updates the list of all modifiers in NBT, called in {@link #rebuildStats()}
   * @param modifiers  New modifiers
   */
  protected void setModifiers(ModifierEntries modifiers) {
    CollectionComponent.set(stack, TinkerData.ALL_MODIFIERS, modifiers);
  }


  /* Slots */

  /** Sets the volatile slot data */
  public void setPersistentSlots(ModifierSlots slots) {
    CollectionComponent.set(stack, TinkerData.PERSISTENT_SLOTS, slots);
  }

  /** Removes the given slots from the tool */
  public void removeSlots(SlotCount slots) {
    setPersistentSlots(getPersistentSlots().without(slots));
  }

  @Override
  public ModifierSlots getPersistentSlots() {
    return stack.getOrDefault(TinkerData.PERSISTENT_SLOTS, ModifierSlots.EMPTY);
  }

  @Override
  public ModifierSlots getVolatileSlots() {
    return stack.getOrDefault(TinkerData.VOLATILE_SLOTS, ModifierSlots.EMPTY);
  }


  /* Utilities */

  @Nullable
  public Component tryValidate() {
    // first check slot counts
    for (SlotType slotType : SlotType.getAllSlotTypes()) {
      if (getFreeSlots(slotType) < 0) {
        return Component.translatable(KEY_VALIDATE_SLOTS, slotType.getLowercaseName());
      }
    }
    // next, ensure modifiers validate
    Component result;
    for (ModifierEntry entry : getModifiers()) {
      result = entry.getHook(ModifierHooks.VALIDATE).validate(this, entry);
      if (result != null) {
        return result;
      }
    }
    // some validations should only run if the modifier was crafted on the tool
    for (ModifierEntry entry : getCraftedModifiers()) {
      result = entry.getHook(ModifierHooks.VALIDATE_CRAFTED).validate(this, entry);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  /** Called on inventory tick to ensure the tool has all required data including materials and starting slots, prevents tools with no stats from existing */
  public void ensureHasData() {
    // if we try initializing before datapacks load we will get garbage data
    if (definition.isDataLoaded()) {
      // check if missing materials; either means we have none or too few
      MissingMaterialsToolHook missingMaterials = definition.getHook(ToolHooks.MISSING_MATERIALS);
      boolean needsMaterials = definition.hasMaterials() && (!stack.has(TinkerData.MATERIALS) || missingMaterials.needsMaterials(definition, getMaterials()));
      // build data if we either lack data (signified by no stats) or we lack materials but expect them
      if (needsMaterials || !isInitialized(stack)) {
        // randomize materials if missing
        if (needsMaterials) {
          setMaterialsRaw(missingMaterials.fillMaterials(definition, getMaterials(), RandomSource.create()));
        }
        rebuildStats();
      }
    }
  }

  /**
   * Recalculates any relevant cached data. Called after either the materials or modifiers list changes
   */
  public void rebuildStats() {
    // quick safety checks: to rebuild stats we need
    // * tool definition (contains stats and traits)
    // * material registry (to fetch material stats and traits)
    // * modifier registry (run relevant modifier hooks)
    // * item tags (control tool behaviors in various places)
    // if any of these are missing, attempting to rebuild stats may corrupt the tool's state (persistent data, damage, broken)
    if (!definition.isDataLoaded() || !MaterialRegistry.isFullyLoaded() || !ModifierManager.INSTANCE.isDynamicModifiersLoaded() || !TinkerTags.isTagsLoaded()) {
      return;
    }

    // clear out volatile data
    for (DataComponentType<?> type : VOLATILE_COMPONENTS) {
      stack.remove(type);
    }

    // first, determine the list of modifiers, this is done in a couple stages
    // we start by cloning upgrades and adding tool traits and material traits
    ToolMaterials materials = getMaterials();
    ModifierEntries.Builder modBuilder = ModifierEntries.builder();
    ModifierEntries craftedModifiers = getCraftedModifiers();
    modBuilder.add(craftedModifiers);
    ToolDefinitionData toolData = getDefinitionData();
    toolData.getHook(ToolHooks.TOOL_TRAITS).addTraits(definition, materials, modBuilder);
    ModifierEntries beforeTraits = modBuilder.build();

    // temporary context while we add modifier traits, will recreate if we have modifiers
    // clear out volatile data, mostly affects the volatile data hook
    ToolRebuildContext context = new ToolRebuildContext(stack, definition, materials, craftedModifiers, beforeTraits);

    // if we have modifiers, apply modifier traits, saves creating some builders if empty
    List<ModifierEntry> modifierList = Collections.emptyList();
    if (beforeTraits.isEmpty()) {
      // if no modifiers, just clear modifiers
      setModifiers(ModifierEntries.EMPTY);
    } else {
      modBuilder = ModifierEntries.builder();
      TraitBuilder traitBuilder = new TraitBuilder(context, modBuilder);
      traitBuilder.add(beforeTraits);

      // set the final modifier list on the tool
      ModifierEntries allMods = modBuilder.build();
      setModifiers(allMods);
      modifierList = allMods.modifiers();
      // context for further modifier hooks
      context = context.withModifiers(allMods);
    }

    // build volatile data first, it's a parameter to the other hooks
    VolatileBuilder volatileBuilder = new VolatileBuilder();
    // TODO: is the restricted component map necessary?
    MutableDataComponentHolder volatileData = RestrictedComponentMap.volatileData(stack);
    toolData.getHook(ToolHooks.VOLATILE_DATA).addVolatileData(context, volatileBuilder, volatileData);
    for (ModifierEntry entry : modifierList) {
      entry.getHook(ModifierHooks.VOLATILE_DATA).addVolatileData(context, entry, volatileBuilder, volatileData);
    }
    // adding to the stack directly as volatile is forbidden from the tool
    // it will error internally if a non-volatile flag is modified
    volatileBuilder.addToTool(stack);

    // regular stats last so we can include volatile data
    ModifierStatsBuilder statBuilder = ModifierStatsBuilder.builder();
    toolData.getHook(ToolHooks.TOOL_STATS).addToolStats(context, statBuilder);
    for (ModifierEntry entry : modifierList) {
      entry.getHook(ModifierHooks.TOOL_STATS).addToolStats(context, entry, statBuilder);
    }
    setStats(statBuilder.build());
    setMultipliers(statBuilder.buildMultipliers());

    // finally, update raw data, called last to make the parameters more convenient mostly, plus no other hooks should be responding to this data
    for (ModifierEntry entry : modifierList) {
      entry.getHook(ModifierHooks.RAW_DATA).addRawData(this, entry);
    }
  }


  /* Static helpers */

  /**
   * Checks if the given tool stats have been initialized, used as a marker to indicate slots are not yet applied
   * @param stack  Stack to check
   * @return  True if initialized
   */
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public static boolean isInitialized(ItemStack stack) {
    return stack.has(TinkerData.TOOL_STATS);
  }

  /**
   * Ensures the given item stack is initialized. Intended to be called in {@link Item#onCraftedBy(ItemStack, Level, Player)}
   * @param stack           ItemStack to initialize
   * @param toolDefinition  Tool definition
   */
  public static void ensureInitialized(ItemStack stack, ToolDefinition toolDefinition) {
    // must be loaded and not initialized
    if (toolDefinition.isDataLoaded() && !isInitialized(stack)) {
      ToolStack.from(stack).ensureHasData();
    }
  }

  /**
   * Rebuilds the item stack when loaded from NBT.
   * Designed to be called in {@link Item#verifyComponentsAfterLoad(ItemStack)}.
   * @param stack       Stack to update
   * @param definition  Tool definition
   */
  public static void verifyComponents(ItemStack stack, ToolDefinition definition) {
    // only rebuild stats if we either have materials, or we don't need materials
    if (definition.isDataLoaded() && !TooltipUtil.isDisplay(stack) && (!definition.hasMaterials() || MaterialRegistry.isFullyLoaded() && stack.has(TinkerData.MATERIALS))) {
      new ToolStack(stack, definition).rebuildStats();
    }
  }
}
