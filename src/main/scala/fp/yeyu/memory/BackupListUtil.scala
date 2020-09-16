package fp.yeyu.memory

import java.io.File

import fp.yeyu.memory.mixin.SelectWorldScreenAccessor
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.TitleScreen
import net.minecraft.client.gui.screen.world.{CreateWorldScreen, SelectWorldScreen}
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.LiteralText

object BackupListUtil {

  val showBackups = new LiteralText("Show Backups")
  val hideBackups = new LiteralText("Hide Backups")
  // hide or not
  var toggleState = false

  //noinspection ScalaUnusedSymbol
  def onToggleWorld(buttonWidget: ButtonWidget): Unit = {
    val screen = MinecraftClient.getInstance().currentScreen
    if (screen == null) return
    if (!screen.isInstanceOf[SelectWorldScreen] && !screen.isInstanceOf[CreateWorldScreen]) return
    toggleState = !toggleState
    MemoryMain.writeState(toggleState)
    buttonWidget.setMessage(if (toggleState) hideBackups else showBackups)

    if (screen.isInstanceOf[SelectWorldScreen]) {
      val worldScreen = screen.asInstanceOf[SelectWorldScreenAccessor]
      worldScreen.getField_3218.filter(() => worldScreen.getField_3220.getText, true)
      return
    }

    if (screen.isInstanceOf[CreateWorldScreen]) {
      MinecraftClient.getInstance().openScreen(new SelectWorldScreen(new TitleScreen))
    }
  }

  def findOldestLevel(dir: Iterator[File], oldest: File = null): File = {
    if (!dir.hasNext) return oldest
    if (oldest == null) return findOldestLevel(dir, dir.next())
    val levelDir = dir.next()
    val levelDat = new File(levelDir, "level.dat")
    if (!levelDat.exists()) return findOldestLevel(dir, oldest)
    if (new File(oldest, "level.dat").lastModified() > levelDat.lastModified()) return findOldestLevel(dir, levelDir)
    findOldestLevel(dir, oldest)
  }
}
