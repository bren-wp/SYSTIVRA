// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text as MaterialText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

// The visual reference boards are English. Never localize route IDs, telemetry,
// data formats, persistent keys or geometry; only user-visible strings.
internal const val UI_LANGUAGE_PREFERENCE = "ui_language"
internal const val UI_LANGUAGE_ENGLISH = "en"
internal const val UI_LANGUAGE_CROATIAN = "hr"
internal fun normalizedUiLanguage(code: String?): String =
    if (code == UI_LANGUAGE_CROATIAN) UI_LANGUAGE_CROATIAN else UI_LANGUAGE_ENGLISH

internal val LocalUiLanguage = compositionLocalOf { UI_LANGUAGE_ENGLISH }
internal val LocalSetUiLanguage = compositionLocalOf<(String) -> Unit> { {} }

/**
 * Source-English to Croatian UI translation catalog. English is an identity
 * translation so the approved 390x844 SVG baseline remains geometrically
 * untouched when users never change the language setting.
 *
 * A missing translation deliberately falls back to English, never guesses
 * translations for Android-supplied device names/values or export content.
 */
private val croatianUi = """
System Health|Stanje sustava
Your system at a glance|Pregled sustava
Real device data overview|Stvarni podaci uređaja
SYSTIVRA|SYSTIVRA
System intelligence|Analiza sustava
Battery status|Stanje baterije
Details / availability|Detalji / dostupnost
Storage and memory|Pohrana i memorija
Hardware diagnostics|Dijagnostika hardvera
Dashboard|Početna
Monitor|Nadzor
Tests|Testovi
More|Više
Device Detection|Otkrivanje uređaja
Hardware capabilities|Mogućnosti hardvera
Supported Android APIs|Podržani Android API-ji
Device model|Model uređaja
Memory and storage|Memorija i pohrana
Available sensors|Dostupni senzori
Privacy Center|Centar privatnosti
Privacy and control|Privatnost i kontrola
Account-free monitoring|Nadzor bez računa
Local data only|Samo lokalni podaci
No analytics|Bez analitike
Optional Android access|Neobvezan pristup Androidu
Your device at a glance|Pregled vašeg uređaja
Live information|Podaci uživo
Memory|Memorija
Storage|Pohrana
Battery|Baterija
Thermal status|Toplinsko stanje
Hardware Explorer|Pregled hardvera
Explore components|Istražite komponente
Hardware information|Podaci o hardveru
CPU and SoC|CPU i SoC
GPU|GPU
Sensors|Senzori
CPU Details|Detalji CPU-a
Processor overview|Pregled procesora
System information|Podaci o sustavu
Live data chart|Graf podataka uživo
Illustrative visual · not a measured reading|Ilustracija · nije mjerenje
Architecture|Arhitektura
Core count|Broj jezgri
CPU load|Opterećenje CPU-a
Data availability|Dostupnost podataka
CPU Core Inspector|Pregled jezgri CPU-a
Core information|Podaci o jezgrama
Values vary by device|Vrijednosti ovise o uređaju
Core 0|Jezgra 0
Core 1|Jezgra 1
Core 2|Jezgra 2
Core 3|Jezgra 3
GPU Details|Detalji GPU-a
Graphics processor|Grafički procesor
OpenGL and Vulkan data|Podaci o OpenGL-u i Vulkanu
Renderer|Prikazivač
Graphics API|Grafički API
GPU load|Opterećenje GPU-a
Availability|Dostupnost
Memory Details|Detalji memorije
System memory|Sistemska memorija
Used and available RAM|Zauzeti i slobodni RAM
Total RAM|Ukupni RAM
Available RAM|Slobodni RAM
Memory pressure|Opterećenje memorije
History|Povijest
Storage Details|Detalji pohrane
Internal storage|Unutarnja pohrana
Space and volumes|Prostor i volumeni
Total storage|Ukupna pohrana
Available space|Slobodni prostor
Used space|Zauzeti prostor
Volume details|Detalji volumena
Battery Details|Detalji baterije
Power information|Podaci o napajanju
Charge and temperature|Napunjenost i temperatura
Charge level|Razina napunjenosti
Charging state|Stanje punjenja
Battery temperature|Temperatura baterije
Current|Struja
Charging Analysis|Analiza punjenja
Power session|Sesija napajanja
Battery trends|Trendovi baterije
Charge timeline|Tijek napunjenosti
Current if available|Struja ako je dostupna
Temperature|Temperatura
Session duration|Trajanje sesije
Thermal Details|Toplinski detalji
Android platform reporting|Podaci sustava Android
Headroom if available|Toplinska rezerva
Safety guidance|Sigurnosne smjernice
Display Details|Detalji zaslona
Screen information|Podaci o zaslonu
Resolution and density|Rezolucija i gustoća
Resolution|Rezolucija
Pixel density|Gustoća piksela
Refresh rate|Frekvencija osvježavanja
Color modes|Načini boja
Device & Android|Uređaj i Android
System profile|Profil sustava
Platform identifiers|Identifikatori platforme
Manufacturer|Proizvođač
Model|Model
Android version|Verzija Androida
Security patch|Sigurnosna zakrpa
Network Details|Detalji mreže
Connection overview|Pregled veze
Available interface details|Podaci o sučelju
Connection type|Vrsta veze
Network state|Stanje mreže
Wi-Fi information|Podaci o Wi-Fi mreži
Sensor Explorer|Pregled senzora
Device-specific inventory|Senzori ovog uređaja
Accelerometer|Akcelerometar
Gyroscope|Žiroskop
Light sensor|Senzor svjetla
Proximity|Blizina
Sensor Live View|Senzori uživo
Sensor timeline|Tijek senzora
Real-time values|Vrijednosti u stvarnom vremenu
X axis|Os X
Y axis|Os Y
Z axis|Os Z
Sensor metadata|Podaci o senzoru
App Usage|Uporaba aplikacija
Usage statistics|Statistika uporabe
Optional Android permission|Neobvezna Android dozvola
Permission required|Potrebna dozvola
Local statistics|Lokalna statistika
Time range|Vremenski raspon
Privacy|Privatnost
Live Monitor|Nadzor uživo
System readings|Očitanja sustava
Real-time activity|Aktivnost uživo
Metric Fullscreen|Mjerenje na cijelom zaslonu
Detailed timeline|Detaljan vremenski prikaz
Units and data source|Jedinice i izvor podataka
Current value|Trenutačna vrijednost
Minimum|Minimum
Maximum|Maksimum
Data origin|Podrijetlo podataka
Recording Session|Snimanje sesije
Session capture|Snimanje sesije
User-controlled recording|Snimanje pod nadzorom korisnika
Start|Pokreni
Pause|Pauza
Duration|Trajanje
Storage policy|Pravila pohrane
Session Analysis|Analiza sesije
Session overview|Pregled sesije
Statistics and metadata|Statistika i metapodaci
Average|Prosjek
Export|Izvoz
Alerts|Upozorenja
Device notifications|Obavijesti uređaja
Configurable thresholds|Podesivi pragovi
Battery level|Razina baterije
Thermal warnings|Toplinska upozorenja
Storage capacity|Kapacitet pohrane
Notification access|Pristup obavijestima
Diagnostics Center|Centar dijagnostike
Hardware checks|Provjere hardvera
Opt-in device testing|Testiranje na zahtjev
Display|Zaslon
Touch|Dodir
Speakers|Zvučnici
Component Test|Test komponenti
Interactive testing|Interaktivno testiranje
Explicit user confirmation|Potvrda korisnika
Run test|Pokreni test
Check result|Provjeri rezultat
Skip test|Preskoči test
Permission state|Stanje dozvole
Diagnostic Results|Rezultati dijagnostike
Test summary|Sažetak testova
Only completed tests count|Računaju se samo dovršeni testovi
Passed|Uspješno
Failed|Neuspješno
Skipped|Preskočeno
Unavailable|Nedostupno
Benchmark Center|Centar performansi
Performance tests|Testovi performansi
Bounded safe workloads|Ograničeno sigurno opterećenje
CPU test|CPU test
Memory test|Test memorije
Storage test|Test pohrane
Test conditions|Uvjeti testa
Benchmark Running|Test u tijeku
Test in progress|Testiranje u tijeku
Stop at any time|Moguće zaustaviti u svakom trenutku
Elapsed time|Proteklo vrijeme
Stop test|Zaustavi test
Benchmark Results|Rezultati performansi
Performance summary|Sažetak performansi
Methodology and context|Metodologija i kontekst
Test version|Verzija testa
Thermal state|Toplinsko stanje
Past sessions|Prethodne sesije
Overlay Configuration|Postavke prikaza
Floating monitor|Plutajući nadzor
Requires Android overlay access|Potreban pristup preklapanju
Metric selection|Odabir mjerenja
Opacity|Prozirnost
Size|Veličina
Position|Položaj
Recent sessions|Nedavne sesije
Local device records|Lokalna povijest
Today|Danas
This week|Ovaj tjedan
All sessions|Sve sesije
Filters|Filtri
Reports & Export|Izvještaji i izvoz
Export your data|Izvezite podatke
Choose fields and format|Odaberite polja i format
CSV|CSV
JSON|JSON
PDF|PDF
Android Sharesheet|Dijeljenje putem Androida
Permission Center|Centar dozvola
Access overview|Pregled pristupa
Optional features|Neobvezne funkcije
Notifications|Obavijesti
Overlay|Prikaz preko drugih aplikacija
Camera|Kamera
Microphone|Mikrofon
Settings|Postavke
Preferences|Postavke
Customize your experience|Prilagodite aplikaciju
Appearance|Izgled
Update interval|Interval osvježavanja
Units|Jedinice
About SYSTIVRA|O aplikaciji SYSTIVRA
Version and licenses|Verzija i licence
Open development|Otvoreni razvoj
Source code|Izvorni kod
MIT license|MIT licenca
Device support|Podrška za uređaje
Language|Jezik
App language|Jezik aplikacije
English|English
Hrvatski|Hrvatski
Choose interface language|Odaberite jezik sučelja
Changes are applied immediately and saved on this device.|Promjene se primjenjuju odmah i spremaju na uređaj.
English is the default language.|Engleski je zadani jezik.
Device shortcuts|Prečaci uređaja
All tools|Svi alati
Hardware explorer|Pregled hardvera
Close|Zatvori
Cancel|Odustani
Save|Spremi
Delete|Izbriši
Delete all|Izbriši sve
Search tools|Pretraži alate
Search readings|Pretraži očitanja
Show all tools|Prikaži sve alate
Find a function|Pronađite funkciju
Screen catalog|Katalog zaslona
Screen Catalog|Katalog zaslona
All|Sve
Hardware|Hardver
Records|Zapisi
System|Sustav
Open Recording|Otvori snimanje
No recorded samples|Nema snimljenih uzoraka
No tools match. Change category or search.|Nema rezultata. Promijenite kategoriju ili pretragu.
More options|Dodatne opcije
Details|Detalji
Refresh status|Osvježi stanje
Start recording|Pokreni snimanje
Stop recording|Zaustavi snimanje
Saved recordings|Spremljene snimke
Recording controls|Kontrole snimanja
Recording options|Opcije snimanja
Recording Library|Biblioteka snimaka
Session Recorder|Snimanje sesije
Saved samples|Spremljeni uzorci
Show details|Prikaži detalje
No saved session available|Nema spremljene sesije
Recordings|Snimke
View results|Pregledaj rezultate
Back to Performance Lab|Natrag u centar performansi
Performance Lab|Centar performansi
Test safety|Sigurnost testa
CPU benchmark safety|Sigurnost CPU testa
Start CPU test|Pokreni CPU test
CPU Benchmark|CPU test
No completed test|Nema dovršenog testa
Measured device samples|Izmjereni uzorci uređaja
Waiting for measured samples|Čekanje stvarnih mjerenja
No illustrative or fabricated readings|Bez ilustriranih ili izmišljenih očitanja
Recorded device samples · measured percentages|Snimljeni podaci · izmjereni postoci
Monitor tools|Alati nadzora
Additional hardware|Dodatni hardver
No account, onboarding or unsupported theme toggle is required.|Nije potreban račun ni uvodno postavljanje.
SYSTIVRA uses its midnight dark theme.|SYSTIVRA koristi tamnu temu.
Temperature display|Prikaz temperature
Celsius · °C|Celzij · °C
Fahrenheit · °F|Fahrenheit · °F
Applied to live device sampling and saved for future launches.|Primjenjuje se na očitanja uživo i pamti za iduća pokretanja.
Visible device readings|Vidljiva očitanja uređaja
Open source|Otvoreni kod
Report Export|Izvoz izvještaja
Session Report|Izvještaj sesije
Choose one format. Only the selected export action is shown.|Odaberite format. Prikazana je samo odabrana radnja.
Save JSON report|Spremi JSON izvještaj
Save CSV report|Spremi CSV izvještaj
Create PDF document|Izradi PDF dokument
Open Android Sharesheet|Otvori Android dijeljenje
Export saved readings|Izvezi spremljena očitanja
No report exported yet.|Izvještaj još nije izvezen.
No active recording. A new Start creates a new session.|Nema aktivnog snimanja. Pokretanje stvara novu sesiju.
Paused / inactive|Pauzirano / neaktivno
Recording while app is visible|Snimanje dok je aplikacija otvorena
Loading saved history|Učitavanje povijesti
Loading saved history…|Učitavanje povijesti…
Delete all saved recordings|Izbriši sve spremljene snimke
Delete all recordings?|Izbrisati sve snimke?
No session|Nema sesije
Captured samples|Snimljeni uzorci
Battery range|Raspon baterije
Temperature range|Raspon temperature
Average battery charge|Prosječna napunjenost baterije
Thermal states|Toplinska stanja
Insights|Uvidi
Session Insights|Uvidi u sesiju
Statistics from genuine saved readings, never estimated health scores.|Statistika iz stvarnih spremljenih očitanja.
No recorded data|Nema snimljenih podataka
Battery charge trend|Trend napunjenosti baterije
Net battery change|Neto promjena baterije
Reported thermal states|Prijavljena toplinska stanja
No threshold warnings at this time|Trenutačno nema upozorenja
Enable on-screen warnings|Omogući upozorenja na zaslonu
Current status|Trenutačno stanje
Android thermal state|Toplinsko stanje Androida
Local in-app warnings only. No background monitoring or notifications.|Samo lokalna upozorenja, bez pozadinskog nadzora.
Performance HUD|Prikaz performansi
Interactive in-app preview. No permission to draw over other apps.|Interaktivni prikaz u aplikaciji, bez pristupa drugim aplikacijama.
Metrics|Mjerenja
Top left|Gore lijevo
Top right|Gore desno
Bottom left|Dolje lijevo
Bottom right|Dolje desno
Select at least one metric.|Odaberite barem jedno mjerenje.
Speaker audio test|Test zvučnika
Display & touch test|Test zaslona i dodira
Touches registered|Zabilježeni dodiri
Next color|Sljedeća boja
Pass|Uspješno
Fail|Neuspješno
Skip|Preskoči
Result|Rezultat
Report PDF|PDF izvještaj
Live Sensor Inspector|Pregled senzora uživo
Selected sensor|Odabrani senzor
Live values|Vrijednosti uživo
No supported sensors|Nema podržanih senzora
Waiting for a sensor event…|Čekanje podatka senzora…
Network status|Stanje mreže
Transport|Vrsta veze
Validated Internet|Potvrđen pristup internetu
Metered network|Veza s naplatom podataka
Reported downstream bandwidth|Procijenjena brzina preuzimanja
App Usage Access|Pristup uporabi aplikacija
Not granted|Nije odobreno
Allowed|Dopušteno
Manage Usage Access|Upravljaj pristupom uporabi
Review other permissions|Pregled drugih dozvola
Permissions and privacy|Dozvole i privatnost
No active network|Nema aktivne mreže
Wi-Fi|Wi-Fi
Cellular|Mobilna mreža
Ethernet|Ethernet
VPN|VPN
Unknown|Nepoznato
No data|Nema podataka
Ready|Spremno
Completed|Dovršeno
Canceled by user|Korisnik je odustao
Loading selected recording…|Učitavanje odabrane snimke…
A local CPU workload with real measurements, no invented score.|Lokalni CPU test sa stvarnim mjerenjima, bez izmišljenih rezultata.
Actual Android measurements. No artificial health score.|Stvarna Android mjerenja. Bez umjetne ocjene stanja.
Additional actions|Dodatne radnje
Additional tests|Dodatni testovi
All insights|Svi uvidi
Analyze|Analiziraj
Analyze all|Analiziraj sve
Android exposes a processor count, not guaranteed per-core load or clocks.|Android prikazuje broj procesora, ali ne jamči podatke o opterećenju i taktu svake jezgre.
Battery  |Baterija  
Battery Charge · Live|Napunjenost baterije · uživo
Browser unavailable|Preglednik nije dostupan
Copy measurement|Kopiraj mjerenje
Delete session|Izbriši sesiju
Delete this recording?|Izbrisati ovu snimku?
Export CSV, JSON or PDF|Izvezi CSV, JSON ili PDF
Free RAM  |Slobodni RAM  
Inspect sources and limitations|Pregled izvora i ograničenja
Install a browser or open the SYSTIVRA project on GitHub.|Instalirajte preglednik ili otvorite projekt SYSTIVRA na GitHubu.
Local CPU measurements are not comparable to commercial benchmark rankings.|Lokalna CPU mjerenja nisu usporediva s komercijalnim testovima.
Logical processors|Logički procesori
Maximum two seconds; Android thermal safety cutoff is active.|Najviše dvije sekunde; aktivna je toplinska zaštita Androida.
Minimum: |Minimum: 
More readings are needed to draw a chart.|Za prikaz grafa potrebno je više očitanja.
No battery metrics available from Android.|Android nema dostupnih podataka o bateriji.
No measurements or test results are displayed.|Nema prikazanih mjerenja ni rezultata.
No saved recordings yet. Start recording from Monitor.|Još nema snimaka. Pokrenite snimanje u Nadzoru.
Offline-first. No account required. No invented device readings.|Radi izvan mreže. Bez računa i izmišljenih očitanja.
One worker, at most two seconds. Stops at severe Android thermal status. Avoid running when the device is already hot.|Jedna radna nit, najviše dvije sekunde. Prekida se pri previsokoj temperaturi. Ne pokrećite na vrućem uređaju.
Open full controls|Otvori sve kontrole
Per-core frequency and utilization are unavailable until verified.|Takt i uporaba pojedinih jezgri nisu dostupni bez provjere.
Performance options|Opcije performansi
Play a short tone and confirm whether you hear it. No microphone access.|Reproducirajte kratki ton i potvrdite da ga čujete. Mikrofon nije potreban.
Play test tone|Reproduciraj testni ton
Privacy by design|Privatnost u dizajnu
Real Android battery readings. No fabricated capacity estimate.|Stvarna Android očitanja baterije, bez izmišljenog kapaciteta.
Recording actions|Radnje snimanja
Result: |Rezultat: 
SYSTIVRA by Brendigo|SYSTIVRA by Brendigo
SYSTIVRA · Live Preview|SYSTIVRA · Prikaz uživo
Saved locally. Up to 900 total readings. Deletion is disabled during recording.|Spremljeno lokalno, najviše 900 očitanja. Brisanje je onemogućeno tijekom snimanja.
Session insights|Uvidi u sesiju
Size, position and opacity affect only this local preview.|Veličina, položaj i prozirnost utječu samo na ovaj lokalni prikaz.
Start a new CPU benchmark|Pokreni novi CPU test
System-reported percentage. Graph uses actual session samples.|Postotak koji prijavljuje sustav. Graf koristi stvarne uzorke sesije.
TAP HERE|DODIRNITE OVDJE
Tap for analysis, export or deletion|Dodirnite za analizu, izvoz ili brisanje
Tap the panel and inspect each color. Confirm the result manually.|Dodirnite površinu i provjerite boje. Potvrdite rezultat ručno.
Test vibration|Testiraj vibraciju
This screen is unavailable.|Ovaj zaslon nije dostupan.
User-confirmed result: |Rezultat koji je potvrdio korisnik: 
Vibration test|Test vibracije
View last CPU result|Prikaži zadnji CPU rezultat
View session insights|Prikaži uvide u sesiju
Waiting for device readings…|Čekanje očitanja uređaja…
© 2026 Brendigo · Android system diagnostics|© 2026 Brendigo · Android dijagnostika
All saved recording sessions and their samples will be permanently deleted from this device. This cannot be undone.|Sve snimljene sesije i uzorci trajno će se izbrisati s uređaja. Radnja se ne može poništiti.
App usage — last 24 hours|Uporaba aplikacija — zadnja 24 sata
Applies to battery and thermal readings in device details. |Primjenjuje se na bateriju i temperaturu u detaljima uređaja. 
At least two valid battery samples are required.|Potrebna su barem dva valjana uzorka baterije.
Available samples: |Dostupni uzorci: 
CSV Export|CSV izvoz
Camera, microphone and cross-app overlays are not requested.|Pristup kameri, mikrofonu i prikazu preko drugih aplikacija nije zatražen.
Choose export location|Odaberite mjesto izvoza
Continue to Settings|Nastavi na Postavke
Device monitoring remains usable without optional Usage Access.|Nadzor uređaja radi i bez neobveznog pristupa uporabi aplikacija.
Export saved session|Izvezi spremljenu sesiju
Export timestamps, battery, memory, thermal and measured CPU load as structured JSON.|Izvezite vremena, bateriju, memoriju, toplinsko stanje i izmjereno CPU opterećenje kao JSON.
Foreground only · Private SQLite · No automatic cloud backup.|Samo u prvom planu · Privatni SQLite · Bez automatske sigurnosne kopije u oblaku.
Generate a multipage PDF from measured UTC timestamps, charge, temperature, RAM and thermal state.|Izradite višestranični PDF iz stvarnih UTC vremena, napunjenosti, temperature, RAM-a i toplinskog stanja.
History filters|Filtri povijesti
Includes UTC timestamp, battery charge, temperature, free RAM, and Android thermal state.|Uključuje UTC vrijeme, napunjenost, temperaturu, slobodni RAM i toplinsko stanje Androida.
JSON Export|JSON izvoz
Library|Biblioteka
Link bandwidth is an Android estimate, not a measured transfer speed.|Propusnost veze procjena je Androida, a ne izmjerena brzina prijenosa.
Live Alerts|Upozorenja uživo
Live readings are not saved without recording.|Očitanja uživo ne spremaju se bez snimanja.
Local recordings only. Select the sort direction.|Samo lokalne snimke. Odaberite redoslijed.
Monitoring uses Android system APIs without dangerous runtime permissions.|Nadzor koristi Android API-je bez opasnih dozvola.
Network state is a normal system permission; INTERNET is not requested.|Stanje mreže koristi osnovnu dozvolu; pristup internetu nije zatražen.
Newest first|Najnovije prvo
No live Android samples available.|Nema dostupnih Android uzoraka uživo.
No readings|Nema očitanja
No saved recordings for this period.|Nema snimaka u ovom razdoblju.
Not now|Ne sada
Oldest first|Najstarije prvo
Only user-confirmed results appear. Unassessed tests are not passes.|Prikazuju se samo rezultati koje je potvrdio korisnik. Neprovjereni testovi nisu uspješni.
Open Usage Access settings|Otvori postavke pristupa uporabi
Optional Usage Access. Statistics stay on this device.|Neobvezan pristup uporabi. Statistika ostaje na ovom uređaju.
Optional app usage access|Neobvezan pristup uporabi aplikacija
PDF Export|PDF izvoz
Package names are shown without requesting extra app discovery access.|Nazivi paketa prikazuju se bez dodatnog pristupa drugim aplikacijama.
Reads connection state only. No internet request or speed test is performed.|Čita se samo stanje veze. Nema internetskih zahtjeva ni testa brzine.
Real SensorManager events; listener stops when leaving this screen.|Stvarni događaji senzora; praćenje se zaustavlja pri izlasku sa zaslona.
Recent live readings|Nedavna očitanja uživo
Refresh usage|Osvježi uporabu
SYSTIVRA can read which apps were in the foreground and for how long. |SYSTIVRA može očitati koje su aplikacije korištene i koliko dugo. 
Sampling pauses and charging changes affect observed trends; this is not a battery health assessment.|Prekidi mjerenja i punjenje utječu na trendove; ovo nije procjena zdravlja baterije.
Saved readings persist privately in SQLite until deleted. Exports are copies written |Spremljena očitanja ostaju privatno u SQLite bazi do brisanja. Izvozi su kopije spremljene 
Sensor inventory is available in Hardware Explorer.|Popis senzora dostupan je u Pregledu hardvera.
Share the recorded CSV as text only after choosing the receiving app. No background uploads.|Podijelite CSV kao tekst tek nakon odabira aplikacije. Nema pozadinskog slanja.
Statistics from explicitly saved device samples.|Statistika iz izričito spremljenih uzoraka uređaja.
X: recorded time · Y: Android battery percentage|X: vrijeme snimanja · Y: Android postotak baterije
CPU Readings|CPU očitanja
GPU Readings|GPU očitanja
Memory Readings|Očitanja memorije
Storage Readings|Očitanja pohrane
Battery Readings|Očitanja baterije
Thermal Readings|Toplinska očitanja
Display Readings|Očitanja zaslona
Device Readings|Očitanja uređaja
Sensor Readings|Očitanja senzora
Device RAM and internal storage|RAM uređaja i unutarnja pohrana
Processor Information|Podaci o procesoru
Usage Controls|Kontrole uporabe
Session Measurements|Mjerenja sesije
Charging Readings|Očitanja punjenja
CPU Test Controls|Kontrole CPU testa
Benchmark Measurements|Mjerenja performansi
Recording Controls|Kontrole snimanja
Permission Controls|Kontrole dozvola
Health Readings|Očitanja stanja
Privacy Details|Detalji privatnosti
Live Sensor Controls|Kontrole senzora uživo
Detailed Battery Timeline|Detaljan tijek baterije
Performance Lab Controls|Kontrole performansi
Network Connection Readings|Očitanja mrežne veze
Live Alerts Controls|Kontrole upozorenja
Report Export Controls|Kontrole izvoza izvještaja
HUD Preview Controls|Kontrole prikaza HUD-a
Passed Diagnostic Results|Uspješni dijagnostički rezultati
Failed Diagnostic Results|Neuspješni dijagnostički rezultati
Skipped Diagnostic Results|Preskočeni dijagnostički rezultati
Unavailable Diagnostic Results|Nedostupni dijagnostički rezultati
S|S
Advanced Device Intelligence|Napredna analiza uređaja
""".trimIndent().lineSequence().filter { it.isNotBlank() }.associate { row ->
    val delimiter = row.indexOf('|')
    require(delimiter > 0) { "Malformed SYSTIVRA translation row" }
    row.substring(0, delimiter) to row.substring(delimiter + 1)
}

