#!/bin/zsh
set -euo pipefail

profile_xcode_developer_dir=/Applications/Xcode-26.5.0.app/Contents/Developer
profile_simulator_udid=${STREETCOMPLETE_IOS_SIMULATOR:-4AE0B7AD-8A31-457E-B505-A46F3644E43D}
profile_bundle_id=com.sargunv.streetcomplete.iosport
profile_derived_data=build/xcode-simulator
profile_app_path="$profile_derived_data/Build/Products/Debug-iphonesimulator/StreetComplete.app"
profile_artifact_dir="build/map-performance/$(date -u +%Y%m%dT%H%M%SZ)"
profile_xcode_actions=(clean build)

if [[ ${STREETCOMPLETE_MAP_PERFORMANCE_INCREMENTAL_BUILD:-0} == 1 ]]; then
    profile_xcode_actions=(build)
fi

export DEVELOPER_DIR="$profile_xcode_developer_dir"
mkdir -p "$profile_artifact_dir"

if ! xcrun simctl list devices | rg -q "$profile_simulator_udid .*Booted"; then
    xcrun simctl boot "$profile_simulator_udid" 2>/dev/null || true
fi
xcrun simctl bootstatus "$profile_simulator_udid" -b
open -a Simulator
osascript -e 'tell application "Simulator" to activate'

xcodebuild \
    -quiet \
    -project iosApp/iosApp.xcodeproj \
    -scheme iosApp \
    -configuration Debug \
    -destination "id=$profile_simulator_udid" \
    -derivedDataPath "$profile_derived_data" \
    PRODUCT_BUNDLE_IDENTIFIER="$profile_bundle_id" \
    COMPILER_INDEX_STORE_ENABLE=NO \
    CODE_SIGNING_ALLOWED=NO \
    "${profile_xcode_actions[@]}"

xcrun simctl install "$profile_simulator_udid" "$profile_app_path"
xcrun simctl terminate "$profile_simulator_udid" "$profile_bundle_id" 2>/dev/null || true
profile_data_container=$(xcrun simctl get_app_container \
    "$profile_simulator_udid" "$profile_bundle_id" data)
profile_database="$profile_data_container/Library/Application Support/streetcomplete_v2.db"
profile_console="$profile_artifact_dir/console.log"
profile_console_pid=
profile_sample_pid=

profile_cleanup() {
    xcrun simctl terminate "$profile_simulator_udid" "$profile_bundle_id" 2>/dev/null || true
    if [[ -n "$profile_console_pid" ]]; then
        kill "$profile_console_pid" 2>/dev/null || true
        wait "$profile_console_pid" 2>/dev/null || true
    fi
    if [[ -n "$profile_sample_pid" ]]; then
        kill "$profile_sample_pid" 2>/dev/null || true
        wait "$profile_sample_pid" 2>/dev/null || true
    fi
}
trap profile_cleanup EXIT

profile_log_rowid=$(sqlite3 "$profile_database" 'SELECT coalesce(max(rowid), 0) FROM logs;')
SIMCTL_CHILD_STREETCOMPLETE_MAP_PERFORMANCE=1 \
    xcrun simctl launch --console "$profile_simulator_udid" "$profile_bundle_id" \
    >"$profile_console" 2>&1 &
profile_console_pid=$!

profile_wait_for_log() {
    local expected_message=$1
    local attempt
    for attempt in {1..600}; do
        if sqlite3 "$profile_database" \
            "SELECT 1 FROM logs
             WHERE tag = 'MapPerformanceScenario'
               AND message = '$expected_message'
               AND rowid > $profile_log_rowid
             LIMIT 1;" 2>/dev/null | rg -q '^1$'; then
            return 0
        fi
        sleep 0.1
    done
    echo "Timed out waiting for MapPerformanceScenario: $expected_message" >&2
    tail -200 "$profile_console" >&2 || true
    return 1
}

# Xcode does not declare the generated static Kotlin framework as a linker input, so an
# incremental build can leave an older app executable around after Gradle rebuilds the framework.
# Refuse to profile it even when an incremental run was explicitly requested.
profile_wait_for_log "VERSION 10"

