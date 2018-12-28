package de.westnordost.streetcomplete.util;

/** Same as Java 8 Supplier class, only this is not available on Android API < 24 */
@FunctionalInterface
public interface Supplier<T>
{
	T get();
}
