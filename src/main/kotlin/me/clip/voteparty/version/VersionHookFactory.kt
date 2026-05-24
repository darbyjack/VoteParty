package me.clip.voteparty.version

internal object VersionHookFactory
{

	private const val LEGACY_HOOK_CLASS = "me.clip.voteparty.version.VersionHookOld"
	private const val MODERN_HOOK_CLASS = "me.clip.voteparty.version.VersionHookNew"

	fun create(): VersionHook
	{
		val className = if (MinecraftVersion.current().isAtLeast(MinecraftVersion.V1_13))
		{
			MODERN_HOOK_CLASS
		}
		else
		{
			LEGACY_HOOK_CLASS
		}

		return try
		{
			Class.forName(className)
				.getDeclaredConstructor()
				.newInstance() as VersionHook
		}
		catch (exception: ReflectiveOperationException)
		{
			throw IllegalStateException("Failed to initialize particle compatibility hook: $className", exception)
		}
		catch (exception: ClassCastException)
		{
			throw IllegalStateException("Particle compatibility hook does not implement VersionHook: $className", exception)
		}
	}

}
