package fp.yeyu.memory

import java.io.{File, FileReader, FileWriter}
import java.nio.file.FileAlreadyExistsException

import net.fabricmc.api.ClientModInitializer
import org.apache.logging.log4j.{LogManager, Logger}

object MemoryMain extends ClientModInitializer {
  private val LOGGER: Logger = LogManager.getLogger()
  private val MOD_FOLDER = "./mods/Time Shifter"
  private val CONFIG_FILE = "config.txt"
  val CONFIG_FILE_INSTANCE = new File(MOD_FOLDER, CONFIG_FILE)
  val BACKUPS_FOLDER = new File("backups")
  private val DIR_IS_A_FILE_EX = new FileAlreadyExistsException(s"$MOD_FOLDER/$CONFIG_FILE")

  def writeState(bool: Boolean): Unit = {
    new FileWriter(CONFIG_FILE_INSTANCE) {
      this.write(if(bool) 1 else 0)
    }.close()
  }

  def createConfigFile(): Unit = {
    val MOD_FOLDER_INSTANCE = new File(MOD_FOLDER)
    if (!MOD_FOLDER_INSTANCE.exists() && MOD_FOLDER_INSTANCE.mkdirs()) LOGGER.info("First launch: Create recycle bin")
    else if (MOD_FOLDER_INSTANCE.isFile) throw DIR_IS_A_FILE_EX
    if (CONFIG_FILE_INSTANCE.exists()) return
    writeState(false)
  }

  override def onInitializeClient(): Unit = {
    LOGGER.info("Memory mod initialized")
    createConfigFile()
    new FileReader(CONFIG_FILE_INSTANCE) {
      BackupListUtil.toggleState = this.read() != 0
    }.close()
  }
}
