@file:Suppress("DuplicatedCode")

package org.availlang.artifact.environment.project

import org.availlang.json.JSONFriendly
import org.availlang.json.JSONObject
import org.availlang.json.JSONWriter
import java.awt.Color

/**
 * The style attributes to apply to a styling pattern.
 *
 * @property fontFamily
 *   The font family for text rendition.
 * @property foreground
 *   The foreground color, i.e., the color of rendered text.
 * @property background
 *   The background color.
 * @property bold
 *   Whether to give bold weight to the rendered text.
 * @property italic
 *   Whether to give italic style to the rendered text.
 * @property underline
 *   Whether to give underline decoration to the rendered text.
 * @property superscript
 *   Whether to give superscript position to the rendered text.
 * @property subscript
 *   Whether to give subscript position to the rendered text.
 * @property strikethrough
 *   Whether to give strikethrough decoration to the rendered text.
 * @author Todd L Smith &lt;todd@availlang.org&gt;
 */
data class StyleAttributes constructor(
	val fontFamily: String?,
	val foreground: Color?,
	val background: Color?,
	val bold: Boolean?,
	val italic: Boolean?,
	val underline: Boolean?,
	val superscript: Boolean?,
	val subscript: Boolean?,
	val strikethrough: Boolean?
): JSONFriendly
{
	constructor(data: JSONObject): this(
		fontFamily = data.getStringOrNull(StyleAttributes::fontFamily.name),
		foreground = data.getColorOrNull(StyleAttributes::foreground.name),
		background = data.getColorOrNull(StyleAttributes::background.name),
		bold = data.getBooleanOrNull(StyleAttributes::bold.name),
		italic = data.getBooleanOrNull(StyleAttributes::italic.name),
		underline = data.getBooleanOrNull(StyleAttributes::underline.name),
		superscript = data.getBooleanOrNull(StyleAttributes::superscript.name),
		subscript = data.getBooleanOrNull(StyleAttributes::subscript.name),
		strikethrough = data.getBooleanOrNull(
			StyleAttributes::strikethrough.name)
	)
	{
		// No implementation required.
	}

	override fun writeTo(writer: JSONWriter)
	{
		writer.writeObject {
			fontFamily?.let { at(::fontFamily.name) { write(fontFamily) } }
			foreground?.let { at(::foreground.name) { write(foreground.hex) } }
			background?.let { at(::background.name) { write(background.hex) } }
			bold?.let { at(::bold.name) { write(bold) } }
			italic?.let { at(::italic.name) { write(italic) } }
			underline?.let { at(::underline.name) { write(underline) } }
			superscript?.let { at(::superscript.name) { write(superscript) } }
			subscript?.let { at(::subscript.name) { write(subscript) } }
			strikethrough?.let {
				at(::strikethrough.name) { write(strikethrough) }
			}
		}
	}

	companion object
	{
		/**
		 * A padded hexadecimal rendition of the [receiver][Color], compatible
		 * with HTML 5/CSS 3/Swing.
		 */
		private val Color.hex get() = buildString(9) {
			append('#')
			append("%02X".format(red))
			append("%02X".format(green))
			append("%02X".format(blue))
			append("%02X".format(alpha))
		}

		/**
		 * Construct a [Color] from the hex string accessible via the specified
		 * key.
		 *
		 * @param key
		 *   The hexadecimal string, compatible with [hex]/HTML 5/CSS 3/Swing.
		 * @return
		 *   The decoded color.
		 */
		private fun JSONObject.getColorOrNull(key: String): Color?
		{
			val hex = getStringOrNull(key) ?: return null
			// The first character is the octothorp, so ignore it.
			val r = hex.substring(1 .. 2).toInt(16)
			val g = hex.substring(3 .. 4).toInt(16)
			val b = hex.substring(5 .. 6).toInt(16)
			val a = hex.substring(7 .. 8).toInt(16)
			return Color(r, g, b, a)
		}
	}
}
