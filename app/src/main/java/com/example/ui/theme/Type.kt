package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val OxaniumFont = GoogleFont("Oxanium")
val DmSansFont = GoogleFont("DM Sans")

val Oxanium = FontFamily(
    Font(googleFont = OxaniumFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = OxaniumFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = OxaniumFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = OxaniumFont, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = OxaniumFont, fontProvider = provider, weight = FontWeight.ExtraBold)
)

val DmSans = FontFamily(
    Font(googleFont = DmSansFont, fontProvider = provider, weight = FontWeight.Light),
    Font(googleFont = DmSansFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = DmSansFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = DmSansFont, fontProvider = provider, weight = FontWeight.Bold)
)

val Typography = Typography(
    displayLarge = TextStyle(fontFamily = Oxanium, fontWeight = FontWeight.Bold, fontSize = 34.sp),
    displayMedium = TextStyle(fontFamily = Oxanium, fontWeight = FontWeight.Bold, fontSize = 28.sp),
    displaySmall = TextStyle(fontFamily = Oxanium, fontWeight = FontWeight.Bold, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = Oxanium, fontWeight = FontWeight.Bold, fontSize = 20.sp),
    titleMedium = TextStyle(fontFamily = Oxanium, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    titleSmall = TextStyle(fontFamily = Oxanium, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    bodyLarge = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 0.5.sp)
)
