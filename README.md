# 我的电视·〇

电视网络视频播放软件，可以自定义视频源

[my-tv-0](https://github.com/lizongying/my-tv-0)

## 使用

* 遥控器左键/触屏单击打开节目列表
* 遥控器右键/触屏双击打开配置
* 遥控器返回键关闭节目列表/配置
* 打开配置页后，配置地址后并确认，更新节目列表

可以通过本地服务器，导入视频源

[my-tv-server](https://github.com/lizongying/my-tv-server)

目前仅支持json格式，其他格式可以配合[my-tv-server](https://github.com/lizongying/my-tv-server)进行转换
配置格式：

```json
[
  {
    "group": "组名",
    "logo": "图标",
    "name": "标准标题，用于自动获取节目等信息",
    "title": "显示标题",
    "uris": [
      "视频链接"
    ]
  }
]
```

注意：
配置地址更新后，如遇到之前节目还存在的情况，可能需要重启软件后生效，后面会进行优化

下载安装 [releases](https://github.com/lizongying/my-tv-0/releases/)

更多地址 [my-tv](https://lyrics.run/my-tv-0.html)

![image](./screenshots/img.png)

## 更新日志

### v1.0.4

* 在触屏设备上双击打开节目列表
* 支持自动更新

### v1.0.3

* 保存上次频道

### v1.0.2

* 改变部分样式

### v1.0.1

* 支持返回键退出
* 支持基本的视频源配置

### v1.0.0

* 基本视频播放

## 其他

小米电视可以使用小米电视助手进行安装

如电视可以启用ADB，也可以通过ADB进行安装：

```shell
adb install my-tv-0.apk
```

## TODO

* 音量不同
* 收藏夹
* 自定义源
* 节目增加预告
* 频道列表优化
* 更新的视频源不会覆盖已有的节目源
* 更新后自动播放
* 节目列表焦点消失的问题

## 赞赏

![image](./screenshots/appreciate.jpeg)