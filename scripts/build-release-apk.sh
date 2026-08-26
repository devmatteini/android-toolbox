#!/usr/bin/env bash

set -euo pipefail

function usage() {
    echo "usage $(basename "$0") <version>" >&2
    echo >&2
    echo "ARGS:" >&2
    echo "    <version>    release version" >&2
    exit 1
}

if [[ -z ${1:-} ]]; then usage; fi
if [[ -z ${TOOLBOX_KEYSTORE_ENCODED:-} ]]; then
    echo "Missing TOOLBOX_KEYSTORE_ENCODED env variable" >&2
    usage
fi
if [[ -z ${TOOLBOX_KEYSTORE_PASSWORD:-} ]]; then
    echo "Missing TOOLBOX_KEYSTORE_PASSWORD env variable" >&2
    usage
fi
if [[ -z ${TOOLBOX_KEY_ALIAS:-} ]]; then
    echo "Missing TOOLBOX_KEY_ALIAS env variable" >&2
    usage
fi
if [[ -z ${TOOLBOX_KEY_PASSWORD:-} ]]; then
    echo "Missing TOOLBOX_KEY_PASSWORD env variable" >&2
    usage
fi

version="$1"
keystore_file="$PWD/toolbox-keystore.jks"
apk_file="toolbox-$version.apk"

echo "$TOOLBOX_KEYSTORE_ENCODED" | base64 --decode >"$keystore_file"
chmod 600 "$keystore_file"

cat >signing.properties <<EOF
storeFile=$keystore_file
storePassword=$TOOLBOX_KEYSTORE_PASSWORD
keyAlias=$TOOLBOX_KEY_ALIAS
keyPassword=$TOOLBOX_KEY_PASSWORD
EOF

make release >&2

cp app/build/outputs/apk/release/app-release.apk "$apk_file"

echo "$apk_file"
