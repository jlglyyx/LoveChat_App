package com.yang.lovechat.data


data class ProductData(
    val productInfo: List<ProductInfoData>,
    val productPrivilegeList: List<ProductPrivilegeData>
)

data class ProductInfoData(
    val id: Int,
    val skuCode: String,
    val productName: String,
    val productType: Int,
    val priceAmount: Double,
    val diamondCount: Int,
    val bonusCount: Int,
    val durationDays: Int,
    val save: String?,
){
    var isSelect: Boolean = false
}

data class ProductPrivilegeData(
    val id: Int,
    val privilegeType: Int,
    val title: String,
    val description: String,
    val descriptionZn: String,
    val iconUrl: String?,
    val sortOrder: Int,
    val isPrivate: Boolean,
)