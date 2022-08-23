{ pkgs, androidSdk }:

let
  fhsUserEnv = pkgs.buildFHSUserEnv {
    name = "android-env";
    targetPkgs = pkgs: (with pkgs; [ androidSdk kotlin gradle jdk ]);
    profile = ''
      export ANDROID_HOME="${androidSdk}/libexec/android-sdk"
      export ANDROID_JAVA_HOME="${pkgs.jdk.home}"
      export ANDROID_AVD_HOME="${toString ./.}/.android/avd"
    '';
  };
in fhsUserEnv.env
