package de.westnordost.streetcomplete.data.upload;


public class VersionBannedException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	private final String banReason;

	public VersionBannedException(String banReason)
	{
		super("This version is banned from making any changes!");
		this.banReason = banReason;
	}

	public String getBanReason()
	{
		return banReason;
	}
}
