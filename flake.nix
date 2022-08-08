{
  description = "StreetComplete";
  nixConfig = { bash-prompt = "[StreetComplete]$ "; };
  inputs = {
    nixpkgs = { url = "nixpkgs/nixos-22.05"; };
    android-nixpkgs = {
      url = "github:tadfisher/android-nixpkgs/stable";
      inputs.nixpkgs.follows = "nixpkgs";
    };
  };
  outputs = { self, nixpkgs, android-nixpkgs }:
    let
      system = "x86_64-linux";
      pkgs = import nixpkgs {
        inherit system;
        config.android_sdk.accept_license = true;
      };
      androidSdk = android-nixpkgs.sdk (sdkPkgs:
        with sdkPkgs; [
          cmdline-tools-latest
          build-tools-32-0-0
          platform-tools
          platforms-android-31
          emulator
        ]);
    in {
      checks = { };
      devShell."${system}" = import ./shell.nix {
        inherit pkgs;
        inherit androidSdk;
      };
    };
}
