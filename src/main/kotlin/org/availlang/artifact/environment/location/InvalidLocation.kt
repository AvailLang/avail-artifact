package org.availlang.artifact.environment.location

/**
 * The canonical representation of an invalid [AvailLocation].
 *
 * @author Richard Arriaga
 *
 * @property problem
 *   Text explaining the reason the location is invalid.
 *
 * @constructor
 * Construct an [InvalidLocation].
 *
 * @param path
 *   The [AvailLocation.path].
 * @param problem
 *   Text explaining the reason the location is invalid.
 */
class InvalidLocation constructor (
	path: String,
	val problem: String
): AvailLocation(LocationType.invalid, Scheme.INVALID, path)
{
	override val fullPathNoPrefix: String get() = path

	init
	{
		// TODO establish a logs file
	}
}
