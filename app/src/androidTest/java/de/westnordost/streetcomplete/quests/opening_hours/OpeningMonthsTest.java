package de.westnordost.streetcomplete.quests.opening_hours;

import junit.framework.TestCase;

import java.util.Locale;

public class OpeningMonthsTest extends TestCase
{
	private final boolean l = true;
	private final boolean o = false;

	private final TimeRange nineToFive = new TimeRange(540,1020, false);
	private final TimeRange eightOpenEnd = new TimeRange(1200,1200, true);
	private final Weekdays monday = new Weekdays(new boolean[]{l});
	private final Weekdays wednesday = new Weekdays(new boolean[]{o,o,l});
/*yyo:..:ss+/:/yhddmydmNNNNNNNNNNNNNNNMMNNMMMMMMMMMMMMMMMMMMMMMMNNNmdddhs:..-::-
+o+:--.-:ssoooshdmmmmmmNNNNNNNNNNNNNMMMMNMMMNNMMMMMMMMMMMMMMMMMMMMMNNNmhs/---::-
sdhs/-..:so+osyydhdmmNNNNNNNNNNNNNMMMMMMMMMMNNMMMMMMMMMMMMMMMMMMMNNNNNmmddy+:/:-
smmh+-..:soohdddmmmmmNNNMMMMMMMMNNNNNNmmmNmmNNNNNNMMMMMMMMMMMMMMMNNNNNmmdho::::-
oddy/-..:sooddmddmNNNNNMMMMMMMMmhysyysoo+//+syhyyhddmNNNMMMMMMMMMMNMMNNNdo/:://:
+hdy/-..:so+shddmNNNNNMMMMMMMMNho+//:---------::---:+shdmNMMMMMMMMMMMMNNNmso+//:
+hhy/-..:so/shddmNNNMMMMMMMMMNmyyhyyyso/:...........--//+smMMMMMMMMMMMMNNNmy://:
+hhys:..:so/yyhmmNNNNMMMMMMMMMNmys+/+syys/-....-/oyyssso+odMMMMMMMMMNNNNNNNy///-
+hhys:..:so/+shdmNNNNMMMMMMMMmdhhyyo++++yy/-..:/+/+///+sdhdMMMMMMMMMMMNNNNNNy/:-
+soo+-..:ss+osyhmNNNNNNNMMMMNhdmdhddy+o/os:--://oyyyo+++oyNMMMMMMMMMMNNNNNNNdo:-
/+/--...:ssoossyhmmNNNNMMMMNho+/++++///oo:----/:+oydddhhyomMMMMMMMMMMNNNNNms+/:-
::--....:yysoosyhddmNNNMMMMho/-------://.....-::-:////+oo+dMMMMMMMMMMNNNNNms/:--
:--.....:syyysssyhdmmNNNMMNo:---....-////:...-:-...-----:/hMMMNNMMMNNNNNmmh+::::
/:-.....:syysssyyhhdmmNNMMNs+/::----/hhymdyooyo:-.......-:sNMMNMMMMNNNNNmdo::/:-
o/-.....-/+++++yhddmdmmNNNdsso+/:-:://++/:/::/+:.-.....--:yNMMMMMMNNNNNmmd+-://:
s+:.....-::::::/ydddmmmmNNdyo+/:-:+/:::::-----:--.-...-::+hNMMMMMMNNNNNdhs:.-::-
::-......------:yhdmmmdmNNNmds:::++:oyyysssoo+::------:/+omNMMMMMNNNNmdy+-..-:-.
:---....-::::::/+odmmdmNNNNNmh+://+yh/--./..:+yo:----:/+oyNNNNNMMNNhyho/:--.-:--
sso+/:-:/osooo+oyhmmmmmNNNNNNms//:/dmmNNdmhhhosh::--:/+oymNMNNMMMNmhs+///:---:-.
ddhhhyysyhdhyo+ymNmNNmmNNNNMNNh//::hdysosyhyyhNm:---/oshdNMNNNMMNNNmdysso:..---.
/::::::::::::/+dmmmNNmmNNNNMMNmso/+++:..-----/s/-:-/sdhmNNMMNNNNMNNNmhss+-..-:-.
.............:+hyhdmNmmNNNNMMNNmh+oso+/:::::://:-:+ymNNNNMMMMNNNNNNNmdys:...-::-
..............-:odmNNmmNNNNNNNNNms++++oooo++//::+hdNNMMMMMNNNMMMNNNNNdo:-...-::-
---------------:ymmNNNNmNNNNNNMMNmo//::::---::/omNNMMMMMMMMNNNNNNNmdh+-.....-:-.
--------...--.--/ooyNNNNNNNNMMMMMmho+/:::--::/omMMMMMMMMMMMMMNNNNNNs-..``...-:-.
...............-.../hshNMhymNMMMMNmdyo////://+yNMMMMMMMMMNMMNNNNhhy-``..```.-:-.
..---....-::-.--..../dMMMo-/ymNMMMMNNdhysyyyhdNMMMMMMMNddsNhh::----.```.```.-:-.
---:--..-:///::-..-sNMMMMm/:+dNMMMMMMNNNNNNNNNMMMMNmdhssmy/+:.......```````..-..
------.---::-:/+osmNNMMMMMNyshNNNNMMMNNNNNNNNNmdyo++++ohNmdho:..................
------:/+ssydmNmNNNNNMMMMMNdhyyyhhhhdhs+//+oso///////+smNmhmmmho-....----.......
                  https://www.youtube.com/watch?v=wvUQcnfwUUM */
	private final CircularSection inTheSummerTime = new CircularSection(5,8);


	@Override public void setUp() throws Exception
	{
		super.setUp();
		Locale.setDefault(Locale.US);
	}

	public void testEmptyConstructor()
	{
		assertEquals("", new OpeningMonths().toString());
		assertEquals("January–December: ", new OpeningMonths().getLocalizedMonthsString());

	}

	public void testMonthsComposition()
	{
		assertEquals("Mo 09:00-17:00", new OpeningMonths(
				new CircularSection(0,11), new OpeningWeekdays(	monday, nineToFive)).toString());

		assertEquals("Jun-Sep: Mo 09:00-17:00", new OpeningMonths(
				inTheSummerTime, new OpeningWeekdays( monday, nineToFive)).toString());

		OpeningMonths om = new OpeningMonths();
		om.months = inTheSummerTime;
		om.weekdaysList.add(new OpeningWeekdays(monday, nineToFive));
		om.weekdaysList.add(new OpeningWeekdays(wednesday, nineToFive));

		assertEquals("Jun-Sep: Mo 09:00-17:00, Jun-Sep: We 09:00-17:00", om.toString());
	}

	public void testMergeTimes()
	{
		OpeningMonths om = new OpeningMonths();
		om.months = inTheSummerTime;
		om.weekdaysList.add(new OpeningWeekdays(monday, nineToFive));
		om.weekdaysList.add(new OpeningWeekdays(monday, eightOpenEnd));

		assertEquals("Jun-Sep: Mo 09:00-17:00,20:00+", om.toString());
	}
}