internal fun translatedUiText(text: String, languageCode: String): String {
    if (normalizedUiLanguage(languageCode) == UI_LANGUAGE_ENGLISH) return text
    croatianUi[text]?.let { return it }
    // Translate the static words of actual device-derived dynamic copy; the
    // measured value and units on the right-hand side stay authentic.
    val prefixes = listOf(
        "Open " to "Otvori ",
        "Source: " to "Izvor: ",
        "Preview position: " to "Položaj prikaza: ",
        "Saved samples: " to "Spremljeni uzorci: ",
        "Battery threshold: " to "Prag baterije: ",
        "Touches registered: " to "Zabilježeni dodiri: ",
        "Refresh interval: " to "Interval osvježavanja: ",
        "Selected recording: " to "Odabrana snimka: ",
        "Recorded span: " to "Snimljeno trajanje: ",
        "No " to "Nema ",
        "Live data: " to "Podaci uživo: ",
        "Status: " to "Stanje: "
    )
    for ((english, croatian) in prefixes) {
        if (text.startsWith(english) && text.length > english.length) {
            val suffix = text.removePrefix(english)
            return croatian + (croatianUi[suffix] ?: suffix)
        }
    }
    return text
}

/**
 * Shared Material 3 Text adapter preserves every default text/typography
 * parameter and all click/semantics modifiers. UI screens import this as Text
 * to localize existing content without replacing icons, card geometry or
 * actual hardware readings.
 */
@Composable
internal fun LocalizedText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    MaterialText(
        text = translatedUiText(text, LocalUiLanguage.current),
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = style
    )
}
