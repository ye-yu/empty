package fp.yeyu.memory

import java.io.File
import java.util.function.BiFunction

import com.mojang.datafixers.DataFixer
import com.mojang.serialization.Dynamic
import net.minecraft.SharedConstants
import net.minecraft.datafixer.DataFixTypes
import net.minecraft.nbt.{CompoundTag, NbtIo, NbtOps, Tag}
import net.minecraft.resource.DataPackSettings
import net.minecraft.world.level.LevelInfo
import net.minecraft.world.level.storage.{LevelSummary, SaveVersionInfo}
import org.apache.logging.log4j.LogManager

object LevelUtil {
  private val LOGGER = LogManager.getLogger()

  def createLevelFileParser(file: File, locked: Boolean): BiFunction[File, DataFixer, LevelSummary] =
    (file2: File, dataFixer: DataFixer) =>
      try {
        val compoundTag: CompoundTag = NbtIo.readCompressed(file2)
        val compoundTag2: CompoundTag = compoundTag.getCompound("Data")
        compoundTag2.remove("Player")
        val i: Int = if (compoundTag2.contains("DataVersion", 99)) {
          compoundTag2.getInt("DataVersion")
        }
        else -1
        val dynamic: Dynamic[Tag] = dataFixer.update(DataFixTypes.LEVEL.getTypeReference, new Dynamic[Tag](NbtOps.INSTANCE, compoundTag2), i, SharedConstants.getGameVersion.getWorldVersion)
        val saveVersionInfo: SaveVersionInfo = SaveVersionInfo.fromDynamic(dynamic)
        val j: Int = saveVersionInfo.getLevelFormatVersion
        if (j != 19132 && j != 19133) null
        else {
          val bl2: Boolean = j != 19133
          val file3: File = new File(file, "icon.png")
          val dataPackSettings: DataPackSettings = dynamic.get("DataPacks").result.map(e => applyCodec(e)).orElse(DataPackSettings.SAFE_MODE)
          val levelInfo: LevelInfo = LevelInfo.fromDynamic(dynamic, dataPackSettings)
          new LevelSummary(levelInfo, saveVersionInfo, file.getName, bl2, locked, file3)
        }
      } catch {
        case var15: Exception =>
          LOGGER.error("Exception reading {}", file2, var15)
          null
      }

  private def applyCodec[T](dynamic: Dynamic[T]) = {
    val codec = DataPackSettings.CODEC.parse(dynamic)
    codec.resultOrPartial(err => LOGGER.error(err)).orElse(DataPackSettings.SAFE_MODE)
  }

  def readLevelProperties[T](file: File, biFunction: BiFunction[File, DataFixer, T], dataFixer: DataFixer): Option[T] = if (!file.exists) null
  else {
    var file2 = new File(file, "level.dat")
    if (file2.exists) {
      val output = biFunction.apply(file2, dataFixer)
      if (output != null) return Option(output)
    }
    file2 = new File(file, "level.dat_old")
    if (file2.exists) Option(biFunction.apply(file2, dataFixer))
    else null
  }
}
