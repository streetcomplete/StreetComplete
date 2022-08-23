{ pkgs, androidSdk }:

let
  fhsUserEnv = pkgs.buildFHSUserEnv {
    name = "android-env";
    targetPkgs = pkgs:
      (with pkgs; [ android-udev-rules androidSdk kotlin gradle jdk8 ]);
    profile = ''
      export ANDROID_HOME="${androidSdk}/share/android-sdk"
      export ANDROID_SDK_ROOT="${androidSdk}/share/android-sdk"
      export JAVA_HOME="${pkgs.jdk8.home}"
    '';
  };
in fhsUserEnv.env
