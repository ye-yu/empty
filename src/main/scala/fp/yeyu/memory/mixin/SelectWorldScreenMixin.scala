package fp.yeyu.memory.mixin

import fp.yeyu.memory.{BackupLevelSummary, ConfirmRestoreScreen, MemoryMain}
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.world.{SelectWorldScreen, WorldListWidget}
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.{LiteralText, TranslatableText}
import net.minecraft.world.level.storage.LevelSummary
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.LogManager
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.{At, Inject}
import org.spongepowered.asm.mixin.{Mixin, Shadow}

@Mixin(Array(classOf[SelectWorldScreen]))
class SelectWorldScreenMixin {
  @Shadow var levelList: WorldListWidget = _

  @Shadow var deleteButton: ButtonWidget = _
  @Shadow var selectButton: ButtonWidget = _
  @Shadow var editButton: ButtonWidget = _
  @Shadow var recreateButton: ButtonWidget = _

  //noinspection ScalaUnusedSymbol
  @Inject(method = Array("worldSelected"), at = Array(new At("RETURN")), cancellable = true)
  def onWorldSelected(active: Boolean, callbackInfo: CallbackInfo): Unit = {
    val recreateText = new TranslatableText("selectWorld.recreate")
    val restoreText = new LiteralText("Restore")
    if (!active) {
      this.recreateButton.setMessage(recreateText)
      return
    }
    if (!levelList.getSelected.asInstanceOf[WorldListWidgetEntryAccessor].getLevel.isInstanceOf[BackupLevelSummary]) {
      this.recreateButton.setMessage(recreateText)
      return
    }

    this.selectButton.active = false
    this.editButton.active = false
    this.deleteButton.active = true
    this.recreateButton.active = true

    this.recreateButton.setMessage(restoreText)
  }

  //noinspection ScalaUnusedSymbol
  @Inject(method = Array("method_19941"), at = Array(new At("INVOKE")), cancellable = true)
  def onRecreate(buttonWidget: ButtonWidget, callbackInfo: CallbackInfo): Unit = {
    val level = levelList.getSelected.asInstanceOf[WorldListWidgetEntryAccessor].getLevel
    if (!level.isInstanceOf[BackupLevelSummary]) return
    MinecraftClient.getInstance().openScreen(new ConfirmRestoreScreen(level.asInstanceOf[BackupLevelSummary], this.asInstanceOf[Object].asInstanceOf[SelectWorldScreen]))
    callbackInfo.cancel()
  }
}
