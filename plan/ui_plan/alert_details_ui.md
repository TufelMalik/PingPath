Here is a detailed, screen-by-screen composition breakdown of the **ProximAlert - Live Alert Detail Screen**, based on the provided HTML and image.

The screen is built using a dark theme with a primary teal/mint accent color (`#44DDC1`), a tertiary amber accent (`#ffba38`), and a modular, card-based layout stacked vertically.

### 1. Top App Bar (Header)
* **Layout:** Sticky header fixed at the top, using a flexbox layout (`justify-between`, `items-center`).
* **Background:** Solid dark surface (`#131313`) to blend with the app background.
* **Components:**
    * **Leading Icon:** "Back" arrow icon (`arrow_back`) in the primary teal color.
    * **Title:** "ProximAlert" centered, medium weight, 1.75rem text.
    * **Trailing Icon:** "More options" (3 vertical dots) in muted grey.

### 2. Main Content Area
A scrollable container with horizontal padding and consistent vertical spacing (`space-y-6`) between cards.

#### A. Screen Title
* **Text:** "Alert Details"
* **Styling:** Left-aligned, bold, 2xl typography in the primary text color (`text-on-surface`).

#### B. Route Timeline Card (Hero Card)
* **Background:** Elevated dark grey surface (`bg-surface-container-high`) with fully rounded corners.
* **Top Badges Row:** * **Left Badge:** "ON ENTRY ALARM" - Pill-shaped with a teal tint background, teal outline, and a location pin icon.
    * **Right Badge:** "LIVE TRACKING" - Text paired with an animated (pulsing) bright green indicator dot.
* **Vertical Timeline:** Uses `relative` and `absolute` positioning to draw connecting lines and nodes.
    * **Node 1 (Your Location):** Teal circular icon with a hollow center. Subtitle "Near Borivali, Mumbai".
    * **Connector 1:** Teal dashed vertical line.
    * **Node 2 (Alarm Trigger):** Glowing amber circle (utilizes a drop shadow for the glow effect). Features right-aligned teal pill indicating "4.2 km away". Subtitle "Dadar Railway Station".
    * **Connector 2:** Muted grey dashed vertical line.
    * **Node 3 (Final Destination):** Hollow teal circle. Subtitle "Mumbai Central Railway Station" with a right-aligned muted distance "6.8 km away".

#### C. Distance Metrics Card
* **Background:** Elevated dark grey surface, identical to the Hero Card.
* **Data Split (Top Half):** Two equal-width columns separated by a subtle vertical right border.
    * **Left Column:** "To Alarm Location" label. Large prominent "4.2 km" in primary teal. A smaller label underneath with a downward arrow stating "Getting closer".
    * **Right Column:** "To Destination" label. Large "6.8 km" in standard white text. A truncated route label "Dadar → Mumbai Central" below.
* **Progress Bar (Bottom Half):**
    * **Track:** Dark background with rounded ends.
    * **Fill:** Primary teal bar spanning roughly 55% of the width.
    * **Markers:** Three vertical notch lines overlaying the track. The middle notch is amber, signifying the alarm point (Dadar).
    * **Labels:** Three tiny text labels aligned under their respective markers (Borivali, Dadar, Mumbai Central).

#### D. ETA (Estimated Time of Arrival) Card
* **Background:** Very subtle teal tinted background (`bg-primary-fixed/5`) with a soft primary teal border and faint drop shadow.
* **Layout:** Row-based flex layout.
* **Left Icon:** Large "schedule" (clock) icon sitting inside a circular teal-tinted background.
* **Right Content:**
    * **Top Row:** Large teal text "~8 minutes" followed by a dark grey, blocky "ON TIME" badge.
    * **Bottom Row:** Amber text explaining the trigger condition: "Alert fires 10 min before arrival".

#### E. Quick Settings Row
* **Background:** Single elevated dark grey container (`bg-surface-container-high`).
* **Layout:** Three equal-width flex buttons divided by subtle vertical borders (`divide-x`).
* **Buttons:**
    * **Default Sound:** Music note icon.
    * **Pulse Vibrate:** Phone vibration icon.
    * **Snooze 5M:** Alarm snooze icon.
* **Styling:** Each button stacks an outline icon above highly tracked, tiny uppercase text (`text-[10px] uppercase font-bold tracking-wider`).

#### F. Call to Action (Cancel Section)
* **Cancel Button:** Full-width, prominent, highly rounded button (`rounded-full`). Background is a bright alert red (`#FF5252`) with a matching soft red shadow to make it pop against the dark theme. Text is bold and white.
* **Status Footer:** Muted, tiny text below the button stating "Alarm is running in background..." accompanied by a small spinning sync icon.