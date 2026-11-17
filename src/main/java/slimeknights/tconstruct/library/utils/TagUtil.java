package slimeknights.tconstruct.library.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import slimeknights.tconstruct.TConstruct;

import javax.annotation.Nullable;
import java.util.Optional;

/** Helpers related to Tag */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TagUtil {
  /* Helper functions */

  /**
   * Reads a block position from Tag
   * @param parent  Parent tag
   * @param key     Position key
   * @param offset  Amount to offset position by
   * @return  Block position, or null if invalid or missing
   */
  @Nullable
  public static BlockPos readOptionalPos(CompoundTag parent, String key, BlockPos offset) {
    if (parent.contains(key, Tag.TAG_COMPOUND)) {
      return NbtUtils.readBlockPos(parent.getCompound(key)).offset(offset);
    }
    return null;
  }

  /**
   * Checks if the given tag is a numeric type
   * @param tag  Tag to check
   * @return  True if the type matches
   */
  public static boolean isNumeric(Tag tag) {
    byte type = tag.getId();
    return type == Tag.TAG_BYTE || type == Tag.TAG_SHORT || type == Tag.TAG_INT || type == Tag.TAG_LONG || type == Tag.TAG_FLOAT || type == Tag.TAG_DOUBLE;
  }


  /* Codec */

  /** Parses the given object from NBT */
  public static <T> Optional<T> decode(DynamicOps<Tag> ops, Codec<T> codec, String name, Tag tag) {
    return codec.parse(ops, tag).resultOrPartial(error -> TConstruct.LOG.error("Tried to load invalid {} '{}'", name, error));
  }

  /** Parses the given object from NBT */
  public static <T> Optional<T> decode(Codec<T> codec, String name, Tag tag) {
    return decode(NbtOps.INSTANCE, codec, name, tag);
  }

  /** Serializes the given object to NBT */
  public static <T> Tag encode(DynamicOps<Tag> ops, Codec<T> codec, T value) {
    return codec.encodeStart(ops, value).getOrThrow();
  }

  /** Serializes the given object to NBT */
  public static <T> Tag encode(Codec<T> codec, T value) {
    return encode(NbtOps.INSTANCE, codec, value);
  }
}
