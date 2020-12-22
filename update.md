版本更新日志

### v2.3.5
1. 适配小米6手机从非全屏界面打开全屏图片预览时，出现顿挫问题
2. 优化大图未加载完全时动画已经开始，此时过渡动画能很突兀问题

### v2.3.4
1. 修复ShareData引起的空指针Bug
2. 针对缩略图为圆形或圆角矩形提供图形变换，让过渡动画无缝衔接

### v2.3.3
1. 默认预览界面预览图预加载，优化过度动画效果

### v2.3.2
1. 修复应用其它config时，如果config未指定imageLoader，不会取全局图片加载器作为默认加载器Bug
2. 修复横屏下拉关闭时，x轴坐标计算错误导致过渡动画X轴先回撤一段距离再开始动画Bug
3. 对于异形屏、横竖屏都绘制到耳朵区域
4. 优化大图拖拽，如果图片超出屏幕大小，则可上下左右拖拽一段距离，目的是防止非全屏状态下或异形屏下内容被遮挡


### v2.3.0~2.3.1
1. 发现之前PhotoPreview设置list类型的数据不方便，必须要传List<Object>才调用sources(List)，其它都调用sources(Object...)，所以调整list为List<?>,只要是List类型就可以，不管泛型

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






