package fp.yeyu.memory.mixin

import fp.yeyu.memory.mixinaccessor.LevelSummaryAccessor
import net.minecraft.world.level.LevelInfo
import org.spongepowered.asm.mixin.{Final, Mixin, Shadow}

@Mixin(targets = Array("net.minecraft.world.level.storage.LevelSummary"))
class LevelSummaryMixin extends LevelSummaryAccessor {
  @Shadow
  @Final val levelInfo: LevelInfo = null

  override def getLevelInfo: LevelInfo = levelInfo
}
