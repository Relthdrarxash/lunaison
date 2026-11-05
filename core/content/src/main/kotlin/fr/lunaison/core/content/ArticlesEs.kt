package fr.lunaison.core.content

/**
 * Artículos en español, traducidos de los artículos originales.
 * Misma estructura, mismos identificadores y mismas fuentes clínicas.
 */
internal object ArticlesEs {

    val cycle = Article(
        id = "cycle",
        title = "Comprender el ciclo",
        summary = "Qué ocurre durante un ciclo y por qué el tuyo no siempre se parece al de los manuales.",
        sections = listOf(
            Section(
                "¿Qué es un ciclo?",
                listOf(
                    "Un ciclo va desde el primer día de una regla hasta el primer día de la siguiente. El primer día es el de sangrado real: las simples manchas no cuentan.",
                    "Durante el ciclo, dos hormonas producidas por los ovarios, los estrógenos y la progesterona, suben y bajan. Preparan el endometrio para un posible embarazo. Sin embarazo, esa capa se desprende: es la menstruación.",
                ),
            ),
            Section(
                "Las cuatro fases",
                listOf(
                    "La menstruación: la pared del útero se desprende. Las hormonas están en su nivel más bajo.",
                    "La fase folicular: un folículo madura en un ovario y los estrógenos aumentan.",
                    "La ovulación: el ovario libera un óvulo. No ocurre en una fecha fija.",
                    "La fase lútea: predomina la progesterona hasta la siguiente regla. Los síntomas premenstruales suelen aparecer al final de esta fase.",
                ),
            ),
            Section(
                "El ciclo de 28 días es una media, no una norma",
                listOf(
                    "Un estudio de más de 600 000 ciclos halló una duración media de 29,3 días, y solo un 13 % de ciclos de exactamente 28 días. La ovulación no ocurre el día 14 en todo el mundo: varía sobre todo la fase previa a la ovulación.",
                    "En personas adultas, un ciclo de 24 a 38 días y una menstruación de hasta 8 días entran en los rangos habituales. Una variación de unos días entre ciclos es normal.",
                    "Durante la adolescencia, en los primeros años, los ciclos suelen ser irregulares: la mayoría duran entre 21 y 45 días.",
                ),
            ),
            Section(
                "Cómo calcula Lunaison sus estimaciones",
                listOf(
                    "Lunaison observa tus reglas y calcula la duración habitual de tus últimos ciclos. Cuantos más ciclos anotes, más fiable será la estimación. La ovulación se calcula unos 13 días antes de la regla siguiente, ya que esta última parte varía poco.",
                    "Estas estimaciones ayudan a organizarse. No sirven para evitar un embarazo.",
                ),
            ),
        ),
        sources = listOf(Sources.BULL_2019, Sources.FIGO_2018, Sources.ACOG_ADOLESCENTS_2015),
    )

