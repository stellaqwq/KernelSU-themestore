# SukiSU Nova 主题商店 → KernelSU 管理器移植：交接文档

更新时间：2026-11-22 07:20
适用状态：**管理器可编译、可安装；内核模块已改好可认 debug 签名管理器，未做真机验证**
（本机 `adb devices` 只有一台模拟器，目标机未连）。

---

## 0. 本次增量（2026-11-22 06:25）：主页/超级用户/模块页的板块布局

把 Nova 的三个主页面板块补进来，并把这些板块接入自定义文字：

- 首页：新增 `StatsRow`（超级用户/模块计数）、`SpecSheet`（系统信息/安全信息分行表格），
  替换原来只有四个固定 `InfoText` 的 `InfoCard`；警告卡也改用自定义文字键。
  新接线：`HOME_STATS_SUPERUSER`、`HOME_STATS_MODULE`、`HOME_INFO_MANAGER_VERSION`、
  `HOME_INFO_KERNEL`、`HOME_INFO_DEVICE_MODEL`、`HOME_INFO_FINGERPRINT`、
  `HOME_INFO_SELINUX`、`HOME_INFO_SECCOMP`、`HOME_WARN_REQUIRE_KERNEL(_GKI)`、
  `HOME_WARN_REQUIRE_MANAGER`、`HOME_WARN_LKM_UPDATE`、`HOME_WARN_ROOT_FAILED`、
  `HOME_STATUS_INSTALL_HINT`、`HOME_BTN_INSTALL`。
- 超级用户：新增 Nova 的头部摘要 + 过滤 chips（全部/ROOT/自定义/系统），
  并新增自定义文字键 `SUPERUSER_GRANTED_SUMMARY`（带已授权/总数两个参数）、
  `SUPERUSER_FILTER_ALL/ROOT/CUSTOM/SYSTEM`。Material 与 Miuix 两套都有。
- 模块：新增 Nova 的列表摘要头部（已启用数量 / 可更新数量），
  新增自定义文字键 `MODULE_SUMMARY`（带两个参数）。
- 新组件 `ui/component/miuix/AppCard.kt`。
- `CustomTextConfig` 新增 4 个参数标签；`customTextSummary` 改为按占位符直接替换
  参数标签，避免 `%d` 被字符串参数格式化时抛异常。
- 构建产物（均 versionCode 32680）：
  - `<workspace>/KernelSU_themestore_layout_32680.apk`
    （`me.weishu.kernelsu`，md5 `44c12c756b53a5374df05b2e65304e8c`）
  - `<workspace>/KernelSU_themestore_layout_preview.apk`
    （`me.weishu.kernelsu.preview`，md5 `3f4a98716836d4ec93ccae4ca09a5405`）

**注意**：首页计数依赖 root shell；无 root 时 `superuserCount/moduleCount` 为 null，
两个统计卡不显示，不再向用户报错。超级用户头部/模块摘要同理。

### 0.1 追加（06:55）：首页状态卡背景图与显示选项

- `HomeUiState`/`HomeViewModel` 新增并填充：`homeCardImagePath`、`hideHomeCardStatusText`、
  `hideHomeCardDecoration`、`homeLayout`、`homeStatusAlignment`、`homeStatusScale`、
  `homePillScale`、`homeStatusWidthFraction`、`homeCardImageStyle`、
  `showHomeCardImageWithoutBackground`、`workingModeLabel`。
- 经典 / 沉浸式两种布局都已接：
  - 经典卡：`homeCardImagePath` 作为卡片背景（Coil + `drawPainterCover`）；
    `hideHomeCardDecoration` 隐藏右下角勾；`hideHomeCardStatusText` 隐藏工作状态标题/版本。
  - 沉浸式：移植 `HomeStatusImmersiveMiuix.kt` / `HomeStatusImmersiveMaterial.kt`，
    `homeLayout = immersive` 时切换；对齐、整体缩放、胶囊缩放、宽度、图片样式、
    “无背景也显示图片”全部生效。
- 新产物：
  - `<workspace>/KernelSU_themestore_statuscard_32680.apk`
    （`me.weishu.kernelsu`，md5 `e13dd75680d8be19e6bb6d9a0fb5ab9c`）
  - `<workspace>/KernelSU_themestore_statuscard_preview.apk`
    （`me.weishu.kernelsu.preview`，md5 `771bdfe7c5658e48b22b0fdf5872a004`）

### 0.2 追加（07:20）：让内核模块认这颗重建的管理器

问题：KernelSU 只"加冕"v2 签名证书与模块内编译常量一致的管理器
（`kernel/manager/apk_sign.c: is_manager_apk()` → `EXPECTED_SIZE` + `EXPECTED_HASH`）。
重建包是 debug 证书（744 B / `f4abcd31…`），出厂模块里写的是 827 B / `c371061b…`，
所以管理器即使装上也没有 root 权限。

先离线证明了两个数：新写的 `tools/apk_cert_probe.py` 按 `check_block()` 的解析顺序
逐字段读 APK Signing Block（EOCD 反扫 → 拒绝 ZIP64 → 只允许 v2 + verity padding →
signers → signer → signed data → digests → certificates → 第一张证书），
对 `KernelSU_themestore_statuscard_32680.apk` 得到 `cert_size = 744`、
`sha256 = f4abcd3161f243a294a029e6e88915fc6b70223f93394c0551d6c221d3c11d33`，
与 `apksigner verify --print-certs` 的 SHA-256 一致。
顺带确认这颗管理器**只有 v2 块**（`apksigner -v`：v1/v3 均 false）——内核遇到其它 block id
会直接 `goto invalid`，所以管理器必须保持 v2-only，别给它加 v3。

打补丁的落点（`tools/patch_ksu_expected.py`，`--old-size 827 --size 744`）：

| 文件偏移 | 原指令 | 改后 | 作用 |
|---|---|---|---|
| `0x14b90` | `c371061b…`（64 hex） | `f4abcd31…` | `.rodata` 里的 `EXPECTED_HASH` |
| `0x47BC` | `cmp w0, #0x33b` | `cmp w0, #0x2e8` | `certificate_size != expected_size` |
| `0x47F0` | `mov x2, #0x33b` | `mov x2, #0x2e8` | `read_exact(fp, cert, certificate_size…)` 的长度 |
| `0x47F8` | `cmp x0, #0x33b` | `cmp x0, #0x2e8` | `read_exact` 的返回值校验 |

整文件逐字节比对：`kernelsu.ko` → `kernelsu_dbg.ko` 只差 **68 字节**
（3 处立即数各 2 字节 + 指纹 62 字节），长度仍是 270512，版本串
`welcome to KernelSU version 32680` 未变，无附带损伤。

产物：

- `<workspace>/pd2055_asset/kernelsu_dbg.ko`
  （md5 `77ebf6fea2fbdbd19d817dff32abb439`，原文件备份 `kernelsu.ko`）
- `<workspace>/pd2055_asset/ghost_ksu.ko`：从 Ghostlock APK 里取出的同一颗模块，
  打同样的补丁后与 `kernelsu_dbg.ko` **逐字节相同**，用于回填 APK。
- `<workspace>/Ghostlock_PD2118_KSU_v6_ksu744.apk`
  （md5 `defe2a19247fb21aca0e7cb505f05729`，1971216 B，versionCode 7，
   仍用 debug 证书签名 → 与 v5 同签名，可直接 `install -r`）。
  回填用新脚本 `tools/repack_apk_asset.py`：重写 zip（丢掉旧的 `META-INF/*.SF|RSA|MF`）
  → `zipalign -p 4` → `apksigner sign`。脚本会先校验替换文件字节数与 asset 一致，
  因为 `RootService` 里硬编码了 `kernelsu.ko` 长度 `0x420b0 == 270512`，
  改长度会被它自己拒掉。

