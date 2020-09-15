package fp.yeyu.memory

import java.io.File

import net.minecraft.world.level.LevelInfo
import net.minecraft.world.level.storage.{LevelSummary, SaveVersionInfo}

class BackupLevelSummary(levelInfo: LevelInfo, saveVersionInfo: SaveVersionInfo, name: String, requireConversion: Boolean, locked: Boolean, icon: File, val directory: File = null)
  extends LevelSummary(levelInfo, saveVersionInfo, name, requireConversion, locked, icon) {
}
