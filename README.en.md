# Lunaison

[Français](README.md) · **English**

A free and open source menstrual cycle tracker, with no account. To log your periods, symptoms and mood, understand what is happening in each phase, know when your next period is coming, and let your partner know if you wish.

Lunaison is a free Android app, open source (GPL-3.0), with no ads, no trackers and **no Internet access**. Everything stays on the phone.

## What the app does

- **Log your days**: flow, pain (described by its effect), symptoms, moods, a personal note. Everything is optional, and a forgotten day can be filled in later.
- **Forecast periods**: an estimated date and a plausible range, calculated from your recent cycles. The range narrows as your history grows.
- **Understand the current phase**: what is happening, what helps, and what tends to come back for you during this phase based on your previous cycles.
- **Spot what deserves medical advice**: repeated severe pain, periods longer than 8 days, repeated heavy periods, cycles outside the usual range, bleeding between periods, missing periods. Never a diagnosis.
- **Read referenced guides**: the cycle, pain, food, premenstrual syndrome, when to see a doctor, toxic shock, limits of forecasts, a guide for partners and family.
- **Reminders**: before your period and to log your day, discreet on the lock screen.
- **See what your cycles show**: usual length, accuracy of forecasts, what comes back during a phase and how many days before your period, a review of each completed cycle. Calculated on the phone, never before two complete cycles.
- **One referenced tip a day**: it changes each day according to the phase, and responds first to what you have logged.
- **Share with a partner** (18 and over): the app prepares a message and opens SMS, WhatsApp, Signal or another app. The person taps Send themselves: nothing is sent automatically.

Lunaison is **neither a medical device nor a method of contraception**.

The app sticks to what is necessary. What it does not say itself, where its reference ranges come from and why it is built this way, is explained in [Sources and design choices](https://relthdrarxash.github.io/lunaison/references.html) (in French).

## Why Lunaison is free

Lunaison grew out of my partner's need: to track her cycle more easily, and to let me support her day to day. Existing apps often keep their useful features behind a subscription, or fund themselves with their users' data, even though it is some of the most intimate data there is. Lunaison does the opposite: it is free, open source, works without the Internet and keeps everything on the phone. Sharing with a partner goes through the phone's messaging app, when the person decides, and never contains fertility data.

## Privacy

- No Internet permission, no third-party SDK, no account.
- Cloud backup and automatic transfer turned off: health data does not leave the phone. A spreadsheet export is available in the settings.
- The system contact picker gives access only to the chosen contact, with no permission on the address book.
- Complete erasure in one tap.

## Technical details

Kotlin, Jetpack Compose, Material 3, Room, WorkManager. minSdk 26, targetSdk 36.

The app is in French and English, following the phone's language. Every text is written in both languages side by side (the `tr` function in `:core:model`), so that no translation can go missing; the guides have one file per language, checked by the tests.

```
:app                 Screens, navigation, reminders, sharing
:core:model          Logged day: flow, pain, symptoms, moods; app language (pure Kotlin)
:core:cycle          Engine: period detection, forecast, phases, alerts, reminders (pure Kotlin, tested)
:core:content        Guides, advice for each phase, share messages, sources (pure Kotlin, tested)
:core:data           Local Room database and single repository
:core:designsystem   Theme, Atkinson Hyperlegible font, accessible components
```

The engine's rules and their references are in [`CycleConfig.kt`](core/cycle/src/main/kotlin/fr/lunaison/core/cycle/CycleConfig.kt). The sources for the content and the design choices are in [documentation/sources.md](documentation/sources.md) (in French), published on the website as [Sources and design choices](https://relthdrarxash.github.io/lunaison/references.html). After a change, regenerate the page with `python3 scripts/site-sources.py`.

Tests: `./gradlew test`. Build: `./gradlew :app:assembleDebug`.

## Ideas being considered

Features that have been studied but not yet built, such as importing your history from Clue or Flo, are described in [documentation/pistes.md](documentation/pistes.md) (in French).

## Contact

pilmil.studio@proton.me

## A note on development

Most of Lunaison's code was written with Claude, Anthropic's AI assistant. I reviewed it, tested it and maintain it: the cycle engine and the content are covered by automated tests, and I answer issues and messages myself.
