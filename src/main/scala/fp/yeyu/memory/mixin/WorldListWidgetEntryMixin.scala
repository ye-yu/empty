package fp.yeyu.memory.mixin

import java.text.SimpleDateFormat
import java.util.Date

import com.mojang.blaze3d.systems.RenderSystem
import fp.yeyu.memory.{BackupLevelSummary, MemoryMain}
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.gui.screen.world.SelectWorldScreen
import net.minecraft.client.resource.language.I18n
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralText
import net.minecraft.util.{Formatting, Identifier}
import net.minecraft.world.level.storage.LevelSummary
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.{At, Inject}
import org.spongepowered.asm.mixin.{Final, Mixin, Shadow}

@Mixin(targets = Array("net.minecraft.client.gui.screen.world.WorldListWidget$Entry"))
abstract class WorldListWidgetEntryMixin {

  @Shadow var screen: SelectWorldScreen = _

  @Shadow var iconLocation: Identifier = _

  @Shadow
  @Final val icon: NativeImageBackedTexture = null

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

  //noinspection ScalaDeprecation
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
      val levelCaption = new LiteralText("This level was deleted.")
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
}
