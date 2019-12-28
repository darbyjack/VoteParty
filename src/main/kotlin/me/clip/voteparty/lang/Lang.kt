package me.clip.voteparty.lang

import me.clip.voteparty.util.JarFileWalker
import java.io.File

class Lang
{
	
	fun save(dir: File) = JarFileWalker.walk("/languages")
	{ path, stream ->
		
		if (stream == null)
		{
			return@walk // do nothing if the stream couldn't be opened
		}
		
		val file = dir.resolve(path.toString().drop(1)).absoluteFile
		if (file.exists())
		{
			return@walk // language file was already created
		}
		
		file.parentFile.mkdirs()
		file.createNewFile()
		
		file.outputStream().use()
		{
			stream.copyTo(it)
			stream.close()
		} // save file from jar to disk
		
	}
	
	
}