{ pkgs }:

pkgs.androidenv.buildApp {
  name = "StreetComplete";
  src = ../.;
}
