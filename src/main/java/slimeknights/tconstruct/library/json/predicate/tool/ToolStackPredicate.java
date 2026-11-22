package slimeknights.tconstruct.library.json.predicate.tool;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.loadable.record.SingletonLoader;
import slimeknights.mantle.data.predicate.FallbackPredicateRegistry;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.item.ItemPredicate;
import slimeknights.tconstruct.library.tools.stack.ToolContext;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;

import java.util.List;
import java.util.function.Predicate;

/** Predicate supporting tool stacks, for broader context */
public interface ToolStackPredicate extends IJsonPredicate<ToolStackView> {
  /** Predicate that matches all tools */
  ToolStackPredicate ANY = simple(tool -> true);
  /** Predicate that matches no tools */
  ToolStackPredicate NONE = simple(tool -> true);
  /** Loader for tool predicates */
  FallbackPredicateRegistry<ToolStackView, ToolContext> LOADER = new FallbackPredicateRegistry<>("Tool Stack Predicate", ANY, NONE, ToolContextPredicate.LOADER, t -> t, "tool");

  @Override
  default IJsonPredicate<ToolStackView> inverted() {
    return LOADER.invert(this);
  }


  /* Singleton */

  /** Predicate that matches all tools */
  ToolStackPredicate NOT_BROKEN = simple(tool -> !tool.isBroken());

  /** Creates a new simple predicate */
  static ToolStackPredicate simple(Predicate<ToolStackView> predicate) {
    return SingletonLoader.singleton(loader -> new ToolStackPredicate() {
      @Override
      public boolean matches(ToolStackView tool) {
        return predicate.test(tool);
      }

      @Override
      public RecordLoadable<? extends ToolStackPredicate> getLoader() {
        return loader;
      }
    });
  }


  /* Helper methods */

  /** Creates a tag predicate */
  static IJsonPredicate<ToolStackView> context(IJsonPredicate<ToolContext> predicate) {
    return LOADER.fallback(predicate);
  }

  /** Creates a tag predicate */
  static IJsonPredicate<ToolStackView> fallback(IJsonPredicate<Item> predicate) {
    return context(ToolContextPredicate.fallback(predicate));
  }

  /** Creates an item set predicate */
  static IJsonPredicate<ToolStackView> set(Item... items) {
    return fallback(ItemPredicate.set(items));
  }

  /** Creates a tag predicate */
  static IJsonPredicate<ToolStackView> tag(TagKey<Item> tag) {
    return fallback(ItemPredicate.tag(tag));
  }

  /** Creates an and predicate */
  @SafeVarargs
  static IJsonPredicate<ToolStackView> and(IJsonPredicate<ToolStackView>... predicates) {
    return LOADER.and(List.of(predicates));
  }

  /** Creates an or predicate */
  @SafeVarargs
  static IJsonPredicate<ToolStackView> or(IJsonPredicate<ToolStackView>... predicates) {
    return LOADER.or(List.of(predicates));
  }
}
