package fr.lunaison.core.content

/**
 * Artikel auf Deutsch, übersetzt aus den Originaltexten.
 * Identische Struktur, Identifikatoren und klinische Quellen.
 */
internal object ArticlesDe {

    val cycle = Article(
        id = "cycle",
        title = "Den Zyklus verstehen",
        summary = "Was während eines Zyklus geschieht und warum der eigene nicht zwingend wie im Lehrbuch verläuft.",
        sections = listOf(
            Section(
                "Was ist ein Zyklus?",
                listOf(
                    "Ein Zyklus reicht vom ersten Tag einer Blutung bis zum ersten Tag der nächsten Blutung. Der erste Tag ist der erste Tag einer echten Blutung: leichte Schmierblutungen zählen noch nicht mit.",
                    "Während eines Zyklus steigen und fallen zwei von den Eierstöcken gebildete Hormone: Östrogen und Progesteron. Sie bereiten die Gebärmutterschleimhaut auf eine mögliche Schwangerschaft vor. Bleibt diese aus, wird die Schleimhaut abgestoßen: das ist die Periode.",
                ),
            ),
            Section(
                "Die vier Phasen",
                listOf(
                    "Die Periode: Die Gebärmutterschleimhaut wird abgestoßen. Die Hormone sind auf ihrem Tiefststand.",
                    "Die Follikelphase: Ein Follikel reift im Eierstock heran und das Östrogen steigt an.",
                    "Der Eisprung: Der Eierstock gibt eine Eizelle frei. Dies geschieht nicht an einem festen Kalendertag.",
                    "Die Lutealphase: Progesteron dominiert bis zur nächsten Blutung. Prämenstruelle Symptome treten häufig am Ende dieser Phase auf.",
                ),
            ),
            Section(
                "Der 28-Tage-Zyklus ist ein Durchschnittswert, keine Norm",
                listOf(
                    "Eine Studie mit über 600 000 Zyklen ermittelte eine durchschnittliche Zykluslänge von 29,3 Tagen und nur 13 % Zyklen von exakt 28 Tagen. Der Eisprung findet nicht bei allen am 14. Tag statt: Vor allem die Phase vor dem Eisprung variiert von Mensch zu Mensch.",
                    "Bei Erwachsenen liegen Zyklen von 24 bis 38 Tagen und eine Blutungsdauer von bis zu 8 Tagen im Normalbereich. Schwankungen von wenigen Tagen von Monat zu Monat sind völlig normal.",
                    "In der Jugendzeit sind die Zyklen in den ersten Jahren oft unregelmäßig: Die meisten dauern zwischen 21 und 45 Tagen.",
                ),
            ),
            Section(
                "Wie Lunaison seine Schätzungen berechnet",
                listOf(
                    "Lunaison erfasst deine Perioden und berechnet die übliche Länge deiner letzten Zyklen. Je mehr Zyklen du einträgst, desto verlässlicher wird die Schätzung. Der Eisprung wird etwa 13 Tage vor der nächsten Periode vermutet, da dieser letzte Zyklusabschnitt kaum schwankt.",
                    "Diese Schätzungen helfen bei der Alltagsorganisation. Sie eignen sich ausdrücklich nicht zur Verhütung.",
                ),
            ),
        ),
        sources = listOf(Sources.BULL_2019, Sources.FIGO_2018, Sources.ACOG_ADOLESCENTS_2015),
    )

