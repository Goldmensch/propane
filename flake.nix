{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
    flake-parts.url = "github:hercules-ci/flake-parts";
  };

  outputs = {
    self,
    flake-parts,
    ...
  } @ inputs:
    flake-parts.lib.mkFlake {inherit inputs;} {
      systems = ["x86_64-linux"];

      perSystem = {
        config,
        lib,
        pkgs,
        system,
        ...
      }: let
        javaVersion = 25;

        jdk = pkgs."temurin-bin-${toString javaVersion}";
        jdks = [
            jdk
            pkgs.temurin-bin
        ];

        gradle = pkgs.gradle_9.override {
            javaToolchains = jdks;

            java = pkgs.temurin-bin;
        };
       in {
         devShells.default = pkgs.mkShell {
           name = "propane";
           packages = with pkgs; [git maven] ++ jdks ++ [gradle];
         };
       };
    };
}