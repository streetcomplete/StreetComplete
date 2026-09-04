#!/bin/zsh
set -euo pipefail

metrics_file=${1:?usage: check-map-performance-metrics.sh <metrics.tsv>}

if [[ ! -f "$metrics_file" ]]; then
    echo "Map performance metrics not found: $metrics_file" >&2
    exit 1
fi

expected_phases=(
    warmup
    load-352-pins-37-icons
    cluster-to-pins
    overlay-160-elements-8-icons
    quest-open
    selected-pan
    quest-close
    reload-known-icons
    far-pan-source-loads
    sustained-pan-source-soak
    location-heading-track-realistic
    location-heading-track-stress
    track-stop
    base-style-reload
    downloaded-area-1681-tiles
    app-background-foreground
    presentation-cycles
)

phase_line() {
    local phase=$1
    rg -m 1 $'\tMapPerformanceScenario\tEND '"$phase"':' "$metrics_file"
}

performance_failed=0

for phase in $expected_phases; do
    line=$(phase_line "$phase") || {
        echo "Missing completed map performance phase: $phase" >&2
        exit 1
    }
done

# Cold style startup is measured but is not an interaction regression. The lifecycle phases put
# the app in the background or remove its render surface. They assert successful recovery instead
# of requiring uninterrupted callbacks while the map cannot present frames.
no_freeze_phases=(
    load-352-pins-37-icons
    cluster-to-pins
    overlay-160-elements-8-icons
    quest-open
    selected-pan
    quest-close
    reload-known-icons
    far-pan-source-loads
    sustained-pan-source-soak
    location-heading-track-realistic
    track-stop
    base-style-reload
    downloaded-area-1681-tiles
)

for phase in $no_freeze_phases; do
    line=$(phase_line "$phase")
    if [[ ! "$line" =~ 'uiFramesOver100Millis=0$' ]]; then
        echo "Map performance regression: $phase had a display-frame freeze over 100 ms" >&2
        echo "$line" >&2
        performance_failed=1
    fi
    if [[ ! "$line" =~ 'mapFramesOver100Millis=0 ' ]]; then
        echo "Map performance regression: $phase had no map frame callback for over 100 ms" >&2
        echo "$line" >&2
        performance_failed=1
    fi
done

for lifecycle_phase in app-background-foreground presentation-cycles; do
    lifecycle_line=$(phase_line "$lifecycle_phase")
    lifecycle_frames=$(echo "$lifecycle_line" | sed -E 's/.* frames=([0-9]+) .*/\1/')
    if [[ "$lifecycle_frames" == "$lifecycle_line" || "$lifecycle_frames" -lt 10 ]]; then
        echo "Map presentation did not resume rendering during $lifecycle_phase" >&2
        echo "$lifecycle_line" >&2
        exit 1
    fi
done

rg -q $'\tMapPerformanceScenario\tLIFECYCLE_RESUME_PROBE_READY$' "$metrics_file"
rg -q $'\tMapPerformanceScenario\tSUSTAINED_PAN_PROBE_READY$' "$metrics_file"
presentation_recoveries=$(rg -c \
    $'\tMapPerformanceScenario\tPRESENTATION_RECOVERED cycle=' "$metrics_file")
if [[ "$presentation_recoveries" -ne 5 ]]; then
    echo "Only $presentation_recoveries of 5 map presentation cycles rendered fresh frames" >&2
    exit 1
fi

# Startup and first-time image registration have separate cold-path costs. Once the images and
# style are warm, these user interactions must not miss three consecutive 60 Hz display frames.
smooth_phases=(
    cluster-to-pins
    overlay-160-elements-8-icons
    quest-open
    selected-pan
    quest-close
    reload-known-icons
    far-pan-source-loads
    sustained-pan-source-soak
    location-heading-track-realistic
    downloaded-area-1681-tiles
)

