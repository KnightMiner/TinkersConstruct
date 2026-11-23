package slimeknights.tconstruct.library.tools.definition.module.build;

import com.google.common.collect.ImmutableSet;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.definition.module.ToolModule;
import slimeknights.tconstruct.library.tools.stack.ToolContext;

import java.util.List;

/**
 * Module that just sets a boolean flag to true on a tool.
 * @see VolatileIntModule
 * @see slimeknights.tconstruct.library.modifiers.modules.build.VolatileFlagModule
 */
public record VolatileFlagModule(ResourceLocation flag) implements ToolModule, VolatileFlagToolHook {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<VolatileFlagModule>defaultHooks(ToolHooks.VOLATILE_FLAGS);
  public static final RecordLoadable<VolatileFlagModule> LOADER = RecordLoadable.create(Loadables.RESOURCE_LOCATION.requiredField("flag", VolatileFlagModule::flag), VolatileFlagModule::new);

  @Override
  public RecordLoadable<VolatileFlagModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void addVolatileFlags(ToolContext context, ImmutableSet.Builder<ResourceLocation> builder) {
    builder.add(flag);
  }
}
