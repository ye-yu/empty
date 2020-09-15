package fp.yeyu.memory

import fp.yeyu.memory.mixin.SelectWorldScreenAccessor
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.TitleScreen
import net.minecraft.client.gui.screen.world.{CreateWorldScreen, SelectWorldScreen}
import net.minecraft.client.gui.widget.ButtonWidget

object BackupListUtil {

  // hide or not
  var toggleState = false

  //noinspection ScalaUnusedSymbol
  def onToggleWorld(buttonWidget: ButtonWidget): Unit = {
    val screen = MinecraftClient.getInstance().currentScreen
    if (screen == null) return
    if (!screen.isInstanceOf[SelectWorldScreen] && !screen.isInstanceOf[CreateWorldScreen]) return
    toggleState = !toggleState
    MemoryMain.writeState(toggleState)

    if (screen.isInstanceOf[SelectWorldScreen]) {
      val worldScreen = screen.asInstanceOf[SelectWorldScreenAccessor]
      worldScreen.getLevelList.filter(() => worldScreen.getSearchBox.getText, true)
      return
    }

    if (screen.isInstanceOf[CreateWorldScreen]) {
      MinecraftClient.getInstance().openScreen(new SelectWorldScreen(new TitleScreen))
    }
  }

}
