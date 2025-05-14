# NeuroFleet

### Wichtigste Links & URL
| Name          | URL |
|--------------|----|
| Deployte Applikation         | [Azure Deployment]() |
| Code         | [GitHub Repository](https://github.com/ISxOdin/neurofleet) |
| Postman Dokumentation  | [Postman_Documentation](https://documenter.getpostman.com/view/40518505/2sB2j1gBvr) |
| Roadmap-History         | [History](https://github.com/users/ISxOdin/projects/2/views/7) |
| Chart mit Iterations und Labels         | [Iterations](https://github.com/users/ISxOdin/projects/2/insights/2) |
| SonarQube         | [Tbd]() |

![Workflow](https://github.com/ISxOdin/neurofleet/actions/workflows/ci.yaml/badge.svg)
![Coverage](https://github.com/ISxOdin/neurofleet/blob/main/.github/badges/jacoco.svg) 
![Branches](https://github.com/ISxOdin/neurofleet/blob/main/.github/badges/branches.svg)

Was sind die Kernideen?

1. KI-gestützte Transportkapazitätsprognose
   - Maschinelles Lernen analysiert vergangene Sendungen, Wetter & Verkehr, um zukünftige Engpässe vorherzusagen.
   - Mit Hilfe von ML-Modellen, Kapazitätsengpässe vorhersehen.
2. Automatische Routenoptimierung
   - Echtzeitdaten helfen, die beste Route basierend auf Verkehr, Mautgebühren oder Umweltfaktoren zu berechnen.
   - Nutzung von Google Maps API oder OpenStreetMap für smarte Wegfindung.
3. Kapazitäts- und Frachtplanung mit KI
   - KI schlägt vor, welche Ladung auf welche Fahrzeuge verteilt werden sollte.
   - Reduzierung von Leerfahrten durch smarte Ladungsoptimierung.
4. Echtzeit-Sichtbarkeit & Visualisierung
   - Live-Dashboards mit Heatmaps für Engpässe.
   - Simulationen für verschiedene Transport-Szenarien.

Echtzeitdaten sind für den Anfang Out of Scope.

# Inhaltsverzeichnis

- [Einleitung](#einleitung)
  - [Explore-Board](#explore-board)
  - [Create-Board](#create-board)
  - [Evaluate-Board](#evaluate-board)
  - [Diskussion Feedback Pitch](#diskussion-feedback-pitch)
- [Anforderungen](#anforderungen)
  - [Use-Case Diagramm](#use-case-diagramm)
  - [Use-Case Beschreibung](#use-case-beschreibung)
  - [Fachliches Datenmodell](#fachliches-datenmodell)
  - [Erläuterungen zum Datenmodell](#erläuterungen-zum-datenmodell)
  - [Zustandsdiagramm](#zustandsdiagramm)
  - [UI-Mockup](#ui-mockup)
- [Implementation](#implementation)
  - [Frontend](#frontend)
  - [KI-Funktionen](#ki-funktionen)
- [Fazit](#fazit)
  - [Stand der Implementation](#stand-der-implementation)

# Einleitung

## Explore-Board

### TRENDS & TECHNOLOGIE

- **Megatrends:** Digitalisierung, Automatisierung, Nachhaltigkeit, Urbanisierung
- **Technologische Entwicklungen:** KI-gestützte Routenplanung, Predictive Analytics, IoT-basierte Fahrzeugüberwachung
- **Soziokulturelle Trends:** Sharing Economy, Umweltbewusstsein, steigende Nachfrage nach On-Demand-Logistik
- **Konsum- und Zeitgeisttrends:** Effizienzsteigerung in der Logistik, steigende E-Commerce-Bedürfnisse, autonome Fahrzeuge
- **Digitale Innovationen:** Cloud-basierte Flottenmanagement-Systeme

### POTENTIELLE PARTNER & WETTBEWERB

- **Potenzielle Partner:** Logistikdienstleister, Speditionen, Technologieanbieter für KI und IoT, Universitäten mit Forschung zu Transport und KI
- **Wettbewerber:** SAP Transportation Management, Transporeon, Sennder, FourKites, Project44
- **Marktentwicklung:** Wachsende Nachfrage nach intelligenter Transportplanung, zunehmende Vernetzung von Logistiksystemen

### FAKTEN

- NeuroFleet ist ein Prototyp-Projekt mit 150 Stunden Entwicklungszeit
- Fokus auf Simulation statt Echtzeit-Datenintegration
- Einsatz von Mock-Daten für Kapazitätsplanung
- Hauptnutzer: Logistikunternehmen mit mehreren Standorten, wobei der Fokus auf Lastwagentransport liegt
- Kombination aus KI, Simulation und Datenanalyse für optimierte Transportkapazitäten

### POTENZIALFELDER

- Optimierung von Flottenauslastung
- Einsatz von KI zur Kapazitätsverteilung
- Automatisierte Entscheidungsunterstützung für Disponenten
- Dynamische Anpassung der Kapazitätsplanung an veränderte Marktbedingungen

### USER

- **Primäre Zielgruppe:** Transport- und Logistikunternehmen
- **Sekundäre Zielgruppe:** Disponenten, Fuhrparkmanager, strategische Planer
- **Mögliche Stakeholder:** IT-Abteilungen, Geschäftsführung, Kunden der Logistikunternehmen

### BEDÜRFNISSE

- Effizientere Nutzung von Flottenkapazitäten
- Reduzierung von Leerfahrten und Ressourcenverschwendung
- Optimierte Kapazitätsverteilung durch KI-basierte Entscheidungen
- Intuitive Benutzeroberfläche zur schnellen Entscheidungsfindung

### ERKENNTNISSE

- Disponenten benötigen einfache, visuelle Dashboards für schnelle Entscheidungen
- Logistikunternehmen stehen unter wachsendem Kostendruck
- Die Transportbranche leidet unter Fahrermangel und ineffizienter Kapazitätsnutzung
- Datenbasierte Entscheidungsfindung wird immer wichtiger

### TOUCHPOINTS

- Web-Dashboard für Flottenmanagement\*\*
- Benachrichtigungen und Vorschläge für optimierte Kapazitätsverteilung
- Schnittstellen zur Nutzung von Mock-Daten für Simulationen

### WIE KÖNNEN WIR?

- Wie können wir Transportkapazitäten so optimieren, dass Unternehmen Leerfahrten vermeiden und gleichzeitig flexibel auf Marktveränderungen reagieren können?
- Wie können wir Disponenten eine intuitive Möglichkeit bieten, ihre Kapazitäten effizienter zu verteilen und Ressourcen bestmöglich zu nutzen?
- Wie können wir durch KI-gestützte Simulationen eine möglichst realitätsnahe Kapazitätsplanung ermöglichen?

## Create-Board

### IDEEN-BESCHREIBUNG

NeuroFleet ist eine KI-gestützte Plattform zur Optimierung der Transportkapazitäten in Logistikunternehmen. Sie nutzt Simulationen und vorausschauende Analysen, um eine intelligente Kapazitätsverteilung zu ermöglichen und Leerfahrten zu reduzieren. Die Plattform basiert vollständig auf Mock-Daten und bietet eine intuitive Benutzeroberfläche für Disponenten.

### ADRESSIERTE NUTZER

Logistikunternehmen mit mehreren Standorten

Disponenten und Flottenmanager

Strategische Planer für Kapazitätsmanagement

IT-Abteilungen und Digitalisierungsbeauftragte

### ADRESSIERTE BEDÜRFNISSE

Optimale Verteilung der vorhandenen Transportkapazitäten

Reduktion von Leerfahrten und ineffizienter Nutzung von Ressourcen

Dynamische Anpassung der Kapazitätsplanung an veränderte Marktbedingungen

Effiziente Disposition ohne hohe Integrationsaufwände

### PROBLEME

1. Ineffiziente Kapazitätsnutzung: Unternehmen nutzen ihre Transportressourcen nicht optimal, was zu hohen Kosten und ungenutzten Kapazitäten führt.
2. Hohes Mass an Leerfahrten: Lkw und Transportkapazitäten werden nicht effizient ausgelastet, was Kosten verursacht und die Umwelt belastet.
3. Komplexe Entscheidungsprozesse: Disponenten stehen vor der Herausforderung, Kapazitäten manuell und ineffizient zuzuweisen.

### IDEENPOTENZIAL

**Mehrwert: Mückenstich vs. Hai-Attacke**

🔵🔵🔵⚪️⚪️⚪️⚪️⚪️⚪️⚪️  
(Mittelmässiger Schmerzpunkt, aber relevant für Effizienzsteigerung)

**Übertragbarkeit: Robinson Crusoe vs. die Welt**

🔵🔵🔵🔵🔵⚪️⚪️⚪️⚪️⚪️  
(Gutes Potenzial zur Skalierung auf viele Logistikunternehmen)

**Machbarkeit: Hammer vs. Raumschiff**

🔵🔵🔵🔵🔵🔵🔵🔵⚪️⚪️  
(Technisch umsetzbar mit vorhandenen Technologien)

### DAS WOW

NeuroFleet bietet eine vollständig KI-gesteuerte Kapazitätsverteilung, die ohne komplexe menschliche Eingriffe funktioniert. Unternehmen erhalten innerhalb von Minuten eine optimierte Verteilung ihrer Flottenkapazitäten basierend auf dynamischen Simulationsmodellen.

### HIGH-LEVEL-KONZEPT

„Das Tetris für Transportkapazitäten“ - NeuroFleet nutzt KI, um Transportkapazitäten so effizient zu „stapeln“, dass keine Leerflächen entstehen – genau wie im perfekten Tetris-Spiel.

### WERTVERSPRECHEN

NeuroFleet ermöglicht Logistikunternehmen eine intelligente, KI-gestützte Kapazitätsverteilung. Durch die Simulation verschiedener Szenarien und die automatische Optimierung von Transportressourcen werden Leerfahrten minimiert, die Auslastung maximiert und CO₂-Emissionen reduziert. Unsere Plattform sorgt dafür, dass die richtigen Kapazitäten zur richtigen Zeit am richtigen Ort sind – ohne zeitaufwändige manuelle Planung.

## **Evaluate-Board**

### KANÄLE

Die NutzerInnen sollen über verschiedene digitale und analoge Kanäle erreicht werden, um eine breite Zielgruppe zu erreichen und die Verbreitung der Lösung sicherzustellen. Dazu gehören:

- **LinkedIn** – Fachbeiträge und gezieltes B2B-Marketing für Logistikunternehmen und Entscheidungsträger.
- **Google Ads & SEO** – Optimierung der Webpräsenz für gezielte Suchanfragen im Bereich Transportoptimierung.
- **Messen & Fachkonferenzen** – Präsentation auf Logistik- und Transportmessen zur direkten Kundenansprache.
- **E-Mail-Marketing** – Gezielte Kampagnen für potenzielle Unternehmenskunden mit Use Cases und Erfolgsstories.
- **Webinare & Demos** – Regelmässige Online-Veranstaltungen zur Vorstellung von Funktionen und Vorteilen der Lösung.
- **Partnerschaften mit Logistikverbänden** – Zusammenarbeit mit Branchennetzwerken zur Steigerung der Bekanntheit.

### UNFAIRER VORTEIL

Die Lösung bietet folgende Alleinstellungsmerkmale, die schwer oder gar nicht kopierbar sind:

- **KI-gestützte Optimierung**: Selbstlernende Algorithmen analysieren kontinuierlich Transportdaten und verbessern die Effizienz.
- **Simulationen für Kapazitätsauslastung**: Vergleich verschiedener Szenarien zur besseren Entscheidungsfindung.
- **Dynamische Disposition**: Automatische Neuzuweisung von Transportkapazitäten in Echtzeit.
- **Benutzerfreundlichkeit**: Intuitives Dashboard für schnelle Analyse und Anpassung von Kapazitätsplänen.

### KPI

Zur Erfolgsmessung der Lösung werden folgende KPIs verwendet:

- **Nutzerwachstum**: Anzahl der registrierten Unternehmen und aktiven Nutzer.
- **Kapazitätsauslastung**: Durchschnittlicher Anstieg der Transportauslastung durch Nutzung der Lösung.
- **Reduktion von Leerfahrten**: Prozentuale Abnahme der Leerfahrten pro Unternehmen.
- **Kosteneinsparungen**: Durchschnittliche Kostenreduktion durch optimierte Transporte.
- **CO₂-Einsparung**: Reduktion des CO₂-Ausstosses durch effizientere Routenplanung.
- **Kundenzufriedenheit**: Net Promoter Score (NPS) basierend auf Kundenfeedback.

### EINNAHMEQUELLEN

Die Monetarisierung erfolgt über verschiedene Einnahmemodelle:

- **Lizenzbasierte Abonnements**: Monatliche oder jährliche Gebühren für Unternehmen mit verschiedenen Nutzungsplänen.
- **Enterprise-Lösungen**: Individuelle Anpassungen und Integrationen für Grosskunden (mindestens 100 Fahrzeugen oder >10.000 Transporten pro Monat) mit massgeschneiderten Features.
- **Datenanalyse & Insights**: Verkauf anonymisierter Datenanalysen für Markttrends und Logistikoptimierung.
- **Fördergelder & Subventionen**: Unterstützung durch Programme für nachhaltige Logistiklösungen.

## Diskussion Feedback Pitch

> Diskussion des Feedbacks aus dem Pitch (bezogen auf Projektinhalt)

# Anforderungen

## Use-Case Diagramm

![Das Datenmodell zur Applikation NeuroFleet](uc-diagramm.drawio.svg)

## Use-Case Beschreibung

Es werden folgende Rollen gebraucht:

- Manager
- Employee
- Admin

## Fachliches Datenmodell

![Das Datenmodell zur Applikation NeuroFleet](datenmodell.drawio.svg)

## Erläuterungen zum Datenmodell

> Beschreibe die Entitäten, deren Attribute sowie die Beziehungen zwischen den Entitäten.

## Zustandsdiagramm

> Hier das Zustandsdiagramm einbinden für diejenige Entität(en), welche mehrere Zustände durchläuft mit Events, Effects und Guards.

## UI-Mockup

[Figma Link](https://www.figma.com/proto/HXADeWdARjWyuPwI7rRJwC/NeuroFleet-Mockup?node-id=0-1&t=kFJGtDqYuxMPDniw-1)

<iframe style="border: 1px solid rgba(0, 0, 0, 0.1);" width="800" height="450" src="https://embed.figma.com/design/HXADeWdARjWyuPwI7rRJwC/NeuroFleet-Mockup?node-id=0-1&embed-host=share" allowfullscreen></iframe>

# Implementation

## Frontend

> Beschreibung des Frontends mit Screenshots der fertigen Applikation. Alle Teile des GUIs, die bewertet werden sollen, müssen abgebildet sein.

## KI-Funktionen

> Aufgaben und Funktionen des eingebundenen KI-Modells.

# Fazit

## Stand der Implementation

> Stand der Implementation, nächste Schritte (mit Referenz auf den Backlog).
