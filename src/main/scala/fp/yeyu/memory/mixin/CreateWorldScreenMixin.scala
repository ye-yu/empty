package fp.yeyu.memory.mixin

import fp.yeyu.memory.BackupListUtil
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.world.CreateWorldScreen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.LiteralText
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.{At, Inject}
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(Array(classOf[CreateWorldScreen]))
abstract class CreateWorldScreenMixin extends Screen(null) {
  //noinspection ScalaUnusedSymbol
  @Inject(method = Array("init"), at = Array(new At("TAIL")))
  def onInit(callbackInfo: CallbackInfo): Unit = {
    addButton(new ButtonWidget(this.width - 105, 2, 100, 20, new LiteralText("Show Backups"), BackupListUtil.onToggleWorld))
  }
}
