package fr.lunaison.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.PathParser
import fr.lunaison.core.designsystem.LunaColors
import fr.lunaison.core.designsystem.R as DesignR
import fr.lunaison.core.model.Language
import fr.lunaison.core.model.tr
import java.io.File
import java.io.FileOutputStream
import kotlinx.datetime.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

internal actual fun formatReportFullDate(date: LocalDate): String =
    date.toJava().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(ReportText.locale()))

internal actual fun formatReportShortDate(date: LocalDate): String =
    date.toJava().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(ReportText.locale()))

/**
 * Le polonais et l'ukrainien ont des lettres (ą ć ę ł ń ś ź ż, cyrillique) absentes d'Atkinson Hyperlegible
 * telle qu'embarquée dans l'appli (219 glyphes : latin de base et Latin-1, sans Latin étendu A ni cyrillique).
 * Pour ces langues le PDF utilise la police sans empattement du système, qui les couvre.
 */
internal fun reportNeedsSystemFont(language: Language): Boolean = language == Language.PL || language == Language.UK

// ---------------------------------------------------------------------------------------------
// Dessin : PdfDocument, Canvas, StaticLayout. Aucune bibliothèque tierce, aucun accès au réseau.
// ---------------------------------------------------------------------------------------------

/** Écrit le bilan dans un PDF A4, paginé automatiquement. */
object HealthReportPdf {
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 48f
    private const val CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN
    private const val HEADER_HEIGHT = 96f
    private const val FIRST_PAGE_TOP = HEADER_HEIGHT + 18f
    private const val PAGE_TOP = MARGIN + 22f
    private const val CONTENT_BOTTOM = PAGE_HEIGHT - 66f
    private const val FOOTER_RULE_Y = PAGE_HEIGHT - 50f
    private const val LINE_SPACING = 1.22f

    /** Dossier du cache où le PDF est écrit puis partagé : voir res/xml/file_paths.xml. */
    const val CACHE_DIRECTORY = "rapports"
    const val FILE_NAME = "bilan-lunaison.pdf"

    /** Écrit le PDF dans le cache de l'appli (en écrasant le précédent) et renvoie le fichier. */
    fun export(context: Context, document: ReportDocument): File {
        val directory = File(context.cacheDir, CACHE_DIRECTORY)
        if (!directory.exists() && !directory.mkdirs()) throw java.io.IOException("Dossier $CACHE_DIRECTORY inaccessible")
        val file = File(directory, FILE_NAME)
        write(context, document, file)
        return file
    }

