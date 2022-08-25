{ pkgs ? import <nixpkgs> { config.android_sdk.accept_license = true; } }:

pkgs.androidenv.emulateApp {
  name = "androidenv";
  platformVersion = "31";
  abiVersion = "x86_64";
  systemImageType = "default";
  app = import ./android-package.nix { };
  package = "com.ngi.streetcomplete";
  enableGPU = true;
}
