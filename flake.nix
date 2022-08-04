{
  description = "StreetComplete";
  nixConfig = { bash-prompt = "[StreetComplete]$ "; };
  inputs = {
    flake-utils = { url = "github:numtide/flake-utils"; };
    pre-commit-hooks = { url = "github:cachix/pre-commit-hooks.nix"; };
  };
  outputs = { self, nixpkgs, flake-utils, pre-commit-hooks }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
        src = ./.;
      in {
        packages = {
          android-udev-rules = with pkgs; stdenv.mkDerivation rec {
            pname = "android-udev-rules";
            version = "20220102";

            src = pkgs.fetchFromGitHub {
              owner = "M0Rf30";
              repo = "android-udev-rules";
              rev = version;
              sha256 = "sha256-D2dPFvuFcZtosfTfsW0lmK5y8zqHdIxJBlvmP/R91CE=";
            };

            installPhase = ''
              runHook preInstall
              install -D 51-android.rules $out/etc/udev/rules.d/51-android.rules
              runHook postInstall
            '';
          };
        };
        checks = {
          pre-commit-check = pre-commit-hooks.lib.${system}.run {
            inherit src;
            hooks = {
              nixfmt = { enable = true; };
              shellcheck = { enable = true; };
              custom-hunspell = {
                enable = true;
                name = "hunspell";
                entry = "${pkgs.hunspell}/bin/hunspell -l";
                files = "\\.((txt)|(wiki)|\\d)$";
              };
            };
          };
        };
        devShell = import ./shell.nix {
          inherit pkgs;
          inherit (self.checks.${system}.pre-commit-check) shellHook;
        };
      });
}
