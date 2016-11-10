#osmagent

![Screenshot](screenshot.png)

An Android app which finds wrong, incomplete or extendable data in the user's vicinity and provides
him the tools to complete these easily and directly on site without having to use another editor.

The found issues are presented to the user in a map of quests (like i.e. in Mapdust) that each are
solvable by filling out a simple form to complete/correct the information on site. The user's
answer is then processed and directly uploaded into the OSM database in atomic commits in the name
of the user's OSM account, eliminating the need to fire up another OSM editing tool (when back from
the survey).

Since the app is meant to be used on a survey, it can be used offline and otherwise aims to be
economic with data usage.

The app is aimed at users who do not know anything about OSM tagging schemes but still want to
contribute to the OpenStreetMap by surveying their neighbourhood (or other places as well).

Because of the target group, only those quests are shown which are answerable very clearly by asking
one simple question and only those quests are created which contain very few false positives:

I.e. the app will never ask users "Is this footway covered?" because while covered=yes might be a
valid extension of the tagging of a footway, it only applies to a overwhelming minority of all
footways. On the other hand, the majority of all shops will have fixed opening hours, so the app
will ask users "What are the opening hours of this shop?" and so on.

Other examples:
* What is the name of this road?
* How many lanes does this road have?
* What is the surface  of this road?
* How many storeys does this building have?

etc..

## State of development (What's missing)

From the point of view of targeting a first functional release on Google Play, not from what I plan
for it later...

* Many more "recipies" (aka "quests") for incomplete data and forms to complete this data
* Data economic quest download and changes upload behavior

## Contributing

Yes, absolutely! Even if you do not know Android programming, you can help in many other ways. For
example:

* collecting or creating adequate imagery for answers that are more easily given by selecting a
  picture (i.e. road surface, roof shape, footway/cycleway designation/access, bridge or tunnel type
  etc.)
* collecting ideas for possible quests and how the form should look like
* making graphics and/or helping with UX design
* translations
* betatesting

## License

This software is released under the terms of the [GNU General Public License](http://www.gnu.org/licenses/gpl-3.0.html).