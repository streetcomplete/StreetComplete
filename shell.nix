{ pkgs ? import <nixpkgs> { config.android_sdk.accept_license = true; } }:

let androidSdk = pkgs.androidenv.androidPkgs_9_0.androidsdk;
in pkgs.mkShell {
  buildInputs = with pkgs; [
    android-udev-rules
    androidenv.androidPkgs_9_0.platform-tools
  ];
  GRADLE_OPTS =
    "-Dorg.gradle.project.android.aapt2FromMavenOverride=${androidSdk}/libexec/android-sdk/build-tools/28.0.3/aapt2";
}
