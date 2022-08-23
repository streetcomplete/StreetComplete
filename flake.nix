{
  description = "StreetComplete";
  nixConfig = { bash-prompt = "[StreetComplete]$ "; };
  inputs = { nixpkgs = { url = "nixpkgs/nixos-22.05"; }; };
  outputs = { self, nixpkgs }:
    let
      system = "x86_64-linux";
      pkgs = import nixpkgs {
        inherit system;
        config = {
          allowUnfree = true;
          android_sdk.accept_license = true;
        };
      };
    in {
      checks = { };
      packages.${system} = {
        android-emulator = import ./nix/android-emulator.nix { inherit pkgs; };
      };
      devShell.${system} = import ./nix/shell.nix { inherit pkgs; };
    };
}
