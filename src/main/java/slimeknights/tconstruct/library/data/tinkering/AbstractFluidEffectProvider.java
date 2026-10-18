package slimeknights.tconstruct.library.data.tinkering;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.conditions.OrCondition;
import slimeknights.mantle.data.GenericRegistryDataProvider;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.entity.EntityPredicate;
import slimeknights.mantle.recipe.condition.TagFilledCondition;
import slimeknights.mantle.registration.object.FluidObject;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.tconstruct.common.TinkerDamageTypes;
import slimeknights.tconstruct.common.json.ConfigEnabledCondition;
import slimeknights.tconstruct.library.json.FluidStackSet;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffect;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffectContext;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffectManager;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffects;
import slimeknights.tconstruct.library.modifiers.fluid.FluidMobEffect;
import slimeknights.tconstruct.library.modifiers.fluid.TimeAction;
import slimeknights.tconstruct.library.modifiers.fluid.block.OffsetBlockFluidEffect;
import slimeknights.tconstruct.library.modifiers.fluid.block.PlaceBlockFluidEffect;
import slimeknights.tconstruct.library.modifiers.fluid.entity.DamageFluidEffect;
import slimeknights.tconstruct.library.modifiers.fluid.entity.DamageFluidEffect.DamageTypePair;
import slimeknights.tconstruct.library.modifiers.fluid.general.ConditionalFluidEffect;
import slimeknights.tconstruct.library.recipe.FluidValues;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static slimeknights.mantle.Mantle.commonResource;

/** Data provider for spilling fluids */
public abstract class AbstractFluidEffectProvider extends GenericRegistryDataProvider {
  private final String modId;
  private final Map<ResourceLocation,Builder> entries = new HashMap<>();

