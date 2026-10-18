package slimeknights.tconstruct.library.modifiers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.netty.handler.codec.DecoderException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.impl.ComposableModifier;
import slimeknights.tconstruct.library.utils.GenericTagUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/** Packet to sync modifiers, including definitions, tags, redirects, and the enchantment maps. */
@Internal
@RequiredArgsConstructor
public class UpdateModifiersPacket implements ISimplePacket {
  public static final Type<UpdateModifiersPacket> TYPE = new Type<>(TConstruct.getResource("update_modifiers"));

  /** Collection of all modifiers */
  @Getter
  private final Map<ModifierId,Modifier> allModifiers;
  /** Map of all modifier tags */
  @Getter
  private final Map<TagKey<Modifier>,List<Modifier>> tags;
  /** Collection of non-redirect modifiers */
  private Collection<ComposableModifier> modifiers;
  /** Map of modifier redirect ID pairs */
  private Map<ModifierId,ModifierId> redirects;
  /** Map of enchantment to modifier pair */
  @Getter
  private final Map<Holder.Reference<Enchantment>,Modifier> enchantmentMap;
  /** Collection of all enchantment tag mappings */
  @Getter
  private final List<ModifierManager.EnchantmentTag> enchantmentTags;

  @Override
  public Type<UpdateModifiersPacket> type() {
    return TYPE;
  }

  @Override
  public void handle(IPayloadContext context) {
    ModifierManager.INSTANCE.updateModifiersFromServer(this);
  }

  /** Ensures both the modifiers and redirects lists are calculated, allows one packet to be used multiple times without redundant work */
  private void ensureCalculated() {
    if (this.modifiers == null || this.redirects == null) {
      ImmutableList.Builder<ComposableModifier> modifiers = ImmutableList.builder();
      ImmutableMap.Builder<ModifierId,ModifierId> redirects = ImmutableMap.builder();
      for (Entry<ModifierId,Modifier> entry : allModifiers.entrySet()) {
        ModifierId id = entry.getKey();
        Modifier value = entry.getValue();
        ModifierId actual = value.id();
        if (id.equals(actual)) {
          // we can't sync anything that is not composable
          if (value instanceof ComposableModifier composable) {
            modifiers.add(composable);
          } else {
            TConstruct.LOG.warn("Unable to sync modifier {} as its not ComposableModifier; got class {}", id, value.getClass().getName());
          }
        } else {
          redirects.put(id, actual);
        }
      }
      this.modifiers = modifiers.build();
      this.redirects = redirects.build();
    }
  }

  public static final StreamCodec<RegistryFriendlyByteBuf,UpdateModifiersPacket> CODEC = new StreamCodec<>() {
    /** Gets a modifier by the given ID, falling back to the map if needed */
    private static Modifier getModifier(Map<ModifierId,Modifier> modifiers, ModifierId id) {
      Modifier modifier = ModifierManager.INSTANCE.getStatic(id);
      if (modifier == ModifierManager.INSTANCE.getDefaultValue()) {
        modifier = modifiers.get(id);
        if (modifier == null) {
          throw new DecoderException("Unknown modifier " + id);
        }
      }
      return modifier;
    }

    @Override
    public UpdateModifiersPacket decode(RegistryFriendlyByteBuf buffer) {
      // read in modifiers
      int size = buffer.readVarInt();
      Map<ModifierId,Modifier> modifiers = new HashMap<>();
      for (int i = 0; i < size; i++) {
        ModifierId id = new ModifierId(buffer.readUtf(Short.MAX_VALUE));
        try {
          Modifier modifier = ComposableModifier.LOADER.decode(buffer, ModifierManager.contextBuilder(id).build());
          modifier.setId(id);
          modifiers.put(id, modifier);
        } catch (RuntimeException e) {
          TConstruct.LOG.error("Failed to decode modifier with ID {}", id, e);
          throw e;
        }
      }
      // read in redirects
      size = buffer.readVarInt();
      for (int i = 0; i < size; i++) {
        ModifierId from = new ModifierId(buffer.readUtf(Short.MAX_VALUE));
        modifiers.put(from, getModifier(modifiers, new ModifierId(buffer.readUtf(Short.MAX_VALUE))));
      }
      Map<TagKey<Modifier>,List<Modifier>> tags = GenericTagUtil.decodeTags(buffer, ModifierManager.REGISTRY_KEY, id -> getModifier(modifiers, new ModifierId(id)));

      // read in enchantment to modifier mapping
      Registry<Enchantment> enchantmentRegistry = buffer.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
      ImmutableMap.Builder<Holder.Reference<Enchantment>,Modifier> enchantmentBuilder = ImmutableMap.builder();
      size = buffer.readVarInt();
      for (int i = 0; i < size; i++) {
        ResourceLocation id = buffer.readResourceLocation();
        enchantmentBuilder.put(
          enchantmentRegistry.getHolder(ResourceKey.create(Registries.ENCHANTMENT, id)).orElseThrow(() -> new DecoderException("Unknown enchantment " + id)),
          getModifier(modifiers, new ModifierId(buffer.readResourceLocation())));
      }
      ImmutableList.Builder<ModifierManager.EnchantmentTag> enchantmentTagBuilder = ImmutableList.builder();
      size = buffer.readVarInt();
      for (int i = 0; i < size; i++) {
        ResourceLocation tag = buffer.readResourceLocation();
        enchantmentTagBuilder.add(new ModifierManager.EnchantmentTag(
          enchantmentRegistry.getTag(TagKey.create(Registries.ENCHANTMENT, tag)).orElseThrow(() -> new DecoderException("Unknown enchantment tag " + tag)),
          getModifier(modifiers, new ModifierId(buffer.readResourceLocation()))
        ));
      }
      return new UpdateModifiersPacket(Map.copyOf(modifiers), tags, enchantmentBuilder.build(), enchantmentTagBuilder.build());
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer, UpdateModifiersPacket packet) {
      packet.ensureCalculated();
      // write modifiers
      buffer.writeVarInt(packet.modifiers.size());
      for (ComposableModifier modifier : packet.modifiers) {
        ResourceLocation id = modifier.id();
        buffer.writeResourceLocation(id);
        try {
          ComposableModifier.LOADER.encode(buffer, modifier);
        } catch (RuntimeException e) {
          // improve error logging
          TConstruct.LOG.error("Failed to encode modifier with ID {}", id, e);
          throw e;
        }
      }
      // write redirects
      buffer.writeVarInt(packet.redirects.size());
      for (Entry<ModifierId,ModifierId> entry : packet.redirects.entrySet()) {
        buffer.writeResourceLocation(entry.getKey());
        buffer.writeResourceLocation(entry.getValue());
      }
      GenericTagUtil.encodeTags(buffer, Modifier::id, packet.tags);

      // enchantment mapping
      buffer.writeVarInt(packet.enchantmentMap.size());
      for (Entry<Holder.Reference<Enchantment>,Modifier> entry : packet.enchantmentMap.entrySet()) {
        buffer.writeResourceLocation(entry.getKey().key().location());
        buffer.writeResourceLocation(entry.getValue().id());
      }
      buffer.writeVarInt(packet.enchantmentTags.size());
      for (ModifierManager.EnchantmentTag entry : packet.enchantmentTags) {
        buffer.writeResourceLocation(entry.tag().key().location());
        buffer.writeResourceLocation(entry.modifier().id());
      }
    }
  };
}
