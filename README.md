Jetty 9.2.x WebSocket Client for Android
========================================

Standard Jetty 9 WebSockets do not run on Android, because it's using Java 1.7 APIs that are not present on Android. This fork patches the troublesome parts for Android and is tested to work with Android 4.1 and above.

WebSockets Secure (wss) was tested to work on Android 5.0. Jetty 8 could not establish the wss connection on Android 5.0. It did not throw, we just saw a System.err log "WARN:oeji.nio:javax.net.ssl.SSLHandshakeException: Handshake failed". Digging deeper, the causing exception looked something like this: "javax.net.ssl.SSLProtocolException: SSL handshake terminated: ssl=0x9a5f7e00: Failure in SSL library, usually a protocol error error: ...". Ouch, we hit some lower layer based on OpenSSL (openssl/ssl/s3_clnt.c)...

Keep in mind that this is a somewhat experimental set of patches. We use it in production apps, but that does not mean it's going to work for you.

Please use at your own risk and provide feedback.

API Documentation
-----------------
[Jetty Websocket API](http://www.eclipse.org/jetty/documentation/9.2.7.v20150116/websocket-jetty.html)

Install and use patched modules
-------------------------------
The following modules were patched:

 * jetty-util
 * jetty-websocket/websocket-common
 * jetty-websocket/websocket-client

Install or deploy those modules (their artifact ids have an "-android" postfix) using Maven. Now, you're good to use websocket-client-android in your Android project. Put a dependency in your Gradle build:

```
compile 'org.eclipse.jetty.websocket:websocket-client-android:9.2.8-SNAPSHOT'
```

ProGuard configuration
----------------------

```
-keep public class * implements org.eclipse.jetty.websocket.api.extensions.Extension;
-dontwarn org.eclipse.jetty.**
```