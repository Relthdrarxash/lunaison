package fr.lunaison.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.lunaison.core.designsystem.LunaCard
import fr.lunaison.core.designsystem.SectionTitle
import fr.lunaison.core.model.tr

/** Un repère du schéma de la vulve : son numéro, son nom et sa courte explication. */
private class VulvaLandmark(val title: String, val description: String)

private fun vulvaLandmarks(): List<VulvaLandmark> = listOf(
    VulvaLandmark(
        tr(
            fr = "Mont du pubis",
            en = "Mons pubis",
            pl = "Wzgórek łonowy",
            uk = "Лобковий горбок",
            es = "Monte del pubis",
            de = "Schamhügel",
        ),
        tr(
            fr = "Renflement arrondi de tissu adipeux au-dessus de la vulve, recouvert de peau et, après la puberté, de poils.",
            en = "Rounded pad of fatty tissue above the vulva, covered with skin and, after puberty, with pubic hair.",
            pl = "Zaokrąglone wypukłe miejsce z tkanki tłuszczowej nad sromem, pokryte skórą i, po okresie dojrzewania, owłosieniem łonowym.",
            uk = "Заокруглене підвищення з жирової тканини над вульвою, вкрите шкірою та, після статевого дозрівання, волоссям.",
            es = "Zona redondeada de tejido graso situada sobre la vulva, cubierta de piel y, tras la pubertad, de vello.",
            de = "Gewölbtes Polster aus Fettgewebe oberhalb der Vulva, von Haut und nach der Pubertät von Schamhaaren bedeckt.",
        ),
    ),
    VulvaLandmark(
        tr(
            fr = "Capuchon du clitoris",
            en = "Clitoral hood",
            pl = "Kapturek łechtaczki",
            uk = "Капюшон клітора",
            es = "Capuchón del clítoris",
            de = "Klitorisvorhaut",
        ),
        tr(
            fr = "Repli de peau formé par la jonction des petites lèvres, qui recouvre et protège le gland.",
            en = "Fold of skin formed where the inner lips meet, covering and protecting the glans.",
            pl = "Fałd skóry powstały z połączenia warg sromowych mniejszych, który osłania i chroni żołądź łechtaczki.",
            uk = "Складка шкіри, утворена з'єднанням малих статевих губ, яка вкриває та захищає голівку клітора.",
            es = "Pliegue de piel formado por la unión de los labios menores, que cubre y protege el glande.",
            de = "Hautfalte, die durch die Vereinigung der inneren Schamlippen entsteht und die Klitoriseichel bedeckt und schützt.",
        ),
    ),
    VulvaLandmark(
        tr(
            fr = "Gland du clitoris",
            en = "Clitoral glans",
            pl = "Żołądź łechtaczki",
            uk = "Голівка клітора",
            es = "Glande del clítoris",
            de = "Klitoriseichel",
        ),
        tr(
            fr = "Partie visible du clitoris, très riche en terminaisons nerveuses. Le clitoris se prolonge à l'intérieur du corps.",
            en = "Visible part of the clitoris, very rich in nerve endings. The clitoris continues inside the body.",
            pl = "Widoczna część łechtaczki, bogato unerwiona. Łechtaczka ma dalszy ciąg wewnątrz ciała.",
            uk = "Видима частина клітора, з великою кількістю нервових закінчень. Клітор продовжується всередині тіла.",
            es = "Parte visible del clítoris, con muchísimas terminaciones nerviosas. El clítoris continúa en el interior del cuerpo.",
            de = "Sichtbarer Teil der Klitoris, sehr reich an Nervenenden. Die Klitoris setzt sich im Körperinneren fort.",
        ),
    ),
    VulvaLandmark(
        tr(
            fr = "Grandes lèvres",
            en = "Outer lips (labia majora)",
            pl = "Wargi sromowe większe",
            uk = "Великі статеві губи",
            es = "Labios mayores",
            de = "Äußere Schamlippen",
        ),
        tr(
            fr = "Deux replis de peau externes et charnus, qui entourent et protègent le reste de la vulve.",
            en = "Two fleshy outer folds of skin that surround and protect the rest of the vulva.",
            pl = "Dwa zewnętrzne, mięsiste fałdy skóry, które otaczają i chronią pozostałe części sromu.",
            uk = "Дві зовнішні м'ясисті складки шкіри, що оточують і захищають решту вульви.",
            es = "Dos pliegues externos de piel, carnosos, que rodean y protegen el resto de la vulva.",
            de = "Zwei äußere, fleischige Hautfalten, die den Rest der Vulva umgeben und schützen.",
        ),
    ),
    VulvaLandmark(
        tr(
            fr = "Petites lèvres",
            en = "Inner lips (labia minora)",
            pl = "Wargi sromowe mniejsze",
            uk = "Малі статеві губи",
            es = "Labios menores",
            de = "Innere Schamlippen",
        ),
        tr(
            fr = "Deux replis plus fins, à l'intérieur des grandes lèvres. Leur taille, leur forme et leur couleur varient beaucoup.",
            en = "Two thinner folds inside the outer lips. Their size, shape and colour vary a great deal.",
            pl = "Dwa cieńsze fałdy skóry, położone do wewnątrz od warg większych. Ich wielkość, kształt i kolor bywają bardzo różne.",
            uk = "Дві тонші складки шкіри всередині від великих губ. Їхні розмір, форма та колір можуть дуже відрізнятися.",
            es = "Dos pliegues más finos situados dentro de los labios mayores. Su tamaño, forma y color varían mucho.",
            de = "Zwei dünnere Hautfalten innerhalb der äußeren Schamlippen. Größe, Form und Farbe sind sehr unterschiedlich.",
        ),
    ),
    VulvaLandmark(
        tr(
            fr = "Méat urinaire",
            en = "Urethral opening",
            pl = "Ujście cewki moczowej",
            uk = "Зовнішній отвір сечівника",
            es = "Meato urinario",
            de = "Harnröhrenöffnung",
        ),
        tr(
            fr = "Petit orifice par lequel sort l'urine, entre le clitoris et l'entrée du vagin.",
            en = "Small opening where urine leaves the body, between the clitoris and the vaginal opening.",
            pl = "Mały otwór, przez który wydalany jest mocz, położony między łechtaczką a wejściem do pochwy.",
            uk = "Невеликий отвір, через який виводиться сеча, розташований між клітором і входом до піхви.",
            es = "Pequeño orificio por el que sale la orina, situado entre el clítoris y la entrada de la vagina.",
            de = "Kleine Öffnung, durch die der Urin austritt, zwischen Klitoris und Scheideneingang.",
        ),
    ),
    VulvaLandmark(
        tr(
            fr = "Orifice du vagin",
            en = "Vaginal opening",
            pl = "Wejście do pochwy",
            uk = "Вхід до піхви",
            es = "Orificio vaginal",
            de = "Scheideneingang",
        ),
        tr(
            fr = "Entrée du vagin, qui mène au col de l'utérus. C'est par là que s'écoulent les règles.",
            en = "Entrance of the vagina, which leads to the cervix. Menstrual blood flows out through it.",
            pl = "Otwór prowadzący do pochwy i dalej do szyjki macicy. Tędy wypływa krew menstruacyjna.",
            uk = "Отвір, що веде до піхви й далі до шийки матки. Саме звідси виділяється менструальна кров.",
            es = "Entrada de la vagina, que lleva al cuello del útero. Por aquí sale la sangre de la regla.",
            de = "Öffnung der Scheide, die zum Gebärmutterhals führt. Hier tritt das Menstruationsblut aus.",
        ),
    ),
    VulvaLandmark(
        tr(
            fr = "Périnée",
            en = "Perineum",
            pl = "Krocze",
            uk = "Промежина",
            es = "Periné",
            de = "Damm",
        ),
        tr(
            fr = "Zone de peau entre l'entrée du vagin et l'anus.",
            en = "Area of skin between the vaginal opening and the anus.",
            pl = "Obszar skóry między wejściem do pochwy a odbytem.",
            uk = "Ділянка шкіри між входом до піхви та анусом.",
            es = "Zona de piel entre la entrada de la vagina y el ano.",
            de = "Hautbereich zwischen Scheideneingang und After.",
        ),
    ),
    VulvaLandmark(
        tr(
            fr = "Anus",
            en = "Anus",
            pl = "Odbyt",
            uk = "Анус",
            es = "Ano",
            de = "After",
        ),
        tr(
            fr = "Orifice terminal du tube digestif, distinct du vagin et de l'urètre.",
            en = "Final opening of the digestive tract, separate from the vagina and the urethra.",
            pl = "Zewnętrzny otwór przewodu pokarmowego, oddzielny od pochwy i cewki moczowej.",
            uk = "Кінцевий отвір травного тракту, окремий від піхви та сечівника.",
            es = "Orificio final del aparato digestivo, distinto de la vagina y de la uretra.",
            de = "Äußere Öffnung des Verdauungstrakts, getrennt von Scheide und Harnröhre.",
        ),
    ),
)

