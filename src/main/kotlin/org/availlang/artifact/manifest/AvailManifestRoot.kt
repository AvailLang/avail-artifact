package org.availlang.artifact.manifest

import org.availlang.artifact.AvailArtifact
import org.availlang.artifact.AvailArtifactException
import org.availlang.json.JSONFriendly
import org.availlang.json.JSONObject
import org.availlang.json.JSONWriter

/**
 * Contains information about an Avail Module Root inside an [AvailArtifact].
 *
 * @author Richard Arriaga
 */
data class AvailManifestRoot constructor(
	val name: String,
	val availModuleExtensions: List<String>,
	val entryPoints: List<String>
): JSONFriendly
{
	override fun writeTo(writer: JSONWriter)
	{
		writer.writeObject {
			at(AvailManifestRoot::name.name)
			{
				write(name)
			}
			at(AvailManifestRoot::availModuleExtensions.name)
			{
				writeStrings(availModuleExtensions)
			}
			at(AvailManifestRoot::entryPoints.name)
			{
				writeStrings(entryPoints)
			}
		}
	}

	companion object
	{
		/**
		 * Extract an [AvailManifestRoot] from the given [JSONObject].
		 *
		 * @param obj
		 *   The [JSONObject] to extract the data from.
		 * @return
		 *   The extracted [AvailManifestRoot].
		 * @throws AvailArtifactException
		 *   If there is an issue with extracting the [AvailManifestRoot].
		 */
		fun from (obj: JSONObject): AvailManifestRoot
		{
			val name =
				try
				{
					obj.getString(AvailManifestRoot::name.name)
				}
				catch (e: Throwable)
				{
					throw AvailArtifactException(
						"Problem extracting Avail Manifest Root name.", e)
				}
			val extensions =
				try
				{
					obj.getArray(
						AvailManifestRoot::availModuleExtensions.name).strings
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
						AvailManifestRoot::entryPoints.name).strings
				}
				catch (e: Throwable)
				{
					throw AvailArtifactException(
						"Problem extracting Avail Manifest Root entry points.",
						e)
				}
			return AvailManifestRoot(name, extensions, entryPoints)
		}
	}
}
