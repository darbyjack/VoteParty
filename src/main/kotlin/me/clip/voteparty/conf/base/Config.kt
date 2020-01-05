package me.clip.voteparty.conf.base

import java.lang.reflect.Modifier

interface Config
{
	
	fun mergeJ(inst: Config)
	{
		val thisClazz = this::class.java
		val instClazz = inst::class.java
		
		if (thisClazz != instClazz)
		{
			return // diff classes
		}
		
		val fields = thisClazz.declaredFields.associateBy({ field -> field }, { field -> instClazz.declaredFields.find { it.name == field.name } })
		
		
		for ((thisField, instField) in fields)
		{
			instField ?: continue
			
			thisField.isAccessible = true
			instField.isAccessible = true
			
			if (Modifier.isFinal(thisField.modifiers))
			{
				continue
			}
			
			val value = thisField.get(this)
			
			if (value == null)
			{
				thisField.set(this, instField.get(inst))
				continue
			}
			
			if (value is Config)
			{
				value.mergeJ(instField.get(inst) as? Config ?: continue)
			}
		}
	}
	
	
	/*fun mergeK(inst: Config)
	{
		val thisClazz = this::class
		val instClazz = inst::class
		
		if (thisClazz != instClazz)
		{
			return // diff classes
		}
		
		val props = thisClazz.declaredMemberProperties.associateWith()
		{ prop ->
			instClazz.declaredMemberProperties.find { it.name == prop.name }
		}
		
		for ((thisProp, instProp) in props)
		{
			instProp ?: continue
			
			thisProp.isAccessible = true
			instProp.isAccessible = true
			
			if (thisProp !is KMutableProperty1<*, *>)
			{
				continue
			}
			
			val value = thisProp.getter.call(this)
			
			if (value == null)
			{
				thisProp.setter.call(this, instProp.getter.call(inst))
				continue
			}
			
			if (value is Config)
			{
				val def = instProp.getter.call(inst) as? Config ?: continue
				value.mergeK(def)
			}
		}
	}*/
	
}