# Keep trace capture out of the deterministic run. xctrace can outlive its time limit and prevent
# screenshots and metrics from being collected; CPU traces are a separate, optional investigation.
if [[ ${STREETCOMPLETE_MAP_PERFORMANCE_SAMPLE:-0} == 1 ]]; then
    profile_sample_phase=${STREETCOMPLETE_MAP_PERFORMANCE_SAMPLE_PHASE:-quest-open}
    profile_sample_seconds=${STREETCOMPLETE_MAP_PERFORMANCE_SAMPLE_SECONDS:-5}
    profile_wait_for_log "BEGIN $profile_sample_phase"
    profile_app_pid=$(ps -axo pid,command | awk \
        '/[S]treetComplete.app\/StreetComplete/ && !found { print $1; found = 1 }')
    sample "$profile_app_pid" "$profile_sample_seconds" \
        -file "$profile_artifact_dir/$profile_sample_phase.sample.txt" \
        >"$profile_artifact_dir/$profile_sample_phase.sample.log" 2>&1 &
    profile_sample_pid=$!
fi
profile_wait_for_log "BEGIN quest-open"
for profile_marker_attempt in {1..600}; do
    if sqlite3 "$profile_database" \
        "WITH marker_phase AS (
             SELECT max(rowid) AS begin_rowid FROM logs
             WHERE tag = 'MapPerformanceScenario'
               AND message = 'BEGIN quest-open'
               AND rowid > $profile_log_rowid
         )
         SELECT 1 FROM marker_phase
         WHERE EXISTS (
             SELECT 1 FROM logs
             WHERE tag = 'MapPinsPerformance'
               AND message = 'Published 48 geometry markers phase=quest-open'
               AND rowid > marker_phase.begin_rowid
         )
           AND EXISTS (
             SELECT 1 FROM logs
             WHERE tag = 'MapPinsPerformance'
               AND message LIKE 'Selected-pin setData%'
               AND rowid > marker_phase.begin_rowid
         );" 2>/dev/null | rg -q '^1$'; then
        break
    fi
    if [[ "$profile_marker_attempt" == 600 ]]; then
        echo "Timed out waiting for native geometry-marker data" >&2
        tail -200 "$profile_console" >&2 || true
        exit 1
    fi
    sleep 0.1
done
xcrun simctl io "$profile_simulator_udid" screenshot \
    "$profile_artifact_dir/selected-markers.png" >/dev/null
if [[ -n "$profile_sample_pid" ]]; then
    wait "$profile_sample_pid"
    profile_sample_pid=
fi

profile_wait_for_log "SUSTAINED_PAN_PROBE_READY"
xcrun simctl io "$profile_simulator_udid" screenshot \
    "$profile_artifact_dir/soak.png" >/dev/null

profile_wait_for_log "BEGIN app-background-foreground"
xcrun simctl launch "$profile_simulator_udid" com.apple.Preferences >/dev/null
sleep 1
xcrun simctl launch "$profile_simulator_udid" "$profile_bundle_id" >/dev/null
profile_wait_for_log "LIFECYCLE_RESUME_PROBE_READY"
xcrun simctl io "$profile_simulator_udid" screenshot \
    "$profile_artifact_dir/resumed.png" >/dev/null

profile_wait_for_log "COMPLETE"

if rg -q 'Style image missing:' "$profile_console"; then
    echo "Map published feature data before its required style image was installed" >&2
    rg 'Style image missing:' "$profile_console" >&2
    exit 1
fi

xcrun simctl io "$profile_simulator_udid" screenshot \
    "$profile_artifact_dir/final.png" >/dev/null
profile_cleanup

sqlite3 -header -separator $'\t' "$profile_database" \
    "SELECT datetime(timestamp/1000, 'unixepoch') AS utc, tag, message
     FROM logs
     WHERE tag IN ('MapPerformanceScenario', 'MapPinsPerformance', 'MapSourcePerformance')
       AND rowid > $profile_log_rowid
     ORDER BY rowid ASC;" \
    > "$profile_artifact_dir/metrics.tsv"

scripts/check-map-screenshot.swift \
    "$profile_artifact_dir/selected-markers.png" \
    --require-markers
scripts/check-map-screenshot.swift "$profile_artifact_dir/soak.png"
scripts/check-map-screenshot.swift "$profile_artifact_dir/resumed.png"
scripts/check-map-screenshot.swift "$profile_artifact_dir/final.png"

rg -q $'\tMapPerformanceScenario\tCOMPLETE$' "$profile_artifact_dir/metrics.tsv"
echo "Map performance artifacts: $profile_artifact_dir"
if [[ ${STREETCOMPLETE_MAP_PERFORMANCE_SAMPLE:-0} == 1 ]]; then
    echo "Skipping frame thresholds because process sampling pauses the app"
else
    scripts/check-map-performance-metrics.sh "$profile_artifact_dir/metrics.tsv"
fi
sed -n '1,200p' "$profile_artifact_dir/metrics.tsv"
