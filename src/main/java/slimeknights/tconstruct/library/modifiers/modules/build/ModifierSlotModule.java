package slimeknights.tconstruct.library.modifiers.modules.build;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.data.loadable.field.LegacyField;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.json.LevelingInt;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.VolatileDataModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModuleBuilder;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.stack.BuilderKeys;
import slimeknights.tconstruct.library.tools.stack.ToolContext;
import slimeknights.tconstruct.library.tools.stack.VolatileBuilder;

import java.util.List;

/**
 * Module that adds extra modifier slots to a tool.
 */
public record ModifierSlotModule(SlotType type, LevelingInt count, ModifierCondition<ToolContext> condition) implements VolatileDataModifierHook, ModifierModule, ConditionalModule<ToolContext> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ModifierSlotModule>defaultHooks(ModifierHooks.VOLATILE_DATA);
  public static final RecordLoadable<ModifierSlotModule> LOADER = RecordLoadable.create(
    SlotType.LOADABLE.requiredField("name", ModifierSlotModule::type),
    IntLoadable.ANY_SHORT.defaultField("flat", 0, m -> m.count.flat()),
    // TODO 1.21ish: drop legacy support in favor of a direct LevelingInt field
    new LegacyField<>(IntLoadable.ANY_SHORT.defaultField("each_level", 0, m -> m.count.eachLevel()), "count"),
    ModifierCondition.CONTEXT_FIELD,
    (type, flat, leveling, condition) -> new ModifierSlotModule(type, new LevelingInt(flat, leveling), condition));
  
  /** @apiNote Internal constructor, use {@link #slot(SlotType)} */
  @Internal
  public ModifierSlotModule {}

  @Override
  public Integer getPriority() {
    // show lower priority so they group together
    return 50;
  }

  @Override
  public void addVolatileData(ToolContext context, ModifierEntry modifier, VolatileBuilder builder, MutableDataComponentHolder volatileData) {
    if (condition.matches(context, modifier)) {
      builder.get(BuilderKeys.SLOTS).add(type, count.computeForLevel(modifier.getEffectiveLevel()));
    }
  }

  @Override
  public RecordLoadable<ModifierSlotModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  
  /** Creates a builder instance */
  public static Builder slot(SlotType slotType) {
    return new Builder(slotType);
  }
  
  /** Builder for the module */
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder extends ModuleBuilder.Context<Builder> implements LevelingInt.Builder<ModifierSlotModule> {
    private final SlotType slotType;

    @Override
    public ModifierSlotModule amount(int flat, int eachLevel) {
      return new ModifierSlotModule(slotType, new LevelingInt(flat, eachLevel), condition);
    }
  }
}
