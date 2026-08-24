# Play Store release checklist

## Product

- Choose a unique final title after checking Play Store availability.
- Replace the provisional package `com.shivayogih.packmate` if necessary before first release.
- Add onboarding/help text and support contact information.
- Test date handling, rotation, process death, backup/restore, and low-storage behavior.
- Add accessibility checks for TalkBack, font scaling, touch targets, and contrast.

## Store assets

- 512×512 high-resolution icon.
- Feature graphic.
- Phone screenshots; add tablet screenshots if tablet support is claimed.
- Short and full descriptions.
- Category, contact email, website, and hosted privacy-policy URL.

## Policy and privacy

- Revalidate `PRIVACY_POLICY.md` against the final implementation.
- Complete the Data safety form accurately.
- Keep the no-permission/no-network claim only while it remains true.
- Add a consent flow before introducing analytics, advertising, or cloud sync.

## Release engineering

- Create an upload keystore and keep it outside source control.
- Add secure signing configuration through environment variables or CI secrets.
- Run `testReleaseUnitTest`, `lintRelease`, and `bundleRelease`.
- Inspect the release bundle with Android Studio APK Analyzer.
- Upload first to Internal testing, then Closed testing.
- Enable Play App Signing and staged rollout.

## Suggested next product increments

- Edit trip metadata.
- Reorder packing items.
- Custom reusable templates.
- Export/share checklist.
- Home-screen widget.
- Optional encrypted cloud sync, added only with a clear privacy model.
