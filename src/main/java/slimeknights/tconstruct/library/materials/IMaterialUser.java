package slimeknights.tconstruct.library.materials;

/** Common interface between {@link slimeknights.tconstruct.library.materials.stats.MaterialStatsId} and {@link slimeknights.tconstruct.library.tools.part.IMaterialItem} */
public interface IMaterialUser {
  /**
   * Checks if the given material can be used by this object.
   * @deprecated use {@link #canUseMaterial(MaterialHolder)}
   */
  @Deprecated
  boolean canUseMaterial(MaterialId material);

  /** Checks if the given material can be used by this object. */
  boolean canUseMaterial(MaterialHolder material);
}
