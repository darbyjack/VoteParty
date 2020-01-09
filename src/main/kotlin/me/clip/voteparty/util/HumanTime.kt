package me.clip.voteparty.util

import java.time.Duration
import java.time.temporal.ChronoUnit

object HumanTime
{
	
	private val REGEX = "(\\d+) ?([a-zA-Z]+)".toRegex()
	
	
	fun none(): Duration
	{
		return Duration.ZERO
	}
	
	fun read(text: String): Duration
	{
		if (text.isBlank())
		{
			return none()
		}
		
		val time = REGEX.findAll(text).map(MatchResult::destructured).fold(0L)
		{ prev, (numb, unit) ->
			
			val long = numb.toLongOrNull() ?: 0L
			val time = unit.toDuration().toMillis()
			
			prev + (long * time)
		}
		
		return Duration.ofMillis(time)
	}
	
	
	private fun String.toDuration(): Duration
	{
		val unit = when (this.toLowerCase())
		{
			"mils", "millis", "milliseconds"  -> ChronoUnit.MILLIS
			"s", "sec", "secs", "seconds"     -> ChronoUnit.SECONDS
			"m", "min", "mins", "minutes"     -> ChronoUnit.MINUTES
			"h", "hs", "hrs", "hour", "hours" -> ChronoUnit.HOURS
			"d", "day", "days"                -> ChronoUnit.DAYS
			"w", "week", "weeks"              -> ChronoUnit.WEEKS
			"mon", "mons", "month", "months"  -> ChronoUnit.MONTHS
			"y", "year", "years"              -> ChronoUnit.YEARS
			else                              -> null
		}
		
		return unit?.duration ?: Duration.ZERO
	}
	
}