package fp.yeyu.memory.mixin

import fp.yeyu.memory.MemoryMain
import net.minecraft.client.MinecraftClient
import net.minecraft.world.level.storage.LevelSummary
import org.apache.commons.io.FileUtils
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.{At, Inject}
import org.spongepowered.asm.mixin.{Final, Mixin, Shadow}

@Mixin(targets = Array("net.minecraft.client.gui.screen.world.WorldListWidget$Entry"))
abstract class LevelStorageSessionMixin {

  @Shadow
  @Final val level: LevelSummary = null

  @Inject(method = Array("method_20170"), at = Array(new At("HEAD")), cancellable = true)
  def onDelete(bool: Boolean, callback: CallbackInfo): Unit = {
    if (!bool) return
    val directory = MinecraftClient.getInstance().getLevelStorage.getSavesDirectory.resolve(level.getName)
    println(s"About to copy $directory")
    if (MemoryMain.lock) return
    MemoryMain.lock = true
    val saveDirectory = directory.getName(directory.getNameCount - 1)
    var i = 0
    while (MemoryMain.RECYCLE_BIN_FILE.toPath.resolve(s"$saveDirectory-$i").toFile.exists()) i = i + 1
    FileUtils.copyDirectory(directory.toFile, MemoryMain.RECYCLE_BIN_FILE.toPath.resolve(s"$saveDirectory-$i").toFile)
    MemoryMain.lock = false
  }
}
