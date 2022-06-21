package org.availlang.artifact.configuration

import org.availlang.artifact.AvailArtifact
import org.availlang.artifact.AvailArtifactException
import org.availlang.artifact.AvailArtifactType
import org.availlang.json.JSONFriendly
import org.availlang.json.JSONObject

/**
 * The interface for declaring the contents of an Avail configuration file used
 * to configure an Avail [application][AvailArtifactType.LIBRARY]
 *
 * @author Richard Arriaga
 */
interface AvailApplicationConfiguration: JSONFriendly
{
	/**
	 * The configuration version that indicates the version of this
	 * configuration. This version dictates the contents of the backing
	 * configuration file.
	 */
	val configurationVersion: Int

	/**
	 * The list of [AvailRootRename]s that indicate name changes for roots
	 * packaged in the Avail artifact.
	 */
	val rootRenames: List<AvailRootRename>

	/**
	 * The names of the Avail Module Roots to load into the Avail Runtime at
	 * startup.
	 */
	val includedRoots: List<String>

	companion object
	{
		/**
		 * The version that represents the current structure of the
		 * [AvailApplicationConfiguration] file.
		 */
		private const val CURRENT_ARTIFACT_VERSION = 1

		/**
		 * The path within this artifact of the [AvailApplicationConfiguration]
		 * file.
		 */
		const val availApplicationConfigurationFile =
			AvailArtifact.artifactRootDirectory +
					"/avail-application-configuration.json"

		/**
		 * Answer an [AvailApplicationConfiguration] from the provided
		 * [JSONObject].
		 *
		 * @param obj
		 *   The [JSONObject] tp extract the [AvailApplicationConfiguration]
		 *   from.
		 * @return
		 *   The extracted [AvailApplicationConfiguration].
		 * @throws AvailArtifactException
		 *   If there is any problem extracting the
		 *   [AvailApplicationConfiguration].
		 */
		fun from (obj: JSONObject): AvailApplicationConfiguration
		{
			val version =
				try
				{
					obj.getNumber(
						AvailApplicationConfiguration::configurationVersion.name).int
				}
				catch (e: Throwable)
				{
					throw AvailArtifactException(
						"Problem accessing Avail Artifact Manifest Version.", e)
				}
			return when (version)
			{
				1 -> AvailApplicationConfigurationV1.fromJSON(obj)
				else ->
					throw AvailArtifactException("Invalid Avail " +
							"Configuration: Version $version is not in the" +
							" valid range of known configuration versions," +
							" [1, $CURRENT_ARTIFACT_VERSION].")
			}
		}
	}
}
