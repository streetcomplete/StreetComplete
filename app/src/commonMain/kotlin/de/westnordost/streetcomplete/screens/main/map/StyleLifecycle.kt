package de.westnordost.streetcomplete.screens.main.map

/**
 * An in-flight cluster query or style command can outlive its loaded style. MapLibre Compose
 * 0.16 exposes these lifecycle failures as ordinary IllegalStateExceptions; do not hide other
 * command failures. No application source handle is retained across animation frames.
 */
internal fun IllegalStateException.isStyleHandleRace(): Boolean =
    message in
        setOf(
            "No ready loaded style",
            "Style operation belongs to a stale loaded-style identity",
            "Style operation belongs to a stale or unready loaded-style identity",
        )