    val pain = Article(
        id = "douleurs",
        title = "Regelschmerzen lindern",
        summary = "Was nachweislich hilft, was unterstützen kann und wann Schmerzen ärztlich abgeklärt werden sollten.",
        sections = listOf(
            Section(
                "Woher der Schmerz kommt",
                listOf(
                    "Während der Periode bildet die Gebärmutter Prostaglandine. Diese Botenstoffe sorgen dafür, dass sie sich zusammenzieht, um die Schleimhaut abzustoßen, was Schmerzen verursachen kann. Deshalb sind Wirkstoffe, die Prostaglandine hemmen, besonders wirksam.",
                ),
            ),
            Section(
                "Entzündungshemmende Schmerzmittel",
                listOf(
                    "Nichtsteroidale Antirheumatika (NSAR) wie Ibuprofen lindern Regelschmerzen besser als Paracetamol. In Studien brachten sie rund der Hälfte der Betroffenen spürbare Erleichterung, verglichen mit weniger als einem Fünftel unter Placebo.",
                    "Sie wirken am besten, wenn sie direkt bei Beginn der Schmerzen oder der Blutung eingenommen werden. Beachte stets die Packungsbeilage und halte Rücksprache mit Arzt oder Apotheke bei Magen-, Nierenproblemen, Asthma oder möglicher Schwangerschaft.",
                ),
            ),
            Section(
                "Wärme",
                listOf(
                    "Eine Wärmflasche oder ein Wärmepflaster auf dem Unterbauch entspannt die Muskulatur. In Studien war der lindernde Effekt vergleichbar mit Schmerzmitteln. Vorsicht vor Verbrennungen: Wärme niemals zu heiß direkt auf die ungeschützte Haut auflegen.",
                ),
            ),
            Section(
                "Bewegung",
                listOf(
                    "Regelmäßige körperliche Aktivität, etwa drei Einheiten von 45 bis 60 Minuten pro Woche, kann die Schmerzintensität nachweislich verringern. Spazierengehen, Schwimmen oder sanftes Yoga: Wähle, was dir gut tut.",
                ),
            ),
            Section(
                "Ingwer",
                listOf(
                    "Kleinere Studien weisen darauf hin, dass Ingwerpulver in den ersten Tagen der Blutung Schmerzen lindern kann. Die wissenschaftliche Beweislage ist begrenzt, ein Versuch kann sich aber lohnen.",
                ),
            ),
            Section(
                "Wann ärztlicher Rat wichtig ist",
                listOf(
                    "Regelschmerzen kommen häufig vor. Schmerzen jedoch, die dich an Schule, Studium oder Arbeit hindern, auf Schmerzmittel nicht ansprechen oder über die Jahre zunehmen, sind nicht normal. Sie können auf Endometriose oder andere behandelbare Ursachen hinweisen.",
                    "Sprich mit deiner gynäkologischen Praxis. Notiere deine Beschwerden in Lunaison: Ein präziser Verlauf ist im Gespräch eine wertvolle Hilfe.",
                ),
            ),
        ),
        sources = listOf(
            Sources.COCHRANE_NSAIDS_2015,
            Sources.HEAT_2018,
            Sources.COCHRANE_EXERCISE_2019,
            Sources.GINGER_2015,
            Sources.ACOG_DYSMENORRHEA_2018,
            Sources.ESHRE_ENDOMETRIOSIS_2022,
        ),
    )

