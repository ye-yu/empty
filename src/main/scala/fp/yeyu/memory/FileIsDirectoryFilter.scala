package fp.yeyu.memory

import java.io.{File, FileFilter}

object FileIsDirectoryFilter extends FileFilter {
  override def accept(pathname: File): Boolean = pathname.isDirectory
}
