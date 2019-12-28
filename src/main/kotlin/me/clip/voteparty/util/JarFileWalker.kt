package me.clip.voteparty.util

import java.io.InputStream
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

/**
 * Walks the "directory" inside the actual jar
 */
object JarFileWalker
{

    fun walk(path: String, function: (Path, InputStream?) -> Unit)
    {
        FileSystems.newFileSystem(javaClass.getResource(path).toURI(), emptyMap<String, Any>()).use()
        { files ->
            Files.walk(files.getPath(path)).forEach()
            { path ->
                if (Files.isDirectory(path))
                {
                    return@forEach // do nothing if this is a directory
                }

                try
                {
                    // attempt to pass the stream for this resource
                    function.invoke(path, javaClass.classLoader.getResourceAsStream(path.toString().drop(1)))
                } catch (ex: Exception)
                {
                    // fallback to just the path
                    function.invoke(path, null)
                }
            }
        }
    }

}