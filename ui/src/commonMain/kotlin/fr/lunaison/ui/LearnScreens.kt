package fr.lunaison.ui

import kotlin.math.PI
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import fr.lunaison.core.content.Article
import fr.lunaison.core.content.Articles
import fr.lunaison.core.designsystem.LunaCard
import fr.lunaison.core.designsystem.SectionTitle
import fr.lunaison.core.model.tr

@Composable
fun LearnScreen(onArticle: (String) -> Unit) {
    Page {
        PageTitle(
            tr("Comprendre", "Learn", pl = "Wiedza", uk = "Довідник", es = "Aprender", de = "Wissen"),
            subtitle = tr("Des fiches courtes, relues et sourcées pour mieux vivre son cycle.", "Short, reviewed and referenced guides to better understand your cycle.", pl = "Krótkie, sprawdzone i opatrzone źródłami artykuły, które pomagają lepiej przeżywać cykl.", uk = "Короткі, перевірені та підкріплені джерелами статті, щоб краще проживати свій цикл.", es = "Fichas breves, revisadas y con fuentes para vivir mejor tu ciclo.", de = "Kurze, geprüfte und mit Quellen belegte Artikel, um den eigenen Zyklus besser zu erleben."),
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
                        tr("Découvrir son anatomie et son cycle", "Discovering anatomy and the cycle", pl = "Poznaj swoją anatomię i cykl", uk = "Дізнайтеся більше про свою анатомію та цикл", es = "Descubre tu anatomía y tu ciclo", de = "Anatomie und Zyklus entdecken"),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        tr("Repères illustrés, organes et vagues hormonales expliqués simplement.", "Illustrated guide, organs and hormonal waves simply explained.", pl = "Ilustrowane wskazówki, narządy i fale hormonalne wyjaśnione prosto.", uk = "Ілюстровані орієнтири, органи та гормональні хвилі, пояснені просто.", es = "Esquemas ilustrados, órganos y oleadas hormonales explicados con sencillez.", de = "Illustrierte Orientierung, Organe und Hormonwellen einfach erklärt."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Catégorie 1 : Le cycle et les hormones
        SectionTitle(tr("Le cycle et les hormones", "The cycle and hormones", pl = "Cykl i hormony", uk = "Цикл і гормони", es = "El ciclo y las hormonas", de = "Der Zyklus und die Hormone"))
        LunaCard(modifier = Modifier.clickable(role = Role.Button) { onArticle("contraception") }) {
            Text(tr("Contraception & règles : ce qui change", "Contraception & periods: what changes", pl = "Antykoncepcja a miesiączka: co się zmienia", uk = "Контрацепція та менструація: що змінюється", es = "Anticoncepción y regla: qué cambia", de = "Verhütung und Periode: was sich ändert"), style = MaterialTheme.typography.titleMedium)
            Text(
                tr("Pilule, stérilet au cuivre ou hormonal, implant : pourquoi les saignements sont différents et comment ils fonctionnent.", "Pill, copper or hormonal IUD, implant: why bleeding differs and how it works.", pl = "Tabletka, wkładka miedziana lub hormonalna, implant: dlaczego krwawienia są inne i jak działają te metody.", uk = "Таблетка, мідна чи гормональна спіраль, імплант: чому кровотечі інші та як ці методи діють.", es = "Píldora, DIU de cobre u hormonal, implante: por qué los sangrados son distintos y cómo funcionan.", de = "Pille, Kupfer- oder Hormonspirale, Implantat: warum die Blutungen anders sind und wie sie funktionieren."),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        val cycleArticles = Articles.all.filter { it.id in listOf("cycle", "previsions") }
        cycleArticles.forEach { article ->
            ArticleCard(article, onArticle)
        }
        LunaCard(modifier = Modifier.clickable(role = Role.Button) { onArticle("sopk") }) {
            Text(tr("Le SOPK (ovaires polykystiques)", "PCOS (polycystic ovary syndrome)", pl = "PCOS (zespół policystycznych jajników)", uk = "СПКЯ (синдром полікістозних яєчників)", es = "El SOP (ovarios poliquísticos)", de = "PCOS (polyzystische Ovarien)"), style = MaterialTheme.typography.titleMedium)
            Text(
                tr(
                    fr = "Critères de Rotterdam, ovulations rares, hyperandrogénie et repères au quotidien.",
                    en = "Rotterdam criteria, infrequent ovulation, hyperandrogenism and everyday management.",
                    pl = "Kryteria rotterdamskie, rzadkie owulacje, hiperandrogenizm i wskazówki na co dzień.",
                    uk = "Роттердамські критерії, рідкісні овуляції, гіперандрогенія та орієнтири на щодень.",
                    es = "Criterios de Róterdam, ovulaciones infrecuentes, hiperandrogenismo y pautas del día a día.",
                    de = "Rotterdam-Kriterien, seltene Eisprünge, Hyperandrogenismus und Orientierung im Alltag.",
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LunaCard(modifier = Modifier.clickable(role = Role.Button) { onArticle("perimenopause") }) {
            Text(tr("La périménopause et ses changements", "Perimenopause and its changes", pl = "Perimenopauza i jej zmiany", uk = "Перименопауза та її зміни", es = "La perimenopausia y sus cambios", de = "Die Perimenopause und ihre Veränderungen"), style = MaterialTheme.typography.titleMedium)
            Text(
                tr(
                    fr = "Cycles irréguliers, bouffées de chaleur, sommeil et transition hormonale expliqués.",
                    en = "Irregular cycles, hot flashes, sleep and hormonal transition explained.",
                    pl = "Nieregularne cykle, uderzenia gorąca, sen i przejście hormonalne wyjaśnione.",
                    uk = "Нерегулярні цикли, припливи жару, сон і гормональний перехід: пояснення.",
                    es = "Ciclos irregulares, sofocos, sueño y transición hormonal explicados.",
                    de = "Unregelmäßige Zyklen, Hitzewallungen, Schlaf und hormoneller Übergang erklärt.",
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Catégorie 2 : Soulagement et bien-être
        SectionTitle(tr("Soulagement et bien-être", "Relief and well-being", pl = "Ulga i dobre samopoczucie", uk = "Полегшення та самопочуття", es = "Alivio y bienestar", de = "Linderung und Wohlbefinden"))
        LunaCard(modifier = Modifier.clickable(role = Role.Button) { onArticle("endometriose") }) {
            Text(tr("Endométriose & douleurs pelviennes", "Endometriosis & pelvic pain", pl = "Endometrioza i ból miednicy", uk = "Ендометріоз і тазовий біль", es = "Endometriosis y dolor pélvico", de = "Endometriose und Beckenschmerzen"), style = MaterialTheme.typography.titleMedium)
            Text(
                tr(
                    fr = "Comprendre les signaux d'alerte (les 5 « D »), le diagnostic et les pistes de soulagement au quotidien.",
                    en = "Understand warning signs (the 5 'Ds'), diagnosis and everyday relief options.",
                    pl = "Zrozumieć sygnały alarmowe (5 „D”), diagnostykę i sposoby łagodzenia dolegliwości na co dzień.",
                    uk = "Розуміння тривожних сигналів (5 «Д»), діагностики та способів полегшення в повсякденному житті.",
                    es = "Comprender las señales de alerta (las 5 «D»), el diagnóstico y las vías de alivio en el día a día.",
                    de = "Warnzeichen (die 5 „D“), Diagnose und Wege zur Linderung im Alltag verstehen.",
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LunaCard(modifier = Modifier.clickable(role = Role.Button) { onArticle("adenomyose") }) {
            Text(tr("L'adénomyose : repères et saignements", "Adenomyosis: bleeding and pain", pl = "Adenomioza: wskazówki i krwawienia", uk = "Аденоміоз: орієнтири та кровотечі", es = "La adenomiosis: pautas y sangrados", de = "Adenomyose: Orientierung und Blutungen"), style = MaterialTheme.typography.titleMedium)
            Text(
                tr(
                    fr = "L'endométriose interne du muscle utérin, règles très abondantes, crampes et prise en charge.",
                    en = "Internal endometriosis in the uterine wall, heavy bleeding, cramps and management.",
                    pl = "Endometrioza wewnętrzna mięśnia macicy, bardzo obfite miesiączki, skurcze i postępowanie.",
                    uk = "Внутрішній ендометріоз м'яза матки, дуже рясні менструації, спазми та лікування.",
                    es = "Endometriosis interna del músculo uterino, reglas muy abundantes, calambres y tratamiento.",
                    de = "Innere Endometriose der Gebärmuttermuskulatur, sehr starke Perioden, Krämpfe und Behandlung.",
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
        SectionTitle(tr("Santé, quotidien et entourage", "Health, daily life and loved ones", pl = "Zdrowie, codzienność i otoczenie", uk = "Здоров'я, повсякденне життя та оточення", es = "Salud, día a día y entorno", de = "Gesundheit, Alltag und Umfeld"))
        val lifeArticles = Articles.all.filter { it.id in listOf("consulter", "protections", "entourage") }
        lifeArticles.forEach { article ->
            ArticleCard(article, onArticle)
        }

        Muted(tr("Ces fiches informent et ne remplacent pas l'avis d'un professionnel de santé.", "These guides inform you and do not replace the advice of a health professional.", pl = "Te artykuły informują i nie zastępują porady osoby z personelu medycznego.", uk = "Ці статті інформують і не замінюють консультацію медичного працівника.", es = "Estas fichas informan y no sustituyen la opinión de un profesional sanitario.", de = "Diese Artikel informieren und ersetzen nicht die Einschätzung von medizinischem Fachpersonal."))
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
    val context = rememberPlatformContext()
    Page {
        if (article == null) {
            PageTitle(tr("Fiche introuvable", "Guide not found", pl = "Nie znaleziono artykułu", uk = "Статтю не знайдено", es = "Ficha no encontrada", de = "Artikel nicht gefunden"), onBack = onBack)
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
                    tr("Sources de cette fiche, sur le site", "Sources for this guide, on the website (in French)", pl = "Źródła tego artykułu na stronie", uk = "Джерела цієї статті на сайті", es = "Fuentes de esta ficha, en el sitio web", de = "Quellen zu diesem Artikel auf der Website"),
                    style = MaterialTheme.typography.bodySmall,
                    textDecoration = TextDecoration.Underline,
                )
            }
        }
    }
}

/** Types de contraception avec leur logique physiologique. */
enum class ContraceptiveMethod {
    COMBINED_PILL,
    COPPER_IUD,
    IMPLANT_OR_PROGESTIN,
    HORMONAL_IUD,
    NATURAL,
    ;

    val label: String get() = when (this) {
        COMBINED_PILL -> tr("Pilule combinée", "Combined pill", pl = "Pigułka złożona", uk = "Комбінована таблетка", es = "Píldora combinada", de = "Kombinationspille")
        COPPER_IUD -> tr("Stérilet cuivre", "Copper IUD", pl = "Wkładka miedziana", uk = "Мідна спіраль", es = "DIU de cobre", de = "Kupferspirale")
        IMPLANT_OR_PROGESTIN -> tr("Implant / Pilule continue", "Implant / Mini-pill", pl = "Implant / Tabletka ciągła", uk = "Імплантат / Безперервна таблетка", es = "Implante / Píldora continua", de = "Implantat / Dauerpille")
        HORMONAL_IUD -> tr("Stérilet hormonal", "Hormonal IUD", pl = "Wkładka hormonalna", uk = "Гормональна спіраль", es = "DIU hormonal", de = "Hormonspirale")
        NATURAL -> tr("Cycle naturel / Préservatif", "Natural cycle / Condoms", pl = "Cykl naturalny / Prezerwatywa", uk = "Природний цикл / Презерватив", es = "Ciclo natural / Preservativo", de = "Natürlicher Zyklus / Kondom")
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ContraceptionGuideScreen(onBack: () -> Unit) {
    var selectedMethod by rememberSaveable { mutableStateOf(ContraceptiveMethod.COMBINED_PILL) }
    val context = rememberPlatformContext()

    Page {
        PageTitle(
            tr("Contraception & règles", "Contraception & periods", pl = "Antykoncepcja i miesiączka", uk = "Контрацепція та менструація", es = "Anticoncepción y regla", de = "Verhütung und Periode"),
            onBack = onBack,
            subtitle = tr("Comprendre vos saignements selon votre méthode.", "Understand your bleeding depending on your method.", pl = "Zrozum swoje krwawienia w zależności od metody.", uk = "Зрозумійте свої кровотечі залежно від вашого методу.", es = "Entiende tus sangrados según tu método.", de = "Deine Blutungen je nach Methode verstehen."),
        )

        LunaCard {
            Text(
                tr("Sélectionnez votre moyen de contraception pour voir son fonctionnement :", "Select your contraceptive method to see how it works:", pl = "Wybierz swoją metodę antykoncepcji, aby zobaczyć, jak działa:", uk = "Оберіть свій спосіб контрацепції, щоб побачити, як він діє:", es = "Selecciona tu método anticonceptivo para ver cómo funciona:", de = "Wähle deine Verhütungsmethode, um zu sehen, wie sie funktioniert:"),
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
        SectionTitle(tr("Votre méthode : ${selectedMethod.label}", "Your method: ${selectedMethod.label}", pl = "Twoja metoda: ${selectedMethod.label}", uk = "Ваш метод: ${selectedMethod.label}", es = "Tu método: ${selectedMethod.label}", de = "Deine Methode: ${selectedMethod.label}"))
        when (selectedMethod) {
            ContraceptiveMethod.COMBINED_PILL -> CombinedPillDetailsCard()
            ContraceptiveMethod.COPPER_IUD -> CopperIudDetailsCard()
            ContraceptiveMethod.IMPLANT_OR_PROGESTIN -> ImplantDetailsCard()
            ContraceptiveMethod.HORMONAL_IUD -> HormonalIudDetailsCard()
            ContraceptiveMethod.NATURAL -> NaturalDetailsCard()
        }

        SectionTitle(tr("Toutes les méthodes en résumé", "All methods overview", pl = "Wszystkie metody w skrócie", uk = "Усі методи коротко", es = "Todos los métodos en resumen", de = "Alle Methoden im Überblick"))
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
            tr("Ce que sont vos saignements : des hémorragies de privation", "What your bleeding is: withdrawal bleeding", pl = "Czym są Twoje krwawienia: krwawienia z odstawienia", uk = "Що таке ваші кровотечі: кровотечі відміни", es = "Qué son tus sangrados: sangrados por deprivación", de = "Was deine Blutungen sind: Abbruchblutungen"),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            tr(
                fr = "Sous pilule combinée (œstrogène + progestatif), vous n'avez pas de véritables règles. La pilule bloque l'ovulation. Les saignements lors de la semaine de pause (ou des comprimés placebo) sont déclenchés artificiellement par l'arrêt temporaire des hormones de synthèse.",
                en = "Under a combined pill, you do not have genuine menstrual periods. The pill stops ovulation. Bleeding during the pill-free week (or the placebo pills) is artificially triggered by the temporary stop of synthetic hormones.",
                pl = "W przypadku złożonej tabletki antykoncepcyjnej (estrogen + gestagen) nie masz prawdziwej miesiączki. Tabletka blokuje owulację. Krwawienia w tygodniu przerwy (lub podczas przyjmowania tabletek placebo) są wywoływane sztucznie przez czasowe odstawienie hormonów syntetycznych.",
                uk = "Під час прийому комбінованої таблетки справжніх менструацій немає. Таблетка блокує овуляцію. Кровотечі в тижневу перерву (або під час прийому плацебо) штучно викликає тимчасова відміна синтетичних гормонів.",
                es = "Con la píldora combinada (estrógeno + progestágeno) no tienes una regla verdadera. La píldora bloquea la ovulación. El sangrado de la semana de descanso (o de las pastillas de placebo) lo provoca artificialmente la interrupción temporal de las hormonas sintéticas.",
                de = "Unter der kombinierten Pille (Östrogen + Gestagen) hast du keine echte Periode. Die Pille verhindert den Eisprung. Die Blutung in der Pillenpause (oder bei den Placebo-Tabletten) wird künstlich durch das vorübergehende Absetzen der synthetischen Hormone ausgelöst.",
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            tr("Enchaîner sans pause :", "Taking packs back-to-back:", pl = "Przyjmowanie bez przerwy:", uk = "Приймання без перерви:", es = "Encadenar sin pausa:", de = "Ohne Pause durchnehmen:"),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            tr(
                fr = "Enchaîner deux plaquettes sans pause de 7 jours supprime tout saignement. C'est médicalement sans danger et validé par les sociétés savantes (OMS, HAS) : le sang ne s'accumule nulle part.",
                en = "Taking two pill packs back-to-back without the 7-day break suppresses bleeding entirely. It is medically safe and endorsed by medical societies (WHO, HAS): blood does not build up anywhere.",
                pl = "Przyjmowanie dwóch opakowań jedno po drugim, bez 7-dniowej przerwy, eliminuje wszelkie krwawienia. Jest to medycznie bezpieczne i zatwierdzone przez towarzystwa naukowe (WHO, HAS): krew nigdzie się nie gromadzi.",
                uk = "Приймання двох блістерів поспіль без 7-денної перерви усуває будь-які кровотечі. Це медично безпечно й підтверджено науковими товариствами (ВООЗ, HAS): кров ніде не накопичується.",
                es = "Encadenar dos blísteres sin la pausa de 7 días suprime cualquier sangrado. Es médicamente seguro y está respaldado por las sociedades científicas (OMS, HAS): la sangre no se acumula en ningún sitio.",
                de = "Zwei Packungen ohne die 7-tägige Pause hintereinander einzunehmen unterdrückt jede Blutung. Das ist medizinisch unbedenklich und von Fachgesellschaften (WHO, HAS) bestätigt: Das Blut sammelt sich nirgendwo an.",
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            tr("Dans Lunaison :", "In Lunaison:", pl = "W Lunaison:", uk = "У Lunaison:", es = "En Lunaison:", de = "In Lunaison:"),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            tr(
                fr = "Vous pouvez noter vos prises quotidiennes et vos saignements. Les prévisions de cycle naturel (ovulation estimée) n'ont pas de valeur sous pilule combinée puisque c'est le calendrier de la plaquette qui fait foi.",
                en = "You can log daily intake and bleeding. Natural cycle forecasts (estimated ovulation) are not biologically applicable under the combined pill as the pack schedule dictates bleeding.",
                pl = "Możesz zapisywać codzienne przyjmowanie tabletek i krwawienia. Prognozy cyklu naturalnego (szacowana owulacja) nie mają wartości przy złożonej tabletce antykoncepcyjnej, ponieważ decyduje harmonogram opakowania.",
                uk = "Ви можете записувати щоденний прийом і кровотечі. Прогнози природного циклу (орієнтовна овуляція) не мають значення при комбінованій таблетці, оскільки вирішальним є графік блістера.",
                es = "Puedes anotar las tomas diarias y los sangrados. Las previsiones del ciclo natural (ovulación estimada) no tienen valor con la píldora combinada, ya que lo que manda es el calendario del blíster.",
                de = "Du kannst deine täglichen Einnahmen und Blutungen eintragen. Die Vorhersagen des natürlichen Zyklus (geschätzter Eisprung) sind unter der kombinierten Pille nicht aussagekräftig, da der Kalender der Packung maßgeblich ist.",
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun CopperIudDetailsCard() {
    LunaCard {
        Text(
            tr("Ce que sont vos saignements : de vraies règles naturelles", "What your bleeding is: genuine natural periods", pl = "Czym są Twoje krwawienia: prawdziwe, naturalne miesiączki", uk = "Що таке ваші кровотечі: справжні природні менструації", es = "Qué son tus sangrados: reglas naturales de verdad", de = "Was deine Blutungen sind: echte, natürliche Perioden"),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            tr(
                fr = "Le stérilet au cuivre (DIU cuivre) est 100 % sans hormone. Votre cycle biologique naturel, vos variations hormonales et votre ovulation se poursuivent exactement comme d'habitude.",
                en = "The copper IUD is 100% hormone-free. Your natural cycle, hormonal variations and ovulation proceed normally.",
                pl = "Miedziana wkładka domaciczna jest w 100 % wolna od hormonów. Twój naturalny cykl biologiczny, wahania hormonalne i owulacja przebiegają dokładnie tak jak zwykle.",
                uk = "Мідна спіраль (мідна ВМС) на 100 % не містить гормонів. Ваш природний біологічний цикл, гормональні коливання та овуляція тривають так само, як і раніше.",
                es = "El DIU de cobre es 100 % libre de hormonas. Tu ciclo biológico natural, tus variaciones hormonales y tu ovulación continúan exactamente como siempre.",
                de = "Die Kupferspirale ist zu 100 % hormonfrei. Dein natürlicher biologischer Zyklus, deine hormonellen Schwankungen und dein Eisprung laufen genau wie gewohnt weiter.",
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            tr("L'inflammation bénigne et les flux abondants :", "Benign inflammation and heavy bleeding:", pl = "Łagodny stan zapalny i obfite krwawienia:", uk = "Доброякісне запалення та рясні виділення:", es = "La inflamación benigna y los flujos abundantes:", de = "Die gutartige Entzündung und starke Blutungen:"),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            tr(
                fr = "Le cuivre diffuse de petites quantités d'ions cuivre qui provoquent une réaction inflammatoire locale bénigne de l'endomètre (spermicide). C'est pour cela que les règles sont souvent plus abondantes, plus longues et plus douloureuses les 3 à 6 premiers mois.",
                en = "Copper releases ions causing a localized, benign inflammatory response in the endometrium (spermicidal). This is why periods are frequently heavier, longer and more crampy during the first 3 to 6 months.",
                pl = "Miedź uwalnia niewielkie ilości jonów miedzi, które wywołują łagodną miejscową reakcję zapalną endometrium (działanie plemnikobójcze). Dlatego miesiączki są często obfitsze, dłuższe i bardziej bolesne przez pierwsze 3 do 6 miesięcy.",
                uk = "Мідь вивільняє невеликі кількості іонів міді, що спричиняють доброякісну локальну запальну реакцію ендометрія (сперміцидна дія). Саме тому менструації часто бувають ряснішими, довшими та болючішими в перші 3-6 місяців.",
                es = "El cobre libera pequeñas cantidades de iones de cobre que provocan una reacción inflamatoria local benigna del endometrio (espermicida). Por eso las reglas suelen ser más abundantes, más largas y más dolorosas durante los 3 a 6 primeros meses.",
                de = "Das Kupfer gibt kleine Mengen Kupferionen ab, die eine lokale, gutartige Entzündungsreaktion der Gebärmutterschleimhaut hervorrufen (spermienabtötend). Deshalb sind die Perioden in den ersten 3 bis 6 Monaten oft stärker, länger und schmerzhafter.",
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            tr("Vigilance fer et anémie :", "Iron monitoring and fatigue:", pl = "Czujność w sprawie żelaza i niedokrwistości:", uk = "Увага до заліза та анемії:", es = "Vigilancia del hierro y la anemia:", de = "Eisenmangel und Blutarmut im Blick:"),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            tr(
                fr = "Des règles très abondantes entraînent une perte de fer. Privilégiez les aliments riches en fer et parlez-en à un soignant pour un bilan sanguin (ferritine) si vous ressentez une grande fatigue ou des vertiges.",
                en = "Heavier periods increase iron loss. Favor iron-rich foods and ask your clinician for a ferritin blood test if you experience unusual fatigue or dizziness.",
                pl = "Bardzo obfite miesiączki powodują utratę żelaza. Wybieraj produkty bogate w żelazo i porozmawiaj z osobą z personelu medycznego o badaniu krwi (ferrytyna), jeśli odczuwasz duże zmęczenie lub zawroty głowy.",
                uk = "Дуже рясні менструації призводять до втрати заліза. Віддавайте перевагу продуктам, багатим на залізо, і поговоріть із медичним працівником про аналіз крові (феритин), якщо відчуваєте сильну втому або запаморочення.",
                es = "Las reglas muy abundantes provocan una pérdida de hierro. Da preferencia a los alimentos ricos en hierro y habla con un profesional sanitario para hacerte un análisis de sangre (ferritina) si sientes mucho cansancio o mareos.",
                de = "Sehr starke Perioden führen zu Eisenverlust. Bevorzuge eisenreiche Lebensmittel und sprich mit medizinischem Fachpersonal über eine Blutuntersuchung (Ferritin), wenn du dich sehr müde fühlst oder dir schwindlig ist.",
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ImplantDetailsCard() {
    LunaCard {
        Text(
            tr("Ce que sont vos saignements : un endomètre au repos complet", "What your bleeding is: resting endometrium", pl = "Czym są Twoje krwawienia: endometrium w całkowitym spoczynku", uk = "Що таке ваші кровотечі: ендометрій у повному спокої", es = "Qué son tus sangrados: un endometrio en reposo completo", de = "Was deine Blutungen sind: eine Gebärmutterschleimhaut in völliger Ruhe"),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            tr(
                fr = "L'implant et la pilule progestative continue (ex. désogestrel) diffusent un progestatif en continu, sans aucune interruption. L'ovulation est le plus souvent bloquée et la muqueuse utérine reste très fine.",
                en = "The implant and progestin-only pill continuously release progestin without breaks. Ovulation is mostly suppressed and the uterine lining remains very thin.",
                pl = "Implant i ciągła tabletka gestagenowa (np. dezogestrel) uwalniają gestagen w sposób ciągły, bez żadnej przerwy. Owulacja jest najczęściej blokowana, a błona śluzowa macicy pozostaje bardzo cienka.",
                uk = "Імплант і безперервна прогестинова таблетка (напр. дезогестрел) постійно вивільняють прогестаген без жодних перерв. Овуляція здебільшого блокується, а слизова оболонка матки залишається дуже тонкою.",
                es = "El implante y la píldora de progestágeno continua (p. ej., desogestrel) liberan un progestágeno de forma continua, sin ninguna interrupción. La ovulación suele quedar bloqueada y la mucosa uterina se mantiene muy fina.",
                de = "Das Implantat und die durchgehend eingenommene Gestagen-Pille (z. B. Desogestrel) geben ohne jede Unterbrechung kontinuierlich ein Gestagen ab. Der Eisprung wird meist unterdrückt, und die Gebärmutterschleimhaut bleibt sehr dünn.",
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            tr("Disparition des règles ou saignements irréguliers :", "No periods or irregular spotting:", pl = "Brak miesiączek lub nieregularne krwawienia:", uk = "Зникнення менструацій або нерегулярні кровотечі:", es = "Desaparición de la regla o sangrados irregulares:", de = "Ausbleibende Periode oder unregelmäßige Blutungen:"),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            tr(
                fr = "Environ une femme sur trois n'a plus du tout de saignements (aménorrhée). C'est totalement normal et sans danger pour la fertilité future. D'autres personnes connaissent de petits saignements légers et anarchiques (spotting).",
                en = "About one in three women stops having periods entirely. This is completely normal and safe for future fertility. Others experience light, irregular spotting.",
                pl = "Około co trzecia kobieta nie ma już żadnych krwawień (brak miesiączki, amenorrhea). To całkowicie normalne i bezpieczne dla przyszłej płodności. Inne osoby mają niewielkie, nieregularne krwawienia (plamienia).",
                uk = "Приблизно в кожної третьої жінки кровотеч більше немає зовсім (аменорея). Це абсолютно нормально та безпечно для майбутньої фертильності. В інших бувають невеликі нерегулярні кров'янисті виділення (спотинг).",
                es = "Aproximadamente una de cada tres mujeres deja de tener sangrados por completo (amenorrea). Es totalmente normal y no supone ningún riesgo para la fertilidad futura. Otras personas tienen pequeños sangrados ligeros e irregulares (manchado).",
                de = "Etwa jede dritte Frau hat gar keine Blutungen mehr (Amenorrhoe). Das ist völlig normal und für die spätere Fruchtbarkeit unbedenklich. Andere haben leichte, unregelmäßige Schmierblutungen (Spotting).",
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            tr("Dans Lunaison :", "In Lunaison:", pl = "W Lunaison:", uk = "У Lunaison:", es = "En Lunaison:", de = "In Lunaison:"),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            tr(
                fr = "Le mode « Mettre les prévisions en pause » dans les réglages est idéal si vos règles ont disparu, pour continuer à noter vos symptômes sans alertes de retard inutiles.",
                en = "The \"Pause predictions\" mode in settings is ideal if your periods have stopped, allowing you to log symptoms without unneeded late period alerts.",
                pl = "Tryb „Wstrzymaj prognozy” w ustawieniach jest idealny, jeśli miesiączki ustały, aby nadal zapisywać objawy bez niepotrzebnych alertów o opóźnieniu.",
                uk = "Режим «Призупинити прогнози» в налаштуваннях ідеально підходить, якщо менструації зникли: ви продовжуєте записувати симптоми без зайвих сповіщень про затримку.",
                es = "El modo «Pausar las previsiones» de los ajustes es ideal si tu regla ha desaparecido, para seguir anotando tus síntomas sin alertas de retraso innecesarias.",
                de = "Der Modus „Vorhersagen pausieren“ in den Einstellungen ist ideal, wenn deine Periode ausgeblieben ist: So kannst du weiter Symptome eintragen, ohne unnötige Verspätungswarnungen.",
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun HormonalIudDetailsCard() {
    LunaCard {
        Text(
            tr("Ce que sont vos saignements : des flux fortement diminués", "What your bleeding is: significantly reduced bleeding", pl = "Czym są Twoje krwawienia: znacznie zmniejszone krwawienia", uk = "Що таке ваші кровотечі: значно зменшені виділення", es = "Qué son tus sangrados: flujos muy disminuidos", de = "Was deine Blutungen sind: stark verminderte Blutungen"),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            tr(
                fr = "Le stérilet hormonal (DIU au lévonorgestrel) agit localement dans l'utérus. L'endomètre ne s'épaissit quasiment plus chaque mois. Les règles deviennent très légères, courtes, et disparaissent souvent complètement après 6 à 12 mois.",
                en = "The hormonal IUD acts locally inside the uterus. The endometrium barely thickens. Periods become very light, short, and frequently disappear entirely after 6 to 12 months.",
                pl = "Hormonalna wkładka domaciczna (z lewonorgestrelem) działa miejscowo w macicy. Endometrium prawie już nie pogrubia się co miesiąc. Miesiączki stają się bardzo skąpe i krótkie, a często zanikają całkowicie po 6 do 12 miesiącach.",
                uk = "Гормональна спіраль (ВМС із левоноргестрелом) діє локально в матці. Ендометрій майже перестає потовщуватися щомісяця. Менструації стають дуже скупими й короткими та часто повністю зникають через 6-12 місяців.",
                es = "El DIU hormonal (con levonorgestrel) actúa localmente en el útero. El endometrio casi deja de engrosarse cada mes. Las reglas se vuelven muy ligeras y cortas, y con frecuencia desaparecen por completo tras 6 a 12 meses.",
                de = "Die Hormonspirale (mit Levonorgestrel) wirkt lokal in der Gebärmutter. Die Gebärmutterschleimhaut baut sich kaum noch jeden Monat auf. Die Perioden werden sehr schwach und kurz und bleiben nach 6 bis 12 Monaten oft ganz aus.",
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            tr("Les premiers mois :", "The first few months:", pl = "Pierwsze miesiące:", uk = "Перші місяці:", es = "Los primeros meses:", de = "Die ersten Monate:"),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            tr(
                fr = "Des petits saignements de repérage (spotting) fréquents sont courants au cours des premières semaines, le temps que la muqueuse s'adapte à l'action locale du progestatif.",
                en = "Frequent light spotting is common in the first weeks while the uterine lining adjusts to the localized progestin.",
                pl = "W pierwszych tygodniach często występują niewielkie plamienia (spotting), gdy błona śluzowa dostosowuje się do miejscowego działania gestagenu.",
                uk = "У перші тижні часто бувають невеликі кров'янисті виділення (спотинг), поки слизова оболонка пристосовується до місцевої дії прогестагену.",
                es = "Es habitual tener pequeños sangrados frecuentes (manchado) durante las primeras semanas, mientras la mucosa se adapta a la acción local del progestágeno.",
                de = "In den ersten Wochen sind häufige leichte Schmierblutungen (Spotting) üblich, bis sich die Schleimhaut an die lokale Wirkung des Gestagens angepasst hat.",
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun NaturalDetailsCard() {
    LunaCard {
        Text(
            tr("Ce que sont vos saignements : un cycle naturel et spontané", "What your bleeding is: a spontaneous natural cycle", pl = "Czym są Twoje krwawienia: spontaniczny cykl naturalny", uk = "Що таке ваші кровотечі: природний спонтанний цикл", es = "Qué son tus sangrados: un ciclo natural y espontáneo", de = "Was deine Blutungen sind: ein natürlicher, spontaner Zyklus"),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            tr(
                fr = "Sans contraception hormonale ni DIU (préservatifs, méthode barrière, symptothermie ou pas de contraception), votre cycle est gouverné par vos propres hormones. Les règles surviennent environ 14 jours après une ovulation naturelle.",
                en = "Without hormonal contraception or an IUD (condoms, barrier methods, symptothermy, or no contraception), your cycle is governed by your own hormones. Periods arrive ~14 days after natural ovulation.",
                pl = "Bez antykoncepcji hormonalnej ani wkładki domacicznej (prezerwatywy, metody barierowe, metoda objawowo-termiczna lub brak antykoncepcji) Twój cykl jest sterowany przez Twoje własne hormony. Miesiączka pojawia się około 14 dni po naturalnej owulacji.",
                uk = "Без гормональної контрацепції та ВМС (презервативи, бар'єрні методи, симптотермальний метод або відсутність контрацепції) ваш цикл керується вашими власними гормонами. Менструація настає приблизно через 14 днів після природної овуляції.",
                es = "Sin anticoncepción hormonal ni DIU (preservativos, método de barrera, sintotérmico o sin anticoncepción), tu ciclo está gobernado por tus propias hormonas. La regla llega unos 14 días después de una ovulación natural.",
                de = "Ohne hormonelle Verhütung und ohne Spirale (Kondome, Barrieremethoden, symptothermale Methode oder keine Verhütung) wird dein Zyklus von deinen eigenen Hormonen gesteuert. Die Periode setzt etwa 14 Tage nach einem natürlichen Eisprung ein.",
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            tr("Dans Lunaison :", "In Lunaison:", pl = "W Lunaison:", uk = "У Lunaison:", es = "En Lunaison:", de = "In Lunaison:"),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            tr(
                fr = "Les calculs de durée habituelle, la fenêtre fertile estimée, les analyses de phases et le suivi de la glaire cervicale ou de la température s'appliquent pleinement à votre profil.",
                en = "Usual duration calculations, estimated fertile windows, phase patterns and mucus/temperature tracking fully apply to your biological profile.",
                pl = "Obliczenia typowej długości, szacowane dni płodne, analizy faz oraz śledzenie śluzu szyjkowego lub temperatury w pełni dotyczą Twojego profilu.",
                uk = "Розрахунки звичної тривалості, орієнтовне фертильне вікно, аналіз фаз і відстеження цервікального слизу чи температури повною мірою стосуються вашого профілю.",
                es = "Los cálculos de duración habitual, la ventana fértil estimada, los análisis de fases y el seguimiento del moco cervical o de la temperatura se aplican plenamente a tu perfil.",
                de = "Die Berechnung der üblichen Dauer, das geschätzte fruchtbare Fenster, die Phasenanalysen und die Beobachtung von Zervixschleim oder Temperatur gelten uneingeschränkt für dein Profil.",
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ContraceptionComparisonCard() {
    LunaCard {
        ComparisonRow(
            title = tr("Pilule combinée", "Combined pill", pl = "Złożona tabletka antykoncepcyjna", uk = "Комбінована таблетка", es = "Píldora combinada", de = "Kombinierte Pille"),
            summary = tr("Hémorragie de privation (pas de vraie règle). Pas d'ovulation. Possibilité d'enchaîner.", "Withdrawal bleeding (not real period). No ovulation. Can take back-to-back.", pl = "Krwawienie z odstawienia (nie prawdziwa miesiączka). Brak owulacji. Można przyjmować bez przerwy.", uk = "Кровотеча відміни (не справжня менструація). Овуляції немає. Можна приймати без перерви.", es = "Sangrado por deprivación (no es una regla verdadera). Sin ovulación. Se puede encadenar.", de = "Abbruchblutung (keine echte Periode). Kein Eisprung. Durchnehmen ist möglich."),
        )
        ComparisonRow(
            title = tr("Stérilet cuivre", "Copper IUD", pl = "Miedziana wkładka", uk = "Мідна спіраль", es = "DIU de cobre", de = "Kupferspirale"),
            summary = tr("Vraies règles naturelles. Règles souvent plus abondantes et crampes au début (inflammation locale bénigne). Surveiller le fer.", "Natural periods. Frequently heavier flow & cramps initially (benign local inflammation). Watch iron.", pl = "Prawdziwe, naturalne miesiączki. Na początku miesiączki często obfitsze i skurcze (łagodny miejscowy stan zapalny). Obserwować poziom żelaza.", uk = "Справжні природні менструації. На початку менструації часто рясніші та зі спазмами (доброякісне локальне запалення). Стежити за залізом.", es = "Reglas naturales de verdad. Reglas a menudo más abundantes y calambres al principio (inflamación local benigna). Vigilar el hierro.", de = "Echte, natürliche Perioden. Anfangs oft stärkere Perioden und Krämpfe (gutartige lokale Entzündung). Auf das Eisen achten."),
        )
        ComparisonRow(
            title = tr("Implant / Pilule continue", "Implant / Mini-pill", pl = "Implant / Tabletka ciągła", uk = "Імплант / Безперервна таблетка", es = "Implante / Píldora continua", de = "Implantat / Minipille"),
            summary = tr("Endomètre au repos. Règles souvent absentes (aménorrhée sans danger) ou spotting irrégulier.", "Resting endometrium. Often no periods (safe amenorrhea) or irregular spotting.", pl = "Endometrium w spoczynku. Miesiączki często nie występują (niegroźny brak miesiączki) lub występuje nieregularne plamienie.", uk = "Ендометрій у спокої. Менструацій часто немає (безпечна аменорея) або є нерегулярний спотинг.", es = "Endometrio en reposo. A menudo no hay regla (amenorrea sin peligro) o hay manchado irregular.", de = "Gebärmutterschleimhaut in Ruhe. Periode oft ausbleibend (harmlose Amenorrhoe) oder unregelmäßige Schmierblutungen."),
        )
        ComparisonRow(
            title = tr("Stérilet hormonal", "Hormonal IUD", pl = "Hormonalna wkładka", uk = "Гормональна спіраль", es = "DIU hormonal", de = "Hormonspirale"),
            summary = tr("Action locale. Diminution drastique des règles, disparition fréquente au bout d'un an.", "Local action. Drastic flow reduction, often complete cessation after one year.", pl = "Działanie miejscowe. Drastyczne zmniejszenie miesiączek, często całkowity zanik po roku.", uk = "Локальна дія. Різке зменшення менструацій, часто повне зникнення за рік.", es = "Acción local. Disminución drástica de las reglas, desaparición frecuente al cabo de un año.", de = "Lokale Wirkung. Drastische Abnahme der Periode, nach etwa einem Jahr häufig ganz ausbleibend."),
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
        SectionTitle(tr("Schéma des 4 temps du cycle", "The 4 cycle phases diagram", pl = "Schemat 4 etapów cyklu", uk = "Схема 4 етапів циклу", es = "Esquema de los 4 tiempos del ciclo", de = "Schema der 4 Zyklusphasen"))
        LunaCard {
            Text(
                tr("Le cycle menstruel en 4 étapes clés :", "The menstrual cycle in 4 key stages:", pl = "Cykl miesiączkowy w 4 kluczowych etapach:", uk = "Менструальний цикл у 4 ключових етапи:", es = "El ciclo menstrual en 4 etapas clave:", de = "Der Menstruationszyklus in 4 Schlüsselphasen:"),
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
                phase = tr("1. Les règles (J1 à J5)", "1. Period (Days 1 to 5)", pl = "1. Miesiączka (dni 1 do 5)", uk = "1. Менструація (дні 1-5)", es = "1. La regla (días 1 a 5)", de = "1. Die Periode (Tage 1 bis 5)"),
                detail = tr("La muqueuse intérieure de l'utérus (l'endomètre) se détache et s'élimine. Les taux d'hormones sont au plus bas.", "The inner uterine lining (endometrium) sheds. Hormone levels are at their lowest.", pl = "Wewnętrzna błona śluzowa macicy (endometrium) się złuszcza i jest usuwana. Poziom hormonów jest najniższy.", uk = "Внутрішня слизова оболонка матки (ендометрій) відшаровується та виводиться. Рівень гормонів найнижчий.", es = "La mucosa interior del útero (el endometrio) se desprende y se elimina. Los niveles hormonales están en su punto más bajo.", de = "Die innere Schleimhaut der Gebärmutter (das Endometrium) löst sich ab und wird ausgeschieden. Die Hormonspiegel sind am niedrigsten."),
            )
            PhaseExplainerRow(
                color = Color(0xFFB4D6C4),
                phase = tr("2. Phase folliculaire (J6 à J13)", "2. Follicular phase (Days 6 to 13)", pl = "2. Faza folikularna (dni 6 do 13)", uk = "2. Фолікулярна фаза (дні 6-13)", es = "2. Fase folicular (días 6 a 13)", de = "2. Follikelphase (Tage 6 bis 13)"),
                detail = tr("Un follicule mûrit dans l'ovaire. Les œstrogènes montent pour reconstruire la muqueuse et préparer une éventuelle fécondation.", "An ovarian follicle matures. Estrogens rise to rebuild the uterine lining.", pl = "W jajniku dojrzewa pęcherzyk. Estrogeny rosną, aby odbudować błonę śluzową i przygotować ją na ewentualne zapłodnienie.", uk = "У яєчнику дозріває фолікул. Естрогени зростають, щоб відновити слизову оболонку та підготувати можливе запліднення.", es = "Un folículo madura en el ovario. Los estrógenos suben para reconstruir la mucosa y preparar una posible fecundación.", de = "Ein Follikel reift im Eierstock heran. Das Östrogen steigt an, um die Schleimhaut wieder aufzubauen und eine mögliche Befruchtung vorzubereiten."),
            )
            PhaseExplainerRow(
                color = Color(0xFFF9D162),
                phase = tr("3. L'ovulation (~ J14)", "3. Ovulation (~ Day 14)", pl = "3. Owulacja (~ dzień 14)", uk = "3. Овуляція (~ 14-й день)", es = "3. La ovulación (~ día 14)", de = "3. Der Eisprung (~ Tag 14)"),
                detail = tr("L'ovaire libère l'ovule mûr. Il ne survit qu'entre 12 et 24 heures.", "The ovary releases a mature egg. It survives for 12 to 24 hours.", pl = "Jajnik uwalnia dojrzałą komórkę jajową. Przeżywa tylko od 12 do 24 godzin.", uk = "Яєчник вивільняє зрілу яйцеклітину. Вона живе лише від 12 до 24 годин.", es = "El ovario libera el óvulo maduro. Solo sobrevive entre 12 y 24 horas.", de = "Der Eierstock gibt die reife Eizelle frei. Sie überlebt nur 12 bis 24 Stunden."),
            )
            PhaseExplainerRow(
                color = Color(0xFFD3BBD9),
                phase = tr("4. Phase lutéale (J15 à J28)", "4. Luteal phase (Days 15 to 28)", pl = "4. Faza lutealna (dni 15 do 28)", uk = "4. Лютеїнова фаза (дні 15-28)", es = "4. Fase lútea (días 15 a 28)", de = "4. Lutealphase (Tage 15 bis 28)"),
                detail = tr("La progestérone domine pour maintenir la muqueuse. Sans grossesse, elle s'effondre et déclenche les règles suivantes.", "Progesterone dominates to maintain the lining. Without pregnancy, it drops, triggering the next period.", pl = "Dominuje progesteron, który podtrzymuje błonę śluzową. Bez ciąży jego poziom gwałtownie spada i wywołuje kolejną miesiączkę.", uk = "Переважає прогестерон, що підтримує слизову оболонку. Без вагітності його рівень різко падає й запускає наступну менструацію.", es = "La progesterona predomina para mantener la mucosa. Sin embarazo, se desploma y desencadena la regla siguiente.", de = "Progesteron dominiert, um die Schleimhaut zu erhalten. Ohne Schwangerschaft fällt es stark ab und löst die nächste Periode aus."),
            )
        }

        HormoneCurvesDiagram()

        VulvaDiagram()

        InteractiveUterusDiagram()

        SectionTitle(tr("L'anatomie interne sans tabou", "Internal anatomy explained", pl = "Anatomia wewnętrzna bez tabu", uk = "Внутрішня анатомія без табу", es = "La anatomía interna sin tabúes", de = "Die innere Anatomie ohne Tabus"))
        LunaCard {
            Text(
                tr("Les repères clés du corps féminin :", "Key body reference points:", pl = "Kluczowe punkty orientacyjne kobiecego ciała:", uk = "Ключові орієнтири жіночого тіла:", es = "Las referencias clave del cuerpo femenino:", de = "Die wichtigsten Orientierungspunkte des weiblichen Körpers:"),
                style = MaterialTheme.typography.titleSmall,
            )
            AnatomyItemRow(
                title = tr("La vulve (externe)", "The vulva (external)", pl = "Srom (zewnętrzny)", uk = "Вульва (зовнішня)", es = "La vulva (externa)", de = "Die Vulva (außen)"),
                description = tr("Tous les organes visibles à l'extérieur : lèvres, clitoris, méat urinaire et entrée du vagin. Souvent confondue avec le vagin !", "All visible external parts: labia, clitoris, urethra and vaginal opening. Often confused with the vagina!", pl = "Wszystkie narządy widoczne na zewnątrz: wargi, łechtaczka, ujście cewki moczowej i wejście do pochwy. Często mylony z pochwą!", uk = "Усі органи, видимі ззовні: губи, клітор, отвір сечівника та вхід до піхви. Її часто плутають із піхвою!", es = "Todos los órganos visibles por fuera: labios, clítoris, meato urinario y entrada de la vagina. ¡A menudo se confunde con la vagina!", de = "Alle außen sichtbaren Organe: Schamlippen, Klitoris, Harnröhrenöffnung und Scheideneingang. Wird oft mit der Vagina verwechselt!"),
            )
            AnatomyItemRow(
                title = tr("Le vagin (interne)", "The vagina (internal)", pl = "Pochwa (wewnętrzna)", uk = "Піхва (внутрішня)", es = "La vagina (interna)", de = "Die Vagina (innen)"),
                description = tr("Le conduit musculaire souple reliant la vulve au col de l'utérus. C'est par lui que s'écoulent les règles.", "The flexible muscular canal connecting the vulva to the cervix. It is where menstrual blood flows.", pl = "Elastyczny kanał mięśniowy łączący srom z szyjką macicy. To przez niego wypływa krew menstruacyjna.", uk = "Гнучкий м'язовий канал, що з'єднує вульву з шийкою матки. Саме ним виходить менструальна кров.", es = "El conducto muscular flexible que une la vulva con el cuello del útero. Por él sale la sangre de la regla.", de = "Der flexible Muskelschlauch, der die Vulva mit dem Gebärmutterhals verbindet. Durch ihn fließt die Periodenblutung ab."),
            )
            AnatomyItemRow(
                title = tr("Le col de l'utérus (cervix)", "The cervix", pl = "Szyjka macicy (cervix)", uk = "Шийка матки (цервікс)", es = "El cuello del útero (cérvix)", de = "Der Gebärmutterhals (Zervix)"),
                description = tr("La partie basse de l'utérus. Il produit la glaire cervicale qui change au fil du mois et s'entrouvre légèrement pendant les règles.", "The lower part of the uterus. It secretes cervical mucus that changes texture throughout the month and opens slightly during periods.", pl = "Dolna część macicy. Wytwarza śluz szyjkowy, który zmienia się w ciągu miesiąca, i lekko się otwiera podczas miesiączki.", uk = "Нижня частина матки. Він виробляє цервікальний слиз, що змінюється впродовж місяця, і трохи розкривається під час менструації.", es = "La parte baja del útero. Produce el moco cervical, que cambia a lo largo del mes, y se entreabre ligeramente durante la regla.", de = "Der untere Teil der Gebärmutter. Er bildet den Zervixschleim, der sich im Lauf des Monats verändert, und öffnet sich während der Periode leicht."),
            )
            AnatomyItemRow(
                title = tr("L'utérus et le myomètre (muscle)", "The uterus & myometrium (muscle)", pl = "Macica i miometrium (mięsień)", uk = "Матка та міометрій (м'яз)", es = "El útero y el miometrio (músculo)", de = "Die Gebärmutter und das Myometrium (Muskel)"),
                description = tr("Un muscle creux puissant de la taille d'une petite poire. Sa paroi musculaire (le myomètre) se contracte lors des règles sous l'effet des prostaglandines.", "A powerful hollow pear-sized muscle. Its muscular wall (myometrium) contracts during periods under the effect of prostaglandins.", pl = "Silny, wydrążony mięsień wielkości małej gruszki. Jego ściana mięśniowa (miometrium) kurczy się podczas miesiączki pod wpływem prostaglandyn.", uk = "Потужний порожнистий м'яз завбільшки з невелику грушу. Його м'язова стінка (міометрій) скорочується під час менструації під дією простагландинів.", es = "Un músculo hueco y potente del tamaño de una pera pequeña. Su pared muscular (el miometrio) se contrae durante la regla por efecto de las prostaglandinas.", de = "Ein kräftiger Hohlmuskel von der Größe einer kleinen Birne. Seine Muskelwand (das Myometrium) zieht sich während der Periode unter dem Einfluss der Prostaglandine zusammen."),
            )
            AnatomyItemRow(
                title = tr("L'endomètre (muqueuse interne)", "The endometrium (inner lining)", pl = "Endometrium (błona śluzowa wewnętrzna)", uk = "Ендометрій (внутрішня слизова оболонка)", es = "El endometrio (mucosa interna)", de = "Das Endometrium (innere Schleimhaut)"),
                description = tr("La muqueuse vivante qui tapisse l'intérieur de l'utérus. Elle s'épaissit chaque mois, s'enrichit en vaisseaux, puis desquame si aucune grossesse ne débute : ce sont les règles.", "The living tissue lining the inside of the uterus. It thickens each month, vascularizes, then sheds if no pregnancy occurs: this is menstruation.", pl = "Żywa błona śluzowa wyściełająca wnętrze macicy. Co miesiąc pogrubia się, wzbogaca w naczynia krwionośne, a następnie złuszcza się, jeśli nie rozpocznie się ciąża: to właśnie miesiączka.", uk = "Жива слизова оболонка, що вистилає внутрішню поверхню матки. Щомісяця вона потовщується, збагачується судинами, а потім відшаровується, якщо вагітність не настала: це і є менструація.", es = "La mucosa viva que reviste el interior del útero. Cada mes se engrosa, se enriquece de vasos y luego se descama si no empieza un embarazo: es la regla.", de = "Die lebendige Schleimhaut, die die Gebärmutter innen auskleidet. Sie baut sich jeden Monat auf, bekommt mehr Gefäße und wird dann abgestoßen, wenn keine Schwangerschaft beginnt: das ist die Periode."),
            )
            AnatomyItemRow(
                title = tr("Les trompes de Fallope", "The fallopian tubes", pl = "Jajowody", uk = "Маткові труби", es = "Las trompas de Falopio", de = "Die Eileiter"),
                description = tr("Deux conduits fins reliant les ovaires à l'utérus, où a lieu la fécondation éventuelle d'un ovocyte.", "Two slender ducts connecting ovaries to uterus, where potential egg fertilization takes place.", pl = "Dwa cienkie przewody łączące jajniki z macicą, w których może dojść do zapłodnienia komórki jajowej.", uk = "Дві тонкі труби, що з'єднують яєчники з маткою, де може відбутися запліднення яйцеклітини.", es = "Dos conductos finos que unen los ovarios con el útero, donde puede producirse la fecundación de un óvulo.", de = "Zwei dünne Gänge, die die Eierstöcke mit der Gebärmutter verbinden und in denen eine Eizelle befruchtet werden kann."),
            )
            AnatomyItemRow(
                title = tr("Les ovaires (maturation & hormones)", "The ovaries (maturation & hormones)", pl = "Jajniki (dojrzewanie i hormony)", uk = "Яєчники (дозрівання та гормони)", es = "Los ovarios (maduración y hormonas)", de = "Die Eierstöcke (Reifung und Hormone)"),
                description = tr("Deux glandes qui abritent les ovocytes dès la naissance et fabriquent les œstrogènes et la progestérone au fil du cycle.", "Two glands containing egg cells from birth and producing estrogen and progesterone throughout the cycle.", pl = "Dwa gruczoły, które od urodzenia zawierają komórki jajowe i wytwarzają estrogeny oraz progesteron w ciągu cyklu.", uk = "Дві залози, що з народження містять яйцеклітини та виробляють естрогени й прогестерон протягом циклу.", es = "Dos glándulas que albergan los óvulos desde el nacimiento y fabrican los estrógenos y la progesterona a lo largo del ciclo.", de = "Zwei Drüsen, die seit der Geburt die Eizellen enthalten und im Lauf des Zyklus Östrogen und Progesteron bilden."),
            )
        }

        SectionTitle(tr("Signes visibles : la glaire cervicale", "Visible signs: cervical mucus", pl = "Widoczne oznaki: śluz szyjkowy", uk = "Видимі ознаки: цервікальний слиз", es = "Signos visibles: el moco cervical", de = "Sichtbare Zeichen: der Zervixschleim"))
        LunaCard {
            Text(
                tr("Comment la glaire change au fil du mois :", "How cervical mucus changes during the cycle:", pl = "Jak śluz zmienia się w ciągu miesiąca:", uk = "Як слиз змінюється впродовж місяця:", es = "Cómo cambia el moco a lo largo del mes:", de = "So verändert sich der Zervixschleim im Lauf des Monats:"),
                style = MaterialTheme.typography.titleSmall,
            )
            MucusStageRow(
                stage = tr("Après les règles", "After period", pl = "Po miesiączce", uk = "Після менструації", es = "Después de la regla", de = "Nach der Periode"),
                texture = tr("Sensation plutôt sèche, peu ou pas de glaire visible.", "Rather dry feeling, little to no visible mucus.", pl = "Raczej sucho, mało śluzu lub brak widocznego śluzu.", uk = "Відчуття радше сухості, слизу мало або його не видно.", es = "Sensación más bien seca, poco o nada de moco visible.", de = "Eher trockenes Gefühl, wenig oder kein sichtbarer Schleim."),
            )
            MucusStageRow(
                stage = tr("Avant l'ovulation", "Before ovulation", pl = "Przed owulacją", uk = "Перед овуляцією", es = "Antes de la ovulación", de = "Vor dem Eisprung"),
                texture = tr("Glaire crémeuse, blanchâtre ou collante.", "Creamy, whitish or sticky mucus.", pl = "Śluz kremowy, białawy lub lepki.", uk = "Слиз кремоподібний, білуватий або липкий.", es = "Moco cremoso, blanquecino o pegajoso.", de = "Cremiger, weißlicher oder klebriger Schleim."),
            )
            MucusStageRow(
                stage = tr("Période fertile & ovulation", "Fertile window & ovulation", pl = "Okres płodny i owulacja", uk = "Фертильний період і овуляція", es = "Periodo fértil y ovulación", de = "Fruchtbare Zeit und Eisprung"),
                texture = tr("Glaire abondante, transparente et étirable entre deux doigts (comme du blanc d'œuf cru). Elle aide les spermatozoïdes.", "Abundant, clear and stretchy between two fingers (like raw egg white). It helps sperm.", pl = "Śluz obfity, przezroczysty i ciągnący się między dwoma palcami (jak surowe białko jaja). Pomaga plemnikom.", uk = "Слиз рясний, прозорий і тягучий між двома пальцями (як сирий яєчний білок). Він допомагає сперматозоїдам.", es = "Moco abundante, transparente y elástico entre dos dedos (como clara de huevo cruda). Ayuda a los espermatozoides.", de = "Reichlicher, durchsichtiger Schleim, der sich zwischen zwei Fingern ziehen lässt (wie rohes Eiweiß). Er unterstützt die Spermien."),
            )
            MucusStageRow(
                stage = tr("Après l'ovulation", "After ovulation", pl = "Po owulacji", uk = "Після овуляції", es = "Después de la ovulación", de = "Nach dem Eisprung"),
                texture = tr("Retour rapide à une glaire opaque, pâteuse ou sensation sèche.", "Quick return to opaque, pasty mucus or dry feeling.", pl = "Szybki powrót do śluzu nieprzezroczystego, gęstego lub uczucia suchości.", uk = "Швидке повернення до непрозорого, пастоподібного слизу або відчуття сухості.", es = "Vuelta rápida a un moco opaco, pastoso o a una sensación seca.", de = "Schnelle Rückkehr zu undurchsichtigem, pastösem Schleim oder zu einem trockenen Gefühl."),
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
fun HormoneCurvesDiagram() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionTitle(tr("La courbe des hormones (cycle de 28 jours)", "Hormone level curves (28-day cycle)", pl = "Krzywe hormonów (cykl 28-dniowy)", uk = "Криві гормонів (28-денний цикл)", es = "La curva de las hormonas (ciclo de 28 días)", de = "Die Hormonkurven (28-Tage-Zyklus)"))
        LunaCard {
            Text(
                tr("Évolution des 3 hormones clés sur un cycle naturel :", "Evolution of the 3 key hormones over a natural cycle:", pl = "Zmiany 3 kluczowych hormonów w cyklu naturalnym:", uk = "Зміна 3 ключових гормонів протягом природного циклу:", es = "Evolución de las 3 hormonas clave en un ciclo natural:", de = "Verlauf der 3 wichtigsten Hormone in einem natürlichen Zyklus:"),
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

                // 1. Œstrogènes (Vert sauge / émeraude - Trait continu)
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

                // 2. Progestérone (Terracotta chaud - Trait tireté pour accessibilité daltonisme WCAG 1.4.1)
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
                val progDash = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
                drawSmoothCurve(progesteronePoints, Color(0xFFC04E3A), 3.dp.toPx(), pathEffect = progDash)

                // 3. Pic de LH (Ambre doré - Trait pointillé)
                val lhPoints = listOf(
                    Offset(x(1f), y(0.08f)),
                    Offset(x(11f), y(0.08f)),
                    Offset(x(12.5f), y(0.28f)),
                    Offset(x(13.5f), y(0.96f)),
                    Offset(x(14.5f), y(0.26f)),
                    Offset(x(16f), y(0.08f)),
                    Offset(x(28f), y(0.08f)),
                )
                val lhDots = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(5f, 6f), 0f)
                drawSmoothCurve(lhPoints, Color(0xFFD49B24), 2.5.dp.toPx(), pathEffect = lhDots)
            }

            // Repères temporels horizontaux
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(tr("J1 (Règles)", "D1 (Period)", pl = "D1 (Miesiączka)", uk = "Д1 (Менструація)", es = "D1 (Regla)", de = "T1 (Periode)"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(tr("J7", "D7", pl = "D7", uk = "Д7", es = "D7", de = "T7"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(tr("J14 (Ovulation)", "D14 (Ovulation)", pl = "D14 (Owulacja)", uk = "Д14 (Овуляція)", es = "D14 (Ovulación)", de = "T14 (Eisprung)"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text(tr("J21", "D21", pl = "D21", uk = "Д21", es = "D21", de = "T21"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(tr("J28", "D28", pl = "D28", uk = "Д28", es = "D28", de = "T28"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Légende descriptive avec repère visuel tactile
            HormoneLegendItem(
                color = Color(0xFF2E7D5E),
                name = tr("Œstrogènes (Ligne continue)", "Estrogen (Solid line)", pl = "Estrogeny (linia ciągła)", uk = "Естрогени (суцільна лінія)", es = "Estrógenos (línea continua)", de = "Östrogen (durchgezogene Linie)"),
                role = tr("Pic pré-ovulatoire. Stimule le renouvellement de l'endomètre, fluidifie la glaire cervicale et soutient l'énergie.", "Peaks before ovulation. Stimulates endometrial renewal, thins cervical mucus and supports energy.", pl = "Szczyt przedowulacyjny. Pobudza odnowę endometrium, rozrzedza śluz szyjkowy i wspiera energię.", uk = "Передовуляторний пік. Стимулює оновлення ендометрія, розріджує цервікальний слиз і підтримує енергію.", es = "Pico preovulatorio. Estimula la renovación del endometrio, fluidifica el moco cervical y sostiene la energía.", de = "Höhepunkt vor dem Eisprung. Regt die Erneuerung der Gebärmutterschleimhaut an, macht den Zervixschleim flüssiger und stützt die Energie."),
            )
            HormoneLegendItem(
                color = Color(0xFFC04E3A),
                name = tr("Progestérone (Ligne tiretée)", "Progesterone (Dashed line)", pl = "Progesteron (linia przerywana)", uk = "Прогестерон (штрихова лінія)", es = "Progesterona (línea discontinua)", de = "Progesteron (gestrichelte Linie)"),
                role = tr("Pic en milieu de phase lutéale (sécrétée par le corps jaune). Maintient l'endomètre réceptif pour une éventuelle nidation. Sa chute déclenche les règles.", "Peaks in mid-luteal phase (secreted by corpus luteum). Maintains endometrial readiness for implantation. Its drop triggers menstruation.", pl = "Szczyt w środku fazy lutealnej (wydzielany przez ciałko żółte). Utrzymuje endometrium gotowe na ewentualne zagnieżdżenie. Jego spadek wywołuje miesiączkę.", uk = "Пік у середині лютеїнової фази (виробляється жовтим тілом). Підтримує ендометрій готовим до можливої імплантації. Його падіння запускає менструацію.", es = "Pico a mitad de la fase lútea (secretada por el cuerpo lúteo). Mantiene el endometrio receptivo para una posible implantación. Su descenso desencadena la regla.", de = "Höhepunkt in der Mitte der Lutealphase (vom Gelbkörper gebildet). Hält die Gebärmutterschleimhaut für eine mögliche Einnistung aufnahmebereit. Sein Abfall löst die Periode aus."),
            )
            HormoneLegendItem(
                color = Color(0xFFD49B24),
                name = tr("Pic de LH (Ligne pointillée)", "LH surge (Dotted line)", pl = "Szczyt LH (linia kropkowana)", uk = "Пік ЛГ (пунктирна лінія)", es = "Pico de LH (línea de puntos)", de = "LH-Gipfel (gepunktete Linie)"),
                role = tr("Décharge brève et intense à J13-J14 qui déclenche l'expulsion de l'ovule par le follicule en 24 à 36 heures.", "Brief, intense surge at Days 13-14 that triggers egg release within 24 to 36 hours.", pl = "Krótki, intensywny wyrzut w dniach 13-14, który w ciągu 24 do 36 godzin powoduje uwolnienie komórki jajowej z pęcherzyka.", uk = "Короткий інтенсивний викид на 13-14-й день, що протягом 24-36 годин спричиняє вивільнення яйцеклітини з фолікула.", es = "Descarga breve e intensa en los días 13-14 que desencadena la expulsión del óvulo por el folículo en 24 a 36 horas.", de = "Kurze, intensive Ausschüttung an den Tagen 13 und 14, die innerhalb von 24 bis 36 Stunden die Freisetzung der Eizelle aus dem Follikel auslöst."),
            )
        }
    }
}

private fun DrawScope.drawSmoothCurve(
    points: List<Offset>,
    color: Color,
    strokeWidth: Float,
    pathEffect: androidx.compose.ui.graphics.PathEffect? = null,
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
            pathEffect = pathEffect,
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

enum class UterusPart {
    ENDOMETRIUM,
    MYOMETRIUM,
    CERVIX,
    TUBES,
    OVARIES,
    VAGINA,
}

private fun uterusPartTitle(part: UterusPart): String = when (part) {
    UterusPart.ENDOMETRIUM -> tr("L'endomètre (muqueuse utérine)", "The endometrium (uterine lining)", pl = "Endometrium (błona śluzowa macicy)", uk = "Ендометрій (слизова оболонка матки)", es = "El endometrio (mucosa uterina)", de = "Das Endometrium (Gebärmutterschleimhaut)")
    UterusPart.MYOMETRIUM -> tr("Le myomètre (paroi musculaire)", "The myometrium (muscular wall)", pl = "Miometrium (ściana mięśniowa)", uk = "Міометрій (м'язова стінка)", es = "El miometrio (pared muscular)", de = "Das Myometrium (Muskelwand)")
    UterusPart.CERVIX -> tr("Le col de l'utérus (zone de passage)", "The cervix (cervical canal)", pl = "Szyjka macicy (strefa przejścia)", uk = "Шийка матки (зона проходу)", es = "El cuello del útero (zona de paso)", de = "Der Gebärmutterhals (Durchgangszone)")
    UterusPart.TUBES -> tr("Les trompes de Fallope (conduits utéro-ovariens)", "The fallopian tubes (uterine tubes)", pl = "Jajowody (przewody macicy i jajników)", uk = "Маткові труби (канали між маткою та яєчниками)", es = "Las trompas de Falopio (conductos útero-ováricos)", de = "Die Eileiter (Verbindungsgänge zwischen Gebärmutter und Eierstöcken)")
    UterusPart.OVARIES -> tr("Les ovaires (glandes hormonales et reproductrices)", "The ovaries (hormonal and reproductive glands)", pl = "Jajniki (gruczoły hormonalne i rozrodcze)", uk = "Яєчники (гормональні та репродуктивні залози)", es = "Los ovarios (glándulas hormonales y reproductoras)", de = "Die Eierstöcke (Hormon- und Keimdrüsen)")
    UterusPart.VAGINA -> tr("Le vagin (canal musculaire élastique)", "The vagina (elastic muscular canal)", pl = "Pochwa (elastyczny kanał mięśniowy)", uk = "Піхва (еластичний м'язовий канал)", es = "La vagina (conducto muscular elástico)", de = "Die Vagina (elastischer Muskelkanal)")
}

private fun uterusPartChipLabel(part: UterusPart): String = when (part) {
    UterusPart.ENDOMETRIUM -> tr("Endomètre (muqueuse)", "Endometrium (lining)", pl = "Endometrium (błona śluzowa)", uk = "Ендометрій (слизова)", es = "Endometrio (mucosa)", de = "Endometrium (Schleimhaut)")
    UterusPart.MYOMETRIUM -> tr("Myomètre (muscle)", "Myometrium (muscle)", pl = "Miometrium (mięsień)", uk = "Міометрій (м'яз)", es = "Miometrio (músculo)", de = "Myometrium (Muskel)")
    UterusPart.CERVIX -> tr("Col de l'utérus", "Cervix", pl = "Szyjka macicy", uk = "Шийка матки", es = "Cuello del útero", de = "Gebärmutterhals")
    UterusPart.TUBES -> tr("Trompes de Fallope", "Fallopian tubes", pl = "Jajowody", uk = "Маткові труби", es = "Trompas de Falopio", de = "Eileiter")
    UterusPart.OVARIES -> tr("Ovaires (hormones)", "Ovaries (hormones)", pl = "Jajniki (hormony)", uk = "Яєчники (гормони)", es = "Ovarios (hormonas)", de = "Eierstöcke (Hormone)")
    UterusPart.VAGINA -> tr("Vagin (interne)", "Vagina (internal)", pl = "Pochwa (wewnętrzna)", uk = "Піхва (внутрішня)", es = "Vagina (interna)", de = "Vagina (innen)")
}

private fun uterusPartSummary(part: UterusPart): String = when (part) {
    UterusPart.ENDOMETRIUM -> tr(
        fr = "La muqueuse vivante qui tapisse la cavité interne de l'utérus. Chaque mois, sous l'effet des œstrogènes puis de la progestérone, elle s'épaissit et développe de fins vaisseaux sanguins pour préparer une éventuelle implantation embryonnaire (nidation). En l'absence de fécondation, la chute hormonale entraîne son décollement et son élimination : c'est le flux menstruel des règles. Il s'agit d'un processus naturel de renouvellement tissulaire.",
        en = "The mucous membrane lining the inner uterine cavity. Each month, influenced by estrogen and progesterone, it thickens and develops micro-vessels in preparation for potential embryo implantation. If pregnancy does not occur, hormone decline causes this layer to shed and flow outward: this is menstruation. It is a natural process of regular tissue renewal.",
        pl = "Żywa błona śluzowa wyściełająca wnętrze jamy macicy. Co miesiąc, pod wpływem estrogenów, a następnie progesteronu, pogrubia się i wytwarza drobne naczynia krwionośne, aby przygotować się na ewentualne zagnieżdżenie zarodka (implantację). Gdy nie dojdzie do zapłodnienia, spadek poziomu hormonów powoduje jej oddzielenie i usunięcie: to krwawienie menstruacyjne. Jest to naturalny proces odnowy tkanki.",
        uk = "Жива слизова оболонка, що вистилає внутрішню порожнину матки. Щомісяця під дією естрогенів, а потім прогестерону вона потовщується й утворює дрібні кровоносні судини, щоб підготуватися до можливої імплантації ембріона. Якщо запліднення не відбулося, зниження рівня гормонів спричиняє її відшарування й виведення: це менструальна кровотеча. Це природний процес оновлення тканини.",
        es = "La mucosa viva que reviste la cavidad interna del útero. Cada mes, por efecto de los estrógenos y después de la progesterona, se engrosa y desarrolla pequeños vasos sanguíneos para preparar una posible implantación embrionaria (anidación). Si no hay fecundación, la caída hormonal provoca su desprendimiento y su eliminación: es el flujo menstrual de la regla. Se trata de un proceso natural de renovación del tejido.",
        de = "Die lebendige Schleimhaut, die die Gebärmutterhöhle auskleidet. Jeden Monat baut sie sich unter dem Einfluss von Östrogen und dann Progesteron auf und bildet feine Blutgefäße, um eine mögliche Einnistung des Embryos vorzubereiten. Bleibt die Befruchtung aus, führt der Hormonabfall zu ihrer Ablösung und Ausscheidung: das ist die Menstruationsblutung. Es handelt sich um einen natürlichen Prozess der Gewebeerneuerung.",
    )
    UterusPart.MYOMETRIUM -> tr(
        fr = "L'épaisse paroi musculaire qui compose le corps de l'utérus. Pendant les règles, il effectue des contractions rythmées, déclenchées par les prostaglandines, pour faciliter l'évacuation de la muqueuse utérine. Lorsque ces contractions sont intenses, la compression temporaire des petits vaisseaux sanguins réduit brièvement l'apport en oxygène des tissus, provoquant la sensation de crampe. La chaleur (bouillotte, bain tiède) soulage en détendant les fibres musculaires. En revanche, des douleurs invalidantes qui empêchent les activités quotidiennes ne sont pas normales et justifient un avis médical (suspicion d'endométriose ou d'adénomyose).",
        en = "The thick muscular wall that forms the body of the uterus. During menstruation, it contracts in rhythmic waves triggered by prostaglandins to help shed the uterine lining. Strong contractions can temporarily compress local micro-vessels, briefly reducing oxygen delivery and causing cramp pain. Applying gentle heat relaxes the muscle fibers. However, debilitating pain that disrupts daily activities is not normal and warrants medical evaluation for conditions such as endometriosis or adenomyosis.",
        pl = "Gruba ściana mięśniowa tworząca trzon macicy. Podczas miesiączki wykonuje rytmiczne skurcze, wywoływane przez prostaglandyny, aby ułatwić usunięcie błony śluzowej macicy. Gdy te skurcze są silne, chwilowe uciskanie drobnych naczyń krwionośnych krótko zmniejsza dopływ tlenu do tkanek, co wywołuje uczucie skurczu. Ciepło (termofor, ciepła kąpiel) przynosi ulgę, rozluźniając włókna mięśniowe. Natomiast bóle uniemożliwiające codzienne czynności nie są normalne i uzasadniają poradę lekarską (podejrzenie endometriozy lub adenomiozy).",
        uk = "Товста м'язова стінка, що утворює тіло матки. Під час менструації вона виконує ритмічні скорочення, спричинені простагландинами, щоб полегшити виведення слизової оболонки матки. Коли ці скорочення інтенсивні, тимчасове стискання дрібних кровоносних судин ненадовго зменшує надходження кисню до тканин, що викликає відчуття спазму. Тепло (грілка, тепла ванна) полегшує стан, розслабляючи м'язові волокна. Натомість біль, що унеможливлює повсякденну діяльність, не є нормою і потребує консультації лікаря (підозра на ендометріоз чи аденоміоз).",
        es = "La gruesa pared muscular que forma el cuerpo del útero. Durante la regla realiza contracciones rítmicas, desencadenadas por las prostaglandinas, para facilitar la evacuación de la mucosa uterina. Cuando estas contracciones son intensas, la compresión temporal de los pequeños vasos sanguíneos reduce brevemente el aporte de oxígeno a los tejidos y provoca la sensación de calambre. El calor (bolsa de agua caliente, baño templado) alivia al relajar las fibras musculares. En cambio, los dolores incapacitantes que impiden las actividades cotidianas no son normales y justifican una consulta médica (sospecha de endometriosis o de adenomiosis).",
        de = "Die dicke Muskelwand, die den Gebärmutterkörper bildet. Während der Periode zieht sie sich rhythmisch zusammen, ausgelöst durch Prostaglandine, um das Ausstoßen der Gebärmutterschleimhaut zu erleichtern. Sind diese Kontraktionen stark, drückt sie vorübergehend kleine Blutgefäße ab, was die Sauerstoffversorgung des Gewebes kurz verringert und das Krampfgefühl auslöst. Wärme (Wärmflasche, warmes Bad) lindert, indem sie die Muskelfasern entspannt. Schmerzen, die den Alltag unmöglich machen, sind dagegen nicht normal und sollten ärztlich abgeklärt werden (Verdacht auf Endometriose oder Adenomyose).",
    )
    UterusPart.CERVIX -> tr(
        fr = "La portion cylindrique inférieure reliant la cavité utérine au fond du vagin. Il assure un rôle protecteur et fonctionnel : la majeure partie du cycle, son canal est scellé par un bouchon muqueux dense et acide qui fait obstacle aux bactéries extérieures. À l'approche de l'ovulation, il s'assouplit et produit une glaire limpide et fluide facilitant le passage des spermatozoïdes. Pendant les règles, son orifice s'entrouvre légèrement pour laisser s'écouler le flux menstruel.",
        en = "The lower cylindrical portion of the uterus connecting the uterine cavity to the upper vagina. It provides both protection and guidance: during most of the cycle, its canal is closed by thick, acidic mucus blocking bacterial entry. Near ovulation, it softens and secretes clear, fluid mucus facilitating sperm passage. During menstruation, it slightly relaxes to allow menstrual flow to pass.",
        pl = "Dolna, cylindryczna część łącząca jamę macicy z głębią pochwy. Pełni funkcję ochronną i czynnościową: przez większą część cyklu jego kanał jest zamknięty gęstym, kwaśnym czopem śluzowym, który stanowi barierę dla bakterii z zewnątrz. W okolicach owulacji staje się bardziej miękki i wytwarza przejrzysty, płynny śluz ułatwiający przechodzenie plemników. Podczas miesiączki jego ujście lekko się otwiera, by umożliwić odpływ krwi menstruacyjnej.",
        uk = "Нижня циліндрична частина, що з'єднує порожнину матки з глибиною піхви. Вона виконує захисну та функціональну роль: більшу частину циклу її канал закритий щільною кислою слизовою пробкою, яка перешкоджає проникненню бактерій ззовні. Наближаючись до овуляції, вона стає м'якшою й виробляє прозорий рідкий слиз, що полегшує проходження сперматозоїдів. Під час менструації її отвір трохи розкривається, щоб пропустити менструальні виділення.",
        es = "La porción cilíndrica inferior que une la cavidad uterina con el fondo de la vagina. Cumple una función protectora y funcional: durante la mayor parte del ciclo, su canal está sellado por un tapón mucoso denso y ácido que impide el paso de bacterias del exterior. Al acercarse la ovulación, se ablanda y produce un moco límpido y fluido que facilita el paso de los espermatozoides. Durante la regla, su orificio se entreabre ligeramente para dejar salir el flujo menstrual.",
        de = "Der untere, zylinderförmige Abschnitt, der die Gebärmutterhöhle mit dem Scheidengewölbe verbindet. Er hat eine schützende und funktionelle Aufgabe: Den größten Teil des Zyklus ist sein Kanal von einem dichten, sauren Schleimpfropf verschlossen, der Bakterien von außen abhält. Vor dem Eisprung wird er weicher und bildet einen klaren, flüssigen Schleim, der den Spermien den Durchtritt erleichtert. Während der Periode öffnet sich seine Öffnung leicht, damit die Menstruationsblutung abfließen kann.",
    )
    UterusPart.TUBES -> tr(
        fr = "Deux fins conduits musculo-membraneux d'environ 10 à 12 cm reliant les ovaires à l'utérus. Leurs pavillons bordés de franges mobiles (les fimbriae) captent l'ovocyte dès son expulsion de l'ovaire. C'est dans le tiers externe de la trompe qu'a lieu la fécondation si un spermatozoïde rencontre l'ovule. Des micro-cils vibratiles et des mouvements péristaltiques acheminent ensuite l'ovocyte ou l'embryon vers la cavité utérine.",
        en = "Two delicate channels (10 to 12 cm long) linking each ovary to the uterus. Their flared outer ends, fringed with mobile fingers (fimbriae), capture the released egg at ovulation. Fertilization occurs in the outer portion of the tube when sperm is present. Microscopic cilia and gentle muscular contractions then transport the egg or embryo toward the uterine cavity.",
        pl = "Dwa cienkie przewody mięśniowo-błoniaste o długości około 10 do 12 cm, łączące jajniki z macicą. Ich lejkowate końce, obrzeżone ruchomymi frędzlami (strzępkami), wychwytują komórkę jajową zaraz po jej uwolnieniu z jajnika. W zewnętrznej jednej trzeciej jajowodu dochodzi do zapłodnienia, jeśli plemnik spotka komórkę jajową. Mikroskopijne rzęski i ruchy perystaltyczne przenoszą następnie komórkę jajową lub zarodek do jamy macicy.",
        uk = "Дві тонкі м'язово-перетинчасті труби завдовжки близько 10-12 см, що з'єднують яєчники з маткою. Їхні лійкоподібні кінці з рухомою бахромою (фімбріями) вловлюють яйцеклітину одразу після виходу з яєчника. У зовнішній третині труби відбувається запліднення, якщо сперматозоїд зустрічає яйцеклітину. Мікроскопічні війки та перистальтичні рухи потім просувають яйцеклітину або ембріон до порожнини матки.",
        es = "Dos conductos finos, musculomembranosos, de unos 10 a 12 cm, que unen los ovarios con el útero. Sus pabellones, bordeados de franjas móviles (las fimbrias), recogen el óvulo en cuanto sale del ovario. La fecundación tiene lugar en el tercio externo de la trompa si un espermatozoide encuentra el óvulo. Unas pestañas vibrátiles microscópicas y los movimientos peristálticos llevan después el óvulo o el embrión hacia la cavidad uterina.",
        de = "Zwei dünne Muskel-Schleimhaut-Gänge von etwa 10 bis 12 cm Länge, die die Eierstöcke mit der Gebärmutter verbinden. Ihre trichterförmigen Enden mit beweglichen Fransen (den Fimbrien) fangen die Eizelle auf, sobald sie den Eierstock verlässt. Im äußeren Drittel des Eileiters findet die Befruchtung statt, wenn ein Spermium auf die Eizelle trifft. Winzige Flimmerhärchen und Muskelbewegungen befördern die Eizelle oder den Embryo anschließend in die Gebärmutterhöhle.",
    )
    UterusPart.OVARIES -> tr(
        fr = "Deux glandes en forme d'amande situées de chaque côté de l'utérus. Dès la naissance, elles renferment la réserve complète d'ovocytes sous forme de follicules au repos. À chaque cycle, une cohorte de follicules entre en phase de maturation sous contrôle hormonal, et le plus développé libère un ovule (ovulation). Les ovaires sont aussi les principaux producteurs d'hormones sexuelles : les œstrogènes (croissance des tissus et vitalité) et la progestérone (stabilisation de la muqueuse utérine).",
        en = "Two almond-shaped glands nestled on either side of the uterus. From birth, they contain the complete lifetime reserve of egg cells in dormant follicles. Each cycle, a cohort of follicles matures under hormonal cues, and the leading follicle releases an egg (ovulation). The ovaries also produce primary sex hormones: estrogen (supporting tissue growth and vitality) and progesterone (stabilizing the uterine lining).",
        pl = "Dwa gruczoły w kształcie migdałów, położone po obu stronach macicy. Od urodzenia zawierają pełną rezerwę komórek jajowych w postaci uśpionych pęcherzyków. W każdym cyklu grupa pęcherzyków wchodzi w fazę dojrzewania pod kontrolą hormonów, a najbardziej rozwinięty uwalnia komórkę jajową (owulacja). Jajniki są też głównym źródłem hormonów płciowych: estrogenów (wzrost tkanek i witalność) oraz progesteronu (stabilizacja błony śluzowej macicy).",
        uk = "Дві залози мигдалеподібної форми, розташовані по боках від матки. З народження вони містять повний запас яйцеклітин у вигляді фолікулів у стані спокою. У кожному циклі група фолікулів вступає у фазу дозрівання під гормональним контролем, а найрозвиненіший вивільняє яйцеклітину (овуляція). Яєчники також є основними виробниками статевих гормонів: естрогенів (ріст тканин і життєві сили) та прогестерону (стабілізація слизової оболонки матки).",
        es = "Dos glándulas en forma de almendra situadas a cada lado del útero. Desde el nacimiento contienen toda la reserva de óvulos en forma de folículos en reposo. En cada ciclo, un grupo de folículos entra en fase de maduración bajo control hormonal, y el más desarrollado libera un óvulo (ovulación). Los ovarios son también los principales productores de hormonas sexuales: los estrógenos (crecimiento de los tejidos y vitalidad) y la progesterona (estabilización de la mucosa uterina).",
        de = "Zwei mandelförmige Drüsen zu beiden Seiten der Gebärmutter. Sie enthalten seit der Geburt den gesamten Vorrat an Eizellen in Form ruhender Follikel. In jedem Zyklus tritt eine Gruppe von Follikeln unter hormoneller Steuerung in die Reifung ein, und der am weitesten entwickelte gibt eine Eizelle frei (Eisprung). Die Eierstöcke sind außerdem die wichtigsten Bildungsorte der Geschlechtshormone: Östrogen (Gewebewachstum und Vitalität) und Progesteron (Stabilisierung der Gebärmutterschleimhaut).",
    )
    UterusPart.VAGINA -> tr(
        fr = "Le canal fibro-musculaire souple reliant la vulve au col de l'utérus. Au repos, ses parois antérieures et postérieures sont naturellement en contact. Sa muqueuse pourvue de plis transversaux lui confère une grande souplesse et une capacité d'extension indolore, lui permettant d'accueillir tampons, coupes menstruelles ou rapports sexuels, ainsi que le passage du bébé lors de l'accouchement, avant de retrouver ses dimensions de repos. Son milieu acide protecteur est préservé par une flore bactérienne équilibrée (lactobacilles), sans nécessiter de lavage interne.",
        en = "The flexible muscular canal connecting the vulva to the cervix. At rest, its anterior and posterior walls naturally rest against each other. Transverse mucosal folds provide great elasticity, allowing it to stretch comfortably to accommodate tampons, menstrual cups, or intimacy, as well as childbirth, before returning to resting dimensions. Its protective acidic ecosystem is maintained by balanced flora (lactobacilli), requiring no internal washing.",
        pl = "Elastyczny kanał włóknisto-mięśniowy łączący srom z szyjką macicy. W spoczynku jego ściany przednia i tylna naturalnie przylegają do siebie. Błona śluzowa z poprzecznymi fałdami nadaje mu dużą elastyczność i zdolność bezbolesnego rozciągania, dzięki czemu może pomieścić tampony, kubeczki menstruacyjne lub współżycie, a także przejście dziecka podczas porodu, po czym wraca do wymiarów spoczynkowych. Jego ochronne, kwaśne środowisko utrzymuje zrównoważona flora bakteryjna (pałeczki kwasu mlekowego), bez potrzeby płukania wewnętrznego.",
        uk = "Гнучкий волокнисто-м'язовий канал, що з'єднує вульву з шийкою матки. У спокої його передня й задня стінки природно прилягають одна до одної. Слизова оболонка з поперечними складками надає йому великої еластичності та здатності безболісно розтягуватися, що дає змогу приймати тампони, менструальні чаші чи статевий акт, а також пропускати дитину під час пологів, після чого він повертається до звичайних розмірів. Його захисне кисле середовище підтримується збалансованою бактеріальною флорою (лактобактерії), без потреби внутрішнього промивання.",
        es = "El conducto fibromuscular flexible que une la vulva con el cuello del útero. En reposo, sus paredes anterior y posterior están naturalmente en contacto. Su mucosa, provista de pliegues transversales, le da una gran flexibilidad y una capacidad de distensión indolora, lo que le permite acoger tampones, copas menstruales o relaciones sexuales, así como el paso del bebé durante el parto, antes de recuperar sus dimensiones de reposo. Su medio ácido protector se mantiene gracias a una flora bacteriana equilibrada (lactobacilos), sin necesidad de lavados internos.",
        de = "Der flexible, fibromuskuläre Kanal, der die Vulva mit dem Gebärmutterhals verbindet. In Ruhe liegen seine vordere und hintere Wand natürlich aneinander. Seine Schleimhaut mit Querfalten verleiht ihm große Elastizität und die Fähigkeit, sich schmerzlos zu dehnen, sodass er Tampons, Menstruationstassen oder Geschlechtsverkehr aufnehmen kann, ebenso wie den Durchtritt des Babys bei der Geburt, bevor er wieder seine Ruhemaße annimmt. Sein schützendes saures Milieu wird durch eine ausgewogene Bakterienflora (Laktobazillen) aufrechterhalten, ganz ohne Spülungen im Inneren.",
    )
}

private fun cyclePhaseName(day: Int): String = when (day) {
    in 1..5 -> tr("1. Phase menstruelle (Règles)", "1. Menstrual phase (Period)", pl = "1. Faza menstruacyjna (miesiączka)", uk = "1. Менструальна фаза (менструація)", es = "1. Fase menstrual (regla)", de = "1. Menstruationsphase (Periode)")
    in 6..13 -> tr("2. Phase folliculaire (Pré-ovulation)", "2. Follicular phase (Pre-ovulation)", pl = "2. Faza folikularna (przed owulacją)", uk = "2. Фолікулярна фаза (до овуляції)", es = "2. Fase folicular (preovulación)", de = "2. Follikelphase (vor dem Eisprung)")
    14 -> tr("3. Phase ovulatoire (Ovulation)", "3. Ovulatory phase (Ovulation)", pl = "3. Faza owulacyjna (owulacja)", uk = "3. Овуляторна фаза (овуляція)", es = "3. Fase ovulatoria (ovulación)", de = "3. Ovulationsphase (Eisprung)")
    else -> tr("4. Phase lutéale (Post-ovulation)", "4. Luteal phase (Post-ovulation)", pl = "4. Faza lutealna (po owulacji)", uk = "4. Лютеїнова фаза (після овуляції)", es = "4. Fase lútea (posovulación)", de = "4. Lutealphase (nach dem Eisprung)")
}

private fun cyclePhaseDescription(day: Int): String = when (day) {
    in 1..5 -> tr(
        "J1 à J5 : Le premier jour de saignement rouge vif marque le Jour 1 du cycle. En l'absence de nidation, les taux d'hormones chutent : la couche superficielle de la muqueuse utérine se détache et s'évacue. Les contractions régulières du myomètre favorisent cette élimination. Ce renouvellement marque le départ d'un nouveau cycle physiologique.",
        "D1 to D5: The first day of bright red flow marks Day 1 of the cycle. When no implantation occurs, hormone levels drop: the superficial uterine lining sheds and flows outward. Regular contractions of the uterine muscle facilitate this shedding, initiating a fresh cycle of tissue renewal.",
        pl = "D1 do D5: pierwszy dzień jasnoczerwonego krwawienia to 1. dzień cyklu. Gdy nie dojdzie do zagnieżdżenia, poziom hormonów spada: powierzchowna warstwa błony śluzowej macicy odrywa się i zostaje usunięta. Regularne skurcze miometrium sprzyjają temu oczyszczaniu. To odnowienie zaczyna nowy cykl fizjologiczny.",
        uk = "Д1-Д5: перший день яскраво-червоної кровотечі є 1-м днем циклу. Якщо імплантації не було, рівень гормонів знижується: поверхневий шар слизової оболонки матки відшаровується й виводиться. Регулярні скорочення міометрія сприяють цьому виведенню. Таке оновлення дає початок новому фізіологічному циклу.",
        es = "D1 a D5: el primer día de sangrado rojo vivo marca el día 1 del ciclo. Si no hay anidación, los niveles hormonales caen: la capa superficial de la mucosa uterina se desprende y se elimina. Las contracciones regulares del miometrio favorecen esta eliminación. Esta renovación marca el inicio de un nuevo ciclo fisiológico.",
        de = "T1 bis T5: Der erste Tag mit hellroter Blutung ist Tag 1 des Zyklus. Bleibt eine Einnistung aus, sinken die Hormonspiegel: Die oberste Schicht der Gebärmutterschleimhaut löst sich ab und wird ausgeschieden. Regelmäßige Kontraktionen des Myometriums fördern diese Ausscheidung. Diese Erneuerung markiert den Beginn eines neuen physiologischen Zyklus.",
    )
    in 6..13 -> tr(
        "J6 à J13 : Un follicule grandit dans l'un des ovaires pour mener un ovocyte à maturité. Il sécrète des œstrogènes en quantité progressive, stimulant l'épaississement de la muqueuse utérine et accompagnant souvent une période d'énergie et de vitalité.",
        "D6 to D13: A follicle develops inside one ovary to bring an egg to maturity. It secretes increasing amounts of estrogen, rebuilding the uterine lining while often supporting energy and overall vitality.",
        pl = "D6 do D13: w jednym z jajników rośnie pęcherzyk, który doprowadza komórkę jajową do dojrzałości. Wydziela stopniowo coraz więcej estrogenów, pobudzając pogrubienie błony śluzowej macicy i często towarzysząc okresowi energii i witalności.",
        uk = "Д6-Д13: в одному з яєчників росте фолікул, щоб довести яйцеклітину до зрілості. Він виробляє дедалі більше естрогенів, стимулюючи потовщення слизової оболонки матки та нерідко супроводжуючи період енергії та життєвих сил.",
        es = "D6 a D13: un folículo crece en uno de los ovarios para llevar un óvulo a la madurez. Secreta estrógenos en cantidad progresiva, estimulando el engrosamiento de la mucosa uterina y acompañando a menudo un periodo de energía y vitalidad.",
        de = "T6 bis T13: In einem der Eierstöcke wächst ein Follikel heran, um eine Eizelle zur Reife zu bringen. Er bildet zunehmend Östrogen, regt den Aufbau der Gebärmutterschleimhaut an und begleitet oft eine Phase voller Energie und Vitalität.",
    )
    14 -> tr(
        "J14 : Sous l'effet d'un pic soudain d'hormone lutéinisante (LH), le follicule dominant libère son ovule mûr, capté par la trompe. L'ovule est fécondable pendant 12 à 24 heures. Pour optimiser le passage des spermatozoïdes, le col sécrète une glaire cervicale limpide, fluide et étirable.",
        "D14: Triggered by a rapid surge in luteinizing hormone (LH), the mature follicle releases its egg, captured by the fallopian tube. The egg remains viable for fertilization for 12 to 24 hours. The cervix produces clear, slippery, and stretchy mucus to facilitate sperm transit.",
        pl = "D14: pod wpływem nagłego szczytu hormonu luteinizującego (LH) dominujący pęcherzyk uwalnia dojrzałą komórkę jajową, wychwytywaną przez jajowód. Komórka jajowa może zostać zapłodniona przez 12 do 24 godzin. Aby ułatwić przejście plemników, szyjka macicy wytwarza przejrzysty, płynny i rozciągliwy śluz.",
        uk = "Д14: під впливом різкого піка лютеїнізуючого гормону (ЛГ) домінантний фолікул вивільняє зрілу яйцеклітину, яку вловлює маткова труба. Яйцеклітина здатна до запліднення впродовж 12-24 годин. Щоб полегшити проходження сперматозоїдів, шийка матки виробляє прозорий, рідкий і тягучий слиз.",
        es = "D14: bajo el efecto de un pico repentino de hormona luteinizante (LH), el folículo dominante libera su óvulo maduro, que recoge la trompa. El óvulo es fecundable durante 12 a 24 horas. Para facilitar el paso de los espermatozoides, el cuello del útero secreta un moco cervical límpido, fluido y elástico.",
        de = "T14: Durch einen plötzlichen Anstieg des luteinisierenden Hormons (LH) gibt der dominante Follikel seine reife Eizelle frei, die vom Eileiter aufgenommen wird. Die Eizelle ist 12 bis 24 Stunden befruchtungsfähig. Um den Spermien den Durchtritt zu erleichtern, bildet der Gebärmutterhals einen klaren, flüssigen und dehnbaren Zervixschleim.",
    )
    else -> tr(
        "J15 à J28 : Le follicule libéré s'est transformé en corps jaune et sécrète de la progestérone. Cette hormone enrichit la muqueuse utérine en glycogène et en vaisseaux sanguins, la rendant propice à une éventuelle nidation. En l'absence de fécondation, le corps jaune régresse, les hormones diminuent, ce qui peut influencer l'humeur ou l'énergie (syndrome prémenstruel), avant l'arrivée du cycle suivant.",
        "D15 to D28: The follicle transforms into the corpus luteum, secreting progesterone. This hormone enriches the uterine lining with nutrients and blood vessels for potential embryo implantation. Without fertilization, the corpus luteum regresses, hormone levels decline, sometimes prompting premenstrual symptoms (PMS), before the next cycle begins.",
        pl = "D15 do D28: uwolniony pęcherzyk przekształcił się w ciałko żółte i wydziela progesteron. Ten hormon wzbogaca błonę śluzową macicy w glikogen i naczynia krwionośne, czyniąc ją sprzyjającą ewentualnemu zagnieżdżeniu. Gdy nie dojdzie do zapłodnienia, ciałko żółte zanika, poziom hormonów spada, co może wpływać na nastrój lub energię (zespół napięcia przedmiesiączkowego), przed rozpoczęciem kolejnego cyklu.",
        uk = "Д15-Д28: фолікул, що вивільнив яйцеклітину, перетворився на жовте тіло й виробляє прогестерон. Цей гормон збагачує слизову оболонку матки глікогеном і кровоносними судинами, роблячи її сприятливою для можливої імплантації. Якщо запліднення не відбулося, жовте тіло регресує, рівень гормонів знижується, що може впливати на настрій чи енергію (передменструальний синдром), перед початком наступного циклу.",
        es = "D15 a D28: el folículo liberado se ha transformado en cuerpo lúteo y secreta progesterona. Esta hormona enriquece la mucosa uterina en glucógeno y en vasos sanguíneos, haciéndola propicia para una posible anidación. Si no hay fecundación, el cuerpo lúteo involuciona y las hormonas descienden, lo que puede influir en el ánimo o la energía (síndrome premenstrual), antes de que empiece el ciclo siguiente.",
        de = "T15 bis T28: Der freigesetzte Follikel hat sich in den Gelbkörper verwandelt und bildet Progesteron. Dieses Hormon reichert die Gebärmutterschleimhaut mit Glykogen und Blutgefäßen an und macht sie für eine mögliche Einnistung geeignet. Bleibt die Befruchtung aus, bildet sich der Gelbkörper zurück und die Hormone sinken, was Stimmung oder Energie beeinflussen kann (prämenstruelles Syndrom), bevor der nächste Zyklus beginnt.",
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InteractiveUterusDiagram(
    initialPart: UterusPart = UterusPart.ENDOMETRIUM,
    initialDay: Int = 14,
) {
    var selectedPart by remember { mutableStateOf(initialPart) }
    var cycleDay by remember { mutableFloatStateOf(initialDay.toFloat()) }
    val currentDay = cycleDay.roundToInt().coerceIn(1, 28)
    val colors = MaterialTheme.colorScheme
    val isDarkScheme = colors.background.luminance() < 0.5f
    val measurer = rememberTextMeasurer()

    SectionTitle(tr("Voyage interactif dans votre cycle", "Interactive journey inside your cycle", pl = "Interaktywna podróż przez Twój cykl", uk = "Інтерактивна подорож вашим циклом", es = "Viaje interactivo por tu ciclo", de = "Interaktive Reise durch deinen Zyklus"))
    LunaCard {
        Text(
            tr(
                fr = "Faites glisser le curseur pour observer l'évolution de la muqueuse et des ovaires au fil des jours (J1 à J28). Touchez un organe sur le schéma pour afficher ses explications :",
                en = "Drag the slider to observe changes in the uterine lining and ovaries across days (D1 to D28). Tap an organ on the diagram to see its details:",
                pl = "Przesuń suwak, aby obserwować zmiany błony śluzowej i jajników z dnia na dzień (D1 do D28). Dotknij narządu na schemacie, aby zobaczyć jego opis:",
                uk = "Пересувайте повзунок, щоб спостерігати за змінами слизової оболонки та яєчників день за днем (Д1-Д28). Торкніться органа на схемі, щоб побачити його опис:",
                es = "Desliza el control para observar la evolución de la mucosa y de los ovarios a lo largo de los días (D1 a D28). Toca un órgano en el esquema para ver sus explicaciones:",
                de = "Verschiebe den Regler, um die Entwicklung der Schleimhaut und der Eierstöcke im Lauf der Tage zu beobachten (T1 bis T28). Tippe auf ein Organ im Schema, um seine Erklärungen anzuzeigen:",
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
        )

        // 1. Curseur temporel du cycle (Slider J1 - J28)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = colors.surfaceVariant.copy(alpha = 0.45f),
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.5f)),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        tr("Jour du cycle : J$currentDay", "Cycle day: D$currentDay", pl = "Dzień cyklu: D$currentDay", uk = "День циклу: Д$currentDay", es = "Día del ciclo: D$currentDay", de = "Zyklustag: T$currentDay"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (currentDay) {
                            in 1..5 -> Color(0xFFEF5350).copy(alpha = 0.2f)
                            in 6..13 -> Color(0xFF42A5F5).copy(alpha = 0.2f)
                            14 -> Color(0xFFFFB300).copy(alpha = 0.25f)
                            else -> Color(0xFFAB47BC).copy(alpha = 0.2f)
                        },
                    ) {
                        Text(
                            cyclePhaseName(currentDay),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkScheme) {
                                when (currentDay) {
                                    in 1..5 -> Color(0xFFFF8A80)
                                    in 6..13 -> Color(0xFF90CAF9)
                                    14 -> Color(0xFFFFCC80)
                                    else -> Color(0xFFE1BEE7)
                                }
                            } else {
                                when (currentDay) {
                                    in 1..5 -> Color(0xFFC62828)
                                    in 6..13 -> Color(0xFF1565C0)
                                    14 -> Color(0xFFE65100)
                                    else -> Color(0xFF6A1B9A)
                                }
                            },
                        )
                    }
                }

                Slider(
                    value = cycleDay,
                    onValueChange = { cycleDay = it },
                    valueRange = 1f..28f,
                    steps = 26,
                    modifier = Modifier.fillMaxWidth(),
                )

                // Raccourcis de sélection rapide de phase
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    listOf(
                        Triple(1f, tr("1. Règles", "1. Period", pl = "1. Miesiączka", uk = "1. Менструація", es = "1. Regla", de = "1. Periode"), 1..5),
                        Triple(8f, tr("2. Folliculaire", "2. Follicular", pl = "2. Folikularna", uk = "2. Фолікулярна", es = "2. Folicular", de = "2. Follikulär"), 6..13),
                        Triple(14f, tr("3. Ovulation", "3. Ovulation", pl = "3. Owulacja", uk = "3. Овуляція", es = "3. Ovulación", de = "3. Eisprung"), 14..14),
                        Triple(21f, tr("4. Lutéale", "4. Luteal", pl = "4. Lutealna", uk = "4. Лютеїнова", es = "4. Lútea", de = "4. Luteal"), 15..28),
                    ).forEach { (targetDay, label, range) ->
                        val isCurrent = currentDay in range
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isCurrent) colors.primaryContainer else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isCurrent) colors.primary else colors.outlineVariant.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { cycleDay = targetDay },
                        ) {
                            Text(
                                label,
                                modifier = Modifier.padding(vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrent) colors.onPrimaryContainer else colors.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            )
                        }
                    }
                }

                // Synthèse physiologique de la phase active
                Text(
                    cyclePhaseDescription(currentDay),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }

        // 2. Canvas vectoriel interactif et dynamique
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(330.dp)
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
                            val topY = h * 0.20f
                            val cervixY = h * 0.64f
                            val dx = offset.x - cx
                            val dy = offset.y

                            // Hit testing par zone anatomique précise
                            val leftOvaryX = cx - w * 0.36f
                            val rightOvaryX = cx + w * 0.36f
                            val ovaryY = topY + h * 0.22f
                            val distLeftOvary = kotlin.math.hypot(offset.x - leftOvaryX, dy - ovaryY)
                            val distRightOvary = kotlin.math.hypot(offset.x - rightOvaryX, dy - ovaryY)

                            if (distLeftOvary < w * 0.13f || distRightOvary < w * 0.13f) {
                                selectedPart = UterusPart.OVARIES
                            } else if (dy < topY + h * 0.18f && kotlin.math.abs(dx) > w * 0.18f) {
                                selectedPart = UterusPart.TUBES
                            } else if (dy > cervixY + h * 0.05f && kotlin.math.abs(dx) < w * 0.16f) {
                                selectedPart = UterusPart.VAGINA
                            } else if (kotlin.math.abs(dy - cervixY) <= h * 0.07f && kotlin.math.abs(dx) < w * 0.12f) {
                                selectedPart = UterusPart.CERVIX
                            } else if (dy >= topY && dy <= cervixY && kotlin.math.abs(dx) <= w * 0.09f && dy < cervixY - h * 0.03f) {
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

                val topY = h * 0.20f
                val fundusW = w * 0.17f
                val isthmusY = h * 0.55f
                val isthmusW = w * 0.055f
                val cervixY = h * 0.64f
                val cervixW = w * 0.075f
                val vaginaBottomY = h * 0.94f
                val vaginaW = w * 0.095f

                val activePart = selectedPart

                // Facteur physiologique d'épaisseur et de réceptivité de l'endomètre selon le jour du cycle
                val endoFactor: Float = when (currentDay) {
                    1 -> 0.70f
                    2 -> 0.45f
                    3 -> 0.28f
                    4, 5 -> 0.15f
                    in 6..13 -> 0.18f + ((currentDay - 5) / 8f) * 0.65f
                    14 -> 0.90f
                    in 15..24 -> 1.0f
                    else -> 1.0f - ((currentDay - 24) / 4f) * 0.22f
                }

                // Opacité de mise en valeur (effet projecteur)
                fun dimAlpha(part: UterusPart, normalAlpha: Float): Float {
                    return if (activePart == null) normalAlpha else if (activePart == part) normalAlpha else normalAlpha * 0.35f
                }

                // =========================================================================
                // 1. Vagin & Culs-de-sac de la voûte vaginale (Fornices)
                // =========================================================================
                val isVaginaSelected = activePart == UterusPart.VAGINA
                val fornixLeft = cx - cervixW * 1.15f
                val fornixRight = cx + cervixW * 1.15f
                val fornixY = cervixY + h * 0.015f

                val vaginaPath = Path().apply {
                    moveTo(fornixLeft, fornixY)
                    cubicTo(fornixLeft - w * 0.02f, fornixY + h * 0.04f, cx - vaginaW, cervixY + h * 0.08f, cx - vaginaW, vaginaBottomY)
                    lineTo(cx + vaginaW, vaginaBottomY)
                    cubicTo(cx + vaginaW, cervixY + h * 0.08f, fornixRight + w * 0.02f, fornixY + h * 0.04f, fornixRight, fornixY)
                    cubicTo(cx + cervixW * 0.8f, cervixY + h * 0.04f, cx - cervixW * 0.8f, cervixY + h * 0.04f, fornixLeft, fornixY)
                    close()
                }

                val vaginaFill = if (isVaginaSelected) Color(0xFFCE93D8).copy(alpha = 0.80f) else Color(0xFFE1BEE7).copy(alpha = dimAlpha(UterusPart.VAGINA, 0.45f))
                val vaginaStroke = if (isVaginaSelected) colors.primary else colors.onSurfaceVariant.copy(alpha = dimAlpha(UterusPart.VAGINA, 0.60f))

                drawPath(vaginaPath, vaginaFill)
                drawPath(
                    vaginaPath,
                    vaginaStroke,
                    style = Stroke(width = if (isVaginaSelected) 2.6.dp.toPx() else 1.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                )

                // Crêtes et rides vaginales transversales (rugae vaginales)
                val rugaeColor = (if (isVaginaSelected) Color.White else colors.onSurfaceVariant).copy(alpha = dimAlpha(UterusPart.VAGINA, 0.45f))
                val rugaeYOffsets = listOf(0.06f, 0.11f, 0.16f, 0.21f, 0.26f)
                rugaeYOffsets.forEach { rY ->
                    val y = cervixY + h * rY
                    val rPath = Path().apply {
                        val rw = vaginaW * (0.65f + (rY - 0.06f) * 0.4f)
                        moveTo(cx - rw, y)
                        cubicTo(cx - rw * 0.4f, y - 2.dp.toPx(), cx + rw * 0.4f, y + 2.dp.toPx(), cx + rw, y)
                    }
                    drawPath(rPath, rugaeColor, style = Stroke(width = 1.3.dp.toPx(), cap = StrokeCap.Round))
                }

                // Menstruation : écoulement sanguin dans le vagin (J1 à J5)
                if (currentDay in 1..5) {
                    val mBlood = Color(0xFF991B1B).copy(alpha = dimAlpha(UterusPart.VAGINA, 0.90f))
                    val drops = listOf(
                        Triple(cx, cervixY + h * 0.05f, 2.6.dp.toPx()),
                        Triple(cx - 3.dp.toPx(), cervixY + h * 0.10f, 2.8.dp.toPx()),
                        Triple(cx + 3.5.dp.toPx(), cervixY + h * 0.15f, 2.5.dp.toPx()),
                        Triple(cx - 1.dp.toPx(), cervixY + h * 0.20f, 3.0.dp.toPx()),
                        Triple(cx + 2.dp.toPx(), cervixY + h * 0.25f, 2.4.dp.toPx()),
                    )
                    drops.forEach { (dx, dy, r) ->
                        drawCircle(mBlood, radius = r, center = Offset(dx, dy))
                    }
                }

                // =========================================================================
                // 2. Corps de l'utérus : Myomètre & Pérymètre anatomique
                // =========================================================================
                val isMyoSelected = activePart == UterusPart.MYOMETRIUM

                val uterusOuterPath = Path().apply {
                    moveTo(cx - isthmusW, isthmusY)
                    cubicTo(cx - isthmusW * 1.3f, isthmusY - h * 0.06f, cx - fundusW * 0.85f, topY + h * 0.12f, cx - fundusW, topY + h * 0.015f)
                    cubicTo(cx - fundusW * 0.95f, topY - h * 0.035f, cx - fundusW * 0.45f, topY - h * 0.045f, cx, topY - h * 0.045f)
                    cubicTo(cx + fundusW * 0.45f, topY - h * 0.045f, cx + fundusW * 0.95f, topY - h * 0.035f, cx + fundusW, topY + h * 0.015f)
                    cubicTo(cx + fundusW * 0.85f, topY + h * 0.12f, cx + isthmusW * 1.3f, isthmusY - h * 0.06f, cx + isthmusW, isthmusY)
                    cubicTo(cx + cervixW * 0.8f, cervixY - h * 0.02f, cx - cervixW * 0.8f, cervixY - h * 0.02f, cx - isthmusW, isthmusY)
                    close()
                }

                val myoFill = if (isMyoSelected) Color(0xFFE57373) else Color(0xFFEF9A9A).copy(alpha = dimAlpha(UterusPart.MYOMETRIUM, 0.60f))
                val myoStroke = if (isMyoSelected) colors.primary else colors.onSurfaceVariant.copy(alpha = dimAlpha(UterusPart.MYOMETRIUM, 0.75f))
                drawPath(uterusOuterPath, myoFill)

                // Faisceaux musculaires lisses du myomètre (fibres plexiformes spiralées)
                val muscleFiberColor = Color(0xFFC62828).copy(alpha = dimAlpha(UterusPart.MYOMETRIUM, if (isMyoSelected) 0.45f else 0.25f))
                val fiberPaths = listOf(
                    Path().apply {
                        moveTo(cx - fundusW * 0.82f, topY + h * 0.03f)
                        cubicTo(cx - fundusW * 0.4f, topY - h * 0.015f, cx + fundusW * 0.4f, topY - h * 0.015f, cx + fundusW * 0.82f, topY + h * 0.03f)
                    },
                    Path().apply {
                        moveTo(cx - fundusW * 0.70f, topY + h * 0.08f)
                        cubicTo(cx - fundusW * 0.35f, topY + h * 0.05f, cx + fundusW * 0.35f, topY + h * 0.05f, cx + fundusW * 0.70f, topY + h * 0.08f)
                    },
                    Path().apply {
                        moveTo(cx - fundusW * 0.60f, topY + h * 0.14f)
                        cubicTo(cx - fundusW * 0.25f, topY + h * 0.11f, cx + fundusW * 0.25f, topY + h * 0.11f, cx + fundusW * 0.60f, topY + h * 0.14f)
                    },
                )
                fiberPaths.forEach { fPath ->
                    drawPath(fPath, muscleFiberColor, style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round))
                }

                drawPath(
                    uterusOuterPath,
                    myoStroke,
                    style = Stroke(width = if (isMyoSelected) 2.8.dp.toPx() else 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                )

                // =========================================================================
                // 3. Col de l'utérus (Cervix) & Canal endocervical avec plis palmés
                // =========================================================================
                val isCervixSelected = activePart == UterusPart.CERVIX
                val cervixFill = if (isCervixSelected) Color(0xFFF06292).copy(alpha = 0.90f) else Color(0xFFF48FB1).copy(alpha = dimAlpha(UterusPart.CERVIX, 0.65f))
                val cervixStroke = if (isCervixSelected) colors.primary else Color(0xFFC2185B).copy(alpha = dimAlpha(UterusPart.CERVIX, 0.75f))

                // Museau de tanche (portio vaginalis) faisant saillie dans le vagin
                val cervixEctoPath = Path().apply {
                    moveTo(cx - cervixW, cervixY - h * 0.02f)
                    cubicTo(cx - cervixW * 1.1f, cervixY + h * 0.02f, cx - cervixW * 0.7f, cervixY + h * 0.045f, cx, cervixY + h * 0.045f)
                    cubicTo(cx + cervixW * 0.7f, cervixY + h * 0.045f, cx + cervixW * 1.1f, cervixY + h * 0.02f, cx + cervixW, cervixY - h * 0.02f)
                    cubicTo(cx + isthmusW, isthmusY, cx - isthmusW, isthmusY, cx - cervixW, cervixY - h * 0.02f)
                    close()
                }
                drawPath(cervixEctoPath, cervixFill)
                drawPath(
                    cervixEctoPath,
                    cervixStroke,
                    style = Stroke(width = if (isCervixSelected) 2.6.dp.toPx() else 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                )

                // Canal endocervical fusiforme (avec plicae palmatae / arbor vitae uteri)
                val canalWidth = when (currentDay) {
                    in 1..5 -> cervixW * 0.32f // Entrouvert pour l'écoulement des règles
                    14 -> cervixW * 0.28f // Dilaté et perméable pour l'ovulation
                    else -> cervixW * 0.16f // Rétracté sous progestérone
                }
                val endocervixPath = Path().apply {
                    moveTo(cx - canalWidth * 0.6f, isthmusY)
                    cubicTo(cx - canalWidth, cervixY, cx - canalWidth * 0.8f, cervixY + h * 0.035f, cx - canalWidth * 0.5f, cervixY + h * 0.042f)
                    lineTo(cx + canalWidth * 0.5f, cervixY + h * 0.042f)
                    cubicTo(cx + canalWidth * 0.8f, cervixY + h * 0.035f, cx + canalWidth, cervixY, cx + canalWidth * 0.6f, isthmusY)
                    close()
                }
                drawPath(endocervixPath, Color(0xFF880E4F).copy(alpha = dimAlpha(UterusPart.CERVIX, 0.80f)))

                // Plis palmés endocervicaux (arbor vitae uteri)
                val plicaeAlpha = dimAlpha(UterusPart.CERVIX, 0.55f)
                val palmY1 = isthmusY + h * 0.025f
                val palmY2 = isthmusY + h * 0.055f
                drawLine(Color(0xFFFFEBEE).copy(alpha = plicaeAlpha), Offset(cx - canalWidth * 0.5f, palmY1 - 2.dp.toPx()), Offset(cx, palmY1), strokeWidth = 1.dp.toPx())
                drawLine(Color(0xFFFFEBEE).copy(alpha = plicaeAlpha), Offset(cx + canalWidth * 0.5f, palmY1 - 2.dp.toPx()), Offset(cx, palmY1), strokeWidth = 1.dp.toPx())
                drawLine(Color(0xFFFFEBEE).copy(alpha = plicaeAlpha), Offset(cx - canalWidth * 0.5f, palmY2 - 2.dp.toPx()), Offset(cx, palmY2), strokeWidth = 1.dp.toPx())
                drawLine(Color(0xFFFFEBEE).copy(alpha = plicaeAlpha), Offset(cx + canalWidth * 0.5f, palmY2 - 2.dp.toPx()), Offset(cx, palmY2), strokeWidth = 1.dp.toPx())

                // Orifice externe du col (Os externum)
                val osRadius = when (currentDay) {
                    in 1..5 -> canalWidth * 0.45f
                    14 -> canalWidth * 0.40f
                    else -> canalWidth * 0.25f
                }
                drawCircle(Color(0xFF4A0E17).copy(alpha = dimAlpha(UterusPart.CERVIX, 0.90f)), radius = osRadius, center = Offset(cx, cervixY + h * 0.042f))

                // Glaire cervicale fertile (J14) ou bouchon muqueux (phase lutéale)
                if (currentDay == 14) {
                    val mucusPath = Path().apply {
                        moveTo(cx - 2.dp.toPx(), cervixY + h * 0.042f)
                        cubicTo(cx - 3.dp.toPx(), cervixY + h * 0.07f, cx - 1.dp.toPx(), cervixY + h * 0.10f, cx, cervixY + h * 0.13f)
                        cubicTo(cx + 1.dp.toPx(), cervixY + h * 0.10f, cx + 3.dp.toPx(), cervixY + h * 0.07f, cx + 2.dp.toPx(), cervixY + h * 0.042f)
                        close()
                    }
                    drawPath(mucusPath, Color(0xFFE0F7FA).copy(alpha = dimAlpha(UterusPart.CERVIX, 0.90f)))
                    drawCircle(Color(0xFFB2EBF2).copy(alpha = dimAlpha(UterusPart.CERVIX, 0.95f)), radius = 2.5.dp.toPx(), center = Offset(cx, cervixY + h * 0.13f))
                } else if (currentDay in 15..28) {
                    drawCircle(Color(0xFFFFCDD2).copy(alpha = dimAlpha(UterusPart.CERVIX, 0.85f)), radius = canalWidth * 0.5f, center = Offset(cx, cervixY + h * 0.038f))
                }

                // =========================================================================
                // 4. Cavité utérine & Endomètre (Couche basale + couche fonctionnelle cyclique)
                // =========================================================================
                val isEndoSelected = activePart == UterusPart.ENDOMETRIUM
                val cornuY = topY + h * 0.055f
                val cornuXLeft = cx - fundusW * 0.52f
                val cornuXRight = cx + fundusW * 0.52f
                val internalOsY = isthmusY

                val cavityLumenPath = Path().apply {
                    moveTo(cornuXLeft, cornuY)
                    cubicTo(cx - fundusW * 0.20f, cornuY - h * 0.02f, cx + fundusW * 0.20f, cornuY - h * 0.02f, cornuXRight, cornuY)
                    cubicTo(cx + fundusW * 0.26f, topY + h * 0.18f, cx + isthmusW * 0.8f, internalOsY - h * 0.06f, cx + isthmusW * 0.5f, internalOsY)
                    lineTo(cx - isthmusW * 0.5f, internalOsY)
                    cubicTo(cx - isthmusW * 0.8f, internalOsY - h * 0.06f, cx - fundusW * 0.26f, topY + h * 0.18f, cornuXLeft, cornuY)
                    close()
                }

                // Couche basale permanente
                drawPath(cavityLumenPath, Color(0xFF991B1B).copy(alpha = dimAlpha(UterusPart.ENDOMETRIUM, 0.35f)))

                // Teinte de la couche fonctionnelle
                val endoFillColor = when (currentDay) {
                    in 1..5 -> Color(0xFF7F1D1D).copy(alpha = dimAlpha(UterusPart.ENDOMETRIUM, 0.60f))
                    in 6..13 -> Color(0xFFE11D48).copy(alpha = dimAlpha(UterusPart.ENDOMETRIUM, 0.30f + endoFactor * 0.45f))
                    14 -> Color(0xFFBE123C).copy(alpha = dimAlpha(UterusPart.ENDOMETRIUM, 0.85f))
                    else -> Color(0xFF9F1239).copy(alpha = dimAlpha(UterusPart.ENDOMETRIUM, 0.88f))
                }
                drawPath(cavityLumenPath, endoFillColor)

                val baseEndoWidth = (1.8.dp + 5.2.dp * endoFactor).toPx()
                val endoBorderColor = if (isEndoSelected) colors.primary else Color(0xFF880E4F).copy(alpha = dimAlpha(UterusPart.ENDOMETRIUM, 0.85f))
                drawPath(
                    path = cavityLumenPath,
                    color = endoBorderColor,
                    style = Stroke(width = if (isEndoSelected) baseEndoWidth + 2.dp.toPx() else baseEndoWidth, join = StrokeJoin.Round),
                )

                // Détails histologiques dynamiques
                if (currentDay in 1..5) {
                    val sloughColor = Color(0xFFFF8A80).copy(alpha = dimAlpha(UterusPart.ENDOMETRIUM, 0.75f))
                    val sloughs = listOf(
                        Triple(cx - fundusW * 0.15f, cornuY + h * 0.06f, 3.dp.toPx()),
                        Triple(cx + fundusW * 0.10f, cornuY + h * 0.10f, 3.5.dp.toPx()),
                        Triple(cx - fundusW * 0.05f, cornuY + h * 0.16f, 2.8.dp.toPx()),
                        Triple(cx + fundusW * 0.08f, cornuY + h * 0.20f, 3.2.dp.toPx()),
                    )
                    sloughs.forEach { (sx, sy, sr) ->
                        drawCircle(sloughColor, radius = sr, center = Offset(sx, sy))
                    }
                } else if (currentDay in 6..13) {
                    val glandColor = Color(0xFFFFCDD2).copy(alpha = dimAlpha(UterusPart.ENDOMETRIUM, 0.50f))
                    for (gx in listOf(-0.25f, -0.10f, 0.05f, 0.20f)) {
                        val startX = cx + fundusW * gx
                        val startY = cornuY + h * 0.04f
                        val glandH = h * (0.06f + (currentDay - 5) * 0.012f)
                        drawLine(
                            color = glandColor,
                            start = Offset(startX, startY),
                            end = Offset(startX, startY + glandH),
                            strokeWidth = 1.4.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }
                } else if (currentDay in 14..28) {
                    val spiralColor = Color(0xFFFFCDD2).copy(alpha = dimAlpha(UterusPart.ENDOMETRIUM, 0.75f))
                    val sY = cornuY + h * 0.03f
                    val spiral1 = Path().apply {
                        moveTo(cx - fundusW * 0.22f, sY)
                        cubicTo(cx - fundusW * 0.16f, sY + h * 0.03f, cx - fundusW * 0.26f, sY + h * 0.07f, cx - fundusW * 0.18f, sY + h * 0.11f)
                        cubicTo(cx - fundusW * 0.12f, sY + h * 0.14f, cx - fundusW * 0.20f, sY + h * 0.17f, cx - fundusW * 0.14f, sY + h * 0.20f)
                    }
                    val spiral2 = Path().apply {
                        moveTo(cx, sY - h * 0.01f)
                        cubicTo(cx + fundusW * 0.08f, sY + h * 0.03f, cx - fundusW * 0.08f, sY + h * 0.07f, cx + fundusW * 0.06f, sY + h * 0.11f)
                        cubicTo(cx - fundusW * 0.04f, sY + h * 0.15f, cx + fundusW * 0.04f, sY + h * 0.18f, cx, sY + h * 0.21f)
                    }
                    val spiral3 = Path().apply {
                        moveTo(cx + fundusW * 0.22f, sY)
                        cubicTo(cx + fundusW * 0.26f, sY + h * 0.03f, cx + fundusW * 0.16f, sY + h * 0.07f, cx + fundusW * 0.24f, sY + h * 0.11f)
                        cubicTo(cx + fundusW * 0.16f, sY + h * 0.14f, cx + fundusW * 0.22f, sY + h * 0.17f, cx + fundusW * 0.16f, sY + h * 0.20f)
                    }
                    drawPath(spiral1, spiralColor, style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round))
                    drawPath(spiral2, spiralColor, style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round))
                    drawPath(spiral3, spiralColor, style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round))
                }

                // =========================================================================
                // 5. Trompes de Fallope (Salpinx) : Isthme, Ampoule, Pavillon & Franges
                // =========================================================================
                val isTubesSelected = activePart == UterusPart.TUBES
                val tubeColor = if (isTubesSelected) Color(0xFFFF7043) else Color(0xFFFFAB91).copy(alpha = dimAlpha(UterusPart.TUBES, 0.75f))
                val tubeBorderColor = if (isTubesSelected) colors.primary else Color(0xFFD84315).copy(alpha = dimAlpha(UterusPart.TUBES, 0.75f))

                val tubePaths = mutableListOf<Path>()

                for (side in listOf(-1f, 1f)) {
                    val isthmusStartX = cx + side * fundusW * 0.95f
                    val isthmusStartY = topY + h * 0.015f
                    val ampullaApexX = cx + side * w * 0.30f
                    val ampullaApexY = topY - h * 0.065f
                    val infundibulumX = cx + side * w * 0.41f
                    val infundibulumY = topY + h * 0.09f

                    val tubeOuter = Path().apply {
                        moveTo(isthmusStartX, isthmusStartY - 2.5.dp.toPx())
                        cubicTo(
                            cx + side * w * 0.24f, ampullaApexY,
                            cx + side * w * 0.36f, ampullaApexY + h * 0.02f,
                            infundibulumX + side * 4.dp.toPx(), infundibulumY,
                        )
                        // Évasement en pavillon infundibulaire
                        lineTo(infundibulumX - side * 4.dp.toPx(), infundibulumY + h * 0.035f)
                        cubicTo(
                            cx + side * w * 0.36f, ampullaApexY + h * 0.05f,
                            cx + side * w * 0.24f, ampullaApexY + h * 0.03f,
                            isthmusStartX, isthmusStartY + 2.5.dp.toPx(),
                        )
                        close()
                    }
                    tubePaths.add(tubeOuter)
                    drawPath(tubeOuter, tubeColor)
                    drawPath(tubeOuter, tubeBorderColor, style = Stroke(width = if (isTubesSelected) 2.4.dp.toPx() else 1.4.dp.toPx()))

                    // Pavillon et franges tubaires (Fimbriae) : lobes doux et galbés entourant l'ovaire
                    val fimbriaeCount = 5
                    for (f in 0 until fimbriaeCount) {
                        val fRatio = f.toFloat() / (fimbriaeCount - 1)
                        val fLen = (8f + kotlin.math.sin(f * 1.5f) * 3f).dp.toPx()
                        val fxStart = infundibulumX + side * (fRatio * 4.dp.toPx() - 2.dp.toPx())
                        val fyStart = infundibulumY + h * (0.015f + fRatio * 0.025f)
                        val fxEnd = fxStart + side * (fLen * 0.7f)
                        val fyEnd = fyStart + (fRatio - 0.5f) * fLen

                        val fPetal = Path().apply {
                            moveTo(fxStart, fyStart)
                            cubicTo(
                                fxStart + side * 4.dp.toPx(), (fyStart + fyEnd) / 2f,
                                fxEnd - side * 2.dp.toPx(), (fyStart + fyEnd) / 2f,
                                fxEnd, fyEnd,
                            )
                        }
                        val fColor = if (side == -1f && currentDay == 14) Color(0xFFE91E63) else tubeBorderColor
                        drawPath(fPetal, fColor, style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))
                    }
                }

                // =========================================================================
                // 6. Ovaires & Ligament utéro-ovarien (sans traits flottants hors-cadre)
                // =========================================================================
                val isOvariesSelected = activePart == UterusPart.OVARIES
                val ovaryColor = if (isOvariesSelected) Color(0xFFFFB300) else Color(0xFFFFE082).copy(alpha = dimAlpha(UterusPart.OVARIES, 0.75f))
                val ovaryBorderColor = if (isOvariesSelected) colors.primary else colors.onSurfaceVariant.copy(alpha = dimAlpha(UterusPart.OVARIES, 0.65f))
                val leftOvaryPos = Offset(cx - w * 0.36f, topY + h * 0.22f)
                val rightOvaryPos = Offset(cx + w * 0.36f, topY + h * 0.22f)
                val ovaryW = w * 0.13f
                val ovaryH = h * 0.11f

                val ovaryPaths = mutableListOf<Path>()

                for (side in listOf(-1f, 1f)) {
                    val oPos = if (side == -1f) leftOvaryPos else rightOvaryPos

                    // Ligament propre de l'ovaire (utéro-ovarien) reliant le pôle inférieur à la corne utérine
                    val uteroOvarianLigPath = Path().apply {
                        moveTo(cx + side * fundusW * 0.85f, topY + h * 0.10f)
                        cubicTo(
                            cx + side * (fundusW * 0.9f + w * 0.05f), topY + h * 0.16f,
                            oPos.x - side * ovaryW * 0.18f, oPos.y + ovaryH * 0.1f,
                            oPos.x - side * ovaryW * 0.30f, oPos.y + ovaryH * 0.18f,
                        )
                    }
                    val ligColor = if (isOvariesSelected) colors.primary.copy(alpha = 0.85f) else Color(0xFFBCAAA4).copy(alpha = dimAlpha(UterusPart.OVARIES, 0.65f))
                    drawPath(uteroOvarianLigPath, ligColor, style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))

                    // Forme amygdaloïde / amande anatomique de l'ovaire
                    val ovaryPath = Path().apply {
                        moveTo(oPos.x - ovaryW * 0.5f, oPos.y)
                        cubicTo(oPos.x - ovaryW * 0.45f, oPos.y - ovaryH * 0.55f, oPos.x + ovaryW * 0.45f, oPos.y - ovaryH * 0.45f, oPos.x + ovaryW * 0.5f, oPos.y)
                        cubicTo(oPos.x + ovaryW * 0.45f, oPos.y + ovaryH * 0.55f, oPos.x - ovaryW * 0.45f, oPos.y + ovaryH * 0.45f, oPos.x - ovaryW * 0.5f, oPos.y)
                        close()
                    }
                    ovaryPaths.add(ovaryPath)
                    drawPath(ovaryPath, ovaryColor)
                    drawOval(
                        color = Color(0xFFFFCC80).copy(alpha = dimAlpha(UterusPart.OVARIES, 0.45f)),
                        topLeft = Offset(oPos.x - ovaryW * 0.28f, oPos.y - ovaryH * 0.25f),
                        size = androidx.compose.ui.geometry.Size(ovaryW * 0.56f, ovaryH * 0.50f),
                    )
                    drawPath(
                        ovaryPath,
                        ovaryBorderColor,
                        style = Stroke(width = if (isOvariesSelected) 2.6.dp.toPx() else 1.4.dp.toPx()),
                    )
                }

                // Dynamique du cycle ovarien sur l'ovaire gauche
                val oAlpha = dimAlpha(UterusPart.OVARIES, 1f)
                when (currentDay) {
                    in 1..5 -> {
                        drawCircle(Color(0xFFFFF9C4).copy(alpha = oAlpha), radius = 2.8.dp.toPx(), center = Offset(leftOvaryPos.x - 6.dp.toPx(), leftOvaryPos.y - 4.dp.toPx()))
                        drawCircle(Color(0xFFFFF9C4).copy(alpha = oAlpha), radius = 2.4.dp.toPx(), center = Offset(leftOvaryPos.x + 5.dp.toPx(), leftOvaryPos.y - 5.dp.toPx()))
                        drawCircle(Color(0xFFFFF9C4).copy(alpha = oAlpha), radius = 2.2.dp.toPx(), center = Offset(leftOvaryPos.x, leftOvaryPos.y + 6.dp.toPx()))
                    }
                    in 6..13 -> {
                        val fRadius = (3.5f + (currentDay - 5) * 0.70f).dp.toPx()
                        val graafCenter = Offset(leftOvaryPos.x - ovaryW * 0.12f, leftOvaryPos.y - ovaryH * 0.15f)
                        drawCircle(Color(0xFFFFB300).copy(alpha = oAlpha), radius = fRadius, center = graafCenter)
                        drawCircle(Color(0xFFFFF59D).copy(alpha = oAlpha), radius = fRadius * 0.70f, center = graafCenter)
                        drawCircle(Color(0xFFE65100).copy(alpha = oAlpha), radius = 2.4.dp.toPx(), center = Offset(graafCenter.x + fRadius * 0.25f, graafCenter.y))
                        drawCircle(Color(0xFFFFF9C4).copy(alpha = oAlpha * 0.6f), radius = 2.2.dp.toPx(), center = Offset(leftOvaryPos.x + 7.dp.toPx(), leftOvaryPos.y + 4.dp.toPx()))
                    }
                    14 -> {
                        val stigmaCenter = Offset(leftOvaryPos.x - ovaryW * 0.25f, leftOvaryPos.y - ovaryH * 0.28f)
                        drawCircle(Color(0xFFFF8F00).copy(alpha = oAlpha), radius = 5.dp.toPx(), center = stigmaCenter)
                        val ovocytePos = Offset(cx - w * 0.38f, topY + h * 0.15f)
                        drawCircle(Color(0xFFFBC02D).copy(alpha = oAlpha * 0.35f), radius = 9.dp.toPx(), center = ovocytePos)
                        drawCircle(Color(0xFFFFF9C4).copy(alpha = oAlpha), radius = 4.5.dp.toPx(), center = ovocytePos)
                        drawCircle(Color(0xFFE65100).copy(alpha = oAlpha), radius = 4.5.dp.toPx(), center = ovocytePos, style = Stroke(width = 1.6.dp.toPx()))
                        drawCircle(Color(0xFFBF360C).copy(alpha = oAlpha), radius = 1.3.dp.toPx(), center = Offset(ovocytePos.x + 2.dp.toPx(), ovocytePos.y - 2.dp.toPx()))
                    }
                    in 15..18 -> {
                        drawCircle(Color(0xFFFB8C00).copy(alpha = oAlpha), radius = 7.0.dp.toPx(), center = leftOvaryPos)
                        drawCircle(Color(0xFFFFD54F).copy(alpha = oAlpha), radius = 4.0.dp.toPx(), center = leftOvaryPos)
                        val progress = (currentDay - 15) / 3f
                        val transitX = cx - w * (0.37f - progress * 0.09f)
                        val transitY = topY - h * (0.04f - progress * 0.04f)
                        drawCircle(Color(0xFFFFF9C4).copy(alpha = oAlpha), radius = 3.6.dp.toPx(), center = Offset(transitX, transitY))
                        drawCircle(Color(0xFFE65100).copy(alpha = oAlpha), radius = 3.6.dp.toPx(), center = Offset(transitX, transitY), style = Stroke(width = 1.3.dp.toPx()))
                    }
                    in 19..24 -> {
                        val cjCenter = leftOvaryPos
                        val cjPath = Path().apply {
                            val cjR = 8.5.dp.toPx()
                            val lobes = 8
                            for (lb in 0 until lobes) {
                                val a = (lb.toFloat() / lobes) * 2f * PI.toFloat()
                                val rad = cjR * (if (lb % 2 == 0) 1.0f else 0.82f)
                                val lx = cjCenter.x + kotlin.math.cos(a) * rad
                                val ly = cjCenter.y + kotlin.math.sin(a) * rad
                                if (lb == 0) moveTo(lx, ly) else lineTo(lx, ly)
                            }
                            close()
                        }
                        drawPath(cjPath, Color(0xFFFB8C00).copy(alpha = oAlpha))
                        drawCircle(Color(0xFFFFEE58).copy(alpha = oAlpha), radius = 4.5.dp.toPx(), center = cjCenter)
                        drawCircle(Color(0xFFE65100).copy(alpha = oAlpha), radius = 2.0.dp.toPx(), center = cjCenter)
                    }
                    else -> {
                        val regrRadius = (5.5f - (currentDay - 24) * 0.7f).coerceAtLeast(2.8f).dp.toPx()
                        drawCircle(Color(0xFFECEFF1).copy(alpha = oAlpha), radius = regrRadius, center = leftOvaryPos)
                        drawCircle(Color(0xFFCFD8DC).copy(alpha = oAlpha), radius = regrRadius, center = leftOvaryPos, style = Stroke(width = 1.2.dp.toPx()))
                    }
                }

                // Ovaire droit (controlatéral)
                drawCircle(Color(0xFFFFF9C4).copy(alpha = oAlpha * 0.65f), radius = 2.5.dp.toPx(), center = Offset(rightOvaryPos.x - 5.dp.toPx(), rightOvaryPos.y - 3.dp.toPx()))
                drawCircle(Color(0xFFFFF9C4).copy(alpha = oAlpha * 0.65f), radius = 2.2.dp.toPx(), center = Offset(rightOvaryPos.x + 4.dp.toPx(), rightOvaryPos.y + 4.dp.toPx()))
                drawCircle(Color(0xFFCFD8DC).copy(alpha = oAlpha * 0.50f), radius = 2.0.dp.toPx(), center = Offset(rightOvaryPos.x - 2.dp.toPx(), rightOvaryPos.y + 5.dp.toPx()))

                // =========================================================================
                // 7. Halo lumineux et mise en valeur de l'organe sélectionné
                // =========================================================================
                if (activePart != null) {
                    val focusHaloColor = colors.primary.copy(alpha = 0.28f)
                    val focusStrokeColor = colors.primary

                    when (activePart) {
                        UterusPart.ENDOMETRIUM -> {
                            drawPath(cavityLumenPath, focusHaloColor, style = Stroke(width = baseEndoWidth + 6.dp.toPx(), join = StrokeJoin.Round))
                            drawPath(cavityLumenPath, focusStrokeColor, style = Stroke(width = baseEndoWidth + 2.dp.toPx(), join = StrokeJoin.Round))
                        }
                        UterusPart.MYOMETRIUM -> {
                            drawPath(uterusOuterPath, focusHaloColor, style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                            drawPath(uterusOuterPath, focusStrokeColor, style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                        }
                        UterusPart.CERVIX -> {
                            drawPath(cervixEctoPath, focusHaloColor, style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                            drawPath(cervixEctoPath, focusStrokeColor, style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                        }
                        UterusPart.TUBES -> {
                            tubePaths.forEach { tPath ->
                                drawPath(tPath, focusHaloColor, style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                                drawPath(tPath, focusStrokeColor, style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                            }
                        }
                        UterusPart.OVARIES -> {
                            ovaryPaths.forEach { oPath ->
                                drawPath(oPath, focusHaloColor, style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                                drawPath(oPath, focusStrokeColor, style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                            }
                        }
                        UterusPart.VAGINA -> {
                            drawPath(vaginaPath, focusHaloColor, style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                            drawPath(vaginaPath, focusStrokeColor, style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                        }
                    }
                }

                // =========================================================================
                // 8. Badge interactif et indicateur de sélection
                // =========================================================================
                if (activePart != null) {
                    val label = when (activePart) {
                        UterusPart.ENDOMETRIUM -> tr("Endomètre", "Endometrium", pl = "Endometrium", uk = "Ендометрій", es = "Endometrio", de = "Endometrium")
                        UterusPart.MYOMETRIUM -> tr("Myomètre", "Myometrium", pl = "Miometrium", uk = "Міометрій", es = "Miometrio", de = "Myometrium")
                        UterusPart.CERVIX -> tr("Col de l'utérus", "Cervix", pl = "Szyjka macicy", uk = "Шийка матки", es = "Cuello del útero", de = "Gebärmutterhals")
                        UterusPart.TUBES -> tr("Trompes de Fallope", "Fallopian tubes", pl = "Jajowody", uk = "Маткові труби", es = "Trompas de Falopio", de = "Eileiter")
                        UterusPart.OVARIES -> tr("Ovaires", "Ovaries", pl = "Jajniki", uk = "Яєчники", es = "Ovarios", de = "Eierstöcke")
                        UterusPart.VAGINA -> tr("Vagin", "Vagina", pl = "Pochwa", uk = "Піхва", es = "Vagina", de = "Vagina")
                    }

                    val pin = when (activePart) {
                        UterusPart.ENDOMETRIUM -> Offset(cx, cornuY + h * 0.08f)
                        UterusPart.MYOMETRIUM -> Offset(cx - fundusW * 0.70f, topY + h * 0.12f)
                        UterusPart.CERVIX -> Offset(cx, cervixY + h * 0.02f)
                        UterusPart.TUBES -> Offset(cx + w * 0.28f, topY - h * 0.03f)
                        UterusPart.OVARIES -> Offset(leftOvaryPos.x, leftOvaryPos.y)
                        UterusPart.VAGINA -> Offset(cx, cervixY + h * 0.15f)
                    }

                    val badgePos = when (activePart) {
                        UterusPart.ENDOMETRIUM -> Offset(cx, topY - h * 0.10f)
                        UterusPart.MYOMETRIUM -> Offset(cx - fundusW * 0.90f, topY + h * 0.32f)
                        UterusPart.CERVIX -> Offset(cx + cervixW * 2.5f, cervixY + h * 0.02f)
                        UterusPart.TUBES -> Offset(cx + w * 0.28f, topY - h * 0.12f)
                        UterusPart.OVARIES -> Offset(leftOvaryPos.x, leftOvaryPos.y + h * 0.12f)
                        UterusPart.VAGINA -> Offset(cx + vaginaW * 1.8f, cervixY + h * 0.15f)
                    }

                    val textLayout = measurer.measure(
                        text = label,
                        style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.onPrimary),
                    )

                    val padH = 8.dp.toPx()
                    val padV = 4.dp.toPx()
                    val badgeW = textLayout.size.width + padH * 2
                    val badgeH = textLayout.size.height + padV * 2
                    val badgeLeft = (badgePos.x - badgeW / 2f).coerceIn(8.dp.toPx(), w - badgeW - 8.dp.toPx())
                    val badgeTop = (badgePos.y - badgeH / 2f).coerceIn(6.dp.toPx(), h - badgeH - 6.dp.toPx())

                    val leaderEnd = Offset(
                        x = pin.x.coerceIn(badgeLeft, badgeLeft + badgeW),
                        y = if (pin.y < badgeTop) badgeTop else if (pin.y > badgeTop + badgeH) badgeTop + badgeH else (badgeTop + badgeH / 2f),
                    )

                    drawLine(
                        color = colors.primary.copy(alpha = 0.85f),
                        start = pin,
                        end = leaderEnd,
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx())),
                    )

                    drawCircle(colors.primary, radius = 4.dp.toPx(), center = pin)
                    drawCircle(Color.White, radius = 1.8.dp.toPx(), center = pin)

                    drawRoundRect(
                        color = colors.primary,
                        topLeft = Offset(badgeLeft, badgeTop),
                        size = Size(badgeW, badgeH),
                        cornerRadius = CornerRadius(6.dp.toPx()),
                    )
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(badgeLeft + padH, badgeTop + padV),
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
                                    UterusPart.MYOMETRIUM -> Color(0xFFE57373)
                                    UterusPart.CERVIX -> Color(0xFFD81B60)
                                    UterusPart.TUBES -> Color(0xFFFF7043)
                                    UterusPart.OVARIES -> Color(0xFFFFB300)
                                    UterusPart.VAGINA -> Color(0xFFAB47BC)
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
    val context = rememberPlatformContext()

    Page {
        PageTitle(
            tr("Endométriose & douleurs pelviennes", "Endometriosis & pelvic pain", pl = "Endometrioza i ból miednicy", uk = "Ендометріоз і тазовий біль", es = "Endometriosis y dolor pélvico", de = "Endometriose und Beckenschmerzen"),
            onBack = onBack,
            subtitle = tr("Repères, signaux d'alerte et pistes de soulagement.", "Indicators, warning signs and everyday relief.", pl = "Wskazówki, sygnały alarmowe i sposoby łagodzenia dolegliwości.", uk = "Орієнтири, тривожні сигнали та способи полегшення.", es = "Pautas, señales de alerta y vías de alivio.", de = "Orientierung, Warnzeichen und Wege zur Linderung."),
        )

        SectionTitle(tr("Qu'est-ce que l'endométriose ?", "What is endometriosis?", pl = "Czym jest endometrioza?", uk = "Що таке ендометріоз?", es = "¿Qué es la endometriosis?", de = "Was ist Endometriose?"))
        LunaCard {
            Text(
                tr(
                    fr = "L'endométriose est une affection gynécologique chronique dans laquelle du tissu semblable à la muqueuse utérine (l'endomètre) se développe hors de l'utérus (sur les ovaires, les trompes, les ligaments utérins, le péritoine, la vessie ou les intestins).",
                    en = "Endometriosis is a chronic condition in which tissue similar to the lining of the uterus (the endometrium) grows outside the uterus (on ovaries, tubes, ligaments, peritoneum, bladder or bowels).",
                    pl = "Endometrioza to przewlekła choroba ginekologiczna, w której tkanka podobna do błony śluzowej macicy (endometrium) rozwija się poza macicą (na jajnikach, jajowodach, więzadłach macicy, otrzewnej, pęcherzu lub jelitach).",
                    uk = "Ендометріоз є хронічним гінекологічним захворюванням, за якого тканина, подібна до слизової оболонки матки (ендометрія), розростається поза маткою (на яєчниках, маткових трубах, зв'язках матки, очеревині, сечовому міхурі або кишківнику).",
                    es = "La endometriosis es una afección ginecológica crónica en la que un tejido semejante a la mucosa uterina (el endometrio) crece fuera del útero (en los ovarios, las trompas, los ligamentos uterinos, el peritoneo, la vejiga o los intestinos).",
                    de = "Endometriose ist eine chronische gynäkologische Erkrankung, bei der gebärmutterschleimhautähnliches Gewebe (Endometrium) außerhalb der Gebärmutter wächst (an Eierstöcken, Eileitern, Gebärmutterbändern, Bauchfell, Blase oder Darm).",
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                tr(
                    fr = "Sous l'influence des hormones du cycle, ces lésions s'épaississent et saignent à chaque cycle, provoquant une inflammation locale, des adhérences et des douleurs.",
                    en = "Influenced by hormonal variations, these lesions thicken and bleed each cycle, causing local inflammation, adhesions and pain.",
                    pl = "Pod wpływem hormonów cyklu zmiany te pogrubiają się i krwawią w każdym cyklu, powodując miejscowy stan zapalny, zrosty i ból.",
                    uk = "Під впливом гормонів циклу ці осередки потовщуються й кровоточать у кожному циклі, спричиняючи локальне запалення, спайки та біль.",
                    es = "Bajo la influencia de las hormonas del ciclo, estas lesiones se engrosan y sangran en cada ciclo, provocando inflamación local, adherencias y dolor.",
                    de = "Unter dem Einfluss der Zyklushormone verdicken sich diese Herde und bluten in jedem Zyklus, was lokale Entzündungen, Verwachsungen und Schmerzen verursacht.",
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Bullet(tr("Fréquence : elle concerne environ 1 personne sur 10 en âge de procréer (OMS / Inserm).", "Prevalence: affects approximately 1 in 10 individuals of reproductive age (WHO / Inserm).", pl = "Częstość: dotyczy około 1 osoby na 10 w wieku rozrodczym (WHO / Inserm).", uk = "Поширеність: стосується приблизно 1 людини з 10 дітородного віку (ВООЗ / Inserm).", es = "Frecuencia: afecta a aproximadamente 1 de cada 10 personas en edad reproductiva (OMS / Inserm).", de = "Häufigkeit: Betroffen ist etwa 1 von 10 Personen im gebärfähigen Alter (WHO / Inserm)."))
            Bullet(tr("Une règle essentielle : une douleur de règles qui empêche d'aller en cours, au travail ou de vivre normalement n'est pas normale.", "Key takeaway: menstrual pain that prevents you from going to work, school or daily activities is not normal.", pl = "Podstawowa zasada: ból miesiączkowy, który uniemożliwia chodzenie na zajęcia, do pracy lub normalne życie, nie jest normalny.", uk = "Головне правило: менструальний біль, через який неможливо піти на навчання чи роботу або жити звичайним життям, не є нормою.", es = "Una regla esencial: un dolor menstrual que impide ir a clase, al trabajo o llevar una vida normal no es normal.", de = "Eine wichtige Regel: Regelschmerzen, die verhindern, zur Schule oder zur Arbeit zu gehen oder normal zu leben, sind nicht normal."))
        }

        SectionTitle(tr("Les 5 « D » : les principaux signaux d'alerte", "The 5 'Ds': primary warning signs", pl = "5 „D”: główne sygnały alarmowe", uk = "5 «Д»: основні тривожні сигнали", es = "Las 5 «D»: las principales señales de alerta", de = "Die 5 „D“: die wichtigsten Warnzeichen"))
        LunaCard {
            Text(
                tr(
                    fr = "Les sociétés savantes (HAS, ESHRE) retiennent 5 manifestations douloureuses caractéristiques à surveiller :",
                    en = "Medical societies (HAS, ESHRE) highlight 5 characteristic pain symptoms to monitor:",
                    pl = "Towarzystwa naukowe (HAS, ESHRE) wskazują 5 charakterystycznych objawów bólowych, na które warto zwracać uwagę:",
                    uk = "Наукові товариства (HAS, ESHRE) виділяють 5 характерних больових проявів, за якими варто стежити:",
                    es = "Las sociedades científicas (HAS, ESHRE) señalan 5 manifestaciones dolorosas características que conviene vigilar:",
                    de = "Die Fachgesellschaften (HAS, ESHRE) nennen 5 charakteristische Schmerzzeichen, auf die man achten sollte:",
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Bullet(tr("Dysménorrhée : règles très douloureuses qui résistent aux antalgiques usuels (paracétamol, anti-inflammatoires).", "Dysmenorrhea: severe period pain that resists standard pain relievers.", pl = "Dysmenorrhea: bardzo bolesne miesiączki, które nie ustępują po zwykłych lekach przeciwbólowych (paracetamol, leki przeciwzapalne).", uk = "Дисменорея: дуже болісні менструації, що не піддаються звичайним знеболювальним (парацетамол, протизапальні засоби).", es = "Dismenorrea: reglas muy dolorosas que no ceden con los analgésicos habituales (paracetamol, antiinflamatorios).", de = "Dysmenorrhoe: sehr schmerzhafte Perioden, die auf übliche Schmerzmittel nicht ansprechen (Paracetamol, Entzündungshemmer)."))
            Bullet(tr("Dyspareunie : douleurs profondes pendant ou après les rapports sexuels.", "Dyspareunia: deep pain during or after sexual intercourse.", pl = "Dyspareunia: głębokie bóle podczas lub po stosunku płciowym.", uk = "Диспареунія: глибокий біль під час або після статевого акту.", es = "Dispareunia: dolor profundo durante o después de las relaciones sexuales.", de = "Dyspareunie: tiefe Schmerzen während oder nach dem Geschlechtsverkehr."))
            Bullet(tr("Dyschésie : douleurs lors du transit ou de la défécation, accentuées pendant les règles.", "Dyschezia: painful bowel movements, especially during periods.", pl = "Dyschezja: ból podczas pasażu jelitowego lub wypróżniania, nasilający się podczas miesiączki.", uk = "Дисхезія: біль під час кишкового транзиту чи дефекації, що посилюється під час менструації.", es = "Disquecia: dolor durante el tránsito intestinal o la defecación, que empeora durante la regla.", de = "Dyschezie: Schmerzen bei der Darmpassage oder beim Stuhlgang, die sich während der Periode verstärken."))
            Bullet(tr("Dysurie : douleurs, brûlures ou difficultés à uriner pendant les règles, sans infection bactérienne.", "Dysuria: painful or difficult urination during periods, without bacterial infection.", pl = "Dyzuria: ból, pieczenie lub trudności w oddawaniu moczu podczas miesiączki, bez zakażenia bakteryjnego.", uk = "Дизурія: біль, печіння або утруднене сечовипускання під час менструації без бактеріальної інфекції.", es = "Disuria: dolor, ardor o dificultad para orinar durante la regla, sin infección bacteriana.", de = "Dysurie: Schmerzen, Brennen oder Schwierigkeiten beim Wasserlassen während der Periode, ohne bakterielle Infektion."))
            Bullet(tr("Douleurs pelviennes chroniques : douleurs continues dans le bas-ventre, tiraillements ou irradiations vers le dos, les lombaires et les cuisses.", "Chronic pelvic pain: persistent lower abdominal pain, pulling or radiating pain down the back and legs.", pl = "Przewlekły ból miednicy: ciągły ból w podbrzuszu, ciągnięcie lub promieniowanie do pleców, okolicy lędźwiowej i ud.", uk = "Хронічний тазовий біль: постійний біль унизу живота, тягнучі відчуття або іррадіація в спину, поперек і стегна.", es = "Dolor pélvico crónico: dolor continuo en el bajo vientre, tirones o irradiación hacia la espalda, la zona lumbar y los muslos.", de = "Chronische Beckenschmerzen: anhaltende Schmerzen im Unterbauch, Ziehen oder Ausstrahlen in den Rücken, den Lendenbereich und die Oberschenkel."))
        }

        SectionTitle(tr("Le parcours de diagnostic", "Diagnostic journey", pl = "Droga do diagnozy", uk = "Шлях до діагнозу", es = "El camino hacia el diagnóstico", de = "Der Weg zur Diagnose"))
        LunaCard {
            Text(
                tr(
                    fr = "Le diagnostic met encore souvent plusieurs années à être posé. Noter précisément vos symptômes dans Lunaison et utiliser le « Bilan de santé » permet d'apporter un historique clair à votre soignant.",
                    en = "Diagnosis still takes several years on average. Tracking symptoms in Lunaison and using the 'Health report' helps provide clear history to your clinician.",
                    pl = "Diagnoza nadal często zajmuje kilka lat. Dokładne zapisywanie objawów w Lunaison i korzystanie z „Raportu medycznego” pozwala przedstawić osobie z personelu medycznego czytelną historię.",
                    uk = "Діагноз і досі часто ставлять лише через кілька років. Точний запис симптомів у Lunaison та використання «Медичного звіту» дають змогу надати вашому медичному працівнику чітку історію.",
                    es = "El diagnóstico todavía tarda con frecuencia varios años en llegar. Anotar con precisión tus síntomas en Lunaison y usar el «Informe médico» permite llevar un historial claro a tu profesional sanitario.",
                    de = "Bis zur Diagnose vergehen oft noch mehrere Jahre. Wenn du deine Symptome in Lunaison genau einträgst und den „Arztbericht“ nutzt, kannst du deinem medizinischen Fachpersonal eine klare Vorgeschichte mitbringen.",
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Bullet(tr("Qui consulter en premier : médecin généraliste, sage-femme ou gynécologue.", "Who to see first: GP, midwife or gynecologist.", pl = "Do kogo zgłosić się najpierw: lekarz rodzinny, położna lub ginekolog.", uk = "До кого звернутися насамперед: лікар загальної практики, акушерка або гінеколог.", es = "A quién consultar primero: médico de cabecera, matrona o ginecólogo.", de = "Wen man zuerst aufsucht: Hausärztin oder Hausarzt, Hebamme oder Gynäkologe."))
            Bullet(tr("Examens clés : échographie pelvienne par voie endovaginale et IRM pelvienne. Il est recommandé de les faire réaliser par un radiologue référent formé à l'endométriose (une imagerie normale n'exclut pas une forme superficielle).", "Key exams: transvaginal pelvic ultrasound and pelvic MRI, ideally performed by a specialized radiologist.", pl = "Kluczowe badania: USG miednicy przezpochwowe i rezonans magnetyczny miednicy. Zaleca się, aby wykonywał je radiolog referencyjny przeszkolony w zakresie endometriozy (prawidłowy obraz nie wyklucza postaci powierzchownej).", uk = "Ключові обстеження: трансвагінальне УЗД органів малого тазу та МРТ малого тазу. Рекомендується проводити їх у референтного радіолога, навченого діагностувати ендометріоз (нормальна візуалізація не виключає поверхневу форму).", es = "Pruebas clave: ecografía pélvica endovaginal y resonancia magnética pélvica. Se recomienda que las realice un radiólogo de referencia formado en endometriosis (una imagen normal no descarta una forma superficial).", de = "Wichtige Untersuchungen: transvaginaler Ultraschall des Beckens und MRT des Beckens. Empfohlen wird, sie von einer auf Endometriose spezialisierten Radiologin oder einem Radiologen durchführen zu lassen (eine unauffällige Bildgebung schließt eine oberflächliche Form nicht aus)."))
            Bullet(tr("Filières régionales : des réseaux de soins spécialisés labellisés (ARS) regroupent soignants, radiologues et centres experts pour faciliter l'orientation.", "Regional networks: specialized care networks help patients access experienced clinicians and reference centers.", pl = "Sieci regionalne: wyspecjalizowane, certyfikowane sieci opieki (ARS) zrzeszają personel medyczny, radiologów i ośrodki eksperckie, aby ułatwić skierowanie do odpowiedniej opieki.", uk = "Регіональні мережі: спеціалізовані акредитовані мережі допомоги (ARS) об'єднують медичних працівників, радіологів і експертні центри, щоб полегшити маршрутизацію пацієнток.", es = "Redes regionales: redes de atención especializadas con sello oficial (ARS) reúnen a profesionales sanitarios, radiólogos y centros expertos para facilitar la orientación.", de = "Regionale Netzwerke: zertifizierte spezialisierte Versorgungsnetze (ARS) bündeln medizinisches Fachpersonal, Radiologie und Expertenzentren, um die Orientierung zu erleichtern."))
        }

        SectionTitle(tr("Pistes de soulagement au quotidien", "Everyday relief options", pl = "Sposoby łagodzenia dolegliwości na co dzień", uk = "Способи полегшення в повсякденному житті", es = "Vías de alivio en el día a día", de = "Linderung im Alltag"))
        LunaCard {
            Bullet(tr("Chaleur : bouillotte ou patchs chauffants sur le ventre ou les lombaires pour détendre les muscles contractés.", "Heat: hot water bottle or heat patches on the abdomen or back to relax tight muscles.", pl = "Ciepło: termofor lub plastry rozgrzewające na brzuch lub okolicę lędźwiową, aby rozluźnić napięte mięśnie.", uk = "Тепло: грілка або зігрівальні пластирі на живіт чи поперек, щоб розслабити напружені м'язи.", es = "Calor: bolsa de agua caliente o parches térmicos sobre el vientre o la zona lumbar para relajar los músculos contraídos.", de = "Wärme: Wärmflasche oder Wärmepflaster auf Bauch oder Lendenbereich, um verkrampfte Muskeln zu entspannen."))
            Bullet(tr("Électrostimulation TENS : ces appareils de neurostimulation transcutanée envoient de légers influx non douloureux qui court-circuitent le message de douleur. Ils peuvent être prescrits et pris en charge.", "TENS neurostimulation: wearable devices sending gentle pulses that block pain signals to the brain. Often available on prescription.", pl = "Elektrostymulacja TENS: te urządzenia do przezskórnej neurostymulacji wysyłają lekkie, niebolesne impulsy, które zakłócają przekazywanie sygnału bólowego. Mogą być przepisane i refundowane.", uk = "Електростимуляція TENS: ці пристрої черезшкірної нейростимуляції надсилають легкі безболісні імпульси, які перебивають больовий сигнал. Їх можуть призначати лікарі, і їх вартість може відшкодовуватися.", es = "Electroestimulación TENS: estos aparatos de neuroestimulación transcutánea envían impulsos leves e indoloros que interfieren con el mensaje del dolor. Pueden prescribirse y estar cubiertos por el sistema sanitario.", de = "TENS-Elektrostimulation: Diese Geräte zur transkutanen Nervenstimulation senden leichte, schmerzfreie Impulse, die die Schmerzsignale überlagern. Sie können verordnet werden und werden unter Umständen erstattet."))
            Bullet(tr("Kinésithérapie et ostéopathie pelvienne : exercices de respiration, étirements du psoas et travail doux du plancher pelvien pour soulager les tensions réflexes.", "Pelvic physiotherapy: breathing exercises, psoas stretches and pelvic floor release to ease reflex tension.", pl = "Fizjoterapia i osteopatia miednicy: ćwiczenia oddechowe, rozciąganie mięśnia lędźwiowego i delikatna praca z dnem miednicy, aby złagodzić napięcia odruchowe.", uk = "Кінезітерапія та остеопатія таза: дихальні вправи, розтягування клубово-поперекового м'яза й м'яка робота з тазовим дном, щоб зняти рефлекторну напругу.", es = "Kinesiterapia y osteopatía pélvica: ejercicios de respiración, estiramientos del psoas y trabajo suave del suelo pélvico para aliviar las tensiones reflejas.", de = "Physiotherapie und Beckenosteopathie: Atemübungen, Dehnung des Psoas und sanfte Arbeit am Beckenboden, um reflektorische Verspannungen zu lösen."))
            Bullet(tr("Cohérence cardiaque : le module « Respirer » de Lunaison aide à apaiser le système nerveux autonome lors des pics douloureux.", "Cardiac coherence: Lunaison's 'Breathe' exercise helps calm the nervous system during pain flares.", pl = "Koherencja serca: moduł „Oddychaj” w Lunaison pomaga uspokoić autonomiczny układ nerwowy podczas nasilenia bólu.", uk = "Серцева когерентність: модуль «Дихати» в Lunaison допомагає заспокоїти вегетативну нервову систему під час піків болю.", es = "Coherencia cardíaca: el módulo «Respirar» de Lunaison ayuda a calmar el sistema nervioso autónomo durante los picos de dolor.", de = "Herzkohärenz: Das Modul „Atmen“ von Lunaison hilft, das autonome Nervensystem bei Schmerzspitzen zu beruhigen."))
            Bullet(tr("Traitements médicaux : pilule en continu ou DIU hormonal pour mettre les règles au repos, antalgiques adaptés ou chirurgie conservatrice si nécessaire, selon un projet de soins personnalisé.", "Medical management: continuous hormonal contraception to suppress periods, tailored medications or surgery when indicated.", pl = "Leczenie medyczne: tabletki w trybie ciągłym lub hormonalna wkładka domaciczna, aby wyciszyć miesiączki, odpowiednie leki przeciwbólowe lub w razie potrzeby zachowawcza operacja, zgodnie z indywidualnym planem opieki.", uk = "Медикаментозне лікування: безперервний прийом таблеток або гормональна ВМС, щоб «заспокоїти» менструації, відповідні знеболювальні або за потреби органозберігальна операція відповідно до персоналізованого плану лікування.", es = "Tratamientos médicos: píldora en continuo o DIU hormonal para poner la regla en reposo, analgésicos adaptados o cirugía conservadora si es necesario, según un plan de cuidados personalizado.", de = "Medizinische Behandlungen: Pille im Langzyklus oder Hormonspirale, um die Periode ruhigzustellen, passende Schmerzmittel oder bei Bedarf eine organerhaltende Operation, nach einem individuellen Behandlungsplan."))
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

        Muted(tr("Cette fiche est informative et ne remplace pas une consultation médicale avec un professionnel de santé.", "This guide is for informational purposes and does not replace consultation with a healthcare professional.", pl = "Ten artykuł ma charakter informacyjny i nie zastępuje konsultacji medycznej z osobą z personelu medycznego.", uk = "Ця стаття має інформаційний характер і не замінює медичну консультацію з медичним працівником.", es = "Esta ficha es informativa y no sustituye una consulta médica con un profesional sanitario.", de = "Dieser Artikel dient der Information und ersetzt keine ärztliche Beratung durch medizinisches Fachpersonal."))
    }
}

@Composable
fun PcosGuideScreen(onBack: () -> Unit) {
    val context = rememberPlatformContext()

    Page {
        PageTitle(
            tr("Le SOPK (ovaires polykystiques)", "PCOS (polycystic ovary syndrome)", pl = "PCOS (zespół policystycznych jajników)", uk = "СПКЯ (синдром полікістозних яєчників)", es = "El SOP (ovarios poliquísticos)", de = "PCOS (polyzystische Ovarien)"),
            onBack = onBack,
            subtitle = tr("Repères cliniques, critères de Rotterdam et pistes d'action.", "Clinical signs, Rotterdam criteria and management options.", pl = "Wskazówki kliniczne, kryteria rotterdamskie i możliwe działania.", uk = "Клінічні орієнтири, Роттердамські критерії та можливі кроки.", es = "Pautas clínicas, criterios de Róterdam y vías de actuación.", de = "Klinische Orientierung, Rotterdam-Kriterien und Handlungsmöglichkeiten."),
        )

        SectionTitle(tr("Qu'est-ce que le SOPK ?", "What is PCOS?", pl = "Czym jest PCOS?", uk = "Що таке СПКЯ?", es = "¿Qué es el SOP?", de = "Was ist PCOS?"))
        LunaCard {
            Text(
                tr(
                    fr = "Le syndrome des ovaires polykystiques (SOPK) est la première cause d'infertilité anovulatoire et le dérèglement hormonal le plus fréquent chez les femmes en âge de procréer (environ 1 personne sur 10 selon l'Inserm et l'OMS).",
                    en = "Polycystic ovary syndrome (PCOS) is the leading cause of anovulatory infertility and the most common hormonal disorder in women of reproductive age (approx. 1 in 10 per WHO/Inserm).",
                    pl = "Zespół policystycznych jajników (PCOS) jest główną przyczyną niepłodności z powodu braku owulacji i najczęstszym zaburzeniem hormonalnym u kobiet w wieku rozrodczym (około 1 osoba na 10 według Inserm i WHO).",
                    uk = "Синдром полікістозних яєчників (СПКЯ) є основною причиною ановуляторного безпліддя та найпоширенішим гормональним порушенням у жінок дітородного віку (приблизно 1 особа з 10 за даними Inserm і ВООЗ).",
                    es = "El síndrome de ovario poliquístico (SOP) es la primera causa de infertilidad por anovulación y el trastorno hormonal más frecuente en mujeres en edad reproductiva (aproximadamente 1 de cada 10 personas según Inserm y la OMS).",
                    de = "Das polyzystische Ovarialsyndrom (PCOS) ist die häufigste Ursache anovulatorischer Unfruchtbarkeit und die häufigste Hormonstörung bei Frauen im gebärfähigen Alter (etwa 1 von 10 Personen laut Inserm und WHO).",
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                tr(
                    fr = "Malgré son nom historique, il ne s'agit pas de kystes organiques mais d'une multitude de petits follicules immatures bloqués au stade de maturation dans l'ovaire, liés à un déséquilibre entre hormones hypophysaires (LH/FSH) et androgènes.",
                    en = "Despite its name, it does not involve ovarian cysts, but rather multiple small immature follicles paused in their growth, driven by an imbalance in pituitary hormones and androgens.",
                    pl = "Mimo historycznej nazwy nie chodzi o torbiele organiczne, lecz o wiele małych niedojrzałych pęcherzyków zatrzymanych na etapie dojrzewania w jajniku, związanych z zaburzeniem równowagi między hormonami przysadki (LH/FSH) a androgenami.",
                    uk = "Попри історичну назву, йдеться не про органічні кісти, а про безліч дрібних незрілих фолікулів, що застрягли на стадії дозрівання в яєчнику, пов'язаних із дисбалансом між гормонами гіпофіза (ЛГ/ФСГ) та андрогенами.",
                    es = "A pesar de su nombre histórico, no se trata de quistes orgánicos, sino de una multitud de pequeños folículos inmaduros bloqueados en la fase de maduración en el ovario, relacionados con un desequilibrio entre las hormonas hipofisarias (LH/FSH) y los andrógenos.",
                    de = "Trotz des historischen Namens handelt es sich nicht um organische Zysten, sondern um eine Vielzahl kleiner, unreifer Follikel, die im Eierstock in der Reifung stecken geblieben sind, bedingt durch ein Ungleichgewicht zwischen den Hormonen der Hirnanhangdrüse (LH/FSH) und den Androgenen.",
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        SectionTitle(tr("Les critères de Rotterdam (diagnostic)", "Rotterdam diagnostic criteria", pl = "Kryteria rotterdamskie (diagnostyka)", uk = "Роттердамські критерії (діагностика)", es = "Los criterios de Róterdam (diagnóstico)", de = "Die Rotterdam-Kriterien (Diagnose)"))
        LunaCard {
            Text(
                tr(
                    fr = "Selon le consensus international de Rotterdam, le diagnostic est posé en présence d'au moins 2 des 3 critères suivants (après exclusion d'autres causes) :",
                    en = "Under international Rotterdam criteria, diagnosis requires at least 2 of the following 3 signs:",
                    pl = "Zgodnie z międzynarodowym konsensusem z Rotterdamu diagnozę stawia się przy obecności co najmniej 2 z 3 poniższych kryteriów (po wykluczeniu innych przyczyn):",
                    uk = "За міжнародним Роттердамським консенсусом діагноз ставлять за наявності щонайменше 2 з 3 наведених критеріїв (після виключення інших причин):",
                    es = "Según el consenso internacional de Róterdam, el diagnóstico se establece cuando hay al menos 2 de los 3 criterios siguientes (tras descartar otras causas):",
                    de = "Nach dem internationalen Konsens von Rotterdam wird die Diagnose gestellt, wenn mindestens 2 der folgenden 3 Kriterien erfüllt sind (nach Ausschluss anderer Ursachen):",
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Bullet(tr("1. Ovulations rares ou absentes : cycles très longs (> 35 jours, spanioménorrhée), imprévisibles ou absence de règles pendant plus de 3 mois (aménorrhée).", "1. Infrequent or absent ovulation: very long cycles (> 35 days), unpredictable bleeding or absence of periods for 3+ months.", pl = "1. Rzadkie lub brakujące owulacje: bardzo długie cykle (> 35 dni, spaniomenorea), nieprzewidywalne lub brak miesiączki przez ponad 3 miesiące (amenorrhea).", uk = "1. Рідкісні або відсутні овуляції: дуже довгі цикли (> 35 днів, спаніоменорея), непередбачувані або відсутність менструацій понад 3 місяці (аменорея).", es = "1. Ovulaciones infrecuentes o ausentes: ciclos muy largos (> 35 días, espaniomenorrea), impredecibles o ausencia de regla durante más de 3 meses (amenorrea).", de = "1. Seltene oder ausbleibende Eisprünge: sehr lange Zyklen (> 35 Tage, Spanomenorrhoe), unvorhersehbar oder Ausbleiben der Periode über mehr als 3 Monate (Amenorrhoe)."))
            Bullet(tr("2. Hyperandrogénie : excès d'hormones androgènes visible (acné d'adulte tenace, pilosité marquée sur le menton, le thorax ou la ligne ombilicale, chute de cheveux) ou biologique (taux de testostérone libre élevé à la prise de sang).", "2. Hyperandrogenism: excess androgens manifesting as persistent acne, hirsutism, hair loss or elevated blood levels.", pl = "2. Hiperandrogenizm: nadmiar hormonów androgenowych widoczny (uporczywy trądzik u dorosłych, nasilone owłosienie na brodzie, klatce piersiowej lub linii pępka, wypadanie włosów) lub wykazany w badaniach (podwyższony poziom wolnego testosteronu we krwi).", uk = "2. Гіперандрогенія: надлишок андрогенних гормонів, видимий (стійкі акне в дорослому віці, виражене оволосіння на підборідді, грудях чи по лінії пупка, випадіння волосся) або біохімічний (підвищений рівень вільного тестостерону в аналізі крові).", es = "2. Hiperandrogenismo: exceso de hormonas andrógenas visible (acné persistente en la edad adulta, vello marcado en el mentón, el tórax o la línea umbilical, caída del cabello) o biológico (nivel elevado de testosterona libre en el análisis de sangre).", de = "2. Hyperandrogenismus: Überschuss an männlichen Hormonen, sichtbar (hartnäckige Akne im Erwachsenenalter, ausgeprägte Behaarung an Kinn, Brust oder Nabellinie, Haarausfall) oder laborchemisch (erhöhter freier Testosteronspiegel im Bluttest)."))
            Bullet(tr("3. Morphologie ovarienne polykystique : présence de 20 follicules ou plus par ovaire ou volume ovarien supérieur à 10 ml à l'échographie endovaginale.", "3. Polycystic ovarian morphology: 20+ follicles per ovary or ovarian volume > 10 ml on pelvic ultrasound.", pl = "3. Policystyczna morfologia jajników: obecność 20 lub więcej pęcherzyków w jajniku albo objętość jajnika powyżej 10 ml w USG przezpochwowym.", uk = "3. Полікістозна морфологія яєчників: наявність 20 або більше фолікулів в яєчнику чи об'єм яєчника понад 10 мл при трансвагінальному УЗД.", es = "3. Morfología ovárica poliquística: presencia de 20 o más folículos por ovario o volumen ovárico superior a 10 ml en la ecografía endovaginal.", de = "3. Polyzystische Eierstockmorphologie: 20 oder mehr Follikel pro Eierstock oder ein Eierstockvolumen von mehr als 10 ml im transvaginalen Ultraschall."))
        }

        SectionTitle(tr("Santé métabolique & globale", "Metabolic & general health", pl = "Zdrowie metaboliczne i ogólne", uk = "Метаболічне та загальне здоров'я", es = "Salud metabólica y global", de = "Stoffwechsel und allgemeine Gesundheit"))
        LunaCard {
            Bullet(tr("Résistance à l'insuline : jusqu'à 70 % des femmes avec SOPK présentent une insulinorésistance qui stimule la production d'androgènes par les ovaires et favorise la prise de poids abdominale et la fatigue.", "Insulin resistance: present in up to 70% of individuals with PCOS, stimulating androgen production and abdominal weight gain.", pl = "Insulinooporność: do 70 % kobiet z PCOS ma insulinooporność, która pobudza produkcję androgenów w jajnikach i sprzyja przyrostowi masy ciała w okolicy brzucha oraz zmęczeniu.", uk = "Інсулінорезистентність: до 70 % жінок із СПКЯ мають інсулінорезистентність, яка стимулює вироблення андрогенів яєчниками та сприяє збільшенню ваги в ділянці живота й втомі.", es = "Resistencia a la insulina: hasta el 70 % de las mujeres con SOP presentan insulinorresistencia, que estimula la producción de andrógenos por los ovarios y favorece el aumento de peso abdominal y el cansancio.", de = "Insulinresistenz: Bis zu 70 % der Frauen mit PCOS haben eine Insulinresistenz, die die Androgenbildung in den Eierstöcken anregt und Gewichtszunahme am Bauch sowie Müdigkeit begünstigt."))
            Bullet(tr("Risques à long terme : surveillance préventive recommandée pour le diabète de type 2, l'hypertension et la santé cardiovasculaire.", "Long-term monitoring: recommended for type 2 diabetes risk and cardiovascular health.", pl = "Ryzyka długoterminowe: zalecany jest profilaktyczny nadzór w kierunku cukrzycy typu 2, nadciśnienia i zdrowia sercowo-naczyniowego.", uk = "Довгострокові ризики: рекомендовано профілактичний нагляд щодо цукрового діабету 2 типу, артеріальної гіпертензії та здоров'я серцево-судинної системи.", es = "Riesgos a largo plazo: se recomienda una vigilancia preventiva de la diabetes de tipo 2, la hipertensión y la salud cardiovascular.", de = "Langfristige Risiken: Eine vorbeugende Überwachung auf Typ-2-Diabetes, Bluthochdruck und Herz-Kreislauf-Gesundheit wird empfohlen."))
            Bullet(tr("Bien-être émotionnel : l'impact sur l'image corporelle, l'anxiété et le moral est réel et mérite un accompagnement bienveillant et sans jugement.", "Emotional well-being: the psychological impact on self-image and anxiety warrants compassionate care.", pl = "Dobrostan emocjonalny: wpływ na obraz ciała, lęk i nastrój jest realny i zasługuje na życzliwe wsparcie bez oceniania.", uk = "Емоційне благополуччя: вплив на образ тіла, тривожність і настрій реальний і заслуговує на доброзичливу підтримку без осуду.", es = "Bienestar emocional: el impacto sobre la imagen corporal, la ansiedad y el ánimo es real y merece un acompañamiento respetuoso y sin juicios.", de = "Emotionales Wohlbefinden: Die Auswirkungen auf das Körperbild, die Angst und die Stimmung sind real und verdienen eine wohlwollende Begleitung ohne Wertung."))
        }

        SectionTitle(tr("Pistes de prise en charge", "Management options", pl = "Możliwe kierunki postępowania", uk = "Напрями лікування та підтримки", es = "Vías de tratamiento y acompañamiento", de = "Behandlungs- und Unterstützungsmöglichkeiten"))
        LunaCard {
            Bullet(tr("Alimentation à index glycémique modéré : privilégier les légumes, les légumineuses, les protéines et les bonnes graisses pour éviter les pics d'insuline.", "Low-glycemic nutrition: emphasizing vegetables, legumes, protein and fiber to smooth insulin spikes.", pl = "Dieta o umiarkowanym indeksie glikemicznym: wybierać warzywa, rośliny strączkowe, białko i zdrowe tłuszcze, aby unikać skoków insuliny.", uk = "Харчування з помірним глікемічним індексом: віддавати перевагу овочам, бобовим, білкам і корисним жирам, щоб уникати стрибків інсуліну.", es = "Alimentación con índice glucémico moderado: dar preferencia a las verduras, las legumbres, las proteínas y las grasas buenas para evitar los picos de insulina.", de = "Ernährung mit moderatem glykämischem Index: Gemüse, Hülsenfrüchte, Proteine und gesunde Fette bevorzugen, um Insulinspitzen zu vermeiden."))
            Bullet(tr("Activité physique régulière : la marche, la natation et le renforcement musculaire augmentent directement la sensibilité cellulaire à l'insuline.", "Regular physical activity: strength training and aerobic exercise directly improve cellular insulin sensitivity.", pl = "Regularna aktywność fizyczna: spacery, pływanie i trening siłowy bezpośrednio zwiększają wrażliwość komórek na insulinę.", uk = "Регулярна фізична активність: ходьба, плавання та силові вправи безпосередньо підвищують чутливість клітин до інсуліну.", es = "Actividad física regular: caminar, nadar y el fortalecimiento muscular aumentan directamente la sensibilidad de las células a la insulina.", de = "Regelmäßige Bewegung: Gehen, Schwimmen und Krafttraining erhöhen direkt die Insulinempfindlichkeit der Zellen."))
            Bullet(tr("Protéger l'endomètre : en l'absence de règles régulières, un traitement progestatif séquentiel permet d'évacuer régulièrement la muqueuse utérine.", "Endometrial protection: sequential progestins ensure regular shedding when periods are absent.", pl = "Ochrona endometrium: przy braku regularnych miesiączek sekwencyjne leczenie gestagenem pozwala regularnie usuwać błonę śluzową macicy.", uk = "Захист ендометрія: за відсутності регулярних менструацій послідовне лікування прогестагеном дає змогу регулярно виводити слизову оболонку матки.", es = "Proteger el endometrio: en ausencia de reglas regulares, un tratamiento secuencial con progestágeno permite eliminar con regularidad la mucosa uterina.", de = "Die Gebärmutterschleimhaut schützen: Wenn die Periode nicht regelmäßig kommt, ermöglicht eine sequenzielle Gestagenbehandlung, die Gebärmutterschleimhaut regelmäßig abzustoßen."))
            Bullet(tr("Traitements médicaux : pilule oestroprogestative anti-androgène, metformine en cas d'insulinorésistance ou inducteurs d'ovulation en projet de grossesse.", "Medical options: anti-androgenic pill, metformin or ovulation induction when seeking pregnancy.", pl = "Leczenie medyczne: złożona tabletka antykoncepcyjna o działaniu antyandrogennym, metformina w przypadku insulinooporności lub leki indukujące owulację przy planowaniu ciąży.", uk = "Медикаментозне лікування: естроген-прогестагенна таблетка з антиандрогенною дією, метформін за інсулінорезистентності або стимулятори овуляції при плануванні вагітності.", es = "Tratamientos médicos: píldora estroprogestágena antiandrógena, metformina en caso de insulinorresistencia o inductores de la ovulación si hay un proyecto de embarazo.", de = "Medizinische Behandlungen: antiandrogene Östrogen-Gestagen-Pille, Metformin bei Insulinresistenz oder Ovulationsauslöser bei Kinderwunsch."))
            Bullet(tr("Dans Lunaison : activez l'option « Cycles naturellement irréguliers » dans les Réglages pour bénéficier de prévisions adaptées sans alertes anxiogènes.", "In Lunaison: enable 'Naturally irregular cycles' in Settings for adapted prediction intervals.", pl = "W Lunaison: włącz opcję „Naturalnie nieregularne cykle” w Ustawieniach, aby korzystać z dopasowanych prognoz bez niepokojących alertów.", uk = "У Lunaison: увімкніть опцію «Природно нерегулярні цикли» в Налаштуваннях, щоб отримувати адаптовані прогнози без тривожних сповіщень.", es = "En Lunaison: activa la opción «Ciclos naturalmente irregulares» en Ajustes para obtener previsiones adaptadas sin alertas que generen ansiedad.", de = "In Lunaison: Aktiviere in den Einstellungen die Option „Natürlich unregelmäßige Zyklen“, um angepasste Vorhersagen ohne beunruhigende Warnungen zu erhalten."))
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

        Muted(tr("Cette fiche est informative et ne remplace pas une consultation médicale avec un professionnel de santé.", "This guide is for informational purposes and does not replace consultation with a healthcare professional.", pl = "Ten artykuł ma charakter informacyjny i nie zastępuje konsultacji medycznej z osobą z personelu medycznego.", uk = "Ця стаття має інформаційний характер і не замінює медичну консультацію з медичним працівником.", es = "Esta ficha es informativa y no sustituye una consulta médica con un profesional sanitario.", de = "Dieser Artikel dient der Information und ersetzt keine ärztliche Beratung durch medizinisches Fachpersonal."))
    }
}

@Composable
fun AdenomyosisGuideScreen(onBack: () -> Unit) {
    val context = rememberPlatformContext()

    Page {
        PageTitle(
            tr("L'adénomyose", "Adenomyosis", pl = "Adenomioza", uk = "Аденоміоз", es = "La adenomiosis", de = "Adenomyose"),
            onBack = onBack,
            subtitle = tr("Repères, règles très abondantes et options de soulagement.", "Indicators, heavy menstrual bleeding and relief options.", pl = "Wskazówki, bardzo obfite miesiączki i możliwości łagodzenia dolegliwości.", uk = "Орієнтири, дуже рясні менструації та варіанти полегшення.", es = "Pautas, reglas muy abundantes y opciones de alivio.", de = "Orientierung, sehr starke Perioden und Möglichkeiten der Linderung."),
        )

        SectionTitle(tr("Qu'est-ce que l'adénomyose ?", "What is adenomyosis?", pl = "Czym jest adenomioza?", uk = "Що таке аденоміоз?", es = "¿Qué es la adenomiosis?", de = "Was ist Adenomyose?"))
        LunaCard {
            Text(
                tr(
                    fr = "L'adénomyose se caractérise par l'infiltration de cellules de l'endomètre (la muqueuse qui tapisse l'intérieur de l'utérus) dans l'épaisseur du muscle utérin (le myomètre). On la décrit couramment comme une « endométriose interne » de l'utérus.",
                    en = "Adenomyosis occurs when endometrial tissue (the lining inside the uterus) infiltrates the muscular uterine wall (myometrium). It is often called 'internal endometriosis'.",
                    pl = "Adenomiozę cechuje naciekanie komórek endometrium (błony śluzowej wyścielającej wnętrze macicy) w głąb mięśnia macicy (miometrium). Powszechnie określa się ją jako „endometriozę wewnętrzną” macicy.",
                    uk = "Аденоміоз характеризується проникненням клітин ендометрія (слизової оболонки, що вистилає матку зсередини) у товщу м'яза матки (міометрій). Його зазвичай описують як «внутрішній ендометріоз» матки.",
                    es = "La adenomiosis se caracteriza por la infiltración de células del endometrio (la mucosa que reviste el interior del útero) en el espesor del músculo uterino (el miometrio). Se describe con frecuencia como una «endometriosis interna» del útero.",
                    de = "Bei der Adenomyose wachsen Zellen des Endometriums (der Schleimhaut, die die Gebärmutter innen auskleidet) in die Dicke der Gebärmuttermuskulatur (das Myometrium) ein. Sie wird häufig als „innere Endometriose“ der Gebärmutter bezeichnet.",
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                tr(
                    fr = "Ces foyers de muqueuse réagissent aux hormones de chaque cycle : ils saignent dans le muscle, provoquant une inflammation locale, des crampes douloureuses et une augmentation du volume de l'utérus.",
                    en = "These tissue patches respond to hormonal cycles by bleeding inside the uterine muscle, causing inflammation, intense cramps and an enlarged uterus.",
                    pl = "Te ogniska błony śluzowej reagują na hormony każdego cyklu: krwawią w obrębie mięśnia, powodując miejscowy stan zapalny, bolesne skurcze i powiększenie macicy.",
                    uk = "Ці осередки слизової оболонки реагують на гормони кожного циклу: вони кровоточать у м'язі, спричиняючи локальне запалення, болісні спазми та збільшення об'єму матки.",
                    es = "Estos focos de mucosa reaccionan a las hormonas de cada ciclo: sangran dentro del músculo, provocando inflamación local, calambres dolorosos y un aumento del volumen del útero.",
                    de = "Diese Schleimhautherde reagieren auf die Hormone jedes Zyklus: Sie bluten im Muskel und verursachen lokale Entzündungen, schmerzhafte Krämpfe und eine Vergrößerung der Gebärmutter.",
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Bullet(tr("Fréquence : elle touche 15 à 20 % des femmes et coexiste souvent avec l'endométriose pelvienne.", "Prevalence: affects 15 to 20% of women and frequently coexists with pelvic endometriosis.", pl = "Częstość: dotyczy 15 do 20 % kobiet i często współwystępuje z endometriozą miednicy.", uk = "Поширеність: стосується 15-20 % жінок і часто поєднується з ендометріозом органів малого тазу.", es = "Frecuencia: afecta al 15 a 20 % de las mujeres y coexiste con frecuencia con la endometriosis pélvica.", de = "Häufigkeit: Sie betrifft 15 bis 20 % der Frauen und tritt oft zusammen mit der Beckenendometriose auf."))
        }

        SectionTitle(tr("Les symptômes principaux", "Main symptoms", pl = "Główne objawy", uk = "Основні симптоми", es = "Los síntomas principales", de = "Die wichtigsten Symptome"))
        LunaCard {
            Bullet(tr("Règles très abondantes (ménorragies) : saignements prolongés (> 7 jours), abondants avec émission fréquente de caillots sanguins nécessitant de doubler les protections ou de se lever la nuit.", "Heavy menstrual bleeding (menorrhagia): prolonged bleeding (> 7 days), large blood clots and frequent protection changes.", pl = "Bardzo obfite miesiączki (menorrhagia): przedłużone krwawienia (> 7 dni), obfite, z częstym oddawaniem skrzepów, wymagające podwójnych zabezpieczeń lub wstawania w nocy.", uk = "Дуже рясні менструації (меноррагії): тривалі кровотечі (> 7 днів), рясні з частим виходом згустків крові, що вимагають подвійного захисту або нічного вставання.", es = "Reglas muy abundantes (menorragias): sangrados prolongados (> 7 días), abundantes, con expulsión frecuente de coágulos que obligan a doblar las protecciones o a levantarse por la noche.", de = "Sehr starke Perioden (Menorrhagien): verlängerte Blutungen (> 7 Tage), stark, mit häufigem Abgang von Blutgerinnseln, sodass man Hygieneartikel doppelt benötigt oder nachts aufstehen muss."))
            Bullet(tr("Crampes utérines violentes : contractions douloureuses avant et pendant les règles, résistant souvent aux antalgiques courants.", "Severe menstrual cramps: intense pain before and during periods that resists standard analgesics.", pl = "Silne skurcze macicy: bolesne skurcze przed miesiączką i w jej trakcie, często oporne na zwykłe leki przeciwbólowe.", uk = "Сильні маткові спазми: болісні скорочення перед менструацією та під час неї, які часто не піддаються звичайним знеболювальним.", es = "Calambres uterinos violentos: contracciones dolorosas antes y durante la regla, que a menudo no ceden con los analgésicos habituales.", de = "Heftige Gebärmutterkrämpfe: schmerzhafte Kontraktionen vor und während der Periode, die oft auf gängige Schmerzmittel nicht ansprechen."))
            Bullet(tr("Pesanteur pelvienne : sensation d'inconfort continu ou de bas-ventre lourd et gonflé.", "Pelvic heaviness: continuous lower abdominal discomfort or feelings of pressure.", pl = "Ciężkość w miednicy: uczucie ciągłego dyskomfortu lub ciężkiego, wzdętego podbrzusza.", uk = "Тяжкість у тазі: відчуття постійного дискомфорту або важкого, роздутого низу живота.", es = "Pesadez pélvica: sensación de molestia continua o de bajo vientre pesado e hinchado.", de = "Beckenschwere: Gefühl von anhaltendem Unbehagen oder einem schweren, geblähten Unterbauch."))
            Bullet(tr("Fatigue et carence en fer : les saignements abondants répétés épuisent les réserves de fer (anémie ferriprive), causant épuisement et essoufflement.", "Fatigue and iron deficiency: heavy blood loss depletes iron stores, causing persistent fatigue and shortness of breath.", pl = "Zmęczenie i niedobór żelaza: powtarzające się obfite krwawienia wyczerpują zapasy żelaza (niedokrwistość z niedoboru żelaza), powodując wyczerpanie i duszność.", uk = "Втома та дефіцит заліза: повторювані рясні кровотечі виснажують запаси заліза (залізодефіцитна анемія), спричиняючи виснаження й задишку.", es = "Cansancio y falta de hierro: los sangrados abundantes repetidos agotan las reservas de hierro (anemia ferropénica), lo que causa agotamiento y falta de aire.", de = "Müdigkeit und Eisenmangel: Wiederholte starke Blutungen erschöpfen die Eisenreserven (Eisenmangelanämie) und verursachen Erschöpfung und Atemnot."))
        }

        SectionTitle(tr("Le diagnostic", "Diagnosis", pl = "Diagnoza", uk = "Діагностика", es = "El diagnóstico", de = "Die Diagnose"))
        LunaCard {
            Text(
                tr(
                    fr = "Le diagnostic est posé grâce à des examens d'imagerie ciblés réalisés par des praticiens formés :",
                    en = "Diagnosis relies on specialized pelvic imaging performed by experienced practitioners:",
                    pl = "Diagnozę stawia się na podstawie ukierunkowanych badań obrazowych wykonywanych przez przeszkolonych specjalistów:",
                    uk = "Діагноз ставлять за допомогою цілеспрямованих візуалізаційних обстежень, які проводять підготовлені фахівці:",
                    es = "El diagnóstico se establece mediante pruebas de imagen dirigidas, realizadas por profesionales formados:",
                    de = "Die Diagnose wird durch gezielte bildgebende Untersuchungen gestellt, die von geschulten Fachleuten durchgeführt werden:",
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Bullet(tr("Échographie pelvienne endovaginale : visualise l'asymétrie des parois utérines, l'aspect hétérogène du muscle et les micro-kystes myométriaux.", "Transvaginal ultrasound: shows wall asymmetry, heterogeneous muscle texture and myometrial microcysts.", pl = "Przezpochwowe USG miednicy: uwidacznia asymetrię ścian macicy, niejednorodny wygląd mięśnia i mikrotorbiele w miometrium.", uk = "Трансвагінальне УЗД малого тазу: виявляє асиметрію стінок матки, неоднорідний вигляд м'яза та мікрокісти в міометрії.", es = "Ecografía pélvica endovaginal: visualiza la asimetría de las paredes uterinas, el aspecto heterogéneo del músculo y los microquistes miometriales.", de = "Transvaginaler Beckenultraschall: zeigt die Asymmetrie der Gebärmutterwände, das ungleichmäßige Aussehen des Muskels und Mikrozysten im Myometrium."))
            Bullet(tr("IRM pelvienne : examen de référence mesurant l'épaississement de la « zone jonctionnelle » (> 12 mm) entre l'endomètre et le myomètre.", "Pelvic MRI: gold standard measuring the thickening of the junctional zone (> 12 mm).", pl = "Rezonans magnetyczny miednicy: badanie referencyjne mierzące pogrubienie „strefy połączenia” (> 12 mm) między endometrium a miometrium.", uk = "МРТ малого тазу: референтне обстеження, що вимірює потовщення «перехідної зони» (> 12 мм) між ендометрієм і міометрієм.", es = "RM pélvica: prueba de referencia que mide el engrosamiento de la «zona de unión» (> 12 mm) entre el endometrio y el miometrio.", de = "MRT des Beckens: Referenzuntersuchung, die die Verdickung der „Junktionalzone“ (> 12 mm) zwischen Endometrium und Myometrium misst."))
        }

        SectionTitle(tr("Pistes de soulagement", "Relief and treatments", pl = "Sposoby łagodzenia dolegliwości", uk = "Способи полегшення", es = "Vías de alivio", de = "Möglichkeiten der Linderung"))
        LunaCard {
            Bullet(tr("Anti-hémorragiques (acide tranexamique) : pris uniquement pendant les règles, il diminue d'environ 50 % l'abondance des saignements.", "Antifibrinolytics (tranexamic acid): taken during flow days to reduce blood loss volume by approx. 50%.", pl = "Leki przeciwkrwotoczne (kwas traneksamowy): przyjmowany wyłącznie w czasie miesiączki zmniejsza obfitość krwawienia o około 50 %.", uk = "Протикровотечні засоби (транексамова кислота): її приймають лише під час менструації, і вона зменшує рясність кровотечі приблизно на 50 %.", es = "Antihemorrágicos (ácido tranexámico): tomado solo durante la regla, reduce en torno a un 50 % la abundancia del sangrado.", de = "Blutstillende Mittel (Tranexamsäure): nur während der Periode eingenommen, verringert sie die Stärke der Blutung um etwa 50 %."))
            Bullet(tr("Contraception hormonale : stérilet hormonal au lévonorgestrel ou pilule progestative en continu pour atrophier la muqueuse et stopper les hémorragies.", "Hormonal therapy: levonorgestrel IUD or continuous progestin pill to thin the lining and stop heavy bleeding.", pl = "Antykoncepcja hormonalna: hormonalna wkładka z lewonorgestrelem lub ciągła tabletka gestagenowa, aby spowodować zanik błony śluzowej i zatrzymać krwawienia.", uk = "Гормональна контрацепція: гормональна спіраль із левоноргестрелом або безперервна прогестинова таблетка, щоб спричинити атрофію слизової оболонки та зупинити кровотечі.", es = "Anticoncepción hormonal: DIU hormonal con levonorgestrel o píldora de progestágeno continua para atrofiar la mucosa y detener las hemorragias.", de = "Hormonelle Verhütung: Hormonspirale mit Levonorgestrel oder durchgehend eingenommene Gestagen-Pille, um die Schleimhaut zurückzubilden und die Blutungen zu stoppen."))
            Bullet(tr("Recharger le fer : dosage de la ferritine et complémentation en fer (orale ou injectable si sévère) pour restaurer l'énergie.", "Iron repletion: ferritin checks and oral or IV iron supplements to resolve anemia.", pl = "Uzupełnianie żelaza: oznaczenie ferrytyny i suplementacja żelaza (doustna lub w iniekcjach w ciężkich przypadkach), aby przywrócić energię.", uk = "Поповнення заліза: визначення феритину та додавання заліза (перорально або ін'єкційно за тяжкого дефіциту), щоб відновити енергію.", es = "Recargar el hierro: dosificación de ferritina y suplementos de hierro (orales o inyectables si es grave) para recuperar la energía.", de = "Eisen auffüllen: Bestimmung des Ferritins und Eisenergänzung (oral, bei schwerem Mangel als Injektion), um die Energie wiederherzustellen."))
            Bullet(tr("Chaleur et antispasmodiques : bouillotte chaude sur le bas-ventre et repos pour relâcher les spasmes myométriaux.", "Heat and antispasmodics: heating pad on the lower abdomen to ease myometrial spasms.", pl = "Ciepło i leki rozkurczowe: ciepły termofor na podbrzuszu i odpoczynek, aby rozluźnić skurcze miometrium.", uk = "Тепло та спазмолітики: тепла грілка на низ живота й відпочинок, щоб зняти спазми міометрія.", es = "Calor y antiespasmódicos: bolsa de agua caliente sobre el bajo vientre y reposo para relajar los espasmos miometriales.", de = "Wärme und krampflösende Mittel: warme Wärmflasche auf dem Unterbauch und Ruhe, um die Spasmen des Myometriums zu lösen."))
            Bullet(tr("Gestes interventionnels : embolisation des artères utérines ou chirurgie discutées au cas par cas en cas d'échec des traitements médicaux.", "Interventional options: uterine artery embolization or surgery evaluated in severe cases.", pl = "Zabiegi interwencyjne: embolizacja tętnic macicznych lub operacja, omawiane indywidualnie, gdy leczenie farmakologiczne zawiedzie.", uk = "Інтервенційні втручання: емболізація маткових артерій або операція, які обговорюють в індивідуальному порядку у разі неефективності медикаментозного лікування.", es = "Procedimientos intervencionistas: la embolización de las arterias uterinas o la cirugía se valoran caso por caso si fracasan los tratamientos médicos.", de = "Interventionelle Maßnahmen: Embolisation der Gebärmutterarterien oder Operation werden im Einzelfall besprochen, wenn die medikamentösen Behandlungen nicht wirken."))
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

        Muted(tr("Cette fiche est informative et ne remplace pas une consultation médicale avec un professionnel de santé.", "This guide is for informational purposes and does not replace consultation with a healthcare professional.", pl = "Ten artykuł ma charakter informacyjny i nie zastępuje konsultacji medycznej z osobą z personelu medycznego.", uk = "Ця стаття має інформаційний характер і не замінює медичну консультацію з медичним працівником.", es = "Esta ficha es informativa y no sustituye una consulta médica con un profesional sanitario.", de = "Dieser Artikel dient der Information und ersetzt keine ärztliche Beratung durch medizinisches Fachpersonal."))
    }
}

@Composable
fun PerimenopauseGuideScreen(onBack: () -> Unit) {
    val context = rememberPlatformContext()

    Page {
        PageTitle(
            tr("La périménopause", "Perimenopause", pl = "Perimenopauza", uk = "Перименопауза", es = "La perimenopausia", de = "Die Perimenopause"),
            onBack = onBack,
            subtitle = tr("Comprendre les variations hormonales et vivre sereinement la transition.", "Understanding hormonal fluctuations and navigating the transition.", pl = "Zrozumieć wahania hormonalne i spokojnie przejść przez tę zmianę.", uk = "Зрозуміти гормональні коливання та спокійно пройти цей перехід.", es = "Comprender las variaciones hormonales y vivir la transición con serenidad.", de = "Hormonelle Schwankungen verstehen und den Übergang gelassen erleben."),
        )

        SectionTitle(tr("Qu'est-ce que la périménopause ?", "What is perimenopause?", pl = "Czym jest perimenopauza?", uk = "Що таке перименопауза?", es = "¿Qué es la perimenopausia?", de = "Was ist die Perimenopause?"))
        LunaCard {
            Text(
                tr(
                    fr = "La périménopause est la phase naturelle de transition hormonale qui précède la ménopause (l'arrêt définitif des règles pendant 12 mois consécutifs).",
                    en = "Perimenopause is the natural transition period preceding menopause (defined as 12 consecutive months without periods).",
                    pl = "Perimenopauza to naturalna faza przejścia hormonalnego poprzedzająca menopauzę (trwałe ustanie miesiączek przez 12 kolejnych miesięcy).",
                    uk = "Перименопауза є природною фазою гормонального переходу, що передує менопаузі (остаточному припиненню менструацій упродовж 12 місяців поспіль).",
                    es = "La perimenopausia es la fase natural de transición hormonal que precede a la menopausia (el cese definitivo de la regla durante 12 meses consecutivos).",
                    de = "Die Perimenopause ist die natürliche Phase des hormonellen Übergangs vor der Menopause (dem endgültigen Ausbleiben der Periode über 12 aufeinanderfolgende Monate).",
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                tr(
                    fr = "Elle débute le plus souvent entre 40 et 48 ans et dure en moyenne entre 2 et 8 ans. La réserve de follicules ovariens diminue et la production d'œstrogènes et de progestérone commence à fluctuer de manière imprévisible.",
                    en = "It typically begins between ages 40 and 48 and lasts 2 to 8 years on average. Ovarian follicle reserve declines, causing estrogen and progesterone to fluctuate.",
                    pl = "Najczęściej zaczyna się między 40. a 48. rokiem życia i trwa średnio od 2 do 8 lat. Rezerwa pęcherzyków jajnikowych maleje, a produkcja estrogenów i progesteronu zaczyna nieprzewidywalnie się zmieniać.",
                    uk = "Найчастіше вона починається між 40 і 48 роками та триває в середньому від 2 до 8 років. Запас яєчникових фолікулів зменшується, а вироблення естрогенів і прогестерону починає непередбачувано коливатися.",
                    es = "Suele comenzar entre los 40 y los 48 años y dura de media entre 2 y 8 años. La reserva de folículos ováricos disminuye y la producción de estrógenos y de progesterona empieza a fluctuar de forma impredecible.",
                    de = "Sie beginnt meist zwischen 40 und 48 Jahren und dauert durchschnittlich 2 bis 8 Jahre. Der Vorrat an Eierstockfollikeln nimmt ab, und die Bildung von Östrogen und Progesteron beginnt unvorhersehbar zu schwanken.",
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        SectionTitle(tr("Les deux phases de la transition", "The two phases of transition", pl = "Dwie fazy przejścia", uk = "Дві фази переходу", es = "Las dos fases de la transición", de = "Die zwei Phasen des Übergangs"))
        LunaCard {
            Bullet(tr("1. Chute précoce de la progestérone : les ovulations deviennent irrégulières. Les œstrogènes ne sont plus contrebalancés : les cycles se raccourcissent (21 à 25 jours), les seins deviennent très sensibles, l'irritabilité et le syndrome prémenstruel s'accentuent.", "1. Early progesterone decline: ovulation becomes irregular. Estrogen dominates unopposed: cycles shorten (21-25 days), breast tenderness increases and PMS intensifies.", pl = "1. Wczesny spadek progesteronu: owulacje stają się nieregularne. Estrogeny nie są już równoważone: cykle się skracają (21 do 25 dni), piersi stają się bardzo tkliwe, nasilają się drażliwość i zespół napięcia przedmiesiączkowego.", uk = "1. Ранній спад прогестерону: овуляції стають нерегулярними. Естрогени більше не врівноважуються: цикли скорочуються (21-25 днів), груди стають дуже чутливими, посилюються дратівливість і передменструальний синдром.", es = "1. Descenso precoz de la progesterona: las ovulaciones se vuelven irregulares. Los estrógenos ya no están contrarrestados: los ciclos se acortan (21 a 25 días), los pechos se vuelven muy sensibles y se acentúan la irritabilidad y el síndrome premenstrual.", de = "1. Früher Abfall des Progesterons: Die Eisprünge werden unregelmäßig. Das Östrogen wird nicht mehr ausgeglichen: Die Zyklen verkürzen sich (21 bis 25 Tage), die Brüste werden sehr empfindlich, Reizbarkeit und prämenstruelles Syndrom nehmen zu."))
            Bullet(tr("2. Chute tardive des œstrogènes : les cycles s'allongent considérablement (parfois 2 à 4 mois sans règles), les flux deviennent imprévisibles, et les symptômes vasomoteurs apparaissent.", "2. Late estrogen decline: cycles lengthen significantly (2-4 months between periods), flow becomes unpredictable, and vasomotor symptoms emerge.", pl = "2. Późny spadek estrogenów: cykle znacznie się wydłużają (czasem 2 do 4 miesięcy bez miesiączki), krwawienia stają się nieprzewidywalne i pojawiają się objawy naczynioruchowe.", uk = "2. Пізній спад естрогенів: цикли значно подовжуються (іноді 2-4 місяці без менструації), кровотечі стають непередбачуваними, з'являються вазомоторні симптоми.", es = "2. Descenso tardío de los estrógenos: los ciclos se alargan considerablemente (a veces 2 a 4 meses sin regla), los flujos se vuelven impredecibles y aparecen los síntomas vasomotores.", de = "2. Später Abfall des Östrogens: Die Zyklen werden deutlich länger (manchmal 2 bis 4 Monate ohne Periode), die Blutungen werden unvorhersehbar, und vasomotorische Symptome treten auf."))
        }

        SectionTitle(tr("Les manifestations fréquentes", "Common symptoms", pl = "Częste objawy", uk = "Часті прояви", es = "Las manifestaciones frecuentes", de = "Häufige Beschwerden"))
        LunaCard {
            Bullet(tr("Bouffées de chaleur et sueurs nocturnes : sensation soudaine de chaleur intense remontant au cou et au visage, parfois suivie de frissons.", "Hot flashes & night sweats: sudden waves of intense heat over the face and neck, sometimes followed by chills.", pl = "Uderzenia gorąca i nocne poty: nagłe uczucie intensywnego gorąca, które wędruje ku szyi i twarzy, czasem po którym pojawiają się dreszcze.", uk = "Припливи жару та нічне пітніння: раптове відчуття сильного жару, що піднімається до шиї та обличчя, інколи з наступним ознобом.", es = "Sofocos y sudores nocturnos: sensación repentina de calor intenso que sube hacia el cuello y la cara, a veces seguida de escalofríos.", de = "Hitzewallungen und nächtliches Schwitzen: plötzliches Gefühl intensiver Hitze, die zu Hals und Gesicht aufsteigt und manchmal von Schüttelfrost gefolgt wird."))
            Bullet(tr("Troubles du sommeil : difficultés d'endormissement et réveils nocturnes fréquents (souvent vers 3h-4h).", "Sleep disruption: difficulty falling asleep and frequent night awakenings (often around 3-4 a.m.).", pl = "Zaburzenia snu: trudności z zasypianiem i częste nocne przebudzenia (często około 3-4 w nocy).", uk = "Порушення сну: труднощі із засинанням і часті нічні пробудження (нерідко близько 3-4 години ранку).", es = "Trastornos del sueño: dificultad para conciliar el sueño y despertares nocturnos frecuentes (a menudo hacia las 3-4 h).", de = "Schlafstörungen: Einschlafschwierigkeiten und häufiges nächtliches Erwachen (oft gegen 3 bis 4 Uhr)."))
            Bullet(tr("Brouillard mental (« brain fog ») : sensation de tête confuse, trous de mémoire ponctuels et baisse de concentration passagère.", "Brain fog: temporary concentration lapses and occasional memory blanks.", pl = "Mgła mózgowa („brain fog”): uczucie mętlika w głowie, okresowe luki w pamięci i przemijające pogorszenie koncentracji.", uk = "Туман у голові («brain fog»): відчуття сплутаності думок, окремі провали в пам'яті та тимчасове зниження концентрації.", es = "Niebla mental («brain fog»): sensación de cabeza confusa, lapsus de memoria puntuales y bajada pasajera de la concentración.", de = "Gedankennebel („Brain Fog“): Gefühl eines benebelten Kopfes, gelegentliche Gedächtnislücken und vorübergehend nachlassende Konzentration."))
            Bullet(tr("Variations de l'humeur : anxiété nouvelle, irritabilité ou moral fluctuant liés aux vagues d'œstrogènes.", "Mood changes: heightened anxiety or irritability linked to shifting hormone levels.", pl = "Wahania nastroju: nowo pojawiający się lęk, drażliwość lub zmienny nastrój związane z falami estrogenów.", uk = "Коливання настрою: нова тривожність, дратівливість або мінливий настрій, пов'язані з хвилями естрогенів.", es = "Variaciones del estado de ánimo: ansiedad nueva, irritabilidad o ánimo fluctuante vinculados a las oleadas de estrógenos.", de = "Stimmungsschwankungen: neu auftretende Angst, Reizbarkeit oder schwankende Stimmung durch die Östrogenwellen."))
            Bullet(tr("Sécheresse intime : inconfort vaginal lors des rapports, attribuable à la baisse progressive d'imprégnation œstrogénique.", "Vaginal dryness: intimate discomfort during sex due to lower estrogen support.", pl = "Suchość intymna: dyskomfort w pochwie podczas współżycia, wynikający ze stopniowego spadku działania estrogenów.", uk = "Інтимна сухість: вагінальний дискомфорт під час статевого акту, зумовлений поступовим зниженням естрогенної насиченості.", es = "Sequedad íntima: molestias vaginales durante las relaciones, atribuibles a la disminución progresiva del efecto de los estrógenos.", de = "Intime Trockenheit: Unbehagen in der Scheide beim Geschlechtsverkehr, bedingt durch die allmählich sinkende Östrogenwirkung."))
        }

        SectionTitle(tr("Pistes d'accompagnement et bien-être", "Support and well-being options", pl = "Wsparcie i dobre samopoczucie", uk = "Підтримка та самопочуття", es = "Vías de acompañamiento y bienestar", de = "Begleitung und Wohlbefinden"))
        LunaCard {
            Bullet(tr("Activité physique régulière : le renforcement musculaire et les exercices avec impact (marche active, danse) protègent la densité osseuse et la masse musculaire.", "Regular physical activity: strength training and weight-bearing exercise protect bone density and muscle mass.", pl = "Regularna aktywność fizyczna: trening siłowy i ćwiczenia z obciążeniem (szybki marsz, taniec) chronią gęstość kości i masę mięśniową.", uk = "Регулярна фізична активність: силові вправи та вправи з навантаженням (жвава ходьба, танці) захищають щільність кісток і м'язову масу.", es = "Actividad física regular: el fortalecimiento muscular y los ejercicios con impacto (caminata activa, baile) protegen la densidad ósea y la masa muscular.", de = "Regelmäßige Bewegung: Krafttraining und Übungen mit Belastung (zügiges Gehen, Tanzen) schützen die Knochendichte und die Muskelmasse."))
            Bullet(tr("Alimentation protectrice : apport suffisant en protéines, calcium (produits laitiers, eaux minérales calciques) et vitamine D.", "Bone & metabolic nutrition: adequate protein, calcium and vitamin D intake.", pl = "Dieta ochronna: wystarczająca podaż białka, wapnia (produkty mleczne, wody mineralne bogate w wapń) i witaminy D.", uk = "Захисне харчування: достатнє надходження білка, кальцію (молочні продукти, кальцієві мінеральні води) і вітаміну D.", es = "Alimentación protectora: aporte suficiente de proteínas, calcio (lácteos, aguas minerales ricas en calcio) y vitamina D.", de = "Schützende Ernährung: ausreichende Zufuhr von Eiweiß, Calcium (Milchprodukte, calciumreiche Mineralwässer) und Vitamin D."))
            Bullet(tr("Modérer les déclencheurs de bouffées : l'alcool, le café, les repas très épicés et le stress aigu accentuent la fréquence des bouffées de chaleur.", "Moderate triggers: alcohol, caffeine, spicy foods and acute stress commonly trigger hot flashes.", pl = "Ograniczać czynniki wyzwalające uderzenia gorąca: alkohol, kawa, bardzo ostre potrawy i silny stres zwiększają częstość uderzeń gorąca.", uk = "Помірковано ставитися до тригерів припливів: алкоголь, кава, дуже гостра їжа та гострий стрес збільшують частоту припливів жару.", es = "Moderar los desencadenantes de los sofocos: el alcohol, el café, las comidas muy picantes y el estrés agudo aumentan la frecuencia de los sofocos.", de = "Auslöser von Hitzewallungen mäßigen: Alkohol, Kaffee, sehr scharfe Mahlzeiten und akuter Stress erhöhen die Häufigkeit der Hitzewallungen."))
            Bullet(tr("Traitement hormonal de la ménopause (THM) : peut être discuté avec un soignant si les symptômes altèrent la qualité de vie, avec un bilan bénéfice/risque personnalisé.", "Menopausal hormone therapy (MHT): can be evaluated with a doctor if symptoms substantially affect daily quality of life.", pl = "Menopauzalna terapia hormonalna (MTH): można ją omówić z osobą z personelu medycznego, jeśli objawy pogarszają jakość życia, z indywidualną oceną korzyści i ryzyka.", uk = "Менопаузальна гормональна терапія (МГТ): її можна обговорити з медичним працівником, якщо симптоми погіршують якість життя, з індивідуальною оцінкою користі та ризику.", es = "Tratamiento hormonal de la menopausia (THM): puede plantearse con un profesional sanitario si los síntomas afectan a la calidad de vida, con una valoración personalizada del beneficio y el riesgo.", de = "Menopausale Hormontherapie (MHT): Sie kann mit medizinischem Fachpersonal besprochen werden, wenn die Beschwerden die Lebensqualität beeinträchtigen, mit einer individuellen Nutzen-Risiko-Abwägung."))
            Bullet(tr("Dans Lunaison : notez vos bouffées et troubles du sommeil dans votre carnet, et activez le mode « Cycles irréguliers » dans les Réglages.", "In Lunaison: track hot flashes and sleep in your log, and enable 'Irregular cycles' mode in Settings.", pl = "W Lunaison: zapisuj uderzenia gorąca i zaburzenia snu w dzienniku oraz włącz tryb „Naturalnie nieregularne cykle” w Ustawieniach.", uk = "У Lunaison: записуйте припливи жару та порушення сну у своєму щоденнику й увімкніть режим «Природно нерегулярні цикли» в Налаштуваннях.", es = "En Lunaison: anota tus sofocos y trastornos del sueño en tu registro y activa el modo «Ciclos naturalmente irregulares» en Ajustes.", de = "In Lunaison: Trage deine Hitzewallungen und Schlafstörungen in dein Tagebuch ein und aktiviere in den Einstellungen den Modus „Natürlich unregelmäßige Zyklen“."))
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

        Muted(tr("Cette fiche est informative et ne remplace pas une consultation médicale avec un professionnel de santé.", "This guide is for informational purposes and does not replace consultation with a healthcare professional.", pl = "Ten artykuł ma charakter informacyjny i nie zastępuje konsultacji medycznej z osobą z personelu medycznego.", uk = "Ця стаття має інформаційний характер і не замінює медичну консультацію з медичним працівником.", es = "Esta ficha es informativa y no sustituye una consulta médica con un profesional sanitario.", de = "Dieser Artikel dient der Information und ersetzt keine ärztliche Beratung durch medizinisches Fachpersonal."))
    }
}
