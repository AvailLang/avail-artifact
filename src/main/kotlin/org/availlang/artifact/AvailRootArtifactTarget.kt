package org.availlang.artifact

import org.availlang.artifact.manifest.AvailManifestRoot

/**
 * Contains information needed to locate an Avail root to be added to an
 * [AvailArtifact].
 *
 * @author Richard Arriaga
 *
 * @property rootPath
 *   The String path to the root to add.
 * @property availManifestRoot
 *   The [AvailManifestRoot] of the root to add.
 *
 * @constructor
 * Construct an [AvailRootArtifactTarget].
 *
 * @param rootPath
 *   The String path to the root to add.
 * @param availManifestRoot
 *   The [AvailManifestRoot] of the root to add.
 */
data class AvailRootArtifactTarget constructor(
	val rootPath: String,
	val availManifestRoot: AvailManifestRoot)
{
	/**
	 * The name of the root to add.
	 */
	val rootName: String get() =  availManifestRoot.name
}