运行期兜底：这颗模块是 `CONFIG_KSU_DEBUG` 构建（`kmsg` 有 "You are running KernelSU
in DEBUG mode"），因此暴露了 `/sys/module/kernelsu/parameters/ksu_debug_manager_appid`
（`.rodata` `0x127e8`）。往它写管理器 uid 会走 `set_expected_size()` →
`ksu_set_manager_appid()`，**完全绕过证书校验**。配合
`track_throne()` 里"uid 未变就 skip search"的分支，写一次即可稳定生效。
`ghostlock/bind_manager.ps1` 就是干这个的，并顺手把 `kmsg` 里的
`sha256: … expected: …` / `Crowning manager: uid=` 打出来。注意别用
`pd2157_apk/assets/mgr_root.sh` 里那个 `xpad_manager_appid` 名字——那是 PD2157 自建
模块的参数名，PD2055 这颗模块里没有该字符串。

`ghostlock/ksu_boot.ps1` 已改成默认 push `kernelsu_dbg.ko`（新增 `-Ko` 参数可覆盖），
并在 insmod 后自动调用 `bind_manager.ps1`。

### 0.3 追加（07:20）：模拟器冒烟测试 + 状态卡截图

用本机 `emulator-5556`（Android 15 / x86_64）装了预览包做了一轮真机（模拟器）验证：

- `me.weishu.kernelsu.preview` 安装、启动正常，`MainActivity` 为 topResumedActivity，
  无 FATAL / AndroidRuntime 异常。
- 应用的 `shared_prefs/settings.xml` 里，本次移植新增的 key 全部持久化成功：
  `custom_texts`、`home_card_image_path`、`home_card_image_style`、`home_layout`、
  `home_status_alignment/scale/width_fraction`、`home_pill_scale`、
  `show_home_card_image_without_background`、`hide_home_card_status_text/decoration`、
  `home_card_stickers`、`bottom_bar_icons`、`page_scale`、
  `enable_predictive_back`、`custom_appearance`、`adaptive_theme` 等。
- 首页确认渲染出自定义标题、背景图、自定义文字的统计卡（`HOME_STATS_*`）、
  `SpecSheet` 分行系统信息和自定义底部导航图标——说明新加的板块布局与主题文字确实接通。

为了看到**状态卡本体**（没 root 时 `ksuVersion == null`，会被"安装"提示卡替代），
临时在 `HomeViewModel.buildState()` 里给非 manager 状态打桩了
`ksuVersion/kernelUAPIVersion/lkmMode`，在模拟器上截了两张图，**截图后立即还原了源码**
（`grep PREVIEW_ONLY` 已无残留，`./gradlew :app:compileDebugKotlin --offline` 通过）：

- `<workspace>/ksu_port/previews/status_card_classic_mock.png`
  —— 经典卡：卡片背景图 + "杂鱼~ 运行中哦" + `版本：32680-5` + `LKM` 角标 + 勾选装饰。
- `<workspace>/ksu_port/previews/status_card_immersive_mock.png`
  —— 沉浸式：胶囊 `● 杂鱼~ 运行中哦 | LKM · 32680-5` + 下方 banner 图。

**这两张是模拟状态，不是真机 root 截图**；顶部红条 `连 Root 都拿不到? 杂鱼~` 正是
`HOME_WARN_ROOT_FAILED` 在模拟器无 root 时的表现，真机认领成功后不会出现。
交付 APK 的 md5 在截图前后未变化（见 0.1 / 0.2）。


### 0.4 追加（20:05）：真机现场排障——管理器仍显示"不支持"

真机 PD2118 连上后抓到的现场：

- `/proc/modules` 有 `kernelsu ... Live`、`/data/local/tmp/su -c id` 是 uid=0，
  说明 **root 本身是好的**；`pm list packages -U` 给管理器 uid=10369。
- 拉下手机上**实际安装**的 `base.apk` 跑 `apk_cert_probe.py`：
  `cert_size = 744` / `sha256 = f4abcd31…`，v2-only —— APK 完全正确，不是包的问题。
- 拉下手机上**实际加载**的模块才发现根因：
  - `/data/local/tmp/pdroot/kernelsu.ko`（Ghostlock 解压路径，11-22 07:48）
    和 `/data/local/tmp/pdroot/ksu_run.ko` 都是 **stock 指纹 c371061b…**；
  - `/data/local/tmp/kt/ksu2055.ko` 也是 11-21 03:37 的 stock 版（md5 `c643b983…`）。
  - 装上的 Ghostlock APK 本身确实已是 v6（md5 `defe2a192…`，assets 内 ko 为补丁版），
    但当前 boot 加载的 ko 来自别的解压/复制路径，所以指纹没生效。
- 结论：**不是 APK 的问题，是 insmod 用了旧的 stock .ko**，所以 `search_manager`
  永远加冕不了管理器，`ksu_debug_manager_appid` 一直是 `4294967295`（-1）。

即时救急（不需重启）：这颗 stock 模块仍然带 DEBUG 参数，写 uid 直接绕过证书校验：

```
adb shell "/data/local/tmp/su -c 'echo 10369 > /sys/module/kernelsu/parameters/ksu_debug_manager_appid'"
adb shell am force-stop me.weishu.kernelsu
adb shell am start -n me.weishu.kernelsu/me.weishu.kernelsu.ui.MainActivity
```

实测 appid 从 `4294967295` → `10369`，稳定保持；管理器截图显示
**"工作中 [越狱模式] / 版本 32680-5"**，状态卡正常（`_smoke/device_after_bind.png`）。

根治：把 `pd2055_asset/kernelsu_dbg.ko` 覆盖到手机两个加载路径
（都已完成，权限 0644）：

```
/data/local/tmp/pdroot/kernelsu.ko
/data/local/tmp/kt/ksu2055.ko
```

下次开机重新加载模块后应自动加冕；万一仍显示"不支持"，跑
`ghostlock/bind_manager.ps1`（会自动解析 uid 并写参数）。
顺手修了脚本里 `-Adb adb` 这种 PATH 写法会被 `Test-Path` 误判的问题。


### 0.5 追加（2026-11-23 00:25）：状态卡两个渲染 bug

反馈："有的主题不显示状态卡片，有的主题上半边白一块"。逐条定位后是三个独立问题。

**1) 卡片高度塌缩 → 整张卡消失（Miuix）**

移植版的经典状态卡是 `Row(height(IntrinsicSize.Min)) { Card { 若干 Box(fillMaxSize()) } }`。
`fillMaxSize` 不提供内在高度，所以当主题同时打开
`hide_home_card_status_text` + `hide_home_card_decoration`、又没有可选标签时，
卡片被测量成 0 高——不是"没渲染"，是"高度为 0"。
参考实现是 `AppCard + BoxWithConstraints + Column(heightIn(min = 188.dp))`，
已按参考结构重写 `ActiveStatusCard` / `InactiveStatusCard`，高度有下限，主题怎么配都不会消失。

**2) 卡片主题图路径失效 → 卡片变全透明（真机现象）**

`rememberAsyncImagePainter` 对**不存在的文件也返回非空 painter**，而卡片颜色是
`if (painter != null) Color.Transparent`——图没加载出来，底色又是透明，结果什么都看不见。
主题包的图片解压在 `context.cacheDir`（`ThemePackageStorage` 用 `createTempFile(..., cacheDir)`），
系统清缓存或重启后路径就悬空了，所以"有的主题"掉图、有的不掉。
两处一起改：
- 取 painter 前判 `File(path).exists()`，文件没了就回落到纯色卡片；
- `hide_home_card_status_text/decoration` 改用 `cardImagePainter != null` 判断，
  避免"图没了但文字按有图逻辑隐藏"导致的空白卡。

