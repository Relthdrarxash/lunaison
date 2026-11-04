package fr.lunaison.ui

import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import fr.lunaison.core.content.Articles
import fr.lunaison.core.designsystem.LunaCard
import fr.lunaison.core.designsystem.SectionTitle
import fr.lunaison.core.model.tr

@Composable
fun LearnScreen(onArticle: (String) -> Unit) {
    Page {
        PageTitle(tr("Comprendre", "Learn"), subtitle = tr("Des fiches courtes, relues et sourcées.", "Short, reviewed and referenced guides."))
        Articles.all.forEach { article ->
            LunaCard(modifier = Modifier.clickable(role = Role.Button) { onArticle(article.id) }) {
                Text(article.title, style = MaterialTheme.typography.titleMedium)
                Text(article.summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Muted(tr("Ces fiches informent et ne remplacent pas l'avis d'un professionnel de santé.", "These guides inform you and do not replace the advice of a health professional."))
    }
}

@Composable
fun ArticleScreen(id: String, onBack: () -> Unit) {
    val article = Articles.byId(id)
    val context = LocalContext.current
    Page {
        if (article == null) {
            PageTitle(tr("Fiche introuvable", "Guide not found"), onBack = onBack)
            return@Page
        }
        PageTitle(article.title, onBack = onBack)
        Text(article.summary, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        article.sections.forEach { section ->
            SectionTitle(section.heading)
            section.paragraphs.forEach { Text(it, style = MaterialTheme.typography.bodyLarge) }
        }
        if (article.sources.isNotEmpty()) {
            SectionTitle(tr("Sources", "Sources"))
            article.sources.forEach { source ->
                TextButton(onClick = { openLink(context, source.url) }) {
                    Text(source.label, style = MaterialTheme.typography.bodySmall, textDecoration = TextDecoration.Underline)
                }
            }
            Muted(tr("Les liens s'ouvrent dans votre navigateur.", "Links open in your browser."))
        }
    }
}
