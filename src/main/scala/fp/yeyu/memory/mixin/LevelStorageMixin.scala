package fp.yeyu.memory.mixin

import java.io.{File, FileInputStream}
import java.nio.file.Path
import java.util

import fp.yeyu.memory.{BackupLevelSummary, BackupListUtil}
import net.minecraft.nbt.NbtIo
import net.minecraft.resource.DataPackSettings
import net.minecraft.world.level.LevelInfo
import net.minecraft.world.level.storage.{LevelStorage, LevelSummary, SaveVersionInfo}
import net.minecraft.world.{Difficulty, GameMode, GameRules}
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import org.spongepowered.asm.mixin.injection.{At, Inject}
import org.spongepowered.asm.mixin.{Final, Mixin, Shadow}

import scala.annotation.tailrec

@Mixin(Array(classOf[LevelStorage]))
class LevelStorageMixin {

  @Shadow
  @Final val backupsDirectory: Path = null

  @Inject(method = Array("getLevelList"), at = Array(new At("RETURN")), cancellable = true)
  def onListLevels(callback: CallbackInfoReturnable[util.List[LevelSummary]]): Unit = {
    if (!BackupListUtil.toggleState) return
    val levelList = callback.getReturnValue
    val files = backupsDirectory.toFile.listFiles
    if (files == null) return
    val size = files.length
    appendBackupLevel(levelList, files, size)
  }

  @tailrec
  final def appendBackupLevel(levelList: util.List[LevelSummary], files: Array[File], count: Int, value: Int = 0): Unit = {
    if (value >= count) return
    val backupDirectory = files(value)
    if (backupDirectory.isDirectory) {
      val nbt = NbtIo.readCompressed(new FileInputStream(new File(backupDirectory, "level.dat"))).getCompound("Data")
      nbt.remove("Player")
      val lastPlayed = if (nbt.contains("LastPlayed")) nbt.getLong("LastPlayed") else 0L
      val saveVersionInfo = new SaveVersionInfo(-1, lastPlayed, "", -1, false)
      val name = if (nbt.contains("LevelName")) nbt.getString("LevelName") else "(Deleted World)"
      val gameMode = GameMode.byId(if (nbt.contains("GameType")) nbt.getInt("GameType") else -1)
      val levelInfo = new LevelInfo(name, gameMode, false, Difficulty.EASY, false, new GameRules(), DataPackSettings.SAFE_MODE)
      val iconFile = new File(backupDirectory, "icon.png")
      val levelSummary = new BackupLevelSummary(levelInfo, saveVersionInfo, backupDirectory.getName, true, true, iconFile, backupDirectory)
      levelList.add(levelSummary)
    }
    appendBackupLevel(levelList, files, count, value + 1)
  }
}
