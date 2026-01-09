#!/bin/bash

set -eu -o pipefail

# Pre-flight checks - verify all required tools are available and configured
# before making any changes to the repository

check_command() {
    if ! command -v "$1" &>/dev/null; then
        echo "Error: $1 is not installed or not in PATH"
        exit 1
    fi
}

# Verify gh CLI is authenticated
if ! gh auth status &>/dev/null; then
    echo "Error: gh CLI is not authenticated. Run 'gh auth login' first."
    exit 1
fi

# Verify we can access this repository via gh
if ! gh repo view --json name &>/dev/null; then
    echo "Error: Cannot access repository via gh. Check your authentication and repository access."
    exit 1
fi

# Verify git can connect to the remote (catches SSH key issues, etc.)
if ! git ls-remote origin &>/dev/null; then
    echo "Error: Cannot connect to git remote. Check your git credentials/SSH keys."
    exit 1
fi

check_command perl
check_command ./gradlew
check_command mise

# Ensure mise is activated and all tools from mise.toml are installed
if ! mise current &>/dev/null; then
    echo "Error: mise is not activated in your shell."
    echo "Run 'eval \"\$(mise activate bash)\"' or add it to your shell config."
    exit 1
fi
mise install --quiet

# Check that we're not on the main branch
current_branch=$(git branch --show-current)
if [ "$current_branch" = "main" ]; then
    echo "Error: Releases should not be done directly on the main branch."
    echo "Please create a release branch and run this script from there."
    exit 1
fi

# Fetch latest changes and check that we're not behind origin/main
echo "Fetching from origin..."
git fetch origin

if ! git merge-base --is-ancestor origin/main HEAD; then
    echo "Error: Current branch is behind origin/main."
    echo "Please merge or rebase with origin/main before releasing."
    exit 1
fi

changelog=$(cat CHANGELOG.md)

# Parse changelog in format: ## X.Y.Z (YYYY-MM-DD)
regex='## ([0-9]+\.[0-9]+\.[0-9]+[a-zA-Z0-9\-]*) \(([0-9]{4}-[0-9]{2}-[0-9]{2})\)

((.|
)*)'

if [[ ! $changelog =~ $regex ]]; then
    echo "Could not find date line in change log!"
    echo "Expected format: ## X.Y.Z (YYYY-MM-DD)"
    exit 1
fi

version="${BASH_REMATCH[1]}"
date="${BASH_REMATCH[2]}"
# Extract notes until the next version header or end of file
notes="$(echo "${BASH_REMATCH[3]}" | sed -n -e '/^## [0-9]\+\.[0-9]\+\.[0-9]\+/,$!p')"

if [[ "$date" != "$(date +"%Y-%m-%d")" ]]; then
    echo "$date is not today!"
    exit 1
fi

tag="v$version"

if [ -n "$(git status --porcelain)" ]; then
    echo ". is not clean." >&2
    exit 1
fi

# Check for dependency updates
# Note: --no-parallel is required for Gradle 9+ compatibility
echo ""
echo "Checking for dependency updates..."
./gradlew dependencyUpdates -Drevision=release --no-parallel --no-daemon

read -r -n 1 -p "Continue given above dependencies? (y/n) " should_continue
echo ""

if [ "$should_continue" != "y" ]; then
    echo "Aborting"
    exit 1
fi

# Run tests
echo ""
echo "Running tests..."
./gradlew :device-sdk:test --no-daemon

read -r -n 1 -p "Continue given above tests? (y/n) " should_continue
echo ""

if [ "$should_continue" != "y" ]; then
    echo "Aborting"
    exit 1
fi

# Update version in build.gradle.kts
echo ""
echo "Updating version to $version..."
perl -pi -e "s/version = \"[^\"]+\"/version = \"$version\"/" build.gradle.kts

# Update version in README.md
perl -pi -e "s/com\.maxmind\.device:device-sdk:[0-9]+\.[0-9]+\.[0-9]+[a-zA-Z0-9\-]*/com.maxmind.device:device-sdk:$version/g" README.md

git diff

read -r -n 1 -p "Commit changes? (y/n) " should_commit
echo ""
if [ "$should_commit" != "y" ]; then
    echo "Aborting"
    exit 1
