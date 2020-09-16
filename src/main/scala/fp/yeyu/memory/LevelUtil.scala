package fp.yeyu.memory

import java.util.function.Supplier

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents
import net.minecraft.world.level.storage.LevelSummary

object LevelUtil {
  def createTextSupplier(searchBox: TextFieldWidget): Supplier[String] = () => searchBox.getText

  def restoreWorld(level: LevelSummary): Unit = {
    println(s"Restoring level $level")
    MinecraftClient.getInstance().getSoundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))
  }
}