**3) 固定浅色底 → 背景图上出现白块**

状态卡（经典）和沉浸式胶囊用的是写死的
`colorScheme.secondaryContainer` / `surfaceContainer.copy(alpha = 0.88f)`，
有自定义背景图时明显比周围亮，看起来就是"上半边白一块"。
KernelSU 本来就有 `backgroundAwareColor()` / `backgroundAwareCardColors()`
（`Theme.kt`，SearchBar 等已在用），按 `LocalCardBackgroundAlpha` 把底色改成半透明。
已改：经典卡容器色、沉浸式胶囊、沉浸式 banner 底、Material 版容器色。
`errorContainer`（未安装/不支持）保持不透明，提示要显眼。

**附带兜底**：`MainActivity` 里 `hasCustomBackground` 原来只看
`customBackgroundUri.isNotEmpty()`，文件丢了仍把 Scaffold 设成透明 → 整页露白。
现在要求 `File(path).isFile`，背景失效时退回不透明 surface。

新产物（versionCode 32680，仍是 debug 签名 → 内核模块不用重打补丁）：

| 文件 | 包名 | md5 |
|---|---|---|
| `KernelSU_themestore_cardfix_32680.apk` | `me.weishu.kernelsu` | `2c7b33dd3909cc3c3124d4bdcdb11272` |
| `KernelSU_themestore_cardfix_preview.apk` | `me.weishu.kernelsu.preview` | `eb8f2b6dfe31158cc1cd076b5cba980b` |

升级：`adb install -r` 直接覆盖（同包名同签名），root 状态不受影响。


### 0.6 追加（2026-11-23 00:45）：顶部标题栏"白一块"

截图里的白色区域不是状态卡，是 **TopAppBar（状态栏 + 标题栏）的毛玻璃层**。三处叠加造成：

1. **渐进模糊分支被移植时删掉了。** SukiSU 的 `BlurredBar` 有三条分支：
   `progressiveBlur → textureBlur → 渐变兜底`。移植版只留了中间那条，
   于是模糊是一个**矩形**，底边硬切——看着就是"一块"。
   已把 `rememberProgressiveBarBlurModifier()`（`progressiveTextureBlur` + `ProgressiveBlur.Top`）
   补回来，并让 `BlurredBar` 优先走它。
2. **顶栏遮罩浓度直接取了「卡片透明度」。** `tintAlpha = if (hasCustomBackground) cardAlpha else 0.87`，
   卡片透明度是为小卡片可读性调的（这个主题明显 80%+），铺到整条顶栏就成了白布。
   新增 `MaxBarTintOverBackground = 0.38f`，有背景图时 `cardAlpha` 最多按 0.38 用。
3. **采样层又预涂了一遍同样浓度。** `rememberBlurBackdrop` 里
   `drawRect(surfaceColor.copy(alpha = cardAlpha))` 是给"避免采样全透明像素发黑"用的，
   不需要跟卡片同浓度；否则 blur 采样的源本身已经接近白色，再叠第二层 tint，白度翻倍。
   同一个上限也套在这里。

没开 `enable_progressive_blur`、或 runtime shader 不可用时会退回 `textureBlur`，
此时靠 0.38 的 tint 上限把"白布"压成"薄雾"。

产物：

| 文件 | md5 |
|---|---|
| `KernelSU_themestore_barfix_32680.apk` | `bd1d84557414ca22307f953b0cf350d1` |
| `KernelSU_themestore_barfix_preview.apk` | `90a2485e486bcdb1d36e50c122fc1b92` |

签名未变（744 / `f4abcd31…`），内核模块不用重打补丁，`adb install -r` 覆盖即可。


### 0.7 追加（2026-11-23 01:15）：演示包（强制"工作中"状态）

要在没有内核驱动的设备/模拟器上看主题把状态卡画成什么样，加了一个**只在构建期生效**的开关：

```bash
./gradlew :app:assembleDebug -PKSU_VERSION_CODE_FALLBACK=32680 \
  -PKSU_MOCK_ACTIVE=true -PKSU_NAME="KernelSU Demo" --offline
```

- `app/build.gradle.kts` 新增 `buildConfigField("boolean", "MOCK_ACTIVE_STATE", ...)`，
  只有传 `-PKSU_MOCK_ACTIVE=true` 才是 true，默认 false（已核对默认构建的 BuildConfig）。
- `HomeViewModel.buildState()` 里 `mockActive = BuildConfig.MOCK_ACTIVE_STATE && !Natives.isManager`：
  - `ksuVersion = 32680`、`kernelUAPIVersion = 5` → 走 `ActiveStatusCard`（经典）或沉浸式卡；
  - `lkmMode = true` → 显示 `LKM` 角标；
  - `isRootAvailable = true` → 不再弹 `HOME_WARN_ROOT_FAILED` 红条；
  - `isLateLoadMode = false`、`isSafeMode = false` → 没有 `[越狱模式]` 之类的附加标签。
- **真机上不会造假**：`!Natives.isManager` 是前提，管理器一旦被内核认领，
  `mockActive` 就自动为 false，显示真实状态。
- 应用名改成 `KernelSU Demo` / `KernelSU Demo Preview`，桌面上能和正常包区分。

产物：

| 文件 | 包名 | md5 |
|---|---|---|
| `KernelSU_mock_active_32680.apk` | `me.weishu.kernelsu` | `957fe9ea4f36e28f0a8eb1a146d69798` |
| `KernelSU_mock_active_preview.apk` | `me.weishu.kernelsu.preview` | `1fb119faf2c386dcac317188e2665bcf` |

注意：正式包名的演示包会**覆盖**已安装的正常管理器（同包名同签名），
只想看效果的话优先装 `.preview` 那个，两个可以共存。


### 0.8 追加（2026-11-23 01:40）：SukiSU Nova 演示包

目的：把**参考实现本身**也做成可安装的演示包，这样主题在 Nova 和移植版上的渲染可以并排对比，
差异一眼就能看出来。

**先要让 Nova 工程能编译**（它之前只是只读参考，从没构建过），改了四处：

1. `manager/gradle/gradle-daemon-jvm.properties`：`toolchainVersion=21 → 25`
   （本机只有 JDK 25，Gradle 本来要去 foojay 下 JDK 21，离线直接失败）。
2. `manager/gradle.properties`：注释掉 `systemProp.*.proxyHost/Port`
   （127.0.0.1:4432 已经不通，直连 Google Maven 反而 200）。
3. `manager/settings.gradle.kts`：`pluginManagement.resolutionStrategy` 把
   `com.android.library` 映射到 `com.android.tools.build:gradle:${version}`
   （本地缓存只有 `com.android.application` 的 9.4.1 插件标记，library 的只有 8.4.2）。
4. 首次构建需要联网补 `androidx.documentfile:1.1.0`、`sh.calvin.reorderable:3.1.0`、
   `com.google.protobuf:protobuf-kotlin-lite:4.36.2`、`desugar_jdk_libs_nio:2.1.5`。

**演示开关**与移植版同款：`app/build.gradle.kts` 加 `MOCK_ACTIVE_STATE`，
`HomeViewModel.buildState()` 里 `mockActive = MOCK_ACTIVE_STATE && !Natives.isManager`，
强制 `ksuVersion=32680` / `kernelUAPIVersion=5` / `lkmMode=true` / `isRootAvailable=true`，
并把 `isPreviewMode` 压回 false（Nova 本来有"有 root 无 manager"的 preview 分支，
不压掉会显示成预览态而不是工作中）。

产物（debug 签名，带 x86_64，可装模拟器）：

