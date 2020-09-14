package fp.yeyu.memory.mixin

import net.minecraft.client.gui.screen.world.{SelectWorldScreen, WorldListWidget}
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(Array(classOf[SelectWorldScreen]))
trait SelectWorldScreenAccessor {
  @Accessor
  def getLevelList: WorldListWidget
}