/**
 * Schéma sobre de l'anatomie externe (vulve), vue de face, avec neuf repères numérotés
 * dont la légende est en texte sous le dessin (donc traduite comme le reste de l'appli).
 */
@Composable
fun VulvaDiagram() {
    val colors = MaterialTheme.colorScheme
    val landmarks = vulvaLandmarks()
    val description = tr(
        fr = "Schéma de la vulve vue de face, avec neuf repères numérotés : de haut en bas, le mont du pubis, le capuchon et le gland du clitoris, les grandes et les petites lèvres, le méat urinaire, l'orifice du vagin, le périnée et l'anus.",
        en = "Diagram of the vulva seen from the front, with nine numbered landmarks: from top to bottom, the mons pubis, the clitoral hood and glans, the outer and inner lips, the urethral opening, the vaginal opening, the perineum and the anus.",
        pl = "Schemat sromu widziany z przodu z dziewięcioma ponumerowanymi punktami: od góry wzgórek łonowy, kapturek i żołądź łechtaczki, wargi sromowe większe i mniejsze, ujście cewki moczowej, wejście do pochwy, krocze i odbyt.",
        uk = "Схема вульви спереду з дев'ятьма пронумерованими позначками: згори донизу лобковий горбок, капюшон і голівка клітора, великі та малі статеві губи, отвір сечівника, вхід до піхви, промежина й анус.",
        es = "Esquema de la vulva vista de frente con nueve puntos numerados: de arriba abajo, monte del pubis, capuchón y glande del clítoris, labios mayores y menores, meato urinario, orificio vaginal, periné y ano.",
        de = "Schema der Vulva von vorn mit neun nummerierten Markierungen: von oben nach unten Schamhügel, Klitorisvorhaut und Klitoriseichel, äußere und innere Schamlippen, Harnröhrenöffnung, Scheideneingang, Damm und After.",
    )

    SectionTitle(
        tr(
            fr = "Anatomie externe : la vulve",
            en = "External anatomy: the vulva",
            pl = "Anatomia zewnętrzna: srom",
            uk = "Зовнішня анатомія: вульва",
            es = "Anatomía externa: la vulva",
            de = "Äußere Anatomie: die Vulva",
        ),
    )
    LunaCard {
        Text(
            tr(
                fr = "Vue de face, schéma simplifié. Chaque numéro correspond à un repère de la légende.",
                en = "Front view, simplified diagram. Each number matches an entry in the legend.",
                pl = "Widok z przodu, schemat uproszczony. Każdy numer odpowiada pozycji w legendzie.",
                uk = "Вигляд спереду, спрощена схема. Кожен номер відповідає пункту легенди.",
                es = "Vista frontal, esquema simplificado. Cada número corresponde a un elemento de la leyenda.",
                de = "Ansicht von vorn, vereinfachtes Schema. Jede Nummer entspricht einem Eintrag der Legende.",
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
        )

        val measurer = rememberTextMeasurer()
        val badgeStyle = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.onPrimary)
        val skin = colors.secondaryContainer
        val outer = colors.tertiaryContainer
        val inner = colors.primary.copy(alpha = 0.22f)
        val line = colors.onSurfaceVariant
        val opening = colors.surface

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surfaceVariant.copy(alpha = 0.35f))
                .semantics { contentDescription = description },
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(320.dp)) {
                val w = size.width
                val h = size.height
                // Le dessin occupe un carré centré dans le cadre, les repères sont sur les côtés.
                val cx = w / 2f
                val bodyW = minOf(w * 0.62f, h * 0.70f)
                fun px(fx: Float): Float = cx + fx * bodyW
                fun py(fy: Float): Float = fy * h
                val hair = 1.5.dp.toPx()
                val edge = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

                // Silhouette de la région : mont du pubis en haut, périnée et anus en bas.
                val silhouette = Path().apply {
                    moveTo(px(0f), py(0.03f))
                    cubicTo(px(0.46f), py(0.03f), px(0.46f), py(0.30f), px(0.38f), py(0.60f))
                    cubicTo(px(0.34f), py(0.84f), px(0.22f), py(0.97f), px(0f), py(0.97f))
                    cubicTo(px(-0.22f), py(0.97f), px(-0.34f), py(0.84f), px(-0.38f), py(0.60f))
                    cubicTo(px(-0.46f), py(0.30f), px(-0.46f), py(0.03f), px(0f), py(0.03f))
                    close()
                }
                drawPath(silhouette, skin)
                drawPath(silhouette, line.copy(alpha = 0.5f), style = edge)

                // Grandes lèvres (de part et d'autre, du mont du pubis au périnée)
                for (side in listOf(-1f, 1f)) {
                    val lip = Path().apply {
                        moveTo(px(side * 0.06f), py(0.26f))
                        cubicTo(px(side * 0.34f), py(0.28f), px(side * 0.36f), py(0.64f), px(side * 0.16f), py(0.80f))
                        cubicTo(px(side * 0.10f), py(0.84f), px(side * 0.08f), py(0.76f), px(side * 0.09f), py(0.62f))
                        cubicTo(px(side * 0.10f), py(0.48f), px(side * 0.07f), py(0.36f), px(side * 0.06f), py(0.26f))
                        close()
                    }
                    drawPath(lip, outer)
                    drawPath(lip, line, style = edge)
                }

                // Vestibule : zone entre les petites lèvres
                val vestibule = Path().apply {
                    moveTo(px(0f), py(0.36f))
                    cubicTo(px(0.12f), py(0.38f), px(0.13f), py(0.64f), px(0.05f), py(0.74f))
                    cubicTo(px(0.02f), py(0.76f), px(-0.02f), py(0.76f), px(-0.05f), py(0.74f))
                    cubicTo(px(-0.13f), py(0.64f), px(-0.12f), py(0.38f), px(0f), py(0.36f))
                    close()
                }
                drawPath(vestibule, opening.copy(alpha = 0.55f))

                // Petites lèvres : deux replis fins qui se rejoignent en haut autour du clitoris
                for (side in listOf(-1f, 1f)) {
                    val lip = Path().apply {
                        moveTo(px(side * 0.02f), py(0.31f))
                        cubicTo(px(side * 0.20f), py(0.35f), px(side * 0.21f), py(0.64f), px(side * 0.07f), py(0.77f))
                        cubicTo(px(side * 0.05f), py(0.76f), px(side * 0.045f), py(0.70f), px(side * 0.06f), py(0.62f))
                        cubicTo(px(side * 0.11f), py(0.50f), px(side * 0.09f), py(0.42f), px(side * 0.02f), py(0.31f))
                        close()
                    }
                    drawPath(lip, skin)
                    drawPath(lip, inner)
                    drawPath(lip, line, style = edge)
                }

                // Capuchon du clitoris : arche qui recouvre le gland
                val hood = Path().apply {
                    moveTo(px(-0.075f), py(0.37f))
                    cubicTo(px(-0.075f), py(0.25f), px(0.075f), py(0.25f), px(0.075f), py(0.37f))
                    cubicTo(px(0.04f), py(0.35f), px(-0.04f), py(0.35f), px(-0.075f), py(0.37f))
                    close()
                }
                drawPath(hood, skin)
                drawPath(hood, inner)
                drawPath(hood, line, style = edge)

                // Gland du clitoris
                drawCircle(colors.primary.copy(alpha = 0.75f), radius = bodyW * 0.030f, center = Offset(px(0f), py(0.385f)))
                drawCircle(line, radius = bodyW * 0.030f, center = Offset(px(0f), py(0.385f)), style = edge)

                // Méat urinaire
                drawCircle(line, radius = bodyW * 0.014f, center = Offset(px(0f), py(0.475f)))

                // Orifice du vagin
                drawOval(
                    color = opening,
                    topLeft = Offset(px(-0.055f), py(0.55f)),
                    size = Size(bodyW * 0.11f, h * 0.15f),
                )
                drawOval(
                    color = line,
                    topLeft = Offset(px(-0.055f), py(0.55f)),
                    size = Size(bodyW * 0.11f, h * 0.15f),
                    style = edge,
                )

                // Périnée : fine ligne médiane entre la fourchette et l'anus
                drawLine(line.copy(alpha = 0.45f), Offset(px(0f), py(0.76f)), Offset(px(0f), py(0.86f)), strokeWidth = hair)

                // Anus
                drawCircle(colors.primary.copy(alpha = 0.5f), radius = bodyW * 0.035f, center = Offset(px(0f), py(0.90f)))
                drawCircle(line, radius = bodyW * 0.035f, center = Offset(px(0f), py(0.90f)), style = edge)

                // Repères numérotés : trait fin de la pastille vers l'élément visé
                fun marker(number: Int, badge: Offset, target: Offset) {
                    drawLine(line, badge, target, strokeWidth = 1.dp.toPx())
                    drawCircle(line, radius = 2.5.dp.toPx(), center = target)
                    drawCircle(colors.primary, radius = 10.dp.toPx(), center = badge)
                    drawBadgeNumber(measurer, number.toString(), badge, badgeStyle)
                }

                val left = w * 0.07f
                val right = w * 0.93f
                marker(1, Offset(left, py(0.12f)), Offset(px(-0.20f), py(0.14f)))
                marker(2, Offset(right, py(0.16f)), Offset(px(0.05f), py(0.30f)))
                marker(3, Offset(left, py(0.30f)), Offset(px(-0.02f), py(0.385f)))
                marker(4, Offset(right, py(0.40f)), Offset(px(0.28f), py(0.50f)))
                marker(5, Offset(left, py(0.50f)), Offset(px(-0.12f), py(0.52f)))
                marker(6, Offset(right, py(0.52f)), Offset(px(0.012f), py(0.475f)))
                marker(7, Offset(left, py(0.65f)), Offset(px(-0.04f), py(0.64f)))
                marker(8, Offset(right, py(0.80f)), Offset(px(0f), py(0.81f)))
                marker(9, Offset(left, py(0.90f)), Offset(px(-0.035f), py(0.90f)))
            }
        }

        // Légende numérotée
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            landmarks.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier = Modifier.size(22.dp).clip(CircleShape).background(colors.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            (index + 1).toString(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.onPrimary,
                        )
                    }
                    Column {
                        Text(item.title, style = MaterialTheme.typography.titleSmall)
                        Text(
                            item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        Text(
            tr(
                fr = "Toutes les vulves sont différentes : la forme, la taille et la couleur des lèvres varient beaucoup d'une personne à l'autre, et c'est normal. Ce schéma est simplifié, il ne sert pas de modèle à comparer.",
                en = "Every vulva is different: the shape, size and colour of the lips vary a lot from one person to another, and that is normal. This diagram is simplified and is not a model to compare yourself with.",
                pl = "Każdy srom jest inny: kształt, wielkość i kolor warg bardzo różnią się między osobami i to jest zupełnie normalne. Schemat jest uproszczony i nie służy do porównań.",
                uk = "Кожна вульва унікальна: форма, розмір і колір статевих губ дуже різняться від людини до людини, і це цілком нормально. Схема спрощена й не призначена для порівнянь.",
                es = "Todas las vulvas son diferentes: la forma, el tamaño y el color de los labios varían mucho de una persona a otra, y es normal. Este esquema está simplificado y no sirve para compararse.",
                de = "Jede Vulva ist anders: Form, Größe und Farbe der Schamlippen unterscheiden sich von Mensch zu Mensch sehr stark, und das ist normal. Das Schema ist vereinfacht und kein Maßstab zum Vergleichen.",
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/** Écrit un numéro (langue neutre) centré sur une pastille du schéma. */
private fun DrawScope.drawBadgeNumber(
    measurer: androidx.compose.ui.text.TextMeasurer,
    text: String,
    center: Offset,
    style: TextStyle,
) {
    val layout = measurer.measure(text, style)
    drawText(layout, topLeft = Offset(center.x - layout.size.width / 2f, center.y - layout.size.height / 2f))
}
