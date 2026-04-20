ProximAlert: Add Alert Flow - Screen-by-Screen Composition

This document breaks down the visual and structural composition of the "New Alert" creation flow, analyzing the progressive disclosure pattern and state-based UI changes across the four screens.

Global Design System Elements

Theme: Dark mode default (#0D0D0D background) with high-contrast colored accents.

Typography: 'Inter' font family, utilizing heavy weights (Bold/800) for hero headers and readable standard weights for secondary text.

Color Coding Strategy: The flow uses distinct accent colors to differentiate steps:

Step 1 (Destination): Cyan / Teal (#00BFA5 / #44DDC1)

Step 2 (Arrival/Alarm Area): Orange / Yellow (#E09D00 / #FFBA38)

Step 3 (Time): Purple / Lavender (#CDBDFF)

Layout Structure: Sticky top app bar, scrollable main content area, and fixed bottom fade or action bars to ensure content isn't obscured.

Screen 1: Destination Selection (add_alert_step_1)

Purpose: Initial input screen where the user searches for their final destination.

Composition Breakdown:

Top App Bar: Minimalist header with a 'Close' (X) icon on the left, "New Alert" centered title, and an empty placeholder on the right for flex balance.

Hero Section:

Large, two-line heading: "Where are you going?" (30px, Bold).

Subtext: "Search and select your destination" (#9E9E9E).

Active Input State:

Label: "Destination" preceded by a cyan indicator dot.

Search Bar: Elevated dark grey (#1A1A1A) container with a prominent cyan bottom border signifying the active state. Includes a leading location pin icon, active text ("Mumbai"), and a trailing microphone icon.

Dropdown Results:

Attached directly to the search bar to form a cohesive container.

Highlighted Row: The top result ("Mumbai Central Railway Station") has a left cyan border and a slightly lighter background, indicating focus/selection.

List Items: Each item features a square icon container (train/subway), title, subtitle (address), and a right-pointing arrow (north_east icon) to prompt selection.

Screen 2: Alarm Location Selection (add_alert_state_2)

Purpose: Step two of the progressive flow. The user selects exactly where the alarm should trigger (usually prior to the final destination).

Composition Breakdown:

Progress Stepper (Timeline): Introduction of a vertical timeline on the left edge connecting the steps.

Completed Step 1 (Destination):

Timeline indicator becomes a filled cyan dot.

The container shrinks into a locked, summary state: dark green tint border, cyan checkmark, the selected station name, and a "CHANGE" text button.

Hero Section: Updates contextually to "Now, where should the alarm fire?".

Active Step 2 (Alarm Location):

Timeline indicator is a glowing orange dot.

Input Bar: The bottom border and leading bell icon change to the step's orange accent color. Contains a clear ('X') button.

Dropdown Results & Helper:

Active selection ("Dadar Railway Station") highlighted with an orange left-border.

Info Tooltip: A distinct, pill-shaped container at the bottom with a lightbulb icon providing contextual help: "This is usually a stop or two before your final destination".

Screen 3: Time Selection (add_alert_state_3)

Purpose: Step three. The user selects how many minutes before arriving at the alarm location the notification should fire.

Composition Breakdown:

Extended Timeline: Now shows three nodes connected by dashed lines, colored cyan, orange, and purple.

Completed Steps 1 & 2:

Destination (Cyan) and Arrival Area (Orange) are both collapsed into summary cards with their respective checkmarks and "Change" buttons.

Hero Section: Updates to "How early should we alert you?".

Active Step 3 (Time):

Timeline indicator is a glowing purple dot.

Includes a contextual help link: "How is this calculated?" aligned right above the input.

Dropdown Trigger: A clickable surface with a purple left-border, a clock icon, placeholder text, and a chevron indicating expansion.

Floating Dropdown Menu:

Elevated above the UI with a shadow drop (z-30).

Contains predefined time intervals (5, 10, 15, 20, 30 minutes) and a "Custom..." option separated by a divider line.

Screen 4: Review and Save (add_alert_review_save)

Purpose: The final confirmation screen. Provides a comprehensive overview of the alert settings before activation.

Composition Breakdown:

Hero Section: "Almost done! Review & save."

Timeline Summary List:

All three previous steps are now collapsed.

Timeline markers are muted circles with internal checkmarks (Cyan, Orange, Purple).

Each step displays its final selected value clearly with a "Change" option.

Alert Summary Card:

A prominent, slightly elevated card summarizing the core journey.

Visual "Route" representation: Start dot (Cyan) -> Dotted line -> End dot (Orange) with an "alarm fires here" badge.

Bottom pill showing the trigger condition ("10 min before") with Sound and Vibration icons.

Features a subtle, blurred cyan radial gradient in the top right corner for visual depth.

Settings Row:

A three-column grid at the bottom (Sound, Vibration, Snooze) showing current hardware settings ("Default", "Pulse", "5 min").

Fixed Bottom Action Bar:

A sticky container with a glass-morphism effect (backdrop-blur).

Primary CTA: A large, full-width button with a cyan-to-teal gradient background, reading "Save Alert" with a ringing bell icon.

System Status Text: Small informational text below the CTA ("Runs in background even when app is closed").