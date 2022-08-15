package org.availlang.artifact.environment.location

import java.io.File

/**
 * The [AvailLocation] that is supplied as an absolute path.
 *
 * @author Richard Arriaga
 *
 * @constructor
 * Construct an [Absolute].
 *
 * @param path
 *   The absolute path to the location.
 * @param scheme
 *   The [Scheme] of the location.
 */
@Suppress("unused")
class Absolute constructor (
	path: String,
	scheme: Scheme
): AvailLocation(LocationType.absolute, scheme, path)
{
	override val fullPathNoPrefix: String get() = path

	override fun relativeLocation(
		relativePath: String,
		scheme: Scheme,
		locationType: LocationType
	): AvailLocation =
		Absolute(
			"$path${File.pathSeparator}$relativePath", scheme)

}
