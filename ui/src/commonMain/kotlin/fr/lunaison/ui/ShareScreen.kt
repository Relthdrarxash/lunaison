package fr.lunaison.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import fr.lunaison.core.model.PartnerPeriod
import fr.lunaison.core.model.PartnerSnapshot
import fr.lunaison.core.model.tr
import fr.lunaison.core.model.toEpochDay

/**
 * Partage avec un ou une partenaire. L'appli prépare un message et ouvre la messagerie choisie ;
 * c'est la personne qui appuie sur Envoyer. Réservé aux adultes.
 */
@Composable
fun ShareScreen(state: AppState, actions: Actions, onBack: () -> Unit, onArticle: (String) -> Unit) {
    Page {
        PageTitle(tr("Partager", "Share", pl = "Udostępnij", uk = "Поділитися", es = "Compartir", de = "Teilen"), onBack = onBack)
        if (!state.settings.isAdult) {
            Text(tr("Le partage avec un ou une partenaire est réservé aux personnes de 18 ans et plus.", "Sharing with a partner is only available to people aged 18 and over.", pl = "Udostępnianie partnerowi lub partnerce jest dostępne tylko dla osób w wieku 18 lat i więcej.", uk = "Ділитися з партнером чи партнеркою можуть лише особи віком від 18 років.", es = "Compartir con tu pareja está reservado a personas de 18 años o más.", de = "Das Teilen mit einer Partnerin oder einem Partner ist Personen ab 18 Jahren vorbehalten."), style = MaterialTheme.typography.bodyLarge)
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
            SectionTitle(tr("Repères pour votre partenaire", "Guide for your partner", pl = "Wskazówki dla partnera", uk = "Орієнтири для вашого партнера", es = "Guía para tu pareja", de = "Hinweise für deinen Partner"))
            LunaCard {
                Text(guide.phaseTitle, style = MaterialTheme.typography.titleMedium)
                Text(guide.whatPartnerShouldKnow, style = MaterialTheme.typography.bodyMedium)
                Text(tr("Comment soutenir concrètement :", "Concrete ways to help:", pl = "Jak konkretnie wesprzeć:", uk = "Як підтримати конкретно:", es = "Cómo apoyar de forma concreta:", de = "So kann man konkret unterstützen:"), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                guide.supportTips.forEach { Bullet(it) }
            }
        }

        QrSyncSection(state = state, actions = actions, partner = partner)

        LunaCard {
            Text(tr("Ce qui est partagé", "What is shared", pl = "Co jest udostępniane", uk = "Що передається", es = "Qué se comparte", de = "Was geteilt wird"), style = MaterialTheme.typography.titleMedium)
            Bullet(tr("Seulement le message que vous voyez, quand vous l'envoyez.", "Only the message you see, when you send it.", pl = "Tylko wiadomość, którą widzisz, w chwili jej wysłania.", uk = "Лише повідомлення, яке ви бачите, у момент надсилання.", es = "Solo el mensaje que ves, cuando lo envías.", de = "Nur die Nachricht, die du siehst, wenn du sie sendest."))
            Bullet(tr("Rien n'est envoyé automatiquement, et Lunaison ne garde aucune trace de l'envoi.", "Nothing is sent automatically, and Lunaison keeps no record of what you send.", pl = "Nic nie jest wysyłane automatycznie, a Lunaison nie zachowuje żadnego śladu wysyłki.", uk = "Нічого не надсилається автоматично, а Lunaison не зберігає жодного сліду надсилання.", es = "No se envía nada automáticamente y Lunaison no guarda ningún registro del envío.", de = "Nichts wird automatisch gesendet, und Lunaison speichert keinerlei Spur des Versands."))
            Bullet(tr("Le message reste dans l'historique de votre messagerie, comme tout autre message.", "The message stays in your messaging history, like any other message.", pl = "Wiadomość pozostaje w historii Twojego komunikatora, jak każda inna wiadomość.", uk = "Повідомлення залишається в історії вашого месенджера, як і будь-яке інше.", es = "El mensaje queda en el historial de tu mensajería, como cualquier otro mensaje.", de = "Die Nachricht bleibt im Verlauf deines Messengers erhalten, wie jede andere Nachricht."))
        }
        TextButton(onClick = { onArticle("entourage") }) { Text(tr("Une fiche à faire lire à votre entourage", "A guide for the people around you", pl = "Artykuł do przeczytania dla Twoich bliskich", uk = "Стаття, яку варто дати прочитати вашим близьким", es = "Una ficha para que la lean tus allegados", de = "Ein Artikel zum Weitergeben an dein Umfeld")) }
    }
}

@Composable
private fun PartnerForm(initial: Partner?, onSave: (Partner) -> Unit, onCancel: (() -> Unit)?) {
    val context = rememberPlatformContext()
    var name by rememberSaveable { mutableStateOf(initial?.name.orEmpty()) }
    var phone by rememberSaveable { mutableStateOf(initial?.phone.orEmpty()) }
    var channel by rememberSaveable { mutableStateOf(initial?.channel ?: PartnerChannel.SMS) }

    // Le sélecteur de contacts du système ne donne accès qu'au contact choisi : aucune autorisation n'est nécessaire.
    val pickContact = rememberContactPicker { pickedName, pickedPhone ->
        name = pickedName
        phone = pickedPhone
    }

    SectionTitle(tr("À qui écrire ?", "Who to write to?", pl = "Do kogo napisać?", uk = "Кому написати?", es = "¿A quién escribir?", de = "An wen schreiben?"))
    if (pickContact != null) {
        LunaButton(
            tr("Choisir dans mes contacts", "Choose from my contacts", pl = "Wybierz z kontaktów", uk = "Вибрати з контактів", es = "Elegir de mis contactos", de = "Aus meinen Kontakten wählen"),
            onClick = pickContact,
            style = LunaButtonStyle.Outlined,
            icon = Icons.Filled.Person,
        )
    }
    OutlinedTextField(name, { name = it.take(40) }, label = { Text(tr("Prénom", "First name", pl = "Imię", uk = "Ім'я", es = "Nombre", de = "Vorname")) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    OutlinedTextField(
        phone,
        { phone = it.take(25) },
        label = { Text(tr("Numéro de téléphone", "Phone number", pl = "Numer telefonu", uk = "Номер телефону", es = "Número de teléfono", de = "Telefonnummer")) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
    )
    SectionTitle(tr("Par quelle messagerie ?", "Which messaging app?", pl = "Którym komunikatorem?", uk = "Через який месенджер?", es = "¿Por qué aplicación de mensajería?", de = "Über welchen Messenger?"))
    ChoiceChips(
        options = PartnerChannel.entries,
        label = { it.label },
        isSelected = { it == channel },
        onToggle = { channel = it },
    )
    Muted(
        when (channel) {
            PartnerChannel.SMS -> tr("Lunaison ouvre la conversation SMS avec le message prêt.", "Lunaison opens the text conversation with the message ready.", pl = "Lunaison otwiera rozmowę SMS z gotową wiadomością.", uk = "Lunaison відкриває розмову в SMS із готовим повідомленням.", es = "Lunaison abre la conversación de SMS con el mensaje preparado.", de = "Lunaison öffnet die SMS-Unterhaltung mit vorbereiteter Nachricht.")
            PartnerChannel.WHATSAPP -> tr("Lunaison ouvre la conversation WhatsApp avec le message prêt. Le numéro doit être au format international, par exemple +33 6…", "Lunaison opens the WhatsApp conversation with the message ready. The number must be in international format, for example +44 7…", pl = "Lunaison otwiera rozmowę w WhatsApp z gotową wiadomością. Numer musi być w formacie międzynarodowym, na przykład +48 5…", uk = "Lunaison відкриває розмову в WhatsApp із готовим повідомленням. Номер має бути в міжнародному форматі, наприклад +380 6…", es = "Lunaison abre la conversación de WhatsApp con el mensaje preparado. El número debe estar en formato internacional, por ejemplo +34 6…", de = "Lunaison öffnet die WhatsApp-Unterhaltung mit vorbereiteter Nachricht. Die Nummer muss im internationalen Format sein, zum Beispiel +49 1…")
            PartnerChannel.SIGNAL -> tr("Lunaison ouvre Signal avec le message prêt ; vous choisissez la conversation.", "Lunaison opens Signal with the message ready; you choose the conversation.", pl = "Lunaison otwiera Signal z gotową wiadomością; rozmowę wybierasz Ty.", uk = "Lunaison відкриває Signal із готовим повідомленням; розмову обираєте ви.", es = "Lunaison abre Signal con el mensaje preparado; tú eliges la conversación.", de = "Lunaison öffnet Signal mit vorbereiteter Nachricht; du wählst die Unterhaltung.")
            PartnerChannel.OTHER -> tr("Lunaison ouvre le menu de partage d'Android ; vous choisissez l'appli et la conversation.", "Lunaison opens the Android share menu; you choose the app and the conversation.", pl = "Lunaison otwiera menu udostępniania Androida; aplikację i rozmowę wybierasz Ty.", uk = "Lunaison відкриває меню «Поділитися» Android; застосунок і розмову обираєте ви.", es = "Lunaison abre el menú de compartir de Android; tú eliges la aplicación y la conversación.", de = "Lunaison öffnet das Android-Teilen-Menü; du wählst die App und die Unterhaltung.")
        },
    )
    LunaButton(
        tr("Enregistrer", "Save", pl = "Zapisz", uk = "Зберегти", es = "Guardar", de = "Speichern"),
        onClick = { onSave(Partner(name.trim(), phone.trim().ifBlank { null }, channel)) },
        enabled = name.isNotBlank() && (phone.isNotBlank() || channel == PartnerChannel.SIGNAL || channel == PartnerChannel.OTHER),
    )
    if (onCancel != null) LunaButton(tr("Annuler", "Cancel", pl = "Anuluj", uk = "Скасувати", es = "Cancelar", de = "Abbrechen"), onClick = onCancel, style = LunaButtonStyle.Quiet)
}

@Composable
private fun Composer(state: AppState, partner: Partner, onEdit: () -> Unit, onRemove: () -> Unit) {
    val context = rememberPlatformContext()
    val suggestions = remember(state.cycle) { ShareMessages.suggestions(state.cycle) }
    var kind by rememberSaveable { mutableStateOf(suggestions.first()) }
    var text by rememberSaveable(kind) { mutableStateOf(ShareMessages.text(kind, state.cycle)) }

    SectionTitle(tr("Message pour ${partner.name}", "Message for ${partner.name}", pl = "Wiadomość dla ${partner.name}", uk = "Повідомлення для ${partner.name}", es = "Mensaje para ${partner.name}", de = "Nachricht an ${partner.name}"))
    ChoiceChips(
        options = suggestions,
        label = { it.label },
        isSelected = { it == kind },
        onToggle = { kind = it },
    )
    OutlinedTextField(
        value = text,
        onValueChange = { text = it.take(500) },
        label = { Text(tr("Vous pouvez modifier le texte", "You can edit the text", pl = "Możesz zmienić tekst", uk = "Ви можете змінити текст", es = "Puedes editar el texto", de = "Du kannst den Text bearbeiten")) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
    )
    LunaButton(
        tr(
            "Partager avec ${partner.name}",
            "Share with ${partner.name}",
            pl = "Udostępnij dla: ${partner.name}",
            uk = "Поділитися з ${partner.name}",
            es = "Compartir con ${partner.name}",
            de = "Mit ${partner.name} teilen",
        ),
        onClick = { ShareLauncher.send(context, partner, text) },
        icon = Icons.AutoMirrored.Filled.Send,
    )
    Muted(
        tr(
            "Ouvre ${partner.channel.label} avec le message prêt : vous le relisez avant l'envoi.",
            "Opens ${partner.channel.label} with the message ready: you review it before sending.",
            pl = "Otwiera aplikację ${partner.channel.label} z gotową wiadomością: możesz ją sprawdzić przed wysłaniem.",
            uk = "Відкриває ${partner.channel.label} із готовим повідомленням: ви зможете перевірити його перед відправленням.",
            es = "Abre ${partner.channel.label} con el mensaje preparado: puedes revisarlo antes de enviarlo.",
            de = "Öffnet ${partner.channel.label} mit dem vorbereiteten Text: Du prüfst ihn vor dem Absenden.",
        ),
    )
    TextButton(onClick = onEdit) { Text(tr("Changer de contact ou de messagerie", "Change contact or messaging app", pl = "Zmień kontakt lub komunikator", uk = "Змінити контакт або месенджер", es = "Cambiar de contacto o de mensajería", de = "Kontakt oder Messenger wechseln")) }
    TextButton(onClick = onRemove) { Text(tr("Ne plus partager", "Stop sharing", pl = "Przestań udostępniać", uk = "Припинити ділитися", es = "Dejar de compartir", de = "Nicht mehr teilen")) }
}

@Composable
private fun QrSyncSection(
    state: AppState,
    actions: Actions,
    partner: Partner?,
) {
    val context = rememberPlatformContext()
    val known = state.cycle as? CycleState.Known
    var showQr by rememberSaveable { mutableStateOf(false) }
    var showScanner by rememberSaveable { mutableStateOf(false) }
    var includeFertility by rememberSaveable { mutableStateOf(state.settings.showFertility) }

    SectionTitle(
        tr(
            "Partage et mise à jour (QR Code, NFC ou SMS)",
            "Share and update (QR Code, NFC or SMS)",
            pl = "Udostępnianie i aktualizacja (QR Code, NFC lub SMS)",
            uk = "Поширення та оновлення (QR-код, NFC або SMS)",
            es = "Compartir y actualizar (código QR, NFC o SMS)",
            de = "Teilen und Aktualisieren (QR-Code, NFC oder SMS)",
        ),
    )

    if (known != null) {
        val allPeriods = remember(known) {
            val base = known.periods.takeLast(6).map { p ->
                val len = if (known.inPeriod && p.start == known.lastStart) maxOf(p.length, known.stats.periodLength) else p.length
                PartnerPeriod(p.start, len)
            }.toMutableList()
            if (base.none { kotlin.math.abs(it.start.toEpochDay() - known.lastStart.toEpochDay()) <= 2 }) {
                base.add(PartnerPeriod(known.lastStart, known.stats.periodLength))
            }
            base.sortedBy { it.start }.takeLast(6)
        }
        val snapshot = remember(allPeriods, state.today, state.settings.myShareName, includeFertility, known.stats.cycleLength) {
            PartnerSnapshot(
                partnerName = state.settings.myShareName.trim().ifEmpty { tr("Partenaire", "Partner", pl = "Partner", uk = "Партнер", es = "Pareja", de = "Partner") },
                sharedAt = state.today,
                periods = allPeriods,
                usualCycleLength = known.stats.cycleLength,
                showFertility = includeFertility,
            )
        }
        val encoded = remember(snapshot) { snapshot.encode() }

        NfcShareEffect(encoded)

        LunaCard {
            Text(
                tr("Transmettre ou mettre à jour mes estimations (sans serveur)", "Share or update my estimates (serverless)", pl = "Przekaż lub zaktualizuj moje prognozy (bez serwera)", uk = "Передати або оновити мої прогнози (без сервера)", es = "Enviar o actualizar mis estimaciones (sin servidor)", de = "Meine Schätzungen übermitteln oder aktualisieren (ohne Server)"),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                tr(
                    "Ce même QR Code (ou approche NFC, ou code SMS) sert aussi bien la première fois que pour mettre à jour le calendrier de votre partenaire au cycle suivant : les cycles déjà connus sont fusionnés sans doublon. Vous pouvez aussi coller les deux téléphones dos à dos (NFC) pour synchroniser instantanément sans viser. Vos notes personnelles ne sont jamais incluses.",
                    "This same QR Code (or NFC tap, or SMS code) works both the first time and to update your partner's calendar next cycle: already-known cycles are merged without duplicates. You can also tap phones back-to-back (NFC) to sync instantly without aiming. Your personal notes are never included.",
                    pl = "Ten sam kod QR (albo zbliżenie NFC, czy SMS) służy zarówno przy pierwszym połączeniu, jak i do aktualizacji kalendarza partnera w kolejnym cyklu: znane cykle są łączone bez duplikatów. Możecie też po prostu zbliżyć telefony tyłem do siebie (NFC), aby zsynchronizować bez celowania. Twoje osobiste notatki nigdy nie są dołączane.",
                    uk = "Цей самий QR-код (або NFC-дотик, чи SMS-код) підходить як для першого разу, так і для оновлення календаря партнера в наступному циклі: відомі цикли об\'єднуються без дублікатів. Ви також можете просто прикласти телефони спинками (NFC) для миттєвої синхронізації без прицілювання. Ваші особисті нотатки ніколи не передаються.",
                    es = "Este mismo código QR (o contacto NFC, o código SMS) sirve tanto la primera vez como para actualizar el calendario de tu pareja en el siguiente ciclo: los ciclos ya conocidos se combinan sin duplicados. También podéis juntar los teléfonos dorso con dorso (NFC) para sincronizar al instante sin apuntar. Tus notas personales nunca se incluyen.",
                    de = "Derselbe QR-Code (oder NFC-Kopplung bzw. SMS-Code) funktioniert sowohl beim ersten Mal als auch zur Aktualisierung des Kalenders deines Partners im nächsten Zyklus: Bekannte Zyklen werden ohne Duplikate zusammengeführt. Ihr könnt die Telefone auch Rücken an Rücken halten (NFC), um sofort ohne Zielen zu synchronisieren. Deine persönlichen Notizen werden niemals übertragen.",
                ),
                style = MaterialTheme.typography.bodySmall,
            )
            OutlinedTextField(
                value = state.settings.myShareName,
                onValueChange = { name -> actions.updateSettings { it.copy(myShareName = name.take(30)) } },
                label = { Text(tr("Mon prénom (affiché chez mon ou ma partenaire)", "My first name (shown to my partner)", pl = "Moje imię (wyświetlane u partnera lub partnerki)", uk = "Моє ім'я (відображається у мого партнера чи партнерки)", es = "Mi nombre (se muestra a mi pareja)", de = "Mein Vorname (wird meiner Partnerin oder meinem Partner angezeigt)")) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val label = tr("Inclure la fenêtre fertile estimée", "Include estimated fertile window", pl = "Dołącz szacowane dni płodne", uk = "Додати орієнтовне фертильне вікно", es = "Incluir la ventana fértil estimada", de = "Geschätztes fruchtbares Fenster einbeziehen")
                Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Switch(
                    checked = includeFertility,
                    onCheckedChange = { includeFertility = it },
                    modifier = Modifier.semantics { contentDescription = label },
                )
            }
            LunaButton(
                if (showQr) tr("Masquer mon QR Code", "Hide my QR Code", pl = "Ukryj mój kod QR", uk = "Сховати мій QR-код", es = "Ocultar mi código QR", de = "Meinen QR-Code ausblenden") else tr("Afficher mon QR Code (partage ou mise à jour)", "Show my QR Code (share or update)", pl = "Pokaż mój kod QR (udostępnianie lub aktualizacja)", uk = "Показати мій QR-код (передача або оновлення)", es = "Mostrar mi código QR (compartir o actualizar)", de = "Meinen QR-Code anzeigen (Teilen oder Aktualisieren)"),
                onClick = { showQr = !showQr },
                style = LunaButtonStyle.Outlined,
            )
            if (showQr) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    QrCodeBox(content = encoded)
                }
                Muted(
                    tr(
                        "Sur le téléphone de votre partenaire : appuyez sur « Mettre à jour (QR / NFC / SMS) » sur l'accueil, ou ouvrez Partager > Scanner un QR Code (ou collez les téléphones dos à dos).",
                        "On your partner's phone: tap 'Update (QR / NFC / SMS)' on the home screen, or open Share > Scan a QR Code (or tap phones back-to-back).",
                        pl = "Na telefonie partnera: naciśnij „Aktualizuj (QR / NFC / SMS)” na ekranie głównym lub otwórz Udostępnij > Zeskanuj kod QR (albo zbliżcie telefony tyłem do siebie).",
                        uk = "На телефоні партнера: натисніть «Оновити (QR / NFC / SMS)» на головному екрані або відкрийте Поділитися > Сканувати QR-код (чи прикладіть телефони спинками).",
                        es = "En el teléfono de tu pareja: pulsa en «Actualizar (QR / NFC / SMS)» en la pantalla de inicio, o abre Compartir > Escanear código QR (o junta los teléfonos dorso con dorso).",
                        de = "Auf dem Smartphone deines Partners: Tippe auf der Startseite auf „Aktualisieren (QR / NFC / SMS)“ oder öffne Teilen > QR-Code scannen (oder halte die Geräte Rücken an Rücken).",
                    ),
                )
            }
            val shareBody = remember(known, encoded) {
                tr(
                    fr = "Voici mon suivi Lunaison (${Format.nextPeriod(known)}) Pour afficher ou mettre à jour mes estimations sur ton téléphone, colle ce message dans Lunaison (Accueil > Mettre à jour ou Partager > Scanner) : $encoded",
                    en = "Here is my Lunaison update (${Format.nextPeriod(known)}) To view or update my estimates on your phone, paste this message in Lunaison (Home > Update or Share > Scan): $encoded",
                    pl = "Oto mój status w Lunaison (${Format.nextPeriod(known)}) Aby wyświetlić lub zaktualizować moje prognozy na Twoim telefonie, wklej tę wiadomość w Lunaison (Dzisiaj > Aktualizuj lub Udostępnij > Skanuj): $encoded",
                    uk = "Ось мій стан у Lunaison (${Format.nextPeriod(known)}) Щоб побачити або оновити мої прогнози на твоєму телефоні, встав це повідомлення в Lunaison (Сьогодні > Оновити або Поділитися > Сканувати): $encoded",
                    es = "Aquí está mi seguimiento de Lunaison (${Format.nextPeriod(known)}) Para ver o actualizar mis estimaciones en tu teléfono, pega este mensaje en Lunaison (Hoy > Actualizar o Compartir > Escanear): $encoded",
                    de = "Hier ist mein Lunaison-Stand (${Format.nextPeriod(known)}) Um meine Schätzungen auf deinem Telefon anzuzeigen oder zu aktualisieren, füge diese Nachricht in Lunaison ein (Heute > Aktualisieren oder Teilen > Scannen): $encoded",
                )
            }
            if (partner != null) {
                LunaButton(
                    tr("Envoyer le code via ${partner.channel.label}", "Send code via ${partner.channel.label}", pl = "Wyślij kod przez ${partner.channel.label}", uk = "Надіслати код через ${partner.channel.label}", es = "Enviar el código por ${partner.channel.label}", de = "Code über ${partner.channel.label} senden"),
                    onClick = { ShareLauncher.send(context, partner, shareBody) },
                    style = LunaButtonStyle.Outlined,
                    icon = Icons.AutoMirrored.Filled.Send,
                )
            }
            LunaButton(
                if (partner != null) tr("Partager autrement (SMS, messagerie…)", "Share another way (SMS, messaging…)", pl = "Udostępnij inaczej (SMS, komunikator…)", uk = "Поділитися іншим способом (SMS, месенджер…)", es = "Compartir de otra forma (SMS, mensajería…)", de = "Anders teilen (SMS, Messenger…)")
                else tr("Partager le code (SMS, messagerie…)", "Share code (SMS, messaging…)", pl = "Udostępnij kod (SMS, komunikator…)", uk = "Поділитися кодом (SMS, месенджер…)", es = "Compartir el código (SMS, mensajería…)", de = "Code teilen (SMS, Messenger…)"),
                onClick = { ShareLauncher.shareAny(context, shareBody) },
                style = if (partner != null) LunaButtonStyle.Quiet else LunaButtonStyle.Outlined,
                icon = Icons.AutoMirrored.Filled.Send,
            )
        }
    }

    val followed = state.settings.followedPartner
    val followedCycle = state.followedPartnerCycle
    if (followed != null && followedCycle != null) {
        PartnerSnapshotCard(
            snapshot = followed,
            cycle = followedCycle,
            onRescan = { showScanner = true },
            onRemove = { actions.updateSettings { it.copy(followedPartner = null) } },
        )
    } else {
        LunaCard {
            Text(
                tr("Suivre le cycle d'un ou d'une partenaire", "Follow a partner's cycle", pl = "Śledź cykl partnera lub partnerki", uk = "Стежити за циклом партнера чи партнерки", es = "Seguir el ciclo de tu pareja", de = "Den Zyklus einer Partnerin oder eines Partners begleiten"),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                tr(
                    "Scannez le QR Code affiché sur le téléphone de votre partenaire, collez le SMS reçu ou collez les téléphones dos à dos (NFC) pour afficher ses estimations et les conseils de soutien.",
                    "Scan the QR Code shown on your partner's phone, paste the SMS received, or tap phones back-to-back (NFC) to view their estimates and support tips.",
                    pl = "Zeskanuj kod QR na telefonie partnera, wklej otrzymany SMS lub zbliż telefony tyłem do siebie (NFC), aby wyświetlić prognozy i wskazówki wsparcia.",
                    uk = "Відскануйте QR-код на телефоні партнера, вставте отримане SMS або прикладіть телефони спинками (NFC), щоб переглядати оцінки та поради щодо підтримки.",
                    es = "Escanea el código QR del teléfono de tu pareja, pega el SMS recibido o junta los teléfonos dorso con dorso (NFC) para ver sus previsiones y consejos de apoyo.",
                    de = "Scanne den QR-Code auf dem Gerät deines Partners, füge die empfangene SMS ein oder halte die Telefone Rücken an Rücken (NFC), um Vorhersagen und Tipps anzuzeigen.",
                ),
                style = MaterialTheme.typography.bodySmall,
            )
            LunaButton(
                tr(
                    "Scanner un QR Code (ou NFC / SMS)",
                    "Scan a QR Code (or NFC / SMS)",
                    pl = "Zeskanuj kod QR (lub NFC / SMS)",
                    uk = "Сканувати QR-код (або NFC / SMS)",
                    es = "Escanear un código QR (o NFC / SMS)",
                    de = "QR-Code scannen (oder NFC / SMS)",
                ),
                onClick = { showScanner = true },
                style = LunaButtonStyle.Outlined,
            )
        }
    }

    if (showScanner) {
        QrScannerDialog(
            existingPartner = followed,
            onScanned = { scanned ->
                showScanner = false
                actions.updateSettings { it.copy(followedPartner = scanned) }
            },
            onDismiss = { showScanner = false },
        )
    }
}
