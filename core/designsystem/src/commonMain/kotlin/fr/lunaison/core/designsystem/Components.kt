package fr.lunaison.core.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

enum class LunaButtonStyle { Primary, Accent, Outlined, Quiet }

/** Bouton toujours libellé, avec une hauteur généreuse pour les actions principales. */
@Composable
fun LunaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: LunaButtonStyle = LunaButtonStyle.Primary,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
    val colors = MaterialTheme.colorScheme
    when (style) {
        LunaButtonStyle.Primary -> Button(
            onClick = onClick,
            modifier = modifier.fillMaxWidth().heightIn(min = 58.dp),
            enabled = enabled,
            shape = CircleShape,
            content = content,
        )
        LunaButtonStyle.Accent -> Button(
            onClick = onClick,
            modifier = modifier.fillMaxWidth().heightIn(min = 58.dp),
            enabled = enabled,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = colors.secondary, contentColor = colors.onSecondary),
            content = content,
        )
        LunaButtonStyle.Outlined -> OutlinedButton(
            onClick = onClick,
            modifier = modifier.fillMaxWidth().heightIn(min = 52.dp),
            enabled = enabled,
            shape = CircleShape,
            border = BorderStroke(2.dp, colors.primary),
            content = content,
        )
        LunaButtonStyle.Quiet -> TextButton(
            onClick = onClick,
            modifier = modifier.fillMaxWidth().heightIn(min = 48.dp),
            enabled = enabled,
            content = content,
        )
    }
}

/** Titre de section, annoncé comme tel par TalkBack. */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.semantics { heading() },
    )
}

/** Carte de contenu. */
@Composable
fun LunaCard(
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = container),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

/** Pastilles à choix, une ou plusieurs selon l'usage. La sélection est signalée par la couleur du conteneur, avec taille stable. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ChoiceChips(
    options: List<T>,
    label: (T) -> String,
    isSelected: (T) -> Boolean,
    onToggle: (T) -> Unit,
    modifier: Modifier = Modifier,
    showCheckmark: Boolean = false,
) {
    FlowRow(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        options.forEach { option ->
            val selected = isSelected(option)
            FilterChip(
                selected = selected,
                onClick = { onToggle(option) },
                label = { Text(label(option), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(vertical = 10.dp)) },
                leadingIcon = if (selected && showCheckmark) {
                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else {
                    null
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        }
    }
}
