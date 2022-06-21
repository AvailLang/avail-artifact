package org.availlang.artifact.manifest

import org.availlang.artifact.AvailArtifact
import org.availlang.artifact.AvailArtifactException
import org.availlang.artifact.AvailArtifactType
import org.availlang.artifact.formattedNow
import org.availlang.json.JSONFriendly
import org.availlang.json.JSONObject

/**
 * The interface that for the manifest that describes the contents of the
 * [AvailArtifact] of the associated artifact.
 *
 * Implementations of this interface are versioned using [artifactVersion] which
 * should be used to dispatch manifest construction from an Avail Manifest File.
 *
 * As new manifest file components are added to this interface, new version
 * implementations should be added and existing versions should be back-filled
 * with logic to handle the new state from existing manifests built under the
 * older version.
 *
 * @author Richard Arriaga
 */
sealed interface AvailArtifactManifest: JSONFriendly
{
	/**
	 * The packaging version that the contents of the artifact was packaged
	 * under.
	 */
	val artifactVersion: Int

	/**
	 * The [AvailArtifactType] that describes the nature of the Avail artifact.
	 */
	val artifactType: AvailArtifactType

	/**
	 * The UTC timestamp that represents when the artifact was constructed. Must
	 * be created using [formattedNow] when newly constructing an artifact.
	 */
	val constructed: String

	/**
	 * The map of the [AvailManifestRoot]s keyed by [AvailManifestRoot.name]
	 * that are present in the artifact.
	 */
	val roots: Map<String, AvailManifestRoot>

	companion object
	{
		/**
		 * The version that represents the current structure under which Avail
		 * libs are packaged in the artifact.
		 */
		private const val CURRENT_ARTIFACT_VERSION = 1

		/**
		 * The path within this artifact of the [AvailArtifactManifest].
		 */
		const val availArtifactManifestFile =
			"${AvailArtifact.artifactRootDirectory}/avail-artifact-manifest.json"

		/**
		 * Answer an [AvailArtifactManifest] from the provided [JSONObject].
		 *
		 * @param obj
		 *   The [JSONObject] tp extract the [AvailArtifactManifest] from.
		 * @return
		 *   The extracted [AvailArtifactManifest].
		 * @throws AvailArtifactException
		 *   If there is any problem extracting the [AvailArtifactManifest].
		 */
		fun from (obj: JSONObject): AvailArtifactManifest
		{
			val version =
				try
				{
					obj.getNumber(
						AvailArtifactManifest::artifactVersion.name).int
				}
				catch (e: Throwable)
				{
					throw AvailArtifactException(
						"Problem accessing Avail Artifact Manifest Version.", e)
				}
			return when (version)
			{
				1 -> AvailArtifactManifestV1.fromJSON(obj)
				else ->
					throw AvailArtifactException("Invalid Avail Artifact: " +
							"Version $version is not in the valid range of " +
							"known artifact versions," +
							" [1, $CURRENT_ARTIFACT_VERSION].")
			}
		}
	}
}

