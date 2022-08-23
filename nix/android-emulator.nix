{ pkgs }:

pkgs.androidenv.emulateApp {
  name = "androidenv";
  platformVersion = "30";
  abiVersion = "x86_64";
  app = pkgs.callPackage ./android-package.nix { };
  package = "StreetComplete";
}
