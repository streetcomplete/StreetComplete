{ pkgs, androidSdk }:

pkgs.mkShell {
  buildInputs = with pkgs; [
    android-udev-rules
    androidSdk
    kotlin
    gradle
    jdk8
  ];
  ANDROID_HOME = "${androidSdk}/share/android-sdk";
  ANDROID_SDK_ROOT = "${androidSdk}/share/android-sdk";
  JAVA_HOME = pkgs.jdk8.home;
}
