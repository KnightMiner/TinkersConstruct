package slimeknights.tconstruct.library.json.predicate.tool;

import net.neoforged.neoforge.common.ItemAbility;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** Predicate matching tools exposing a given tool action */
public record ItemAbilityPredicate(ItemAbility ability) implements ToolStackPredicate {
  public static final RecordLoadable<ItemAbilityPredicate> LOADER = RecordLoadable.create(Loadables.ITEM_ABILITY.requiredField("ability", ItemAbilityPredicate::ability), ItemAbilityPredicate::new);

  @Override
  public boolean matches(IToolStackView tool) {
    return ModifierUtil.canPerformAction(tool, ability);
  }

  @Override
  public RecordLoadable<ItemAbilityPredicate> getLoader() {
    return LOADER;
  }
}
