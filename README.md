## AlphaPlayer

> Powered by ByteDance Live Android team.

AlphaPlayer是直播中台使用的一个视频动画特效SDK，可以通过制作Alpha通道分离的视频素材，再在客户端上通过OpenGL ES重新实现Alpha通道和RGB通道的混合，从而实现在端上播放带透明通道的视频。

这套方案对设计师而言明显降低了特效的制作成本，对于客户端而言有着更可靠的性能和稳定性，且相比cocos2d引擎有着更低的入门门槛和维护成本，为复杂动画的实现提供了一种全新的方式，新的复杂动画开发将会变得更加简单高效。

### 背景

在直播项目的原有礼物动画实现效果是通过cocos引擎实现的，大部分动画都是通过一系列的旋转平移缩放组合而成，能实现的动画效果较简单且开发成本较高。为了丰富动画的表现形式，降低开发成本，我们引入了AlphaPlayer的动画实现方案。

### 方案对比

目前较常见的动画实现方案有原生动画、帧动画、gif/webp、lottie/SVGA、cocos引擎，对于复杂动画特效的实现做个简单对比

| 方案        | 实现成本                             | 上手成本 | 还原程度           | 接入成本 |
| ----------- | ------------------------------------ | -------- | ------------------ | -------- |
| 原生动画    | 复杂动画实现成本高                   | 低       | 中                 | 低       |
| 帧动画      | 实现成本低，但资源消耗大             | 低       | 中                 | 低       |
| gif/webp    | 实现成本低，但资源消耗大             | 低       | 只支持8位颜色      | 低       |
| Lottie/SVGA | 实现成本低，部分复杂特效不支持       | 低       | 部分复杂特效不支持 | 低       |
| cocos2d引擎 | 实现成本高                           | 高       | 较高               | 较高     |
| AlphaPlayer | 开发无任何实现成本，一次接入永久使用 | 低       | 高                 | 低       |

### 运行效果

![demo](./image/demo.gif)

### 项目结构

主要有两个核心部分，一个是MediaPlayer，负责视频每一帧的解码，支持接入方自行实现；另一个是VideoRenderer，负责将解析出来的每一帧画面进行alpha通道混合，再输出到GLSurfaceView上。

### 快速接入

##### 添加依赖

```kotlin
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.bytedance:AlphaPlayer:1.0'
}
```

##### 初始化PlayerController

```kotlin
val config = Configuration(context, lifecycleOwner)
val playerController = PlayerController.get(config, DefaultSystemPlayer())	// 也可以设置自行实现的Player, demo中提供了基于ExoPlayer的实现
playerController.setPlayerAction(object: IPlayerAction {
  override fun onVideoSizeChanged(videoWidth: Int, videoHeight: Int, scaleType: ScaleType) {
  }
  override fun startAction() {
  }
  override fun endAction() {
  }
})
playController.setMonitor(object: IMonitor {
  override fun monitor(result: Boolean, playType: String, what: Int, extra: Int, errorInfo: String) {
  }
}) 
```

##### 将PlayerController绑定到ViewGroup

```kotlin
playerController.attachAlphaView(mVideoContainer)
```

##### 播放动画视频

```kotlin
fun startVideoAnimation() {
  val baseDir = "your video file base dir"
  val portraitFileName = "portrait.mp4"
  val portraitScaleType = 2
  val landscapeFileName = "landscape.mp4"
  val landscapeScaleType = 2
  val dataSource = DataSource().setBaseDir(baseDir)
    .setPortraitPath(portraitFileName, portraitScaleType)
    .setLandscapePath(landscapeFileName, landscapeScaleType)
  if (dataSource.isValid()) {
    playerController.start(dataSource)
  }
}
```

##### 资源释放

```kotlin
fun releasePlayerController() {
  playerController.detachAlphaView(mVideoContainer)
  playerController.release()
}
```

### 已知接入方

| ![douyin](./image/douyin.png) | ![douyin](./image/tiktok.png) | ![douyin](./image/hotsoon.png) | ![douyin](./image/xigua.png) | ![douyin](./image/toutiao.png) |
| :---------------------------: | :---------------------------: | :----------------------------: | :--------------------------: | :----------------------------: |
|             抖音              |            Tiktok             |           抖音火山版           |          西瓜小视频          |            今日头条            |

### 联系我们

如果你有任何关于AlphaPlayer的问题或建议，可以发邮件到邮箱：dengzhuoyao@bytedance.com, 在邮件中详细描述你的问题。

### License

Apache 2.0