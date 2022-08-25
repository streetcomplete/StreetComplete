{ pkgs ? import <nixpkgs> { config.android_sdk.accept_license = true; } }:

pkgs.androidenv.buildApp {
  name = "StreetComplete";
  src = ../buildSrc/src;
  platformVersions = [ "31" ];
}
