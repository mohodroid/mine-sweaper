package com.mohdroid.compose.theme

import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mohdroid.compose.R


val fontFamily = FontFamily(
    Font(R.font.iransans, FontWeight.Normal),
    Font(R.font.iransans_black, FontWeight.Black),
    Font(R.font.iransans_light, FontWeight.Light),
    Font(R.font.iransans_ultralight, FontWeight.ExtraLight),
    Font(R.font.iransans_medium, FontWeight.Medium),
    Font(R.font.iransans_bold, FontWeight.Bold),
    Font(R.font.iransans_bold, FontWeight.SemiBold)
)

val Typography.mediumRed14: TextStyle
    @Composable get() = body2.copy(
        color = red, fontWeight = FontWeight.Medium
    )

val Typography.mediumBlack20: TextStyle
    @Composable get() = body2.copy(
        color = black, fontWeight = FontWeight.Medium
    )

val Typography.semiBoldRed32: TextStyle
    @Composable get() = body2.copy(
        color = red, fontWeight = FontWeight.SemiBold, fontSize = 32.sp
    )

val MsTypography
    @Composable get() = Typography(
        defaultFontFamily = remember { fontFamily }
    )

val Typography.bottomStyle: TextStyle
    @Composable get() = body1.copy(
        fontWeight = FontWeight.SemiBold, fontSize = 24.sp
    )

val Typography.dialogBottomStyle: TextStyle
    @Composable get() = body1.copy(
        fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = black
    )

val Typography.textInput: TextStyle
    @Composable get() = body2.copy(
        color = red, fontWeight = FontWeight.Normal
    )



