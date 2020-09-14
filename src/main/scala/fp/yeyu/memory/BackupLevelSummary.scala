package fp.yeyu.memory

import java.io.File

import fp.yeyu.memory.mixin.LevelSummaryAccessor
import net.minecraft.world.level.LevelInfo
import net.minecraft.world.level.storage.{LevelSummary, SaveVersionInfo}

class BackupLevelSummary(levelInfo: LevelInfo, saveVersionInfo: SaveVersionInfo, name: String, requireConversion: Boolean, locked: Boolean, icon: File, val directory: File = null)
  extends LevelSummary(levelInfo, saveVersionInfo, name, requireConversion, locked, icon) {
}

object BackupLevelSummary {
  @Deprecated
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