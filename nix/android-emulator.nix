{ pkgs ? import <nixpkgs> { config.android_sdk.accept_license = true; }, ... }:

pkgs.androidenv.emulateApp {
  name = "androidenv";
  platformVersion = "30";
  abiVersion = "x86_64";
  avdHomeDir = .android/avd;
}
