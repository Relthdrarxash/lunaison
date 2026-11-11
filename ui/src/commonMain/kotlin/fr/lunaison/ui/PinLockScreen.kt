package fr.lunaison.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import fr.lunaison.core.data.AppSettings
import fr.lunaison.core.model.tr

/**
 * Écran de déverrouillage par code PIN à 4 chiffres au lancement de l'application,
 * lorsque la personne a activé le verrouillage dans les réglages.
 */
@Composable
fun PinUnlockScreen(
    expectedHash: String,
    onUnlocked: () -> Unit,
) {
    var entered by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf(false) }

    fun appendDigit(digit: Char) {
        if (entered.length >= 4) return
        val next = entered + digit
        error = false
        if (next.length == 4) {
            if (AppSettings.hashPin(next) == expectedHash) {
                onUnlocked()
            } else {
                entered = ""
                error = true
            }
        } else {
            entered = next
        }
    }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Lunaison", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                if (error) tr("Code incorrect, réessayez", "Incorrect code, please try again", pl = "Nieprawidłowy kod, spróbuj ponownie", uk = "Неправильний код, спробуйте ще раз", es = "Código incorrecto, inténtalo de nuevo", de = "Falscher Code, bitte erneut versuchen")
                else tr("Entrez votre code à 4 chiffres", "Enter your 4-digit code", pl = "Wpisz swój 4-cyfrowy kod", uk = "Введіть свій 4-значний код", es = "Introduce tu código de 4 dígitos", de = "Gib deinen 4-stelligen Code ein"),
                style = MaterialTheme.typography.bodyLarge,
                color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.semantics {
                    contentDescription = tr("${entered.length} chiffres saisis sur 4", "${entered.length} of 4 digits entered", pl = "Wpisane cyfry: ${entered.length} z 4", uk = "Введено цифр: ${entered.length} з 4", es = "${entered.length} de 4 dígitos introducidos", de = "${entered.length} von 4 Ziffern eingegeben")
                },
            ) {
                repeat(4) { index ->
                    val filled = index < entered.length
                    Box(
                        Modifier
                            .size(16.dp)
                            .background(
                                if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape,
                            ),
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
            val rows = listOf(
                listOf('1', '2', '3'),
                listOf('4', '5', '6'),
                listOf('7', '8', '9'),
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        row.forEach { digit ->
                            PinKey(label = digit.toString(), onClick = { appendDigit(digit) })
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.size(72.dp))
                    PinKey(label = "0", onClick = { appendDigit('0') })
                    TextButton(
                        onClick = { if (entered.isNotEmpty()) entered = entered.dropLast(1) },
                        modifier = Modifier.size(72.dp),
                    ) {
                        Text("⌫", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun PinKey(label: String, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier.size(72.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Text(label, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun PinSetupDialog(
    onSavePin: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var pin by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("Choisir un code à 4 chiffres", "Choose a 4-digit PIN", pl = "Wybierz 4-cyfrowy kod", uk = "Оберіть 4-значний код", es = "Elige un código de 4 dígitos", de = "4-stelligen Code wählen")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    tr("Ce code sera demandé à l'ouverture de Lunaison et masquera l'aperçu dans les applications récentes. Attention : comme tout reste sur votre téléphone, il n'existe pas de récupération à distance.", "This code will be requested when opening Lunaison and will hide the preview in recent apps. Note: since everything stays on your phone, there is no remote recovery.", pl = "Ten kod będzie wymagany przy otwieraniu Lunaison i ukryje podgląd w ostatnio używanych aplikacjach. Uwaga: ponieważ wszystko pozostaje na Twoim telefonie, nie ma zdalnego odzyskiwania.", uk = "Цей код вимагатиметься під час відкриття Lunaison і приховуватиме попередній перегляд у нещодавніх застосунках. Увага: оскільки все залишається на вашому телефоні, віддаленого відновлення не існує.", es = "Este código se pedirá al abrir Lunaison y ocultará la vista previa en las aplicaciones recientes. Atención: como todo se queda en tu teléfono, no existe recuperación a distancia.", de = "Dieser Code wird beim Öffnen von Lunaison abgefragt und blendet die Vorschau in den zuletzt verwendeten Apps aus. Achtung: Da alles auf deinem Telefon bleibt, gibt es keine Wiederherstellung aus der Ferne."),
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = pin,
                    onValueChange = { value -> pin = value.filter { it.isDigit() }.take(4) },
                    label = { Text(tr("Code (4 chiffres)", "PIN (4 digits)", pl = "Kod (4 cyfry)", uk = "Код (4 цифри)", es = "Código (4 dígitos)", de = "Code (4 Ziffern)")) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (pin.length == 4) onSavePin(pin) },
                enabled = pin.length == 4,
            ) {
                Text(tr("Activer", "Enable", pl = "Włącz", uk = "Увімкнути", es = "Activar", de = "Aktivieren"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(tr("Annuler", "Cancel", pl = "Anuluj", uk = "Скасувати", es = "Cancelar", de = "Abbrechen"))
            }
        },
    )
}
