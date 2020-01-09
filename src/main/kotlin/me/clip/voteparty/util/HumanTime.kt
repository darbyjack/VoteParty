package me.clip.voteparty.util

import java.time.Duration
import java.time.temporal.ChronoUnit

data class HumanTime(val duration: Duration)
{
	
	fun getData(unit: ChronoUnit): Long
	{
		return when (unit)
		{
			ChronoUnit.MILLIS  -> duration.toMillis()
			ChronoUnit.SECONDS -> duration.seconds
			ChronoUnit.MINUTES -> duration.toMinutes()
			ChronoUnit.HOURS   -> duration.toHours()
			ChronoUnit.DAYS    -> duration.toDays()
			ChronoUnit.WEEKS   -> duration.toDays() / 7
			ChronoUnit.MONTHS  -> duration.toDays() / 31
			else               -> -1L
		}
	}
	
	companion object
	{
		private val EMPTY = HumanTime(Duration.ZERO)
		private val REGEX = "(\\d+) ?([a-zA-Z]+)".toPattern()
		
		
		fun none(): HumanTime
		{
			return EMPTY
		}
		
		fun read(text: String): HumanTime
		{
			if (text.isBlank())
			{
				return none()
			}
			
			val matcher = REGEX.matcher(text)
			var millis = 0L
			
			while (matcher.find())
			{
				val numb = matcher.group(1).toLongOrNull()
				val unit = matcher.group(2).let(::unit)
				
				if (numb == null || numb <= 0 || unit == null)
				{
					continue
				}
				
				millis += unit.duration.toMillis() * numb
			}
			
			return HumanTime(Duration.ofMillis(millis))
		}
		
		
		private fun unit(text: String): ChronoUnit?
		{
			return when (text.toLowerCase())
			{
				"mils", "millis", "milliseconds"  -> ChronoUnit.MILLIS
				"s", "sec", "secs", "seconds"     -> ChronoUnit.SECONDS
				"m", "min", "mins", "minutes"     -> ChronoUnit.MINUTES
				"h", "hs", "hrs", "hour", "hours" -> ChronoUnit.HOURS
				"d", "day", "days"                -> ChronoUnit.DAYS
				"w", "week", "weeks"              -> ChronoUnit.WEEKS
				"mon", "mons", "month", "months"  -> ChronoUnit.MONTHS
				else                              -> null
			}
		}
		
	}
	
}