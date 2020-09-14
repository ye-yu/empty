package fp.yeyu.memory.mixin

import net.minecraft.client.gui.screen.world.WorldListWidget
import net.minecraft.world.level.storage.LevelSummary
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(Array(classOf[WorldListWidget#Entry]))
trait WorldListWidgetEntryAccessor {
  @Accessor
  def getLevel: LevelSummary
}
