package slimeknights.tconstruct.tools.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.tconstruct.TConstruct;

/** Packet to update motion on an entity. TODO: is this no longer needed? */
public record EntityMovementChangePacket(int entityID, double x, double y, double z, float yRot, float xRot) implements ISimplePacket {
  public static final Type<EntityMovementChangePacket> TYPE = new Type<>(TConstruct.getResource("entity_movement_change"));
  public static final StreamCodec<ByteBuf,EntityMovementChangePacket> CODEC = StreamCodec.composite(
    ByteBufCodecs.VAR_INT, EntityMovementChangePacket::entityID,
    ByteBufCodecs.DOUBLE, EntityMovementChangePacket::x,
    ByteBufCodecs.DOUBLE, EntityMovementChangePacket::y,
    ByteBufCodecs.DOUBLE, EntityMovementChangePacket::z,
    ByteBufCodecs.FLOAT, EntityMovementChangePacket::xRot,
    ByteBufCodecs.FLOAT, EntityMovementChangePacket::yRot,
    EntityMovementChangePacket::new);

  public static EntityMovementChangePacket from(Entity entity) {
    Vec3 movement = entity.getDeltaMovement();
    return new EntityMovementChangePacket(entity.getId(), movement.x, movement.y, movement.z, entity.getXRot(), entity.getYRot());
  }

  @Override
  public Type<EntityMovementChangePacket> type() {
    return TYPE;
  }

  @Override
  public void handle(IPayloadContext context) {
    Entity entity = context.player().level().getEntity(entityID);
    if (entity != null) {
      entity.setDeltaMovement(x, y, z);
      entity.setYRot(yRot);
      entity.setXRot(xRot);
    }
  }
}
