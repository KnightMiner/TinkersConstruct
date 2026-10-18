package slimeknights.tconstruct.library.tools.definition.module.build;

import com.google.common.collect.ImmutableSet;
import net.neoforged.neoforge.common.ItemAbility;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.definition.module.ToolModule;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;
import java.util.Set;

/** Module that allows a tool to perform tool actions */
public record ItemAbilitiesModule(Set<ItemAbility> abilities) implements ItemAbilityToolHook, ToolModule {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ItemAbilitiesModule>defaultHooks(ToolHooks.ITEM_ABILITY);
  public static final RecordLoadable<ItemAbilitiesModule> LOADER = RecordLoadable.create(Loadables.ITEM_ABILITY.set().requiredField("abilities", ItemAbilitiesModule::abilities), ItemAbilitiesModule::new);

  public static ItemAbilitiesModule of(ItemAbility... actions) {
    return new ItemAbilitiesModule(ImmutableSet.copyOf(actions));
  }

  @Override
  public RecordLoadable<ItemAbilitiesModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public boolean canPerformAction(IToolStackView tool, ItemAbility ability) {
    return abilities.contains(ability);
  }
}
