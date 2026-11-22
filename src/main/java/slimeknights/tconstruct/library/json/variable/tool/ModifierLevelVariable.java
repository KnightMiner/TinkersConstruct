package slimeknights.tconstruct.library.json.variable.tool;

import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.tconstruct.library.json.predicate.modifier.ModifierPredicate;
import slimeknights.tconstruct.library.json.predicate.modifier.SingleModifierPredicate;
import slimeknights.tconstruct.library.json.predicate.tool.HasModifierPredicate.ModifierCheck;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;

/**
 * Variable which fetches the level of a modifier on the tool.
 * @param modifier  Predicate for modifiers to match. Note that if multiple match, the first one is returned.
 * @param check     Whether to check upgrades or all modifiers (including traits).
 */
public record ModifierLevelVariable(IJsonPredicate<ModifierId> modifier, ModifierCheck check) implements ToolVariable {
  public static final RecordLoadable<ModifierLevelVariable> LOADER = RecordLoadable.create(
    ModifierPredicate.LOADER.requiredField("modifier", ModifierLevelVariable::modifier),
    ModifierCheck.LOADABLE.requiredField("check", ModifierLevelVariable::check),
    ModifierLevelVariable::new);

  @Override
  public float getValue(ToolStackView tool) {
    for (ModifierEntry entry : check.getModifiers(tool)) {
      if (modifier.matches(entry.id())) {
        return entry.getEffectiveLevel();
      }
    }
    return 0;
  }

  @Override
  public RecordLoadable<? extends ToolVariable> getLoader() {
    return LOADER;
  }


  /* Constructors */

  /** Checks the list of craftable modifiers */
  public static ModifierLevelVariable crafted(IJsonPredicate<ModifierId> modifier) {
    return new ModifierLevelVariable(modifier, ModifierCheck.CRAFTED);
  }

  /** Checks the list of craftable modifiers */
  public static ModifierLevelVariable crafted(ModifierId modifier) {
    return crafted(new SingleModifierPredicate(modifier));
  }

  /** Checks all modifiers including traits */
  public static ModifierLevelVariable modifier(IJsonPredicate<ModifierId> modifier) {
    return new ModifierLevelVariable(modifier, ModifierCheck.ALL);
  }

  /** Checks all modifiers including traits */
  public static ModifierLevelVariable modifier(ModifierId modifier) {
    return modifier(new SingleModifierPredicate(modifier));
  }
}
