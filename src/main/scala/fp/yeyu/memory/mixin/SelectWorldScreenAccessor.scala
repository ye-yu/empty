package fp.yeyu.memory.mixin

import net.minecraft.client.gui.screen.world.{SelectWorldScreen, WorldListWidget}
import net.minecraft.client.gui.widget.TextFieldWidget
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(Array(classOf[SelectWorldScreen]))
trait SelectWorldScreenAccessor {
  @Accessor
  def getField_3218: WorldListWidget

  @Accessor
  def getField_3220: TextFieldWidget
}
