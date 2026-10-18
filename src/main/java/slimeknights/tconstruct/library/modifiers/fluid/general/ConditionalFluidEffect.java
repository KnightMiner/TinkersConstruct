package slimeknights.tconstruct.library.modifiers.fluid.general;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.block.BlockPredicate;
import slimeknights.tconstruct.library.json.predicate.EntityTargetPredicate;
import slimeknights.tconstruct.library.modifiers.fluid.EffectLevel;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffect;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffectContext;

/**
 * Fluid effect that conditions on the holder and target.
 * @apiNote This class is internal, no need to use directly. Instead, make use of the helpers in {@link slimeknights.tconstruct.library.data.tinkering.AbstractFluidEffectProvider}.
 */
@Internal
public interface ConditionalFluidEffect<C extends FluidEffectContext> extends FluidEffect<C> {
  /** Gets the effect to apply */
  FluidEffect<? super C> effect();

  /** Checks if this effect can be applied */
  boolean canApply(C context);

  @Override
  default float apply(FluidStack fluid, EffectLevel level, C context, FluidAction action) {
    return canApply(context) ? effect().apply(fluid, level, context, action) : 0;
  }

  @Override
  default Component getDescription(RegistryAccess registryAccess) {
    return effect().getDescription(registryAccess);
  }

  /** Conditional block effect */
  record BlockContext(IJsonPredicate<BlockState> block, FluidEffect<? super FluidEffectContext.Block> effect) implements ConditionalFluidEffect<FluidEffectContext.Block> {
    public static final RecordLoadable<BlockContext> LOADER = RecordLoadable.create(
      BlockPredicate.LOADER.defaultField("block", e -> e.block),
      FluidEffect.BLOCK_EFFECTS.requiredField("effect", e -> e.effect),
      BlockContext::new);

    @Override
    public boolean canApply(FluidEffectContext.Block context) {
      return block.matches(context.getBlockState());
    }

    @Override
    public RecordLoadable<BlockContext> getLoader() {
      return LOADER;
    }
  }

  /** Common logic between both entity implementations. */
  record EntityContext(EntityTargetPredicate target, FluidEffect<? super FluidEffectContext.Entity> effect) implements ConditionalFluidEffect<FluidEffectContext.Entity> {
    public static final RecordLoadable<EntityContext> LOADER = RecordLoadable.create(
      EntityTargetPredicate.LOADABLE.directField(EntityContext::target),
      FluidEffect.ENTITY_EFFECTS.requiredField("effect", EntityContext::effect),
      EntityContext::new);

    /** Conditions on a direct target */
    public static EntityContext direct(IJsonPredicate<Entity> target, FluidEffect<? super FluidEffectContext.Entity> effect) {
      return new EntityContext(EntityTargetPredicate.direct(target), effect);
    }

    /** Conditions on a living target */
    public static EntityContext living(IJsonPredicate<LivingEntity> target, FluidEffect<? super FluidEffectContext.Entity> effect) {
      return new EntityContext(EntityTargetPredicate.living(target), effect);
    }

    @Override
    public RecordLoadable<EntityContext> getLoader() {
      return LOADER;
    }

    @Override
    public boolean canApply(FluidEffectContext.Entity context) {
      return target.matches(context.getTarget(), context.getLivingTarget());
    }
  }
}
