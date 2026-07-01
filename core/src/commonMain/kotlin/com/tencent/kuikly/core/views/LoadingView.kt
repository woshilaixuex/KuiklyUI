/*
 * Tencent is pleased to support the open source community by making KuiklyUI
 * available.
 * Copyright (C) 2025 Tencent. All rights reserved.
 * Licensed under the License of KuiklyUI;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://github.com/Tencent-TDS/KuiklyUI/blob/main/LICENSE
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.kuikly.core.views

import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.timer.setTimeout

/**
 * Loading 状态控制器，管理显示/隐藏状态及超时自动关闭。
 *
 * 通过 [Pager.createLoadingController] 创建，在 body() 里用 [Loading] DSL 绑定：
 *
 * ```kotlin
 * @Page("MyPage")
 * class MyPage : Pager() {
 *     lateinit var loading: LoadingController
 *
 *     override fun willInit() {
 *         super.willInit()
 *         loading = createLoadingController()
 *     }
 *
 *     override fun body(): ViewBuilder = {
 *         // 局部遮罩：覆盖父容器
 *         View {
 *             attr { size(300f, 200f) }
 *             Loading(loading)
 *         }
 *         // 全屏遮罩：挂到页面顶层
 *         Loading(loading, fullScreen = true)
 *     }
 *
 *     fun fetchData() {
 *         loading.show(timeoutMs = 5000)   // 5 秒后自动关闭
 *         // ... 异步操作完成后 ...
 *         loading.hide()
 *     }
 * }
 * ```
 */
class LoadingController internal constructor(private val pagerId: String) {

    /**
     * 当前可见状态，可在 vif/body 中读取以建立响应式依赖。
     * 写操作请通过 [show] / [hide] 进行。
     */
    var isVisible: Boolean by observable(false)
        private set

    // 用于防止旧的超时回调误关闭新一轮 show
    private var generation = 0

    /**
     * 显示 Loading。
     * @param timeoutMs 超时自动关闭时间（毫秒）。0 表示不自动关闭。
     */
    fun show(timeoutMs: Int = 0) {
        isVisible = true
        val current = ++generation
        if (timeoutMs > 0) {
            setTimeout(pagerId, timeoutMs) {
                if (generation == current) {
                    isVisible = false
                }
            }
        }
    }

    /**
     * 隐藏 Loading，同时使所有待执行的超时回调失效。
     */
    fun hide() {
        generation++
        isVisible = false
    }
}

/**
 * 在 [Pager] 中创建 [LoadingController]。
 * 推荐在 [Pager.willInit] 中调用，此时 pagerId 已就绪。
 */
fun Pager.createLoadingController(): LoadingController = LoadingController(pagerId)

/**
 * Loading 遮罩 DSL 组件，绑定 [LoadingController]，状态变化时自动显示/隐藏。
 *
 * @param controller  [LoadingController] 实例，控制显示/隐藏
 * @param fullScreen  true = 全屏遮罩（挂到页面顶层，阻断所有交互）；
 *                    false = 局部遮罩（覆盖父容器，默认）
 * @param maskColor   遮罩背景色，默认半透明黑色；传 null 不显示蒙版
 * @param grayStyle   true = 灰色菊花（适合浅色背景）；false = 白色菊花（默认）
 * @param scale       菊花缩放倍数，默认 1f（20×20pt）；传 2f 则为 40×40pt
 */
fun ViewContainer<*, *>.Loading(
    controller: LoadingController,
    fullScreen: Boolean = false,
    maskColor: Color? = Color(0, 0, 0, 0.3f),
    grayStyle: Boolean = false,
    scale: Float = 1f,
) {
    vif({ controller.isVisible }) {
        Loading(
            fullScreen = fullScreen,
            maskColor = maskColor,
            grayStyle = grayStyle,
            scale = scale,
        )
    }
}
