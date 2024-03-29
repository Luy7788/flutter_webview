// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.webviewflutter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.flutter.Log;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.platform.PlatformView;

public class FlutterWebView implements PlatformView, MethodCallHandler {

  private static final String JS_CHANNEL_NAMES_FIELD = "javascriptChannelNames";
  private final WebView webView;
  private final MethodChannel methodChannel;
  private final FlutterWebViewClient flutterWebViewClient;
  private final Handler platformThreadHandler;

    //修复视频全屏问题
    private Activity activity;
    private FlutterWebChromeClient mWebChromeClient;

  // Verifies that a url opened by `Window.open` has a secure url.
  private class FlutterWebChromeClient extends WebChromeClient {

      ///视频问题
      private View mCustomView;
      private CustomViewCallback mCustomViewCallback;
      private WebView webview;
      private int normalSystemUiVisibility;
      private Activity activity ;

      public void setActivity(Activity activity) {
          this.activity = activity;
      }

      public void setWebView(WebView webview) {
          this.webview = webview;
      }

      //MARK:修改插件
      @Override
      public void onShowCustomView(View view, CustomViewCallback callback) {
          Log.d("webviewtest", "onShowCustomView: activity :" + (activity == null));
          if (mCustomView != null || activity == null) {
              onHideCustomView();
              return;
          }
          mCustomView = view;
          mCustomView.setBackgroundColor(0xff000000);
          mCustomViewCallback = callback;
          FrameLayout decorView = (FrameLayout)activity.getWindow().getDecorView();
          normalSystemUiVisibility = decorView.getWindowSystemUiVisibility();
          Log.d("webviewtest", "show normalSystemUiVisibility: " + normalSystemUiVisibility);
          decorView.addView(this.mCustomView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
          webview.setVisibility(View.GONE);
          decorView.setSystemUiVisibility(
                  View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                          // Set the content to appear under the system bars so that the
                          // content doesn't resize when the system bars hide and show.
                          | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                          | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                          | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                          // Hide the nav bar and status bar
                          | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                          | View.SYSTEM_UI_FLAG_FULLSCREEN);
          super.onShowCustomView(view, callback);
      }

      public void onHideCustomView() {
          Log.d("webviewtest", "onHideCustomView: ");
          webview.setVisibility(View.VISIBLE);
          if (mCustomView == null  ||activity == null) {
              return;
          }

          mCustomView.setVisibility(View.GONE);

          FrameLayout decorView = (FrameLayout) activity.getWindow().getDecorView();
          decorView.removeView(mCustomView);
          mCustomViewCallback.onCustomViewHidden();
          mCustomView = null;
          super.onHideCustomView();
          decorView.setSystemUiVisibility(normalSystemUiVisibility);
          Log.d("webviewtest", "hide normalSystemUiVisibility: " + decorView.getSystemUiVisibility());
      }

      @Override
    public boolean onCreateWindow(
        final WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
      final WebViewClient webViewClient =
          new WebViewClient() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(
                @NonNull WebView view, @NonNull WebResourceRequest request) {
              final String url = request.getUrl().toString();
              if (!flutterWebViewClient.shouldOverrideUrlLoading(
                  FlutterWebView.this.webView, request)) {
                webView.loadUrl(url);
              }
              return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
              if (!flutterWebViewClient.shouldOverrideUrlLoading(
                  FlutterWebView.this.webView, url)) {
                webView.loadUrl(url);
              }
              return true;
            }
          };

      final WebView newWebView = new WebView(view.getContext());
      newWebView.setWebViewClient(webViewClient);

      final WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
      transport.setWebView(newWebView);
      resultMsg.sendToTarget();

      return true;
    }

    @Override
    public boolean onShowFileChooser(
            WebView webView,
            ValueCallback<Uri[]> filePathCallback,
            FileChooserParams fileChooserParams) {
      // info as of 2021-03-08:
      // don't use fileChooserParams.getTitle() as it is (always? on Mi 9T Pro Android 10 at least) null
      // don't use fileChooserParams.isCaptureEnabled() as it is (always? on Mi 9T Pro Android 10 at least) false, even when the file upload allows images or any file
      final Context context = webView.getContext();
      final boolean allowMultipleFiles =
              Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                      && fileChooserParams.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE;
      final String[] acceptTypes =
              Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                      ? fileChooserParams.getAcceptTypes()
                      : new String[0];
      new FileChooserLauncher(context, allowMultipleFiles, filePathCallback, acceptTypes).start();
      return true;
    }

