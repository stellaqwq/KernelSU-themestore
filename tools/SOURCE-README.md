# KernelSU 管理器「SukiSU Nova 外观」移植版 —— 源码包

本包是随分发的 APK 一起提供的**对应源码**，用于满足 GPL 的源码提供义务，也方便任何人
自行编译、审计和二次修改。

## 上游与许可证

| 组件 | 来源 | 许可证 |
|---|---|---|
| 管理器（`KernelSU/manager`） | KernelSU v3.3.1 | GPL-3.0 |
| 外观/主题系统（移植进来的代码） | SukiSU Nova | GPL-3.0 |
| 内核模块（`kernelsu.ko`） | PD2055 预编译模块，本仓库只做二进制补丁 | GPL-2.0 |
| `ghostlock/` | 本项目自有代码 | 随包分发，见下 |

三个许可证全文在 `LICENSES/` 下。分发本包的二进制产物时请一并保留这些文件。

## 目录

```
KernelSU/          管理器源码（含本项目的全部修改）
ghostlock/         临时 root 工具源码（C）+ 宿主机脚本
tools/             补丁 / 重打包 / 重签 / 校验脚本
docs/HANDOFF.md    移植交接文档：改了什么、为什么这么改、踩过哪些坑
LICENSES/          GPL-3.0 ×2、GPL-2.0
```

## 本项目的修改点

管理器侧（均在 `KernelSU/manager/app/src/main/java/me/weishu/kernelsu/`）：

1. 移植 SukiSU Nova 的主题商店、外观覆盖（配色/图片效果）、背景壁纸、首页状态卡
   （经典 + 沉浸式）、卡片贴纸、底部导航图标、自定义文字、自适应主题、主题包导入导出；
2. 首页/超级用户/模块三个页面补上 Nova 的板块布局（统计行、信息表格、摘要头、过滤 chips）；
3. 修复状态卡在「隐藏文字 + 隐藏装饰」主题下高度塌缩为 0 的问题（`heightIn(min = 188.dp)`）；
4. 状态卡与顶栏改用 `backgroundAwareColor()`，避免自定义壁纸上出现白色块；
5. `BlurredBar` 补回参考实现的 `scrollBehavior` 判定：静止时不绘制，滚动后才出现模糊/着色；
6. 设置页新增「清除自定义外观」，一键复位外观相关偏好并删除对应图片文件。

`ghostlock/` 侧：`bind_manager.sh` / `bind_manager.ps1` 用于在管理器重装（uid 变化）后
重新认领；`kernelsu_dbg.ko` 是把内核模块里的期望证书指纹改成调试证书后的产物。

## 编译管理器

```bash
cd KernelSU/manager
# sdk.dir 指向你的 Android SDK
echo 'sdk.dir=/path/to/Android/Sdk' > local.properties
./gradlew :app:assembleDebug -PKSU_VERSION_CODE_FALLBACK=32680 --offline
```

依赖：JDK 21+（本机用 25）、Android SDK 37 / build-tools 37.0.0 / NDK 29.0.14206865、
Gradle 9.7.1（随 wrapper）。首次构建需联网拉依赖，之后可 `--offline`。

演示包（无内核驱动也显示"工作中"状态，仅用于看主题效果）：

```bash
./gradlew :app:assembleDebug -PKSU_VERSION_CODE_FALLBACK=32680 \
  -PKSU_MOCK_ACTIVE=true -PKSU_NAME="KernelSU Demo" --offline
```

## 换自己的签名并重打模块补丁

分发前**务必**换成自己的密钥，否则任何人都能用公开的 debug 密钥伪造更新
（内核模块认的就是这个证书指纹）：

```bash
python tools/release_sign.py --storepass <你的口令> \
    --manager  <原始管理器.apk> \
    --ghostlock <原始 Ghostlock.apk> \
    --outdir release
```

脚本会生成 RSA-2048 密钥（**必须 2048**：内核的证书缓冲上限 1024 字节，4096 位证书约
1.4 KB 会被直接拒绝）、导出证书、把新指纹打进 `kernelsu.ko`、替换 Ghostlock 内嵌的模块、
重签两个 APK，并用与内核相同的解析逻辑校验结果。管理器必须**只带 v2 签名块**
（脚本已强制关闭 v1/v3，内核遇到 v3 会拒绝整包）。

## 已知限制

- 内核模块是 PD2055 的预编译产物，靠 koreloc 在 PD2118 / 4.19.152 上按当次 KASLR 重定位，
  仅适用于该机型与该内核版本；重启后失效，需要重新提权。
- 上游未提供 PD2055 内核模块的源码，因此 GPL-2.0 意义上的"对应源码"无法完整提供，
  本包只包含对该二进制的补丁脚本与产物说明。若你需要合规分发，请自行取得内核源码。
- 主题商店需要使用者自备访问令牌；商店内容与主题包素材的版权归各自作者。
- 本包**不含** APK 产物，也**不含**设备现场调试日志（其中记录了设备序列号等标识），
  需要复现设备侧行为请参考 `docs/HANDOFF.md` 的步骤自行采集。

## 免责

本项目仅用于在**自己拥有**的设备上做系统研究与个性化，不提供任何担保。
刷机、提权可能导致数据丢失、设备变砖、保修失效，风险自负。
