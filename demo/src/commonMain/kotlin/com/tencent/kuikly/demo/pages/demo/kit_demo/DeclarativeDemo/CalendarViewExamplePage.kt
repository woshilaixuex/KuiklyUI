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
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.CalendarDate
import com.tencent.kuikly.core.views.CalendarView
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.tencent.kuikly.demo.pages.base.BasePager
import com.tencent.kuikly.demo.pages.demo.base.NavBar

@Page("CalendarViewExamplePage")
internal class CalendarViewExamplePage : BasePager() {

    private var selectedDateText: String by observable("点击日期开始选择")

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            NavBar {
                attr { title = "CalendarView Example" }
            }

            // ===== 选中结果展示 =====
            View {
                attr {
                    height(48f)
                    allCenter()
                    backgroundColor(Color(0xFFF5F5F5))
                    marginLeft(16f)
                    marginRight(16f)
                    borderRadius(8f)
                    marginBottom(8f)
                }
                Text {
                    attr {
                        text(ctx.selectedDateText)
                        fontSize(15f)
                        color(Color(0xFF4A90E2))
                        fontWeightBold()
                    }
                }
            }

            // ===== 日历组件 =====
            ViewExampleSectionHeader {
                attr { title = "日历（默认当月，无预选日期）" }
            }
            CalendarView {
                attr {
                    size(360f, 360f)
                }
                event {
                    onDateSelected = { date ->
                        ctx.selectedDateText =
                            "选中: ${date.year}-${date.month}-${date.day} (${date.timeInMillis})"
                    }
                }
            }

            // ===== 指定初始月份的日历 =====
            ViewExampleSectionHeader {
                attr { title = "日历（指定2024年1月，预选22日）" }
            }
            CalendarView {
                attr {
                    size(360f, 360f)
                    initialDate(2024, 1, 22)
                }
                event {
                    onDateSelected = { date ->
                        ctx.selectedDateText =
                            "选中: ${date.year}-${date.month}-${date.day} (${date.timeInMillis})"
                    }
                }
            }
        }
    }
}
