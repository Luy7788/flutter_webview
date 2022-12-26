class ScreenshotConfiguration {
  ///A Boolean value that indicates whether to take the snapshot after incorporating any pending screen updates.
  ///The default value of this property is `true`, which causes the web view to incorporate any recent changes to the view’s content and then generate the snapshot.
  ///If you change the value to `false`, the [WebView] takes the snapshot immediately, and before incorporating any new changes.
  ///
  ///**NOTE**: available only on iOS.
  ///
  ///**NOTE for iOS**: Available from iOS 13.0+.
  bool afterScreenUpdates;

  ///导出格式
  CompressFormat compressFormat;

  ///质量0-100
  int quality;

  ///要裁剪的rect
  WebViewRect? rect;

  ///想要返回的图片宽度
  double? snapshotWidth;

  ScreenshotConfiguration(
      {this.rect,
      this.snapshotWidth,
      this.compressFormat = CompressFormat.JPEG,
      this.quality = 100,
      this.afterScreenUpdates = true}) {
    assert(this.quality >= 0);
  }

  ///Converts instance to a map.
  Map<String, dynamic> toMap() {
    return {
      "afterScreenUpdates": afterScreenUpdates,
      "compressFormat": compressFormat.toNativeValue(),
      "quality": quality,
      "rect": rect?.toMap(),
      "snapshotWidth": snapshotWidth,
    };
  }

  @override
  String toString() {
    return 'ScreenshotConfiguration{afterScreenUpdates: $afterScreenUpdates, compressFormat: $compressFormat, quality: $quality, rect: $rect, snapshotWidth: $snapshotWidth}';
  }
}

///Class that represents the known formats a bitmap can be compressed into.
class CompressFormat {
  final String _value;
  final String _nativeValue;
  const CompressFormat._internal(this._value, this._nativeValue);
// ignore: unused_element
  factory CompressFormat._internalMultiPlatform(
      String value, Function nativeValue) =>
      CompressFormat._internal(value, nativeValue());

  ///Compress to the `JPEG` format.
  ///Quality of `0` means compress for the smallest size.
  ///`100` means compress for max visual quality.
  static const JPEG = CompressFormat._internal('JPEG', 'JPEG');

  ///Compress to the `PNG` format.
  ///PNG is lossless, so `quality` is ignored.
  static const PNG = CompressFormat._internal('PNG', 'PNG');

  ///Compress to the `WEBP` lossy format.
  ///Quality of `0` means compress for the smallest size.
  ///`100` means compress for max visual quality.
  ///
  ///**NOTE**: available only on Android.
  static const WEBP = CompressFormat._internal('WEBP', 'WEBP');

  ///Compress to the `WEBP` lossless format.
  ///Quality refers to how much effort to put into compression.
  ///A value of `0` means to compress quickly, resulting in a relatively large file size.
  ///`100` means to spend more time compressing, resulting in a smaller file.
  ///
  ///**NOTE**: available only on Android.
  ///
  ///**NOTE for Android**: available on Android 30+.
  static const WEBP_LOSSLESS =
  CompressFormat._internal('WEBP_LOSSLESS', 'WEBP_LOSSLESS');

  ///Compress to the `WEBP` lossy format.
  ///Quality of `0` means compress for the smallest size.
  ///`100` means compress for max visual quality.
  ///
  ///**NOTE**: available only on Android.
  ///
  ///**NOTE for Android**: available on Android 30+.
  static const WEBP_LOSSY =
  CompressFormat._internal('WEBP_LOSSY', 'WEBP_LOSSY');

  ///Set of all values of [CompressFormat].
  static final Set<CompressFormat> values = [
    CompressFormat.JPEG,
    CompressFormat.PNG,
    CompressFormat.WEBP,
    CompressFormat.WEBP_LOSSLESS,
    CompressFormat.WEBP_LOSSY,
  ].toSet();

  ///Gets a possible [CompressFormat] instance from [String] value.
  static CompressFormat? fromValue(String? value) {
    if (value != null) {
      try {
        return CompressFormat.values
            .firstWhere((element) => element.toValue() == value);
      } catch (e) {
        return null;
      }
    }
    return null;
  }

  ///Gets a possible [CompressFormat] instance from a native value.
  static CompressFormat? fromNativeValue(String? value) {
    if (value != null) {
      try {
        return CompressFormat.values
            .firstWhere((element) => element.toNativeValue() == value);
      } catch (e) {
        return null;
      }
    }
    return null;
  }

  ///Gets [String] value.
  String toValue() => _value;

  ///Gets [String] native value.
  String toNativeValue() => _nativeValue;

  @override
  int get hashCode => _value.hashCode;

  @override
  bool operator ==(value) => value == _value;

  @override
  String toString() {
    return _value;
  }
}

class WebViewRect {
  ///rect height
  double height;

  ///rect width
  double width;

  ///x position
  double x;

  ///y position
  double y;
  WebViewRect(
      {required this.x,
        required this.y,
        required this.width,
        required this.height}) {
    assert(this.x >= 0 && this.y >= 0 && this.width >= 0 && this.height >= 0);
  }

  ///Gets a possible [WebViewRect] instance from a [Map] value.
  static WebViewRect? fromMap(Map<String, dynamic>? map) {
    if (map == null) {
      return null;
    }
    final instance = WebViewRect(
      height: map['height'],
      width: map['width'],
      x: map['x'],
      y: map['y'],
    );
    return instance;
  }

  ///Converts instance to a map.
  Map<String, dynamic> toMap() {
    return {
      "height": height,
      "width": width,
      "x": x,
      "y": y,
    };
  }

  ///Converts instance to a map.
  Map<String, dynamic> toJson() {
    return toMap();
  }

  @override
  String toString() {
    return 'InAppWebViewRect{height: $height, width: $width, x: $x, y: $y}';
  }
}
