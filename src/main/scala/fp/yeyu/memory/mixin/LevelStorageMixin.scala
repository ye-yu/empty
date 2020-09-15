package fp.yeyu.memory.mixin

import java.io.File
import java.nio.file.Path
import java.util

import com.mojang.datafixers.DataFixer
import com.mojang.serialization.Dynamic
import fp.yeyu.memory.{BackupLevelSummary, BackupListUtil}
import net.minecraft.SharedConstants
import net.minecraft.nbt.{NbtIo, NbtOps}
import net.minecraft.resource.DataPackSettings
import net.minecraft.world.level.LevelInfo
import net.minecraft.world.level.storage.{LevelStorage, LevelSummary, SaveVersionInfo}
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import org.spongepowered.asm.mixin.injection.{At, Inject}
import org.spongepowered.asm.mixin.{Final, Mixin, Shadow}

import scala.annotation.tailrec

@Mixin(Array(classOf[LevelStorage]))
class LevelStorageMixin {

  @Shadow
  @Final val dataFixer: DataFixer = null

  @Shadow
  @Final val backupsDirectory: Path = null

  @Inject(method = Array("getLevelList"), at = Array(new At("RETURN")), cancellable = true)
  def onListLevels(callback: CallbackInfoReturnable[util.List[LevelSummary]]): Unit = {
    if (!BackupListUtil.toggleState) return
    val levelList = callback.getReturnValue
    val files = backupsDirectory.toFile.listFiles
    if (files == null) return
    val size = files.length

    repeat(i => {
      val backupDirectory = files(i)
      if (backupDirectory.isDirectory) {
        val nbt = NbtIo.readCompressed(new File(backupDirectory, "level.dat")).getCompound("Data")
        nbt.remove("Player")
        val dataVersion = if (nbt.contains("DataVersion", 99)) nbt.getInt("DataVersion") else -1

        if (SharedConstants.getGameVersion.getWorldVersion == dataVersion) {
          val dynamic = new Dynamic(NbtOps.INSTANCE, nbt)
          val saveVersionInfo = SaveVersionInfo.fromDynamic(dynamic)
          val requiresConversion = saveVersionInfo.getVersionId != 19133
          val iconFile = new File(backupDirectory, "icon.png")
          val levelInfo = LevelInfo.fromDynamic(dynamic, DataPackSettings.SAFE_MODE)
          val levelSummary = new BackupLevelSummary(levelInfo, saveVersionInfo, backupDirectory.getName, requiresConversion, true, iconFile, backupDirectory)
          levelList.add(levelSummary)
        }
      }
    }, size)
  }

  @tailrec
  final def repeat(intConsumer: Int => Unit, count: Int, start: Int = 0): Unit = {
    if (start < count) {
      intConsumer(start)
      repeat(intConsumer, count, start + 1)
    }
  }
}
