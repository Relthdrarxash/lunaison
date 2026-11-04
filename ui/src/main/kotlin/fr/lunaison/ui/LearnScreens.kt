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

@Composable
fun LearnScreen(onArticle: (String) -> Unit) {
    Page {
        PageTitle("Comprendre", subtitle = "Des fiches courtes, relues et sourcées.")
        Articles.all.forEach { article ->
            LunaCard(modifier = Modifier.clickable(role = Role.Button) { onArticle(article.id) }) {
                Text(article.title, style = MaterialTheme.typography.titleMedium)
                Text(article.summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Muted("Ces fiches informent et ne remplacent pas l'avis d'un professionnel de santé.")
    }
}

@Composable
fun ArticleScreen(id: String, onBack: () -> Unit) {
    val article = Articles.byId(id)
    val context = LocalContext.current
    Page {
        if (article == null) {
            PageTitle("Fiche introuvable", onBack = onBack)
            return@Page
        }
        PageTitle(article.title, onBack = onBack)
        Text(article.summary, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        article.sections.forEach { section ->
            SectionTitle(section.heading)
            section.paragraphs.forEach { Text(it, style = MaterialTheme.typography.bodyLarge) }
        }
        // Les références ne sont pas dans le texte : un lien discret mène à la page du site qui les détaille.
        if (article.sources.isNotEmpty()) {
            TextButton(onClick = { openLink(context, Links.sourcesOf(article.id)) }) {
                Text("Sources de cette fiche, sur le site", style = MaterialTheme.typography.bodySmall, textDecoration = TextDecoration.Underline)
            }
        }
    }
}