    val food = Article(
        id = "alimentation",
        title = "Ernährung und Zyklus",
        summary = "Eisen an erster Stelle. Ansonsten fundierte Orientierung und keine Wunderdiäten.",
        sections = listOf(
            Section(
                "Eisen",
                listOf(
                    "Durch die Blutung geht Eisen verloren. Menstruierende Menschen haben daher einen erhöhten Bedarf: Die Europäische Behörde für Lebensmittelsicherheit (EFSA) empfiehlt 16 mg täglich als Richtwert. Starke Blutungen können zu Eisenmangel führen, der sich durch Müdigkeit und Abgeschlagenheit äußert.",
                    "Am besten verwertbares Eisen findet sich in Fleisch und Fisch. Hülsenfrüchte (Linsen, Kichererbsen), Tofu, Vollkorngetreide und grünes Gemüse sind ebenfalls gute Quellen. Kombiniere sie mit Vitamin C (Zitrusfrüchte, Paprika, Beeren) und trinke keinen Kaffee oder schwarzen Tee zu den Mahlzeiten.",
                    "Nimm Eisenpräparate nie ohne ärztliche Anweisung ein: Ein einfacher Bluttest zeigt zuverlässig, ob Bedarf besteht.",
                ),
            ),
            Section(
                "Omega-3-Fettsäuren",
                listOf(
                    "Omega-3-Fettsäuren aus fettem Fisch (Lachs, Makrele, Hering), Walnüssen und Lein- oder Rapsöl konnten Regelschmerzen in mehreren Studien leicht verringern. Eine ausgewogene Ernährung deckt diesen Bedarf unkompliziert ab.",
                ),
            ),
            Section(
                "Calcium",
                listOf(
                    "Eine Studie zeigte, dass eine ausreichende Calciumzufuhr prämenstruelle Beschwerden mildern kann. Milchprodukte, calciumreiches Mineralwasser, Mandeln, Grünkohl und Tofu sind verlässliche Quellen.",
                ),
            ),
            Section(
                "Was wissenschaftlich nicht belegt ist",
                listOf(
                    "Kein Lebensmittel lässt Schmerzen völlig verschwinden, und Ernährungspläne, die für jede Zyklusphase strikt alles umwerfen, entbehren solider wissenschaftlicher Grundlagen. Regelmäßige Mahlzeiten, ausreichend Wasser und Vielfalt bleiben die beste Basis.",
                ),
            ),
        ),
        sources = listOf(Sources.EFSA_IRON_2015, Sources.OMEGA3_2022, Sources.CALCIUM_PMS_1998, Sources.GINGER_2015),
    )

    val pms = Article(
        id = "spm",
        title = "Das prämenstruelle Syndrom",
        summary = "Symptome in den Tagen vor der Blutung, was Linderung verschafft und wann Hilfe nötig ist.",
        sections = listOf(
            Section(
                "Worum es sich handelt",
                listOf(
                    "In den Tagen vor der Periode spüren viele Menschen empfindliche Brüste, ein Völlegefühl, Erschöpfung, Reizbarkeit oder gedrückte Stimmung. Diese Symptome klingen mit dem Einsetzen der Blutung meist rasch ab.",
                    "Indem du sie in Lunaison festhältst, erkennst du, ob sie in jedem Zyklus wiederkehren. Die Startseite zeigt dir an, was für deine aktuelle Phase typisch ist.",
                ),
            ),
            Section(
                "Was helfen kann",
                listOf(
                    "Regelmäßige Bewegung, verlässlicher Schlaf und geregelte Mahlzeiten federn die Beschwerden oft ab. Auch eine gute Calciumversorgung zeigte in Studien positive Effekte.",
                    "Zu wissen, dass diese Tage bevorstehen, nimmt bereits Druck: Plane anstehende Termine etwas lockerer und informiere dein Umfeld.",
                ),
            ),
            Section(
                "Wann professionelle Unterstützung wichtig ist",
                listOf(
                    "Wenn deine Stimmung vor der Blutung massiv einbricht oder Ängste und Wut deinen Alltag belasten, suche ärztlichen Rat. Eine schwere Form, die prämenstruelle dysphorische Störung (PMDS), lässt sich gezielt behandeln. Bei akuten Krisen oder suizidalen Gedanken wende dich sofort an die TelefonSeelsorge unter 116 123 (Deutschland) oder den Notruf 112.",
                ),
            ),
        ),
        sources = listOf(Sources.CALCIUM_PMS_1998, Sources.COCHRANE_EXERCISE_2019),
    )

