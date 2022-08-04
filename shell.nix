{ pkgs ? import <nixpkgs> { }, shellHook ? ''
  EDITOR=vim
'' }:

pkgs.mkShell {
  buildInputs =
    [ pkgs.android-udev-rules pkgs.androidenv.androidPkgs_9_0.platform-tools ];
  inherit shellHook;
}
