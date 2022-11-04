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
 * @param rootNameInJar
 *   If the path indicates a jar file, this is the name of the root to use
 *   within that file.
 */
class AvailRepositories constructor (
	path: String = "",
	scheme: Scheme = Scheme.FILE,
	rootNameInJar: String?
): AvailHome(path, scheme, LocationType.availRepositories, rootNameInJar)
{
	override val fullPathNoPrefix: String get() =
		"${AvailEnvironment.availHomeRepos}${File.separator}$path"

	override fun relativeLocation(
		relativePath: String,
		scheme: Scheme,
		locationType: LocationType
	): AvailLocation = AvailRepositories(
		"$path${File.separator}$relativePath", scheme, rootNameInJar)
}
