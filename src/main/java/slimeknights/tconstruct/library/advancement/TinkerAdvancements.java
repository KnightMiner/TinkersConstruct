package slimeknights.tconstruct.library.advancement;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.tconstruct.TConstruct;

/** Handles advancement related registrations */
public class TinkerAdvancements {
  private static final DeferredRegister<ItemSubPredicate.Type<?>> ITEM_PREDICATES = DeferredRegister.create(Registries.ITEM_SUB_PREDICATE_TYPE, TConstruct.MOD_ID);
  private static final DeferredRegister<CriterionTrigger<?>> CRITERIA_TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, TConstruct.MOD_ID);

  private TinkerAdvancements() {}

  /** Registers this to the bus */
  @Internal
  public static void register(IEventBus bus) {
    ITEM_PREDICATES.register(bus);
    CRITERIA_TRIGGERS.register(bus);
  }

  /** Predicate matching a tool condition */
  public static final DeferredHolder<ItemSubPredicate.Type<?>,ItemSubPredicate.Type<ToolSubPredicate>> toolPredicate = ITEM_PREDICATES.register("tool", () -> new ItemSubPredicate.Type<>(ToolSubPredicate.CODEC));
  /** Predicate matching a tool condition */
  public static final DeferredHolder<ItemSubPredicate.Type<?>,ItemSubPredicate.Type<FluidFullSubPredicate>> fluidPredicate = ITEM_PREDICATES.register("fluid_full", () -> new ItemSubPredicate.Type<>(FluidFullSubPredicate.CODEC));

  /** Trigger when opening a container menu */
  public static final DeferredHolder<CriterionTrigger<?>, BlockContainerOpenedTrigger> blockContainerOpened = CRITERIA_TRIGGERS.register("block_container_opened", BlockContainerOpenedTrigger::new);
}
