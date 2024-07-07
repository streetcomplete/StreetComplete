# Changelog

## v58.2
- Fixed it was impossible to login with a third party that required 2FA (e.g. Google) (#5724, #5711)

## v58.1

- Bike paths: Roads previously mapped with [recently deprecated](https://wiki.openstreetmap.org/wiki/Proposal:Deprecate_cycleway%3Dopposite_family) tags for describing bike infrastructure are now marked for re-survey (#5694)
- Places overlay, things overlay, ...: Fix crash on displaying features that were incompletely translated to a dialect of a language, by @logan12358
- Avoid asking about existence of shop right after asking about its opening hours (#5674)
- Moped on bike paths: When there is no sign, just tag that and don't infer anything else (#5565)
- Parcel locker drop-off: Add option for return deliveries only (#5687)
- Crossing kerb height: Only ask if road has a sidewalk (#5668)
- Addresses: recognize common housenumber formats in Bulgaria (#5683), by @mnalis
- Bus stop ref: Now enabled in Portugal (#5695)
- Other small improvements (#5594, #5676, #5698, #5710...)

## v58.0

### New quests

- _"What’s the brand of this parcel locker?"_ (#5638), by @Arthur-GYT
- _"Can you pick up parcels here?"_ (#5639), by @Arthur-GYT
- _"Can you drop off parcels here?"_ (#5639), by @Arthur-GYT
- _"Is there a sign indicating access for mopeds on this bike path?"_ – asked exclusively in Belgium (#5567), by @PowerPlop

### Overlay improvements

- Bike paths: Now possible to select that cycling on footway is explicitly allowed or prohibited (#5575, #4913), by @wielandb
- Places: Entrances are shown to help with orientation (#5497)
- Things: Advertising totem was not displayed (#5588)

### Quest improvements

- Road Surface: Fix it was possible to answer track type and surface for a road in an endless loop (#5650)
- Max height: Don't ask if it has been specified for forward and backward or individual lanes separately (#5609)
- Fire hydrant diameter: Now enabled in Hungary (#5617), by @dnet
- Power pole material: Can now answer with that power line is anchored to building (#5663), by @qugebert
- Tactile paving: Now enabled in Columbia (#5579)
- Prohibited for pedestrians: Clarified UI and wording (#5610)
- Bicycle parking: Add option for just markings on the ground (#5191)
- Drinking water: Don't ask for intermittent water sources (#5632), by @esilja
- Other small improvements and fixes (#5631, ...)

### General improvements

- For some quests, there is now an ℹ-button which expands a help text (#5612, #1913)
- The user profile screen looks a bit different now (#5607)
- Other small improvements (#5635, #5645, ...), thanks @riQQ

## v57.4

- Fix crash under certain circumstances in bike path overlay (#5604) (regression from #5596)

## v57.3

- Fix UNDO: It didn't actually do anything for edits that were already synced! This critical issue existed since v57.2 (#5600, #5602)
- Traffic signals: Improve wording (#5591)
- Max speed: Show warning when inputting implausible slow zone tempo limit (#5592)
- Payment methods: Don't ask in shops if they have been specified exhaustively already (#5589), by @urbalazs
- Railway crossings barriers: Don't ask for abandoned railways (#5597)
- Bike paths overlay: Fix selecting "not designated as bike path" when it was a "path or trail" wouldn't do anything (#5596)

## v57.2

- Lit overlay: Unsupported current tagging is now indicated as such (#5571)
- Building overlay: Selecting a specific building type for a historic building does now not remove its property as historic (#5547)
- Fix max width for road narrowing traffic calmings were not answerable if mapped as a way (#5569, #5578), by @mnalis
- Fix regression in v57.1 that may lead to issues displaying the current GPS location (#5516)
- Fix the feature name label was slightly wrong for a few map features (#5549)
- Fix description of Prettymapp (#5570), by @FloEdelmann
- Other small improvements (#5533, #5558, #5559, #5525, #5573), thanks @matkoniecz, @burrscurr

## v57.1

- fixed crash on startup if you recently solved a crossing quest (#5522)
- fixed that you could e.g. add a POI in an overlay twice if you tap OK fast enough (#5523)

## v57.0

Take cover! For you don't want to be squashed by this m-m-mega phat update, or do you?

### Buildings Overlay (#5461)

The new Buildings overlay now lets you comprehensively view and edit building types all around town,
color-coded for your convenience!
Conversely, the building type quest is disabled by default now as it was really spammy, don't you think?

(You'll be amazed and/or appalled how many buildings are currently actually tagged wrong or imprecise! 😅)

### Things Overlay (#4912), thanks @matkoniecz

The new Things overlay allows you to map all those small map features like benches, bicycle parkings,
roadside trees, ATMs and other street furniture in general.

It complements the Places Overlay which has been renamed from Shop overlay because it now also lets
you map places that are not shops - like hotels, hospitals, schools and so forth. (#5152)

### New Quests

- The crossing type quest has been split up into two quests: One that asks for markings, the other for traffic signals (#5471, #5476)

### Quest and Overlay Enhancements and Fixes

- Lit and Surface Overlay: now each have a button to apply the last answer (#4741)
- Measuring widths and heights: [StreetMeasure](https://play.google.com/store/apps/details?id=de.westnordost.streetmeasure) doesn't work for you? In that case you'll be offered to quickly disable these AR measurement quests now (#4849)
- Max Height: Don't prompt to estimate height when there is no sign (#5458)
- Opening Hours: Also ask for places whose already set opening hours likely contain mistakes (#5463)
- BBQ fuel: Add "gas" answer (#5495), by @k-yle
- Steps: Don't ask about ramps and tactile paving for hiking steps, by @matkoniecz
- Fire Hydrant Diameter: Disable for Austria again (#5470)

### Other

- Fix a (shockingly common) crash (#5498)
- Fix issues with doing edits while data is being downloaded (#4258)
- Add some interesting links as achievement rewards (#5466)
- Translation to Amharic has been disabled, it was not maintained for over a year
- Use new method to upload and download data as mandated by Google Play policy. On Android 13 and above, there's no notification for continuing to sync in the background anymore. (#5492)
- Other small enhancements, performance and wording improvements (#5468, #5154, #5474...), thanks @Jean-BaptisteC, @matkoniecz, @FloEdelmann

## v56.1

Some work towards an [iOS port](https://github.com/streetcomplete/StreetComplete/issues/5421) of
StreetComplete has begun - starting with making the code more platform independent for now.
(Monitor the linked tickets for updates, because non user-facing changes are usually not
mentioned in the changelogs)

Thanks to @FloEdelmann, @neonowy, @starsep, @YoshiRulz, @riQQ and others [for their contributions](https://github.com/orgs/streetcomplete/projects/1/views/1)
so far!

### Quest Enhancements and Fixes

- ATM, charging station and clothing container operators: Provide better suggestions
- Bike parking capacity: Don't ask for informal bike parkings (#5428), by @bxl-forever
- Road width: Don't ask for roads that may not be accessed by pedestrians (#5437)
- Fire hydrant diameter: Also ask in Austria and Luxembourg
- Fix traffic calmings did not display in the parking overlay
- Other small enhancements (#5438, #5440, ...), thanks @Helium314, @matkoniecz

### General Enhancements and Fixes

- In settings, show how many quests have been hidden by you (#5359), by @jmizv
- Show both ref number and name in the quest title hint for roads (#5427) @arrival-spring
- Fix filtering logs by tag name did not work
- Other small enhancements (#5435, ...), thanks @matkoniecz

## v56.0

### New Authorization

Sorry, you'll need to log in again. Authorization via OAuth 1.0a is being deprecated on OSM, so we have switched to OAuth 2.0 (#5383, #5322).  
Sponsored by [SUSE Open Source Community Citizens (OSCC)](https://www.suse.com/esg/).

### General Enhancements and Fixes

- You can now view, filter and copy logs from within the app. (This helps finding the cause of issues.) (#5335), by @neonowy
- Other small improvements (#5360), by @riQQ

### New Quests

- _"Is there a sanitary dump station?"_ asked for caravan sites (#5363), by @qugebert, (#5407) thanks @sun-geo

### Quest Enhancements and Fixes

- Other small improvements (#5332, #5371, #5399, #5395), thanks @catdogmat, @mnalis

## v55.1

### Quest Enhancements and Fixes

- Charging station operator: Do not ask if the operator is not signed
- Indoor amenities: Don't consider roofs as indoor (#5337), by @qugebert
- Crossing kerbs: Do not ask if the kerbs on both sides are already specified (#5374), by @FloEdelmann
- Fix bike paths overlay: When leaving the contra-flow side of a oneway unspecified, also leave the oneway-for-cyclists status untouched (#5367)
- Fix display of max height, max speed and max weight signs in US, Canada and Australia (#5384)
- Speed limits: For trunk roads without speed limit signs, ask whether it is inside an urban area (#5378)
- Other small improvements (#5336, #5387), thanks @arrival-spring

### General Enhancements and Fixes

- Do not offer splitting whole roundabouts, leave a note in that case. See ticket for details (#5372)
- Various small enhancements, wording improvements and fixes (#5353, #5346, ...), thanks @neonowy, @riQQ, ...

## v55.0

### New Quests

- _"What is this grill powered by?"_ for bbq places (#5211), by @qugebert
- _"Is this inside a building?"_ for small amenities such as parcel lockers, vending machines, defibrillators, ... (#5278), by @qugebert
- _"Where is this defibrillator located?"_ (#2146, #5328), by @qugebert

### Quest Enhancements

- Ask "Is there a crossing here?" for more potential crossings (#5160, #5162), by @Helium314
- Ask for bus stop refs but don't ask for postbox royal cyphers in New Zealand (#5277), by @FloEdelmann
- Ask more quests for caravan sites (#5238, #5239)
- Recycling: Add "food waste" option (#5297)
- Other small fixes and wording improvements (#5292, #5236, #5171, #5276, #5195), thanks @matkoniecz

### General Enhancements and Fixes

- Quest selection: Can search by English quest titles too (#5284), by @FloEdelmann
- It's now easier to tap a road in an overlay on low zoom... (#5282)
- Various small enhancements and fixes (#5286, #5170...), thanks @FloEdelmann

## v54.1

### New translations

- **Hebrew**, by David, Tal Einat, Slava Sukhochev and more
- **Slovenian**, by CTJ, Jaka Kranjc, StefanB, SimonG and more

### Quest Enhancements

- Bike path segregation: Never ask for paths where biking is prohibited (#5213), by @matkoniecz
- Max height: Allow input of restrictions of 10m and above (#5232)
- Max height: Clarify wording – ask what is indicated by a sign (see [discussion](https://community.openstreetmap.org/t/streetcomplete-fragt-bei-eisenbahn-weg-kreuzung-nach-durchfahrtshohe/103606))
- Max height: Do not ask for crossings with tram lines – too numerous and usually never signed (#5180)
- Also ask for names of tram stops (#5215)
- Also ask if a caravan site requires a fee (#5228), by @qugebert
- in Slovenia: Ask about tactile paving and bike paths – exclusive and advisory bike lanes are differentiated (#5274, #5237), by @CTJoriginal

### Overlay Enhancements

- Shops overlay: Display and allow to add clubs (#5244)
- Surface overlay: Show missing data for motorways and motorway links (#5247)

### General Enhancements and Fixes

- Profile page: For ascribing on which day a user made any one edit, use the UTC timezone (#5245, #4951), by @mnalis
- Fix a (rare) crash on startup (#5241), by @Helium314
- Other small enhancements (#5268, #5264, #5262, #5265...), by @tapetis, @Helium314

## v54.0

### New Quests

- _"What’s the height of the curbs at this crossing?"_ (asked even if the curbs have been mapped separately, too) (#5104)

### Quest Enhancements

- Max height: Also ask for crossings with rails electrified with overhead wires (#5180, #5223)
- Bus stop refs: Also ask in Colombia (#5124)
- Sidewalks, bike paths: Also ask on roads that have a foot path or bike path nearby.  
  You can always answer that it exists but is displayed separately on the map (#5060).
- Shop level in mall: Only ask after its name has been determined (#5198), by @matkoniecz
- Ask for more railway platforms about their properties (#5183), by @burrscurr
- Fix put name of railway halts and stations on correct element (#5215)
- Other small fixes and enhancements (#5212, #5173, #5190, #5194, ...), thanks @qugebert

### General Enhancements and Fixes

- Moved the overlay button to the main screen (#5109).  
  Enjoy the short tutorial first time you tap on that button (#5158)!
- Never ask _"Are you sure you checked this on-site?"_ for places you've just been to up to ten minutes ago (#4947), by @Helium314
- Fix two problems that may appear as performance issues (#5091, #5146), by @Helium314
- Fix source for a crash (#5177)
- Other small fixes and enhancements (#5147, #5181, #5184 ...), thanks @tapetis, @arrival-spring

## v53.3

### Enhancements and Fixes

- Fix yet another crash (#5090), by @Helium314
- Do not automatically title-case (street) names as it is not correct to title-case them in all languages (#4784)
- Enable bus stop number quest in Israel (#4784), by @FloEdelmann
- Street lanes: Fix input different lane count per side when user language is set to a right-to-left language (#5118)
- Other small fixes and enhancements (#5059, #5065, #5112, #5117, #5094, #5128...)

### Unmaintained translations

Translations to Turkish, Malayalam, Amharic, Armenian, Bosnian, Norwegian Nyorsk, Galician and Japanese haven't been maintained for over half a year.

As a native speaker of any of these languages, you can [help translate the app](https://poeditor.com/join/project/IE4GC127Ki) in order to retain this translation in the app.

To avoid an awkward mix of properly localized and English texts all over the app that wouldn't be usable by people who do not understand both languages, languages whose translation completeness becomes too low are removed from the app and only re-added when they reach near 100% again.

## v53.2

- Fix a crash (#5066, #5073) by @Helium314
- Multilingual (street) names: More and more fine-grained suggestions which languages to add in which regions. E.g. German in Italy only selectable in Trentino-Südtirol, etc.
- Fix edits done in the shop overlay (and more) didn't actually count in the statistics (#5074)
- Other small fixes and enhancements (#5062, #5076, #5068, #5071, #5010, #5082...), thanks @Helium314, @tapetis

## v53.1

- Fix a crash (#5040, #5026), thanks @Helium314
- Bike path segregation: Add another option that is especially common in the Netherlands (#5037, #5045)
- Don't start a new instance when tapping on the "Syncing..." notification (#4975, #5044), by @tapetis
- Other small fixes and enhancements (#5008, #5012, #5013, #5030, #5028, #5012, #5049...), thanks @arrival-spring, @HolgerJeromin

## v53.0

### Quest & Overlay Enhancements

- Parking overlay: You can now add and modify curb extensions etc. (#4976)
- Address overlay: You can now add addresses as entrances directly to building outlines (#4976, #4995)
- Also ask whether barbecue spots are covered (#4905), thanks @roptat
- Crossing: Different wording and use `crossing=informal` for points at which there are no crossings but people can still cross (#4944)

### New Quests

- _"Which customers visit this hairdresser?"_ (#4833, #4909), by @mnalis
- _"Is there tactile paving at the top and bottom of these steps?"_ (#3534, #5003), by @arrival-spring

### General Enhancements

- Add Belarusian translation, by Yau, Андрей and more
- In the various bi-lingual republics and autonomous regions of Russia, allow inputting names in the respective languages
- It is now visible on the map which areas have been downloaded already (#4970, #4986)
- Animate current selection for better visibility (#4417)
- Two pane layout for settings & about screen for tablets (#4954), by @tapetis
- Other small fixes and enhancements (#4842, #4970, #4984, #4985, #4991, #4992, #4939, #4989, #4988, #4980), thanks @tapetis, @Helium314, @matkoniecz

## v52.1

### Note

If you turned off auto upload in the settings:

When you upgrade to the next release, your local edits will be cleared because incompatible changes were made to the data model. So, be sure to upload them before. The release is scheduled for Wednesday, May 10th. The public beta for May 3rd.

### Enhancements & Fixes

- Surface overlay: Make paving stones and asphalt distinguishable by color (#4946, #4888)
- Another overlay can now be selected from the dialog with one tap only (#4949), by @tapetis
- Pitch sports: Fix sports were not shown in the order of their popularity (#4962, #4922), by @tapetis
- Fix keyboard was not dismissed after selecting a POI in the dialog on certain Android versions (#4961, #4952), by @tapetis
- Do not treat `surface=ground` \+ `tracktype=grade2` as sure mistake (#4863), by @matkoniecz
- Recognize zone 30 (etc.) streets tagged with a more uncommon tag combination, too (#4960)
- Specify type of entrance, entrance ref: Also ask for buildings with an inner courtyard (#4971, #4968), by @Helium314
- Surface & lit overlay: Can now change footways to steps in overlays (#4973, #4938), by @arrival-spring
- Other small fixes and enhancements (#4928, #4929, #4936, #4924, #4945, #4972, #4981...), thanks @BenWiederhake, @matkoniecz, @Helium314

## v52.0

### New overlay for surfaces!

Now you can comprehensively map and verify surfaces mapped for roads and paths (#4378, #4642), thanks @matkoniecz

### New Quests

- _"Is this still here?"_, asked for shops every 2 years (#4232, #4843), by @matkoniecz

### Enhancements & Fixes

- Increase the speed by which the map is updated after an edit by \~30% (#4897, #4906), by @Helium314
- You can now duplicate quest presets (#4609, #4820), by @michalgwo
- Fix some memory leaks and crashes (#4869, #4884, #4896)
- Addresses: Fix block number input in Japan, by @Helium314
- Other small fixes (#4901, #2780, #4880, #4890...), thanks @Helium314

### Quest & Overlay Enhancements

- Addresses: Allow to specify block numbers (#4718, #4850), by @Helium314
- Address overlay: Addresses can now not be added to buildings in Italy (#4801)
- Address overlay: You can now add addresses to entrances (#4823)
- Surfaces: Add "persistent mud" as selectable surface, by @matkoniecz
- Building levels: Require specifying the roof levels, too (with exceptions) (#4874, #4821, #4661)
- Wheelchair accessibility: Also ask for toilets in restaurants etc. (#4866, #4868, #4879), thanks @arrival-spring, @Helium314
- Other small enhancements and wording improvements (#4864, #4883, #4899, #4872, #4907, #4892...), thanks @matkoniecz, @arrival-spring, @bmaggi, @Helium314

## v51.1

### Enhancements

- Also ask in the Netherlands whether shops accept cash (#4826), by @matkoniecz (quest still disabled by default)
- Don't ask anything about benches and recycling containers if they are private (#4815), by @Helium314
- Allow more options when specifying the type of shop, by @matkoniecz

### Fixes

- Solve a problem with getting the GPS location on certain devices and circumstances (#4846, #4652), by @Helium314
- Max height: Open [StreetMeasure](https://play.google.com/store/apps/details?id=de.westnordost.streetmeasure) in vertical measurement mode (#4808)
- Type of entrance: Don't ask for paths that only continue on top of a roof (#4805), by @matkoniecz
- Answering that a ping pong table mapped is gone would not remove it if it was an area (#4816)
- Render highway service areas and healthcare places as areas (#4824, #4809), by @matkoniecz, @dbdean
- Opening hours: Fixed shifting of the form when selecting the closing time (#4763)

## v51.0

### Standalone measuring app

Measuring distances with the camera has been outsourced into a standalone app, so you can also measure things outside of the context of solving quests: [StreetMeasure](https://play.google.com/store/apps/details?id=de.westnordost.streetmeasure)

This has been done for licensing reasons (see #4289). In a nutshell, ARCore (= [Google Play Services for AR](https://play.google.com/store/apps/details?id=com.google.ar.core)), on which the measuring is dependent on, is not open source.  
 This is why it is also not available on the main repo in F-Droid. If you do not have Google Play, you can [download the APK from GitHub](https://github.com/streetcomplete/StreetMeasure/releases), but then you also need to install ARCore.

### New Quests

- _"Is this also here outside of winter?"_, asked for grit bins (#4749, #4754), by @FloEdelmann

### Enhancements

- The standalone measure app now explains how to achieve optimal precision and warns if the potential for a measuring error becomes too big (#4655)
- Parking overlay: Support new street parking schema (#4664, #4768), sponsored by @gislars from [OpenStreetMap Verkehrswende](https://parkraum.osm-verkehrswende.org/)
- Parking overlay: Add option to specify that cars park on alternating sides (i.e. not enough space to park on both sides at the same time)
- Address overlay: Make entering addresses in places with no street names more efficient (#4718, #4751), by @Helium314
- Ask for kerbs also on traffic islands (#3786), by @arrival-spring
- Do not ask various quests for ways that only exist to connect an e.g. footway to the street-way (#4787)
- Notes: You can now preview the to be attached photos by tapping on them (#2497, #4797), by @Helium314
- The name and location of a place is now also shown in the undo dialog (#4767, #4772), by @Helium314
- Other small enhancements, improved wordings, ... (#4762, #4750, #2880, #4765, #4710, #4781, #4745, #4800...), thanks @matkoniecz, @FloEdelmann, @Helium314, @dbdean

### Fixes

- Don't ask whether vacant shop is still the same shop (#4752, #4756), by @Helium314
- (Street) names should not automatically be capitalized in Georgian (#4784, #4785), by @balsoft
- Other fixes (#4774, #4763, #4790...)

## v50.2

- Add new language: Swahili by SUZA Youth Mappers - Asha Omar, Haitham Alawi & Hajra Salim
- Fix existence of cycleway was asked in 30 zones (#4706)
- Fix display of multi-polygons (#4689)
- Fix "Explicitly shared use pavement, both directions" did not show (#4711)
- Fix last answer button for cycleways would (often) set the wrong direction (#4722)
- Other small enhancements and fixes on the cycleway overlay (#4677, #4715, #4721, #4671 ...)

## v50.1

StreetComplete now has [3000 stars on GitHub](https://github.com/streetcomplete/StreetComplete/). Thanks, guys! 🤩

- Fix latinization of Serbian to latin script
- Fix sidewalk, bike paths, road width and lanes quest would often not show up in Romania and Russia (#4684)
- Fix shop overlay would not add name of place when selecting a brand (#4688 #4694), by @matkoniecz
- Fix display of multi-polygons (#4689)
- Fix rare crash (#4685)
- Fire hydrant type quest: Replace deprecated selection of "pond" with "pipe" (see #362)
- Small enhancements and fixes on the bike paths overlay (#4698, #4669, #4701)
- Bike paths quest and overlay: Add "explicitly on sidewalk, both directions" as selectable option (#4692)
- Clarify wordings and other small enhancements (#4682, #4695, #4702, #4699, #1236...), thanks @matkoniecz

## v50.0

### 🚲️️ Cycleway Overlay (#4657, #4233)

There is a new overlay which allows you to review and edit the streetside cycleway infrastructure as well as whether there is any segregation on combined foot- and cycle paths. Sponsored by [FixMyCity](https://www.fixmycity.de/).

### 🚚 Move node (#3502, #4579), by @Helium314

"It’s at slightly different position…" is a new generic answer available for every POI-based quest and overlay. It lets you correct the position of the POI.

### Enhanced Overlays

- Sidewalks: Missing sidewalk information on unpaved roads and on roads with a very low speed limit are not shown in red
- Sidewalks: Support sidewalks mapped on exclusive cycleways
- Street parking: Fix a crash when editing partially (#4634)
- Shops: Add option to explicitly state that a shop has no name (#4648)
- Shops: Fix removing localized names did not actually remove them
- Other small enhancements (see #4657)

### Enhanced Quests

- Surfaces for segregated foot and cycleway: Correct common surface (#4615), by @mnalis
- Disabled cycleway quest by default because it can be done more comprehensively in the overlay
- Road width: Ask for confirmation when adding improbable road widths (#4655)
- Other small enhancements (#4637, #4643, #4649...), thanks @matkoniecz, @tapetis

### Other enhancements and fixes

- Added language: Serbian in latin script (automatically generated from Serbian in cyrillic script)
- Fix quest pins sometimes disappear and labels flicker on moving the map (#4522), by @Helium314

## v49.2

Tiny bugfix release for v49.1, because

- Shop overlay: Fix adding places would only add the name and not the type (regression from #4605)
- Shop overlay: Two small UI enhancements

## v49.1

- Shop overlay: Don't show confirmation dialog when just adding a place (#4605)
- Sidewalk overlay: Don't color missing sidewalks on motorways etc. as red (#4606)
- Shop overlay, Bus stop names, place names...: Show name in several lines if it doesn't fit into one line (#4613)
- Shoulders: Completely disable this quest because the definition in OSM right now is too fuzzy to be useful & easy to answer. See [forum discussion](https://community.openstreetmap.org/t/shoulder-tag-is-confusing/5185). (#4617)
- Other small enhancements and fixes (#4610)

## v49.0

### QR Codes

Share your quest presets with friends via QR Code! Also, check out [streetcomplete.app](https://streetcomplete.app)! (#3642, #3481, #4537). Sponsored by [FixMyCity](https://www.fixmycity.de/).

### My Profile screen

The My Profile screen is now more interesting and engaging! New graphics, new animations, weekly ranks and a calendar of your recent activity! (#2946, #3230, #4572, #4569, #3414, #4203), by @matkoniecz

### Enhanced Overlays

- Shop overlay: Disable 3D buildings for easier placement of new shops (#4563)
- Shop overlay: Allow input of names in different languages (in countries where that's common)
- Shop overlay: On edit, in doubt ask whether it is still the same place or not (#4567)
- Shop overlay, Bus stop names, place names...: Remember user's choice about which primary language to use for multilingual names (#4257)
- Address overlay: Explicitly tag a building as having no address if the user stated this explicitly (#4583)
- Parking overlay: Fix images would not show in countries where they drive on the left side (#4590, #4600), by @tapetis
- Other small enhancements and fixes (#4559)

### Enhanced Quests

- Cycle lanes: Only show "advisory cycle lane" option in countries where a concept of such second-rate cycle lanes exist (#4502)
- Cycle lanes: Adapt appearance of cycle lanes, depending on the country one is mapping in (#4502)
- Cycle lanes, sidewalk surface: Also remember and offer shortcut to apply last selection for roads on which only one side is to be defined (#4524, #4503), by @Helium314
- Bike parking type: add two-tier bike racks as answer option (#4473)
- Do not ask for housenumbers in Latvia (#4597)
- Fire hydrant diameter: Allow quick selection of previous values (#4369), by @FloEdelmann
- Cycleway and footway surfaces: Remove contradicting surface information (#4526, #4548), by @mnalis
- Other small enhancements and fixes (#4560, #4513, #4574, #4601, #4223, #4592, #4603...), thanks @matkoniecz

## v48.0

### M-M-M-Mega Update!

A few things came together... this update contains what would usually be in 4 or so releases! Enjoy!

#### 1️⃣️️ Addresses Overlay

The most efficient way to map addresses on-site, even in places where no buildings are mapped yet!  
You can add addresses to buildings, edit existing ones and also add them at any new position, using the same sleek UI as for the address quests.

#### 🛍️ Shops Overlay

Easily map and check what shops exist in your local shopping area!

#### 🚀 HUGE performance improvements (#4079, #4125), all by @Helium314

The app is now more responsive during normal use thanks to extensive caching and other improvements, in particular, roughly:

- 2x to 8x times faster display of quest pins and overlays
- 2x to 3x times faster showing of quest details
- up to 2x times faster solving of quests
- up to 2x faster upload and thus another 2x faster solving of quests if auto-sync is on (#4077, #4472)
- slightly faster download (#4469)

#### New Quests

- _"How is drinking water provided here?"_ (#4423, #4390), by @matkoniecz
- _"Which direction leads upwards here?"_, asked for mountainbike tracks (#4385), by @matkoniecz
- _"What’s the identification number here?"_, asked for emergency access points (#4384, #4386), by @mcliquid
- _"What’s the identification number here?"_, asked for fire hydrants (#3059, #4440), by @mcliquid
- _"Can this cycle barrier be opened?"_ (#4293, #4406), by @mnalis

#### Enhanced Quests

- Enable quest that asks if cash is accepted in United Kingdom (#4517)
- Clarify some wordings for buildings (#4431), by @matkoniecz
- Do not ask for parking access if some access is already tagged (#4538, #4547), by @mnalis
- Parking overlay: Simplify selecting that there is no parking (#4534)

#### Other enhancements

- Redid UI for selecting the type of shop - better search, with icons from iD! (#3774)
- Redid UI for the housenumber and address quests to be consistent with the overlay
- Add links to documentation for tags changed as shown in undo dialog (#3442, #4419), by @matkoniecz
- The obtrusive downloading-view is gone, downloading and uploading is now shown in the upper left corner
- Resurvey quests no longer vanish after splitting the way - only works if auto-sync is off (#3567, #4523), by @Helium314
- Fix which tags the parking overlay removes when changing the answer (#4501, #4552), thanks @tapetis
- Other small enhancements and fixes (#4453, #4496, #4506, #4494, #4499, #4544, #4540...), thanks @Helium314, @tapetis, @mnalis

## v47.2

- Fix note quests by default would not show up if that note contained several paragraphs (#4482)
- Do not ask for surfaces of private cycleways and footways (#4434, #4433), by @legofahrrad
- Also ask for bus stop identification numbers in Australia and Turkey (#4487, #4489), thanks @andrewharvey
- Other minor enhancements (#4486, #4475, #4490...), thanks @mnalis

## v47.1

- Add links to documentation for tags changed as shown in undo dialog (#3442, #4419), by @matkoniecz
- Do not show quests for kind of shops that cannot be referred to by name or type (#4448)
- Sidewalk and street parking overlays: Allow to only specify the situation on one side (#4427)
- Street parking overlay: Show parking areas semi-transparent in front of street side parkings
- Overlays: Fix possible crash when editing roads with incomplete or invalid tagging
- Other minor enhancements, fixes and crash fixes (#4424, #4429, #4435, #4447, ...), thanks @mcliquid, @Helium314

## v47.0

### 🅿️ Street Parking Overlay (#4177, #4329)

Comprehensively record the physical parking situation along the streets in your neighborhood!  
 Be warned, of the overlays so far, this is the most complex one and the first that does not replace a pre-existing quest 🙂

### New Quests

- _"Can you deposit cash at this ATM?"_ (#4292, #4333), by @mnalis

### Other enhancements

- All overlays now use the same color-blind friendly color-palette for consistency
- Several performance improvements and reduced network traffic on uploading (#4078, #4202, #4319, #4339), by @Helium314
- Performance improvement when scrolling the map (#4283), by @Helium314
- Refine when the entrance type quest is asked (#4372, #4380), by @matkoniecz
- Postbox Cyphers: Now possible to select the cypher for Charles III (#4420)
- Other minor enhancements (#4380, #4381, #4393, #4402, #4413, #4416), thanks @matkoniecz, @HolgerJeromin, @mcliquid

### Fixes

- Fix sometimes quests would vanish after uploading (#4371)
- Fix common crash when using the app for a long time, then sending it to background

## v46.1

- Enable public transport stop quests also when they are mapped as areas (#4341)
- Fix trunk link roads were not displayed on map
- Do not ask about lanes for inaccessible private roads (#4359)
- Other minor enhancements (#4348, #4356...)

## v46.0

### New Quests

Asked for campsites (#4213), by @mnalis:

- _"Who may camp here?"_
- _"Are there showers here?"_
- _"Is drinking water available here?"_
- _"Is there any power supply here?"_

### Disabled Quests

_Orchard produce_, _Building levels_, _Sidewalks_ and _Lit roads_ are now disabled by default because either they are replaced by designated overlays or they overwhelm users (#4264,...).  
You can enable them again in the settings at any time.

### Enhancements on quests

- Road surface: Ask for confirmation when selection contradicts a previous info (#4105, #662 #4063), by @matkoniecz
- Opening hours: Months and weekdays are now (also) shown in the local language
- All bus stop quests: Ask for any public transport stops, e.g. subway, train, ferry, ... (#3374)
- Road width: Also ask for the max width of chokers and chicanes (#3258, #4323)
- Recycling: Support PET as selectable choice (#2885)
- Tactile paving quests now enabled in Portugal (#4325)
- Internet access: Do not ask for backcountry campsites
- Toilet availability: Ask for campsites, too (#4330), by @mnalis
- Steps incline: Fix display issue on Android 5 (#4317)
- Buildings: Remove "show more" button (#3387, #4215), by @matkoniecz

### Other enhancements and fixes

- Fix editing tunnels in overlays (#4269), thanks @matkoniecz
- Fix rare error when uploading too long opening hours (#3471)
- Fix rare faulty links in notes (#4312, #4309)
- Improved map style clarity, especially in regard to overlays (#4291, #4310, ...)
- Performance improvement on uploading (#4297), by @Helium314
- Android 13: Only ask about notification permission when about to show one (#4294)
- More minor enhancements and fixes (#4158, #4226, #4307, #4302, #4271...), thanks @Helium314, @peternewman

## v45.2

- Fix rare crash when phone is low on memory (#4221)
- Fix rare crash when closing or sending the app to background (#4259)
- Fix drawing order of bridges and tunnels, by @matkoniecz
- Fix a layout issue for right-to-left locales (#4260)
- Max weight: Fix input field not showing more than one digit (#4216)
- House numbers: Do not warn about a house number format being unusual that is common in Australia (#4196, #4251)
- Roof shape: Do not ask for ruins and underground buildings, by @matkoniecz
- Roof shape: For certain building types, even ask when building and roof levels have not been recorded yet (#4261), by @matkoniecz
- More minor enhancements (#4160, #4241, #4243, #4246, #4242, #4249...), thanks @Helium314, @matkoniecz

## v45.1

- Fix the same recorded GPS tracks was attached to subsequently created notes (#4199, #4204), by @goldbattle
- Fix crash when trying to upload notes with a long description and attached GPS tracks (#4195)
- Fix weird filenames for the GPS tracks uploaded (#4208), by @peternewman
- Fix "keep screen on" option was not respected before app restart (#4189)
- Fix sidewalk overlay should not ask whether to discard changes when there are none (#4206)
- More minor enhancements (#4187, #4200, #4160...), thanks @matkoniecz, @peternewman

## v45.0

### 🍰 Overlays!

Make every survey unique! How? Dedicate each to updating a different aspect of the map by activating a different overlay, respectively!

This is **for advanced users** who want to be more efficient in adding and maintaining certain data area-wide. Read more in [this blog post](https://www.openstreetmap.org/user/westnordost/diary/399378)!

For now, there are only overlays for _street lighting_ and for _sidewalks_, more will be added over time. 🎉 (#2461, #2506, #3920, #3644)

### New Quests

- _"What kind of memorial is this?"_ (#4102, #1572), by @matkoniecz, thanks @kmpoppe, @naposm, @FloEdelmann, @mnalis
- _"Is there a summit cross here?"_ (#4111, #4095), by @FloEdelmann
- _"What is signed at this entrance?"_ (#4066, #3064), by @matkoniecz

### Enhancements and Fixes

- Consider that cycle lanes on the right side of the road do not always go in forward direction (and respectively for countries with left-hand traffic)
- Do not require OpenStreetMap read/write GPS track permission for login (the record GPS track feature can then simply not be used) (#4122)
- Improve GPS accuracy for some devices (#4139), by @Helium314
- In dark mode, remove obtrusive glow from railways and motorways, by @Helium314
- Ask about bus stop reference numbers in Ireland (#4161)
- Stop asking about toilet fees in US and Canada (#4174), by @matkoniecz
- Other small enhancements and performance improvements (#3919, #4120, #4138, #4181, #4184, #4180, #4183, #4164...), thanks @Helium314

## v44.1

- Fix: Tag `barrier=kerb`, not `curb` (#4087), by @FloEdelmann
- Fix recorded track was attached to every subsequent note (#4082), by @goldbattle
- Fix summit register quest, it did not show before (#4075), by @Helium314
- Some other tiny fixes and enhancements (#4084, ...)

## v44.0

### 🧭 Recording GPS track

Did you ever notice some path missing on OSM while solving quests? Now, you can walk that path and then attach it to a public note!  
Note that the track can only be recorded while the app is in foreground. So, it is not meant to replace tools like GPS Logger but to help locate short missing path segments and similar.  
(#3573, #2209), by @goldbattle

### New Quests

- _"What type of bicycle rental is this?"_ (#3995, #4010), by @FloEdelmann
- _"How many rental bike spaces are here?"_ (#2129, #4008), by @FloEdelmann
- _"Are bicycle repair services offered here?"_, disabled by default (#4012, #4038), by @matkoniecz
- _"Are second-hand bicycles sold here?"_, disabled by default (#4012, #4038), by @matkoniecz
- _"What kind of building entrance is this?"_ (#3941, #3949), by @matkoniecz

### Enhancements and Fixes

- Clarify a lot of wordings in American English (#4030) by @ZeLonewolf
- Improve some aspects of the background map rendering, by @Helium314
- Improve performance when using the app in a long session (#4070), by @Helium314
- Various minor enhancements, wording improvements and fixes (#4015, #4042, #4001, #4039, #4032, #4056...), thanks @Helium314, @peternewman, @riQQ

## v43.2

Sorry, I fucked something up. You need to log in again. 🙇‍ Don't worry, your not yet uploaded edits are safe. (#4028)

## v43.1

### Enhancements and Fixes

- Correct hint text for shoulder quest: Shoulders that are not wide enough for a car are still considered shoulders as long as they are designated (i.e. marked) as such
- Cycleways: For oneways, make it clearer what the selection options imply for cyclists (#3984)
- Don't ask about tactile buttons on traffic lights in Russia (#4021)
- Various minor enhancements and fixes (#3994, #4020...), thanks @peternewman
- Fix new languages (Armenian, Esperanto) are now displayed as options in the language selection screen

### New Translations

- Added Latvian, by @Mazurs

## v43.0

### New Quests

- _"How many steps are here?"_, asked for stiles (#3932, #3141), by @matkoniecz
- _"Is there a working bicycle pump available for everyone here?"_, asked for bicycle shops etc. (#3934, #3053), by @mnalis
- _"Is there an air compressor available here?"_, asked for fuel stations (#3934, #3053), by @mnalis
- _"Are credit or debit cards accepted here?"_, disabled by default (#3953), by @matkoniecz

### Other Enhancements and Fixes

- Do not ask questions about objects tagged as seasonal (#3943)
- Fix in rare cases answered sidewalk quest would not go away (#3976)
- Kerb type: Remove kerb if user answered that there is no kerb at all (#3983)
- Ask about opening hours and wheelchair accessibility for a few more kind of places (#3982), by @dbdean
- Better indicate when there is no GPS signal (#3944, #3977)
- Various minor enhancements and fixes (#3931, #3933, #3717, #3951, #3698, #3795, #3972, #3978, #3962, #2652...), thanks @mnalis, @CJ-Malone, @matkoniecz

### New Translations

- Added Armenian, by Alexander, Vasak87 and Anteneh Belayneh
- Added Esperanto, by Jolesh, Jan, Martin Constantino-Bodin, Hendursaga, Robin van der Vliet, Fabio and more

## v42.0

### General Enhancements

- Quest titles are now shorter and more consistent (#3870, #3903), thanks @FloEdelmann, @peternewman

### New Quests

- _"Is smoking allowed anywhere in this place?"_ (#539, #3784, #3856, #3865), by @mnalis
- _"What is the surface of the sidewalk here?"_ (#1593, #3735), by @arrival-spring
- _"What kind of seating does this place have?"_ (#1176, #3758, #3911), thanks @fatal69100

### Quest enhancements

- Parking fee: Add answer option for when there is just a max parking duration (#3841, #981, #102)
- Sidewalk: Also ask when current tagging is incomplete or invalid (#3821), by @arrival-spring
- Tactile paving on crossings: Add answer option for when it exists only one one side (#3844, #3813), by @arrival-spring
- Barrier details: Add answer option for when there is a different type of barrier (#3846, #3563, #3845, #3817), by @mnalis
- Oneway streets: Also ask for roads that are below or equal 4 meters wide
- Cycleway: Warn before removing that cyclists may use a oneway road in both directions (#3795)
- Halal: Don't ask for vegan only places (#3883), by @peternewman
- Fire hydrant position: Allow deletion of hydrant if it doesn't exist (#3871)
- Housenumbers: More convenient input for housename & housenumber, plus fixed various small issues (#3904, #3907)

### Enhancements and Fixes

- Fix crash issues on Android 5 (#3889, #3914), thanks @FloEdelmann
- Fix shop type suggestions were not available in Norwegian (Bokmål) and Serbian (#3890, #3896, #3897), thanks @matkoniecz
- Added support for region or script-specific shop type suggestions, e.g. English in New Zealand or Serbian in Latin or Cyrillic (#3897)
- Fix rare crashes (#3888), thanks @tapetis
- Other minor enhancements and fixes (#3852, #3893, #3895, #3867...), thanks @matkoniecz, @mnalis, @Cj-Malone

## v41.2

- Improved accuracy of measuring with AR (#3832) but also tag that it has been measured with AR
- Cycleway and road width, number of lanes: Don't ask for sections that have explicitly been marked as "transitions" (#3835, #3842), by @mnalis
- Cycleway width: Fix the quest did not disappear after solving in rare cases (#3843)
- Number of lanes: Also ask for trunks and motorways (#3849), by @matkoniecz

## v41.1

- Fix: ask for camera permission when attaching a photo to a note
- Fix: app v41.0 didn't show on Google Play

## v41.0

### Measure with the camera (AR)

Measuring with AR is only supported [on certain devices](https://developers.google.com/ar/devices#google%5Fplay%5Fdevices) and you need to have the Google Play Services on your phone. If AR is not available, the new quests are disabled by default because you'd need to use a tape measure instead. (#3709, #879)

### New Quests

- _"What’s the width of this cycleway here?"_ (#698)
- _"What’s the width of this road here?"_
- _"What is the height limit here?"_

### Quest enhancements

- Add "handlebar holders" as bicycle parking type option (#3061, #3724), by @sams96
- Tapping on road in street name sometimes did not work (#3797)
- Other small improvements (#3740, #3755, #3793, #3783, #3814, #3811), thanks @matkoniecz, @tiptoptom, @peternewman, @arrival-spring

## v40.2

Google Play version only: In the context of donating, removed link to source repository because this is now also against their policies (see #3768).

## v40.1

### Enhancements and fixes

- Faster downloads and faster display of nearby data (#3741), by @Helium314
- Faster initial download when starting the app
- Other small improvements, quest enhancements and UI fixes (#3736, #3757, #3719), thanks @mnalis, @matkoniecz
- Fixed display issue when going through the tutorial too fast (#3767)

### Un-Features

- Google Play version only: Removed donation links in the about screen because this is now against their policies unless those links lead to their own billing system (see #3768).

## v40.0

### Important: Update manually after Feb 21!

Google Play / F-Droid will not update to the next major version v41 automatically because the app will ask for an additional permission. In v41, you'll be able to measure widths and heights with the camera!

### New Quests

- _"How does this road cross the barrier here?"_ (#3372, #3515), by @matkoniecz
- _"Can you pump gas yourself at this fuel station?"_ (#2822, #1827), by @naposm
- _"Does this place have air conditioning?"_ (#3641, #3127), by @coolultra1
- _"Are the opening hours signed for this place?"_, asked at intervals for places that have previously been tagged as having no signed opening hours (#3682, #3130)

### Quest enhancements

- Allow answering whether there is a shoulder for each side individually (#3659)
- Allow answering whether the sidewalk is displayed separately on the map for each side individually
- When asking where a hydrant is located, show example pictures of pillar hydrants if it is a pillar hydrant (#3686, #3695, #3711), by @FloEdelmann
- Ask opening hours, place names and wheelchair accessibility for more amenities in the healthcare sector (#3710), by @FloEdelmann
- Other small tweaks in UI, quest filters and wording (#3593, #3726, #3657, #3727, #3719, #3693, #3712, #3722, #3716), thanks @matkoniecz, @peternewman, @smichel17, @arrival-spring

### Fixes and improvements

- More accurate conflict detection
- Fix a few rare crash issues

## v39.1

### Fixes

- Fix housenumbers, road names etc. were not visible on the map on Android 12 (by @matteblair)
- Fix build for F-Droid (#3667)
- Fix recycling materials quest would delete also tags that don't start with "recycling:" but have "yes" as value

## v39.0

### New Quests

- _"Does this road have a shoulder?"_ (#2444, #3613)
- _"What is the surface quality of this road here?"_ (#3617, #3257, #1630, #3633), special thanks to @Helium314 but also to @FloEdelmann, @mcliquid, @NicoHood, @rhhsm, @mnalis, @1ec5 and many more  
  It is disabled by default, enable it in the settings to try it out!
- _"Where is this fire hydrant located?"_ (#3368), by @thefeiter

### Other enhancements

- You can now answer that a building has a house name _and_ a number (#1983, #3582), by @matkoniecz
- Some improvements on the fire hydrant quest(s) (#3608), by @tiptoptom and @thefeiter, (#3601)
- Some minor enhancements and clearer wordings (#3576, #3610, #3599, #3653...), thanks @matkoniecz, @olo81
- Added "Lifesaver" achievement (#3616), by @FloEdelmann

### Fixes

- Fix rarely quests don't go away after solving them (#3290)
- Fix rarely quest answers don't get uploaded automatically until next restart of app (#3494)
- Fix a few crashes (#3622, ...)

## v38.2

### Other enhancements

- You can now answer that a playground is only for customers (#3433), by @matkoniecz
- The fire hydrant diameter is now only asked for underground or pillar hydrants (#3575), by @matkoniecz
- Some minor enhancements

### Fixes

- Fix rarely quests were not removed after giving an answer (#3588)
- Fix an issue with in-app language switching (#3554)
- Fix a rare crash issue when tabbing out while quest form is open (#3590)
- Fix opening a geo url (#3580)
- Fix order in which selections are shown in the religion quests (#3570), by @matkoniecz

## v38.1

- fix location wasn't updated after tabbing out and in again (#3543), thanks @smichel17
- improve some wordings (#3528, #3529), thanks @1ec5

## v38.0

### New Quests

- _"What type of bicycle barrier is this?"_ (#3361, #3487), by @matkoniecz
- _"On which level is ... located?"_ (#1487, #3509)

### Quest Enhancements

- Also ask for barriers on a pedestrian railway crossing (#3277)
- Also ask for the street address for buildings with no number but a name (#3457), by @arrival-spring
- On resurveying postbox collection times, show current collection times first (#2105, #2986), by @eginhard
- You can now specify the name of a place in multiple languages (#2610, #3317)
- You can now again answer whether a way is lit also during the day. Starting in v34 it was only shown during the night but many people didn't like that feature (#3248)
- Clarify some wordings (#3227, #3483, #3429), by @matkoniecz

### Other enhancements

- Nearby relevant elements are now shown in context when answering various quests (#3480, #3338, #2354)
- Allow switching the language in the app (#3199, #2643, #3512), thanks @amenk
- Other small enhancements and improvements

### Fixes

- Fix some crash issues (#3517, #3522)

## v37.2

Fix a common crash issue when opening certain quests (#3504, #3506, #3510, #3511)

## v37.1

- Fix rare layout issue when creating a note with a photo (#3405, #3474), by @smichel17
- Fix entering brand names when asked what (shop) is here now (#3483)
- The "delete map cache" option in the settings now also deletes the background map (#3475)
- Don't ask for type of crossing on driveways, even if it is tagged as a crossing
- Some stability fixes
- Translation updates

## v37.0

### New Quests

- _"What diameter is on this fire hydrant’s sign?"_ (#3342), by @thefeiter
- _"What type of traffic calming is this?"_ (#2742, #3380), by @matkoniecz

### Quest Enhancements

- Add concrete lanes and concrete plates as answer options for surfaces (#2437, #3354), by @matkoniecz
- Remember and offer to apply the last answer for the cycleway and sidewalk quest (#2542)
- Don't ask whether a street has a cycleway or sidewalk if there is a construction nearby (#3436) by @matkoniecz
- In Norway, all cycle lanes are exclusive, thus don't offer to distinguish between advisory and exclusive ones in cycleway quest (#3438)
- The top answers in the building type quest are sorted smarter (#1771, #3373), by @smichel17
- Ask for more places whether they have vegan food etc. (#3431, #3455) by @peternewman
- Allow answering that a café sells no food at all when asked if they have vegan food etc. (#3408, #3422) by @matkoniecz
- Ask again for the type of stile every 8 years (#3187, #3370), thanks @matkoniecz, @peternewman
- For stile type quest, add option to state that it is now something other than a stile (#3409, #3188, #3391), by @matkoniecz
- Many minor (UX) enhancements (#3386, #3390, #3421, #2897, #3383, #3443, #3396, #3427), by @matkoniecz

### Other enhancements, fixes, etc.

- Edit history sidebar now appears immediately on tapping the undo button, last edit is at the bottom and pre-selected (#3349, #3435, #3426), by @smichel17
- Fix (speed limit, max height, max weight, ...) signs should have a yellow background in Sweden, Finland and Iceland (#3369, #3393)
- Fix "public transport" was displayed twice in board type quest (#3365), by @kmpoppe
- Fix "give feedback" link in about screen didn't work. Now you can give us your feedback! 🙂
- Fix quest selection screen didn't update when changing the preset (#3430)
- Fix Serbian translators weren't listed in the credits screen
- Slight battery-usage optimization regarding access to location (#3420, #3424) by @Isira-Seneviratne

## v36.1

- Faster startup and initial download when opening the app (#3337)
- Faster clearing of quest cache (an option in the settings) (#3337)
- Fix GPS button sometimes displays wrong mode (#3345)
- Fix layout for profile screen on very small devices (#3388)
- Fix a few causes for common crashes and application not responding
- Updated translations

## v36.0

### Easier orientation

🖊️ The path you went today with the app is now marked on the map for better orientation! (#2209)

The location button (lower right corner) also has a new behavior:

- 🛰️ Tap it once to let the view follow your position on the map (icon turns orange)
- 🚴 Tap it again to let the view rotate with the direction you are going (icon turns into an arrow). Very helpful for cyclists! (#1040, #2191, #3335)

Furthermore, the only thing the compass button now does is to rotate the view back to north-up and it is only displayed if the map is not displayed north-up.

### New Quests

- _"Is this picnic table covered (protected from rain)?"_ (#3311, #3142), by @FloEdelmann
- _"Does ... offer halal products?"_ (#2990, #3331), by @SMUsamaShah

### Quest Enhancements

- Add answer option to state that there are actually steps, not a footway (#3288, #3334), by @FloEdelmann
- Building type: Add building bridge (skyway) as answer option (#2630, #3320), by @kmpoppe
- Self-service laundry: Add answer option to say self-service is optional (#3309)
- Don't ask some of the quests for private things (#3356, #3357, #3358), by @dbdean

### Other enhancements, fixes, maintenance

- More contrast on text (#3328), by @matkoniecz
- (#3327) by @FloEdelmann, (#3208) by @matkoniecz

## v35.2

Fix crash on entering quest selection screen

## v35.1

### Fixes

- Fix sometimes notes would be created at wrong positions when tapping OK too fast after taking a photo (#3312), thanks @mnalis
- Fix rare abortion of download when handling corrupt/invalid OSM data
- Fix some more rare crashes

## v35.0

### App maintenance

- Android 4.x support has been dropped. As a consequence, the app's download size is reduced by 40%! (#2031), thanks @Isira-Seneviratne
- Translator credits are now generated automatically (#3214)
- The uploading of changes will now never be cancelled by the Android system after the app has been sent to the background for a while (#3279)
- Various code maintenance and refactorings (#3274, #3223, #3259, #3273, #3296, #2894, ...), thanks @sumanabhi, @Isira-Seneviratne, @peternewman, @smichel17

### Quest Enhancements

- Clearer illustrations for the bridge structure quest (#2867), by @TurnrDev
- Quests are now shown for arbitrarily long ways too, you can split them up if you only surveyed a part of it (#3280, #3234), by @dbdean
- Now only known tags are cleared when answering that a place doesn't exist and there is something new now (#3244, #3278), by @mnalis
- Other small enhancements (#3185, #3171)

## v34.2

### Fixes

- Fix sometimes notes would be created at wrong positions if the follow-me mode was on (#3284), thanks @smichel17
- Fix sometimes one would be notified of all achievements one ever achieved on logging out and in again (#3282)

### Quest Enhancements

- Don't ask for the building type of airport buildings (#3233), by @matkoniecz
- Don't ask if a marina is wheelchair-accessible (#3270), by @matkoniecz
- Never ask whether there is tactile paving where the sidewalk crosses a driveway (#3301), by @smichel17

## v34.1

### Enhancements

- If you answer that a place has no vegetarian food, it also means that it has no vegan food (#3253) by @Helium314

### Fixes

- Fix crash when asking whether a traffic calming (etc.) still exists (#3261)
- Fix crash during scanning for quests
- Don't delete certain tags when selecting that a shop is now vacant (#3244)

## v34.0

### 🚌🚴 Quest presets

- Show quests that are quick and easy to solve (from a distance) first by default. This makes contributing more efficient and diverse while also easier to do as e.g. a passenger (#2944, ...)
- You can now save different quest presets for different situations, e.g. one preset when you only want a solve some important quests while on a bus, or on a bike tour etc.  
  This was a much requested feature: (#1301, #1654, #1746, #1987, #2054, #2069, #2279, #2565, #2308, #3034, #3216, ...)
- Inform new users once about the possibility to change quest presets
- Add search/filter function for the quest presets settings (#3008)

### New Quests

- _"Are there curbs where this way meets this road here? What kind?"_ (#398, #2999)

### Enhancements

- Ask whether a road has a cycleway when the current tagging is invalid (#3206, #3148)
- The quests that ask whether roads, ways etc. are lit are now only shown at night (#1285, #2872), by @TurnrDev
- The roof shape quest is no longer enabled by default - you can enable it in the settings (#3161), by @matkoniecz
- Clearer wording when explaining the team mode (#3170, #3172) by @matkoniecz and @smichel17
- Ask for more disused places whether they are no longer vacant (#3158)
- Add Animism as choice for when asked about the religion of a place (#3063)
- Add more building type choices: allotment house, boathouse and grandstand (#3078)
- Stone stiles are now more clearly selectable (#3175) by @matkoniecz
- More minor quest enhancements and clarified wordings: (#3139) by @john-h-kastner, (#3117) by @smichel17, (#3165) by @yrtimiD, (#3194) by @ZeLonewolf, (#3200) by @matkoniecz (#3092, #3149, #2760, #3116...)

### Fixes

- Fix layout for right-to-left languages (Persian, Arabic) (#3181, #3184)
- Fix steps ramp quest could seemingly in rare cases not be answered (#3115)
- Fix tapping compass rotates with phone direction even when still searching for GPS location (#3147, #3166), by @smichel17
- Fix crash when displaying opening hours whose range ended at 12 AM
- Fix couldn't press OK anymore after pressing cancel in the dialog that asks if you are really on-site (#3198)
- Fix crash when asking for the religion of a place (#3217)
- Clear user statistics properly when logging out (#3168)
- Remove location pointer pin from UI when GPS position is lost (#3213), by @smichel17
- In the edit history, scroll to item when it is selected on the map (#3226)
- Fix a few more crashes

## v33.2

### Fixes

- Fix that the app wouldn't start on Android 4.x (#3069) which was broken since v32.0.  
  Android 4.x support will be dropped in an upcoming version.
- Ask about tactile paving on kerbs also when the type of kerb is already set (#3104) by @jyasskin

### Other Enhancements

- Amharic (አማርኛ) translation by Anteneh Belayneh, Alexander Menk (www.AddisMap.com)

### Quest Enhancements

- Number of lanes: Don't ask if they have been recorded for both sides individually already (#3038), by @FloEdelmann
- Enable cycleways and tactile pavings quest for Croatia (#3041), by @mnalis
- Allow splitting ways when asking whether a set of steps has a handrail (#3037), by @peternewman
- Allow answering that a place does not exist when asking whether it offers Kosher food (#3073), by @peternewman
- For pubs that definitely serve food, also ask whether they have vegetarian and vegan food (#3097, #3099), by @peternewman
- Don't ask whether a place offers vegetarian food if it only offers vegan food (#3072, #3081), by @starsep
- Don't ask for the existence of things that are seasonal, e.g. loungers that can only be found in the park during summer (#3089), by @matkoniecz
- Periodically ask if a drinking fountain still exists (#3102)
- Clean tags from previous vacant shop when answering that there is a new shop now (#3045), by @TurnrDev
- Improve illustration in building levels quest (#3092)

## v33.1

- Fix crash when tapping on an achievement in your profile screen (#3027)
- Only ask for bollard type for bollards on roads (#3017)
- Updated translations and other small fixes

## v33.0

### New Quests

- _"What type of surveillance camera is this?"_ (#87, #299, #2856), by @Helium314, thanks @Binnette
- _"Does this bus stop have a waste basket?"_ (#2760, #2898), by @FloEdelmann
- _"What type of bollard is this?"_ (#2128, #2915), by @FloEdelmann, thanks @RubenKelevra

### Quest Enhancements

- Remember more building level quest answers (#1772, #2925), by @FloEdelmann
- In the address quest, when answering that the building type is not correct, follow-up by asking about the building type (#3001)
- A few minor improvements for various quests (#2971, #3000, #3012, #3013, #3002...)

### Other Enhancements

- Added Bosnian translation by OSMcontributorBH
- Added Thai translation by Mishari, PPNplus, Kamthorn and more

## v32.2

### Fixes

- Fix many crashes (#2949) by @tapetis, (#2981, #2985, ...)
- Fix quest wouldn't disappear in a rare situation (#2958)
- Only ask again whether a road is lit for road types for which the app would usually ask (#2955)
- Don't ask user to send an error report if photo upload failed due to a connection issue (#2984)

### Enhancements

- Some performance improvements
- Ask some quests in more situations (#2941) by @FloEdelmann, (#2927, #2935) by @matkoniecz, (#2939, #2951, #2953, #2816)
- Enable the "Does not exist" answer for more quests (#2913, #2914), by @matkoniecz
- More small quest/UI enhancements (#2911, #2926) by @tapetis, (#2836)

## v32.1

### Fixes

- Fix possible crash when downloading on a wonky internet connection (#2912)
- Fix edit history sidebar does not appear when animations are off (#2904)
- Fix display glitch in quest statistics screen (#2906)
- Fix crash when solving quest with shaky finger

### Enhancements

- You can now click links in notes (#2905, #2918), by @FloEdelmann
- Added woodchips as surface option for paths (#2889, #2920), by @FloEdelmann
- Ask _"What is the royal cypher on this postbox"_ for more territories that have those (#2922), by @TurnrDev

## v32.0

The grant from the [Prototype Fund](https://prototypefund.de/en/project/streetcomplete/) ended in February, however, there is one last big mega-update I can delight you with before development on this app must necessarily wind down:  
It may not look like a lot, but I basically replaced the whole architecture of the app (#2506, ...) to do this. Note that this required dropping most local data, such as unsynced edits, downloaded quests and statistics.  
Special thanks to @FloEdelmann for helping with the refactor.

### 🔙️ Superpowered Undo

- You can now **undo any edit** that hasn't been uploaded yet:  
  quest answers, split ways, deleted places, hidden quests, created notes and note comments (#1029, #2616)
- You can now look at your **edit history** and undo any recent edit in any order, not just the last one
- It is now shown what exactly is tagged for each edit in the edit history
- You can now undo deletions of places even after you have uploaded it (#2441)

### ⛰️ Improved usability and offline mode

- Immediately unlock new quests based on the one you just solved, **even when offline**. This has been a much requested feature but was simply not possible with the previous architecture (#1369, #1510, #1550, #1826, #2254, #2438, #2473, #2474, #2676, #2743...)
- The above also works when splitting a way. The quests for the split segments will be shown immediately
- When you scan for quests manually, no cache will be used: you always get the freshest data (#2554)
- When unable to fully upload pictures attached to a note due to a bad connection, the app will try again later (#2102)

### New Quests

- _"Is this sport field lit?"_ (#2639, #2737), by @eginhard
- _"What type of barrier is this?"_ (#2739, #2753), by @matkoniecz
- _"What type of stile is this?"_ (#2749, #2766), by @matkoniecz
- _"What type of shop is this?"_ (#2391, #2759), by @matkoniecz
- Italy only: _"Which type of police station is this?"_ (#2456, #2675), by @naposm
- _"Is this bus stop lit?"_ (#2383, #2846), by @TurnrDev

### Quest Enhancements

- Don't ask for building levels for industrial buildings (#2835), by @matkoniecz
- Improve referring to map features by name for some quests (#2840)
- Checking existence: ask for some map features less frequently, by @matkoniecz
- Enable answering that the cycleway is displayed separately on the map (#2525, #2665)
- Some wording improvements (#2728), by @matkoniecz, (#2806, #2814) by @peternewman, (#2685)
- Enhance maxspeed collection for the United Kingdom (#2745, #2748, #2750, #2811), by @arrival-spring
- Periodically ask for the existence of BBQ spots, ticket validators, emergency life rings and emergency phones (#2679), thanks @arrival-spring
- Also ask for the surface of service roads as long as they are not driveways (#2783)
- Don't remove the `check_date` tag if it is already tagged, instead, update it (#2861, #2883)
- Don't tag `foot=use_sidepath` on the road if there is a separate sidewalk (#2895)

### Other Enhancements

- Added Arabic by Charbel Bechara and others
- Added Romanian by Georgian Iosef and others
- Improvements in the quest selection screen (#2243, #2847, #2828, #2843, #2887), by @FloEdelmann
- Slightly more performant compass
- When showing all notes, really show all except those where the user commented last (#2692)

### Fixes

- On tablets, correctly restore the position when reentering the app (#2659, #2311), thanks @matteblair
- Sometimes the link collection wouldn't show (#2801, #2809), by @tapetis
- The keyboard would not appear in the opening hours times dialog (#2799, #2820), by @tapetis
- The flags in the statistics view were sometimes wrongly stretched (#2819, #2831), by @tapetis
- The time picker for the postbox collection times now follows the system settings (#2807)
- Fix crash on trying to display opening hours that range to 24:00 (#2830)
- Remove checking existence of grit bins, as many are removed for summer and not marked as seasonal :( (#2726), by @matkoniecz
- Fix quests that are very close to notes could in rare cases not be opened (#2853)
- Fixes on specifying shop type quest, by @peternewman, @matkoniecz
- ...and other fixes and enhancements (#2521, #1821, #2628, #2691, #2715, #2870, #2891...)

## v31.3

**Remember:** Sync any edits before upgrading to the next major version, which will be released around the end of April. If you have auto-sync on (the default), this does not concern you.

### Enhanced Quests

- Also ask for tactile pavings if explicitly set to "unknown" (#2738)
- Periodically ask if grit bins still exist (#2726, #2734), by @arrival-spring
- Don't ask for traffic signals features for blind people on bicycle-only crossings (#2751), by @Helium314
- Don't ask for the max height of parking entrances for pedestrians (#2736)

### Fixes

- Fix error when asking checking if a bus stop still has a bench (#2746)
- More fixes... (#2733 by @UKChris-osm, #2725)

## v31.2

**Remember:** Sync any edits before upgrading to the next major version, which will be released around the end of April. If you have auto-sync on (the default), this does not concern you.

### Enhancements

- Opening hours (etc.) are now displayed with AM/PM if that's how times are displayed on your phone (#2709)
- Don't ask for housenumbers of buildings that are within an area that already has one tagged on its outline (#2686)
- Ask periodically if various traffic calmings still exist (#2672)
- Don't ask for the crossing barrier for disused railways (#2695)
- Also ask for opening hours of shops with no signed name (#2716, #2718), by @Helium314
- Vacant shops are now tagged as `disused:shop=*` (#2707, #2724), by @matkoniecz
- Don't ask for the kerb height and type if tagging is suspect (#2654), by @matkoniecz
- Other small enhancements and fixes: #2723, #2677 by @Eginhard, #2699 and #2680 by @matkoniecz

## v31.1

### Warning: Upload your edits!

If you have auto-sync on (the default), this does not concern you, as all edits are uploaded immediately.  
Due to big changes under the hood, when you upgrade to the next major version, local edits will be dropped! The next major version v32.0 will be in beta not earlier than in 2nd week of April.

### Fixes

- Fix crash issue when zooming too fast with the pinch gesture (#2601)
- Fix link to the wheelchair-routing on openrouteservice (#2646)
- Fix roof quest would show up for too many places (#2666, #2667, #2655), by @Helium314, by @matkoniecz
- Fix the correction of magnetic North to true North (#2653), thanks @pkoby

### Enhancements

- Always show notes on the map that contain "#surveyme" (#2641)
- Do not ask if a foot+cycle path is segregated if it already has a sidewalk (#2644)
- Ask for opening hours and wheelchair eligibility for consulates and other diplomatic offices (#2645)

## v31.0

### 🤝 Team mode!

(#2189, #2557) by @FloEdelmann

Lockdown fatigue? You could go on a walk together with your roommate or partner – with the team mode, you can evenly divide up all quests among yourselves and map together!

It also works for groups of up to twelve people, but maybe wait with that until the pandemic is over 😷. Happy mapping!

### New quests

- 👑 _"What is the royal cypher on this postbox?"_, only in the UK (#2563, #2583), by @arrival-spring

### Enhanced Quests

- Building type: Add fire station as selection (#2476, #2579)
- Recycling containers: Add beverage cartons as choice (#2502, #2580)
- Check existence: Also ask for public bookcases
- Roof shape: Don't ask for high buildings (#2609)
- Orchard produces: for the Canaries, sort choices by prevalence (#2596), by @erik55
- For various quests, also display the name, brand or operator of the point of interest (#2593, #2594), by @FloEdelmann

### Fixes

- Fall back to English when displaying untranslated feature names (#2608)
- Cycleways: Do not implicitly tag a sidewalk if it is not known for both sides (#2633)

## v30.1

- Max height: Do not ask for roads that merely connect to bridges (#2555)
- Kerbs: Do not ask for kerbs on nodes that are intersections with another road or path (#2564)
- Roof shape: Also ask for buildings where no roof levels have been specified (#2556), by @dbdean
- Check existence: Ask for parking ticket machines every few years too (#2567), by @peternewman
- Traffic signal sound and vibration: Better changeset comments (#2578), by @arrival-spring

## v30.0

Did you already [take the survey](https://osmf.limequery.org/281662) of the OpenStreetMap Foundation? It's open only till Feb 14.

### New Quests

- _"Is the water potable here?"_ (#549, #2509), by @FloEdelmann
- _"Do you have to pay to park a bike here?"_ (#2507, #2517), by @FloEdelmann
- _"Is it restricted who may park a bike here?"_ (#2496, #2517), by @FloEdelmann

### Enhanced Quests

- Ask for the surface of sport tracks (#2468, #2310), by @FloEdelmann
- More opening hours in non-standard syntax are understood and displayed on resurvey, thanks @simonpoole
- Check existence: Display brand name if object has no name (#2512)
- Sidewalks: Ask for residential roads even if they are not lit (#2519, #2531), by @riQQ
- Recycling materials: Do not ask for private containers (#2515)
- Road name: Do not ask if name:left or name:right is defined already (#2526)
- A few clarifications in wording (#2467, #2431, #2539)

### Fixes & More

- Added Bulgarian translation by Plamen and Kalin
- Update geometry of displayed elements after another download (#2503)
- Some code modernizations (#2495, #2501, #2504, #2511) by @FloEdelmann
- Do not ask for kerb properties for kerbs on pedestrian areas (#2552)
- Fixed misplaced quest pin and selection ring (#2548, #1810), thanks @matteblair

## v29.1

### Fixes

- Cycleways: Fix regression that it was asked in 30 zones (#2448)
- Fix quest did not show up again after undo of its answer (#2418), thanks @thefeiter
- Fix upload problem if exactly the intended change has already been made server-side

### Enhancements

- Smoothen view direction in compass mode more (#2465)

### Enhanced Quests

- Cycleways: rearrange a selection option (#2310)
- Road access for pedestrians: improve wording in English (#2472)
- Road access for pedestrians: don't ask if there is a shoulder
- Speed limit: do not ask for cycle streets (#2488)

## v29.0

### 🗺️ Map available offline!

In the area quests are downloaded, the background map is now also downloaded into the cache.

So, when you are offline later, you can continue to use this app in that area without any problem because the background is also available (#122, #2428).

To download not at your current GPS location but somewhere else, just pan and zoom to that location and tap "search for quests here" in the menu.

### New Quests

- _"What surface does this sport field have?"_ (#1170, #2377), by @matkoniecz
- _"Does ... offer kosher products?"_ (#639, #2244), disabled by default, by @matkoniecz

### Enhanced Quests

- Construction quest: Make it possible to select date at which construction will be finished (#1341, #2402), by @matkoniecz
- Cycleways: Provide hint what to answer if there is no cycleway at all (#2285, #2405), by @matkoniecz
- Housenumbers: Do not ask in France (#2427), by @matkoniecz
- Tactile pavings: enable for Denmark (#2463)
- Crossing islands: Ask for more crossings (#2454, #2455), by @dbdean
- Ask for sidewalks, cycleways and lanes for residential roads in the US if the speed limit is above X miles per hour (#2448)

### Fixes

- Answer that a thing does not exist anymore, sometimes wasn't uploaded correctly (#2419)
- Sometimes during download, some quests would appear and then vanish again (#2430)
- Android 11: Fix note creation dialog gets covered by keyboard (#2442)
- Fix sometimes the pin would not vanish after solving the quest (#2447)
- Fix crash when discarding the new mail dialog when it animates in (#2451)
- Fix that the counter on the upload button always showed a number that didn't go away after uploading all for some people (#2460)

## v28.1

### Fixes

- Android 11: Fix unable to take photo, unable to open location in another app, unable to send crash report (#2410)

### Enhanced Quests

- Cycleways: Improve visibility of "no cycleway" option, by @matkoniecz (#2385, #2404, #2285)
- Street address: Allow tapping on footways, paths, ... (#2411)
- Street address: Do not ask for vertices of an address interpolation line that already has a street set, by @matkoniecz (#2417)
- Parking access: Do not ask for parking access for street side and lane parking, by @jdhoek (#2408)

### Enhancements

- Added a few links to achievements, by @matkoniecz (#2403)
- Only download quests automatically if GPS accuracy is ok (#2422)

## v28.0

### 🚧 Keeping map up-to-date

This update is devoted to checking whether things still exist the way they are mapped since keeping data up-to-date is at least as important as contributing new data.

### New Quests

- _"This shop has been vacant. What's here now?"_
- _"Is this still here?"_ (#2074), asked periodically for benches, telephones, post boxes etc.

### Enhanced Quests

- Added "doesn't exist" answer option to many quests. If it was a shop of some kind, you can directly specify what is there now. (#1673)
- Tracktypes: Better wording and pictures (#2294)
- Parking type: Added lane and off-street parking (#2346)
- Ask for roof shape even if roof levels is 0 - but only in countries where flat roofs are not the norm (#1929)
- Kerb quests: Tag `barrier=kerb` if it is missing (#2348, #2393)
- Do not ask for the bench backrest if the bench type is already recorded (#2365)
- Lanes quest: Move "differs for each side" option to main UI (#2350)
- Other small enhancements (#2382)

### Enhancements

- Serbian translation by Mario, Nemanja Bračko
- Android 11: Enable smooth keyboard appear animation (#2133)
- Other small enhancements (#1832, #2371, #2368, #2349, #2351...)

### Fixes

- Fix crash on Android 11 (#2355)
- Fix the below bridge height quest was shown for too many roads (#2370)
- Fix problem on deletion of a node that is a vertex of a way (#2369)
- More crash fixes (#2386, ...)

## v27.2

- Lanes quest: Don't ask for residential roads with a speed limit of 30 km/h and below (#2324)
- Lanes quest: A few tiny UX improvements (#2337, ...)
- Kerb height quest: Always show available options in the same order (#2345)
- Fix display of achievements when app was rotated in landscape mode on start (#2330)
- Update translations

## v27.1

- Fix the lanes quest was asked again (and again and again) if you answered that there are no marked lanes (#2328)
- Show tactile paving quests in Italy (#2320)
- Update translations

## v27.0

### New Quests

- _"Is there tactile paving on this curb here?"_ (#1305, #2183), by @matkoniecz
- _"What is the height of this curb?"_ (#1305, #2183), by @matkoniecz
- _"How many lanes for cars does this road have?"_ (#856, #2269, #2305, #2299 ...)

### Enhanced Quests

- Ask for the max height also below bridges (#1882, #2234)
- Building type: fix wrong name for a silo (#2303)
- Surface: Add "rock" as an answer option (#2298)
- Cycleway resurvey: Interpret a road with `oneway:bicycle=no` to have no cycleway unless it is tagged explicitly (#2310)
- A few minor enhancements (#2280, #2276, #2221, #2160, #2315, ...)

## v26.2

- fix that quests that turn out to be already solved will be removed on next download (#2255)
- ask for cycleways on residential roads with a speed limit above 30 km/h
- add another achievement graphic
- add a visual tap hint for the cycleway, sidewalk, oneway and steps incline quest (#2240)
- update translations

## v26.1

- Don't ask for cycleways on residential roads after all (#2251)
- Fix cycleway and sidewalk quest sometimes appeared if there was a separately mapped cycleway or sidewalk (#2248)
- Don't ask for cycleways if the sidewalk is mapped explicitly as a separate way (#2247), by @matkoniecz

## v26.0

### ⚡ Wait for the download no more!

Reworked it completely. Quest download is now substantially faster and the pins for all quest types appear immediately at the same time! (#1874, #1901)

### New Quests

- _"How many cars can be charged at this charging station at the same time?"_ (#900, #2242)

### Enhanced Quests

- Road surfaces: Improve interface, ask for reason when a generic surface is selected (#2078, #2237), by @matkoniecz
- Building types: Add answer options for silos (#2180), historic, abandoned buildings and ruins (#2177, #2214, #2233), by @RiffLord
- Cycleway and sidewalk: Shown for more roads (#2230)
- Improve a few wordings (#2202, #2204)

### Other Enhancements and Fixes

- Added Croatian translation by Matija Nalis
- Added more achievement graphics by Judith Gastell
- Miscellaneous UX tweaks(#2223, #2232, #2225, #2199, #2227, ...)
- Fix input of housename did not work (#2241)

## v25.1

### New Quests

- _"What is the reference number of this bus stop?"_ (#2126)
- _"Do these steps have a ramp? What kind?"_ (#2036, #2168)
- _"Does this pedestrian crossing have an island?"_ (#1961, #2030), by @kmpoppe
- _"What's the name of the bank for this ATM?"_ (#203)
- _"Who is the operator of this charging station?"_ (#911)
- _"Who accepts donations for this clothing bin?"_ (#570)

### Enhanced Quests

- House number quest: Make it faster to collect house numbers in a row (#2131)
- Recycling materials quest: In the Czech Republic, offer "any glass" option instead of "glass bottles" (#2123)
- Improve a few wordings (#2047, #2096)
- Handrails: Don't ask for places where the information has already been supplied in another way (#2162)
- Way lit: Don't ask for indoor corridors (#2176)

### Other Enhancements and Fixes

- new achievement graphics by Judith Gastell ([melusine](https://www.artstation.com/melusine))
- Map style update: Amongst other things, all housenumbers are now displayed again on the map, private roads are displayed as such and 3D buildings can be displayed more detailed
- Fix tagging tactile paving on crosswalks on a resurvey (#2176, #2172), by @peternewman, @dbdean
- Other small fixes and enhancements (#2186, #2190, #2182, #2184, #2181, #2179, ...)

## v24.2

- Fix crash for the max height quest (#2147)
- Surface and building type quest: Make categories visually more distinct (#1952, #2000)
- A few fixes (#2145, #2148, #2161...), thanks @peternewman
- Updated translations

## v24.1

- Less ambiguous interface for one-way and steps inclination quest (#2140)
- Don't ask for obvious dead end streets if they are one-ways
- Tactile paving on crossing quest: Ask for more crossings (#2141)
- Added photo how a special button for the blind at a crossing looks like in the UK (#2127), thanks @CJ-Malone
- Other small enhancements and fixes

## v24.0

Good News! StreetComplete received a grant from the [German Federal Ministry of Education and Research](https://www.bmbf.de/) within the frame of the [Prototype Fund](https://prototypefund.de/en/project/streetcomplete/), spanning over a period of 6 months! For the next few months, my focus is on adding new quest types, starting with this update!

### New Quests

- _"Is this a one-way street? In which direction?"_ (#1982, #2122)
- _"Do these traffic lights have a tactile indication for blind people for when it's safe to cross?"_ (#1330, #2127)
- _"Which direction leads upwards for these steps?"_ (#1817)
- _"How may steps are here?"_ (#875)

### Enhanced Quests

- Max weight: A range of different max weight signs are now supported (#1880, #2121)
- Postbox collection times: Can now select more times for the same day (range) (#2108, #2109), by @kmpoppe
- All pedestrian traffic signals quests: Ask for more crossings (#2130)
- Building levels: Do not ask for barns and warehouses (#2124), by @Etua

### Other Enhancements and Fixes

- hide GPS button only when the screen follows current position (#1996, #2101, #2110), by @smichel17 and me
- other small fixes (#2103, ...)

## v23.0

### 🚧 Map maintenance

Quest types whose answer is likely to change over the years will be asked again in reasonable intervals. So now you can help not only to complete the map, but also to keep it up-to-date, which is at least as important.

Most prominently, you'll be asked if opening hours are still the same as before and be offered to correct them if they changed, the same with cycle tracks and lanes on roads.

This feature has been sponsored by the OpenStreetMap Foundation (OSMF) as part of their new [microgrants](https://blog.openstreetmap.org/2020/07/01/osmf-microgrants-program-congratulations-to-selected-projects/) program. You can support the OSMF and other initiatives like these and have a say by [becoming a member](https://join.osmfoundation.org/), if you like.

### New Quests

- _"Is there a summit register at this peak?"_ (#561, #2065) by @matkoniecz
- _"Is this defibrillator (AED) inside a building?"_ (#2068) by @matkoniecz
- _"Does this bus stop have a bench?"_ (#1079, #2073) by @matkoniecz

### Enhanced Quests

- Motorcycle and bicycle parking: Do not accept an input of "0" (#516)
- Also ask for the opening hours of defibrillators (#1756, #2066), by @matkoniecz
- Better picture of paving stones in surface quest (#2088), by @matkoniecz
- Accepts cash quest: Ask for more places (#2019), by @kmpoppe
- For opening hours, wheelchair access and accepts cash quest, always display the type of place alongside its name (#2012), by @Helium314
- Recycling container materials: Don't ask for containers close to other containers

### Other Enhancements and Fixes

- Explain that you have to download manually if auto-sync is off (#1989, #2009, #2070), by @matkoniecz
- Antialias pointer pin (#2091), by @Plastix
- Show link how to contribute translations in settings (#2079)
- Improve visibility of highlighted elements on the map (#1769)
- Fix a few rare crashes
- Fix housenumber quest sometimes appearing even if there is a housenumber node inside (#2064)

## v22.3

### Fixes

- For F-Droid users: Fix error on downloading quests / crash when opening add place name quest (#2007)

## v22.2

Same as v22.1 only removing once all local data subject to a possible data corruption that happened in v22.0 (#2014)

## v22.1

### Fixes

- Fix login failing with the app crashing (#2007)
- Fix some crashes when app comes into foreground again (#2003 and more)

## v22.0

### Enhancements

- When you split a way, all previous quests reappear on the split segments immediately after upload (#1950, #1951)
- Better fallback for showing of (street) names in user language or script
- Always display the house number above the quest question if it is available (#1971)
- For longer displays, show more of the quest forms initially (#1977)
- Added an achievement graphic, by @FloEdelmann

### New and enhanced Quests

- Board Type: _"What is the topic of this information board?"_ (#1226, #1920), by @matkoniecz
- Detailed Road Surface: _"What specific surface does the road ... have here?"_ (#279, #1915), by @matkoniecz
- Street Names: Allow to specify the romanized/international name of a street name in another script, such as Greek (#1765)
- Sidewalks: Add answer that the sidewalk is already displayed separately on the map (#1958, #1925), by @matkoniecz
- Handrails: Do not ask for private stairways (#1976)
- Oneways: Better fitting description text (#1975)
- Building Type: Correctly refer to "row house**s**" and not "row house" (#1973)
- Housenumbers: Consider a housenumber with 5 digits still as valid (#1995)

### Fixes

- Fix a rare crash on device connectivity change
- Fix a rare problem of photos being associated to the wrong notes (#1981)
- switching off compass mode returns tilt to 0 (#1984)
- Fix location pointer pin now always shown in correct position (#1842)

## v21.2

### Fixes

- fix another display issue with bridges (#1954)
- fix least important quest pins would show up in front (#1927)

## v21.1

Good news everyone! The microgrant proposal [Map maintenance with StreetComplete](https://wiki.openstreetmap.org/wiki/Microgrants/Microgrants%5F2020/Proposal/Map%5FMaintenance%5Fwith%5FStreetComplete) is one of the [12 projects selected for funding by the OpenStreetMap Foundation](https://blog.openstreetmap.org/2020/07/01/osmf-microgrants-program-congratulations-to-selected-projects/)!  
Work on this feature will commence this August, thank you for support and all those endorsements!

### Fixes

- fix quest pins would sometimes not show up (#1927), thanks @matkoniecz
- added missing flag for the Canary Islands (for the statistics)
- don't show location pointer pin if the position cannot be ascertained (#1842)
- correct text color after change to dark mode (#1807)
- display bridges above roads and tunnels below (#1941)
- don't show notification button when there are no notifications
- limit max tilt to 40° (#1932)

## v21.0

### 🗺️ New Map Provider

Big thanks to [JawgMaps](https://www.jawg.io), a provider of online custom maps, geocoding and routing based on OpenStreetMap data. They are providing their vector map tiles service to StreetComplete for free!  
Their maps are also quite excellent: You can look forward to **daily map updates** and **map loading times cut in half**, as their map data is very lightweight.

But let's not forget to thank @Akasch for hosting the previous solution with zero budget over the last years and maintaining it in his free time. Without his endeavor, there likely wouldn't have been a map at all in this time.

### Enhancements

- Use adaptive launcher icon (#1928), by @pstorch
- The app can now be moved to SD card in the Android settings (#1787), thanks @Mortein and @Atrate
- Labels for countries and big cities stand out more, so they are better distinguishable from smaller places
- Accessibility: The text on the map now scales up when zooming and when increasing the font size in the Android settings (#1877), by @matkoniecz
- Accessibility: Increase contrast for orange text (#1899), by @matkoniecz
- Recycling type: Added cooking and motor oil options (#1830)
- Building type: Don't ask for (theme park) attractions (#1891)
- Added some achievement links

### Fixes

- Fix a crash for Finnish users (#1911), by @matkoniecz
- Quest pins are not occluded by street names and other labels (#1905, #1906, #1908, #1927, #1930), by @matkoniecz
- Fix display of compass rose for people with a larger system font size (#1916), by @matkoniecz
- Fix animation of solved quest was cut off (#1910)
- Fix country flag for Costa Rica was missing

## v20.1

### Fixes and Enhancements

- Workaround drawing problem with the location pin in Android 6 (#1879), thanks @matkoniecz
- Fix crashy country bubble in statistics screen (#1886)
- Road name and bus name: Trim whitespace at start and end of user input (#1861)
- Crossing type: Don't ask for crossings that are not for pedestrians (#1868), by @matkoniecz
- Tactile paving on crossing: Don't ask for crossings that are not for pedestrians (#1875)
- House numbers: Don't ask the housenumber of buildings which are in a relation that already has a housenumber (#1860)
- Show tutorial again if user exits the app before finishing it (#1867)
- Max weight: Correct German translation (#1884)
- Address street: Do not show immediately after answering the housenumber quest but only after next download (#1856). Selecting the street by tapping on it would not work otherwise.

## v20.0

### 📈 New Statistics

- 🌍 See your solved quests grouped by country
- See how you rank amongst other StreetComplete users
- See how many days you contributed and the sum of your achievement levels
- Access the mapping portal for each country you contributed to, with useful infos about local mapping efforts and for connecting with the local community

### New Quests

- What street does the (house) number ... belong to? (#1782, #213), by @dbdean, @matkoniecz and me

### Enhancements

- A pointer pin at the edge of the screen replaces the GPS button and points to your current location
- 📱 The app is now optimized to look great on tablets and phablets as well! (#1794)
- On map, use different colors to differentiate town, green and trees
- Text in note discussions is now selectable
- Add proper shadows for all the speech bubble forms
- bubbles in ball pit now display with shadow (#1840)
- text is selectable now in changelog, credits and privacy statement (#1833)
- smaller animation time for quest counter in statistics (#1832)
- Distribute answer buttons over several lines if it does not fit into one (#1372)
- Do not ask for the type of building for places where it is known for what the building is used (#1854)

### Fixes

- on pressing the hardware menu button, don't open the menu twice (#1806)
- bubbles in the statistics view shouldn't grow too large anymore (#1818)
- Previous quest selection was still displayed when creating a note (#1820)
- On splitting a way, require the user to zoom in far enough to use the scissors
- Fix link collection sometimes appearing empty
- Fix close-button was not shown when bottom sheet was pulled up completely
- fix achievement icons would sometimes vanish (and other oddities) (#1834)
- if the text on the buttons in the button bar is too long, use several lines (#1372)
- More minor / technical fixes

## v19.2

Maybe the announced end of new (big) features for StreetComplete is not over yet! I applied to two microgrants at the OpenStreetMap Foundation, you can endorse them if you like these ideas:

1. [StreetComplete as an entry point to OpenStreetMap](https://wiki.openstreetmap.org/wiki/Microgrants/Microgrants%5F2020/Proposal/StreetComplete%5Fas%5Fan%5Fentry%5Fpoint%5Fto%5FOpenStreetMap)
2. [Map maintenance with StreetComplete](https://wiki.openstreetmap.org/wiki/Microgrants/Microgrants%5F2020/Proposal/Map%5FMaintenance%5Fwith%5FStreetComplete)

### Fixes

- updated translations
- don't show map context menu while bottom sheet is open (#1820)
- cycleway type quest: for oneway roads, always only ask for the traffic flow direction side of the road (#1822, thanks @peternewman)
- fix problem where the quest type bubbles in the statistics view would not have enough space (#1818)
- when turning off GPS, also do not show the compass direction (#1823)
- fix download icon would get stuck if the app was disconnected during download (#1821)
- fix crash when app was disconnected directly after login

## v19.1

- add some achievement graphics by Sanja Dimitrijevic ([modesty031](https://www.artstation.com/modesty031))
- selected quest is displayed always on top (#1801, #1805, #1810)
- More minor fixes and improvements (#1799, #1802, #1803, #1808, #1806, #1812), updated translations

## v19.0

This one is a huge release, maybe the biggest one yet! But don't get used to it, I have been working on it full-time for a few months but must soon return to work life as my savings are running low. That being said, you **can** donate, check the about screen 😛.  
You can also read this list of changes later in that screen if you are impatient now.

### 🗺️ Reworked main screen UI

- **👨‍🏫 Added small tutorial**, only shown when the app is first installed (#178, #1552)
- Removed app bar, map now expands over status and navigation bar
- New main menu with bigger icons
- More informative **download indicator**
- **Notifications button** shows news, like this changelog or new mail in your OpenStreetMap inbox (#1690, #1751)
- Notes are now created via long-press

### Added profile screen

- Login to OSM now happens within the app and not the browser, solves login problems for some users (#1760, #413, #804)
- Your solved **quest count is now synchronized** across your devices and when you reinstall the app
- Undone quest answers are now correctly counted as -1 to your quest count, so after synchronization your count may be a little lower than before
- **⭐ Added statistics of your solved quests** and links with more information for each quest type (#80, #1294). Try rotating it!
- **🏆 You can now earn achievements** which in turn unlock interesting OpenStreetMap related links and add them to your collection! (#1715, #1749, #1009)

### Fixes and Enhancements

- Fix quest download at 180th meridian (#1767)
- Disable photo upload for Android 4.4 and below (#1768)
- Improve the postbox collection time form (#1776, #1789), thanks @peternewman
- Exclude hail & ride bus stops from questions about bus stops (#1784, #1793), thanks @peternewman
- Correct tagging for small electrical appliances when asked what can be recycled at a container (#1783)
- Slightly improved the credits screen
- Add a selection marker to the currently selected quest (#1792)

## v18.0

### New Quests

- **Does ... accept cash payment?** (#1743, #1573), by @quite. Enabled only in Sweden because it cannot be taken for granted that cash is accepted there.
- **What type of tourist information is this?** (#1722, #1115), by @Dosenpfand

### New Translation

Indonesian by Froyobread, Agha Pradipta, Wijayaa16d and Suryamudti0128

### Enhanced Quests

- Building types: Provide a description of what distinguishes a garage from garages (#1720), by @smichel17
- Recycling containers: Add "it's a waste container" as answer option (#1745)
- Car wash type: Allow selecting self-service + staff cleans car (#1738)

### More

- Clearer wording for cancelling current download, especially in French (#1741)
- Do not show this dialog for new users (#1751)
- Remove "show more" button for certain quests, instead show all choices right away (#1692, #1754)

## v17.3

Fix: App should not require a network location provider (#1733)

## v17.2

Fix: App should not require a GPS sensor (most tablets do not have one)

## v17.1

### Fixes

- Fix caching of the background map tiles (#1589, #1727, #1723)
- Turn off GPS when app is sent to background (#1729)
- Fix quests vanished when app was stopped and later restarted (#1698)
- Fix crash when showing the underground building quest in Dutch
- Fix some more possible crashes

### More

- On tapping the compass button, tilt the map (#1725), by @smichel17
- Update translations
- New Language: Malayalam by Muhammed Yaseen, Rajeev R R, Arun Raj, Dharwish and Mujeeb Rahman K

## v17.0

This release includes a major update of the map rendering library (in order to satisfy new Google Play guidelines). I took this as an opportunity to re-do most of the things that concern the map view (#1279, #1606) and thus solve some long-standing issues with it. If any new have been introduced, please report them!

### Enhancements

- Faster start-up time of map, better performance
- Smoother zoom to a quest pin and back
- No or less of a flash when a solved quest pin is removed from the map
- Removed the confusing "glued to your location" functionality
- Create new note marker is now initially on users location if it is in view (#1422)
- Map now retains map focus on current quest / note when rotating the screen (#1524)
- Stop downloading quests when user changed activated quests in the settings (#1681)
- Treat metered wi-fis as mobile data (#1699)

### Fixes

- Location accuracy now displayed at correct scale during zooming (#1684)
- Quests pins should not disappear under certain circumstances anymore (#1698, #1689)
- Quest pin is removed from the map after splitting up the way (#1701)
- Minor layout fixes (#1548, #1702, #1703)

### Enhanced Quests

- Do not reorder last picked options for the track type quest form to the front (#1692)
- Moved building type quest importance up a bit because the housenumber quest is only shown after this one has been answered (#1717)

## v16.1

- fix crash on viewing a wheelchair quest when language is Finnish
- fix possible crash on sending app to background
- update translations
- Leaf type is now asked for tree rows as well (#1694) by @MegaArthur
- Ask for tactile pavings at bus stops in Luxembourg as well (#1696) by @dwaxweiler
- Do not ask for speed limits on trunk link and motorway links
- clearer wording for a changeset commit message (#1691)

## v16.0

### New Features

- 📰 You now see this changelog in the app after each app update and in the about menu
- ❤️ You can now sponsor the development of this app on [GitHub Sponsors](https://github.com/sponsors/westnordost), [liberapay](https://liberapay.com/westnordost) or [Patreon](https://www.patreon.com/westnordost). Thanks to anyone considering it!

### New Translation

Vietnamese by Minh Nguyễn

### New Quests

- What is the reference number of this postbox? (#1556, #1628)
- What can be left here for recycling? (#223, #1627)
- Can only glass bottles and jars be recycled here, or any type of glass?

### Enhanced Quests

- ask about internet access for more types of accommodations (#1620), thanks @wvanderp
- ask opening hours, names and wheelchair accessibility for more types of places (#1621), thanks @wvanderp
- do not ask for handrails of escalators by @matkoniecz
- add hint how to answer the cycleway and sidewalk question (#1671) by @matkoniecz
- clearer wording for parking access quest (#1685)
- ask opening hours also for shops with no name (#1647)
- let the user specify the type of crossing if before it was simply specified as an island (#1637)

### General Enhancements

- better visibility of some icons in dark mode (#1663, #1664, #1665, #1668, #1659, #1658), thanks @matkoniecz
- fix dark mode for right-to-left layouts (#1634)
- fix strange behavior in auto download of quests
- try again later if the upload of attached photos to a note failed (#1575)
- indicate the user's locale for each changeset (#1674)
- Add rate this app on Google Play button in the about menu
- Add donate to this app button in the about menu

### Fixes

Maxspeed quest: Tag motorway and trunk link implicit speed limits correctly

## v15.0

### New Translation

Norwegian Bokmål by Mats Randgaard and Simen Heggestøyl

### New Quests

- **Does it cost a fee to enter ...?** (#873, #1600) by @matkoniecz
- **Is this laundry a self service laundry?** (#1385, #1608) by @matkoniecz
- **Do these steps have a handrail?** (#1390, #1616) by @xuiqzy
- **What is the weight limit here?** (#1622, #1467, #361) thanks @matkoniecz

### Enhanced Quests

- Do not ask for the surface of escalators or indoor pathways (#1604, #1594)
- Do not ask for the bridge structure of movable bridges (#1595)
- Improve wording when asking for tactile paving on streetcar stops (#1584)
- Allow to select multiple produces for orchards (#1568)
- Ask for the name of cemeteries, allotments, airfields, barracks and training areas (#1618) by @wvanderp
- Do not require paved surface for asking about the surface of the footway / cycleway part of a segregated path (#1587)

### UX Enhancements

- Ask earlier for authorization and clarify that it can be done later in the settings (#1557, #1563)
- "Did you check on-site?" warning not shown anymore if the user was on-site when he opened the form (#1591)
- Hide quest pins while splitting a way (#1581)
- Show dots for quests where there is no space for pins (#1615)
- Disable 3D buildings when creating notes (#1607, #1589) by @typebrook
- Less ugly texts in the create note form

### Fixes

- Fix generation of overpass QL
- Fix maxspeed quest - OK button did not show and (from v15.0-beta1) (#1632, #1636) thanks @peternewman and @matkoniecz

### Technical

- Migrate parts of the code from Java to Kotlin, lots of refactoring (-3000 lines of code!)
- Distribution on Google Play is now about less than half the size (app bundles)

A portion of this update is credit to Mateusz Konieczny, powered by a [NGI Zero Discovery grant](https://www.openstreetmap.org/user/Mateusz%20Konieczny/diary/368849).

## v14.1

### Fixes

- disable upload button when uploading, only show when there is something to undo (#1433, #1480)
- fix pins on the maps sometimes vanished after sending app to background (#1571, #1415), thanks @matkoniecz
- fix changes not properly grouped into changesets don't close open changesets (fixes #1579)
- fix crash when taking a photo on not being able to answer a quest
- fix auto-sync not working correctly when many quest types are disabled (fixes #1561)

### More

- update translations

## v14.0

### Major Features

- **🚀 quest download now more than 4 times as fast as before!** (#1479, #1514, #1516)
- **✂️ Splitting ways is now possible!** (#1329)

### New Quests

- **Do the trees here have needles or leaves?** (#366, #1465) by @matkoniecz
- **What's the surface of the cycleway here?** \- asked for segregated paved foot- and cycleways (#1493) by @matkoniecz
- **What's the surface of the footway here?** \- asked for segregated paved foot- and cycleways (#1544, #1489) by @matkoniecz
- **Is the restroom at ... wheelchair accessible?** \- asked for places that have toilets (#1391, #1547) by @matkoniecz

### Enhanced Quests

- **roads prohibited for pedestrians**: Switched icon - old one was leading to misunderstandings (#1342)
- **lit roads, max speed**: Also ask for link roads (#1520) by @matkoniecz
- **bicycle parking type**: Do not ask for private bicycle parkings (#1521) by @matkoniecz
- **place name**: Also ask for the name of `tourism=chalet`s (#1519) by @matkoniecz
- **opening hours, place name, wheelchair access**: Ask for more `office=*` and `craft=*` places (#1522, #1526) by @matkoniecz
- **road name**: Do not ask for names of private roads (#1529, #1533) by @matkoniecz
- **housenumber**: tag `nohousenumber=yes` instead of `noaddress=yes` (#1553)
- **all building-related**: Do not ask for ruins (#1543, #1541) by @matkoniecz
- **road name**: If the user enters a ref instead, it will be tagged as such (#1490)

### Enhancements

- Show confirmation dialog when removing authorization for the app in the settings (#1534, #1535) by @typebrook
- improve icons for dark mode (#1545)

### Fixes

- Show only labels from OpenStreetMap on the background map (#1527)

A portion of this update is credit to Mateusz Konieczny, powered by a [NGI Zero Discovery grant](https://www.openstreetmap.org/user/Mateusz%20Konieczny/diary/368849).

## v13.0

### New Quests

- **Does this ferry route transport pedestrians?** (#39, #1432) by @matkoniecz
- **Does this ferry route transport motor vehicles?** (#39, #1432) by @matkoniecz

### Enhanced Quests

- Ask about names of ferry terminals (#1477) by @matkoniecz
- Show `operator` name in question for collection times of postboxes (#1473, #1474, #1484 ) by @matkoniecz

### UX Enhancements and Features

- make it clearer how cycleway and sidewalk quest UI works (#1450) by @matkoniecz
- explain what is the preferred language for notes (#1471) by @matkoniecz
- explain that adding photos is always useful for notes (#1452) by @matkoniecz
- Increase height of download bar (#1460) by @matkoniecz
- Show some helpful information when clicking the star icon (#1478) by @matkoniecz
- Offer user to change download server in order to bypass blocking of overpass-api.de in Russia (#1389, #1438) by @matkoniecz

### Fixes

- On download, only timeout after 180 seconds (#1010, #1466, #1472) by @matkoniecz
- Quest marker of selected quest will never disappear any more when zooming in (#1462) by @matkoniecz
- Fix `natural=water` was not detected as an area - bug had no effect though (#1476) by @matkoniecz
- Fix upload button was not working if a user that is not logged in tries to upload (#1446, #1475) by @matkoniecz
- Fix theoretical incorrect display of highlighted multipolygon or multi-polyline (#1498) by @matkoniecz
- Show quests that come into view also when rotating or tilting (#1496)
- Auto download was not triggered when there were too many quests that were deactivated in the settings around (#1497)

This update is almost entirely credit to Mateusz Konieczny, powered by a [NGI Zero Discovery grant](https://www.openstreetmap.org/user/Mateusz%20Konieczny/diary/368849).

## v12.2

### Enhancements

- **address**: support for house numbers in Japan (#1407)
- **railway crossing barrier**: do not ask for abandoned railways (#1413)
- **bus stop shelter**: add _"whole stop is covered"_ answer option (#1417, #1395)
- **diaper changing table**: use `changing_table` key instead of `diaper` key (#1424)
- **opening hours**: do not ask for `leisure=sports_centre` (#1423)
- reorder priority of quests by importance and ease-of-answer (#1437, #1441, #1442, #1444) by @matkoniecz
- postpone nagging new users to register until they answered a few quests (#1446) by @matkoniecz
- reorder settings, frequently used up, less used down (#1448) by @matkoniecz
- enforce a minimum zoom level (of 14) on opening geo intent (#1425)
- reduce default, min and max tile cache sizes to more realistic values (#1398)
- update translations

### Fixes

- switch off following GPS-position on open geo intent (#1426)
- **sidewalk**: warn user before closing a half-completed answer (#1451)

A portion of the work done for this update is powered by a [NGI Zero Discovery grant](https://www.openstreetmap.org/user/Mateusz%20Konieczny/diary/368849) given to Mateusz Konieczny.

## v12.1

### Fixes

- Fix error when uploading answer for railway crossings (#1408)

### Minor Enhancements

- Add the option to open the location in another app (#190, #1396) by @ENT8R
- reduce the size of downloaded areas so that quests are closer to the user (#1357)
- Enlarge maximum map cache and cache step size (#1398)
- Don't ask for road names in Japan (#1407)
- Don't send crash report in case of a request timeout during download
- Update translations

## v12.0

### New Quest

- **What is the name of this place?** Asked for unnamed shops, amenities etc. (#309, #1376)

### Minor fixes and Enhancements

- fix color of close button in night mode
- show the placeholder avatar if an avatar cannot be decoded (crash fix)
- don't ask for location on startup when user last denied it (fixes #1382)
- don't ask for railway crossing barriers for tram lines (fixes #1387)

## v11.0

### Dark Theme (night mode)

### Fixes

- fix a few sources for crashes and other small fixes (#1307, ...)

### Enhancements

- Opening hours and wheelchairs quest: Also ask for `office=religion` (#1365) by @matkoniecz
- Do not ask about railway crossings on private roads (#1366, #1321) by @matkoniecz
- slightly better wordings for a few quests (#1349, #1353)
- quest upload now treats situations where the geometry of the element in question changed significantly as an unsolvable conflict

## v10.2

### Fixes

- fix a few sources for crashes

### Enhancements

- update translations
- never show the prohibited for pedestrians quest for residential and service roads
- support to tag `foot=use_sidepath` and `sidewalk=separate` in prohibited for pedestrians quest (#1345)
- OK button is now visibly disabled when the note text is empty (fixes #1340)
- do not show the description for leaving a note after showing it already in the confirmation dialog
- Increase visual distinction of images of sidewalk quest (fixes #1339)

## v10.1

### Fixes

- fix crash when trying to enter non-numbers (also: empty string) into the height limit form for imperial units
- fix crash for Android 5.x devices on auto-closing changesets (require wake lock permission) (#1333)
- workaround for Android 9 bug, fixes crash when trying to upload/download automatically
- fix a possible cause for IllegalStateExceptions - remove messages from handlers when activity/fragment is destroyed
- fix crash when trying to download an invalid note (#1338)

### Enhancements

- Sidewalk quest: Only ask for lit roads
- Pedestrian prohibited quest: greatly reduce the number of streets for which this information is asked and clarify wording (#1336 and [mailing list discussion](https://lists.openstreetmap.org/pipermail/tagging/2019-February/042852.html))
- update translations

## v10.0

### New language

- Korean by Dongha Hwang

### New quests

- **Add sidewalk**: Does this street have a sidewalk? (#152)
- **Add accessible on foot**: Is this street accessible for pedestrians here?

### Improvements

- Quests previously hidden can now be made visible again through an option in the settings (#302, #1302) by @ENT8R
- All "other answer..." options that lead to another dialog, the text now ends with a "..." (#1296, #1274)
- clarify wording in maxspeed quest (use "built-up area" instead of "urban") (#1314)
- the choices in the crops quest are now sorted with new UN FAO data (#1319) by @rugk
- Show the tactile paving bus stop quests for ways as well (#1309) by @MrKrisKrisu
- when showing a quest for an element (e.g. a shop) that is not at street level, show additionally on which floor it is located (#1270, #1326)

### Fixes

- fix missing pictures in building type and surface selection quests in Android 4.4 and below (#1317)
- fix sometimes quests were still being generated even though there was a note for that element (#1089)
- exclude roads from the oneway quest where its direction can not be determined reliably (#1320)

### Technical

- Migrate parts of the code from Java to Kotlin, lots of refactoring, use Android Kotlin extensions
- Migrate from appcompat to androidx, migrate from `evernote.android-job` to `work.work-runtime` library
- Use R8 instead of ProGuard

For the average user, this means that this version will probably be more unstable than v9 at the start. (There have been 3 beta releases before this first production release)

## v9.0

### New Quests

- **Track type**: What is the surface firmness of this track? (#959) by @ENT8R
- **Underground building**: Is this building completely underground? (#912)
- **Traffic signals sounds**: Are there sound signals for the blind here? (#1268, #574) by @matkoniecz
- **Traffic signals button**: Do these traffic lights have a button to request green light? (#1269, #574) by @matkoniecz
- **Motorcycle parking capacity**: How many motorcycles can be parked here? (#1181)
- **Motorcycle parking covered**: Is this motorcycle parking covered (protected from rain)?

### Disabled Quests

- Disabled the max speed quest by default. You can re-enable it in the settings. Rationale and discussion: #1281

### Enhanced Quests

- Max height: do not ask for pedestrian streets (#1261)
- Building levels:
  - also ask for cabins (#1248)
  - remember the last chosen values and offer to prefill the form with it
- Wheelchair: Add some more places to ask for wheelchair access
- Opening hours: Add some more places to ask for opening hours

### Enhancements

- The app now deletes cached unsolved quests downloaded more than one month ago (#766)
- When exiting the quest details, return view position to where it was before (fixes #1257)
- Quest markers for some quests are now shown at the end of the street rather than the center (#733)

### Fixes

- Opening hours quest: use "," instead of ";" to separate opening hours rules whose time extend to another day (#1292)

## v8.4

### New Translation

Galician by Iván Seoane

### Enhancements

- use a better jpeg quality for photo upload (80 instead of 60)
- show tunnels semi-transparently on the map
- show barriers/gates on the map
- indicate the direction of one-way streets on the map
- render private/semi-private roads differently on the map
- make algorithm that decides how to interpret the weekdays given on a opening hours plate more intuitive

### Fixes

- do not trigger automatic upload if not authorized yet (fixes #1253)
- avoid outputting lat/long coordinates in scientific notation because Overpass-API cannot parse this
- fix max height quest did not show for height restrictors and parking entrances (fixes #1245)

## v8.3

### New Translation

Asturian by Víctor Suárez

### Enhancements

- Wheelchair quest: Also ask for arts centres (#1235) by @matkoniecz
- Tactile paving quest: Exclude crossings not for pedestrians (#1238) by @matkoniecz
- Cycleway segregation quest: do not ask for areas
- Oneway quest: Also ask for private roads that are still accessible on foot
- Maxspeed quest: do not ask for areas
- Maxheight quest: only consider heights lower than 1.9m as unusual
- Opening hours quest: Enhancement on the form
  - make all weekday rows deletable, months rows vanish when the last weekday row is deleted
  - require to insert first weekdays after changing to months mode (if no weekdays were specified before)
- Road name quest: do not ask for areas
- improve layout support for RTL languages (Arabic, Persian, ...)

### Fixes

- Opening hours quest: if the phone's locale is on Arabic, do not use Arabic numbers for tagging opening hours times (#1234)
- fix IllegalArgumentException on Android Oreo and up when trying to auto-upload changes on application start

## v8.2

### Enhancements

- update translations
- performance improvements on cold start of the app (around 1 second less)
- in Spain, offer to add Asturian to street sign
- do not show maxspeed quests for roads that are tagged with `zone:traffic`
- do not show building level quest for underground buildings and `man_made`
- make the (other answers) context menu also look like a speech bubble (#1211)

### Fixes

- fix black boxes sometimes being displayed in place of quest icons (#850)
- workaround bug in Android 5.0.x that results in a crash when rotating device to landscape mode
- workaround crash in AppCompat library for Android 7.x that results in a crash when tapping on a underlined in red word (because of the spellchecker)
- fix various possible exceptions when uploading

## v8.1

- Building type quest: clarify that buildings constructed to house restaurants & co should also be tagged as retail (#1204)
- do not show quests for elements longer than 500m because users cannot be expected to survey such long ways (#1207)
- Performance improvement on rotating the screen (portrait / landscape) - should happen now almost instantaneously (#1217)
- fix detection in which country the user is located (#1215)
- auto upload also triggers whenever app comes back into foreground or the user creates a note (fixes #1206)

## v8.0

### UX Enhancements

- **Redesign quest forms and dialogs to a new look** (this is quite huge) (#1158)
- when leaving a note, it is done in the bottom sheet instead of in a dialog now so that you can still see the quest geometry
- solve performance problems when downloading quests (especially housenumbers and building types)
- only play sound effects when system setting for that is enabled (fixes #1187) and in "media" instead of "notification" stream (#1167)
- In notes, shortcut the link to the element to `https://osm.org/way/...` for better readability of notes left by SC users (#1178)
- tapping on the quest title now toggles expanding the quest to full size and back
- it is now again possible to attach photos to notes
- add "deselect all" button in the quest selection screen (#1203)

### Quests

- **new** Railway crossing: _"How is this railway crossing protected?"_
- Redesign note discussion quest (new: avatars displayed also) to make it look like a chat
- Surface and building type quest: When tapping to expand a category, the view scrolls down to show the expanded items
- Cycleway quest: Exclude motorroads

### Other & Bugfixes

- StreetComplete is now ready for GDPR-related changes made to the OSM api
- solve quest upload problem for quests on relations when phone locale is set to a Turkic language

## v7.1

Prepare for map server maintenance. After that, the shown map will be current and loads faster.

## v7.0

### Un-Features

- disabled support to put photos into notes, because framapic.org service has discontinued until further notice. (see #1161)
- expect hiccups and/or old maps being displayed in the coming week as the tileserver goes into maintenance and tiles from nextzen are displayed instead

### New Quests

- Playground access: _"Is this playground publicly accessible?"_ (#1134) by @matkoniecz
- Max height: _"What is the height limit of this tunnel/parking entrance/height restrictor?"_ (#960, #399, #421, #447) by @ENT8R
- Cycleway segregation: _"How are the footway and cycleway laid out here?"_ (#527, #1135) by @matkoniecz

### Enhanced Quests

- Building type: Added toilets and sport centres to selectable building types (#1124, #1125)
- Max speed:
  - Added pictograms for living street signs in Portugal, France and Israel, added pictogram for slow zones in Israel and speed limit signs in general for Iceland, Sweden and Finland
  - Can now also add living streets in Israel and Azerbaijan and slow zones in Mauritius
  - Redesigned to force the user to make a deliberate choice to either specify what is written on the sign or state that there is no sign. Also, beautified the input for specifying slow zones (#1085, #1149)
- Road name: Detecting more abbreviations in Portuguese (#1143) by @xendez
- Housenumber:
  - Disabled for Italy (#714)
  - The app will not suggest that an input like "5,5a,6" may be wrong
- Postbox collection times: Allow marking the postbox as having no signed collection times (#1118, #1076 ) by @matkoniecz
- Opening hours: Allow marking a shop as having no signed opening hours (#1118) by @matkoniecz
- Any quest with selection of images: The last choices are remembered and displayed as first items (#1072, #73, #826)

### Small Enhancements and Fixes

- Orchard quest used to tag `produce=tomatoe`. Corrected to `produce=tomato` (#1171)
- Increase number of quests that are downloaded in one go (#1091)
- Undoing quests now removed quests that are no longer applicable because of that (#1131, #746)
- Fix crash on attaching a photo (#1144)
- Fix some quests were wrongly displayed as being disabled in France (#1150)
- Fix an error when downloading the oneway quest for roads tagged as areas
- Added a nice animation when solving a quest (amongst others #944)
- Better feedback when manually uploading changes, do only show the upload-button when autosync is off
- Show the blinking cursor within the housenumber, speed limit etc. input field signs (#1154)
- When exiting the quest details, always zoom back to the previous zoom (#965)
- The app will not automatically open the next quest for the element for which the user just answered the quest any more

## v6.1

- **fix crash when app goes into background while downloading quests for Android 8+ (#1123)**
- fix crash when rotating display in bus stop name quest
- fix crash when showing a note that was moderated (hidden) (#1116)
- update translations

## v6.0

### New Quests

- Add building type: _"What was this building constructed as?"_ (#25, #774, #1092) thanks @ENT8R
- Add path surface: _"What surface does this path have here?"_ (#133)
- Add one-way road: _"Is this a one-way street?"_ (#370)

### Enhanced Quests

- House numbers: Can now answer that the building has no house number (#351)
- Parking type, fee and access: Include relations in search for parking areas (#1033)
- Add road surface: Show different surfaces initially for different types of roads, e.g. for tracks more unpaved surfaces
- Add cycleway: Now understands `bicycle=use_sidepath` and will not show the cycleway quest in that case (#1084, #1087) by @ENT8R
- Wheelchair access: Added internet cafes, theatres and casinos (#1086) by @matkoniecz
- Bicycle parking: Clearer wording (#1038)

### Small Enhancements and Fixes

- split up English into US-English and UK-English
- Update privacy statement
- Add confirmation dialogue on resetting quest enablement to default (#1095)
- Make background of compass needle not flicker any more (#1057)
- Cursor does not jump to the start of the input any more when pressing \[abc\]-button in house number quest (#1093)
- Correct wording in mark completed construction quest

## v5.2

Fix a regression bug of the implementation for making the map rendering more performant (#1036), which ironically led to a serious performance issue on (at least) Android 8 (#1047)

Otherwise identical to v5.1.

## v5.1

- **fix battery drain/performance problem** (#1036)
- **fix crash on downloading construction quest** (#1041)
- distinguish implicit speed limits in built-up (=lit) areas and in other places in GB (#1037)
- avoid possible user mistake when placing a new note (#1039)
- the compass now changes its direction more smoothly (#982)
- the compass now shows true North, not magnetic North (#982) thanks @hochwasser
- add Greek translations by Gerasimos Maris

## v5.0

### New Features

- some wording changes for clarity
- add Slovenian translations
- Performance Improvement: Use fast country detection library <https://github.com/westnordost/countryboundaries>
- Now uses HTTPS for all connections (#63)
- Always mention StreetComplete in created notes (#1012)
- add icons for bus stop shelter and bicycle parking capacity quest (#997, #999, #1004) thanks @rugk

### Fixes

- Fix crash on rotating the phone while parking fee quest was open
- Fix duplicate opening of quests (i.e. on rotate, or when a new quest is unlocked) (#973)

### New Quests

- Bicycle parking type: _"What is the type of this bicycle parking?"_ (#923) by @matkoniecz
- Bus stop names: _"What is the name of this bus stop?"_ (#986 / #551) by @PanierAvide
- Postbox collection times: _"What are the collection times of this postbox?"_ (#85)
- Construction Site complete: _"Is this road completed?"_ and _"Is this building completed?"_ (#920) by @matkoniecz

### Enhanced Quests

- Housenumbers: Add clarification that it is OK to tag ranges and comma-separated housenumbers (#939, #1015) by @ENT8R
- Cycleways:
  - Surveyors can now tag dashed cycle lanes (aka advisory cycle lanes) (#888)
  - Surveyors in Netherlands and Belgium can now tag suggestion cycle lanes (Fietssuggestiestrooken) (#888)
  - Improved the illustrations for the cycleway selection (#888)
  - Do not ask for cycleways on residential roads, but do ask in other roads with speed limits as low as 30km/h (#1013)
- Opening hours: Interpret rules that are meant to overwrite previous rules on a opening hours sign correctly (i.e. _"Open Monday to Friday 8:00-16:00, Thursday 10:00-12:00"_)

## v4.1

### Fixes and Improvements

- fix crash when clicking "in the sky" on a map tilted to the max
- fix crash with the map rendering (by reverting back to older tangram-es version)
- fix sometimes-crash when showing a tooltip
- show in italics and remove quotation marks around names in quest questions (#876) thanks @huftis
- update translations

### Enhanced Quests

#### Housenumber quest

- do not show for underground buildings (#907)
- substantially increase performance for download
- fix bug where buildings with housenumbers on entrances were sometimes not excluded (#885)

#### Bicycle parking quest

- do not show for private bicycle parkings (#895) by @ENT8R

#### Roof Shape quest

- do not show for buildings with alternative roof shape tagging scheme given (#896)

#### Cycleway quest

- do not show for private roads

#### Opening hours quest

- expect Swedish opening hours to be different on Saturdays (#919) by @andreasn

## v4.0

### Highlights

- Add a note anywhere, like on openstreetmap.org but you can also attach a photo (#732) by @ENT8R
- New simple map style that shows only what is important for surveyors (#183)

### More Features

- Added unsynced changes counter, click to upload changes manually (#75)
- Immediately open next quest for same element after answering (#164)
- Unglue map while quest details or note input form is shown (#795)

### New Quests

- Internet connection: _"What kind of internet connection does ... offer?"_ (#784) by @ENT8R
- Religion of place of worship: _"What religion is practised at ...?"_ (#777)
- Religion of shrine: _"What religion is represented at this shrine?"_ (#799)
- Parking fee: _"Does it cost a fee to park here?"_ (#825) by @ENT8R
- Parking access: _"Is it restricted who may park here?"_ (#839) by @ENT8R
- Bench backrest: _"Does this bench have a backrest?"_ (#830) by @Map3428
- Bridge structure: _"What is the structure of this bridge?"_ (#709) thanks @Southernswampfrog
- Wheelchair toilet access: _"Are these toilets wheelchair accessible?"_

### Languages

- Remove Malayalam (not updated anymore) and add Norwegian Nynorsk

### Enhanced Quests

#### Cycleway Quest

- Clearer wording of question (#768)
- Do not ask for a cycleway when a separately mapped sidewalk exists (#718)
- [Show only in a limited set of countries](https://ent8r.github.io/blacklistr/?java=cycleway/AddCycleway.java) (#749)

#### Tactile Pavings Quests

- Clearer description (#762)
- [Show only in a limited set of countries](https://ent8r.github.io/blacklistr/?java=tactile%5Fpaving/AddTactilePavingCrosswalk.java) (#750)

#### Max Speed quest

- Clearer English wording when asking if the road is in or out of town (#757)
- Still disabled for the US (#813)

#### Street surface quest

- Improve street surface quest UI (#76)
- Clearer wording for pedestrian streets (#862) by @ENT8R
- Add "metal" as answer option (#568)
- tag either `sett` or `unhewn_cobblestone`, not `cobblestone`

#### Road name quest

- Clearer UI flow when answering the road has no name (#756)
- Clearer OSM changeset comment message (#158)
- Clearer wording for pedestrian streets (#862)

#### Bus stop shelter quest

- Only show for actual bus and tram stops, clearer wording (#806)

#### Vegetarian quest

- Clearer English wording (#883)

#### Parking fee quest

- Able to select the times where the fee applies / does not apply (#848)

## v3.7

fix critical bug that led to the undo feature not working properly (#849)

## v3.6

- fix country-detection of Arunchal Pradesh
- disable max-speed quest for the United States (#813)
- fix undo not working when conflict arose during upload (#812)
- fix clearing quest changes on undo (bug had no adverse effect)
- updated translations

## v3.5

- _use another tileserver for map display_ (fixes #747 for now) thanks @Akasch
- fix possible error during conflict handling
- change the default priority of some quests
- update translations

_Max speed quest_:

- exclude roads unavailable for vehicles

_Sport quest_:

- fix theoretical crash
- don't show for private pitches

_Cycleway quest_:

- imply `oneway=yes` on `junction=roundabout`
- set correct initial rotation of compass (fixes #760)

## v3.4

same as v3.3 but fixed a stupid bug in cycleway quest

## v3.3

A pretty long list for a bugfix update! To be honest, I squeezed in some tiny features:

- on handling a conflict, recheck if the quest still applies to the updated element (#720)
- fix another crash and a memory leak
- fix crash when unable to create file for taking a photo
- add new language Slovak by Vlado Jendrol and updated other languages

**Add opening hours quest:**

- don't allow the user add an empty opening hours description (#721)

**Add maxspeed quest:**

- clearer wording on confirmation of no sign posted for a street (#715)
- exclude unpaved roads from maxspeed quest (#752)
- allow the user to specify the speed unit in certain currently-mph countries

**Add cycleway quest:**

- don't ask for a cycleway on areas (#711)
- fix crash when inputting certain values for cycleways (#740)
- fix rare crash when rotating the map while specifying the cycleway
- don't display the quest for Android <4.4 users (#713)
- always tag `oneway:bicycle=no` for oneways with bicycle infrastructure for contraflow direction (#717)
- for oneways in contraflow, replace "sharrows" option with option to state that a oneway road is not oneway for cyclists (but has still no dedicated cycleway) (#717)
- show compass needle in cycleway form (#723)
- limit cycleway quests to a few countries which are likely to have any bicycle infrastructure (#749)

## v3.2

- translation updates
- Explicitly state in cycleway quest that both sides need to be filled (#700)
- fix crash when reordering quest priorities (#704)

## v3.1

- solve many warnings and possible problems found with Lint and FindBugs
- fixed build for F-Droid
- fixes for quest selection and reordering (#694, #693)
- cycleway quest: avoid asking for both sides of one side of dual carriageways (#689, #690)
- translation update

## v3.0

### New Features

- you can now disable and reorder the priority of quest types in the settings
- you can now undo hiding of a quests
- you can now attach photos to a note and note comments
- a little nicer settings and about screen

### New Quests

- cycleways
- vegetarian restaurants
- vegan restaurants
- type of car wash

### Enhanced Quests

- house number quest is now disabled for Netherlands, Norway, Denmark and Czech Republic
- house number quest now supports Slovakian addressing system (conscription and orientation numbers)

## v2.4

- slightly change wording of tactile quest question (#547)
- do not assume roads tagged as residential are always urban (following German forum discussion)
- fix a memory leak

## v2.3

Just a few bugfixes and a translation update

## v2.2

- bugfixes (#628, #624, #638, #593, #627, #645, #640 and crash bugs) thanks @dbdean
- lit quest is now shown also for steps (#631) by @dbdean
- reorder the importance of bus stop shelters (#637) and bicycle parkings up (#641), because this data is actually used
- use maxspeed:type to tag implicit speed limits and properly ask for single/dual carriageways in GB (#492)

## v2.1

only bugfixes and translations updates

## v2.0

### New Features

- undo
- compass mode, free map rotate and tilt
- better UI layout in landscape mode

### New Quests

- toilet availability
- power poles material type
- orchard produce
- parking type
- crossing type

### Enhanced Quests

- can specify house names
- can specify advisory speed limits
- change building level quest to tagging

### New Languages

- Persian

## v1.4

- the user's own notes (created on openstreetmap.org) are now also displayed as quests
- StreetComplete version now mentioned in created note
- do not show quests for private roads
- fixed 4 crash bugs for edge cases
- updated translations

## v1.3

Only some minor improvements:

- Don't display quests for private roads
- Don't ask for house numbers inside an area with a housenumber (i.e. schools etc.)
- Explicitly tag a recycling container as overground if user selected that

## v1.2

- add Basque
- update translations
- fix issues
- improve lit-quest

## v1.1

- new quest: Is this street lit?
- street name suggestions for the street name quest
- improvements/fixes for the speed limit quest
- fix performance issues
- some other fixes

## v1.0

- added quests:
  - speed limit
  - sport(s) played on a pitch
  - wheelchair accessibility
  - hydrant type
  - recycling amenity type
  - baby diaper changing table
- improved many quests with country-wise intelligence, such as:
  - able to input multilingual streetname signs
  - sort sports selection by most popular sports first
  - for speed limits, automatically use mph or km/h depending on the country
- make created notes understandable better for non-users of this app
- better credits screen
- can login on osm with google account now
- many bugfixes and improvements

## v0.14

fix crash on leaving note

## v0.13

minor bugfix update: bugfixes, updated translations

## v0.12

- warning dialog if user tries to add information not at his location
- bugfixes
- new languages Ukrainian, Finnish

## v0.11

- bugfixes
- new quest: pitches sport
- new languages: Malayalam, Czech, Portuguese (PT)

## v0.10

fixed #108: `opening_hours` used wrong separator

## v0.9

- add Lithuanian language
- many (crash)bugfixes
- new quests: "Housenumber", "Bus stop shelter", "Bike parking capacity"

## v0.8

- add translations: Albanian, Danish, Japanese, Dutch, Polish, Russian, simplified Chinese, Swedish and Turkish (whooaa!!!)
- enable the app to send crash reports to me
- bugfixes

## v0.7

Same as v0.6 but with a fix for a bug that prevented database upgrade from v0.5 to work properly.

## v0.6

- Groups changes into changesets now by quest type
- Fixed bugs reported here and elsewhere
- made "add building levels" quest clearer by adding an illustration
- added languages: Hungarian, Portuguese, Chinese and Spanish
- other smaller changes

## v0.5

- add better location marker with compass and accuracy display
- add new icons
- add translations: French, Italian, Catalan
- add "What is the surface of this road" quest
- rework "What are the opening hours of this place" quest to be more simple and clear
- some bugfixes

## v0.4

fixed a crash bug, some small usability stuff

## v0.3

Last beta probably. Released on Google Play (Beta) also.

## v0.2

internal forum beta test version no.2
