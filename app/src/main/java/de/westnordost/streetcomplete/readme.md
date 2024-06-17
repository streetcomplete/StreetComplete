
This is a short and high-level overview what to find in which package

- `data` - Everything concerning data management, persistence, upload, download
  - `download` - Download service, automatic download behavior (strategy), and persist which tiles have been downloaded already
  - `edithistory` - Control of user's edit history, undoing etc.
  - `elementfilter` - Parser and data model for the "Element filter expression" (Overpass-Wizard-like syntax used to filter OSM elements)
  - `location` - Persistence of user's location
  - `logs` - Management and persistence of log messages
  - `maptiles` - Downloader and cache for (vector) map tiles of the background map
  - `messages` - Control of messages shown to the user
  - `meta` - Parsing and data model for country- or locale-specific metadata
  - `osm` - Management, download, persistence and upload of OSM map data: OSM map data itself, its geometry, quests generated from that and edits made on that data
  - `osmnotes` - Management, download, persistence and upload of OSM notes data: OSM notes data itself, quests generated from that, edits made on that data, attaching and upload of photos
  - `osmtracks` - User GPS traces and upload to OSM
  - `overlays` - Management and persistence of selected overlay
  - `quest` - Management of quests, automatic upload behavior
  - `sync` - Android intent service and notification for upload / download
  - `upload` - Upload service
  - `urlconfig` - Configuration of quest presets via a short URL
  - `user` - Management and persistence of user login, user data, statistics, achievements and unlocked links
  - `visiblequests` - Management and persistence of quest type order, enablement and quest presets

- `osm` - Parsers, data model and other stuff dealing with OpenStreetMap tagging logic. Some tagging logic is (still) in the `quests` package

- `overlays` - Overlay definitions, forms and associated logic

- `quests` - Quest type definitions, quest forms and associated logic

- `screens` - Everything for the different screens in the app: activities, fragments, adapters, dialogs, custom views, drawables, ...
  - `about` - About screen
  - `main` - Main screen: The map, the controls (buttons etc.), edit history sidebar, notifications
  - `measure` - AR measuring screen
  - `settings` - Settings screen, quest selection and quest preset selection screens
  - `tutorial` - Tutorial screen
  - `user` - User screen: Login, profile, statistics, link collection, achievements, ...

- `util` - General purpose utility and convenience functions, classes etc. Specifically view-related stuff is usually in the `view` package
  - `ktx` - Extension functions (usually to classes outside this project)
  - `location` - Utilities to request location (permission)
  - `math` - Math utilities, chiefly geodesy (math assuming a spherical Earth)

- `view` - Generic views and related classes used in various places: dialogs, adapters, custom views, view controllers, drawables, ...
