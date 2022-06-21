package org.availlang.artifact.configuration

import org.availlang.artifact.AvailArtifactException
import org.availlang.json.JSONObject
import org.availlang.json.JSONWriter

/**
 * The first version of [AvailApplicationConfiguration].
 *
 * @author Richard Arriaga
 */
class AvailApplicationConfigurationV1 constructor(
	override val includedRoots: List<String>,
	override val rootRenames: List<AvailRootRename>
): AvailApplicationConfiguration
{
	override val configurationVersion = 1

	override fun writeTo(writer: JSONWriter)
	{
		writer.writeObject {
			at(AvailApplicationConfigurationV1::configurationVersion.name)
			{
				write(configurationVersion)
			}
			at(AvailApplicationConfigurationV1::includedRoots.name)
			{
				writeStrings(includedRoots)
			}
			at(AvailApplicationConfigurationV1::rootRenames.name)
			{
				writeArray(rootRenames)
			}
		}
	}

	companion object
	{
		/**
		 * Answer an [AvailApplicationConfiguration] from the provided
		 * [JSONObject].
		 *
		 * @param obj
		 *   The [JSONObject] to extract the [AvailApplicationConfiguration]
		 *   from.
		 * @return
		 *   The extracted [AvailApplicationConfiguration].
		 * @throws AvailArtifactException
		 *   If there is any problem extracting the
		 *   [AvailApplicationConfiguration].
		 */
		internal fun fromJSON (obj: JSONObject): AvailApplicationConfiguration
		{
			val includedRoots =
				try
				{
					obj.getArray(
						AvailApplicationConfigurationV1::includedRoots.name
					).strings
				}
				catch (e: Throwable)
				{
					throw AvailArtifactException(
						"Problem accessing Avail Application Configuration " +
								"included roots.",
						e)
				}
			val renames =
				try
				{
					obj.getArray(
						AvailApplicationConfigurationV1::rootRenames.name
					).map { AvailRootRename.fromJSON(it as JSONObject) }
						.toList()
				}
				catch (e: Throwable)
				{
					throw AvailArtifactException(
						"Problem accessing Avail Application Configuration " +
								"root renames.",
						e)
				}
			return AvailApplicationConfigurationV1(includedRoots, renames)
		}
	}
}
