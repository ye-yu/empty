package fp.yeyu.memory.mixin

import java.io.File
import java.nio.file.Path

import fp.yeyu.memory.{BackupListUtil, ConfirmRestoreScreen, FileIsDirectoryFilter, MemoryMain}
import net.minecraft.util.WorldSavePath
import net.minecraft.world.level.storage.LevelStorage
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.LogManager
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import org.spongepowered.asm.mixin.injection.{At, Inject}
import org.spongepowered.asm.mixin.{Mixin, Shadow}

@Mixin(Array(classOf[LevelStorage#Session]))
abstract class LevelStorageSessionMixin {

  @Shadow def getDirectory(worldSavePath: WorldSavePath): Path

  @Shadow def getDirectoryName: String

  def parseSplitName(name: String): String = {
    val lastDelimAt = name.lastIndexOf('-')
    if (lastDelimAt == 0) return name
    val intCandidate = name.substring(name.lastIndexOf('-') + 1)
    try {
      intCandidate.toInt
      name.substring(0, lastDelimAt)
    } catch {
      case _: NumberFormatException => name
    }
  }

  @Inject(method = Array("createBackup"), at = Array(new At("HEAD")), cancellable = true)
  def onCreateBackup(callbackInfoReturnable: CallbackInfoReturnable[Long]): Unit = {
    val logger = LogManager.getLogger
    try {
      val directory = getDirectory(WorldSavePath.ROOT)
      logger.info(s"About to copy $directory")
      val saveDirectory = ConfirmRestoreScreen.resolveTargetName(MemoryMain.BACKUPS_FOLDER.toPath, parseSplitName(getDirectoryName))
      FileUtils.copyDirectory(directory.toFile, saveDirectory)
      val sessionLock = new File(saveDirectory, "session.lock")
      if (sessionLock.exists() && !sessionLock.delete()) logger.error("Cannot delete session.lock!")

      val backups = MemoryMain.BACKUPS_FOLDER.listFiles(FileIsDirectoryFilter)
      if (backups == null || backups.isEmpty || backups.length < 15) return
      val oldest = BackupListUtil.findOldestLevel(backups.iterator)
      if (oldest == null) return
      val size = FileUtils.sizeOfDirectory(directory.toFile)
      FileUtils.deleteDirectory(oldest)
      callbackInfoReturnable.setReturnValue(size)
    } catch {
      case throwable: Throwable =>
        logger.error(s"Cannot copy world. Resorting to vanilla backup", throwable)
    }
  }

}
