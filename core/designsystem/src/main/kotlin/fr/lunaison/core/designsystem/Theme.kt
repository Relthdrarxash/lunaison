package fr.lunaison.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Palette « nuit de lune » : prune profonde, rose terre et crème. Contrastes de texte de 4,5:1 minimum.
 * Le rouge vif est évité : les règles ne sont ni une alerte ni une erreur.
 */
object LunaColors {
    val Plum = Color(0xFF4A2C4F)
    val PlumSoft = Color(0xFFF1E2EA)
    val Clay = Color(0xFFA8473F)
    val ClaySoft = Color(0xFFF6DCD6)
    val Cream = Color(0xFFFBF5F2)
    val Paper = Color(0xFFFFFCFA)
    val Ink = Color(0xFF2A1F2D)
    val InkMuted = Color(0xFF5E5162)
    val Line = Color(0xFFE7DCE0)
    val Outline = Color(0xFFB9A8B6)

    val Night = Color(0xFF1A1220)
    val NightSurface = Color(0xFF241A2B)
    val NightContainer = Color(0xFF34263C)
    val Moon = Color(0xFFE8C9DA)
    val ClayNight = Color(0xFFE39A8F)
    val ClayNightSoft = Color(0xFF4A2A2C)
    val TextNight = Color(0xFFF1E6EC)
    val TextNightMuted = Color(0xFFC2B2C0)
}

private val LightColors: ColorScheme = lightColorScheme(
    primary = LunaColors.Plum,
    onPrimary = LunaColors.Paper,
    primaryContainer = LunaColors.PlumSoft,
    onPrimaryContainer = LunaColors.Plum,
    secondary = LunaColors.Clay,
    onSecondary = LunaColors.Paper,
    secondaryContainer = LunaColors.ClaySoft,
    onSecondaryContainer = LunaColors.Ink,
    background = LunaColors.Cream,
    onBackground = LunaColors.Ink,
    surface = LunaColors.Paper,
    onSurface = LunaColors.Ink,
    surfaceVariant = LunaColors.PlumSoft,
    onSurfaceVariant = LunaColors.InkMuted,
    outline = LunaColors.Outline,
    outlineVariant = LunaColors.Line,
)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = LunaColors.Moon,
    onPrimary = LunaColors.Night,
    primaryContainer = LunaColors.NightContainer,
    onPrimaryContainer = LunaColors.Moon,
    secondary = LunaColors.ClayNight,
    onSecondary = LunaColors.Night,
    secondaryContainer = LunaColors.ClayNightSoft,
    onSecondaryContainer = LunaColors.TextNight,
    background = LunaColors.Night,
    onBackground = LunaColors.TextNight,
    surface = LunaColors.NightSurface,
    onSurface = LunaColors.TextNight,
    surfaceVariant = LunaColors.NightContainer,
    onSurfaceVariant = LunaColors.TextNightMuted,
    outline = LunaColors.TextNightMuted,
    outlineVariant = LunaColors.NightContainer,
)

/** Atkinson Hyperlegible Next (Braille Institute, licence SIL OFL 1.1, voir FONT-LICENSE.txt). */
val Atkinson = FontFamily(
    Font(R.font.atkinson_regular, FontWeight.Normal),
    Font(R.font.atkinson_semibold, FontWeight.SemiBold),
    Font(R.font.atkinson_bold, FontWeight.Bold),
    Font(R.font.atkinson_extrabold, FontWeight.ExtraBold),
)

private fun style(size: Int, weight: FontWeight, lineHeight: Int = (size * 1.3).toInt()) = TextStyle(
    fontFamily = Atkinson,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
)

/** Corps de texte à 17 sp, jamais sous 14 sp ; la taille suit le réglage du téléphone. */
private val LunaTypography = Typography(
    displayLarge = style(56, FontWeight.ExtraBold, 62),
    displayMedium = style(44, FontWeight.ExtraBold),
    displaySmall = style(34, FontWeight.ExtraBold),
    headlineLarge = style(30, FontWeight.ExtraBold),
    headlineMedium = style(26, FontWeight.Bold),
    headlineSmall = style(22, FontWeight.Bold),
    titleLarge = style(20, FontWeight.Bold),
    titleMedium = style(18, FontWeight.Bold),
    titleSmall = style(16, FontWeight.Bold),
    bodyLarge = style(18, FontWeight.Normal, 26),
    bodyMedium = style(17, FontWeight.Normal, 24),
    bodySmall = style(15, FontWeight.Normal, 21),
    labelLarge = style(18, FontWeight.Bold),
    labelMedium = style(15, FontWeight.SemiBold),
    labelSmall = style(14, FontWeight.SemiBold),
)

private val LunaShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

@Composable
fun LunaisonTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = LunaTypography,
        shapes = LunaShapes,
        content = content,
    )
}
