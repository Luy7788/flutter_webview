# WebView for Flutter

基于官方插件2.0.13版本进行维护，修改多个问题，提供选相册、窗口切换全屏播放视频等

### JS交互实现代码说明:
 [JS交互说明.md](./JS交互说明.md)
 
 ps：两者交互尽量用jsonString

### flutter交互实现说明:
 [Flutter交互说明.md](./Flutter交互说明.md)
 
 
#### WebViewController提供新的方法

```
  ///截图，支持安卓、iOS
  Future<Uint8List?> takeScreenshot(
      {ScreenshotConfiguration? screenshotConfiguration}) async {
    return await _webViewPlatformController.takeScreenshot(screenshotConfiguration: screenshotConfiguration);
  }
  
  ///设置蒙版，只支持iOS
  Future setupVisualEffect(bool enable, {bool? isDark, double? alpha}) {
    return _webViewPlatformController.setupVisualEffect(enable, isDark:isDark, alpha:alpha);
  }

  ///设置交互响应，预防h5事件穿透，只支持iOS
  Future setupUserAction(bool enable) {
    return _webViewPlatformController.setupUserAction(enable);
  }

``` 
 
## plugin

A Flutter plugin that provides a WebView widget.

On iOS the WebView widget is backed by a [WKWebView](https://developer.apple.com/documentation/webkit/wkwebview);
On Android the WebView widget is backed by a [WebView](https://developer.android.com/reference/android/webkit/WebView).

## Usage
Add `webview_flutter` as a [dependency in your pubspec.yaml file](https://flutter.dev/docs/development/platform-integration/platform-channels). If you are targeting Android, make sure to read the *Android Platform Views* section below to choose the platform view mode that best suits your needs.

You can now include a WebView widget in your widget tree. See the
[WebView](https://pub.dev/documentation/webview_flutter/latest/webview_flutter/WebView-class.html)
widget's Dartdoc for more details on how to use the widget.

## Android Platform Views
The WebView is relying on
[Platform Views](https://flutter.dev/docs/development/platform-integration/platform-views) to embed
the Android’s webview within the Flutter app. It supports two modes: *Virtual displays* (the current default) and *Hybrid composition*.

Here are some points to consider when choosing between the two:

* *Hybrid composition* mode has a built-in keyboard support while *Virtual displays* mode has multiple
[keyboard issues](https://github.com/flutter/flutter/issues?q=is%3Aopen+label%3Avd-only+label%3A%22p%3A+webview-keyboard%22)
* *Hybrid composition* mode requires Android SKD 19+ while *Virtual displays* mode requires Android SDK 20+
* *Hybrid composition* mode has [performence limitations](https://flutter.dev/docs/development/platform-integration/platform-views#performance) when working on Android versions prior to Android 10 while *Virtual displays* is performant on all supported Android versions 

|                             | Hybrid composition  | Virtual displays |
| --------------------------- | ------------------- | ---------------- |
| **Full keyboard supoport**  | yes                 | no               |
| **Android SDK support**     | 19+                 | 20+              |
| **Full performance**        | Android 10+         | always           |
| **The default**             | no                  | yes              |

### Using Virtual displays

The mode is currently enabled by default. You should however make sure to set the correct `minSdkVersion` in `android/app/build.gradle` (if it was previously lower than 20):

```groovy
android {
    defaultConfig {
        minSdkVersion 20
    }
}
```


### Using Hybrid Composition

1. Set the correct `minSdkVersion` in `android/app/build.gradle` (if it was previously lower than 19):

    ```groovy
    android {
        defaultConfig {
            minSdkVersion 19
        }
    }
    ```

2. Set `WebView.platform = SurfaceAndroidWebView();` in `initState()`.
    For example:
    
    ```dart
    import 'dart:io';
    
    import 'package:webview_flutter/webview_flutter.dart';

    class WebViewExample extends StatefulWidget {
      @override
      WebViewExampleState createState() => WebViewExampleState();
    }
    
    class WebViewExampleState extends State<WebViewExample> {
      @override
      void initState() {
        super.initState();
            // Enable hybrid composition.
    if (Platform.isAndroid) WebView.platform = SurfaceAndroidWebView();
      }

      @override
      Widget build(BuildContext context) {
        return WebView(
          initialUrl: 'https://flutter.dev',
        );
      }
    }
    ```

### Enable Material Components for Android

To use Material Components when the user interacts with input elements in the WebView,
follow the steps described in the [Enabling Material Components instructions](https://flutter.dev/docs/deployment/android#enabling-material-components).