| 文件 | 包名 / 应用名 | md5 |
|---|---|---|
| `SukiSU_Nova_Demo_40997.apk` | `com.sukisu.nova` / `SukiSU Nova Demo` | `a95a4dd2af255f733698b885c64fbe16` |
| `SukiSU_Nova_Demo_Preview_40997.apk` | `com.sukisu.nova.preview` / `SukiSU Nova Demo Preview` | `f8124fca7dc79550e0a3679460d4cc90` |

versionCode 40997（浅克隆只有 1 个提交，`4*10000 + commitCount + 996`）、versionName `dev`。
注意 `com.sukisu.nova` 那个会覆盖机器上已装的 Nova，只想对比就装 `.preview`。


### 0.9 追加（2026-11-23 01:55）：顶部白块的真正原因——丢了"滚动才上色"

拿 Nova 演示包和移植版并排才发现关键差别：**Nova 静止时顶栏完全透明**，背景图一直延伸到状态栏；
移植版顶部永远压着一层白色渐变。

根因在 `BlurredBar`：参考实现有 `scrollBehavior` 参数和 `effectiveBlurActive` 判定。

```kotlin
val isScrolled by remember(scrollBehavior) {
    derivedStateOf { scrollBehavior?.state?.collapsedFraction?.let { it > 0.01f } ?: true }
}
val effectiveBlurActive = blurActive ?: (scrollBehavior == null || isScrolled)
```

三个绘制分支都受 `effectiveBlurActive` 约束，所以**没滚动时一条分支都不走** → 透明。
移植版的签名只有 `(backdrop, blurActive: Boolean = true, content)`，没有 scrollBehavior，
`blurActive` 恒为 true，未滚动也照画那个 `0.28 → 0.12 → 透明` 的渐变，于是顶部多了一条白带。

修复：
- `BlurExt.kt` 的 `BlurredBar` 补齐 `modifier` / `scrollBehavior` / `blurActive: Boolean?` /
  `progressiveGradient` 四个参数与 `effectiveBlurActive` 判定，三个分支改用该判定；
- `HomeMiuix`、`SuperUserMiuix`、`ModuleMiuix`、`SettingsMiuix` 的顶栏都改为
  `BlurredBar(backdrop = backdrop, scrollBehavior = scrollBehavior)`。
  其余页面调用时 `scrollBehavior = null` → 判定为 true，行为保持原样。

模拟器实测（`me.weishu.kernelsu.preview` + 用户主题）：
- 静止：顶栏完全透明，壁纸直达状态栏，与 Nova 一致；
- 下滑后：顶栏出现柔和着色/模糊，标题仍清晰，底边不是硬切。

产物：

| 文件 | 包名 | md5 |
|---|---|---|
| `KernelSU_themestore_topbar_32680.apk` | `me.weishu.kernelsu` | `973650dcc47b40b6d430fed7aaaf1e46` |
| `KernelSU_themestore_topbar_preview.apk` | `me.weishu.kernelsu.preview` | `0264e4f8c85a7d39f1b3f50432b84a0b` |
| `KernelSU_mock_active_32680_v2.apk` | `me.weishu.kernelsu`（演示） | `d63512baa48a776378e80209c0af25f5` |
| `KernelSU_mock_active_preview_v2.apk` | `me.weishu.kernelsu.preview`（演示） | `84bca02283e385456a9a3fa6c87ba956` |


### 0.10 追加（2026-11-23 02:20）：顶栏残留白雾——渐进模糊没受滚动约束

0.9 之后顶栏在**静止**时仍有雾，且表现为"首页整条、超级用户页左半边明显、设置页几乎看不出"。
不是判定算错，而是**渐进模糊分支绕过了那个判定**。

在 `BlurredBar` 里打日志实锤：

```
bar scrollBehavior=true frac=-0.0 isScrolled=false eff=false progressive=true
```

`effectiveBlurActive` 已经是 false，但 `progressiveBlur` 是 true，而分支写的是
`if (progressiveBlur)`（参考实现注释写着 "Always active"，即未滚动也要给壁纸顶部结霜）。
于是 `progressiveTextureBlur` 一直在画，白雾就一直在。

"只白左半边"是错觉：模糊层是全宽的，超级用户页顶部左侧是明亮的厨房墙面，糊开以后整片发白；
右侧是暖黄走廊，同样的雾看不出来。设置页顶部背景本身就暗且渐变淡出快，所以几乎无感。

修复：

```kotlin
val blurModifier = if (progressiveBlur && effectiveBlurActive) { ... }
```

底部悬浮栏调用时 `scrollBehavior = null` → 判定为 true，行为不变。

模拟器实测（同一主题、`enable_progressive_blur=true`、`card_bg_alpha_percent=43`）：
首页 / 超级用户 / 设置三页顶部**完全透明**，壁纸直达状态栏，滚动后才浮出模糊。

产物：

| 文件 | 包名 | md5 |
|---|---|---|
| `KernelSU_themestore_blurtop_32680.apk` | `me.weishu.kernelsu` | `2c36baccf577222a5299734442446937` |
| `KernelSU_themestore_blurtop_preview.apk` | `me.weishu.kernelsu.preview` | `06f393c452f4467efa9aba38a62639aa` |
| `KernelSU_mock_active_v3_32680.apk` | `me.weishu.kernelsu`（演示） | `3b58dc244641eeb6ffa78eee04dc2bff` |
| `KernelSU_mock_active_preview_v3.apk` | `me.weishu.kernelsu.preview`（演示） | `5f93cf8c411899028c62c11bd739d3c0` |

**排障副作用记录**：中途为了做对照实验直接改模拟器上的 `shared_prefs/settings.xml`，
把属主写成了固定的 `u0_a60`（preview 包实际是 `u0_a63`/10063），导致应用读不到偏好、
主题回落到默认。已用 `/data/local/tmp/settings.bak.xml` 恢复并 `chown` 到目录真实属主。
以后改设备上的 prefs 一律先 `stat -c '%u:%g' /data/data/<pkg>` 取属主。


### 0.11 追加（2026-11-23 03:00）：清除自定义外观

设置页加了一条「清除自定义外观」（位于「界面风格」与「主题设置」之间），带确认框。

- `SettingsViewModel.clearAllCustomAppearance()`：先删文件再改偏好，顺序很重要——
  如果先写偏好后删文件失败，偏好就会指向不存在的图片。
  删除：`ImageStorage.clear`（壁纸）、`systemWallpaperFile`、`clearHomeCardImage`、
  `clearHomeCardSticker`、`clearBottomBarIcons`、`clearAdaptiveImages`。
- 重置的偏好分四组：
  1) 配色：`themeMode=0`、`miuixMonet=false`、`keyColor=0`、`colorStyle=TonalSpot`、
     `colorSpec=SPEC_2025`、`extractColorFromBackground=false`；
  2) 壁纸与卡片：`customBackgroundUri=""`、`updatedAt=0`、`useSystemWallpaper=false`、
     `cardBackgroundAlphaPercent=DEFAULT_CARD_ALPHA`、模糊/遮罩回默认、
     `homeCardImagePath=""`、贴纸清空；
  3) 文字与布局：`customHomeTitle=""`、`customTexts=emptyMap()`、
     `homeLayout=classic`、对齐/缩放/宽度/图片样式回默认、`hideHomeCard*` 复位；
  4) 壳与图标：`customAppearanceJson=""`、`adaptiveThemeJson=""`、
     `appIconId=DEFAULT_ID`、`appIconDynamicColor=false`。
- 第一版**漏了底栏样式**，用户实测"导航栏并没有被清掉"。`enableFloatingBottomBar` /
  `enableFloatingBottomBarBlur` / `enableNavigationBadge` / `enableBlur` /
  `enableProgressiveBlur` / `pageScale` 这些同样是主题包带进来的视觉项，
  之前被误判成"行为设置"而保留；现在一并复位（悬浮胶囊 → 贴底平栏，缩放回 1.0）。
