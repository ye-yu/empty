package fp.yeyu.memory.mixin

import net.minecraft.world.level.LevelInfo
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(targets = Array("net.minecraft.world.level.storage.LevelSummary"))
trait LevelSummaryAccessor {
  @Accessor
  def getLevelInfo: LevelInfo
}
