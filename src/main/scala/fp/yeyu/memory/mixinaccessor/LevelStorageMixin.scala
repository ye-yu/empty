package fp.yeyu.memory.mixinaccessor

import java.nio.file.Path
import java.util

import com.mojang.datafixers.DataFixer
import fp.yeyu.memory.{BackupLevelSummary, LevelUtil}
import net.minecraft.world.level.storage.LevelSummary
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import org.spongepowered.asm.mixin.injection.{At, Inject}
import org.spongepowered.asm.mixin.{Final, Mixin, Shadow}

/**
 * KIV: Why cannot access?
 * */
@Mixin(targets = Array("net.minecraft.world.level.storage.LevelStorage"))
abstract class LevelStorageMixin {

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

    for (i <- 0 until size) {
      val file = files(i)
      if (file.isDirectory) {
        val dataParser = LevelUtil.createLevelFileParser(file, locked = false)
        val levelSummary = LevelUtil.readLevelProperties[LevelSummary](file, (f, d) => dataParser(f, d), dataFixer)
        if (levelSummary != null || levelSummary.nonEmpty) {
          levelList.add(BackupLevelSummary.copy(levelSummary.get))
        }
      }
    }
  }
}
