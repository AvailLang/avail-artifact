package org.availlang.artifact.manifest

import org.availlang.artifact.AvailArtifact
import org.availlang.artifact.AvailArtifactException
import org.availlang.artifact.environment.project.AvailProjectRoot
import org.availlang.artifact.environment.project.Palette
import org.availlang.artifact.environment.project.StyleAttributes
import org.availlang.artifact.environment.project.TemplateExpansion
import org.availlang.json.JSONFriendly
import org.availlang.json.JSONObject
import org.availlang.json.JSONWriter
import java.security.MessageDigest

/**
 * Contains information about an Avail Module Root inside an [AvailArtifact].
 *
 * @author Richard Arriaga
 *
 * @property name
 *   The name of the Avail root.
 * @property availModuleExtensions
 *   The file extensions that signify files that should be treated as Avail
 *   modules.
 * @property entryPoints
 *   The Avail entry points exposed by this root.
 * @property templates
 *   The templates that should be available when editing Avail source
 *   modules in the workbench.
 * @property stylesheet
 *   The default stylesheet for this root. Symbolic names are resolved against
 *   the accompanying [palette].
 * @property palette
 *   The [Palette] used for the [stylesheet].
 * @property description
 *   A description of the root.
 * @property digestAlgorithm
 *   The [MessageDigest] algorithm to use to create the digests for all the
 *   root's contents. This must be a valid algorithm accessible from
 *   [java.security.MessageDigest.getInstance].
 */
data class AvailRootManifest constructor(
	val name: String,
	val availModuleExtensions: MutableList<String>,
	val entryPoints: MutableList<String> = mutableListOf(),
	val templates: MutableMap<String, TemplateExpansion> = mutableMapOf(),
	val stylesheet: Map<String, StyleAttributes> = mutableMapOf(),
	val palette: Palette = Palette.empty,
	val description: String = "",
	val digestAlgorithm: String = "SHA-256"
): JSONFriendly
{
	override fun writeTo(writer: JSONWriter)
	{
		writer.writeObject {
			at(::name.name) { write(name) }
			at(::description.name) { write(description) }
			at(::digestAlgorithm.name) { write(digestAlgorithm) }
			at(::availModuleExtensions.name) {
				writeStrings(availModuleExtensions)
			}
			at(::entryPoints.name) { writeStrings(entryPoints) }
			at(::palette.name) { write(palette) }
			at(::stylesheet.name) {
				writeObject {
					stylesheet.forEach { (name, attribute) ->
						at(name) { write(attribute) }
					}
				}
			}
			at(::templates.name) {
				writeObject {
					templates.forEach { (name, expansion) ->
						at(name) { write(expansion) }
					}
				}
			}
		}
	}

	companion object
	{
		/**
		 * Extract an [AvailRootManifest] from the given [JSONObject].
		 *
		 * @param obj
		 *   The [JSONObject] to extract the data from.
		 * @return
		 *   The extracted [AvailRootManifest].
		 * @throws AvailArtifactException
		 *   If there is an issue with extracting the [AvailRootManifest].
		 */
		fun from (obj: JSONObject): AvailRootManifest
		{
			val name =
				try
				{
					obj.getString(AvailRootManifest::name.name)
				}
				catch (e: Throwable)
				{
					throw AvailArtifactException(
						"Problem extracting Avail Manifest Root name.", e)
				}
			val description =
				try
				{
					obj.getString(AvailRootManifest::description.name)
				}
				catch (e: Throwable)
				{
					throw AvailArtifactException(
						"Problem extracting Avail Manifest Root description.",
						e)
				}
			val digestAlgorithm =
				try
				{
					obj.getString(AvailRootManifest::digestAlgorithm.name)
				}
				catch (e: Throwable)
				{
					throw AvailArtifactException(
						"Problem extracting Avail Manifest Root digest " +
							"algorithm.",
						e)
				}
			val extensions =
				try
				{
					obj.getArray(
						AvailRootManifest::availModuleExtensions.name
					).strings.toMutableList()

				}
				catch (e: Throwable)
				{
					throw AvailArtifactException(
						"Problem extracting Avail Manifest Root module file " +
							"extensions.",
						e)
				}
			val entryPoints =
				try
				{
					obj.getArray(
						AvailRootManifest::entryPoints.name
					).strings.toMutableList()
				}
				catch (e: Throwable)
				{
					throw AvailArtifactException(
						"Problem extracting Avail Manifest Root entry points.",
						e)
				}
			val templates = mutableMapOf<String, TemplateExpansion>()
			try
			{
				val key = AvailRootManifest::templates.name
				if (obj.containsKey(key))
				{
					obj.getObject(key).associateTo(templates) {
							(name, expansion) ->
						name to TemplateExpansion(expansion as JSONObject)
					}
				}
			}
			catch (e: Throwable)
			{
				throw AvailArtifactException(
					"Problem extracting Avail Manifest Root templates.",
					e)
			}
			return AvailRootManifest(
				name = name,
				availModuleExtensions = extensions,
				entryPoints = entryPoints,
				templates = templates,
				stylesheet = obj.getObjectOrNull(
					AvailProjectRoot::stylesheet.name
				)?.let {
					it.associateTo(mutableMapOf()) { (rule, attributes) ->
						rule to StyleAttributes(attributes as JSONObject)
					}
				} ?: mapOf(),
				palette = obj.getObjectOrNull(
					AvailProjectRoot::palette.name
				)?.let {
					Palette.from(it)
				} ?: Palette.empty,
				description = description,
				digestAlgorithm = digestAlgorithm)
		}
	}
}