fi

git add build.gradle.kts README.md
git commit -m "Preparing for $version"

# Build and publish to Maven Central
echo ""
echo "Building and publishing to Maven Central..."

# Read credentials from ~/.m2/settings.xml and export as env vars
# (Gradle properties from settings.xml don't work with Vanniktech plugin)
if [ -z "${ORG_GRADLE_PROJECT_mavenCentralUsername:-}" ]; then
    settings_xml="$HOME/.m2/settings.xml"
    if [ -f "$settings_xml" ]; then
        # Extract username and password for server id "central" using yq
        # The [.settings.servers.server] | flatten handles both single server (object) and multiple servers (array)
        maven_username=$(yq -p xml -oy '[.settings.servers.server] | flatten | .[] | select(.id == "central") | .username' "$settings_xml" 2>/dev/null)
        maven_password=$(yq -p xml -oy '[.settings.servers.server] | flatten | .[] | select(.id == "central") | .password' "$settings_xml" 2>/dev/null)

        if [ -n "$maven_username" ] && [ -n "$maven_password" ]; then
            export ORG_GRADLE_PROJECT_mavenCentralUsername="$maven_username"
            export ORG_GRADLE_PROJECT_mavenCentralPassword="$maven_password"
            echo "Using Maven Central credentials from ~/.m2/settings.xml"
        else
            echo "Error: Maven Central credentials not found in ~/.m2/settings.xml (server id 'central')"
            exit 1
        fi
    else
        echo "Error: ~/.m2/settings.xml not found and ORG_GRADLE_PROJECT_mavenCentralUsername not set"
        exit 1
    fi
fi

./gradlew :device-sdk:publishAndReleaseToMavenCentral --no-daemon

echo ""
echo "Release notes for $version:"
echo ""
echo "$notes"
echo ""

read -r -n 1 -p "Push to origin? (y/n) " should_push
echo ""

if [ "$should_push" != "y" ]; then
    echo "Aborting"
    exit 1
fi

git push

# Create GitHub release with artifacts
echo ""
echo "Creating GitHub release..."

# Build outputs location
build_dir="device-sdk/build/outputs/aar"

# Clean up any previous release artifacts
rm -f device-sdk-*.aar device-sdk-*.jar device-sdk-*.asc 2>/dev/null || true

# Collect release artifacts
release_files=()

# AAR file
aar_file="$build_dir/device-sdk-release.aar"
if [ -f "$aar_file" ]; then
    cp "$aar_file" "device-sdk-$version.aar"
    release_files+=("device-sdk-$version.aar")
fi

# Sources JAR (from publications)
sources_jar=$(find device-sdk/build -name "*-sources.jar" -type f 2>/dev/null | head -1)
if [ -n "$sources_jar" ]; then
    cp "$sources_jar" "device-sdk-$version-sources.jar"
    release_files+=("device-sdk-$version-sources.jar")
fi

# Javadoc JAR
javadoc_jar=$(find device-sdk/build -name "*-javadoc.jar" -type f 2>/dev/null | head -1)
if [ -n "$javadoc_jar" ]; then
    cp "$javadoc_jar" "device-sdk-$version-javadoc.jar"
    release_files+=("device-sdk-$version-javadoc.jar")
fi

# Sign artifacts for GitHub release
for file in "${release_files[@]}"; do
    gpg --armor --detach-sign "$file"
done

# Add signature files to release
for file in "${release_files[@]}"; do
    if [ -f "$file.asc" ]; then
        release_files+=("$file.asc")
    fi
done

gh release create --target "$(git branch --show-current)" -t "$version" -n "$notes" "$tag" "${release_files[@]}"

# Cleanup temporary files
for file in device-sdk-*.aar device-sdk-*.jar device-sdk-*.asc; do
    rm -f "$file" 2>/dev/null || true
done

echo ""
echo "Release $version complete!"
echo "GitHub release: https://github.com/maxmind/device-android/releases/tag/$tag"
echo "Maven Central: https://central.sonatype.com/artifact/com.maxmind.device/device-sdk/$version"
