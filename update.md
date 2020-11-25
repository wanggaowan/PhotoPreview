版本更新日志

### v2.2
1. 过渡动画使用Transition库实现，同时保留常规过渡动画实现方式，自动选择最佳过渡动画
2. 增加动画时间配置，如果 <=0 则不启用过渡动画
3. 优化触摸手势，解决触摸冲突
4. 由于部分触摸逻辑只能修改PhotoView源码，最终决定还是内嵌PhotoView库
5. 修复2.0一些已发现的Bug

### v2.1
由于PhotoView包名与源库一致，导致依赖此库的用户使用源库PhotoView使用不了,因此发布2.2版本库

### v2.0
1. 适配AndroidX
2. 提供链式调用
3. 优化预览过渡动画，适配大部分机型，常用使用场景
4. 移除内嵌PhotoView，采用依赖方式

### v1.5
1. PhotoPreview增加dismiss()、dismiss(boolean callBack)方法
2. 修复横屏状态下，原始View的x轴坐标计算错误，导致关闭时，预览图缩放时未正确与原图重合Bug

### v1.4
1. 优化横屏模式下缩放动画，优化缩放动画缩放比例计算逻辑，使源图和预览图打开和关闭时无缝衔接
2. 增加标志防止出现PreviewDialogFragment already added异常

### v1.2
修复PreviewDialogFragment already added异常

### v1.1
修复PhotoPreviewFragment not attach context异常

### v1.0
1. 修复弹窗中点击View预览时，View位置计算错误导致打开和关闭不能顺滑衔接问题
2. 现在根据需要预览的View所在Activity是否全屏来决定预览界面是否全屏功能（全屏时已处理异形屏问题），
   目的是为了顺滑打开和关闭预览窗口，目前还做不到单独设置预览窗口是否全屏以保证打开和关闭时的顺滑性，
   甚至会出现错位问题，后续版本改进

### v0.9
1. 修复当点击弹窗中的图片时，预览界面显示在弹窗背后Bug
2. 优化展示和关闭时缩放动画，适配异形屏，之前版本关闭时缩放后图标不能完全和原图重合

### v0.8
修复动态创建ViewPager赋值Id时，值为负数在部分机型崩溃Bug

### v0.7
修复如果同一个Activity存在多个ViewPager导致图片无法预览Bug

### v0.6
将PhotoView库整合到com.wgw.photo.preview目录下，防止同一个项目引入过PhotoView库导致依赖冲突。

### v0.5
1. 修复打开预览窗口后，在图片还未加载完成就退出导致内存溢出Bug
2. PhotoPreview新增setDelayShowProgressTime、setProgressColor以及setProgressDrawable方法

### v0.4
优化触摸逻辑，解决图片放大后触摸冲突

### v0.3
修复初始展示界面不是第0个的时候，indicator初始化还未完成就被调用获取child导致空指针Bug

### v0.2
show方法中picUrls集合泛型使用?通配符

### v0.1
初始化项目，完成基础功能






