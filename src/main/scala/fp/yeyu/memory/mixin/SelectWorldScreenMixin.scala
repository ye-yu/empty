package fp.yeyu.memory.mixin

import fp.yeyu.memory.{BackupLevelSummary, BackupListUtil, ConfirmRestoreScreen}
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.world.{SelectWorldScreen, WorldListWidget}
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.{LiteralText, TranslatableText}
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.{At, Inject}
import org.spongepowered.asm.mixin.{Mixin, Shadow}

@Mixin(Array(classOf[SelectWorldScreen]))
abstract class SelectWorldScreenMixin extends Screen(null) {

  @Shadow var deleteButton: ButtonWidget = _
  @Shadow var selectButton: ButtonWidget = _
  @Shadow var editButton: ButtonWidget = _
  @Shadow var recreateButton: ButtonWidget = _
  @Shadow var levelList: WorldListWidget = _

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

  //noinspection ScalaUnusedSymbol
  @Inject(method = Array("init"), at = Array(new At("TAIL")))
  def onInit(callbackInfo: CallbackInfo): Unit = {
    addButton(
      new ButtonWidget(this.width - 105,
        2,
        100,
        20,
        if (BackupListUtil.toggleState) BackupListUtil.hideBackups else BackupListUtil.showBackups,
        BackupListUtil.onToggleWorld))
  }
}
