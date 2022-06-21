package org.availlang.artifact

import org.availlang.artifact.configuration.AvailApplicationConfiguration
import org.availlang.artifact.manifest.AvailArtifactManifest

/**
 * Represents a packaged Avail artifact.
 *
 * @author Richard Arriaga
 */
interface AvailArtifact
{
	/**
	 * The name of the Avail artifact.
	 */
	val name: String

	/**
	 * This [AvailArtifact]'s [AvailArtifactManifest].
	 */
	val manifest: AvailArtifactManifest

	/**
	 * This [AvailArtifact]'s [AvailApplicationConfiguration] or `null` if not
	 * an [AvailArtifactType.APPLICATION].
	 */
	val configuration: AvailApplicationConfiguration?

	/**
	 * @return
	 *   Extract and return the [AvailArtifactManifest] from this
	 *   [AvailArtifact].
	 * @throws AvailArtifactException
	 *   Should be thrown if there is trouble accessing the
	 *   [AvailArtifactManifest].
	 */
	fun extractManifest (): AvailArtifactManifest

	/**
	 * @return
	 *   Extract and return the [AvailApplicationConfiguration] from this
	 *   [AvailArtifact] if this is an [AvailArtifactType.APPLICATION]; `null`
	 *   otherwise.
	 * @throws AvailArtifactException
	 *   Should be thrown if there is trouble accessing the
	 *   [AvailApplicationConfiguration].
	 */
	fun extractConfiguration (): AvailApplicationConfiguration?

	/**
	 * Extract the map of file digests keyed by the file name.
	 *
	 * @param rootName
	 *   The name of the root to extract digest map for.
	 * @return
	 *   The list of digest map.
	 * @throws AvailArtifactException
	 *   Should be thrown if there is trouble extracting the digest.
	 */
	fun extractDigestForRoot (
		rootName: String
	): Map<String, ByteArray>

	/**
	 * Extract the list of [AvailRootFileMetadata] for all the files in the
	 * Avail Root Module for the given root module name.
	 *
	 * @param rootName
	 *   The name of the root to extract metadata for.
	 * @return
	 *   The list of extracted [AvailRootFileMetadata].
	 * @throws AvailArtifactException
	 *   Should be thrown if there is trouble accessing the roots files.
	 */
	fun extractFileMetadataForRoot (
		rootName: String
	): List<AvailRootFileMetadata>

	companion object
	{
		/**
		 * The name of the root directory in which all the contents of the
		 * [AvailArtifact] is stored.
		 */
		const val artifactRootDirectory = "avail-artifact-contents"

		/**
		 * The prefix of paths of Avail Module Root *source* file names within
		 * the artifact.
		 */
		const val availSourcesPathInArtifact = "Avail-Sources/"

		/**
		 * The path within this artifact of the digests file. The file contains
		 * a series of entries of the form ```<path>:<digest>\n```.
		 */
		const val availDigestsPathInArtifact = "Avail-Digests/all_digests.txt"
	}
}
