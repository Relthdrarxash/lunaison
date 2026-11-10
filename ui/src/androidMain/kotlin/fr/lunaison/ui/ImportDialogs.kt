package fr.lunaison.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import fr.lunaison.core.data.ImportSummary
import fr.lunaison.core.data.InvalidPasswordException
import fr.lunaison.core.data.PeriodCalendarImporter
import fr.lunaison.core.model.tr
import kotlinx.coroutines.launch

/** Message affiché après un import réussi. */
internal fun importSummaryMessage(summary: ImportSummary): String =
    if (summary.cyclesCount > 0) {
        tr(
            fr = "${summary.daysCount} journées et ${summary.cyclesCount} cycles importés depuis Calendrier.",
            en = "${summary.daysCount} days and ${summary.cyclesCount} cycles imported from Calendar.",
            pl = "Zaimportowano z aplikacji Kalendarz (dni: ${summary.daysCount}, cykle: ${summary.cyclesCount}).",
            uk = "Імпортовано з Календаря (днів: ${summary.daysCount}, циклів: ${summary.cyclesCount}).",
            es = "${summary.daysCount} días y ${summary.cyclesCount} ciclos importados desde Calendario.",
            de = "${summary.daysCount} Tage und ${summary.cyclesCount} Zyklen aus Kalender importiert.",
        )
    } else {
        tr("${summary.daysCount} journées importées.", "${summary.daysCount} days imported.", pl = "Zaimportowano dni: ${summary.daysCount}.", uk = "Імпортовано днів: ${summary.daysCount}.", es = "${summary.daysCount} días importados.", de = "${summary.daysCount} Tage importiert.")
    }

/**
 * Import d'un historique (Calendrier des règles, CSV Lunaison) : choix de la source, sélecteur de fichier du système,
 * mot de passe d'une archive chiffrée. Partagé par les réglages et l'accueil du premier lancement.
 * Tout est traité sur le téléphone ; [onResult] reçoit le message à afficher.
 */
