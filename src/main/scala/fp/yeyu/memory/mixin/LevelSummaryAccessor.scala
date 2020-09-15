package fp.yeyu.memory.mixin

import net.minecraft.world.level.LevelInfo
import net.minecraft.world.level.storage.LevelSummary
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(Array(classOf[LevelSummary]))
trait LevelSummaryAccessor {
  @Accessor
  def getField_25022: LevelInfo
}
