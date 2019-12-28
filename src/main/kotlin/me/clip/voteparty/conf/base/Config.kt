package me.clip.voteparty.conf.base

import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

interface Config
{

    fun merge(inst: Config)
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
                value.merge(def)
            }
        }
    }

}