package de.westnordost.osmagent.quests;

public class QuestImportance
{
	/** Solving this quest will fix data that is shown as invalid or erronous in QA tools */
	public static final int ERROR = 1;

	/** Solving this quest will fix data that is shown as warnings in QA tools */
	public static final int WARNING = 5;

	/** Solving this quest will complement important/very useful data that is used by many data
	 *  consumers */
	public static final int MAJOR = 10;

	/** Solving this quest will complement useful data that is used by some data consumers */
	public static final int MINOR = 50;

	/** Solving this quest will complement data that is used for a very specific use case of the map
	 * */
	public static final int EXTRA = 100;

	/** Solving this quest will complement data that is defined in the wiki but has no concrete uses
	 *  (yet). It is collected for the sake of mapping it in case this might make sense later */
	public static final int INSIGNIFICANT = 200;

	private QuestImportance()
	{
		// do not instantiate, just a constants class. Not using an enum here because it should
		// be possible to use other values than the given ones here.
	}
}
