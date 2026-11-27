package slimeknights.tconstruct.library.tools.definition.module.build;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.definition.module.ToolModule;
import slimeknights.tconstruct.library.tools.stack.ToolContext;
import slimeknights.tconstruct.library.tools.stack.VolatileBuilder;

import java.util.List;
import java.util.function.Supplier;

/**
 * Module that just sets a number of components into the tool during volatile data building.
 * @see VolatileFlagModule
 * @see VolatileIntModule
 * @see slimeknights.tconstruct.library.modifiers.modules.build.VolatileDataModule
 */
public record VolatileDataModule(DataComponentPatch components) implements ToolModule, VolatileDataToolHook {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<VolatileDataModule>defaultHooks(ToolHooks.VOLATILE_DATA);
  public static final RecordLoadable<VolatileDataModule> LOADER = RecordLoadable.create(Loadables.DATA_COMPONENTS.requiredField("components", VolatileDataModule::components), VolatileDataModule::new);

  /** Creates an instance for a single component. Use {@link DataComponentPatch#builder()} if working with multiple. */
  public static <T> VolatileDataModule single(DataComponentType<T> type, T value) {
    return new VolatileDataModule(DataComponentPatch.builder().set(type, value).build());
  }

  /** Creates an instance for a single component. Use {@link DataComponentPatch#builder()} if working with multiple. */
  public static <T> VolatileDataModule single(Supplier<? extends DataComponentType<T>> type, T value) {
    return single(type.get(), value);
  }

  @Override
  public RecordLoadable<VolatileDataModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void addVolatileData(ToolContext context, VolatileBuilder builder, MutableDataComponentHolder volatileData) {
    volatileData.applyComponents(components);
  }
}
