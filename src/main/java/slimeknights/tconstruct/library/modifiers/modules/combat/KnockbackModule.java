package slimeknights.tconstruct.library.modifiers.modules.combat;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.entity.LivingEntityPredicate;
import slimeknights.tconstruct.library.json.math.FormulaLoadable;
import slimeknights.tconstruct.library.json.math.ModifierFormula;
import slimeknights.tconstruct.library.json.math.ModifierFormula.FallbackFormula;
import slimeknights.tconstruct.library.json.predicate.EntityTargetPredicate;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ProjectilePredicate;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;

/**
 * Module to add knockback to a melee attack
 * @param target     Filter on entities to receive knockback
 * @param attacker   Filter on entities holding the weapon
 * @param formula    Formula to compute the knockback amount
 */
// TODO 1.21: support formulas, maybe with melee variable?
public record KnockbackModule(EntityTargetPredicate target, IJsonPredicate<LivingEntity> attacker, ModifierFormula formula, ProjectilePredicate projectile, ModifierCondition<IToolStackView> condition) implements MeleeHitModifierHook, ModifierModule, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<KnockbackModule>defaultHooks(ModifierHooks.MELEE_HIT);
  /** Setup for the formula */
  private static final FormulaLoadable FORMULA = new FormulaLoadable(FallbackFormula.ADD, "level", "knockback");
  /** Loader instance */
  public static final RecordLoadable<KnockbackModule> LOADER = RecordLoadable.create(
    EntityTargetPredicate.defaultField(KnockbackModule::target),
    LivingEntityPredicate.LOADER.defaultField("attacker", KnockbackModule::attacker),
    FORMULA.directField(KnockbackModule::formula),
    ProjectilePredicate.LOADABLE.defaultField("allow", ProjectilePredicate.ALWAYS, KnockbackModule::projectile),
    ModifierCondition.TOOL_FIELD,
    KnockbackModule::new);

  /** @apiNote Internal constructor, use {@link #builder()} */
  @Internal
  public KnockbackModule {}

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public float beforeMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damage, float baseKnockback, float knockback) {
    // might want to consider an entity predicate here, this special casing is a bit odd
    if (this.condition.matches(tool, modifier) && projectile.test(context.isProjectile()) && this.attacker.matches(context.getAttacker()) && this.target.matches(context)) {
      return formula.apply(formula.processLevel(modifier), knockback);
    }
    return knockback;
  }


  @Override
  public RecordLoadable<KnockbackModule> getLoader() {
    return LOADER;
  }


  /* Builder */

  /** Creates a builder instance */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder class */
  @Setter
  @Accessors(fluent = true)
  public static class Builder extends ModifierFormula.Builder<Builder,KnockbackModule> implements EntityTargetPredicate.Builder<Builder> {
    private EntityTargetPredicate target = EntityTargetPredicate.ANY;
    private IJsonPredicate<LivingEntity> attacker = LivingEntityPredicate.ANY;
    private ProjectilePredicate projectile = ProjectilePredicate.ALWAYS;

    private Builder() {
      super(FORMULA.variables());
    }

    @Override
    protected KnockbackModule build(ModifierFormula formula) {
      return new KnockbackModule(target, attacker, formula, projectile, condition);
    }
  }
}
