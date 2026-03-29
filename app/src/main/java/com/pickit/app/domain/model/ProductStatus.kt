package com.pickit.app.domain.model

enum class ProductStatus(val label: String) {
    NOT_PURCHASED("未购买"),
    IN_USE("正在使用"),
    USED_UP("已用完"),
    DAMAGED("已损坏"),
    ABANDONED("已放弃"),
}
