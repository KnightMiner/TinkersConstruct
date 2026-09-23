package slimeknights.tconstruct.tools.modifiers.melee.dual;

import slimeknights.tconstruct.library.modifiers.modules.behavior.ShowOffhandModule;
import slimeknights.tconstruct.library.module.ModuleHookMap.Builder;

public class AmbidextrousModifier extends OffhandAttackModifier {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    super.registerHooks(hookBuilder);
    hookBuilder.addModule(ShowOffhandModule.DISALLOW_BROKEN);
  }

  @Override
  public boolean shouldDisplay(boolean advanced) {
    return true;
  }
}