    val consult = Article(
        id = "consulter",
        title = "Wann zum Arzt?",
        summary = "Anhaltspunkte für eine fachliche Abklärung und akute Warnsignale.",
        sections = listOf(
            Section(
                "Einen Termin vereinbaren",
                listOf(
                    "Sprich mit deiner Ärztin, deinem Arzt oder deiner Hebamme, wenn du Folgendes bemerkst:",
                    "Schmerzen, die dich im Alltag einschränken oder auf übliche Schmerzmittel nicht ansprechen;",
                    "Blutungen, die länger als 8 Tage anhalten oder so stark sind, dass Binde oder Tampon alle 1 bis 2 Stunden gewechselt werden müssen, oder Blutkoageln größer als 2,5 cm;",
                    "Zyklen, die bei Erwachsenen regelmäßig kürzer als 24 Tage oder länger als 38 Tage sind (21 bis 45 Tage in der Jugend);",
                    "mehr als drei Monate Ausbleiben der Periode außerhalb einer Schwangerschaft;",
                    "Zwischenblutungen oder Blutungen nach dem Geschlechtsverkehr;",
                    "ungewöhnliche Erschöpfung oder Atemnot bei starken Blutungen als mögliches Zeichen von Eisenmangel.",
                ),
            ),
            Section(
                "Endometriose",
                listOf(
                    "Endometriose betrifft etwa jede zehnte menstruierende Person. Bis zur Diagnose vergehen im Schnitt 7 bis 12 Jahre, oft weil Schmerzen fälschlicherweise als normal abgetan werden. Sehr starke Krämpfe, Schmerzen beim Geschlechtsverkehr oder beim Wasserlassen während der Periode sind triftige Gründe, auf einer Abklärung zu bestehen.",
                ),
            ),
            Section(
                "Den Notruf (112) wählen",
                listOf(
                    "Plötzliche, heftigste Schmerzen im Unterbauch, insbesondere wenn eine Schwangerschaft denkbar ist.",
                    "Sehr starke Blutung mit Ohnmacht, Schwindel oder ausgeprägter Blässe.",
                    "Fieber in Verbindung mit Unterleibsschmerzen.",
                    "Plötzliches hohes Fieber, Erbrechen, sonnenbrandähnlicher Hautausschlag oder Kreislaufkollaps beim Tragen eines Tampons oder einer Menstruationstasse: Sofort entfernen und 112 wählen (Verdacht auf toxisches Schocksyndrom).",
                ),
            ),
        ),
        sources = listOf(
            Sources.FIGO_2018,
            Sources.MSD_PARAMETERS,
            Sources.NHS_HEAVY_PERIODS,
            Sources.ACOG_ADOLESCENTS_2015,
            Sources.ENDOMETRIOSIS_2024,
            Sources.ACOG_DYSMENORRHEA_2018,
            Sources.ESHRE_ENDOMETRIOSIS_2022,
            Sources.PTGIP_ENDOMETRIOSIS_2024,
            Sources.AWMF_DGGG_ENDOMETRIOSIS_2020,
            Sources.SANIDAD_ES_ENDOMETRIOSIS_2013,
            Sources.MOZ_UA_ENDOMETRIOSIS_2016,
        ),
    )

    val protection = Article(
        id = "protections",
        title = "Periodenprodukte und TSS",
        summary = "Tampons und Tassen sicher anwenden, um seltene, aber ernste Risiken zu vermeiden.",
        sections = listOf(
            Section(
                "Das toxische Schocksyndrom (TSS)",
                listOf(
                    "TSS ist eine seltene Infektion, die durch Gifte des Bakteriums Staphylococcus aureus ausgelöst wird. Sie steht im Zusammenhang mit zu langem Tragen von internen Periodenprodukten wie Tampons oder Menstruationstassen.",
                ),
            ),
            Section(
                "Wichtige Hygieneregeln",
                listOf(
                    "Wasche dir vor und nach dem Wechseln der Periodenprodukte gründlich die Hände.",
                    "Halte die maximale Tragedauer der Packungsbeilage genau ein und überschreite sie nachts nicht: Nutze zum Schlafen lieber Binden oder Periodenunterwäsche.",
                    "Wähle immer die kleinste Saugstärke, die für deine Blutung ausreicht, und verwende Tampons ausschließlich während der Menstruation.",
                ),
            ),
            Section(
                "Warnzeichen",
                listOf(
                    "Plötzliches Fieber, Erbrechen, Durchfall, sonnenbrandartiger Ausschlag oder Schwindel beim Tragen von Tampons oder Tassen: Produkt sofort entfernen und umgehend den Notdienst rufen.",
                ),
            ),
        ),
        sources = listOf(Sources.ANSES_TSS_2025),
    )

