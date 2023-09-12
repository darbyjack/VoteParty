package me.clip.voteparty.util
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

object SchedulerUtil {
	private var IS_FOLIA: Boolean? = null
	private var GLOBAL_REGION_SCHEDULER: Any? = null
	private var ASYNC_SCHEDULER: Any? = null
	fun <T> callMethod(
		clazz: Class<*>,
		`object`: Any?,
		methodName: String?,
		parameterTypes: Array<Class<*>?>,
		vararg args: Any?
	): T {
		return try {
			clazz.getDeclaredMethod(methodName, *parameterTypes).invoke(`object`, *args) as T
		} catch (t: Throwable) {
			throw IllegalStateException(t)
		}
	}

	fun <T> callMethod(`object`: Any?, methodName: String?, parameterTypes: Array<Class<*>?>, vararg args: Any?): T {
		return callMethod(`object`!!.javaClass, `object`, methodName, parameterTypes, *args)
	}

	fun <T> callMethod(`object`: Any, methodName: String?): T {
		return callMethod(`object`.javaClass, null, methodName, arrayOf())
	}

	fun <T> callMethod(clazz: Class<*>, methodName: String?): T {
		return callMethod(clazz, null, methodName, arrayOf())
	}

	private fun methodExist(clazz: Class<*>, methodName: String, vararg parameterTypes: Class<*>): Boolean {
		try {
			clazz.getDeclaredMethod(methodName, *parameterTypes)
			return true
		} catch (ignored: Throwable) {
		}
		return false
	}

	val isFolia: Boolean?
		get() {
			if (IS_FOLIA == null) IS_FOLIA = methodExist(Bukkit::class.java, "getGlobalRegionScheduler")
			return IS_FOLIA
		}
	val globalRegionScheduler: Any?
		get() {
			if (GLOBAL_REGION_SCHEDULER == null) {
				GLOBAL_REGION_SCHEDULER = callMethod<Any>(Bukkit::class.java, "getGlobalRegionScheduler")
			}
			return GLOBAL_REGION_SCHEDULER
		}
	val asyncScheduler: Any?
		get() {
			if (ASYNC_SCHEDULER == null) {
				ASYNC_SCHEDULER = callMethod<Any>(Bukkit::class.java, "getAsyncScheduler")
			}
			return ASYNC_SCHEDULER
		}

	fun runTask(plugin: Plugin?, runnable: Runnable) {
		if (isFolia!!) {
			val globalRegionScheduler = globalRegionScheduler
			callMethod<Any>(globalRegionScheduler, "run", arrayOf(
				Plugin::class.java,
				Consumer::class.java
			), plugin,
				Consumer { task: Any? -> runnable.run() } as Consumer<*>)
			return
		}
		Bukkit.getScheduler().runTask(plugin!!, runnable)
	}

	fun runTaskAsynchronously(plugin: Plugin?, runnable: Runnable) {
		if (isFolia!!) {
			val asyncScheduler = asyncScheduler
			callMethod<Any>(asyncScheduler, "runNow", arrayOf(
				Plugin::class.java,
				Consumer::class.java
			), plugin,
				Consumer { task: Any? -> runnable.run() } as Consumer<*>)
			return
		}
		Bukkit.getScheduler().runTaskAsynchronously(plugin!!, runnable)
	}

	fun runTaskTimerAsynchronously(plugin: Plugin?, runnable: Runnable, initialDelayTicks: Long, periodTicks: Long) {
		if (isFolia!!) {
			val asyncScheduler = asyncScheduler
			callMethod<Any>(asyncScheduler,
				"runAtFixedRate",
				arrayOf(
					Plugin::class.java,
					Consumer::class.java,
					Long::class.javaPrimitiveType,
					Long::class.javaPrimitiveType,
					TimeUnit::class.java
				),
				plugin,
				Consumer { task: Any? -> runnable.run() } as Consumer<*>,
				initialDelayTicks,
				periodTicks * 50,
				TimeUnit.MILLISECONDS)
			return
		}
		Bukkit.getScheduler().runTaskTimerAsynchronously(plugin!!, runnable, initialDelayTicks, periodTicks)
	}

	fun runTaskLater(plugin: Plugin?, runnable: Runnable, delayedTicks: Long) {
		if (isFolia!!) {
			val globalRegionScheduler = globalRegionScheduler
			callMethod<Any>(globalRegionScheduler, "runDelayed", arrayOf(
				Plugin::class.java,
				Consumer::class.java,
				Long::class.javaPrimitiveType
			),
				plugin,
				Consumer { task: Any? -> runnable.run() } as Consumer<*>, delayedTicks)
			return
		}
		Bukkit.getScheduler().runTaskLater(plugin!!, runnable, delayedTicks)
	}

	fun runTaskLaterAsynchronously(plugin: Plugin?, runnable: Runnable, delayedTicks: Long) {
		if (isFolia!!) {
			val asyncScheduler = callMethod<Any>(Bukkit::class.java, "getAsyncScheduler")
			callMethod<Any>(asyncScheduler, "runDelayed", arrayOf(
				Plugin::class.java,
				Consumer::class.java,
				Long::class.javaPrimitiveType,
				TimeUnit::class.java
			),
				plugin,
				Consumer { task: Any? -> runnable.run() } as Consumer<*>, delayedTicks * 50, TimeUnit.MILLISECONDS)
			return
		}
		Bukkit.getScheduler().runTaskLaterAsynchronously(plugin!!, runnable, delayedTicks)
	}

	fun runTaskForPlayer(plugin: Plugin?, player: Player, runnable: Runnable) {
		if (isFolia!!) {
			val entityScheduler = callMethod<Any>(player, "getScheduler")
			callMethod<Any>(entityScheduler, "run", arrayOf(
				Plugin::class.java,
				Consumer::class.java,
				Runnable::class.java
			),
				plugin,
				Consumer { task: Any? -> runnable.run() } as Consumer<*>, null)
			return
		}
		Bukkit.getScheduler().runTask(plugin!!, runnable)
	}

	fun cancelTasks(plugin: Plugin?) {
		if (isFolia!!) {
			val asyncScheduler = asyncScheduler
			val globalRegionScheduler = globalRegionScheduler
			callMethod<Any>(asyncScheduler, "cancelTasks", arrayOf(Plugin::class.java), plugin)
			callMethod<Any>(globalRegionScheduler, "cancelTasks", arrayOf(Plugin::class.java), plugin)
			return
		}
		Bukkit.getScheduler().cancelTasks(plugin!!)
	}
}