    val pain = Article(
        id = "douleurs",
        title = "Aliviar el dolor menstrual",
        summary = "Lo que ha demostrado eficacia, lo que puede ayudar y cuándo el dolor debe motivar una consulta.",
        sections = listOf(
            Section(
                "De dónde procede el dolor",
                listOf(
                    "Durante la regla, el útero produce prostaglandinas. Estas sustancias hacen que se contraiga para expulsar el endometrio, y esas contracciones duelen. Por eso los fármacos que bloquean las prostaglandinas son eficaces.",
                ),
            ),
            Section(
                "Los antiinflamatorios",
                listOf(
                    "Los antiinflamatorios no esteroideos, como el ibuprofeno, alivian mejor que el paracetamol. En los ensayos clínicos, aportaron un alivio claro a cerca de la mitad de las personas, frente a menos de una de cada cinco con placebo.",
                    "Son más eficaces si se toman al inicio del dolor o al empezar la regla. Respeta el prospecto. Consulta a tu médico o farmacéutico si tienes problemas gástricos, renales, asma, posible embarazo u otro tratamiento.",
                ),
            ),
            Section(
                "El calor",
                listOf(
                    "Una bolsa de agua caliente o un parche térmico en el bajo vientre calma el dolor. En los estudios, el efecto fue comparable al de los antiinflamatorios. Precaución con las quemaduras: nunca apliques calor excesivo directo en la piel.",
                ),
            ),
            Section(
                "Moverse",
                listOf(
                    "Una actividad física regular, unas tres sesiones de 45 minutos a una hora por semana, puede reducir la intensidad del dolor menstrual. Los estudios son modestos, pero el riesgo es bajo y aporta muchos otros beneficios. Caminar, nadar, yoga: elige lo que mejor te siente.",
                ),
            ),
            Section(
                "El jengibre",
                listOf(
                    "Algunos ensayos pequeños sugieren que el jengibre en polvo, tomado los primeros días de la regla, reduce el dolor. Las pruebas son limitadas, pero puede probarse sin sustituir a un tratamiento eficaz.",
                ),
            ),
            Section(
                "Cuándo consultar",
                listOf(
                    "El dolor menstrual es frecuente, pero un dolor que te impide ir a clase o trabajar, que no cede con antiinflamatorios o que empeora con los años no es normal. Puede indicar endometriosis u otra causa tratable.",
                    "Habla de ello con tu médico, matrona o ginecólogo. Anota tus síntomas en Lunaison: un historial preciso ayuda mucho en consulta.",
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
        title = "Alimentación y menstruación",
        summary = "El hierro ante todo. En lo demás, pautas sencillas y ninguna dieta milagrosa.",
        sections = listOf(
            Section(
                "El hierro",
                listOf(
                    "La regla implica pérdida de hierro. En personas que menstrúan, las necesidades son mayores: la Autoridad Europea de Seguridad Alimentaria fija 16 mg al día como ingesta de referencia. Reglas abundantes pueden causar déficit de hierro, fatiga y falta de aire.",
                    "El hierro mejor asimilado procede de carne, pescado y marisco. Legumbres (lentejas, garbanzos), tofu, cereales integrales y verduras de hoja verde también lo contienen. Combínalos con vitamina C (cítricos, pimientos, kiwi) y evita té o café durante esas comidas.",
                    "No tomes suplementos de hierro sin prescripción médica: un simple análisis de sangre indica si son necesarios.",
                ),
            ),
            Section(
                "Los omega-3",
                listOf(
                    "Los ácidos grasos omega-3 presentes en pescados azules (sardina, caballa, salmón), nueces y aceite de colza o lino redujeron el dolor en varios ensayos. El efecto es moderado. Incluirlos en una dieta variada siempre es beneficioso.",
                ),
            ),
            Section(
                "El calcio",
                listOf(
                    "Un ensayo clínico demostró que un aporte adecuado de calcio reduce los síntomas premenstruales. Lácteos, aguas ricas en calcio, almendras, verduras verdes y tofu son buenas fuentes.",
                ),
            ),
            Section(
                "Lo que la ciencia aún no avala",
                listOf(
                    "Ningún alimento elimina por completo el dolor menstrual, y las dietas que cambian todo el menú según cada fase del ciclo no descansan en estudios sólidos. Una alimentación equilibrada, suficiente agua y horarios regulares son la mejor base.",
                ),
            ),
        ),
        sources = listOf(Sources.EFSA_IRON_2015, Sources.OMEGA3_2022, Sources.CALCIUM_PMS_1998, Sources.GINGER_2015),
    )

    val pms = Article(
        id = "spm",
        title = "Síndrome premenstrual",
        summary = "Los síntomas de los días previos a la regla, qué puede aliviar y cuándo pedir ayuda.",
        sections = listOf(
            Section(
                "De qué se trata",
                listOf(
                    "En los días previos a la regla, muchas personas notan tensión mamaria, hinchazón, cansancio, irritabilidad o bajo estado de ánimo. Suelen desaparecer en los primeros días del sangrado.",
                    "Anotarlos en Lunaison permite comprobar si se repiten en cada ciclo. La pantalla de inicio te muestra lo que suele coincidir con tu fase actual.",
                ),
            ),
            Section(
                "Qué puede ayudar",
                listOf(
                    "Actividad física regular, descanso suficiente y comidas regulares ayudan con frecuencia. Un aporte adecuado de calcio redujo los síntomas en ensayos clínicos.",
                    "Saber que el período se acerca también tranquiliza: programar menos exigencias esos días o avisar al entorno evita tensiones.",
                ),
            ),
            Section(
                "Cuándo pedir ayuda",
                listOf(
                    "Si tu estado de ánimo se desploma, o si la ansiedad o la irritabilidad dañan tus relaciones o tu trabajo, consulta a un profesional. Una forma grave, el trastorno disfórico premenstrual, tiene tratamiento. Y si surgen pensamientos oscuros, no esperes: llama al 024 en España, al 3114 en Francia, o al 112 en toda Europa.",
                ),
            ),
        ),
        sources = listOf(Sources.CALCIUM_PMS_1998, Sources.COCHRANE_EXERCISE_2019),
    )

    val consult = Article(
        id = "consulter",
        title = "Cuándo consultar",
        summary = "Puntos de referencia para solicitar consejo médico y señales de alarma urgentes.",
        sections = listOf(
            Section(
                "Pedir cita médica",
                listOf(
                    "Con tu médico de cabecera, matrona o ginecólogo si observas:",
                    "dolores que te impiden realizar tus actividades habituales o que no ceden con antiinflamatorios;",
                    "reglas que duran más de 8 días, tan abundantes que obligan a cambiar de compresa o tampón cada 1 o 2 horas, o con coágulos de más de 2,5 cm;",
                    "ciclos con frecuencia más cortos de 24 días o más largos de 38 días en adultas (21 y 45 días en adolescentes);",
                    "más de tres meses sin regla, descartando embarazo;",
                    "sangrados entre reglas o tras las relaciones sexuales;",
                    "fatiga inusual o falta de aire junto con menstruaciones abundantes, posible signo de falta de hierro.",
                ),
            ),
            Section(
                "La endometriosis",
                listOf(
                    "La endometriosis afecta a cerca de una de cada diez personas menstruantes. Según los estudios, el diagnóstico tarda entre 7 y 12 años, con frecuencia por normalizar el dolor. Reglas muy dolorosas, dolor en las relaciones o al evacuar durante la menstruación: son motivos válidos para insistir en una valoración.",
                ),
            ),
            Section(
                "Llamar a urgencias (112)",
                listOf(
                    "Dolor brusco y muy intenso en el bajo vientre, especialmente ante la posibilidad de un embarazo.",
                    "Hemorragia muy abundante con mareos, palidez o desmayo.",
                    "Fiebre junto a dolor abdominal bajo.",
                    "Fiebre repentina, vómitos, diarrea, erupción cutánea o mareo mientras usas tampón o copa: retíralo y llama de inmediato al 112, puede ser un choque tóxico.",
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
        title = "Protecciones y choque tóxico",
        summary = "Utilizar tampones y copas correctamente para prevenir un riesgo infrecuente pero grave.",
        sections = listOf(
            Section(
                "El choque tóxico menstrual",
                listOf(
                    "Es una infección infrecuente causada por una toxina del estafilococo dorado. Está vinculada al uso prolongado de productos internos como tampones o copas.",
                ),
            ),
            Section(
                "Buenas prácticas",
                listOf(
                    "Lávate las manos antes y después de manipular tu protección.",
                    "Respeta el tiempo máximo de uso indicado en el envase y no lo superes por la noche: opta por compresas o bragas menstruales para dormir.",
                    "Usa la menor absorción necesaria para tu flujo y emplea tampones solo durante los días de sangrado.",
                ),
            ),
            Section(
                "Signos de alarma",
                listOf(
                    "Fiebre brusca, vómitos, diarrea, erupción similar a una quemadura solar o mareo llevando una protección interna: retírala y avisa a los servicios de urgencias.",
                ),
            ),
        ),
        sources = listOf(Sources.ANSES_TSS_2025),
    )

    val predictions = Article(
        id = "previsions",
        title = "Qué dicen las previsiones",
        summary = "Estimaciones útiles para organizarse, nunca un método anticonceptivo.",
        sections = listOf(
            Section(
                "Un intervalo, no un día fijo",
                listOf(
                    "Incluso en ciclos muy regulares, la duración varía unos días de un mes a otro. Lunaison muestra un intervalo probable de fechas que se ajusta a medida que anotas más ciclos.",
                ),
            ),
            Section(
                "No es anticoncepción",
                listOf(
                    "La ovulación no avisa con exactitud: Lunaison la estima a partir de tus reglas anteriores, con un margen de error. Los métodos basados en calendarios fallan con frecuencia: hasta una de cada cuatro personas se queda embarazada en un año de uso habitual. Para evitar un embarazo, consulta a un profesional sanitario.",
                ),
            ),
            Section(
                "Un retraso",
                listOf(
                    "Un retraso puede deberse a múltiples factores: estrés, viajes, enfermedad, cambio de peso, deporte intenso, lactancia o embarazo. Si un embarazo es posible, un test en orina suele ser fiable desde la fecha esperada de la regla, y más aún unos días después.",
                ),
            ),
        ),
        sources = listOf(Sources.BULL_2019, Sources.TRUSSELL_2011),
    )

    val partners = Article(
        id = "entourage",
        title = "Para la pareja y el entorno",
        summary = "Comprender lo que experimenta una persona durante su ciclo y cómo apoyar sin invadir.",
        sections = listOf(
            Section(
                "Lo que conviene saber",
                listOf(
                    "La menstruación y los días previos pueden acompañarse de dolor, cansancio y cambios de humor totalmente reales. No son exageraciones ni están en la cabeza.",
                    "Cada ciclo es diferente y cada persona también. La única forma de saber qué ayuda es preguntar con empatía.",
                ),
            ),
            Section(
                "Lo que suele ayudar",
                listOf(
                    "Asumir tareas cotidianas sin esperar a que te lo pidan.",
                    "Ofrecer una bolsa caliente, comida reconfortante, tranquilidad o simplemente compañía.",
                    "No juzgar el estado de ánimo: comentarios como \"debes de tener la regla\" hieren más que ayudan.",
                ),
            ),
            Section(
                "Lo que recibes",
                listOf(
                    "Si alguien te comparte información desde Lunaison, es porque ha decidido hacerlo en ese momento puntual. Nada se envía automáticamente. Respeta también el momento en que decida no compartir nada: es su derecho.",
                ),
            ),
        ),
        sources = emptyList(),
    )

    val all: List<Article> = listOf(cycle, pain, pms, food, consult, protection, predictions, partners)
}
