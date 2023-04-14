
判断是否在flutter环境
```
假设以UA内的flutter参数为准，具体看实际情况

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

///监听flutter方法、提供flutter调用使用
function registerFlutterListener(eventName, handler) {
  setupFlutterBridge((flutter) =>
    flutter.registerHandler(eventName, function (res) {
      console.debug(`received event ${eventName}, data=${res}`)
      handler(res)
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
//调用flutter wechatPay方法 
  function wechatPay(res) {
    //……
    var data = res.data
    if (isFlutter()) {
      callFlutterMethod("wechatPay", JSON.stringify(data), (val)=>{
        //返回调起sdk 成功|失败
      });
    } else {
        // xxxx
    }
  }


//注册提供flutter调用方法scrollToTop
  static registerScrollToTopListener(callback) {
    if (isFlutter()) {
      registerFlutterListener('scrollToTop', callback)
    } else {
    	// xxxx
    }
  }
```

