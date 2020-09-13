package fp.yeyu.memory

import java.io.File

import fp.yeyu.memory.mixin.LevelStorageMixin
import fp.yeyu.memory.mixinaccessor.LevelSummaryAccessor
import net.minecraft.world.level.LevelInfo
import net.minecraft.world.level.storage.{LevelSummary, SaveVersionInfo}

class BackupLevelSummary(levelInfo: LevelInfo, saveVersionInfo: SaveVersionInfo, name: String, requireConversion: Boolean, locked: Boolean, file: File)
  extends LevelSummary(levelInfo, saveVersionInfo, name, requireConversion, locked, file) {
}

object BackupLevelSummary {
  def copy(levelSummary: LevelSummary): BackupLevelSummary = {
    val levelInfo = levelSummary.asInstanceOf[LevelSummaryAccessor].getLevelInfo
    val saveVersionInfo = levelSummary.method_29586
    val name = levelSummary.getName
    val requireConversion = levelSummary.requiresConversion
    val locked = levelSummary.isLocked
    val file = levelSummary.getFile
    new BackupLevelSummary(levelInfo, saveVersionInfo, name, requireConversion, locked, file)
  }
}