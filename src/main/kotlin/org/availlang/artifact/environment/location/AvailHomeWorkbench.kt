package org.availlang.artifact.environment.location

import org.availlang.artifact.environment.AvailEnvironment

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
): AvailHome(path, scheme, LocationType.availHomeWorkbench)
{
	override val fullPathNoPrefix: String get() =
		"${AvailEnvironment.availHomeWorkbench}/$path"
}
