package fp.yeyu.memory.mixin

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

import com.mojang.blaze3d.systems.RenderSystem
import fp.yeyu.memory.{BackupLevelSummary, ConfirmRestoreScreen, LevelUtil, MemoryMain}
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.gui.screen.world.{EditWorldScreen, SelectWorldScreen, WorldListWidget}
import net.minecraft.client.resource.language.I18n
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralText
import net.minecraft.util.{Formatting, Identifier, Util}
import net.minecraft.world.level.storage.LevelSummary
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.spongepowered.asm.mixin.injection.callback.{CallbackInfo, CallbackInfoReturnable}
import org.spongepowered.asm.mixin.injection.{At, Inject}
import org.spongepowered.asm.mixin.{Final, Mixin, Shadow}

@Mixin(Array(classOf[WorldListWidget#Entry]))
abstract class WorldListWidgetEntryMixin {

  @Shadow var screen: SelectWorldScreen = _

  @Shadow var iconLocation: Identifier = _

  @Shadow var time: Long = _

  @Shadow
  @Final val icon: NativeImageBackedTexture = null

  @Shadow
  @Final val level: LevelSummary = null


  //noinspection ScalaUnusedSymbol
  @Inject(method = Array("method_20170"), at = Array(new At("HEAD")), cancellable = true)
  def onDelete(bool: Boolean, callback: CallbackInfo): Unit = {
    val logger = LogManager.getLogger
    if (!bool) return

    level match {
      case summary: BackupLevelSummary =>
        FileUtils.deleteDirectory(summary.directory)
        val levelList = screen.asInstanceOf[SelectWorldScreenAccessor].getLevelList
        val searchBox = screen.asInstanceOf[SelectWorldScreenAccessor].getSearchBox
        levelList.filter(LevelUtil.createTextSupplier(searchBox), true)
        MinecraftClient.getInstance().openScreen(screen)
        callback.cancel()
      case _ =>
        try {
          val directory = MinecraftClient.getInstance().getLevelStorage.getSavesDirectory.resolve(level.getName)
          logger.info(s"About to copy $directory")
          val saveDirectory = ConfirmRestoreScreen.resolveTargetName(MemoryMain.BACKUPS_FOLDER.toPath, level.getName)
          FileUtils.moveDirectory(directory.toFile, saveDirectory)
          val sessionLock = new File(saveDirectory, "session.lock")
          if (sessionLock.exists() && !sessionLock.delete()) logger.error("Cannot delete session.lock!")

          val backups = MemoryMain.BACKUPS_FOLDER.listFiles((f: File) => f.isDirectory)

          if (backups == null || backups.isEmpty || backups.length < 15) return
          val oldest = findOldestLevel(backups.iterator)
          if (oldest == null) return
          FileUtils.deleteDirectory(oldest)
        } catch {
          case throwable: Throwable =>
            logger.error(s"Cannot copy world. Resolving by vanilla backup", throwable)
            val session = MinecraftClient.getInstance().getLevelStorage.createSession(level.getName)
            EditWorldScreen.backupLevel(session)
            session.close()
        }
    }
  }

  private def findOldestLevel(dir: Iterator[File], oldest: File = null): File = {
    if (!dir.hasNext) return oldest
    if (oldest == null) return findOldestLevel(dir, dir.next())
    val levelDir = dir.next()
    val levelDat = new File(levelDir, "level.dat")
    if (!levelDat.exists()) return findOldestLevel(dir, oldest)
    if (new File(oldest, "level.dat").lastModified() > levelDat.lastModified()) return findOldestLevel(dir, levelDir)
    findOldestLevel(dir, oldest)
  }

  //noinspection ScalaDeprecation,ScalaUnusedSymbol
  @Inject(method = Array("render"), at = Array(new At("INVOKE")), cancellable = true)
  def onRender(matrices: MatrixStack,
               index: Int,
               y: Int,
               x: Int,
               entryWidth: Int,
               entryHeight: Int,
               mouseX: Int,
               mouseY: Int,
               hovered: Boolean,
               tickDelta: Float,
               callbackInfo: CallbackInfo): Unit = {
    if (level.isInstanceOf[BackupLevelSummary]) {
      val DATE_FORMAT = new SimpleDateFormat
      val client = MinecraftClient.getInstance()
      val UNKNOWN_SERVER_LOCATION = new Identifier("textures/misc/unknown_server.png")
      val WORLD_SELECTION_LOCATION = new Identifier("textures/gui/world_selection.png")

      // statics
      val restoreMessage = new LiteralText("You can restore this world").formatted(Formatting.YELLOW)

      var displayName = this.level.getDisplayName
      val date = this.level.getName + " (" + DATE_FORMAT.format(new Date(this.level.getLastPlayed)) + ")"
      val levelCaption = new LiteralText("This world was deleted.")
      if (StringUtils.isEmpty(displayName)) displayName = I18n.translate("selectWorld.world") + " " + (index + 1)

      val textRenderer = client.textRenderer
      val captionX = (x + 32 + 3).toFloat

      val nameCaptionY = (y + 1).toFloat
      textRenderer.draw(matrices, displayName, captionX, nameCaptionY, 0x999966)

      val dateCaptionY = nameCaptionY + 12
      textRenderer.draw(matrices, date, captionX, dateCaptionY, 0x999966)

      val levelCaptionY = dateCaptionY + 10
      textRenderer.draw(matrices, levelCaption, captionX, levelCaptionY.toFloat, 0x999966)

      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F)

      client.getTextureManager.bindTexture(if (icon != null) iconLocation else UNKNOWN_SERVER_LOCATION)

      RenderSystem.enableBlend()
      DrawableHelper.drawTexture(matrices, x, y, 0.0F, 0.0F, 32, 32, 32, 32)
      RenderSystem.disableBlend()

      if (client.options.touchscreen || hovered) {
        client.getTextureManager.bindTexture(WORLD_SELECTION_LOCATION)
        DrawableHelper.fill(matrices, x, y, x + 32, y + 32, -1601138544)
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F)
        val i = mouseX - x
        val bl = i < 32
        val j = if (bl) 32
        else 0
        DrawableHelper.drawTexture(matrices, x, y, 96.0F, j.toFloat, 32, 32, 256, 256)
        if (bl) this.screen.setTooltip(client.textRenderer.wrapLines(restoreMessage, 175))
      }
      callbackInfo.cancel()
    }
  }

  //noinspection ScalaUnusedSymbol
  @Inject(method = Array("mouseClicked"), at = Array(new At("HEAD")), cancellable = true)
  def onMouseClicked(mouseX: Double, mouseY: Double, button: Int, callbackInfo: CallbackInfoReturnable[Boolean]): Unit = {
    if (!level.isInstanceOf[BackupLevelSummary]) return
    if (MinecraftClient.getInstance().currentScreen == null) return
    val thisWorldListWidget = MinecraftClient.getInstance().currentScreen.asInstanceOf[SelectWorldScreenAccessor].getLevelList
    thisWorldListWidget.setSelected(this.asInstanceOf[Object].asInstanceOf[WorldListWidget#Entry])
    this.screen.worldSelected(thisWorldListWidget.method_20159.isPresent)
    if (Util.getMeasuringTimeMs - this.time < 250L) {
      LevelUtil.restoreWorld(level)
      callbackInfo.setReturnValue(true)
    }
    else {
      this.time = Util.getMeasuringTimeMs
      callbackInfo.setReturnValue(true)
    }
    callbackInfo.setReturnValue(true)
  }
}
