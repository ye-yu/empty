package fp.yeyu.memory

import java.io.File
import java.nio.file.Path

import fp.yeyu.memory.mixin.SelectWorldScreenAccessor
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ConfirmScreen
import net.minecraft.client.gui.screen.world.SelectWorldScreen
import net.minecraft.client.toast.SystemToast
import net.minecraft.text.LiteralText
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.LogManager

import scala.annotation.tailrec

class ConfirmRestoreScreen(level: BackupLevelSummary, selectWorldScreen: SelectWorldScreen) extends ConfirmScreen(
  confirm => { ConfirmRestoreScreen.onRestore(confirm, level, selectWorldScreen) },
  new LiteralText(s"Restore '${level.getDisplayName}'?"),
  new LiteralText(""),
  new LiteralText("Restore"),
  new LiteralText("Cancel")) {

}

object ConfirmRestoreScreen {

  private val LOGGER = LogManager.getLogger

  def onRestore(confirm: Boolean, level: BackupLevelSummary, selectWorldScreen: SelectWorldScreen): Unit = {
    if (!confirm) {
      val levelList = selectWorldScreen.asInstanceOf[SelectWorldScreenAccessor].getLevelList
      levelList.filter(() => selectWorldScreen.asInstanceOf[SelectWorldScreenAccessor].getSearchBox.getText, true)
      MinecraftClient.getInstance().openScreen(selectWorldScreen)
      return
    }
    try {
      val worldDirectory = MinecraftClient.getInstance().getLevelStorage.getSavesDirectory
      val saveDirectory = resolveTargetName(worldDirectory, level.getName)
      FileUtils.copyDirectory(level.directory, saveDirectory)
      SystemToast.add(MinecraftClient.getInstance().getToastManager, SystemToast.Type.WORLD_BACKUP, new LiteralText(s"Restored ${level.getName}"), null)
      LOGGER.info(s"Restored ${level.getName}")
    } catch {
      case _: Throwable =>
        LOGGER.error(s"Failed to restore ${level.getName}")
        SystemToast.add(MinecraftClient.getInstance().getToastManager, SystemToast.Type.WORLD_ACCESS_FAILURE, new LiteralText(s"Failed to restore ${level.getName}"), null)
    }

    FileUtils.deleteDirectory(level.directory)
    val levelList = selectWorldScreen.asInstanceOf[SelectWorldScreenAccessor].getLevelList
    levelList.filter(() => selectWorldScreen.asInstanceOf[SelectWorldScreenAccessor].getSearchBox.getText, true)
    MinecraftClient.getInstance().openScreen(selectWorldScreen)
  }

  @tailrec
  def resolveTargetName(directory: Path, levelName: String, index: Int = 0): File = {
    val candidateDirectory = if (index == -1) directory.resolve(levelName).toFile
    else directory.resolve(levelName + "-" + index).toFile
    if (candidateDirectory.exists()) resolveTargetName(directory, levelName, index + 1)
    else candidateDirectory
  }

}