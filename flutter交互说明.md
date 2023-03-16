
webview设置
```
final jsBridge = WebViewJSBridge();

WebView(
          javascriptChannels: jsBridge.jsChannels,
          userAgent: " hxfw Flutter iPhone",
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
          onPageStarted: (url) {
            jsBridge.checkJsBridge().then((isHave) {
              if (isHave == false) {
                jsBridge.injectJs(esVersion: jsVersion);
              }
            });
          },
          onWebViewCreated: (controller) {
            jsBridge.controller = controller;
            jsHandle();
          },
          onPageFinished: (String url) {
            jsBridge.checkJsBridge().then((isHave) {
              if (isHave == false) {
                jsBridge.injectJs(esVersion: jsVersion);
              }
            });
          },
        )

```

接收JS方法
```
使用jsBridge.registerHandler
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
使用jsBridge.callHandler
eg：
  Future onWebviewClosing() async {
    var isHave = await jsBridge.checkJsBridge();
    if (isHave == true) {
      return jsBridge.callHandler('onWebviewClosing', data: '');
    }
    return 0;
  }
```
