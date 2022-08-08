{
  description = "StreetComplete";
  nixConfig = { bash-prompt = "[StreetComplete]$ "; };
  inputs = { nixpkgs = { url = "nixpkgs/nixos-22.05"; }; };
  outputs = { self, nixpkgs }:
    let
      system = "x86_64-linux";
      pkgs = import nixpkgs {
        inherit system;
        config.android_sdk.accept_license = true;
      };
    in {
      checks = { };
      devShell."${system}" = import ./shell.nix { inherit pkgs; };
    };
}