for phase in $smooth_phases; do
    line=$(phase_line "$phase")
    if [[ ! "$line" =~ 'uiFramesOver50Millis=0 ' && ! "$line" =~ 'uiFramesOver50Millis=0$' ]]; then
        echo "Map performance regression: $phase had a display-frame gap over 50 ms" >&2
        echo "$line" >&2
        performance_failed=1
    fi
    if [[ ! "$line" =~ 'mapFramesOver50Millis=0 ' ]]; then
        echo "Map performance regression: $phase had no map frame callback for over 50 ms" >&2
        echo "$line" >&2
        performance_failed=1
    fi
    if [[ ! "$line" =~ 'framesBelow20Fps=0 ' ]]; then
        echo "Map performance regression: $phase reported a rendered frame below 20 fps" >&2
        echo "$line" >&2
        performance_failed=1
    fi
done

# Focus animation keeps the native renderer continuously invalidated. Require useful forward
# progress; this callback measures MapLibre render notifications, not physical display presents.
continuous_render_phases=(
    quest-open
    selected-pan
)

for phase in $continuous_render_phases; do
    line=$(phase_line "$phase")
    callback_rate=$(echo "$line" | sed -E 's/.* callbackRate=([^ ]+) .*/\1/')
    if ! awk -v rate="$callback_rate" 'BEGIN { exit !(rate >= 30) }'; then
        echo "Map renderer rate fell below 30 callbacks/second during $phase" >&2
        echo "$line" >&2
        performance_failed=1
    fi
done

# Opening and closing a quest only changes layer visibility on master. It must not deactivate,
# clear, or republish the retained quest-pin source.
selection_pin_publications=$(awk -F '\t' '
    $2 == "MapPinsPerformance" && $3 ~ /^Published [0-9]+ pins / &&
        $3 ~ / phase=(quest-open|quest-close)$/ { count++ }
    END { print count + 0 }
' "$metrics_file")
if [[ "$selection_pin_publications" -ne 0 ]]; then
    echo "Quest selection rebuilt the retained pin source $selection_pin_publications time(s)" >&2
    exit 1
fi

if awk -F '\t' '
    $3 ~ /^ACTIVE_PIN_MODE NONE phase=(quest-open|quest-close)$/ { found = 1 }
    END { exit !found }
' "$metrics_file"; then
    echo "Quest selection deactivated the quest-pin data pipeline" >&2
    exit 1
fi

production_loads=$(awk -F '\t' '
    $3 ~ /^SCENARIO_PIN_VIEWPORT_LOAD .* phase=far-pan-source-loads$/ { count++ }
    END { print count + 0 }
' "$metrics_file")
if [[ "$production_loads" -lt 4 ]]; then
    echo "Far-pan scenario exercised only $production_loads pin publications" >&2
    exit 1
fi

style_loading_count=$(rg -c $'\tMapPerformanceScenario\tSTYLE Loading$' "$metrics_file")
style_ready_count=$(rg -c $'\tMapPerformanceScenario\tSTYLE Ready$' "$metrics_file")
if [[ "$style_loading_count" -lt 2 || "$style_ready_count" -lt 2 ]]; then
    echo "Base-style reload did not cross a second Loading/Ready generation" >&2
    exit 1
fi

style_reload_pin_publications=$(awk -F '\t' '
    $2 == "MapPinsPerformance" && $3 ~ /Published 352 pins after required images/ &&
        $3 ~ / phase=base-style-reload$/ {
        count++
    }
    END { print count + 0 }
' "$metrics_file")
style_reload_image_batches=$(awk -F '\t' '
    $2 == "MapPinsPerformance" &&
        $3 ~ /^Published [0-9]+ style images across display frames/ &&
        $3 ~ / phase=base-style-reload$/ {
        count++
    }
    END { print count + 0 }
' "$metrics_file")
if [[ "$style_reload_pin_publications" -lt 1 || "$style_reload_image_batches" -lt 1 ]]; then
    echo "Pins and style images were not restored after the base-style reload" >&2
    exit 1
fi

rg -q $'\tMapPerformanceScenario\tCOMPLETE$' "$metrics_file"
if [[ "$performance_failed" -ne 0 ]]; then
    exit 1
fi
echo "Map performance assertions passed: $metrics_file"