    val predictions = Article(
        id = "previsions",
        title = "Was Prognosen leisten können",
        summary = "Nützliche Schätzungen für die Alltagsplanung, keine Methode zur Empfängnisverhütung.",
        sections = listOf(
            Section(
                "Ein Zeitraum, kein fester Stichtag",
                listOf(
                    "Selbst bei Menschen mit sehr regelmäßigem Zyklus schwankt die Dauer um einige Tage. Lunaison zeigt daher einen wahrscheinlichen Zeitraum an, der sich präzisiert, je mehr Zyklen eingetragen werden.",
                ),
            ),
            Section(
                "Keine Verhütung",
                listOf(
                    "Der Eisprung lässt sich nicht auf den Tag genau vorhersehen: Lunaison schätzt ihn anhand früherer Zyklen mit einer Fehlertoleranz. Kalenderbasierte Methoden versagen in der Praxis häufig: Bis zu eine von vier Personen wird bei typischer Anwendung innerhalb eines Jahres schwanger. Zur Verhütung wende dich an eine gynäkologische Fachberatung.",
                ),
            ),
            Section(
                "Wenn die Periode ausbleibt",
                listOf(
                    "Eine Verzögerung kann viele Ursachen haben: Stress, Reisen, Infekte, Gewichtsveränderungen, intensiver Sport, Stillzeit oder Schwangerschaft. Ist eine Schwangerschaft möglich, liefert ein Urintest ab dem Fälligkeitstag meist ein verlässliches Ergebnis.",
                ),
            ),
        ),
        sources = listOf(Sources.BULL_2019, Sources.TRUSSELL_2011),
    )

    val partners = Article(
        id = "entourage",
        title = "Für Partner und Bezugspersonen",
        summary = "Verstehen, was der Körper im Zyklus leistet, und aufmerksam unterstützen, ohne aufdringlich zu sein.",
        sections = listOf(
            Section(
                "Hintergrundwissen",
                listOf(
                    "Die Periode und die Tage davor können mit realen Schmerzen, Erschöpfung und Stimmungsschwankungen einhergehen. Das ist weder Einbildung noch Übertreibung.",
                    "Jeder Zyklus verläuft anders und jeder Mensch reagiert individuell. Was wirklich hilft, erfährt man am besten durch offenes, zugewandtes Nachfragen.",
                ),
            ),
            Section(
                "Was oft entlastet",
                listOf(
                    "Alltagsaufgaben übernehmen, ohne erst auf Aufforderung zu warten.",
                    "Eine Wärmflasche, eine stärkende Mahlzeit, Ruhe oder einfach verlässliche Gesellschaft anbieten.",
                    "Die Stimmung nicht abwertend kommentieren: Bemerkungen wie « Hast du etwa deine Tage? » verletzen, statt zu helfen.",
                ),
            ),
            Section(
                "Was geteilt wird",
                listOf(
                    "Wenn dir jemand eine Nachricht aus Lunaison sendet, geschieht das immer ganz bewusst und manuell in diesem Moment. Es wird nichts automatisch übertragen. Respektiere auch Tage, an denen nichts geteilt wird: Das ist das gute Recht der Person.",
                ),
            ),
        ),
        sources = emptyList(),
    )

    val all: List<Article> = listOf(cycle, pain, pms, food, consult, protection, predictions, partners)
}
