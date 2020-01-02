package me.clip.voteparty.messages

import co.aikar.locales.MessageKey
import co.aikar.locales.MessageKeyProvider

enum class Messages : MessageKeyProvider
{
	ERROR__INVALID_NUMBER,
	
	INFO__VOTES_NEEDED,
	
	PARTY__FORCE_START_SUCCESSFUL, PARTY__UPDATED_VOTES_NEEDED;
	
	private val key = MessageKey.of(name.toLowerCase().replace("__", ".").replace("_", "-"))
	
	override fun getMessageKey(): MessageKey?
	{
		return key
	}
}
