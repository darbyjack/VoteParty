package me.clip.voteparty.messages

import co.aikar.locales.MessageKey
import co.aikar.locales.MessageKeyProvider

enum class Messages : MessageKeyProvider
{
	INFO__VOTES_NEEDED,
	
	PARTY__FORCE_START_SUCCESSFUL;
	
	private val key = MessageKey.of(name.toLowerCase().replace("__", ".").replace("_", "-"))
	
	override fun getMessageKey(): MessageKey?
	{
		return key
	}
}
