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

package com.tencent.kuikly.demo.pages.demo.kit_demo.DeclarativeDemo

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.views.Loading
import com.tencent.kuikly.core.views.LoadingController
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.tencent.kuikly.core.views.compose.Button
import com.tencent.kuikly.core.views.createLoadingController
import com.tencent.kuikly.demo.pages.base.BasePager
import com.tencent.kuikly.demo.pages.demo.base.NavBar

@Page("LoadingExamplePage")
internal class LoadingExamplePage : BasePager() {

    // 局部和全屏各用独立 controller
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
            NavBar { attr { title = "Loading Example" } }

            // ---- 局部遮罩 ----
            ViewExampleSectionHeader { attr { title = "局部遮罩（覆盖父容器，默认）" } }
            View {
                attr {
                    height(160f)
                    backgroundColor(Color(0xFF4A90E2))
                    borderRadius(12f)
                    margin(16f)
                    allCenter()
                }
                Text {
                    attr {
                        text("内容区域")
                        fontSize(18f)
                        color(Color.WHITE)
                    }
                }
                // 局部：Loading 声明在目标容器内部，覆盖该容器
                Loading(ctx.partialLoading, scale = 1.5f)
            }
            View {
                attr { allCenter(); marginBottom(24f) }
                Button {
                    attr {
                        width(220f); height(44f); borderRadius(22f)
                        backgroundColor(Color(0xFF4A90E2))
                        titleAttr { text("触发局部 Loading（2s）"); color(Color.WHITE); fontSize(15f) }
                    }
                    event {
                        click { ctx.partialLoading.show(timeoutMs = 2000) }
                    }
                }
            }

            // ---- 全屏遮罩 ----
            ViewExampleSectionHeader { attr { title = "全屏遮罩（挂到页面顶层）" } }
            View {
                attr { allCenter(); marginBottom(24f) }
                Button {
                    attr {
                        width(220f); height(44f); borderRadius(22f)
                        backgroundColor(Color(0xFF333333))
                        titleAttr { text("触发全屏 Loading（3s）"); color(Color.WHITE); fontSize(15f) }
                    }
                    event {
                        click { ctx.fullScreenLoading.show(timeoutMs = 3000) }
                    }
                }
            }

            // 全屏：Loading 声明在页面根层，fullScreen=true 挂到顶层
            Loading(ctx.fullScreenLoading, fullScreen = true, scale = 2f)
        }
    }
}
