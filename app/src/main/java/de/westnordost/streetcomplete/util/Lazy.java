package de.westnordost.streetcomplete.util;

public class Lazy<T>
{
	private T value;
	private Supplier<T> create;

	public Lazy(Supplier<T> create)
	{
		this.create = create;
	}

	public T get()
	{
		if(value == null && create != null)
		{
			value = create.get();
			create = null;
		}
		return value;
	}
}
