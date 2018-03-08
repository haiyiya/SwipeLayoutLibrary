# SwipeLayoutLibrary
可左右滑动的布局<br>
![Alt text](https://github.com/haluolym/SwipeLayoutLibrary/blob/master/screenshots/screenshot.gif)

# 导入
Android Studio-New-Import Module

# 添加依赖
```java
dependencies {
    implementation project(':SwipeLayoutLibrary-master')
}
```

# 布局
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

# 状态常量
```java
public static final int SCROLL_STATE_NORMAL = 0;
public static final int SCROLL_STATE_LEFT = 1;
public static final int SCROLL_STATE_RIGHT = 2;
```

# 方法
```java
/**
 * 获得当前状态
 * @return
 */
public int getScrollState()

/**
 * 切换到状态
 * @param scrollState
 */
public void setScrollState(int scrollState)

/**
 * 平滑滚动到状态
 * @param scrollState
 */
public void smoothScrollToState(int scrollState)
```

# 事件
```java
public interface OnSwipedListener {
    /**
     * 状态改变事件
     * @param state 改变后状态
     * @param stateOld 改变前状态
     */
    void onSwiped(int state, int stateOld);
}
```
