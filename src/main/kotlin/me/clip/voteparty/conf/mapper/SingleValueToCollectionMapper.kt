package me.clip.voteparty.conf.mapper

import ch.jalu.configme.beanmapper.MapperImpl
import ch.jalu.configme.beanmapper.MappingContext
import java.util.Collections


/**
 * Extension of the bean mapper representing a simple migration where a property is changed from
 * a single value to a collection. This mapper wraps a single value into a collection whenever a
 * collection should be constructed. Example for issue #117.
 */
class SingleValueToCollectionMapper : MapperImpl()
{
	override fun createCollection(context: MappingContext, value: Any): Collection<*>?
	{
		if (value !is Iterable<*>)
		{
			val coll = super.createCollection(context, Collections.singleton(value))
			// Register error to trigger a rewrite with the proper structure
			context.registerError("Found single value where a collection is expected")
			return if (isCollectionWithOneElement(coll)) coll else null
		}
		return super.createCollection(context, value)
	}
	
	companion object
	{
		private fun isCollectionWithOneElement(coll: Collection<*>?): Boolean
		{
			return coll != null && coll.size == 1
		}
	}
}