// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#import "FLTWebViewFlutterPlugin.h"
#import "FLTCookieManager.h"
#import "FlutterWebView.h"

@implementation FLTWebViewFlutterPlugin

static NSObject<FlutterPluginRegistrar>* mRegistrar;


+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    mRegistrar = registrar;
    FLTWebViewFactory* webviewFactory = [[FLTWebViewFactory alloc] initWithMessenger:registrar.messenger];
    [registrar registerViewFactory:webviewFactory withId:@"plugins.flutter.io/webview"];
    [FLTCookieManager registerWithRegistrar:registrar];
}

+ (NSString *)getAssetPath:(NSString *)asset {
    return [mRegistrar lookupKeyForAsset:asset];
}


@end
