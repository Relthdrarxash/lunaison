package fr.lunaison.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import fr.lunaison.core.designsystem.resources.Res
import fr.lunaison.core.designsystem.resources.atkinson_bold
import fr.lunaison.core.designsystem.resources.atkinson_extrabold
import fr.lunaison.core.designsystem.resources.atkinson_regular
import fr.lunaison.core.designsystem.resources.atkinson_semibold
import org.jetbrains.compose.resources.Font

@Composable
internal actual fun atkinsonFamily(): FontFamily = FontFamily(
    Font(Res.font.atkinson_regular, FontWeight.Normal),
    Font(Res.font.atkinson_semibold, FontWeight.SemiBold),
    Font(Res.font.atkinson_bold, FontWeight.Bold),
    Font(Res.font.atkinson_extrabold, FontWeight.ExtraBold),
)
