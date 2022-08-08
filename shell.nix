{ pkgs, androidSdk }:

pkgs.mkShell {
  buildInputs = with pkgs; [
    android-udev-rules
    androidenv.androidPkgs_9_0.platform-tools
    androidSdk
    kotlin
    gradle
  ];
  ANDROID_HOME = "${androidSdk}/share/android-sdk";
}
