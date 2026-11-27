package slimeknights.tconstruct.library.modifiers.modules.build;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.VolatileDataModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.stack.ToolContext;
import slimeknights.tconstruct.library.tools.stack.VolatileBuilder;

import java.util.List;
import java.util.function.Supplier;

/**
 * Module that just sets a number of components into the tool during volatile data building.
 * @see slimeknights.tconstruct.library.tools.definition.module.build.VolatileDataModule
 * @see VolatileFlagModule
 * @see VolatileIntModule
 * @see VolatileFloatModule
 */
public record VolatileDataModule(DataComponentPatch components, ModifierCondition<ToolContext> condition) implements VolatileDataModifierHook, ModifierModule, ConditionalModule<ToolContext> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<VolatileDataModule>defaultHooks(ModifierHooks.VOLATILE_DATA);
  public static final RecordLoadable<VolatileDataModule> LOADER = RecordLoadable.create(Loadables.DATA_COMPONENTS.requiredField("components", VolatileDataModule::components), ModifierCondition.CONTEXT_FIELD, VolatileDataModule::new);

  public VolatileDataModule(DataComponentPatch components) {
    this(components, ModifierCondition.ANY_CONTEXT);
  }

  /** Creates an instance for a single component. Use {@link DataComponentPatch#builder()} if working with multiple. */
  public static <T> VolatileDataModule single(DataComponentType<T> type, T value) {
    return new VolatileDataModule(DataComponentPatch.builder().set(type, value).build());
  }

  /** Creates an instance for a single component. Use {@link DataComponentPatch#builder()} if working with multiple. */
  public static <T> VolatileDataModule single(Supplier<? extends DataComponentType<T>> type, T value) {
    return single(type.get(), value);
  }

  @Override
  public void addVolatileData(ToolContext context, ModifierEntry modifier, VolatileBuilder builder, MutableDataComponentHolder volatileData) {
    if (condition.matches(context, modifier)) {
      volatileData.applyComponents(components);
    }
  }

  @Override
  public RecordLoadable<VolatileDataModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }
}
