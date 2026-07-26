# CalendarView(日历组件)

基于 View 组合实现的日历网格组件，提供月份切换、日期选择功能。不依赖原生组件，全平台一致。

[组件使用示例](https://github.com/Tencent-TDS/KuiklyUI/blob/main/demo/src/commonMain/kotlin/com/tencent/kuikly/demo/pages/demo/kit_demo/DeclarativeDemo/CalendarViewExamplePage.kt)

## 属性

支持所有[基础属性](basic-attr-event.md#基础属性)，此外还支持：

### initialDate

设置初始显示月份与可选预选日期。

| 参数 | 描述 | 类型 |
| -- | -- | -- |
| year | 年份 | Int |
| month | 月份（1-12） | Int |
| day | 日（1-31），传 0 或不传表示不预选日期 | Int |

```kotlin
attr {
    initialDate(2026, 7)       // 仅设定初始月份
    initialDate(2026, 7, 15)   // 设定初始月份 + 预选 15 日
}
```

若不调用 `initialDate`，默认显示系统当前月份。

## 事件

支持所有[基础事件](basic-attr-event.md#基础事件)，此外还支持：

### onDateSelected

日期选中回调，点击当月任一日期时触发。回调参数为 `CalendarDate` 类型。

**CalendarDate**

| 成员 | 描述 | 类型 |
| -- | -- | -- |
| year | 年 | Int |
| month | 月（1-12） | Int |
| day | 日（1-31） | Int |
| timeInMillis | epoch 毫秒时间戳 | Long |

:::tabs

@tab:active 示例

```kotlin{14-27}
@Page("demo_page")
internal class TestPage : BasePager() {
    private var selectedDate: CalendarDate? by observable(null)

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            CalendarView {
                attr {
                    size(360f, 360f)
                    initialDate(2026, 7)
                }
                event {
                    onDateSelected = { date ->
                        ctx.selectedDate = date
                    }
                }
            }
            Text {
                attr {
                    text(ctx.selectedDate?.let {
                        "${it.year}-${it.month}-${it.day}"
                    } ?: "未选择")
                }
            }
        }
    }
}
```

@tab 效果

<div align="center">
<img src="./img/calendar_view.png" style="width: 30%; border: 1px gray solid">
</div>

:::
