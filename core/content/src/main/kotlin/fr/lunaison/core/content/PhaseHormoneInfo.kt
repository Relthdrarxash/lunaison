package fr.lunaison.core.content

import fr.lunaison.core.cycle.Phase
import fr.lunaison.core.model.tr

/** Niveau relatif d'une hormone à un stade du cycle. */
enum class HormoneLevel(
    private val fr: String,
    private val en: String,
    private val pl: String,
    private val uk: String,
    private val es: String,
    private val de: String,
) {
    LOW("Bas", "Low", "Niski", "Низький", "Bajo", "Niedrig"),
    RISING("En hausse", "Rising", "Wzrastający", "Зростає", "En ascenso", "Steigend"),
    PEAK("Pic maximal", "Peak", "Szczyt", "Пік", "Pico máximo", "Spitze"),
    DOMINANT("Dominant", "Dominant", "Dominujący", "Домінує", "Dominante", "Dominant"),
    FALLING("En baisse", "Falling", "Opadający", "Спадає", "En descenso", "Fallend"),
    ;

    val label: String get() = tr(fr, en, pl, uk, es, de)
}

/** Vue d'ensemble des 4 hormones clés du cycle ovarien. */
data class HormoneSnapshot(
    val estrogen: HormoneLevel,
    val progesterone: HormoneLevel,
    val lh: HormoneLevel,
    val fsh: HormoneLevel,
    val summary: String,
)

/** Profil physiologique, atouts et défis d'une phase du cycle. */
data class PhaseProfile(
    val phase: Phase,
    val title: String,
    val hormones: HormoneSnapshot,
    val strengths: List<String>,
    val challenges: List<String>,
    val supportAdvice: List<String>,
)

object PhaseHormoneInfo {

