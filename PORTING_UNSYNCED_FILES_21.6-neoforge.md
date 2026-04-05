# NeoForge 26.1 端口：仍旧不同步的文件说明

> 最后验证时间：2026-04-05  
> 构建验证：`JAVA_HOME=D:\Applications\Scoop\apps\temurin-jdk\current` 后执行 `./gradlew.bat compileJava --console=plain`，结果为 **`BUILD SUCCESSFUL`**（仅剩 33 条 warning）

---

## 1. 仍然**有意保持不同步**的文件

这些文件不是漏改，而是当前 NeoForge 端口里**刻意保留的差异**。

| 文件 | 当前状态 | 不同步原因 | 后续处理建议 |
|---|---|---|---|
| `src/main/java/snownee/kiwi/customization/compat/jade/JadeCompat.java` | 暂时保留为 no-op 占位实现 | 已实测：当前缓存的 `jade-15.9.1+neoforge` 仍暴露 `ResourceLocation` 型 provider API，而本工作区映射侧仍是 `Identifier` 风格；直接恢复 Fabric 终态实现会重新触发 `getUid()` 返回类型不兼容等编译错误。 | 等 Jade 依赖/映射对齐后再恢复真实 provider 实现。 |
| `invalid/src/main/java/snownee/kiwi/customization/compat/emi/**` | 已从主源码集移出，保存在 `invalid/` | 按本次移植约束，**EMI 在 NeoForge 分支保持禁用**；代码保留仅用于后续参考，避免丢失 viewer 逻辑。 | 仅在确认 NeoForge 26.1 上的 EMI 兼容链路可用后再回迁。 |
| `invalid/src/main/java/snownee/kiwi/customization/compat/rei/**` | 已从主源码集移出，保存在 `invalid/` | 按本次移植约束，**REI 在 NeoForge 分支保持禁用**；当前不应进入正式源码集。 | 若未来决定重新启用 REI，再单独做 compat 回迁与验证。 |
| `invalid/src/main/java/snownee/kiwi/mixin/minieffects/rei/**` | 跟随 REI 一并移出主源码集 | 这些 mixin 依赖 REI 客户端路径；既然 REI 当前禁用，这部分也必须保持停用，避免误加载。 | 仅在 REI 恢复时一并回迁。 |

---

## 2. 属于**预期的 NeoForge 平台分歧**（不是 backlog）

下面这些文件仍和 Fabric 端不完全一致，但这是**平台适配后的预期结果**，不是“未同步完成”。

| 文件 | 为什么必须保留差异 |
|---|---|
| `src/main/java/snownee/kiwi/loader/Platform.java` | 物理端判断、生产环境判断、资源查找、合成剩余物等都必须走 NeoForge/FancyModLoader 的 API，例如 `FMLEnvironment.getDist().isClient()`；不能和 Fabric 实现做 1:1 同步。 |
| `src/main/java/snownee/kiwi/loader/ClientPlatform.java` | 客户端渲染器、粒子、颜色处理在 NeoForge 26.1 通过事件总线注册，不再是 Fabric 的同一套入口。 |
| `src/main/java/snownee/kiwi/util/ClientProxy.java` | `RegisterColorHandlersEvent.BlockTintSources`、`ScreenEvent` 等均为 NeoForge 事件式接入，和 Fabric 客户端 hook 设计不同。 |
| `src/main/resources/kiwi.mixins.json`、`src/main/java/snownee/kiwi/mixin/customization/CrossCollisionBlockAccessor.java`、`src/main/java/snownee/kiwi/mixin/customization/WallBlockAccessor.java` | 26.1 的形状缓存字段访问方式发生变化，NeoForge 端需要 accessor mixin 才能替换 `CrossCollisionBlock` / `WallBlock` 的 shape provider。 |
| `.gitignore` | 增加了 `context/` 忽略规则，仅用于本地对照源码和工作区清洁，不属于上游 Fabric 功能逻辑。 |

---

## 3. 已确认**不再属于“未同步”**的问题

以下项目这次已经完成处理，因此**不再列入剩余不同步清单**：

- `Platform.isPhysicalClient()` 已改为 `FMLEnvironment.getDist().isClient()`，并已通过编译验证。
- `KiwiDataGen.java` / `KiwiLanguageProvider.java` / `KiwiBlockLoot.java` 当前已是可编译的 NeoForge datagen 路径，不再是本轮的阻塞项。
- 自定义 block/item/recipe/customization 大批 26.1 API 适配已完成，当前主线构建已恢复成功。

---

## 4. 结论

当前需要继续关注的“仍不同步”文件，主要就是：

1. `JadeCompat.java` 的临时占位实现；
2. `invalid/` 下被刻意停用的 EMI / REI 相关代码；
3. 少量必须保留的 NeoForge-only 平台差异文件。

除此之外，本轮已修复的主线代码已经达到 **可编译、可继续分主题提交** 的状态。
