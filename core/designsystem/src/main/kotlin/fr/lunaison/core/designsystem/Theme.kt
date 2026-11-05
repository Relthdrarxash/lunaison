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

private val SageLightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFF2E5443),
    onPrimary = Color(0xFFFAFBF8),
    primaryContainer = Color(0xFFDCE9E1),
    onPrimaryContainer = Color(0xFF1F3B2E),
    secondary = Color(0xFF8C4F38),
    onSecondary = Color(0xFFFAFBF8),
    secondaryContainer = Color(0xFFF3DDD4),
    onSecondaryContainer = Color(0xFF2B221E),
    background = Color(0xFFF4F6F1),
    onBackground = Color(0xFF1E2923),
    surface = Color(0xFFFAFBF8),
    onSurface = Color(0xFF1E2923),
    surfaceVariant = Color(0xFFDCE9E1),
    onSurfaceVariant = Color(0xFF495950),
    outline = Color(0xFF96A69D),
    outlineVariant = Color(0xFFD8E0DA),
)

private val SageDarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFFB4D6C4),
    onPrimary = Color(0xFF121C17),
    primaryContainer = Color(0xFF24382E),
    onPrimaryContainer = Color(0xFFB4D6C4),
    secondary = Color(0xFFE0A189),
    onSecondary = Color(0xFF121C17),
    secondaryContainer = Color(0xFF472C22),
    onSecondaryContainer = Color(0xFFE8EFEA),
    background = Color(0xFF121C17),
    onBackground = Color(0xFFE8EFEA),
    surface = Color(0xFF1A2620),
    onSurface = Color(0xFFE8EFEA),
    surfaceVariant = Color(0xFF24382E),
    onSurfaceVariant = Color(0xFFB1C2B8),
    outline = Color(0xFFB1C2B8),
    outlineVariant = Color(0xFF24382E),
)

private val OceanLightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFF234A63),
    onPrimary = Color(0xFFFAFCFE),
    primaryContainer = Color(0xFFD9E7F2),
    onPrimaryContainer = Color(0xFF183547),
    secondary = Color(0xFF8E4B42),
    onSecondary = Color(0xFFFAFCFE),
    secondaryContainer = Color(0xFFF4DCD8),
    onSecondaryContainer = Color(0xFF2B1F1D),
    background = Color(0xFFF3F7FA),
    onBackground = Color(0xFF1B2630),
    surface = Color(0xFFFAFCFE),
    onSurface = Color(0xFF1B2630),
    surfaceVariant = Color(0xFFD9E7F2),
    onSurfaceVariant = Color(0xFF495866),
    outline = Color(0xFF98A8B5),
    outlineVariant = Color(0xFFD8E1E8),
)

private val OceanDarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFFB3D1E8),
    onPrimary = Color(0xFF111A22),
    primaryContainer = Color(0xFF213445),
    onPrimaryContainer = Color(0xFFB3D1E8),
    secondary = Color(0xFFE39E94),
    onSecondary = Color(0xFF111A22),
    secondaryContainer = Color(0xFF4A2B27),
    onSecondaryContainer = Color(0xFFE6EEF5),
    background = Color(0xFF111A22),
    onBackground = Color(0xFFE6EEF5),
    surface = Color(0xFF19242E),
    onSurface = Color(0xFFE6EEF5),
    surfaceVariant = Color(0xFF213445),
    onSurfaceVariant = Color(0xFFB0C0CE),
    outline = Color(0xFFB0C0CE),
    outlineVariant = Color(0xFF213445),
)

private val TerracottaLightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFF6E3828),
    onPrimary = Color(0xFFFFFBF8),
    primaryContainer = Color(0xFFF5DFD6),
    onPrimaryContainer = Color(0xFF4D2519),
    secondary = Color(0xFF5A4632),
    onSecondary = Color(0xFFFFFBF8),
    secondaryContainer = Color(0xFFEFE3D5),
    onSecondaryContainer = Color(0xFF2B2118),
    background = Color(0xFFFCF6F2),
    onBackground = Color(0xFF2B201C),
    surface = Color(0xFFFFFBF8),
    onSurface = Color(0xFF2B201C),
    surfaceVariant = Color(0xFFF5DFD6),
    onSurfaceVariant = Color(0xFF635049),
    outline = Color(0xFFB8A29A),
    outlineVariant = Color(0xFFE8DAD4),
)

private val TerracottaDarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFFEBBBA8),
    onPrimary = Color(0xFF1F1410),
    primaryContainer = Color(0xFF3D261E),
    onPrimaryContainer = Color(0xFFEBBBA8),
    secondary = Color(0xFFD9C3AC),
    onSecondary = Color(0xFF1F1410),
    secondaryContainer = Color(0xFF3B3024),
    onSecondaryContainer = Color(0xFFF2E8E4),
    background = Color(0xFF1F1410),
    onBackground = Color(0xFFF2E8E4),
    surface = Color(0xFF291C17),
    onSurface = Color(0xFFF2E8E4),
    surfaceVariant = Color(0xFF3D261E),
    onSurfaceVariant = Color(0xFFC7B4AC),
    outline = Color(0xFFC7B4AC),
    outlineVariant = Color(0xFF3D261E),
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
fun LunaisonTheme(
    dark: Boolean = isSystemInDarkTheme(),
    palette: String = "PLUM",
    content: @Composable () -> Unit,
) {
    val scheme = when (palette) {
        "SAGE" -> if (dark) SageDarkColors else SageLightColors
        "OCEAN" -> if (dark) OceanDarkColors else OceanLightColors
        "TERRACOTTA" -> if (dark) TerracottaDarkColors else TerracottaLightColors
        else -> if (dark) DarkColors else LightColors
    }
    MaterialTheme(
        colorScheme = scheme,
        typography = LunaTypography,
        shapes = LunaShapes,
        content = content,
    )
}