    fun write(context: Context, document: ReportDocument, file: File) {
        val fonts = Fonts.load(context, Language.current())
        val chunks = ReportLayout(fonts).chunks(document)
        val pages = paginate(chunks)
        val pdf = PdfDocument()
        try {
            pages.forEachIndexed { index, placed ->
                val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, index + 1).create()
                val page = pdf.startPage(info)
                val canvas = page.canvas
                if (index == 0) drawHeader(canvas, document, fonts) else drawRunningHeader(canvas, document, fonts)
                placed.forEach { it.chunk.draw(canvas, MARGIN, it.y) }
                drawFooter(canvas, fonts, index + 1, pages.size)
                pdf.finishPage(page)
            }
            FileOutputStream(file).use { pdf.writeTo(it) }
        } finally {
            pdf.close()
        }
    }

    // --- Pagination ---------------------------------------------------------------------------

    private class Placed(val chunk: Chunk, val y: Float)

    /**
     * Répartit les morceaux sur les pages : on ne coupe qu'entre deux morceaux, un morceau marqué
     * « garder avec le suivant » (titre de rubrique, en-tête de tableau) n'est jamais seul en bas de page,
     * et l'en-tête d'un tableau est répété si le tableau continue sur la page suivante.
     */
    private fun paginate(chunks: List<Chunk>): List<List<Placed>> {
        val pages = ArrayList<MutableList<Placed>>()
        var current = ArrayList<Placed>()
        var y = FIRST_PAGE_TOP
        var top = FIRST_PAGE_TOP
        var lastTableHeader: Chunk? = null

        fun newPage() {
            pages.add(current)
            current = ArrayList()
            top = PAGE_TOP
            y = PAGE_TOP
        }

        var index = 0
        while (index < chunks.size) {
            val chunk = chunks[index]
            if (chunk.isTableHeader) lastTableHeader = chunk
            var need = chunk.before + chunk.height
            var last = index
            while (chunks[last].keepWithNext && last + 1 < chunks.size) {
                last++
                need += chunks[last].before + chunks[last].height
            }
            val atTop = y == top
            if (!atTop && y + need > CONTENT_BOTTOM) {
                newPage()
                val header = lastTableHeader
                if (chunk.repeatsTableHeader && header != null) {
                    current.add(Placed(header, y))
                    y += header.height
                }
            }
            val before = if (y == top) 0f else chunk.before
            current.add(Placed(chunk, y + before))
            y += before + chunk.height
            index++
        }
        pages.add(current)
        return pages
    }

    // --- En-tête et pied de page --------------------------------------------------------------

    private fun drawHeader(canvas: Canvas, document: ReportDocument, fonts: Fonts) {
        val fill = Paint(Paint.ANTI_ALIAS_FLAG)
        fill.color = LunaColors.Plum.toArgb()
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), HEADER_HEIGHT, fill)

        // Halo lunaire discret à droite du bandeau pour donner du relief sans gêner la lecture.
        canvas.save()
        canvas.clipRect(0f, 0f, PAGE_WIDTH.toFloat(), HEADER_HEIGHT)
        val haloOuter = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF56345C.toInt() }
        val haloInner = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF603B67.toInt() }
        canvas.drawCircle(PAGE_WIDTH - 48f, HEADER_HEIGHT * 0.35f, 88f, haloOuter)
        canvas.drawCircle(PAGE_WIDTH - 24f, HEADER_HEIGHT * 0.25f, 52f, haloInner)
        canvas.restore()

        // Liseré d'accent argile rosé en bas du bandeau.
        val accentBar = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = LunaColors.ClayNight.toArgb() }
        canvas.drawRect(0f, HEADER_HEIGHT - 3f, PAGE_WIDTH.toFloat(), HEADER_HEIGHT, accentBar)

        val logoSize = 58f
        val logoLeft = MARGIN
        val logoTop = (HEADER_HEIGHT - 3f - logoSize) / 2f
        drawLogo(canvas, logoLeft, logoTop, logoSize)

        val textLeft = logoLeft + logoSize + 16f
        val title = fonts.paint(LunaColors.Cream, 22f, bold = true)
        canvas.drawText(document.title, textLeft, HEADER_HEIGHT / 2f - 3f, title)
        val subtitle = fonts.paint(LunaColors.Moon, 10.5f)
        canvas.drawText(document.subtitle, textLeft, HEADER_HEIGHT / 2f + 16f, subtitle)

        // Pilule « LUNAISON · CONFIDENTIEL » + date à droite.
        val badgePaint = fonts.paint(LunaColors.ClayNight, 7.5f, bold = true)
        val badgeTextWidth = badgePaint.measureText(document.confidentialBadge)
        val badgePadX = 8f
        val badgeHeight = 16f
        val badgeRight = PAGE_WIDTH - MARGIN
        val badgeLeft = badgeRight - badgeTextWidth - 2 * badgePadX
        val badgeTop = 22f
        val badgeBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF633E6A.toInt() }
        val badgeStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF8A6291.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }
        val badgeRect = RectF(badgeLeft, badgeTop, badgeRight, badgeTop + badgeHeight)
        canvas.drawRoundRect(badgeRect, 8f, 8f, badgeBg)
        canvas.drawRoundRect(badgeRect, 8f, 8f, badgeStroke)
        canvas.drawText(document.confidentialBadge, badgeLeft + badgePadX, badgeTop + 11f, badgePaint)

        val dateText = fonts.paint(LunaColors.Cream, 9f)
        dateText.textAlign = Paint.Align.RIGHT
        canvas.drawText(document.generatedLabel, PAGE_WIDTH - MARGIN, badgeTop + badgeHeight + 17f, dateText)
    }

    private fun drawRunningHeader(canvas: Canvas, document: ReportDocument, fonts: Fonts) {
        val y = 26f
        drawLogo(canvas, MARGIN, y - 3f, 18f)
        val label = fonts.paint(LunaColors.Plum, 9f, bold = true)
        canvas.drawText("Lunaison · ${document.title}", MARGIN + 24f, y + 9f, label)

        val right = fonts.paint(LunaColors.InkMuted, 8.5f)
        right.textAlign = Paint.Align.RIGHT
        canvas.drawText(document.generatedLabel, PAGE_WIDTH - MARGIN, y + 9f, right)

        val rule = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = LunaColors.PlumSoft.toArgb()
            strokeWidth = 1.2f
        }
        canvas.drawLine(MARGIN, y + 18f, PAGE_WIDTH - MARGIN, y + 18f, rule)
    }

    /**
     * Croissant de l'icône de l'appli (app/src/main/res/drawable/ic_launcher_foreground.xml, viewport 108,
     * groupe agrandi de 1,25 autour de 54,54), redessiné ici car le module de l'interface n'a pas accès
     * aux ressources de l'application. L'icône a un fond prune : on pose le logo sur une pastille
     * prune un peu plus claire pour qu'il se détache du bandeau.
     */
    private fun drawLogo(canvas: Canvas, left: Float, top: Float, size: Float) {
        val rect = RectF(left, top, left + size, top + size)
        val radius = size * 0.26f
        val tile = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF5E3A64.toInt() }
        canvas.drawRoundRect(rect, radius, radius, tile)
        if (size >= 32f) {
            val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF825B88.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }
            canvas.drawRoundRect(rect, radius, radius, stroke)
        }
        try {
            val scale = size / 108f
            val matrix = Matrix()
            matrix.setScale(1.25f, 1.25f, 54f, 54f)
            matrix.postScale(scale, scale)
            matrix.postTranslate(left, top)

            val moon = PathParser.createPathFromPathData("M60,32 A23,23 0 1,0 76,64 A18,18 0 1,1 60,32 Z")
            moon.transform(matrix)
            val moonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            moonPaint.color = LunaColors.Cream.toArgb()
            canvas.drawPath(moon, moonPaint)

            val dot: Path = PathParser.createPathFromPathData("M71,38 m-3,0 a3,3 0 1,0 6,0 a3,3 0 1,0 -6,0")
            dot.transform(matrix)
            val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            dotPaint.color = LunaColors.ClayNight.toArgb()
            canvas.drawPath(dot, dotPaint)
        } catch (_: RuntimeException) {
            // Tracé illisible : le bandeau garde sa pastille, le reste du bilan n'est pas touché.
        }
    }

    private fun drawFooter(canvas: Canvas, fonts: Fonts, page: Int, total: Int) {
        val accentRule = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = LunaColors.Clay.toArgb()
            strokeWidth = 1.5f
        }
        canvas.drawLine(MARGIN, FOOTER_RULE_Y, MARGIN + 36f, FOOTER_RULE_Y, accentRule)
        val rule = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = LunaColors.Line.toArgb()
            strokeWidth = 0.75f
        }
        canvas.drawLine(MARGIN + 36f, FOOTER_RULE_Y, PAGE_WIDTH - MARGIN, FOOTER_RULE_Y, rule)

        val left = tr(
            fr = "Lunaison · page $page / $total",
            en = "Lunaison · page $page / $total",
            pl = "Lunaison · strona $page / $total",
            uk = "Lunaison · стор. $page / $total",
            es = "Lunaison · página $page / $total",
            de = "Lunaison · Seite $page / $total",
        )
        val leftPaint = fonts.textPaint(LunaColors.Plum, 8f, bold = true)
        val leftWidth = 125
        canvas.save()
        canvas.translate(MARGIN, FOOTER_RULE_Y + 8f)
        textLayout(left, leftPaint, leftWidth).draw(canvas)
        canvas.restore()

        val note = tr(
            fr = "Généré sur le téléphone de la patiente. Ce document n'est pas un diagnostic médical.",
            en = "Generated on the patient's phone. This document is not a medical diagnosis.",
            pl = "Wygenerowano na telefonie pacjentki. Ten dokument nie jest diagnozą medyczną.",
            uk = "Створено на телефоні пацієнтки. Цей документ не є медичним діагнозом.",
            es = "Generado en el teléfono de la paciente. Este documento no es un diagnóstico médico.",
            de = "Auf dem Smartphone der Patientin erstellt. Dieses Dokument ist keine medizinische Diagnose.",
        )
        val notePaint = fonts.textPaint(LunaColors.InkMuted, 8f)
        val noteWidth = (CONTENT_WIDTH - leftWidth - 12f).toInt()
        canvas.save()
        canvas.translate(MARGIN + leftWidth + 12f, FOOTER_RULE_Y + 8f)
        textLayout(note, notePaint, noteWidth, Layout.Alignment.ALIGN_OPPOSITE).draw(canvas)
        canvas.restore()
    }

    // --- Morceaux dessinables -----------------------------------------------------------------

    private fun textLayout(
        text: CharSequence,
        paint: TextPaint,
        width: Int,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
    ): StaticLayout = StaticLayout.Builder.obtain(text, 0, text.length, paint, width.coerceAtLeast(1))
        .setAlignment(alignment)
        .setLineSpacing(0f, LINE_SPACING)
        .setIncludePad(false)
        .build()

    private fun Canvas.drawLayout(layout: StaticLayout, x: Float, y: Float) {
        save()
        translate(x, y)
        layout.draw(this)
        restore()
    }

    /** Un morceau de page : jamais coupé, de hauteur connue avant d'être dessiné. */
    private abstract class Chunk(
        /** Espace laissé au-dessus, ignoré en haut de page. */
        val before: Float = 0f,
        val keepWithNext: Boolean = false,
    ) {
        abstract val height: Float
        open val isTableHeader: Boolean get() = false
        open val repeatsTableHeader: Boolean get() = false
        abstract fun draw(canvas: Canvas, x: Float, y: Float)
    }

    private class Fonts(private val regular: Typeface, private val bold: Typeface) {
        fun textPaint(color: androidx.compose.ui.graphics.Color, size: Float, bold: Boolean = false): TextPaint {
            val paint = TextPaint(Paint.ANTI_ALIAS_FLAG)
            paint.color = color.toArgb()
            paint.textSize = size
            paint.typeface = if (bold) this.bold else regular
            return paint
        }

        fun paint(color: androidx.compose.ui.graphics.Color, size: Float, bold: Boolean = false): Paint =
            textPaint(color, size, bold)

        companion object {
            /**
             * Atkinson Hyperlegible comme dans l'appli, ou la police sans empattement du système quand elle
             * ne couvre pas la langue (polonais, ukrainien : voir [reportNeedsSystemFont]).
             */
            fun load(context: Context, language: Language): Fonts {
                if (reportNeedsSystemFont(language)) {
                    return Fonts(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL), Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD))
                }
                fun font(id: Int, style: Int): Typeface = try {
                    ResourcesCompat.getFont(context, id) ?: Typeface.create(Typeface.SANS_SERIF, style)
                } catch (_: Exception) {
                    Typeface.create(Typeface.SANS_SERIF, style)
                }
                return Fonts(font(DesignR.font.atkinson_regular, Typeface.NORMAL), font(DesignR.font.atkinson_bold, Typeface.BOLD))
            }
        }
    }

    private data class SectionPalette(val accent: Int, val soft: Int)

    private fun sectionPalette(section: ReportSection?): SectionPalette {
        val p = reportSectionPalette(section)
        return SectionPalette(p.accent.toInt(), p.soft.toInt())
    }

    private class ReportLayout(private val fonts: Fonts) {
        private val width = CONTENT_WIDTH.toInt()
        private val body = fonts.textPaint(LunaColors.Ink, 10.5f)
        private val bodyBold = fonts.textPaint(LunaColors.Ink, 10.5f, bold = true)
        private val label = fonts.textPaint(LunaColors.InkMuted, 10.5f)
        private val small = fonts.textPaint(LunaColors.InkMuted, 8.5f)

        private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = LunaColors.Line.toArgb()
            strokeWidth = 0.75f
        }

        fun chunks(document: ReportDocument): List<Chunk> {
            val list = ArrayList<Chunk>()
            list.add(SituationChunk(document))
            if (document.categoryBadges.isNotEmpty()) {
                list.add(CategoriesChunk(document.categoriesTitle, document.categoryBadges))
            }
            document.earlyTracking?.let { list.add(EarlyTrackingChunk(it)) }
            document.parts.forEach { part ->
                val palette = sectionPalette(part.section)
                list.add(HeadingChunk(part.title, palette))
                part.blocks.forEach { block -> list.addAll(blockChunks(block, palette)) }
            }
            if (document.parts.size <= 2 && document.consultationNotesTitle.isNotBlank()) {
                list.add(ConsultationNotesChunk(document.consultationNotesTitle))
            }
            if (document.references.isNotEmpty()) {
                list.add(HeadingChunk(document.referencesTitle, sectionPalette(null), small = true))
                document.references.forEach { list.add(ParagraphChunk(it, small, 2f)) }
            }
            return list
        }

        private fun blockChunks(block: ReportBlock, palette: SectionPalette): List<Chunk> = when (block) {
            is ReportBlock.Rows -> block.rows.map { RowChunk(it.first, it.second, palette.accent) }
            is ReportBlock.Bullets -> block.items.map { BulletChunk(it, palette.accent) }
            is ReportBlock.Paragraph -> listOf(ParagraphChunk(block.text, if (block.muted) small else body, 4f))
            is ReportBlock.CycleTable -> tableChunks(block, palette)
            is ReportBlock.Bars -> block.bars.mapIndexed { i, bar -> BarChunk(bar, palette, emphasized = i == 1 && block.bars.size > 1) }
            is ReportBlock.Alerts -> block.items.mapIndexed { i, item -> AlertChunk(item, i == 0, i == block.items.lastIndex) }
        }

        private fun tableChunks(table: ReportBlock.CycleTable, palette: SectionPalette): List<Chunk> {
            val columns = floatArrayOf(0.27f, 0.22f, 0.22f, 0.29f).map { it * CONTENT_WIDTH }
            val header = TableRowChunk(table.headers, columns, palette, header = true, striped = false)
            val rows = table.rows.mapIndexed { i, row -> TableRowChunk(row, columns, palette, header = false, striped = i % 2 == 1, repeatHeader = true) }
            val all = ArrayList<Chunk>()
            all.add(header)
            all.addAll(rows)
            table.summary?.let { all.add(ParagraphChunk(it, small, 6f)) }
            return all
        }

        // Titre de rubrique : bandeau doux de la catégorie, filet gauche et pastille d'accent.
        private inner class HeadingChunk(text: String, private val palette: SectionPalette, private val small: Boolean = false) :
            Chunk(before = if (small) 14f else 15f, keepWithNext = true) {
            private val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = palette.accent
                textSize = if (small) 9.8f else 11.8f
                typeface = fonts.textPaint(LunaColors.Plum, 11.8f, bold = true).typeface
            }
            private val layout = textLayout(text, paint, width - 34)
            private val padY = if (small) 4f else 6f
            override val height = layout.height + padY * 2 + 5f

            override fun draw(canvas: Canvas, x: Float, y: Float) {
                val boxHeight = layout.height + padY * 2
                val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.soft }
                canvas.drawRoundRect(RectF(x, y, x + width, y + boxHeight), 5f, 5f, bg)

                val bar = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.accent }
                canvas.drawRoundRect(RectF(x, y, x + 3.5f, y + boxHeight), 2f, 2f, bar)

                val dotRadius = if (small) 2.5f else 3.2f
                canvas.drawCircle(x + 13f, y + boxHeight / 2f, dotRadius, bar)
                canvas.drawLayout(layout, x + 22f, y + padY)
            }
        }

        private inner class ParagraphChunk(text: String, paint: TextPaint, private val gap: Float) :
            Chunk() {
            private val layout = textLayout(text, paint, width)
            override val height = layout.height + gap
            override fun draw(canvas: Canvas, x: Float, y: Float) = canvas.drawLayout(layout, x, y)
        }

        private inner class BulletChunk(text: String, private val accentColor: Int) : Chunk() {
            private val layout = textLayout(text, body, width - 16)
            override val height = layout.height + 4f
            override fun draw(canvas: Canvas, x: Float, y: Float) {
                val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accentColor }
                canvas.drawCircle(x + 3f, y + body.textSize * 0.62f, 2f, dot)
                canvas.drawLayout(layout, x + 14f, y)
            }
        }

        // Ligne libellé à gauche (avec puce couleur de catégorie), valeur à droite, filet fin dessous.
        private inner class RowChunk(labelText: String, valueText: String, private val accentColor: Int) : Chunk() {
            private val labelWidth = 170
            private val labelLayout = textLayout(labelText, label, labelWidth - 12)
            private val valueLayout = textLayout(valueText, bodyBold, width - labelWidth - 14)
            override val height = maxOf(labelLayout.height, valueLayout.height) + 9f
            override fun draw(canvas: Canvas, x: Float, y: Float) {
                val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accentColor }
                canvas.drawCircle(x + 3.5f, y + 3f + label.textSize * 0.55f, 2.1f, dot)
                canvas.drawLayout(labelLayout, x + 11f, y + 3f)
                canvas.drawLayout(valueLayout, x + labelWidth + 14f, y + 3f)
                canvas.drawLine(x, y + height - 1f, x + width, y + height - 1f, line)
            }
        }

        // Libellé, barre proportionnelle sur 90 jours dans la couleur de la catégorie, nombre de jours à droite.
        private inner class BarChunk(
            private val bar: ReportBar,
            private val palette: SectionPalette,
            private val emphasized: Boolean,
        ) : Chunk() {
            private val labelWidth = 235
            private val trackLeft = 245f
            private val trackWidth = 120f
            private val valueWidth = 95
            private val labelLayout = textLayout(bar.label, body, labelWidth)
            private val valueLayout = textLayout(bar.valueText, bodyBold, valueWidth, Layout.Alignment.ALIGN_OPPOSITE)
            override val height = maxOf(labelLayout.height, valueLayout.height) + 9f
            override fun draw(canvas: Canvas, x: Float, y: Float) {
                canvas.drawLayout(labelLayout, x, y + 3f)
                val centerY = y + 3f + body.textSize * 0.6f
                val track = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.soft }
                canvas.drawRoundRect(RectF(x + trackLeft, centerY - 4f, x + trackLeft + trackWidth, centerY + 4f), 4f, 4f, track)
                val ratio = (bar.days.toFloat() / ReportDocument.PERIOD_DAYS).coerceIn(0f, 1f)
                if (ratio > 0f) {
                    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = if (emphasized) 0xFF8F2D2C.toInt() else palette.accent
                    }
                    val right = x + trackLeft + maxOf(trackWidth * ratio, 8f)
                    canvas.drawRoundRect(RectF(x + trackLeft, centerY - 4f, right, centerY + 4f), 4f, 4f, fill)
                }
                canvas.drawLayout(valueLayout, x + width - valueWidth, y + 3f)
                canvas.drawLine(x, y + height - 1f, x + width, y + height - 1f, line)
            }
        }

        // Encadré d'alerte : fond ClaySoft, bordure Clay, un morceau par alerte pour pouvoir couper entre deux.
        private inner class AlertChunk(item: ReportAlert, private val first: Boolean, private val last: Boolean) :
            Chunk(before = if (first) 2f else 0f) {
            private val titleLayout = textLayout(item.title, bodyBold, width - 28)
            private val messageLayout = textLayout(item.message, body, width - 28)
            override val height = 9f + titleLayout.height + 2f + messageLayout.height + 9f
            override fun draw(canvas: Canvas, x: Float, y: Float) {
                val fill = Paint(Paint.ANTI_ALIAS_FLAG)
                fill.color = LunaColors.ClaySoft.toArgb()
                canvas.drawRect(x, y, x + width, y + height, fill)
                val border = Paint(Paint.ANTI_ALIAS_FLAG)
                border.color = LunaColors.Clay.toArgb()
                border.strokeWidth = 1f
                canvas.drawLine(x, y, x, y + height, border)
                canvas.drawLine(x + width, y, x + width, y + height, border)
                if (first) canvas.drawLine(x, y, x + width, y, border)
                if (last) canvas.drawLine(x, y + height, x + width, y + height, border)
                canvas.drawRect(x, y, x + 4f, y + height, border)
                canvas.drawLayout(titleLayout, x + 16f, y + 9f)
                canvas.drawLayout(messageLayout, x + 16f, y + 9f + titleLayout.height + 2f)
            }
        }

        // Tableau des cycles : en-tête aux couleurs de la catégorie, lignes alternées très légères.
        private inner class TableRowChunk(
            cells: List<String>,
            private val columns: List<Float>,
            private val palette: SectionPalette,
            private val header: Boolean,
            private val striped: Boolean,
            private val repeatHeader: Boolean = false,
        ) : Chunk(keepWithNext = header) {
            private val paint = if (header) {
                TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = palette.accent
                    textSize = 9f
                    typeface = bodyBold.typeface
                }
            } else {
                body
            }
            private val layouts = cells.mapIndexed { i, text -> textLayout(text, paint, (columns[i] - 16f).toInt()) }
            override val height = layouts.maxOf { it.height } + if (header) 12f else 10f
            override val isTableHeader: Boolean get() = header
            override val repeatsTableHeader: Boolean get() = repeatHeader

            override fun draw(canvas: Canvas, x: Float, y: Float) {
                if (header || striped) {
                    val fill = Paint(Paint.ANTI_ALIAS_FLAG)
                    fill.color = if (header) palette.soft else 0xFFFAF8FC.toInt()
                    canvas.drawRect(x, y, x + width, y + height, fill)
                }
                var left = x
                layouts.forEachIndexed { i, layout ->
                    canvas.drawLayout(layout, left + 8f, y + (if (header) 6f else 5f))
                    left += columns[i]
                }
                if (header) {
                    val headerRule = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = palette.accent
                        strokeWidth = 1.1f
                    }
                    canvas.drawLine(x, y + height, x + width, y + height, headerRule)
                } else {
                    canvas.drawLine(x, y + height, x + width, y + height, line)
                }
            }
        }

        // Situation actuelle : encadré PlumSoft avec les chiffres clés en tuiles et barre d'accent colorée.
        private inner class SituationChunk(document: ReportDocument) : Chunk() {
            private val padding = 14f
            private val gap = 10f
            private val tileAccents = intArrayOf(
                LunaColors.Plum.toArgb(),
                LunaColors.Clay.toArgb(),
                0xFF5B4B8A.toInt(),
                0xFF2F6F62.toInt(),
            )
            private val titleLayout = textLayout(document.situationTitle, fonts.textPaint(LunaColors.Plum, 12f, bold = true), width - 2 * padding.toInt())
            private val tileWidth = (width - 2 * padding - gap * (document.tiles.size - 1)) / document.tiles.size
            private val valuePaints = document.tiles.mapIndexed { i, tile ->
                val color = tileAccents[i % tileAccents.size]
                val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = color
                    textSize = 18f
                    typeface = bodyBold.typeface
                }
                while (paint.measureText(tile.value) > tileWidth - 16f && paint.textSize > 9f) paint.textSize -= 0.5f
                paint
            }
            private val labelLayouts = document.tiles.map { tile ->
                val text = if (tile.detail != null) tile.label + "\n" + tile.detail else tile.label
                textLayout(text, small, (tileWidth - 16f).toInt())
            }
            private val tileHeight = 12f + 24f + 3f + (labelLayouts.maxOfOrNull { it.height } ?: 0) + 10f
            private val tiles = document.tiles
            override val height = padding + titleLayout.height + 8f + tileHeight + padding + 4f

            override fun draw(canvas: Canvas, x: Float, y: Float) {
                val boxHeight = height - 4f
                val frame = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = LunaColors.PlumSoft.toArgb() }
                val frameRect = RectF(x, y, x + width, y + boxHeight)
                canvas.drawRoundRect(frameRect, 8f, 8f, frame)
                val frameStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFFDCC8D5.toInt()
                    style = Paint.Style.STROKE
                    strokeWidth = 0.8f
                }
                canvas.drawRoundRect(frameRect, 8f, 8f, frameStroke)

                canvas.drawLayout(titleLayout, x + padding, y + padding)
                val tileTop = y + padding + titleLayout.height + 8f
                val tileFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() }
                val tileStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = LunaColors.Line.toArgb()
                    style = Paint.Style.STROKE
                    strokeWidth = 0.7f
                }
                tiles.forEachIndexed { i, tile ->
                    val left = x + padding + i * (tileWidth + gap)
                    val tileRect = RectF(left, tileTop, left + tileWidth, tileTop + tileHeight)
                    canvas.drawRoundRect(tileRect, 6f, 6f, tileFill)
                    canvas.drawRoundRect(tileRect, 6f, 6f, tileStroke)

                    val accent = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = tileAccents[i % tileAccents.size] }
                    canvas.drawRoundRect(RectF(left + 1f, tileTop + 1f, left + tileWidth - 1f, tileTop + 4f), 3f, 3f, accent)

                    canvas.drawText(tile.value, left + 8f, tileTop + 12f + 18f, valuePaints[i])
                    canvas.drawLayout(labelLayouts[i], left + 8f, tileTop + 12f + 24f + 3f)
                }
            }
        }

        // Périmètre du bilan : badges colorés pour toutes les catégories cochées par la patiente.
        private inner class CategoriesChunk(title: String, private val badges: List<ReportCategoryBadge>) :
            Chunk(before = 8f) {
            private val titlePaint = fonts.textPaint(LunaColors.InkMuted, 8.5f, bold = true)
            private val titleLayout = textLayout(title, titlePaint, width)
            private val chipPaint = fonts.textPaint(LunaColors.Ink, 8.2f, bold = true)
            private val chipMutedPaint = fonts.textPaint(LunaColors.InkMuted, 8.2f)
            private val chipHeight = 18f
            private val gapX = 6f
            private val gapY = 5f
            private val placedChips: List<PlacedChip>

            init {
                var cx = 0f
                var cy = titleLayout.height + 6f
                val items = ArrayList<PlacedChip>(badges.size)
                badges.forEach { badge ->
                    val paint = if (badge.hasData) chipPaint else chipMutedPaint
                    val chipW = (18f + paint.measureText(badge.label) + 10f).coerceAtMost(width.toFloat())
                    if (cx > 0f && cx + chipW > width) {
                        cx = 0f
                        cy += chipHeight + gapY
                    }
                    items.add(PlacedChip(badge, cx, cy, chipW))
                    cx += chipW + gapX
                }
                placedChips = items
            }

            override val height = (placedChips.lastOrNull()?.y ?: 0f) + chipHeight + 6f

            override fun draw(canvas: Canvas, x: Float, y: Float) {
                canvas.drawLayout(titleLayout, x, y)
                placedChips.forEach { item ->
                    val palette = sectionPalette(item.badge.section)
                    val rect = RectF(x + item.x, y + item.y, x + item.x + item.w, y + item.y + chipHeight)
                    val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = if (item.badge.hasData) palette.soft else 0xFFFAF7F8.toInt()
                    }
                    val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = if (item.badge.hasData) palette.accent else LunaColors.Line.toArgb()
                        style = Paint.Style.STROKE
                        strokeWidth = 0.75f
                    }
                    canvas.drawRoundRect(rect, 9f, 9f, bg)
                    canvas.drawRoundRect(rect, 9f, 9f, border)

                    val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = if (item.badge.hasData) palette.accent else LunaColors.Outline.toArgb()
                        style = if (item.badge.hasData) Paint.Style.FILL else Paint.Style.STROKE
                        strokeWidth = 1f
                    }
                    canvas.drawCircle(rect.left + 9f, rect.centerY(), 2.6f, dot)

                    val textPaint = TextPaint(if (item.badge.hasData) chipPaint else chipMutedPaint).apply {
                        color = if (item.badge.hasData) palette.accent else LunaColors.InkMuted.toArgb()
                    }
                    canvas.drawText(item.badge.label, rect.left + 16f, rect.centerY() + 3f, textPaint)
                }
            }
        }

        private class PlacedChip(val badge: ReportCategoryBadge, val x: Float, val y: Float, val w: Float)

        // Encadré affiché en début de suivi (aucun cycle complet ou aucune donnée sur la période).
        private inner class EarlyTrackingChunk(info: ReportEarlyTracking) : Chunk(before = 10f) {
            private val pad = 12f
            private val titleLayout = textLayout(info.title, fonts.textPaint(LunaColors.Plum, 10.5f, bold = true), width - 2 * pad.toInt() - 8)
            private val msgLayout = textLayout(info.message, body, width - 2 * pad.toInt() - 8)
            override val height = pad + titleLayout.height + 4f + msgLayout.height + pad + 4f

            override fun draw(canvas: Canvas, x: Float, y: Float) {
                val boxHeight = height - 4f
                val rect = RectF(x, y, x + width, y + boxHeight)
                val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = LunaColors.Cream.toArgb() }
                val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = LunaColors.Line.toArgb()
                    style = Paint.Style.STROKE
                    strokeWidth = 0.8f
                }
                canvas.drawRoundRect(rect, 6f, 6f, bg)
                canvas.drawRoundRect(rect, 6f, 6f, border)

                val bar = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = LunaColors.Plum.toArgb() }
                canvas.drawRoundRect(RectF(x, y, x + 3.5f, y + boxHeight), 2f, 2f, bar)

                canvas.drawLayout(titleLayout, x + pad + 4f, y + pad)
                canvas.drawLayout(msgLayout, x + pad + 4f, y + pad + titleLayout.height + 4f)
            }
        }

        // Zone de notes manuscrites pour le praticien lorsque le bilan est court (début de suivi).
        private inner class ConsultationNotesChunk(title: String) : Chunk(before = 14f) {
            private val pad = 12f
            private val linesCount = 4
            private val lineGap = 24f
            private val titleLayout = textLayout(title, fonts.textPaint(LunaColors.Plum, 9.5f, bold = true), width - 2 * pad.toInt())
            override val height = pad + titleLayout.height + 8f + linesCount * lineGap + pad

            override fun draw(canvas: Canvas, x: Float, y: Float) {
                val rect = RectF(x, y, x + width, y + height)
                val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = LunaColors.Paper.toArgb() }
                val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = LunaColors.Outline.toArgb()
                    style = Paint.Style.STROKE
                    strokeWidth = 0.75f
                }
                canvas.drawRoundRect(rect, 6f, 6f, bg)
                canvas.drawRoundRect(rect, 6f, 6f, border)

                canvas.drawLayout(titleLayout, x + pad, y + pad)
                val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = LunaColors.Line.toArgb()
                    strokeWidth = 0.8f
                }
                var ly = y + pad + titleLayout.height + lineGap
                repeat(linesCount) {
                    canvas.drawLine(x + pad, ly, x + width - pad, ly, linePaint)
                    ly += lineGap
                }
            }
        }
    }
}
