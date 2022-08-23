{ pkgs }:

pkgs.androidenv.emulateApp {
  name = "androidenv";
  platformVersion = "30";
  abiVersion = "x86_64";
  package = "StreetComplete";
}
