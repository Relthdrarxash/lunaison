@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package fr.lunaison.ui

import fr.lunaison.core.model.Language
import fr.lunaison.core.model.tr
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.datetime.LocalDate
import platform.CoreGraphics.CGContextMoveToPoint
import platform.CoreGraphics.CGContextAddLineToPoint
import platform.CoreGraphics.CGContextSetLineWidth
import platform.CoreGraphics.CGContextSetStrokeColorWithColor
import platform.CoreGraphics.CGContextStrokePath
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.create
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.NSMutableParagraphStyle
import platform.UIKit.NSParagraphStyleAttributeName
import platform.UIKit.NSStringDrawingUsesFontLeading
import platform.UIKit.NSStringDrawingUsesLineFragmentOrigin
import platform.UIKit.NSTextAlignment
import platform.UIKit.NSTextAlignmentLeft
import platform.UIKit.NSTextAlignmentRight
import platform.UIKit.UIBezierPath
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIGraphicsGetCurrentContext
import platform.UIKit.UIGraphicsPDFRenderer
import platform.UIKit.UIGraphicsPDFRendererFormat
import platform.UIKit.boundingRectWithSize
import platform.UIKit.drawWithRect
import kotlin.math.ceil

internal actual fun formatReportFullDate(date: LocalDate): String =
    if (Language.current() == Language.EN) {
        "${Format.dayMonth(date)}, ${date.year}"
    } else {
        "${Format.dayMonth(date)} ${date.year}"
    }

internal actual fun formatReportShortDate(date: LocalDate): String = Format.dayMonth(date)

/**
 * Génère le PDF A4 du bilan de santé sur iOS avec le même graphisme, les mêmes couleurs
 * par catégorie et la même pagination automatique que la version Android.
 */
internal object HealthReportPdfIos {
    private const val PAGE_WIDTH = 595.0
    private const val PAGE_HEIGHT = 842.0
    private const val MARGIN = 48.0
    private const val CONTENT_WIDTH = PAGE_WIDTH - 2.0 * MARGIN
    private const val HEADER_HEIGHT = 96.0
    private const val FIRST_PAGE_TOP = HEADER_HEIGHT + 18.0
    private const val PAGE_TOP = MARGIN + 22.0
    private const val CONTENT_BOTTOM = PAGE_HEIGHT - 66.0
    private const val FOOTER_RULE_Y = PAGE_HEIGHT - 50.0
    private const val LINE_SPACING = 1.22

    private val PLUM = 0xFF4A2C4FL.toUIColor()
    private val PLUM_SOFT = 0xFFF1E2EAL.toUIColor()
    private val CLAY = 0xFFA8473FL.toUIColor()
    private val CLAY_SOFT = 0xFFF6DCD6L.toUIColor()
    private val CREAM = 0xFFFBF5F2L.toUIColor()
    private val PAPER = 0xFFFFFCFAL.toUIColor()
    private val INK = 0xFF2A1F2DL.toUIColor()
    private val INK_MUTED = 0xFF5E5162L.toUIColor()
    private val LINE = 0xFFE7DCE0L.toUIColor()
    private val OUTLINE = 0xFFB9A8B6L.toUIColor()
    private val MOON = 0xFFE8C9DAL.toUIColor()
    private val CLAY_NIGHT = 0xFFE39A8FL.toUIColor()

    fun render(document: ReportDocument): NSData {
        val layout = LayoutBuilder()
        val chunks = layout.buildChunks(document)
        val pages = paginate(chunks)
        val renderer = UIGraphicsPDFRenderer(
            bounds = CGRectMake(0.0, 0.0, PAGE_WIDTH, PAGE_HEIGHT),
            format = UIGraphicsPDFRendererFormat(),
        )
        return renderer.PDFDataWithActions { pdfCtx ->
            pages.forEachIndexed { index, placed ->
                pdfCtx?.beginPage()
                if (index == 0) drawHeader(document) else drawRunningHeader(document)
                placed.forEach { it.chunk.draw(MARGIN, it.y) }
                drawFooter(index + 1, pages.size)
            }
        }
    }

    // --- Pagination ---------------------------------------------------------------------------

    private class Placed(val chunk: PdfChunk, val y: Double)

