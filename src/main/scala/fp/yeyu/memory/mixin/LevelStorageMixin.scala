package fp.yeyu.memory.mixin

import java.io.File
import java.nio.file.Path
import java.util

import com.mojang.datafixers.DataFixer
import com.mojang.serialization.Dynamic
import fp.yeyu.memory.BackupLevelSummary
import net.minecraft.SharedConstants
import net.minecraft.client.gui.screen.world.WorldListWidget
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
    val levelList = callback.getReturnValue
    val files = backupsDirectory.toFile.listFiles
    if (files == null) return
    val size = files.length

    repeat(i => {
      val file = files(i)
      if (file.isDirectory) {
        val nbt = NbtIo.readCompressed(new File(file, "level.dat")).getCompound("Data")
        nbt.remove("Player")
        val dataVersion = if (nbt.contains("DataVersion", 99)) nbt.getInt("DataVersion") else -1

        if (SharedConstants.getGameVersion.getWorldVersion == dataVersion) {
          val dynamic = new Dynamic(NbtOps.INSTANCE, nbt)
          val saveVersionInfo = SaveVersionInfo.fromDynamic(dynamic)
          val requiresConversion = saveVersionInfo.getVersionId != 19133
          val iconFile = new File(file, "icon.png")
          val levelInfo = LevelInfo.fromDynamic(dynamic, DataPackSettings.SAFE_MODE)
          val levelSummary = new BackupLevelSummary(levelInfo, saveVersionInfo, file.getName, requiresConversion, true, iconFile)
          levelList.add(levelSummary)
        }
      }
    }, size)
  }

  @tailrec
  final def repeat(intConsumer: Int => Unit, count: Int, start: Int = 0): Unit = {
    intConsumer(start)
    if (start < count - 1) repeat(intConsumer, count, start + 1)
  }
}