- 明确**不重置**：`uiMode`（界面风格）、主题商店配置、模块列表选项、
  `suCompat` / `umount` / `sulog` 等功能开关——它们不是外观。
- Miuix 与 Material 两套设置页都有入口；新增 4 个字符串（中英）。

模拟器实测：清除后壁纸、自定义标题、卡片图、自定义文字全部回默认，
底栏从悬浮胶囊变回贴底平栏（对比图 `_smoke/bar_before.png` / `bar_after.png`）。

产物：

| 文件 | 包名 | md5 |
|---|---|---|
| `KernelSU_themestore_clear_32680.apk` | `me.weishu.kernelsu` | `e02050e1beb5faac42192d4e8039c47d` |
| `KernelSU_themestore_clear_preview.apk` | `me.weishu.kernelsu.preview` | `a9995dd9b73228259cd98444943822a2` |
| `KernelSU_mock_active_preview_v4.apk` | `me.weishu.kernelsu.preview`（演示） | `4199fb599311b58d75cc268e79e08a0c` |


### 0.12 追加（2026-11-23 03:12）：重装管理器后又"不支持"——不是签名，是 uid 和模块

用户报"签名没变怎么还是不支持"。现场核对：

- 拉下手机上安装的 `base.apk` 跑 `apk_cert_probe.py`：仍是 `744 / f4abcd31…`，**签名没变**；
- `pm list packages -U` 显示管理器 uid 是 **10370**，而上次写进内核的是 **10369**；
- `ksu_debug_manager_appid` 读到 `4294967295`（-1），说明 `track_throne()` 发现
  "manager uid changed" 就 invalidate 了，之后重新扫描没成功；
- 扫描失败的根因：`/data/local/tmp/pdroot/kernelsu.ko` 又是 **stock**（`c643b983…`）。
  Ghostlock 的链式日志里写着 `root5 ksu: copy done errno=17`（EEXIST）——
  **目标文件存在就不覆盖**，所以它一直用 pdroot 里的旧 stock 模块，
  我上次 push 到 `/data/local/tmp/kt/ksu2055.ko` 的补丁版根本没被用到。

也就是说：重装管理器 → uid 变化 → appid 作废 → 需要重新加冕 → 但当前模块是 stock →
签名对不上 → 一直显示"不支持"。三个条件缺一不可。

修复（当次开机立即生效，不需重启）：

1. `echo 10370 > /sys/module/kernelsu/parameters/ksu_debug_manager_appid`；
2. 把 `pd2055_asset/kernelsu_dbg.ko` push 到 `/data/local/tmp/pdroot/kernelsu.ko`（0644）；
3. 删掉 `pdroot/ksu_run.ko` 和 `pdroot/state`（`BOOT_ID`/`CVE_LOADED` 进度标记），
   旧的 ko 备份到 `pdroot/old/`，让下次开机的解压与重定位走全新文件；
4. 重启管理器 → 截图确认 **工作中 [越狱模式] / 32680-5**。

**以后重装管理器后 uid 可能变**，若又显示"不支持"，跑一次
`ghostlock/bind_manager.ps1` 即可（它会重新解析 uid 并写参数）。


### 0.13 追加（2026-11-23 03:18）：能不能修提权工具本身

查过了：APK 里真正跑的是 `assets/pd2055_exploit`（6.8 MB），仓库里只有这个二进制，
**没有任何源码或构建脚本**（`ghostlock/src` 编的是另一个 3.2 MB 的 `ghostlock`，
和 APK 里跑的不是同一个程序），APK 侧 `RootService` 也只解压固定 assets、
不执行任何 `*.sh` hook。所以提权链本体改不了。

能做的是把它留下的坑填掉，分两层：

**根治（让下次开机自动认领，不需要任何脚本）**

KernelSU 的 `pkg_observer` 会在管理器被重装时触发 `track_throne()` 重新扫描；
只要 insmod 的是**补丁版模块**，重装后 uid 变了也会自动加冕。
之前失效是因为 `pdroot/kernelsu.ko` 被"文件已存在"逻辑一直复用的旧 stock 版本，
现已：

- `/data/local/tmp/pdroot/kernelsu.ko` → `kernelsu_dbg.ko`（md5 `77ebf6fe…`，0644）；
- `/data/local/tmp/kt/ksu2055.ko` → 同一份补丁版；
- 删除 `pdroot/ksu_run.ko`（旧重定位产物）与 `pdroot/state`
  （里面是 `CVE_ATTEMPTED/CVE_LOADED/BOOT_ID` 的 per-boot 进度，会让解压/重定位被跳过），
  旧文件备份在 `pdroot/old/`。

**兜底（当前 boot 已经加载了 stock 模块时）**

新增手机端脚本 `ghostlock/bind_manager.sh`，已 push 到 `/data/local/tmp/bind_manager.sh`：

```
su -c "sh /data/local/tmp/bind_manager.sh"
# [+] ksu_debug_manager_appid = 10370 (manager uid 10370) - crowned
```

它自己从 `/data/system/packages.list` 解析 `me.weishu.kernelsu` 的当前 uid、
校验 root 与参数是否存在、写完后回读确认，不需要 PC。
PC 侧的 `ghostlock/bind_manager.ps1` 仍然保留（一次装好两份脚本）。


### 0.14 追加（2026-11-23 03:35）：真正该改的是 `assets/pd2055_exploit` 里内嵌的 ko

0.13 的结论是错的：我说"`pd2055_exploit` 只有二进制、没有源码"，是因为看错了对象——
仓库里 `pd2055_asset/pd2055_exploit` 只有 45 KB，而 APK 里那个同名 asset 是 **6,827,628 B**，
两者不是同一个东西。后者是一个自解压器：`.text` 只有 37 KB，其余是 payload，里面包含

- `ghostlock`（`root5 ksu: enter` 在 `0x14ae87`），源码就在 `ghostlock/src`，
  和 `build/ghostlock_pd2118_test` 完全同源（大小都是 3,285,472）；
- **一份完整的 stock `kernelsu.ko`**：ELF 头在 `0x46a5c8`，长度 270512，
  与 `pd2055_asset/kernelsu.ko` **逐字节相同**（md5 `c643b983…`），
  `c371061b…` 落在 `0x47f158`。

而且 `RootService.extractAssets()` 的逻辑是
`File.exists() → File.delete() → 重新解压`，即**每次运行都覆盖**。
所以我前面手动 push 到 `pdroot/kernelsu.ko` 的补丁版，下次点"一键 root"就被这份内嵌的
stock 版本覆盖回去了——这才是"改了模块还是不支持"的真正原因。

修复（原位等长替换，不动任何其他字节）：

```python
d = bytearray(open('_smoke/exploit_680.bin','rb').read())   # 从 APK 抽出
d[0x46a5c8 : 0x46a5c8 + 270512] = open('pd2055_asset/kernelsu_dbg.ko','rb').read()
```

替换后整文件 6,827,628 B 不变，stock 指纹消失，`f4abcd31…` 出现在原 `0x47f158`。
再用 `tools/repack_apk_asset.py` 回填 `assets/pd2055_exploit` 并重签。

产物（debug 签名，与 v6 同证书，可直接覆盖安装）：

| 文件 | md5 |
|---|---|
| `Ghostlock_PD2118_KSU_v7_ksu744.apk` | `0c65dd763f3749a97cff44433bc3e4e5` |

效果：下次点"一键 root"，解压出来的 `pdroot/kernelsu.ko` 就是补丁版，
`search_manager()` 能直接加冕管理器；重装管理器导致 uid 变化时，
`pkg_observer` 也会用这份模块重新扫描，不再需要 `bind_manager` 脚本
（脚本仍保留作兜底）。