  public AbstractFluidEffectProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries, String modId) {
    super(packOutput, Target.DATA_PACK, FluidEffectManager.FOLDER, registries);
    this.modId = modId;
  }

  /** Adds the fluids to the map */
  protected abstract void addFluids(HolderLookup.Provider registries);

  @Override
  public CompletableFuture<?> run(CachedOutput cache, HolderLookup.Provider registries) {
    addFluids(registries);
    return allOf(entries.entrySet().stream().map(entry -> saveJson(cache, entry.getKey(), entry.getValue().build(entry.getKey()))));
  }

  /* Helpers */

  /** Creates a new fluid builder for the given location */
  protected Builder addFluid(ResourceLocation id, FluidStackSet fluid) {
    Builder newBuilder = new Builder(fluid);
    Builder original = entries.put(id, newBuilder);
    if (original != null) {
      throw new IllegalArgumentException("Duplicate spilling fluid " + id);
    }
    return newBuilder;
  }

  /** Creates a new fluid builder for the given mod ID */
  protected Builder addFluid(String name, FluidStackSet fluid) {
    return addFluid(ResourceLocation.fromNamespaceAndPath(modId, name), fluid);
  }

  /** Creates a new fluid builder for the given mod ID */
  protected Builder addFluid(String name, HolderSet<Fluid> fluid, int amount) {
    return addFluid(name, FluidStackSet.of(fluid, amount));
  }

  /** Creates a builder for a fluid and amount */
  protected Builder addFluid(Fluid fluid, int amount) {
    return addFluid(BuiltInRegistries.FLUID.getKey(fluid).getPath(), FluidStackSet.of(fluid, amount));
  }

  /** Creates a builder for a tag and amount */
  protected Builder addFluid(String name, TagKey<Fluid> fluid, int amount) {
    return addFluid(name, FluidStackSet.of(fluid, amount));
  }

  /** Creates a builder for a tag and amount */
  protected Builder addFluid(TagKey<Fluid> fluid, int amount) {
    return addFluid(fluid.location().getPath(), fluid, amount);
  }

  /** Creates a builder for a fluid object */
  protected Builder addFluid(FluidObject<?> fluid, int amount) {
    return addFluid(fluid.id().getPath(), fluid.holderSet(), amount);
  }


  /* Common units */

  /** Builder for a gem based fluid */
  protected Builder addGem(FluidObject<?> fluid) {
    return addFluid(fluid, FluidValues.GEM_SHARD);
  }

  /** Builder for a metal based fluid */
  protected Builder addMetal(FluidObject<?> fluid) {
    return addFluid(fluid, FluidValues.NUGGET);
  }

  /** Adds a conditional fluid effect */
  protected Builder compatFluid(TagKey<Fluid> fluid, int amount) {
    return addFluid(fluid, amount).addCondition(new TagFilledCondition<>(fluid));
  }

  /** Adds a conditional fluid effect */
  protected Builder compatFluid(String name, int amount) {
    return compatFluid(FluidTags.create(commonResource(name)), amount);
  }

  /** Adds a conditional fluid effect */
  protected Builder compatFluid(String modId, TagKey<Fluid> fluid, int amount) {
    return addFluid(fluid, amount).addCondition(new ModLoadedCondition(modId));
  }

  /** Adds a conditional fluid effect */
  protected Builder compatFluid(String modId, String name, int amount) {
    return compatFluid(modId, FluidTags.create(commonResource(name)), amount);
  }

  /** Builder for a metal based fluid */
  protected Builder compatMetal(FluidObject<?> fluid, String... extraIngots) {
    Builder builder = addMetal(fluid);
    // automatically add ourself
    String ourself = fluid.id().getPath().substring("molten_".length());
    if (extraIngots.length == 0) {
      builder.metalCondition(ourself);
    } else {
      // also add extra ingots if requested
      builder.metalCondition(Stream.concat(Stream.of(ourself), Stream.of(extraIngots)).toArray(String[]::new));
    }
    return builder;
  }

  /** Builder for a clay based fluid */
  protected Builder addClay(FluidObject<?> fluid) {
    return addFluid(fluid, FluidValues.BRICK);
  }

  /** Builder for a glass based fluid */
  protected Builder addGlass(FluidObject<?> fluid) {
    return addFluid(fluid, FluidValues.GLASS_PANE);
  }

  /** Builder for a glass based fluid */
  protected Builder addSlime(FluidObject<?> fluid) {
    return addFluid(fluid, FluidValues.SLIME_DROP);
  }


  /** Builder class */
  @RequiredArgsConstructor
  @CanIgnoreReturnValue
  protected static class Builder {
    private final List<ICondition> conditions = new ArrayList<>();
    private final FluidStackSet fluid;
    private final List<FluidEffect<? super FluidEffectContext.Block>> blockEffects = new ArrayList<>();
    private final List<FluidEffect<? super FluidEffectContext.Entity>> entityEffects = new ArrayList<>();
    private boolean hidden = false;

    /** Hides this effect from the book */
    public Builder hidden() {
      this.hidden = true;
      return this;
    }

    /** Adds a condition to the builder */
    public Builder addCondition(ICondition condition) {
      this.conditions.add(condition);
      return this;
    }

    /** Adds conditions for a metal fluid based on any of the given list of ingots being present */
    public Builder metalCondition(String... names) {
      List<ICondition> conditions = new ArrayList<>(names.length + 1);
      conditions.add(ConfigEnabledCondition.FORCE_INTEGRATION_MATERIALS);
      for (String name : names) {
        conditions.add(new TagFilledCondition<>(ItemTags.create(commonResource("ingots/" + name))));
      }
      return addCondition(new OrCondition(conditions));
    }

    /** Adds an effect to the given fluid */
    public Builder addBlockEffect(FluidEffect<? super FluidEffectContext.Block> effect) {
      blockEffects.add(effect);
      return this;
    }

    /** Adds an offset effect to the given fluid */
    public Builder offsetBlockEffect(FluidEffect<? super FluidEffectContext.Block> effect) {
      return addBlockEffect(new OffsetBlockFluidEffect(effect));
    }

    /** Adds an effect to the given fluid */
    public Builder addEntityEffect(FluidEffect<? super FluidEffectContext.Entity> effect) {
      entityEffects.add(effect);
      return this;
    }

    public Builder addEntityEffects(List<? extends FluidEffect<? super FluidEffectContext.Entity>> effects) {
      for (FluidEffect<? super FluidEffectContext.Entity> effect : effects) {
        addEntityEffect(effect);
      }
      return this;
    }

    /** Adds an effect to the given fluid */
    public Builder addEffect(FluidMobEffect.Builder builder, TimeAction action) {
      addBlockEffect(builder.buildCloud());
      addEntityEffects(builder.buildEntity(action));
      return this;
    }

    /** Adds an effect to the given fluid */
    public Builder addEffect(FluidEffect<FluidEffectContext> effect) {
      addBlockEffect(effect);
      addEntityEffect(effect);
      return this;
    }

    /** Adds an effect to the given fluid that only matches if the block matches the predicate */
    public Builder addBlockEffect(IJsonPredicate<BlockState> predicate, FluidEffect<? super FluidEffectContext.Block> effect) {
      return addBlockEffect(new ConditionalFluidEffect.BlockContext(predicate, effect));
    }

    /** Adds an offset effect to the given fluid that only matches if the block matches the offset predicate */
    public Builder offsetBlockEffect(IJsonPredicate<BlockState> predicate, FluidEffect<? super FluidEffectContext.Block> effect) {
      return offsetBlockEffect(new ConditionalFluidEffect.BlockContext(predicate, effect));
    }

    /** Adds an effect to the given fluid that only matches if the direct entity matches the predicate */
    public Builder addDirectEntityEffect(IJsonPredicate<Entity> predicate, FluidEffect<? super FluidEffectContext.Entity> effect) {
      return addEntityEffect(ConditionalFluidEffect.EntityContext.direct(predicate, effect));
    }

    /** Adds an effect to the given fluid that only matches if the resolved living entity matches the predicate */
    public Builder addLivingEntityEffect(IJsonPredicate<LivingEntity> predicate, FluidEffect<? super FluidEffectContext.Entity> effect) {
      return addEntityEffect(ConditionalFluidEffect.EntityContext.living(predicate, effect));
    }


    /* Damage helpers */

    /** Adds a fire block to the block effects */
    public Builder placeFire() {
      return addBlockEffect(new PlaceBlockFluidEffect(Blocks.FIRE, SoundEvents.FIRECHARGE_USE));
    }

    /** Adds a damage effect to the builder */
    public Builder addDamage(float amount, DamageTypePair type) {
      return addEntityEffect(new DamageFluidEffect(amount, type));
    }

    /** Adds a damage effect to the builder */
    public Builder addDirectDamage(IJsonPredicate<Entity> predicate, float amount, DamageTypePair type) {
      return addDirectEntityEffect(predicate, new DamageFluidEffect(amount, type));
    }

    /** Adds a damage effect to the builder */
    public Builder addLivingDamage(IJsonPredicate<LivingEntity> predicate, float amount, DamageTypePair type) {
      return addLivingEntityEffect(predicate, new DamageFluidEffect(amount, type));
    }

    /** Adds fire damage to the builder */
    public Builder impactDamage(float amount) {
      return addDamage(amount, TinkerDamageTypes.FLUID_IMPACT);
    }

    /** Adds fire damage to the builder */
    public Builder fireDamage(float amount) {
      return addDirectDamage(EntityPredicate.FIRE_IMMUNE.inverted(), amount, TinkerDamageTypes.FLUID_FIRE);
    }

    /** Adds fire damage to the builder */
    public Builder coldDamage(float amount) {
      return addDirectDamage(EntityPredicate.CAN_FREEZE, amount, TinkerDamageTypes.FLUID_COLD);
    }

    /** Adds fire damage to the builder */
    public Builder magicDamage(float amount) {
      return addDamage(amount, TinkerDamageTypes.FLUID_MAGIC);
    }

    /** Adds fire damage to the builder */
    public Builder spikeDamage(float amount) {
      return addDamage(amount, TinkerDamageTypes.FLUID_SPIKE);
    }

    /** Builds the instance */
    @CheckReturnValue
    private JsonObject build(ResourceLocation id) {
      JsonObject json = new JsonObject();
      if (!conditions.isEmpty()) {
        json.add("conditions", JsonHelper.serializeList(ICondition.CODEC, conditions));
      }
      if (blockEffects.isEmpty() && entityEffects.isEmpty()) {
        throw new IllegalStateException("Must have at least 1 effect");
      }
      FluidEffects effects = new FluidEffects(fluid, blockEffects, entityEffects, hidden);
      try {
        FluidEffects.LOADABLE.serializeInto(effects, json);
      } catch (Exception e) {
        throw new RuntimeException("Error serializing fluid effect ID " + id + " with value " + effects, e);
      }
      return json;
    }
  }
}
