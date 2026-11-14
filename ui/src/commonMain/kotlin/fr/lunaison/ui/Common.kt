package fr.lunaison.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import fr.lunaison.core.model.tr

/** Colonne de page défilante, avec les marges communes et un léger dégradé d'arrière-plan. */
@Composable
fun Page(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background,
        ),
    )
    // La surface fixe la couleur du texte, y compris hors de la barre de navigation (démarrage, thème sombre).
    Surface(color = MaterialTheme.colorScheme.background) {
        PageColumn(modifier.background(bgBrush), content)
    }
}

@Composable
private fun PageColumn(modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = {
            content()
            Spacer(Modifier.height(24.dp))
        },
    )
}

/** Titre de page, avec un retour libellé si la page n'est pas un onglet. */
@Composable
fun PageTitle(title: String, onBack: (() -> Unit)? = null, subtitle: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr("Retour", "Back", pl = "Wstecz", uk = "Назад", es = "Atrás", de = "Zurück"))
            }
            Spacer(Modifier.width(4.dp))
        }
        Column {
            Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun Bullet(text: String) {
    Row {
        Text("•", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(end = 10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun Muted(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = modifier)
}

object Links {
    val PRIVACY: String
        get() = tr("https://relthdrarxash.github.io/lunaison/confidentialite.html", "https://relthdrarxash.github.io/lunaison/en/privacy.html")
    const val SOURCES = "https://relthdrarxash.github.io/lunaison/references.html"

    /** Ancre de la fiche sur la page des sources, au format de documentation/sources.md. */
    fun sourcesOf(articleId: String) = "$SOURCES#fiche-$articleId"
    const val SOURCE = "https://github.com/Relthdrarxash/lunaison"
    const val CONTACT = "mailto:pilmil.studio@proton.me"
}
