package fr.lunaison.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
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

        // Catégorie 2 : Soulagement et bien-être
        SectionTitle(tr("Soulagement et bien-être", "Relief and well-being"))
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

        TextButton(onClick = { openLink(context, "https://www.questionsexualite.fr/choisir-sa-contraception") }) {
            Text(
                tr("Guides officiels de Santé publique France (QuestionSexualité.fr)", "Official public health guidance (in French)"),
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
                title = tr("Le col de l'utérus (l'entrée)", "The cervix (the doorway)"),
                description = tr("La partie basse de l'utérus. Il produit la glaire cervicale qui change d'aspect au fil du mois.", "The lower part of the uterus. It secretes cervical mucus that changes texture throughout the month."),
            )
            AnatomyItemRow(
                title = tr("L'utérus et l'endomètre (le nid)", "The uterus & endometrium (the nest)"),
                description = tr("Un muscle creux de la taille d'une petite poire. Sa muqueuse intérieure (l'endomètre) s'épaissit chaque mois puis se détache lors des règles.", "A hollow pear-sized muscle. Its inner lining (endometrium) thickens monthly then sheds during menstruation."),
            )
            AnatomyItemRow(
                title = tr("Les trompes de Fallope (les bras)", "The fallopian tubes (the arms)"),
                description = tr("Deux conduits fins reliant les ovaires à l'utérus, où a lieu la fécondation.", "Two slender ducts connecting ovaries to uterus, where fertilization takes place."),
            )
            AnatomyItemRow(
                title = tr("Les ovaires (les réservoirs)", "The ovaries (the reservoirs)"),
                description = tr("Deux glandes qui abritent les ovocytes dès la naissance et fabriquent les œstrogènes et la progestérone.", "Two glands containing egg cells from birth and producing estrogen and progesterone."),
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
