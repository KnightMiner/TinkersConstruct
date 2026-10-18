package slimeknights.tconstruct.library.modifiers.modules.behavior;

import com.google.common.collect.ImmutableSet;
import net.neoforged.neoforge.common.ItemAbility;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ItemAbilityModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;
import java.util.Set;

/**
 * Module that allows a modifier to perform tool actions
 */
public record ItemAbilitiesModule(Set<ItemAbility> abilities, ModifierCondition<IToolStackView> condition) implements ItemAbilityModifierHook, ModifierModule, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ItemAbilitiesModule>defaultHooks(ModifierHooks.ITEM_ABILITY);
  public static final RecordLoadable<ItemAbilitiesModule> LOADER = RecordLoadable.create(
    Loadables.ITEM_ABILITY.set().requiredField("abilities", ItemAbilitiesModule::abilities),
    ModifierCondition.TOOL_FIELD,
    ItemAbilitiesModule::new);

  public ItemAbilitiesModule(ItemAbility... actions) {
    this(ImmutableSet.copyOf(actions), ModifierCondition.ANY_TOOL);
  }

  @Override
  public boolean canPerformAction(IToolStackView tool, ModifierEntry modifier, ItemAbility ability) {
    return condition.matches(tool, modifier) && abilities.contains(ability);
  }

  @Override
  public RecordLoadable<ItemAbilitiesModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }
}
