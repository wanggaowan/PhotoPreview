# PhotoPreview
仿微信朋友圈图片预览。此库参考 [PhotoViewer](https://github.com/wanglu1209/PhotoViewer)，并在此基础上做了修改，修复内存泄漏问题，优化动画,现在打开关闭更加流畅。

效果请下载[demoApk](/app-debug.apk)查看

* [项目github地址](https://github.com/wanggaowan/PhotoPreview)

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![](https://jitpack.io/v/wanggaowan/PhotoPreview.svg)](https://jitpack.io/#wanggaowan/PhotoPreview)

#### 引入：
1. 添加 JitPack repository到你的build文件
      ```groovy
       allprojects {
           repositories {
               maven { url 'https://www.jitpack.io' }
           }
       }
      ```

2. 增加依赖
      ```groovy
      dependencies {
         implementation 'com.github.wanggaowan:PhotoPreview:1.4'
      }
      ```

#### Proguard
无需添加任何混淆规则，可直接混淆

#### *License*
PhotoPreview is released under the Apache 2.0 license.
```
Copyright 2019 wanggaowan.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at following link.

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitat
```