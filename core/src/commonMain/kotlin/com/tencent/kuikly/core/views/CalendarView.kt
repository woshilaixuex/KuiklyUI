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
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.directives.vbind
import com.tencent.kuikly.core.manager.PagerManager
import com.tencent.kuikly.core.module.CalendarModule
import com.tencent.kuikly.core.module.ICalendar
import com.tencent.kuikly.core.reactive.handler.observable

/**
 * 日历选中日期回调数据。
 *
 * @param year 年
 * @param month 月（1-12）
 * @param day 日（1-31）
 * @param timeInMillis epoch 毫秒时间戳
 */
data class CalendarDate(
    val year: Int,
    val month: Int,
    val day: Int,
    val timeInMillis: Long
)

/**
 * 日历网格组件。提供月份切换、日期选择功能。
 *
 * 使用示例：
 * ```kotlin
 * CalendarView {
 *     attr {
 *         size(350f, 320f)
 *         initialDate(2026, 7)
 *     }
 *     event {
 *         onDateSelected = { date ->
 *             Log.i("选中: ${date.year}-${date.month}-${date.day}")
 *         }
 *     }
 * }
 * ```
 */
class CalendarView : ComposeView<CalendarViewAttr, CalendarViewEvent>() {

    /** 当前显示的年份 */
    var currentYear: Int by observable(2026)
    /** 当前显示的月份（0-11） */
    var currentMonth: Int by observable(0)
    /** 选中的日（0 = 未选中） */
    var selectedDay: Int by observable(0)
    /** 选中日所在的月份（0-11） */
    var selectedMonth: Int by observable(0)
    /** 选中日所在的年份 */
    var selectedYear: Int by observable(0)

    override fun createAttr(): CalendarViewAttr = CalendarViewAttr()
    override fun createEvent(): CalendarViewEvent = CalendarViewEvent()

    /** 日期格子 */
    private data class CalendarCell(
        val year: Int,
        val month: Int,       // 0-11
        val day: Int,
        val isCurrentMonth: Boolean
    )

    // ==================== 日期计算辅助 ====================

    private fun getFirstDayOfWeek(year: Int, month: Int): Int {
        val calendar = PagerManager.getCurrentPager()
            .acquireModule<CalendarModule>(CalendarModule.MODULE_NAME)
            .newCalendarInstance()
        calendar.set(ICalendar.Field.YEAR, year)
        calendar.set(ICalendar.Field.MONTH, month)
        calendar.set(ICalendar.Field.DAY_OF_MONTH, 1)
        return calendar.get(ICalendar.Field.DAY_OF_WEEK) // 1=周日
    }

