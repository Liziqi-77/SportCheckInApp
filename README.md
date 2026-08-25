# 运动打卡 App

轻量级 Android 日常运动打卡应用，按产品文档实现「首页打卡闭环 + 统计数据可视化」双核心能力，并为个人资料、提醒、备份、深色模式、成就墙预留扩展入口。

## 技术栈

- Kotlin + Jetpack Compose + Material 3
- MVVM：ViewModel + StateFlow
- Room 本地持久化
- WorkManager 每日维护任务
- MPAndroidChart 柱状图
- Navigation Compose 三 Tab 导航

## 运行环境

- Android Studio Otter 2 Feature Drop | 2025.2.2 或相近版本
- JDK 17
- Android SDK：compileSdk 35，minSdk 26，targetSdk 35
- Gradle 8.11.1（首次 Sync 自动下载）

## 打开方式

1. 解压源码包。
2. Windows 下建议把项目放到纯英文路径，例如 `C:\lzq\keep\SportCheckInApp`。
3. 用 Android Studio 打开项目根目录。
4. 等待 Gradle Sync 完成。
5. 运行 `app` 到模拟器或真机。

> 项目已在 `gradle.properties` 加入 `android.overridePathCheck=true` 用于绕过中文路径检查，但仍建议使用纯英文路径，避免 NDK/工具链对非 ASCII 路径敏感。

## 功能清单

### 已实现

- [x] 三 Tab 底部导航：首页 / 统计 / 我的
- [x] 首页运动项目卡片列表
- [x] 新建任意数量运动项目
- [x] 长按卡片编辑项目名称与每日目标时间
- [x] 删除运动项目（首页左滑触发确认弹窗，确认后才级联删除该项目历史打卡记录；取消则保留）
- [x] 今日一键打卡 / 取消打卡
- [x] 已完成项目自动沉底，未完成项目保持在上方
- [x] 已完成项目卡片背景设置为浅绿色
- [x] 打卡记录按 `yyyy-MM-dd` 存储，跨自然日后首页自动进入新打卡日
- [x] WorkManager 每日维护任务，清理 370 天前历史记录
- [x] 首页每 30 秒检查日期变化，前台跨天后自动刷新勾选状态
- [x] 运动时间滚轮选择：小时 0-23、分钟 0-59
- [x] 滚轮禁止手动输入数字，组合范围校验为 1 ~ 1440 分钟
- [x] 统计页三段式结构：顶部日历、中部柱状图、底部数据摘要卡
- [x] 日历月视图 / 年视图切换
- [x] 年视图热力图严格按星期一列对齐
- [x] 日历状态颜色：绿色全部完成、黄色部分完成、灰色无记录
- [x] 点击日期弹出当日打卡明细 BottomSheet
- [x] 历史日期补卡 / 取消补卡
- [x] 修改历史记录分钟数（保存后该项目当天标记为已完成）
- [x] 未来日期禁止补卡
- [x] 新增运动项目按创建日开始生效，不影响历史日期的完成度与明细
- [x] 柱状图使用 MPAndroidChart
- [x] 周视图：每日 7 根柱子，刚好完整展示，不需要滑动
- [x] 月视图：每日 28-31 根柱子，支持横向滚动与双指缩放
- [x] 季度视图：按周聚合，约 13 根柱子
- [x] 年视图：按月聚合，12 根柱子
- [x] 点击柱子显示 MPAndroidChart MarkerView 气泡提示
- [x] 数据摘要卡：总运动时长、平均每日运动时长
- [x] 总时长超过 60 分钟自动显示为「X小时X分钟」
- [x] 日均时长按周期总天数计算并保留 1 位小数
- [x] 统计周期切换：上一段 / 下一段 / 回到今天
- [x] 修复柱状图维度切换后的缩放状态污染：月/季/年缩放后回到周视图，周视图仍保持 7 根完整展示
- [x] 个人中心预留入口 UI：个人资料、备份恢复、深色模式、勋章墙
- [x] 每日打卡提醒真实本地通知：我的页面开关控制，默认每天 20:00，Android 13+ 自动请求通知权限
- [x] 提醒可用性检测与系统设置引导：检测通知开关，引导打开通知、自启动、关闭电池优化
- [x] 浅色活力橙品牌主题，薄荷绿/警示黄状态色
- [x] 首页空状态引导、统计页无项目提示
- [x] 矢量 Tab 图标、摘要图标、自适应启动图标
- [x] Windows 中文路径构建兼容开关

### 未开发 / 待确认

