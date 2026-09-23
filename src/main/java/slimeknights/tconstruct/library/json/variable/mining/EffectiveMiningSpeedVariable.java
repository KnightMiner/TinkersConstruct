package slimeknights.tconstruct.library.json.variable.mining;

import net.minecraft.world.entity.player.Player;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IHaveLoader;
import slimeknights.tconstruct.library.modifiers.hook.mining.BreakSpeedContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;

/** Variable providing a different value based on whether the modifier is effective or not */
public record EffectiveMiningSpeedVariable(MiningSpeedVariable ifTrue, MiningSpeedVariable ifFalse) implements MiningSpeedVariable {
  public static final RecordLoadable<EffectiveMiningSpeedVariable> LOADER = RecordLoadable.create(
    MiningSpeedVariable.LOADER.requiredField("if_true", EffectiveMiningSpeedVariable::ifTrue),
    MiningSpeedVariable.LOADER.requiredField("if_false", EffectiveMiningSpeedVariable::ifFalse),
    EffectiveMiningSpeedVariable::new);

  public EffectiveMiningSpeedVariable(float ifTrue, float ifFalse) {
    this(new MiningSpeedVariable.Constant(ifTrue), new MiningSpeedVariable.Constant(ifFalse));
  }

  @Override
  public RecordLoadable<? extends IHaveLoader> getLoader() {
    return LOADER;
  }

  @Override
  public float getValue(IToolStackView tool, @Nullable BreakSpeedContext context, @Nullable Player player) {
    return (context != null && context.isEffective() ? ifTrue : ifFalse).getValue(tool, context, player);
  }
}
