#!/usr/bin/env bash

# Alternative build script using Maven profiles
# Default configuration
DOCKERUSER=grolimundachim
IMAGENAME=csp-report-api

# Parse command line arguments
RELEASE_MODE=false
while [[ $# -gt 0 ]]; do
    case $1 in
        --release)
            RELEASE_MODE=true
            shift
            ;;
        -h|--help)
            echo "Usage: $0 [--release]"
            echo ""
            echo "Options:"
            echo "  --release    Build as release version using Maven release profile"
            echo "  -h, --help   Show this help message"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Get the current version from pom.xml
CURRENT_VERSION=$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)
echo "Current version from pom.xml: $CURRENT_VERSION"

# Process version based on release mode
if [[ "$RELEASE_MODE" == true ]]; then
    # Remove -SNAPSHOT suffix for release builds
    RELEASE_VERSION=${CURRENT_VERSION%-SNAPSHOT}
    DOCKER_TAG=$RELEASE_VERSION
    MAVEN_PROFILES="-Prelease"
    
    echo "Release mode enabled"
    echo "Docker tag will be: $DOCKER_TAG"
    echo "Using Maven profile: release"
else
    # Keep full version for snapshot builds
    DOCKER_TAG=$CURRENT_VERSION
    MAVEN_PROFILES=""
    echo "Snapshot mode (default)"
    echo "Docker tag will be: $DOCKER_TAG"
fi

# Build the application with appropriate profile
echo "Building application with Maven..."
./mvnw clean package $MAVEN_PROFILES -DskipTests

# Check if Maven build was successful
if [ $? -ne 0 ]; then
    echo "Maven build failed!"
    exit 1
fi

# Build the Docker image
echo "Building Docker image: $DOCKERUSER/$IMAGENAME:$DOCKER_TAG"
docker build -t "$DOCKERUSER/$IMAGENAME:$DOCKER_TAG" .

# Check if Docker build was successful
if [ $? -ne 0 ]; then
    echo "Docker build failed!"
    exit 1
fi

# Push the Docker image
echo "Pushing Docker image: $DOCKERUSER/$IMAGENAME:$DOCKER_TAG"
docker push "$DOCKERUSER/$IMAGENAME:$DOCKER_TAG"

# Check if push was successful
if [ $? -ne 0 ]; then
    echo "Docker push failed!"
    exit 1
fi

# Only increment version for release builds
if [[ "$RELEASE_MODE" == true ]]; then
    echo "Release build successful, incrementing patch version..."
    
    # Parse version components (major.minor.patch)
    IFS='.' read -r MAJOR MINOR PATCH <<< "$RELEASE_VERSION"
    
    # Increment patch version
    NEW_PATCH=$((PATCH + 1))
    NEW_VERSION="$MAJOR.$MINOR.$NEW_PATCH-SNAPSHOT"
    
    echo "Updating version from $CURRENT_VERSION to $NEW_VERSION"
    
    # Update pom.xml with new version
    ./mvnw versions:set -DnewVersion="$NEW_VERSION" -DgenerateBackupPoms=false
    
    if [ $? -eq 0 ]; then
        echo "Successfully updated pom.xml to version: $NEW_VERSION"
        echo "Next development cycle: $NEW_VERSION"
    else
        echo "Failed to update pom.xml version!"
        exit 1
    fi
fi

echo "Build process completed successfully!"