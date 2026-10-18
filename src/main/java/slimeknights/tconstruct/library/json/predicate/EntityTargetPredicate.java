package slimeknights.tconstruct.library.json.predicate;

import com.mojang.datafixers.util.Either;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.entity.PartEntity;
import slimeknights.mantle.data.loadable.mapping.EitherRecordLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.entity.EntityPredicate;
import slimeknights.mantle.data.predicate.entity.LivingEntityPredicate;

import javax.annotation.Nullable;

/** Predicate matching either an entity or a living entity. If entity, it matches the direct target, while living entity will possibly use the parent. */
public sealed interface EntityTargetPredicate {
  /** Loader instance, designed for use in direct fields. */
  RecordLoadable<EntityTargetPredicate> LOADABLE = new EitherRecordLoadable<>("direct_target", EntityPredicate.LOADER, "living_target", LivingEntityPredicate.LOADER)
    .flatXmap(either -> either.map(DirectTarget::new, LivingTarget::new), EntityTargetPredicate::predicate);

  /**
   * Gets a {@link LivingEntity} to target from the given entity, resolving the {@link PartEntity} parent if needed.
   * Note if the entity is already {@link LivingEntity}, this method is not necessary as living entities cannot be part entities.
   * @param entity  entity instance
   * @return  living entity, or null if it's not living
   */
  @Nullable
  static LivingEntity resolveParent(Entity entity) {
    if (entity instanceof PartEntity<?> part) {
      entity = part.getParent();
    }
    return entity instanceof LivingEntity living ? living : null;
  }

  /** Creates a direct predicate */
  static EntityTargetPredicate direct(IJsonPredicate<Entity> target) {
    return new DirectTarget(target);
  }

  /** Creates a living predicate */
  static EntityTargetPredicate living(IJsonPredicate<LivingEntity> target) {
    return new LivingTarget(target);
  }


  /* Object methods */

  /** Gets the predicate for the given type */
  Either<IJsonPredicate<Entity>,IJsonPredicate<LivingEntity>> predicate();

  /**
   * Tests the given entity and living pair.
   * @param entity  Direct target
   * @param living  Parent of target
   * @return true if the condition matches.
   */
  boolean matches(Entity entity, @Nullable LivingEntity living);

  /** Tests the given entity, fetching their parent for the living check. */
  default boolean matches(Entity entity) {
    return matches(entity, resolveParent(entity));
  }

  /** Predicate matching the direct target. */
  record DirectTarget(IJsonPredicate<Entity> target) implements EntityTargetPredicate {
    @Override
    public Either<IJsonPredicate<Entity>, IJsonPredicate<LivingEntity>> predicate() {
      return Either.left(target);
    }

    @Override
    public boolean matches(Entity entity, @Nullable LivingEntity living) {
      return target.matches(entity);
    }
  }

  /** Predicate matching the living target, which may be the parent. */
  record LivingTarget(IJsonPredicate<LivingEntity> target) implements EntityTargetPredicate {
    @Override
    public Either<IJsonPredicate<Entity>, IJsonPredicate<LivingEntity>> predicate() {
      return Either.right(target);
    }

    @Override
    public boolean matches(Entity entity, @Nullable LivingEntity living) {
      return living != null && target.matches(living);
    }
  }
}
