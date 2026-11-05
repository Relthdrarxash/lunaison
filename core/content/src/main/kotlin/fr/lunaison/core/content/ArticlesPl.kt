package fr.lunaison.core.content

/**
 * Artykuły w języku polskim, przetłumaczone z tekstów oryginalnych.
 * Identyczna struktura, identyfikatory i źródła kliniczne (w tym wytyczne PTGiP).
 */
internal object ArticlesPl {

    val cycle = Article(
        id = "cycle",
        title = "Zrozumieć swój cykl",
        summary = "Co dzieje się w trakcie cyklu i dlaczego Twój cykl nie musi wyglądać dokładnie tak jak w podręcznikach.",
        sections = listOf(
            Section(
                "Czym jest cykl?",
                listOf(
                    "Cykl trwa od pierwszego dnia miesiączki do pierwszego dnia kolejnego krwawienia. Pierwszy dzień to dzień wyraźnego krwawienia: samo plamienie się nie liczy.",
                    "W trakcie cyklu stężenie dwóch hormonów wydzielanych przez jajniki, estrogenów i progesteronu, rośnie i spada. Przygotowują one błonę śluzową macicy na ewentualną ciążę. Jeśli do niej nie dojdzie, błona się złuszcza: to właśnie miesiączka.",
                ),
            ),
            Section(
                "Cztery fazy",
                listOf(
                    "Miesiączka: błona śluzowa macicy ulega złuszczeniu. Poziom hormonów jest najniższy.",
                    "Faza folikularna: pęcherzyk dojrzewa w jajniku, a stężenie estrogenów rośnie.",
                    "Owulacja: jajnik uwalnia komórkę jajową. Nie następuje w ściśle określonym dniu.",
                    "Faza lutealna: progesteron dominuje aż do kolejnej miesiączki. Objawy przedmiesiączkowe pojawiają się najczęściej pod koniec tej fazy.",
                ),
            ),
            Section(
                "Cykl 28-dniowy to średnia, a nie reguła",
                listOf(
                    "Badanie ponad 600 000 cykli wykazało średnią długość 29,3 dnia i zaledwie 13% cykli trwających dokładnie 28 dni. Owulacja nie u każdego przypada na 14. dzień: to faza przedowulacyjna różni się najbardziej u poszczególnych osób.",
                    "U dorosłych cykl od 24 do 38 dni oraz krwawienie trwające do 8 dni mieszczą się w normie. Różnica kilku dni między kolejnymi cyklami jest naturalna.",
                    "W okresie dojrzewania, przez pierwsze lata, cykle bywają nieregularne: większość trwa od 21 do 45 dni.",
                ),
            ),
            Section(
                "Jak Lunaison wylicza przewidywania",
                listOf(
                    "Lunaison analizuje Twoje miesiączki i oblicza typową długość ostatnich cykli. Im więcej cykli zapiszesz, tym dokładniejsze są szacunki. Owulację wylicza się na około 13 dni przed kolejnym krwawieniem, ponieważ ta część cyklu jest najbardziej stabilna.",
                    "Te szacunki ułatwiają organizację. Nie służą do zapobiegania ciąży.",
                ),
            ),
        ),
        sources = listOf(Sources.BULL_2019, Sources.FIGO_2018, Sources.ACOG_ADOLESCENTS_2015),
    )

