Screen Composition: ProximAlert Home ("My Alerts")

This document breaks down the structural and visual composition of the ProximAlert home screen. The UI is a dark-themed, mobile-first interface designed to track proximity-based alarms.

1. Global App Shell

Theme: Dark mode (bg-[#0D0D0D]).

Typography: The 'Inter' font family is used globally for a clean, modern, and highly legible appearance.

Layout: A vertical flexbox layout (flex-col) that spans the full height of the viewport, keeping the content scrollable while pinning the header and floating actions.

2. Top App Bar (Header)

A sticky, full-width header that establishes brand identity and provides global actions.

Left Group (Branding):

Logo Icon: A filled notification bell icon in the primary cyan/teal color (#44DDC1).

App Title: "ProximAlert" written in bold, white text.

Right Group (Actions):

Secondary Icons: An outlined notification bell and a filter/tune icon, both in a muted gray (#9E9E9E). These are interactive and designed to hover/transition to white.

3. Section Header

The entry point to the main content area, providing context on what the user is looking at.

Title: "My Alerts" displayed prominently in white text.

Status Badge: A pill-shaped badge displaying "4 ACTIVE". It uses a dark green background (#1A2A24) with bright green text (#00BFA5) to subtly indicate a positive, active state without overwhelming the primary cyan brand color.

4. Alert Card Component (List Items)

The core component of this screen. Each card represents an active proximity alarm and is structured into distinct vertical sections inside a rounded (16px), dark gray container (#1A1A1A).

Row 1: Badges & Status

Trigger Type Badge: A bright cyan pill (#00BFA5 background, black text) indicating the trigger condition (e.g., "ON ENTRY").

Live Status Indicator: A dark, semi-transparent pill on the right containing a glowing, pulsating green dot and the text "LIVE", signaling real-time tracking.

Row 2 & 3: Location Details

Destination Name: The primary target (e.g., "Mumbai Central Station") in large, bold white text.

Trigger Context: A subtitle featuring a small location pin icon and text (e.g., "Alarm at Dadar Station") in muted gray, explaining exactly when/where the alarm goes off.

Row 4: Progress Metrics

Distance: The remaining distance (e.g., "4.2 km remaining") highlighted in the primary cyan color.

ETA: The estimated time of arrival (e.g., "~8 min") right-aligned in muted gray.

Row 5: Visual Progress Bar

Track: A dark gray background track.

Fill: A cyan fill (#44DDC1 or a gradient) representing the distance covered.

Knob: A glowing cyan dot at the leading edge of the progress bar, drawing the eye to the current progress state.

Row 6: Card Footer / Actions

Settings Summary: A small speaker icon and text (e.g., "10 min before · Sound") indicating the alarm's configuration.

Primary Action: A text-based "Cancel" button in a distinct red (#FF5252), placed on the right for easy thumb access to abort the alarm.

5. Floating Action Button (FAB)

Placement: Fixed to the bottom-right corner of the screen.

Design: A large, circular button with a bright teal/cyan background (#00BFA5), containing a white "+" (add) icon.

Purpose: The primary call-to-action for the screen, allowing users to quickly create a new proximity alert. It features a drop-shadow to maintain elevation above the scrolling list of cards.