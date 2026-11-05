package fr.lunaison.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import fr.lunaison.core.content.ShareKind
import fr.lunaison.core.content.ShareMessages
import fr.lunaison.core.cycle.CycleState
import fr.lunaison.core.data.Partner
import fr.lunaison.core.data.PartnerChannel
import fr.lunaison.core.designsystem.ChoiceChips
import fr.lunaison.core.designsystem.LunaButton
import fr.lunaison.core.designsystem.LunaButtonStyle
import fr.lunaison.core.designsystem.LunaCard
import fr.lunaison.core.designsystem.SectionTitle
import fr.lunaison.core.model.tr

/**
 * Partage avec un ou une partenaire. L'appli prépare un message et ouvre la messagerie choisie ;
 * c'est la personne qui appuie sur Envoyer. Réservé aux adultes.
 */
@Composable
fun ShareScreen(state: AppState, actions: Actions, onBack: () -> Unit, onArticle: (String) -> Unit) {
    Page {
        PageTitle(tr("Partager", "Share"), onBack = onBack)
        if (!state.settings.isAdult) {
            Text(tr("Le partage avec un ou une partenaire est réservé aux personnes de 18 ans et plus.", "Sharing with a partner is only available to people aged 18 and over."), style = MaterialTheme.typography.bodyLarge)
            return@Page
        }
        val partner = state.settings.partner
        var editing by rememberSaveable { mutableStateOf(partner == null) }
        if (partner == null || editing) {
            PartnerForm(
                initial = partner,
                onSave = {
                    actions.updateSettings { settings -> settings.copy(partner = it) }
                    editing = false
                },
                onCancel = if (partner != null) ({ editing = false }) else null,
            )
        } else {
            Composer(state, partner, onEdit = { editing = true }, onRemove = { actions.updateSettings { it.copy(partner = null) } })
        }
        val knownCycle = state.cycle as? CycleState.Known
        if (knownCycle != null) {
            val guide = remember(knownCycle.phase) { ShareMessages.partnerGuide(knownCycle.phase) }
            SectionTitle(tr("Repères pour votre partenaire", "Guide for your partner"))
            LunaCard {
                Text(guide.phaseTitle, style = MaterialTheme.typography.titleMedium)
                Text(guide.whatPartnerShouldKnow, style = MaterialTheme.typography.bodyMedium)
                Text(tr("Comment soutenir concrètement :", "Concrete ways to help:"), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                guide.supportTips.forEach { Bullet(it) }
            }
        }
        LunaCard {
            Text(tr("Ce qui est partagé", "What is shared"), style = MaterialTheme.typography.titleMedium)
            Bullet(tr("Seulement le message que vous voyez, quand vous l'envoyez.", "Only the message you see, when you send it."))
            Bullet(tr("Rien n'est envoyé automatiquement, et Lunaison ne garde aucune trace de l'envoi.", "Nothing is sent automatically, and Lunaison keeps no record of what you send."))
            Bullet(tr("Le message reste dans l'historique de votre messagerie, comme tout autre message.", "The message stays in your messaging history, like any other message."))
        }
        TextButton(onClick = { onArticle("entourage") }) { Text(tr("Une fiche à faire lire à votre entourage", "A guide for the people around you")) }
    }
}

@Composable
private fun PartnerForm(initial: Partner?, onSave: (Partner) -> Unit, onCancel: (() -> Unit)?) {
    val context = LocalContext.current
    var name by rememberSaveable { mutableStateOf(initial?.name.orEmpty()) }
    var phone by rememberSaveable { mutableStateOf(initial?.phone.orEmpty()) }
    var channel by rememberSaveable { mutableStateOf(initial?.channel ?: PartnerChannel.SMS) }

    // Le sélecteur de contacts du système ne donne accès qu'au contact choisi : aucune autorisation n'est nécessaire.
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data ?: return@rememberLauncherForActivityResult
        context.contentResolver.query(
            uri,
            arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                name = cursor.getString(0).orEmpty().substringBefore(" ")
                phone = cursor.getString(1).orEmpty()
            }
        }
    }

    SectionTitle(tr("À qui écrire ?", "Who to write to?"))
    LunaButton(
        tr("Choisir dans mes contacts", "Choose from my contacts"),
        onClick = {
            try {
                picker.launch(Intent(Intent.ACTION_PICK).setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE))
            } catch (_: ActivityNotFoundException) {
                // Pas de carnet d'adresses : la saisie manuelle reste possible.
            }
        },
        style = LunaButtonStyle.Outlined,
        icon = Icons.Filled.Person,
    )
    OutlinedTextField(name, { name = it.take(40) }, label = { Text(tr("Prénom", "First name")) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    OutlinedTextField(
        phone,
        { phone = it.take(25) },
        label = { Text(tr("Numéro de téléphone", "Phone number")) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
    )
    SectionTitle(tr("Par quelle messagerie ?", "Which messaging app?"))
    ChoiceChips(
        options = PartnerChannel.entries,
        label = { it.label },
        isSelected = { it == channel },
        onToggle = { channel = it },
    )
    Muted(
        when (channel) {
            PartnerChannel.SMS -> tr("Lunaison ouvre la conversation SMS avec le message prêt.", "Lunaison opens the text conversation with the message ready.")
            PartnerChannel.WHATSAPP -> tr("Lunaison ouvre la conversation WhatsApp avec le message prêt. Le numéro doit être au format international, par exemple +33 6…", "Lunaison opens the WhatsApp conversation with the message ready. The number must be in international format, for example +44 7…")
            PartnerChannel.SIGNAL -> tr("Lunaison ouvre Signal avec le message prêt ; vous choisissez la conversation.", "Lunaison opens Signal with the message ready; you choose the conversation.")
            PartnerChannel.OTHER -> tr("Lunaison ouvre le menu de partage d'Android ; vous choisissez l'appli et la conversation.", "Lunaison opens the Android share menu; you choose the app and the conversation.")
        },
    )
    LunaButton(
        tr("Enregistrer", "Save"),
        onClick = { onSave(Partner(name.trim(), phone.trim().ifBlank { null }, channel)) },
        enabled = name.isNotBlank() && (phone.isNotBlank() || channel == PartnerChannel.SIGNAL || channel == PartnerChannel.OTHER),
    )
    if (onCancel != null) LunaButton(tr("Annuler", "Cancel"), onClick = onCancel, style = LunaButtonStyle.Quiet)
}

@Composable
private fun Composer(state: AppState, partner: Partner, onEdit: () -> Unit, onRemove: () -> Unit) {
    val context = LocalContext.current
    val suggestions = remember(state.cycle) { ShareMessages.suggestions(state.cycle) }
    var kind by rememberSaveable { mutableStateOf(suggestions.first()) }
    var text by rememberSaveable(kind) { mutableStateOf(ShareMessages.text(kind, state.cycle)) }

    SectionTitle(tr("Message pour ${partner.name}", "Message for ${partner.name}"))
    ChoiceChips(
        options = suggestions,
        label = { it.label },
        isSelected = { it == kind },
        onToggle = { kind = it },
    )
    OutlinedTextField(
        value = text,
        onValueChange = { text = it.take(500) },
        label = { Text(tr("Vous pouvez modifier le texte", "You can edit the text")) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
    )
    LunaButton(
        tr("Ouvrir ${partner.channel.label}", "Open ${partner.channel.label}"),
        onClick = { ShareLauncher.send(context, partner, text) },
        icon = Icons.AutoMirrored.Filled.Send,
    )
    Muted(tr("Vous relirez le message dans ${partner.channel.label} avant de l'envoyer.", "You will review the message in ${partner.channel.label} before sending it."))
    TextButton(onClick = onEdit) { Text(tr("Changer de contact ou de messagerie", "Change contact or messaging app")) }
    TextButton(onClick = onRemove) { Text(tr("Ne plus partager", "Stop sharing")) }
}

/** Prépare le message et ouvre la messagerie. Aucune autorisation : c'est la messagerie qui envoie. */
object ShareLauncher {
    private const val WHATSAPP = "com.whatsapp"
    private const val WHATSAPP_BUSINESS = "com.whatsapp.w4b"
    private const val SIGNAL = "org.thoughtcrime.securesms"

    fun send(context: Context, partner: Partner, text: String) {
        val intent = when (partner.channel) {
            PartnerChannel.SMS -> partner.phone?.let {
                Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + Uri.encode(it))).putExtra("sms_body", text)
            }
            PartnerChannel.WHATSAPP -> installed(context, WHATSAPP, WHATSAPP_BUSINESS)?.let { pkg ->
                val digits = partner.phone.orEmpty().filter { it.isDigit() }
                Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits?text=" + Uri.encode(text))).setPackage(pkg)
            }
            PartnerChannel.SIGNAL -> installed(context, SIGNAL)?.let { pkg -> plainText(text).setPackage(pkg) }
            PartnerChannel.OTHER -> null
        }
        try {
            context.startActivity(intent ?: Intent.createChooser(plainText(text), null))
        } catch (_: ActivityNotFoundException) {
            context.startActivity(Intent.createChooser(plainText(text), null))
        }
    }

    private fun plainText(text: String) = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)

    private fun installed(context: Context, vararg packages: String): String? = packages.firstOrNull {
        try {
            context.packageManager.getPackageInfo(it, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }
}
