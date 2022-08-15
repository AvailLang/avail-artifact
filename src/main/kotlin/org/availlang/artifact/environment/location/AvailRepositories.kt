package org.availlang.artifact.environment.location

import org.availlang.artifact.environment.AvailEnvironment
import java.io.File

/**
 * The [AvailHome] location that is path relative to
 * [AvailEnvironment.availHomeRepos].
 *
 * @author Richard Arriaga
 *
 * @constructor
 * Construct an [AvailRepositories].
 *
 * @param path
 *   The path relative to the [AvailEnvironment.availHomeRepos] directory.
 * @param scheme
 *   The [Scheme] of the location.
 */
class AvailRepositories constructor (
	path: String = "",
	scheme: Scheme = Scheme.FILE
): AvailHome(path, scheme, LocationType.availRepositories)
{
	override val fullPathNoPrefix: String get() =
		"${AvailEnvironment.availHomeRepos}${File.pathSeparator}$path"

	override fun relativeLocation(
		relativePath: String,
		scheme: Scheme,
		locationType: LocationType
	): AvailLocation =
		AvailRepositories(
			"$path${File.pathSeparator}$relativePath", scheme)
}
