{
  description = "StreetComplete";
  nixConfig = { bash-prompt = "[StreetComplete]$ "; };
  inputs = { flake-utils = { url = "github:numtide/flake-utils"; }; };
  outputs = { self, nixpkgs, flake-utils, pre-commit-hooks }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
        src = ./.;
      in {
        checks = { };
        devShell = import ./shell.nix {
          inherit pkgs;
          inherit (self.checks.${system}.pre-commit-check) shellHook;
        };
      });
}
