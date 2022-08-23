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
        config = {
          allowUnfree = true;
          android_sdk.accept_license = true;
        };
      };
      androidSdk = android-nixpkgs.sdk.${system} (sdkPkgs:
        with sdkPkgs; [
          cmdline-tools-latest
          build-tools-30
          platform-tools
          platforms-android-30
          emulator
        ]);
    in {
      checks = { };
      packages.${system} = {
        android-emulator = import ./nix/android-emulator.nix { inherit pkgs; };
      };
      devShell.${system} = import ./nix/shell.nix {
        inherit pkgs;
        inherit androidSdk;
      };
    };
}
