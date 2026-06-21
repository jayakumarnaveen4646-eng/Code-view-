package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun HTMLRenderView(htmlCode: String, modifier: Modifier = Modifier) {
    val cleanHtml = htmlCode.trim()
    if (cleanHtml.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                "Write HTML tags above and click Run to preview here!",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        return
    }

    // A lightweight scanner that parses line elements or tag nodes and renders them sequentially
    val nodes = remember(htmlCode) { parseHtmlSnippet(cleanHtml) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (nodes.isEmpty()) {
            Text(
                text = "HTML Render Frame: No paint nodes detected. Write standard header titles, buttons, or stylized tags!",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        } else {
            nodes.forEach { node ->
                RenderNode(node)
            }
        }
    }
}

sealed class HtmlNode {
    data class Header(val level: Int, val text: String, val styleMap: Map<String, String>) : HtmlNode()
    data class Paragraph(val text: String, val styleMap: Map<String, String>) : HtmlNode()
    data class Div(val children: List<HtmlNode>, val styleMap: Map<String, String>) : HtmlNode()
    data class Link(val text: String, val href: String, val styleMap: Map<String, String>) : HtmlNode()
    data class AnchorButton(val text: String, val styleMap: Map<String, String>) : HtmlNode()
    data class Label(val text: String, val styleMap: Map<String, String>) : HtmlNode()
    data class TextInput(val placeholder: String, val styleMap: Map<String, String>) : HtmlNode()
    data class ListItem(val text: String, val styleMap: Map<String, String>) : HtmlNode()
    data class PlainText(val text: String) : HtmlNode()
    object Divider : HtmlNode()
}

@Composable
private fun RenderNode(node: HtmlNode) {
    when (node) {
        is HtmlNode.Header -> {
            val color = parseColor(node.styleMap["color"], MaterialTheme.colorScheme.onSurface)
            val fontSize = when (node.level) {
                1 -> 24.sp
                2 -> 20.sp
                3 -> 18.sp
                else -> 16.sp
            }
            Text(
                text = node.text,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = applyMargins(node.styleMap)
            )
        }
        is HtmlNode.Paragraph -> {
            val color = parseColor(node.styleMap["color"], MaterialTheme.colorScheme.onSurfaceVariant)
            val fontSize = parseFontSize(node.styleMap["font-size"], 14.sp)
            Text(
                text = node.text,
                fontSize = fontSize,
                color = color,
                modifier = applyMargins(node.styleMap)
            )
        }
        is HtmlNode.Div -> {
            val bgColor = parseColor(node.styleMap["background-color"], Color.Transparent)
            val borderColor = parseColor(node.styleMap["border-color"], Color.Transparent)
            val paddingValue = parseDimension(node.styleMap["padding"], 0).dp
            val radiusValue = parseDimension(node.styleMap["border-radius"], 0).dp
            val textColor = parseColor(node.styleMap["color"], MaterialTheme.colorScheme.onSurface)

            var modifier = Modifier.fillMaxWidth()
            if (bgColor != Color.Transparent) {
                modifier = modifier.background(bgColor, RoundedCornerShape(radiusValue))
            }
            if (borderColor != Color.Transparent) {
                val borderThick = parseDimension(node.styleMap["border-width"], 1).dp
                modifier = modifier.border(borderThick, borderColor, RoundedCornerShape(radiusValue))
            }
            // clip in all cases for neat layout backgrounds
            modifier = modifier.clip(RoundedCornerShape(radiusValue)).padding(paddingValue)

            // Dynamic card
            Box(modifier = modifier) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    node.children.forEach { childNode ->
                        // Propagate base textColor for children if they don't override
                        RenderNode(childNode)
                    }
                }
            }
        }
        is HtmlNode.Link -> {
            val color = parseColor(node.styleMap["color"], Color(0xFF007396))
            Text(
                text = node.text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = color,
                modifier = applyMargins(node.styleMap)
            )
        }
        is HtmlNode.AnchorButton -> {
            val bgColor = parseColor(node.styleMap["background-color"], MaterialTheme.colorScheme.primary)
            val textColor = parseColor(node.styleMap["color"], Color.White)
            val radius = parseDimension(node.styleMap["border-radius"], 6).dp
            val padding = parseDimension(node.styleMap["padding"], 8).dp

            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = bgColor, contentColor = textColor),
                shape = RoundedCornerShape(radius),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = padding),
                modifier = applyMargins(node.styleMap)
            ) {
                Text(node.text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
        is HtmlNode.Label -> {
            val color = parseColor(node.styleMap["color"], MaterialTheme.colorScheme.onSurface)
            Text(
                text = node.text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = applyMargins(node.styleMap)
            )
        }
        is HtmlNode.TextInput -> {
            var textState by remember { mutableStateOf("") }
            val borderRad = parseDimension(node.styleMap["border-radius"], 6).dp
            
            OutlinedTextField(
                value = textState,
                onValueChange = { textState = it },
                placeholder = { Text(node.placeholder) },
                shape = RoundedCornerShape(borderRad),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                modifier = Modifier.fillMaxWidth().then(applyMargins(node.styleMap)),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
        is HtmlNode.ListItem -> {
            val color = parseColor(node.styleMap["color"], MaterialTheme.colorScheme.onSurface)
            Row(
                modifier = applyMargins(node.styleMap),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("•", fontWeight = FontWeight.Black, color = color)
                Text(node.text, color = color, fontSize = 14.sp)
            }
        }
        is HtmlNode.PlainText -> {
            Text(text = node.text, fontSize = 14.sp)
        }
        is HtmlNode.Divider -> {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

// Map style parameters to modifiers
private fun applyMargins(styleMap: Map<String, String>): Modifier {
    val top = parseDimension(styleMap["margin-top"] ?: styleMap["margin"], 0).dp
    val bottom = parseDimension(styleMap["margin-bottom"] ?: styleMap["margin"], 0).dp
    return Modifier.padding(top = top, bottom = bottom)
}

// Light XML styled scanner
private fun parseHtmlSnippet(html: String): List<HtmlNode> {
    val results = mutableListOf<HtmlNode>()
    var index = 0

    try {
        while (index < html.length) {
            val openTagStart = html.indexOf('<', index)
            if (openTagStart == -1) {
                val plainText = html.substring(index).trim()
                if (plainText.isNotEmpty()) {
                    results.add(HtmlNode.PlainText(plainText))
                }
                break
            }

            // Append plain text leading up to compile tag
            if (openTagStart > index) {
                val leadText = html.substring(index, openTagStart).trim()
                if (leadText.isNotEmpty() && !leadText.startsWith("<!--")) {
                    results.add(HtmlNode.PlainText(leadText))
                }
            }

            val openTagEnd = html.indexOf('>', openTagStart)
            if (openTagEnd == -1) break

            val tagInner = html.substring(openTagStart + 1, openTagEnd).trim()
            if (tagInner.startsWith("!--")) {
                // Skips comments
                val commentEnd = html.indexOf("-->", openTagEnd)
                index = if (commentEnd == -1) html.length else commentEnd + 3
                continue
            }

            // Extract parts of tag, e.g. <div style="background: red;">
            val tagParts = parseTagParts(tagInner)
            val tagName = tagParts[0].lowercase(Locale.ROOT)
            val attrMap = tagParts.getOrNull(1)?.let { parseAttributes(it) } ?: emptyMap()
            val styleMap = attrMap["style"]?.let { parseStyleAttribute(it) } ?: emptyMap()

            // Handle self-closing tags
            val isSelfClosing = tagInner.endsWith("/") || tagName == "input" || tagName == "hr"
            val closeTag = "</$tagName>"

            val contentEnd = if (isSelfClosing) -1 else html.indexOf(closeTag, openTagEnd)
            val innerContent = if (contentEnd != -1) {
                html.substring(openTagEnd + 1, contentEnd).trim()
            } else {
                ""
            }

            when (tagName) {
                "h1", "h2", "h3", "h4", "h5" -> {
                    val level = tagName.substring(1).toIntOrNull() ?: 2
                    results.add(HtmlNode.Header(level, innerContent, styleMap))
                }
                "p" -> {
                    results.add(HtmlNode.Paragraph(innerContent, styleMap))
                }
                "hr" -> {
                    results.add(HtmlNode.Divider)
                }
                "a" -> {
                    val href = attrMap["href"] ?: ""
                    results.add(HtmlNode.Link(innerContent, href, styleMap))
                }
                "button" -> {
                    results.add(HtmlNode.AnchorButton(innerContent, styleMap))
                }
                "label" -> {
                    results.add(HtmlNode.Label(innerContent, styleMap))
                }
                "input" -> {
                    val placeholder = attrMap["placeholder"] ?: "Type here..."
                    results.add(HtmlNode.TextInput(placeholder, styleMap))
                }
                "li" -> {
                    results.add(HtmlNode.ListItem(innerContent, styleMap))
                }
                "div" -> {
                    // recursively parse inner html
                    val innerNodes = parseHtmlSnippet(innerContent)
                    results.add(HtmlNode.Div(innerNodes, styleMap))
                }
                "ul", "ol" -> {
                    val innerNodes = parseHtmlSnippet(innerContent)
                    results.addAll(innerNodes)
                }
            }

            index = if (isSelfClosing) {
                openTagEnd + 1
            } else if (contentEnd != -1) {
                contentEnd + closeTag.length
            } else {
                openTagEnd + 1
            }
        }
    } catch (e: Exception) {
        // Fallback for parsing errors
        results.add(HtmlNode.PlainText("Parse warning: rendering incomplete."))
    }

    return results
}

private fun parseTagParts(tagInner: String): List<String> {
    val spaceIdx = tagInner.indexOf(' ')
    if (spaceIdx == -1) return listOf(tagInner.trimEnd('/'))
    val name = tagInner.substring(0, spaceIdx).trim()
    val attributes = tagInner.substring(spaceIdx + 1).trimEnd('/')
    return listOf(name, attributes)
}

private fun parseAttributes(attrString: String): Map<String, String> {
    val map = mutableMapOf<String, String>()
    var i = 0
    while (i < attrString.length) {
        val eq = attrString.indexOf('=', i)
        if (eq == -1) break
        val name = attrString.substring(i, eq).trim()
        val quoteStart = attrString.indexOf('"', eq)
        if (quoteStart == -1) break
        val quoteEnd = attrString.indexOf('"', quoteStart + 1)
        if (quoteEnd == -1) break
        val value = attrString.substring(quoteStart + 1, quoteEnd)
        map[name] = value
        i = quoteEnd + 1
    }
    return map
}

private fun parseStyleAttribute(styleValue: String): Map<String, String> {
    val map = mutableMapOf<String, String>()
    val pairs = styleValue.split(";")
    for (pair in pairs) {
        val split = pair.split(":")
        if (split.size == 2) {
            val key = split[0].trim().lowercase(Locale.ROOT)
            val value = split[1].trim()
            map[key] = value
        }
    }
    return map
}

private fun parseColor(colorValue: String?, default: Color): Color {
    if (colorValue == null) return default
    val clean = colorValue.replace("#", "").trim()
    if (clean.length == 6) {
        return try {
            Color(android.graphics.Color.parseColor("#$clean"))
        } catch (e: Exception) {
            default
        }
    }
    return when (clean.lowercase(Locale.ROOT)) {
        "white" -> Color.White
        "black" -> Color.Black
        "red" -> Color.Red
        "blue" -> Color.Blue
        "green" -> Color(0xFF4CAF50)
        "yellow" -> Color.Yellow
        "gray" -> Color.Gray
        "grey" -> Color.Gray
        else -> default
    }
}

private fun parseDimension(dimValue: String?, default: Int): Int {
    if (dimValue == null) return default
    val numberOnly = dimValue.replace("px", "").replace("dp", "").trim()
    return numberOnly.toIntOrNull() ?: default
}

private fun parseFontSize(valStr: String?, default: androidx.compose.ui.unit.TextUnit): androidx.compose.ui.unit.TextUnit {
    if (valStr == null) return default
    val numStr = valStr.replace("px", "").replace("sp", "").trim()
    val num = numStr.toIntOrNull()
    return if (num != null) num.sp else default
}
