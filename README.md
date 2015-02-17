jetty 9.2.x WebSocket Client patched for Android
================================================

Standard Jetty 9 WebSockets do not run on Android, because it's using Java 1.7 APIs that are not present on Android. This fork patches the troublesome parts for Android and is tested to work with Android 4.1 and above.

WebSockets Secure (wss) was tested to work on Android 5.0 (Jetty 8 wss did not work for us anymore on Android 5.0).

Keep in mind that this is a somewhat experimental set of patches. We use it in production apps, but that does not mean it's going to work for you.

Please use at your own risk and provide feedback.
