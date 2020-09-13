package fp.yeyu.memory

import java.io.File
import java.nio.file.FileAlreadyExistsException

import fp.yeyu.memory.MemoryMain._
import net.fabricmc.api.ClientModInitializer
import org.apache.logging.log4j.{LogManager, Logger}

class MemoryMain extends ClientModInitializer {
  private val DIR_IS_A_FILE_EX = new FileAlreadyExistsException(s"$MOD_FOLDER/$RECYCLE_BIN")

  def createRecycleBin(): Unit = {
    if (!RECYCLE_BIN_FILE.exists() && RECYCLE_BIN_FILE.mkdirs()) LOGGER.info("First launch: Create recycle bin")
    else if (RECYCLE_BIN_FILE.isFile) throw DIR_IS_A_FILE_EX
  }

  override def onInitializeClient(): Unit = {
    LOGGER.info("Memory mod initialized")
    createRecycleBin()
  }
}

object MemoryMain {
  @volatile var lock = false
  private val LOGGER: Logger = LogManager.getLogger()
  private val MOD_FOLDER = "./mods/Time Shifter"
  private val RECYCLE_BIN = "recyclebin"
  val RECYCLE_BIN_FILE = new File(MOD_FOLDER, RECYCLE_BIN)
}