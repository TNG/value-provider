#!/bin/bash

set -e

echo "Beginning"
while [[ -n "${1-}" ]] ; do
  case "${1}" in
    --repository-username*)
      SONATYPE_USERNAME=${1#*=}
      ;;
    --repository-password*)
      SONATYPE_PASSWORD=${1#*=}
      ;;
    --signing-key*)
      GPG_SIGNING_KEY=${1#*=}
      ;;
    --signing-password*)
      GPG_SIGNING_PASSWORD=${1#*=}
      ;;
    *)
      echo "Unknown option '${1}'"
      exit 1
      ;;
  esac
  shift
done

echo Building and testing...
./gradlew build

echo Publishing to Sonatype, closing repository, checking uploaded artifacts and releasing repository...
./gradlew publishToSonatype closeSonatypeStagingRepository releaseSonatypeStagingRepository --no-parallel -PsonatypeUsername="$SONATYPE_USERNAME" -PsonatypePassword="$SONATYPE_PASSWORD" -PsigningKey="$GPG_SIGNING_KEY" -PsigningPassword="$GPG_SIGNING_PASSWORD"
