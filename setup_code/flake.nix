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

        # https://github.com/NixOS/nixpkgs/pull/491015
        jbang = pkgs.jbang.overrideAttrs {
              installPhase = ''
                runHook preInstall
                rm bin/jbang.{cmd,ps1}
                cp -r . $out
                wrapProgram $out/bin/jbang \
                  --set JAVA_HOME ${jdk} \
                  --prefix PATH ${
                    lib.makeBinPath [
                      (placeholder "out")
                      pkgs.coreutils
                      jdk
                      pkgs.curl
                    ]
                  }
                runHook postInstall
              '';
        };
       in {
         devShells.default = pkgs.mkShell {
           name = "Repository Setup";
           packages = with pkgs; [git jbang jdk];
           GIT_BIN = "${pkgs.git}/bin/git";
           ON_NIXOS = "yes";
         };
       };
    };
}