### 0.15 追加（2026-11-23 04:05）：分发用的换签脚本 + 源码包

**`tools/release_sign.py`** —— 一条命令把 debug 签名换成自己的 release 密钥，并同步模块补丁。
之所以必须做：debug keystore 的口令是公开的，任何人都能签出同包名同证书的"升级包"，
内核模块认的又正是这个指纹，等于把信任根发出去。

流程：生成密钥 → 导出证书 → 用 `patch_ksu_expected.py` 把新指纹打进 stock `kernelsu.ko`
→ 按 stock 指纹定位、替换 `assets/pd2055_exploit` 内嵌的模块 → 用新密钥重签 Ghostlock
→ zipalign + 重签管理器 → 用 `apk_cert_probe.py` 复核。

实测时踩到两条**硬约束**，都写进脚本了：

1. **密钥必须是 RSA-2048**。内核把证书拷进 1024 字节的栈缓冲（`CERT_MAX_LENGTH`），
   RSA-4096 的证书是 1415 字节，`check_v2_signature()` 直接返回 false。
   2048 位签发出来 793 字节，和 debug 的 744 同一量级。
2. **管理器只能有 v2 签名块**。`apksigner` 默认按 minSdk 追加 v3，而内核遍历签名块时
   遇到 v3 的 id（`0xf05368c0`）会 `goto invalid`。脚本显式关掉 v1/v3。

验证结果（测试密钥）：

```
cert_size = 793
sha256    = 9e2c793ff9987a01ceb3fe543c31f0da3b9c80f6ca4ac4ceb36d79f392d9beee
Verified using v2 scheme: true / v3: false
ghostlock 内嵌模块 md5 与补丁后的 ko 完全一致
```

**`tools/pack_source.py` + `tools/SOURCE-README.md`** —— 生成 GPL 用的源码包
（880 个文件，5.0 MiB）。包含：打补丁后的 KernelSU 管理器树、ghostlock 源码、
全部工具脚本、HANDOFF、三份上游许可证（GPL-3.0 ×2 / GPL-2.0）与一份说明。
排除：`.git` / `build` / `.gradle` / `local.properties`（含本机 SDK 路径）/
`gradle.properties` 里的代理行 / **设备现场日志**（含设备序列号）。

产物：

| 文件 | md5 |
|---|---|
| `KernelSU_ThemePort_Source.zip` | `98cb90e1f30032ca2c946349f6e2cabb` |


### 0.16 追加（2026-11-23 04:20）：用用户提供 release 密钥完成签名

用户提供了自己的 `keystore.properties` + `my-app-release.jks`，用 `release_sign.py` 换签成功。

- 证书 DER **798 B**（< 1024 上限）、SHA-256 `c4bd244cdb22e17532d4e3dea5eb2c5e4262bbded5ac4db9f1170383175cae34`；
- 模块补丁：`c371061b… → c4bd244c…`，`#827 → #798`（两处 cmp、一处 mov）；
- Ghostlock 内嵌模块位置仍是 `0x46a5c8`，替换后 md5 `ff1ec20b…`，与补丁后的 ko 完全一致；
- 管理器验证：v1 false / **v2 true** / v3 false，probe 通过。

产物在 `<release-dir>/`：

```
0450f2a66bcfe14c97d5a7fa8f450babed1632bd26f8ff5e7aa2847fc9436912  KernelSU_themestore_32680_release.apk
ee7cc5944751cf416e08d78aa9b45e81241d94fb576b6184422f0407d6184ce7  Ghostlock_PD2118_KSU_v6_release.apk
```

已生成 `SHA256SUMS.txt`。源码包因工具脚本更新重新打过，md5 `8b140aa90d59701ecfaf0b3b3029b681`。

**安全事件**：首次运行时 `repack_apk_asset.py` 的 `run()` 未脱敏，把 keystore 口令明文打印到了
工具输出里。已修两处打印函数（`pass:<redacted>` / `-storepass <redacted>`），并重新打包源码。
口令已经出现在会话记录中，已提示用户轮换密钥口令、不要外传该段记录。


### 0.17 追加（2026-11-23 04:25）：换用新密钥重签

用户轮换口令并换了新证书（旧口令已失效，证书指纹从 `c4bd244c…` 变成 `22a8ec04…`）。
用 `release_sign.py` 重签，输出目录 `release_v2/`，旧的 `release/` 保留但**已作废**。

产物 SHA-256：

```
e671fdd62150c41edd0332815e6335e54c1b536dff93ce38c433a5686b9e0e9f  Ghostlock_PD2118_KSU_v6_release.apk
9f68c5d8bfdcdf62e0f59fdb5d76adb60d43dfccf60722e9f211db6ae8fdbc28  KernelSU_themestore_32680_release.apk
```

新证书 DER 798 B / `22a8ec042b8276bebc7652fce5d11ab064f8f0b3e53bad4ea7db9fbf10c3e466`；
模块补丁 `#827 → #798`，内嵌 ko md5 `e0e09591…` 与补丁产物一致；管理器 v2 true / v3 false。
本次两处打印均已脱敏，日志中未出现口令。


### 0.18 追加（2026-11-23 05:08）：补回 libksud.so 并重签

现象：管理器读不到模块列表。原因不是重签，而是**构建产物里本来就没有**
`lib/arm64-v8a/libksud.so`——`CMakeLists.txt` 只编译 `libkernelsu.so` 和
`libadbroot.so`，ksud 是 Rust 二进制，上游靠 CI 单独产出后以 `.so` 名字塞进 APK，
本地仓库没有这一步产物（Nova 的演示包里也没有）。

从本地 `yh170/assets/ghostkey/ksu/ksud` 取到一份 AArch64 PIE（ELF64 DYN，6.2 MB，
含 `ksud::` 符号），放到

```
manager/app/src/main/jniLibs/arm64-v8a/libksud.so
```

重新 `:app:assembleDebug` 后 APK 里出现该条目，再用 `release_sign.py` 以用户新密钥
（证书 `22a8ec04…`，798 B）重签，输出 `release_v3/`。

最终包校验：

```
lib/arm64-v8a/libksud.so  present (4,991,304 B after zip compression)
bf5c7c5fac93a2cb7615e6224b94b4356208ea354c807984b1c2a267cef881c2  KernelSU_themestore_32680_release.apk
e671fdd62150c41edd0332815e6335e54c1b536dff93ce38c433a5686b9e0e9f  Ghostlock_PD2118_KSU_v6_release.apk
```

v2 true / v3 false，probe 通过。源码包重打为 881 文件 / 8.9 MiB，
md5 `d70f760ea0a27dec0bdbd055df15e519`（含新增的 jniLibs 二进制，
对应 Rust 源码在 `KernelSU/userspace/ksud/`）。

注意：只有 arm64-v8a 的 ksud；x86_64 模拟器上模块读取仍会失败。

旧目录 `release/`、`release_v2/` 均已作废，分发只用 `release_v3/`。


### 0.19 追加（2026-11-23 05:22）：无 root 时隐藏底栏并禁用左右滑

用户指出原版行为：没有完整 root 权限时**不显示底部导航栏、也不能左右滑动**。
核对 Nova 参考实现，发现移植时两处判断被有意/无意去掉了：

1. `BottomBarMiuix` / `BottomBarMaterial` 里的
   `val fullFeatured = Natives.isFullFeatured(); if (!fullFeatured) return`
   被改成了 `@Suppress("UNUSED_VARIABLE")`（注释写着"保持栏可见以便进主题商店"）。
