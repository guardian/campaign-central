package util

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

import org.apache.commons.io.IOUtils


object Compression {

  def compress(data: String): Array[Byte] = {
    val arrOutputStream = new ByteArrayOutputStream()
    val zipOutputStream = new GZIPOutputStream(arrOutputStream)
    zipOutputStream.write(data.getBytes)
    zipOutputStream.close()
    val bytes = arrOutputStream.toByteArray
    arrOutputStream.close()
    bytes
  }

  def decompress(compressed: Array[Byte]): String = {
    val bis = new ByteArrayInputStream(compressed)
    val gis = new GZIPInputStream(bis)
    val bytes = IOUtils.toByteArray(gis)
    return new String(bytes, "UTF-8")
  }

}
