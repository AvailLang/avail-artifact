package org.availlang.artifact.environment.location

import org.availlang.artifact.environment.AvailEnvironment

/**
 * The [AvailHome] location that is path relative to
 * [AvailEnvironment.availHomeLibs].
 *
 * @author Richard Arriaga
 *
 * @constructor
 * Construct an [AvailLibraries].
 *
 * @param path
 *   The path relative to the [AvailEnvironment.availHomeRepos] directory.
 * @param scheme
 *   The [Scheme] of the location.
 */
open class AvailLibraries constructor (
	path: String = "",
	scheme: Scheme = Scheme.FILE
): AvailHome(path, scheme, LocationType.availLibraries)
{
	override val fullPathNoPrefix: String get() =
		"${AvailEnvironment.availHomeLibs}/$path"
}