    val pain = Article(
        id = "douleurs",
        title = "Łagodzenie bólu miesiączkowego",
        summary = "Co ma potwierdzoną skuteczność, co może pomóc i kiedy ból powinien skłonić do wizyty u lekarza.",
        sections = listOf(
            Section(
                "Skąd bierze się ból",
                listOf(
                    "Podczas miesiączki macica wytwarza prostaglandyny. Substancje te powodują jej skurcze, aby usunąć złuszczającą się błonę, a skurcze te wywołują ból. Dlatego leki hamujące wydzielanie prostaglandyn przynoszą ulgę.",
                ),
            ),
            Section(
                "Leki przeciwzapalne",
                listOf(
                    "Niesteroidowe leki przeciwzapalne (NLPZ), takie jak ibuprofen, łagodzą ból skuteczniej niż paracetamol. W badaniach klinicznych wyraźną ulgę odczuła połowa badanych, w porównaniu z niespełna jedną na pięć osób przyjmujących placebo.",
                    "Działają najlepiej przyjęte na samym początku bólu lub krwawienia. Stosuj się do ulotki. Skonsultuj się z lekarzem lub farmaceutą w przypadku chorób żołądka, nerek, astmy czy podejrzenia ciąży.",
                ),
            ),
            Section(
                "Ciepło",
                listOf(
                    "Termofor lub kompres rozgrzewający na podbrzuszu rozluźnia mięśnie. W badaniach efekt był porównywalny ze środkami przeciwbólowymi. Uważaj na oparzenia: nie przykładaj silnego ciepła bezpośrednio na skórę.",
                ),
            ),
            Section(
                "Aktywność fizyczna",
                listOf(
                    "Regularny umiarkowany ruch, około 3 razy w tygodniu po 45-60 minut, może zmniejszyć nasilenie bólu. Spacer, pływanie, joga: wybierz to, co sprawia Ci przyjemność.",
                ),
            ),
            Section(
                "Imbir",
                listOf(
                    "Niewielkie badania sugerują, że sproszkowany imbir przyjmowany w pierwszych dniach krwawienia może zmniejszać ból. Dowody są skromne, ale warto spróbować jako uzupełnienie.",
                ),
            ),
            Section(
                "Kiedy zgłosić się do lekarza",
                listOf(
                    "Ból miesiączkowy jest częsty, ale ból, który uniemożliwia normalną naukę lub pracę, nie ustępuje po lekach przeciwzapalnych lub nasila się z wiekiem, nie jest czymś naturalnym. Może wskazywać na endometriozę lub inną przyczynę wymagającą leczenia.",
                    "Porozmawiaj o tym z ginekologiem lub położną. Zapisuj swoje dolegliwości w Lunaison: dokładny dziennik jest bezcenną pomocą podczas wizyty.",
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
        title = "Dieta a cykl",
        summary = "Przede wszystkim żelazo. Poza tym rzetelne wskazówki i brak cudownych diet.",
        sections = listOf(
            Section(
                "Żelazo",
                listOf(
                    "Wraz z krwią traci się żelazo. Osoby miesiączkujące potrzebują go więcej: Europejski Urząd ds. Bezpieczeństwa Żywności (EFSA) zaleca 16 mg dziennie jako wartość referencyjną. Obfite miesiączki mogą prowadzić do niedoboru żelaza, zmęczenia i osłabienia.",
                    "Najlepiej przyswajalne żelazo znajduje się w mięsie i rybach. Rośliny strączkowe (soczewica, ciecierzyca), tofu, pełnoziarniste zboża i zielone warzywa również je zawierają. Łącz je z witaminą C (cytrusy, papryka, kiwi) i unikaj picia kawy oraz herbaty w trakcie tych posiłków.",
                    "Nie przyjmuj preparatów żelaza bez zalecenia lekarza: proste badanie krwi wykaże, czy suplementacja jest konieczna.",
                ),
            ),
            Section(
                "Kwasy omega-3",
                listOf(
                    "Kwasy omega-3 obecne w tłustych rybach morskich (sardynki, makrela, łosoś), orzechach włoskich i oleju rzepakowym lub lnianym łagodziły ból w kilku próbach klinicznych. Warto włączyć je do codziennej diety.",
                ),
            ),
            Section(
                "Wapń",
                listOf(
                    "Badanie wykazało, że odpowiednia podaż wapnia zmniejszała objawy zespołu napięcia przedmiesiączkowego. Nabiał, woda wysokozmineralizowana, migdały i tofu to świetne źródła.",
                ),
            ),
            Section(
                "Czego nauka nie potwierdza",
                listOf(
                    "Żaden produkt spożywczy nie likwiduje bólu miesiączkowego jak za dotknięciem czarodziejskiej różdżki, a restrykcyjne diety zmieniające jadłospis w każdej fazie cyklu nie mają potwierdzenia w rzetelnych badaniach. Urozmaicona dieta, nawodnienie i regularne posiłki to najlepsza podstawa.",
                ),
            ),
        ),
        sources = listOf(Sources.EFSA_IRON_2015, Sources.OMEGA3_2022, Sources.CALCIUM_PMS_1998, Sources.GINGER_2015),
    )

    val pms = Article(
        id = "spm",
        title = "Zespół napięcia przedmiesiączkowego (PMS)",
        summary = "Objawy dni poprzedzających miesiączkę, co może pomóc i kiedy warto poprosić o wsparcie.",
        sections = listOf(
            Section(
                "O czym mowa",
                listOf(
                    "W dniach przed krwawieniem wiele osób doświadcza tkliwości piersi, wzdęć, zmęczenia, drażliwości lub obniżonego nastroju. Zwykle mijają one w pierwszych dniach miesiączki.",
                    "Zapisywanie ich w Lunaison pozwala sprawdzić, czy powtarzają się w każdym cyklu. Ekran główny podpowiada, jakie objawy pojawiały się wcześniej w Twojej obecnej fazie.",
                ),
            ),
            Section(
                "Co może pomóc",
                listOf(
                    "Regularny ruch, odpowiednia ilość snu i stałe pory posiłków przynoszą ulgę. Odpowiednia dawka wapnia zmniejszała nasilenie objawów w badaniach klinicznych.",
                    "Świadomość, że ten czas nadchodzi, pozwala zaplanować mniej obciążające dni i uprzedzić bliskich, co pozwala uniknąć wielu napięć.",
                ),
            ),
            Section(
                "Kiedy szukać pomocy",
                listOf(
                    "Jeśli Twój nastrój drastycznie spada, a lęk lub złość niszczą relacje lub pracę, skonsultuj się z lekarzem. Ciężką postać, przedmiesiączkowe zaburzenie dysforyczne (PMDD), można skutecznie leczyć. W razie kryzysu psychicznego zadzwoń pod bezpłatny numer 116 123 (Kryzysowy Telefon Zaufania w Polsce) lub 112.",
                ),
            ),
        ),
        sources = listOf(Sources.CALCIUM_PMS_1998, Sources.COCHRANE_EXERCISE_2019),
    )

    val consult = Article(
        id = "consulter",
        title = "Kiedy udać się do lekarza",
        summary = "Sygnały wymagające konsultacji medycznej oraz sytuacje nagłe.",
        sections = listOf(
            Section(
                "Umówienie wizyty",
                listOf(
                    "Skonsultuj się z ginekologiem lub lekarzem rodzinnym, jeśli zauważasz:",
                    "ból, który uniemożliwia codzienne aktywności lub nie ustępuje po lekach przeciwzapalnych;",
                    "krwawienia trwające dłużej niż 8 dni lub tak obfite, że wymagają wymiany podpaski czy tamponu co 1-2 godziny, albo ze skrzepami o średnicy powyżej 2,5 cm;",
                    "cykle u dorosłych regularnie krótsze niż 24 dni lub dłuższe niż 38 dni (21 i 45 dni u nastolatek);",
                    "brak miesiączki przez ponad trzy miesiące (po wykluczeniu ciąży);",
                    "krwawienia między miesiączkami lub po stosunku;",
                    "nietypowe zmęczenie i duszności przy obfitych krwawieniach, mogące świadczyć o anemii z niedoboru żelaza.",
                ),
            ),
            Section(
                "Endometrioza",
                listOf(
                    "Endometrioza dotyka około jednej na dziesięć osób miesiączkujących. Według wytycznych PTGiP i badań, diagnoza trwa średnio od 7 do 12 lat, często przez bagatelizowanie dolegliwości bólowych. Bardzo bolesne miesiączki, ból podczas współżycia czy oddawania moczu w czasie krwawienia to powody, by domagać się rzetelnej diagnostyki.",
                ),
            ),
            Section(
                "Kiedy dzwonić pod numer alarmowy 112",
                listOf(
                    "Nagły, bardzo silny ból podbrzusza, zwłaszcza gdy istnieje możliwość ciąży.",
                    "Bardzo obfite krwawienie z omdleniem, zawrotami głowy lub silną bladością.",
                    "Gorączka połączona z silnym bólem podbrzusza.",
                    "Nagła gorączka, wymioty, biegunka, wysypka przypominająca oparzenie słoneczne lub zasłabnięcie podczas noszenia tamponu lub kubeczka: natychmiast go wyjmij i wezwij pogotowie (podejrzenie wstrząsu toksycznego).",
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
        title = "Środki higieniczne a TSS",
        summary = "Bezpieczne korzystanie z tamponów i kubeczków w celu uniknięcia rzadkiego, lecz groźnego powikłania.",
        sections = listOf(
            Section(
                "Zespół wstrząsu toksycznego (TSS)",
                listOf(
                    "To rzadka infekcja wywołana przez toksynę gronkowca złocistego. Jest związana ze zbyt długim pozostawianiem środków higienicznych wewnątrz ciała.",
                ),
            ),
            Section(
                "Zasady bezpieczeństwa",
                listOf(
                    "Myj ręce przed wymianą i po wymianie podpaski, tamponu lub kubeczka.",
                    "Przestrzegaj maksymalnego czasu noszenia podanego na ulotce i nie przekraczaj go w nocy: do snu lepiej wybrać podpaskę lub majtki menstruacyjne.",
                    "Stosuj najmniejszy stopień chłonności odpowiadający Twojemu krwawieniu i używaj tamponów wyłącznie w trakcie miesiączki.",
                ),
            ),
            Section(
                "Objawy ostrzegawcze",
                listOf(
                    "Nagła gorączka, wymioty, biegunka, wysypka jak po słońcu lub zawroty głowy podczas używania tamponu lub kubeczka: wyjmij produkt i natychmiast wezwij pomoc medyczną.",
                ),
            ),
        ),
        sources = listOf(Sources.ANSES_TSS_2025),
    )

    val predictions = Article(
        id = "previsions",
        title = "Co mówią przewidywania",
        summary = "Wskazówki przydatne w planowaniu, ale w żadnym wypadku nie metoda antykoncepcji.",
        sections = listOf(
            Section(
                "Przedział dat, a nie jeden dzień",
                listOf(
                    "Nawet przy bardzo regularnym cyklu jego długość waha się o kilka dni. Lunaison wskazuje prawdopodobny zakres dat, który staje się dokładniejszy wraz z liczbą zanotowanych cykli.",
                ),
            ),
            Section(
                "To nie jest antykoncepcja",
                listOf(
                    "Owulacji nie da się przewidzieć bezbłędnie: Lunaison szacuje ją na podstawie wcześniejszych miesięcy, z marginesem błędu. Metody kalendarzowe zawodzą: około jedna na cztery osoby zachodzi w ciążę w ciągu roku ich typowego stosowania. W celu zapobiegania ciąży skonsultuj się ze specjalistą.",
                ),
            ),
            Section(
                "Spóźniająca się miesiączka",
                listOf(
                    "Opóźnienie może wynikać ze stresu, podróży, infekcji, wahań wagi, intensywnego sportu, karmienia piersią lub ciąży. Jeśli ciąża jest możliwa, test z moczu jest zwykle wiarygodny od dnia spodziewanej miesiączki.",
                ),
            ),
        ),
        sources = listOf(Sources.BULL_2019, Sources.TRUSSELL_2011),
    )

    val partners = Article(
        id = "entourage",
        title = "Dla bliskich i partnerów",
        summary = "Zrozumienie, przez co przechodzi organizm w trakcie cyklu, i jak pomagać bez narzucania się.",
        sections = listOf(
            Section(
                "Warto wiedzieć",
                listOf(
                    "Miesiączka i dni ją poprzedzające mogą wiązać się z realnym bólem, zmęczeniem i zmianami nastroju. Nie są to wymysły ani przesada.",
                    "Każdy cykl i każda osoba są inne. Najlepszym sposobem, by dowiedzieć się, co pomaga, jest spokojne i życzliwe pytanie.",
                ),
            ),
            Section(
                "Co często pomaga",
                listOf(
                    "Przejęcie codziennych obowiązków domowych bez czekania na prośbę.",
                    "Zaproponowanie ciepłego kompresu, posiłku, chwili spokoju lub po prostu swojej obecności.",
                    "Unikanie kąśliwych uwag o nastroju: komentarze w stylu « Chyba masz okres » ranią, zamiast pomagać.",
                ),
            ),
            Section(
                "Co otrzymujesz",
                listOf(
                    "Jeśli otrzymujesz wiadomość z Lunaison, to dlatego, że dana osoba sama zdecydowała się ją wysłać w tym momencie. Nic nie jest udostępniane automatycznie. Uszanuj także dni, w których nic nie jest udostępniane: to jej pełne prawo.",
                ),
            ),
        ),
        sources = emptyList(),
    )

    val all: List<Article> = listOf(cycle, pain, pms, food, consult, protection, predictions, partners)
}
