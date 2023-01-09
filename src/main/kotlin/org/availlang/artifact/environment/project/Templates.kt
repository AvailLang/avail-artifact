package org.availlang.artifact.environment.project

import org.availlang.artifact.AvailArtifact
import org.availlang.artifact.manifest.AvailArtifactManifest
import org.availlang.artifact.manifest.AvailRootManifest
import org.availlang.json.JSONFriendly
import org.availlang.json.JSONObject
import org.availlang.json.JSONWriter

/**
 * The expansion text of a template.
 *
 * @author Richard Arriaga
 *
 * @property expansion
 *   The full template expansion string.
 */
data class TemplateExpansion constructor(var expansion: String): JSONFriendly
{
	/**
	 * `true` indicates this [TemplateExpansion] should be included in an
	 * [AvailArtifact]'s [AvailArtifactManifest]'s
	 * [AvailRootManifest.templates]s; `false` otherwise.
	 */
	@Suppress("MemberVisibilityCanBePrivate")
	var markedForArtifactInclusion: Boolean = true

	/**
	 * An optional description of this [TemplateExpansion] or `null` if not
	 * used.
	 */
	@Suppress("MemberVisibilityCanBePrivate")
	var description: String? = null

	override fun writeTo(writer: JSONWriter)
	{
		writer.writeObject {
			at(::expansion.name) { write(expansion) }
			at(::markedForArtifactInclusion.name)
			{
				write(markedForArtifactInclusion)
			}
			description?.let {
				at(::description.name) { write(description) }
			}
		}
	}

	/**
	 * Construct a [TemplateExpansion] from the provided [JSONObject].
	 *
	 * @param obj
	 *   The [JSONObject] to extract the [TemplateExpansion] from.
	 */
	constructor(
		obj: JSONObject
	): this(obj.getString(TemplateExpansion::expansion.name))
	{
		markedForArtifactInclusion =
			obj.getBoolean(::markedForArtifactInclusion.name)
		description = obj.getStringOrNull(::description.name)
	}
}
