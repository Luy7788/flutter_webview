
webview初始化设置
```
import 'package:webview_flutter/webview_flutter.dart';
import 'package:webview_flutter/webview_jsbridge.dart';

final jsBridge = WebViewJSBridge();
final jsVersion = WebViewInjectJsVersion.es5; //默认的js为es5版本


WebView(
          javascriptChannels: jsBridge.jsChannels, //
          userAgent: "xxxxx hxfw Flutter iPhone", //可使用fk_user_agent或看情况自定义
          onWebViewCreated: (controller) {
            jsBridge.controller = controller;
            jsHandle();
          },
          onPageStarted: (url) {
            jsBridge.checkJsBridge().then((isHave) {
              if (isHave == false) {
                jsBridge.injectJs(esVersion: jsVersion);
              }
            });
          },
          onPageFinished: (String url) {
          	 ///预防加载失败措施
            jsBridge.checkJsBridge().then((isHave) {
              if (isHave == false) {
                jsBridge.injectJs(esVersion: jsVersion);
              }
            });
          },
          initialMediaPlaybackPolicy: AutoMediaPlaybackPolicy.always_allow,
          allowsInlineMediaPlayback: widget.isAllowsInlineMediaPlayback ?? true,
          debuggingEnabled: true,
          gestureNavigationEnabled: widget.gestureNavigationEnabled ?? false,
          javascriptMode: JavascriptMode.unrestricted,
          gestureRecognizers: widget.gestureRecognizers ?? [
            Factory<EagerGestureRecognizer>(
              () => EagerGestureRecognizer(),
            ),
          ].toSet(),
        )

```

提供JS调用方法
```
使用jsBridge.registerHandler(方法名,回调)
eg：

jsHandle() {
    jsBridge.registerHandler("openNativePage", (Object? data) async { 
      return Future<Object?>
    }); 
    jsBridge.registerHandler("openLogin", (Object? data) async { 
      return Future<Object?>
    });
}
```

调用JS方法

```
使用jsBridge.callHandler(方法名，data:参数)
eg：

  Future onWebviewClosing() async {
    var isHave = await jsBridge.checkJsBridge();
    if (isHave == true) {
      return jsBridge.callHandler('onWebviewClosing', data: '');
    }
    return 0;
  }
```