    private fun paginate(chunks: List<PdfChunk>): List<List<Placed>> {
        val pages = ArrayList<MutableList<Placed>>()
        var current = ArrayList<Placed>()
        var y = FIRST_PAGE_TOP
        var top = FIRST_PAGE_TOP
        var lastTableHeader: PdfChunk? = null

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
            val before = if (y == top) 0.0 else chunk.before
            current.add(Placed(chunk, y + before))
            y += before + chunk.height
            index++
        }
        pages.add(current)
        return pages
    }

    // --- En-têtes et pied de page -------------------------------------------------------------

    private fun drawHeader(document: ReportDocument) {
        fillRect(0.0, 0.0, PAGE_WIDTH, HEADER_HEIGHT, PLUM)

        // Halo lunaire discret à droite du bandeau.
        val ctx = UIGraphicsGetCurrentContext()
        if (ctx != null) {
            platform.CoreGraphics.CGContextSaveGState(ctx)
            platform.CoreGraphics.CGContextClipToRect(ctx, CGRectMake(0.0, 0.0, PAGE_WIDTH, HEADER_HEIGHT))
            fillCircle(PAGE_WIDTH - 48.0, HEADER_HEIGHT * 0.35, 88.0, 0xFF56345CL.toUIColor())
            fillCircle(PAGE_WIDTH - 24.0, HEADER_HEIGHT * 0.25, 52.0, 0xFF603B67L.toUIColor())
            platform.CoreGraphics.CGContextRestoreGState(ctx)
        }

        fillRect(0.0, HEADER_HEIGHT - 3.0, PAGE_WIDTH, 3.0, CLAY_NIGHT)

        val logoSize = 58.0
        val logoLeft = MARGIN
        val logoTop = (HEADER_HEIGHT - 3.0 - logoSize) / 2.0
        drawLogo(logoLeft, logoTop, logoSize)

        val textLeft = logoLeft + logoSize + 16.0
        val titleWidth = PAGE_WIDTH - MARGIN - textLeft - 140.0
        drawText(document.title, textLeft, HEADER_HEIGHT / 2.0 - 24.0, titleWidth, UIFont.boldSystemFontOfSize(21.0), CREAM)
        drawText(document.subtitle, textLeft, HEADER_HEIGHT / 2.0 + 4.0, titleWidth, UIFont.systemFontOfSize(10.5), MOON)

        val badgeFont = UIFont.boldSystemFontOfSize(7.5)
        val badgeTextW = measureWidth(document.confidentialBadge, badgeFont)
        val badgePadX = 8.0
        val badgeH = 16.0
        val badgeW = badgeTextW + 2.0 * badgePadX
        val badgeX = PAGE_WIDTH - MARGIN - badgeW
        val badgeY = 22.0
        fillAndStrokeRoundedRect(badgeX, badgeY, badgeW, badgeH, 8.0, 0xFF633E6AL.toUIColor(), 0xFF8A6291L.toUIColor(), 0.8)
        drawText(document.confidentialBadge, badgeX + badgePadX, badgeY + 3.5, badgeTextW + 2.0, badgeFont, CLAY_NIGHT)

        drawText(
            document.generatedLabel,
            PAGE_WIDTH - MARGIN - 200.0,
            badgeY + badgeH + 8.0,
            200.0,
            UIFont.systemFontOfSize(9.0),
            CREAM,
            NSTextAlignmentRight,
        )
    }

    private fun drawRunningHeader(document: ReportDocument) {
        val y = 26.0
        drawLogo(MARGIN, y - 3.0, 18.0)
        drawText("Lunaison · ${document.title}", MARGIN + 24.0, y, 280.0, UIFont.boldSystemFontOfSize(9.0), PLUM)
        drawText(
            document.generatedLabel,
            PAGE_WIDTH - MARGIN - 200.0,
            y,
            200.0,
            UIFont.systemFontOfSize(8.5),
            INK_MUTED,
            NSTextAlignmentRight,
        )
        drawLine(MARGIN, y + 18.0, PAGE_WIDTH - MARGIN, y + 18.0, PLUM_SOFT, 1.2)
    }

    private fun drawLogo(left: Double, top: Double, size: Double) {
        val tileColor = 0xFF5E3A64L.toUIColor()
        val radius = size * 0.26
        if (size >= 32.0) {
            fillAndStrokeRoundedRect(left, top, size, size, radius, tileColor, 0xFF825B88L.toUIColor(), 1.0)
        } else {
            fillRoundedRect(left, top, size, size, radius, tileColor)
        }
        // Croissant de lune centré + étoile ClayNight, proportionnels au logo Android.
        val s = (size / 108.0) * 1.25
        val cx = left + size / 2.0
        val cy = top + size / 2.0
        fun mapX(vx: Double) = cx + (vx - 54.0) * s
        fun mapY(vy: Double) = cy + (vy - 54.0) * s

        fillCircle(mapX(51.5), mapY(56.0), 23.0 * s, CREAM)
        fillCircle(mapX(62.0), mapY(44.5), 18.5 * s, tileColor)
        fillCircle(mapX(71.0), mapY(38.0), 3.0 * s, CLAY_NIGHT)
    }

    private fun drawFooter(page: Int, total: Int) {
        drawLine(MARGIN, FOOTER_RULE_Y, MARGIN + 36.0, FOOTER_RULE_Y, CLAY, 1.5)
        drawLine(MARGIN + 36.0, FOOTER_RULE_Y, PAGE_WIDTH - MARGIN, FOOTER_RULE_Y, LINE, 0.75)

        val leftText = tr(
            fr = "Lunaison · page $page / $total",
            en = "Lunaison · page $page / $total",
            pl = "Lunaison · strona $page / $total",
            uk = "Lunaison · стор. $page / $total",
            es = "Lunaison · página $page / $total",
            de = "Lunaison · Seite $page / $total",
        )
        val leftW = 125.0
        drawText(leftText, MARGIN, FOOTER_RULE_Y + 8.0, leftW, UIFont.boldSystemFontOfSize(8.0), PLUM)

        val noteText = tr(
            fr = "Généré sur le téléphone de la patiente. Ce document n'est pas un diagnostic médical.",
            en = "Generated on the patient's phone. This document is not a medical diagnosis.",
            pl = "Wygenerowano na telefonie pacjentki. Ten dokument nie jest diagnozą medyczną.",
            uk = "Створено на телефоні пацієнтки. Цей документ не є медичним діагнозом.",
            es = "Generado en el teléfono de la paciente. Este documento no es un diagnóstico médico.",
            de = "Auf dem Smartphone der Patientin erstellt. Dieses Dokument ist keine medizinische Diagnose.",
        )
        val noteW = CONTENT_WIDTH - leftW - 12.0
        drawText(
            noteText,
            MARGIN + leftW + 12.0,
            FOOTER_RULE_Y + 8.0,
            noteW,
            UIFont.systemFontOfSize(8.0),
            INK_MUTED,
            NSTextAlignmentRight,
        )
    }

    // --- Construction et mise en page des blocs -----------------------------------------------

    private abstract class PdfChunk(
        val before: Double = 0.0,
        val keepWithNext: Boolean = false,
    ) {
        abstract val height: Double
        open val isTableHeader: Boolean get() = false
        open val repeatsTableHeader: Boolean get() = false
        abstract fun draw(x: Double, y: Double)
    }

    private class LayoutBuilder {
        private val bodyFont = UIFont.systemFontOfSize(10.5)
        private val bodyBoldFont = UIFont.boldSystemFontOfSize(10.5)
        private val smallFont = UIFont.systemFontOfSize(8.5)

        fun buildChunks(document: ReportDocument): List<PdfChunk> {
            val list = ArrayList<PdfChunk>()
            list.add(SituationChunk(document))
            if (document.categoryBadges.isNotEmpty()) {
                list.add(CategoriesChunk(document.categoriesTitle, document.categoryBadges))
            }
            document.earlyTracking?.let { list.add(EarlyTrackingChunk(it)) }
            document.parts.forEach { part ->
                val palette = reportSectionPalette(part.section)
                list.add(HeadingChunk(part.title, palette))
                part.blocks.forEach { block -> list.addAll(blockChunks(block, palette)) }
            }
            if (document.parts.size <= 2 && document.consultationNotesTitle.isNotBlank()) {
                list.add(ConsultationNotesChunk(document.consultationNotesTitle))
            }
            if (document.references.isNotEmpty()) {
                list.add(HeadingChunk(document.referencesTitle, reportSectionPalette(null), small = true))
                document.references.forEach { list.add(ParagraphChunk(it, smallFont, INK_MUTED, 2.0)) }
            }
            return list
        }

        private fun blockChunks(block: ReportBlock, palette: ReportSectionPalette): List<PdfChunk> = when (block) {
            is ReportBlock.Rows -> block.rows.map { RowChunk(it.first, it.second, palette.accent.toUIColor()) }
            is ReportBlock.Bullets -> block.items.map { BulletChunk(it, palette.accent.toUIColor()) }
            is ReportBlock.Paragraph -> listOf(
                ParagraphChunk(block.text, if (block.muted) smallFont else bodyFont, if (block.muted) INK_MUTED else INK, 4.0),
            )
            is ReportBlock.CycleTable -> tableChunks(block, palette)
            is ReportBlock.Bars -> block.bars.mapIndexed { i, bar -> BarChunk(bar, palette, emphasized = i == 1 && block.bars.size > 1) }
            is ReportBlock.Alerts -> block.items.mapIndexed { i, item -> AlertChunk(item, i == 0, i == block.items.lastIndex) }
        }

        private fun tableChunks(table: ReportBlock.CycleTable, palette: ReportSectionPalette): List<PdfChunk> {
            val columns = listOf(0.27, 0.22, 0.22, 0.29).map { it * CONTENT_WIDTH }
            val header = TableRowChunk(table.headers, columns, palette, header = true, striped = false)
            val rows = table.rows.mapIndexed { i, row ->
                TableRowChunk(row, columns, palette, header = false, striped = i % 2 == 1, repeatHeader = true)
            }
            val all = ArrayList<PdfChunk>()
            all.add(header)
            all.addAll(rows)
            table.summary?.let { all.add(ParagraphChunk(it, smallFont, INK_MUTED, 6.0)) }
            return all
        }

        private inner class HeadingChunk(
            private val text: String,
            private val palette: ReportSectionPalette,
            private val small: Boolean = false,
        ) : PdfChunk(before = if (small) 14.0 else 15.0, keepWithNext = true) {
            private val font = UIFont.boldSystemFontOfSize(if (small) 9.8 else 11.8)
            private val accentColor = palette.accent.toUIColor()
            private val softColor = palette.soft.toUIColor()
            private val textH = measureHeight(text, CONTENT_WIDTH - 34.0, font)
            private val padY = if (small) 4.0 else 6.0
            override val height = textH + padY * 2.0 + 5.0

            override fun draw(x: Double, y: Double) {
                val boxH = textH + padY * 2.0
                fillRoundedRect(x, y, CONTENT_WIDTH, boxH, 5.0, softColor)
                fillRoundedRect(x, y, 3.5, boxH, 2.0, accentColor)
                fillCircle(x + 13.0, y + boxH / 2.0, if (small) 2.5 else 3.2, accentColor)
                drawText(text, x + 22.0, y + padY, CONTENT_WIDTH - 34.0, font, accentColor)
            }
        }

        private inner class ParagraphChunk(
            private val text: String,
            private val font: UIFont,
            private val color: UIColor,
            private val gap: Double,
        ) : PdfChunk() {
            private val textH = measureHeight(text, CONTENT_WIDTH, font)
            override val height = textH + gap
            override fun draw(x: Double, y: Double) = drawText(text, x, y, CONTENT_WIDTH, font, color)
        }

        private inner class BulletChunk(private val text: String, private val accentColor: UIColor) : PdfChunk() {
            private val textH = measureHeight(text, CONTENT_WIDTH - 16.0, bodyFont)
            override val height = textH + 4.0
            override fun draw(x: Double, y: Double) {
                fillCircle(x + 3.0, y + bodyFont.pointSize * 0.62, 2.0, accentColor)
                drawText(text, x + 14.0, y, CONTENT_WIDTH - 16.0, bodyFont, INK)
            }
        }

        private inner class RowChunk(
            private val labelText: String,
            private val valueText: String,
            private val accentColor: UIColor,
        ) : PdfChunk() {
            private val labelWidth = 170.0
            private val labelH = measureHeight(labelText, labelWidth - 12.0, bodyFont)
            private val valueW = CONTENT_WIDTH - labelWidth - 14.0
            private val valueH = measureHeight(valueText, valueW, bodyBoldFont)
            override val height = maxOf(labelH, valueH) + 9.0

            override fun draw(x: Double, y: Double) {
                fillCircle(x + 3.5, y + 3.0 + bodyFont.pointSize * 0.55, 2.1, accentColor)
                drawText(labelText, x + 11.0, y + 3.0, labelWidth - 12.0, bodyFont, INK_MUTED)
                drawText(valueText, x + labelWidth + 14.0, y + 3.0, valueW, bodyBoldFont, INK)
                drawLine(x, y + height - 1.0, x + CONTENT_WIDTH, y + height - 1.0, LINE, 0.75)
            }
        }

        private inner class BarChunk(
            private val bar: ReportBar,
            private val palette: ReportSectionPalette,
            private val emphasized: Boolean,
        ) : PdfChunk() {
            private val labelWidth = 235.0
            private val trackLeft = 245.0
            private val trackWidth = 120.0
            private val valueWidth = 95.0
            private val labelH = measureHeight(bar.label, labelWidth, bodyFont)
            private val valueH = measureHeight(bar.valueText, valueWidth, bodyBoldFont)
            override val height = maxOf(labelH, valueH) + 9.0

            override fun draw(x: Double, y: Double) {
                drawText(bar.label, x, y + 3.0, labelWidth, bodyFont, INK)
                val centerY = y + 3.0 + bodyFont.pointSize * 0.6
                fillRoundedRect(x + trackLeft, centerY - 4.0, trackWidth, 8.0, 4.0, palette.soft.toUIColor())
                val ratio = (bar.days.toDouble() / ReportDocument.PERIOD_DAYS).coerceIn(0.0, 1.0)
                if (ratio > 0.0) {
                    val fillW = maxOf(trackWidth * ratio, 8.0)
                    val color = if (emphasized) 0xFF8F2D2CL.toUIColor() else palette.accent.toUIColor()
                    fillRoundedRect(x + trackLeft, centerY - 4.0, fillW, 8.0, 4.0, color)
                }
                drawText(bar.valueText, x + CONTENT_WIDTH - valueWidth, y + 3.0, valueWidth, bodyBoldFont, INK, NSTextAlignmentRight)
                drawLine(x, y + height - 1.0, x + CONTENT_WIDTH, y + height - 1.0, LINE, 0.75)
            }
        }

        private inner class AlertChunk(
            private val item: ReportAlert,
            private val first: Boolean,
            private val last: Boolean,
        ) : PdfChunk(before = if (first) 2.0 else 0.0) {
            private val titleH = measureHeight(item.title, CONTENT_WIDTH - 28.0, bodyBoldFont)
            private val msgH = measureHeight(item.message, CONTENT_WIDTH - 28.0, bodyFont)
            override val height = 9.0 + titleH + 2.0 + msgH + 9.0

            override fun draw(x: Double, y: Double) {
                fillRect(x, y, CONTENT_WIDTH, height, CLAY_SOFT)
                drawLine(x, y, x, y + height, CLAY, 1.0)
                drawLine(x + CONTENT_WIDTH, y, x + CONTENT_WIDTH, y + height, CLAY, 1.0)
                if (first) drawLine(x, y, x + CONTENT_WIDTH, y, CLAY, 1.0)
                if (last) drawLine(x, y + height, x + CONTENT_WIDTH, y + height, CLAY, 1.0)
                fillRect(x, y, 4.0, height, CLAY)
                drawText(item.title, x + 16.0, y + 9.0, CONTENT_WIDTH - 28.0, bodyBoldFont, INK)
                drawText(item.message, x + 16.0, y + 9.0 + titleH + 2.0, CONTENT_WIDTH - 28.0, bodyFont, INK)
            }
        }

        private inner class TableRowChunk(
            private val cells: List<String>,
            private val columns: List<Double>,
            private val palette: ReportSectionPalette,
            private val header: Boolean,
            private val striped: Boolean,
            private val repeatHeader: Boolean = false,
        ) : PdfChunk(keepWithNext = header) {
            private val font = if (header) UIFont.boldSystemFontOfSize(9.0) else bodyFont
            private val color = if (header) palette.accent.toUIColor() else INK
            private val cellHeights = cells.mapIndexed { i, text -> measureHeight(text, columns[i] - 16.0, font) }
            override val height = (cellHeights.maxOrNull() ?: 12.0) + if (header) 12.0 else 10.0
            override val isTableHeader: Boolean get() = header
            override val repeatsTableHeader: Boolean get() = repeatHeader

            override fun draw(x: Double, y: Double) {
                if (header || striped) {
                    val bg = if (header) palette.soft.toUIColor() else 0xFFFAF8FCL.toUIColor()
                    fillRect(x, y, CONTENT_WIDTH, height, bg)
                }
                var left = x
                cells.forEachIndexed { i, text ->
                    drawText(text, left + 8.0, y + if (header) 6.0 else 5.0, columns[i] - 16.0, font, color)
                    left += columns[i]
                }
                if (header) {
                    drawLine(x, y + height, x + CONTENT_WIDTH, y + height, palette.accent.toUIColor(), 1.1)
                } else {
                    drawLine(x, y + height, x + CONTENT_WIDTH, y + height, LINE, 0.75)
                }
            }
        }

        private inner class SituationChunk(private val document: ReportDocument) : PdfChunk() {
            private val padding = 14.0
            private val gap = 10.0
            private val tileAccents = listOf(PLUM, CLAY, 0xFF5B4B8AL.toUIColor(), 0xFF2F6F62L.toUIColor())
            private val titleFont = UIFont.boldSystemFontOfSize(12.0)
            private val titleH = measureHeight(document.situationTitle, CONTENT_WIDTH - 2.0 * padding, titleFont)
            private val tileWidth = (CONTENT_WIDTH - 2.0 * padding - gap * (document.tiles.size - 1)) / document.tiles.size
            private val valueFonts = document.tiles.map { tile ->
                var size = 18.0
                var f = UIFont.boldSystemFontOfSize(size)
                while (measureWidth(tile.value, f) > tileWidth - 16.0 && size > 9.0) {
                    size -= 0.5
                    f = UIFont.boldSystemFontOfSize(size)
                }
                f
            }
            private val labelTexts = document.tiles.map { tile ->
                if (tile.detail != null) "${tile.label}\n${tile.detail}" else tile.label
            }
            private val labelHeights = labelTexts.map { measureHeight(it, tileWidth - 16.0, smallFont) }
            private val tileHeight = 12.0 + 24.0 + 3.0 + (labelHeights.maxOrNull() ?: 0.0) + 10.0
            override val height = padding + titleH + 8.0 + tileHeight + padding + 4.0

            override fun draw(x: Double, y: Double) {
                val boxH = height - 4.0
                fillAndStrokeRoundedRect(x, y, CONTENT_WIDTH, boxH, 8.0, PLUM_SOFT, 0xFFDCC8D5L.toUIColor(), 0.8)
                drawText(document.situationTitle, x + padding, y + padding, CONTENT_WIDTH - 2.0 * padding, titleFont, PLUM)

                val tileTop = y + padding + titleH + 8.0
                val white = UIColor.whiteColor
                document.tiles.forEachIndexed { i, tile ->
                    val left = x + padding + i * (tileWidth + gap)
                    fillAndStrokeRoundedRect(left, tileTop, tileWidth, tileHeight, 6.0, white, LINE, 0.7)
                    fillRoundedRect(left + 1.0, tileTop + 1.0, tileWidth - 2.0, 3.0, 2.0, tileAccents[i % tileAccents.size])
                    drawText(tile.value, left + 8.0, tileTop + 10.0, tileWidth - 16.0, valueFonts[i], tileAccents[i % tileAccents.size])
                    drawText(labelTexts[i], left + 8.0, tileTop + 12.0 + 24.0 + 3.0, tileWidth - 16.0, smallFont, INK_MUTED)
                }
            }
        }

        private class PlacedChip(val badge: ReportCategoryBadge, val x: Double, val y: Double, val w: Double)

        private inner class CategoriesChunk(
            private val title: String,
            badges: List<ReportCategoryBadge>,
        ) : PdfChunk(before = 8.0) {
            private val titleFont = UIFont.boldSystemFontOfSize(8.5)
            private val chipFont = UIFont.boldSystemFontOfSize(8.2)
            private val chipMutedFont = UIFont.systemFontOfSize(8.2)
            private val titleH = measureHeight(title, CONTENT_WIDTH, titleFont)
            private val chipHeight = 18.0
            private val gapX = 6.0
            private val gapY = 5.0
            private val placedChips: List<PlacedChip>

            init {
                var cx = 0.0
                var cy = titleH + 6.0
                val items = ArrayList<PlacedChip>(badges.size)
                badges.forEach { badge ->
                    val f = if (badge.hasData) chipFont else chipMutedFont
                    val chipW = (18.0 + measureWidth(badge.label, f) + 10.0).coerceAtMost(CONTENT_WIDTH)
                    if (cx > 0.0 && cx + chipW > CONTENT_WIDTH) {
                        cx = 0.0
                        cy += chipHeight + gapY
                    }
                    items.add(PlacedChip(badge, cx, cy, chipW))
                    cx += chipW + gapX
                }
                placedChips = items
            }

            override val height = (placedChips.lastOrNull()?.y ?: 0.0) + chipHeight + 6.0

            override fun draw(x: Double, y: Double) {
                drawText(title, x, y, CONTENT_WIDTH, titleFont, INK_MUTED)
                placedChips.forEach { item ->
                    val palette = reportSectionPalette(item.badge.section)
                    val cx = x + item.x
                    val cy = y + item.y
                    val bg = if (item.badge.hasData) palette.soft.toUIColor() else 0xFFFAF7F8L.toUIColor()
                    val border = if (item.badge.hasData) palette.accent.toUIColor() else LINE
                    fillAndStrokeRoundedRect(cx, cy, item.w, chipHeight, 9.0, bg, border, 0.75)

                    val dotX = cx + 9.0
                    val dotY = cy + chipHeight / 2.0
                    if (item.badge.hasData) {
                        fillCircle(dotX, dotY, 2.6, palette.accent.toUIColor())
                    } else {
                        strokeCircle(dotX, dotY, 2.6, OUTLINE, 1.0)
                    }
                    val f = if (item.badge.hasData) chipFont else chipMutedFont
                    val tc = if (item.badge.hasData) palette.accent.toUIColor() else INK_MUTED
                    drawText(item.badge.label, cx + 16.0, cy + 4.0, item.w - 20.0, f, tc)
                }
            }
        }

        private inner class EarlyTrackingChunk(private val info: ReportEarlyTracking) : PdfChunk(before = 10.0) {
            private val pad = 12.0
            private val titleFont = UIFont.boldSystemFontOfSize(10.5)
            private val textW = CONTENT_WIDTH - 2.0 * pad - 8.0
            private val titleH = measureHeight(info.title, textW, titleFont)
            private val msgH = measureHeight(info.message, textW, bodyFont)
            override val height = pad + titleH + 4.0 + msgH + pad + 4.0

            override fun draw(x: Double, y: Double) {
                val boxH = height - 4.0
                fillAndStrokeRoundedRect(x, y, CONTENT_WIDTH, boxH, 6.0, CREAM, LINE, 0.8)
                fillRoundedRect(x, y, 3.5, boxH, 2.0, PLUM)
                drawText(info.title, x + pad + 4.0, y + pad, textW, titleFont, PLUM)
                drawText(info.message, x + pad + 4.0, y + pad + titleH + 4.0, textW, bodyFont, INK)
            }
        }

        private inner class ConsultationNotesChunk(private val title: String) : PdfChunk(before = 14.0) {
            private val pad = 12.0
            private val linesCount = 4
            private val lineGap = 24.0
            private val titleFont = UIFont.boldSystemFontOfSize(9.5)
            private val titleH = measureHeight(title, CONTENT_WIDTH - 2.0 * pad, titleFont)
            override val height = pad + titleH + 8.0 + linesCount * lineGap + pad

            override fun draw(x: Double, y: Double) {
                fillAndStrokeRoundedRect(x, y, CONTENT_WIDTH, height, 6.0, PAPER, OUTLINE, 0.75)
                drawText(title, x + pad, y + pad, CONTENT_WIDTH - 2.0 * pad, titleFont, PLUM)
                var ly = y + pad + titleH + lineGap
                repeat(linesCount) {
                    drawLine(x + pad, ly, x + CONTENT_WIDTH - pad, ly, LINE, 0.8)
                    ly += lineGap
                }
            }
        }
    }

    // --- Utilitaires CoreGraphics / UIKit -----------------------------------------------------

    private fun Long.toUIColor(): UIColor {
        val a = ((this ushr 24) and 0xFF) / 255.0
        val r = ((this ushr 16) and 0xFF) / 255.0
        val g = ((this ushr 8) and 0xFF) / 255.0
        val b = (this and 0xFF) / 255.0
        return UIColor.colorWithRed(r, green = g, blue = b, alpha = a)
    }

    private fun textAttributes(font: UIFont, color: UIColor, alignment: NSTextAlignment = NSTextAlignmentLeft): Map<Any?, Any?> {
        val style = NSMutableParagraphStyle().apply {
            setLineHeightMultiple(LINE_SPACING)
            setAlignment(alignment)
        }
        return mapOf(
            NSFontAttributeName to font,
            NSForegroundColorAttributeName to color,
            NSParagraphStyleAttributeName to style,
        )
    }

    private fun measureHeight(text: String, width: Double, font: UIFont): Double {
        if (text.isEmpty()) return 0.0
        val rect = (text as NSString).boundingRectWithSize(
            CGSizeMake(width.coerceAtLeast(1.0), 10000.0),
            options = NSStringDrawingUsesLineFragmentOrigin or NSStringDrawingUsesFontLeading,
            attributes = textAttributes(font, INK),
            context = null,
        )
        return ceil(rect.useContents { size.height })
    }

    private fun measureWidth(text: String, font: UIFont): Double {
        if (text.isEmpty()) return 0.0
        val rect = (text as NSString).boundingRectWithSize(
            CGSizeMake(10000.0, 100.0),
            options = NSStringDrawingUsesLineFragmentOrigin or NSStringDrawingUsesFontLeading,
            attributes = mapOf(NSFontAttributeName to font),
            context = null,
        )
        return ceil(rect.useContents { size.width })
    }

    private fun drawText(
        text: String,
        x: Double,
        y: Double,
        width: Double,
        font: UIFont,
        color: UIColor,
        alignment: NSTextAlignment = NSTextAlignmentLeft,
    ) {
        if (text.isEmpty()) return
        val h = measureHeight(text, width, font) + 4.0
        (text as NSString).drawWithRect(
            CGRectMake(x, y, width.coerceAtLeast(1.0), h),
            options = NSStringDrawingUsesLineFragmentOrigin or NSStringDrawingUsesFontLeading,
            attributes = textAttributes(font, color, alignment),
            context = null,
        )
    }

    private fun fillRect(x: Double, y: Double, w: Double, h: Double, color: UIColor) {
        color.setFill()
        UIBezierPath.bezierPathWithRect(CGRectMake(x, y, w, h)).fill()
    }

    private fun fillRoundedRect(x: Double, y: Double, w: Double, h: Double, radius: Double, color: UIColor) {
        color.setFill()
        UIBezierPath.bezierPathWithRoundedRect(CGRectMake(x, y, w, h), cornerRadius = radius).fill()
    }

    private fun fillAndStrokeRoundedRect(
        x: Double,
        y: Double,
        w: Double,
        h: Double,
        radius: Double,
        fill: UIColor,
        stroke: UIColor,
        lineWidth: Double,
    ) {
        val path = UIBezierPath.bezierPathWithRoundedRect(CGRectMake(x, y, w, h), cornerRadius = radius)
        path.setLineWidth(lineWidth)
        fill.setFill()
        path.fill()
        stroke.setStroke()
        path.stroke()
    }

    private fun fillCircle(cx: Double, cy: Double, radius: Double, color: UIColor) {
        color.setFill()
        UIBezierPath.bezierPathWithOvalInRect(CGRectMake(cx - radius, cy - radius, radius * 2.0, radius * 2.0)).fill()
    }

    private fun strokeCircle(cx: Double, cy: Double, radius: Double, color: UIColor, lineWidth: Double) {
        val path = UIBezierPath.bezierPathWithOvalInRect(CGRectMake(cx - radius, cy - radius, radius * 2.0, radius * 2.0))
        path.setLineWidth(lineWidth)
        color.setStroke()
        path.stroke()
    }

    private fun drawLine(x1: Double, y1: Double, x2: Double, y2: Double, color: UIColor, lineWidth: Double) {
        val ctx = UIGraphicsGetCurrentContext() ?: return
        CGContextSetStrokeColorWithColor(ctx, color.CGColor)
        CGContextSetLineWidth(ctx, lineWidth)
        CGContextMoveToPoint(ctx, x1, y1)
        CGContextAddLineToPoint(ctx, x2, y2)
        CGContextStrokePath(ctx)
    }
}
