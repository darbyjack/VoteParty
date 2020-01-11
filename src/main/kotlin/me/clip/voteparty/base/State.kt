package me.clip.voteparty.base

/**
 * Defines an object that holds state and requires enable and disable logic
 */
internal interface State
{
	
	/**
	 * Holds logic for when this enables
	 */
	fun load()
	
	/**
	 * Holds logic for when this is disabled
	 */
	fun kill()
	
}