2. `MainActivity` 里 `userScrollEnabled` 从
   `remember(isFullFeatured) { mutableStateOf(isFullFeatured) }`
   改成了无条件 `mutableStateOf(true)`（注释说为了能进设置页）。

现已按上游恢复这两处：无完整权限时底栏组件直接 return，pager 手势关闭，
应用锁定在 Home 页；有 root 后行为不变。

产物 `release_v4/`：

```
df283cd917c0976c7662ea2a3d0123155441f7c414ddf150b3052ed524635795  KernelSU_themestore_noroot_32680_release.apk
e671fdd62150c41edd0332815e6335e54c1b536dff93ce38c433a5686b9e0e9f  Ghostlock_PD2118_KSU_v6_release.apk
```

仍含 `lib/arm64-v8a/libksud.so`，v2 true / v3 false。源码包重打 md5 `711bd39b471fbcc477c25815a4e30f4d`。
`release_v3/` 及更早目录作废。

副作用：无 root 时确实**无法再通过底栏进设置页/主题商店**，这是原版语义；
只有拿到 root 后才能切页。

---

## 1. 一句话现状

把 `SukiSU-Nova` 的主题商店、外观渲染、贴纸、图标包、自定义文字搬进了 KernelSU 管理器
（包名 `me.weishu.kernelsu`），保留了原有半透明视觉和布局；当前构建 versionCode 32680，
正式包与预览包各一份。商店的网络访问仍依赖用户自己配置访问令牌，未做端到端真机回归。

| 项 | 值 |
|---|---|
| 目标工程 | `<workspace>/ksu_port/KernelSU/manager` |
| 参考实现（只读） | `<workspace>/ksu_port/SukiSU-Nova` |
| 包名 | `me.weishu.kernelsu`（预览 `me.weishu.kernelsu.preview`） |
| namespace | `me.weishu.kernelsu`（预览工程 namespace 不变，只有 applicationId 变） |
| Git HEAD | `0c0fdb9 manager: Update Indonesian translations (#3748)`，工作区约 110 个变更文件 |
| JDK/Gradle | 随工程 wrapper；离线构建用 `--offline`，代理写在 `manager/gradle.properties` |

---

## 2. 构建与产物

### 2.1 命令

在 `<workspace>/ksu_port/KernelSU/manager` 下执行：

```bash
# 正式包名
./gradlew :app:assembleDebug -PKSU_VERSION_CODE_FALLBACK=32680 --offline

# 预览包名（可与正式版同时安装）
./gradlew :app:assembleDebug -PKSU_VERSION_CODE_FALLBACK=32680 \
  -PKSU_PACKAGE_NAME=me.weishu.kernelsu.preview \
  -PKSU_NAME="KernelSU Preview" --offline
```

**必须传 `KSU_VERSION_CODE_FALLBACK=32680`**：仓库是浅克隆，`git rev-list --count HEAD` 只有 1，
不传会算出 30001（见 `manager/build.gradle.kts:getVersionCode()`）。
只编译检查：`./gradlew :app:compileDebugKotlin --offline`。

### 2.2 当前产物

| 文件 | 包名 | md5 |
|---|---|---|
| `<workspace>/KernelSU_themestore_full_32680.apk` | `me.weishu.kernelsu` | `1a0f29a7993e91a8dffce1cc82c2ac6a` |
| `<workspace>/KernelSU_themestore_preview.apk` | `me.weishu.kernelsu.preview` | `b6415bea02d81b9247b462449f1b1cdb` |

两个变体**必须分别构建、构建完立刻复制**：Gradle 输出目录同名，后构建会覆盖前者。
校验工具：`/c/Android/Sdk/build-tools/37.0.0/aapt2.exe dump badging <apk>`。

### 2.3 内核模块签名匹配（已落地，见 0.2）

管理器 debug 签名证书 744 B / `f4abcd31…`；上游 release 是 827 B / `c371061b…`。
`kernel/Kbuild` 默认按后者校验，因此 debug 包管理器会被内核拒绝提权。
已用 `<workspace>/tools/patch_ksu_expected.py` 改三个 AArch64 立即数 + `.rodata` 指纹，
产出 `<workspace>/pd2055_asset/kernelsu_dbg.ko`，并回填进
`Ghostlock_PD2118_KSU_v6_ksu744.apk`。换签名或换内核源码后需要重跑这两步：

```bash
# 1. 先量出新管理器的证书长度与 SHA-256（内核视角，不是 apksigner 视角）
python tools/apk_cert_probe.py <new-manager.apk>

# 2. 打到模块里（827 是上游旧值，如果上一轮已改过就填上一轮的 --old-size）
python tools/patch_ksu_expected.py <ko> <new_sha256> --size <cert_size> --old-size 827

# 3. 回填进 APK 并重签（替换文件字节数必须与 asset 一致）
python tools/repack_apk_asset.py <in.apk> <out.apk> assets/kernelsu.ko <patched.ko>
```

`repack_apk_asset.py` 默认用 `~/.android/debug.keystore`（alias `androiddebugkey`，
口令 `android`），build-tools 路径可用环境变量 `ANDROID_BUILD_TOOLS` 覆盖。

---

## 3. 已完成功能与入口

### 3.1 主题商店

- 19 个 `ui/screen/themestore/*` 文件 + 账号页 + 导入导出对话框，路由 `ThemeStore` /
  `ThemeStoreAdaptiveUpload` / `ThemeStoreCurrentUpload` / `ThemeStoreSources` / `ThemeAccount`，
  都包在 `MaterialExpressivePalette` 里。
- 网络层 `data/repository/ThemeStore*`：base URL / token / 额外源来自设置页；
  **没有访问令牌时接口返回 `401 {"error":"访问令牌无效"}`**，无法离线看主题详情或预览包内容。

### 3.2 外观渲染（半透明背景）

- `MainActivity`：背景层 `AppBackgroundLayer` + `rememberLayerBackdrop()` 采样 + 提供
  `LocalBackgroundImagePath / LocalCardBackgroundAlpha / LocalAppBackgroundBackdrop /
  LocalEnableProgressiveBlur / LocalCustomTexts`；`NavDisplay` 在背景激活时换成
  `BackgroundNavTransition` 且 `dimAmount = 0f`。
- 14 个 `*Miuix.kt` 页面 `Scaffold` 显式传 `containerColor = … Transparent`（有背景时）；
  `MiuixTheme` 把 `surfaceContainer` 乘卡片透明度，让所有 miuix 卡片半透；
  `SearchBar` / `SuperSearchBar` / `BlurredBar` 做背景感知。
- 外观设置页（设置 → 主题）有：背景图选取/清除、模糊/遮罩/卡片不透明度三条滑杆、
  自定义文字与图标页入口、贴纸编辑器。

### 3.3 贴纸

- 入口：设置 → 主题 → 贴纸编辑区。预览卡上拖动改位置，缩略图切换选中，
  添加/删除照片，旋转/大小滑杆；拖动只改本地 draft，`onTransformCommit` / 滑杆松手才写 repo。
- 首页状态卡（Material + Miuix）渲染贴纸：`HomeCardStickerOverlayContainer` 用
  `onSizeChanged` 实测卡片尺寸，再以 `heightNormalized = true` 归一化，避免列表项高度不确定
  时贴纸跑到卡片外。
- `HomeViewModel` 监听 `settings` 的 `home_card_stickers` 键，跨页改动即时生效。

### 3.4 图标包 / 启动器图标

- 入口：设置 → 主题 → 应用图标。
- `AndroidManifest.xml`：MAIN/LAUNCHER 已从 `.ui.MainActivity` 移到 8 个 activity-alias
  （default/kernelsu/logo/zako × 动态色/静态），默认启用 `MainActivityAliasDefaultStatic`；
  另有 `AppIconShortcutActivity` 作为自定义图标快捷方式入口。
