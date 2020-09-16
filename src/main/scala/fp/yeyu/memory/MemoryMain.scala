package fp.yeyu.memory

import java.io.{File, FileReader, FileWriter}
import java.nio.file.FileAlreadyExistsException

import net.fabricmc.api.ClientModInitializer
import org.apache.logging.log4j.{LogManager, Logger}

object MemoryMain extends ClientModInitializer {
  private val LOGGER: Logger = LogManager.getLogger()
  private val MOD_FOLDER = "./mods/pdmod"
  private val CONFIG_FILE = "config.txt"
  val BACKUPS_FOLDER = new File("backups")
  val CONFIG_FILE_INSTANCE = new File(MOD_FOLDER, CONFIG_FILE)
  private val DIR_IS_A_FILE_EX = new FileAlreadyExistsException(s"$MOD_FOLDER/$CONFIG_FILE")

  override def onInitializeClient(): Unit = {
    createConfigFile()
    new FileReader(CONFIG_FILE_INSTANCE) {
      BackupListUtil.toggleState = this.read() != 0
    }.close()
  }

  def createConfigFile(): Unit = {
    val MOD_FOLDER_INSTANCE = new File(MOD_FOLDER)
    if (!MOD_FOLDER_INSTANCE.exists() && MOD_FOLDER_INSTANCE.mkdirs()) LOGGER.debug("First launch: Created mod folder")
    else if (MOD_FOLDER_INSTANCE.isFile) throw DIR_IS_A_FILE_EX
    if (CONFIG_FILE_INSTANCE.exists()) return
    writeState(false)
  }

  def writeState(bool: Boolean): Unit = {
    new FileWriter(CONFIG_FILE_INSTANCE) {
      this.write(if (bool) 1 else 0)
    }.close()
  }
}
