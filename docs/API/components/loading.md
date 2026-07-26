# Loading(加载遮罩)

基于 `ActivityIndicator` 实现的加载遮罩组件，支持全屏和局部两种模式，提供状态管理与超时自动关闭能力。

[组件使用示例](https://github.com/Tencent-TDS/KuiklyUI/blob/main/demo/src/commonMain/kotlin/com/tencent/kuikly/demo/pages/demo/kit_demo/DeclarativeDemo/LoadingExamplePage.kt)

## 快速开始

通过 `LoadingController` 管理显示/隐藏，在 `body()` 中用 `Loading(controller)` DSL 绑定：

```kotlin
@Page("demo_page")
internal class TestPage : BasePager() {
    lateinit var loading: LoadingController

    override fun willInit() {
        super.willInit()
        loading = createLoadingController()
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            // 局部遮罩：覆盖父容器
            View {
                attr { height(200f) }
                Loading(ctx.loading)
            }
            // 全屏遮罩：挂到页面顶层
            Loading(ctx.loading, fullScreen = true)
        }
    }

    fun onLoad() {
        loading.show(timeoutMs = 5000)  // 5s 后自动关闭
        // 或手动关闭
        // loading.hide()
    }
}
```

## LoadingController

状态控制器，通过 `Pager.createLoadingController()` 在 `willInit()` 中创建。

### show

显示 Loading 遮罩。

| 参数 | 描述 | 类型 | 默认值 |
| -- | -- | -- | -- |
| timeoutMs | 超时自动关闭时间（毫秒） | Int | 0（不自动关闭） |

多次调用 `show()` 会重置超时计时，前一次的超时回调不会影响新的 `show()`。

### hide

隐藏 Loading 遮罩，同时使所有待执行的超时回调失效。

## Loading DSL

### 控制器绑定版本

```kotlin
fun ViewContainer<*, *>.Loading(
    controller: LoadingController,
    fullScreen: Boolean = false,
    maskColor: Color? = Color(0, 0, 0, 0.3f),
    grayStyle: Boolean = false,
    scale: Float = 1f,
)
```

### 无状态版本

```kotlin
fun ViewContainer<*, *>.Loading(
    fullScreen: Boolean = false,
    maskColor: Color? = Color(0, 0, 0, 0.3f),
    grayStyle: Boolean = false,
    scale: Float = 1f,
)
```

### 参数说明

| 参数 | 描述 | 类型 | 默认值 |
| -- | -- | -- | -- |
| controller | LoadingController 实例，控制显隐 | LoadingController | - |
| fullScreen | true = 全屏遮罩（通过 Modal 挂顶层）；false = 局部 | Boolean | false |
| maskColor | 遮罩背景色，传 null 不显示蒙版 | Color? | Color(0,0,0,0.3) |
| grayStyle | true = 灰色菊花；false = 白色菊花 | Boolean | false |
| scale | 菊花缩放倍数（原始大小 20×20pt） | Float | 1f |

:::tabs

@tab:active 示例

```kotlin{17-37}
@Page("demo_page")
internal class TestPage : BasePager() {
    lateinit var partialLoading: LoadingController
    lateinit var fullScreenLoading: LoadingController

    override fun willInit() {
        super.willInit()
        partialLoading = createLoadingController()
        fullScreenLoading = createLoadingController()
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            // 局部遮罩
            View {
                attr {
                    height(160f)
                    backgroundColor(Color(0xFF4A90E2))
                }
                Loading(ctx.partialLoading, scale = 1.5f)
            }
            Button {
                attr { titleAttr { text("局部 Loading（2s）") } }
                event { click { ctx.partialLoading.show(2000) } }
            }

            // 全屏遮罩
            View {
                Button {
                    attr { titleAttr { text("全屏 Loading（3s）") } }
                    event { click { ctx.fullScreenLoading.show(3000) } }
                }
            }
            Loading(ctx.fullScreenLoading, fullScreen = true, scale = 2f)
        }
    }
}
```

@tab 效果

<div align="center">
<img src="./img/loading_view.png" style="width: 30%; border: 1px gray solid">
</div>

:::
