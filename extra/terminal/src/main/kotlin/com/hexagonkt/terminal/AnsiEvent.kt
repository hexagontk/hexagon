package com.hexagonkt.terminal

enum class AnsiEventKind {
    KEY,
    MOUSE,
}

data class AnsiEvent(
    val kind: AnsiEventKind,
)