- [ ] 实际运动时长单独录入（当前打卡默认完成项目目标分钟）
- [ ] 取消打卡留痕（当前取消勾选会删除当天该项目记录）
- [ ] 季度聚合严格 ISO 周规则可配置
- [ ] 日均时长可选「只统计截至今天」而非完整周期天数
- [ ] 数据备份与恢复真实能力
- [ ] 深色模式真实切换并完善深色配色
- [ ] 成就勋章墙真实规则与页面
- [ ] 单元测试 / UI 测试

## 关键实现说明

- 数据库两张表：`exercise_projects` 存项目；`check_in_records` 存打卡，主键为 `projectId + date`。
- `exercise_projects.startDate` 表示项目生效日；统计日历/明细只把 `startDate <= 当前日期` 的项目计入完成度，因此新增项目不影响历史。
- 勾选完成时，会把项目当前 `targetMinutes` 写入当天记录；统计页按已完成记录的 `minutes` 求和。
- 修改项目目标时间后，如果今天已打卡，会同步更新今天记录分钟数。
- 统计图维度切换使用 `key(ui.dimension)` 重建图表，避免月视图缩放状态污染周/季/年视图。

## 修改记录

- 2026-07-21：初始化完整 Android Studio 项目结构与核心功能。
- 2026-07-21：修复 Windows 中文路径导致 AGP 中断，修改 `gradle.properties`。
- 2026-07-21：修复统计图维度切换后周视图被缩放/可滑动的问题，修改 `app/src/main/java/com/example/sportcheckin/ui/stats/StatsScreen.kt`。
- 2026-07-21：新增本 README，修改 `README.md`。
- 2026-07-21：首页新增左滑删除手势、已完成项目沉底与浅绿色卡片，修改 `app/src/main/java/com/example/sportcheckin/ui/home/HomeScreen.kt`、`app/src/main/java/com/example/sportcheckin/ui/home/HomeViewModel.kt`、`README.md`。
- 2026-07-21：新增历史日期补卡/修改历史记录分钟数，修改 `app/src/main/java/com/example/sportcheckin/ui/stats/StatsScreen.kt`、`app/src/main/java/com/example/sportcheckin/ui/stats/StatsViewModel.kt`、`app/src/main/java/com/example/sportcheckin/data/SportRepository.kt`、`README.md`。
- 2026-07-21：新增运动项目生效日，新增项目只影响当天和未来，不影响历史完成度；修改 `app/src/main/java/com/example/sportcheckin/data/Entities.kt`、`app/src/main/java/com/example/sportcheckin/data/AppDatabase.kt`、`app/src/main/java/com/example/sportcheckin/data/SportRepository.kt`、`app/src/main/java/com/example/sportcheckin/ui/stats/StatsViewModel.kt`、`README.md`。
- 2026-07-21：左滑删除增加二次确认，取消则不删除；修改 `app/src/main/java/com/example/sportcheckin/ui/home/HomeScreen.kt`、`README.md`。
- 2026-07-21：年视图热力图严格按星期一列对齐，修改 `app/src/main/java/com/example/sportcheckin/ui/stats/StatsViewModel.kt`、`app/src/main/java/com/example/sportcheckin/ui/components/CalendarViews.kt`、`README.md`。
- 2026-07-21：图表点击提示从 Toast 换成 MarkerView 气泡，修改 `app/src/main/java/com/example/sportcheckin/ui/components/SportBarChart.kt`，新增 `app/src/main/java/com/example/sportcheckin/ui/components/SportMarkerView.kt`、`app/src/main/res/layout/chart_marker_view.xml`、`app/src/main/res/drawable/bg_chart_marker.xml`、`README.md`。
- 2026-07-21：新增每日打卡提醒真实本地通知，修改 `app/src/main/AndroidManifest.xml`、`app/src/main/java/com/example/sportcheckin/SportCheckInApplication.kt`、`app/src/main/java/com/example/sportcheckin/ui/profile/ProfileScreen.kt`，新增 `app/src/main/java/com/example/sportcheckin/ui/profile/ProfileViewModel.kt`、`app/src/main/java/com/example/sportcheckin/reminder/ReminderPreferences.kt`、`app/src/main/java/com/example/sportcheckin/reminder/ReminderScheduler.kt`、`app/src/main/java/com/example/sportcheckin/reminder/ReminderWorker.kt`、`README.md`。
- 2026-07-21：新增提醒可用性检测与系统设置引导，修改 `app/src/main/java/com/example/sportcheckin/ui/profile/ProfileScreen.kt`，新增 `app/src/main/java/com/example/sportcheckin/reminder/SystemSettingsNavigator.kt`、`README.md`。
