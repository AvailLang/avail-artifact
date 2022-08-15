package org.availlang.artifact.environment.location

import org.availlang.artifact.environment.AvailEnvironment
import java.io.File

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
		"${AvailEnvironment.availHomeLibs}${File.separator}$path"

	override fun relativeLocation(
		relativePath: String,
		scheme: Scheme,
		locationType: LocationType
	): AvailLocation =
		AvailLibraries("$path${File.separator}$relativePath", scheme)
}
