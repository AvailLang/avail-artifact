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
 * @param rootNameInJar
 *   If the path indicates a jar file, this is the name of the root to use
 *   within that file.
 */
open class AvailLibraries constructor (
	path: String = "",
	scheme: Scheme = Scheme.FILE,
	rootNameInJar: String?
): AvailHome(path, scheme, LocationType.availLibraries, rootNameInJar)
{
	override val fullPathNoPrefix: String get() =
		"${AvailEnvironment.availHomeLibs}${File.separator}$path"

	override fun relativeLocation(
		relativePath: String,
		scheme: Scheme,
		locationType: LocationType
	): AvailLocation = AvailLibraries(
		"$path${File.separator}$relativePath", scheme, rootNameInJar)
}
