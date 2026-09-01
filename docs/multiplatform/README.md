# Multiplatform migration notes

These notes are the durable ledger for the Android, desktop, and iOS migration.
They are updated in the same commits as the implementation they describe.

- [What is going well](01-going-well.md)
- [What still needs work](02-needs-work.md)
- [MapLibre Compose upstream findings](03-maplibre-compose-upstream.md)
- [Validation evidence](04-validation.md)

The migration is complete only when all three targets run the same product flows,
all unavoidable platform differences are implemented behind narrow platform seams,
and every remaining limitation has both evidence and an actionable `TODO`.