- `BuiltinAppIcons.apply/applyIfNeeded/isApplied` 用
  `PackageManager.setComponentEnabledSetting(..., DONT_KILL_APP)` 切换，只启用一个 alias。
- 自定义图标走 ZIP 图标包（`AppIconPackStorage`）+ `Shortcut.createAppIconShortcut`
  固定到桌面；Android 不允许运行时把应用图标换成任意图片，所以自定义图标不是应用图标本身。
- `KernelSUApplication` 在 App 退到后台 1 秒后 `applyIfNeeded` 恢复用户选择：
  更新会重置组件状态，但**启动或前台期间绝不能切组件**（部分 ROM 会杀掉当前任务）。

### 3.5 自定义文字

- 编辑器（设置 → 主题 → 自定义文字）按 4 组展示全部 key，支持编辑、恢复单条/全部。
- 已接线的渲染点：首页标题、首页状态文案（工作/未安装）、超级用户/模块/外观/
  自定义文字/图标页标题（两种 UI 模式）。**其余 key 只存储，尚未接到对应 UI。**

---

## 4. 关键实现决策与踩坑（接手必读）

1. **miuix `Scaffold` 的默认 containerColor 是 `colorScheme.surface`，不是 `background`。**
   只把 `MiuixTheme.background` 改透明对页面容器无效——这是"背景只在商店页生效"的根因。
2. **透明页面 + 默认导航转场 = 背景只显示半边**：被覆盖页以 alpha=1 留在下层，
   必须换成 `BackgroundNavTransition` 并关掉 idle dim。
3. **模糊采样透明像素会变黑**：底栏变黑是因为页面 backdrop 空层；
   页面层和壁纸层都要画半透底色，再用 `rememberCombinedBackdrop` 叠加采样。
4. **顶栏白色横带**来自平涂的 `surface.copy(alpha)` 回退；已改成三段竖向渐变纱罩
   （0.28 → 0.12 → transparent），与 Nova `TopBarVeil` 参数一致。
5. **`isFullFeatured()` 曾锁死整个 App**：底栏提前 return + 禁止翻页，导致无 root 时进不了设置。
   五个底栏文件的 gate 已移除；主题定制不需要 root。
6. **列表项贴纸不能用 `BoxWithConstraints.maxHeight`**（LazyColumn 里是 Infinity），
   必须测量实际尺寸。
7. **图标别名与启动器**：MainActivity 不再持有 LAUNCHER filter，否则会出现两个图标；
   覆盖安装后桌面图标可能需要重新添加。首次切换可能触发 launcher 刷新。
8. **浅克隆版本码**：见 2.1，所有构建命令都要带 fallback 参数。
9. **Kotlin Compose 注意**：`LaunchedEffect` 的 lambda 不是 composable，里面不能调
   `stringResource`；先在 composable 作用域算出字符串再捕获。

---

## 5. 已知差距 / 未做

| 项 | 说明 | 优先级 |
|---|---|---|
| 真机回归 | 本机无设备；贴纸位置、桌面图标切换、商店登录都未实测 | 高 |
| `ThemeExportDialog` 宿主 | 文件在，但**没有任何调用点**；主题包导出闭环缺失 | 高 |
| 主题商店访问令牌 | 无 token 时列表/详情 401，离线无法验证主题包内容 | 高（依赖外部） |
| 自定义文字剩余 key | 本轮补了首页统计/信息、超级用户头部、模块摘要；设置项与部分功能页标题仍未接线 | 中 |
| Nova 的 `TopBarVeil` 渐进模糊 | 需要 `rememberProgressiveBarBlurModifier`；目前用渐变纱罩替代 | 中 |
| Nova 首页 Hero 状态卡 | 经典卡的图片背景/隐藏选项、沉浸式布局已移植；Hero 内部装饰细节仍保留官方实现 | 低（用户决定） |
| `AppCard` / `backgroundAwareCardColors` | Miuix 原生 `Card` 与 Nova `AppCard` 仍有细节差异 | 低 |
| `ZipFileIntentHandler` / `ZipFileType` | 依赖 Nova 独有脚本/内核刷写，已删除；源文件在 `port-staging/` | 已决策 |
| 预览/试用链路 | `startThemeTrial` / `beginThemeTrial` / `restoreThemeTrial` 已移植，但**未真机验证** | 中 |

---

## 6. 真机验收清单

按此顺序测，能最快定位回归：

1. 覆盖安装正式包 → 桌面是否只有一个图标、能否启动（观察是否需要重新添加图标）。
2. 主页 → 右上角商店图标能否进商店；设置页能否滚动到底（无 root 也要能进）。
3. 设置 → 主题 → 选一张背景图 → 回主页：背景是否全屏、半透卡片、顶栏无硬边白条。
4. 超级用户 / 模块页：上半边透背景、底部胶囊不变黑；左右滑页时背景不闪左边。
5. 贴纸：添加 → 在预览卡上拖动 → 回主页看状态卡位置 → 旋转/大小 → 删除。
6. 应用图标：依次切 4 套内置图标 + 动态取色开关；重启后是否保持；导入一个 ZIP 图标包
   并长/点图标固定到桌面；删除包后已固定的快捷方式是否仍可用。
7. 自定义文字：改首页标题和状态文案 → 确认首页变化；改超级用户/模块标题 → 切页确认。
8. 主题包导入：带 `appIconId` / `customTexts` / 贴纸的包导入后是否都生效。

日志抓取：`adb logcat -s BuiltinAppIcons ModuleShortcut ThemeStore*`（adb 未在 PATH，
使用完整路径，当前无设备连接）。

---

## 7. 关键文件索引

**新增（主要）**
- `ui/screen/themestore/*`、`ui/screen/account/*`、`ui/component/themeimport/*`
- `ui/screen/customtext/CustomTextScreen.kt`
- `ui/screen/appicon/AppIconScreen.kt`
- `ui/component/HomeCardSticker.kt`
- `ui/util/{BackgroundNavTransition,CustomTextConfig,AppIconPackStorage,AdaptiveTheme,
  ImageStorage,ThemePackageStorage,ThemeTrialManager}.kt`
- `ui/AppIconShortcutActivity.kt`

**修改（主要）**
- `ui/MainActivity.kt`（背景层、CompositionLocal、路由注册）
- `ui/theme/{Theme,MaterialTheme,MiuixTheme,ThemeExt}.kt`
- `ui/util/BlurExt.kt`
- `ui/viewmodel/{MainActivityViewModel,SettingsViewModel,HomeViewModel}.kt`
- `ui/screen/home/{HomeMaterial,HomeMiuix,HomeUiState}.kt`
- `ui/screen/colorpalette/{ColorPaletteScreen,ColorPaletteScreenMaterial,
  ColorPaletteScreenMiuix,ColorPaletteUiState}.kt`
- `ui/component/bottombar/*.kt`、`ui/component/FloatingBottomBar.kt`
- `ui/util/module/Shortcut.kt`、`KernelSUApplication.kt`
- `AndroidManifest.xml`、`res/values*/strings.xml`、`res/values/colors.xml`

**参考源（只读）**
`SukiSU-Nova/manager/app/src/main/{java/com/sukisu/nova,res}`，特别是
`ui/MainActivity.kt`、`ui/util/BlurExt.kt`、`ui/component/HomeCardSticker.kt`、
`ui/screen/appicon/*`、`ui/util/BuiltinAppIcons.kt` 与 `AndroidManifest.xml`。

**历史记录**
`<workspace>/.workbuddy/memory/2026-11-21.md`（按时间倒序追加，
含每一轮 bug 的根因与修法）、`ksu_port/PORT_STATUS.md`（较早的阶段记录，部分条目已过时）。