@Composable
actual fun ImportDialogs(
    showSource: Boolean,
    onDismiss: () -> Unit,
    actions: Actions,
    onResult: (String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showPasswordDialog by rememberSaveable { mutableStateOf(false) }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingZipBytes by remember { mutableStateOf<ByteArray?>(null) }
    val importResult by rememberUpdatedState(onResult)

    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    if (bytes == null || bytes.isEmpty()) {
                        importResult(tr("Fichier vide ou inaccessible.", "Empty or inaccessible file.", pl = "Plik jest pusty lub niedostępny.", uk = "Файл порожній або недоступний.", es = "Archivo vacío o inaccesible.", de = "Datei leer oder nicht zugänglich."))
                        return@launch
                    }
                    if (PeriodCalendarImporter.isZip(bytes) && PeriodCalendarImporter.isEncryptedZip(bytes)) {
                        pendingZipBytes = bytes
                        passwordInput = ""
                        passwordError = null
                        showPasswordDialog = true
                    } else {
                        val summary = actions.importAuto(bytes, null)
                        importResult(importSummaryMessage(summary))
                    }
                } catch (e: Exception) {
                    importResult(tr("Erreur lors de l'import : ${e.localizedMessage}", "Import error: ${e.localizedMessage}", pl = "Błąd podczas importu: ${e.localizedMessage}", uk = "Помилка під час імпорту: ${e.localizedMessage}", es = "Error al importar: ${e.localizedMessage}", de = "Fehler beim Import: ${e.localizedMessage}"))
                }
            }
        }
    }

    if (showSource) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text(tr("Importer des données", "Import data", pl = "Importuj dane", uk = "Імпорт даних", es = "Importar datos", de = "Daten importieren")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        tr(
                            fr = "De quelle application souhaitez-vous récupérer votre historique ?",
                            en = "Which app do you want to import your history from?",
                            pl = "Z której aplikacji chcesz zaimportować swoją historię?",
                            uk = "З якого застосунку ви хочете імпортувати свою історію?",
                            es = "¿De qué aplicación quieres recuperar tu historial?",
                            de = "Aus welcher App möchtest du deinen Verlauf importieren?",
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Card(
                        onClick = {
                            onDismiss()
                            importer.launch(arrayOf("application/zip", "application/json", "application/octet-stream", "*/*"))
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                tr("Calendrier des règles", "Period Calendar", pl = "Kalendarz miesiączek", uk = "Календар менструацій", es = "Calendario menstrual", de = "Periodenkalender"),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                tr(
                                    fr = "Archive .zip (chiffrée par mot de passe) ou export .json de l'application.",
                                    en = ".zip archive (password-protected) or .json export from the app.",
                                    pl = "Archiwum .zip (zabezpieczone hasłem) lub eksport .json z aplikacji.",
                                    uk = "Архів .zip (захищений паролем) або експорт .json із застосунку.",
                                    es = "Archivo .zip (cifrado con contraseña) o exportación .json de la aplicación.",
                                    de = "Archiv .zip (passwortgeschützt) oder .json-Export der App.",
                                ),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                    Card(
                        onClick = {
                            onDismiss()
                            importer.launch(arrayOf("text/*", "text/comma-separated-values", "application/csv", "*/*"))
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                tr("Lunaison ou tableur (CSV)", "Lunaison or spreadsheet (CSV)", pl = "Lunaison lub arkusz kalkulacyjny (CSV)", uk = "Lunaison або електронна таблиця (CSV)", es = "Lunaison u hoja de cálculo (CSV)", de = "Lunaison oder Tabellenkalkulation (CSV)"),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                tr(
                                    fr = "Fichier .csv de sauvegarde exporté depuis Lunaison ou un tableur.",
                                    en = ".csv backup file exported from Lunaison or a spreadsheet.",
                                    pl = "Plik kopii zapasowej .csv wyeksportowany z Lunaison lub arkusza kalkulacyjnego.",
                                    uk = "Файл резервної копії .csv, експортований із Lunaison або електронної таблиці.",
                                    es = "Archivo .csv de copia de seguridad exportado desde Lunaison o una hoja de cálculo.",
                                    de = ".csv-Sicherungsdatei, exportiert aus Lunaison oder einer Tabellenkalkulation.",
                                ),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                    Text(
                        tr(
                            fr = "D'autres applications (Clue, Flo...) seront ajoutées dans les prochaines mises à jour.",
                            en = "Other applications (Clue, Flo...) will be added in upcoming updates.",
                            pl = "Kolejne aplikacje (Clue, Flo...) zostaną dodane w następnych aktualizacjach.",
                            uk = "Інші застосунки (Clue, Flo...) буде додано в наступних оновленнях.",
                            es = "Se añadirán otras aplicaciones (Clue, Flo...) en próximas actualizaciones.",
                            de = "Weitere Apps (Clue, Flo...) werden in kommenden Updates hinzugefügt.",
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { onDismiss() }) {
                    Text(tr("Annuler", "Cancel", pl = "Anuluj", uk = "Скасувати", es = "Cancelar", de = "Abbrechen"))
                }
            },
        )
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showPasswordDialog = false
                pendingZipBytes = null
            },
            title = { Text(tr("Sauvegarde protégée", "Protected backup", pl = "Chroniona kopia zapasowa", uk = "Захищена резервна копія", es = "Copia de seguridad protegida", de = "Geschützte Sicherung")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        tr(
                            fr = "Cette sauvegarde de l'application « Calendrier » est chiffrée. Entrez le mot de passe fourni lors de l'exportation :",
                            en = "This backup from the \"Calendar\" app is encrypted. Enter the password provided during export:",
                            pl = "Ta kopia zapasowa z aplikacji „Kalendarz” jest zaszyfrowana. Wpisz hasło podane podczas eksportu:",
                            uk = "Ця резервна копія із застосунку «Календар» зашифрована. Введіть пароль, указаний під час експорту:",
                            es = "Esta copia de seguridad de la aplicación «Calendario» está cifrada. Introduce la contraseña indicada durante la exportación:",
                            de = "Diese Sicherung der App „Kalender“ ist verschlüsselt. Gib das beim Export vergebene Passwort ein:",
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            passwordError = null
                        },
                        label = { Text(tr("Mot de passe", "Password", pl = "Hasło", uk = "Пароль", es = "Contraseña", de = "Passwort")) },
                        singleLine = true,
                        isError = passwordError != null,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (passwordError != null) {
                        Text(
                            passwordError.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val bytes = pendingZipBytes
                        if (bytes != null) {
                            scope.launch {
                                try {
                                    val summary = actions.importAuto(bytes, passwordInput.trim())
                                    showPasswordDialog = false
                                    pendingZipBytes = null
                                    importResult(importSummaryMessage(summary))
                                } catch (e: InvalidPasswordException) {
                                    passwordError = tr("Mot de passe incorrect.", "Incorrect password.", pl = "Nieprawidłowe hasło.", uk = "Неправильний пароль.", es = "Contraseña incorrecta.", de = "Falsches Passwort.")
                                } catch (e: Exception) {
                                    passwordError = tr("Erreur : ${e.localizedMessage}", "Error: ${e.localizedMessage}", pl = "Błąd: ${e.localizedMessage}", uk = "Помилка: ${e.localizedMessage}", es = "Error: ${e.localizedMessage}", de = "Fehler: ${e.localizedMessage}")
                                }
                            }
                        }
                    },
                ) {
                    Text(tr("Déchiffrer et importer", "Decrypt & import", pl = "Odszyfruj i zaimportuj", uk = "Розшифрувати й імпортувати", es = "Descifrar e importar", de = "Entschlüsseln und importieren"))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPasswordDialog = false
                        pendingZipBytes = null
                    },
                ) {
                    Text(tr("Annuler", "Cancel", pl = "Anuluj", uk = "Скасувати", es = "Cancelar", de = "Abbrechen"))
                }
            },
        )
    }
}
