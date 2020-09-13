package fp.yeyu.memory.mixin

import java.util

import net.minecraft.world.level.storage.{LevelStorage, LevelSummary}
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.{At, Inject}
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Mixin(Array(classOf[LevelStorage]))
class LevelStorageMixin {
  @Inject(method = Array("getLevelList"), at = Array(new At("RETURN")), cancellable = true)
  def onListLevels(callback: CallbackInfoReturnable[util.List[LevelSummary]]): Unit = {
    println(s"There are ${callback.getReturnValue.size()} worlds.")
  }
}
