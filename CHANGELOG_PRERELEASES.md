## v64.0-beta2

- Fix opening hours quest would show up for *any* barrier, amongst other things (#7267) (regression of #6732)

## v64.0-beta1

👋 Hey there, betatesters on Android! You are not alone anymore, you are now joined by betatesters on iOS (TestFlight).
This is the first beta version of StreetComplete available for both Android and iOS!

Tell your friends!

### General

- Add back button to dismissible onboarding screens (#7209)
- Allow swiping between steps on all onboarding screens (#7198)
- Other small improvements (#7228, #7221…), thanks @Amaanprobably
- Back button on the bottom sheet title is now shown when the bottom sheet almost fills the whole screen (#7218)
- Monochrome POI icons with halo don't visually clump together on the map anymore when they are too close together, thanks @sargunv

### New Quests

- _"What’s the surface here?"_ - asked for motorcycle parkings (#7237, #6999, #6829), by @Amaanprobably, @ontheair81
- _"How many sockets does this charging station have?"_ (#6732, #5164), thanks @mcliquid
- _"Can cars be charged here?"_ (#7083, also #7098), by @paulklie. This and the above quest replace the quests that ask if and how many cars / bicycles can be charged at a charging station
- _"Can you lock this while charging?"_, asked for bicycle charging stations etc. (#7189), by @paulklie

### Quest & overlay improvements

- Add tap hint for street side select UI (#7215), by @sargunv
- Localized names: if user explicitly specifies a language, also explicitly tag it that way (#7161)
- Addresses: Don't tag `noaddress=yes` when removing an address (#7145)
- Other small improvements (#7217, #7245, …), thanks @paulklie

### Fixes

- Fix crash on Android when downloading more areas in quick succession (#7051)
- Opening hours: Fix crash when editing times like 26:00 (#7246) (yeah, that's valid OSM syntax!)
- Fix logs screen didn't reliably scroll to bottom when new logs arrived (#7233), by @sargunv
- Postbox collection times: Fix always asked whether to discard changes on exit even when nothing was input
- Parking Fee: Fix never asked whether to discard changes on exit

### Changes and fixes for earlier v64.0-alphas

- Fix crash when scrolling around in building overlay (#7214), by @sargunv
- Fix crash in language selection screen in iOS (#7210, #7213), by @sargunv
- Fix note forms were not closeable by clicking on map (#7216)
- Fix switch-keyboard button now not hidden below keyboard on iOS, show it just above text input field instead (#7208, #6852)
- Fix map click just above forms was not registered as clicking on map (#7216)
- Fix per-app language setting was overwritten on each start of the app on iOS (#7203, #7213), by @sargunv
- Fix settings screen background turning black on Android 7 (#7175), by @sargunv
- Fix quest header was duplicated for non-resurvey quests that had a note
- Reference number: Fix answer "nothing visible" didn't do anything
- Fix rare crash in opening hours and collection times form
- Now possible again to switch directly between different elements in the overlay

### Changes and fixes for earlier v64.0-alphas, related to map display

- Fix pin icons didn't reload correctly when returning from another screen (#7241), by @sargunv
- Fix map-related crash on Android (#7220), by @sargunv
- Made usage of map generally less janky, by @sargunv
- Made overlay elements clickable even easier, by @sargunv
- On iOS, the quick zoom (tap, then press and drag) zooms in when you drag up, like in Apple Maps (#7238), by @sargunv

## v64.0-alpha3

### General

- Support predictive back gesture (#7113), by @sargunv
- Other minor UI improvements
- Reduce yank when animating between screens (#7194, #7193…), by @Amaanprobably, @sargunv
- Show button on map to return to currently focused element (#7195)

### Fixes

- Fix rendering issue in parking overlay form
- Fix avatar image didn't appear immediately after login

### Quest improvements

- Other minor improvements (#7182, #7187, #7201), thanks @paulklie

### Changes and fixes for earlier v64.0-alphas

- Map now works on iOS 15.5 (#7193), by @sargunv
- Dismiss quest, overlay and note forms by clicking on the map (#7179, #7202, …), by @paulklie
- Only show drag handle on bottom sheets if it can actually be dragged up
- Fix crash in cycleway overlay and board type quest on iOS (#7184)
- Fix rare crash in opening hours and collection times form
- Fix cycleway form would in rare circumstances detect changes when there were none
- Fix cycleway form would not recognize resurvey of cycleways as resurvey
- Made overlay elements clickable easier (#7178), thanks @sargunv
- Fix new-address pin was also displayed when editing existing opening hours (#7183)
- Fix zoomed in geometry of focused element was not fully in view (regression of #7135)
- Make buttons in quest answer button bar a little bigger (#7190)

## v64.0-alpha2

🍎 After v64.0-alpha1, this update brings huge changes AGAIN, which again should be mostly
invisible! @sargunv and I ported the map and any interaction with it to a multiplatform UI framework
(#6352, #7088).

But this is not all! @sargunv also took the last remaining steps for a fully working iOS version! (#7125)

### General

- Increase size of time picker in dialogs (#7144), by @Amaanprobably

### Quest improvements

- Step count: Don't ask if step count is given already in another form (#7136, #7137, #7168), by @paulklie, @peternewman
- Sidewalk surface: Upgrade ambiguous to explicit tagging correctly (#7138, #7139), by @Amaanprobably
- Barrier opening: Add answer option "not suitable for wheelchairs" (#7127), by @kmpoppe
- Level of things: Don't ask for things whose location is already known to be non-indoor (#7114)
- Buildings: Can now select "terraced house" (#7159), by @Amaanprobably
- Other small improvements (#7164, #7170, #7165), by @paulklie

### Overlay improvements

- Buildings overlay: Buildings are now shown in 3D again (#7153), by @paulklie

### Fixes

- Fix zoom animation was sometimes interrupted by the map following the user's position (#5860), by @sargunv
- Fix selected language was sometimes overwritten by system default language (#5942), by @sargunv

### Changes and fixes for v64.0-alpha1

- Quest peek height is now relative to available screen height (#7135), by @mcliquid
- Parking charge: Also ask when there's not always a fee (#7133)
- Keyboard would sometimes obscure the quest form (#7107), by @sargunv
- Fix several small issues related to the map (#7152, #7108), by @sargunv
- Small visual fixes (#7132, #7130, #7176)

## v64.0-alpha1

🍏 This update brings huge changes, albeit they should be mostly invisible. I re-did the UI for all
quest forms, a side effect of the migration to a multiplatform UI framework. An iOS version of the
app has now come within reach! (#6842).
@kiliankoe (#6989), @maxwellward (#7033), @sargunv (#7123, #7124) helped a bit, too.

### General

- Re-did UI for splitting ways: It is now based on the crosshair (like moving node and overlay) instead of tapping where to split
- Improved UI for attaching tracks and images to a note and undoing the same
- Now you must press "back" to close a quest form, clicking on the map will not close the form
  anymore (this is a bit of a technical limitation of the new implementation)
- Serbian language in Latin script is now available again (#6914)
- All forms with free text input: Visually show error state when input exceeds 255 characters
- Improved quest creation processing speed after download by about 50%! (#7099), by @paulklie

### New Quests

- _"How much do you need to pay to park here?"_ (#6653), thanks @marekkrug
- _"What doctors are present here?"_ (#7044), by @paulklie
- _"What type of vending machine is this?"_ (#7061), by @paulklie, thanks @Amaanprobably
- _"Who may enter here?"_ (#7071, #7109), by @esilja. Replaces previous quests that asked for playground and tower access

### Fixes

- Fix crash when trying to report another crash via e-mail 😬 (#7052)
- Post box was missing in things overlay (#6976)
- Sharing logs no longer crashes when the log is too large (#5561)
- UI now prevents possibility to create duplicate notes (#4853)
- Context menus now appear at less wrong positions (#6665)
- "Are you sure?" dialog had poor contrast in dark mode (#6753)
- Fix rare crash in places overlay (#6648), and another (#5809)
- Quest form used to not have padding for the camera notch (#6764)
- Internet access: "none" option was not exclusive (#7016)
- Fix formatting of links in notes (#7048)
- Fix crash in postbox collection form when sending app to background
- Other small (visual) fixes (#6827, #7007, #6981, #7070, #7074, #7115…), thanks @esilja, @paulklie

### Quest improvements

- Parking fee: Don't ask for customer parking (#6996), by @paulklie
- Parking access: Add "permit" as answer option (#2662, #7058), by @paulklie
- Crossings: Don't ask for islands and kerb height on continuous crossings (#6983), by @eginhard
- BBQ fuel: Allow to specify several fuels (#6915, #7028), by @paulklie
- Toilets fee: Don't ask in Australia and New Zealand (#7011), by @andrewharvey
- Many quests are also asked for relations now (#7086), by @paulklie
- Toilets fee: Allow answering that there's a fee only for non-customers (#7050), by @esilja
- Add a few icons for various quests (#7128, #7100), by @Amaanprobably
- Other small improvements that improve clarity (#6991, #6984, #7003, #7020, #7027, #7025, #7049, #7072, #7076, #7091, #7118, #7119), thanks @paulklie, @mcliquid

### Problematic Quests removed

- _"Are pedestrians forbidden to walk on this road without a sidewalk here?"_ (#7014)

## v63.0-beta1

### General Improvements

- You’ll now get in-app notifications when a new issue of weeklyOSM is out, the news blog about what’s happening in the OpenStreetMap world (#1796, #6728, #5977)
- You’ll also get in-app notifications about local OSM community events in your area (#6743, #1798)
- You can now change in settings which in-app notifications you want to receive (#6493)
- You can now also zoom by dragging the zoom button up or down (#6719, #6716), by @Helium314
- Redid the UI for opening hours, postbox collection times and parking fee quests with slight improvements, especially in regards to localization (#6691, #6175)

### New Quests

- _"In what direction can you ride this?"_, asked for lifts (#6583), by @NariaReynhard
- _"What kind of artwork is this?"_ (#6605, #2125), by @paulklie and @vfosnar
- _"Are there cabins that you may stay in here?"_ asked for campsites (#6685, #6447), by @paulklie
- _"What’s the surface here?"_, asked for beaches (#6729, #1098), by @mcliquid
- _"Are bicycles allowed on this ferry?"_ (#6718, #6509), by @mcliquid
- _"Does this ferry route transport heavy goods vehicles?"_ (#6734, #6737), by @mcliquid

### Quest improvements

- Postbox collection times: Allow input of time ranges (#6646)
- Information board topics: Allow to select multiple topics (#6148), thanks @paulklie
- Campsite type: Split the quest up into several quests (#6685, #6447), by @paulklie
- Other small improvements on quests (#6704, #6705, #6696, #6710, #6598…), thanks @matkoniecz, @paulklie, @mcliquid

### Fixes

- Parking overlay: Specifying only one side is enabled again (#6721)
- Achievement dialog now displays well on square screen (#6702)
- Fix that some icons were black on black background in dark mode (#6453, #6717), by @Helium314

## v62.0-beta1

### New quests

- _"Can you wash your hands here?"_, asked for toilets (#6394), by @paulklie
- _"How many people can sleep here?"_ asked for wilderness huts etc. (#6442), by @paulklie
- _"On which level number is this thing located?"_ asked for things in malls (#6433), by @paulklie
- _"Do you have to pay to park here?"_ asked for motorbike parkings (#6532), by @ElPoraz
- _"Is firewood provided here?"_ (#6538) asked for firepits etc., by @NicodeH
- _"Where is this first aid kit located?"_ (#6539), by @NicodeH
- _"Does this aerial lift transport bicycles?"_ (#6544), by @RaphaLLamothe
- _"Does this road have lane markings?"_ (#6490)
- _"Can bicycles be charged here, too?"_ (#6595)
- _"How many bicycles can be charged here at the same time?"_ (#6595), thanks @HugoTHOLLON
- _"Who may access this tower? It may be free or paid."_ (#6581), thanks @ElPoraz

### General

- Address and name quests and overlay look different now, with a few improvements (#6403, #5479)
- Max weight form quest looks different now, with a few improvements (#6490)
- Any quest or overlay that shows an image picker or an illustration of a street looks different now, with many improvements (#6490, #6239, #6560) such as showing several apply last-picked buttons like in the building levels quest (#5605)
- Replace app suggestion Organic Maps with CoMaps (#6451)
- Language selection is now in a separate screen, with search function (#6490)
- Map buttons are now better visible in dark mode (#6587), by @paulklie
- Fix StreetComplete has been removing newlines from tag values it touches (#6466)

### Quest improvements

- Refine which quests to ask for the different places and things (#6481, #6475, #6428, #6474, #6463, #6402, #6498, #6499, #6523, #6522, #6521, #6520, #6529, #6519, #6492, #6534, #6549, #6576…), by @paulklie
- Housenumbers: Ask for some buildings that were previously excluded (#6533)
- Place name: Ask for some historic things, such as monuments (#6042)
- Postbox collection times: Disable in Denmark (#6455), by @matkoniecz
- Bus stop name: Disable in Sweden (#6515), by @paulklie
- Mopeds on bike paths: Enable in Sweden (#6516), by @paulklie
- Information board topics: Add "rules" (#6580), by @NicodeH
- Postbox royal cypher: Show "None" as normal answer option (#6490)
- Other small refinements (#6445, #6511…)

### Overlay improvements

- Things: Added exercise equipment, moorings, buoys, disc golf, graves and pillory as things (#6546), by @paulklie
- Places: don't show "it's vacant" option for already vacant places (#6462), by @paulklie
- Addresses: Allow omitting the street name (#6528)

## v61.0-beta1

### Android below 7.1 no longer supported (#6210)

In reality, support for Android versions below 7.1 has already been just on-paper for a while now,  as more and more issues with old Androids have been popping up lately that cannot be fixed on our side. Luckily, it seems that >99.6% of app users already use Android 7.1 and above. (See #6210)

### Main screen improvements (#6213)

Cleaned up the appearance of the main screen a bit:

- Merged the +/- buttons into one button, not shown by default, it's a setting. By the way, you can also zoom with one finger by double-tapping, then moving the finger up or down. This works in any app in which you can zoom, actually.
- The upload button is now in the main menu
- The pointer pin button moves more smoothly (in an ellipsis near the screen border)
- Added a scale bar on the lower right (#5765)
- Replaced the static map attribution text with an info button

### New overlays

- _MTB difficulty_: Add the difficulty ratings for mountainbike trails (#1850, #5726), thanks @RubenKelevra and @ravenfield. It is disabled by default because you need to be a domain expert to accurately record this data. (#6226)

### New quests

- _"Are gluten-free products offered here?"_, disabled by default (#3031, #6143), by @mnalis

### Quest and Overlay improvements

- Opening hours: Fix semantic issue when specifying off-days at the top (#6191)
- Surfaces: Fix surface was asked twice when a generic surface was selected (#6172)
- Smoothness: Don't ask if surface is still correct just after specifying the surface smoothness (#6178)
- Baby changing tables: Ask at more places (#6167), by @agent-redd
- Sidewalk overlay: Align which roads are selectable with the bike paths overlay, for consistency (#6185), by @mnalis
- Roadwork: Also ask whether minor roadwork (i.e. construction without full road closure) is completed (#6090)
- Various: Ask about more features of alpine and wilderness huts (#6200, #6201), thanks @tiptoptom
- Building levels: Prettified input form a bit (#6022), by @GaeaKat
- Other small things (#6209, #6216, #6217…), thanks @mnalis

### General Improvements

- Performance improvements on upload and download (#6150) as well as significantly decreased data download size (#6218)
- Performance improvements in the quest selection screen (#6205), by @irakliy01
- Added a few new links to unlock via achievements. Newly added links do not unlock silently for old users anymore (#6220)
- Render parks and playgrounds with an outline (#6221), by @doniks

## v60.0-beta1

Redesigned the main screen a little. Here and there you'll notice small differences. The dialog to
start the team mode from the menu is more descriptive now, for example. (#5799)

Also, added Estonian language by Priit Jõerüüt

### New quests

- _"How is this power line attached?"_ (#4825, #5382), by @Jean-BaptisteC
- _"What bicycle equipment is available here?"_, asked for bicycle repair stations (#5910, #6008)
- _"Which type of boats are for rent here?"_ (#5741, #5426), thanks @kmpoppe

### Quest and Overlay improvements

- Cycleway overlay: Can now specify signs on bicycle access for pedestrian roads (#6016, #6020)
- Show overlays already at lower zoom levels (#5986, #6010)
- Other small improvements (#6015, #6002, #5996, #6021 ...)

### Fixes

- Cycleway: Fix selecting "unspecified lane" didn't do anything in edge cases (#5988)
- Cycleway: Don't assume a default pedestrian access on bike paths (because that depends on legislation) (#5991)
- Max vehicle height: Fix wasn't shown below roofs (#5912, #5915), thanks @RubenKelevra
- Fix app sometimes went back to system default locale on screen rotate (#5942)

## v60.0-alpha2

- Added language Estonian by Priit Jõerüüt
- Building levels: Don't ask if building levels and roof height are already specified (#5996)

## v60.0-alpha1

Redesigned the main screen a little. Here and there you'll notice small differences. The dialog to
start the team mode from the menu is more descriptive now, for example. (#5799)

## v59.0-beta1

Oh! Again such a monster update!

### 🗺️ MapLibre (#5693), by @Helium314 and me

Migrated from the map renderer tangram-es to [MapLibre](https://maplibre.org/). The former has effectively been unmaintained for quite a few years and does not support modern iOS, this solves some long-standing display and crash issues.

Display-wise, nothing much *should* change, the map will just look and behave a little different.

Issues solved by the migration are #5601, #5241, #5216, #5161, #5146, #5137, #5048, #5027, #5006, #4844, #4830, #4556, #4554, #4550, #4542, #4522, #4277, #4259, #4026, #3417, #3397, #3346, #3275, #3022, #2878, #2711, #2703, #2612, #2571, #2373, #2359, #2358, #2187, #1713, #1684, #1264, #1019, #869, #807, #318, #179, [mapstyle#119](https://github.com/streetcomplete/streetcomplete-mapstyle/issues/119), [mapstyle#56](https://github.com/streetcomplete/streetcomplete-mapstyle/issues/56)... maybe more. Thanks to @riQQ for this list.

### 🍏 iOS progress

In case you wondered what happened since I [announced to work on an iOS version of the app](https://www.openstreetmap.org/user/westnordost/diary/403704). I've taken some big steps forward, see [this progress update](https://github.com/streetcomplete/StreetComplete/issues/5421#issuecomment-2332402123) for more info.
Most of it is behind the scenes, but what's visible is the completely redone UI for the settings screens, about screens, statistics screens and tutorial screens (#5719, #5855). Of course, it looks mostly the same now, as it should.

### New Quests
- _"What’s the title of this information board?"_, (#5882, #5871), by @mnalis
- _"What is the width of the opening here?"_, asked for gates etc. (#5713, #5800) by @Nick-Tallguy

### Removed Quests
- Asking _"Is this a one-way street?"_, based on processed data from ImproveOSM because Grab is shutting it down (#5725)
- Asking _"Can you pick up parcels here?"_ and _"Can you drop off parcels here?"_ because it turns out that it is often not surveyable and the answer is always the same for one brand of parcel lockers (#5763)

### Fixes
- Fix element is now updated correctly on conflict during upload (#5818), by @Helium314
- Fix only enable OK button for separate bike paths in bike path overlay after changes have been made (#5819)
- Fix rare crash issues (#5804, #5873)

### Quest Improvements
- Tree leaf type: User can answer that it is now a tree stump (#5853), by @mnalis
- Barrier opening: Allow inputting widths of above 9.99 meters (#5865)
- Charging stations: Skip private ones (#5842), by @mnalis
- Traffic signals vibration: Disable in Bulgaria (#5846)
- Barrier opening: Ask for certain cycle barriers, too (#5823), by @mnalis
- Barrier opening: Also disable this quest when disabling all width-measurement quests
- Ask many road-related quests also for busways (#5749)
- Recycling containers: Also ask for containers that have been mapped as an area (#5735)
- Recycling containers: Also highlight nearby waste containers and bins (#5754)
- Accepting cards: Don't ask for places that explicitly require no fee (#5772), by @HolgerJeromin
- Drinking water: Don't ask for intermittent water sources (#5750), by @mnalis
- Track type: Don't ask for track type on bridges (#5731), by @kmpoppe
- Building levels, roof shapes: Also ask for houseboats (#5737), by @HolgerJeromin
- Traffic signals vibration: Disabled in Finland (#5795), by @mnalis
- Clothing container operators: Allow answering that there's no information on the container (#5764)
- Check existence: Don't ask if something else on the same place has just been checked (#5729)
- Other small improvements (#5740, #5792, #5548, #5776, #5877, #5880, #5879, #5874...), thanks @mnalis, @RubenKelevra

### Overlay Improvements
- Addresses: Presence of housenumbers is now highlighted in blue (#5748)
- Bike paths, Sidewalks: Highlight less situations as missing (#5716, #5722)
- Bike paths: When selecting "designated shared-use path", always mark it as designated for pedestrians, too (#5742)
- allow hiding notes that block editing with an overlay (#5703)

## v59.0-alpha4

- Some statistics in the user profile screen look different now (#5855)
- Fix follow-me mode didn't always turn off on moving map (#5824)
- Make it easier to hit the right line in overlays (#5849)
- Tree leaf type: User can answer that it is now a tree stump (#5853), by @mnalis
- Barrier opening: Allow inputting widths of above 9.99 meters (#5865)

## v59.0-alpha3

Almost no change from alpha2. For debugging #5850.

## v59.0-alpha2

### Fixes
- Fix common crash on startup (#5829)
- Fix some icons were missing in things & places overlays (#5832)
- Fix rotation of view direction cone (#5821, #5834)
- Fix barrier opening width should only be asked for barriers that are vertices of ways (#5830)
- Workaround building color didn't change immediately when selecting another type (#2746)
- Fix element is now updated correctly on conflict during upload (#5818), by @Helium314
- Fix only enable OK button for separate bike paths in bike path overlay after changes have been made (#5819)
- Fix links in the "What's New" dialog didn't work (#5814)

### Improvements
- Scale icons on low zoom a bit, cluster pin later (#5848, #5835)
- Load quests on lower zoom levels, too (#5823)
- Make it easier to tap elements on overlay when there are quests around (#5827)
- Charging stations: Skip private ones (#5842), by @mnalis
- Traffic signals vibration: Disable in Bulgaria (#5846)
- Barrier opening: Ask for certain cycle barriers, too (#5823), by @mnalis
- Barrier opening: Also disable this quest when disabling all width-measurement quests

## v59.0-alpha1

### 🗺️ MapLibre (#5693), by @Helium314 and me

Migrated from the map renderer tangram-es to [MapLibre](https://maplibre.org/). The former has effectively been unmaintained for quite a few years and does not support modern iOS, this solves some long-standing display and crash issues.

Display-wise, nothing much *should* change, the map will just look and behave a little different.

Issues solved by the migration are #5601, #5241, #5216, #5161, #5146, #5137, #5048, #5027, #5006, #4844, #4830, #4556, #4554, #4550, #4542, #4522, #4277, #4259, #4026, #3417, #3397, #3346, #3275, #3022, #2878, #2711, #2703, #2612, #2571, #2373, #2359, #2358, #2187, #1713, #1684, #1264, #1019, #869, #807, #318, #179, [mapstyle#119](https://github.com/streetcomplete/streetcomplete-mapstyle/issues/119), [mapstyle#56](https://github.com/streetcomplete/streetcomplete-mapstyle/issues/56)... maybe more. Thanks to @riQQ for this list.

### General improvements

- Redesigned the settings screens, about screens and tutorial screens (#5719)
- minor improvements for right-to-left localization (Arabic, Hebrew, Farsi)
- fix rare crash issues (#5804)

### New Quests

- _"What is the width of the opening here?"_, asked for gates etc. (#5713, #5800) by @Nick-Tallguy

### Removed Quests

- Asking _"Is this a one-way street?"_, based on processed data from ImproveOSM because Grab is shutting it down (#5725)
- Asking _"Can you pick up parcels here?"_ and _"Can you drop off parcels here?"_ because it turns out that it is often not surveyable and the answer is always the same for one brand of parcel lockers (#5763)

### Overlay Improvements

- Addresses: Presence of housenumbers is now highlighted in blue (#5748)
- Bike paths, Sidewalks: Highlight less situations as missing (#5716, #5722)
- Bike paths: When selecting "designated shared-use path", always mark it as designated for pedestrians, too (#5742)
- allow hiding notes that block editing with an overlay (#5703)

### Quest Improvements

- Ask many road-related quests also for busways (#5749)
- Recycling containers: Also ask for containers that have been mapped as an area (#5735)
- Recycling containers: Also highlight nearby waste containers and bins (#5754)
- Accepting cards: Don't ask for places that explicitly require no fee (#5772), by @HolgerJeromin
- Drinking water: Don't ask for intermittent water sources (#5750), by @mnalis
- Track type: Don't ask for track type on bridges (#5731), by @kmpoppe
- Building levels, roof shapes: Also ask for houseboats (#5737), by @HolgerJeromin
- Traffic signals vibration: Disabled in Finland (#5795), by @mnalis
- Clothing container operators: Allow answering that there's no information on the container (#5764)
- Check existence: Don't ask if something else on the same place has just been checked (#5729)
- Other small improvements (#5740, #5792, #5548, #5776, ...), thanks @mnalis

## v57.0-beta2

- Use new method to upload and download data as mandated by Google Play policy. Be sure to report any issues related to this. On Android 13 and above, there's no notification for continuing to sync in the background anymore. (#5492)
- A few small fixes, thanks @matkoniecz

## v57.0-beta1

### Buildings Overlay (#5461)

<img src="https://github.com/streetcomplete/StreetComplete/assets/4661658/f9e6417d-5344-4e1d-9d80-7d1ce8533d84" width="300">

The new Buildings overlay now lets you comprehensively view and edit building types all around town, color-coded for your convenience!

(You'll be amazed and/or appalled how many buildings are currently actually tagged wrong or imprecise! 😅)

### Things Overlay (#4912), thanks @matkoniecz

<img  src="https://github.com/streetcomplete/StreetComplete/assets/4661658/0f971035-06a5-474b-b410-41fcc5d4f7da" width="300"> 

The new Things overlay allows you to map all those small map features like benches, bicycle parkings, roadside trees, ATMs and other street furniture in general.

It complements the Places Overlay which has been renamed from Shop overlay because it now also lets you map places that are not shops - like hotels, hospitals, schools and so forth. (#5152)

### New Quests
- The crossing type quest has been split up into two quests: One that asks for markings, the other for traffic signals (#5471, #5476)

### Quest and Overlay Enhancements and Fixes
- Lit and Surface Overlay: now each have a button to apply the last answer (#4741)
- Measuring widths and heights: [StreetMeasure](https://play.google.com/store/apps/details?id=de.westnordost.streetmeasure) doesn't work for you? In that case you'll be offered to quickly disable these AR measurement quests now (#4849)
- Max Height: Don't prompt to estimate height when there is no sign (#5458)
- Opening Hours: Also ask for places whose already set opening hours likely contain mistakes (#5463)
- Building Type: disabled by default now, because we have an overlay now and it was really spammy, don't you think?
- Fire Hydrant Diameter: Disable for Austria again (#5470)

### Other
- Fix issues with doing edits while data is being downloaded (#4258)
- Add some interesting links as achievement rewards (#5466)
- Translation to Amharic has been disabled, it was not maintained for over a year
- Other small enhancements, performance and wording improvements (#5468, #5154, #5474...), thanks @Jean-BaptisteC

## v56.0-beta1

### New Authorization

Sorry, you'll need to log in again. Authorization via OAuth 1.0a is being deprecated on OSM, so we have switched to OAuth 2.0 (#5383, #5322).
Sponsored by [SUSE Open Source Community Citizens (OSCC)](https://www.suse.com/esg/).

### General Enhancements and Fixes

- You can now view, filter and copy logs from within the app. (This helps finding the cause of issues.) (#5335), by @neonowy
- Other small improvements (#5360), by @riQQ

### New Quests

- *Is there a sanitary dump station?* asked for caravan sites (#5363), by @qugebert

### Quest Enhancements and Fixes

- Other small improvements (#5332, #5371, #5399, #5395), thanks @catdogmat, @mnalis

## v54.0-beta1

### New Quests

- *What’s the height of the curbs at this crossing?* (asked even if the curbs have been mapped separately, too) (#5104)

### Quest Enhancements

- Max height: Also ask for crossings with rails electrified with overhead wires (#5180)
- Bus stop refs: Also ask in Colombia (#5124)
- Sidewalks, bike paths: Also ask on roads that have a foot path or bike path nearby. You can always answer that it exists but is displayed separately on the map (#5060).

### General Enhancements and Fixes

- Moved the overlay button to the main screen (#5109). Enjoy the short tutorial first time you tap on that button (#5158)!
- Never ask *"Are you sure you checked this on-site?"* for places you've just been to up to ten minutes ago (#4947), by @Helium314
- Other small fixes and enhancements (#5147, #5091, #5146, #5173, #5177, #5181, #5190, #5194, #5184...), thanks @tapetis, @Helium314, @riQQ, @arrival-spring

## v53.0-beta1

https://user-images.githubusercontent.com/4661658/235497181-6aa55fad-9957-4cde-9b7d-4b830c57686d.mp4

### Quest & Overlay Enhancements

- (Note for beta users: The following two features currently only work on objects that do not extend beyond 100m from the cursor position when tapped. A fix is in review.)
- Parking overlay: You can now add and modify curb extensions etc. (#4976)
- Address overlay: You can now add addresses as entrances directly to building outlines (#4976)
- Also ask whether barbecue spots are covered (#4905), thanks @roptat
- Crossing: Different wording and use `crossing=informal` for points at which there are no crossings but people can still cross (#4944)

### New Quests

- *Which customers visit this hairdresser?* (#4833, #4909), by @mnalis

### General Enhancements

- It is now visible on the map which areas have been downloaded already (#4970, #4986)
- Two pane layout for settings & about screen for tablets (#4954), by @tapetis
- Other small fixes and enhancements (#4842, #4970, #4984, #4985, #4991, #4992), thanks @tapetis, @Helium314

## v52.0-beta1

![](https://user-images.githubusercontent.com/4661658/226214144-5a405a98-79d6-4d6c-8759-8739c2c5fc46.png)

### New overlay for surfaces (#4378, #4642), thanks @matkoniecz

Now you can comprehensively map and verify surfaces mapped for roads and paths!

### New Quests

- *Is this still here?*, asked for shops every 2 years (#4232, #4843), by @matkoniecz

### Enhancements and Fixes

- Addresses: Allow specifying block numbers (#4718, #4850), by @Helium314
- Fix block number input in Japan, by @Helium314
- Address overlay: Addresses can now not be added to buildings in Italy (#4801)
- Address overlay: You can now add addresses to entrances (#4823)
- Surfaces: Added "persistent mud" as selectable surface, by @matkoniecz
- Building levels: Require specifying the roof levels, too (with exceptions) (#4874, #4821, #4661)
- Ask about wheelchair accessibility of toilets in restaurants etc. (#4866, #4868, #4879), thanks @arrival-spring, @Helium314
- You can now duplicate quest presets (#4609, #4820), by @michalgwo
- Fix some memory leaks and crashes (#4869, #4884)
- Other small enhancements (#4864, #4880, #4883...), thanks @matkoniecz, @arrival-spring, @Helium314

## v51.0-beta1

![](https://user-images.githubusercontent.com/4661658/215844472-67489786-bb31-40d5-937e-bd497a3872d1.jpg)![](https://user-images.githubusercontent.com/4661658/215844510-eae189f9-cdbe-4936-ad08-96e33f22a11a.jpg)

### Standalone measuring app

[![](https://raw.githubusercontent.com/streetcomplete/StreetMeasure/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png)](https://play.google.com/store/apps/details?id=de.westnordost.streetmeasure)

Measuring distances with the camera has been outsourced into a standalone app, so you can also measure things outside of the context of solving quests: [StreetMeasure](https://play.google.com/store/apps/details?id=de.westnordost.streetmeasure)

This has been done for licensing reasons (see #4289). In a nutshell, ARCore (= [Google Play Services for AR](https://play.google.com/store/apps/details?id=com.google.ar.core)), on which the measuring is dependent on, is not open source.
This is why it is also not available on the main repo in F-Droid. If you do not have Google Play, you can [download the APK from GitHub](https://github.com/streetcomplete/StreetMeasure/releases), but then you also need to install ARCore.

### New Quests

- *"Is this also here outside of winter?"*, asked for grit bins (#4749, #4754), by @FloEdelmann

### Enhancements

- The standalone measure app now explains how to achieve optimal precision and warns if the potential for a measuring error becomes too big (#4655)
- Parking overlay: Support new street parking schema (#4664, #4768), sponsored by @gislars from [OpenStreetMap Verkehrswende](https://parkraum.osm-verkehrswende.org/)
- Parking overlay: Add option to specify that cars park on alternating sides (i.e. not enough space to park on both sides at the same time)
- Address overlay: Make entering addresses in places with no street names more efficient (#4718, #4751), by @Helium314
- Ask for kerbs also on traffic islands (#3786), by @arrival-spring
- The name and location of a place is now also shown in the undo dialog (#4767, #4772), by @Helium314
- Other small enhancements (#4762, #4750, #2880, #4765, #4710, #4781...), thanks @matkoniecz, @FloEdelmann, @Helium314

### Fixes

- Don't ask whether vacant shop is still the same shop (#4752, #4756), by @Helium314
- Other fixes (#4774, #4763...)

## v50.0-beta1

### 🚲️️ Cycleway Overlay (#4657, #4233)

There is a new overlay which allows you to review and edit the streetside cycleway infrastructure as well as whether there is any segregation on combined foot- and cycle paths. Sponsored by [FixMyCity](https://www.fixmycity.de/).

### 🚚 Move node (#3502, #4579), by @Helium314

"It’s at slightly different position…" is a new generic answer available for every POI-based quest and overlay. It lets you correct the position of the POI.

### Enhanced Overlays

- Sidewalks: Missing sidewalk information on unpaved roads and on roads with a very low speed limit are not shown in red
- Sidewalks: Support sidewalks mapped on exclusive cycleways
- Street parking: Fix a crash when editing partially (#4634)
- Shops: Add option to explicitely state that a shop has no name (#4648)
- Shops: Fix removing localized names did not actually remove them
- Other small enhancements (see #4657)

### Enhanced Quests

- Surfaces for segregated foot and cycleway: Correct common surface (#4615), by @mnalis
- Disabled cycleway quest by default because it can be done more comprehensively in the overlay
- Road width: Ask for confirmation when adding improbable road widths (#4655)
- Other small enhancements (#4637, #4643, #4649...), thanks @matkoniecz

## v49.0-beta1

### QR Codes

Share your quest presets with friends via QR Code! Also, check out [streetcomplete.app](https://streetcomplete.app)! (#3481, #4537). Sponsored by [FixMyCity](https://www.fixmycity.de/).

### My Profile screen

The My Profile screen is now more interesting and engaging! New graphics, new animations, weekly ranks and a calendar of your recent activity! (#2946, #3230, #4572, #4569, #3414, #4203), by @matkoniecz

### Enhanced Quests

- Cycle lanes: Only show "advisory cycle lane" option in countries where a concept of such second-rate cycle lanes exist (#4502)
- Cycle lanes: Adapt appearance of cycle lanes, depending on the country one is mapping in (#4502)
- Cycle lanes, sidewalk surface: Also remember and offer shortcut to apply last selection for roads on which only one side is to be defined (#4524, #4503), by @Helium314
- Shop overlay: Disable 3D buildings for easier placement of new shops (#4563)
- Shop overlay: Allow input of names in different languages (in countries where that's common)
- Shop overlay, Bus stop names, place names...: Remember user's choice about which primary language to use for multilingual names (#4257)
- Bike parking type: add two-tier bike racks as answer option (#4473)
- Other small enhancements and fixes (#4559, #4560...)

## v48.0-beta1

## M-M-M-Mega Update!

A few things came together... this update contains what would usually be in 3 or 4 releases! Enjoy!

### 1️⃣️️ Addresses Overlay

The most efficient way to map addresses on-site, even in places where no buildings are mapped yet!
You can add addresses to buildings, edit existing ones and also add them at any new position, using the same sleek UI as for the address quests.

### 🛍️ Shops Overlay

Easily map and check what shops exist in your local shopping area!

### 🚀 HUGE performance improvements (#4079, #4125), all by @Helium314

The app is now more responsive during normal use thanks to extensive caching and other improvements, in particular, roughly:

- 2x to 8x times faster display of quest pins and overlays
- 2x to 3x times faster showing of quest details
- up to 2x times faster solving of quests
- up to 2x faster upload and thus another 2x faster solving of quests if auto-sync is on (#4077, #4472)
- slightly faster download (#4469)

### New Quests

- *How is drinking water provided here?* (#4423, #4390), by @matkoniecz
- *Which direction leads upwards here?*, asked for mountainbike tracks (#4385), by @matkoniecz
- *What’s the identification number here?*, asked for emergency access points (#4384, #4386), by @mcliquid
- *What’s the identification number here?*, asked for fire hydrants (#3059, #4440), by @mcliquid
- *Can this cycle barrier be opened?* (#4293, #4406), by @mnalis

### Other enhancements

- Redid UI for selecting the type of shop - better search, with icons from iD! (#3774)
- Redid UI for the housenumber and address quests to be consistent with the overlay
- Add links to documentation for tags changed as shown in undo dialog (#3442, #4419), by @matkoniecz
- The obstrusive downloading-view is gone, downloading and uploading is now shown in the upper left corner
- Other small enhancements and fixes (#4431, #4453, #4496...), thanks @matkoniecz, @Helium314

## v48.0-alpha1

## M-M-M-Mega Update!

A few things came together... this update contains what would usually be in 3 or 4 releases! Have fun!

![](https://user-images.githubusercontent.com/4661658/193340941-84d9544c-355c-4acb-ba01-2031e20357ef.png)![](https://user-images.githubusercontent.com/4661658/193340924-492b148a-2f82-4fe5-8652-8e3d601281ed.png)

### 1️⃣️️ Addresses Overlay

The most efficient way to map addresses on-site, even in places where no buildings are mapped yet!
You can add addresses to buildings, edit existing ones and also add them at any new position, using the same sleek UI as for the address quests.

### 🛍️ Shops Overlay

Easily map and check what shops exist in your local shopping area!

### 🚀 HUGE performance improvements (#4079, #4125), by @Helium314

The app is now more responsive during normal use thanks to extensive caching, in particular, roughly:

- 2x to 8x times faster display of quest pins and overlays
- 2x to 3x times faster showing of quest details
- up to 2x times faster solving of a quests

### New Quests

- *How is drinking water provided here?* (#4423, #4390), by @matkoniecz
- *Which direction leads upwards here?*, asked for mountainbike tracks (#4385), by @matkoniecz
- *What’s the identification number here?*, asked for emergency access points (#4384, #4386), by @mcliquid

### Other enhancements

- Redid UI for selecting the type of shop - better search, with icons from iD! (#3774)
- Redid UI for the housenumber and address quests to be consistent with the overlay
- Add links to documentation for tags changed as shown in undo dialog (#3442, #4419), by @matkoniecz
- The obstrusive downloading-view is gone, downloading and uploading is now shown in the upper left corner
- Other small enhancements and fixes (#4431, #4429, #4424, #4435...), thanks @matkoniecz, @mcliquid, @Helium314

## v47.0-beta1

### 🅿️ Street Parking Overlay (#4177, #4329)

Comprehensively record the physical parking situation along the streets in your neighborhood!
Be warned, of the overlays so far, this is the most complex one and the first that does not replace a pre-existing quest 🙂

### New Quests

- *Can you deposit cash at this ATM?* (#4292, #4333), by @mnalis

### Other enhancements

- All overlays now use the same color-blind friendly color-palette for consistency
- Several performance improvements and reduced network traffic on uploading (#4078, #4202, #4319, #4339), by @Helium314
- Performance improvement when scrolling the map (#4283), by @Helium314
- Refine when the entrance type quest is asked (#4372, #4380), by @matkoniecz
- Other minor enhancements (#4380, #4381, #4393), thanks @matkoniecz, @HolgerJeromin, @mcliquid

### Fixes

- Fix sometimes quests would vanish after uploading (#4371)
- Fix common crash when using the app for a long time, then sending it to background

## v47.0-alpha1

### 🅿️ Street Parking Overlay

![](https://user-images.githubusercontent.com/4661658/187932690-0fb5700f-bdd1-40c0-9543-7ea73bf0b4cb.png)

Comprehensively record the physical parking situation along the streets in your neighbourhood!
Be warned, of the overlays so far, this is the most complex one and the first that does not replace a pre-existing quest 🙂

### Other enhancements and fixes

- All overlays now use the same color-blind friendly color-palette for consistency

## v46.0-beta1

### New Quests

Asked for campsites (#4213), by @mnalis. All but the first are disabled by default:

- *Who may camp here?*
- *Are there showers here?*
- *Is drinking water available here?*
- *Is there any power supply here?*

### Disabled Quests

*Orchard produce*, *Building levels*, *Sidewalks* and *Lit roads* are now disabled by default because either they are replaced by designated overlays or they overwhelm users (#4264,...).
You can enable them again in the settings at any time.

### Enhancements on quests

- Road surface: Ask for confirmation when selection contradicts a previous info (#4105, #662 #4063), by @matkoniecz
- Opening hours: Months and weekdays are now (also) shown in the local language
- All bus stop quests: Ask for any public transport stops, e.g. subway, train, ferry, ...
- Road width: Also ask for the max width of chokers and chicanes (#3258)
- Internet access: Do not ask for backcountry campsites
- Steps incline: Fix display issue on Android 5 (#4317)

### Other enhancements and fixes

- Fix editing tunnels in overlays (#4269), thanks @matkoniecz
- Fix rare error when uploading too long opening hours (#3471)
- Fix rare faulty links in notes (#4312, #4309)
- Improved map style clarity, especially in regard to overlays (#4291, #4310, ...)
- Performance improvement on uploading (#4297), by @Helium314
- More minor enhancements and fixes (#4158, #4226, #4307, #4302, #4271...), thanks @Helium314, @peternewman

## v45.0-beta1

### 🍰 Overlays!

Make every survey unique! How? Dedicate each to updating a different aspect of the map by activating a different overlay, respectively!

This is **for advanced users** who want to be more efficient in adding and maintaining certain data area-wide. Read more in [this blog post](https://www.openstreetmap.org/user/westnordost/diary/399378)!

For now, there are only overlays for *street lighting* and for *sidewalks*, more will be added over time. 🎉 (#2461, #2506, #3920, #3644)

### New Quests

- *What kind of memorial is this?* (#4102, #1572), by @matkoniecz, thanks @kmpoppe, @naposm, @FloEdelmann
- *Is there a summit cross here?* (#4111, #4095), by @FloEdelmann
- *What is signed at this entrance?* (#4066, #3064), by @matkoniecz

### Enhancements and Fixes

- Do not assume that any cycle lane or track on the right side of the road will always be in forward direction (in countries with right-hand traffic, for left-hand traffic respectively)
- Do not require OpenStreetMap read/write GPS track permission for login (the record GPS track feature can then simply not be used) (#4122)
- Improve GPS accuracy for some devices (#34139...), by @Helium314
- In dark mode, remove obstrusive glow from railways and motorways, by @Helium314
- Other small enhancements and performance improvements (#3919, #4120...)

## v45.0-alpha1

### 🍰 Overlays!

Make every survey unique! How? Dedicate each to updating a different aspect of the map by activating a different overlay, respectively!

This is **for advanced users** who want to be more efficient in adding and maintaining certain data area-wide. Read more in [this blog post](https://www.openstreetmap.org/user/westnordost/diary/399378)!
For now, there are only overlays for *street lighting* and for *sidewalks*, more will be added over time. 🎉 (#2461, #2506, #3920)

### New Quests

- *What kind of memorial is this?* (#4102, #1572), by @matkoniecz, thanks @kmpoppe, @naposm, @FloEdelmann

### Enhancements and Fixes

- Other small enhancements (#3919, ...)

## v44.0-beta1

### Recording GPS track

Did you ever notice some path missing on OSM while solving quests? Now, you can walk that path and then attach it to a public note!
Note that the track can only be recorded while the app is in foreground. So, it is not meant to replace tools like GPS Logger but to help locate short missing path segments and similar.
(#3573, #2209), by @goldbattle

### New Quests

- *What type of bicycle rental is this?* (#3995, #4010), by @FloEdelmann
- *How many rental bike spaces are here?* (#2129, #4008), by @FloEdelmann
- *Are bicycle repair services offered here?*, disabled by default (#4012, #4038), by @matkoniecz
- *Are second-hand bicycles sold here?, disabled by default* (#4012, #4038), by @matkoniecz
- *What kind of building entrance is this?* (#3941, #3949), by @matkoniecz

### Enhancements and Fixes

- Clarify a lot of wordings in American English (#4030) by @ZeLonewolf
- Various minor enhancements, wording improvements and fixes (#4015, #4042, #4001, #4039, #4032, #4056...), thanks @Helium314, @peternewman, @riQQ


## v43.0-beta1

### New Quests

- *How many steps are here?*, asked for stiles (#3932, #3141), by @matkoniecz
- *Is there a working bicycle pump available for everyone here?*, asked for bicycle shops etc. (#3934, #3053), by @mnalis
- *Is there an air compressor available here?*, asked for fuel stations (#3934, #3053), by @mnalis
- *Are credit or debit cards accepted here?*, disabled by default (#3953), by @matkoniecz

### Other Enhancements

- Do not ask questions about objects tagged as seasonal (#3943)
- Various minor enhancements and fixes (#3931, #3933, #3717, #3951, #3698, #3795, #3972...), thanks @mnalis, @CJ-Malone, @matkoniecz

### New Translations

- Added Armenian, by Alexander and Anteneh Belayneh
- Added Esperanto, by Jolesh, Jan, Martin Constantino-Bodin, Hendursaga, Robin van der Vliet, Fabio and more

## v42.0-beta1

### New Quests

- *Is smoking allowed anywhere in this place?* (#539, #3784), by @mnalis
- *What is the surface of the sidewalk here?* (#1593, #3735), by @arrival-spring
- *What kind of seating does this place have?* (#1176, #3758), by @fatal69100

### Quest enhancements

- Parking fee: Add answer option for when there is no fee but a max parking duration (#3841, #981, #102)
- Sidewalk: Also ask when current tagging is incomplete or invalid (#3821), by @arrival-spring
- Tactile paving on crossings: Add answer option that it’s only one one side (#3844, #3813), by @arrival-spring
- Barrier details: Add answer option that leads to changing the type of barrier (#3846, #3563, #3845, #3817), by @mnalis
- Oneway streets: Also ask for roads that are below or equal 4 meters wide
- Cycleway: Warn user when he is about to remove that cyclists may use a oneway road in both directions (#3795)
- Halal: Don't ask for vegan only places (#3883), by @peternewman
- Fire hydrant position: Allow user to delete the hydrant if it doesn't exist on-site (#3871)

### Enhancements and Fixes

- Quest titles are now shown in a more consistent manner: The name, type and location of the object it is asked for is always shown below the question (#3870)
- Other minor enhancements and fixes (#3852)

## v41.0-beta1

### Measure with the camera (AR)

Measuring with AR is only supported [on certain devices](https://developers.google.com/ar/devices#google_play_devices) and you need to have the Google Play Services on your phone. If AR is not available, the new quests are disabled by default because you'd need to use a tape measure instead. (#3709, #879)

### New Quests

- *What’s the width of this cycleway here?* (#698)
- *What’s the width of this road here?*
- *What is the height limit here?*

### Quest enhancements

- Add "handlebar holders" as bicycle parking type option (#3061, #3724), by @sams96
- Tapping on road in street name sometimes did not work (#3797)
- Other small improvements (#3740, #3755, #3793, #3783), thanks @matkoniecz, @tiptoptom

## v40.0-beta1

### New Quests

- *How does this road cross the barrier here?* (#3372, #3515), by @matkoniecz
- *Can you pump gas yourself at this fuel station?* (#2822, #1827), by @naposm
- *Does this place have air conditioning?* (#3641, #3127), by @coolultra1
- *Are the opening hours signed for this place?*, asked at intervals for places that have previously been tagged as having no signed opening hours (#3682, #3130)

### Quest enhancements

- Allow answering whether there is a shoulder for each side individually (#3659)
- Allow answering whether the sidewalk is displayed separately on the map for each side individually
- When asking where a hydrant is located, show example pictures of pillar hydrants if it is a pillar hydrant (#3686, #3695, #3711), by @FloEdelmann
- Ask opening hours, place names and wheelchair accessibility for more amenities in the healthcare sector (#3710), by @FloEdelmann

### Fixes and improvements

- More accurate conflict detection
- Fix a few rare crash issues

## v39.0-beta1

Finally, a new release! You know how it is, the holidays... But this update should you give plenty to do, for now you can record the surface quality of roads!

### New Quests

- *Does this road have a shoulder?* (#2444, #3613)
- *What is the surface quality of this road here?* (#3617, #3257, #1630), special thanks to @Helium314 but also to @mcliquid, @NicoHood, @rhhsm, @mnalis, @1ec5 and many more
- *Where is this fire hydrant located?* (#3368), by @thefeiter

### Other enhancements

- You can now answer that a building has a house name *and* a number (#1983, #3582), by @matkoniecz
- Some improvements on the fire hydrant quest(s) (#3608), by @tiptoptom, (#3601)
- Some minor enhancements and clearer wordings (#3576, #3610, ...), thanks @matkoniecz
- Added "Lifesaver" achievement (#3616), by @FloEdelmann

### Fixes

- Fix rarely quest answers don't get uploaded automatically until next restart of app (#3494)

## v38.0-beta1

### New Quests

- *What type of bicycle barrier is this?* (#3361, #3487), by @matkoniecz
- *On which level is ... located?* (#1487, #3509)

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
- Other small UX enhancements (#3432)

## v37.0-beta1

### New Quests

- *What diameter is on this fire hydrant’s sign?* (#3342), by @thefeiter
- *What type of traffic calming is this?* (#2742, #3380), by @matkoniecz

### Quest Enhancements

- Remember and offer to apply the last answer for the cycleway and sidewalk quest (#2542)
- The top answers in the building type quest are sorted smarter (#1771, #3373), by @smichel17
- Ask again for the type of stile every 8 years (#3187, #3370), thanks @matkoniecz, @peternewman
- For stile type quest, add option to state that it is now something else than a stile (#3409, #3188, #3391), by @matkoniecz
- Add concrete lanes and concrete plates as answer options for surfaces (#2437, #3354), by @matkoniecz
- More minor enhancements (#3386, #3390, #3421, #2897, #3383), by @matkoniecz

### Other enhancements, fixes, etc.

- Edit history sidebar now appears immediately on tapping the undo button, last edit is at the bottom and pre-selected (#3349), by @smichel17
- Fix (speed limit, max height, max weight, ...) signs should have a yellow background in Sweden, Finland and Iceland (#3369, #3393)
- Fix "public transport" was displayed twice in board type quest (#3365), by @kmpoppe

## v36.0-beta2

Fix crash on entering quest selection screen

## v36.0-beta1

### Easier orientation

🖊️ The path you went today with the app is now marked on the map for better orientation! (#2209)

The location button (lower right corner) also has a new behavior:

- 🛰️ Tap it once to let the view follow your position on the map (icon turns orange)
- 🚴 Tap it again to let the view rotate with the direction you are going (icon turns into an arrow). Very helpful for cyclists! (#1040, #2191, #3335)

Furthermore, the only thing the compass button now does is to rotate the view back to north-up and it is only displayed if the map is not displayed north-up.

### New Quests

- *Is this picnic table covered (protected from rain)?* (#3311, #3142), by @FloEdelmann
- *Does ... offer halal products?* (#2990, #3331), by @SMUsamaShah

### Quest Enhancements

- Add answer option to state that there are actually steps, not a footway (#3288, #3334), by @FloEdelmann
- Building type: Add building bridge (skyway) as answer option (#2630, #3320), by @kmpoppe
- Self-service laundry: Add answer option to say self-service is optional (#3309)

### Other enhancements, fixes, maintenance

- More contrast on text (#3328), by @matkoniecz
- (#3327) by @FloEdelmann, (#3208) by @matkoniecz

## v35.0-beta1

### App maintenance

- Android 4.x support has been dropped. As a consequence, the app's download size is reduced by 40%! (#2031), thanks @Isira-Seneviratne
- Translator credits are now generated automatically (#3214)
- The uploading of changes will now never be cancelled by the Android system after the app has been sent to the background for a while (#3279)
- Various code maintenance and refactorings (#3274, #3223, #3259, #3273, #3296, #2894, ...), thanks @sumanabhi, @Isira-Seneviratne, @peternewman, @smichel17

### Quest Enhancements

- Clearer illustrations for the bridge structure quest (#2867), by @TurnrDev
- Other small enhancements (#3185, #3171)

## v34.0-beta1

### 🚌🚴 Quest presets

- Show quests that are quick and easy to solve (from a distance) first by default. This makes contributing more efficient and diverse while also easier to do as e.g. a passenger (#2944, ...)
- You can now save different quest presets for different situations, e.g. one preset when you only want a solve some important quests while on a bus, or on a bike tour etc. This was a much requested feature: (#1301, #1654, #1746, #1987, #2054, #2069, #2279, #2565, #2308, #3034, ...)
- Inform new users once about the possibility to change quest presets
- Add search/filter function for the quest presets settings (#3008)

### New Quests

- *Are there curbs where this way meets this road here? What kind?* (#398, #2999)

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
- Fix tapping compass rotates map back to North even when currently searching for GPS location (#3147, #3166), by @smichel17
- Fix crash when displaying opening hours whose range ended at 12 AM

## v33.0-beta1

### New Quests

- *What type of surveillance camera is this?* (#87, #299, #2856), by @Helium314, thanks @Binnette
- *Does this bus stop have a waste basket?* (#2760, #2898), by @FloEdelmann
- *What type of bollard is this?* (#2128, #2915), by @FloEdelmann, thanks @RubenKelevra

### Enhancements

- Remember more building level quest answers (#1772, #2925), by @FloEdelmann
- Added Bosnian translation by OSMcontributorBH
- Added Thai translation by Mishari, PPNplus, Kamthorn and more

## v32.0-beta1

The grant from the [Prototype Fund](https://prototypefund.de/en/project/streetcomplete/) ended in February, however, there is one last big mega-update I can delight you with before development on this app must necessarily wind down:
It may not look like a lot, but I basically replaced the whole architecture of the app (#2506, ...) to do this. Note that this required to drop most local data, such as unsynced edits, downloaded quests and statistics.
Special thanks to @FloEdelmann for helping with the refactor.

### 🔙️ Superpowered Undo

- You can now **undo any edit** that hasn't been uploaded yet: Quest answers, split ways, deleted places, hid quests, created notes and note comments (#1029, #2616)
- You can now look at your **edit history** and undo any recent edit in any order, not just the last one
- It is now shown what exactly is tagged for each edit in the edit history
- You can now undo deletions of places even after you have already uploaded it (#2441)

### ⛰️ Improved usability and offline mode

- Immediately unlock new quests based on the one you just solved, **also when offline**. This has been a much requested feature but was simply not possible with the previous architecture (#1369, #1510, #1550, #1826, #2254, #2438, #2473, #2474, #2676, #2743...)
- The above also works when splitting a way. The quests for the split segments will be shown immediately
- When you scan for quests manually, no cache will be used: You always get the most fresh data (#2554)
- When unable to fully upload pictures attached to a note due to a bad connection, the app will try again later (#2102)

### New Quests

- *Is this sport field lit?* (#2639, #2737), by @eginhard
- *What type of barrier is this?* (#2739, #2753), by @matkoniecz
- *What type of stile is this?* (#2749, #2766), by @matkoniecz
- *What type of shop is this?* (#2391, #2759), by @matkoniecz
- Italy only: *Which type of police station is this?* (#2456, #2675), by @naposm
- *Is this bus stop lit?* (#2383, #2846), by @TurnrDev

### Quest Enhancements

- Don't ask for building levels for industrial buildings (#2835), by @matkoniecz
- Improve referring to map features by name for some quests (#2840)
- Checking existence: Ask for some map features less frequently, by @matkoniecz
- Enable answering that the cycleway is displayed separately on the map (#2525, #2665)
- Some wording improvements (#2728), by @matkoniecz, (#2806, #2814) by @peternewman, (#2685)
- Enhance maxspeed collection for the United Kingdom (#2745, #2748, #2750, #2811), by @arrival-spring
- Periodically ask for the existence of bbq spots, ticket validators, emergency life rings and emergency phones (#2679), thanks @arrival-spring
- Also ask for the surface of service roads as long as they are not driveways (#2783)
- Don't remove the check_date tag if it is already tagged, instead, update it (#2861, #2883)

### Other Enhancements

- Added Arabic by Charbel Bechara and others
- Added Romanian by Georgian Iosef and others
- Improvements in the quest selection screen (#2243, #2847, #2828, #2843, #2887), by @FloEdelmann
- Slightly more performant compass
- When showing all notes, really show all except those where the user last commented (#2692)

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
- ...and other fixes and enhancements (#2521, #1821, #2628, #2691, #2715, #2870...)

## v32.0-alpha2

**Remember:** Sync any edits before upgrading to this version from v31 or earlier. If you have auto-sync on (the default), this does not concern you.

**Note:** Due to database upgrades in this release, previously hidden quests and the quest types you have chosen to disable will be reset, you can enable/disable quests in settings again. Your quest statistics will also be briefly wrong until they're re-calculated again.

### New Quests

- *Is this bus stop lit?* (#2383, #2846), by @TurnrDev

### Other Enhancements

- Added Arabic by Charbel Bechara and others
- Added Romanian by Georgian and others
- Improvements in the quest selection screen (#2243, #2847, #2828, #2843), by @FloEdelmann
- Don't ask for building levels for industrial buildings (#2835), by @matkoniecz
- Improve referring to map features by name for some quests (#2840)
- Checking existence: Ask for some map features less frequently, by @matkoniecz

### Fixes

- Fixes on specifying shop type quest, by @peternewman, @matkoniecz
- Possibly fix an endless download-loop in certain unknown circumstances (#2860)
- Fix downloading suspected oneways for locales that use a "," as decimal mark
- Fix opening a location in another app for locales that use a "," as decimal mark
- Fix quests that are very close to notes could in rare cases not be opened (#2853)
- Fix housenumber quest does mostly not appear (#2863)

## v32.0-alpha1

The grant from the [Prototype Fund](https://prototypefund.de/en/project/streetcomplete/) ended in February, however, there is one last big mega-update I can delight you with before development on this app must necessarily wind down:
It may not look like a lot, but I basically replaced the whole architecture of the app (#2506, ...) to do this. Special thanks to @FloEdelmann for helping with the refactor.

### 🔙️ Superpowered Undo

- You can now **undo any edit** that hasn't been uploaded yet: Quest answers, split ways, deleted places, hid quests, created notes and note comments (#1029, #2616)
- You can now look at your **edit history** and undo any recent edit in any order, not just the last one
- It is now shown what exactly is tagged for each edit in the edit history
- You can now undo deletions of places even after you have already uploaded it (#2441)

### ⛰️ Improved usability and offline mode

- Immediately unlock new quests based on the one you just solved, **also when offline**. This has been a much requested feature but was simply not possible with the previous architecture (#1369, #1510, #1550, #1826, #2254, #2438, #2473, #2474, #2676, #2743...)
- The above also works when splitting a way. The quests for the split segments will be shown immediately
- When you scan for quests manually, no cache will be used: You always get the most fresh data (#2554)
- When unable to fully upload pictures attached to a note due to a bad connection, the app will try again later (#2102)

### New Quests

- *Is this sport field lit?* (#2639, #2737), by @eginhard
- *What type of barrier is this?* (#2739, #2753), by @matkoniecz
- *What type of stile is this?* (#2749, #2766), by @matkoniecz
- *What type of shop is this?* (#2391, #2759), by @matkoniecz
- Italy only: *Which type of police station is this?* (#2456, #2675), by @naposm

### Other Enhancements

- Enable answering that the cycleway is displayed separately on the map (#2525, #2665)
- Some wording improvements (#2728), by @matkoniecz, (#2806, #2814) by @peternewman, (#2685)
- Enhance maxspeed collection for the United Kingdom (#2745, #2748, #2750, #2811), by @arrival-spring
- Periodically ask for the existence of bbq spots, ticket validators, emergency life rings and emergency phones (#2679), thanks @arrival-spring
- Also ask for the surface of service roads as long as they are not driveways (#2783)
- Slightly more performant compass

### Fixes

- On tablets, correctly restore the position when reentering the app (#2659, #2311), thanks @matteblair
- Sometimes the link collection wouldn't show (#2801, #2809), by @tapetis
- The keyboard would not appear in the opening hours times dialog (#2799, #2820), by @tapetis
- The flags in the statistics view were sometimes wrongly stretched (#2819, #2831), by @tapetis
- The time picker for the postbox collection times now follows the system settings (#2807)
- Fix crash on trying to display opening hours that range to 24:00 (#2830)
- ...and other fixes and enhancements (#2521, #1821, #2628, #2691, #2715, #2728...)
- Remove checking existence of grit bins, as many are removed for summer and not marked as seasonal :( (#2726), by @matkoniecz

## v31.0-beta1

### 🤝 Team mode!

(#2189, #2557) by @FloEdelmann

Lockdown fatigue? You could go on a walk together with your roommate or partner – with the team mode, you can evenly divide up all quests among yourselves and map together!

It also works for groups of up to twelve people, but maybe wait with that until the pandemic is over 😷. Happy mapping!

### New quests

- 👑 *What is the royal cypher on this postbox?*, only in the UK (#2563, #2583), by @arrival-spring

### Enhanced Quests

- Building type: Add fire station as selection (#2476, #2579)
- Recycling containers: Add beverage cartons as choice (#2502, #2580)
- Check existence: Also ask for public bookcases
- Roof shape: Don't ask for high buildings (#2609)
- Orchard produces: for the Canaries, sort choices by prevalence (#2596), by @erik55
- For various quests, also display the name, brand or operator of the point of interest (#2593, #2594), by @FloEdelmann

### Fixes

- Fall back to English when displaying untranslated feature names (#2608)

## v30.0-beta1

Did you already [take the survey](https://osmf.limequery.org/281662) of the OpenStreetMap Foundation? It's open only till Feb 14.

### New Quests

- *Is the water potable here?* (#549, #2509), by @FloEdelmann
- *Do you have to pay to park a bike here?* (#2507, #2517), by @FloEdelmann
- *Is it restricted who may park a bike here?* (#2496, #2517), by @FloEdelmann

### Enhanced Quests

- Ask for the surface of sport tracks (#2468, #2310), by @FloEdelmann
- More opening hours in non-standard syntax are understood and displayed on resurvey, thanks @simonpoole
- Check existence: Display brand name if object has no name (#2512)
- Kosher: Do not ask for veg-only places (#2513), by @FloEdelmann
- Sidewalks: Ask for residential roads even if they are not lit (#2519, #2531), by @riQQ
- Reycling materials: Do not ask for private containers (#2515)
- Road name: Do not ask if name:left or name:right is defined already (#2526)
- A few clarifications in wording (#2467, #2431)

### Fixes & More

- Added Bulgarian translation by Plamen and Kalin
- Update geometry of displayed elements after another download (#2503)
- Some code modernizations (#2495, #2501, #2504, #2511) by @FloEdelmann

## v29.0-beta1

### 🗺️ Map available offline!

In the area quests are downloaded, the background map is now also downloaded into the cache.

So, when you are offline later, you can continue to use this app in that area without any problem because the background is also available (#122, #2428).

To download not at your current GPS location but somewhere else, just pan and zoom to that location and tap "search for quests here" in the menu.

### New Quests

- *What surface does this sport field have?* (#1170, #2377), by @matkoniecz
- *Does ... offer kosher products?* (#639, #2244), disabled by default, by @matkoniecz

### Enhanced Quests

- Construction quest: Make it possible to select date at which construction will be finished (#1341, #2402), by @matkoniecz
- Cycleways: Provide hint what to answer if there is no cycleway at all (#2285, #2405), by @matkoniecz
- Housenumbers: Do not ask in France (#2427), by @matkoniecz

### Fixes

- Answer that a thing does not exist anymore, sometimes wasn't uploaded correctly (#2419)
- Sometimes during download, some quests would appear and then vanish again (#2430)

## v28.0-beta2

### Fixes

- Fix crash on Android 11 (#2355)
- Fix the below bridge height quest was shown for too many roads (#2370)
- Fix problem on deletion of a node that is a vertex of a way (#2350)
- More crash fixes

### Enhancements

- Ask for roof shape even if roof levels is 0 - but only in countries where flat roofs are not the norm (#1929)
- Kerb quests: Tag barrier=kerb if it is missing (#2348)
- Do not ask for the bench backrest if the bench type is already recorded (#2365)
- Other small enhancements (#1832, #2371, #2368)

## v28.0-beta1

This update is devoted to checking whether things still exist the way they are mapped since keeping data up-to-date is at least as important as contributing new data.

### New Quests

- *This shop has been vacant. What's here now?*
- *Is this still here?* (#2074), asked periodically for benches, telephones, post boxes etc.

### Enhanced Quests

- Added "doesn't exist" answer option to many quests. If it was a shop of some kind, you can directly specify what is there now.
- Tracktypes: Better wording and pictures (#2294)
- Parking type: Added lane and off-street parking (#2346)

### Other Enhancements and Fixes

- Serbian translation by Mario, Nemanja Bračko
- Android 11: Enable smooth keyboard appear animation (#2133)
- even slightly faster download
- Other small enhancements and fixes (#2349, #2351...)

## v27.0-beta3

- A few adjustments on the new lanes quest. For example, don't ask for the number of lanes for roads without lane markings (#2305, #2299 ...)
- Building type: fix wrong name for a silo (#2303)
- Max height below bridges: fix splitting the way (#2304)
- A few wording improvements (#2160, #2315, ...)
- Surface: Add "rock" as an answer option (#2298)
- Cycleway resurvey: Interpret a road with oneway:bicycle=no to have no cycleway unless it is tagged explicitly (#2310)

## v27.0-beta2

Just a few fixes for the newly introduced quest types (#2284, #2286, #2287, #2288, #2289)

## v27.0-beta1

### New Quests

- *Is there tactile paving on this curb here?* (#1305, #2183), by @matkoniecz
- *What is the height of this curb?* (#1305, #2183), by @matkoniecz
- *How many lanes for cars does this road have?* (#856, #2269)

### Enhanced Quests

- Ask for the max height also below bridges (#1882, #2234)
- A few minor enhancements (#2280, #2276, #2221)

## v26.0-beta3

- Fixed problem with build system - beta2 didn't start at all (#2224)
- Fix leaf type quest showing up for too many elements (#2226)

## v26.0-beta2

- Added Croatian translation by Matija Nalis
- All forms for quests except image selection forms now automatically expand to full height
- Fixed two crashes (#2220, #2219)

## v26.0-beta1

### ⚡ Wait for the download no more!

Reworked it completely. Quest download is now substantially faster and the pins for all quest types appear immediately at the same time! (#1874, #1901)

![97378680-34e45280-18c3-11eb-9061-78dfa68635af](https://user-images.githubusercontent.com/4661658/97779862-4064a180-1b81-11eb-8536-a87daaa410bb.png)

### New Quests

- *How many cars can be charged at this charging station at the same time?* (#900)

### Enhanced Quests

- Road surfaces: Improve interface, ask for reason when a generic surface is selected (#2078), by @matkoniecz
- Building types: Add answer options for silos (#2180), historic, abandoned buildings and ruins (#2177, #2214), by @RiffLord
- Improve a few wordings (#2202, #2204)

### Other Enhancements and Fixes

- You cannot accidentally open the copyright statements for the map (links at the bottom) anymore (#2199)

## v25.0-beta1

### New Quests

- *What is the reference number of this bus stop?* (#2126)
- *Do these steps have a ramp? What kind?* (#2036)
- *Does this pedestrian crossing have an island?* (#1961, #2030), by @kmpoppe
- *What's the name of the bank for this ATM?* (#203)
- *Who is the operator of this charging station?* (#911)
- *Who accepts donations for this clothing bin?* (#570)

### Enhanced Quests

- House number quest: Make it faster to collect house numbers in a row (#2131)
- Recycling materials quest: In the Czech Republic, offer "any glass" option instead of "glass bottles" (#2123)
- Improve a few wordings (#2047, #2096)
- Handrails: Don't ask for places where the information has already been supplied in another way (#2162)

### Other Enhancements and Fixes

- new achievement graphics by Judith Gastell ([melusine](https://www.artstation.com/melusine))
- Map style update: Amongst other things, all housenumbers are now displayed again on the map, private roads are displayed as such and 3D buildings can be displayed more detailed

## v24.0-beta1

Good News! StreetComplete received a grant from the [German Federal Ministry of Education and Research](https://www.bmbf.de/) within the frame of the [Prototype Fund](https://prototypefund.de/en/project/streetcomplete/), spanning over a period of 6 months! For the next few months, my focus is on adding new quest types, starting with this update!

### New Quests

- *Is this a one-way street? In which direction?* (#1982, #2122)
- *What features for the blind do these traffic lights have, if any?* (#1330)
- *Which direction leads upwards for these steps?* (#1817)
- *How may steps are here?* (#875)

### Enhanced Quests

- Max weight: A range of different max weight signs are now supported (#1880, #2121)
- Postbox collection times: Can now select more times for the same day (range) (#2108, #2109), by @kmpoppe

### Other Enhancements and Fixes

- hide GPS button only when the screen follows current position (#1996, #2101, #2110), by @smichel17 and me
- other small fixes (#2103)

## v23.0-beta1

### 🚧 Map maintenance

Quest types whose answer is likely to change over the years will be asked again in reasonable intervals. So now you can help not only to complete the map, but also to keep it up-to-date, which is at least as important.

Most prominently, you'll be asked if opening hours are still the same as before and be offered to correct them if they changed, the same with cycle tracks and lanes on roads.

This feature has been sponsored by the OpenStreetMap Foundation (OSMF) as part of their new [microgrants](https://blog.openstreetmap.org/2020/07/01/osmf-microgrants-program-congratulations-to-selected-projects/) program. You can support the OSMF and other initiatives like these and have a say by [becoming a member](https://join.osmfoundation.org/), if you like.

### New Quests

- *Is there a summit register at this peak?* (#561, #2065) by @matkoniecz
- *Is this defibrillator (AED) inside a building?* (#2068) by @matkoniecz
- *Does this bus stop have a bench?* (#1079, #2073) by @matkoniecz

### Enhanced Quests

- Motorcycle and bicycle parking: Do not accept an input of "0" (#516)
- Also ask for the opening hours of defilbrilators (#1756, #2066), by @matkoniecz
- Accepts cash quest: Ask for more places (#2019), by @kmpoppe
- For opening hours, wheelchair access and accepts cash quest, always display the type of place alongside its name (#2012), by @Helium314
- Recycling container materials: Don't ask for containers close to other containers

### Other Enhancements and Fixes

- Explain that you have to download manually if auto-sync is off (#1989, #2009, #2070), by @matkoniecz
- Show link how to contribute translations in settings (#2079)
- Improve visibility of highlighted elements on the map (#1769)
- Fix a few rare crashes
- Fix housenumber quest sometimes appearing even if there is a housenumber node inside (#2064)

## v23.0-alpha1

### 🚧 Map maintenance

Quest types whose answer is likely to change over the years will be asked again in reasonable intervals. So now you can help not only to complete the map, but also to keep it up-to-date, which is at least as important.

Most prominently, you'll be asked if opening hours are still the same as before and be offered to correct them if they changed, the same with cycle tracks and -lanes on roads.

This feature has been sponsored by the OpenStreetMap Foundation (OSMF) as part of their new [microgrants](https://blog.openstreetmap.org/2020/07/01/osmf-microgrants-program-congratulations-to-selected-projects/) program. You can support the OSMF and other initiatives like these and have a say by [becoming a member](https://join.osmfoundation.org/), if you like.

### Other Enhancements and Fixes

- Accepts cash quest: Ask for more places (#2019), by @kmpoppe
- For opening hours, wheelchair access and accepts cash quest, always display the type of place alongside its name (#2012), by @Helium314
- Recycling container materials: Don't ask for containers close to other containers
- Fix a few rare crashes

## v22.0-beta1

### Enhancements

- When you split a way, all previous quests reappear on the split segments immediately after upload (#1950, #1951)
- Better fallback for showing of (street) names in user language or script
- Always display the house number above the quest question if it is available (#1971)
- For longer displays, show more of the quest forms initially (#1977)

### New and enhanced Quests

- Board Type: *What is the topic of this information board?* (#1226, #1920), by @matkoniecz
- Detailed Road Surface: *What specific surface does the road ... have here?* (#279, #1915), by @matkoniecz
- Street Names: Allow to specify the romanized/international name of a street name in another script, such as Greek (#1765)
- Sidewalks: Add answer that the sidewalk is already displayed separately on the map (#1958, #1925), by @matkoniecz
- Handrails: Do not ask for private stairways (#1976)
- Oneways: Better fitting description text (#1975)
- Building Type: Correctly refer to "row house**s**" and not "row house" (#1973)

### Fixes

- Fix a rare crash on device connectivity change
- Fix a rare problem of photos being associated to the wrong notes (#1981)

## v21.0-beta1

### 🗺️ New Map Provider

Big thanks to [JawgMaps](https://www.jawg.io), a provider of online custom maps, geocoding, routing and elevation data. They are helping OpenStreetMap by providing their map service to StreetComplete for free!
Their maps are also quite excellent: You can look forward to **daily map updates** and **map loading times cut in half**, as their map data is very lightweight.

But let's not forget to thank @Akasch for hosting the previous solution with zero budget over the last years and maintaining it in his free time. Without his endeavor, there likely wouldn't have been a map at all in this time.

### Enhancements

- The app can now be moved to SD card in the Android settings (#1787), thanks @Mortein and @Atrate
- Labels for countries and big cities stand out more, so they are better distinguishable from smaller places
- Accessibility: The text on the map now scales up when zooming and when increasing the font size in the Android settings (#1877), by @matkoniecz
- Accessibility: Increase contrast for orange text (#1899), by @matkoniecz
- Recycling type: Added cooking and motor oil options (#1830)
- Building type: Don't ask for (theme park) attractions (#1891)
- Added some achievement links

## v20.0-beta2

### New Quests

- What street does the (house) number ... belong to? (#1782, #213), by @dbdean, @matkoniecz and me

### Fixes and Enhancements

- bubbles in ball pit now display with shadow (#1840)
- fix achievement icons would sometimes vanish (and other oddities) (#1834)
- text is selectable now in changelog, credits and privacy statement (#1833)
- move pointer pin below other map buttons (#1838)
- buttons in bottom sheet button bar are a bit slimmer now (#1839)
- smaller animation time for quest counter in statistics (#1832)

## v20.0-beta1

### 📈 New Statistics

- 🌍 See your solved quests grouped by country
- See how you rank amongst other StreetComplete users
- See how many days you contributed and the sum of your achievement levels
- Access the mapping portal for each country you contributed to, with useful infos about local mapping efforts and for connecting with the local community

### Enhancements

- A pointer pin at the edge of the screen replaces the GPS button and points to your current location
- 📱 The app is now optimized to look great on tablets and phablets as well! (#1794)
- Text in note discussions is now selectable
- Add proper shadows for all the speech bubble forms

### Fixes

- on pressing the hardware menu button, don't open the menu twice (#1806)
- bubbles in the statistics view shouldn't grow too large anymore (#1818)
- Previous quest selection was still displayed when creating a note (#1820)
- On splitting a way, require the user to zoom in far enough to use the scissors
- Fix link collection sometimes appearing empty
- Fix close-button was not shown when bottom sheet was pulled up completely
- More minor / technical fixes

## v19.0-alpha2

I am sorry about this, by accident the alpha test version of v19 ended up on F-Droid. If you do not like to be an alpha tester, you should reinstall v18.
Usually, only stable versions are published on F-Droid. If you like to be on the cutting edge in the future whenever I do not botch this up, you can watch releases for [StreetComplete](https://github.com/westnordost/StreetComplete/releases/)

### Fixes

- **fix crash on startup if user has no OSM profile image**
- **fix crash when displaying the link to osmweekly**
- **fix crash when showing main screen while being offline**
- fix keyboard would hide input form when the form was not visible compeletely before
- fix rare crash that occurs when opening a quest while downloading
- for auto-sync on: do not try and thus fail to upload when offline
- fix undo button was not enabled again after one undo
- fix star count not being immediately updated after login
- fix crash when opening vegetarian quest on Albanian locale
- fix incorrect display of how many quests were needed to unlock an achievement
- if no more quests are available when trying to download, show message
- correct text color in tab bar for profile screen on tablets
- correct layout for achievements and link screen
- screen should follow current location by default
- fix clipping problem during the star counter animation
- don't animate star counter when simply returning to the main screen

### More

- update translations
- on stats screen, make the difference in bubble size smaller

## v19.0-alpha1

Pre-beta release of v19.0. So fresh, I didn't even write a changelog yet!

## v18.0-beta1

### New Quests

- **Does ... accept cash payment?** (#1743, #1573), by @quite. Enabled only in Sweden because it cannot be taken for granted that cash is accepted there.
- **What type of tourist information is this?** (#1722, #1115), by @Dosenpfand

### Enhanced Quests

- Building types: Provide a description of what distinguishes a garage from garages (#1720), by @smichel17
- Recycling containers: Add "it's a waste container" as answer option (#1745)
- Car wash type: Allow selecting self-service + staff cleans car (#1738)

### More

- Clearer wording for cancelling current download, especially in French (#1741)
- Do not show this dialog for new users (#1751)

## v17.0-beta3

- Fixed several crashes
- Fixed buttons not working if animations are disabled (#1706)

## v17.0-beta2

- Fixed a crash on startup, thanks @RubenKelevra, @Atrate (#1704)
- Fixed a crash on exiting the app

## v17.0-beta1

This release includes a major update of the map rendering library (in order to satisfy new Google Play guidelines). I took this as an oppurtunity to re-do most of the things that concern the map view (#1279, #1606) and thus solve some long-standing issues with it. If any new have been introduced, please report them!

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

## v16.0-beta1

### New Features
* 📰 You now see this changelog in the app after each app update and in the about menu
* ❤️ You can now sponsor the development of this app on [GitHub Sponsors](https://github.com/sponsors/westnordost), [liberapay](https://liberapay.com/westnordost) or [Patreon](https://www.patreon.com/westnordost). Thanks to anyone considering it!

### New Quests
* What is the reference number of this postbox? (#1556, #1628)
* What can be left here for recycling? (#223, #1627)
* Can only glass bottles and jars be recycled here, or any type of glass?

### Enhanced Quests
* ask about internet access for more types of accomodations (#1620), thanks @wvanderp
* ask opening hours, names and wheelchair accessibility for more types of places (#1621), thanks @wvanderp
* do not ask for handrails of escalators by @matkoniecz
* add hint how to answer the cycleway and sidewalk question (#1671) by @matkoniecz
* clearer wording for parking access quest (#1685)
* ask opening hours also for shops with no name (#1647)
* let the user specify the type of crossing if before it was simply specified as an island (#1637)

### General Enhancements
* better visibility of some icons in dark mode (#1663, #1664, #1665, #1668, #1659, #1658), thanks @matkoniecz
* fix dark mode for right-to-left layouts (#1634)
* fix strange behavior in auto download of quests
* try again later if the upload of attached photos to a note failed (#1575)
* indicate the user's locale for each changeset (#1674)

## v15.0-beta1

### New Translation
Norwegian Bokmål by Mats Randgaard and Simen Heggestøyl

### New Quests
- **Does it cost a fee to enter ...?** (#873,  #1600) by @matkoniecz
- **Is this laundry a self service laundry?** (#1385, #1608) by @matkoniecz
- **Do these steps have a handrail?** (#1390, #1616)  by @xuiqzy
- **What is the weight limit here?** (#1622, #1467, #361) by @matkoniecz

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
- Disable 3D buildings when creating notes (#1607) by @typebrook
- Less ugly texts in the create note form

### Technical
* Migrate parts of the code from Java to Kotlin, lots of refactoring (-3000 lines of code!)
* Distribution on Google Play is now about less than half the size (app bundles)

A portion of this update is credit to Mateusz Konieczny, powered by a [NGI Zero Discovery grant](https://www.openstreetmap.org/user/Mateusz%20Konieczny/diary/368849).

## v14.0-beta1

### Major Features
- **🚀 quest download now more than 4 times as fast as before!** (#1479, #1514, #1516)
- **✂️ Splitting ways is now possible!** (#1329)

![splitway](https://user-images.githubusercontent.com/4661658/63652924-99a79880-c766-11e9-9f8b-53fbdc75e590.gif)

### New Quests
- **Do the trees here have needles or leaves?** (#366,  #1465) by @matkoniecz
- **What's the surface of the cycleway here?** - asked for segregated paved foot- and cycleways (#1493) by @matkoniecz
- **What's the surface of the footway here?** - asked for segregated paved foot- and cycleways (#1544, #1489) by @matkoniecz
- **Is the restroom at ... wheelchair accessible?** - asked for places that have toilets (#1391, #1547) by @matkoniecz

### Enhanced Quests
- **roads prohibited for pedestrians**: Switched icon - old one was leading to misunderstandings (#1342)
- **lit roads, max speed**: Also ask for link roads (#1520) by @matkoniecz
- **bicycle parking type**: Do not ask for private bicycle parkings (#1521) by @matkoniecz
- **place name**: Also ask for the name of `tourism=chalet`s (#1519) by @matkoniecz
- **opening hours, place name, wheelchair access**: Ask for more `office=*` and `craft=*` places (#1522, #1526) by @matkoniecz
- **road name**: Do not ask for names of private roads (#1529, 1533) by @matkoniecz
- **housenumber**: tag `nohousenumber=yes` instead of `noaddress=yes` (#1553)
- **all building-related**: Do not ask for ruins (#1543, #1541) by @matkoniecz

### Enhancements
- Show confirmation dialog when removing authorization for the app in the settings (#1534, #1535) by @typebrook

### Fixes
- Show only labels from OpenStreetMap on the background map (#1527)

A portion of this update is credit to Mateusz Konieczny, powered by a [NGI Zero Discovery grant](https://www.openstreetmap.org/user/Mateusz%20Konieczny/diary/368849).

## v13.0-beta1

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

This update is entirely credit to Mateusz Konieczny, powered by a [NGI Zero Discovery grant](https://www.openstreetmap.org/user/Mateusz%20Konieczny/diary/368849).

## v12.0-beta1

### New Quest
* **What is the name of this place?** Asked for unnamed shops, amenities etc. (#309, #1376)

### Minor fixes and Enhancements
* fix color of close button in night mode
* show the placeholder avatar if an avatar cannot be decoded (crash fix)
* don't ask for location on startup when user last denied it (fixes #1382)

## v11.0-beta1

### Dark Theme (night mode)
![dark](https://user-images.githubusercontent.com/4661658/54872879-a5fc8380-4dcb-11e9-8c4c-4be5baa8b9e9.png)

### Fixes
* fix a few sources for crashes and other small fixes (#1307, ...)

### Enhancements
* slightly better wordings for a few quests (#1349, #1353)
* quest upload now treats situations where the geometry of the element in question changed significantly as an unsolvable conflict

## v10.0-beta3

* fix Lit-Quest tagging "yes" incorrectly as "answer" (#1328)
* fix possible nullpointer
* update strings

## v10.0-beta2

* update strings
* fix crash when sending app to background while quests are downloading
* fix crash in building type, road and path surface quests

## v10.0-beta1

### New language
* Korean by Dongha Hwang

### Improvements
* Quests previously hidden can now be made visible again through an option in the settings (#302, #1302) by @ENT8R
* All "other answer..." options that lead to another dialog, the text now ends with a "..." (#1296, #1274)
* clarify wording in maxspeed quest (use "built-up area" instead of "urban") (#1314)
* the choices in the crops quest are now sorted with new UN FAO data (#1319) by @rugk
* Show the tactile paving bus stop quests for ways as well (#1309) by @MrKrisKrisu
* when showing a quest for an element (e.g. a shop) that is not at street level, show additionally on which floor it is located (#1270)

### Fixes
* fix missing pictures in building type and surface selection quests in Android 4.4 and below (#1317)
* fix sometimes quests were still being generated even though there was a note for that element  (#1089)
* exclude roads from the oneway quest where its direction can not be determined reliably (#1320)

### Technical
* Migrate parts of the code from Java to Kotlin, lots of refactoring, use Android Kotlin extensions
* Migrate from appcompat to androidx, migrate from `evernote.android-job` to `work.work-runtime` library
* Use R8 instead of ProGuard

For the average user, this means that this version will be more unstable than v9 for now.

## v9.0-beta1

### New Quests
- **Track type**: What is the surface firmness of this track? (#959) by @ENT8R
- **Underground building**:  Is this building completely underground? (#912)
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

## v8.0-beta1

### UX Enhancements
- **Redesign quest forms and dialogs to a new look** (this is quite huge) (#1158)
- when leaving a note, it is done in the bottom sheet instead of in a dialog now so that you can still see the quest geometry
- solve performance problems when downloading quests (especially housenumbers and building types)
- only play sound effects when system setting for that is enabled (fixes #1187) and in "media" instead of "notification" stream (#1167)
- In notes, shortcut the link to the element to https://osm.org/way/... for better readability of notes left by SC users (#1178)
- tapping on the quest title now toggles expanding the quest to full size and back
- it is now again possible to attach photos to notes

### Quests
- **new** Railway crossing: *How is this railway crossing protected?*
- Redesign note discussion quest (new: avatars displayed also) to make it look like a chat
- Surface and building type quest: When tapping to expand a category, the view scrolls down to show the expanded items

### Other & Bugfixes
- StreetComplete is now ready for GDPR-related changes made to the OSM api
- solve quest upload problem for quests on relations when phone locale is set to a Turkic language

## v7.0-beta1

### New Quests:
- Playground access: *Is this playground publicly accessible?* (#1134) by @matkoniecz
- Max height: *What is the height limit of this tunnel/parking entrance/height restrictor?* (#960, #399, #421, #447) by @ENT8R
- Cycleway segregation: *"How are the footway and cycleway laid out here?"* (#527, #1135) by @matkoniecz

### Enhanced Quests:
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

### Small Enhancements and Fixes:
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

## v6.0-beta2

* updated translations
* show direction of traffic flow arrows in one-way quest
* **fix bug which led to that roads marked as one-ways through the one-way quest may be erroneously marked as one-ways into the wrong direction!**

## v6.0-beta1

### New Quests:
- Add building type: *"What was this building constructed as?"* (#25, #774, #1092) thanks @ENT8R
- Add path surface: *"What surface does this path have here?"* (#133)
- Add one-way road: *"Is this a one-way street?"* (#370)

### Enhanced Quests:
- House numbers: Can now answer that the building has no house number (#351)
- Parking type, fee and access: Include relations in search for parking areas (#1033)
- Add road surface: Show different surfaces initially for different types of roads, e.g. for tracks more unpaved surfaces
- Add cycleway: Now understands `bicycle=use_sidepath` and will not show the cycleway quest in that case (#1084, #1087) by @ENT8R
- Wheelchair access: Added internet cafes, theatres and casinos (#1086) by @matkoniecz
- Bicycle parking: Clearer wording (#1038)

### Small Enhancements and Fixes:
- split up English into US-English and UK-English
- Update privacy statement
- Add confirmation dialogue on resetting quest enablement to default (#1095)
- Make background of compass needle not flicker any more (#1057)
- Cursor does not jump to the start of the input any more when pressing [abc]-button in house number quest (#1093)
- Correct wording in mark completed construction quest

## v5.0-beta2

- updated strings
- *fix wrong graphics showing up in cycleway and maxspeed quest (#1007)*
- added "Is this road completed?" and "Is this building completed?" quest (#920) by @matkoniecz
- Do not show "odd housenumber" warning when specifying several housenumbers comma-separated (#1015)
- Mention StreetComplete in created notes (#1012)
- add icons for bus stop shelter and bicycle parking capacity quest (#997, #999, #1004) thanks @rugk

## v5.0-beta1

### New Features
- some wording changes for clarity
- add Slovenian translations
- Performance Improvement: Use fast country detection library https://github.com/westnordost/countryboundaries
- Now uses HTTPS for all connections (#63)

### Fixes:
- Fix crash on rotating the phone while parking fee quest was open
- Fix duplicate opening of quests (i.e. on rotate, or when a new quest is unlocked) (#973)

### New Quests:
- Bicycle parking type: *"What is the type of this bicycle parking?"* (#923) by @matkoniecz
- Bus stop names: *"What is the name of this bus stop?*" (#986 / #551) by @PanierAvide
- Postbox collection times: *"What are the collection times of this postbox?"* (#85)

### Enhanced Quests:
- Housenumbers: Add clarification that it is OK to tag ranges and comma-separated housenumbers (#939) by @ENT8R
- Cycleways:
  - Surveyors can now tag dashed cycle lanes (aka advisory cycle lanes) (#888)
  - Surveyors in Netherlands and Belgium can now tag suggestion cycle lanes (Fietssuggestiestrooken) (#888)
  - Improved the illustrations for the cycleway selection (#888)

## v4.0-beta2

- fix critical bug that led to the undo feature not working properly (#849)
- fix crash on answering "street has no name" (#845)
- add custom icon for toilet fee (#843)

## v4.0-beta1

### Highlights:
- Add a note anywhere, like on openstreetmap.org but you can also attach a photo (#732) by @ENT8R
- New simple map style that shows only what is important for surveyors (#183)

### More Features:
- Added unsynced changes counter, click to upload changes manually (#75)
- Immediately open next quest for same element after answering (#164)
- Unglue map while quest details or note input form is shown (#795)

### New Quests:
- Internet connection: *"What kind of internet connection does ... offer?"* (#784) by @ENT8R
- Religion of place of worship: *"What religion is practised at ...?"* (#777)
- Religion of shrine: *"What religion is represented at this shrine?"* (#799)
- Parking fee: *"Does it cost a fee to park here?"* (#825) by @ENT8R
- Parking access: *"Is it restricted who may park here?"* (#839) by @ENT8R
- Bench backrest: *"Does this bench have a backrest?"* (#830) by @Map3428
- Bridge structure: *"What is the structure of this bridge?"* (#709) thanks @Southernswampfrog
- Wheelchair toilet access: *"Are these toilets wheelchair accessible?"*

### Enhanced Quests

#### Cycleway Quest:
- Clearer wording of question (#768)
- Do not ask for a cycleway when a separately mapped sidewalk exists (#718)
- [Show only in a limited set of countries](https://ent8r.github.io/blacklistr/?java=bikeway/AddCycleway.java) (#749)

#### Tactile Pavings Quests:
- Clearer description (#762)
- [Show only in a limited set of countries](https://ent8r.github.io/blacklistr/?java=tactile_paving/AddTactilePavingCrosswalk.java) (#750)

#### Max Speed quest:
- Clearer English wording when asking if the road is in or out of town (#757)
- Still disabled for the US (#813)

#### Street surface quest:
- Improve street surface quest UI (#76)
- Add "metal" as answer option (#568)

#### Road name quest:
- Clearer UI flow when answering the road has no name (#756)
- Clearer OSM changeset comment message (#158)

#### Bus stop shelter quest:
- Only show for actual bus and tram stops, clearer wording (#806)
