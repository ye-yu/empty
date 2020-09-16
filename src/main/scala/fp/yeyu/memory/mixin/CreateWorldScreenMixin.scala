package fp.yeyu.memory.mixin

import fp.yeyu.memory.BackupListUtil
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.world.CreateWorldScreen
import net.minecraft.client.gui.widget.ButtonWidget
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.{At, Inject}

@Mixin(Array(classOf[CreateWorldScreen]))
abstract class CreateWorldScreenMixin extends Screen(null) {
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
