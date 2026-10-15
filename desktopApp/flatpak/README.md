# Linux Flatpak development build

This is an experimental way to test the desktop app on Linux, including ARM64
postmarketOS phones. It is not a release or a Flathub submission. The JVM code can
be compiled on macOS; the native dependencies and Java runtime must match the
Linux target.

## Stage the app

With the project's normal Java and Android SDK prerequisites installed, run from
the repository root:

```sh
./gradlew -I desktopApp/flatpak/stage.gradle :desktopApp:stageFlatpak -PflatpakArch=arm64
```

Use `-PflatpakArch=x64` for x86-64 Linux. The output is
`desktopApp/build/flatpak/app`. Staging replaces this directory, so add the Java
runtime only after staging. The init script changes runtime dependency selection
for this invocation only; do not use it for normal host launches or packaging.

Place a Linux glibc Java 25 runtime for the target architecture at
`desktopApp/build/flatpak/app/files/jre`, with `bin/java` directly below it.
Use a trusted distribution and verify its checksum. The runtime needs `java.desktop`,
`java.prefs`, and `jdk.unsupported`; a full Java 25 JDK also works.

Before transferring the directory to Linux, make the launcher executable:

```sh
chmod +x desktopApp/build/flatpak/app/files/bin/streetcomplete
```

## Install on Linux

Copy the staged directory, including the Java runtime, to `app` in a working
directory on the Linux device. Install Flatpak and the matching Freedesktop 25.08
runtime first. Then run in that working directory:

```sh
flatpak build-finish app
flatpak build-export repo app
flatpak remote-add --user --if-not-exists --no-gpg-verify streetcomplete-dev "$PWD/repo"
flatpak install --user streetcomplete-dev de.westnordost.streetcomplete.desktop
flatpak run de.westnordost.streetcomplete.desktop
```

The unsigned local remote is only for development. For subsequent builds, replace
the staged files, export again, and use `flatpak update --user
de.westnordost.streetcomplete.desktop`. Do not run `build-finish` a second time on
an already finalized directory. Close running app instances before updating.

To uninstall:

```sh
flatpak uninstall --user de.westnordost.streetcomplete.desktop
```

## Packaging details and limitations

- Nucleus Tao provides an OpenGL context on Linux, so the map runtime must also
  use OpenGL rather than Vulkan.
- The preset dictionary and country boundaries currently require filesystem
  access. They are bundled under `/app/share/streetcomplete`, not read from the
  developer's checkout.
- The window uses Wayland. X11 access is also needed by the current AWT display
  density lookup in `DesktopModule`; otherwise creating `MainViewModel` fails
  with `HeadlessException`.
- Java preferences are rooted in `$XDG_CONFIG_HOME` so onboarding, settings, and
  other preferences survive sandbox restarts. Databases use FileKit's persistent
  application data directory.
- Location uses the XDG Location portal. GeoClue must be available and system
  location services enabled. A GTK portal fallback can disable location via
  `org.gnome.system.location enabled`, even in Plasma Mobile. Enabling it is an
  explicit user/system setting, not something this launcher changes.
- Permission approval does not guarantee a precise GPS fix. Wi-Fi/IP positioning
  can be coarse; modem/GNSS support and satellite reception are separate issues.
- Location recovery after services/permission changes, automatic on-screen
  keyboard appearance, and unwanted window decorations need further investigation.

The ARM64 OpenGL map, location delivery through the portal, and persistent Java
preferences were tested on a OnePlus 6 with postmarketOS v26.06, Plasma Mobile,
Freedesktop 25.08, and Java 25. The x86-64 path has not been tested on a device.
