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

  @Shadow var field_3219: ButtonWidget = _
  @Shadow var field_3224: ButtonWidget = _
  @Shadow var field_3215: ButtonWidget = _
  @Shadow var field_3216: ButtonWidget = _
  @Shadow var field_3218: WorldListWidget = _

  //noinspection ScalaUnusedSymbol
  @Inject(method = Array("method_19940"), at = Array(new At("RETURN")), cancellable = true)
  def onWorldSelected(active: Boolean, callbackInfo: CallbackInfo): Unit = {
    val recreateText = new TranslatableText("selectWorld.recreate")
    val restoreText = new LiteralText("Restore")
    if (!active) {
      this.field_3216.setMessage(recreateText)
      return
    }

    if (!field_3218.getSelected.asInstanceOf[WorldListWidgetEntryAccessor].getField_19138.isInstanceOf[BackupLevelSummary]) {
      this.field_3216.setMessage(recreateText)
      return
    }

    this.field_3224.active = false
    this.field_3215.active = false
    this.field_3219.active = true
    this.field_3216.active = true

    this.field_3216.setMessage(restoreText)
  }

  //noinspection ScalaUnusedSymbol
  @Inject(method = Array("method_19941"), at = Array(new At("INVOKE")), cancellable = true)
  def onRecreate(buttonWidget: ButtonWidget, callbackInfo: CallbackInfo): Unit = {
    val level = field_3218.getSelected.asInstanceOf[WorldListWidgetEntryAccessor].getField_19138
    if (!level.isInstanceOf[BackupLevelSummary]) return
    MinecraftClient.getInstance().openScreen(new ConfirmRestoreScreen(level.asInstanceOf[BackupLevelSummary], this.asInstanceOf[Object].asInstanceOf[SelectWorldScreen]))
    callbackInfo.cancel()
  }

  //noinspection ScalaUnusedSymbol
  @Inject(method = Array("method_25426"), at = Array(new At("TAIL")))
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
