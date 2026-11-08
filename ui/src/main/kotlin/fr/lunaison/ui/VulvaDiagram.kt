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
            fr = "Le mont du pubis (le coussinet protecteur)",
            en = "Mons pubis (the protective cushion)",
            pl = "Wzgórek łonowy (poduszka ochronna)",
            uk = "Лобковий горбок (захисна подушечка)",
            es = "Monte del pubis (la almohadilla protectora)",
            de = "Schamhügel (das Schutzpolster)",
        ),
        tr(
            fr = "Un dôme de tissu moelleux situé au-dessus des lèvres, sur l'os du pubis. Recouvert de poils à la puberté, il agit comme un airbag naturel pour amortir les chocs et protéger les organes intimes lors des mouvements.",
            en = "A soft mound of natural padding over the pubic bone. Covered with hair after puberty, it acts as a natural shock-absorber to cushion delicate intimate parts during sports, movement, and intimacy.",
            pl = "Zaokrąglone wzniesienie z tkanki tłuszczowej nad kością łonową. Pokryte owłosieniem po okresie dojrzewania, działa jak naturalna poduszka chroniąca delikatne narządy przed naciskiem i otarciami.",
            uk = "М'яке підвищення з природної жирової тканини над лобковою кісткою. Вкрите волоссям після дозрівання, воно діє як подушка безпеки, захищаючи чутливі органи під час рухів.",
            es = "Una suave almohadilla de tejido graso sobre el hueso púbico. Cubierta de vello tras la pubertad, actúa como un amortiguador natural para proteger las estructuras íntimas frente al roce.",
            de = "Ein weiches Polster aus natürlichem Gewebe über dem Schambein. Nach der Pubertät behaart, dämpft und schützt es die empfindlichen inneren Organe wie ein kleiner Airbag.",
        ),
    ),
    VulvaLandmark(
        tr(
            fr = "Le capuchon du clitoris (la capuche protectrice)",
            en = "Clitoral hood (the protective eyelid)",
            pl = "Kapturek łechtaczki (fałd ochronny)",
            uk = "Капюшон клітора (захисна складка)",
            es = "Capuchón del clítoris (el pliegue protector)",
            de = "Klitorisvorhaut (die Schutzfalte)",
        ),
        tr(
            fr = "Un pli de peau doux situé tout en haut des petites lèvres. Semblable à une paupière délicate, il abrite la pointe ultrasensible du clitoris pour la protéger des frottements des sous-vêtements, et recule en douceur lorsqu'on le stimule.",
            en = "A delicate fold of soft skin where the inner lips meet. Just like an eyelid protects the eye, it shelters the hypersensitive tip of the clitoris from daily friction, sliding back smoothly during stimulation.",
            pl = "Delikatny fałd skóry w miejscu połączenia warg mniejszych. Działa jak powieka chroniąca niezwykle wrażliwy czubek łechtaczki przed otarciami bielizny.",
            uk = "М'яка складочка шкіри на стику малих губ. Як ніжна повіка для ока, вона оберігає надчутливу голівку клітора від тертя білизни й легко відсувається при торканні.",
            es = "Un pliegue suave de piel donde se unen los labios menores. Como un párpado protector, resguarda el glande hipersensible del clítoris del roce diario de la ropa y se desplaza suavemente.",
            de = "Eine zarte Hautfalte am oberen Ende der inneren Schamlippen. Wie ein Augenlid bewahrt sie die hochempfindliche Spitze vor Reibung durch Kleidung und gleitet bei Berührung sanft zurück.",
        ),
    ),
    VulvaLandmark(
        tr(
            fr = "Le gland du clitoris (la sonnette du plaisir)",
            en = "Clitoral glans (the visible pleasure center)",
            pl = "Żołądź łechtaczki (ośrodek przyjemności)",
            uk = "Голівка клітора (центр насолоди)",
            es = "Glande del clítoris (el centro de placer)",
            de = "Klitoriseichel (das Lustzentrum)",
        ),
        tr(
            fr = "La petite perle très sensible visible à l'extérieur. C'est le seul organe du corps humain voué exclusivement au plaisir, avec plus de 8 000 à 10 000 fibres nerveuses ! Mais ce n'est que la partie émergée de l'iceberg : sous la peau, le clitoris mesure 8 à 10 cm et déploie deux bras érectiles qui enlacent le vagin.",
            en = "A tiny, intensely sensitive pearl at the top of the vulva. It is the only organ in the human body dedicated purely to pleasure, packed with over 8,000 nerve endings! Yet it is just the tip of an iceberg: beneath the skin, the clitoris extends 8 to 10 cm with erectile branches embracing the vagina.",
            pl = "Niezwykle wrażliwa perełka widoczna na zewnątrz. To jedyny narząd ludzkiego ciała służący wyłącznie przyjemności, z ponad 8 000 zakończeń nerwowych! To wierzchołek góry lodowej: pod skórą łechtaczka ma 8-10 cm i obejmuje pochwę.",
            uk = "Крихітна надчутлива перлинка згори вульви. Це єдиний орган людського тіла, створений виключно для насолоди, з понад 8 000 нервових закінчень! Але це лише верхівка айсберга: під шкірою клітор має 8–10 см і огортає піхву ніжками.",
            es = "Un pequeño nodo hipersensible dedicado exclusivamente al placer, con más de 8.000 terminaciones nerviosas. Es solo la punta del iceberg: bajo la piel, el clítoris mide de 8 a 10 cm y abraza la vagina con sus ramas eréctiles.",
            de = "Eine kleine, hochempfindsame Perle oben an der Vulva. Das einzige Organ des Menschen, das einzig der Lust dient, mit über 8.000 Nervenenden! Sichtbar ist nur die Spitze des Eisbergs: Im Inneren misst die Klitoris 8 bis 10 cm.",
        ),
    ),
    VulvaLandmark(
        tr(
            fr = "Les grandes lèvres (les lèvres externes)",
            en = "Labia majora (outer lips)",
            pl = "Wargi sromowe większe (wargi zewnętrzne)",
            uk = "Великі статеві губи (зовнішні губи)",
            es = "Labios mayores (labios externos)",
            de = "Große Schamlippen (äußere Schamlippen)",
        ),
        tr(
            fr = "Deux replis de peau moelleux qui entourent et gardent au chaud les parties intimes. Malgré leur nom de « grandes », elles ne cachent pas forcément les petites lèvres : chez beaucoup de femmes, les petites lèvres dépassent naturellement vers l'extérieur.",
            en = "Two soft outer cushions that frame and shelter the intimate openings, keeping them warm and protected. Despite being called 'outer' or 'major', they do not always enclose the inner lips, which often peek past them naturally.",
            pl = "Dwa miękkie fałdy skóry osłaniające wnętrze sromu. Wbrew nazwie, nie muszą zakrywać warg mniejszych – u wielu kobiet wargi mniejsze naturalnie wystają na zewnątrz.",
            uk = "Дві м'які зовнішні складки, що огортають і зігрівають інтимні зони. Незважаючи на назву, вони зовсім не обов'язково ховають малі губи: у багатьох вони природно визирають назовні.",
            es = "Dos pliegues suaves de piel que rodean y resguardan las aberturas íntimas. A pesar de su nombre, no tienen que tapar a los labios menores: en muchos cuerpos, los menores sobresalen de forma completamente natural.",
            de = "Zwei weiche äußere Hautkissen, die das Innere der Vulva wärmend umschließen. Entgegen dem Namen müssen sie die inneren Lippen keineswegs verdecken – ein Hervorschauen ist völlig normal.",
        ),
    ),
    VulvaLandmark(
        tr(
            fr = "Les petites lèvres (les voiles intérieurs)",
            en = "Labia minora (inner mucosal lips)",
            pl = "Wargi sromowe mniejsze (wargi wewnętrzne)",
            uk = "Малі статеві губи (внутрішні губи)",
            es = "Labios menores (labios internos)",
            de = "Kleine Schamlippen (innere Schamlippen)",
        ),
        tr(
            fr = "Deux replis souples et très sensibles qui guident l'urine et font barrière aux poussières. Leurs bords peuvent être dentelés comme des pétales de fleur, courts ou dépasser largement, roses ou plus foncés : toutes ces formes sont parfaitement saines et magnifiques.",
            en = "Two soft, sensitive folds that guide the urinary stream and protect the entrance from bacteria. Their edges may be ruffled like flower petals, short or long, pale or pigmented: every variation is totally normal and healthy.",
            pl = "Dwa delikatne, bardzo czułe fałdy śluzówki chroniące wejście przed zanieczyszczeniami. Mogą być pofałdowane jak płatki kwiatów, wystawać, być jaśniejsze lub ciemniejsze – każda forma jest zdrowa i piękna.",
            uk = "Дві ніжні й чутливі складки, що спрямовують струмінь сечі та захищають від пилу. Їхні краї можуть нагадувати пелюстки квітів, бути довшими, світлішими чи темнішими — це абсолютно здорово й природно.",
            es = "Dos pliegues suaves y sensibles que encauzan la orina y protegen de bacterias. Pueden tener bordes ondulados como pétalos, sobresalir, ser claros o pigmentados: todas las formas son completamente sanas.",
            de = "Zwei zarte, feinfühlige Schleimhautfalten, die den Urinstrahl leiten und Schutz vor Bakterien bieten. Ob gewellt wie Blütenblätter, kürzer oder länger, rosa oder dunkel – jede Form ist gesund und wunderbar.",
        ),
    ),
    VulvaLandmark(
        tr(
            fr = "Le méat urinaire (la sortie de l'urine)",
            en = "Urethral opening (where urine exits)",
            pl = "Ujście cewki moczowej (odpływ moczu)",
            uk = "Зовнішній отвір сечівника (вихід сечі)",
            es = "Meato urinario (salida de la orina)",
            de = "Harnröhrenöffnung (Harnausgang)",
        ),
        tr(
            fr = "Le minuscule petit trou par lequel s'échappe le pipi, relié à la vessie. Il est situé juste entre le clitoris et le vagin. Attention à l'idée reçue : l'urine ne sort pas du tout par le vagin, ce sont deux portes de sortie entièrement différentes !",
            en = "The tiny doorway through which urine leaves the body from the bladder, located between the clitoris and the vagina. A vital fact: urine does not come from the vagina—they are two completely separate channels!",
            pl = "Niewielki otworek, którym wypływa mocz z pęcherza, położony między łechtaczką a pochwą. Ważny fakt: mocz nie wypływa z pochwy, to dwa zupełnie oddzielne przewody!",
            uk = "Крихітний отвір, крізь який виходить сеча з сечового міхура, розташований між клітором і піхвою. Важливо пам'ятати: сеча зовсім не виходить з піхви — це два абсолютно окремі канали!",
            es = "El diminuto orificio por donde sale la orina de la vejiga, situado entre el clítoris y la vagina. Dato esencial: la orina no sale por la vagina, son dos salidas completamente independientes.",
            de = "Die winzige Öffnung, durch die der Urin aus der Blase fließt, zwischen Klitoris und Scheideneingang gelegen. Wichtig: Urin kommt keineswegs aus der Scheide – es sind zwei völlig getrennte Ausgänge!",
        ),
    ),
    VulvaLandmark(
        tr(
            fr = "L'orifice du vagin (la porte vers l'intérieur)",
            en = "Vaginal opening (the doorway inside)",
            pl = "Wejście do pochwy (przejście wewnętrzne)",
            uk = "Вхід до піхви (двері всередину)",
            es = "Orificio vaginal (la entrada interna)",
            de = "Scheideneingang (die Pforte nach innen)",
        ),
        tr(
            fr = "L'entrée du canal intérieur qui mène à l'utérus. C'est par là que sort le flux des règles et les bébés à la naissance, et que l'on glisse un tampon, une coupe menstruelle ou lors des rapports sexuels. Ses parois sont très élastiques et s'adaptent naturellement sans douleur.",
            en = "The entrance to the internal canal leading up to the uterus. It is where menstrual flow leaves the body and where babies are born, as well as where tampons, cups, or intimacy enter. It is incredibly stretchy and naturally adapts.",
            pl = "Wejście do wewnętrznego kanału prowadzącego do macicy. Tędy wypływa krew miesiączkowa, rodzą się dzieci oraz tędy zakłada się tampon czy kubeczek menstruacyjny. Jest niezwykle elastyczne.",
            uk = "Вхід до внутрішнього каналу, що веде до матки. Звідси виходить менструальна кров і народжуються діти, сюди вводять тампон чи менструальну чашу. Його стінки дивовижно еластичні.",
            es = "La entrada al canal interno que conduce al útero. Por aquí sale el flujo menstrual y nacen los bebés, y por aquí se introduce un tampón, copa o durante la intimidad. Es sumamente elástico y adaptable.",
            de = "Der Eingang zum inneren Kanal, der zur Gebärmutter führt. Hier fließt das Menstruationsblut ab, hier werden Babys geboren und hier finden Tampons, Tassen oder Intimität Raum. Es ist elastisch und anpassungsfähig.",
        ),
    ),
    VulvaLandmark(
        tr(
            fr = "Le périnée (le pont de soutien)",
            en = "Perineum (the supportive bridge)",
            pl = "Krocze (most podtrzymujący)",
            uk = "Промежина (опорний місток)",
            es = "Periné (el puente de soporte)",
            de = "Damm (die stützende Brücke)",
        ),
        tr(
            fr = "La bande de peau située entre le vagin et l'anus. En profondeur, elle cache un ensemble de muscles très puissants (le plancher pelvien) qui soutient tous vos organes comme un solide hamac élastique. C'est lui qui contrôle quand vous vous retenez d'uriner !",
            en = "The smooth bridge of skin between the vagina and the anus. Deep inside, it connects to a powerful muscular hammock (the pelvic floor) that supports your bladder, uterus, and bowels, helping control when you hold or release urine.",
            pl = "Obszar skóry między pochwą a odbytem. Wewnątrz kryje silne mięśnie dna miednicy, które podtrzymują narządy niczym elastyczny hamak i odpowiadają za trzymanie moczu.",
            uk = "Ділянка шкіри між піхвою та анусом. У глибині вона поєднується з сильними м'язами тазового дна, які тримають органи немов надійний еластичний гамак і контролюють сечовипускання.",
            es = "La zona de piel entre la vagina y el ano. En su interior alberga un potente conjunto muscular (suelo pélvico) que sostiene los órganos como una hamaca elástica y ayuda a controlar la micción.",
            de = "Der Hautbereich zwischen Scheide und After. In der Tiefe bildet er ein starkes Muskelnetz (Beckenboden), das wie eine elastische Hängematte die Organe stützt und das Halten des Urins steuert.",
        ),
    ),
    VulvaLandmark(
        tr(
            fr = "L'anus (la sortie de la digestion)",
            en = "Anus (the digestive exit)",
            pl = "Odbyt (ujście układu pokarmowego)",
            uk = "Анус (вихід травної системи)",
            es = "Ano (la salida digestiva)",
            de = "After (der Verdauungsausgang)",
        ),
        tr(
            fr = "La sortie par laquelle s'évacuent les selles, commandée par un muscle circulaire (sphincter). Bien qu'il soit voisin de la vulve, il n'a aucun lien avec la sexualité génitale ou les règles. Règle d'or : s'essuyer toujours de l'avant vers l'arrière pour garder les microbes loin du vagin et de la vessie.",
            en = "The exit for bowel movements, closed by a ring of muscle. Though close to the vulva, it is completely separate from your reproductive organs. Golden hygiene rule: always wipe from front to back to keep gut bacteria away from your bladder and vagina.",
            pl = "Zewnętrzny otwór przewodu pokarmowego zamykany mięśniem zwieraczem. Złota zasada higieny: zawsze podcieraj się od przodu do tyłu, by chronić pęcherz i pochwę przed bakteriami.",
            uk = "Кінцевий отвір травного тракту, оточений м'язом-сфінктером. Золоте правило гігієни: завжди витиратися спереду назад, щоб бактерії не потрапляли до піхви чи сечового міхура.",
            es = "La abertura por donde se expulsan las heces, cerrada por un esfínter muscular. Regla de oro de higiene: limpiarse siempre de delante hacia atrás para alejar las bacterias de la uretra y la vagina.",
            de = "Der äußere Ausgang des Darms, verschlossen von einem Ringmuskel. Goldene Hygieneregel: Immer von vorn nach hinten wischen, damit Darmbakterien nicht an Scheide oder Blase gelangen.",
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
        fr = "Schéma éducatif de la vulve vue de face : il met en évidence les trois orifices distincts (sortie d'urine, entrée du vagin, digestion) ainsi que les zones du plaisir et de protection, du mont du pubis jusqu'à l'anus.",
        en = "Educational diagram of the vulva seen from the front: highlighting the three separate openings (urinary, vaginal, digestive) alongside pleasure and protective zones from the mons pubis down to the anus.",
        pl = "Edukacyjny schemat sromu od przodu: ukazuje trzy niezależne ujścia (cewka moczowa, wejście do pochwy, odbyt) oraz strefy przyjemności i ochrony.",
        uk = "Навчальна схема вульви спереду: показує три окремі отвори (сечівник, вхід до піхви, анус) та зони задоволення й захисту.",
        es = "Esquema educativo de la vulva vista de frente: muestra los tres orificios independientes (uretra, vagina, ano) y las zonas de placer y protección.",
        de = "Pädagogisches Schema der Vulva von vorn: Zeigt die drei separaten Öffnungen (Harnröhre, Scheide, After) sowie Lust- und Schutzbereiche.",
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
                fr = "La vulve regroupe toutes les parties intimes visibles à l'extérieur. Suivez les 9 repères pour explorer votre anatomie en toute liberté, sans tabou.",
                en = "The vulva includes everything visible on the outside. Follow the 9 markers to explore your anatomy openly and with confidence.",
                pl = "Srom to wszystkie narządy intymne widoczne na zewnątrz. Poznaj 9 punktów, by bez tabu zrozumieć swoje ciało.",
                uk = "Вульва — це всі інтимні частини, видимі ззовні. Слідуйте за 9 позначками, щоб вільно й без табу пізнати своє тіло.",
                es = "La vulva comprende todas las partes íntimas visibles en el exterior. Sigue los 9 puntos para explorar tu anatomía sin tabúes.",
                de = "Die Vulva umfasst alle von außen sichtbaren Intimbereiche. Folge den 9 Markierungen, um deinen Körper ganz ohne Tabus zu verstehen.",
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
                .height(340.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surfaceVariant.copy(alpha = 0.35f))
                .semantics { contentDescription = description },
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(340.dp)) {
                val w = size.width
                val h = size.height
                val cx = w / 2f
                val bodyW = minOf(w * 0.60f, h * 0.68f)
                fun px(fx: Float): Float = cx + fx * bodyW
                fun py(fy: Float): Float = fy * h
                val hair = 1.0.dp.toPx()
                val edge = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                val fineEdge = Stroke(width = 1.0.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

                // 1. Silhouette globale de la région périnéale & cuisses
                val silhouette = Path().apply {
                    moveTo(px(-0.46f), py(0.06f))
                    cubicTo(px(-0.25f), py(0.03f), px(0.25f), py(0.03f), px(0.46f), py(0.06f))
                    // Pli de l'aine et haut de la cuisse droite
                    cubicTo(px(0.48f), py(0.24f), px(0.44f), py(0.50f), px(0.38f), py(0.72f))
                    cubicTo(px(0.32f), py(0.90f), px(0.20f), py(0.97f), px(0f), py(0.97f))
                    // Pli de l'aine et cuisse gauche
                    cubicTo(px(-0.20f), py(0.97f), px(-0.32f), py(0.90f), px(-0.38f), py(0.72f))
                    cubicTo(px(-0.44f), py(0.50f), px(-0.48f), py(0.24f), px(-0.46f), py(0.06f))
                    close()
                }
                drawPath(silhouette, skin)
                drawPath(silhouette, line.copy(alpha = 0.45f), style = edge)

                // Texture subtile du mont du pubis (follicules / poils fins discrets)
                val hairColor = line.copy(alpha = 0.22f)
                val pubicPoints = listOf(
                    px(-0.24f) to py(0.07f), px(-0.12f) to py(0.06f), px(0.08f) to py(0.06f), px(0.22f) to py(0.08f),
                    px(-0.18f) to py(0.10f), px(-0.04f) to py(0.09f), px(0.14f) to py(0.10f),
                    px(-0.26f) to py(0.13f), px(-0.10f) to py(0.13f), px(0.06f) to py(0.12f), px(0.24f) to py(0.14f),
                    px(-0.16f) to py(0.16f), px(0f) to py(0.15f), px(0.18f) to py(0.16f),
                    px(-0.08f) to py(0.18f), px(0.10f) to py(0.19f),
                )
                pubicPoints.forEach { (hx, hy) ->
                    val hPath = Path().apply {
                        moveTo(hx, hy)
                        cubicTo(hx + 2.dp.toPx(), hy + 3.dp.toPx(), hx - 1.dp.toPx(), hy + 6.dp.toPx(), hx + 1.5.dp.toPx(), hy + 8.dp.toPx())
                    }
                    drawPath(hPath, hairColor, style = fineEdge)
                }

                // 2. Grandes lèvres (Labia majora) : replis charnus avec galbe naturel
                for (side in listOf(-1f, 1f)) {
                    val outerLip = Path().apply {
                        // Commissure labiale antérieure
                        moveTo(px(0f), py(0.20f))
                        // Galbe externe convexe
                        cubicTo(px(side * 0.22f), py(0.22f), px(side * 0.36f), py(0.40f), px(side * 0.36f), py(0.56f))
                        cubicTo(px(side * 0.34f), py(0.70f), px(side * 0.22f), py(0.82f), px(0f), py(0.83f))
                        // Sillon interlabial interne
                        cubicTo(px(side * 0.12f), py(0.77f), px(side * 0.14f), py(0.64f), px(side * 0.13f), py(0.48f))
                        cubicTo(px(side * 0.11f), py(0.34f), px(side * 0.06f), py(0.24f), px(0f), py(0.20f))
                        close()
                    }
                    drawPath(outerLip, outer)
                    // Dégradé de volume subtil sur le renflement
                    val shadeLip = Path().apply {
                        moveTo(px(side * 0.08f), py(0.26f))
                        cubicTo(px(side * 0.24f), py(0.32f), px(side * 0.28f), py(0.50f), px(side * 0.24f), py(0.68f))
                    }
                    drawPath(shadeLip, line.copy(alpha = 0.18f), style = fineEdge)
                    drawPath(outerLip, line.copy(alpha = 0.70f), style = edge)
                }

                // 3. Vestibule vulvaire (zone médiane réceptive entre les petites lèvres)
                val vestibule = Path().apply {
                    moveTo(px(0f), py(0.28f))
                    cubicTo(px(0.12f), py(0.34f), px(0.14f), py(0.58f), px(0.06f), py(0.72f))
                    cubicTo(px(0.03f), py(0.75f), px(-0.03f), py(0.75f), px(-0.06f), py(0.72f))
                    cubicTo(px(-0.14f), py(0.58f), px(-0.12f), py(0.34f), px(0f), py(0.28f))
                    close()
                }
                drawPath(vestibule, opening.copy(alpha = 0.50f))

                // 4. Petites lèvres (Labia minora) : replis délicats, festonnés et ondulants
                for (side in listOf(-1f, 1f)) {
                    val innerLip = Path().apply {
                        // Origine sous le capuchon
                        moveTo(px(side * 0.02f), py(0.27f))
                        // Marge libre festonnée (ondulations naturelles et asymétriques)
                        cubicTo(px(side * 0.12f), py(0.30f), px(side * 0.16f), py(0.36f), px(side * 0.14f), py(0.42f))
                        cubicTo(px(side * 0.12f), py(0.46f), px(side * 0.18f), py(0.52f), px(side * 0.15f), py(0.60f))
                        cubicTo(px(side * 0.12f), py(0.66f), px(side * 0.08f), py(0.72f), px(0f), py(0.75f))
                        // Jonction postérieure / fourchette
                        cubicTo(px(side * 0.04f), py(0.70f), px(side * 0.06f), py(0.58f), px(side * 0.07f), py(0.46f))
                        cubicTo(px(side * 0.07f), py(0.36f), px(side * 0.04f), py(0.30f), px(side * 0.02f), py(0.27f))
                        close()
                    }
                    drawPath(innerLip, skin)
                    drawPath(innerLip, inner.copy(alpha = 0.35f))
                    drawPath(innerLip, line.copy(alpha = 0.85f), style = edge)
                }

                // 5. Capuchon du clitoris (Preputium clitoridis) : drapé protecteur avec plis
                val hood = Path().apply {
                    moveTo(px(-0.075f), py(0.32f))
                    cubicTo(px(-0.08f), py(0.23f), px(0.08f), py(0.23f), px(0.075f), py(0.32f))
                    cubicTo(px(0.04f), py(0.30f), px(-0.04f), py(0.30f), px(-0.075f), py(0.32f))
                    close()
                }
                drawPath(hood, skin)
                drawPath(hood, inner.copy(alpha = 0.30f))
                drawPath(hood, line, style = edge)

                // Plis cutanés transversaux délicats sur le capuchon
                val fold1 = Path().apply {
                    moveTo(px(-0.05f), py(0.27f))
                    cubicTo(px(-0.02f), py(0.255f), px(0.02f), py(0.255f), px(0.05f), py(0.27f))
                }
                val fold2 = Path().apply {
                    moveTo(px(-0.06f), py(0.29f))
                    cubicTo(px(-0.02f), py(0.275f), px(0.02f), py(0.275f), px(0.06f), py(0.29f))
                }
                drawPath(fold1, line.copy(alpha = 0.40f), style = fineEdge)
                drawPath(fold2, line.copy(alpha = 0.40f), style = fineEdge)

                // 6. Gland du clitoris (Glans clitoridis) : tubercule arrondi et son frein
                val glansCenter = Offset(px(0f), py(0.325f))
                val glansRadius = bodyW * 0.032f
                val glansPath = Path().apply {
                    moveTo(glansCenter.x - glansRadius, glansCenter.y)
                    cubicTo(glansCenter.x - glansRadius, glansCenter.y - glansRadius * 1.1f, glansCenter.x + glansRadius, glansCenter.y - glansRadius * 1.1f, glansCenter.x + glansRadius, glansCenter.y)
                    cubicTo(glansCenter.x + glansRadius, glansCenter.y + glansRadius * 0.8f, glansCenter.x, glansCenter.y + glansRadius * 1.2f, glansCenter.x, glansCenter.y + glansRadius * 1.2f)
                    cubicTo(glansCenter.x, glansCenter.y + glansRadius * 1.2f, glansCenter.x - glansRadius, glansCenter.y + glansRadius * 0.8f, glansCenter.x - glansRadius, glansCenter.y)
                    close()
                }
                drawPath(glansPath, colors.primary.copy(alpha = 0.80f))
                // Reflet bombé
                drawCircle(Color.White.copy(alpha = 0.35f), radius = glansRadius * 0.35f, center = Offset(glansCenter.x - glansRadius * 0.25f, glansCenter.y - glansRadius * 0.25f))
                drawPath(glansPath, line, style = edge)

                // Frein du clitoris (Frenulum clitoridis)
                val frenulum = Path().apply {
                    moveTo(glansCenter.x, glansCenter.y + glansRadius * 1.1f)
                    lineTo(glansCenter.x, py(0.37f))
                }
                drawPath(frenulum, line.copy(alpha = 0.55f), style = fineEdge)

                // 7. Méat urinaire (Ostium urethrae externum) : fente sagittale avec bourrelets
                val meatusY = py(0.44f)
                // Papille muqueuse péri-urétrale
                drawOval(
                    color = inner.copy(alpha = 0.40f),
                    topLeft = Offset(px(-0.022f), meatusY - h * 0.016f),
                    size = Size(bodyW * 0.044f, h * 0.032f),
                )
                drawOval(
                    color = line.copy(alpha = 0.40f),
                    topLeft = Offset(px(-0.022f), meatusY - h * 0.016f),
                    size = Size(bodyW * 0.044f, h * 0.032f),
                    style = fineEdge,
                )
                // Fente urétrale centrale
                drawLine(
                    color = line,
                    start = Offset(px(0f), meatusY - h * 0.011f),
                    end = Offset(px(0f), meatusY + h * 0.011f),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round,
                )

                // 8. Orifice du vagin (Introitus) & Caroncules hyménéales crénelées
                val introY = py(0.53f)
                val introH = h * 0.16f
                val introW = bodyW * 0.14f
                val introRect = androidx.compose.ui.geometry.Rect(cx - introW / 2f, introY, cx + introW / 2f, introY + introH)

                // Fond de l'orifice vaginal
                drawOval(
                    color = opening,
                    topLeft = Offset(introRect.left, introRect.top),
                    size = Size(introW, introH),
                )
                drawOval(
                    color = colors.onSurface.copy(alpha = 0.15f),
                    topLeft = Offset(introRect.left + introW * 0.15f, introRect.top + introH * 0.10f),
                    size = Size(introW * 0.70f, introH * 0.80f),
                )

                // Crêtes et caroncules hyménéales festonnées le long du pourtour
                val carunclesPath = Path().apply {
                    val count = 12
                    for (i in 0 until count) {
                        val angle = (i.toFloat() / count) * 2f * Math.PI.toFloat()
                        val rx = (introW / 2f) * (0.92f + if (i % 2 == 0) 0.10f else -0.06f)
                        val ry = (introH / 2f) * (0.92f + if (i % 2 == 0) 0.08f else -0.05f)
                        val x = cx + kotlin.math.cos(angle) * rx
                        val y = (introY + introH / 2f) + kotlin.math.sin(angle) * ry
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }
                drawPath(carunclesPath, inner.copy(alpha = 0.45f))
                drawPath(carunclesPath, line.copy(alpha = 0.85f), style = fineEdge)

                // Fossette naviculaire
                val fossa = Path().apply {
                    moveTo(px(-0.04f), introY + introH + py(0.005f))
                    cubicTo(px(-0.02f), introY + introH + py(0.015f), px(0.02f), introY + introH + py(0.015f), px(0.04f), introY + introH + py(0.005f))
                }
                drawPath(fossa, line.copy(alpha = 0.45f), style = fineEdge)

                // Fourchette vulvaire (Frenulum labiorum pudendi)
                val fourchette = Path().apply {
                    moveTo(px(-0.06f), py(0.74f))
                    cubicTo(px(-0.02f), py(0.755f), px(0.02f), py(0.755f), px(0.06f), py(0.74f))
                }
                drawPath(fourchette, line.copy(alpha = 0.65f), style = edge)

                // 9. Périnée : corps périnéal et raphé médian cutané
                val rapheY1 = py(0.765f)
                val rapheY2 = py(0.835f)
                drawLine(line.copy(alpha = 0.50f), Offset(px(0f), rapheY1), Offset(px(0f), rapheY2), strokeWidth = hair)
                // Ridules cutanées transversales légères
                drawLine(line.copy(alpha = 0.25f), Offset(px(-0.015f), py(0.785f)), Offset(px(0.015f), py(0.785f)), strokeWidth = hair)
                drawLine(line.copy(alpha = 0.25f), Offset(px(-0.018f), py(0.81f)), Offset(px(0.018f), py(0.81f)), strokeWidth = hair)

                // 10. Anus : orifice plissé avec plis radiés cutanés (plicae anales)
                val anusCenter = Offset(px(0f), py(0.89f))
                val anusRadius = bodyW * 0.036f
                // Marge anale pigmentée
                drawCircle(colors.tertiary.copy(alpha = 0.25f), radius = anusRadius * 1.5f, center = anusCenter)
                drawCircle(colors.primary.copy(alpha = 0.45f), radius = anusRadius, center = anusCenter)
                drawCircle(line.copy(alpha = 0.70f), radius = anusRadius, center = anusCenter, style = fineEdge)
                // Plis radiés de la marge anale (sillons radiés)
                val radialCount = 10
                for (r in 0 until radialCount) {
                    val angle = (r.toFloat() / radialCount) * 2f * Math.PI.toFloat()
                    val cosA = kotlin.math.cos(angle)
                    val sinA = kotlin.math.sin(angle)
                    val startR = anusRadius * 0.35f
                    val endR = anusRadius * 1.45f
                    drawLine(
                        color = line.copy(alpha = 0.45f),
                        start = Offset(anusCenter.x + cosA * startR, anusCenter.y + sinA * startR),
                        end = Offset(anusCenter.x + cosA * endR, anusCenter.y + sinA * endR),
                        strokeWidth = hair,
                        cap = StrokeCap.Round,
                    )
                }
                // Fente anale centrale
                drawLine(
                    color = line,
                    start = Offset(anusCenter.x, anusCenter.y - anusRadius * 0.4f),
                    end = Offset(anusCenter.x, anusCenter.y + anusRadius * 0.4f),
                    strokeWidth = 1.6.dp.toPx(),
                    cap = StrokeCap.Round,
                )

                // 11. Repères numérotés : trait fin de la pastille vers l'élément anatomique ciblé
                fun marker(number: Int, badge: Offset, target: Offset) {
                    drawLine(line.copy(alpha = 0.85f), badge, target, strokeWidth = 1.dp.toPx())
                    drawCircle(line, radius = 2.5.dp.toPx(), center = target)
                    drawCircle(colors.primary, radius = 10.dp.toPx(), center = badge)
                    drawBadgeNumber(measurer, number.toString(), badge, badgeStyle)
                }

                val left = w * 0.07f
                val right = w * 0.93f
                marker(1, Offset(left, py(0.10f)), Offset(px(-0.16f), py(0.12f)))
                marker(2, Offset(right, py(0.24f)), Offset(px(0.04f), py(0.27f)))
                marker(3, Offset(left, py(0.32f)), Offset(px(-0.02f), py(0.325f)))
                marker(4, Offset(right, py(0.44f)), Offset(px(0.28f), py(0.48f)))
                marker(5, Offset(left, py(0.52f)), Offset(px(-0.14f), py(0.54f)))
                marker(6, Offset(right, py(0.40f)), Offset(px(0.02f), py(0.44f)))
                marker(7, Offset(left, py(0.64f)), Offset(px(-0.05f), py(0.60f)))
                marker(8, Offset(right, py(0.76f)), Offset(px(0.02f), py(0.78f)))
                marker(9, Offset(left, py(0.89f)), Offset(px(-0.035f), py(0.89f)))
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
                fr = "Chaque corps est unique : il n'y a aucune vulve « modèle ». Que vos petites lèvres soient plus longues que les grandes, qu'elles soient ondulées, asymétriques, roses, beiges ou brun foncé, tout cela est parfaitement naturel et sain. Votre sexe ne doit ressembler à aucun standard.",
                en = "Every body is unique: there is no 'textbook' vulva. Whether your inner lips peek past the outer lips, whether they are wavy, asymmetrical, pink, brown, or purplish, it is completely natural and healthy. There is no single shape to conform to.",
                pl = "Każde ciało jest wyjątkowe: nie ma jednego «wzorcowego» sromu. Wargi mniejsze mogą wystawać, być asymetryczne, pofałdowane, jasne lub ciemniejsze – to w 100% zdrowe i normalne.",
                uk = "Кожне тіло неповторне: не існує «правильної» вульви. Малі губи можуть бути довшими за великі, хвилястими, асиметричними, світлими або темними — це абсолютно природно й здорово.",
                es = "Cada cuerpo es único: no existe una vulva «modelo». Que los labios menores sobresalgan, sean ondulados, asimétricos, rosados o más oscuros es completamente natural y sano.",
                de = "Jeder Körper ist einzigartig: Es gibt keine «Norm-Vulva». Ob die inneren Lippen länger sind, gewellt, asymmetrisch, hellrosa oder dunkel pigmentiert – alles ist vollkommen gesund und natürlich.",
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
