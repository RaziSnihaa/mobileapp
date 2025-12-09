# EyeScroll (Android)

Application Android (Kotlin) pour scroller automatiquement dans d'autres apps (Instagram, TikTok) en utilisant :
- Eye-tracking (exemple fonctionnel avec ML Kit, notes pour MediaPipe Face Mesh)
- AccessibilityService pour simuler des gestes de scroll
- Contrôle à distance (serveur TCP simple + squelette Bluetooth)

Cible : Android 10+ (API 29+)

Voir `app/` pour le code source.

Build & Run (développement)

1. Ouvrir le projet avec Android Studio.
2. Lancer un build Gradle.
3. Installer sur un appareil Android 10+.

Notes importantes

- Vous devez activer le service d'accessibilité `ScrollAccessibilityService` manuellement dans les paramètres Accessibilité après l'installation pour permettre le scroll global.
- La partie "MediaPipe Face Mesh" est documentée dans les commentaires et peut remplacer l'implémentation ML Kit si vous ajoutez les dépendances MediaPipe appropriées.

MediaPipe (optionnel)

- If you prefer MediaPipe Face Mesh for more accurate gaze/iris landmarks, add MediaPipe Android AARs or use the MediaPipe Solutions Android packages. Typical steps:
	1. Add MediaPipe repositories and AAR dependencies (see MediaPipe docs).
	2. Use a FrameProcessor to get face mesh landmarks and compute gaze vector (iris landmarks available).
	3. Replace the ML Kit processing in `EyeTracker.kt` with the MediaPipe processor callbacks.

Security and permissions

- This project includes a sample TCP server for remote control on port 8080. Do NOT expose this to untrusted networks — add authentication and TLS for any real use.

Enable Accessibility

- After installing the APK, open Settings -> Accessibility and enable the service named `EyeScroll` (or the app's accessibility service) to allow global scrolling.

Building in Android Studio

1. Open the project folder in Android Studio.
2. Sync Gradle; install any missing SDK components.
3. Build and run on a USB-connected device (enable developer mode).

Feedback

If you want, I can:
- swap ML Kit implementation for MediaPipe Face Mesh (requires adding MediaPipe deps),
- implement pairing and Bluetooth control, or
- add a demo client for remote commands.
