package fr.lunaison.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import fr.lunaison.core.content.Article
import fr.lunaison.core.content.Articles
import fr.lunaison.core.designsystem.LunaCard
import fr.lunaison.core.designsystem.SectionTitle
import fr.lunaison.core.model.tr

@Composable
fun LearnScreen(onArticle: (String) -> Unit) {
    Page {
        PageTitle(
            tr("Comprendre", "Learn"),
            subtitle = tr("Des fiches courtes, relues et sourcées pour mieux vivre son cycle.", "Short, reviewed and referenced guides to better understand your cycle."),
        )

        // Encart pédagogique mis en avant pour débutantes / ados et pour découvrir son anatomie
        LunaCard(
            modifier = Modifier.clickable(role = Role.Button) { onArticle("cycle") },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        tr("Découvrir son anatomie et son cycle", "Discovering anatomy and the cycle"),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        tr("Repères illustrés, organes et vagues hormonales expliqués simplement.", "Illustrated guide, organs and hormonal waves simply explained."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Catégorie 1 : Le cycle et les hormones
        SectionTitle(tr("Le cycle et les hormones", "The cycle and hormones"))
        LunaCard(modifier = Modifier.clickable(role = Role.Button) { onArticle("contraception") }) {
            Text(tr("Contraception & règles : ce qui change", "Contraception & periods: what changes"), style = MaterialTheme.typography.titleMedium)
            Text(
                tr("Pilule, stérilet au cuivre ou hormonal, implant : pourquoi les saignements sont différents et comment ils fonctionnent.", "Pill, copper or hormonal IUD, implant: why bleeding differs and how it works."),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        val cycleArticles = Articles.all.filter { it.id in listOf("cycle", "previsions") }
        cycleArticles.forEach { article ->
            ArticleCard(article, onArticle)
        }
        LunaCard(modifier = Modifier.clickable(role = Role.Button) { onArticle("sopk") }) {
            Text(tr("Le SOPK (ovaires polykystiques)", "PCOS (polycystic ovary syndrome)"), style = MaterialTheme.typography.titleMedium)
            Text(
                tr(
                    "Critères de Rotterdam, ovulations rares, hyperandrogénie et repères au quotidien.",
                    "Rotterdam criteria, infrequent ovulation, hyperandrogenism and everyday management.",
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LunaCard(modifier = Modifier.clickable(role = Role.Button) { onArticle("perimenopause") }) {
            Text(tr("La périménopause et ses changements", "Perimenopause and its changes"), style = MaterialTheme.typography.titleMedium)
            Text(
                tr(
                    "Cycles irréguliers, bouffées de chaleur, sommeil et transition hormonale expliqués.",
                    "Irregular cycles, hot flashes, sleep and hormonal transition explained.",
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Catégorie 2 : Soulagement et bien-être
        SectionTitle(tr("Soulagement et bien-être", "Relief and well-being"))
        LunaCard(modifier = Modifier.clickable(role = Role.Button) { onArticle("endometriose") }) {
            Text(tr("Endométriose & douleurs pelviennes", "Endometriosis & pelvic pain"), style = MaterialTheme.typography.titleMedium)
            Text(
                tr(
                    "Comprendre les signaux d'alerte (les 5 « D »), le diagnostic et les pistes de soulagement au quotidien.",
                    "Understand warning signs (the 5 'Ds'), diagnosis and everyday relief options.",
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LunaCard(modifier = Modifier.clickable(role = Role.Button) { onArticle("adenomyose") }) {
            Text(tr("L'adénomyose : repères et saignements", "Adenomyosis: bleeding and pain"), style = MaterialTheme.typography.titleMedium)
            Text(
                tr(
                    "L'endométriose interne du muscle utérin, règles très abondantes, crampes et prise en charge.",
                    "Internal endometriosis in the uterine wall, heavy bleeding, cramps and management.",
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        val healthArticles = Articles.all.filter { it.id in listOf("douleurs", "spm", "alimentation") }
        healthArticles.forEach { article ->
            ArticleCard(article, onArticle)
        }

        // Catégorie 3 : Santé, quotidien et entourage
        SectionTitle(tr("Santé, quotidien et entourage", "Health, daily life and loved ones"))
        val lifeArticles = Articles.all.filter { it.id in listOf("consulter", "protections", "entourage") }
        lifeArticles.forEach { article ->
            ArticleCard(article, onArticle)
        }

        Muted(tr("Ces fiches informent et ne remplacent pas l'avis d'un professionnel de santé.", "These guides inform you and do not replace the advice of a health professional."))
    }
}

@Composable
private fun ArticleCard(article: Article, onArticle: (String) -> Unit) {
    LunaCard(modifier = Modifier.clickable(role = Role.Button) { onArticle(article.id) }) {
        Text(article.title, style = MaterialTheme.typography.titleMedium)
        Text(article.summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ArticleScreen(id: String, onBack: () -> Unit) {
    if (id == "contraception") {
        ContraceptionGuideScreen(onBack = onBack)
        return
    }
    if (id == "endometriose") {
        EndometriosisGuideScreen(onBack = onBack)
        return
    }
    if (id == "sopk") {
        PcosGuideScreen(onBack = onBack)
        return
    }
    if (id == "adenomyose") {
        AdenomyosisGuideScreen(onBack = onBack)
        return
    }
    if (id == "perimenopause") {
        PerimenopauseGuideScreen(onBack = onBack)
        return
    }
    val article = Articles.byId(id)
    val context = LocalContext.current
    Page {
        if (article == null) {
            PageTitle(tr("Fiche introuvable", "Guide not found"), onBack = onBack)
            return@Page
        }
        PageTitle(article.title, onBack = onBack)
        Text(article.summary, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

        // Schémas visuels pédagogiques intégrés pour la fiche cycle
        if (id == "cycle") {
            CycleVisualGuide()
        }

        article.sections.forEach { section ->
            SectionTitle(section.heading)
            section.paragraphs.forEach { Text(it, style = MaterialTheme.typography.bodyLarge) }
        }
        // Les références ne sont pas dans le texte : un lien discret mène à la page du site qui les détaille.
        if (article.sources.isNotEmpty()) {
            TextButton(onClick = { openLink(context, Links.sourcesOf(article.id)) }) {
                Text(
                    tr("Sources de cette fiche, sur le site", "Sources for this guide, on the website (in French)"),
                    style = MaterialTheme.typography.bodySmall,
                    textDecoration = TextDecoration.Underline,
                )
            }
        }
    }
}

/** Types de contraception avec leur logique physiologique. */
enum class ContraceptiveMethod(private val fr: String, private val en: String) {
    COMBINED_PILL("Pilule combinée", "Combined pill"),
    COPPER_IUD("Stérilet cuivre", "Copper IUD"),
    IMPLANT_OR_PROGESTIN("Implant / Pilule continue", "Implant / Mini-pill"),
    HORMONAL_IUD("Stérilet hormonal", "Hormonal IUD"),
    NATURAL("Cycle naturel / Préservatif", "Natural cycle / Condoms"),
    ;

    val label: String get() = tr(fr, en)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ContraceptionGuideScreen(onBack: () -> Unit) {
    var selectedMethod by rememberSaveable { mutableStateOf(ContraceptiveMethod.COMBINED_PILL) }
    val context = LocalContext.current

    Page {
        PageTitle(
            tr("Contraception & règles", "Contraception & periods"),
            onBack = onBack,
            subtitle = tr("Comprendre vos saignements selon votre méthode.", "Understand your bleeding depending on your method."),
        )

        LunaCard {
            Text(
                tr("Sélectionnez votre moyen de contraception pour voir son fonctionnement :", "Select your contraceptive method to see how it works:"),
                style = MaterialTheme.typography.titleSmall,
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ContraceptiveMethod.entries.forEach { method ->
                    val isSelected = method == selectedMethod
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedMethod = method },
                        label = {
                            Text(
                                method.label,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(vertical = 10.dp),
                            )
                        },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                }
            }
        }

        // Fiche détaillée mise en valeur pour la méthode sélectionnée
        SectionTitle(tr("Votre méthode : ${selectedMethod.label}", "Your method: ${selectedMethod.label}"))
        when (selectedMethod) {
            ContraceptiveMethod.COMBINED_PILL -> CombinedPillDetailsCard()
            ContraceptiveMethod.COPPER_IUD -> CopperIudDetailsCard()
            ContraceptiveMethod.IMPLANT_OR_PROGESTIN -> ImplantDetailsCard()
            ContraceptiveMethod.HORMONAL_IUD -> HormonalIudDetailsCard()
            ContraceptiveMethod.NATURAL -> NaturalDetailsCard()
        }

        SectionTitle(tr("Toutes les méthodes en résumé", "All methods overview"))
        ContraceptionComparisonCard()

        TextButton(onClick = {
            openLink(
                context,
                tr(
                    fr = "https://www.questionsexualite.fr/choisir-sa-contraception",
                    en = "https://www.nhs.uk/contraception/",
                    pl = "https://ponton.org.pl/antykoncepcja/",
                    uk = "https://www.nhs.uk/contraception/",
                    es = "https://sedra-fpfe.org/",
                    de = "https://www.familienplanung.de/verhuetung/",
                ),
            )
        }) {
            Text(
                tr(
                    fr = "Guides officiels de Santé publique France (QuestionSexualité.fr)",
                    en = "Official NHS Contraception guide (nhs.uk)",
                    pl = "Grupa Ponton : Edukacja seksualna i antykoncepcja (ponton.org.pl)",
                    uk = "Керівництво з контрацепції NHS (nhs.uk)",
                    es = "Guía de anticoncepción SEDRA - Planificación Familiar (sedra-fpfe.org)",
                    de = "Offizielle Verhütungsinformationen der BZgA (familienplanung.de)",
                ),
                style = MaterialTheme.typography.bodySmall,
                textDecoration = TextDecoration.Underline,
            )
        }
    }
}

@Composable
private fun CombinedPillDetailsCard() {
    LunaCard {
        Text(
            tr("Ce que sont vos saignements : des hémorragies de privation", "What your bleeding is: withdrawal bleeding"),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            tr(
                "Sous pilule combinée (œstrogène + progestatif), vous n'avez pas de véritables règles. La pilule bloque l'ovulation. Les saignements lors de la semaine de pause (ou des comprimés placebo) sont déclenchés artificiellement par l'arrêt temporaire des hormones de synthèse.",
                "Under a combined pill, you do not have genuine menstrual periods. The pill stops ovulation. Bleeding during the 7-day pill-free break is artificially triggered by the temporary drop in hormones.",
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            tr("Enchaîner sans pause :", "Taking packs back-to-back:"),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            tr(
                "Enchaîner deux plaquettes sans pause de 7 jours supprime tout saignement. C'est médicalement sans danger et validé par les sociétés savantes (OMS, HAS) : le sang ne s'accumule nulle part.",
                "Taking active pill packs consecutively without a break suppresses bleeding entirely. It is safe according to WHO/HAS medical guidelines: blood does not build up anywhere.",
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            tr("Dans Lunaison :", "In Lunaison:"),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            tr(
                "Vous pouvez noter vos prises quotidiennes et vos saignements. Les prévisions de cycle naturel (ovulation estimée) n'ont pas de valeur sous pilule combinée puisque c'est le calendrier de la plaquette qui fait foi.",
                "You can log daily intake and bleeding. Natural cycle forecasts (estimated ovulation) are not biologically applicable under the combined pill as the pack schedule dictates bleeding.",
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun CopperIudDetailsCard() {
    LunaCard {
        Text(
            tr("Ce que sont vos saignements : de vraies règles naturelles", "What your bleeding is: genuine natural periods"),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            tr(
                "Le stérilet au cuivre (DIU cuivre) est 100 % sans hormone. Votre cycle biologique naturel, vos variations hormonales et votre ovulation se poursuivent exactement comme d'habitude.",
                "The copper IUD is 100% hormone-free. Your natural cycle, hormonal variations and ovulation proceed normally.",
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            tr("L'inflammation bénigne et les flux abondants :", "Benign inflammation and heavy bleeding:"),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            tr(
                "Le cuivre diffuse de petites quantités d'ions cuivre qui provoquent une réaction inflammatoire locale bénigne de l'endomètre (spermicide). C'est pour cela que les règles sont souvent plus abondantes, plus longues et plus douloureuses les 3 à 6 premiers mois.",
                "Copper releases ions causing a localized, benign inflammatory response in the endometrium (spermicidal). This is why periods are frequently heavier, longer and more crampy during the first 3 to 6 months.",
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            tr("Vigilance fer et anémie :", "Iron monitoring and fatigue:"),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            tr(
                "Des règles très abondantes entraînent une perte de fer. Privilégiez les aliments riches en fer et parlez-en à un soignant pour un bilan sanguin (ferritine) si vous ressentez une grande fatigue ou des vertiges.",
                "Heavier periods increase iron loss. Favor iron-rich foods and ask your clinician for a ferritin blood test if you experience unusual fatigue or dizziness.",
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ImplantDetailsCard() {
    LunaCard {
        Text(
            tr("Ce que sont vos saignements : un endomètre au repos complet", "What your bleeding is: resting endometrium"),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            tr(
                "L'implant et la pilule progestative continue (ex. désogestrel) diffusent un progestatif en continu, sans aucune interruption. L'ovulation est le plus souvent bloquée et la muqueuse utérine reste très fine.",
                "The implant and progestin-only pill continuously release progestin without breaks. Ovulation is mostly suppressed and the uterine lining remains very thin.",
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            tr("Disparition des règles ou saignements irréguliers :", "No periods or irregular spotting:"),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            tr(
                "Environ une femme sur trois n'a plus du tout de saignements (aménorrhée). C'est totalement normal et sans danger pour la fertilité future. D'autres personnes connaissent de petits saignements légers et anarchiques (spotting).",
                "About one in three women stops having periods entirely. This is completely normal and safe for future fertility. Others experience light, irregular spotting.",
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            tr("Dans Lunaison :", "In Lunaison:"),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            tr(
                "Le mode « Mettre les prévisions en pause » dans les réglages est idéal si vos règles ont disparu, pour continuer à noter vos symptômes sans alertes de retard inutiles.",
                "The \"Pause predictions\" mode in settings is ideal if your periods have stopped, allowing you to log symptoms without unneeded late period alerts.",
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun HormonalIudDetailsCard() {
    LunaCard {
        Text(
            tr("Ce que sont vos saignements : des flux fortement diminués", "What your bleeding is: significantly reduced bleeding"),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            tr(
                "Le stérilet hormonal (DIU au lévonorgestrel) agit localement dans l'utérus. L'endomètre ne s'épaissit quasiment plus chaque mois. Les règles deviennent très légères, courtes, et disparaissent souvent complètement après 6 à 12 mois.",
                "The hormonal IUD acts locally inside the uterus. The endometrium barely thickens. Periods become very light, short, and frequently disappear entirely after 6 to 12 months.",
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            tr("Les premiers mois :", "The first few months:"),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            tr(
                "Des petits saignements de repérage (spotting) fréquents sont courants au cours des premières semaines, le temps que la muqueuse s'adapte à l'action locale du progestatif.",
                "Frequent light spotting is common in the first weeks while the uterine lining adjusts to the localized progestin.",
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun NaturalDetailsCard() {
    LunaCard {
        Text(
            tr("Ce que sont vos saignements : un cycle naturel et spontané", "What your bleeding is: a spontaneous natural cycle"),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            tr(
                "Sans contraception hormonale ni DIU (préservatifs, méthode barrière, symptothermie ou pas de contraception), votre cycle est gouverné par vos propres hormones. Les règles surviennent environ 14 jours après une ovulation naturelle.",
                "Without hormonal contraception or an IUD (condoms, barrier methods, symptothermy, or no contraception), your cycle is governed by your own hormones. Periods arrive ~14 days after natural ovulation.",
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            tr("Dans Lunaison :", "In Lunaison:"),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            tr(
                "Les calculs de durée habituelle, la fenêtre fertile estimée, les analyses de phases et le suivi de la glaire cervicale ou de la température s'appliquent pleinement à votre profil.",
                "Usual duration calculations, estimated fertile windows, phase patterns and mucus/temperature tracking fully apply to your biological profile.",
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ContraceptionComparisonCard() {
    LunaCard {
        ComparisonRow(
            title = tr("Pilule combinée", "Combined pill"),
            summary = tr("Hémorragie de privation (pas de vraie règle). Pas d'ovulation. Possibilité d'enchaîner.", "Withdrawal bleeding (not real period). No ovulation. Can take back-to-back."),
        )
        ComparisonRow(
            title = tr("Stérilet cuivre", "Copper IUD"),
            summary = tr("Vraies règles naturelles. Règles souvent plus abondantes et crampes au début (inflammation locale bénigne). Surveiller le fer.", "Natural periods. Frequently heavier flow & cramps initially (benign local inflammation). Watch iron."),
        )
        ComparisonRow(
            title = tr("Implant / Pilule continue", "Implant / Mini-pill"),
            summary = tr("Endomètre au repos. Règles souvent absentes (aménorrhée sans danger) ou spotting irrégulier.", "Resting endometrium. Often no periods (safe amenorrhea) or irregular spotting."),
        )
        ComparisonRow(
            title = tr("Stérilet hormonal", "Hormonal IUD"),
            summary = tr("Action locale. Diminution drastique des règles, disparition fréquente au bout d'un an.", "Local action. Drastic flow reduction, often complete cessation after one year."),
        )
    }
}

@Composable
private fun ComparisonRow(title: String, summary: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Text(summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CycleVisualGuide() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionTitle(tr("Schéma des 4 temps du cycle", "The 4 cycle phases diagram"))
        LunaCard {
            Text(
                tr("Le cycle menstruel en 4 étapes clés :", "The menstrual cycle in 4 key stages:"),
                style = MaterialTheme.typography.titleSmall,
            )

            // Frise visuelle horizontale avec 4 segments proportionnels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .height(16.dp),
            ) {
                // Règles J1-J5
                Box(
                    modifier = Modifier
                        .weight(5f)
                        .fillMaxHeight()
                        .background(Color(0xFFE8998D)),
                )
                // Folliculaire J6-J13
                Box(
                    modifier = Modifier
                        .weight(8f)
                        .fillMaxHeight()
                        .background(Color(0xFFB4D6C4)),
                )
                // Ovulation J14
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight()
                        .background(Color(0xFFF9D162)),
                )
                // Lutéale J15-J28
                Box(
                    modifier = Modifier
                        .weight(13f)
                        .fillMaxHeight()
                        .background(Color(0xFFD3BBD9)),
                )
            }

            PhaseExplainerRow(
                color = Color(0xFFE8998D),
                phase = tr("1. Les règles (J1 à J5)", "1. Period (Days 1 to 5)"),
                detail = tr("La muqueuse intérieure de l'utérus (l'endomètre) se détache et s'élimine. Les taux d'hormones sont au plus bas.", "The inner uterine lining (endometrium) sheds. Hormone levels are at their lowest."),
            )
            PhaseExplainerRow(
                color = Color(0xFFB4D6C4),
                phase = tr("2. Phase folliculaire (J6 à J13)", "2. Follicular phase (Days 6 to 13)"),
                detail = tr("Un follicule mûrit dans l'ovaire. Les œstrogènes montent pour reconstruire la muqueuse et préparer une éventuelle fécondation.", "An ovarian follicle matures. Estrogens rise to rebuild the uterine lining."),
            )
            PhaseExplainerRow(
                color = Color(0xFFF9D162),
                phase = tr("3. L'ovulation (~ J14)", "3. Ovulation (~ Day 14)"),
                detail = tr("L'ovaire libère l'ovule mûr. Il ne survit qu'entre 12 et 24 heures.", "The ovary releases a mature egg. It survives for 12 to 24 hours."),
            )
            PhaseExplainerRow(
                color = Color(0xFFD3BBD9),
                phase = tr("4. Phase lutéale (J15 à J28)", "4. Luteal phase (Days 15 to 28)"),
                detail = tr("La progestérone domine pour maintenir la muqueuse. Sans grossesse, elle s'effondre et déclenche les règles suivantes.", "Progesterone dominates to maintain the lining. Without pregnancy, it drops, triggering the next period."),
            )
        }

        HormoneCurvesDiagram()

        InteractiveUterusDiagram()

        SectionTitle(tr("L'anatomie interne sans tabou", "Internal anatomy explained"))
        LunaCard {
            Text(
                tr("Les repères clés du corps féminin :", "Key body reference points:"),
                style = MaterialTheme.typography.titleSmall,
            )
            AnatomyItemRow(
                title = tr("La vulve (externe)", "The vulva (external)"),
                description = tr("Tous les organes visibles à l'extérieur : lèvres, clitoris, méat urinaire et entrée du vagin. Souvent confondue avec le vagin !", "All visible external parts: labia, clitoris, urethra and vaginal opening. Often confused with the vagina!"),
            )
            AnatomyItemRow(
                title = tr("Le vagin (interne)", "The vagina (internal)"),
                description = tr("Le conduit musculaire souple reliant la vulve au col de l'utérus. C'est par lui que s'écoulent les règles.", "The flexible muscular canal connecting the vulva to the cervix. It is where menstrual blood flows."),
            )
            AnatomyItemRow(
                title = tr("Le col de l'utérus (cervix)", "The cervix"),
                description = tr("La partie basse de l'utérus. Il produit la glaire cervicale qui change au fil du mois et s'entrouvre légèrement pendant les règles.", "The lower part of the uterus. It secretes cervical mucus that changes texture throughout the month and opens slightly during periods."),
            )
            AnatomyItemRow(
                title = tr("L'utérus et le myomètre (muscle)", "The uterus & myometrium (muscle)"),
                description = tr("Un muscle creux puissant de la taille d'une petite poire. Sa paroi musculaire (le myomètre) se contracte lors des règles sous l'effet des prostaglandines.", "A powerful hollow pear-sized muscle. Its muscular wall (myometrium) contracts during periods under the effect of prostaglandins."),
            )
            AnatomyItemRow(
                title = tr("L'endomètre (muqueuse interne)", "The endometrium (inner lining)"),
                description = tr("La muqueuse vivante qui tapisse l'intérieur de l'utérus. Elle s'épaissit chaque mois, s'enrichit en vaisseaux, puis desquame si aucune grossesse ne débute : ce sont les règles.", "The living tissue lining the inside of the uterus. It thickens each month, vascularizes, then sheds if no pregnancy occurs: this is menstruation."),
            )
            AnatomyItemRow(
                title = tr("Les trompes de Fallope", "The fallopian tubes"),
                description = tr("Deux conduits fins reliant les ovaires à l'utérus, où a lieu la fécondation éventuelle d'un ovocyte.", "Two slender ducts connecting ovaries to uterus, where potential egg fertilization takes place."),
            )
            AnatomyItemRow(
                title = tr("Les ovaires (maturation & hormones)", "The ovaries (maturation & hormones)"),
                description = tr("Deux glandes qui abritent les ovocytes dès la naissance et fabriquent les œstrogènes et la progestérone au fil du cycle.", "Two glands containing egg cells from birth and producing estrogen and progesterone throughout the cycle."),
            )
        }

        SectionTitle(tr("Signes visibles : la glaire cervicale", "Visible signs: cervical mucus"))
        LunaCard {
            Text(
                tr("Comment la glaire change au fil du mois :", "How cervical mucus changes during the cycle:"),
                style = MaterialTheme.typography.titleSmall,
            )
            MucusStageRow(
                stage = tr("Après les règles", "After period"),
                texture = tr("Sensation plutôt sèche, peu ou pas de glaire visible.", "Rather dry feeling, little to no visible mucus."),
            )
            MucusStageRow(
                stage = tr("Avant l'ovulation", "Before ovulation"),
                texture = tr("Glaire crémeuse, blanchâtre ou collante.", "Creamy, whitish or sticky mucus."),
            )
            MucusStageRow(
                stage = tr("Période fertile & ovulation", "Fertile window & ovulation"),
                texture = tr("Glaire abondante, transparente et étirable entre deux doigts (comme du blanc d'œuf cru). Elle aide les spermatozoïdes.", "Abundant, clear and stretchy between two fingers (like raw egg white). It helps sperm."),
            )
            MucusStageRow(
                stage = tr("Après l'ovulation", "After ovulation"),
                texture = tr("Retour rapide à une glaire opaque, pâteuse ou sensation sèche.", "Quick return to opaque, pasty mucus or dry feeling."),
            )
        }
    }
}

@Composable
private fun PhaseExplainerRow(color: Color, phase: String, detail: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(12.dp)
                .background(color, CircleShape),
        )
        Column {
            Text(phase, style = MaterialTheme.typography.titleSmall)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AnatomyItemRow(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MucusStageRow(stage: String, texture: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(stage, style = MaterialTheme.typography.titleSmall)
        Text(texture, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun HormoneCurvesDiagram() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionTitle(tr("La courbe des hormones (cycle de 28 jours)", "Hormone level curves (28-day cycle)"))
        LunaCard {
            Text(
                tr("Évolution des 3 hormones clés sur un cycle naturel :", "Evolution of the 3 key hormones over a natural cycle:"),
                style = MaterialTheme.typography.titleSmall,
            )

            // Canvas graphique de la dynamique hormonale
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp)),
            ) {
                val padTop = 16.dp.toPx()
                val padBottom = 16.dp.toPx()
                val h = size.height - padTop - padBottom
                val w = size.width

                fun x(day: Float): Float = ((day - 1f) / 27f).coerceIn(0f, 1f) * w
                fun y(level: Float): Float = padTop + (1f - level.coerceIn(0f, 1f)) * h

                // Bandes pastel des 4 phases
                // Règles J1-J5
                drawRect(
                    color = Color(0xFFE8998D).copy(alpha = 0.16f),
                    topLeft = Offset(x(1f), 0f),
                    size = Size(x(5f) - x(1f), size.height),
                )
                // Folliculaire J5-J13
                drawRect(
                    color = Color(0xFFB4D6C4).copy(alpha = 0.16f),
                    topLeft = Offset(x(5f), 0f),
                    size = Size(x(13f) - x(5f), size.height),
                )
                // Ovulation J13-J15
                drawRect(
                    color = Color(0xFFF9D162).copy(alpha = 0.25f),
                    topLeft = Offset(x(13f), 0f),
                    size = Size(x(15f) - x(13f), size.height),
                )
                // Lutéale J15-J28
                drawRect(
                    color = Color(0xFFD3BBD9).copy(alpha = 0.16f),
                    topLeft = Offset(x(15f), 0f),
                    size = Size(x(28f) - x(15f), size.height),
                )

                // Ligne de base
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.35f),
                    start = Offset(0f, y(0f)),
                    end = Offset(w, y(0f)),
                    strokeWidth = 1.dp.toPx(),
                )

                // 1. Œstrogènes (Vert sauge / émeraude)
                val estrogenPoints = listOf(
                    Offset(x(1f), y(0.12f)),
                    Offset(x(5f), y(0.16f)),
                    Offset(x(9f), y(0.38f)),
                    Offset(x(12f), y(0.78f)),
                    Offset(x(13f), y(0.92f)),
                    Offset(x(14f), y(0.48f)),
                    Offset(x(17f), y(0.42f)),
                    Offset(x(21f), y(0.62f)),
                    Offset(x(25f), y(0.32f)),
                    Offset(x(28f), y(0.12f)),
                )
                drawSmoothCurve(estrogenPoints, Color(0xFF2E7D5E), 3.dp.toPx())

                // 2. Progestérone (Terracotta chaud)
                val progesteronePoints = listOf(
                    Offset(x(1f), y(0.06f)),
                    Offset(x(12f), y(0.06f)),
                    Offset(x(14f), y(0.12f)),
                    Offset(x(16f), y(0.35f)),
                    Offset(x(19f), y(0.78f)),
                    Offset(x(21.5f), y(0.88f)),
                    Offset(x(24f), y(0.65f)),
                    Offset(x(26.5f), y(0.24f)),
                    Offset(x(28f), y(0.06f)),
                )
                drawSmoothCurve(progesteronePoints, Color(0xFFC04E3A), 3.dp.toPx())

                // 3. Pic de LH (Ambre doré)
                val lhPoints = listOf(
                    Offset(x(1f), y(0.08f)),
                    Offset(x(11f), y(0.08f)),
                    Offset(x(12.5f), y(0.28f)),
                    Offset(x(13.5f), y(0.96f)),
                    Offset(x(14.5f), y(0.26f)),
                    Offset(x(16f), y(0.08f)),
                    Offset(x(28f), y(0.08f)),
                )
                drawSmoothCurve(lhPoints, Color(0xFFD49B24), 2.5.dp.toPx())
            }

            // Repères temporels horizontaux
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(tr("J1 (Règles)", "D1 (Period)"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(tr("J7", "D7"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(tr("J14 (Ovulation)", "D14 (Ovulation)"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text(tr("J21", "D21"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(tr("J28", "D28"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Légende descriptive
            HormoneLegendItem(
                color = Color(0xFF2E7D5E),
                name = tr("Œstrogènes (Estradiol)", "Estrogen (Estradiol)"),
                role = tr("Pic juste avant l'ovulation. Stimule la régénération de la muqueuse utérine (endomètre), éclaircit la glaire et soutient l'énergie.", "Peaks right before ovulation. Stimulates uterine lining growth, clarifies mucus and supports energy."),
            )
            HormoneLegendItem(
                color = Color(0xFFC04E3A),
                name = tr("Progestérone", "Progesterone"),
                role = tr("Pic au milieu de la phase lutéale (sécrétée par le corps jaune). Maintient l'endomètre prêt pour un embryon. Sa chute déclenche les règles.", "Peaks in mid-luteal phase (secreted by corpus luteum). Maintains lining for potential pregnancy. Its sharp drop triggers menstruation."),
            )
            HormoneLegendItem(
                color = Color(0xFFD49B24),
                name = tr("Pic de LH (Hormone lutéinisante)", "LH surge (Luteinizing hormone)"),
                role = tr("Décharge brève et intense à J13-J14 qui déclenche l'expulsion de l'ovule par le follicule en 24 à 36 heures.", "Brief, intense surge at Days 13-14 that triggers egg release within 24 to 36 hours."),
            )
        }
    }
}

private fun DrawScope.drawSmoothCurve(
    points: List<Offset>,
    color: Color,
    strokeWidth: Float,
) {
    if (points.size < 2) return
    val path = Path()
    path.moveTo(points[0].x, points[0].y)
    for (i in 0 until points.size - 1) {
        val p0 = points[i]
        val p1 = points[i + 1]
        val cx = (p0.x + p1.x) / 2f
        path.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        ),
    )
}

@Composable
private fun HormoneLegendItem(color: Color, name: String, role: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 5.dp)
                .size(10.dp)
                .background(color, CircleShape),
        )
        Column {
            Text(name, style = MaterialTheme.typography.titleSmall)
            Text(role, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private enum class UterusPart {
    ENDOMETRIUM,
    MYOMETRIUM,
    CERVIX,
    TUBES,
    OVARIES,
    VAGINA,
}

private fun uterusPartTitle(part: UterusPart): String = when (part) {
    UterusPart.ENDOMETRIUM -> tr("L'endomètre (muqueuse interne)", "The endometrium (inner lining)")
    UterusPart.MYOMETRIUM -> tr("Le myomètre (paroi musculaire)", "The myometrium (muscular wall)")
    UterusPart.CERVIX -> tr("Le col de l'utérus (cervix)", "The cervix")
    UterusPart.TUBES -> tr("Les trompes de Fallope", "The fallopian tubes")
    UterusPart.OVARIES -> tr("Les ovaires", "The ovaries")
    UterusPart.VAGINA -> tr("Le vagin & la vulve", "The vagina & vulva")
}

private fun uterusPartChipLabel(part: UterusPart): String = when (part) {
    UterusPart.ENDOMETRIUM -> tr("Endomètre", "Endometrium")
    UterusPart.MYOMETRIUM -> tr("Myomètre", "Myometrium")
    UterusPart.CERVIX -> tr("Col (Cervix)", "Cervix")
    UterusPart.TUBES -> tr("Trompes", "Tubes")
    UterusPart.OVARIES -> tr("Ovaires", "Ovaries")
    UterusPart.VAGINA -> tr("Vagin", "Vagina")
}

private fun uterusPartSummary(part: UterusPart): String = when (part) {
    UterusPart.ENDOMETRIUM -> tr(
        "Tissu vivant qui tapisse l'intérieur de la cavité utérine. Sous l'effet des œstrogènes puis de la progestérone, il s'épaissit et développe de nombreux micro-vaisseaux pour accueillir une éventuelle nidation. En l'absence de grossesse, la chute hormonale entraîne sa desquamation : c'est l'écoulement des règles.",
        "Living tissue lining the uterine cavity. Driven by estrogen and progesterone, it thickens and vascularizes to support potential embryo implantation. Without pregnancy, the drop in hormones causes it to shed: this is menstrual bleeding."
    )
    UterusPart.MYOMETRIUM -> tr(
        "Épaisse couche musculaire entourant la cavité utérine. C'est l'un des muscles lisses les plus puissants du corps. Lors des règles, la libération locale de prostaglandines déclenche ses contractions pour expulser l'endomètre. Ces contractions peuvent temporairement comprimer les vaisseaux sanguins, causant les crampes menstruelles.",
        "Thick muscular layer surrounding the uterine cavity. One of the body's strongest smooth muscles. During periods, local prostaglandins trigger its contractions to shed the endometrium. These contractions can compress local blood vessels, causing menstrual cramps."
    )
    UterusPart.CERVIX -> tr(
        "Partie étroite inférieure de l'utérus faisant la jonction avec le vagin. Il contient des glandes sécrétant la glaire cervicale (qui filtre ou favorise le passage des spermatozoïdes selon les phases du cycle) et s'entrouvre légèrement pour laisser s'évacuer le flux menstruel.",
        "Narrow lower part of the uterus connecting into the vagina. It houses glands that secrete cervical mucus (filtering or assisting sperm according to cycle phase) and opens slightly to allow menstrual flow to pass through."
    )
    UterusPart.TUBES -> tr(
        "Deux conduits fins d'environ 10 à 12 cm reliant chaque ovaire aux angles supérieurs de l'utérus. Leurs franges mobiles captent l'ovocyte émis lors de l'ovulation. C'est dans le tiers externe de la trompe qu'a lieu la fécondation si un spermatozoïde rencontre l'ovocyte.",
        "Two slender 10 to 12 cm ducts connecting each ovary to the upper corners of the uterus. Their mobile fimbriae sweep up the egg released during ovulation. Fertilization occurs in the outer third of the tube if a sperm meets the egg."
    )
    UterusPart.OVARIES -> tr(
        "Deux glandes situées de chaque côté de l'utérus. Elles abritent l'ensemble des follicules ovocytaires dès la naissance et orchestrent le cycle en produisant cycliquement les deux hormones clés : les œstrogènes (maturation du follicule) et la progestérone (sécrétée par le corps jaune après l'ovulation).",
        "Two glands located on each side of the uterus. They house all egg follicles from birth and orchestrate the cycle by cyclically producing two key hormones: estrogen (follicular growth) and progesterone (secreted by the corpus luteum after ovulation)."
    )
    UterusPart.VAGINA -> tr(
        "Conduit musculaire souple et extensible de 8 à 10 cm reliant le col de l'utérus à la vulve extérieure. Il assure l'écoulement vers l'extérieur des flux menstruels et des sécrétions physiologiques, participe aux rapports sexuels et permet le passage du bébé lors de l'accouchement.",
        "Flexible, elastic 8 to 10 cm muscular canal connecting the cervix to the external vulva. It channels menstrual fluid and physiological secretions outward, plays a role in sexual intercourse, and serves as the birth canal."
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InteractiveUterusDiagram() {
    var selectedPart by remember { mutableStateOf(UterusPart.ENDOMETRIUM) }
    val colors = MaterialTheme.colorScheme

    SectionTitle(tr("Schéma interactif de l'appareil reproducteur", "Interactive reproductive anatomy diagram"))
    LunaCard {
        Text(
            tr(
                "Touchez un organe sur le schéma ou les boutons ci-dessous pour découvrir son rôle physiologique :",
                "Touch an organ on the diagram or use the buttons below to explore its physiological function:"
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
        )

        // 1. Canvas vectoriel interactif
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surfaceVariant.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val w = size.width.toFloat()
                            val h = size.height.toFloat()
                            val cx = w / 2f
                            val topY = h * 0.22f
                            val cervixY = h * 0.62f
                            val dx = offset.x - cx
                            val dy = offset.y

                            // Hit testing par zone anatomique
                            val leftOvaryX = cx - w * 0.35f
                            val rightOvaryX = cx + w * 0.35f
                            val ovaryY = topY + h * 0.22f
                            val distLeftOvary = kotlin.math.hypot(offset.x - leftOvaryX, dy - ovaryY)
                            val distRightOvary = kotlin.math.hypot(offset.x - rightOvaryX, dy - ovaryY)

                            if (distLeftOvary < w * 0.12f || distRightOvary < w * 0.12f) {
                                selectedPart = UterusPart.OVARIES
                            } else if (dy < topY + h * 0.16f && kotlin.math.abs(dx) > w * 0.18f) {
                                selectedPart = UterusPart.TUBES
                            } else if (dy > cervixY + h * 0.06f && kotlin.math.abs(dx) < w * 0.16f) {
                                selectedPart = UterusPart.VAGINA
                            } else if (kotlin.math.abs(dy - cervixY) <= h * 0.08f && kotlin.math.abs(dx) < w * 0.14f) {
                                selectedPart = UterusPart.CERVIX
                            } else if (dy >= topY && dy <= cervixY && kotlin.math.abs(dx) <= w * 0.10f && dy < cervixY - h * 0.04f) {
                                selectedPart = UterusPart.ENDOMETRIUM
                            } else if (dy >= topY && dy <= cervixY && kotlin.math.abs(dx) <= w * 0.22f) {
                                selectedPart = UterusPart.MYOMETRIUM
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                val cx = w / 2f

                val topY = h * 0.22f
                val fundusWidth = w * 0.18f
                val cervixY = h * 0.64f
                val cervixWidth = w * 0.07f
                val vaginaBottomY = h * 0.90f
                val vaginaWidth = w * 0.09f

                // --- 1. Dessin du Vagin (canal inférieur) ---
                val vaginaPath = Path().apply {
                    moveTo(cx - cervixWidth, cervixY + h * 0.04f)
                    lineTo(cx + cervixWidth, cervixY + h * 0.04f)
                    lineTo(cx + vaginaWidth, vaginaBottomY)
                    lineTo(cx - vaginaWidth, vaginaBottomY)
                    close()
                }
                val isVaginaSelected = selectedPart == UterusPart.VAGINA
                drawPath(
                    path = vaginaPath,
                    color = if (isVaginaSelected) Color(0xFF78909C) else Color(0xFFB0BEC5).copy(alpha = 0.4f),
                )
                if (isVaginaSelected) {
                    drawPath(
                        path = vaginaPath,
                        color = Color(0xFF37474F),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                    )
                }

                // Plis vaginaux (rugae)
                for (i in 1..3) {
                    val y = cervixY + h * 0.08f + i * (h * 0.05f)
                    drawLine(
                        color = (if (isVaginaSelected) Color.White else colors.onSurfaceVariant).copy(alpha = 0.5f),
                        start = Offset(cx - vaginaWidth * 0.6f, y),
                        end = Offset(cx + vaginaWidth * 0.6f, y),
                        strokeWidth = 1.5.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }

                // --- 2. Dessin du Myomètre (corps musculaire de l'utérus) ---
                val myoPath = Path().apply {
                    moveTo(cx - cervixWidth, cervixY)
                    cubicTo(
                        cx - w * 0.20f, cervixY - h * 0.12f,
                        cx - fundusWidth, topY + h * 0.06f,
                        cx - fundusWidth, topY,
                    )
                    cubicTo(
                        cx - fundusWidth * 0.5f, topY - h * 0.04f,
                        cx + fundusWidth * 0.5f, topY - h * 0.04f,
                        cx + fundusWidth, topY,
                    )
                    cubicTo(
                        cx + fundusWidth, topY + h * 0.06f,
                        cx + w * 0.20f, cervixY - h * 0.12f,
                        cx + cervixWidth, cervixY,
                    )
                    close()
                }
                val isMyoSelected = selectedPart == UterusPart.MYOMETRIUM
                drawPath(
                    path = myoPath,
                    color = if (isMyoSelected) Color(0xFFE57373) else Color(0xFFEF9A9A).copy(alpha = 0.5f),
                )
                if (isMyoSelected) {
                    drawPath(
                        path = myoPath,
                        color = Color(0xFFC62828),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                    )
                }

                // --- 3. Dessin du Col de l'utérus (Cervix) ---
                val isCervixSelected = selectedPart == UterusPart.CERVIX
                val cervixCenter = Offset(cx, cervixY)
                drawCircle(
                    color = if (isCervixSelected) Color(0xFF81C784) else Color(0xFFA5D6A7).copy(alpha = 0.6f),
                    radius = cervixWidth * 0.9f,
                    center = cervixCenter,
                )
                drawCircle(
                    color = if (isCervixSelected) Color(0xFF2E7D32) else Color(0xFF4CAF50),
                    radius = cervixWidth * 0.35f,
                    center = cervixCenter,
                )
                if (isCervixSelected) {
                    drawCircle(
                        color = Color(0xFF1B5E20),
                        radius = cervixWidth * 1.1f,
                        center = cervixCenter,
                        style = Stroke(width = 2.5.dp.toPx()),
                    )
                }

                // --- 4. Dessin de l'Endomètre (cavité utérine interne triangulaire) ---
                val cavityPath = Path().apply {
                    val cornuY = topY + h * 0.08f
                    val cornuXLeft = cx - fundusWidth * 0.55f
                    val cornuXRight = cx + fundusWidth * 0.55f
                    val apexY = cervixY - h * 0.03f

                    moveTo(cornuXLeft, cornuY)
                    quadraticTo(cx, cornuY - h * 0.02f, cornuXRight, cornuY)
                    quadraticTo(cx + fundusWidth * 0.20f, cervixY - h * 0.12f, cx, apexY)
                    quadraticTo(cx - fundusWidth * 0.20f, cervixY - h * 0.12f, cornuXLeft, cornuY)
                    close()
                }
                val isEndoSelected = selectedPart == UterusPart.ENDOMETRIUM
                drawPath(
                    path = cavityPath,
                    color = if (isEndoSelected) Color(0xFFD32F2F) else Color(0xFFC2185B).copy(alpha = 0.6f),
                )
                drawPath(
                    path = cavityPath,
                    color = if (isEndoSelected) Color(0xFFFFCDD2) else Color(0xFF880E4F),
                    style = Stroke(width = if (isEndoSelected) 3.dp.toPx() else 1.5.dp.toPx()),
                )

                // --- 5. Dessin des Trompes de Fallope ---
                val isTubesSelected = selectedPart == UterusPart.TUBES
                val tubeColor = if (isTubesSelected) Color(0xFF1E88E5) else Color(0xFF90CAF9).copy(alpha = 0.7f)
                val tubeStroke = if (isTubesSelected) 4.5.dp.toPx() else 3.dp.toPx()

                // Trompe gauche
                val leftTubePath = Path().apply {
                    moveTo(cx - fundusWidth, topY + h * 0.02f)
                    cubicTo(
                        cx - w * 0.26f, topY - h * 0.08f,
                        cx - w * 0.38f, topY - h * 0.04f,
                        cx - w * 0.40f, topY + h * 0.12f,
                    )
                }
                drawPath(leftTubePath, color = tubeColor, style = Stroke(width = tubeStroke, cap = StrokeCap.Round))
                val leftFimbriaCenter = Offset(cx - w * 0.40f, topY + h * 0.12f)
                drawCircle(color = tubeColor, radius = 5.dp.toPx(), center = leftFimbriaCenter)

                // Trompe droite
                val rightTubePath = Path().apply {
                    moveTo(cx + fundusWidth, topY + h * 0.02f)
                    cubicTo(
                        cx + w * 0.26f, topY - h * 0.08f,
                        cx + w * 0.38f, topY - h * 0.04f,
                        cx + w * 0.40f, topY + h * 0.12f,
                    )
                }
                drawPath(rightTubePath, color = tubeColor, style = Stroke(width = tubeStroke, cap = StrokeCap.Round))
                val rightFimbriaCenter = Offset(cx + w * 0.40f, topY + h * 0.12f)
                drawCircle(color = tubeColor, radius = 5.dp.toPx(), center = rightFimbriaCenter)

                // --- 6. Dessin des Ovaires (forme d'amande) ---
                val isOvariesSelected = selectedPart == UterusPart.OVARIES
                val ovaryColor = if (isOvariesSelected) Color(0xFFFFA000) else Color(0xFFFFE082).copy(alpha = 0.7f)
                val leftOvaryPos = Offset(cx - w * 0.35f, topY + h * 0.22f)
                val rightOvaryPos = Offset(cx + w * 0.35f, topY + h * 0.22f)

                // Ligaments utéro-ovariens
                drawLine(
                    color = Color.Gray.copy(alpha = 0.4f),
                    start = Offset(cx - fundusWidth * 0.8f, topY + h * 0.14f),
                    end = leftOvaryPos,
                    strokeWidth = 1.5.dp.toPx(),
                )
                drawLine(
                    color = Color.Gray.copy(alpha = 0.4f),
                    start = Offset(cx + fundusWidth * 0.8f, topY + h * 0.14f),
                    end = rightOvaryPos,
                    strokeWidth = 1.5.dp.toPx(),
                )

                // Ovaires
                drawOval(
                    color = ovaryColor,
                    topLeft = Offset(leftOvaryPos.x - w * 0.06f, leftOvaryPos.y - h * 0.05f),
                    size = Size(w * 0.12f, h * 0.10f),
                )
                drawOval(
                    color = ovaryColor,
                    topLeft = Offset(rightOvaryPos.x - w * 0.06f, rightOvaryPos.y - h * 0.05f),
                    size = Size(w * 0.12f, h * 0.10f),
                )
                if (isOvariesSelected) {
                    drawOval(
                        color = Color(0xFFFF6F00),
                        topLeft = Offset(leftOvaryPos.x - w * 0.06f, leftOvaryPos.y - h * 0.05f),
                        size = Size(w * 0.12f, h * 0.10f),
                        style = Stroke(width = 2.5.dp.toPx()),
                    )
                    drawOval(
                        color = Color(0xFFFF6F00),
                        topLeft = Offset(rightOvaryPos.x - w * 0.06f, rightOvaryPos.y - h * 0.05f),
                        size = Size(w * 0.12f, h * 0.10f),
                        style = Stroke(width = 2.5.dp.toPx()),
                    )
                }
            }
        }

        // 2. Boutons sélecteurs (Chips)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            UterusPart.entries.forEach { part ->
                FilterChip(
                    selected = part == selectedPart,
                    onClick = { selectedPart = part },
                    label = { Text(uterusPartChipLabel(part), style = MaterialTheme.typography.labelMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.primaryContainer,
                        selectedLabelColor = colors.onPrimaryContainer,
                    ),
                )
            }
        }

        // 3. Fiche détaillée de l'organe sélectionné
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = colors.secondaryContainer.copy(alpha = 0.45f),
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.6f)),
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (selectedPart) {
                                    UterusPart.ENDOMETRIUM -> Color(0xFFD32F2F)
                                    UterusPart.MYOMETRIUM -> Color(0xFFC62828)
                                    UterusPart.CERVIX -> Color(0xFF2E7D32)
                                    UterusPart.TUBES -> Color(0xFF1E88E5)
                                    UterusPart.OVARIES -> Color(0xFFFF8F00)
                                    UterusPart.VAGINA -> Color(0xFF455A64)
                                }
                            )
                    )
                    Text(
                        uterusPartTitle(selectedPart),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                    )
                }
                Text(
                    uterusPartSummary(selectedPart),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun EndometriosisGuideScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Page {
        PageTitle(
            tr("Endométriose & douleurs pelviennes", "Endometriosis & pelvic pain"),
            onBack = onBack,
            subtitle = tr("Repères, signaux d'alerte et pistes de soulagement.", "Indicators, warning signs and everyday relief."),
        )

        SectionTitle(tr("Qu'est-ce que l'endométriose ?", "What is endometriosis?"))
        LunaCard {
            Text(
                tr(
                    "L'endométriose est une affection gynécologique chronique dans laquelle du tissu semblable à la muqueuse utérine (l'endomètre) se développe hors de l'utérus (sur les ovaires, les trompes, les ligaments utérins, le péritoine, la vessie ou les intestins).",
                    "Endometriosis is a chronic condition in which tissue similar to the lining of the uterus (the endometrium) grows outside the uterus (on ovaries, tubes, ligaments, peritoneum, bladder or bowels).",
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                tr(
                    "Sous l'influence des hormones du cycle, ces lésions s'épaississent et saignent à chaque cycle, provoquant une inflammation locale, des adhérences et des douleurs.",
                    "Influenced by hormonal variations, these lesions thicken and bleed each cycle, causing local inflammation, adhesions and pain.",
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Bullet(tr("Fréquence : elle concerne environ 1 personne sur 10 en âge de procréer (OMS / Inserm).", "Prevalence: affects approximately 1 in 10 individuals of reproductive age (WHO / Inserm)."))
            Bullet(tr("Une règle essentielle : une douleur de règles qui empêche d'aller en cours, au travail ou de vivre normalement n'est pas normale.", "Key takeaway: menstrual pain that prevents you from going to work, school or daily activities is not normal."))
        }

        SectionTitle(tr("Les 5 « D » : les principaux signaux d'alerte", "The 5 'Ds': primary warning signs"))
        LunaCard {
            Text(
                tr(
                    "Les sociétés savantes (HAS, ESHRE) retiennent 5 manifestations douloureuses caractéristiques à surveiller :",
                    "Medical societies (HAS, ESHRE) highlight 5 characteristic pain symptoms to monitor:",
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Bullet(tr("Dysménorrhée : règles très douloureuses qui résistent aux antalgiques usuels (paracétamol, anti-inflammatoires).", "Dysmenorrhea: severe period pain that resists standard pain relievers."))
            Bullet(tr("Dyspareunie : douleurs profondes pendant ou après les rapports sexuels.", "Dyspareunia: deep pain during or after sexual intercourse."))
            Bullet(tr("Dyschésie : douleurs lors du transit ou de la défécation, accentuées pendant les règles.", "Dyschezia: painful bowel movements, especially during periods."))
            Bullet(tr("Dysurie : douleurs, brûlures ou difficultés à uriner pendant les règles, sans infection bactérienne.", "Dysuria: painful or difficult urination during periods, without bacterial infection."))
            Bullet(tr("Douleurs pelviennes chroniques : douleurs continues dans le bas-ventre, tiraillements ou irradiations vers le dos, les lombaires et les cuisses.", "Chronic pelvic pain: persistent lower abdominal pain, pulling or radiating pain down the back and legs."))
        }

        SectionTitle(tr("Le parcours de diagnostic", "Diagnostic journey"))
        LunaCard {
            Text(
                tr(
                    "Le diagnostic met encore souvent plusieurs années à être posé. Noter précisément vos symptômes dans Lunaison et utiliser le « Bilan de santé » permet d'apporter un historique clair à votre soignant.",
                    "Diagnosis still takes several years on average. Tracking symptoms in Lunaison and using the 'Health report' helps provide clear history to your clinician.",
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Bullet(tr("Qui consulter en premier : médecin généraliste, sage-femme ou gynécologue.", "Who to see first: GP, midwife or gynecologist."))
            Bullet(tr("Examens clés : échographie pelvienne par voie endovaginale et IRM pelvienne. Il est recommandé de les faire réaliser par un radiologue référent formé à l'endométriose (une imagerie normale n'exclut pas une forme superficielle).", "Key exams: transvaginal pelvic ultrasound and pelvic MRI, ideally performed by a specialized radiologist."))
            Bullet(tr("Filières régionales : des réseaux de soins spécialisés labellisés (ARS) regroupent soignants, radiologues et centres experts pour faciliter l'orientation.", "Regional networks: specialized care networks help patients access experienced clinicians and reference centers."))
        }

        SectionTitle(tr("Pistes de soulagement au quotidien", "Everyday relief options"))
        LunaCard {
            Bullet(tr("Chaleur : bouillotte ou patchs chauffants sur le ventre ou les lombaires pour détendre les muscles contractés.", "Heat: hot water bottle or heat patches on the abdomen or back to relax tight muscles."))
            Bullet(tr("Électrostimulation TENS : ces appareils de neurostimulation transcutanée envoient de légers influx non douloureux qui court-circuitent le message de douleur. Ils peuvent être prescrits et pris en charge.", "TENS neurostimulation: wearable devices sending gentle pulses that block pain signals to the brain. Often available on prescription."))
            Bullet(tr("Kinésithérapie et ostéopathie pelvienne : exercices de respiration, étirements du psoas et travail doux du plancher pelvien pour soulager les tensions réflexes.", "Pelvic physiotherapy: breathing exercises, psoas stretches and pelvic floor release to ease reflex tension."))
            Bullet(tr("Cohérence cardiaque : le module « Respirer » de Lunaison aide à apaiser le système nerveux autonome lors des pics douloureux.", "Cardiac coherence: Lunaison's 'Breathe' exercise helps calm the nervous system during pain flares."))
            Bullet(tr("Traitements médicaux : pilule en continu ou DIU hormonal pour mettre les règles au repos, antalgiques adaptés ou chirurgie conservatrice si nécessaire, selon un projet de soins personnalisé.", "Medical management: continuous hormonal contraception to suppress periods, tailored medications or surgery when indicated."))
        }

        SectionTitle(
            tr(
                fr = "Ressources et associations de patientes",
                en = "Patient resources & associations",
                pl = "Wsparcie i stowarzyszenia pacjentek",
                uk = "Підтримка та асоціації пацієнток",
                es = "Recursos y asociaciones de pacientes",
                de = "Hilfe & Patientenorganisationen",
            )
        )
        LunaCard {
            Text(
                tr(
                    fr = "Des associations reconnues informent, accompagnent et orientent les personnes touchées vers des soignants formés :",
                    en = "Recognized associations provide support, guidance and specialized clinician directories:",
                    pl = "Uznane organizacje wspierają, informują i pomagają znaleźć doświadczonych specjalistów:",
                    uk = "Організації підтримки надають інформацію та допомагають знайти профільних фахівців:",
                    es = "Las asociaciones de pacientes ofrecen apoyo, información y orientación hacia especialistas formados:",
                    de = "Anerkannte Organisationen bieten Beratung, Informationen und Hilfe bei der Suche nach Fachkräften:",
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(onClick = {
                openLink(
                    context,
                    tr(
                        fr = "https://www.endofrance.org",
                        en = "https://www.endometriosis-uk.org",
                        pl = "https://pokonacendometrioze.pl",
                        uk = "https://www.endometriosis-uk.org",
                        es = "https://adaec.org",
                        de = "https://www.endometriose-vereinigung.de",
                    ),
                )
            }) {
                Text(
                    tr(
                        fr = "EndoFrance : association de lutte contre l'endométriose (endofrance.org)",
                        en = "Endometriosis UK: patient support and information (endometriosis-uk.org)",
                        pl = "Fundacja Pokonać Endometriozę : wsparcie pacjentek (pokonacendometrioze.pl)",
                        uk = "Endometriosis UK: підтримка пацієнток (endometriosis-uk.org)",
                        es = "ADAEC : Asociación de Afectadas de Endometriosis Crónica (adaec.org)",
                        de = "Endometriose-Vereinigung Deutschland e.V. (endometriose-vereinigung.de)",
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    textDecoration = TextDecoration.Underline,
                )
            }
            TextButton(onClick = {
                openLink(
                    context,
                    tr(
                        fr = "https://www.has-sante.fr/jcms/c_2819733/fr/prise-en-charge-de-l-endometriose",
                        en = "https://www.nhs.uk/conditions/endometriosis/",
                        pl = "https://www.mp.pl/pacjent/ginekologia/choroby/260759,endometrioza",
                        uk = "https://www.nhs.uk/conditions/endometriosis/",
                        es = "https://medlineplus.gov/spanish/endometriosis.html",
                        de = "https://www.frauenaerzte-im-netz.de/erkrankungen/endometriose/",
                    ),
                )
            }) {
                Text(
                    tr(
                        fr = "Recommandations officielles de la Haute Autorité de Santé (HAS)",
                        en = "Official clinical guidelines & care (nhs.uk)",
                        pl = "Medycyna Praktyczna : diagnostyka i leczenie endometriozy (mp.pl)",
                        uk = "Клінічні рекомендації та діагностика (nhs.uk)",
                        es = "Guía clínica sobre endometriosis (MedlinePlus)",
                        de = "Frauenärzte im Netz : Diagnose und Therapie der Endometriose",
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    textDecoration = TextDecoration.Underline,
                )
            }
        }

        Muted(tr("Cette fiche est informative et ne remplace pas une consultation médicale avec un professionnel de santé.", "This guide is for informational purposes and does not replace consultation with a healthcare professional."))
    }
}

@Composable
fun PcosGuideScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Page {
        PageTitle(
            tr("Le SOPK (ovaires polykystiques)", "PCOS (polycystic ovary syndrome)"),
            onBack = onBack,
            subtitle = tr("Repères cliniques, critères de Rotterdam et pistes d'action.", "Clinical signs, Rotterdam criteria and management options."),
        )

        SectionTitle(tr("Qu'est-ce que le SOPK ?", "What is PCOS?"))
        LunaCard {
            Text(
                tr(
                    "Le syndrome des ovaires polykystiques (SOPK) est la première cause d'infertilité anovulatoire et le dérèglement hormonal le plus fréquent chez les femmes en âge de procréer (environ 1 personne sur 10 selon l'Inserm et l'OMS).",
                    "Polycystic ovary syndrome (PCOS) is the leading cause of anovulatory infertility and the most common hormonal disorder in women of reproductive age (approx. 1 in 10 per WHO/Inserm).",
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                tr(
                    "Malgré son nom historique, il ne s'agit pas de kystes organiques mais d'une multitude de petits follicules immatures bloqués au stade de maturation dans l'ovaire, liés à un déséquilibre entre hormones hypophysaires (LH/FSH) et androgènes.",
                    "Despite its name, it does not involve ovarian cysts, but rather multiple small immature follicles paused in their growth, driven by an imbalance in pituitary hormones and androgens.",
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        SectionTitle(tr("Les critères de Rotterdam (diagnostic)", "Rotterdam diagnostic criteria"))
        LunaCard {
            Text(
                tr(
                    "Selon le consensus international de Rotterdam, le diagnostic est posé en présence d'au moins 2 des 3 critères suivants (après exclusion d'autres causes) :",
                    "Under international Rotterdam criteria, diagnosis requires at least 2 of the following 3 signs:",
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Bullet(tr("1. Ovulations rares ou absentes : cycles très longs (> 35 jours, spanioménorrhée), imprévisibles ou absence de règles pendant plus de 3 mois (aménorrhée).", "1. Infrequent or absent ovulation: very long cycles (> 35 days), unpredictable bleeding or absence of periods for 3+ months."))
            Bullet(tr("2. Hyperandrogénie : excès d'hormones androgènes visible (acné d'adulte tenace, pilosité marquée sur le menton, le thorax ou la ligne ombilicale, chute de cheveux) ou biologique (taux de testostérone libre élevé à la prise de sang).", "2. Hyperandrogenism: excess androgens manifesting as persistent acne, hirsutism, hair loss or elevated blood levels."))
            Bullet(tr("3. Morphologie ovarienne polykystique : présence de 20 follicules ou plus par ovaire ou volume ovarien supérieur à 10 ml à l'échographie endovaginale.", "3. Polycystic ovarian morphology: 20+ follicles per ovary or ovarian volume > 10 ml on pelvic ultrasound."))
        }

        SectionTitle(tr("Santé métabolique & globale", "Metabolic & general health"))
        LunaCard {
            Bullet(tr("Résistance à l'insuline : jusqu'à 70 % des femmes avec SOPK présentent une insulinorésistance qui stimule la production d'androgènes par les ovaires et favorise la prise de poids abdominale et la fatigue.", "Insulin resistance: present in up to 70% of individuals with PCOS, stimulating androgen production and abdominal weight gain."))
            Bullet(tr("Risques à long terme : surveillance préventive recommandée pour le diabète de type 2, l'hypertension et la santé cardiovasculaire.", "Long-term monitoring: recommended for type 2 diabetes risk and cardiovascular health."))
            Bullet(tr("Bien-être émotionnel : l'impact sur l'image corporelle, l'anxiété et le moral est réel et mérite un accompagnement bienveillant et sans jugement.", "Emotional well-being: the psychological impact on self-image and anxiety warrants compassionate care."))
        }

        SectionTitle(tr("Pistes de prise en charge", "Management options"))
        LunaCard {
            Bullet(tr("Alimentation à index glycémique modéré : privilégier les légumes, les légumineuses, les protéines et les bonnes graisses pour éviter les pics d'insuline.", "Low-glycemic nutrition: emphasizing vegetables, legumes, protein and fiber to smooth insulin spikes."))
            Bullet(tr("Activité physique régulière : la marche, la natation et le renforcement musculaire augmentent directement la sensibilité cellulaire à l'insuline.", "Regular physical activity: strength training and aerobic exercise directly improve cellular insulin sensitivity."))
            Bullet(tr("Protéger l'endomètre : en l'absence de règles régulières, un traitement progestatif séquentiel permet d'évacuer régulièrement la muqueuse utérine.", "Endometrial protection: sequential progestins ensure regular shedding when periods are absent."))
            Bullet(tr("Traitements médicaux : pilule oestroprogestative anti-androgène, metformine en cas d'insulinorésistance ou inducteurs d'ovulation en projet de grossesse.", "Medical options: anti-androgenic pill, metformin or ovulation induction when seeking pregnancy."))
            Bullet(tr("Dans Lunaison : activez l'option « Cycles naturellement irréguliers » dans les Réglages pour bénéficier de prévisions adaptées sans alertes anxiogènes.", "In Lunaison: enable 'Naturally irregular cycles' in Settings for adapted prediction intervals."))
        }

        SectionTitle(
            tr(
                fr = "Ressources et associations",
                en = "Resources & associations",
                pl = "Wsparcie i organizacje",
                uk = "Підтримка та ресурси",
                es = "Recursos y asociaciones",
                de = "Hilfe & Organisationen",
            )
        )
        LunaCard {
            TextButton(onClick = {
                openLink(
                    context,
                    tr(
                        fr = "https://www.espopk.org",
                        en = "https://www.verity-pcos.org.uk",
                        pl = "https://pacjent.gov.pl/zapobiegaj/zespol-policystycznych-jajnikow-pcos",
                        uk = "https://www.verity-pcos.org.uk",
                        es = "https://aesopspain.org",
                        de = "https://www.pcos-selbsthilfe.org",
                    ),
                )
            }) {
                Text(
                    tr(
                        fr = "Association Esp'OPK : association francophone dédiée au SOPK (espopk.org)",
                        en = "Verity PCOS UK: patient support charity (verity-pcos.org.uk)",
                        pl = "Pacjent.gov.pl : Zespół policystycznych jajników (PCOS)",
                        uk = "Verity PCOS: підтримка пацієнток (verity-pcos.org.uk)",
                        es = "AESOP : Asociación Española de Síndrome de Ovario Poliquístico (aesopspain.org)",
                        de = "Selbsthilfegruppe PCO-Syndrom e.V. (pcos-selbsthilfe.org)",
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    textDecoration = TextDecoration.Underline,
                )
            }
            TextButton(onClick = {
                openLink(
                    context,
                    tr(
                        fr = "https://www.inserm.fr/dossier/syndrome-ovaires-polykystiques-sopk/",
                        en = "https://www.nhs.uk/conditions/polycystic-ovary-syndrome-pcos/",
                        pl = "https://www.mp.pl/pacjent/ginekologia/choroby/260757,zespol-policystycznych-jajnikow-pcos",
                        uk = "https://www.nhs.uk/conditions/polycystic-ovary-syndrome-pcos/",
                        es = "https://medlineplus.gov/spanish/polycysticoverydisease.html",
                        de = "https://www.frauenaerzte-im-netz.de/erkrankungen/pcos-polyzystisches-ovarialsyndrom/",
                    ),
                )
            }) {
                Text(
                    tr(
                        fr = "Dossier scientifique de l'Inserm sur le SOPK",
                        en = "NHS clinical guide to PCOS (nhs.uk)",
                        pl = "Medycyna Praktyczna : objawy i postępowanie w PCOS (mp.pl)",
                        uk = "Клінічне керівництво щодо СПКЯ (nhs.uk)",
                        es = "Información médica sobre el SOP (MedlinePlus)",
                        de = "Frauenärzte im Netz : PCO-Syndrom Ursachen und Therapie",
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    textDecoration = TextDecoration.Underline,
                )
            }
        }

        Muted(tr("Cette fiche est informative et ne remplace pas une consultation médicale avec un professionnel de santé.", "This guide is for informational purposes and does not replace consultation with a healthcare professional."))
    }
}

@Composable
fun AdenomyosisGuideScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Page {
        PageTitle(
            tr("L'adénomyose", "Adenomyosis"),
            onBack = onBack,
            subtitle = tr("Repères, règles très abondantes et options de soulagement.", "Indicators, heavy menstrual bleeding and relief options."),
        )

        SectionTitle(tr("Qu'est-ce que l'adénomyose ?", "What is adenomyosis?"))
        LunaCard {
            Text(
                tr(
                    "L'adénomyose se caractérise par l'infiltration de cellules de l'endomètre (la muqueuse qui tapisse l'intérieur de l'utérus) dans l'épaisseur du muscle utérin (le myomètre). On la décrit couramment comme une « endométriose interne » de l'utérus.",
                    "Adenomyosis occurs when endometrial tissue (the lining inside the uterus) infiltrates the muscular uterine wall (myometrium). It is often called 'internal endometriosis'.",
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                tr(
                    "Ces foyers de muqueuse réagissent aux hormones de chaque cycle : ils saignent dans le muscle, provoquant une inflammation locale, des crampes douloureuses et une augmentation du volume de l'utérus.",
                    "These tissue patches respond to hormonal cycles by bleeding inside the uterine muscle, causing inflammation, intense cramps and an enlarged uterus.",
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Bullet(tr("Fréquence : elle touche 15 à 20 % des femmes et coexiste souvent avec l'endométriose pelvienne.", "Prevalence: affects 15 to 20% of women and frequently coexists with pelvic endometriosis."))
        }

        SectionTitle(tr("Les symptômes principaux", "Main symptoms"))
        LunaCard {
            Bullet(tr("Règles très abondantes (ménorragies) : saignements prolongés (> 7 jours), abondants avec émission fréquente de caillots sanguins nécessitant de doubler les protections ou de se lever la nuit.", "Heavy menstrual bleeding (menorrhagia): prolonged bleeding (> 7 days), large blood clots and frequent protection changes."))
            Bullet(tr("Crampes utérines violentes : contractions douloureuses avant et pendant les règles, résistant souvent aux antalgiques courants.", "Severe menstrual cramps: intense pain before and during periods that resists standard analgesics."))
            Bullet(tr("Pesanteur pelvienne : sensation d'inconfort continu ou de bas-ventre lourd et gonflé.", "Pelvic heaviness: continuous lower abdominal discomfort or feelings of pressure."))
            Bullet(tr("Fatigue et carence en fer : les saignements abondants répétés épuisent les réserves de fer (anémie ferriprive), causant épuisement et essoufflement.", "Fatigue and iron deficiency: heavy blood loss depletes iron stores, causing persistent fatigue and shortness of breath."))
        }

        SectionTitle(tr("Le diagnostic", "Diagnosis"))
        LunaCard {
            Text(
                tr(
                    "Le diagnostic est posé grâce à des examens d'imagerie ciblés réalisés par des praticiens formés :",
                    "Diagnosis relies on specialized pelvic imaging performed by experienced practitioners:",
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Bullet(tr("Échographie pelvienne endovaginale : visualise l'asymétrie des parois utérines, l'aspect hétérogène du muscle et les micro-kystes myométriaux.", "Transvaginal ultrasound: shows wall asymmetry, heterogeneous muscle texture and myometrial microcysts."))
            Bullet(tr("IRM pelvienne : examen de référence mesurant l'épaississement de la « zone jonctionnelle » (> 12 mm) entre l'endomètre et le myomètre.", "Pelvic MRI: gold standard measuring the thickening of the junctional zone (> 12 mm)."))
        }

        SectionTitle(tr("Pistes de soulagement", "Relief and treatments"))
        LunaCard {
            Bullet(tr("Anti-hémorragiques (acide tranexamique) : pris uniquement pendant les règles, il diminue d'environ 50 % l'abondance des saignements.", "Antifibrinolytics (tranexamic acid): taken during flow days to reduce blood loss volume by approx. 50%."))
            Bullet(tr("Contraception hormonale : stérilet hormonal au lévonorgestrel ou pilule progestative en continu pour atrophier la muqueuse et stopper les hémorragies.", "Hormonal therapy: levonorgestrel IUD or continuous progestin pill to thin the lining and stop heavy bleeding."))
            Bullet(tr("Recharger le fer : dosage de la ferritine et complémentation en fer (orale ou injectable si sévère) pour restaurer l'énergie.", "Iron repletion: ferritin checks and oral or IV iron supplements to resolve anemia."))
            Bullet(tr("Chaleur et antispasmodiques : bouillotte chaude sur le bas-ventre et repos pour relâcher les spasmes myométriaux.", "Heat and antispasmodics: heating pad on the lower abdomen to ease myometrial spasms."))
            Bullet(tr("Gestes interventionnels : embolisation des artères utérines ou chirurgie discutées au cas par cas en cas d'échec des traitements médicaux.", "Interventional options: uterine artery embolization or surgery evaluated in severe cases."))
        }

        SectionTitle(
            tr(
                fr = "Ressources et accompagnement",
                en = "Resources & support",
                pl = "Wsparcie i źródła informacji",
                uk = "Підтримка та ресурси",
                es = "Recursos y apoyo",
                de = "Hilfe & Ressourcen",
            )
        )
        LunaCard {
            TextButton(onClick = {
                openLink(
                    context,
                    tr(
                        fr = "https://www.endofrance.org/la-maladie-endometriose/adenomyose/",
                        en = "https://www.nhs.uk/conditions/adenomyosis/",
                        pl = "https://pokonacendometrioze.pl",
                        uk = "https://www.nhs.uk/conditions/adenomyosis/",
                        es = "https://adaec.org",
                        de = "https://www.endometriose-vereinigung.de",
                    ),
                )
            }) {
                Text(
                    tr(
                        fr = "EndoFrance : dossier complet sur l'adénomyose (endofrance.org)",
                        en = "NHS guide to adenomyosis symptoms & care (nhs.uk)",
                        pl = "Fundacja Pokonać Endometriozę : informacje o adenomiozie (pokonacendometrioze.pl)",
                        uk = "Керівництво NHS щодо аденоміозу (nhs.uk)",
                        es = "ADAEC : información y apoyo sobre adenomiosis (adaec.org)",
                        de = "Endometriose-Vereinigung : Informationen zu Adenomyose",
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    textDecoration = TextDecoration.Underline,
                )
            }
            TextButton(onClick = {
                openLink(
                    context,
                    tr(
                        fr = "https://www.has-sante.fr/jcms/c_2819733/fr/prise-en-charge-de-l-endometriose",
                        en = "https://www.adenomyosisadviceassociation.org",
                        pl = "https://www.mp.pl/pacjent/ginekologia/choroby/260761,adenomioza",
                        uk = "https://www.adenomyosisadviceassociation.org",
                        es = "https://medlineplus.gov/spanish/ency/article/001513.htm",
                        de = "https://www.frauenaerzte-im-netz.de/erkrankungen/endometriose/formen-symptome/",
                    ),
                )
            }) {
                Text(
                    tr(
                        fr = "Recommandations de prise en charge (HAS)",
                        en = "Adenomyosis Advice Association (adenomyosisadviceassociation.org)",
                        pl = "Medycyna Praktyczna : adenomioza macicy (mp.pl)",
                        uk = "Adenomyosis Advice Association (adenomyosisadviceassociation.org)",
                        es = "MedlinePlus : Adenomiosis uterina y diagnóstico",
                        de = "Frauenärzte im Netz : Adenomyose verstehen und behandeln",
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    textDecoration = TextDecoration.Underline,
                )
            }
        }

        Muted(tr("Cette fiche est informative et ne remplace pas une consultation médicale avec un professionnel de santé.", "This guide is for informational purposes and does not replace consultation with a healthcare professional."))
    }
}

@Composable
fun PerimenopauseGuideScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Page {
        PageTitle(
            tr("La périménopause", "Perimenopause"),
            onBack = onBack,
            subtitle = tr("Comprendre les variations hormonales et vivre sereinement la transition.", "Understanding hormonal fluctuations and navigating the transition."),
        )

        SectionTitle(tr("Qu'est-ce que la périménopause ?", "What is perimenopause?"))
        LunaCard {
            Text(
                tr(
                    "La périménopause est la phase naturelle de transition hormonale qui précède la ménopause (l'arrêt définitif des règles pendant 12 mois consécutifs).",
                    "Perimenopause is the natural transition period preceding menopause (defined as 12 consecutive months without periods).",
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                tr(
                    "Elle débute le plus souvent entre 40 et 48 ans et dure en moyenne entre 2 et 8 ans. La réserve de follicules ovariens diminue et la production d'œstrogènes et de progestérone commence à fluctuer de manière imprévisible.",
                    "It typically begins between ages 40 and 48 and lasts 2 to 8 years on average. Ovarian follicle reserve declines, causing estrogen and progesterone to fluctuate.",
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        SectionTitle(tr("Les deux phases de la transition", "The two phases of transition"))
        LunaCard {
            Bullet(tr("1. Chute précoce de la progestérone : les ovulations deviennent irrégulières. Les œstrogènes ne sont plus contrebalancés : les cycles se raccourcissent (21 à 25 jours), les seins deviennent très sensibles, l'irritabilité et le syndrome prémenstruel s'accentuent.", "1. Early progesterone decline: ovulation becomes irregular. Estrogen dominates unopposed: cycles shorten (21-25 days), breast tenderness increases and PMS intensifies."))
            Bullet(tr("2. Chute tardive des œstrogènes : les cycles s'allongent considérablement (parfois 2 à 4 mois sans règles), les flux deviennent imprévisibles, et les symptômes vasomoteurs apparaissent.", "2. Late estrogen decline: cycles lengthen significantly (2-4 months between periods), flow becomes unpredictable, and vasomotor symptoms emerge."))
        }

        SectionTitle(tr("Les manifestations fréquentes", "Common symptoms"))
        LunaCard {
            Bullet(tr("Bouffées de chaleur et sueurs nocturnes : sensation soudaine de chaleur intense remontant au cou et au visage, parfois suivie de frissons.", "Hot flashes & night sweats: sudden waves of intense heat over the face and neck, sometimes followed by chills."))
            Bullet(tr("Troubles du sommeil : difficultés d'endormissement et réveils nocturnes fréquents (souvent vers 3h-4h).", "Sleep disruption: difficulty falling asleep and frequent night awakenings."))
            Bullet(tr("Brouillard mental (« brain fog ») : sensation de tête confuse, trous de mémoire ponctuels et baisse de concentration passagère.", "Brain fog: temporary concentration lapses and occasional memory blanks."))
            Bullet(tr("Variations de l'humeur : anxiété nouvelle, irritabilité ou moral fluctuant liés aux vagues d'œstrogènes.", "Mood changes: heightened anxiety or irritability linked to shifting hormone levels."))
            Bullet(tr("Sécheresse intime : inconfort vaginal lors des rapports, attribuable à la baisse progressive d'imprégnation œstrogénique.", "Vaginal dryness: intimate discomfort during sex due to lower estrogen support."))
        }

        SectionTitle(tr("Pistes d'accompagnement et bien-être", "Support and well-being options"))
        LunaCard {
            Bullet(tr("Activité physique régulière : le renforcement musculaire et les exercices avec impact (marche active, danse) protègent la densité osseuse et la masse musculaire.", "Regular physical activity: strength training and weight-bearing exercise protect bone density and muscle mass."))
            Bullet(tr("Alimentation protectrice : apport suffisant en protéines, calcium (produits laitiers, eaux minérales calciques) et vitamine D.", "Bone & metabolic nutrition: adequate protein, calcium and vitamin D intake."))
            Bullet(tr("Modérer les déclencheurs de bouffées : l'alcool, le café, les repas très épicés et le stress aigu accentuent la fréquence des bouffées de chaleur.", "Moderate triggers: alcohol, caffeine, spicy foods and acute stress commonly trigger hot flashes."))
            Bullet(tr("Traitement hormonal de la ménopause (THM) : peut être discuté avec un soignant si les symptômes altèrent la qualité de vie, avec un bilan bénéfice/risque personnalisé.", "Menopausal hormone therapy (MHT): can be evaluated with a doctor if symptoms substantially affect daily quality of life."))
            Bullet(tr("Dans Lunaison : notez vos bouffées et troubles du sommeil dans votre carnet, et activez le mode « Cycles irréguliers » dans les Réglages.", "In Lunaison: track hot flashes and sleep in your log, and enable 'Irregular cycles' mode in Settings."))
        }

        SectionTitle(
            tr(
                fr = "Ressources et accompagnement",
                en = "Resources & support",
                pl = "Wsparcie i źródła informacji",
                uk = "Підтримка та ресурси",
                es = "Recursos y apoyo",
                de = "Hilfe & Ressourcen",
            )
        )
        LunaCard {
            TextButton(onClick = {
                openLink(
                    context,
                    tr(
                        fr = "https://www.gemvi.org",
                        en = "https://www.themenopausecharity.org",
                        pl = "https://pacjent.gov.pl/aktualnosc/klimakterium-okres-przekwitania",
                        uk = "https://www.themenopausecharity.org",
                        es = "https://aeem.es",
                        de = "https://www.menopause-gesellschaft.de",
                    ),
                )
            }) {
                Text(
                    tr(
                        fr = "GEMVI : Société savante sur la ménopause (gemvi.org)",
                        en = "The Menopause Charity: evidence-based information (themenopausecharity.org)",
                        pl = "Pacjent.gov.pl : Klimakterium i menopauza",
                        uk = "The Menopause Charity: науково підтверджена інформація (themenopausecharity.org)",
                        es = "AEEM : Asociación Española para el Estudio de la Menopausia (aeem.es)",
                        de = "Deutsche Menopause Gesellschaft e.V. (menopause-gesellschaft.de)",
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    textDecoration = TextDecoration.Underline,
                )
            }
            TextButton(onClick = {
                openLink(
                    context,
                    tr(
                        fr = "https://www.inserm.fr/dossier/menopause/",
                        en = "https://www.nhs.uk/conditions/menopause/",
                        pl = "https://www.mp.pl/pacjent/ginekologia/choroby/260751,klimakterium-i-menopauza",
                        uk = "https://www.nhs.uk/conditions/menopause/",
                        es = "https://medlineplus.gov/spanish/menopause.html",
                        de = "https://www.frauenaerzte-im-netz.de/koerper-sexualitaet/wechseljahre-klimakterium/",
                    ),
                )
            }) {
                Text(
                    tr(
                        fr = "Dossier scientifique de l'Inserm sur la ménopause",
                        en = "NHS guide to perimenopause and menopause (nhs.uk)",
                        pl = "Medycyna Praktyczna : objawy i terapia (mp.pl)",
                        uk = "Керівництво NHS щодо менопаузи (nhs.uk)",
                        es = "MedlinePlus : Información médica sobre perimenopausia",
                        de = "Frauenärzte im Netz : Wechseljahre und Perimenopause",
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    textDecoration = TextDecoration.Underline,
                )
            }
        }

        Muted(tr("Cette fiche est informative et ne remplace pas une consultation médicale avec un professionnel de santé.", "This guide is for informational purposes and does not replace consultation with a healthcare professional."))
    }
}
