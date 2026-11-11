# Dual Lock - Mom Safe (Android / Kotlin)

Native Android project for the Infinix phone use case.

## Intended modes

- **Owner mode:** normal unrestricted phone use after the Owner PIN.
- **Mom mode:** custom limited home with YouTube, Phone and WhatsApp, protected by Android Lock Task Mode.
- **Mom PIN:** returns to the limited home. The Owner PIN is the only in-app route back to normal use.

## Important device requirement

Strong kiosk enforcement requires this app to be provisioned as the Android **Device Owner**. Ordinary screen pinning is not equivalent and can be exited by the user. Device Owner provisioning is normally available only during initial device setup / on a suitably prepared device. Back up the phone before making device-management changes; do not factory-reset without understanding the consequences.

After installing the APK, Device Owner provisioning (where supported) uses:

```sh
adb shell dpm set-device-owner com.momsafe.duallock/.admin.AdminReceiver
```

Then open the app and configure the two PINs.

## Build

GitHub Actions builds a debug APK on pushes to `main`. Download it from the workflow run's **Artifacts** section.

## Current limitations

- This source is an initial implementation; it has not yet been compiled or tested on the target Infinix device.
- The Phone app is resolved from the device's default dialer. YouTube and WhatsApp must be installed.
- This app cannot remove advertisements served inside YouTube itself. It can restrict app switching and non-allowlisted external apps when Device Owner Lock Task Mode is active.
- Android/OEM emergency, power, permission and system surfaces may behave differently on Infinix firmware and need device testing.
