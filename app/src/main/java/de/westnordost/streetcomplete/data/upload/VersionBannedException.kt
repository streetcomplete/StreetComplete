package de.westnordost.streetcomplete.data.upload

class VersionBannedException(val banReason: String?)
	: RuntimeException("This version is banned from making any changes!")
