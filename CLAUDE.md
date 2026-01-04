# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

StreetComplete is a Kotlin Multiplatform Android/iOS app for contributing to OpenStreetMap. Users answer simple questions ("quests") about nearby places, which directly edit OSM data. The app is designed to be usable without OSM knowledge.

**Active Migration**: The codebase is undergoing migration to Kotlin Multiplatform + Compose Multiplatform for iOS support (see issue #5421). Key architectural decisions:
- Use Kotlin `StateFlow` over `LiveData` for multiplatform compatibility
- UI is being migrated from Android XML/Fragments to Compose Multiplatform
- Shared business logic goes in `commonMain`, platform-specific code in `androidMain`/`iosMain`
- Koin is used for dependency injection across platforms

## Build Commands

```bash
# Build and run debug version (requires Android Studio/emulator setup)
./gradlew :app:installDebug

# Run unit tests
./gradlew :app:testDebugUnitTest

# Run a single test class
./gradlew :app:testDebugUnitTest --tests "de.westnordost.streetcomplete.SomeTestClass"

# Run Android instrumented tests (requires device/emulator)
./gradlew :app:connectedDebugAndroidTest

# Lint check
./gradlew :app:lintDebug

# Update all data (translations, presets, metadata, etc.)
./gradlew updateStreetCompleteData
```

## Architecture

### Kotlin Multiplatform Structure
- `app/src/commonMain/` - Shared code (business logic, data models, utilities)
- `app/src/androidMain/` - Android-specific code (UI, platform integrations)
- `app/src/iosMain/` - iOS-specific code
- `app/src/commonTest/` - Shared tests
- `app/src/androidUnitTest/` - Android unit tests
- `app/src/androidInstrumentedTest/` - Android instrumented tests

### Key Components

**Quests** (`app/src/androidMain/kotlin/de/westnordost/streetcomplete/quests/`)
- Each quest is a folder containing the quest type definition and form
- Quest types extend `OsmElementQuestType` and define:
  - `elementFilter`: Tag query determining which OSM elements to show
  - `changesetComment`: Description for OSM changesets
  - `applyAnswerTo()`: How to apply the user's answer to OSM tags
- Quest order is defined in `QuestsModule.kt` with unique ordinal numbers
- To add a quest: duplicate an existing quest folder, modify, add to `QuestsModule.kt`

**Element Filter Syntax** (`app/src/commonMain/kotlin/de/westnordost/streetcomplete/data/elementfilter/`)
- Custom DSL for querying OSM elements, e.g.:
  ```kotlin
  override val elementFilter = """
      nodes with emergency = defibrillator
      and access !~ private|no
      and !indoor
  """
  ```
- Supports regex matching (`~`), age checks (`older today -4 years`), and logical operators

**Overlays** (`app/src/androidMain/kotlin/de/westnordost/streetcomplete/overlays/`)
- Alternative to quests for viewing/editing specific data types on the map
- Similar structure to quests but show colored geometries rather than markers

**OSM Data/Edits** (`app/src/commonMain/kotlin/de/westnordost/streetcomplete/data/osm/`)
- `edits/` - Element edit actions and upload logic
- `mapdata/` - OSM element types and geometry handling
- Supports offline editing with conflict resolution

**Dependency Injection**
- Uses Koin for DI
- Module definitions in `*Module.kt` files

### Resources
- Quest icons: `res/graphics/quest/` (SVG) → converted to `app/src/androidMain/res/drawable/` (Android XML)
- Strings: `app/src/androidMain/res/values/strings.xml` (source), translations managed via POEditor
- Country metadata: `res/country_metadata/` → compiled to `app/src/androidMain/assets/country_metadata/`

## Code Style

- Prefer composition over inheritance; extract shared code to utility files (see `app/src/commonMain/kotlin/de/westnordost/streetcomplete/osm/` for examples)
- Install the Ktlint Android Studio plugin for lint checks
- Translations are crowd-sourced via POEditor - do not manually edit translated strings.xml files

## Quest Development Guidelines

Before implementing a new quest, review `QUEST_GUIDELINES.md`:
- Only add tags to existing elements (no element creation/deletion)
- Use established OSM tags only
- Questions must be answerable from public spaces by pedestrians
- Avoid yes/no questions where 99% answer the same way

See `CONTRIBUTING_A_NEW_QUEST.md` for detailed implementation steps.
