package com.github.tvinke.algorilla.reporting

@Suppress("TooManyFunctions")
public object Ansi {
    public fun bold(
        text: String,
        enabled: Boolean,
    ): String = wrap(text, "1", enabled)

    public fun underline(
        text: String,
        enabled: Boolean,
    ): String = wrap(text, "4", enabled)

    public fun red(
        text: String,
        enabled: Boolean,
    ): String = wrap(text, "31", enabled)

    public fun green(
        text: String,
        enabled: Boolean,
    ): String = wrap(text, "32", enabled)

    public fun yellow(
        text: String,
        enabled: Boolean,
    ): String = wrap(text, "33", enabled)

    public fun cyan(
        text: String,
        enabled: Boolean,
    ): String = wrap(text, "36", enabled)

    public fun dim(
        text: String,
        enabled: Boolean,
    ): String = wrap(text, "2", enabled)

    public fun boldWhite(
        text: String,
        enabled: Boolean,
    ): String = wrap(text, "1;37", enabled)

    public fun bgRed(
        text: String,
        enabled: Boolean,
    ): String = wrap(text, "41;1;37", enabled)

    public fun bgYellow(
        text: String,
        enabled: Boolean,
    ): String = wrap(text, "43;1;30", enabled)

    public fun bgCyan(
        text: String,
        enabled: Boolean,
    ): String = wrap(text, "46;1;37", enabled)

    private fun wrap(
        text: String,
        code: String,
        enabled: Boolean,
    ): String = if (enabled) "\u001b[${code}m$text\u001b[0m" else text
}
