package org.availlang.artifact.environment.location

import org.availlang.artifact.environment.location.AvailLocation.LocationType

/**
 * The [AvailLocation] that is path relative to the user's home directory.
 *
 * @author Richard Arriaga
 *
 * @constructor
 * Construct a [UserHome].
 *
 * @param path
 *   The path relative to the user's home directory.
 * @param scheme
 *   The [Scheme] of the location.
 * @param locationType
 *   The [LocationType].
 */
open class UserHome constructor (
	path: String,
	scheme: Scheme,
	locationType: LocationType = LocationType.home
): AvailLocation(locationType, scheme, path)
{
	override val fullPathNoPrefix: String get() =
		"${System.getProperty("user.home")}/$path"
}
