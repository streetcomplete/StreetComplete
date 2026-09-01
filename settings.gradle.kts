rootProject.name = "StreetComplete"

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven("https://jogamp.org/deployment/maven") {
            content { includeGroupAndSubgroups("org.jogamp") }
        }
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        ivy("https://central.sonatype.com/repository/maven-snapshots/org/maplibre/compose/") {
            name = "maplibreComposePinnedSnapshot"
            patternLayout {
                artifact("[module]/0.15.1-SNAPSHOT/[artifact]-[revision](-[classifier]).[ext]")
            }
            metadataSources {
                gradleMetadata()
                artifact()
            }
            content { includeGroup("org.maplibre.compose") }
        }
        maven("https://jogamp.org/deployment/maven") {
            content { includeGroupAndSubgroups("org.jogamp") }
        }
    }
}

include(":app")
include(":androidApp")
