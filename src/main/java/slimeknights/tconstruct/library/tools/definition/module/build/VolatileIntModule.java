package slimeknights.tconstruct.library.tools.definition.module.build;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.definition.module.ToolModule;
import slimeknights.tconstruct.library.tools.stack.BuilderKeys;
import slimeknights.tconstruct.library.tools.stack.ToolContext;
import slimeknights.tconstruct.library.tools.stack.VolatileBuilder;

import java.util.List;

/**
 * Module that just sets a boolean flag to true on a tool.
 * @see VolatileFlagModule
 * @see VolatileDataModule
 * @see slimeknights.tconstruct.library.modifiers.modules.build.VolatileIntModule
 */
public record VolatileIntModule(ResourceLocation flag, int value) implements ToolModule, VolatileDataToolHook {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<VolatileIntModule>defaultHooks(ToolHooks.VOLATILE_DATA);
  public static final RecordLoadable<VolatileIntModule> LOADER = RecordLoadable.create(
    Loadables.RESOURCE_LOCATION.requiredField("flag", VolatileIntModule::flag),
    IntLoadable.ANY_FULL.requiredField("value", VolatileIntModule::value),
    VolatileIntModule::new);

  @Override
  public RecordLoadable<VolatileIntModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void addVolatileData(ToolContext context, VolatileBuilder builder, MutableDataComponentHolder volatileData) {
    builder.get(BuilderKeys.INTEGERS).add(flag, value);
  }
}
