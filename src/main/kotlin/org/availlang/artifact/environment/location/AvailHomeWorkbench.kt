package org.availlang.artifact.environment.location

import org.availlang.artifact.environment.AvailEnvironment
import java.io.File

/**
 * The [AvailHome] location that is path relative to
 * [AvailEnvironment.availHomeWorkbench].
 *
 * @author Richard Arriaga
 *
 * @constructor
 * Construct an [AvailHomeWorkbench].
 *
 * @param path
 *   The path relative to the [AvailEnvironment.availHomeRepos] directory.
 * @param scheme
 *   The [Scheme] of the location.
 */
class AvailHomeWorkbench constructor (
	path: String = "",
	scheme: Scheme = Scheme.FILE
): AvailHome(path, scheme, )
{
	override val fullPathNoPrefix: String get() =
		"${AvailEnvironment.availHomeWorkbench}${File.pathSeparator}$path"

	override fun relativeLocation(
		relativePath: String,
		scheme: Scheme,
		locationType: LocationType
	): AvailLocation =
		AvailHomeWorkbench(
			"$path${File.pathSeparator}$relativePath", scheme)
}
