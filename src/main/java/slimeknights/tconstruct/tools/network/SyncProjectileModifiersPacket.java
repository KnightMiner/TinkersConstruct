package slimeknights.tconstruct.tools.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.tools.capability.EntityModifierCapability;
import slimeknights.tconstruct.library.tools.component.ModifierEntries;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

/** Packet used to sync modifiers on a projectile. Automatically sent for all relevant projectiles. */
@Internal
public record SyncProjectileModifiersPacket(int entityId, ModifierEntries modifiers, CompoundTag persistentData) implements ISimplePacket {
  public static final Type<SyncProjectileModifiersPacket> TYPE = new Type<>(TConstruct.getResource("sync_projectile_modifiers"));
  public static final StreamCodec<RegistryFriendlyByteBuf,SyncProjectileModifiersPacket> CODEC = StreamCodec.composite(
    ByteBufCodecs.VAR_INT, SyncProjectileModifiersPacket::entityId,
    ModifierEntries.STREAM_CODEC, SyncProjectileModifiersPacket::modifiers,
    ByteBufCodecs.COMPOUND_TAG, SyncProjectileModifiersPacket::persistentData,
    SyncProjectileModifiersPacket::new);

  public SyncProjectileModifiersPacket(Entity entity) {
    this(entity.getId(), EntityModifierCapability.getOrEmpty(entity), ModDataNBT.get(entity).getCopy());
  }

  @Override
  public Type<SyncProjectileModifiersPacket> type() {
    return TYPE;
  }

  @Override
  public void handle(IPayloadContext context) {
    Entity entity = context.player().level().getEntity(entityId);
    if (entity != null) {
      EntityModifierCapability.getCapability(entity).setModifiers(modifiers);
      ModDataNBT.get(entity).copyFrom(persistentData);
    }
  }
}