    @Override
    public void onProgressChanged(WebView view, int progress) {
      flutterWebViewClient.onLoadingProgress(progress);
    }
  }

    public void setActivity(Activity activity) {
        this.activity = activity;
        mWebChromeClient.setActivity(activity);
    }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
  @SuppressWarnings("unchecked")
  FlutterWebView(
      final Context context,
      MethodChannel methodChannel,
      Map<String, Object> params,
      View containerView) {

    DisplayListenerProxy displayListenerProxy = new DisplayListenerProxy();
    DisplayManager displayManager =
        (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
    displayListenerProxy.onPreWebViewInitialization(displayManager);

    this.methodChannel = methodChannel;
    this.methodChannel.setMethodCallHandler(this);
    flutterWebViewClient = new FlutterWebViewClient(methodChannel);


      mWebChromeClient = new FlutterWebChromeClient();
    FlutterDownloadListener flutterDownloadListener =
        new FlutterDownloadListener(flutterWebViewClient);
    webView =
        createWebView(
            new WebViewBuilder(context, containerView),
            params,
                mWebChromeClient,
            flutterDownloadListener);

      mWebChromeClient.setWebView(webView);
      mWebChromeClient.setActivity(activity);

    flutterDownloadListener.setWebView(webView);

    displayListenerProxy.onPostWebViewInitialization(displayManager);

    platformThreadHandler = new Handler(context.getMainLooper());

    Map<String, Object> settings = (Map<String, Object>) params.get("settings");
    if (settings != null) {
      applySettings(settings);
    }

    if (params.containsKey(JS_CHANNEL_NAMES_FIELD)) {
      List<String> names = (List<String>) params.get(JS_CHANNEL_NAMES_FIELD);
      if (names != null) {
        registerJavaScriptChannelNames(names);
      }
    }

    Integer autoMediaPlaybackPolicy = (Integer) params.get("autoMediaPlaybackPolicy");
    if (autoMediaPlaybackPolicy != null) {
      updateAutoMediaPlaybackPolicy(autoMediaPlaybackPolicy);
    }
    if (params.containsKey("userAgent")) {
      String userAgent = (String) params.get("userAgent");
      updateUserAgent(userAgent);
    }
    if (params.containsKey("initialUrl")) {
      String url = (String) params.get("initialUrl");
      if (params.containsKey("initialAssetFile")) {
          String file = (String) params.get("initialAssetFile");
          if (file.length() > 0) {
              url = "file:///android_asset/flutter_assets/" + file;
          }
      }
      webView.loadUrl(url);
    }
  }

  /**
   * Creates a {@link android.webkit.WebView} and configures it according to the supplied
   * parameters.
   *
   * <p>The {@link WebView} is configured with the following predefined settings:
   *
   * <ul>
   *   <li>always enable the DOM storage API;
   *   <li>always allow JavaScript to automatically open windows;
   *   <li>always allow support for multiple windows;
   *   <li>always use the {@link FlutterWebChromeClient} as web Chrome client.
   * </ul>
   *
   * <p><strong>Important:</strong> This method is visible for testing purposes only and should
   * never be called from outside this class.
   *
   * @param webViewBuilder a {@link WebViewBuilder} which is responsible for building the {@link
   *     WebView}.
   * @param params creation parameters received over the method channel.
   * @param webChromeClient an implementation of WebChromeClient This value may be null.
   * @return The new {@link android.webkit.WebView} object.
   */
  @VisibleForTesting
  static WebView createWebView(
      WebViewBuilder webViewBuilder,
      Map<String, Object> params,
      WebChromeClient webChromeClient,
      @Nullable DownloadListener downloadListener) {
    boolean usesHybridComposition = Boolean.TRUE.equals(params.get("usesHybridComposition"));
    webViewBuilder
        .setUsesHybridComposition(usesHybridComposition)
        .setDomStorageEnabled(true) // Always enable DOM storage API.
        .setJavaScriptCanOpenWindowsAutomatically(
            true) // Always allow automatically opening of windows.
        .setSupportMultipleWindows(true) // Always support multiple windows.
        .setWebChromeClient(webChromeClient)
        .setDownloadListener(
            downloadListener); // Always use {@link FlutterWebChromeClient} as web Chrome client.

    return webViewBuilder.build();
  }

  @Override
  public View getView() {
    return webView;
  }

  // @Override
  // This is overriding a method that hasn't rolled into stable Flutter yet. Including the
  // annotation would cause compile time failures in versions of Flutter too old to include the new
  // method. However leaving it raw like this means that the method will be ignored in old versions
  // of Flutter but used as an override anyway wherever it's actually defined.
  // TODO(mklim): Add the @Override annotation once flutter/engine#9727 rolls to stable.
  public void onInputConnectionUnlocked() {
    if (webView instanceof InputAwareWebView) {
      ((InputAwareWebView) webView).unlockInputConnection();
    }
  }

  // @Override
  // This is overriding a method that hasn't rolled into stable Flutter yet. Including the
  // annotation would cause compile time failures in versions of Flutter too old to include the new
  // method. However leaving it raw like this means that the method will be ignored in old versions
  // of Flutter but used as an override anyway wherever it's actually defined.
  // TODO(mklim): Add the @Override annotation once flutter/engine#9727 rolls to stable.
  public void onInputConnectionLocked() {
    if (webView instanceof InputAwareWebView) {
      ((InputAwareWebView) webView).lockInputConnection();
    }
  }

  // @Override
  // This is overriding a method that hasn't rolled into stable Flutter yet. Including the
  // annotation would cause compile time failures in versions of Flutter too old to include the new
  // method. However leaving it raw like this means that the method will be ignored in old versions
  // of Flutter but used as an override anyway wherever it's actually defined.
  // TODO(mklim): Add the @Override annotation once stable passes v1.10.9.
  public void onFlutterViewAttached(View flutterView) {
    if (webView instanceof InputAwareWebView) {
      ((InputAwareWebView) webView).setContainerView(flutterView);
    }
  }

  // @Override
  // This is overriding a method that hasn't rolled into stable Flutter yet. Including the
  // annotation would cause compile time failures in versions of Flutter too old to include the new
  // method. However leaving it raw like this means that the method will be ignored in old versions
  // of Flutter but used as an override anyway wherever it's actually defined.
  // TODO(mklim): Add the @Override annotation once stable passes v1.10.9.
  public void onFlutterViewDetached() {
    if (webView instanceof InputAwareWebView) {
      ((InputAwareWebView) webView).setContainerView(null);
    }
  }

  @Override
  public void onMethodCall(MethodCall methodCall, Result result) {
    switch (methodCall.method) {
      case "loadUrl":
        loadUrl(methodCall, result);
        break;
      case "updateSettings":
        updateSettings(methodCall, result);
        break;
      case "canGoBack":
        canGoBack(result);
        break;
      case "canGoForward":
        canGoForward(result);
        break;
      case "goBack":
        goBack(result);
        break;
      case "goForward":
        goForward(result);
        break;
      case "reload":
        reload(result);
        break;
      case "currentUrl":
        currentUrl(result);
        break;
      case "evaluateJavascript":
        evaluateJavaScript(methodCall, result);
        break;
      case "addJavascriptChannels":
        addJavaScriptChannels(methodCall, result);
        break;
      case "removeJavascriptChannels":
        removeJavaScriptChannels(methodCall, result);
        break;
      case "clearCache":
        clearCache(result);
        break;
      case "getTitle":
        getTitle(result);
        break;
      case "scrollTo":
        scrollTo(methodCall, result);
        break;
      case "scrollBy":
        scrollBy(methodCall, result);
        break;
      case "getScrollX":
        getScrollX(result);
        break;
      case "getScrollY":
        getScrollY(result);
        break;
      case "takeScreenshot":
          takeScreenshot(methodCall, result);
        break;
      default:
        result.notImplemented();
    }
  }
  public Handler mainLooperHandler;
  public void takeScreenshot(MethodCall call, final Result result) {
      final Map<String, Object> screenshotConfiguration = (Map<String, Object>) call.argument("screenshotConfiguration");
      final Context context = webView.getContext();
      final float pixelDensity = context.getResources().getDisplayMetrics().density;
      if (mainLooperHandler == null) {
          mainLooperHandler = new Handler(context.getMainLooper());
      }
      mainLooperHandler.post(new Runnable(){
          @Override
          public void run() {
              try {
                  Bitmap screenshotBitmap = Bitmap.createBitmap(webView.getMeasuredWidth(), webView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                  Canvas canvas = new Canvas(screenshotBitmap);
                  canvas.translate(-webView.getScrollX(), -webView.getScrollY());
                  webView.draw(canvas);
                  ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                  Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.PNG;
                  int quality = 100;

                  if (screenshotConfiguration != null) {
                      Map<String, Double> rect = (Map<String, Double>) screenshotConfiguration.get("rect");
                      if (rect != null) {
                          int rectX = (int) Math.floor(rect.get("x") * pixelDensity + 0.5);
                          int rectY = (int) Math.floor(rect.get("y") * pixelDensity + 0.5);
                          int rectWidth = Math.min(screenshotBitmap.getWidth(), (int) Math.floor(rect.get("width") * pixelDensity + 0.5));
                          int rectHeight = Math.min(screenshotBitmap.getHeight(), (int) Math.floor(rect.get("height") * pixelDensity + 0.5));
                          screenshotBitmap = Bitmap.createBitmap(
                                  screenshotBitmap,
                                  rectX,
                                  rectY,
                                  rectWidth,
                                  rectHeight);
                      }

                      Double snapshotWidth = (Double) screenshotConfiguration.get("snapshotWidth");
                      if (snapshotWidth != null) {
                          int dstWidth = (int) Math.floor(snapshotWidth * pixelDensity + 0.5);
                          float ratioBitmap = (float) screenshotBitmap.getWidth() / (float) screenshotBitmap.getHeight();
                          int dstHeight = (int) ((float) dstWidth / ratioBitmap);
                          screenshotBitmap = Bitmap.createScaledBitmap(screenshotBitmap, dstWidth, dstHeight, true);
                      }

                      try {
                          compressFormat = Bitmap.CompressFormat.valueOf((String) screenshotConfiguration.get("compressFormat"));
                      } catch (IllegalArgumentException e) {
                          e.printStackTrace();
                      }

                      quality = (Integer) screenshotConfiguration.get("quality");
                  }

                  screenshotBitmap.compress(
                          compressFormat,
                          quality,
                          byteArrayOutputStream);
                  try {
                      byteArrayOutputStream.close();
                  } catch (IOException e) {
                      e.printStackTrace();
                  }
                  screenshotBitmap.recycle();
                  result.success(byteArrayOutputStream.toByteArray());
              } catch (IllegalArgumentException e) {
                  e.printStackTrace();
                  result.success(null);
              }
          }
      });
  }

  @SuppressWarnings("unchecked")
  private void loadUrl(MethodCall methodCall, Result result) {
    Map<String, Object> request = (Map<String, Object>) methodCall.arguments;
    String url = (String) request.get("url");
    Map<String, String> headers = (Map<String, String>) request.get("headers");
    if (headers == null) {
      headers = Collections.emptyMap();
    }
    webView.loadUrl(url, headers);
    result.success(null);
  }

  private void canGoBack(Result result) {
    result.success(webView.canGoBack());
  }

  private void canGoForward(Result result) {
    result.success(webView.canGoForward());
  }

  private void goBack(Result result) {
    if (webView.canGoBack()) {
      webView.goBack();
    }
    result.success(null);
  }

  private void goForward(Result result) {
    if (webView.canGoForward()) {
      webView.goForward();
    }
    result.success(null);
  }

  private void reload(Result result) {
    webView.reload();
    result.success(null);
  }

  private void currentUrl(Result result) {
    result.success(webView.getUrl());
  }

  @SuppressWarnings("unchecked")
  private void updateSettings(MethodCall methodCall, Result result) {
    applySettings((Map<String, Object>) methodCall.arguments);
    result.success(null);
  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
  private void evaluateJavaScript(MethodCall methodCall, final Result result) {
    String jsString = (String) methodCall.arguments;
    if (jsString == null) {
      throw new UnsupportedOperationException("JavaScript string cannot be null");
    }
    webView.evaluateJavascript(
        jsString,
        new android.webkit.ValueCallback<String>() {
          @Override
          public void onReceiveValue(String value) {
            result.success(value);
          }
        });
  }

  @SuppressWarnings("unchecked")
  private void addJavaScriptChannels(MethodCall methodCall, Result result) {
    List<String> channelNames = (List<String>) methodCall.arguments;
    registerJavaScriptChannelNames(channelNames);
    result.success(null);
  }

  @SuppressWarnings("unchecked")
  private void removeJavaScriptChannels(MethodCall methodCall, Result result) {
    List<String> channelNames = (List<String>) methodCall.arguments;
    for (String channelName : channelNames) {
      webView.removeJavascriptInterface(channelName);
    }
    result.success(null);
  }

  private void clearCache(Result result) {
    webView.clearCache(true);
    WebStorage.getInstance().deleteAllData();
    result.success(null);
  }

  private void getTitle(Result result) {
    result.success(webView.getTitle());
  }

  private void scrollTo(MethodCall methodCall, Result result) {
    Map<String, Object> request = methodCall.arguments();
    int x = (int) request.get("x");
    int y = (int) request.get("y");

    webView.scrollTo(x, y);

    result.success(null);
  }

  private void scrollBy(MethodCall methodCall, Result result) {
    Map<String, Object> request = methodCall.arguments();
    int x = (int) request.get("x");
    int y = (int) request.get("y");

    webView.scrollBy(x, y);
    result.success(null);
  }

  private void getScrollX(Result result) {
    result.success(webView.getScrollX());
  }

  private void getScrollY(Result result) {
    result.success(webView.getScrollY());
  }

  private void applySettings(Map<String, Object> settings) {
    for (String key : settings.keySet()) {
      switch (key) {
        case "jsMode":
          Integer mode = (Integer) settings.get(key);
          if (mode != null) {
            updateJsMode(mode);
          }
          break;
        case "hasNavigationDelegate":
          final boolean hasNavigationDelegate = (boolean) settings.get(key);

          final WebViewClient webViewClient =
              flutterWebViewClient.createWebViewClient(hasNavigationDelegate);

          webView.setWebViewClient(webViewClient);
          break;
        case "debuggingEnabled":
          final boolean debuggingEnabled = (boolean) settings.get(key);

          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setWebContentsDebuggingEnabled(debuggingEnabled);
          }
          break;
        case "hasProgressTracking":
          flutterWebViewClient.hasProgressTracking = (boolean) settings.get(key);
          break;
        case "gestureNavigationEnabled":
          break;
        case "userAgent":
          updateUserAgent((String) settings.get(key));
          break;
        case "allowsInlineMediaPlayback":
          // no-op inline media playback is always allowed on Android.
          break;
        default:
          throw new IllegalArgumentException("Unknown WebView setting: " + key);
      }
    }
  }

  private void updateJsMode(int mode) {
    switch (mode) {
      case 0: // disabled
        webView.getSettings().setJavaScriptEnabled(false);
        break;
      case 1: // unrestricted
        webView.getSettings().setJavaScriptEnabled(true);
        break;
      default:
        throw new IllegalArgumentException("Trying to set unknown JavaScript mode: " + mode);
    }
  }

  private void updateAutoMediaPlaybackPolicy(int mode) {
    // This is the index of the AutoMediaPlaybackPolicy enum, index 1 is always_allow, for all
    // other values we require a user gesture.
    boolean requireUserGesture = mode != 1;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      webView.getSettings().setMediaPlaybackRequiresUserGesture(requireUserGesture);
    }
  }

  private void registerJavaScriptChannelNames(List<String> channelNames) {
    for (String channelName : channelNames) {
      webView.addJavascriptInterface(
          new JavaScriptChannel(methodChannel, channelName, platformThreadHandler), channelName);
    }
  }

  private void updateUserAgent(String userAgent) {
    webView.getSettings().setUserAgentString(userAgent);
  }

  @Override
  public void dispose() {
    methodChannel.setMethodCallHandler(null);
    if (webView instanceof InputAwareWebView) {
      ((InputAwareWebView) webView).dispose();
    }
    webView.destroy();
  }
}
