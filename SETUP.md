# Using this template

This template contains:
- multimodule gradle project
- build/test workflow with gradle
- release and deploy workflow with
  - wiki support via mkdocs (gh-pages)
  - javadocs (gh-pages)
  - GitHub release creation
  - deploy to maven central via sonatype (+ snapshots)
- implementation of tip and tails release model (jdk)
  - tail branch creation on major bump

## GitHub Bots/Apps to use for best experience
For the best experience, this template is built with some discord bots in mind to help automatic configuration.
Please install following bots, before running the setup action:
- [Settings](https://github.com/apps/settings)

## First steps
Open the repository locally.

If you're on nixos: just run `./setup` in the root directory

If you're not on nixos, you need `jbang` installed, then run `./setup_code/java/Setup.java`

You will be prompted a couple of question to answer.

After a successful run, this readme will be moved to SETUP.md and can be deleted after successfully configuring the repository.

Then
- set the gh-pages setting to "GitHub Actions"
- set the following secret keys in your repository:
  - jreleaser_gpg_pass -> the gpg key password used for publishing to maven central
  - jreleaser_gpg_public_key -> the gpg public key used for publishing to maven central
  - jreleaser_gpg_secret_key -> the gpg secret key used for publishing to maven central
  - jreleaser_user -> your maven central token username
  - jreleaser_password -> your maven central token password

## VARIABLES
This template has several variables, that will be replaced by the setup-repo task.

- PROJECT_NAME -> the project's name
- PROJECT_DESC -> the project's main modules (lib) description
- PROJECT_LOWER_NAME -> the project's name but in lower case, for use in java code

- JAVA_VERSION -> the project java version to use


- LICENSE_NAME -> the project's license name
- LICENSE_URL -> the url to the used license


- AUTHOR_NAME -> the authors name (only one author)

- MVN_GROUP -> the projects maven group, used for publishing
- MVN_ARTIFACT -> the projects maven artifact, used for publishing

- REPO_OWNER -> the repos owner
- REPO_NAME -> the repos name with the author, eg. Goldmensch/fluava
- REPO_WO_OWNER_NAME -> the repos name without the owner
- REPO_URL -> the url to the online code repository, without https://, eg. github.com/godmensch/jtemplate

# Structure / Functionality
The repository is structured like a multimodule gradle project, making it easy to extend.

### Wiki
The wiki source code, that will be deployed to gh-pages, can be found in `./wiki`.
Each commit will be automatically build and deployed to GitHub pages under the `snapshot` version.

If a release is triggered, the wiki is deployed under the respective version.

The string `MVN_ARTIFACT_JAVADOC_VERSION` (`MVN_ARTIFACT` is a [variable](#variables) replaced during [setup](#first-steps))
in your wiki is always replaced with the latest Javadoc version published. (the Javadoc version of the same deployment run).


### Javadoc
Per default the javadoc of the `lib` module will be published to `gh-pages`.
If you want to publish more, you have to adjust the logic in `.github/workflows/cd.yml` in the `javadoc` job,
e.g. setting the right directory in the `Deploy docs`.

## Triggering a Release
Triggering a release is particular easy, you just have to make a commit to the `master` (or any `major.minor.x` = tail branch)
with following format:
`Release MAJOR.MINOR.PATCH : RELEASE TITLE`

It will trigger a wiki/javadoc deploy and push your artifacts to maven central via sonatype. The artifacts
have to be manually "published" in the sonatype web interface after that.
Additionally, if the major version is increased, a new tail branch called `MAJOR.MINOR.x` is created, were
further fixes can be released for that version.

If you trigger a release in a tail branch, then you can only change the patch version, since
tail branches should not include any new features. They're so to speak pinned on their major/minor version.
