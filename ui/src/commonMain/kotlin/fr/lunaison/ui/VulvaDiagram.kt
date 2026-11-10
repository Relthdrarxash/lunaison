package fr.lunaison.ui

import kotlin.math.PI
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
            fr = "Le mont du pubis (zone protectrice)",
            en = "Mons pubis (protective cushion)",
            pl = "Wzgórek łonowy (strefa ochronna)",
            uk = "Лобковий горбок (захисна зона)",
            es = "Monte del pubis (zona protectora)",
            de = "Schamhügel (Schutzzone)",
        ),
        tr(
            fr = "Un dôme de tissu adipeux situé au-dessus des lèvres, sur l'os du pubis. Recouvert de pilosité à la puberté, il amortit les pressions et protège les structures osseuses et intimes lors des mouvements.",
            en = "A soft mound of adipose tissue over the pubic bone. Covered with hair after puberty, it cushions pressures and protects delicate structures during movement and physical activity.",
            pl = "Zaokrąglone wzniesienie z tkanki tłuszczowej nad kością łonową. Pokryte owłosieniem po okresie dojrzewania, amortyzuje nacisk i chroni kość łonową oraz narządy intymne podczas ruchu.",
            uk = "М'яке підвищення з жирової тканини над лобковою кісткою. Вкрите волоссям після дозрівання, воно амортизує тиск і захищає кісткові та інтимні структури під час рухів.",
            es = "Una elevación de tejido graso sobre el hueso púbico. Cubierta de vello tras la pubertad, amortigua la presión y protege las estructuras íntimas frente al roce y el impacto.",
            de = "Ein weiches Fettgewebspolster über dem Schambein. Nach der Pubertät behaart, dämpft es Druck ab und schützt das Schambein sowie die intimen Strukturen bei Bewegung.",
        ),
    ),
    VulvaLandmark(
        tr(
            fr = "Le capuchon du clitoris (repli protecteur)",
            en = "Clitoral hood (protective fold)",
            pl = "Kapturek łechtaczki (fałd ochronny)",
            uk = "Капюшон клітора (захисна складка)",
            es = "Capuchón del clítoris (pliegue protector)",
            de = "Klitorisvorhaut (Schutzfalte)",
        ),
        tr(
            fr = "Un repli de peau souple situé à la jonction supérieure des petites lèvres. Il abrite la partie externe du clitoris pour la protéger des frottements des vêtements, et se rétracte légèrement lors de la stimulation.",
            en = "A soft fold of skin at the upper junction of the labia minora. It shelters the visible glans of the clitoris from garment friction, retracting gently during stimulation.",
            pl = "Delikatny fałd skóry w miejscu połączenia warg mniejszych. Osłania zewnętrzną część łechtaczki przed otarciami odzieży i cofa się podczas stymulacji.",
            uk = "М'яка складка шкіри на стику малих статевих губ. Вона прикриває зовнішню частину клітора від тертя одягу та злегка зміщується при стимуляції.",
            es = "Un pliegue suave de piel situado en la unión superior de los labios menores. Protege el glande del clítoris del roce de la ropa y se desplaza suavemente durante la estimulación.",
            de = "Eine weiche Hautfalte am oberen Ende der kleinen Schamlippen. Sie schützt die Klitoriseichel vor Reibung durch Kleidung und weicht bei Stimulation sanft zurück.",
        ),
    ),
    VulvaLandmark(
        tr(
            fr = "Le clitoris (gland visible)",
            en = "Clitoral glans (visible part)",
            pl = "Żołądź łechtaczki (część widoczna)",
            uk = "Голівка клітора (видима частина)",
            es = "Glande del clítoris (parte visible)",
            de = "Klitoriseichel (sichtbarer Teil)",
        ),
        tr(
            fr = "La partie visible et la plus sensible du clitoris, située au sommet de la vulve. C'est un organe dédié au plaisir, comptant plus de 8 000 à 10 000 fibres nerveuses. En profondeur, le clitoris s'étend sur 8 à 10 cm avec deux corps érectiles qui entourent le vagin.",
            en = "The visible and most sensitive part of the clitoris, located at the top of the vulva. It is an organ dedicated to pleasure, with over 8,000 to 10,000 nerve fibers. Internally, the clitoris extends 8 to 10 cm with erectile bodies embracing the vagina.",
            pl = "Widoczna i najwrażliwsza część łechtaczki na szczycie sromu. Jest narządem służącym przyjemności, z ponad 8 000 zakończeń nerwowych. Wewnątrz ciała łechtaczka mierzy 8-10 cm i obejmuje pochwę ramionami jamistymi.",
            uk = "Видима й найчутливіша частина клітора на вершині вульви. Це орган, призначений для відчуття насолоди, що містить понад 8 000 нервових закінчень. У глибині клітор сягає 8–10 см і охоплює піхву еректильними ніжками.",
            es = "La porción visible y más sensible del clítoris, en la cúspide de la vulva. Es un organe dedicado al placer, con más de 8.000 a 10.000 fibras nerviosas. Hacia el interior, el clítoris se extiende de 8 a 10 cm rodeando la vagina con sus cuerpos eréctiles.",
            de = "Der sichtbare und empfindlichste Teil der Klitoris an der Spitze der Vulva. Ein einzig der Lust dienendes Organ mit über 8.000 bis 10.000 Nervenfasern. Nach innen reicht die Klitoris 8 bis 10 cm tief und umfasst die Vagina mit Schwellkörpern.",
        ),
    ),
    VulvaLandmark(
        tr(
            fr = "Les grandes lèvres (lèvres externes)",
            en = "Labia majora (outer lips)",
            pl = "Wargi sromowe większe (wargi zewnętrzne)",
            uk = "Великі статеві губи (зовнішні губи)",
            es = "Labios mayores (labios externos)",
            de = "Große Schamlippen (äußere Schamlippen)",
        ),
        tr(
            fr = "Deux replis cutanés pourvus de tissu adipeux qui encadrent et protègent les structures vulvaires internes. Contrairement à une idée reçue, elles ne recouvrent pas systématiquement les petites lèvres : chez de nombreuses femmes, les petites lèvres dépassent naturellement vers l'extérieur.",
            en = "Two soft folds of skin and adipose tissue that frame and protect the inner vulvar structures. Contrary to common myths, they do not always cover the inner lips: in many healthy bodies, the labia minora naturally extend beyond them.",
            pl = "Dwa fałdy skóry z podściółką tłuszczową, które otaczają i chronią wewnętrzne struktury sromu. Nie muszą zakrywać warg mniejszych: u wielu kobiet wargi mniejsze naturalnie wystają na zewnątrz.",
            uk = "Дві шкірні складки з жировою тканиною, які обрамляють і захищають внутрішні структури вульви. Вони зовсім не обов'язково перекривають малі губи: у багатьох жінок малі губи природно виступають назовні.",
            es = "Dos pliegues cutáneos con tejido graso que enmarcan y protegen las estructuras vulvares internas. No siempre cubren por completo los labios menores: en muchos cuerpos, los menores sobresalen de forma natural.",
            de = "Zwei Hautfalten mit Fettgewebe, die die inneren Vulvastrukturen umrahmen und schützen. Sie müssen die kleinen Schamlippen keineswegs verdecken: Bei vielen Frauen ragen die inneren Lippen natürlich hervor.",
        ),
    ),
    VulvaLandmark(
        tr(
            fr = "Les petites lèvres (lèvres internes)",
            en = "Labia minora (inner lips)",
            pl = "Wargi sromowe mniejsze (wargi wewnętrzne)",
            uk = "Малі статеві губи (внутрішні губи)",
            es = "Labios menores (labios internos)",
            de = "Kleine Schamlippen (innere Schamlippen)",
        ),
        tr(
            fr = "Deux replis souples et richement vascularisés situés à l'intérieur des grandes lèvres. Elles entourent et protègent le méat urinaire et l'entrée du vagin. Leur taille, longueur, symétrie et pigmentation varient grandement d'une personne à l'autre : toutes ces variations morphologiques sont parfaitement saines.",
            en = "Two delicate, vascular mucosal folds located inside the outer lips. They surround and protect the urethral and vaginal openings. Their size, shape, symmetry, and color vary widely between individuals, and all variations are completely normal.",
            pl = "Dwa delikatne, bogato unaczynione fałdy śluzówkowe wewnątrz warg większych. Chronią cewkę moczową i wejście do pochwy. Ich długość, symetria i barwa różnią się u każdej osoby – każda odmiana jest w pełni prawidłowa.",
            uk = "Дві ніжні, добре васкуляризовані складки всередині великих губ. Вони захищають отвір сечівника та вхід до піхви. Його довжина, форма, симетрія й колір дуже індивідуальні й завжди цілком природні.",
            es = "Dos pliegues mucosos vascularizados situados dentro de los labios mayores. Protegen el meato urinario y la entrada vaginal. Su forma, simetría, longitud y color varían ampliamente de un cuerpo a otro, siendo todas variaciones normales.",
            de = "Zwei gefäßreiche, weiche Schleimhautfalten innerhalb der großen Schamlippen. Sie schützen Harnröhrenöffnung und Scheideneingang. Länge, Form, Symmetrie und Färbung sind individuell verschieden und völlig normal.",
        ),
    ),
    VulvaLandmark(
        tr(
            fr = "Le méat urinaire (orifice urinaire)",
            en = "Urethral opening (urinary orifice)",
            pl = "Ujście cewki moczowej (odpływ moczu)",
            uk = "Зовнішній отвір сечівника (вихід сечі)",
            es = "Meato urinario (orificio urinario)",
            de = "Harnröhrenöffnung (Harnausgang)",
        ),
        tr(
            fr = "Le petit orifice par lequel s'évacue l'urine depuis la vessie. Il est situé entre le clitoris et l'entrée du vagin. Règle anatomique essentielle : l'urine ne s'évacue pas par le vagin, il s'agit de deux canaux entièrement distincts.",
            en = "The small orifice through which urine is voided from the bladder, located between the clitoris and the vaginal entrance. Key anatomical fact: urine does not exit through the vagina; they are two entirely separate passages.",
            pl = "Niewielki otwór, przez który odprowadzany jest mocz z pęcherza, położony między łechtaczką a wejściem do pochwy. Mocz nie wypływa z pochwy – to dwa całkowicie odrębne przewody.",
            uk = "Невеликий отвір, крізь який виводиться сеча з сечового міхура, розташований між клітором і входом до піхви. Сеча не виділяється з піхви — це два цілком окремі анатомічні шляхи.",
            es = "El pequeño orificio por el cual se evacua la orina desde la vejiga, situado entre el clítoris y la entrada vaginal. Dato anatómico clave: la orina no sale por la vagina, son dos vías totalmente distintas.",
            de = "Die feine Öffnung, über die Urin aus der Blase abfließt, gelegen zwischen Klitoris und Scheideneingang. Anatomischer Grundsatz: Urin fließt nicht aus der Scheide – es sind zwei eigenständige Kanäle.",
        ),
    ),
    VulvaLandmark(
        tr(
            fr = "L'orifice du vagin (entrée vaginale)",
            en = "Vaginal opening (vaginal entrance)",
            pl = "Wejście do pochwy (ujście pochwy)",
            uk = "Вхід до піхви (вагінальний отвір)",
            es = "Orificio vaginal (entrada vaginal)",
            de = "Scheideneingang (Introitus)",
        ),
        tr(
            fr = "L'ouverture menant au canal vaginal et à l'utérus. C'est par cet orifice que s'écoule le flux des règles, qu'ont lieu l'accouchement et l'insertion de protections périodiques (tampons, coupes menstruelles) ou les rapports sexuels. Ses tissus sont très souples et extensibles.",
            en = "The entrance leading into the vaginal canal and uterus. Through it, menstrual fluid flows out, childbirth occurs, and menstrual products (tampons, cups) or intimacy can be accommodated. Its tissues are flexible and naturally elastic.",
            pl = "Otwór prowadzący do kanału pochwy i macicy. Tędy wypływa krew miesiączkowa, przebiega poród oraz wprowadza się środki higieniczne (tampony, kubeczki). Tkanki te cechują się dużą elastycznością.",
            uk = "Отвір, що веде до піхвового каналу та матки. Крізь нього витікає менструальна кров, відбувається народження дитини та введення гігієнічних засобів. Його тканини від природи еластичні.",
            es = "La abertura que da acceso al canal vaginal y al útero. Por aquí fluye la menstruación, tiene lugar el parto y se insertan productos higiénicos (tampones, copas) o se mantiene intimidad. Posee tejidos muy elásticos.",
            de = "Der Zugang zum Scheidenkanal und zur Gebärmutter. Hier fließt die Menstruationsblutung ab, findet die Geburt statt und können Menstruationsprodukte eingeführt werden. Das Gewebe ist bemerkenswert dehnbar.",
        ),
    ),
    VulvaLandmark(
        tr(
            fr = "Le périnée (zone de soutien pelvien)",
            en = "Perineum (pelvic support zone)",
            pl = "Krocze (strefa dna miednicy)",
            uk = "Промежина (зона тазового дна)",
            es = "Periné (zona de soporte pélvico)",
            de = "Damm (Beckenbodenbereich)",
        ),
        tr(
            fr = "La région située entre l'orifice vaginal et l'anus. En profondeur, elle correspond au plancher pelvien, un carrefour musculaire essentiel qui soutient la vessie, l'utérus et le rectum, et participe à la continence.",
            en = "The anatomical area between the vaginal opening and the anus. Beneath the surface, it forms part of the pelvic floor musculature, supporting the bladder, uterus, and rectum, and helping maintain continence.",
            pl = "Obszar między wejściem do pochwy a odbytem. W głębi łączy się z mięśniami dna miednicy, które podtrzymują pęcherz moczowy, macicę i odbytnicę oraz wspomagają trzymanie moczu.",
            uk = "Ділянка між входом до піхви та анусом. У глибині вона пов'язана з м'язами тазового дна, що підтримують сечовий міхур, матку й пряму кишку та відповідають за утримання сечі.",
            es = "La zona anatómica entre la entrada vaginal y el ano. En profundidad se articula con el suelo pélvico, una estructura muscular fundamental que sostiene la vejiga, el útero y el recto.",
            de = "Der Bereich zwischen Scheideneingang und After. In der Tiefe bildet er den Beckenboden, ein wichtiges Muskelnetz, das Blase, Gebärmutter und Mastdarm stützt und die Kontinenz sichert.",
        ),
    ),
    VulvaLandmark(
        tr(
            fr = "L'anus (orifice digestif)",
            en = "Anus (digestive orifice)",
            pl = "Odbyt (ujście pokarmowe)",
            uk = "Анус (травний отвір)",
            es = "Ano (orificio digestivo)",
            de = "After (Darmausgang)",
        ),
        tr(
            fr = "L'orifice terminal du tube digestif, contrôlé par un sphincter musculaire. Il est anatomiquement distinct de l'appareil reproducteur. Règle d'hygiène préventive : s'essuyer d'avant en arrière pour éviter le transfert de bactéries intestinales vers le vagin ou les voies urinaires.",
            en = "The terminal opening of the gastrointestinal tract, controlled by an anal sphincter. It is entirely separate from the reproductive tract. Preventive hygiene rule: always wipe front to back to prevent intestinal bacteria from reaching the vagina or urinary tract.",
            pl = "Końcowy odcinek przewodu pokarmowego, sterowany zwieraczem mięśniowym. Anatomicznie oddzielony od narządów rodnych. Zasada higieny: podcieranie zawsze w kierunku od przodu do tyłu.",
            uk = "Кінцевий отвір травної системи, контрольований сфінктером. Він анатомічно відокремлений від репродуктивної системи. Правило гігієни: витиратися завжди спереду назад.",
            es = "El orificio terminal del tubo digestivo, regulado por un esfínter muscular. Es independiente del aparato reproductor. Regla preventiva: limpiarse siempre de delante hacia atrás para evitar transferir bacterias.",
            de = "Die Endöffnung des Magen-Darm-Trakts, reguliert durch einen Schließmuskel. Völlig getrennt vom Fortpflanzungstrakt. Grundregel der Hygiene: Immer von vorn nach hinten wischen.",
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
                        val angle = (i.toFloat() / count) * 2f * PI.toFloat()
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
                    val angle = (r.toFloat() / radialCount) * 2f * PI.toFloat()
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
