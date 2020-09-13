package fp.yeyu.adapters

import net.fabricmc.loader.api.{LanguageAdapter, LanguageAdapterException, ModContainer}
import net.fabricmc.loader.launch.common.FabricLauncherBase

class ScalaAdapter extends LanguageAdapter {

  override def create[T](mod: ModContainer, value: String, clazz: Class[T]): T = {
    val classForName = (name: String) => Class.forName(name, true, FabricLauncherBase.getLauncher.getTargetClassLoader)
    val getMainClass = () => classForName(value)
      .getDeclaredConstructor()
      .newInstance()
      .asInstanceOf[T]

    // two 'try's?? bear with me
    // inner try is for object class not found
    // outer try is for casting exception for both object and class
    try {
      try {
        val instance = classForName(value + "$")
          .getField("MODULE$")
          .get()
        // object class is most likely a companion object
        if (!clazz.isInstance(instance)) getMainClass()
        else instance.asInstanceOf[T]
      } catch {
        case _: ClassNotFoundException => getMainClass()
      }
    } catch {
      case cce: ClassCastException => throw new LanguageAdapterException(cce)
    }
  }

}
