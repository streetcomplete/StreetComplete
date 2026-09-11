# Temporary migration branch guidance

These instructions capture the working preferences for this MapLibre Compose
migration branch. Delete this file before marking the PR ready for review.

## Scope and quality

- Treat a focused, reviewable diff, idiomatic MapLibre Compose, and idiomatic
  Kotlin as equal priorities.
- Finish the production MapLibre Compose migration, preserving StreetComplete's
  Android behavior and keeping the map implementation shared for future iOS work.
  The full iOS Compose Multiplatform migration is outside this branch's scope.
- Aim for code Tobias can maintain in this repository. PR #7068 was an exploratory
  port; use it as a reference, not as the quality bar or a source of extra scope.
- Keep the diff focused. Remove redundant implementations when equivalent upstream
  changes are merged. Do not fold unrelated cleanup or local build problems into
  migration work; record a relevant build hazard only if it helps future work.

## Implementation

- Prefer idiomatic Kotlin and existing repository patterns. Keep ownership and
  data flow explicit; avoid speculative abstractions, unnecessary adapters, and
  indirection that makes a small operation harder to read.
- Check the MapLibre Compose v0.16 demo and the dependency's actual APIs before
  inventing integration patterns or retaining workarounds for older versions.
- Keep restorable presentation state in compositions and expose derived data and
  operations through view models resolved at the screen boundary. Pass data and
  callbacks to child composables. Preserve the existing controllers/sources and
  map source helpers.
- Use DI for shared runtime ownership. Move MapState ownership to the screen
  composition after checking the library's lifetime and restoration APIs.
  Do not add a CompositionLocal bridge just to reach style content or callbacks
  when the library already supports them directly.
- Explain non-obvious state adaptation or lifecycle behavior with short comments.
  When an upstream limitation requires a workaround, link its issue in a TODO.
  Verify the limitation still applies to the version used by this branch.

## Validation

- Test StreetComplete behavior, not guarantees provided by MapLibre Compose or
  other dependencies. Before committing tests, find comparable tests already
  maintained by Tobias and match their scope and depth.
- Temporary tests are welcome for verification. Remove temporary harnesses,
  dependencies, and build configuration before committing unless they meet the
  repository's existing testing conventions.
- Run checks appropriate to the change. Distinguish compilation, automated tests,
  emulator checks, and physical-device observations; do not claim runtime parity
  from a successful build alone.

## Plan and collaboration

- Keep `docs/maplibre-compose-migration.md` limited to concrete remaining work we
  intend to do on this branch. Each item should have an actionable fix or
  investigation and a way to establish completion.
- Delete completed items or completed portions. Refine existing entries in place;
  append only newly discovered defects or gaps. Do not accumulate progress logs,
  resolved blocker lists, speculative architecture goals, or work owned elsewhere.
- Explain proposed changes plainly and concisely, including why they belong in
  this migration. Preserve the user's edits and approved wording.
- Follow the user's commit and push instructions for each step. Authorization to
  commit does not by itself authorize a push. Do not update the PR description
  unless explicitly asked; the user has deliberately trimmed it.
