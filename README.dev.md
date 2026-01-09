# Preparing your environment for a release

- Ensure you have access to publish to the repository on
  [Central Portal](https://central.sonatype.com/).
  - See the section about Central Portal access.
- You need a GPG secret key. You need to publish it as well.
  - See the section about setting up GPG.
- Ensure the SSH key you use on GitHub.com is available.
  - e.g., `~/.ssh/id_rsa`.
- Ensure an appropriate `~/.gitconfig` is set up.
  - The release process generates commits.
- Ensure you have the necessary dependencies available:
  - Java 17+ (Android Studio JDK recommended)
  - Android SDK with API 34+
- Ensure [gh](https://github.com/cli/cli) is set up and in your `PATH`.

## Setting up Central Portal access

To get this access, first create a Central Portal account at
[central.sonatype.com](https://central.sonatype.com/).

You will need access to the `com.maxmind` namespace. Contact MaxMind operations
to request access to the namespace.

### Configuring credentials

This project reads credentials from Maven's `~/.m2/settings.xml`, allowing you
to share credentials with other MaxMind Java/Maven projects.

Configure your `~/.m2/settings.xml` for Central Portal. See
[these instructions](https://central.sonatype.org/publish/publish-portal-maven/#credentials):

```xml
<settings>
  <servers>
    <server>
      <id>central</id>
      <username><!-- your token username --></username>
      <password><!-- your token password --></password>
    </server>
  </servers>
</settings>
```

GPG signing uses the system `gpg` command (same as Maven projects), so your
existing `~/.gnupg` configuration will be used automatically.

**Alternative: local.properties**

You can also configure credentials in `local.properties` (takes precedence over
settings.xml):

```properties
mavenCentralUsername=<your-central-portal-username-or-token>
mavenCentralPassword=<your-central-portal-password-or-token>
```

**For CI/CD**, set environment variables:

- `ORG_GRADLE_PROJECT_mavenCentralUsername`
- `ORG_GRADLE_PROJECT_mavenCentralPassword`

Some links about Central Portal:

- [Maven Central Repository homepage](https://central.sonatype.com/)
- [Publishing guide](https://central.sonatype.org/publish/publish-portal-maven/)

## Setting up GPG

You need a key. It is fine to create/use your own, but you'll probably want one
with your MaxMind email address.

If you need to generate a key: `gpg --gen-key`.

If you have one and need to export/import it:

    gpg --export-secret-keys --armor > secretkey.gpg
    gpg --import secretkey.gpg
    gpg --edit-key <key ID>

and enter `trust` and choose ultimate.

Make sure the key shows up in `gpg --list-secret-keys`.

Make sure you publish it to a keyserver. See
[here](http://central.sonatype.org/pages/working-with-pgp-signatures.html) for
more info.

### gpg "inappropriate ioctl" errors

Add this to `~/.gnupg/gpg.conf`:

    use-agent
    pinentry-mode loopback

Add this to `~/.gnupg/gpg-agent.conf`:

    allow-loopback-pinentry

# Releasing

## Steps

1. Ensure you can run `./gradlew :device-sdk:test` and
   `./gradlew :device-sdk:assemble` successfully. Run `./gradlew clean` after.

2. Create a release branch off `main`. Ensure you have a clean checkout.
   - We'll be generating commits.
   - When the release is complete, deliver the release PR for review.

3. Review open issues and PRs to see if any can easily be fixed, closed, or
   merged.

4. Review `CHANGELOG.md` for completeness and correctness.

5. Set a version and a date in `CHANGELOG.md` and commit that.
   - The format must be: `## X.Y.Z (YYYY-MM-DD)` (markdown heading).
   - It gets used in the release process.

6. Bump copyright year in `README.md` if appropriate.
   - You don't need to update the version. `./dev-bin/release.sh` does this.

7. Run `./dev-bin/release.sh`
   - This will run tests, update versions, publish to Maven Central, and create
     a GitHub release.

8. This will prompt you several times. Generally you need to say `y` or `n`.

9. You may be prompted about dependency updates. Review them and decide if you
   want to update before releasing.

10. If you get HTTP 401 errors from Central Portal, check your credentials in
    `~/.m2/settings.xml` (or `local.properties`).

11. After completion, a release will be on GitHub and Maven Central.

12. Create a PR to merge the release branch back to main.

## Updating dependencies

Review the versions from the dependency update check. If you want to update:

1. Make a branch
2. Update versions in `gradle/libs.versions.toml`
3. Run `./gradlew :device-sdk:test` and fix any errors
4. Push and ensure CI completes successfully
5. Merge

If you did this in the middle of releasing, start the release process over.
