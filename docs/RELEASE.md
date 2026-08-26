# Release

Steps to create a new release of `Toolbox`:

1. Pull remote changes with `git pull --rebase`.
2. Compile and run tests with `make check`.
3. Make sure [README.md](../README.md) is updated.
4. Bump the version in `app/build.gradle.kts`.
   - Increment `versionCode` by 1.
   - Set `versionName` to the new Semver version.
5. Run `git add app/build.gradle.kts && git commit -m "release <version>"`.
6. Push to remote with `git push`.
7. Create a signed tag with `git tag -s <version> -m "release <version>"`.
8. Wait for CI to pass, then push the tag with `git push --tags`.
9. Wait for the release workflow to finish. It creates the GitHub release and uploads the signed APK.

The release commit message becomes the GitHub release notes.
