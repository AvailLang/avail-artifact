package org.availlang.artifact.manifest

import org.availlang.artifact.AvailArtifact
import org.availlang.artifact.AvailArtifactException
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
 * @property description
 *   A description of the root.
 * @property digestAlgorithm
 *   The [MessageDigest] algorithm to use to create the digests for all the
 *   root's contents. This must be a valid algorithm accessible from
 *   [java.security.MessageDigest.getInstance].
 */
data class AvailRootManifest constructor(
	val name: String,
	val availModuleExtensions: List<String>,
	val entryPoints: List<String> = listOf(),
	val templates: Map<String, String> = mapOf(),
	val description: String = "",
	val digestAlgorithm: String = "SHA-256"
): JSONFriendly
{
	override fun writeTo(writer: JSONWriter)
	{
		writer.writeObject {
			at(AvailRootManifest::name.name) { write(name) }
			at(AvailRootManifest::description.name) { write(description) }
			at(AvailRootManifest::digestAlgorithm.name) {
				write(digestAlgorithm)
			}
			at(AvailRootManifest::availModuleExtensions.name) {
				writeStrings(availModuleExtensions)
			}
			at(AvailRootManifest::entryPoints.name) {
				writeStrings(entryPoints)
			}
			at(AvailRootManifest::templates.name) {
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
						AvailRootManifest::availModuleExtensions.name).strings
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
						AvailRootManifest::entryPoints.name).strings
				}
				catch (e: Throwable)
				{
					throw AvailArtifactException(
						"Problem extracting Avail Manifest Root entry points.",
						e)
				}
			val templates =
				try
				{
					val key = AvailRootManifest::templates.name
					if (obj.containsKey(key))
					{
						val map = obj.getObject(key)
						val templates = mutableMapOf<String, String>()
						map.forEach { (name, expansion) ->
							templates[name] = expansion.string
						}
					}
					else
					{
						mapOf<String, String>()
					}
				}
				catch (e: Throwable)
				{
					throw AvailArtifactException(
						"Problem extracting Avail Manifest Root templates.",
						e)
				}
			return AvailRootManifest(name, extensions, entryPoints)
		}
	}
}
