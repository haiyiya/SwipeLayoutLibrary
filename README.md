# SwipeLayoutLibrary
可左右滑动的布局

# 导入
Android Studio-New-Import Module

# 添加依赖
```java
dependencies {
    implementation project(':SwipeLayoutLibrary-master')
}
```

# 使用
```xml
<com.lyml.SwipeLayout xmlns:sl="http://schemas.android.com/apk/res-auto"
    android:id="@+id/layout1"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:clickable="true"
    android:focusable="true"
    android:gravity="center_vertical"
    sl:defaultView="1"
    sl:defaultViewWidthRealMatchParent="true"
    sl:scrollState="normal">
    ...
</com.lyml.SwipeLayout>
```

* 布局内部不限制View个数
* defaultView：默认View的索引，默认View即主要的View
* defaultViewWidthRealMatchParent：可填true/false，默认View是否充满布局（如果默认View的位置不为1，仅对默认View使用android:layout_width="match_parent"不能达到预期效果）
* scrollState：默认的滚动位置，可填left/normal/right
* 超出屏幕外的控件需要使用固定宽度，否则会不显示