    private fun getDaysInMonth(year: Int, month: Int): Int {
        val daysInFebruary = if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        val daysInMonth = intArrayOf(31, daysInFebruary, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        return daysInMonth[month]
    }

    private fun dateToTimeMillis(year: Int, month: Int, day: Int): Long {
        val calendar = PagerManager.getCurrentPager()
            .acquireModule<CalendarModule>(CalendarModule.MODULE_NAME)
            .newCalendarInstance()
        calendar.set(ICalendar.Field.YEAR, year)
        calendar.set(ICalendar.Field.MONTH, month)
        calendar.set(ICalendar.Field.DAY_OF_MONTH, day)
        return calendar.timeInMillis()
    }

    /**
     * 构造当月完整的 42 格日期（6 行 × 7 列）。
     */
    private fun buildCells(year: Int, month: Int): List<CalendarCell> {
        val firstDayOfWeek = getFirstDayOfWeek(year, month)
        val daysInMonth = getDaysInMonth(year, month)

        val prevMonth = if (month == 0) 11 else month - 1
        val prevYear = if (month == 0) year - 1 else year
        val prevMonthDays = getDaysInMonth(prevYear, prevMonth)

        val cells = mutableListOf<CalendarCell>()

        // 上月尾部填充
        val startOffset = firstDayOfWeek - 1
        for (i in (prevMonthDays - startOffset + 1)..prevMonthDays) {
            cells.add(CalendarCell(prevYear, prevMonth, i, false))
        }

        // 当月日期
        for (i in 1..daysInMonth) {
            cells.add(CalendarCell(year, month, i, true))
        }

        // 下月头部填充
        val nextMonth = if (month == 11) 0 else month + 1
        val nextYear = if (month == 11) year + 1 else year
        var nextDay = 1
        while (cells.size < 42) {
            cells.add(CalendarCell(nextYear, nextMonth, nextDay, false))
            nextDay++
        }

        return cells
    }

    override fun body(): ViewBuilder {
        val ctx = this

        // 获取今天日期（仅一次，避免单元格内重复调用原生）
        val nowCalendar = PagerManager.getCurrentPager()
            .acquireModule<CalendarModule>(CalendarModule.MODULE_NAME)
            .newCalendarInstance()
        val todayYear = nowCalendar.get(ICalendar.Field.YEAR)
        val todayMonth = nowCalendar.get(ICalendar.Field.MONTH)
        val todayDay = nowCalendar.get(ICalendar.Field.DAY_OF_MONTH)

        // 初始化：优先 attr.initialDate，否则用当前日期
        if (ctx.attr.initialYear > 0) {
            ctx.currentYear = ctx.attr.initialYear
            ctx.currentMonth = ctx.attr.initialMonth
            if (ctx.attr.initialDay > 0) {
                ctx.selectedYear = ctx.attr.initialYear
                ctx.selectedMonth = ctx.attr.initialMonth
                ctx.selectedDay = ctx.attr.initialDay
            }
        } else {
            val calendar = PagerManager.getCurrentPager()
                .acquireModule<CalendarModule>(CalendarModule.MODULE_NAME)
                .newCalendarInstance()
            ctx.currentYear = calendar.get(ICalendar.Field.YEAR)
            ctx.currentMonth = calendar.get(ICalendar.Field.MONTH)
        }

        return {
            View {
                attr {
                    flexDirectionColumn()
                }

                // ===== Header：月份标题 + 左右箭头 =====
                View {
                    attr {
                        flexDirectionRow()
                        justifyContentSpaceBetween()
                        alignItemsCenter()
                        paddingLeft(16f)
                        paddingRight(16f)
                        paddingTop(12f)
                        paddingBottom(12f)
                    }

                    // 左箭头
                    View {
                        attr {
                            size(36f, 36f)
                            allCenter()
                        }
                        event {
                            click {
                                if (ctx.currentMonth == 0) {
                                    ctx.currentMonth = 11
                                    ctx.currentYear--
                                } else {
                                    ctx.currentMonth--
                                }
                            }
                        }
                        Text {
                            attr {
                                text("◀")
                                fontSize(14f)
                                color(Color(0xFF666666))
                            }
                        }
                    }

                    // 月份标题
                    Text {
                        attr {
                            text("${ctx.currentYear}年${ctx.currentMonth + 1}月")
                            fontSize(18f)
                            fontWeightBold()
                            color(Color(0xFF333333))
                        }
                    }

                    // 右箭头
                    View {
                        attr {
                            size(36f, 36f)
                            allCenter()
                        }
                        event {
                            click {
                                if (ctx.currentMonth == 11) {
                                    ctx.currentMonth = 0
                                    ctx.currentYear++
                                } else {
                                    ctx.currentMonth++
                                }
                            }
                        }
                        Text {
                            attr {
                                text("▶")
                                fontSize(14f)
                                color(Color(0xFF666666))
                            }
                        }
                    }
                }

                // ===== 星期表头 =====
                View {
                    attr {
                        flexDirectionRow()
                    }
                    for (label in arrayOf("日", "一", "二", "三", "四", "五", "六")) {
                        View {
                            attr {
                                flex(1f)
                                height(36f)
                                allCenter()
                            }
                            Text {
                                attr {
                                    text(label)
                                    fontSize(13f)
                                    color(Color(0xFF999999))
                                }
                            }
                        }
                    }
                }

                // ===== 日期网格 =====
                vbind({
                    // 任一状态变化均触发重渲染
                    ctx.currentYear * 1_000_000_000L +
                        ctx.currentMonth * 1_000_000L +
                        ctx.selectedYear * 1_000L +
                        ctx.selectedMonth * 100L +
                        ctx.selectedDay
                }) {
                    val cells = ctx.buildCells(ctx.currentYear, ctx.currentMonth)

                    fun isToday(cell: CalendarView.CalendarCell): Boolean =
                        cell.year == todayYear && cell.month == todayMonth && cell.day == todayDay

                    fun isSelected(cell: CalendarView.CalendarCell): Boolean =
                        cell.isCurrentMonth &&
                            cell.day == ctx.selectedDay &&
                            cell.month == ctx.selectedMonth &&
                            cell.year == ctx.selectedYear

                    for (row in 0..5) {
                        View {
                            attr {
                                flexDirectionRow()
                            }
                            for (col in 0..6) {
                                val cell = cells[row * 7 + col]
                                val selected = isSelected(cell)
                                val today = isToday(cell)

                                View {
                                    attr {
                                        flex(1f)
                                        height(40f)
                                        allCenter()
                                        if (selected) {
                                            backgroundColor(Color(0xFF4A90E2))
                                            borderRadius(20f)
                                        }
                                    }
                                    event {
                                        if (cell.isCurrentMonth) {
                                            click {
                                                ctx.selectedYear = cell.year
                                                ctx.selectedMonth = cell.month
                                                ctx.selectedDay = cell.day
                                                val millis = ctx.dateToTimeMillis(
                                                    cell.year, cell.month, cell.day
                                                )
                                                ctx.event.onDateSelected?.invoke(
                                                    CalendarDate(
                                                        year = cell.year,
                                                        month = cell.month + 1,
                                                        day = cell.day,
                                                        timeInMillis = millis
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    Text {
                                        attr {
                                            text("${cell.day}")
                                            fontSize(14f)
                                            when {
                                                !cell.isCurrentMonth -> color(Color(0xFFCCCCCC))
                                                selected -> color(Color.WHITE)
                                                today -> {
                                                    color(Color(0xFF4A90E2))
                                                    fontWeightBold()
                                                }
                                                else -> color(Color(0xFF333333))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * CalendarView 属性。
 */
class CalendarViewAttr : ComposeAttr() {
    internal var initialYear: Int = 0
    internal var initialMonth: Int = 0 // 0-11
    internal var initialDay: Int = 0

    /**
     * 设置初始显示月份与可选预选日期。
     *
     * @param year  年
     * @param month 月（1-12）
     * @param day   日（1-31），传 0 表示不预选日期
     */
    fun initialDate(year: Int, month: Int, day: Int = 0) {
        initialYear = year
        initialMonth = month - 1
        initialDay = day
    }
}

/**
 * CalendarView 事件。
 */
class CalendarViewEvent : ComposeEvent() {
    /**
     * 日期选中回调。
     * 点击当月任一日期时触发，返回 [CalendarDate]（含年月日及时间戳）。
     */
    var onDateSelected: ((CalendarDate) -> Unit)? = null
}

/**
 * CalendarView DSL 入口。
 */
fun ViewContainer<*, *>.CalendarView(init: CalendarView.() -> Unit) {
    addChild(CalendarView(), init)
}
