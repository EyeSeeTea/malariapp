package org.eyeseetea.malariacare.common

import org.eyeseetea.malariacare.data.file.IFileReader
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader

class ResourcesFileReader : IFileReader {
    @Throws(IOException::class)
    override fun getStringFromFile(filename: String): String {
        val inputStream =
            FileInputStream(getFile(javaClass, filename))
        val isr = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(isr)
        val sb = StringBuilder()
        var line: String?
        while (bufferedReader.readLine().also { line = it } != null) {
            sb.append(line)
        }
        return sb.toString()
    }

    companion object {
        private fun getFile(clazz: Class<*>, filename: String): File {
            val classLoader = clazz.classLoader
            val resource = classLoader!!.getResource(filename)
            return File(resource.path)
        }
    }
}
