package store.util

import java.io.File

object FileReader {
    fun loadFile(fileName: String): List<String> {
        val resource = javaClass.classLoader.getResource(fileName)
            ?: throw IllegalArgumentException("[ERROR] $fileName 파일을 찾을 수 없습니다.")

        return File(resource.toURI()).readLines()
            .drop(1)
            .filter { it.isNotBlank() }
    }
}