    fun profile(phase: Phase): PhaseProfile = when (phase) {
        Phase.MENSTRUAL -> PhaseProfile(
            phase = Phase.MENSTRUAL,
            title = tr("Règles : réinitialisation et repos", "Period: reset and rest", pl = "Miesiączka: reset i odpoczynek", uk = "Менструація: перезапуск і відпочинок", es = "Regla: reinicio y descanso", de = "Periode: Neustart und Ruhe"),
            hormones = HormoneSnapshot(
                estrogen = HormoneLevel.LOW,
                progesterone = HormoneLevel.LOW,
                lh = HormoneLevel.LOW,
                fsh = HormoneLevel.RISING,
                summary = tr(
                    fr = "Les hormones sont à leur niveau plancher, déclenchant le détachement de la muqueuse utérine.",
                    en = "Hormones are at baseline levels, triggering the shedding of the uterine lining.",
                    pl = "Hormony są na najniższym poziomie, co powoduje złuszczanie błony śluzowej macicy.",
                    uk = "Гормони на найнижчому рівні, що запускає відторгнення слизової оболонки матки.",
                    es = "Las hormonas están en su nivel más bajo, lo que desencadena el desprendimiento de la mucosa uterina.",
                    de = "Die Hormone sind auf ihrem Tiefststand, was die Ablösung der Gebärmutterschleimhaut auslöst.",
                ),
            ),
            strengths = listOf(
                tr("Soulagement immédiat du syndrome prémenstruel et des tensions nerveuses.", "Immediate relief from PMS symptoms and nervous tension.", pl = "Natychmiastowa ulga w objawach zespołu napięcia przedmiesiączkowego i napięciu nerwowym.", uk = "Негайне полегшення передменструального синдрому та нервової напруги.", es = "Alivio inmediato del síndrome premenstrual y de la tensión nerviosa.", de = "Sofortige Linderung des prämenstruellen Syndroms und der nervlichen Anspannung."),
                tr("Tendance naturelle au calme, au recentrage et au lâcher-prise.", "Natural tendency towards calm, introspection, and slowing down.", pl = "Naturalna skłonność do spokoju, skupienia na sobie i odpuszczania.", uk = "Природна схильність до спокою, зосередження на собі та відпускання напруги.", es = "Tendencia natural a la calma, al recogimiento y a soltar.", de = "Natürliche Neigung zu Ruhe, Einkehr und Loslassen."),
                tr("Nouveau départ biologique : le corps commence déjà à préparer le cycle suivant.", "Fresh biological start: the body is already gearing up for the next cycle.", pl = "Biologiczny nowy początek: organizm już zaczyna przygotowywać następny cykl.", uk = "Біологічний новий початок: організм уже починає готувати наступний цикл.", es = "Nuevo comienzo biológico: el cuerpo ya empieza a preparar el ciclo siguiente.", de = "Biologischer Neubeginn: Der Körper bereitet bereits den nächsten Zyklus vor."),
            ),
            challenges = listOf(
                tr("Crampes utérines déclenchées par les prostaglandines lors des contractions.", "Uterine cramps caused by prostaglandins during contractions.", pl = "Skurcze macicy wywołane przez prostaglandyny podczas kurczenia się macicy.", uk = "Маткові спазми, спричинені простагландинами під час скорочень.", es = "Calambres uterinos provocados por las prostaglandinas durante las contracciones.", de = "Gebärmutterkrämpfe, ausgelöst durch Prostaglandine bei den Kontraktionen."),
                tr("Baisse d'énergie physique et perte modérée de fer via les saignements.", "Lower physical energy and moderate iron loss through bleeding.", pl = "Spadek energii fizycznej i umiarkowana utrata żelaza z krwawieniem.", uk = "Зниження фізичної енергії та помірна втрата заліза через кровотечу.", es = "Descenso de energía física y pérdida moderada de hierro por el sangrado.", de = "Nachlassende körperliche Energie und mäßiger Eisenverlust durch die Blutung."),
                tr("Sensibilité digestive ou petits maux de tête possibles les premiers jours.", "Digestive sensitivity or mild headaches possible in early days.", pl = "W pierwszych dniach możliwa wrażliwość układu pokarmowego lub niewielkie bóle głowy.", uk = "У перші дні можливі чутливість травлення або легкий головний біль.", es = "Los primeros días pueden aparecer sensibilidad digestiva o dolores de cabeza leves.", de = "In den ersten Tagen sind Verdauungsbeschwerden oder leichte Kopfschmerzen möglich."),
            ),
            supportAdvice = listOf(
                tr("Chaleur locale (bouillotte) pour relâcher les fibres musculaires de l'utérus.", "Local warmth (heating pad) to relax uterine muscle fibers.", pl = "Ciepło miejscowe (termofor), aby rozluźnić włókna mięśniowe macicy.", uk = "Місцеве тепло (грілка), щоб розслабити м'язові волокна матки.", es = "Calor local (bolsa de agua caliente) para relajar las fibras musculares del útero.", de = "Lokale Wärme (Wärmflasche), um die Muskelfasern der Gebärmutter zu entspannen."),
                tr("Privilégier les aliments riches en fer associés à la vitamine C.", "Favor iron-rich foods paired with vitamin C for better absorption.", pl = "Warto wybierać produkty bogate w żelazo, łącząc je z witaminą C.", uk = "Віддавати перевагу продуктам, багатим на залізо, у поєднанні з вітаміном C.", es = "Dar preferencia a los alimentos ricos en hierro combinados con vitamina C.", de = "Eisenreiche Lebensmittel in Kombination mit Vitamin C bevorzugen."),
                tr("Ne pas hésiter à alléger ses journées sans culpabilité.", "Lighten your schedule when possible without guilt.", pl = "Warto bez poczucia winy odciążyć swoje dni.", uk = "Не вагатися полегшувати свої дні без почуття провини.", es = "No dudar en aligerar los días sin sentir culpa.", de = "Ohne schlechtes Gewissen den Tag ruhig entlasten."),
            ),
        )

        Phase.FOLLICULAR -> PhaseProfile(
            phase = Phase.FOLLICULAR,
            title = tr("Phase folliculaire : regain d'élan et clarté", "Follicular phase: renewed drive and clarity", pl = "Faza folikularna: nowy zapał i jasność umysłu", uk = "Фолікулярна фаза: новий порив і ясність", es = "Fase folicular: nuevo impulso y claridad", de = "Follikelphase: neuer Schwung und Klarheit"),
            hormones = HormoneSnapshot(
                estrogen = HormoneLevel.RISING,
                progesterone = HormoneLevel.LOW,
                lh = HormoneLevel.LOW,
                fsh = HormoneLevel.DOMINANT,
                summary = tr(
                    fr = "La FSH stimule les follicules ovariens ; l'un d'eux mûrit et produit des œstrogènes en hausse.",
                    en = "FSH stimulates ovarian follicles; one matures and produces rising estrogen.",
                    pl = "FSH pobudza pęcherzyki jajnikowe; jeden z nich dojrzewa i wytwarza coraz więcej estrogenów.",
                    uk = "ФСГ стимулює фолікули яєчників; один із них дозріває й виробляє дедалі більше естрогенів.",
                    es = "La FSH estimula los folículos ováricos; uno de ellos madura y produce cada vez más estrógenos.",
                    de = "FSH stimuliert die Follikel der Eierstöcke; einer reift heran und bildet zunehmend Östrogen.",
                ),
            ),
            strengths = listOf(
                tr("Regain net d'énergie physique, de vitalité et d'endurance musculaire.", "Marked boost in physical energy, stamina, and muscle recovery.", pl = "Wyraźny przypływ energii fizycznej, witalności i wytrzymałości mięśni.", uk = "Помітний приплив фізичної енергії, життєвих сил і м'язової витривалості.", es = "Notable recuperación de la energía física, la vitalidad y la resistencia muscular.", de = "Spürbarer Zuwachs an körperlicher Energie, Vitalität und Muskelausdauer."),
                tr("Hausse de la motivation, curiosité et facilité pour démarrer de nouveaux projets.", "Higher motivation, curiosity, and ease starting new projects.", pl = "Większa motywacja, ciekawość i łatwość rozpoczynania nowych projektów.", uk = "Вища мотивація, цікавість і легкість у започаткуванні нових проєктів.", es = "Más motivación, curiosidad y facilidad para empezar nuevos proyectos.", de = "Mehr Motivation, Neugier und Leichtigkeit beim Start neuer Projekte."),
                tr("Peau souvent plus lumineuse grâce à la stimulation du collagène par les œstrogènes.", "Clearer, more hydrated skin thanks to estrogen stimulating collagen.", pl = "Skóra często bardziej promienna dzięki pobudzaniu kolagenu przez estrogeny.", uk = "Шкіра часто виглядає сяйливішою завдяки стимуляції колагену естрогенами.", es = "La piel suele verse más luminosa gracias a la estimulación del colágeno por los estrógenos.", de = "Die Haut wirkt oft strahlender, weil Östrogen die Kollagenbildung anregt."),
            ),
            challenges = listOf(
                tr("Transition parfois progressive les premiers jours après la fin des règles.", "Sometimes a slow transition in the first days right after bleeding stops.", pl = "Przejście bywa stopniowe w pierwszych dniach po zakończeniu miesiączki.", uk = "Перехід іноді буває поступовим у перші дні після завершення менструації.", es = "La transición a veces es gradual los primeros días tras el fin de la regla.", de = "Der Übergang verläuft in den ersten Tagen nach dem Ende der Periode manchmal allmählich."),
                tr("Risque d'en faire trop d'un coup par enthousiasme débordant.", "Risk of overcommitting or burnout from sudden surges of energy.", pl = "Ryzyko zrobienia za dużo naraz z nadmiaru entuzjazmu.", uk = "Ризик узятися за забагато одразу через надмірний ентузіазм.", es = "Riesgo de hacer demasiado de golpe por exceso de entusiasmo.", de = "Gefahr, aus überschäumender Begeisterung auf einmal zu viel zu tun."),
            ),
            supportAdvice = listOf(
                tr("Idéal pour planifier des entraînements sportifs plus intenses ou progressifs.", "Ideal time for higher-intensity workouts and progressive exercise.", pl = "Idealny moment na planowanie intensywniejszych lub stopniowo narastających treningów.", uk = "Ідеальний час для планування інтенсивніших або поступових тренувань.", es = "Ideal para planificar entrenamientos deportivos más intensos o progresivos.", de = "Ideal, um intensivere oder steigernde Trainingseinheiten zu planen."),
                tr("Profiter de la clarté cognitive pour les tâches d'apprentissage ou de synthèse.", "Leverage cognitive clarity for complex learning or strategic planning.", pl = "Warto wykorzystać jasność umysłu do nauki lub zadań wymagających syntezy.", uk = "Скористатися ясністю мислення для навчання чи завдань, що потребують синтезу.", es = "Aprovechar la claridad mental para tareas de aprendizaje o de síntesis.", de = "Die geistige Klarheit für Lern- oder Zusammenfassungsaufgaben nutzen."),
            ),
        )

        Phase.OVULATION -> PhaseProfile(
            phase = Phase.OVULATION,
            title = tr("Autour de l'ovulation : fertilité et rayonnement", "Around ovulation: fertility and peak vitality", pl = "Wokół owulacji: płodność i promienność", uk = "Навколо овуляції: фертильність і сяйво", es = "En torno a la ovulación: fertilidad y luminosidad", de = "Rund um den Eisprung: Fruchtbarkeit und Ausstrahlung"),
            hormones = HormoneSnapshot(
                estrogen = HormoneLevel.PEAK,
                progesterone = HormoneLevel.RISING,
                lh = HormoneLevel.PEAK,
                fsh = HormoneLevel.PEAK,
                summary = tr(
                    fr = "Le pic massif de LH provoque la rupture du follicule et libère l'ovocyte dans la trompe.",
                    en = "A surge in LH triggers follicle rupture, releasing the egg into the fallopian tube.",
                    pl = "Gwałtowny szczyt LH powoduje pęknięcie pęcherzyka i uwolnienie komórki jajowej do jajowodu.",
                    uk = "Різкий пік ЛГ спричиняє розрив фолікула й вивільняє яйцеклітину в маткову трубу.",
                    es = "El pico masivo de LH provoca la rotura del folículo y libera el óvulo en la trompa.",
                    de = "Der massive LH-Anstieg lässt den Follikel platzen und setzt die Eizelle in den Eileiter frei.",
                ),
            ),
            strengths = listOf(
                tr("Pic de sociabilité, de confiance en soi et d'aisance relationnelle.", "Peak confidence, sociability, and verbal ease in social settings.", pl = "Szczyt towarzyskości, pewności siebie i swobody w relacjach.", uk = "Пік товариськості, упевненості в собі та легкості у стосунках.", es = "Pico de sociabilidad, confianza en uno mismo y soltura en las relaciones.", de = "Höhepunkt an Geselligkeit, Selbstvertrauen und Leichtigkeit im Umgang mit anderen."),
                tr("Libido et désir généralement au plus haut du cycle.", "Libido and sexual desire typically at their highest of the cycle.", pl = "Libido i pożądanie zwykle na najwyższym poziomie w cyklu.", uk = "Лібідо та бажання зазвичай найвищі за весь цикл.", es = "Libido y deseo generalmente en su punto más alto del ciclo.", de = "Libido und Lust sind in der Regel auf dem Höhepunkt des Zyklus."),
                tr("Force musculaire et coordination motrice au maximum.", "Peak muscular strength and neuromuscular coordination.", pl = "Siła mięśni i koordynacja ruchowa na maksymalnym poziomie.", uk = "Максимальна м'язова сила та рухова координація.", es = "Fuerza muscular y coordinación motora al máximo.", de = "Muskelkraft und motorische Koordination auf dem Maximum."),
            ),
            challenges = listOf(
                tr("Tiraillements pelviens d'un côté (douleur ovarienne d'ovulation).", "Mild pelvic twinges on one side (mittelschmerz ovulation pain).", pl = "Ciągnięcie w miednicy po jednej stronie (ból owulacyjny jajnika).", uk = "Тягнучий біль у тазі з одного боку (овуляторний біль у яєчнику).", es = "Tirones pélvicos de un lado (dolor ovárico de la ovulación).", de = "Ziehen im Becken auf einer Seite (Eisprungschmerz, Mittelschmerz)."),
                tr("Glaire filante et abondante, sensation d'humidité intime marquée.", "Abundant, stretchy cervical mucus and heightened intimate sensation.", pl = "Obfity, ciągnący się śluz i wyraźne uczucie wilgotności w okolicach intymnych.", uk = "Рясний тягучий слиз і виразне відчуття вологи в інтимній ділянці.", es = "Moco filante y abundante, sensación marcada de humedad íntima.", de = "Reichlicher, fadenziehender Zervixschleim und deutliches Feuchtigkeitsgefühl im Intimbereich."),
                tr("Fenêtre fertile maximale : attention contraceptive stricte si pas de désir d'enfant.", "Peak fertility window: strict contraception needed if pregnancy is not desired.", pl = "Maksymalne okno płodne: ścisła czujność antykoncepcyjna, jeśli nie planuje się dziecka.", uk = "Максимальне фертильне вікно: сувора контрацептивна пильність, якщо немає бажання мати дитину.", es = "Ventana fértil máxima: atención estricta a la anticoncepción si no se desea tener un hijo.", de = "Maximales fruchtbares Fenster: Verhütung konsequent beachten, wenn kein Kinderwunsch besteht."),
            ),
            supportAdvice = listOf(
                tr("Noter la glaire cervicale pour repérer précisément votre propre schéma corporel.", "Log cervical mucus observations to map your personal body patterns.", pl = "Warto zapisywać śluz szyjkowy, aby dokładnie rozpoznać własny wzorzec swojego ciała.", uk = "Записувати цервікальний слиз, щоб точно визначити власну закономірність свого тіла.", es = "Anotar el moco cervical para identificar con precisión tu propio patrón corporal.", de = "Den Zervixschleim eintragen, um dein eigenes körperliches Muster genau zu erkennen."),
                tr("Garder en tête que les spermatozoïdes peuvent attendre l'ovocyte jusqu'à 5 jours.", "Remember that sperm can wait for the egg for up to 5 days.", pl = "Pamiętać, że plemniki mogą czekać na komórkę jajową nawet do 5 dni.", uk = "Пам'ятати, що сперматозоїди можуть чекати на яйцеклітину до 5 днів.", es = "Tener presente que los espermatozoides pueden esperar al óvulo hasta 5 días.", de = "Bedenken, dass Spermien bis zu 5 Tage auf die Eizelle warten können."),
            ),
        )

        Phase.LUTEAL -> PhaseProfile(
            phase = Phase.LUTEAL,
            title = tr("Phase lutéale : cocooning et vigilance", "Luteal phase: calm focus and self-care", pl = "Faza lutealna: kokon i czujność", uk = "Лютеїнова фаза: затишок і пильність", es = "Fase lútea: recogimiento y vigilancia", de = "Lutealphase: Rückzug und Aufmerksamkeit"),
            hormones = HormoneSnapshot(
                estrogen = HormoneLevel.FALLING,
                progesterone = HormoneLevel.DOMINANT,
                lh = HormoneLevel.LOW,
                fsh = HormoneLevel.LOW,
                summary = tr(
                    fr = "Le corps jaune sécrète la progestérone qui épaissit la muqueuse utérine, avant de chuter.",
                    en = "The corpus luteum secretes progesterone to nourish the uterine lining, then drops.",
                    pl = "Ciałko żółte wydziela progesteron, który pogrubia błonę śluzową macicy, po czym jego poziom spada.",
                    uk = "Жовте тіло виділяє прогестерон, що потовщує слизову оболонку матки, перш ніж його рівень падає.",
                    es = "El cuerpo lúteo secreta progesterona, que engrosa la mucosa uterina, antes de que baje.",
                    de = "Der Gelbkörper bildet Progesteron, das die Gebärmutterschleimhaut verdickt, bevor es abfällt.",
                ),
            ),
            strengths = listOf(
                tr("Progestérone calmante en première partie : sommeil plus profond et détente.", "Calming progesterone in the first half: deeper sleep and physical relaxation.", pl = "Uspokajający progesteron w pierwszej części: głębszy sen i odprężenie.", uk = "Заспокійливий прогестерон на початку фази: глибший сон і розслаблення.", es = "Progesterona calmante en la primera parte: sueño más profundo y relajación.", de = "Beruhigendes Progesteron in der ersten Hälfte: tieferer Schlaf und Entspannung."),
                tr("Sens du détail aiguisé, propice au tri, à l'organisation et aux finitions.", "Keen eye for detail, well suited for organizing, editing, and closure.", pl = "Wyostrzony zmysł szczegółu, sprzyjający porządkowaniu, organizacji i dopracowywaniu.", uk = "Загострене відчуття деталей, що сприяє сортуванню, організації та доопрацюванню.", es = "Sentido del detalle más agudo, propicio para ordenar, organizar y rematar.", de = "Geschärfter Blick fürs Detail, günstig zum Aussortieren, Organisieren und Abschließen."),
                tr("Métabolisme basal légèrement accru (dépense énergétique un peu plus élevée).", "Slightly elevated basal metabolic rate (burning slightly more energy).", pl = "Nieco podwyższona przemiana podstawowa (zużycie energii trochę większe).", uk = "Трохи підвищений базальний метаболізм (витрата енергії дещо вища).", es = "Metabolismo basal ligeramente aumentado (gasto energético algo mayor).", de = "Leicht erhöhter Grundumsatz (etwas höherer Energieverbrauch)."),
            ),
            challenges = listOf(
                tr("Baisse de sérotonine en fin de phase : hypersensibilité émotionnelle ou irritabilité.", "Drop in serotonin later in the phase: mood swings, irritability, or tearfulness.", pl = "Spadek serotoniny pod koniec fazy: nadwrażliwość emocjonalna lub drażliwość.", uk = "Зниження серотоніну наприкінці фази: емоційна надчутливість або дратівливість.", es = "Descenso de serotonina al final de la fase: hipersensibilidad emocional o irritabilidad.", de = "Abfall des Serotonins am Ende der Phase: emotionale Überempfindlichkeit oder Reizbarkeit."),
                tr("Rétention d'eau, sensation de gonflement abdominal et seins tendus.", "Water retention, bloating sensation, and tender or swollen breasts.", pl = "Zatrzymanie wody, uczucie wzdęcia brzucha i tkliwe piersi.", uk = "Затримка води, відчуття здуття живота та чутливі груди.", es = "Retención de líquidos, sensación de hinchazón abdominal y pechos sensibles.", de = "Wassereinlagerungen, Gefühl eines geblähten Bauchs und gespannte Brüste."),
                tr("Fringales glucidiques (réflexe naturel du cerveau face au déficit de sérotonine).", "Carb cravings (the brain's natural response to lower serotonin).", pl = "Zachcianki na węglowodany (naturalny odruch mózgu wobec niedoboru serotoniny).", uk = "Тяга до вуглеводів (природна реакція мозку на нестачу серотоніну).", es = "Antojos de hidratos de carbono (reflejo natural del cerebro ante el déficit de serotonina).", de = "Heißhunger auf Kohlenhydrate (natürliche Reaktion des Gehirns auf den Serotoninmangel)."),
            ),
            supportAdvice = listOf(
                tr("Apport suffisant en magnésium et calcium pour calmer les tensions neuromusculaires.", "Ensure adequate magnesium and calcium intake to ease neuromuscular tension.", pl = "Wystarczająca podaż magnezu i wapnia, aby łagodzić napięcia nerwowo-mięśniowe.", uk = "Достатнє надходження магнію та кальцію, щоб заспокоїти нервово-м'язову напругу.", es = "Aporte suficiente de magnesio y calcio para calmar las tensiones neuromusculares.", de = "Ausreichende Zufuhr von Magnesium und Calcium, um neuromuskuläre Spannungen zu beruhigen."),
                tr("Modérer les excitants (caféine, alcool) qui accentuent l'anxiété prémenstruelle.", "Moderate caffeine and alcohol, which can exacerbate premenstrual anxiety.", pl = "Ograniczać używki pobudzające (kofeina, alkohol), które nasilają lęk przedmiesiączkowy.", uk = "Помірковано вживати збудливі речовини (кофеїн, алкоголь), які посилюють передменструальну тривожність.", es = "Moderar los excitantes (cafeína, alcohol) que acentúan la ansiedad premenstrual.", de = "Anregende Stoffe (Koffein, Alkohol) mäßigen, da sie die prämenstruelle Angst verstärken."),
                tr("Privilégier les activités douces (marche, étirements, respiration 4-6).", "Favor soothing activities such as walking, gentle stretching, or 4-6 breathing.", pl = "Wybierać łagodne aktywności (spacer, rozciąganie, oddychanie 4-6).", uk = "Віддавати перевагу м'яким активностям (ходьба, розтягування, дихання 4-6).", es = "Dar preferencia a las actividades suaves (caminar, estiramientos, respiración 4-6).", de = "Sanfte Aktivitäten bevorzugen (Spazierengehen, Dehnen, 4-6-Atmung)."),
            ),
        )

        Phase.LATE -> PhaseProfile(
            phase = Phase.LATE,
            title = tr("Retard de règles : écoute et bienveillance", "Late period: gentle observation", pl = "Opóźniająca się miesiączka: uważność i życzliwość", uk = "Затримка менструації: прислухання до себе й дбайливість", es = "Regla con retraso: escucha y amabilidad", de = "Verspätete Periode: Achtsamkeit und Wohlwollen"),
            hormones = HormoneSnapshot(
                estrogen = HormoneLevel.LOW,
                progesterone = HormoneLevel.LOW,
                lh = HormoneLevel.LOW,
                fsh = HormoneLevel.LOW,
                summary = tr(
                    fr = "La phase lutéale s'étire ou l'ovulation a été décalée par un facteur de vie ou de santé.",
                    en = "The cycle is prolonged or ovulation was delayed by stress, travel, or pregnancy.",
                    pl = "Faza lutealna się wydłuża albo owulacja została przesunięta przez czynnik związany ze stylem życia lub zdrowiem.",
                    uk = "Лютеїнова фаза подовжується, або овуляція зсунулася через чинник способу життя чи здоров'я.",
                    es = "La fase lútea se alarga o la ovulación se ha retrasado por un factor de estilo de vida o de salud.",
                    de = "Die Lutealphase zieht sich hin oder der Eisprung hat sich durch einen Lebens- oder Gesundheitsfaktor verschoben.",
                ),
            ),
            strengths = listOf(
                tr("Occasion d'écouter les signaux récents de fatigue, de stress ou de voyage.", "Opportunity to reflect on recent stress, travel, or lifestyle shifts.", pl = "Okazja, by wsłuchać się w ostatnie sygnały zmęczenia, stresu lub podróży.", uk = "Нагода прислухатися до недавніх сигналів втоми, стресу чи подорожі.", es = "Ocasión para escuchar las señales recientes de cansancio, estrés o viaje.", de = "Gelegenheit, auf die jüngsten Signale von Müdigkeit, Stress oder Reisen zu hören."),
            ),
            challenges = listOf(
                tr("Inquiétude ou incertitude liée à l'attente des règles.", "Uncertainty or concern while waiting for bleeding to start.", pl = "Niepokój lub niepewność związane z oczekiwaniem na miesiączkę.", uk = "Тривога чи невизначеність, пов'язані з очікуванням менструації.", es = "Inquietud o incertidumbre ligadas a la espera de la regla.", de = "Sorge oder Unsicherheit beim Warten auf die Periode."),
            ),
            supportAdvice = listOf(
                tr("Un test urinaire est fiable dès le premier jour de retard présumé.", "A home urine test is reliable from the first day of an expected missed period.", pl = "Test moczowy jest wiarygodny już od pierwszego dnia przypuszczalnego opóźnienia.", uk = "Сечовий тест достовірний уже з першого дня передбачуваної затримки.", es = "Una prueba de orina es fiable desde el primer día de retraso presunto.", de = "Ein Urintest ist ab dem ersten Tag der vermuteten Verspätung zuverlässig."),
                tr("Si une pause est souhaitée (grossesse ou contraception), le mode pause est disponible.", "If you need a break from forecasts, the pause mode is available in settings.", pl = "Jeśli pożądana jest przerwa (ciąża lub antykoncepcja), dostępny jest tryb pauzy.", uk = "Якщо потрібна пауза (вагітність або контрацепція), доступний режим паузи.", es = "Si se desea una pausa (embarazo o anticoncepción), está disponible el modo pausa.", de = "Wenn eine Pause gewünscht ist (Schwangerschaft oder Verhütung), steht der Pausenmodus zur Verfügung."),
            ),
        )
    }
}
