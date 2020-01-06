package me.clip.voteparty.conf.base

import java.lang.reflect.Modifier

interface Config
{
	
	fun merge(inst: Config)
	{
		val thisClazz = this::class.java
		
		if (thisClazz != inst::class.java)
		{
			return
		}
		
		for (field in thisClazz.declaredFields)
		{
			field.isAccessible = true
			
			if (Modifier.isFinal(field.modifiers))
			{
				continue
			}
			
			val value = field.get(this)
			
			if (value == null)
			{
				field.set(this, field.get(inst))
			}
			else if (value is Config)
			{
				value.merge(field.get(inst) as? Config ?: continue)
			}
		}
	}
	
}