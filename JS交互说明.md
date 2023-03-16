
判断是否在flutter环境
```
function isFlutter() {
  return /(Flutter|flutter)/i.test(navigator.userAgent)
}
```

与flutter方法交互
```
实现下面方法

///调用flutter方法
function callFlutterMethod(methodName, params, successCallback) {
  setupFlutterBridge((bridge) =>
    bridge.callHandler(methodName, params, function(res) {
      successCallback(res)
    })
  )
}

///监听flutter方法
function registerFlutterListener(eventName, handler) {
  setupFlutterBridge((flutter) =>
    flutter.registerHandler('eventName', function(res) {
      console.debug(`received event ${eventName}, data=${res}`)
      handler(res ? JSON.parse(res.toString()) : {})
    })
  )
}

function setupFlutterBridge(callback) {
  if (window.flutterJsBridge == null) {
    setTimeout(() => {
      setupFlutterBridge(callback)
    }, 500)
  } else {
    callback(window.flutterJsBridge)
  }
}
```

eg：
```
  function wechatPay(res) {
    //……
    var data = res.data
    if (isFlutter()) {
      callFlutterMethod("dunhuangpay", JSON.stringify(data), (val)=>{
        //返回调起sdk成功|失败
      });
    }
  }

```