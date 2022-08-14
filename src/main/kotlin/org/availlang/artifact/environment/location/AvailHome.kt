package org.availlang.artifact.environment.location

import org.availlang.artifact.environment.AvailEnvironment
import org.availlang.artifact.environment.location.AvailLocation.LocationType

/**
 * The [UserHome] location that is path relative to
 * [AvailEnvironment.availHome].
 *
 * @author Richard Arriaga
 *
 * @constructor
 * Construct an [AvailHome].
 *
 * @param path
 *   The path relative to the [AvailEnvironment.availHome] directory.
 * @param scheme
 *   The [Scheme] of the location.
 * @param locationType
 *   The [LocationType].
 */
open class AvailHome constructor (
	path: String,
	scheme: Scheme,
	locationType: LocationType = LocationType.availHome
): UserHome(path, scheme, locationType)
{
	override val fullPathNoPrefix: String get() =
		"${AvailEnvironment.availHome}/$path"
}